package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PistonBlock.class)
class OldPistonRetractionMixin {
    /**
     * Old double piston extenders are broken in newer versions.
     * Almost every cannon relied on them for dropping and pushing sand.
     * Essentially, if two extended pistons retracted at the same time (depends on redstone update order),
     * the back pulled the front one. A piston is normally not supposed to be able to move an extended piston.
     * But this happened because when a piston got a block update, it immediately set its own state to extended=false,
     * but scheduled the actual retraction later. So when another piston came around to pull it,
     * it saw extended=false and pulled it, even though it was actually still extended.
     * <p>
     * This mixin pretty much adds the same line of code that was in the old version, having the affect as what the above
     * describes.
     */
    @Inject(
        method = "tryMove(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;addSyncedBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
            ordinal = 1
        )
    )
    public void onRetract(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (Config.OLD_PISTON_RETRACTION)
            world.setBlockState(pos, state.with(PistonBlock.EXTENDED, false), Block.NOTIFY_ALL);
    }
}
