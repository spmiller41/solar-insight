package com.solar_insight.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSessionDTO {

    @JsonProperty("sessionUUID")
    private String sessionUUID;

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setSessionUUID(String sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    @Override
    public String toString() {
        return "UserSessionDTO{" +
                "sessionUUID='" + sessionUUID + '\'' +
                '}';
    }

}
