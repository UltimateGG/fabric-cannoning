package com.evofaction.fc.mixin;

import com.evofaction.fc.FabricCannoning;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(Entity.class)
public class EastWestCannoningMixin {
    @ModifyVariable(
        method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;",
        at = @At(value = "STORE"),
        ordinal = 0
    )
    private static boolean eastWestCannoningFix(boolean original) {
        // Mojang actually implemented an "e/w patch", controlled by this bool.
        // But it is opposite of what we want in that it does the greater velocity first.
        //
        // The idea of the e/w patch is that it infers the direction you intend to shoot in based
        // on which axis has a greater velocity, since usually you give more exposure to that direction.
        //
        // For example, if there is more X velocity, you are (probably!) shooting towards X.
        // So to ensure the "floating" of a cannon works, we calculate the opposite direction you are
        // shooting in as the first (after Y) leg of the triangle, Z in this case.
        // This is so that it hits the left/right guider first, instead of going towards X first
        // and sliding across Z at the wall.

        if (FabricCannoning.EAST_WEST_CANNONING_FIX) return !original;

        return false; // 1.8 behavior (never change YXZ order)
    }
}
