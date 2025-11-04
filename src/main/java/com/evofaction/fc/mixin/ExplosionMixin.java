package com.evofaction.fc.mixin;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Mixin(Explosion.class)
public class ExplosionMixin {
    @Final @Shadow
    private World world;

    @Final @Shadow
    private double x;

    @Final @Shadow
    private double y;

    @Final @Shadow
    private double z;

    @Final @Shadow @Nullable
    private Entity entity;

    @Final @Shadow
    private float power;

    @Final @Shadow
    private ExplosionBehavior behavior;

    @Final @Shadow
    private ObjectArrayList<BlockPos> affectedBlocks;

    @Shadow @Final
    private Map<PlayerEntity, Vec3d> affectedPlayers;

    // Does not actually return 0. Shadowed for compilation.
    @Shadow
    public static float getExposure(Vec3d source, Entity entity) {
        return 0;
    }

    @Shadow @Final private DamageSource damageSource;

    /**
     * Old MC casted to a float and back to a double.
     * This causes tiny decimal differences compared to 1.20.
     */
    @Unique
    private static double legacySqrt(double v) {
        return (double) (float) Math.sqrt(v);
    }

    /**
     * @author UltimateGamer079
     * @reason Lots to be done to this method. Future optimizations.
     *         For fixing cannons, need to remote the redundant
     */
    @Overwrite
    public void collectBlocksAndDamageEntities() {
        Explosion self = (Explosion) (Object) this;
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.<BlockPos>newHashSet();
        int i = 16;

        // Collecting blocks to affect for explosions, and potentially spawning fire
        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                for (int l = 0; l < 16; l++) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d = j / 15.0F * 2.0F - 1.0F;
                        double e = k / 15.0F * 2.0F - 1.0F;
                        double f = l / 15.0F * 2.0F - 1.0F;
                        double g = Math.sqrt(d * d + e * e + f * f);
                        d /= g;
                        e /= g;
                        f /= g;
                        float h = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
                        double m = this.x;
                        double n = this.y;
                        double o = this.z;

                        for (float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                            BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                            BlockState blockState = this.world.getBlockState(blockPos);
                            FluidState fluidState = this.world.getFluidState(blockPos);
                            if (!this.world.isInBuildLimit(blockPos)) {
                                break;
                            }

                            Optional<Float> optional = this.behavior.getBlastResistance(self, this.world, blockPos, blockState, fluidState);
                            if (optional.isPresent()) {
                                h -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if (h > 0.0F && this.behavior.canDestroyBlock(self, this.world, blockPos, blockState, h)) {
                                set.add(blockPos);
                            }

                            m += d * 0.3F;
                            n += e * 0.3F;
                            o += f * 0.3F;
                        }
                    }
                }
            }
        }

        this.affectedBlocks.addAll(set);
        float q = this.power * 2.0F;
        int k = MathHelper.floor(this.x - q - 1.0);
        int lx = MathHelper.floor(this.x + q + 1.0);
        int r = MathHelper.floor(this.y - q - 1.0);
        int s = MathHelper.floor(this.y + q + 1.0);
        int t = MathHelper.floor(this.z - q - 1.0);
        int u = MathHelper.floor(this.z + q + 1.0);
        List<Entity> list = this.world.getOtherEntities(this.entity, new Box(k, r, t, lx, s, u));
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (int v = 0; v < list.size(); v++) {
            Entity entity = (Entity)list.get(v);
            if (!entity.isImmuneToExplosion()) {
                double w = legacySqrt(entity.squaredDistanceTo(vec3d)) / q;
                if (w <= 1.0) {
                    double x = entity.getX() - this.x;
                    // Removed mojang instanceof tnt entity check to align with 1.8
                    double y = entity.getEyeY() - this.y;
                    double z = entity.getZ() - this.z;
                    double aa = legacySqrt(x * x + y * y + z * z);
                    if (aa != 0.0) {
                        x /= aa;
                        y /= aa;
                        z /= aa;
                        double ab = getExposure(vec3d, entity);
                        double ac = (1.0 - w) * ab;
                        entity.damage(this.damageSource, (int)((ac * ac + ac) / 2.0 * 7.0 * q + 1.0));
                        double ad;
                        if (entity instanceof LivingEntity livingEntity) {
                            ad = ProtectionEnchantment.transformExplosionKnockback(livingEntity, ac);
                        } else {
                            ad = ac;
                        }

                        x *= ad;
                        y *= ad;
                        z *= ad;
                        Vec3d vec3d2 = new Vec3d(x, y, z);
                        entity.setVelocity(entity.getVelocity().add(vec3d2));
                        if (entity instanceof PlayerEntity playerEntity && !playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                            this.affectedPlayers.put(playerEntity, vec3d2);
                        }
                    }
                }
            }
        }
    }
}
