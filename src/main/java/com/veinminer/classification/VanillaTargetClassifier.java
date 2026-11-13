package com.veinminer.classification;

import com.veinminer.config.model.Classification;
import com.veinminer.config.model.ClassifiedTarget;
import com.veinminer.config.model.TargetKind;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Default classifier for vanilla blocks and tools.
 */
public final class VanillaTargetClassifier implements TargetClassifier {
    private final BlockClassificationRules blockRules = new BlockClassificationRules();

    @Override
    public Optional<ClassifiedTarget> classifyBlock(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return Optional.of(new ClassifiedTarget(
                TargetKind.BLOCK,
                id,
                blockRules.classify(state.getBlock())
        ));
    }

    @Override
    public Optional<ClassifiedTarget> classifyItem(Item item) {
        Optional<ClassifiedTarget> tool = ToolFamilies.classify(item);
        if (tool.isPresent()) {
            return tool;
        }

        // Preserve exact item rules from legacy configurations. The fallback
        // for non-tool items is still disabled.
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return Optional.of(new ClassifiedTarget(TargetKind.ITEM, id, Classification.empty()));
    }
}
