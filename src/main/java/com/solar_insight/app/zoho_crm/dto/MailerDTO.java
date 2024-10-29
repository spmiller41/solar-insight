package com.solar_insight.app.zoho_crm.dto;

public class MailerDTO {

    private String recordId;
    private String referenceId;
    private String status;
    private String sendDate;
    private String expectedDelivery;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getExpectedDelivery() {
        return expectedDelivery;
    }

    public void setExpectedDelivery(String expectedDelivery) {
        this.expectedDelivery = expectedDelivery;
    }

    @Override
    public String toString() {
        return "ZohoMailerDataDTO{" +
                "recordId='" + recordId + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", status='" + status + '\'' +
                ", sendDate='" + sendDate + '\'' +
                ", expectedDelivery='" + expectedDelivery + '\'' +
                '}';
    }

}
