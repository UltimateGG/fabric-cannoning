package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import com.evofaction.fc.server.ProtectionBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(EntityExplosionBehavior.class)
public class EntityExplosionBehaviorMixin {
    @Inject(
        method = "canDestroyBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    public void canDestroy(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.PROTECTION_BLOCK_ENABLED) return;

        if (ProtectionBlock.isProtectedColumn((World) world, pos.getX(), pos.getZ()))
            cir.setReturnValue(false);
    }
}
