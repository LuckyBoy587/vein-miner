package com.veinminer.client.gui;

import com.veinminer.config.VeinMinerConfigConstants;
import com.veinminer.config.VeinMinerConfigData;
import com.veinminer.tools.AxeGroup;
import com.veinminer.tools.PickaxeGroup;
import com.veinminer.tools.ShovelGroup;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VeinMinerConfigScreen {

    public static Screen createConfigScreen(Screen parent) {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.vein-miner.config"));

        builder.setSavingRunnable(VeinMinerConfigData::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.vein-miner.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 1. Master toggle at top
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.enable_vein_miner"),
                        config.getOrDefault(VeinMinerConfigConstants.ENABLED, true)
                )
                .setDefaultValue(true)
                .setTooltip(Component.translatable("text.vein-miner.config.enable_vein_miner.tooltip"))
                .setSaveConsumer(newValue -> config.put(VeinMinerConfigConstants.ENABLED, newValue))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("text.vein-miner.config.block.ores"),
                config.getOrDefault(VeinMinerConfigConstants.ORES, true)
            )
            .setDefaultValue(true)
            .setTooltip(Component.translatable("text.vein-miner.config.block.ores.tooltip"))
            .setSaveConsumer(newValue -> config.put(VeinMinerConfigConstants.ORES, newValue))
            .build());

        general.addEntry(GroupSubCategoryBuilder.createToolGroupSubCategory(entryBuilder, new PickaxeGroup()));
        general.addEntry(GroupSubCategoryBuilder.createToolGroupSubCategory(entryBuilder, new AxeGroup()));
        general.addEntry(GroupSubCategoryBuilder.createToolGroupSubCategory(entryBuilder, new ShovelGroup()));
        general.addEntry(GroupSubCategoryBuilder.createBlockGroupSubCategory(entryBuilder));

        return builder.build();
    }
}
