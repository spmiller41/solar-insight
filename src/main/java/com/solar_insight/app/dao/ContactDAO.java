package com.solar_insight.app.dao;

import com.solar_insight.app.entity.Contact;
import com.solar_insight.app.dto.ContactInfoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ContactDAO {

    private final EntityManager entityManager;

    @Autowired
    public ContactDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public int insert(Contact contact) {
        entityManager.persist(contact);
        return contact.getId();
    }

    public Optional<Contact> findByEmail(String email) {
        String query = "SELECT c FROM Contact c WHERE c.email = :email";
        try {
            return Optional.of(entityManager.createQuery(query, Contact.class)
                    .setParameter("email", email)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
