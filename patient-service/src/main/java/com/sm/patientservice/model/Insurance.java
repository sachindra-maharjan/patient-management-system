package com.sm.patientservice.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Insurance {
    
    private String provider;
    private String policyNumber;
   

}
