package com.solar_insight.app.entity;

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

    @Column(name = "contact_address_id")
    private int contact_address_id;

    public BookedConsultation() {}

    public int getContact_address_id() { return contact_address_id; }

    public void setContact_address_id(int contact_address_id) { this.contact_address_id = contact_address_id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getAppointmentType() { return appointmentType; }

    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "BookedConsultation{" +
                "id=" + id +
                ", appointmentDateTime=" + appointmentDateTime +
                ", appointmentType='" + appointmentType + '\'' +
                ", email='" + email + '\'' +
                ", contact_address_id=" + contact_address_id +
                '}';
    }

}
