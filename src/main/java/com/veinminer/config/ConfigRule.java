package com.veinminer.config;

import com.veinminer.config.model.TargetSelector;

/**
 * A selector plus its enabled/disabled state.
 *
 * <p>The state is deliberately separate from the target classification.</p>
 */
public record ConfigRule(TargetSelector selector, boolean enabled) {
}
