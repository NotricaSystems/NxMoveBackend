package com.next.move.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import com.next.move.enums.SubStatus;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class PayPalService {

    @Value("${paypal.client-id}")
    private String clientId;
    @Value("${paypal.client-secret}")
    private String clientSecret;
    @Value("${paypal.subscription.status.url}")
    private String paypalSubStatusUrl; // use api-m.paypal.com for live
    @Value("${paypal.access.token.url}")
    private String paypalAccessTokenUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubStatus getSubscriptionStatus(String subscriptionId) throws Exception {
        String accessToken = getAccessToken();

        String url = paypalSubStatusUrl + subscriptionId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode json = objectMapper.readTree(response.getBody());
            return SubStatus.valueOf(json.get("status").asText().toUpperCase()); // e.g. "ACTIVE", "APPROVAL_PENDING"
        } else {
            //Just log the exception and return UNKNOWN for the status
            System.out.println("Failed to fetch subscription: " + response.getStatusCode().toString());
            return SubStatus.UNKNOWN;
            //throw new RuntimeException("Failed to fetch subscription: " + response.getStatusCode());
        }
    }

    /**
     * Retrieves OAuth 2.0 access token from PayPal
     */
    public String getAccessToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodeCredentials());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> requestEntity = new HttpEntity<>("grant_type=client_credentials", headers);
        System.out.println("The URL: " + paypalAccessTokenUrl);

        ResponseEntity<String> response = restTemplate.exchange(
                paypalAccessTokenUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("access_token").asText();
        } else {
            System.out.println("Failed to get access token: " + response.getStatusCode());
            return "";
            //throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
        }
    }

    private String encodeCredentials() {
        String creds = clientId + ":" + clientSecret;
        return Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }

    public boolean cancelSubscription(String subscriptionId, String reason) throws Exception {
        String accessToken = getAccessToken();

        String url = paypalSubStatusUrl + subscriptionId + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Cancellation body (PayPal requires at least an empty JSON or a reason field)
        ObjectNode body = objectMapper.createObjectNode();
        body.put("reason", reason != null ? reason : "User requested cancellation");

        HttpEntity<String> requestEntity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            // PayPal responds 204 No Content on success
            System.out.println("Subscription " + subscriptionId + " cancelled successfully.");
            return true;
        } else {
            System.out.println("Failed to cancel subscription: "
                    + response.getStatusCode() + " " + response.getBody());
            return false;
        }
    }


}
