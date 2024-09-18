package com.solar_insight.app.dao;

import com.solar_insight.app.entity.BookedConsultation;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BookedConsultationDAO {

    private final EntityManager entityManager;

    @Autowired
    public BookedConsultationDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(BookedConsultation bookedConsultation) { entityManager.persist(bookedConsultation); }

}
