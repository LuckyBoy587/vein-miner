package com.veinminer.mixin;

import com.veinminer.apis.Task;
import com.veinminer.utils.MiningTask;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
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
    private final List<Task> tasks = new ArrayList<>();
    @Final
    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyAndAck", at = @At("HEAD"))
    private void onDestroyAndAck(BlockPos pos, int sequence, String exitId, CallbackInfo ci) {
        if (player != null) {
            MiningTask miningTask = new MiningTask(player, pos);
            tasks.add(miningTask);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        tasks.removeIf(Task::isComplete);
        for (Task task : tasks) {
            task.perform();
        }
    }
}
