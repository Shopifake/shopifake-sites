package com.shopifake.microservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for slug availability check response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlugAvailabilityResponse {

    private String slug;
    private boolean available;
}

