package com.solar_insight.app.dao;

import com.solar_insight.app.entity.ContactAddress;
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

}
