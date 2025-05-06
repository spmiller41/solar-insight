package com.solar_insight.app.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.solar_insight.app.dto.MailerBookingDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mailer_bookings")
public class MailerBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "appointment_type")
    private String appointmentType;

    @Column(name = "booking_ref")
    private String bookingRef;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip")
    private String zip;

    @Column(name = "promo")
    private String promo;

    public MailerBooking() {}

    public MailerBooking(MailerBookingDTO bookingData) {
        this.startsAt = bookingData.getStartsAt();
        this.appointmentType = bookingData.getAppointmentType();
        this.bookingRef = bookingData.getBookingRef();
        this.firstName = bookingData.getFirstName();
        this.lastName = bookingData.getLastName();
        this.email = bookingData.getEmail();
        this.phone = bookingData.getPhone();
        this.street = bookingData.getStreet();
        this.city = bookingData.getCity();
        this.state = bookingData.getState();
        this.zip = bookingData.getZip();
        this.promo = bookingData.getPromo();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getBookingRef() {
        return bookingRef;
    }

    public void setBookingRef(String bookingRef) {
        this.bookingRef = bookingRef;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPromo() { return promo; }

    public void setPromo(String promo) { this.promo = promo; }

    @Override
    public String toString() {
        return "MailerBooking{" +
                "id=" + id +
                ", startsAt=" + startsAt +
                ", appointmentType='" + appointmentType + '\'' +
                ", bookingRef='" + bookingRef + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", promo='" + promo + '\'' +
                '}';
    }

}
