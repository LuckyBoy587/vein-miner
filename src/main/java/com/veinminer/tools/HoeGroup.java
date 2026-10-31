package com.veinminer.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class HoeGroup extends ToolGroup {
    private static final List<Item> HOES = List.of(
            Items.NETHERITE_HOE,
            Items.DIAMOND_HOE,
            Items.IRON_HOE,
            Items.GOLDEN_HOE,
            Items.STONE_HOE,
            Items.WOODEN_HOE
    );

    @Override
    public List<Item> getTools() {
        return HOES;
    }

    @Override
    public String getName() {
        return "Hoe";
    }
}
