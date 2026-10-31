package com.veinminer.client.gui;

import com.veinminer.config.VeinMinerConfigData;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
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
                        config.enableVeinMiner
                )
                .setDefaultValue(true)
                .setTooltip(Component.translatable("text.vein-miner.config.enable_vein_miner.tooltip"))
                .setSaveConsumer(newValue -> config.enableVeinMiner = newValue)
                .build());

        // 2. Pickaxes section
        SubCategoryBuilder pickaxesSub = entryBuilder.startSubCategory(
                Component.translatable("category.vein-miner.pickaxes")
        );
        pickaxesSub.setExpanded(true);

        pickaxesSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.pickaxe.wooden"),
                        config.woodenPickaxe
                )
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.woodenPickaxe = val)
                .build());

        pickaxesSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.pickaxe.stone"),
                        config.stonePickaxe
                )
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.stonePickaxe = val)
                .build());

        pickaxesSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.pickaxe.iron"),
                        config.ironPickaxe
                )
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.ironPickaxe = val)
                .build());

        pickaxesSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.pickaxe.golden"),
                        config.goldenPickaxe
                )
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.goldenPickaxe = val)
                .build());

        pickaxesSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.pickaxe.diamond"),
                        config.diamondPickaxe
                )
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.diamondPickaxe = val)
                .build());

        pickaxesSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.pickaxe.netherite"),
                        config.netheritePickaxe
                )
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.netheritePickaxe = val)
                .build());

        general.addEntry(pickaxesSub.build());

        // 3. Blocks section
        SubCategoryBuilder blocksSub = entryBuilder.startSubCategory(
                Component.translatable("category.vein-miner.blocks")
        );
        blocksSub.setExpanded(true);

        blocksSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.block.ores"),
                        config.ores
                )
                .setDefaultValue(true)
                .setTooltip(Component.translatable("text.vein-miner.config.block.ores.tooltip"))
                .setSaveConsumer(val -> config.ores = val)
                .build());

        blocksSub.add(entryBuilder.startBooleanToggle(
                        Component.translatable("text.vein-miner.config.block.magma_block"),
                        config.magmaBlock
                )
                .setDefaultValue(true)
                .setTooltip(Component.translatable("text.vein-miner.config.block.magma_block.tooltip"))
                .setSaveConsumer(val -> config.magmaBlock = val)
                .build());

        general.addEntry(blocksSub.build());

        // 4. Custom blocks section
        SubCategoryBuilder customBlocksSub = entryBuilder.startSubCategory(
                Component.translatable("category.vein-miner.custom_blocks")
        );
        customBlocksSub.setExpanded(true);

        customBlocksSub.add(new CustomBlockSearchEntry(
                Component.translatable("category.vein-miner.custom_blocks"),
                config.customBlocks,
                newBlocks -> config.customBlocks = newBlocks
        ));

        general.addEntry(customBlocksSub.build());

        return builder.build();
    }
}
