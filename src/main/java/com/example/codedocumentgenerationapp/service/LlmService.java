package com.example.codedocumentgenerationapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LlmService {

    private final RestClient restClient;

    @Value("${llm.model}")
    private String model;

    public LlmService(@Value("${llm.api.url}") String apiUrl, @Value("${llm.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String generateWriteup(String commitDiff) {
        // Truncate input to avoid hitting token limits
        String truncatedDiff = commitDiff;
        if (truncatedDiff.length() > 20000) {
            truncatedDiff = truncatedDiff.substring(0, 20000) + "\n...[TRUNCATED]...";
        }

        String prompt = "Analyze the following code changes and write a documentation summary:\n\n" + truncatedDiff;

        // Gemini Request Structure
        Map<String, Object> requestBody = Map.of(
                "contents", java.util.List.of(
                        Map.of("parts", java.util.List.of(
                                Map.of("text", prompt)))));

        Map<String, Object> response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        // Parse Gemini Response
        try {
            java.util.List<Map<String, Object>> candidates = (java.util.List<Map<String, Object>>) response
                    .get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            return "Error parsing Gemini response: " + e.getMessage();
        }
        return "No content generated";
    }
}
