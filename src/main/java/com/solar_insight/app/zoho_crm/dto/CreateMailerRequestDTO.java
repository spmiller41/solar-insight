package com.solar_insight.app.zoho_crm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateMailerRequestDTO {

    @JsonProperty("User_Session_UUID")
    private String userSessionUUID;

    @JsonProperty("Solar_Insight_Lead_Id")
    private String solarInsightLeadId; // Update to Long

    public String getUserSessionUUID() {
        return userSessionUUID;
    }

    public void setUserSessionUUID(String userSessionUUID) {
        this.userSessionUUID = userSessionUUID;
    }

    public String getSolarInsightLeadId() { // Update to Long
        return solarInsightLeadId;
    }

    public void setSolarInsightLeadId(String solarInsightLeadId) { // Update to Long
        this.solarInsightLeadId = solarInsightLeadId;
    }

    @Override
    public String toString() {
        return "ZohoMailerRequestDTO{" +
                "userSessionUUID='" + userSessionUUID + '\'' +
                ", solarInsightLeadId='" + solarInsightLeadId + '\'' +
                '}';
    }

}
