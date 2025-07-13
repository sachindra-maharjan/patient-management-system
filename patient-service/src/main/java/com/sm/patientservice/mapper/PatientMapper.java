package com.sm.patientservice.mapper;

import java.time.LocalDateTime;

import com.sm.patientservice.model.dto.Patient;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.utils.AppUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatientMapper {
        
    public static Patient toDto(com.sm.patientservice.model.Patient patient) {
        if (patient == null) {
            return null;
        }
        Patient dto = new Patient();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setDob(patient.getDob());
        dto.setGender(null == patient.getGender() ? null : Patient.GenderEnum.valueOf(patient.getGender()));
        dto.setAddress(null == patient.getAddress() ? null : AddressMapper.toDto(patient.getAddress()));
        dto.setInsurance(null == patient.getInsurance() ? null : com.sm.patientservice.mapper.InsuranceMapper.toDto(patient.getInsurance()));
        dto.setCreatedAt(AppUtils.convertLocalDateTimeToString(patient.getCreatedAt()));
        dto.setUpdatedAt(AppUtils.convertLocalDateTimeToString(patient.getUpdatedAt()));
        return dto;
    }

    public static com.sm.patientservice.model.Patient toEntity(PatientCreateRequest patientDto) {
        if (patientDto == null) {
            return null;
        }
        com.sm.patientservice.model.Patient entity = new com.sm.patientservice.model.Patient();
        // Don't set ID manually - let JPA generate it
        entity.setFirstName(patientDto.getFirstName());
        entity.setLastName(patientDto.getLastName());
        entity.setEmail(patientDto.getEmail());
        entity.setPhoneNumber(patientDto.getPhoneNumber());
        entity.setDob(patientDto.getDob());
        entity.setGender(null == patientDto.getGender() ? null : patientDto.getGender().name());
        entity.setAddress(AddressMapper.toEntity(patientDto.getAddress()));
        entity.setInsurance(InsuranceMapper.toEntity(patientDto.getInsurance()));
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        log.info("Patient Request: {}", entity);
        return entity;
    }

}
