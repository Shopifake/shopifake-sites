package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CreateSiteRequest;
import com.shopifake.microservice.dtos.SiteConfig;
import com.shopifake.microservice.dtos.SiteResponse;
import com.shopifake.microservice.dtos.UpdateSiteRequest;
import com.shopifake.microservice.entities.Currency;
import com.shopifake.microservice.entities.Language;
import com.shopifake.microservice.entities.Site;
import com.shopifake.microservice.entities.SiteStatus;
import com.shopifake.microservice.repositories.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SiteService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SiteService Tests")
class SiteServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SlugService slugService;

    @Mock
    private SiteConfigValidationService configValidationService;

    @InjectMocks
    private SiteService siteService;

    private UUID testSiteId;
    private UUID testOwnerId;
    private Site testSite;
    private CreateSiteRequest createRequest;
    private String validConfigJson;

    @BeforeEach
    void setUp() {
        testSiteId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();
        validConfigJson = "{\"bannerUrl\":\"https://example.com/banner.jpg\","
                + "\"name\":\"Test Site\",\"title\":\"Test Title\","
                + "\"subtitle\":\"Test Subtitle\",\"heroDescription\":\"Test Description\","
                + "\"logoUrl\":\"https://example.com/logo.png\","
                + "\"aboutPortraitOneUrl\":\"https://example.com/portrait1.jpg\","
                + "\"aboutLandscapeUrl\":\"https://example.com/landscape.jpg\","
                + "\"aboutPortraitTwoUrl\":\"https://example.com/portrait2.jpg\","
                + "\"history\":\"Test history\",\"values\":[\"Value 1\",\"Value 2\"],"
                + "\"contactHeading\":\"Contact\",\"contactDescription\":\"Contact desc\","
                + "\"contactDetails\":\"Details\",\"contactExtraNote\":\"Note\","
                + "\"primaryColor\":\"#000000\",\"secondaryColor\":\"#FFFFFF\"}";

        testSite = Site.builder()
                .id(testSiteId)
                .name("Test Site")
                .slug("test-site")
                .description("Test Description")
                .currency(Currency.USD)
                .language(Language.EN)
                .status(SiteStatus.DRAFT)
                .ownerId(testOwnerId)
                .config(validConfigJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateSiteRequest.builder()
                .name("Test Site")
                .slug("test-site")
                .description("Test Description")
                .currency("USD")
                .language("EN")
                .config(validConfigJson)
                .build();
    }

    @Test
    @DisplayName("Should create site successfully with provided slug")
    void shouldCreateSiteWithProvidedSlug() {
        // Given
        when(slugService.normalizeSlug("test-site")).thenReturn("test-site");
        when(siteRepository.existsBySlug("test-site")).thenReturn(false);
        when(configValidationService.validateAndParse(validConfigJson))
                .thenReturn(SiteConfig.builder().build());
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        // When
        SiteResponse response = siteService.createSite(createRequest, testOwnerId);

        // Then
        assertNotNull(response);
        assertEquals(testSiteId, response.getId());
        assertEquals("Test Site", response.getName());
        assertEquals("test-site", response.getSlug());
        assertEquals(Currency.USD, response.getCurrency());
        assertEquals(Language.EN, response.getLanguage());
        assertEquals(SiteStatus.DRAFT, response.getStatus());
        verify(slugService).normalizeSlug("test-site");
        verify(siteRepository).existsBySlug("test-site");
        verify(configValidationService).validateAndParse(validConfigJson);
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should create site with auto-generated slug when slug is not provided")
    void shouldCreateSiteWithAutoGeneratedSlug() {
        // Given
        createRequest.setSlug(null);
        when(slugService.generateSlug("Test Site")).thenReturn("test-site");
        when(siteRepository.existsBySlug("test-site")).thenReturn(false);
        when(configValidationService.validateAndParse(validConfigJson))
                .thenReturn(SiteConfig.builder().build());
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        // When
        SiteResponse response = siteService.createSite(createRequest, testOwnerId);

        // Then
        assertNotNull(response);
        verify(slugService).generateSlug("Test Site");
        verify(slugService, never()).normalizeSlug(anyString());
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw exception when slug is already taken")
    void shouldThrowExceptionWhenSlugIsTaken() {
        // Given
        when(slugService.normalizeSlug("test-site")).thenReturn("test-site");
        when(siteRepository.existsBySlug("test-site")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.createSite(createRequest, testOwnerId)
        );
        assertEquals("Slug already taken: test-site", exception.getMessage());
        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw exception when config is empty")
    void shouldThrowExceptionWhenConfigIsEmpty() {
        // Given
        createRequest.setConfig("");
        when(slugService.normalizeSlug("test-site")).thenReturn("test-site");
        when(siteRepository.existsBySlug("test-site")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.createSite(createRequest, testOwnerId)
        );
        assertEquals("Config is empty", exception.getMessage());
        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw exception when currency is invalid")
    void shouldThrowExceptionWhenCurrencyIsInvalid() {
        // Given
        createRequest.setCurrency("INVALID");
        when(slugService.normalizeSlug("test-site")).thenReturn("test-site");
        when(siteRepository.existsBySlug("test-site")).thenReturn(false);
        when(configValidationService.validateAndParse(validConfigJson))
                .thenReturn(any());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.createSite(createRequest, testOwnerId)
        );
        assertTrue(exception.getMessage().contains("Invalid currency"));
        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw exception when language is invalid")
    void shouldThrowExceptionWhenLanguageIsInvalid() {
        // Given
        createRequest.setLanguage("INVALID");
        when(slugService.normalizeSlug("test-site")).thenReturn("test-site");
        when(siteRepository.existsBySlug("test-site")).thenReturn(false);
        when(configValidationService.validateAndParse(validConfigJson))
                .thenReturn(SiteConfig.builder().build());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.createSite(createRequest, testOwnerId)
        );
        assertTrue(exception.getMessage().contains("Invalid language"));
        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    @DisplayName("Should get site by ID successfully")
    void shouldGetSiteById() {
        // Given
        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));

        // When
        SiteResponse response = siteService.getSiteById(testSiteId);

        // Then
        assertNotNull(response);
        assertEquals(testSiteId, response.getId());
        assertEquals("Test Site", response.getName());
        verify(siteRepository).findById(testSiteId);
    }

    @Test
    @DisplayName("Should throw exception when site not found")
    void shouldThrowExceptionWhenSiteNotFound() {
        // Given
        when(siteRepository.findById(testSiteId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.getSiteById(testSiteId)
        );
        assertTrue(exception.getMessage().contains("Site not found"));
    }

    @Test
    @DisplayName("Should update site successfully")
    void shouldUpdateSite() {
        // Given
        UpdateSiteRequest updateRequest = UpdateSiteRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        Site updatedSite = Site.builder()
                .id(testSiteId)
                .name("Updated Name")
                .slug("test-site")
                .description("Updated Description")
                .currency(Currency.USD)
                .language(Language.EN)
                .status(SiteStatus.DRAFT)
                .ownerId(testOwnerId)
                .config(validConfigJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(Site.class))).thenReturn(updatedSite);

        // When
        SiteResponse response = siteService.updateSite(testSiteId, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("Updated Name", response.getName());
        assertEquals("Updated Description", response.getDescription());
        verify(siteRepository).findById(testSiteId);
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should update site slug when provided and available")
    void shouldUpdateSiteSlugWhenAvailable() {
        // Given
        UpdateSiteRequest updateRequest = UpdateSiteRequest.builder()
                .slug("new-slug")
                .build();

        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));
        when(slugService.normalizeSlug("new-slug")).thenReturn("new-slug");
        when(siteRepository.existsBySlug("new-slug")).thenReturn(false);
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        // When
        siteService.updateSite(testSiteId, updateRequest);

        // Then
        verify(slugService).normalizeSlug("new-slug");
        verify(siteRepository).existsBySlug("new-slug");
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw exception when updating slug to already taken slug")
    void shouldThrowExceptionWhenUpdatingToTakenSlug() {
        // Given
        UpdateSiteRequest updateRequest = UpdateSiteRequest.builder()
                .slug("taken-slug")
                .build();

        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));
        when(slugService.normalizeSlug("taken-slug")).thenReturn("taken-slug");
        when(siteRepository.existsBySlug("taken-slug")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.updateSite(testSiteId, updateRequest)
        );
        assertTrue(exception.getMessage().contains("Slug already taken"));
        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    @DisplayName("Should update site status successfully")
    void shouldUpdateSiteStatus() {
        // Given
        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        // When
        SiteResponse response = siteService.updateSiteStatus(testSiteId, "ACTIVE");

        // Then
        assertNotNull(response);
        verify(siteRepository).findById(testSiteId);
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to invalid status")
    void shouldThrowExceptionWhenUpdatingToInvalidStatus() {
        // Given
        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.updateSiteStatus(testSiteId, "INVALID")
        );
        assertTrue(exception.getMessage().contains("Invalid status"));
        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    @DisplayName("Should get sites by owner successfully")
    void shouldGetSitesByOwner() {
        // Given
        Site site2 = Site.builder()
                .id(UUID.randomUUID())
                .name("Site 2")
                .slug("site-2")
                .description("Description 2")
                .currency(Currency.EUR)
                .language(Language.FR)
                .status(SiteStatus.ACTIVE)
                .ownerId(testOwnerId)
                .config(validConfigJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Site> sites = Arrays.asList(testSite, site2);
        when(siteRepository.findByOwnerId(testOwnerId)).thenReturn(sites);

        // When
        List<SiteResponse> responses = siteService.getSitesByOwner(testOwnerId);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(siteRepository).findByOwnerId(testOwnerId);
    }

    @Test
    @DisplayName("Should suggest alternative slug when slug is taken")
    void shouldSuggestAlternativeSlugWhenTaken() {
        // Given
        when(slugService.normalizeSlug("test-site")).thenReturn("test-site");
        when(siteRepository.existsBySlug("test-site")).thenReturn(true);
        when(siteRepository.existsBySlug("test-site-1")).thenReturn(false);

        // When
        var suggestion = siteService.suggestAlternativeSlug("test-site");

        // Then
        assertNotNull(suggestion);
        assertEquals("test-site", suggestion.getOriginalSlug());
        assertEquals("test-site-1", suggestion.getSuggestedSlug());
        assertTrue(suggestion.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Should return available message when slug is available")
    void shouldReturnAvailableMessageWhenSlugIsAvailable() {
        // Given
        when(slugService.normalizeSlug("available-slug")).thenReturn("available-slug");
        when(siteRepository.existsBySlug("available-slug")).thenReturn(false);

        // When
        var suggestion = siteService.suggestAlternativeSlug("available-slug");

        // Then
        assertNotNull(suggestion);
        assertEquals("available-slug", suggestion.getOriginalSlug());
        assertEquals("available-slug", suggestion.getSuggestedSlug());
        assertTrue(suggestion.getMessage().contains("available"));
    }

    @Test
    @DisplayName("Should return true when slug is available")
    void shouldReturnTrueWhenSlugIsAvailable() {
        // Given
        when(slugService.normalizeSlug("available-slug")).thenReturn("available-slug");
        when(siteRepository.existsBySlug("available-slug")).thenReturn(false);

        // When
        boolean isAvailable = siteService.isSlugAvailable("available-slug");

        // Then
        assertTrue(isAvailable);
        verify(slugService).normalizeSlug("available-slug");
        verify(siteRepository).existsBySlug("available-slug");
    }

    @Test
    @DisplayName("Should return false when slug is not available")
    void shouldReturnFalseWhenSlugIsNotAvailable() {
        // Given
        when(slugService.normalizeSlug("taken-slug")).thenReturn("taken-slug");
        when(siteRepository.existsBySlug("taken-slug")).thenReturn(true);

        // When
        boolean isAvailable = siteService.isSlugAvailable("taken-slug");

        // Then
        assertFalse(isAvailable);
    }

    @Test
    @DisplayName("Should delete site successfully")
    void shouldDeleteSite() {
        // Given
        when(siteRepository.existsById(testSiteId)).thenReturn(true);

        // When
        siteService.deleteSite(testSiteId);

        // Then
        verify(siteRepository).existsById(testSiteId);
        verify(siteRepository).deleteById(testSiteId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent site")
    void shouldThrowExceptionWhenDeletingNonExistentSite() {
        // Given
        when(siteRepository.existsById(testSiteId)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.deleteSite(testSiteId)
        );
        assertTrue(exception.getMessage().contains("Site not found"));
        verify(siteRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should update currency when provided")
    void shouldUpdateCurrency() {
        // Given
        UpdateSiteRequest updateRequest = UpdateSiteRequest.builder()
                .currency("EUR")
                .build();

        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        // When
        siteService.updateSite(testSiteId, updateRequest);

        // Then
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should update language when provided")
    void shouldUpdateLanguage() {
        // Given
        UpdateSiteRequest updateRequest = UpdateSiteRequest.builder()
                .language("FR")
                .build();

        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        // When
        siteService.updateSite(testSiteId, updateRequest);

        // Then
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should update config when provided")
    void shouldUpdateConfig() {
        // Given
        UpdateSiteRequest updateRequest = UpdateSiteRequest.builder()
                .config(validConfigJson)
                .build();

        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));
        when(configValidationService.validateAndParse(validConfigJson))
                .thenReturn(SiteConfig.builder().build());
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        // When
        siteService.updateSite(testSiteId, updateRequest);

        // Then
        verify(configValidationService).validateAndParse(validConfigJson);
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw exception when updating config to empty")
    void shouldThrowExceptionWhenUpdatingConfigToEmpty() {
        // Given
        UpdateSiteRequest updateRequest = UpdateSiteRequest.builder()
                .config("")
                .build();

        when(siteRepository.findById(testSiteId)).thenReturn(Optional.of(testSite));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> siteService.updateSite(testSiteId, updateRequest)
        );
        assertTrue(exception.getMessage().contains("Config cannot be empty"));
        verify(siteRepository, never()).save(any(Site.class));
    }
}

