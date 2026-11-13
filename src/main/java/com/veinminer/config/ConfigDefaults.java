package com.veinminer.config;

import com.veinminer.config.model.ClassifiedTarget;
import com.veinminer.config.model.TargetKind;

import java.util.List;

/**
 * Defaults used when no explicit user rule matches.
 */
public final class ConfigDefaults {
    /**
     * Tools are enabled by default so a fresh configuration behaves like the
     * configuration screen has always suggested.
     */
    public static final boolean TOOL_ENABLED = true;

    /**
     * Blocks are opt-in except for the default ore rule below.
     */
    public static final boolean BLOCK_ENABLED = false;

    public static final boolean ORES_ENABLED = true;

    private static final List<ConfigRule> RULES = List.of(
            new ConfigRule(StandardSelectors.ORES, ORES_ENABLED)
    );

    private ConfigDefaults() {
    }

    public static List<ConfigRule> rules() {
        return RULES;
    }

    public static boolean fallback(ClassifiedTarget target) {
        return target.kind().equals(TargetKind.TOOL) ? TOOL_ENABLED : BLOCK_ENABLED;
    }
}
