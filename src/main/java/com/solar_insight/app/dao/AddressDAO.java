package com.solar_insight.app.dao;

import com.solar_insight.app.entity.Address;
import com.solar_insight.app.rest.dto.PreliminaryDataDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AddressDAO {

    private final EntityManager entityManager;

    @Autowired
    public AddressDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public int insert(Address address) {
        entityManager.persist(address);
        return address.getId();
    }

    public Optional<Address> findByCoordinatesOrAddress(PreliminaryDataDTO data) {
        String query = "SELECT a FROM Address a WHERE " +
                "(a.latitude = :latitude AND a.longitude = :longitude) OR " +
                "(a.street = :street AND a.unit = :unit AND a.city = :city AND a.state = :state AND a.zip = :zip)";

        try {
            return Optional.of(entityManager.createQuery(query, Address.class)
                    .setParameter("latitude", data.getLatitude())
                    .setParameter("longitude", data.getLongitude())
                    .setParameter("street", data.getStreet())
                    .setParameter("unit", data.getUnit())
                    .setParameter("city", data.getCity())
                    .setParameter("state", data.getState())
                    .setParameter("zip", data.getZip())
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
