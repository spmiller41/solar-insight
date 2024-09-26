package com.solar_insight.app.dto;

public class MarketCheckResponseDTO {

    private String status;
    private String message;
    private String county;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @Override
    public String toString() {
        return "MarketCheckResponseDTO{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", county='" + county + '\'' +
                '}';
    }

}
