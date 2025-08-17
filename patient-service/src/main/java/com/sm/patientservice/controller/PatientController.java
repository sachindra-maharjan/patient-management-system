package com.sm.patientservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.sm.patientservice.api.PatientsApi;
import com.sm.patientservice.constant.ResponseConstant;
import com.sm.patientservice.model.dto.PaginatedPatientListResponse;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.model.dto.PatientResponseWrapper;
import com.sm.patientservice.service.PatientService;
import com.sm.patientservice.utils.AppUtils;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponseWrapper> createPatient(@Valid PatientCreateRequest patientCreateRequest) {
        var patient = patientService.createPatient(patientCreateRequest);
        var responseWrapper = new PatientResponseWrapper()
            .data(patient)
            .status(ResponseConstant.SUCCESS_STATUS)
            .timestamp(AppUtils.getCurrentTimestamp());
        return ResponseEntity.ok(responseWrapper);
    }

    @Override
    public ResponseEntity<PaginatedPatientListResponse> getPatients(@Valid String name, @Valid Integer page, @Valid Integer size) {
        var patientPage = patientService.getPatients(name, page, size);
        var response = new PaginatedPatientListResponse()
            .status(ResponseConstant.SUCCESS_STATUS)
            .timestamp(AppUtils.getCurrentTimestamp())
            .data(patientPage.patients)
            .meta(patientPage.meta);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PatientResponseWrapper> getPatientById(UUID id) {
        return ResponseEntity.ok(
            new PatientResponseWrapper()
                .data(patientService.getPatientById(id))
                .status(ResponseConstant.SUCCESS_STATUS)
                .timestamp(AppUtils.getCurrentTimestamp())
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponseWrapper> updatePatient(UUID id,
            @Valid PatientCreateRequest patientCreateRequest) {
        return ResponseEntity.ok(
            new PatientResponseWrapper()
                .data(patientService.updatePatient(id, patientCreateRequest))
                .status(ResponseConstant.SUCCESS_STATUS)
                .timestamp(AppUtils.getCurrentTimestamp())
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent()
            .header("X-Status", ResponseConstant.SUCCESS_STATUS)
            .header("X-Timestamp", AppUtils.getCurrentTimestamp())
            .build();
    }

}
