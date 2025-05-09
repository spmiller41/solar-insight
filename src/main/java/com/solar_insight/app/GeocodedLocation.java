package com.solar_insight.app;

public record GeocodedLocation(double latitude, double longitude) {

    @Override
    public String toString() {
        return "GeocodedLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
