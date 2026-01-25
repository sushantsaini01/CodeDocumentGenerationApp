package com.example.codedocumentgenerationapp.controller;

import com.example.codedocumentgenerationapp.schema.FetchCommitRequest;
import com.example.codedocumentgenerationapp.service.CodeDocumentGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.kohsuke.github.GHContent;

@RestController
public class CodeDocumentGenerationController {

    private final CodeDocumentGenerationService codeDocumentGenerationService;
    private final com.example.codedocumentgenerationapp.service.LlmService llmService;

    @Autowired
    public CodeDocumentGenerationController(CodeDocumentGenerationService codeDocumentGenerationService,
            com.example.codedocumentgenerationapp.service.LlmService llmService) {
        this.codeDocumentGenerationService = codeDocumentGenerationService;
        this.llmService = llmService;
    }

    @PostMapping("/fetch-latest-commit")
    public org.springframework.http.ResponseEntity<com.example.codedocumentgenerationapp.schema.CommitResponse> fetchLatestCommit(
            @RequestBody FetchCommitRequest request) throws java.io.IOException {
        com.example.codedocumentgenerationapp.schema.CommitResponse commitResponse = codeDocumentGenerationService
                .getLatestCommit(
                        request.getOwner(),
                        request.getRepo(),
                        request.getToken());

        if (commitResponse.getPatch() != null && !commitResponse.getPatch().isEmpty()) {
            // Fetch existing README content
            GHContent readme = codeDocumentGenerationService.getReadmeContent(
                    request.getOwner(),
                    request.getRepo(),
                    request.getToken());
            String existingReadmeContent = readme.getContent();

            String writeup = llmService.generateWriteup(commitResponse.getPatch(), existingReadmeContent);
            commitResponse.setWriteup(writeup);

            // Update README.md only if there is content to add
            if (!writeup.trim().equals("NO_UPDATE")) {
                codeDocumentGenerationService.updateReadme(
                        readme,
                        writeup);
            }
        }

        return org.springframework.http.ResponseEntity.ok(commitResponse);
    }
}
