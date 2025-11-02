package com.evofaction.fc.mixin;

import com.evofaction.fc.FabricCannoning;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;


@Mixin(Entity.class)
public class EastWestCannoningMixin {
    /**
     * @author UltimateGamer079
     * @reason Mojang actually implemented an "e/w patch", in this method.
     *         But it is opposite of what we want in that it does the <b>greater</b> velocity <b>first</b>.
     *         <br/><br/>
     *         The idea of the e/w patch is that it infers the direction you intend to shoot in based
     *         on which axis has a greater velocity, since usually you give more exposure to that direction.
     *         <br/><br/>
     *         For example, if there is more X velocity, you are (probably!) shooting towards X.
     *         So to ensure the "floating" of a cannon works, we calculate the opposite axis you are
     *         shooting in as the first (after Y) leg of the triangle, Z in this case.
     *         This is so that it hits the left/right guider first, instead of going towards X first
     *         and sliding across Z at the wall.
     *         <br/><br/>
     *         "probably" is a key word there. This is not foolproof, but it's how most servers did it.
     *         It WILL break some cannons (e.g. diags) which is why I prefer it off.
     */
    @Overwrite
    private static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, List<VoxelShape> collisions) {
        if (collisions.isEmpty()) return movement;

        double x = movement.x;
        double y = movement.y;
        double z = movement.z;

        if (y != 0.0) {
            y = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, collisions, y);
            if (y != 0.0) {
                entityBoundingBox = entityBoundingBox.offset(0.0, y, 0.0);
            }
        }

        // Note: OneshotMC classic ("legacy") jar would calculate Z first if
        // the velocities were equal. I want to default to vanilla if they are
        // equal though. If we ever want to change that make it >= here.
        // SK spigot appears to do it this way.

        // noinspection SimplifiableConditionalExpression
        boolean calculateZFirst = FabricCannoning.EAST_WEST_CANNONING_FIX
            ? Math.abs(x) > Math.abs(z) // Flipped condition (relative to Mojangs) so the == edge case lines up (If equal, use YXZ)
            : false; // 1.8 behavior (never change YXZ order)

        if (calculateZFirst && z != 0.0) {
            z = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions, z);
            if (z != 0.0) {
                entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, z);
            }
        }

        if (x != 0.0) {
            x = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, collisions, x);
            if (!calculateZFirst && x != 0.0) {
                entityBoundingBox = entityBoundingBox.offset(x, 0.0, 0.0);
            }
        }

        if (!calculateZFirst && z != 0.0) {
            z = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions, z);
        }

        return new Vec3d(x, y, z);
    }
}
