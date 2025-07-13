package com.sm.patientservice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;

    private String lastName;
    
    private LocalDate dob;
    
    private String email;

    private String phoneNumber;

    private String gender;

    @Embedded
    private Address address;

    @Embedded
    private Insurance insurance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
}
