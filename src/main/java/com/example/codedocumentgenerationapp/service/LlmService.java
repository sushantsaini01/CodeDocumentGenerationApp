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
    public String generateWriteup(String commitDiff, String existingReadme) {
        // Truncate input to avoid hitting token limits
        String truncatedDiff = commitDiff;
        if (truncatedDiff.length() > 20000) {
            truncatedDiff = truncatedDiff.substring(0, 20000) + "\n...[TRUNCATED]...";
        }

        // Truncate existing readme to avoid token limits
        String truncatedReadme = existingReadme;
        if (truncatedReadme != null && truncatedReadme.length() > 50000) {
            truncatedReadme = truncatedReadme.substring(0, 50000) + "\n...[TRUNCATED]...";
        }

        String prompt = "You are a code documentation expert.\n" +
                "Existing README Content:\n" + truncatedReadme + "\n\n" +
                "New Code Changes (Diff):\n" + truncatedDiff + "\n\n" +
                "Instruction:\n" +
                "Analyze the code changes against the existing README.\n" +
                "1. If the existing README already covers these changes, return 'NO_UPDATE'.\n" +
                "2. If the changes are trivial optimizations or refactoring that don't need user-facing documentation, return 'NO_UPDATE'.\n"
                +
                "3. Otherwise, write a concise summary of the changes to be appended to the README. Return ONLY the new content.";

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
