package com.veinminer.classification;

import java.util.Locale;
import java.util.Objects;

/**
 * Tool material id. This is a value type rather than an enum so modded
 * materials can be introduced later without changing the configuration model.
 */
public record ToolMaterial(String id) {
    public static final ToolMaterial WOODEN = new ToolMaterial("wooden");
    public static final ToolMaterial STONE = new ToolMaterial("stone");
    public static final ToolMaterial IRON = new ToolMaterial("iron");
    public static final ToolMaterial GOLD = new ToolMaterial("gold");
    public static final ToolMaterial DIAMOND = new ToolMaterial("diamond");
    public static final ToolMaterial NETHERITE = new ToolMaterial("netherite");

    public ToolMaterial {
        Objects.requireNonNull(id, "id");
        id = id.trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Tool material id must not be empty");
        }
    }
}
