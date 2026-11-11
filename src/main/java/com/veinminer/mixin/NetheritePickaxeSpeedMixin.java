package com.veinminer.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Speed boost for diamond and netherite pickaxes while the player has Haste II.
 *
 * <p>This targets {@link Player#getDestroySpeed(BlockState)} instead of
 * {@link ItemStack#getDestroySpeed(BlockState)} because an {@link ItemStack} does not know
 * which player is holding it, so the Haste check cannot be done from the stack alone.
 *
 * <p>Vanilla diamond/netherite pickaxe base speed is 8.0F/9.0F. With 75.0F, once Haste II is active:
 * - Tuff &amp; Stone (hardness 1.5): insta-mined with Efficiency 4/5 + Haste.
 * - Deepslate &amp; Stone Ores (hardness 3.0): insta-mined with Efficiency 4/5 + Haste.
 * - Cobbled Deepslate (hardness 3.5): insta-mined with Efficiency 4/5 + Haste.
 * - Deepslate Ores (hardness 4.5): insta-mined with Efficiency 5 + Haste 2.
 *
 * <p>The boost is applied before vanilla adds Efficiency and applies the Haste multiplier,
 * so those bonuses still behave exactly as they do in vanilla.
 */
@Mixin(Player.class)
public abstract class NetheritePickaxeSpeedMixin {

    @Unique
    private static final float PICKAXE_BASE_SPEED = 75.0F;

    @Unique
    private static final int HASTE_2_AMPLIFIER = 1;

    @Redirect(
            method = "getDestroySpeed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"
            )
    )
    private float boostPickaxeSpeed(ItemStack stack, BlockState state) {
        float originalSpeed = stack.getDestroySpeed(state);

        if (originalSpeed > 1.0F && isBoostedPickaxe(stack)) {
            Player player = (Player) (Object) this;
            MobEffectInstance haste = player.getEffect(MobEffects.HASTE);

            // Haste I has amplifier 0, so Haste II (or stronger) is amplifier >= 1.
            if (haste != null && haste.getAmplifier() >= HASTE_2_AMPLIFIER) {
                return PICKAXE_BASE_SPEED;
            }
        }

        return originalSpeed;
    }

    @Unique
    private static boolean isBoostedPickaxe(ItemStack stack) {
        return stack.getItem() == Items.DIAMOND_PICKAXE || stack.getItem() == Items.NETHERITE_PICKAXE;
    }
}
