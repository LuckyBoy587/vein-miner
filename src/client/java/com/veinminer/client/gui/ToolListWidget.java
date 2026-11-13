package com.veinminer.client.gui;

import com.veinminer.classification.ToolEntry;
import com.veinminer.classification.ToolFamilies;
import com.veinminer.classification.ToolFamily;
import com.veinminer.config.ConfigService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

/**
 * Scrollable list of every supported tool, grouped by tool family.
 */
final class ToolListWidget extends ObjectSelectionList<ToolListWidget.Row> {

    private static final int HEADER_HEIGHT = 22;
    private static final int TOOL_HEIGHT = 24;

    private final Font font;

    ToolListWidget(Minecraft minecraft, Font font, int x, int y, int width, int height) {
        super(minecraft, width, height, y, TOOL_HEIGHT);
        this.font = font;
        this.updateSizeAndPosition(width, height, x, y);
        buildRows();
    }

    private void buildRows() {
        for (ToolFamily family : ToolFamilies.all()) {
            addEntry(new FamilyHeaderRow(family), HEADER_HEIGHT);
            for (ToolEntry tool : family.entries()) {
                addEntry(new ToolRow(tool), TOOL_HEIGHT);
            }
        }
    }

    @Override
    public int getRowWidth() {
        return Math.max(40, getWidth() - 20);
    }

    @Override
    protected int scrollBarX() {
        return getX() + getWidth() - 8;
    }

    abstract static class Row extends ObjectSelectionList.Entry<Row> {
    }

    final class FamilyHeaderRow extends Row {

        private final ToolFamily family;

        private FamilyHeaderRow(ToolFamily family) {
            this.family = family;
        }

        @Override
        public Component getNarration() {
            return Component.literal(family.displayName());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int x = getX();
            int y = getY();
            int width = getWidth();

            graphics.fill(x, y, x + width, y + getHeight(), 0x18FFFFFF);
            graphics.text(font, Component.literal(family.displayName()), x + 6, y + 6, 0xFFFFD166);
            graphics.fill(x + 6, y + getHeight() - 2, x + width - 6, y + getHeight() - 1, 0x33FFFFFF);
        }
    }

    final class ToolRow extends Row {

        private final ToolEntry tool;

        private ToolRow(ToolEntry tool) {
            this.tool = tool;
        }

        @Override
        public Component getNarration() {
            return Component.translatable(tool.item().getDescriptionId());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int x = getX();
            int y = getY();
            int width = getWidth();
            int height = getHeight();

            if (hovered) {
                graphics.fill(x, y, x + width, y + height, 0x22FFFFFF);
            }

            GuiItemStacks.drawItem(graphics, tool.item(), x + 8, y + 4);

            boolean enabled = ConfigService.INSTANCE.isToolEnabled(tool.item());
            Component state = enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
            int stateWidth = font.width(state);
            int textWidth = Math.max(10, width - 40 - stateWidth - 12);

            String name = font.plainSubstrByWidth(Component.translatable(tool.item().getDescriptionId()).getString(), textWidth);
            graphics.text(font, name, x + 30, y + 8, 0xFFFFFFFF);
            graphics.text(font, state, x + width - stateWidth - 8, y + 8,
                    enabled ? 0xFF55FF55 : 0xFFAAAAAA);

            if (hovered) {
                graphics.setTooltipForNextFrame(font, Component.literal(itemId()), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (GuiInput.isPrimaryClick(event) && isMouseOver(event.x(), event.y())) {
                boolean next = !ConfigService.INSTANCE.isToolEnabled(tool.item());
                ConfigService.INSTANCE.setToolEnabled(tool.item(), next);
                return true;
            }
            return false;
        }

        private String itemId() {
            Item item = tool.item();
            return BuiltInRegistries.ITEM.getKey(item).toString();
        }
    }
}
