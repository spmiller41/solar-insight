package com.solar_insight.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContactInfoDTO {

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("sessionUUID")
    private String sessionUUID;

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

    public String getSessionUUID() { return sessionUUID; }

    public void setSessionUUID(String sessionUUID) { this.sessionUUID = sessionUUID; }

    @Override
    public String toString() {
        return "ContactInfoDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", sessionUUID='" + sessionUUID + '\'' +
                '}';
    }

}
