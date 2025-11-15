package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Site entity operations.
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, UUID> {

    /**
     * Find a site by its slug.
     *
     * @param slug the slug to search for
     * @return Optional containing the site if found
     */
    Optional<Site> findBySlug(String slug);

    /**
     * Check if a site exists with the given slug.
     *
     * @param slug the slug to check
     * @return true if a site with this slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Find all sites owned by a specific owner.
     *
     * @param ownerId the owner ID
     * @return list of sites owned by the owner
     */
    List<Site> findByOwnerId(Long ownerId);

    /**
     * Count sites owned by a specific owner.
     *
     * @param ownerId the owner ID
     * @return the count of sites owned by the owner
     */
    long countByOwnerId(Long ownerId);
}

