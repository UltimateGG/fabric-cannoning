package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class OnePushWebsMixin {
    @Shadow
    protected Vec3d movementMultiplier;

    // If an entity is moved by piston, set its movementMultiplier to 0.
    // This is essentially how it's implemented in 1.21.10
    @Inject(
        method = "move",
        at = @At("HEAD")
    )
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if (Config.ONE_PUSH_WEBS && movementType == MovementType.PISTON) {
            this.movementMultiplier = Vec3d.ZERO;
        }
    }
}
