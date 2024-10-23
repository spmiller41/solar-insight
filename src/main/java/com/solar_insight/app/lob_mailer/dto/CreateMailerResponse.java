package com.solar_insight.app.lob_mailer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CreateMailerResponse {

    @JsonProperty("id")
    private String referenceId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @JsonProperty("date_modified")
    private ZonedDateTime dateModified;

    @JsonProperty("send_date")
    private ZonedDateTime sendDate;

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

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public LocalDateTime getModifiedTimeInNewYork() {
        return dateModified.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
    }

    public void setDateModified(ZonedDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public LocalDateTime getSendDateInNewYork() {
        return sendDate.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
    }

    public void setSendDate(ZonedDateTime sendDate) {
        this.sendDate = sendDate;
    }

    @Override
    public String toString() {
        return "CreateMailerResponse{" +
                "referenceId='" + referenceId + '\'' +
                ", status='" + status + '\'' +
                ", expectedDeliveryDate=" + expectedDeliveryDate +
                ", dateModified=" + getModifiedTimeInNewYork() +
                ", sendDate=" + getSendDateInNewYork() +
                '}';
    }

}
