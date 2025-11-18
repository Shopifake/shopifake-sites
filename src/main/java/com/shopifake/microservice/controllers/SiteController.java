package com.shopifake.microservice.controllers;

import com.shopifake.microservice.dtos.AlternativeSlugSuggestion;
import com.shopifake.microservice.dtos.CreateSiteRequest;
import com.shopifake.microservice.dtos.CurrenciesResponse;
import com.shopifake.microservice.dtos.LanguagesResponse;
import com.shopifake.microservice.dtos.SiteResponse;
import com.shopifake.microservice.dtos.SiteSlugResponse;
import com.shopifake.microservice.dtos.SlugAvailabilityResponse;
import com.shopifake.microservice.dtos.UpdateSiteRequest;
import com.shopifake.microservice.dtos.UpdateSiteStatusRequest;
import com.shopifake.microservice.entities.Currency;
import com.shopifake.microservice.entities.Language;
import com.shopifake.microservice.services.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for site management operations.
 */
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sites", description = "API for managing sites")
public class SiteController {

    /** The site service for business logic operations. */
    private final SiteService siteService;

    /**
     * Create a new site.
     *
     * @param request the site creation request
     * @param ownerId the owner ID from header
     * @return the created site response
     */
    @PostMapping
    @Operation(summary = "Create a new site", description = "Creates a new site with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Site created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or slug already taken"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SiteResponse> createSite(
            @Valid @RequestBody final CreateSiteRequest request,
            @RequestHeader(value = "X-Owner-Id") final UUID ownerId) {

        log.info("Received request to create site: {}", request.getName());

        try {
            SiteResponse response = siteService.createSite(request, ownerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create site: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get a site by ID.
     *
     * @param siteId the site ID
     * @return the site response
     */
    @GetMapping("/{siteId}")
    @Operation(summary = "Get site by ID", description = "Retrieves a site by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Site found"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteResponse> getSiteById(
            @Parameter(description = "Site ID") @PathVariable final UUID siteId) {

        log.debug("Fetching site with ID: {}", siteId);
        SiteResponse response = siteService.getSiteById(siteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a site by slug.
     *
     * @param slug the site slug
     * @return the site response
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get site by slug", description = "Retrieves a site by its slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Site found"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteResponse> getSiteBySlug(
            @Parameter(description = "Site slug") @PathVariable final String slug) {

        log.debug("Fetching site with slug: {}", slug);
        SiteResponse response = siteService.getSiteBySlug(slug);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the slug of a site by ID.
     *
     * @param siteId the site ID
     * @return the site slug response
     */
    @GetMapping("/{siteId}/slug")
    @Operation(summary = "Get site slug", description = "Retrieves only the slug of a site by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Site slug found"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteSlugResponse> getSiteSlug(
            @Parameter(description = "Site ID") @PathVariable final UUID siteId) {

        log.debug("Fetching slug for site with ID: {}", siteId);
        SiteSlugResponse response = siteService.getSiteSlug(siteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a site.
     *
     * @param siteId the site ID
     * @param request the update request
     * @return the updated site response
     */
    @PatchMapping("/{siteId}")
    @Operation(summary = "Update a site", description = "Updates site fields (name, slug, description, currency, language, config)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Site updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or site not found"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteResponse> updateSite(
            @Parameter(description = "Site ID") @PathVariable final UUID siteId,
            @Valid @RequestBody final UpdateSiteRequest request) {

        log.info("Updating site: {}", siteId);
        SiteResponse response = siteService.updateSite(siteId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update the status of a site.
     *
     * @param siteId the site ID
     * @param request the status update request
     * @return the updated site response
     */
    @PatchMapping("/{siteId}/status")
    @Operation(summary = "Update site status", description = "Updates only the status of a site")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Site status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or site not found"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteResponse> updateSiteStatus(
            @Parameter(description = "Site ID") @PathVariable final UUID siteId,
            @Valid @RequestBody final UpdateSiteStatusRequest request) {

        log.info("Updating status for site: {}", siteId);
        SiteResponse response = siteService.updateSiteStatus(siteId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all sites for an owner.
     *
     * @param ownerId the owner ID
     * @return list of site responses
     */
    @GetMapping
    @Operation(summary = "Get sites by owner", description = "Retrieves all sites owned by a specific owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sites retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Owner ID is required")
    })
    public ResponseEntity<List<SiteResponse>> getSitesByOwner(
            @Parameter(description = "Owner ID") @RequestParam(required = true) final UUID ownerId) {
        
        if (ownerId == null) {
            log.warn("Owner ID is required");
            return ResponseEntity.badRequest().build();
        }
        log.debug("Fetching sites for owner: {}", ownerId);
        List<SiteResponse> responses = siteService.getSitesByOwner(ownerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Suggest an alternative slug if the requested slug is already taken.
     *
     * @param slug the requested slug
     * @return alternative slug suggestion
     */
    @GetMapping("/suggest-slug")
    @Operation(summary = "Suggest alternative slug",
            description = "Suggests an alternative slug if the requested one is taken")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alternative slug suggested")
    })
    public ResponseEntity<AlternativeSlugSuggestion> suggestAlternativeSlug(
            @Parameter(description = "Requested slug") @RequestParam final String slug) {

        log.debug("Suggesting alternative slug for: {}", slug);
        AlternativeSlugSuggestion suggestion = siteService.suggestAlternativeSlug(slug);
        return ResponseEntity.ok(suggestion);
    }

    /**
     * Check if a slug is available.
     *
     * @param slug the slug to check
     * @return slug availability response
     */
    @GetMapping("/check-slug")
    @Operation(summary = "Check slug availability", description = "Checks if a slug is available")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slug availability checked")
    })
    public ResponseEntity<SlugAvailabilityResponse> checkSlugAvailability(
            @Parameter(description = "Slug to check") @RequestParam final String slug) {

        log.debug("Checking slug availability for: {}", slug);
        boolean isAvailable = siteService.isSlugAvailable(slug);
        
        SlugAvailabilityResponse response = SlugAvailabilityResponse.builder()
                .slug(slug)
                .available(isAvailable)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all supported languages.
     *
     * @return languages response with list of supported language codes
     */
    @GetMapping("/languages")
    @Operation(summary = "Get all languages", description = "Retrieves all supported language codes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Languages retrieved successfully")
    })
    public ResponseEntity<LanguagesResponse> getAllLanguages() {
        log.debug("Fetching all supported languages");
        List<String> languages = Arrays.stream(Language.values())
                .map(Enum::name)
                .toList();
        
        LanguagesResponse response = LanguagesResponse.builder()
                .languages(languages)
                .count(languages.size())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all supported currencies.
     *
     * @return currencies response with list of supported currency codes
     */
    @GetMapping("/currencies")
    @Operation(summary = "Get all currencies", description = "Retrieves all supported currency codes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currencies retrieved successfully")
    })
    public ResponseEntity<CurrenciesResponse> getAllCurrencies() {
        log.debug("Fetching all supported currencies");
        List<String> currencies = Arrays.stream(Currency.values())
                .map(Enum::name)
                .toList();
        
        CurrenciesResponse response = CurrenciesResponse.builder()
                .currencies(currencies)
                .count(currencies.size())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a site by ID.
     *
     * @param siteId the site ID
     * @return no content response
     */
    @DeleteMapping("/{siteId}")
    @Operation(summary = "Delete a site", description = "Deletes a site by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Site deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<Void> deleteSite(
            @Parameter(description = "Site ID") @PathVariable final UUID siteId) {

        log.info("Received request to delete site: {}", siteId);

        try {
            siteService.deleteSite(siteId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete site: {}", e.getMessage());
            throw e;
        }
    }
}

