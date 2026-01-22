package com.example.codedocumentgenerationapp.schema;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommitResponse {
    private String sha;
    private String message;
    private String authorName;
    private String date;
    private String url;
    private String patch;
    private String writeup;
}
