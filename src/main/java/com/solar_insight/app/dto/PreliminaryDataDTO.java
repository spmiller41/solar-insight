package com.solar_insight.app.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreliminaryDataDTO {

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("clientIp")
    private String clientIp;

    @JsonProperty("avgMonthlyEnergyBill")
    private int avgMonthlyEnergyBill;

    @JsonProperty("street")
    private String street;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("zip")
    private String zip;

    public String getStreet() { return street; }

    public void setStreet(String street) { this.street = street; }

    public String getUnit() { return unit; }

    public void setUnit(String unit) { this.unit = unit; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }

    public void setZip(String zip) { this.zip = zip; }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getClientIp() { return clientIp; }

    public void setClientIp(String clientIP) { this.clientIp = clientIP; }

    public int getAvgMonthlyEnergyBill() { return avgMonthlyEnergyBill; }

    public void setAvgMonthlyEnergyBill(int avgMonthlyEnergyBill) { this.avgMonthlyEnergyBill = avgMonthlyEnergyBill; }

    @Override
    public String toString() {
        return "PreliminaryDataDTO{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", clientIp='" + clientIp + '\'' +
                ", avgMonthlyEnergyBill=" + avgMonthlyEnergyBill +
                ", street='" + street + '\'' +
                ", unit='" + unit + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                '}';
    }

}
