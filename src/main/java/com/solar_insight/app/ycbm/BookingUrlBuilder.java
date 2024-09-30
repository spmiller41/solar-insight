package com.solar_insight.app.ycbm;

import com.solar_insight.app.entity.Contact;
import com.solar_insight.app.ycbm.logs.UrlBuildLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BookingUrlBuilder {

    private static final Logger logger = LoggerFactory.getLogger(BookingUrlBuilder.class);

    public Optional<String> generate(String baseUrl, Contact contact, String sessionUUID) {
        try {
            return Optional.of(String.format("%s?UUID=%s&EMAIL=%s",
                    baseUrl,
                    encode(sessionUUID),
                    encode(contact.getEmail())));
        } catch (Exception ex) {
            UrlBuildLogger.logFailedUrlBuildErr(contact, sessionUUID, logger, ex);
            return Optional.empty();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
