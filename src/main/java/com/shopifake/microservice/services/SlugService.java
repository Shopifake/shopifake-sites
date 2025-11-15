package com.shopifake.microservice.services;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Service for slug generation and normalization.
 */
@Service
public class SlugService {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");

    /**
     * Generate a slug from a given text.
     *
     * @param text the text to generate slug from
     * @return the generated slug
     */
    public String generateSlug(final String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or blank");
        }

        return normalizeSlug(text);
    }

    /**
     * Normalize a slug by converting to lowercase, removing special characters,
     * and replacing spaces with dashes.
     *
     * @param slug the slug to normalize
     * @return the normalized slug
     */
    public String normalizeSlug(final String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Slug cannot be null or blank");
        }

        String normalized = Normalizer.normalize(slug, Normalizer.Form.NFD);
        normalized = NON_LATIN.matcher(normalized).replaceAll("");
        normalized = WHITESPACE.matcher(normalized).replaceAll("-");
        normalized = EDGES_DASHES.matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase(Locale.ENGLISH);

        // Remove consecutive dashes
        normalized = normalized.replaceAll("-+", "-");

        // Ensure slug is not empty
        if (normalized.isEmpty()) {
            normalized = "site-" + System.currentTimeMillis();
        }

        return normalized;
    }
}

