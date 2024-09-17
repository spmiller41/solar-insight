package com.solar_insight.app.ycbm;

import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.Contact;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BookingUrlBuilder {

    public Optional<String> generate(String baseUrl, Contact contact, String sessionUUID) {
        try {
            return Optional.of(String.format("%s?UUID=%s&EMAIL=%s",
                    baseUrl,
                    encode(sessionUUID),
                    encode(contact.getEmail())));
        } catch (Exception ex) {
            // Add more organized error logging here
            System.err.print("There was an issue generating a Query URL: " + ex.getMessage());
            return Optional.empty();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
