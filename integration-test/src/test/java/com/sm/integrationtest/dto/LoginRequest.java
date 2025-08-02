package com.sm.integrationtest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {
    @JsonProperty("email")
    private final String email;
    
    @JsonProperty("password")
    private final String password;

    private LoginRequest(Builder builder) {
        this.email = builder.email;
        this.password = builder.password;
    }

    public static class Builder {
        private String email;
        private String password;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
