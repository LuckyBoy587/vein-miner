package com.veinminer.client.gui;

import net.minecraft.network.chat.Component;

public enum BlockFilter {
    AVAILABLE("text.vein-miner.config.blocks.filter.available"),
    ADDED("text.vein-miner.config.blocks.filter.added"),
    ALL("text.vein-miner.config.blocks.filter.all");

    private final String translationKey;

    BlockFilter(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    public BlockFilter next() {
        BlockFilter[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public boolean accepts(boolean enabled) {
        return switch (this) {
            case ADDED -> enabled;
            case AVAILABLE -> !enabled;
            case ALL -> true;
        };
    }
}
