package com.solar_insight.app.dao;

import com.solar_insight.app.entity.PostcardMailer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostcardMailerDAO {

    private final EntityManager entityManager;

    @Autowired
    public PostcardMailerDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(PostcardMailer mailer) { entityManager.persist(mailer); }

    public PostcardMailer update(PostcardMailer mailer) { return entityManager.merge(mailer); }

    public Optional<PostcardMailer> findMailerByReferenceId(String referenceId) {
        String query = "SELECT pcm FROM PostcardMailer pcm WHERE pcm.referenceId = :referenceId";

        try {
            return Optional.of(entityManager.createQuery(query, PostcardMailer.class)
                    .setParameter("referenceId", referenceId)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public Optional<List<PostcardMailer>> getAllMailerByAddressId(int addressId) {
        String query = "SELECT pcm FROM PostcardMailer pcm WHERE pcm.addressId = :addressId";

        try {
            return Optional.of(entityManager.createQuery(query, PostcardMailer.class)
                    .setParameter("addressId", addressId)
                    .getResultList());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
