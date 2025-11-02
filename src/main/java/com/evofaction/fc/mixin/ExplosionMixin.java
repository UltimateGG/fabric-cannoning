package com.evofaction.fc.mixin;

import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(Explosion.class)
public class ExplosionMixin {
    @Redirect(
        method = "collectBlocksAndDamageEntities()V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;sqrt(D)D",
            ordinal = 1
        )
    )
    private double fixSqrt1(double a) {
        return (double)(float) Math.sqrt(a);
    }

    @Redirect(
        method = "collectBlocksAndDamageEntities()V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;sqrt(D)D",
            ordinal = 2
        )
    )
    private double fixSqrt(double a) {
        return (double)(float) Math.sqrt(a);
    }
}
