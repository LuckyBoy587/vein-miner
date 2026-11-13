package com.veinminer.client.gui;

import com.veinminer.classification.ToolEntry;
import com.veinminer.classification.ToolFamily;
import com.veinminer.config.ConfigDefaults;
import com.veinminer.config.ConfigService;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.network.chat.Component;

public class GroupSubCategoryBuilder {

    public static AbstractConfigListEntry<?> createToolGroupSubCategory(ConfigEntryBuilder entryBuilder, ToolFamily toolFamily) {
        SubCategoryBuilder subCategory = entryBuilder.startSubCategory(
            Component.literal(toolFamily.displayName())
        );
        subCategory.setExpanded(false);

        ConfigService config = ConfigService.INSTANCE;

        for (ToolEntry tool : toolFamily.entries()) {
            subCategory.add(entryBuilder.startBooleanToggle(
                    Component.translatable(tool.item().getDescriptionId()),
                    config.isToolEnabled(tool.item())
                )
                .setDefaultValue(ConfigDefaults.TOOL_ENABLED)
                .setSaveConsumer(val -> config.setToolEnabled(tool.item(), val))
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
