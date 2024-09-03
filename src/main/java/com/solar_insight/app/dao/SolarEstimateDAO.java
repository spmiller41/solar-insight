package com.solar_insight.app.dao;

import com.solar_insight.app.entity.SolarEstimate;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SolarEstimateDAO {

    private final EntityManager entityManager;

    @Autowired
    public SolarEstimateDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public int insert(SolarEstimate solarEstimate) {
        entityManager.persist(solarEstimate);
        return solarEstimate.getId();
    }

    public SolarEstimate update(SolarEstimate solarEstimate) {
        return entityManager.merge(solarEstimate);
    }

}
