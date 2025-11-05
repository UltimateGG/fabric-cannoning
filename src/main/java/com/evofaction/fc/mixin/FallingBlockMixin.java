package com.evofaction.fc.mixin;

import com.evofaction.fc.FabricCannoning;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockMixin extends Entity {
    protected FallingBlockMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    private BlockState block;

    @Shadow
    private boolean destroyedOnLanding;

    /**
     * @author UltimateGamer079
     * @reason Need full control to make falling blocks consistent with 1.8.
     *         The main thing is these large double numbers for precision.
     *         They are tiny, but add up and mess up precise cannons.
     */
    @Overwrite
    public void tick() {
        FallingBlockEntity self = (FallingBlockEntity) (Object)this;
        if (this.block.isAir()) {
            self.discard();
        } else {
            Block block = this.block.getBlock();
            self.timeFalling++;
            if (!self.hasNoGravity()) {
                self.setVelocity(self.getVelocity().add(0.0, -0.03999999910593033D, 0.0));
            }

            self.move(MovementType.SELF, self.getVelocity());
            if (!self.getWorld().isClient) {
                BlockPos blockPos = self.getBlockPos();
                boolean bl = this.block.getBlock() instanceof ConcretePowderBlock;
                boolean bl2 = bl && self.getWorld().getFluidState(blockPos).isIn(FluidTags.WATER);
                double d = self.getVelocity().lengthSquared();
                if (bl && d > 1.0) {
                    BlockHitResult blockHitResult = self.getWorld()
                        .raycast(
                            new RaycastContext(
                                new Vec3d(self.prevX, self.prevY, self.prevZ), self.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, self
                            )
                        );
                    if (blockHitResult.getType() != HitResult.Type.MISS && self.getWorld().getFluidState(blockHitResult.getBlockPos()).isIn(FluidTags.WATER)) {
                        blockPos = blockHitResult.getBlockPos();
                        bl2 = true;
                    }
                }

                if (self.isOnGround() || bl2) {
                    BlockState blockState = self.getWorld().getBlockState(blockPos);
                    self.setVelocity(self.getVelocity().multiply(0.699999988079071D, -0.5, 0.699999988079071D));
                    if (!blockState.isOf(Blocks.MOVING_PISTON)) {
                        if (!this.destroyedOnLanding) {
                            boolean bl3 = blockState.canReplace(new AutomaticItemPlacementContext(self.getWorld(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean bl4 = FallingBlock.canFallThrough(self.getWorld().getBlockState(blockPos.down())) && (!bl || !bl2);
                            boolean bl5 = this.block.canPlaceAt(self.getWorld(), blockPos) && !bl4;
                            if (bl3 && bl5) {
                                if (this.block.contains(Properties.WATERLOGGED) && self.getWorld().getFluidState(blockPos).getFluid() == Fluids.WATER) {
                                    this.block = this.block.with(Properties.WATERLOGGED, true);
                                }

                                if (self.getWorld().setBlockState(blockPos, this.block, Block.NOTIFY_ALL)) {
                                    ((ServerWorld)self.getWorld())
                                        .getChunkManager()
                                        .threadedAnvilChunkStorage
                                        .sendToOtherNearbyPlayers(self, new BlockUpdateS2CPacket(blockPos, self.getWorld().getBlockState(blockPos)));
                                    self.discard();
                                    if (block instanceof LandingBlock) {
                                        ((LandingBlock)block).onLanding(self.getWorld(), blockPos, this.block, blockState, self);
                                    }

                                    if (self.blockEntityData != null && this.block.hasBlockEntity()) {
                                        BlockEntity blockEntity = self.getWorld().getBlockEntity(blockPos);
                                        if (blockEntity != null) {
                                            NbtCompound nbtCompound = blockEntity.createNbt();

                                            for (String string : self.blockEntityData.getKeys()) {
                                                nbtCompound.put(string, self.blockEntityData.get(string).copy());
                                            }

                                            try {
                                                blockEntity.readNbt(nbtCompound);
                                            } catch (Exception var15) {
                                                FabricCannoning.LOGGER.error("Failed to load block entity from falling block", (Throwable)var15);
                                            }

                                            blockEntity.markDirty();
                                        }
                                    }
                                } else if (self.dropItem && self.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                    self.discard();
                                    self.onDestroyedOnLanding(block, blockPos);
                                    self.dropItem(block);
                                }
                            } else {
                                self.discard();
                                if (self.dropItem && self.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                    self.onDestroyedOnLanding(block, blockPos);
                                    self.dropItem(block);
                                }
                            }
                        } else {
                            self.discard();
                            self.onDestroyedOnLanding(block, blockPos);
                        }
                    }
                } else if (!self.getWorld().isClient
                    && (self.timeFalling > 100 && (blockPos.getY() <= self.getWorld().getBottomY() || blockPos.getY() > self.getWorld().getTopY()) || self.timeFalling > 600)) {
                    if (self.dropItem && self.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                        self.dropItem(block);
                    }

                    self.discard();
                }
            }

            self.setVelocity(self.getVelocity().multiply(0.9800000190734863D));
        }
    }

    /**
     * Copied from Paper Spigot's fix cannnons. Falling blocks are measured from middle.
     * This affects exposure.
     */
    @Override
    public double squaredDistanceTo(Vec3d vector) {
        double d = this.getX() - vector.x;
        double e = this.getY() + this.getStandingEyeHeight() - vector.y;
        double f = this.getZ() - vector.z;
        return d * d + e * e + f * f;
    }

    /**
     * Copied from Paper Spigot's fix cannnons. Falling blocks are measured from middle.
     */
    @Override
    public float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height / 2;
    }
}
