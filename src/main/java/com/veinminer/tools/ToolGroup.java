package com.veinminer.tools;

import net.minecraft.world.item.Item;

import java.util.List;

public abstract class ToolGroup {
    public abstract List<Item> getTools();
    public abstract String getName();
}
