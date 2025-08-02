package com.sm.integrationtest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import com.sm.integrationtest.dto.Patient;

/**
 * Integration tests for the Patient API endpoints.
 * These tests verify the functionality of patient-related operations.
 */
@TestMethodOrder(OrderAnnotation.class)
public class PatientIntegrationTest extends BaseIntegrationTest {

    @BeforeAll
    public static void setupPatient() {
        useApiPath();
    }

    @Test
    @Order(1)
    @DisplayName("Should return list of patients when authenticated with valid token")
    public void shouldReturnPatientsWhenValidToken() {
        // Given
        String token = getAuthToken(NORMAL_USER, PASSWORD);
        
        // When
        Response patientsResponse = performGetPatientsRequest(token);

        System.out.println("Patients Response: " + patientsResponse.asString());
        
        // Then
        validateSuccessResponse(patientsResponse);
        validatePatientsData(patientsResponse);
        validatePaginationMetadata(patientsResponse);
    }

    @Test
    @Order(2)
    @DisplayName("Should return 401 when accessing patients with invalid token")
    public void shouldReturnUnauthorizedWhenInvalidToken() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer invalid_token")
        .when()
            .get("/patients")
        .then()
            .statusCode(401);
    }

    @Override
    protected String getAuthToken(String email, String password) {
        String token = super.getAuthToken(email, password);
        useApiPath();
        return token;
    }

    private Response performGetPatientsRequest(String token) {
        return RestAssured
            .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/patients?name=John&page=0&size=5")
            .then()
                .extract()
                .response();
    }

    private void validateSuccessResponse(Response response) {
        response.then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("success"),
                  "timestamp", notNullValue());
    }

    private void validatePatientsData(Response response) {
        response.then()
            .body("data", notNullValue())
            .body("data", hasSize(greaterThan(0)))
            .body("data[0].id", matchesPattern("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"))
            .body("data[0].firstName", notNullValue())
            .body("data[0].lastName", notNullValue())
            .body("data[0].dob", matchesPattern("^\\d{4}-\\d{2}-\\d{2}$"))
            .body("data[0].email", matchesPattern("^[A-Za-z0-9+_.-]+@(.+)$"))
            .body("data[0].phoneNumber", notNullValue())
            .body("data[0].gender", anyOf(equalTo("MALE"), equalTo("FEMALE")))
            .body("data[0].address.street", notNullValue())
            .body("data[0].address.city", notNullValue())
            .body("data[0].address.state", notNullValue())
            .body("data[0].address.zipCode", notNullValue())
            .body("data[0].address.country", notNullValue())
            .body("data[0].insurance.provider", notNullValue())
            .body("data[0].insurance.policyNumber", notNullValue())
            .body("data[0].createdAt", notNullValue())
            .body("data[0].updatedAt", notNullValue());
        
        // Extract and verify typed data
        List<Patient> patients = response.jsonPath().getList("data", Patient.class);
        assertNotNull(patients, "Patient list should not be null");
        assertFalse(patients.isEmpty(), "Patient list should not be empty");
        
        // Verify first patient's data structure
        Patient firstPatient = patients.get(0);
        assertNotNull(firstPatient.getId(), "Patient ID should not be null");
        assertNotNull(firstPatient.getAddress(), "Patient address should not be null");
        assertNotNull(firstPatient.getInsurance(), "Patient insurance should not be null");
    }

    private void validatePaginationMetadata(Response response) {
        response.then()
            .body("meta", notNullValue())
            .body("meta.page", greaterThanOrEqualTo(1))
            .body("meta.size", greaterThanOrEqualTo(1))
            .body("meta.totalPages", greaterThanOrEqualTo(1))
            .body("meta.totalItems", greaterThanOrEqualTo(0));
    }
}

