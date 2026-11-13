package com.veinminer.config;

import com.veinminer.config.model.ClassifiedTarget;
import com.veinminer.config.model.GlobalSetting;
import com.veinminer.config.model.TargetSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable configuration snapshot.
 *
 * <p>Mutations return a new store, which is cheap for the small configuration
 * files used by this mod and keeps reads from the server thread safe while the
 * configuration screen writes from the client thread.</p>
 */
public final class ConfigStore {
    private final Map<GlobalSetting, Boolean> settings;
    private final List<ConfigRule> rules;
    private final List<ConfigRule> defaultRules;

    public ConfigStore(Map<GlobalSetting, Boolean> settings,
                       List<ConfigRule> rules,
                       List<ConfigRule> defaultRules) {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(rules, "rules");
        Objects.requireNonNull(defaultRules, "defaultRules");

        Map<GlobalSetting, Boolean> settingsCopy = new EnumMap<>(GlobalSetting.class);
        settingsCopy.putAll(settings);
        this.settings = Collections.unmodifiableMap(settingsCopy);
        this.rules = List.copyOf(rules);
        this.defaultRules = List.copyOf(defaultRules);
    }

    public static ConfigStore empty() {
        return new ConfigStore(Map.of(), List.of(), ConfigDefaults.rules());
    }

    public boolean get(GlobalSetting setting) {
        Objects.requireNonNull(setting, "setting");
        return settings.getOrDefault(setting, setting.defaultValue());
    }

    public ConfigStore withSetting(GlobalSetting setting, boolean value) {
        Objects.requireNonNull(setting, "setting");
        Map<GlobalSetting, Boolean> copy = new EnumMap<>(GlobalSetting.class);
        copy.putAll(settings);
        copy.put(setting, value);
        return new ConfigStore(copy, rules, defaultRules);
    }

    public ConfigStore withRule(TargetSelector selector, boolean enabled) {
        Objects.requireNonNull(selector, "selector");
        List<ConfigRule> copy = new ArrayList<>(rules);
        copy.removeIf(rule -> rule.selector().equals(selector));
        copy.add(new ConfigRule(selector, enabled));
        return new ConfigStore(settings, copy, defaultRules);
    }

    public Optional<ConfigRule> explicitRule(TargetSelector selector) {
        Objects.requireNonNull(selector, "selector");
        return rules.stream()
                .filter(rule -> rule.selector().equals(selector))
                .findFirst();
    }

    /**
     * Resolves a target against explicit rules first, then defaults.
     * The most specific matching rule wins; equal specificity is resolved in
     * favour of the later rule.
     */
    public Optional<Boolean> resolve(ClassifiedTarget target) {
        ConfigRule match = bestMatch(rules, target);
        if (match == null) {
            match = bestMatch(defaultRules, target);
        }
        return Optional.ofNullable(match).map(ConfigRule::enabled);
    }

    private static ConfigRule bestMatch(List<ConfigRule> candidates, ClassifiedTarget target) {
        ConfigRule best = null;
        for (ConfigRule candidate : candidates) {
            if (!candidate.selector().matches(target)) {
                continue;
            }
            if (best == null || candidate.selector().specificity() >= best.selector().specificity()) {
                best = candidate;
            }
        }
        return best;
    }

    public List<ConfigRule> rules() {
        return rules;
    }

    public List<ConfigRule> defaultRules() {
        return defaultRules;
    }
}
