package com.solar_insight.app.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreliminaryDataDTO {

    @JsonProperty("address")
    private String address;

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("clientIp")
    private String clientIp;

    @JsonProperty("avgMonthlyEnergyBill")
    private String avgMonthlyEnergyBill;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIP) {
        this.clientIp = clientIP;
    }

    public String getAvgMonthlyEnergyBill() {
        return avgMonthlyEnergyBill;
    }

    public void setAvgMonthlyEnergyBill(String avgMonthlyEnergyBill) {
        this.avgMonthlyEnergyBill = avgMonthlyEnergyBill;
    }

    @Override
    public String toString() {
        return "PreliminaryDataDTO{" +
                "address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", clientIp='" + clientIp + '\'' +
                ", avgMonthlyEnergyBill='" + avgMonthlyEnergyBill + '\'' +
                '}';
    }

}
