package com.solar_insight.app.dao;

import com.solar_insight.app.entity.MailerBooking;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MailerBookingDAO {

    private final EntityManager entityManager;

    @Autowired
    public MailerBookingDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(MailerBooking mailerBooking) {
        entityManager.persist(mailerBooking);
    }

}
