package com.sm.patientservice.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.patientservice.exception.EmailAlreadyExistException;
import com.sm.patientservice.mapper.PatientMapper;
import com.sm.patientservice.model.dto.Patient;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.repository.PatientRepository;

@Service
public class PatientService {

    private PatientRepository patientRepository;
    
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }
    
    @Transactional
    public Patient createPatient(@NonNull PatientCreateRequest newPatientRequest) {
        if (patientRepository.existsByEmail(newPatientRequest.getEmail())) {
            throw new EmailAlreadyExistException("Patient with email " + newPatientRequest.getEmail() + " already exists.");
        }
        
        var newPatient = patientRepository.save(PatientMapper.toEntity(newPatientRequest));
        return PatientMapper.toDto(newPatient);
    }

}
