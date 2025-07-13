package com.sm.patientservice.mapper;

import com.sm.patientservice.model.dto.Insurance;

public class InsuranceMapper {
    
    public static Insurance toDto(com.sm.patientservice.model.Insurance insurance) {
        if (insurance == null) {
            return null;
        }
        Insurance dto = new Insurance();
        dto.setProvider(insurance.getProvider());
        dto.setPolicyNumber(insurance.getPolicyNumber());
        return dto;
    }

    public static com.sm.patientservice.model.Insurance toEntity(Insurance insuranceDto) {
        if (insuranceDto == null) {
            return null;
        }
        com.sm.patientservice.model.Insurance entity = new com.sm.patientservice.model.Insurance();
        entity.setProvider(insuranceDto.getProvider());
        entity.setPolicyNumber(insuranceDto.getPolicyNumber());
        return entity;
    }
}
