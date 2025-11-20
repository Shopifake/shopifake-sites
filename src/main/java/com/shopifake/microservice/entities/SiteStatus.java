package com.shopifake.microservice.entities;

/**
 * Enumeration representing the possible statuses of a site.
 */
public enum SiteStatus {

    /**
     * Site is in draft state, not yet published.
     */
    DRAFT,

    /**
     * Site is active and operational.
     */
    ACTIVE,

    /**
     * Site is disabled, not accessible.
     */
    DISABLED,
}

