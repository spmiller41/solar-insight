package com.solar_insight.app.dao;

import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.UserSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ContactAddressDAO {

    private final EntityManager entityManager;

    @Autowired
    public ContactAddressDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(ContactAddress contactAddress) {
        entityManager.persist(contactAddress);
    }

    public ContactAddress update(ContactAddress contactAddress) { return entityManager.merge(contactAddress); }

    public Optional<ContactAddress> findByAddressAndContact(int addressId, int contactId) {
        String query = "SELECT ca FROM ContactAddress ca WHERE ca.addressId = :addressId AND ca.contactId = :contactId";

        try {
            return Optional.of(entityManager.createQuery(query, ContactAddress.class)
                    .setParameter("addressId", addressId)
                    .setParameter("contactId", contactId)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public Optional<ContactAddress> findByUserSession(UserSession userSession) {
        String query = "SELECT ca FROM ContactAddress ca WHERE ca.lastUserSessionId = :userSessionId";

        try {
            return Optional.of(entityManager.createQuery(query, ContactAddress.class)
                    .setParameter("userSessionId", userSession.getId())
                    .getSingleResult());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public Optional<ContactAddress> findById(int contactAddressId) {
        String query = "SELECT ca FROM ContactAddress ca WHERE ca.id = :contactAddressId";

        try {
            return Optional.of(entityManager.createQuery(query, ContactAddress.class)
                    .setParameter("contactAddressId", contactAddressId)
                    .getSingleResult());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

}
