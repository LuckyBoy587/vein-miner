package com.veinminer.client.gui;

import net.minecraft.network.chat.Component;

public enum ConfigTab {
    GENERAL("category.vein-miner.general"),
    BLOCKS("category.vein-miner.blocks"),
    TOOLS("category.vein-miner.tools");

    private final String translationKey;

    ConfigTab(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }
}
