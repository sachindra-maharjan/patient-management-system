package com.sm.patientservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.sm.patientservice.api.PatientsApi;
import com.sm.patientservice.model.dto.PaginatedPatientListResponse;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.model.dto.PatientResponseWrapper;
import com.sm.patientservice.service.PatientService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PatientController implements PatientsApi{
    
    private final PatientService patientService;

    public PatientController (PatientService patientService) {
        this.patientService = patientService;
    }
    
    @Override
    public ResponseEntity<PatientResponseWrapper> createPatient(@Valid PatientCreateRequest patientCreateRequest) {
        var patient = patientService.createPatient(patientCreateRequest);
        var responseWrapper = new PatientResponseWrapper();
        responseWrapper.setData(patient);
        responseWrapper.setStatus("success");
        responseWrapper.setTimestamp(java.time.OffsetDateTime.now());
        return ResponseEntity.ok(responseWrapper);
    }

    @Override
    public ResponseEntity<Void> deletePatient(UUID id) {
        // TODO Auto-generated method stub
        return PatientsApi.super.deletePatient(id);
    }

    @Override
    public ResponseEntity<PatientResponseWrapper> getPatientById(UUID id) {
        // TODO Auto-generated method stub
        return PatientsApi.super.getPatientById(id);
    }

    @Override
    public ResponseEntity<PaginatedPatientListResponse> getPatients(@Valid Integer page, @Valid Integer size) {
        // TODO Auto-generated method stub
        return PatientsApi.super.getPatients(page, size);
    }

    @Override
    public ResponseEntity<PatientResponseWrapper> updatePatient(UUID id,
            @Valid PatientCreateRequest patientCreateRequest) {
        // TODO Auto-generated method stub
        return PatientsApi.super.updatePatient(id, patientCreateRequest);
    }

    
    
}
