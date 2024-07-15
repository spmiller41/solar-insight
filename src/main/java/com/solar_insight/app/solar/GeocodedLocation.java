package com.solar_insight.app.solar;

public record GeocodedLocation(double latitude, double longitude) {

    @Override
    public String toString() {
        return "GeocodedLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
