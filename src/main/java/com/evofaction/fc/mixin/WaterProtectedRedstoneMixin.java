package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(FlowableFluid.class)
public class WaterProtectedRedstoneMixin {
    @Redirect(
        method = "canFill(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/Fluid;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z",
            ordinal = 3
        )
    )
    public boolean isNonBreakable(BlockState state, Block originalBlock) {
        boolean original = state.isOf(originalBlock);
        if (original || !Config.WATER_PROTECTED_REDSTONE) return original;

        // Slight optimization: Check common cases first (air, water).
        if (state.isAir() || !state.getFluidState().isEmpty()) return false;

        return state.isOf(Blocks.REDSTONE_WIRE)
            || state.isOf(Blocks.REPEATER)
            || state.isOf(Blocks.COMPARATOR)
            || state.isOf(Blocks.REDSTONE_TORCH)
            || state.isOf(Blocks.REDSTONE_WALL_TORCH)
            || state.isOf(Blocks.LEVER)
            || state.isIn(BlockTags.BUTTONS);
    }
}
