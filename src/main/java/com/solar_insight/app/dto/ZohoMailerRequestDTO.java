package com.solar_insight.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ZohoMailerRequestDTO {

    @JsonProperty("User_Session_UUID")
    private String userSessionUUID;

    @JsonProperty("Solar_Insight_Lead_Id")
    private String solarInsightLeadId; // Update to Long

    @JsonProperty("Mailers")
    private List<Mailer> mailers;

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

    public List<Mailer> getMailers() {
        return mailers;
    }

    public void setMailers(List<Mailer> mailers) {
        this.mailers = mailers;
    }

    @Override
    public String toString() {
        return "ZohoMailerRequestDTO{" +
                "userSessionUUID='" + userSessionUUID + '\'' +
                ", solarInsightLeadId='" + solarInsightLeadId + '\'' +
                ", mailers=" + mailers +
                '}';
    }

    // Nested class for individual mailer records
    public static class Mailer {

        @JsonProperty("Status")
        private String status;

        @JsonProperty("Created_Time")
        private String createdTime;

        @JsonProperty("Send_Date")
        private String sendDate;

        @JsonProperty("Reference_Id")
        private String referenceId;

        @JsonProperty("Expected_Delivery")
        private String expectedDelivery;

        @JsonProperty("id")
        private String id;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(String createdTime) {
            this.createdTime = createdTime;
        }

        public String getSendDate() {
            return sendDate;
        }

        public void setSendDate(String sendDate) {
            this.sendDate = sendDate;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public String getExpectedDelivery() {
            return expectedDelivery;
        }

        public void setExpectedDelivery(String expectedDelivery) {
            this.expectedDelivery = expectedDelivery;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Mailer{" +
                    "status='" + status + '\'' +
                    ", createdTime=" + createdTime +
                    ", sendDate=" + sendDate +
                    ", referenceId='" + referenceId + '\'' +
                    ", expectedDelivery=" + expectedDelivery +
                    ", id='" + id + '\'' +
                    '}';
        }
    }
}
