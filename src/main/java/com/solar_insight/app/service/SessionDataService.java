package com.solar_insight.app.service;

import com.solar_insight.app.dao.*;
import com.solar_insight.app.dto.BookingDTO;
import com.solar_insight.app.dto.ContactInfoDTO;
import com.solar_insight.app.entity.*;
import com.solar_insight.app.dto.PreliminaryDataDTO;
import com.solar_insight.app.google_solar.utility.SolarOutcomeAnalysis;
import com.solar_insight.app.logs.SessionDataLogger;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SessionDataService handles database transactions for user sessions in the lead generation flow.
 * It processes data input from the frontend, including utility bills, addresses, and contact
 * information, and manages the insertion and updating of addresses, solar estimates, and contacts.
 * <p>
 * This service maintains transactional integrity, ensuring any database failure results in a rollback,
 * and enforces a clear separation of concerns by focusing solely on persistence logic.
 */
@Service
public class SessionDataService {

    private static final Logger logger = LoggerFactory.getLogger(SessionDataService.class);

    private final AddressDAO addressDAO;
    private final UserSessionDAO userSessionDAO;
    private final SolarEstimateDAO solarEstimateDAO;
    private final ContactDAO contactDAO;
    private final ContactAddressDAO contactAddressDAO;
    private final BookedConsultationDAO bookedConsultationDAO;
    private final MailerBookingDAO mailerBookingDAO;

    @Autowired
    public SessionDataService(AddressDAO addressDAO,
                              UserSessionDAO userSessionDAO,
                              SolarEstimateDAO solarEstimateDAO,
                              ContactDAO contactDAO,
                              ContactAddressDAO contactAddressDAO,
                              BookedConsultationDAO bookedConsultationDAO,
                              MailerBookingDAO mailerBookingDAO) {

        this.addressDAO = addressDAO;
        this.userSessionDAO = userSessionDAO;
        this.solarEstimateDAO = solarEstimateDAO;
        this.contactDAO = contactDAO;
        this.contactAddressDAO = contactAddressDAO;
        this.bookedConsultationDAO = bookedConsultationDAO;
        this.mailerBookingDAO = mailerBookingDAO;
    }




    /**
     * Processes a user session by saving a new address, solar estimate, or updating the related data.
     * <p>
     * If the address does not exist:
     * - If the user session does not exist:
     *   - Insert the address, create a new user session, and add a new solar estimate.
     * - If the user session exists:
     *   - Update the address associated with the user session and update the solar estimate.
     * <p>
     * If the address exists:
     * - If the user session does not exist:
     *   - Create a new user session, associating the existing address, and update the solar estimate.
     * - If the user session exists:
     *   - Verify whether the existing address is associated with the current user session.
     *   - If no association is made, the current existing address will be associated with the current user session.
     *   - If the last address associated with the current user session is not associated with any other user session,
     *     it will be removed, along with its associated solar estimate.
     * <p>
     * Transactional: Rolls back if any operation fails.
     *
     * @param data        User-provided preliminary data.
     * @param analysis    Solar analysis data for the estimate.
     */
    @Transactional
    public void processUserSessionData(PreliminaryDataDTO data, SolarOutcomeAnalysis analysis) {
        String sessionUUID = data.getSessionUUID();

        Optional<Address> optionalAddress = addressDAO.findByCoordinatesOrAddress(data);
        Optional<UserSession> optionalUserSession = userSessionDAO.findBySessionUUID(sessionUUID);

        if (optionalAddress.isEmpty()) {
            if (optionalUserSession.isEmpty()) {
                SessionDataLogger.logNewUserNotification(logger);
                insertAllNewSessionData(data, analysis);
            } else {
                SessionDataLogger.logLiveSessionNewDataNotification(logger, optionalUserSession.get());
                Address address = new Address(data);
                addressDAO.insert(address);

                SolarEstimate solarEstimate = new SolarEstimate(analysis, address);
                solarEstimateDAO.insert(solarEstimate);

                manageSessionAddressAssociation(optionalUserSession.get(), address, analysis);
            }
        } else {
            if (optionalUserSession.isEmpty()) {
                SessionDataLogger.logNewSessionExistingDataNotification(logger, optionalAddress.get());
                UserSession userSession = new UserSession(data, optionalAddress.get(), sessionUUID);
                userSessionDAO.insert(userSession); // Add info logging for new user session.
                updateSolarEstimate(analysis, userSession);
            } else {
                SessionDataLogger.logLiveSessionAndAddressNotification(logger, optionalUserSession.get(), optionalAddress.get());
                int sessionAddressId = optionalUserSession.get().getAddressId();
                int newAddressId = optionalAddress.get().getId();
                if (sessionAddressId == newAddressId) {
                    SessionDataLogger.logLiveSessionAddressResult(logger, optionalUserSession.get(), false);
                    updateSolarEstimate(analysis, optionalUserSession.get());
                } else {
                    SessionDataLogger.logLiveSessionAddressResult(logger, optionalUserSession.get(), true);
                    manageSessionAddressAssociation(optionalUserSession.get(), optionalAddress.get(), analysis);
                }
            }
        }
    }




