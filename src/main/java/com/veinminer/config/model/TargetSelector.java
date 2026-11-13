package com.veinminer.config.model;

/**
 * Criterion used by a configuration rule.
 *
 * <p>A selector can target one exact registry entry or any target matching a
 * set of facets. A facet selector with no facets means "all targets of this
 * kind".</p>
 */
public sealed interface TargetSelector permits ExactTargetSelector, FacetTargetSelector {
    TargetKind kind();

    boolean matches(ClassifiedTarget target);

    /**
     * Higher values are more specific. Exact selectors always outrank facet
     * selectors; among facet selectors, more required facets wins.
     */
    int specificity();
}
