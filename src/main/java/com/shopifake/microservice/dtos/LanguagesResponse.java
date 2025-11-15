package com.shopifake.microservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for languages response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguagesResponse {

    private List<String> languages;
    private int count;
}

