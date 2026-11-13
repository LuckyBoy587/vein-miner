package com.veinminer.config.model;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Small factory/builder so callers do not need to construct selector records
 * by hand.
 */
public final class TargetSelectors {
    private TargetSelectors() {
    }

    public static TargetSelector exact(TargetKind kind, Identifier id) {
        return new ExactTargetSelector(kind, id);
    }

    public static Builder of(TargetKind kind) {
        return new Builder(kind);
    }

    public static final class Builder {
        private final TargetKind kind;
        private final Set<Facet> facets = new LinkedHashSet<>();

        private Builder(TargetKind kind) {
            this.kind = kind;
        }

        public Builder facet(FacetType type, String value) {
            facets.add(Facet.of(type, value));
            return this;
        }

        public Builder facet(Facet facet) {
            facets.add(facet);
            return this;
        }

        public TargetSelector build() {
            return new FacetTargetSelector(kind, Classification.of(facets));
        }
    }
}
