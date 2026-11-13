package com.veinminer.classification;

import com.veinminer.config.model.ClassifiedTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Converts Minecraft objects into classified configuration targets.
 */
public interface TargetClassifier {
    Optional<ClassifiedTarget> classifyBlock(BlockState state);

    Optional<ClassifiedTarget> classifyItem(ItemStack stack);
}
