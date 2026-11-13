package com.veinminer.client.gui;

import com.veinminer.config.ConfigDefaults;
import com.veinminer.config.ConfigService;
import com.veinminer.config.StandardSelectors;
import com.veinminer.config.model.GlobalSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Two-panel Vein Miner configuration screen.
 *
 * <p>The left panel selects between General, Blocks and Tools. The right panel
 * is rebuilt for the selected tab; all values are written straight to
 * {@link ConfigService} and persisted when the screen closes.</p>
 */
public class VeinMinerConfigScreen extends Screen {

    private static final int MARGIN = 16;
    private static final int CONTENT_TOP = 36;
    private static final int CONTENT_BOTTOM_OFFSET = 40;
    private static final int TAB_HEIGHT = 27;
    private static final int TAB_GAP = 6;
    private static final int GENERAL_ROW_HEIGHT = 52;

    private final Screen parent;

    private ConfigTab activeTab = ConfigTab.GENERAL;
    private BlockFilter blockFilter = BlockFilter.AVAILABLE;
    private String blockSearch = "";

    private int leftX;
    private int leftY;
    private int leftWidth;
    private int leftHeight;
    private int rightX;
    private int rightY;
    private int rightWidth;
    private int rightHeight;

    private BlockListWidget blockList;
    private EditBox blockSearchBox;

    public VeinMinerConfigScreen(Screen parent) {
        super(Component.translatable("title.vein-miner.config"));
        this.parent = parent;
    }

