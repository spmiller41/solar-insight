package com.solar_insight.app.dao;

import com.solar_insight.app.entity.Address;
import com.solar_insight.app.dto.PreliminaryDataDTO;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.entity.UserSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AddressDAO {

    private final EntityManager entityManager;

    @Autowired
    public AddressDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insert(Address address) { entityManager.persist(address); }

    public Address update(Address address) {
        return entityManager.merge(address);
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

    public Optional<Address> findById(int addressId) {
        String query = "SELECT a FROM Address a WHERE a.id = :addressId";

        try {
            return Optional.of(entityManager.createQuery(query, Address.class)
                    .setParameter("addressId", addressId)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public void remove(int addressId) {
        String removeEstimateQuery = "DELETE FROM SolarEstimate se WHERE se.addressId = :addressId";
        String removeAddressQuery = "DELETE FROM Address a WHERE a.id = :addressId";

        try {
            // Remove solar estimates first
            entityManager.createQuery(removeEstimateQuery)
                    .setParameter("addressId", addressId)
                    .executeUpdate();
            // Add info logging here for estimate removal

            // Remove the address
            entityManager.createQuery(removeAddressQuery)
                    .setParameter("addressId", addressId)
                    .executeUpdate();
            // Add info logging here for address removal
        } catch (Exception ex) {
            // Add more organized error logging here
            System.err.println("Error during deletion: " + ex.getMessage());
        }
    }

}
