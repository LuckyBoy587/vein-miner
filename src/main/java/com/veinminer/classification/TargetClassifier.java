package com.veinminer.classification;

import com.veinminer.config.model.ClassifiedTarget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Converts Minecraft objects into classified configuration targets.
 */
public interface TargetClassifier {
    Optional<ClassifiedTarget> classifyBlock(BlockState state);

    /**
     * Classifies a stack. Implementations classify by item, so callers that only
     * have an {@link Item} should prefer {@link #classifyItem(Item)}; creating an
     * {@link ItemStack} fails while item components are not bound, for example on
     * the main menu.
     */
    default Optional<ClassifiedTarget> classifyItem(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : classifyItem(stack.getItem());
    }

    Optional<ClassifiedTarget> classifyItem(Item item);
}
