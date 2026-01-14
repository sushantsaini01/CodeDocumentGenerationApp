package com.example.codedocumentgenerationapp.schema;

import lombok.Data;

@Data
public class FetchCommitRequest {

    private String owner;
    private String repo;
    private String token;

}
