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
     * Site is pending approval or activation.
     */
    PENDING,

    /**
     * Site is suspended, temporarily unavailable.
     */
    SUSPENDED,

    /**
     * Site is archived, no longer active but preserved.
     */
    ARCHIVED,

    /**
     * Site is disabled, not accessible.
     */
    DISABLED,

    /**
     * Site is deleted, marked for removal.
     */
    DELETED
}

