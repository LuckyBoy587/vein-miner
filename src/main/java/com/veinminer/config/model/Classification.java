package com.veinminer.config.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable set of facets describing what a target is.
 */
public final class Classification {
    private static final Classification EMPTY = new Classification(Set.of());

    private final Set<Facet> facets;

    private Classification(Set<Facet> facets) {
        this.facets = Collections.unmodifiableSet(new LinkedHashSet<>(facets));
    }

    public static Classification empty() {
        return EMPTY;
    }

    public static Classification of(Facet... facets) {
        if (facets.length == 0) {
            return EMPTY;
        }
        return new Classification(Set.of(facets));
    }

    public static Classification of(Collection<Facet> facets) {
        Objects.requireNonNull(facets, "facets");
        if (facets.isEmpty()) {
            return EMPTY;
        }
        return new Classification(new LinkedHashSet<>(facets));
    }

    public Set<Facet> facets() {
        return facets;
    }

    public boolean has(Facet facet) {
        return facets.contains(facet);
    }

    public boolean has(FacetType type, String value) {
        return facets.contains(Facet.of(type, value));
    }

    public boolean containsAll(Collection<Facet> required) {
        return facets.containsAll(required);
    }

    public int size() {
        return facets.size();
    }

    public boolean isEmpty() {
        return facets.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Classification that && facets.equals(that.facets);
    }

    @Override
    public int hashCode() {
        return facets.hashCode();
    }

    @Override
    public String toString() {
        return facets.toString();
    }
}
