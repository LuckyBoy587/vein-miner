package com.veinminer.config.model;

import java.util.Objects;

/**
 * Selects every target of a kind that has all required facets.
 */
public record FacetTargetSelector(TargetKind kind, Classification required) implements TargetSelector {
    public FacetTargetSelector {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(required, "required");
    }

    @Override
    public boolean matches(ClassifiedTarget target) {
        return target.kind().equals(kind)
                && target.classification().containsAll(required.facets());
    }

    @Override
    public int specificity() {
        return required.size();
    }

    @Override
    public String toString() {
        return kind.id() + required.facets();
    }
}
