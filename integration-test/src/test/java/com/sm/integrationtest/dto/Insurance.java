package com.sm.integrationtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Insurance {
    private String provider;
    private String policyNumber;

}
