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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerGameMode.class)
public class MinerMixin {
    @Final
    @Shadow
    protected ServerPlayer player;

    @Unique
    private final List<Task> tasks = new ArrayList<>();

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.level().isClientSide()) {
            MiningTask miningTask = new MiningTask(player, pos);
            tasks.add(miningTask);
        }
    }

    public void onTick() {
        tasks.removeIf(Task::isComplete);
        for (Task task : tasks) {
            task.perform();
        }
    }
}
