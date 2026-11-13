package com.veinminer.classification;

import com.veinminer.config.model.Classification;
import com.veinminer.config.model.ClassifiedTarget;
import com.veinminer.config.model.Facet;
import com.veinminer.config.model.FacetType;
import com.veinminer.config.model.TargetKind;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

/**
 * Declarative catalog of the vanilla tools supported by the mod.
 *
 * <p>Material is explicit data rather than something parsed out of the item
 * id. A future registration API can add modded families without changing the
 * configuration core.</p>
 */
public final class ToolFamilies {
    public static final ToolFamily PICKAXE = new ToolFamily("pickaxe", "Pickaxe", List.of(
            new ToolEntry(Items.NETHERITE_PICKAXE, ToolMaterial.NETHERITE),
            new ToolEntry(Items.DIAMOND_PICKAXE, ToolMaterial.DIAMOND),
            new ToolEntry(Items.IRON_PICKAXE, ToolMaterial.IRON),
            new ToolEntry(Items.GOLDEN_PICKAXE, ToolMaterial.GOLD),
            new ToolEntry(Items.STONE_PICKAXE, ToolMaterial.STONE),
            new ToolEntry(Items.WOODEN_PICKAXE, ToolMaterial.WOODEN)
    ));

    public static final ToolFamily AXE = new ToolFamily("axe", "Axe", List.of(
            new ToolEntry(Items.NETHERITE_AXE, ToolMaterial.NETHERITE),
            new ToolEntry(Items.DIAMOND_AXE, ToolMaterial.DIAMOND),
            new ToolEntry(Items.IRON_AXE, ToolMaterial.IRON),
            new ToolEntry(Items.GOLDEN_AXE, ToolMaterial.GOLD),
            new ToolEntry(Items.STONE_AXE, ToolMaterial.STONE),
            new ToolEntry(Items.WOODEN_AXE, ToolMaterial.WOODEN)
    ));

    public static final ToolFamily SHOVEL = new ToolFamily("shovel", "Shovel", List.of(
            new ToolEntry(Items.NETHERITE_SHOVEL, ToolMaterial.NETHERITE),
            new ToolEntry(Items.DIAMOND_SHOVEL, ToolMaterial.DIAMOND),
            new ToolEntry(Items.IRON_SHOVEL, ToolMaterial.IRON),
            new ToolEntry(Items.GOLDEN_SHOVEL, ToolMaterial.GOLD),
            new ToolEntry(Items.STONE_SHOVEL, ToolMaterial.STONE),
            new ToolEntry(Items.WOODEN_SHOVEL, ToolMaterial.WOODEN)
    ));

    public static final ToolFamily HOE = new ToolFamily("hoe", "Hoe", List.of(
            new ToolEntry(Items.NETHERITE_HOE, ToolMaterial.NETHERITE),
            new ToolEntry(Items.DIAMOND_HOE, ToolMaterial.DIAMOND),
            new ToolEntry(Items.IRON_HOE, ToolMaterial.IRON),
            new ToolEntry(Items.GOLDEN_HOE, ToolMaterial.GOLD),
            new ToolEntry(Items.STONE_HOE, ToolMaterial.STONE),
            new ToolEntry(Items.WOODEN_HOE, ToolMaterial.WOODEN)
    ));

    private static final List<ToolFamily> ALL = List.of(PICKAXE, AXE, SHOVEL, HOE);

    private ToolFamilies() {
    }

    public static List<ToolFamily> all() {
        return ALL;
    }

    public static Optional<ToolMembership> find(Item item) {
        for (ToolFamily family : ALL) {
            for (ToolEntry entry : family.entries()) {
                if (entry.item() == item) {
                    return Optional.of(new ToolMembership(family, entry));
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isKnownTool(Identifier id) {
        for (ToolFamily family : ALL) {
            for (ToolEntry entry : family.entries()) {
                if (BuiltInRegistries.ITEM.getKey(entry.item()).equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Optional<ClassifiedTarget> classify(Item item) {
        return find(item).map(membership -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            Classification classification = Classification.of(
                    Facet.of(FacetType.TOOL_TYPE, membership.family().id()),
                    Facet.of(FacetType.MATERIAL, membership.entry().material().id())
            );
            return new ClassifiedTarget(TargetKind.TOOL, id, classification);
        });
    }

    public record ToolMembership(ToolFamily family, ToolEntry entry) {
    }
}
