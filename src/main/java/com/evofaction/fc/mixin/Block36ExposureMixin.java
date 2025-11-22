package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.block.BlockState;
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
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;


@Mixin(ServerWorld.class)
public abstract class Block36ExposureMixin extends World {
    protected Block36ExposureMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    /**
     * In 1.8 BlockPistonMoving override collisionRayTrace with always null.
     * Later, Mojang made it instead get the piston tile entity, and return the pushed block's collision box.
     * This option overrides that.
     * This is done here and not by overriding PistonExtensionBlock's getCollisionShape because that would affect
     * entity collisions, not just exposure.
     */
    @Override
    public BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
        if (!Config.BLOCK36_RESOLVES_COLLISION && state.getBlock() instanceof PistonExtensionBlock)
            return null;

        return super.raycastBlock(start, end, pos, shape, state);
    }
}