    /**
     * Processes a user session by associating the provided contact information with an address.
     * <p>
     * If the user session is found:
     * - Retrieve the associated address.
     * - Check if the contact already exists by email.
     * <p>
     * If the contact exists:
     * - Check if the contact is already associated with the address in the junction table.
     * - If not associated, create the association.
     * - If already associated, update the contact-address association with the new session UUID.
     * <p>
     * If the contact does not exist:
     * - Insert the new contact and associate it with the address and user session.
     * <p>
     * Returns an Optional<ContactAddress>:
     * - If a new contact or contact-address association is created, returns the ContactAddress entity.
     * - If the contact already exists and is updated with the new session UUID, logs the update and returns Optional.empty().
     * <p>
     * Transactional: Rolls back if any operation fails.
     * <p>
     * This method ensures that returning users have their contact-address association refreshed with the latest session UUID.
     *
     * @param contactInfo  Contact information provided by the user.
     * @return Optional<ContactAddress> - The new or updated contact-address association if a new lead is generated or updated,
     *                                   or Optional.empty() if no changes are made.
     */
    @Transactional
    public Optional<ContactAddress> processUserSessionData(ContactInfoDTO contactInfo) {
        String sessionUUID = contactInfo.getSessionUUID();

        Optional<UserSession> optUserSession = userSessionDAO.findBySessionUUID(sessionUUID);
        if (optUserSession.isEmpty()) {
            SessionDataLogger.logMissingUserSessionErr(logger, contactInfo);
            return Optional.empty();
        }

        Optional<Address> optAddress = addressDAO.findById(optUserSession.get().getAddressId());
        if (optAddress.isEmpty()) {
            SessionDataLogger.logMissingAddressErr(logger, optUserSession.get());
            return Optional.empty();
        }

        Optional<Contact> optContact = contactDAO.findByEmail(contactInfo.getEmail());
        if (optContact.isEmpty()) {
            SessionDataLogger.logNewContactInfoNotification(logger, contactInfo);
            Contact contact = new Contact(contactInfo);
            contactDAO.insert(contact);

            SessionDataLogger.logGeneratingLeadNotification(logger, contact, optAddress.get());
            ContactAddress contactAddress = new ContactAddress(contact, optAddress.get(), optUserSession.get());
            contactAddressDAO.insert(contactAddress);
            return Optional.of(contactAddress);
        } else {
            Optional<ContactAddress> optContactAddress =
                    contactAddressDAO.findByAddressAndContact(optAddress.get().getId(), optContact.get().getId());
            if (optContactAddress.isEmpty()) {
                SessionDataLogger.logExistingContactNewAddressNotification(logger, optContact.get(), optAddress.get());
                ContactAddress contactAddress = new ContactAddress(optContact.get(), optAddress.get(), optUserSession.get());
                contactAddressDAO.insert(contactAddress);
                return Optional.of(contactAddress);
            } else {
                SessionDataLogger.logExistingLeadNotification(logger, optContactAddress.get());

                ContactAddress contactAddress = optContactAddress.get();
                contactAddress.refreshLastUserSession(optUserSession.get());
                SessionDataLogger.logUserSessionUpdateNotification(logger, contactAddress);
            }
        }

        return Optional.empty();
    }



