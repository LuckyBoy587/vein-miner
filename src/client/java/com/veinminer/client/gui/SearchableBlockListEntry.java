package com.veinminer.client.gui;

import com.veinminer.config.VeinMinerConfigData;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class SearchableBlockListEntry extends AbstractConfigListEntry<Map<String, Boolean>> {

    private final EditBox searchBox;
    private final List<Block> displayedBlocks = new ArrayList<>();
    private final Map<String, Button> toggleButtons = new LinkedHashMap<>();
    private boolean edited = false;

    public SearchableBlockListEntry(Component title) {
        super(title, false);

        Font font = Minecraft.getInstance().font;
        this.searchBox = new EditBox(font, 0, 0, 200, 20, Component.literal("Search blocks..."));
        this.searchBox.setHint(Component.literal("Type to filter blocks..."));
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(this::onSearchChanged);

        updateDisplayedBlocks("");
    }

    private static ItemStack safeCreateItemStack(Block block) {
        try {
            net.minecraft.world.item.Item item = block.asItem();
            if (item != Items.AIR && item.builtInRegistryHolder().areComponentsBound()) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {
        }
        return ItemStack.EMPTY;
    }

    private void onSearchChanged(String query) {
        updateDisplayedBlocks(query);
    }

    private void updateDisplayedBlocks(String query) {
        displayedBlocks.clear();
        toggleButtons.clear();

        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        String trimmed = query.trim().toLowerCase(Locale.ROOT);

        if (trimmed.isEmpty()) {
            // When search is empty, show all blocks currently enabled (toggled ON)
            for (Map.Entry<String, Boolean> entry : config.entrySet()) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    Identifier id = Identifier.tryParse(entry.getKey());
                    if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
                        Block block = BuiltInRegistries.BLOCK.getValue(id);
                        if (block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR) {
                            displayedBlocks.add(block);
                            createToggleButton(entry.getKey());
                        }
                    }
                }
            }
            return;
        }

        // When searching, find up to 15 matching blocks
        int count = 0;
        for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
                continue;
            }

            String name = block.getName().getString().toLowerCase(Locale.ROOT);
            String idStr = id.toString().toLowerCase(Locale.ROOT);

            if (name.contains(trimmed) || idStr.contains(trimmed)) {
                displayedBlocks.add(block);
                createToggleButton(id.toString());
                if (++count >= 15) {
                    break;
                }
            }
        }
    }

    private void createToggleButton(String key) {
        VeinMinerConfigData config = VeinMinerConfigData.INSTANCE;
        Button btn = Button.builder(getToggleText(key), b -> {
            boolean next = !config.getOrDefault(key, false);
            config.put(key, next);
            edited = true;
            b.setMessage(getToggleText(key));
        }).bounds(0, 0, 48, 18).build();

        toggleButtons.put(key, btn);
    }

    private Component getToggleText(String key) {
        boolean enabled = VeinMinerConfigData.INSTANCE.getOrDefault(key, false);
        return enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }

    @Override
    public int getItemHeight() {
        int height = 28; // search box + margin
        String query = searchBox.getValue().trim();

        if (query.isEmpty()) {
            if (displayedBlocks.isEmpty()) {
                height += 18; // "Type to search blocks..." hint
            } else {
                height += 16 + (displayedBlocks.size() * 24); // "Currently Enabled Blocks" header + rows
            }
        } else {
            if (displayedBlocks.isEmpty()) {
                height += 18; // "No blocks found" hint
            } else {
                height += displayedBlocks.size() * 24; // matching rows
            }
        }

        return height;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x,
                                   int entryWidth, int entryHeight, int mouseX, int mouseY,
                                   boolean isHovered, float delta) {
        super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);

        Font font = Minecraft.getInstance().font;
        int currentY = y + 2;

        // 1. Search Box
        int searchWidth = Math.min(entryWidth - 8, 280);
        int searchX = x + (entryWidth - searchWidth) / 2;
        searchBox.setX(searchX);
        searchBox.setY(currentY);
        searchBox.setWidth(searchWidth);
        searchBox.setHeight(20);
        searchBox.extractRenderState(graphics, mouseX, mouseY, delta);

        currentY += 26;

        String query = searchBox.getValue().trim();

        // 2. Empty query state
        if (query.isEmpty()) {
            if (displayedBlocks.isEmpty()) {
                graphics.text(font, Component.literal("Type above to search from all blocks..."), x + 10, currentY + 3, 0xFF888888);
                return;
            } else {
                graphics.text(font, Component.literal("Enabled Blocks (" + displayedBlocks.size() + "):"), x + 4, currentY, 0xFF55FF55);
                currentY += 14;
            }
        } else if (displayedBlocks.isEmpty()) {
            graphics.text(font, Component.literal("No blocks matching \"" + query + "\""), x + 10, currentY + 3, 0xFFAAAAAA);
            return;
        }

        // 3. Render Block Rows
        int btnWidth = 48;
        int btnHeight = 18;
        int maxTextWidth = Math.max(10, entryWidth - 26 - btnWidth - 8);

        for (Block block : displayedBlocks) {
            graphics.fill(x + 2, currentY, x + entryWidth - 2, currentY + 22, 0x1AFFFFFF);

            // Block Icon (Item model if components bound, or clean material tile fallback on main menu)
            ItemStack itemStack = safeCreateItemStack(block);
            if (!itemStack.isEmpty()) {
                graphics.fakeItem(itemStack, x + 5, currentY + 3);
            } else {
                int mapCol = 0xFF000000 | block.defaultMapColor().col;
                graphics.fill(x + 5, currentY + 3, x + 21, currentY + 19, 0xFF000000);
                graphics.fill(x + 6, currentY + 4, x + 20, currentY + 18, mapCol);
                graphics.fill(x + 6, currentY + 4, x + 20, currentY + 7, 0x55FFFFFF);
            }

            // Block Name (0xFFFFFFFF ensures alpha is 255)
            String displayName = font.plainSubstrByWidth(block.getName().getString(), maxTextWidth);
            graphics.text(font, displayName, x + 26, currentY + 6, 0xFFFFFFFF);

            String key = BuiltInRegistries.BLOCK.getKey(block).toString();
            Button btn = toggleButtons.get(key);
            if (btn != null) {
                btn.setX(x + entryWidth - btnWidth - 4);
                btn.setY(currentY + 2);
                btn.setWidth(btnWidth);
                btn.setHeight(btnHeight);
                btn.extractRenderState(graphics, mouseX, mouseY, delta);
            }

            currentY += 24;
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> list = new ArrayList<>();
        list.add(searchBox);
        list.addAll(toggleButtons.values());
        return list;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        List<NarratableEntry> list = new ArrayList<>();
        list.add(searchBox);
        list.addAll(toggleButtons.values());
        return list;
    }

    @Override
    public Map<String, Boolean> getValue() {
        return VeinMinerConfigData.INSTANCE;
    }

    @Override
    public Optional<Map<String, Boolean>> getDefaultValue() {
        return Optional.of(Collections.emptyMap());
    }

    @Override
    public boolean isEdited() {
        return edited;
    }

    @Override
    public void save() {
        edited = false;
    }
}
