package com.sm.patientservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm.patientservice.constant.ResponseConstant;
import com.sm.patientservice.controller.PatientController;
import com.sm.patientservice.exception.EmailAlreadyExistException;
import com.sm.patientservice.exception.PatientNotExistException;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PatientService patientService;

    private PatientCreateRequest sampleRequest;
    private com.sm.patientservice.model.dto.Patient samplePatient;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
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
    @DisplayName("POST /patients")
    class CreatePatient {
        @Test
        void shouldCreatePatient() throws Exception {
            when(patientService.createPatient(any())).thenReturn(samplePatient);
            mockMvc.perform(post("/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(sampleId.toString())))
                    .andExpect(jsonPath("$.status", is(ResponseConstant.SUCCESS_STATUS)));
        }

        @Test
        void shouldReturn409IfEmailExists() throws Exception {
            when(patientService.createPatient(any())).thenThrow(new EmailAlreadyExistException("Email already exists"));
            mockMvc.perform(post("/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /patients")
    class GetPatients {
        @Test
        void shouldReturnPatients() throws Exception {
            PatientService.PatientPage page = new PatientService.PatientPage(List.of(samplePatient), null);
            when(patientService.getPatients(any(), any(), any())).thenReturn(page);
            mockMvc.perform(get("/patients?name=John&page=0&size=10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].id", is(sampleId.toString())));
        }

        @Test
        void shouldReturnEmptyList() throws Exception {
            PatientService.PatientPage page = new PatientService.PatientPage(Collections.emptyList(), null);
            when(patientService.getPatients(any(), any(), any())).thenReturn(page);
            mockMvc.perform(get("/patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /patients/{id}")
    class GetPatientById {
        @Test
        void shouldReturnPatient() throws Exception {
            when(patientService.getPatientById(eq(sampleId))).thenReturn(samplePatient);
            mockMvc.perform(get("/patients/{id}", sampleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(sampleId.toString())));
        }

        @Test
        void shouldReturn404IfNotFound() throws Exception {
            when(patientService.getPatientById(eq(sampleId))).thenThrow(new PatientNotExistException("Not found"));
            mockMvc.perform(get("/patients/{id}", sampleId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /patients/{id}")
    class UpdatePatient {
        @Test
        void shouldUpdatePatient() throws Exception {
            when(patientService.updatePatient(eq(sampleId), any())).thenReturn(samplePatient);
            mockMvc.perform(put("/patients/{id}", sampleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(sampleId.toString())));
        }

        @Test
        void shouldReturn404IfNotFound() throws Exception {
            when(patientService.updatePatient(eq(sampleId), any())).thenThrow(new PatientNotExistException("Not found"));
            mockMvc.perform(put("/patients/{id}", sampleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /patients/{id}")
    class DeletePatient {
        @Test
        void shouldDeletePatient() throws Exception {
            doNothing().when(patientService).deletePatient(eq(sampleId));
            mockMvc.perform(delete("/patients/{id}", sampleId))
                    .andExpect(status().isNoContent())
                    .andExpect(header().string("X-Status", ResponseConstant.SUCCESS_STATUS))
                    .andExpect(header().exists("X-Timestamp"));
        }

        @Test
        void shouldReturn404IfNotFound() throws Exception {
            doThrow(new PatientNotExistException("Not found")).when(patientService).deletePatient(eq(sampleId));
            mockMvc.perform(delete("/patients/{id}", sampleId))
                    .andExpect(status().isNotFound());
        }
    }
} 