package com.sm.patientservice.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.patientservice.constant.ResponseConstant;
import com.sm.patientservice.exception.BillingException;
import com.sm.patientservice.exception.EmailAlreadyExistException;
import com.sm.patientservice.exception.PatientNotExistException;
import com.sm.patientservice.grpc.BillingServiceGrpcClient;
import com.sm.patientservice.mapper.AddressMapper;
import com.sm.patientservice.mapper.InsuranceMapper;
import com.sm.patientservice.mapper.PatientMapper;
import com.sm.patientservice.model.dto.PaginatedPatientListResponseMeta;
import com.sm.patientservice.model.dto.Patient;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.repository.PatientRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    
    public PatientService(PatientRepository patientRepository, 
                          BillingServiceGrpcClient billingServiceGrpcClient) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    public static class PatientPage {
        public final List<Patient> patients;
        public final PaginatedPatientListResponseMeta meta;
        public PatientPage(List<Patient> patients, PaginatedPatientListResponseMeta meta) {
            this.patients = patients;
            this.meta = meta;
        }
    }
    
    @Transactional
    public Patient createPatient(@NonNull PatientCreateRequest newPatientRequest) {
        if (patientRepository.existsByEmailIgnoreCase(newPatientRequest.getEmail())) {
            throw new EmailAlreadyExistException("Patient with email " + newPatientRequest.getEmail() + " already exists.");
        }
        
        var newPatient = patientRepository.save(PatientMapper.toEntity(newPatientRequest));
        var billingResponse = billingServiceGrpcClient.createBillingAccount(newPatient);

        if (!billingResponse.getStatus().equals("SUCCESS")) {
            log.error("Failed to create billing account for patient: {}", newPatient.getId());
            throw new BillingException("Failed to create billing account for patient: " + newPatient.getId());
        }

        return PatientMapper.toDto(newPatient);
    }

    public Patient getPatientById(UUID id) {
        return patientRepository.findById(id)
                .map(PatientMapper::toDto)
                .orElseThrow(() -> new PatientNotExistException("Patient with ID " + id + " does not exist."));
    }

    private List<Patient> searchPatients(String name, int page, int size) {
        if (name != null && !name.isBlank()) {
            log.info("Searching patients by name: {}", name);
            String[] parts = name.trim().toLowerCase().split(" ");
            String firstName = parts[0];
            String lastName = parts.length > 1 ? parts[1] : parts[0];
            return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    firstName, lastName, PageRequest.of(page, size))
                    .stream()
                    .map(PatientMapper::toDto)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            return patientRepository.findAll(PageRequest.of(page, size)).stream()
                    .map(PatientMapper::toDto)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }
    }

    public PatientPage getPatients(String name, Integer page, Integer size) {
        int p = page == null ? ResponseConstant.DEFAULT_PAGE_NUMBER : page;
        int s = size == null ? ResponseConstant.DEFAULT_PAGE_SIZE : size;
        if (p < 0 || s <= 0){
            throw new IllegalArgumentException("Page number must be non-negative and size must be positive.");
        } 
        s = s % 10 == 0 ? s : s + (10 - (s % 10));
        if (s > 100) s = 100;

        List<Patient> patients = searchPatients(name, p, s);
        int totalPatients = patients.size();
        int totalPages = (int) Math.ceil((double) totalPatients / s);
        if (p >= totalPages) {
            return new PatientPage(List.of(), new PaginatedPatientListResponseMeta().totalPages(0).totalItems(0));
        }
        PaginatedPatientListResponseMeta meta = patients.isEmpty() ? null : new PaginatedPatientListResponseMeta()
                .page(p + 1)
                .size(s)
                .totalPages(totalPages)
                .totalItems(totalPatients);
        return new PatientPage(patients, meta);
    }

    public Patient updatePatient(@NonNull UUID id, @NonNull PatientCreateRequest patientCreateRequest) {
        if (!patientRepository.existsById(id)) {
            log.warn("Patient with ID {} not found for update.", id);
            throw new PatientNotExistException("Patient with ID " + id + " does not exist.");
        }
        
        if (patientRepository.existsByEmailIgnoreCaseAndIdNot(patientCreateRequest.getEmail(), id)) {
            throw new EmailAlreadyExistException("Patient with email " + patientCreateRequest.getEmail() + " already exists.");
        }
        
        var existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotExistException("Patient with ID " + id + " does not exist."));
        
        // Update fields
        existingPatient.setFirstName(patientCreateRequest.getFirstName());
        existingPatient.setLastName(patientCreateRequest.getLastName());
        existingPatient.setEmail(patientCreateRequest.getEmail());
        existingPatient.setPhoneNumber(patientCreateRequest.getPhoneNumber());
        existingPatient.setDob(patientCreateRequest.getDob());
        existingPatient.setGender(patientCreateRequest.getGender() != null ? patientCreateRequest.getGender().name() : null);
        existingPatient.setAddress(AddressMapper.toEntity(patientCreateRequest.getAddress()));
        existingPatient.setInsurance(InsuranceMapper.toEntity(patientCreateRequest.getInsurance()));
        
        // Save updated patient
        var updatedPatient = patientRepository.save(existingPatient);
        return PatientMapper.toDto(updatedPatient);
    }

    public void deletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            log.warn("Patient with ID {} not found for deletion.", id);
            throw new PatientNotExistException("Patient with ID " + id + " does not exist.");
        }
        patientRepository.deleteById(id);
        log.info("Patient with ID {} deleted successfully.", id);
    }
    
}
