package com.solar_insight.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class BookingDTO {

    @JsonProperty("starts_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startsAt;

    @JsonProperty("email")
    private String email;

    @JsonProperty("appointment_type")
    private String appointmentType;

    @JsonProperty("solar_insight_session_uuid")
    private String sessionUUID;

    public LocalDateTime getStartsAt() { return startsAt; }

    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getAppointmentType() { return appointmentType; }

    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

    public String getSessionUUID() { return sessionUUID; }

    public void setSessionUUID(String sessionUUID) { this.sessionUUID = sessionUUID; }

    @Override
    public String toString() {
        return "BookingDTO{" +
                "startsAt=" + startsAt +
                ", email='" + email + '\'' +
                ", appointmentType='" + appointmentType + '\'' +
                ", sessionUUID='" + sessionUUID + '\'' +
                '}';
    }

}
