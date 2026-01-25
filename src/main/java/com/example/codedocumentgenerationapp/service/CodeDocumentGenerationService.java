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

        String patchData;
        boolean fetchFullCode = false;

        try {
            org.kohsuke.github.GHContent readme = repository.getFileContent("README.md");
            if (readme.getContent() == null || readme.getContent().trim().isEmpty()) {
                fetchFullCode = true;
            }
        } catch (java.io.FileNotFoundException e) {
            fetchFullCode = true;
        }

        if (fetchFullCode) {
            patchData = getAllCodeContents(repository, commit.getSHA1());
        } else {
            StringBuilder diffBuilder = new StringBuilder();
            for (GHCommit.File file : commit.getFiles()) {
                diffBuilder.append("File: ").append(file.getFileName()).append("\n");
                diffBuilder.append("Status: ").append(file.getStatus()).append("\n");
                if (file.getPatch() != null) {
                    diffBuilder.append(file.getPatch()).append("\n");
                }
                diffBuilder.append("--------------------------------------------------\n");
            }
            patchData = diffBuilder.toString();
        }

        return com.example.codedocumentgenerationapp.schema.CommitResponse.builder()
                .sha(commit.getSHA1())
                .message(commit.getCommitShortInfo().getMessage())
                .authorName(commit.getAuthor().getName())
                .date(commit.getCommitDate().toString())
                .url(commit.getHtmlUrl().toString())
                .patch(patchData)
                .build();
    }

    private String getAllCodeContents(GHRepository repository, String sha) throws java.io.IOException {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("FULL CODE CONTENT (README missing or empty)\n");
        contentBuilder.append("--------------------------------------------------\n");

        org.kohsuke.github.GHTree tree = repository.getTreeRecursive(sha, 1);
        for (org.kohsuke.github.GHTreeEntry entry : tree.getTree()) {
            if ("blob".equals(entry.getType())) {
                String path = entry.getPath();
                // Basic filter to avoid binaries and large irrelevant files
                if (path.endsWith(".java") || path.endsWith(".xml") || path.endsWith(".yml") || path.endsWith(".yaml")
                        || path.endsWith(".properties") || path.endsWith(".json")) {
                    contentBuilder.append("File: ").append(path).append("\n");
                    try {
                        org.kohsuke.github.GHContent content = repository.getFileContent(path);
                        if (content.getContent() != null) {
                            contentBuilder.append(content.getContent()).append("\n");
                        }
                    } catch (Exception e) {
                        contentBuilder.append("[Error reading file content]\n");
                    }
                    contentBuilder.append("--------------------------------------------------\n");
                }
            }
        }
        return contentBuilder.toString();
    }

    public org.kohsuke.github.GHContent getReadmeContent(String owner, String repo, String token)
            throws java.io.IOException {
        GitHub github = GitHub.connectUsingOAuth(token);
        GHRepository repository = github.getRepository(owner + "/" + repo);
        try {
            return repository.getFileContent("README.md");
        } catch (java.io.FileNotFoundException e) {
            return repository.createContent()
                    .content("")
                    .message("Created empty README.md")
                    .path("README.md")
                    .commit()
                    .getContent();
        }
    }

    public void updateReadme(org.kohsuke.github.GHContent readme, String content) throws java.io.IOException {
        String currentContent = readme.getContent();
        String formattedWriteup = String.format("\n\n---\n### Documentation Update (%s)\n%s",
                java.time.LocalDateTime.now().toString(), content);
        String newContent = currentContent + formattedWriteup;
        readme.update(newContent, "Update README.md with LLM generated writeup");
    }
}
