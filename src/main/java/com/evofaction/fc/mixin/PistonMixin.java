package com.evofaction.fc.mixin;

import com.evofaction.fc.FabricCannoning;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;


@Mixin(PistonBlockEntity.class)
public abstract class PistonMixin extends BlockEntity {
    private static final int field_31382 = 2;
    private static final double field_31383 = 0.01;
    @Shadow
    private BlockState pushedBlock;
    @Shadow
    private Direction facing;
    @Shadow
    private boolean extending;
    @Shadow
    private boolean source;
    private static final ThreadLocal<Direction> entityMovementDirection = ThreadLocal.withInitial(() -> null);
    @Shadow
    private float progress;

    private float lastProgress;
    private long savedWorldTime;
    private int field_26705;

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
                        if (FabricCannoning.PISTON_PULLBACK_FIX && !self.extending) {
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
    private static Box offsetHeadBox(BlockPos pos, Box boundingBox, PistonBlockEntity blockEntity) {
        return  null;
    }
    @Shadow
    private BlockState getHeadBlockState() {
        return null;
    }
    @Shadow
    private static void moveEntity(Direction direction, Entity entity, double distance, Direction movementDirection) {}
    @Shadow
    private static double getIntersectionSize(Box box, Direction direction, Box box2) {
        return 0;
    }
    @Shadow
    private static void push(BlockPos pos, Entity entity, Direction direction, double amount) {
    }


//    @ModifyVariable(
//        method= "pushEntities(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLnet/minecraft/block/entity/PistonBlockEntity;)V",
//        at = @At("STORE"),
//        ordinal = 0
//    )
//    private static double pistonTestFix(double distance) {
//        return distance + 0.0625F;
//    }
}

@Mixin(Entity.class)
class PistonMovementMixin {
//    @ModifyConstant(
//        method = "calculatePistonMovementFactor(Lnet/minecraft/util/math/Direction$Axis;D)D",
//        constant = @Constant(doubleValue = 0.51)
//    )
//    private double modifyClamp(double original) {
//        return 1;
//    }
//
//    @ModifyConstant(
//        method = "calculatePistonMovementFactor(Lnet/minecraft/util/math/Direction$Axis;D)D",
//        constant = @Constant(doubleValue = -0.51)
//    )
//    private double modifyClamp2(double original) {
//        return -1;
//    }
}