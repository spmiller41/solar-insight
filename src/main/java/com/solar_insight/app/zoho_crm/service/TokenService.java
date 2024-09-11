package com.solar_insight.app.zoho_crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.zoho_crm.dto.ZohoTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
public class TokenService {

    @Value("${zoho.token.base.url}")
    private String baseUrl;

    @Value("${zoho.leads.refresh.token}")
    private String refreshToken;

    @Value("${zoho.client.id}")
    private String clientId;

    @Value("${zoho.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private Instant lastRefreshed;
    private int expiresIn;

    @Autowired
    public TokenService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves a token for accessing Zoho CRM API.
     *
     * <p>
     * This method checks if the access token is null or expired. If the token is expired,
     * it obtains a new access token from Zoho.
     * After refreshing, it returns the valid access token.
     *
     * @return the access token as a String.
     */
    public String getAccessToken() {
        if (accessToken == null || tokenExpired()) refreshToken();
        return accessToken;
    }

    private void refreshToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(baseUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();

                ZohoTokenResponse tokenResponse = objectMapper.readValue(responseBody, ZohoTokenResponse.class);

                accessToken = tokenResponse.getAccessToken();
                lastRefreshed = Instant.now();
                expiresIn = tokenResponse.getExpiresIn();
            } else {
                // Add organized error logging here
                System.err.print("Could not refresh access token");
            }
        } catch (Exception ex) {
            // Add organized error logging here
            System.err.println("Exception Message: " + ex.getMessage());
        }
    }

    private boolean tokenExpired() {
        if (lastRefreshed == null) return true;
        return Instant.now().getEpochSecond() - lastRefreshed.getEpochSecond() >= expiresIn;
    }

}