    public static Screen createConfigScreen(Screen parent) {
        return new VeinMinerConfigScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
        computeLayout();

        ConfigTab[] tabs = ConfigTab.values();
        for (int i = 0; i < tabs.length; i++) {
            ConfigTab tab = tabs[i];
            int tabY = leftY + 10 + i * (TAB_HEIGHT + TAB_GAP);
            addRenderableWidget(new SidebarTabButton(
                    font,
                    leftX + 6,
                    tabY,
                    leftWidth - 12,
                    TAB_HEIGHT,
                    tab,
                    tab == activeTab,
                    () -> selectTab(tab)
            ));
        }

        switch (activeTab) {
            case GENERAL -> addGeneralWidgets();
            case BLOCKS -> addBlocksWidgets();
            case TOOLS -> addToolsWidgets();
        }

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 - 60, height - 30, 120, 20)
                .build());
    }

    private void computeLayout() {
        int contentBottom = height - CONTENT_BOTTOM_OFFSET;

        leftX = MARGIN;
        leftY = CONTENT_TOP;
        leftWidth = Math.max(100, Math.min(160, width / 5));
        leftHeight = contentBottom - leftY;

        rightX = leftX + leftWidth + 12;
        rightY = leftY;
        rightWidth = Math.max(120, width - rightX - MARGIN);
        rightHeight = contentBottom - rightY;
    }

    private void selectTab(ConfigTab tab) {
        if (activeTab == tab) {
            return;
        }
        activeTab = tab;
        blockList = null;
        blockSearchBox = null;
        rebuildWidgets();
    }

    // ------------------------------------------------------------------
    // General
    // ------------------------------------------------------------------

    private int generalRowY(int index) {
        return rightY + 16 + index * GENERAL_ROW_HEIGHT;
    }

    private void addGeneralWidgets() {
        ConfigService config = ConfigService.INSTANCE;

        addSettingToggle(0, GlobalSetting.ENABLE_VEIN_MINER,
                "text.vein-miner.config.enable_vein_miner",
                "text.vein-miner.config.enable_vein_miner.tooltip");

        addSettingToggle(1, GlobalSetting.CROUCH_REQUIRED,
                "text.vein-miner.config.crouch_required",
                "text.vein-miner.config.crouch_required.tooltip");

        boolean oresEnabled = config.isRuleEnabled(StandardSelectors.ORES, ConfigDefaults.ORES_ENABLED);
        addSettingToggle(2, oresEnabled,
                "text.vein-miner.config.block.ores",
                "text.vein-miner.config.block.ores.tooltip",
                value -> config.setRule(StandardSelectors.ORES, value));
    }

    private void addSettingToggle(int index, GlobalSetting setting, String labelKey, String descriptionKey) {
        ConfigService config = ConfigService.INSTANCE;
        addSettingToggle(index, config.getGlobal(setting), labelKey, descriptionKey,
                value -> config.setGlobal(setting, value));
    }

    private void addSettingToggle(int index,
                                  boolean currentValue,
                                  String labelKey,
                                  String descriptionKey,
                                  java.util.function.Consumer<Boolean> saveConsumer) {
        int rowY = generalRowY(index);
        CycleButton<Boolean> toggle = CycleButton.onOffBuilder(currentValue)
                .displayOnlyValue()
                .withTooltip(value -> Tooltip.create(Component.translatable(descriptionKey)))
                .create(
                        rightX + rightWidth - 72,
                        rowY + 12,
                        56,
                        20,
                        Component.translatable(labelKey),
                        (button, value) -> saveConsumer.accept(value)
                );
        addRenderableWidget(toggle);
    }

    private void drawGeneral(GuiGraphicsExtractor graphics) {
        drawSettingRow(graphics, 0, "text.vein-miner.config.enable_vein_miner");
        drawSettingRow(graphics, 1, "text.vein-miner.config.crouch_required");
        drawSettingRow(graphics, 2, "text.vein-miner.config.block.ores");
    }

    private void drawSettingRow(GuiGraphicsExtractor graphics, int index, String labelKey) {
        int rowY = generalRowY(index);
        int rowX = rightX + 8;
        int rowWidth = rightWidth - 16;

        graphics.fill(rowX, rowY, rowX + rowWidth, rowY + 44, 0x1AFFFFFF);
        graphics.outline(rowX, rowY, rowWidth, 44, 0x22FFFFFF);
        graphics.text(font, Component.translatable(labelKey), rowX + 12, rowY + 18, 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------
    // Blocks
    // ------------------------------------------------------------------

    private void addBlocksWidgets() {
        int headerY = rightY + 8;
        int filterWidth = Math.min(120, Math.max(80, rightWidth / 3));
        int searchWidth = Math.max(60, rightWidth - filterWidth - 24);

        blockSearchBox = new EditBox(
                font,
                rightX + 8,
                headerY,
                searchWidth,
                20,
                Component.translatable("text.vein-miner.config.blocks.search_hint")
        );
        blockSearchBox.setHint(Component.translatable("text.vein-miner.config.blocks.search_hint"));
        blockSearchBox.setMaxLength(64);
        blockSearchBox.setValue(blockSearch);
        blockSearchBox.setResponder(this::onBlockSearchChanged);
        addRenderableWidget(blockSearchBox);

        Button filterButton = Button.builder(filterButtonLabel(), button -> {
            blockFilter = blockFilter.next();
            button.setMessage(filterButtonLabel());
            if (blockList != null) {
                blockList.markDirty(true);
            }
        }).bounds(rightX + rightWidth - 8 - filterWidth, headerY, filterWidth, 20).build();
        addRenderableWidget(filterButton);

        blockList = new BlockListWidget(
                minecraft,
                font,
                rightX + 2,
                rightY + 34,
                rightWidth - 4,
                Math.max(40, rightHeight - 42),
                this::computeBlocks,
                this::emptyBlocksMessage
        );
        addRenderableWidget(blockList);
    }

    private void onBlockSearchChanged(String value) {
        blockSearch = value;
        if (blockList != null) {
            blockList.markDirty(true);
        }
    }

    private Component filterButtonLabel() {
        return Component.translatable("text.vein-miner.config.blocks.filter_button", blockFilter.label());
    }

    private List<Block> computeBlocks() {
        ConfigService config = ConfigService.INSTANCE;
        String query = blockSearch.trim().toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            List<Block> blocks = new ArrayList<>();
            for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
                Block block = BuiltInRegistries.BLOCK.getValue(id);
                if (GuiItemStacks.isVisible(block) && blockFilter.accepts(config.isBlockEnabled(block))) {
                    blocks.add(block);
                }
            }
            blocks.sort(Comparator
                    .comparing((Block block) -> block.getName().getString(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));
            return blocks;
        }

        List<RankedBlock> ranked = new ArrayList<>();
        for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            if (!GuiItemStacks.isVisible(block)) {
                continue;
            }
            if (!blockFilter.accepts(config.isBlockEnabled(block))) {
                continue;
            }

            String name = block.getName().getString();
            String lowerName = name.toLowerCase(Locale.ROOT);
            String idString = id.toString().toLowerCase(Locale.ROOT);
            String idPath = id.getPath().toLowerCase(Locale.ROOT);

            int rank;
            if (lowerName.startsWith(query) || idString.startsWith(query) || idPath.startsWith(query)) {
                rank = 0;
            } else if (lowerName.contains(query) || idString.contains(query) || idPath.contains(query)) {
                rank = 1;
            } else {
                continue;
            }

            ranked.add(new RankedBlock(block, rank, name.length(), name, idString));
        }

        ranked.sort(Comparator
                .comparingInt(RankedBlock::rank)
                .thenComparingInt(RankedBlock::nameLength)
                .thenComparing(RankedBlock::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RankedBlock::id));

        return ranked.stream().map(RankedBlock::block).toList();
    }

    private Component emptyBlocksMessage() {
        String query = blockSearch.trim();
        if (!query.isEmpty()) {
            return Component.translatable("text.vein-miner.config.blocks.empty_search", query);
        }
        return switch (blockFilter) {
            case AVAILABLE -> Component.translatable("text.vein-miner.config.blocks.empty_available");
            case ADDED -> Component.translatable("text.vein-miner.config.blocks.empty_added");
            case ALL -> Component.translatable("text.vein-miner.config.blocks.empty_all");
        };
    }

    // ------------------------------------------------------------------
    // Tools
    // ------------------------------------------------------------------

    private void addToolsWidgets() {
        addRenderableWidget(new ToolListWidget(
                minecraft,
                font,
                rightX + 2,
                rightY + 8,
                rightWidth - 4,
                Math.max(40, rightHeight - 16)
        ));
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawChrome(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawChrome(GuiGraphicsExtractor graphics) {
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);

        graphics.fill(leftX - 2, leftY - 2, leftX + leftWidth + 2, leftY + leftHeight + 2, 0x99000000);
        graphics.outline(leftX - 2, leftY - 2, leftWidth + 4, leftHeight + 4, 0xFF2A2A2A);

        graphics.fill(rightX - 2, rightY - 2, rightX + rightWidth + 2, rightY + rightHeight + 2, 0xBB0A0A0A);
        graphics.outline(rightX - 2, rightY - 2, rightWidth + 4, rightHeight + 4, 0xFF2A2A2A);

        graphics.fill(MARGIN, height - 38, width - MARGIN, height - 37, 0x33FFFFFF);

        if (activeTab == ConfigTab.GENERAL) {
            drawGeneral(graphics);
        }
    }

    @Override
    public void onClose() {
        ConfigService.INSTANCE.save();
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    private record RankedBlock(Block block, int rank, int nameLength, String name, String id) {
    }
}
