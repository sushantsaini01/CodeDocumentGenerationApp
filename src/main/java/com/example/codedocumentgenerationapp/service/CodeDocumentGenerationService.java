package com.example.codedocumentgenerationapp.service;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

@Service
public class CodeDocumentGenerationService {

    public com.example.codedocumentgenerationapp.schema.CommitResponse getLatestCommit(String owner, String repo,
            String token) throws java.io.IOException {
        GitHub github = GitHub.connectUsingOAuth(token);
        GHRepository repository = github.getRepository(owner + "/" + repo);
        GHCommit commit = repository.listCommits().iterator().next();

        StringBuilder diffBuilder = new StringBuilder();
        for (GHCommit.File file : commit.getFiles()) {
            diffBuilder.append("File: ").append(file.getFileName()).append("\n");
            diffBuilder.append("Status: ").append(file.getStatus()).append("\n");
            if (file.getPatch() != null) {
                diffBuilder.append(file.getPatch()).append("\n");
            }
            diffBuilder.append("--------------------------------------------------\n");
        }

        return com.example.codedocumentgenerationapp.schema.CommitResponse.builder()
                .sha(commit.getSHA1())
                .message(commit.getCommitShortInfo().getMessage())
                .authorName(commit.getAuthor().getName())
                .date(commit.getCommitDate().toString())
                .url(commit.getHtmlUrl().toString())
                .patch(diffBuilder.toString())
                .build();
    }

    public String getReadmeContent(String owner, String repo, String token) {
        try {
            GitHub github = GitHub.connectUsingOAuth(token);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            org.kohsuke.github.GHContent readme = repository.getFileContent("README.md");
            return readme.getContent();
        } catch (Exception e) {
            return ""; // Return empty string if file doesn't exist or error
        }
    }

    public void updateReadme(String owner, String repo, String token, String content) throws java.io.IOException {
        GitHub github = GitHub.connectUsingOAuth(token);
        GHRepository repository = github.getRepository(owner + "/" + repo);

        try {
            org.kohsuke.github.GHContent readme = repository.getFileContent("README.md");
            String currentContent = readme.getContent();
            String formattedWriteup = String.format("\n\n---\n### Documentation Update (%s)\n%s",
                    java.time.LocalDateTime.now().toString(), content);
            String newContent = currentContent + formattedWriteup;
            readme.update(newContent, "Update README.md with LLM generated writeup");
        } catch (java.io.FileNotFoundException e) {
            // README.md does not exist, create it
            repository.createContent()
                    .content(content)
                    .message("Create README.md with LLM generated writeup")
                    .path("README.md")
                    .commit();
        }
    }
}
