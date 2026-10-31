package com.veinminer.client.gui;

import com.veinminer.config.VeinMinerConfigData;
import com.veinminer.tools.ToolGroup;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class GroupSubCategoryBuilder {

    public static AbstractConfigListEntry<?> createToolGroupSubCategory(ConfigEntryBuilder entryBuilder, ToolGroup toolGroup) {
        SubCategoryBuilder subCategory = entryBuilder.startSubCategory(
            Component.literal(toolGroup.getName())
        );
        subCategory.setExpanded(false);

        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;

        for (Item tool : toolGroup.getTools()) {
            String key = BuiltInRegistries.ITEM.getKey(tool).toString();

            subCategory.add(entryBuilder.startBooleanToggle(
                    Component.translatable(tool.getDescriptionId()),
                    config.getOrDefault(key, true)
                )
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.put(key, val))
                .build());
        }

        return subCategory.build();
    }

    public static AbstractConfigListEntry<?> createBlockGroupSubCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder subCategory = entryBuilder.startSubCategory(
            Component.literal("Blocks")
        );
        subCategory.setExpanded(true);
        subCategory.add(new SearchableBlockListEntry(Component.literal("Blocks Search")));
        return subCategory.build();
    }
}
