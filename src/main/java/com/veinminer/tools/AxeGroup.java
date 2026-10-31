package com.veinminer.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class AxeGroup extends ToolGroup {
    private static final List<Item> AXES = List.of(
            Items.NETHERITE_AXE,
            Items.DIAMOND_AXE,
            Items.IRON_AXE,
            Items.GOLDEN_AXE,
            Items.STONE_AXE,
            Items.WOODEN_AXE
    );

    @Override
    public List<Item> getTools() {
        return AXES;
    }

    @Override
    public String getName() {
        return "Axe";
    }
}
