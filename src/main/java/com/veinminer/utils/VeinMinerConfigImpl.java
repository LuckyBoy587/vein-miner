package com.veinminer.utils;

import com.veinminer.apis.VeinMinerConfig;
import com.veinminer.config.VeinMinerConfigConstants;
import com.veinminer.config.VeinMinerConfigData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class VeinMinerConfigImpl implements VeinMinerConfig {

    private static final TagKey<Block> ORES_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

    @Override
    public boolean isVeinMinerEnabled() {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        return config.getOrDefault(VeinMinerConfigConstants.ENABLED, true);
    }

    @Override
    public boolean isVeinBlock(BlockState blockState) {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        if (blockState.is(ORES_TAG)) {
            return config.getOrDefault(VeinMinerConfigConstants.ORES, true);
        }
        String key = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();

        if (config.containsKey(key)) {
            return config.getOrDefault(key, false);
        }

        return false;
    }

    @Override
    public boolean isVeinMinerTool(ItemStack heldItem) {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        String key = BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
        return config.getOrDefault(key, false);
    }
}
