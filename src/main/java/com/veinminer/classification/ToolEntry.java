package com.veinminer.classification;

import net.minecraft.world.item.Item;

import java.util.Objects;

/**
 * One concrete tool and its material.
 */
public record ToolEntry(Item item, ToolMaterial material) {
    public ToolEntry {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(material, "material");
    }
}
