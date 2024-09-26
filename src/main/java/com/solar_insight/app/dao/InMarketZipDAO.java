package com.solar_insight.app.dao;

import com.solar_insight.app.dto.InMarketZipDTO;
import com.solar_insight.app.entity.InMarketZip;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InMarketZipDAO {

    private final EntityManager entityManager;

    @Autowired
    public InMarketZipDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<InMarketZip> findByZip(InMarketZipDTO zipDTO) {
        String query = "SELECT imz FROM InMarketZip imz WHERE imz.zip = :zip";

        try {
            return Optional.of(entityManager.createQuery(query, InMarketZip.class)
                    .setParameter("zip", zipDTO.getZip())
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
