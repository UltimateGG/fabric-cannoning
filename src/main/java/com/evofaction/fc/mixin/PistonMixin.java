package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Iterator;
import java.util.List;


@Mixin(PistonBlockEntity.class)
public abstract class PistonMixin extends BlockEntity {
    @Shadow
    private BlockState pushedBlock;
    @Shadow
    private Direction facing;
    @Shadow
    private boolean extending;
    @Shadow
    private boolean source;
    @Final
    @Shadow
    private static ThreadLocal<Direction> entityMovementDirection;
    @Shadow
    private float progress;

    protected PistonMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private static void pushEntities(World world, BlockPos pos, float f, PistonBlockEntity blockEntity) {
        PistonMixin self = (PistonMixin)(Object)blockEntity;
        Direction direction = blockEntity.getMovementDirection();
        double d = f - self.progress;

        VoxelShape voxelShape = self.getHeadBlockState().getCollisionShape(world, pos);
        if (!voxelShape.isEmpty()) {
            Box box = offsetHeadBox(pos, voxelShape.getBoundingBox(), blockEntity);
            List<Entity> list = world.getOtherEntities(null, Boxes.stretch(box, direction, d).union(box));
            if (!list.isEmpty()) {
                List<Box> list2 = voxelShape.getBoundingBoxes();
                boolean bl = self.pushedBlock.isOf(Blocks.SLIME_BLOCK);
                Iterator var12 = list.iterator();

                while (true) {
                    Entity entity;
                    while (true) {
                        if (!var12.hasNext()) {
                            return;
                        }

                        entity = (Entity)var12.next();
                        if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                            if (!bl) {
                                break;
                            }

                            if (!(entity instanceof ServerPlayerEntity)) {
                                Vec3d vec3d = entity.getVelocity();
                                double e = vec3d.x;
                                double g = vec3d.y;
                                double h = vec3d.z;
                                switch (direction.getAxis()) {
                                    case X:
                                        e = direction.getOffsetX();
                                        break;
                                    case Y:
                                        g = direction.getOffsetY();
                                        break;
                                    case Z:
                                        h = direction.getOffsetZ();
                                }

                                entity.setVelocity(e, g, h);
                                break;
                            }
                        }
                    }

                    double i = 0.0;

                    for (Box collidingBlockPart : list2) {
                        Box stretchedPath = Boxes.stretch(offsetHeadBox(pos, collidingBlockPart, blockEntity), direction, d);
                        Box targetEntityBoundingBox = entity.getBoundingBox();
                        // FabricCannoning - Piston entity pullback fix
                        if (Config.PISTON_ENTITY_PULLBACK_FIX && !self.extending) {
                            var origPart = collidingBlockPart.offset(
                                pos.getX() + self.facing.getOffsetX(), pos.getY() + self.facing.getOffsetY(), pos.getZ() + self.facing.getOffsetZ()
                            );
                            if (targetEntityBoundingBox.intersects(origPart)) continue;
                        }
                        if (stretchedPath.intersects(targetEntityBoundingBox)) {
                            i = Math.max(i, getIntersectionSize(stretchedPath, direction, targetEntityBoundingBox));
                            if (i >= d) {
                                break;
                            }
                        }
                    }

                    if (!(i <= 0.0)) {
                        i = Math.min(i, d) + 0.01;

                        // --- BEGIN 1.8 COMPATIBILITY PATCH ---
                        // Add the old extra push (2 pixels ≈ 0.125 blocks)
//                        if (self.extending && !bl /* not slime block behavior */ && i > 0) {
//                            i =(f/2)+0.125;
//                        } else
//                            i += 0.01;
                        // --- END PATCH ---

//                        if (i > 0)
                        moveEntity(direction, entity, i, direction);
                        if (!self.extending && self.source) {
                            push(pos, entity, direction, d);
                        }
                    }
                }
            }
        }
    }

    @Shadow
    private float lastProgress;
    @Shadow
    private long savedWorldTime;
    @Shadow
    private int field_26705;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void tick(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity) {
        PistonMixin self = (PistonMixin)(Object)blockEntity;

        self.savedWorldTime = world.getTime();
        self.lastProgress = self.progress;
        if (self.lastProgress >= 1.0F) {
            if (world.isClient && self.field_26705 < 5) {
                self.field_26705++;
            } else {
                if (self.extending) pushEntities(world, pos, 0.25F, blockEntity);
                world.removeBlockEntity(pos);
                blockEntity.markRemoved();
                if (world.getBlockState(pos).isOf(Blocks.MOVING_PISTON)) {
                    BlockState blockState = Block.postProcessState(self.pushedBlock, world, pos);
                    if (blockState.isAir()) {
                        world.setBlockState(pos, self.pushedBlock, Block.FORCE_STATE | Block.MOVED | Block.NO_REDRAW);
                        Block.replace(self.pushedBlock, blockState, world, pos, 3);
                    } else {
                        if (blockState.contains(Properties.WATERLOGGED) && (Boolean)blockState.get(Properties.WATERLOGGED)) {
                            blockState = blockState.with(Properties.WATERLOGGED, false);
                        }

                        world.setBlockState(pos, blockState, Block.NOTIFY_ALL | Block.MOVED);
                        world.updateNeighbor(pos, blockState.getBlock(), pos);
                    }
                }
            }
        } else {
            float f = self.progress + 0.5F;
            pushEntities(world, pos, f, blockEntity);
            moveEntitiesInHoneyBlock(world, pos, f, blockEntity);
            self.progress = f;
            if (self.progress >= 1.0F) {
                self.progress = 1.0F;
            }
        }
    }

    @Shadow
    private static Box offsetHeadBox(BlockPos pos, Box boundingBox, PistonBlockEntity blockEntity) {
        return  null;
    }

    @Shadow
    private BlockState getHeadBlockState() {
        return null;
    }

    /**
     * @author UltimateGamer079
     * @reason 1.8 pistons pushed farther than one block which would align TNT and
     *         cause a certain number of pushes to push sand out of a cobweb.
     *         This is not the exact same but will be enough to be non-symmetrical.
     */
    @Overwrite
    private static void moveEntity(Direction direction, Entity entity, double distance, Direction movementDirection) {
        distance = distance == -0.74 ? 0.25 : 0.5625;
        entityMovementDirection.set(direction);
        entity.move(
            MovementType.PISTON,
            new Vec3d(distance * movementDirection.getOffsetX(), distance * movementDirection.getOffsetY(), distance * movementDirection.getOffsetZ())
        );
        entityMovementDirection.set(null);
    }
    @Shadow
    private static double getIntersectionSize(Box box, Direction direction, Box box2) {
        return 0;
    }
    @Shadow
    private static void push(BlockPos pos, Entity entity, Direction direction, double amount) {}

    @Shadow
    private static void moveEntitiesInHoneyBlock(World world, BlockPos pos, float f, PistonBlockEntity blockEntity) {}
}

@Mixin(Entity.class)
class TmpPistonMovementMixin {
    // Disable whatever adjustment this was doing to our applied movement
    // TODO: Need to investigate what it was doing.
    @Redirect(
        method = "move",
        at = @At(
            value="INVOKE",
            target = "Lnet/minecraft/entity/Entity;adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
        )
    )
    public Vec3d adjMovement(Entity instance, Vec3d movement) {
        return movement;
    }

    @ModifyConstant(
        method = "calculatePistonMovementFactor(Lnet/minecraft/util/math/Direction$Axis;D)D",
        constant = @Constant(doubleValue = 0.51)
    )
    private double modifyClamp(double original) {
        return 1;
    }

    @ModifyConstant(
        method = "calculatePistonMovementFactor(Lnet/minecraft/util/math/Direction$Axis;D)D",
        constant = @Constant(doubleValue = -0.51)
    )
    private double modifyClamp2(double original) {
        return -1;
    }
}
