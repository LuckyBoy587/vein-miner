package com.veinminer.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class ShovelGroup extends ToolGroup {
    private static final List<Item> SHOVELS = List.of(
            Items.NETHERITE_SHOVEL,
            Items.DIAMOND_SHOVEL,
            Items.IRON_SHOVEL,
            Items.GOLDEN_SHOVEL,
            Items.STONE_SHOVEL,
            Items.WOODEN_SHOVEL
    );

    @Override
    public List<Item> getTools() {
        return SHOVELS;
    }

    @Override
    public String getName() {
        return "Shovel";
    }
}
