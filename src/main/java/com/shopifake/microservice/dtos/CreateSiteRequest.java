package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new site.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSiteRequest {

    @NotBlank(message = "Site name is required")
    @Size(max = 255, message = "Site name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;

    @NotBlank(message = "Language is required")
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;

    @NotBlank(message = "Config is required")
    private String config;
}

