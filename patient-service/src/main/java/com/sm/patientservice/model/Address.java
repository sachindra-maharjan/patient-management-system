package com.sm.patientservice.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Address {
    
    String street;
    String city;
    String state;
    String zipCode;
    String country;
}
