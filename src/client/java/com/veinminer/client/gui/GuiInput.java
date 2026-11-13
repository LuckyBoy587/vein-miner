package com.veinminer.client.gui;

import net.minecraft.client.input.MouseButtonEvent;

/**
 * Mouse button helpers.
 *
 * <p>In this Minecraft version the primary mouse button is {@code 1}, not
 * {@code 0}: vanilla's {@code AbstractWidget.isValidClickButton} compares the
 * passed button against {@code 1}. Selection list entries do not inherit that
 * helper, so the check is centralised here.</p>
 */
final class GuiInput {

    static final int PRIMARY_BUTTON = 1;

    private GuiInput() {
    }

    static boolean isPrimaryClick(MouseButtonEvent event) {
        return event.button() == PRIMARY_BUTTON;
    }
}
