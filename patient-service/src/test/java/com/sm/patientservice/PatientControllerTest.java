package com.sm.patientservice;

import com.sm.patientservice.constant.ResponseConstant;
import com.sm.patientservice.controller.PatientController;
import com.sm.patientservice.exception.EmailAlreadyExistException;
import com.sm.patientservice.exception.PatientNotExistException;
import com.sm.patientservice.model.dto.PaginatedPatientListResponse;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.model.dto.PatientResponseWrapper;
import com.sm.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PatientControllerTest {
    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private PatientCreateRequest sampleRequest;
    private com.sm.patientservice.model.dto.Patient samplePatient;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleId = UUID.randomUUID();
        sampleRequest = new PatientCreateRequest()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .email("john.doe@email.com")
                .phoneNumber("1234567890")
                .gender(com.sm.patientservice.model.dto.PatientCreateRequest.GenderEnum.MALE);
        samplePatient = new com.sm.patientservice.model.dto.Patient()
                .id(sampleId)
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .email("john.doe@email.com")
                .phoneNumber("1234567890")
                .gender(com.sm.patientservice.model.dto.Patient.GenderEnum.MALE);
    }

    @Nested
    @DisplayName("createPatient")
    class CreatePatient {
        @Test
        void shouldCreatePatientSuccessfully() {
            when(patientService.createPatient(any())).thenReturn(samplePatient);
            ResponseEntity<PatientResponseWrapper> response = patientController.createPatient(sampleRequest);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).isEqualTo(samplePatient);
            assertThat(response.getBody().getStatus()).isEqualTo(ResponseConstant.SUCCESS_STATUS);
        }

        @Test
        void shouldThrowEmailAlreadyExist() {
            when(patientService.createPatient(any())).thenThrow(new EmailAlreadyExistException("Email already exists"));
            assertThrows(EmailAlreadyExistException.class, () -> patientController.createPatient(sampleRequest));
        }
    }

    @Nested
    @DisplayName("getPatients")
    class GetPatients {
        @Test
        void shouldReturnPaginatedPatients() {
            PatientService.PatientPage page = new PatientService.PatientPage(List.of(samplePatient), null);
            when(patientService.getPatients(any(), any(), any())).thenReturn(page);
            ResponseEntity<PaginatedPatientListResponse> response = patientController.getPatients("John", 0, 10);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).containsExactly(samplePatient);
            assertThat(response.getBody().getStatus()).isEqualTo(ResponseConstant.SUCCESS_STATUS);
        }

        @Test
        void shouldReturnEmptyListIfNoPatients() {
            PatientService.PatientPage page = new PatientService.PatientPage(Collections.emptyList(), null);
            when(patientService.getPatients(any(), any(), any())).thenReturn(page);
            ResponseEntity<PaginatedPatientListResponse> response = patientController.getPatients(null, 0, 10);
            assertThat(response.getBody().getData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPatientById")
    class GetPatientById {
        @Test
        void shouldReturnPatient() {
            when(patientService.getPatientById(any())).thenReturn(samplePatient);
            ResponseEntity<PatientResponseWrapper> response = patientController.getPatientById(sampleId);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody().getData()).isEqualTo(samplePatient);
        }

        @Test
        void shouldThrowIfNotFound() {
            when(patientService.getPatientById(any())).thenThrow(new PatientNotExistException("Not found"));
            assertThrows(PatientNotExistException.class, () -> patientController.getPatientById(sampleId));
        }
    }

    @Nested
    @DisplayName("updatePatient")
    class UpdatePatient {
        @Test
        void shouldUpdatePatient() {
            when(patientService.updatePatient(any(), any())).thenReturn(samplePatient);
            ResponseEntity<PatientResponseWrapper> response = patientController.updatePatient(sampleId, sampleRequest);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody().getData()).isEqualTo(samplePatient);
        }

        @Test
        void shouldThrowIfNotFound() {
            when(patientService.updatePatient(any(), any())).thenThrow(new PatientNotExistException("Not found"));
            assertThrows(PatientNotExistException.class, () -> patientController.updatePatient(sampleId, sampleRequest));
        }
    }

    @Nested
    @DisplayName("deletePatient")
    class DeletePatient {
        @Test
        void shouldDeletePatient() {
            doNothing().when(patientService).deletePatient(any());
            ResponseEntity<Void> response = patientController.deletePatient(sampleId);
            assertThat(response.getStatusCode().value()).isEqualTo(204);
            assertThat(response.getHeaders().getFirst("X-Status")).isEqualTo(ResponseConstant.SUCCESS_STATUS);
            assertThat(response.getHeaders().getFirst("X-Timestamp")).isNotNull();
        }

        @Test
        void shouldThrowIfNotFound() {
            doThrow(new PatientNotExistException("Not found")).when(patientService).deletePatient(any());
            assertThrows(PatientNotExistException.class, () -> patientController.deletePatient(sampleId));
        }
    }
} 