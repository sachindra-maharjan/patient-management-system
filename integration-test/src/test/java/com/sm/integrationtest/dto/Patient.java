package com.sm.integrationtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Patient {
    private String id;
    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private String phoneNumber;
    private String gender;
    private Address address;
    private Insurance insurance;
    private String createdAt;
    private String updatedAt;
}
