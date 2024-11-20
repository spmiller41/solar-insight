package com.solar_insight.app.entity;

import com.solar_insight.app.dto.PreliminaryDataDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "uuid")
    private String sessionUUID;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "address_id")
    private int addressId;

    @Column(name = "referrer")
    private String referrer;

    public UserSession(PreliminaryDataDTO data, Address address, String sessionUUID) {
        this.sessionUUID = sessionUUID;
        this.ipAddress = data.getClientIp();
        this.createdAt = LocalDateTime.now();
        this.addressId = address.getId();
        this.referrer = data.getReferrer();
    }

    public void associateAddress(Address address) {
        this.addressId = address.getId();
    }

    public UserSession() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setSessionUUID(String sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getReferrer() { return referrer; }

    public void setReferrer(String referrer) { this.referrer = referrer; }

    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", sessionUUID='" + sessionUUID + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                ", addressId=" + addressId +
                '}';
    }

}
