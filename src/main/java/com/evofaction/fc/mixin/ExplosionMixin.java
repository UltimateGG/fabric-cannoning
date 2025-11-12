package com.evofaction.fc.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(Explosion.class)
public class ExplosionMixin {
    /**
     * Old MC MathHelper.sqrt casted to a float and back to a double.
     * This causes tiny decimal differences compared to 1.20.
     */
    @Redirect(
        method = "collectBlocksAndDamageEntities",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;sqrt(D)D",
            remap = false
        )
    )
    private double legacySqrt(double dist) {
        return (float) Math.sqrt(dist);
    }

    // Effectivley removes mojang instanceof tnt entity check to align with 1.8
    @Redirect(
        method = "collectBlocksAndDamageEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getY()D"
        )
    )
    private double fixEyeHeight(Entity instance) {
        return instance.getEyeY();
    }
}
