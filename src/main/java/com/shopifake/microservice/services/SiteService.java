package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.AlternativeSlugSuggestion;
import com.shopifake.microservice.dtos.CreateSiteRequest;
import com.shopifake.microservice.dtos.SiteResponse;
import com.shopifake.microservice.dtos.SiteSlugResponse;
import com.shopifake.microservice.dtos.UpdateSiteRequest;
import com.shopifake.microservice.entities.Currency;
import com.shopifake.microservice.entities.Language;
import com.shopifake.microservice.entities.Site;
import com.shopifake.microservice.entities.SiteStatus;
import com.shopifake.microservice.repositories.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing sites.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteService {

    private static final int MAX_SLUG_GENERATION_ATTEMPTS = 100;
    private static final SiteStatus DEFAULT_STATUS = SiteStatus.DRAFT;

    private final SiteRepository siteRepository;
    private final SlugService slugService;
    private final SiteConfigValidationService configValidationService;

    /**
     * Create a new site.
     *
     * @param request the site creation request
     * @param ownerId the owner ID
     * @return the created site response
     * @throws IllegalArgumentException if slug is already taken
     */
    @Transactional
    public SiteResponse createSite(final CreateSiteRequest request, final Long ownerId) {
        log.info("Creating site for owner: {}", ownerId);

        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = slugService.generateSlug(request.getName());
            log.debug("Generated slug from name: {}", slug);
        } else {
            slug = slugService.normalizeSlug(slug);
        }

        if (siteRepository.existsBySlug(slug)) {
            log.warn("Slug already taken: {}", slug);
            throw new IllegalArgumentException("Slug already taken: " + slug);
        }

        if (request.getConfig().isEmpty()) {
            log.warn("Config is empty");
            throw new IllegalArgumentException("Config is empty");
        }
        
        configValidationService.validateAndParse(request.getConfig());
        String validatedConfig = request.getConfig();

        // Convert String to enum types
        Currency currency;
        try {
            currency = Currency.valueOf(request.getCurrency().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid currency: {}", request.getCurrency());
            throw new IllegalArgumentException("Invalid currency: " + request.getCurrency(), e);
        }

        Language language;
        try {
            language = Language.valueOf(request.getLanguage().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid language: {}", request.getLanguage());
            throw new IllegalArgumentException("Invalid language: " + request.getLanguage(), e);
        }

        Site site = Site.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .currency(currency)
                .language(language)
                .status(DEFAULT_STATUS)
                .ownerId(ownerId)
                .config(validatedConfig)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            Site savedSite = siteRepository.save(site);
            log.info("Site created successfully with ID: {}", savedSite.getId());
            return mapToResponse(savedSite);
        } catch (Exception e) {
            log.error("Error creating site for owner: {}", ownerId, e);
            throw new RuntimeException("Failed to create site due to database error", e);
        }
    }

    /**
     * Get a site by ID.
     *
     * @param siteId the site ID
     * @return the site response
     */
    public SiteResponse getSiteById(final UUID siteId) {
        log.debug("Fetching site with ID: {}", siteId);
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));
        return mapToResponse(site);
    }

    /**
     * Get the slug of a site by ID.
     *
     * @param siteId the site ID
     * @return the site slug response
     * @throws IllegalArgumentException if site not found
     */
    public SiteSlugResponse getSiteSlug(final UUID siteId) {
        log.debug("Fetching slug for site with ID: {}", siteId);
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));
        return SiteSlugResponse.builder()
                .slug(site.getSlug())
                .build();
    }

    /**
     * Update a site.
     *
     * @param siteId the site ID
     * @param request the update request
     * @return the updated site response
     * @throws IllegalArgumentException if site not found or invalid data
     */
    @Transactional
    public SiteResponse updateSite(final UUID siteId, final UpdateSiteRequest request) {
        log.info("Updating site: {}", siteId);

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            site.setName(request.getName());
        }

        // Update slug if provided
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String normalizedSlug = slugService.normalizeSlug(request.getSlug());
            // Check if slug is available (excluding current site)
            if (siteRepository.existsBySlug(normalizedSlug) 
                    && !site.getSlug().equals(normalizedSlug)) {
                log.warn("Slug already taken: {}", normalizedSlug);
                throw new IllegalArgumentException("Slug already taken: " + normalizedSlug);
            }
            site.setSlug(normalizedSlug);
        }

        // Update description if provided
        if (request.getDescription() != null) {
            site.setDescription(request.getDescription());
        }

        // Update currency if provided
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            Currency currency;
            try {
                currency = Currency.valueOf(request.getCurrency().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid currency: {}", request.getCurrency());
                throw new IllegalArgumentException("Invalid currency: " + request.getCurrency(), e);
            }
            site.setCurrency(currency);
        }

        // Update language if provided
        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            Language language;
            try {
                language = Language.valueOf(request.getLanguage().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid language: {}", request.getLanguage());
                throw new IllegalArgumentException("Invalid language: " + request.getLanguage(), e);
            }
            site.setLanguage(language);
        }

        // Update config if provided
        if (request.getConfig() != null) {
            if (request.getConfig().isEmpty()) {
                log.warn("Config is empty");
                throw new IllegalArgumentException("Config cannot be empty");
            }
            configValidationService.validateAndParse(request.getConfig());
            site.setConfig(request.getConfig());
        }

        site.setUpdatedAt(LocalDateTime.now());

        try {
            Site updatedSite = siteRepository.save(site);
            log.info("Site updated successfully with ID: {}", siteId);
            return mapToResponse(updatedSite);
        } catch (Exception e) {
            log.error("Error updating site with ID: {}", siteId, e);
            throw new RuntimeException("Failed to update site due to database error", e);
        }
    }

    /**
     * Update the status of a site.
     *
     * @param siteId the site ID
     * @param status the new status
     * @return the updated site response
     * @throws IllegalArgumentException if site not found or invalid status
     */
    @Transactional
    public SiteResponse updateSiteStatus(final UUID siteId, final String status) {
        log.info("Updating status for site: {} to {}", siteId, status);

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + siteId));

        SiteStatus newStatus;
        try {
            newStatus = SiteStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status: {}", status);
            throw new IllegalArgumentException("Invalid status: " + status, e);
        }

        if (site.getStatus() == SiteStatus.ACTIVE && newStatus == SiteStatus.DRAFT) {
            throw new IllegalArgumentException("Cannot update status from " + site.getStatus() + " to " + newStatus);
        }
        else if (site.getStatus() == SiteStatus.DISABLED && newStatus == SiteStatus.DRAFT) {
            throw new IllegalArgumentException("Cannot update status from " + site.getStatus() + " to " + newStatus);
        }
        site.setStatus(newStatus);
        site.setUpdatedAt(LocalDateTime.now());

        try {
            Site updatedSite = siteRepository.save(site);
            log.info("Site status updated successfully for site: {}", siteId);
            return mapToResponse(updatedSite);
        } catch (Exception e) {
            log.error("Error updating site status for site: {}", siteId, e);
            throw new RuntimeException("Failed to update site status due to database error", e);
        }
    }

    /**
     * Get all sites owned by a specific owner.
     *
     * @param ownerId the owner ID
     * @return list of site responses
     */
    public List<SiteResponse> getSitesByOwner(final Long ownerId) {
        log.debug("Fetching sites for owner: {}", ownerId);
        List<Site> sites = siteRepository.findByOwnerId(ownerId);
        return sites.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Suggest an alternative slug if the requested slug is already taken.
     *
     * @param requestedSlug the requested slug
     * @return alternative slug suggestion
     */
    public AlternativeSlugSuggestion suggestAlternativeSlug(final String requestedSlug) {
        log.debug("Suggesting alternative slug for: {}", requestedSlug);
        String normalizedSlug = slugService.normalizeSlug(requestedSlug);
        String suggestedSlug = findAvailableSlug(normalizedSlug);
        String message;
        if (suggestedSlug.equals(normalizedSlug)) {
            message = "The slug '" + normalizedSlug + "' is available.";
        } else {
            message = "The slug '" + normalizedSlug + "' is already taken. "
                    + "Suggested alternative: '" + suggestedSlug + "'";
        }

        return AlternativeSlugSuggestion.builder()
                .originalSlug(normalizedSlug)
                .suggestedSlug(suggestedSlug)
                .message(message)
                .build();
    }

    /**
     * Check if a slug is available.
     *
     * @param slug the slug to check
     * @return true if the slug is available
     */
    public boolean isSlugAvailable(final String slug) {
        String normalizedSlug = slugService.normalizeSlug(slug);
        return !siteRepository.existsBySlug(normalizedSlug);
    }

    /**
     * Delete a site by ID.
     *
     * @param siteId the site ID
     * @throws IllegalArgumentException if site not found
     */
    @Transactional
    public void deleteSite(final UUID siteId) {
        log.info("Deleting site with ID: {}", siteId);

        if (!siteRepository.existsById(siteId)) {
            log.warn("Site not found with ID: {}", siteId);
            throw new IllegalArgumentException("Site not found with ID: " + siteId);
        }

        try {
            siteRepository.deleteById(siteId);
            log.info("Site deleted successfully with ID: {}", siteId);
        } catch (Exception e) {
            log.error("Error deleting site with ID: {}", siteId, e);
            throw new RuntimeException("Failed to delete site due to database error", e);
        }
    }

    /**
     * Find an available slug by appending numbers if needed.
     *
     * @param baseSlug the base slug
     * @return an available slug
     */
    private String findAvailableSlug(final String baseSlug) {
        if (!siteRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }

        for (int i = 1; i <= MAX_SLUG_GENERATION_ATTEMPTS; i++) {
            String candidateSlug = baseSlug + "-" + i;
            if (!siteRepository.existsBySlug(candidateSlug)) {
                return candidateSlug;
            }
        }

        // Fallback: use timestamp if all attempts fail
        return baseSlug + "-" + System.currentTimeMillis();
    }

    /**
     * Map Site entity to SiteResponse DTO.
     *
     * @param site the site entity
     * @return the site response DTO
     */
    private SiteResponse mapToResponse(final Site site) {
        return SiteResponse.builder()
                .id(site.getId())
                .name(site.getName())
                .slug(site.getSlug())
                .description(site.getDescription())
                .currency(site.getCurrency())
                .language(site.getLanguage())
                .status(site.getStatus())
                .ownerId(site.getOwnerId())
                .config(site.getConfig())
                .createdAt(site.getCreatedAt())
                .updatedAt(site.getUpdatedAt())
                .build();
    }
}

