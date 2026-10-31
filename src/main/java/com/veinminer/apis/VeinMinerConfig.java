package com.veinminer.apis;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface VeinMinerConfig {
    boolean isVeinMinerEnabled();

    boolean isVeinBlock(BlockState blockState);

    boolean isVeinMinerTool(ItemStack heldItem);
}
