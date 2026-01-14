package com.example.codedocumentgenerationapp.controller;


import com.example.codedocumentgenerationapp.schema.FetchCommitRequest;
import com.example.codedocumentgenerationapp.service.CodeDocumentGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeDocumentGenerationController {

    @Autowired
    private CodeDocumentGenerationService codeDocumentGenerationService;


    @PostMapping("/fetch-latest-commit")
    public String fetchLatestCommit(@RequestBody FetchCommitRequest request) {
        // Placeholder for document generation logic

        String commitResponse = codeDocumentGenerationService.getLatestCommit(
                request.getOwner(),
                request.getRepo(),
                request.getToken()
        );

        return commitResponse;
    }
}
