package com.veinminer.client.gui;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.function.Consumer;

public class CustomBlockSearchEntry extends AbstractConfigListEntry<List<String>> {

    private final List<String> originalBlocks;
    private final List<String> selectedBlocks;
    private final Consumer<List<String>> saveCallback;

    private final EditBox searchBox;
    private final List<BlockScore> matchingBlocks = new ArrayList<>();
    private final Map<String, Button> addButtons = new LinkedHashMap<>();
    private final Map<String, Button> removeButtons = new LinkedHashMap<>();

    private record BlockScore(Block block, Identifier id, int score, String name) {
    }

    public CustomBlockSearchEntry(Component fieldName, List<String> initialBlocks, Consumer<List<String>> saveCallback) {
        super(fieldName, false);
        this.originalBlocks = new ArrayList<>(initialBlocks != null ? initialBlocks : Collections.emptyList());
        this.selectedBlocks = new ArrayList<>(this.originalBlocks);
        this.saveCallback = saveCallback;

        Font font = Minecraft.getInstance().font;
        this.searchBox = new EditBox(font, 0, 0, 200, 20, Component.translatable("text.vein-miner.config.custom_blocks.search_hint"));
        this.searchBox.setHint(Component.translatable("text.vein-miner.config.custom_blocks.search_hint"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(this::onSearchQueryChanged);

        rebuildSelectedButtons();
    }

    private void onSearchQueryChanged(String query) {
        matchingBlocks.clear();
        addButtons.clear();

        String trimmed = query.trim().toLowerCase(Locale.ROOT);
        if (!trimmed.isEmpty()) {
            List<BlockScore> candidates = new ArrayList<>();
            for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
                Block block = BuiltInRegistries.BLOCK.getValue(id);
                if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
                    continue;
                }

                String idStr = id.toString().toLowerCase(Locale.ROOT);
                String path = id.getPath().toLowerCase(Locale.ROOT);
                String name = block.getName().getString().toLowerCase(Locale.ROOT);

                int score = -1;
                if (idStr.equals(trimmed) || path.equals(trimmed) || name.equals(trimmed)) {
                    score = 0;
                } else if (name.startsWith(trimmed) || path.startsWith(trimmed)) {
                    score = 1;
                } else if (name.contains(trimmed) || idStr.contains(trimmed)) {
                    score = 2;
                }

                if (score != -1) {
                    candidates.add(new BlockScore(block, id, score, name));
                }
            }

            candidates.sort(Comparator.comparingInt((BlockScore b) -> b.score).thenComparing(b -> b.name));

            int limit = Math.min(10, candidates.size());
            for (int i = 0; i < limit; i++) {
                BlockScore match = candidates.get(i);
                matchingBlocks.add(match);

                String idStr = match.id.toString();
                boolean alreadyAdded = selectedBlocks.contains(idStr);
                Button btn = Button.builder(
                        Component.translatable(alreadyAdded
                                ? "text.vein-miner.config.custom_blocks.added"
                                : "text.vein-miner.config.custom_blocks.add"),
                        b -> addCustomBlock(idStr)
                ).bounds(0, 0, 46, 18).build();
                btn.active = !alreadyAdded;
                addButtons.put(idStr, btn);
            }
        }
    }

    private void addCustomBlock(String id) {
        if (!selectedBlocks.contains(id)) {
            selectedBlocks.add(id);
            rebuildSelectedButtons();
            updateAddButtonsState();
        }
    }

    private void removeCustomBlock(String id) {
        if (selectedBlocks.remove(id)) {
            rebuildSelectedButtons();
            updateAddButtonsState();
        }
    }

    private void rebuildSelectedButtons() {
        removeButtons.clear();
        for (String id : selectedBlocks) {
            Button btn = Button.builder(
                    Component.translatable("text.vein-miner.config.custom_blocks.remove"),
                    b -> removeCustomBlock(id)
            ).bounds(0, 0, 52, 18).build();
            removeButtons.put(id, btn);
        }
    }

    private void updateAddButtonsState() {
        for (Map.Entry<String, Button> entry : addButtons.entrySet()) {
            boolean alreadyAdded = selectedBlocks.contains(entry.getKey());
            Button btn = entry.getValue();
            btn.active = !alreadyAdded;
            btn.setMessage(Component.translatable(alreadyAdded
                    ? "text.vein-miner.config.custom_blocks.added"
                    : "text.vein-miner.config.custom_blocks.add"));
        }
    }

