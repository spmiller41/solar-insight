package com.solar_insight.app.entity;

import com.solar_insight.app.dto.PreliminaryDataDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "street")
    private String street;

    @Column(name = "unit")
    private String unit;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip")
    private String zip;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "zoho_solar_insight_lead_id")
    private String zohoSolarInsightLeadId;

    public Address(PreliminaryDataDTO data) {
        this.street = data.getStreet();
        this.unit = data.getUnit();
        this.city = data.getCity();
        this.state = data.getState();
        this.zip = data.getZip();
        this.latitude = data.getLatitude();
        this.longitude = data.getLongitude();
        this.created_at = LocalDateTime.now();
    }

    public Address() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public String getZohoSolarInsightLeadId() { return zohoSolarInsightLeadId; }

    public void setZohoSolarInsightLeadId(String zohoSolarInsightLeadId) { this.zohoSolarInsightLeadId = zohoSolarInsightLeadId; }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", street='" + street + '\'' +
                ", unit='" + unit + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", created_at=" + created_at +
                ", zohoSolarInsightLeadId='" + zohoSolarInsightLeadId + '\'' +
                '}';
    }

}
