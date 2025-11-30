-- Initial migration: database schema creation
-- Site table for managing e-commerce sites
-- Compatible with both H2 (dev/test) and PostgreSQL (production)
-- Note: Status is stored as VARCHAR. JPA @Enumerated(EnumType.STRING) handles enum conversion.

-- Create sites table
-- Note: UUID generation is handled by JPA @PrePersist, so no database default needed
CREATE TABLE sites (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    currency VARCHAR(10) NOT NULL,
    language VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    owner_id UUID NOT NULL,
    config TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sites_slug_unique UNIQUE (slug)
);

-- Create indexes
CREATE INDEX idx_sites_owner_id ON sites(owner_id);
CREATE INDEX idx_sites_slug ON sites(slug);
CREATE INDEX idx_sites_status ON sites(status);
