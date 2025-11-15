package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a site.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSiteRequest {

    @Size(max = 255, message = "Site name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;

    private String config;
}

