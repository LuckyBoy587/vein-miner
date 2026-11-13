package com.veinminer.classification;

import com.veinminer.config.model.Classification;
import com.veinminer.config.model.Facet;
import com.veinminer.config.model.FacetType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Classifies blocks using data-pack tags. No registry-id string parsing is
 * involved: an ore is a block that belongs to an ore tag.
 */
public final class BlockClassificationRules {
    private static final TagKey<Block> COMMON_ORES = tag("c", "ores");

    private static final List<MaterialOreRule> MATERIAL_ORES = List.of(
            new MaterialOreRule("coal", tag("minecraft", "coal_ores")),
            new MaterialOreRule("copper", tag("minecraft", "copper_ores")),
            new MaterialOreRule("diamond", tag("minecraft", "diamond_ores")),
            new MaterialOreRule("emerald", tag("minecraft", "emerald_ores")),
            new MaterialOreRule("gold", tag("minecraft", "gold_ores")),
            new MaterialOreRule("iron", tag("minecraft", "iron_ores")),
            new MaterialOreRule("lapis", tag("minecraft", "lapis_ores")),
            new MaterialOreRule("redstone", tag("minecraft", "redstone_ores"))
    );

    public Classification classify(Block block) {
        Set<Facet> facets = new LinkedHashSet<>();
        boolean matchedMaterialTag = false;

        for (MaterialOreRule rule : MATERIAL_ORES) {
            if (block.defaultBlockState().is(rule.tag())) {
                matchedMaterialTag = true;
                facets.add(Facet.of(FacetType.BLOCK_TYPE, "ore"));
                facets.add(Facet.of(FacetType.MATERIAL, rule.material()));
            }
        }

        // Common tag catches modded ores whose material is unknown to us.
        if (!matchedMaterialTag && block.defaultBlockState().is(COMMON_ORES)) {
            facets.add(Facet.of(FacetType.BLOCK_TYPE, "ore"));
        }

        return Classification.of(facets);
    }

    private static TagKey<Block> tag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(namespace, path));
    }

    private record MaterialOreRule(String material, TagKey<Block> tag) {
    }
}
