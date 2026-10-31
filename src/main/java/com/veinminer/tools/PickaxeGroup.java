package com.veinminer.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class PickaxeGroup extends ToolGroup {
    private static final List<Item> PICKAXES = List.of(
            Items.NETHERITE_PICKAXE,
            Items.DIAMOND_PICKAXE,
            Items.IRON_PICKAXE,
            Items.GOLDEN_PICKAXE,
            Items.STONE_PICKAXE,
            Items.WOODEN_PICKAXE
    );

    @Override
    public List<Item> getTools() {
        return PICKAXES;
    }

    @Override
    public String getName() {
        return "Pickaxe";
    }
}
