package com.veinminer.client.gui;

import com.veinminer.classification.ToolFamilies;
import com.veinminer.classification.ToolFamily;
import com.veinminer.config.ConfigDefaults;
import com.veinminer.config.ConfigService;
import com.veinminer.config.StandardSelectors;
import com.veinminer.config.model.GlobalSetting;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VeinMinerConfigScreen {

    public static Screen createConfigScreen(Screen parent) {
        ConfigService config = ConfigService.INSTANCE;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.vein-miner.config"));

        builder.setSavingRunnable(config::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.vein-miner.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 1. Master toggle at top
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.enable_vein_miner"),
                        config.getGlobal(GlobalSetting.ENABLE_VEIN_MINER)
                )
                .setDefaultValue(GlobalSetting.ENABLE_VEIN_MINER.defaultValue())
                .setTooltip(Component.translatable("text.vein-miner.config.enable_vein_miner.tooltip"))
                .setSaveConsumer(newValue -> config.setGlobal(GlobalSetting.ENABLE_VEIN_MINER, newValue))
                .build());

        // 2. Crouch required toggle
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.crouch_required"),
                        config.getGlobal(GlobalSetting.CROUCH_REQUIRED)
                )
                .setDefaultValue(GlobalSetting.CROUCH_REQUIRED.defaultValue())
                .setTooltip(Component.translatable("text.vein-miner.config.crouch_required.tooltip"))
                .setSaveConsumer(newValue -> config.setGlobal(GlobalSetting.CROUCH_REQUIRED, newValue))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.block.ores"),
                        config.isRuleEnabled(StandardSelectors.ORES, ConfigDefaults.ORES_ENABLED)
                )
                .setDefaultValue(ConfigDefaults.ORES_ENABLED)
                .setTooltip(Component.translatable("text.vein-miner.config.block.ores.tooltip"))
                .setSaveConsumer(newValue -> config.setRule(StandardSelectors.ORES, newValue))
                .build());

        general.addEntry(GroupSubCategoryBuilder.createBlockGroupSubCategory(entryBuilder));
        for (ToolFamily family : ToolFamilies.all()) {
            general.addEntry(GroupSubCategoryBuilder.createToolGroupSubCategory(entryBuilder, family));
        }

        return builder.build();
    }
}
