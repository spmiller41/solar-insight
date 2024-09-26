package com.solar_insight.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InMarketZipDTO {

    @JsonProperty("zip")
    private String zip;

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String toString() {
        return "InMarketZipDTO{" +
                "zip='" + zip + '\'' +
                '}';
    }

}
