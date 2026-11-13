package com.veinminer.config.model;

import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.Objects;

/**
 * A classification dimension, for example {@code tool_type} or {@code material}.
 *
 * <p>Facet types are open-ended on purpose: adding a new dimension does not
 * require changes to the matching or persistence code.</p>
 */
public record FacetType(@NonNull String id) {
    public static final FacetType TOOL_TYPE = new FacetType("tool_type");
    public static final FacetType MATERIAL = new FacetType("material");
    public static final FacetType BLOCK_TYPE = new FacetType("block_type");
    public static final FacetType ITEM_TYPE = new FacetType("item_type");
    public static final FacetType ENTITY_TYPE = new FacetType("entity_type");

    public FacetType {
        Objects.requireNonNull(id, "id");
        id = id.trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Facet type id must not be empty");
        }
    }

    @Override
    public @NonNull String toString() {
        return id;
    }
}
