package com.solar_insight.app.dao;

import com.solar_insight.app.entity.UserSession;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserSessionDAO {

    private final EntityManager entityManager;

    @Autowired
    public UserSessionDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public int insert(UserSession userSession) {
        entityManager.persist(userSession);
        return userSession.getId();
    }

}
