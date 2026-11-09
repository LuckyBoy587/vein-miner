package com.veinminer.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class NetheritePickaxeSpeedMixin {

    /**
     * Vanilla netherite pickaxe base speed is 9.0F.
     * With 75.0F:
     * - Tuff & Stone (hardness 1.5): insta-mined with Efficiency 4/5 + Haste.
     * - Deepslate & Stone Ores (hardness 3.0): insta-mined with Efficiency 4/5 + Haste.
     * - Cobbled Deepslate (hardness 3.5): insta-mined with Efficiency 4/5 + Haste.
     * - Deepslate Ores (hardness 4.5): insta-mined with Efficiency 5 + Haste 2.
     */
    @Unique
    private static final float NETHERITE_PICKAXE_BASE_SPEED = 75.0F;

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void boostNetheriteSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // Apply general base speed boost to any block the netherite pickaxe is effective against
        if (stack.is(Items.NETHERITE_PICKAXE) && cir.getReturnValue() > 1.0F) {
            cir.setReturnValue(NETHERITE_PICKAXE_BASE_SPEED);
        }
    }
}
