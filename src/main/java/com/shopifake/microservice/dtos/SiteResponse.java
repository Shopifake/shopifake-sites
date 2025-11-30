package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.Currency;
import com.shopifake.microservice.entities.Language;
import com.shopifake.microservice.entities.SiteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for site response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private Currency currency;
    private Language language;
    private SiteStatus status;
    private UUID ownerId;
    private String config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

