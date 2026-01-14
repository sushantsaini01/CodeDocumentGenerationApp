package com.example.codedocumentgenerationapp.service;


import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

@Service
public class CodeDocumentGenerationService {

    public String getLatestCommit(String owner, String repo, String token) {
        try {
            GitHub github = GitHub.connectUsingOAuth(token);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHCommit commit = repository.listCommits().toList().get(0);
            return "Commit: " + commit.getSHA1() + " - " + commit.getCommitShortInfo().getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


}
