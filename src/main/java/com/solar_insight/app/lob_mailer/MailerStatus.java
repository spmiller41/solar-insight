package com.solar_insight.app.lob_mailer;

public enum MailerStatus {

    // Postcard events
    POSTCARD_CREATED("postcard.created", "Created"),
    POSTCARD_REJECTED("postcard.rejected", "Rejected"),
    POSTCARD_DELIVERED("postcard.delivered", "Delivered"),
    POSTCARD_IN_LOCAL_AREA("postcard.in_local_area", "In Local Area"),
    POSTCARD_IN_TRANSIT("postcard.in_transit", "In Transit"),
    POSTCARD_PROCESSED_FOR_DELIVERY("postcard.processed_for_delivery", "Processed for Delivery"),
    POSTCARD_RE_ROUTED("postcard.re-routed", "Re-Routed"),
    POSTCARD_RETURNED_TO_SENDER("postcard.returned_to_sender", "Returned to Sender"),

    // Additional render statuses (not prefixed with "postcard")
    PROCESSED("processed", "Processed"),
    RENDERED("rendered", "Rendered"),
    FAILED("failed", "Failed");

    private final String lobEvent;
    private final String friendlyName;

    MailerStatus(String lobEvent, String friendlyName) {
        this.lobEvent = lobEvent;
        this.friendlyName = friendlyName;
    }

    public String getLobEvent() {
        return lobEvent;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    // Method to map Lob event or status to the friendly name
    public static String fromLobEvent(String lobEvent) {
        for (MailerStatus status : MailerStatus.values()) {
            if (status.getLobEvent().equals(lobEvent)) {
                return status.getFriendlyName();
            }
        }
        return "Unknown Status";
    }

}
