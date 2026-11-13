package com.veinminer.config.model;

/**
 * Settings that are not tied to a classified entity.
 */
public enum GlobalSetting {
    ENABLE_VEIN_MINER("enableVeinMiner", true),
    CROUCH_REQUIRED("crouchRequired", true);

    private final String id;
    private final boolean defaultValue;

    GlobalSetting(String id, boolean defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
    }

    public String id() {
        return id;
    }

    public boolean defaultValue() {
        return defaultValue;
    }
}
