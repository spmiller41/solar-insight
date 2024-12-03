package com.solar_insight.app.zoho_crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.zoho_crm.dto.ZohoTokenResponse;
import com.solar_insight.app.zoho_crm.logs.TokenLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * TokenService manages access tokens for Zoho CRM API based on module type (e.g., custom_module, file_upload).
 * <p>
 * Steps:
 * <p>
 * 1. Call getAccessToken(String moduleType) to obtain the access token for the given module.
 * <p>
 * 2. The method first checks if a token is stored in the tokenStore map for the requested module.
 * <p>
 * 3. If no token is found or the token is expired (checked via isExpired()), it calls refreshTokenForModule()
 *    to get a new token from Zoho API.
 * <p>
 * 4. refreshTokenForModule() sends a request to Zoho API using the corresponding refresh token for the module,
 *    retrieves a new access token, and updates the tokenStore with the new token details.
 * <p>
 * 5. If the token is valid, the stored access token is returned.
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${zoho.token.base.url}")
    private String baseUrl;

    @Value("${zoho.custom.module.refresh.token}")
    private String customModuleRefreshToken;

    @Value("${zoho.file.upload.refresh.token}")
    private String fileUploadRefreshToken;

    @Value("${zoho.leads.refresh.token}")
    private String zohoLeadsRefreshToken;

    @Value("${zoho.client.id}")
    private String clientId;

    @Value("${zoho.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Holds details for various modules using inner class (TokenDetails).
    private final Map<String, TokenDetails> tokenStore = new HashMap<>();

    @Autowired
    public TokenService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }




    /**
     * Retrieves a token for accessing Zoho CRM API for a specific module.
     *
     * @param moduleType - The type of module (e.g., "custom_module" or "file_upload").
     * @return the access token as a String.
     */
    public String getAccessToken(String moduleType) {
        // Determine which refresh token to use based on moduleType
        String refreshToken = getRefreshTokenForModule(moduleType);

        // Check if token needs to be refreshed
        TokenDetails tokenDetails = tokenStore.get(moduleType);

        if (tokenDetails == null || tokenDetails.isExpired()) {
            refreshTokenForModule(moduleType, refreshToken);
        }

        return tokenStore.get(moduleType).accessToken();
    }




    private String getRefreshTokenForModule(String moduleType) {
        return switch (moduleType) {
            case "custom_module" -> customModuleRefreshToken;
            case "file_upload" -> fileUploadRefreshToken;
            case "Leads" -> zohoLeadsRefreshToken;
            default -> throw new IllegalArgumentException("Unknown module type: " + moduleType);
        };
    }




    private void refreshTokenForModule(String moduleType, String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                ZohoTokenResponse tokenResponse = objectMapper.readValue(responseBody, ZohoTokenResponse.class);

                // Store the token and its expiration time for the given module
                TokenDetails tokenDetails =
                        new TokenDetails(tokenResponse.getAccessToken(), Instant.now(), tokenResponse.getExpiresIn());
                tokenStore.put(moduleType, tokenDetails);
                TokenLogger.logSuccessfulTokenInfo(moduleType, logger);
            } else {
                TokenLogger.logFailedTokenErr(moduleType, logger);
            }
        } catch (Exception ex) {
            TokenLogger.logTokenExceptionErr(moduleType, logger, ex);
        }
    }




    // Inner class to store token details
    private record TokenDetails(String accessToken, Instant lastRefreshed, int expiresIn) {
        public boolean isExpired() {
            return Instant.now().getEpochSecond() - lastRefreshed.getEpochSecond() >= expiresIn;
        }
    }

}
