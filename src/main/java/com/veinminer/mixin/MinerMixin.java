package com.veinminer.mixin;

import com.veinminer.apis.Task;
import com.veinminer.utils.MiningTask;
import com.veinminer.utils.VeinMinerConfigImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerGameMode.class)
public class MinerMixin {
    @Unique
    private static final VeinMinerConfigImpl veinMinerConfig = new VeinMinerConfigImpl();
    @Final
    @Shadow
    protected ServerPlayer player;
    @Unique
    private List<Task> tasks;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(ServerPlayer player, CallbackInfo ci) {
        tasks = new ArrayList<>();
    }

    @Inject(method = "destroyAndAck", at = @At("HEAD"))
    private void onDestroyAndAck(BlockPos pos, int sequence, String exitId, CallbackInfo ci) {
        if (!veinMinerConfig.isVeinMinerEnabled()) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!veinMinerConfig.isVeinMinerTool(heldItem)) {
            return;
        }

        ServerLevel world = player.level();
        BlockState state = world.getBlockState(pos);
        if (!veinMinerConfig.isVeinBlock(state)) {
            return;
        }
        MiningTask miningTask = new MiningTask(player, pos);
        tasks.add(miningTask);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        tasks.removeIf(Task::isComplete);
        for (Task task : tasks) {
            task.perform();
        }
    }
}
