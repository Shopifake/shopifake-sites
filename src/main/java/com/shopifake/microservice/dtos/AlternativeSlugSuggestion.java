package com.shopifake.microservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for suggesting alternative slugs when the requested slug is already taken.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlternativeSlugSuggestion {

    private String originalSlug;
    private String suggestedSlug;
    private String message;
}

