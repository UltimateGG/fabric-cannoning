package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.block.*;
import net.minecraft.block.enums.PistonType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

// The above mixin does all the fixing. This mixin pretty much just optimizes it
// so that the head doesn't update and fire a "block break" event which would spawn
// particles and play an additional sound.
@Mixin(PistonHeadBlock.class)
class OldPistonRetraction_HeadMixin {
    /**
     * Disables the default behavior of "if the base updates and the head cannot exist here, return air
     * as the new block state". This triggers a .replace and spawns particles. We handle head removal below.
     */
    @Redirect(
        method = "getStateForNeighborUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;canPlaceAt(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"
        )
    )
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (Config.OLD_PISTON_RETRACTION) return true;

        return state.canPlaceAt(world, pos);
    }

    /**
     * When the head block gets an update, check if it can exist here (base is same type, facing correct direction)
     * and if it is not we manually set the block to air.
     */
    @Inject(
        method = "neighborUpdate",
        at = @At("HEAD"),
        cancellable = true
    )
    public void neighborUpdate(BlockState headState, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (!Config.OLD_PISTON_RETRACTION) return;

        ci.cancel();

        BlockPos pistonBasePos = pos.offset(headState.get(PistonHeadBlock.FACING).getOpposite());
        BlockState pistonBaseState = world.getBlockState(pistonBasePos);
        Block headBlockType = headState.get(PistonHeadBlock.TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;

        // Removed check for extended & moving piston
        boolean canExist = pistonBaseState.isOf(headBlockType) && pistonBaseState.get(PistonHeadBlock.FACING) == headState.get(PistonHeadBlock.FACING);

        if (canExist) {
            world.updateNeighbor(pistonBasePos, sourceBlock, sourcePos);
        } else {
            // Remove the head block
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }
    }
}
