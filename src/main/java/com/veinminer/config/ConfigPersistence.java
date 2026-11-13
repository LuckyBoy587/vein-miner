package com.veinminer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.veinminer.VeinMiner;
import com.veinminer.config.model.Classification;
import com.veinminer.config.model.ExactTargetSelector;
import com.veinminer.config.model.Facet;
import com.veinminer.config.model.FacetTargetSelector;
import com.veinminer.config.model.FacetType;
import com.veinminer.config.model.GlobalSetting;
import com.veinminer.config.model.TargetKind;
import com.veinminer.config.model.TargetSelector;
import com.veinminer.config.model.TargetSelectors;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Versioned JSON persistence and legacy migration.
 */
public final class ConfigPersistence {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Type LEGACY_MAP_TYPE =
            new TypeToken<LinkedHashMap<String, Boolean>>() {
            }.getType();

    private ConfigPersistence() {
    }

    public static ConfigStore load(Path path) {
        if (!Files.exists(path)) {
            ConfigStore store = ConfigStore.empty();
            save(path, store);
            return store;
        }

        String json;
        try {
            json = Files.readString(path);
        } catch (IOException exception) {
            VeinMiner.LOGGER.error("Failed to read Vein Miner configuration, using defaults.", exception);
            backup(path);
            ConfigStore store = ConfigStore.empty();
            save(path, store);
            return store;
        }

        try {
            JsonElement root = JsonParser.parseString(json);
            if (isCurrentVersion(root)) {
                ConfigStore store = readV2(root);
                VeinMiner.LOGGER.info("Vein Miner configuration loaded successfully.");
                return store;
            }

            Map<String, Boolean> legacy = GSON.fromJson(root, LEGACY_MAP_TYPE);
            ConfigStore store = LegacyConfigMigrator.migrate(legacy);
            backup(path);
            save(path, store);
            VeinMiner.LOGGER.info("Migrated legacy Vein Miner configuration to version {}.",
                    ConfigDocument.CURRENT_VERSION);
            return store;
        } catch (Exception exception) {
            VeinMiner.LOGGER.error("Failed to load Vein Miner configuration, using defaults.", exception);
            backup(path);
            ConfigStore store = ConfigStore.empty();
            save(path, store);
            return store;
        }
    }

    public static void save(Path path, ConfigStore store) {
        Path temporary = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(toDocument(store), writer);
            }
            moveIntoPlace(temporary, path);
            VeinMiner.LOGGER.info("Vein Miner configuration saved successfully.");
        } catch (IOException exception) {
            VeinMiner.LOGGER.error("Failed to save Vein Miner configuration.", exception);
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // Best effort cleanup only.
            }
        }
    }

    private static void moveIntoPlace(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean isCurrentVersion(JsonElement root) {
        if (!root.isJsonObject()) {
            return false;
        }
        JsonObject object = root.getAsJsonObject();
        if (!object.has("version")) {
            return false;
        }
        try {
            int version = object.get("version").getAsInt();
            if (version > ConfigDocument.CURRENT_VERSION) {
                VeinMiner.LOGGER.warn(
                        "Configuration version {} is newer than supported version {}; loading best effort.",
                        version, ConfigDocument.CURRENT_VERSION
                );
            }
            return version >= 2;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static ConfigStore readV2(JsonElement root) {
        ConfigDocument document = GSON.fromJson(root, ConfigDocument.class);
        if (document == null) {
            return ConfigStore.empty();
        }

        ConfigStore store = ConfigStore.empty();
        if (document.settings != null) {
            for (GlobalSetting setting : GlobalSetting.values()) {
                if (document.settings.containsKey(setting.id())) {
                    store = store.withSetting(setting, Boolean.TRUE.equals(document.settings.get(setting.id())));
                }
            }
            Set<String> knownSettingIds = new LinkedHashSet<>();
            for (GlobalSetting setting : GlobalSetting.values()) {
                knownSettingIds.add(setting.id());
            }
            for (String key : document.settings.keySet()) {
                if (!knownSettingIds.contains(key)) {
                    VeinMiner.LOGGER.warn("Ignoring unknown configuration setting: {}", key);
                }
            }
        }

        if (document.rules != null) {
            for (ConfigDocument.RuleDto rule : document.rules) {
                TargetSelector selector = readSelector(rule);
                if (selector != null) {
                    store = store.withRule(selector, rule.enabled);
                }
            }
        }

        return store;
    }

    private static TargetSelector readSelector(ConfigDocument.RuleDto rule) {
        if (rule == null || rule.kind == null || rule.kind.isBlank()) {
            VeinMiner.LOGGER.warn("Ignoring configuration rule without a target kind.");
            return null;
        }

        TargetKind kind = new TargetKind(rule.kind);

        if (rule.id != null && !rule.id.isBlank()) {
            Identifier id = Identifier.tryParse(rule.id);
            if (id == null) {
                VeinMiner.LOGGER.warn("Ignoring configuration rule with invalid id: {}", rule.id);
                return null;
            }
            return TargetSelectors.exact(kind, id);
        }

        List<Facet> facets = new ArrayList<>();
        if (rule.facets != null) {
            for (ConfigDocument.FacetDto facet : rule.facets) {
                if (facet == null || facet.type == null || facet.value == null) {
                    VeinMiner.LOGGER.warn("Ignoring incomplete facet in configuration rule.");
                    continue;
                }
                try {
                    facets.add(new Facet(new FacetType(facet.type), facet.value));
                } catch (RuntimeException exception) {
                    VeinMiner.LOGGER.warn("Ignoring invalid facet {}={}", facet.type, facet.value);
                }
            }
        }

        return new FacetTargetSelector(kind, Classification.of(facets));
    }

    private static ConfigDocument toDocument(ConfigStore store) {
        ConfigDocument document = new ConfigDocument();
        document.version = ConfigDocument.CURRENT_VERSION;

        for (GlobalSetting setting : GlobalSetting.values()) {
            document.settings.put(setting.id(), store.get(setting));
        }

        for (ConfigRule rule : store.rules()) {
            ConfigDocument.RuleDto dto = new ConfigDocument.RuleDto();
            TargetSelector selector = rule.selector();
            dto.enabled = rule.enabled();

            if (selector instanceof ExactTargetSelector exact) {
                dto.kind = exact.kind().id();
                dto.id = exact.id().toString();
            } else if (selector instanceof FacetTargetSelector facetSelector) {
                dto.kind = facetSelector.kind().id();
                dto.facets = new ArrayList<>();
                for (Facet facet : facetSelector.required().facets()) {
                    ConfigDocument.FacetDto facetDto = new ConfigDocument.FacetDto();
                    facetDto.type = facet.type().id();
                    facetDto.value = facet.value();
                    dto.facets.add(facetDto);
                }
            } else {
                VeinMiner.LOGGER.warn("Ignoring unsupported selector while saving: {}", selector);
                continue;
            }

            document.rules.add(dto);
        }

        return document;
    }

    private static void backup(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        Path backup = path.resolveSibling(path.getFileName().toString() + ".v1.bak");
        if (Files.exists(backup)) {
            backup = path.resolveSibling(
                    path.getFileName().toString() + ".v1." + System.currentTimeMillis() + ".bak"
            );
        }
        try {
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
            VeinMiner.LOGGER.info("Backed up legacy Vein Miner configuration to {}", backup);
        } catch (IOException exception) {
            VeinMiner.LOGGER.warn("Could not back up legacy Vein Miner configuration.", exception);
        }
    }
}
