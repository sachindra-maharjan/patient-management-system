package com.sm.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;

@Configuration
public class HttpConfig {
  
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(Feature.IGNORE_UNKNOWN, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)      // Exclude null values
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)     // Exclude empty collections/strings
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);  // Use ISO-8601 date format
        return objectMapper;
    }
  
}
