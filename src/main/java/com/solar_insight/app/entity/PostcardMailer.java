package com.solar_insight.app.entity;

import com.solar_insight.app.lob_mailer.MailerStatus;
import com.solar_insight.app.lob_mailer.dto.CreateMailerResponse;
import com.solar_insight.app.lob_mailer.dto.TrackingEventData;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "postcard_mailers")
public class PostcardMailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "status")
    private String status;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(name = "send_date")
    private LocalDateTime sendDate;

    @Column(name = "address_id")
    private int addressId;

    public PostcardMailer() {}

    public PostcardMailer(CreateMailerResponse mailerData, Address address) {
        this.referenceId = mailerData.getReferenceId();
        this.status = MailerStatus.fromLobEvent(mailerData.getStatus());
        this.expectedDeliveryDate = mailerData.getExpectedDeliveryDate();
        this.dateModified = mailerData.getModifiedTimeInNewYork();
        this.sendDate = mailerData.getSendDateInNewYork();
        this.addressId = address.getId();
    }

    public void refreshMailerData(TrackingEventData eventData) {
        this.status = MailerStatus.fromLobEvent(eventData.getEvent());
        this.expectedDeliveryDate = eventData.getExpectedDeliveryDate();
        this.dateModified = eventData.getModifiedTimeInNewYork();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public LocalDateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    @Override
    public String toString() {
        return "PostcardMailer{" +
                "id=" + id +
                ", referenceId='" + referenceId + '\'' +
                ", status='" + status + '\'' +
                ", expectedDeliveryDate=" + expectedDeliveryDate +
                ", dateModified=" + dateModified +
                ", sendDate=" + sendDate +
                ", addressId=" + addressId +
                '}';
    }

}
