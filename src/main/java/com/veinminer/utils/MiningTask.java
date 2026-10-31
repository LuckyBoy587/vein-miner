package com.veinminer.utils;

import com.veinminer.apis.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class MiningTask implements Task {
    private final ServerLevel world;
    private final ServerPlayer player;
    private final BlockState blockState;

    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Set<BlockPos> mined = new HashSet<>();

    public MiningTask(ServerPlayer player, BlockPos pos) {
        this.player = player;
        this.world = player.level();
        this.blockState = world.getBlockState(pos);
        queue.add(pos);
    }

    @Override
    public void perform() {
        if (queue.isEmpty()) {
            return;
        }

        int blocksToMine = queue.size();
        while (!queue.isEmpty() && blocksToMine-- > 0) {
            BlockPos currentPos = queue.poll();
            player.gameMode.destroyBlock(currentPos);
            mined.add(currentPos);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighborPos = currentPos.offset(dx, dy, dz);
                        if (mined.contains(neighborPos)) continue;
                        if (world.getBlockState(neighborPos).equals(blockState) && !queue.contains(neighborPos)) {
                            queue.add(neighborPos);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isComplete() {
        return queue.isEmpty();
    }
}
