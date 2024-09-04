package com.solar_insight.app.dao;

import com.solar_insight.app.entity.UserSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserSessionDAO {

    private final EntityManager entityManager;

    @Autowired
    public UserSessionDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(UserSession userSession) { entityManager.persist(userSession); }

    public Optional<UserSession> findBySessionUUID(String sessionUUID) {
        String query = "SELECT us UserSession us WHERE us.sessionUUID = :sessionUUID";

        try {
            return Optional.of(entityManager.createQuery(query, UserSession.class)
                    .setParameter("sessionUUID", sessionUUID)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
