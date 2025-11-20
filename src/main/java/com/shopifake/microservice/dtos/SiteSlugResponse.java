package com.shopifake.microservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for site slug response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteSlugResponse {

    private String slug;
}

