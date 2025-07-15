package com.sm.patientservice;

import com.sm.patientservice.exception.EmailAlreadyExistException;
import com.sm.patientservice.exception.PatientNotExistException;
import com.sm.patientservice.mapper.PatientMapper;
import com.sm.patientservice.model.Address;
import com.sm.patientservice.model.Insurance;
import com.sm.patientservice.model.Patient;
import com.sm.patientservice.model.dto.PatientCreateRequest;
import com.sm.patientservice.repository.PatientRepository;
import com.sm.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {
    @Mock
    private PatientRepository patientRepository;
    @InjectMocks
    private PatientService patientService;

    private PatientCreateRequest request;
    private Patient patientEntity;
    private com.sm.patientservice.model.dto.Patient patientDto;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        request = new PatientCreateRequest()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .email("john.doe@email.com")
                .phoneNumber("1234567890")
                .gender(com.sm.patientservice.model.dto.PatientCreateRequest.GenderEnum.MALE);
        patientEntity = new Patient();
        patientEntity.setId(id);
        patientEntity.setFirstName("John");
        patientEntity.setLastName("Doe");
        patientEntity.setDob(LocalDate.of(1990, 1, 1));
        patientEntity.setEmail("john.doe@email.com");
        patientEntity.setPhoneNumber("1234567890");
        patientEntity.setGender("MALE");
        patientEntity.setAddress(new Address());
        patientEntity.setInsurance(new Insurance());
        patientDto = new com.sm.patientservice.model.dto.Patient()
                .id(id)
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
        void shouldCreatePatient() {
            // given
            when(patientRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(patientRepository.save(any())).thenReturn(patientEntity);
            try (var patientMapperMock = Mockito.mockStatic(PatientMapper.class)) {
                patientMapperMock.when(() -> PatientMapper.toEntity(any())).thenReturn(patientEntity);
                patientMapperMock.when(() -> PatientMapper.toDto(any())).thenReturn(patientDto);
                // when
                var result = patientService.createPatient(request);
                // then
                assertThat(result).isEqualTo(patientDto);
            }
        }

        @Test
        void shouldThrowIfEmailExists() {
            // given
            when(patientRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);
            // when/then
            assertThrows(EmailAlreadyExistException.class, () -> patientService.createPatient(request));
        }
    }

    @Nested
    @DisplayName("getPatientById")
    class GetPatientById {
        @Test
        void shouldReturnPatient() {
            // given
            when(patientRepository.findById(eq(id))).thenReturn(Optional.of(patientEntity));
            try (var patientMapperMock = Mockito.mockStatic(PatientMapper.class)) {
                patientMapperMock.when(() -> PatientMapper.toDto(any())).thenReturn(patientDto);
                // when
                var result = patientService.getPatientById(id);
                // then
                assertThat(result).isEqualTo(patientDto);
            }
        }

        @Test
        void shouldThrowIfNotFound() {
            // given
            when(patientRepository.findById(eq(id))).thenReturn(Optional.empty());
            // when/then
            assertThrows(PatientNotExistException.class, () -> patientService.getPatientById(id));
        }
    }

    @Nested
    @DisplayName("deletePatient")
    class DeletePatient {
        @Test
        void shouldDeletePatient() {
            // given
            when(patientRepository.existsById(eq(id))).thenReturn(true);
            doNothing().when(patientRepository).deleteById(eq(id));
            // when
            patientService.deletePatient(id);
            // then
            verify(patientRepository).deleteById(id);
        }

        @Test
        void shouldThrowIfNotFound() {
            // given
            when(patientRepository.existsById(eq(id))).thenReturn(false);
            // when/then
            assertThrows(PatientNotExistException.class, () -> patientService.deletePatient(id));
        }
    }

    @Nested
    @DisplayName("getPatients")
    class GetPatients {
        @Test
        void shouldReturnPatientsWithAllQueryParams() {
            // given
            int page = 0;
            int size = 10;
            String name = "John";
            var patientEntities = List.of(patientEntity);
            var patientDtos = List.of(patientDto);
            when(patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    anyString(), anyString(), any())).thenReturn(patientEntities);
            try (var patientMapperMock = Mockito.mockStatic(PatientMapper.class)) {
                patientMapperMock.when(() -> PatientMapper.toDto(any())).thenReturn(patientDto);
                // when
                var result = patientService.getPatients(name, page, size);
                // then
                assertThat(result.patients).isEqualTo(patientDtos);
                assertThat(result.meta).isNotNull();
                assertThat(result.meta.getPage()).isEqualTo(page + 1);
                assertThat(result.meta.getSize()).isEqualTo(size);
                assertThat(result.meta.getTotalItems()).isEqualTo(patientDtos.size());
            }
        }

        @Test
        void shouldReturnEmptyWhenPageOutOfBounds() {
            // given
            int page = 10;
            int size = 10;
            String name = "John";
            var patientEntities = List.of(patientEntity);
            when(patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    anyString(), anyString(), any())).thenReturn(patientEntities);
            try (var patientMapperMock = Mockito.mockStatic(PatientMapper.class)) {
                patientMapperMock.when(() -> PatientMapper.toDto(any())).thenReturn(patientDto);
                // when
                var result = patientService.getPatients(name, page, size);
                // then
                assertThat(result.patients).isEmpty();
                assertThat(result.meta.getTotalPages()).isEqualTo(0);
                assertThat(result.meta.getTotalItems()).isEqualTo(0);
            }
        }

        @Test
        void shouldThrowForInvalidPageOrSize() {
            assertThrows(IllegalArgumentException.class, () -> patientService.getPatients(null, -1, 10));
            assertThrows(IllegalArgumentException.class, () -> patientService.getPatients(null, 0, 0));
        }
    }

    @Nested
    @DisplayName("updatePatient")
    class UpdatePatient {
        @Test
        void shouldUpdatePatient() {
            // given
            when(patientRepository.existsById(eq(id))).thenReturn(true);
            when(patientRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(id))).thenReturn(false);
            when(patientRepository.findById(eq(id))).thenReturn(Optional.of(patientEntity));
            when(patientRepository.save(any())).thenReturn(patientEntity);
            try (var patientMapperMock = Mockito.mockStatic(PatientMapper.class);
                 var addressMapperMock = Mockito.mockStatic(com.sm.patientservice.mapper.AddressMapper.class);
                 var insuranceMapperMock = Mockito.mockStatic(com.sm.patientservice.mapper.InsuranceMapper.class)) {
                patientMapperMock.when(() -> PatientMapper.toDto(any())).thenReturn(patientDto);
                addressMapperMock.when(() -> com.sm.patientservice.mapper.AddressMapper.toEntity(any())).thenReturn(new Address());
                insuranceMapperMock.when(() -> com.sm.patientservice.mapper.InsuranceMapper.toEntity(any())).thenReturn(new Insurance());
                // when
                var result = patientService.updatePatient(id, request);
                // then
                assertThat(result).isEqualTo(patientDto);
            }
        }

        @Test
        void shouldThrowIfPatientNotFound() {
            when(patientRepository.existsById(eq(id))).thenReturn(false);
            assertThrows(PatientNotExistException.class, () -> patientService.updatePatient(id, request));
        }

        @Test
        void shouldThrowIfEmailExists() {
            when(patientRepository.existsById(eq(id))).thenReturn(true);
            when(patientRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(id))).thenReturn(true);
            assertThrows(EmailAlreadyExistException.class, () -> patientService.updatePatient(id, request));
        }
    }


} 