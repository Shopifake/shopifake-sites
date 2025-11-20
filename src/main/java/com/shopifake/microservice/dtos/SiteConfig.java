package com.shopifake.microservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing the site configuration structure.
 * This matches the TypeScript SiteDraft interface.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfig {

    @NotBlank(message = "Banner URL is required")
    @JsonProperty("bannerUrl")
    private String bannerUrl;

    @NotBlank(message = "Name is required")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Title is required")
    @JsonProperty("title")
    private String title;

    @NotBlank(message = "Subtitle is required")
    @JsonProperty("subtitle")
    private String subtitle;

    @NotBlank(message = "Hero description is required")
    @JsonProperty("heroDescription")
    private String heroDescription;

    @NotBlank(message = "Logo URL is required")
    @JsonProperty("logoUrl")
    private String logoUrl;

    @NotBlank(message = "About portrait one URL is required")
    @JsonProperty("aboutPortraitOneUrl")
    private String aboutPortraitOneUrl;

    @NotBlank(message = "About landscape URL is required")
    @JsonProperty("aboutLandscapeUrl")
    private String aboutLandscapeUrl;

    @NotBlank(message = "About portrait two URL is required")
    @JsonProperty("aboutPortraitTwoUrl")
    private String aboutPortraitTwoUrl;

    @NotBlank(message = "History is required")
    @JsonProperty("history")
    private String history;

    @NotNull(message = "Values list is required")
    @JsonProperty("values")
    private List<String> values;

    @NotBlank(message = "Contact heading is required")
    @JsonProperty("contactHeading")
    private String contactHeading;

    @NotBlank(message = "Contact description is required")
    @JsonProperty("contactDescription")
    private String contactDescription;

    @NotBlank(message = "Contact details is required")
    @JsonProperty("contactDetails")
    private String contactDetails;

    @JsonProperty("contactExtraNote")
    private String contactExtraNote;

    @NotBlank(message = "Primary color is required")
    @JsonProperty("primaryColor")
    private String primaryColor;

    @NotBlank(message = "Secondary color is required")
    @JsonProperty("secondaryColor")
    private String secondaryColor;
}

