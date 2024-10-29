package com.solar_insight.app.zoho_crm.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class FetchedSubformData {

    private String parentId;

    private final List<MailerDTO> mailerList = new ArrayList<>();

    public String getParentId() { return parentId; }

    public List<MailerDTO> getMailerList() { return mailerList; }




    public FetchedSubformData(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(String.valueOf(response));
        } catch (Exception ex) {
            System.out.println("There was an issue while attempting to build DTO: " + ex.getMessage());
            return;
        }

        // Access the "data" array
        JsonNode dataArray = rootNode.path("data");
        for (JsonNode dataNode : dataArray) {

            // Extract fields within "data" starting with the parent id.
            this.parentId = dataNode.path("id").asText();

            // Now access the "Mailers" list within each data element
            JsonNode mailersArray = dataNode.path("Mailers");
            for (JsonNode mailerNode : mailersArray) {
                MailerDTO mailer = new MailerDTO();

                mailer.setRecordId(mailerNode.path("id").asText());
                mailer.setReferenceId(mailerNode.path("Reference_Id").asText());
                mailer.setStatus(mailerNode.path("Status").asText());
                mailer.setExpectedDelivery(mailerNode.path("Expected_Delivery").asText());
                mailer.setSendDate(mailerNode.path("Send_Date").asText());

                this.mailerList.add(mailer);
            }
        }
    }




    @Override
    public String toString() {
        return "ZohoMailerSubformDTO{" +
                "parentId='" + parentId + '\'' +
                ", mailerList=" + mailerList +
                '}';
    }

}
