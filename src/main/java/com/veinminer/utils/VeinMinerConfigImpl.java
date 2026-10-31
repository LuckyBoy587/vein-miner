package com.veinminer.utils;

import com.veinminer.apis.VeinMinerConfig;
import com.veinminer.config.VeinMinerConfigData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class VeinMinerConfigImpl implements VeinMinerConfig {

    private static final TagKey<Block> ORES_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

    @Override
    public boolean isVeinMinerEnabled() {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        return config.enableVeinMiner;
    }

    @Override
    public boolean isVeinBlock(BlockState blockState) {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        if (blockState.is(ORES_TAG)) {
            return config.ores;
        }

        if (blockState.is(Blocks.MAGMA_BLOCK)) {
            return config.magmaBlock;
        }

        return false;
    }

    @Override
    public boolean isVeinMinerTool(ItemStack heldItem) {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        if (heldItem.is(Items.NETHERITE_PICKAXE)) {
            return config.netheritePickaxe;
        } else if (heldItem.is(Items.DIAMOND_PICKAXE)) {
            return config.diamondPickaxe;
        } else if (heldItem.is(Items.IRON_PICKAXE)) {
            return config.ironPickaxe;
        } else if (heldItem.is(Items.GOLDEN_PICKAXE)) {
            return config.goldenPickaxe;
        } else if (heldItem.is(Items.STONE_PICKAXE)) {
            return config.stonePickaxe;
        } else if (heldItem.is(Items.WOODEN_PICKAXE)) {
            return config.woodenPickaxe;
        } else {
            return false;
        }
    }
}
