package com.solar_insight.app.entity;

import com.solar_insight.app.dto.BookingDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="booked_consultations")
public class BookedConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "appointment_date_time")
    private LocalDateTime appointmentDateTime;

    @Column(name = "appointment_type")
    private String appointmentType;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "contact_address_id")
    private int contactAddressId;

    public BookedConsultation() {}

    public BookedConsultation(BookingDTO bookingData, ContactAddress contactAddress) {
        this.appointmentDateTime = bookingData.getStartsAt();
        this.appointmentType = bookingData.getAppointmentType();
        this.email = bookingData.getEmail();
        this.createdAt = LocalDateTime.now();
        this.contactAddressId = contactAddress.getId();
    }

    public int getContactAddressId() { return contactAddressId; }

    public void setContactAddressId(int contactAddressId) { this.contactAddressId = contactAddressId; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getAppointmentType() { return appointmentType; }

    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "BookedConsultation{" +
                "id=" + id +
                ", appointmentDateTime=" + appointmentDateTime +
                ", appointmentType='" + appointmentType + '\'' +
                ", email='" + email + '\'' +
                ", contact_address_id=" + contactAddressId +
                '}';
    }

}
