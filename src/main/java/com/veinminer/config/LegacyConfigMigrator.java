package com.veinminer.config;

import com.veinminer.VeinMiner;
import com.veinminer.classification.ToolFamilies;
import com.veinminer.config.model.GlobalSetting;
import com.veinminer.config.model.TargetKind;
import com.veinminer.config.model.TargetSelector;
import com.veinminer.config.model.TargetSelectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Converts the old flat {@code String -> Boolean} configuration into the typed
 * model.
 */
public final class LegacyConfigMigrator {
    private static final String TOOL_KEY_PREFIX = "Tool#";

    private LegacyConfigMigrator() {
    }

    public static ConfigStore migrate(Map<String, Boolean> legacy) {
        ConfigStore store = ConfigStore.empty();
        if (legacy == null || legacy.isEmpty()) {
            return store;
        }

        Map<Identifier, Boolean> guiToolRules = new LinkedHashMap<>();
        Set<Identifier> rawToolIds = new LinkedHashSet<>();

        for (Map.Entry<String, Boolean> entry : legacy.entrySet()) {
            String key = entry.getKey();
            boolean value = Boolean.TRUE.equals(entry.getValue());

            switch (key) {
                case "enableVeinMiner" -> {
                    store = store.withSetting(GlobalSetting.ENABLE_VEIN_MINER, value);
                    continue;
                }
                case "crouchRequired" -> {
                    store = store.withSetting(GlobalSetting.CROUCH_REQUIRED, value);
                    continue;
                }
                case "ores" -> {
                    store = store.withRule(StandardSelectors.ORES, value);
                    continue;
                }
                default -> {
                    // Fall through to target handling below.
                }
            }

            if (key.startsWith(TOOL_KEY_PREFIX)) {
                Identifier id = Identifier.tryParse(key.substring(TOOL_KEY_PREFIX.length()));
                if (id == null) {
                    VeinMiner.LOGGER.warn("Skipping legacy tool key with invalid id: {}", key);
                } else {
                    guiToolRules.put(id, value);
                }
                continue;
            }

            Identifier id = Identifier.tryParse(key);
            if (id == null) {
                VeinMiner.LOGGER.warn("Skipping legacy configuration key with invalid id: {}", key);
                continue;
            }

            if (ToolFamilies.isKnownTool(id)) {
                store = store.withRule(TargetSelectors.exact(TargetKind.TOOL, id), value);
                rawToolIds.add(id);
            } else if (BuiltInRegistries.BLOCK.containsKey(id)) {
                store = store.withRule(TargetSelectors.exact(TargetKind.BLOCK, id), value);
            } else if (BuiltInRegistries.ITEM.containsKey(id)) {
                store = store.withRule(TargetSelectors.exact(TargetKind.ITEM, id), value);
            } else {
                VeinMiner.LOGGER.warn("Skipping legacy configuration key for unknown registry id: {}", key);
            }
        }

        // A raw tool id reflects what the runtime actually used. Only fall back
        // to a GUI-generated Tool# key when no raw rule exists.
        for (Map.Entry<Identifier, Boolean> entry : guiToolRules.entrySet()) {
            if (!rawToolIds.contains(entry.getKey())) {
                TargetSelector selector = TargetSelectors.exact(TargetKind.TOOL, entry.getKey());
                store = store.withRule(selector, entry.getValue());
            } else if (legacy.containsKey(entry.getKey().toString())) {
                VeinMiner.LOGGER.warn(
                        "Legacy tool {} has both raw and Tool# entries; using the raw value",
                        entry.getKey()
                );
            }
        }

        return store;
    }
}
