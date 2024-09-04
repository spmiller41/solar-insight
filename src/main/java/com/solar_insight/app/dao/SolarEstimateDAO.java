package com.solar_insight.app.dao;

import com.solar_insight.app.entity.SolarEstimate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SolarEstimateDAO {

    private final EntityManager entityManager;

    @Autowired
    public SolarEstimateDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(SolarEstimate solarEstimate) { entityManager.persist(solarEstimate); }

    public Optional<SolarEstimate> findByAddressId(int addressId) {
        String query = "SELECT se FROM SolarEstimate se WHERE se.addressId = :addressId";

        try {
            return Optional.of(entityManager.createQuery(query, SolarEstimate.class)
                    .setParameter("addressId", addressId)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public SolarEstimate update(SolarEstimate solarEstimate) {
        return entityManager.merge(solarEstimate);
    }

}
