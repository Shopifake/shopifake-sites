package com.shopifake.microservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for currencies response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrenciesResponse {

    private List<String> currencies;
    private int count;
}

