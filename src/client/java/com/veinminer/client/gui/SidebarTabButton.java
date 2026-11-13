package com.veinminer.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A left-hand navigation tab. Vanilla buttons are centre aligned and always use
 * the same sprite, so this widget provides the selected/hover states the
 * two-panel configuration layout needs.
 */
final class SidebarTabButton extends AbstractWidget {

    private static final int BACKGROUND_SELECTED = 0xFF2B3A4A;
    private static final int BACKGROUND_HOVERED = 0xFF242424;
    private static final int ACCENT = 0xFF4C9AFF;
    private static final int TEXT_SELECTED = 0xFFFFFFFF;
    private static final int TEXT_HOVERED = 0xFFE0E0E0;
    private static final int TEXT_NORMAL = 0xFFA0A0A0;

    private final Font font;
    private final ConfigTab tab;
    private final boolean selected;
    private final Runnable onPress;

    SidebarTabButton(Font font, int x, int y, int width, int height, ConfigTab tab, boolean selected, Runnable onPress) {
        super(x, y, width, height, tab.label());
        this.font = font;
        this.tab = tab;
        this.selected = selected;
        this.onPress = onPress;
    }

    ConfigTab tab() {
        return tab;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        boolean hovered = isHoveredOrFocused();

        if (selected) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, BACKGROUND_SELECTED);
            graphics.fill(getX(), getY(), getX() + 3, getY() + height, ACCENT);
        } else if (hovered) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, BACKGROUND_HOVERED);
        }

        int textColour = selected ? TEXT_SELECTED : (hovered ? TEXT_HOVERED : TEXT_NORMAL);
        graphics.text(font, getMessage(), getX() + 12, getY() + (height - font.lineHeight) / 2, textColour);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (active && isValidClickButton(event.buttonInfo()) && isMouseOver(event.x(), event.y())) {
            Button.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            onPress.run();
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
