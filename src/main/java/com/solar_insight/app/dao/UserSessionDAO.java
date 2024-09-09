package com.solar_insight.app.dao;

import com.solar_insight.app.entity.UserSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public class UserSessionDAO {

    private final EntityManager entityManager;

    @Autowired
    public UserSessionDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(UserSession userSession) { entityManager.persist(userSession); }

    public UserSession update(UserSession userSession) { return entityManager.merge(userSession); }

    public Optional<UserSession> findBySessionUUID(String sessionUUID) {
        String query = "SELECT us FROM UserSession us WHERE us.sessionUUID = :sessionUUID";

        try {
            return Optional.of(entityManager.createQuery(query, UserSession.class)
                    .setParameter("sessionUUID", sessionUUID)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public Optional<List<UserSession>> getUserSessionsByAddressId(int addressId) {
        String query = "SELECT us FROM UserSession us WHERE us.addressId = :addressId";

        try {
            List<UserSession> userSessions = entityManager.createQuery(query, UserSession.class)
                    .setParameter("addressId", addressId)
                    .getResultList();

            if (userSessions.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(userSessions);
            }
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