    @Transactional
    public Optional<BookedConsultation> processUserSessionData(BookingDTO bookingData) {
        Optional<UserSession> optUserSession = userSessionDAO.findBySessionUUID(bookingData.getSessionUUID());
        if (optUserSession.isEmpty()) {
            System.err.println("Could not locate User Session when fetching " +
                    "data during a booked consultation. Booking Data: " + bookingData);
            return Optional.empty();
        }

        Optional<ContactAddress> optContactAddress = contactAddressDAO.findByUserSession(optUserSession.get());
        if (optContactAddress.isEmpty()) {
            System.err.println("Could not locate ContactAddress (Generated Lead) when fetching " +
                    "data during a booked consultation. User Session: " + optUserSession.get());
            return Optional.empty();
        }

        BookedConsultation bookedConsultation = new BookedConsultation(bookingData, optContactAddress.get());
        try {
            bookedConsultationDAO.insert(bookedConsultation);
            return Optional.of(bookedConsultation);
        } catch (Exception ex) {
            System.out.println("Issue inserting booking: " + ex.getMessage());
            return Optional.empty();
        }
    }




    /*
     * Handles the insertion of a completely new address, user session, and solar estimate.
     *
     * @param data        User-provided preliminary data.
     * @param analysis    Solar analysis data for the estimate.
     */
    private void insertAllNewSessionData(PreliminaryDataDTO data, SolarOutcomeAnalysis analysis) {
        String sessionUUID = data.getSessionUUID();;

        Address address = new Address(data);
        addressDAO.insert(address);
        // Add info logging for new address

        UserSession userSession = new UserSession(data, address, sessionUUID);
        userSessionDAO.insert(userSession);
        // Add info logging for new user session

        SolarEstimate solarEstimate = new SolarEstimate(analysis, address);
        solarEstimateDAO.insert(solarEstimate);
        // Add info logging for new solar estimate.
    }




    /*
     * Manages re-association of a user session with a new address and handles cleanup of rogue addresses.
     * In addition, updates the solar estimate associated with the address.
     *
     * This method functions by storing the original associated address 'optAddressTemp'
     * and removing its address after associating the new address with the current user session.
     * In the context of how this method should be used, the 'original' address was most likely a
     * mistake on the users' part - entering an incorrect address.
     * The user session is current, so if this address doesn't exist in another user session, we may remove it.
     *
     * @param currentSession Existing user session.
     * @param newAddress     Address to associate with the session.
     * @param analysis       The current calculated solar outcome.
     */
    private void manageSessionAddressAssociation(UserSession currentSession, Address newAddress, SolarOutcomeAnalysis analysis) {
        Optional<Address> optAddressTemp = addressDAO.findById(currentSession.getAddressId());

        // Update user session and solar estimate
        currentSession.associateAddress(newAddress);
        currentSession = userSessionDAO.update(currentSession);
        updateSolarEstimate(analysis, currentSession);

        optAddressTemp.ifPresent(tempAddress -> {
            int tempAddressId = tempAddress.getId();

            // Get all user sessions associated with previous address. Remove if no associations (rogue address).
            Optional<List<UserSession>> optSessionList = userSessionDAO.getUserSessionsByAddressId(tempAddressId);

            if (optSessionList.isEmpty()) {
                addressDAO.remove(tempAddressId);
                // Add more organized info logging here...
                System.out.println("Address/Estimate removed for address: " + tempAddress);
            }
        });
    }




    /*
     * Updates or inserts a solar estimate based on the user's session.
     *
     * @param analysis    Solar analysis data for the estimate.
     * @param userSession User session containing the address.
     */
    private void updateSolarEstimate(SolarOutcomeAnalysis analysis, UserSession userSession) {
        Optional<SolarEstimate> optionalSolarEstimate = solarEstimateDAO.findByAddressId(userSession.getAddressId());
        if (optionalSolarEstimate.isPresent()) {
            SolarEstimate solarEstimate = optionalSolarEstimate.get();
            solarEstimate.refreshSolarEstimate(analysis);
            solarEstimate.setAddressId(userSession.getAddressId());
            solarEstimate = solarEstimateDAO.update(solarEstimate);
            // Add info logging for solar estimate update here
        } else {
            // Add more organized logging for this error
            System.err.println("Could not find estimate...");
        }
    }


    @Transactional
    public MailerBooking insertMailerBooking(MailerBooking mailerBooking) {
        mailerBookingDAO.insert(mailerBooking);
        return mailerBooking;
    }

}
