package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;


@Mixin(Entity.class)
public class PreciseMovementMixin {
    /**
     * There is a check in entity movement that if (after collisions) the distance to move is
     * less than a threshold (0.0000001) the position is not actually saved. I don't really know
     * why, since all the work has already been done. Some floating point error/closeness thing.
     * But 1.8 did not have this, and it actually really adds up. For me, it was wrecking swing.
     */
    @ModifyConstant(
        method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
        constant = @Constant(
            doubleValue = 1.0E-7,
            ordinal = 1
        )
    )
    public double morePreciseMovement(double original) {
        if (!Config.WIP) return original;

        Entity self = (Entity) (Object) this;
        if (self instanceof TntEntity || self instanceof FallingBlockEntity)
            return 0.0D;

        return original;
    }
}
