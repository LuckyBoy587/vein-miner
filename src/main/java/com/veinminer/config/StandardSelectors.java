package com.veinminer.config;

import com.veinminer.config.model.FacetType;
import com.veinminer.config.model.TargetKind;
import com.veinminer.config.model.TargetSelector;
import com.veinminer.config.model.TargetSelectors;

/**
 * Well-known selectors used by the mod.
 */
public final class StandardSelectors {
    /**
     * All blocks classified as ores. This replaces the old special-cased
     * {@code "ores"} key and can be overridden by more specific rules.
     */
    public static final TargetSelector ORES = TargetSelectors.of(TargetKind.BLOCK)
            .facet(FacetType.BLOCK_TYPE, "ore")
            .build();

    private StandardSelectors() {
    }
}
