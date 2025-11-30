package com.shopifake.microservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopifake.microservice.dtos.SiteConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for validating site configuration JSON structure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteConfigValidationService {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * Validate and parse site configuration JSON.
     *
     * @param jsonConfig the JSON string to validate
     * @return the parsed SiteConfig object
     * @throws IllegalArgumentException if the JSON is invalid or doesn't match the structure
     */
    public SiteConfig validateAndParse(final String jsonConfig) {
        if (jsonConfig == null || jsonConfig.isBlank()) {
            throw new IllegalArgumentException("Site configuration JSON cannot be null or empty");
        }

        try {
            SiteConfig config = objectMapper.readValue(jsonConfig, SiteConfig.class);
            validateStructure(config);
            return config;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse site configuration JSON", e);
            throw new IllegalArgumentException("Invalid JSON format: " + e.getMessage(), e);
        }
    }

    /**
     * Validate the structure of the SiteConfig object.
     *
     * @param config the SiteConfig to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateStructure(final SiteConfig config) {
        Set<ConstraintViolation<SiteConfig>> violations = validator.validate(config);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Site configuration validation failed: {}", errorMessage);
            throw new IllegalArgumentException("Site configuration validation failed: " + errorMessage);
        }

        // Additional validation for values list
        if (config.getValues() != null && config.getValues().isEmpty()) {
            throw new IllegalArgumentException("Values list cannot be empty");
        }
    }

    /**
     * Convert SiteConfig to JSON string.
     *
     * @param config the SiteConfig to convert
     * @return JSON string representation
     * @throws IllegalArgumentException if conversion fails
     */
    public String toJson(final SiteConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert SiteConfig to JSON", e);
            throw new IllegalArgumentException("Failed to convert configuration to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a JSON string is valid site configuration.
     *
     * @param jsonConfig the JSON string to check
     * @return true if valid, false otherwise
     */
    public boolean isValid(final String jsonConfig) {
        try {
            validateAndParse(jsonConfig);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

