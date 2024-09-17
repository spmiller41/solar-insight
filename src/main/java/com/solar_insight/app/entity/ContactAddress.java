package com.solar_insight.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name="contacts_addresses")
public class ContactAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "address_id")
    private int addressId;

    @Column(name = "contact_id")
    private int contactId;

    @Column(name = "last_user_session_id")
    private int lastUserSessionId;

    public ContactAddress(Contact contact, Address address, UserSession userSession) {
        this.contactId = contact.getId();
        this.addressId = address.getId();
        this.lastUserSessionId = userSession.getId();
    }

    public void refreshLastUserSession(UserSession userSession) {
        this.lastUserSessionId = userSession.getId();
    }

    public ContactAddress() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getLastUserSessionId() { return lastUserSessionId; }

    public void setLastUserSessionId(int lastUserSessionId) { this.lastUserSessionId = lastUserSessionId; }

    @Override
    public String toString() {
        return "ContactAddress{" +
                "id=" + id +
                ", addressId=" + addressId +
                ", contactId=" + contactId +
                ", lastUserSessionId=" + lastUserSessionId +
                '}';
    }

}
