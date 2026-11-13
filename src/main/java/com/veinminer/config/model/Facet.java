package com.veinminer.config.model;

import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.Objects;

/**
 * One typed classification value, for example {@code tool_type=pickaxe} or
 * {@code material=diamond}.
 */
public record Facet(@NonNull FacetType type, @NonNull String value) {
    public Facet {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Facet value must not be empty");
        }
    }

    public static Facet of(FacetType type, String value) {
        return new Facet(type, value);
    }

    @Override
    public @NonNull String toString() {
        return type.id() + "=" + value;
    }
}
