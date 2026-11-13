package com.veinminer.client.gui;

import com.veinminer.classification.ToolMaterial;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Small rendering helper shared by the configuration lists. Item stacks can only
 * be created when the item registry has its components bound, which is not the
 * case on the main menu, so a coloured tile is used as a fallback. Blocks are
 * coloured from their map colour and tools from their material.
 */
final class GuiItemStacks {

    private GuiItemStacks() {
    }

    static boolean isVisible(Block block) {
        return block != null && block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR;
    }

    static ItemStack stackFor(Block block) {
        try {
            Item item = block.asItem();
            if (item != Items.AIR && item.builtInRegistryHolder().areComponentsBound()) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {
        }
        return ItemStack.EMPTY;
    }

    static ItemStack stackFor(Item item) {
        try {
            if (item != Items.AIR && item.builtInRegistryHolder().areComponentsBound()) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {
        }
        return ItemStack.EMPTY;
    }

    static void drawBlock(GuiGraphicsExtractor graphics, Block block, int x, int y) {
        ItemStack stack = stackFor(block);
        if (!stack.isEmpty()) {
            graphics.fakeItem(stack, x, y);
            return;
        }

        int colour = 0xFF000000 | block.defaultMapColor().col;
        graphics.fill(x, y, x + 16, y + 16, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + 15, y + 15, colour);
        graphics.fill(x + 1, y + 1, x + 15, y + 4, 0x55FFFFFF);
    }

    static void drawItem(GuiGraphicsExtractor graphics, Item item, ToolMaterial material, int x, int y) {
        ItemStack stack = stackFor(item);
        if (!stack.isEmpty()) {
            graphics.fakeItem(stack, x, y);
            return;
        }

        int colour = 0xFF000000 | materialColour(material);
        graphics.fill(x, y, x + 16, y + 16, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + 15, y + 15, colour);
        graphics.fill(x + 1, y + 1, x + 15, y + 4, 0x55FFFFFF);
    }

    /**
     * Representative colour for a tool material, mirroring how blocks fall back
     * to their map colour.
     */
    private static int materialColour(ToolMaterial material) {
        return switch (material.id()) {
            case "wooden" -> 0xFF9A6D3B;
            case "stone" -> 0xFF8F8F8F;
            case "iron" -> 0xFFC8C8C8;
            case "gold" -> 0xFFF2D64B;
            case "diamond" -> 0xFF5BE0D6;
            case "netherite" -> 0xFF50454E;
            default -> 0xFF4A4A4A;
        };
    }
}
