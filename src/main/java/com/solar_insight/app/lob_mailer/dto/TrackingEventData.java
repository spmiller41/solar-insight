package com.solar_insight.app.lob_mailer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TrackingEventData {

    @JsonProperty("reference_id")
    private String referenceId;

    private String event;

    private LocalDate expectedDeliveryDate;

    private ZonedDateTime dateModified;

    @JsonProperty("event_type")
    public void setEventType(EventType eventType) { this.event = eventType.getId(); }

    @JsonProperty("body")
    public void setBodyData(Body body) {
        this.expectedDeliveryDate = body.getExpectedDeliveryDate();
        this.dateModified = body.getDateModified();
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getModifiedTimeInNewYork() {
        if (dateModified != null) {
            return dateModified.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
        }
        // If dateModified is null, return the current LocalDateTime in New York timezone
        return ZonedDateTime.now(ZoneId.of("America/New_York")).toLocalDateTime();
    }

    public String getReferenceId() { return referenceId; }

    public String getEvent() { return event; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }

    @Override
    public String toString() {
        return "TrackingEventDTO{" +
                "referenceId='" + referenceId + '\'' +
                ", event='" + event + '\'' +
                ", expectedDeliveryDate=" + expectedDeliveryDate +
                ", dateModified=" + getModifiedTimeInNewYork() +
                '}';
    }




    // Nested class for event_type
    public static class EventType {
        @JsonProperty("id")
        private String id;

        public String getId() { return id; }

        public void setId(String id) { this.id = id; }
    }




    // Nested class for body data
    public static class Body {
        @JsonProperty("date_modified")
        public ZonedDateTime dateModified;

        @JsonProperty("expected_delivery_date")
        public LocalDate expectedDeliveryDate;

        public ZonedDateTime getDateModified() {
            return dateModified;
        }

        public void setDateModified(ZonedDateTime dateModified) {
            this.dateModified = dateModified;
        }

        public LocalDate getExpectedDeliveryDate() {
            return expectedDeliveryDate;
        }

        public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
            this.expectedDeliveryDate = expectedDeliveryDate;
        }
    }

}
