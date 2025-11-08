package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;


// Overwrites lithium's entity.collisions.movement. See fabric.mod.json
@Mixin(Entity.class)
public class EastWestCannoningMixin {
    @Redirect(
        method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;"
        ),
        require = 5
    )
    private Vec3d adjustMovementForCollisionsGetEntitiesLater(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions) {
        return lithiumCollideMultiAxisMovement(entity, movement, entityBoundingBox, world, true, collisions);
    }

    /**
     * @author 2No2Name
     * @reason Replace with optimized implementation
     */
    @Overwrite
    public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions) {
        return lithiumCollideMultiAxisMovement(entity, movement, entityBoundingBox, world, false, collisions);
    }

    /**
     * @author 2No2Name, UltimateGamer079
     * @implNote Mojang actually implemented an "e/w patch", in this method.
     *  But it is opposite of what we want in that it does the <b>greater</b> velocity <b>first</b>.
     *  <br/><br/>
     *  The idea of the e/w patch is that it infers the direction you intend to shoot in based
     *  on which axis has a greater velocity, since usually you give more exposure to that direction.
     *  <br/><br/>
     *  For example, if there is more X velocity, you are (probably!) shooting towards X.
     *  So to ensure the "floating" of a cannon works, we calculate the opposite axis you are
     *  shooting in as the first (after Y) leg of the triangle, Z in this case.
     *  This is so that it hits the left/right guider first, instead of going towards X first
     *  and sliding across Z at the wall.
     *  <br/><br/>
     *  "probably" is a key word there. This is not foolproof, but it's how most servers did it.
     *  It WILL break some cannons (e.g. diags) which is why I prefer it off.
     */
    // https://github.com/CaffeineMC/lithium/blob/1.20.1/src/main/java/me/jellysquid/mods/lithium/mixin/entity/collisions/movement/EntityMixin.java
    @Unique
    private static Vec3d lithiumCollideMultiAxisMovement(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, boolean getEntityCollisions, List<VoxelShape> otherCollisions) {
        //vanilla order: entities, worldborder, blocks. It is unknown whether changing this order changes the result regarding the confusing 1e-7 VoxelShape margin behavior. Not yet investigated
        double velX = movement.x;
        double velY = movement.y;
        double velZ = movement.z;
        boolean isVerticalOnly = velX == 0 && velZ == 0;
        Box movementSpace;
        if (isVerticalOnly) {
            if (velY < 0) {
                //Check block directly below center of entity first
                VoxelShape voxelShape = LithiumEntityCollisions.getCollisionShapeBelowEntity(world, entity, entityBoundingBox);
                if (voxelShape != null) {
                    double v = voxelShape.calculateMaxDistance(Direction.Axis.Y, entityBoundingBox, velY);
                    if (v == 0) {
                        return Vec3d.ZERO;
                    }
                }
                //Reduced collision volume optimization for entities that are just standing around
                movementSpace = new Box(entityBoundingBox.minX, entityBoundingBox.minY + velY, entityBoundingBox.minZ, entityBoundingBox.maxX, entityBoundingBox.minY, entityBoundingBox.maxZ);
            } else {
                movementSpace = new Box(entityBoundingBox.minX, entityBoundingBox.maxY, entityBoundingBox.minZ, entityBoundingBox.maxX, entityBoundingBox.maxY + velY, entityBoundingBox.maxZ);
            }
        } else {
            movementSpace = entityBoundingBox.stretch(movement);
        }

        List<VoxelShape> blockCollisions = LithiumEntityCollisions.getBlockCollisions(world, entity, movementSpace);
        List<VoxelShape> entityWorldBorderCollisions = null;

        if (velY != 0.0) {
            velY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, blockCollisions, velY);
            if (velY != 0.0) {
                if (!otherCollisions.isEmpty()) {
                    velY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, otherCollisions, velY);
                }
                if (velY != 0.0 && getEntityCollisions) {
                    entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                    velY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, entityWorldBorderCollisions, velY);
                }
                if (velY != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, velY, 0.0);
                }
            }
        }
        // FabricCannoning Start
        // Note: OneshotMC classic ("legacy") jar would calculate Z first if
        // the velocities were equal. I want to default to vanilla if they are
        // equal though. If we ever want to change that make it >= here.
        // SK spigot appears to do it this way.

        // noinspection SimplifiableConditionalExpression
        boolean calculateZFirst = Config.EAST_WEST_CANNONING_FIX
            ? Math.abs(velX) > Math.abs(velZ) // Flipped condition (relative to Mojangs) so the == edge case lines up (If equal, use YXZ)
            : false; // 1.8 behavior (never change YXZ order)
        // FabricCannoning End
        if (calculateZFirst) {
            velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, blockCollisions, velZ);
            if (velZ != 0.0) {
                if (!otherCollisions.isEmpty()) {
                    velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, otherCollisions, velZ);
                }
                if (velZ != 0.0 && getEntityCollisions) {
                    if (entityWorldBorderCollisions == null) {
                        entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                    }

                    velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, entityWorldBorderCollisions, velZ);
                }
                if (velZ != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, velZ);
                }
            }
        }
        if (velX != 0.0) {
            velX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, blockCollisions, velX);
            if (velX != 0.0) {
                if (!otherCollisions.isEmpty()) {
                    velX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, otherCollisions, velX);
                }
                if (velX != 0.0 && getEntityCollisions) {
                    if (entityWorldBorderCollisions == null) {
                        entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                    }

                    velX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, entityWorldBorderCollisions, velX);
                }
                if (velX != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(velX, 0.0, 0.0);
                }
            }
        }
        if (!calculateZFirst && velZ != 0.0) {
            velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, blockCollisions, velZ);
            if (velZ != 0.0) {
                if (!otherCollisions.isEmpty()) {
                    velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, otherCollisions, velZ);
                }
                if (velZ != 0.0 && getEntityCollisions) {
                    if (entityWorldBorderCollisions == null) {
                        entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                    }

                    velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, entityWorldBorderCollisions, velZ);
                }
            }
        }
        return new Vec3d(velX, velY, velZ);
    }
}
