package com.solar_insight.app.lob_mailer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.lob_mailer.dto.CreateMailerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MailerService {

    @Value("${lob.api.key}")
    private String lobApiKey;

    @Value("${lob.postcard.url}")
    private String lobPostcardUrl;

    @Value("${lob.postcard.size}")
    private String postcardSize;

    @Value("${lob.postcard.temp.id.front}")
    private String templateIdFront;

    @Value("${lob.postcard.temp.id.back}")
    private String templateIdBack;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MailerService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }




    public Optional<CreateMailerResponse> sendPostcard(Address address, SolarEstimate solarEstimate) {
        HttpHeaders headers = createHeaders();
        Map<String, Object> mailerRequest =
                buildMailerRequest(address, solarEstimate, templateIdFront, templateIdBack, postcardSize);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(mailerRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(lobPostcardUrl, HttpMethod.POST, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CreateMailerResponse mailerResponse = objectMapper.readValue(response.getBody(), CreateMailerResponse.class);
                return Optional.of(mailerResponse);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            return Optional.empty();
        }
    }




    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = lobApiKey + ":";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        return headers;
    }




    private Map<String, Object> buildMailerRequest(Address address, SolarEstimate solarEstimate, String templateIdFront, String templateIdBack, String size) {
        Map<String, Object> request = new HashMap<>();

        // Inline address object
        Map<String, Object> toAddress = new HashMap<>();
        toAddress.put("name", "Valued Customer");
        toAddress.put("address_line1", address.getStreet());
        toAddress.put("address_city", address.getCity());
        toAddress.put("address_state", address.getState());
        toAddress.put("address_zip", address.getZip());
        toAddress.put("address_country", "US");

        // Merge variables for template
        Map<String, Object> mergeVariables = new HashMap<>();
        mergeVariables.put("savings", solarEstimate.getEstimatedSavings());
        mergeVariables.put("incentives", solarEstimate.getIncentives());

        // Create request payload
        request.put("to", toAddress);
        request.put("from", buildFromAddress());
        request.put("merge_variables", mergeVariables);
        request.put("front", templateIdFront);
        request.put("back", templateIdBack);
        request.put("size", size);
        request.put("use_type", "marketing");
        request.put("mail_type", "usps_first_class");

        return request;
    }




    private Map<String, String> buildFromAddress() {
        Map<String, String> fromAddress = new HashMap<>();
        fromAddress.put("name", "Power Solutions");
        fromAddress.put("address_line1", "2060 Ocean Ave");
        fromAddress.put("address_city", "Ronkonkoma");
        fromAddress.put("address_state", "NY");
        fromAddress.put("address_zip", "11779");
        fromAddress.put("address_country", "US");
        return fromAddress;
    }

}
