package com.sm.integrationtest;

import org.junit.jupiter.api.BeforeAll;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm.integrationtest.dto.LoginRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public abstract class BaseIntegrationTest {
    protected static final String ADMIN_USER = "admin@pms.com";
    protected static final String NORMAL_USER = "qa@pms.com";
    protected static final String PASSWORD = "password123";

    protected static final String BASE_URI = "http://localhost:4100";
    protected static final String AUTH_PATH = "/auth";
    protected static final String API_PATH = "/api";

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected String loginRequestPayload;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    protected static void useAuthPath() {
        RestAssured.basePath = AUTH_PATH;
    }

    protected static void useApiPath() {
        RestAssured.basePath = API_PATH;
    }

    protected String getAuthToken(String email, String password) {
        useAuthPath(); // Switch to auth path
        Response loginResponse = performLogin(email, password);
        validateLoginResponse(loginResponse);
        String token = extractTokenFromResponse(loginResponse);
        return token;
    }

    protected Response performLogin(String email, String password) {
        useAuthPath(); // Ensure we're using auth path
        givenLoginRequestPayload(email, password);
        return RestAssured.given()
              .contentType("application/json")
              .body(loginRequestPayload)
            .post("/login")
            .then()
              .statusCode(200)
              .extract()
              .response();
    }

    protected String extractTokenFromResponse(Response response) {
        return response.jsonPath().getString("token");
    }

    protected void validateLoginResponse(Response response) {
        response.then()
            .statusCode(200)
            .body("token", org.hamcrest.Matchers.notNullValue())
            .body("expiresIn", org.hamcrest.Matchers.notNullValue());
    }

    protected void givenLoginRequestPayload(String email, String password) {
        try {
            LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
            loginRequestPayload = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create login request payload", e);
        }
    }
}
