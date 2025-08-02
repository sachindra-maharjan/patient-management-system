package com.sm.integrationtest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.response.Response;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.equalTo;

/**
 * Integration tests for the authentication service.
 * Tests various authentication scenarios including login and token validation.
 */
public class AuthIntegrationTest extends BaseIntegrationTest {

    private static final String INVALID_PASSWORD = "wrongpassword";

    @BeforeAll
    public static void setupAuth() {
        useAuthPath();
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    public void shouldReturnOkWhenValidCredentials() {
        Response response = performLogin(ADMIN_USER, PASSWORD);
        validateLoginResponse(response);
        response.then().body("roles", hasItem("ROLE_ADMIN"));
    }
    
    @Test
    @DisplayName("Should return unauthorized when login with invalid credentials")
    public void shouldReturnUnauthorizedWhenInvalidCredentials() {
        givenLoginRequestPayload(ADMIN_USER, INVALID_PASSWORD);
        
        given()
            .contentType(ContentType.JSON)
            .body(loginRequestPayload)
        .when()
            .post("/login")
        .then()
            .statusCode(401)
            .body("status", isA(String.class))
            .body("message", isA(String.class))
            .body("timestamp", isA(String.class));
    }

    @Test
    @DisplayName("Should validate token successfully when token is valid")
    public void shouldReturnOkWhenValidToken() {
        // Get valid token
        String token = getAuthToken(ADMIN_USER, PASSWORD);
        
        // Validate token
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/validate")
        .then()
            .statusCode(200)
            .body("valid", isA(Boolean.class))
            .body("email", equalTo(ADMIN_USER))
            .body("roles", hasItem("ROLE_ADMIN"));
    }

}
