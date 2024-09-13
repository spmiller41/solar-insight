package com.solar_insight.app.service;

import com.solar_insight.app.dao.*;
import com.solar_insight.app.dto.ContactInfoDTO;
import com.solar_insight.app.entity.*;
import com.solar_insight.app.dto.PreliminaryDataDTO;
import com.solar_insight.app.google_solar.utility.SolarOutcomeAnalysis;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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

    private final AddressDAO addressDAO;
    private final UserSessionDAO userSessionDAO;
    private final SolarEstimateDAO solarEstimateDAO;
    private final ContactDAO contactDAO;
    private final ContactAddressDAO contactAddressDAO;

    @Autowired
    public SessionDataService(AddressDAO addressDAO, UserSessionDAO userSessionDAO,
                              SolarEstimateDAO solarEstimateDAO, ContactDAO contactDAO, ContactAddressDAO contactAddressDAO) {
        this.addressDAO = addressDAO;
        this.userSessionDAO = userSessionDAO;
        this.solarEstimateDAO = solarEstimateDAO;
        this.contactDAO = contactDAO;
        this.contactAddressDAO = contactAddressDAO;
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
                insertAllNewSessionData(data, analysis);
            } else {
                Address address = new Address(data);
                addressDAO.insert(address);

                SolarEstimate solarEstimate = new SolarEstimate(analysis, address);
                solarEstimateDAO.insert(solarEstimate);

                UserSession currentSession = optionalUserSession.get();
                currentSession.associateAddress(address);
                userSessionDAO.insert(currentSession);
            }
        } else {
            if (optionalUserSession.isEmpty()) {
                UserSession userSession = new UserSession(data, optionalAddress.get(), sessionUUID);
                userSessionDAO.insert(userSession); // Add info logging for new user session.
                updateSolarEstimate(analysis, userSession);
            } else {
                int sessionAddressId = optionalUserSession.get().getAddressId();
                int newAddressId = optionalAddress.get().getId();
                if (sessionAddressId == newAddressId) updateSolarEstimate(analysis, optionalUserSession.get());
                else manageSessionAddressAssociation(optionalUserSession.get(), optionalAddress.get(), analysis);
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
     * <p>
     * If the contact does not exist:
     * - Insert the new contact and associate it with the address.
     * <p>
     * Returns an Optional<String>:
     * - If a new contact or contact-address association is created, returns the ID of the contact-address association.
     * - If no new contact or association is created, returns Optional.empty().
     * <p>
     * Transactional: Rolls back if any operation fails.
     *
     * @param contactInfo  Contact information provided by the user.
     * @return Optional<String> - ID of the new contact-address association if a new lead is generated, or Optional.empty() if no changes are made.
     */
    @Transactional
    public Optional<ContactAddress> processUserSessionData(ContactInfoDTO contactInfo) {
        String sessionUUID = contactInfo.getSessionUUID();

        Optional<UserSession> optionalUserSession = userSessionDAO.findBySessionUUID(sessionUUID);
        if (optionalUserSession.isEmpty()) {
            // Add error logging here before return
            return Optional.empty();
        }

        Optional<Address> optionalAddress = addressDAO.findById(optionalUserSession.get().getAddressId());
        if (optionalAddress.isEmpty()) {
            // Add error logging here before return
            return Optional.empty();
        }

        Optional<Contact> optionalContact = contactDAO.findByEmail(contactInfo.getEmail());
        if (optionalContact.isEmpty()) {
            Contact contact = new Contact(contactInfo);
            contactDAO.insert(contact);
            // Add info logging for new contact here.
            ContactAddress contactAddress = new ContactAddress(contact, optionalAddress.get());
            contactAddressDAO.insert(contactAddress);
            return Optional.of(contactAddress);
            // Add info logging for new contact_address association here
        } else {
            Optional<ContactAddress> optionalContactAddress =
                    contactAddressDAO.findByAddressAndContact(optionalAddress.get().getId(), optionalContact.get().getId());
            if (optionalContactAddress.isEmpty()) {
                ContactAddress contactAddress = new ContactAddress(optionalContact.get(), optionalAddress.get());
                contactAddressDAO.insert(contactAddress);
                return Optional.of(contactAddress);
                // Add info logging for new contact_address association here
            } else {
                // Add info logging here to notify system user has returned
                System.out.println("Existing User Returning...");
            }
        }

        return Optional.empty();
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
     */
    private void manageSessionAddressAssociation(UserSession currentSession, Address newAddress, SolarOutcomeAnalysis analysis) {
        Optional<Address> optAddressTemp = addressDAO.findById(currentSession.getAddressId());

        // Update user session and solar estimate
        currentSession.associateAddress(newAddress);
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
            solarEstimate = solarEstimateDAO.update(solarEstimate);
            // Add info logging for solar estimate update here
        } else {
            // Add more organized logging for this error
            System.err.println("Could not find estimate...");
        }
    }

}
