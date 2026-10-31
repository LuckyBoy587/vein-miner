package com.veinminer.apis;

import net.minecraft.world.level.block.Block;

public interface VeinMinerConfig {
    boolean isVeinMinerEnabled();
    boolean isVeinBlock(Block block);
    boolean isVeinMinerTool();
}
