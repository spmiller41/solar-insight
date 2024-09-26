package com.solar_insight.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "in_market_zips")
public class InMarketZip {

    @Id
    @Column(name = "zip")
    private String zip;

    @Column(name = "county")
    private String county;

    @Column(name = "appointment_type")
    private String appointmentType;

    public InMarketZip() {}

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    @Override
    public String toString() {
        return "InMarketZip{" +
                "zip='" + zip + '\'' +
                ", county='" + county + '\'' +
                ", appointmentType='" + appointmentType + '\'' +
                '}';
    }

}
