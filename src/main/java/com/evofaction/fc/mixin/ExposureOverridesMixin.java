package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;


@Mixin(ServerWorld.class)
public abstract class ExposureOverridesMixin extends World {
    protected ExposureOverridesMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    /**
     * Overrides are done here and not by overriding the block's getCollisionShape because that would affect
     * entity collisions, not just exposure.
     * <p>
     * We have to override both this and the block's raycastShape because it does some initial check to see if
     * it hits the block at all, then checks its raycast shape.
     */
    @Override
    public BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
        Block block = state.getBlock();

        // In 1.8 BlockPistonMoving overrode collisionRayTrace with always null.
        // Later, Mojang made it instead get the piston tile entity, and return the pushed block's collision box.
        if (Config.OLD_BLOCK36_EXPOSURE && block instanceof PistonExtensionBlock)
            return null; // Moving piston never blocks exposure

        if (Config.OLD_LADDER_EXPOSURE && block instanceof LadderBlock)
            shape = state.getRaycastShape(this, pos);

        return super.raycastBlock(start, end, pos, shape, state);
    }
}

@SuppressWarnings("unused")
@Mixin(LadderBlock.class)
abstract class ExposureOverrides_LadderMixin extends Block {
    @Unique
    private static final double OLD_WIDTH = 2.0F;

    @Unique private static final VoxelShape OLD_EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, OLD_WIDTH, 16.0, 16.0);
    @Unique private static final VoxelShape OLD_WEST_SHAPE = Block.createCuboidShape(16.0 - OLD_WIDTH, 0.0, 0.0, 16.0, 16.0, 16.0);
    @Unique private static final VoxelShape OLD_SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, OLD_WIDTH);
    @Unique private static final VoxelShape OLD_NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 16.0 - OLD_WIDTH, 16.0, 16.0, 16.0);

    protected ExposureOverrides_LadderMixin(Settings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("deprecation") // Docs say deprecated for CALLING but not overriding
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        if (!Config.OLD_LADDER_EXPOSURE) return super.getRaycastShape(state, world, pos);

        return switch (state.get(LadderBlock.FACING)) {
            case EAST -> OLD_EAST_SHAPE;
            case WEST -> OLD_WEST_SHAPE;
            case SOUTH -> OLD_SOUTH_SHAPE;
            default -> OLD_NORTH_SHAPE; // and North
        };
    }
}
