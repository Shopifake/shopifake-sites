package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating site status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSiteStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;
}