    @Override
    public int getItemHeight() {
        int height = 26; // search box + margin

        String query = searchBox.getValue().trim();
        if (!query.isEmpty()) {
            height += 16; // "Matching Blocks (Top 10)" header
            if (matchingBlocks.isEmpty()) {
                height += 16; // "No blocks found" message
            } else {
                height += matchingBlocks.size() * 24; // rows
            }
            height += 8; // spacing between sections
        }

        height += 16; // "Selected Custom Blocks" header
        if (selectedBlocks.isEmpty()) {
            height += 16; // empty notice
        } else {
            height += selectedBlocks.size() * 24; // rows
        }

        height += 6; // bottom padding
        return height;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
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

        // 2. Matching Results
        String query = searchBox.getValue().trim();
        if (!query.isEmpty()) {
            Component matchingHeader = Component.translatable("text.vein-miner.config.custom_blocks.search_results_title");
            graphics.text(font, matchingHeader, x + 4, currentY, 0xFFFF55);
            currentY += 14;

            if (matchingBlocks.isEmpty()) {
                Component noMatches = Component.translatable("text.vein-miner.config.custom_blocks.no_results", query);
                graphics.text(font, noMatches, x + 10, currentY, 0xAAAAAA);
                currentY += 16;
            } else {
                int btnWidth = 46;
                int btnHeight = 18;
                int maxTextWidth = Math.max(10, entryWidth - 26 - btnWidth - 8);

                for (BlockScore match : matchingBlocks) {
                    int rowY = currentY;
                    graphics.fill(x + 2, rowY, x + entryWidth - 2, rowY + 22, 0x22FFFFFF);

                    ItemStack itemStack = new ItemStack(match.block);
                    if (!itemStack.isEmpty()) {
                        graphics.fakeItem(itemStack, x + 5, rowY + 3);
                    }

                    String displayName = font.plainSubstrByWidth(match.block.getName().getString(), maxTextWidth);
                    String idStr = font.plainSubstrByWidth(match.id.toString(), maxTextWidth);
                    graphics.text(font, displayName, x + 26, rowY + 3, 0xFFFFFF);
                    graphics.text(font, idStr, x + 26, rowY + 13, 0x888888);

                    Button addBtn = addButtons.get(match.id.toString());
                    if (addBtn != null) {
                        addBtn.setX(x + entryWidth - btnWidth - 4);
                        addBtn.setY(rowY + 2);
                        addBtn.setWidth(btnWidth);
                        addBtn.setHeight(btnHeight);
                        addBtn.extractRenderState(graphics, mouseX, mouseY, delta);
                    }

                    currentY += 24;
                }
            }
            currentY += 8;
        }

        // 3. Selected Blocks
        Component selectedHeader = Component.translatable("text.vein-miner.config.custom_blocks.selected_title", selectedBlocks.size());
        graphics.text(font, selectedHeader, x + 4, currentY, 0x55FF55);
        currentY += 14;

        if (selectedBlocks.isEmpty()) {
            Component emptyNotice = Component.translatable("text.vein-miner.config.custom_blocks.empty_selected");
            graphics.text(font, emptyNotice, x + 10, currentY, 0x888888);
        } else {
            int btnWidth = 52;
            int btnHeight = 18;
            int maxTextWidth = Math.max(10, entryWidth - 26 - btnWidth - 8);

            for (String blockId : selectedBlocks) {
                int rowY = currentY;
                graphics.fill(x + 2, rowY, x + entryWidth - 2, rowY + 22, 0x22FFFFFF);

                Identifier id = Identifier.tryParse(blockId);
                Block block = (id != null && BuiltInRegistries.BLOCK.containsKey(id)) ? BuiltInRegistries.BLOCK.getValue(id) : null;

                if (block != null) {
                    ItemStack itemStack = new ItemStack(block);
                    if (!itemStack.isEmpty()) {
                        graphics.fakeItem(itemStack, x + 5, rowY + 3);
                    }
                    String displayName = font.plainSubstrByWidth(block.getName().getString(), maxTextWidth);
                    String idDisplay = font.plainSubstrByWidth(blockId, maxTextWidth);
                    graphics.text(font, displayName, x + 26, rowY + 3, 0xFFFFFF);
                    graphics.text(font, idDisplay, x + 26, rowY + 13, 0x888888);
                } else {
                    String idDisplay = font.plainSubstrByWidth(blockId, maxTextWidth);
                    graphics.text(font, idDisplay, x + 26, rowY + 6, 0xFF5555);
                }

                Button removeBtn = removeButtons.get(blockId);
                if (removeBtn != null) {
                    removeBtn.setX(x + entryWidth - btnWidth - 4);
                    removeBtn.setY(rowY + 2);
                    removeBtn.setWidth(btnWidth);
                    removeBtn.setHeight(btnHeight);
                    removeBtn.extractRenderState(graphics, mouseX, mouseY, delta);
                }

                currentY += 24;
            }
        }
    }

    @Override
    public List<String> getValue() {
        return selectedBlocks;
    }

    @Override
    public Optional<List<String>> getDefaultValue() {
        return Optional.of(Collections.emptyList());
    }

    @Override
    public void save() {
        if (saveCallback != null) {
            saveCallback.accept(new ArrayList<>(selectedBlocks));
        }
    }

    @Override
    public boolean isEdited() {
        return !selectedBlocks.equals(originalBlocks);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> list = new ArrayList<>();
        list.add(searchBox);
        list.addAll(addButtons.values());
        list.addAll(removeButtons.values());
        return list;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        List<NarratableEntry> list = new ArrayList<>();
        list.add(searchBox);
        list.addAll(addButtons.values());
        list.addAll(removeButtons.values());
        return list;
    }
}
