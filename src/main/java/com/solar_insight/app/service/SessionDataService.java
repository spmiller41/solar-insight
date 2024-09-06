package com.solar_insight.app.service;

import com.solar_insight.app.dao.*;
import com.solar_insight.app.dto.ContactInfoDTO;
import com.solar_insight.app.entity.*;
import com.solar_insight.app.dto.PreliminaryDataDTO;
import com.solar_insight.app.solar.utility.SolarOutcomeAnalysis;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * Saves a new user session, address, and solar estimate based on preliminary data.
     * <p>
     * If the address exists:
     * - Create a new user session.
     * - Update or insert a solar estimate for the address.
     * <p>
     * If the address is new:
     * - Insert the address, create a user session, and add a new solar estimate.
     * <p>
     * Transactional: Rolls back if any operation fails.
     *
     * @param data        User-provided preliminary data.
     * @param analysis    Solar analysis data for the estimate.
     * @param sessionUUID Unique ID for the current session.
     */
    @Transactional
    public void processUserSessionData(PreliminaryDataDTO data, SolarOutcomeAnalysis analysis, String sessionUUID) {
        Optional<Address> optionalAddress = addressDAO.findByCoordinatesOrAddress(data);
        if (optionalAddress.isEmpty()) {
            Address address = new Address(data);
            addressDAO.insert(address);
            // Add info logging for new address here

            UserSession userSession = new UserSession(data, address, sessionUUID);
            userSessionDAO.insert(userSession);
            // Add info logging for new user session here

            SolarEstimate solarEstimate = new SolarEstimate(analysis, address);
            solarEstimateDAO.insert(solarEstimate);
            // Add info logging for new solar estimate here
            // This is a potential trigger for compiling address/estimate and sending it out
        } else {
            Address address = optionalAddress.get();

            UserSession userSession = new UserSession(data, address, sessionUUID);
            userSessionDAO.insert(userSession);
            // Add info logging for new user session here

            Optional<SolarEstimate> optionalSolarEstimate = solarEstimateDAO.findByAddressId(address.getId());
            if (optionalSolarEstimate.isPresent()) {
                SolarEstimate solarEstimate = optionalSolarEstimate.get();
                solarEstimate.refreshSolarEstimate(analysis);
                solarEstimate = solarEstimateDAO.update(solarEstimate);
                // Add info logging for solar estimate update here
                // This is a potential trigger for sending out the updated estimate
            } else {
                SolarEstimate solarEstimate = new SolarEstimate(analysis, address);
                solarEstimateDAO.insert(solarEstimate);
                // Add info logging for new solar estimate here
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
     * Transactional: Rolls back if any operation fails.
     *
     * @param contactInfo  Contact information provided by the user.
     */
    @Transactional
    public void processUserSessionData(ContactInfoDTO contactInfo) {
        String sessionUUID = contactInfo.getSessionUUID();
        Optional<UserSession> optionalUserSession = userSessionDAO.findBySessionUUID(sessionUUID);
        if (optionalUserSession.isEmpty()) {
            // Add error logging here before return
            return;
        }

        Optional<Address> optionalAddress = addressDAO.findById(optionalUserSession.get().getAddressId());
        if (optionalAddress.isEmpty()) {
            // Add error logging here before return
            return;
        }

        Optional<Contact> optionalContact = contactDAO.findByEmail(contactInfo.getEmail());
        if (optionalContact.isEmpty()) {
            Contact contact = new Contact(contactInfo);
            contactDAO.insert(contact);
            // Add info logging for new contact here.
            ContactAddress contactAddress = new ContactAddress(contact, optionalAddress.get());
            contactAddressDAO.insert(contactAddress);
            // Add info logging for new contact_address association here
            // Note: This is a potential trigger for compiling the full lead information and sending out
        } else {
            Optional<ContactAddress> optionalContactAddress =
                    contactAddressDAO.findByAddressAndContact(optionalAddress.get().getId(), optionalContact.get().getId());
            if (optionalContactAddress.isEmpty()) {
                ContactAddress contactAddress = new ContactAddress(optionalContact.get(), optionalAddress.get());
                contactAddressDAO.insert(contactAddress);
                // Add info logging for new contact_address association here
                // Note: This is another potential trigger for compiling the full lead information and sending out
            } else {
                // Add info logging here to notify system user has returned
                System.out.println("Existing User Returning...");
            }
        }
    }

}
