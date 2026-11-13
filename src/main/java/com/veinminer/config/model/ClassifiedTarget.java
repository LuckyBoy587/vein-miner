package com.veinminer.config.model;

import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * A concrete target with meaningful identity and classification.
 *
 * <p>The enabled/disabled state intentionally lives in {@code ConfigRule}, not
 * here: this record describes <em>what</em> something is, not whether it is
 * enabled.</p>
 */
public record ClassifiedTarget(TargetKind kind, Identifier id, Classification classification) {
    public ClassifiedTarget {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(classification, "classification");
    }

    public boolean is(TargetKind expected) {
        return kind.equals(expected);
    }

    public boolean has(Facet facet) {
        return classification.has(facet);
    }

    public boolean has(FacetType type, String value) {
        return classification.has(type, value);
    }
}
