package com.veinminer.utils;

import com.veinminer.VeinMiner;
import com.veinminer.apis.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class MiningTask implements Task {
    public static final int MAX_BLOCKS_PER_TICK = 1000;
    private static final VeinMinerConfigImpl veinMinerConfig = new VeinMinerConfigImpl();
    private final ServerLevel world;
    private final ServerPlayer player;
    private final String blockName;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Set<BlockPos> mined = new HashSet<>();

    public MiningTask(ServerPlayer player, BlockPos pos) {
        this.player = player;
        this.world = player.level();
        this.blockName = getBlockName(pos);
        addNeighbourBlocks(pos);

        VeinMiner.log("MiningTask created for player: " + player.getName().getString() + " at position: " + pos + "for block: " + blockName);
    }

    @Override
    public void perform() {
        if (queue.isEmpty()) {
            return;
        }

        int blocksToMine = Math.min(MAX_BLOCKS_PER_TICK, queue.size());
        while (!queue.isEmpty() && blocksToMine-- > 0) {
            BlockPos currentPos = queue.poll();
            if (!mineBlock(currentPos)) {
                VeinMiner.log("Stopped mining early");
                break;
            }

            addNeighbourBlocks(currentPos);
        }
    }

    private void addNeighbourBlocks(BlockPos currentPos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos neighborPos = currentPos.offset(dx, dy, dz);
                    if (mined.contains(neighborPos)) continue;
                    if (world.isLoaded(neighborPos) && getBlockName(neighborPos).equals(blockName) && !queue.contains(neighborPos)) {
                        queue.add(neighborPos);
                    }
                }
            }
        }
    }

    @Override
    public boolean isComplete() {
        return queue.isEmpty();
    }

    private String getBlockName(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock().getName().getString();
    }

    private boolean mineBlock(BlockPos pos) {
        ItemStack heldItem = player.getMainHandItem();
        if (!veinMinerConfig.isVeinMinerTool(heldItem)) {
            queue.clear();
            return false;
        }
        player.gameMode.destroyBlock(pos);
        mined.add(pos);
        return true;
    }
}
