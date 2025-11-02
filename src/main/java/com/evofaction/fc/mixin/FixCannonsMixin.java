package com.evofaction.fc.mixin;

import net.minecraft.entity.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(TntEntity.class)
public abstract class FixCannonsMixin extends Entity {
    protected FixCannonsMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // Removes the X/Y randomness when TNT is spawned (lit, dispensed)
    @Redirect(
        method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/TntEntity;setVelocity(DDD)V"
        )
    )
    private void removeInitialXZRandomness(TntEntity instance, double x, double y, double z) {
        instance.setVelocity(0.0F, 0.20000000298023224D, 0.0F);
    }

    /**
     * @author UltimateGamer079
     * @reason Explosion is spawned in middle of TNT in 1.8, not at the entity's exact position.
     */
    @Overwrite
    private void explode() {
        TntEntity self = (TntEntity) (Object)this;
        float f = 4.0F;

        self.getWorld().createExplosion(self, self.getX(), self.getY() + (double) (self.getHeight() / 2.0F), self.getZ(), f, World.ExplosionSourceType.TNT);
    }

    /**
     * @author UltimateGamer079
     * @reason Need full control to make TNT consistent with 1.8.
     *         The main thing is these large double numbers for precision.
     *         They are tiny, but add up and mess up precise cannons.
     */
    @Overwrite
    public void tick() {
        TntEntity self = (TntEntity) (Object)this;
        if (!self.hasNoGravity()) {
            self.setVelocity(self.getVelocity().add(0.0, -0.03999999910593033D, 0.0));
        }

        self.move(MovementType.SELF, self.getVelocity());
        self.setVelocity(self.getVelocity().multiply(0.9800000190734863D));
        if (self.isOnGround()) {
            self.setVelocity(self.getVelocity().multiply(0.699999988079071D, -0.5, 0.699999988079071D));
        }

        int i = self.getFuse() - 1;
        self.setFuse(i);
        if (i <= -1) { // Tnt in 1.8 actually exploded on -1 (source code was 0)
            self.discard();
            if (!self.getWorld().isClient) {
                this.explode();
            }
        } else {
            // Disable TNT being pushed by water
            // self.updateWaterState();
            if (self.getWorld().isClient) {
                self.getWorld().addParticle(ParticleTypes.SMOKE, self.getX(), self.getY() + 0.5, self.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    /**
     * Copied from Paper Spigot's fix cannnons. TNT is measured from middle.
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
     * @author UltimateGamer079
     * @reason Copied from Paper Spigot's fix cannnons. TNT is measured from middle.
     */
    @Overwrite
    public float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height / 2;
    }
}