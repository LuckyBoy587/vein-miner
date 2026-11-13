package com.veinminer.client.gui;

import com.veinminer.config.ConfigService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;

/**
 * Scrollable block list used by the Blocks tab. Blocks are supplied by the
 * screen so that search/filtering stays in one place. The list reloads lazily
 * on the next frame, which keeps it safe to toggle a block while the list is
 * dispatching a mouse click.
 */
final class BlockListWidget extends ObjectSelectionList<BlockListWidget.BlockEntry> {

    private static final int ROW_HEIGHT = 24;

    private final Font font;
    private final Supplier<List<Block>> blocksSupplier;
    private final Supplier<Component> emptyMessageSupplier;

    private boolean dirty;
    private boolean resetScroll;

    BlockListWidget(Minecraft minecraft,
                     Font font,
                     int x,
                     int y,
                     int width,
                     int height,
                     Supplier<List<Block>> blocksSupplier,
                     Supplier<Component> emptyMessageSupplier) {
        super(minecraft, width, height, y, ROW_HEIGHT);
        this.font = font;
        this.blocksSupplier = blocksSupplier;
        this.emptyMessageSupplier = emptyMessageSupplier;
        this.updateSizeAndPosition(width, height, x, y);
        reload(false);
    }

    void markDirty(boolean resetScrollToTop) {
        this.dirty = true;
        this.resetScroll |= resetScrollToTop;
    }

    private void reload(boolean resetScrollToTop) {
        double previousScroll = scrollAmount();

        clearEntries();
        for (Block block : blocksSupplier.get()) {
            addEntry(new BlockEntry(block));
        }

        if (resetScrollToTop) {
            setScrollAmount(0.0D);
        } else {
            setScrollAmount(Math.min(previousScroll, maxScrollAmount()));
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (dirty) {
            boolean reset = resetScroll;
            dirty = false;
            resetScroll = false;
            reload(reset);
        }

        super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);

        if (getItemCount() == 0) {
            graphics.centeredText(
                    font,
                    emptyMessageSupplier.get(),
                    getX() + getWidth() / 2,
                    getY() + 28,
                    0xFFAAAAAA
            );
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

    final class BlockEntry extends ObjectSelectionList.Entry<BlockEntry> {

        private final Block block;

        private BlockEntry(Block block) {
            this.block = block;
        }

        @Override
        public Component getNarration() {
            return block.getName();
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

            GuiItemStacks.drawBlock(graphics, block, x + 4, y + 4);

            boolean enabled = ConfigService.INSTANCE.isBlockEnabled(block);
            Component state = enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
            int stateWidth = font.width(state);
            int textWidth = Math.max(10, width - 32 - stateWidth - 12);

            String name = font.plainSubstrByWidth(block.getName().getString(), textWidth);
            graphics.text(font, name, x + 24, y + 8, 0xFFFFFFFF);
            graphics.text(font, state, x + width - stateWidth - 8, y + 8,
                    enabled ? 0xFF55FF55 : 0xFFAAAAAA);

            if (hovered) {
                graphics.setTooltipForNextFrame(font, Component.literal(blockId()), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (GuiInput.isPrimaryClick(event) && isMouseOver(event.x(), event.y())) {
                boolean next = !ConfigService.INSTANCE.isBlockEnabled(block);
                ConfigService.INSTANCE.setBlockEnabled(block, next);
                markDirty(false);
                return true;
            }
            return false;
        }

        private String blockId() {
            return BuiltInRegistries.BLOCK.getKey(block).toString();
        }
    }
}
