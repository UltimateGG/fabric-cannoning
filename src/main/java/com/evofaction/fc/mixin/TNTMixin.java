package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import com.evofaction.fc.TNTInterface;
import net.minecraft.entity.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(TntEntity.class)
public abstract class TNTMixin extends Entity implements TNTInterface {
    @Shadow @Nullable private LivingEntity causingEntity;

    @Shadow public abstract int getFuse();

    @Shadow public abstract void setFuse(int i);

    @Unique
    public int mergedTNT = 1;

    @Unique
    public boolean isClone = false;

    protected TNTMixin(EntityType<?> type, World world) {
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
     * @reason Explosion is spawned in middle of TNT in Paper 1.8, not at the entity's exact position.
     *         In the vanilla game its actually height/16 (1/16th above entity feet).
     */
    @Overwrite
    private void explode() {
        float f = 4.0F;

        this.getWorld().createExplosion(this, this.getX(), this.getY() + (double) (this.getHeight() / 2.0F), this.getZ(), f, World.ExplosionSourceType.TNT);
    }

    /**
     * Optimization: Merged TNT
     * Credits to AtlasSpigot: https://github.com/Atlaspvp/AtlasSpigot I couldn't have figured this out without
     * a reference.
     * <p>
     * Due to the way the tick loop works, when TNT ticks, its first moved one last time, according to its current
     * velocity. Then it explodes, moving other TNT entities that were even in the same exact spot, because the
     * explosion is spawned as 1 tick of extra velocity applied ahead of the others. (They haven't run their .move yet)
     * <p>
     * So TNT with non-zero velocity exploding, even if all spawned in the same gametick, same velocity, same position, etc.
     * will actually move each other around as they blow up. (Yes, really). (I think this is called swing??)
     * <p>
     * All that to say, we can't just call this.explode N times in the exact same spot for merged TNT. What this method
     * does is if it's time to explode, and we have more than one stacked TNT, we spawn a clone. The clone isn't actually
     * in the world, or affected by explosions. *this* TNT (The one its cloned from) is the actor. Each loop, we set
     * the clone's position and velocity to the actor's, and call tick (Which will move itself 1 tick and explode). That
     * explosion will affect the 1 actor TNT's velocity. So next loop around if we have another merged TNT, we repeat.
     * Basically we bully and push this real TNT around a bunch using it as a calculation for where to spawn the *next*
     * cloned TNT. When we are done, it finally explodes for real. This is a major optimization because instead of
     * every explosion pushing 200 other TNTs the same exact way, we just use one actor TNT.
     * I think this preserves OOE, but I need to look into advanced cases.
     */
    @Unique
    private void unmerge() {
        if (this.mergedTNT > 1 && this.getFuse() - 1 <= -1) {
            TntEntity clone = new TntEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), this.causingEntity);
            ((TNTInterface) clone)._$markClone();
            int fuse = this.getFuse();

            for (int i = 0; i < this.mergedTNT - 1; i++) {
                clone.setFuse(fuse);

                // Must be called in the loop to receive the updated velocities
                clone.setVelocity(this.getVelocity());
                clone.setPosition(this.getPos());

                // Will move itself according to the two variables set above,
                // and explode. The explosion will affect `this` which we will
                // then use next loop iteration as the clone's new position/velocity.
                clone.tick();
            }

            clone.discard();
        }
    }

    /**
     * @author UltimateGamer079
     * @reason Need full control to make TNT consistent with 1.8.
     *         The main thing is these large double numbers for precision.
     *         They are tiny, but add up and mess up precise cannons.
     */
    @Overwrite
    public void tick() {
        if (Config.MERGE_TNT) unmerge();
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.03999999910593033D, 0.0));
        }

        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.9800000190734863D));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.699999988079071D, -0.5, 0.699999988079071D));
        }

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= -1) { // Tnt in 1.8 actually exploded on -1 (source code was 0)
            if (!this.isClone) this.discard();
            if (!this.getWorld().isClient) {
                this.explode();
            }
        } else {
            if (Config.LIQUIDS_MOVE_TNT) this.updateWaterState(); // Disable TNT being pushed by water
            if (this.getWorld().isClient) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
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

    /**
     * Optimization: TNT can never sneak. The name descending is misleading,
     * it only calls isSneaking. It only affects players.
     * This method causes a lot of unnecessary checks. It waits on a ReentrantLock.
     */
    @Override
    public boolean isDescending() {
        return false;
    }

    @Override
    public int _$getMergedTNT() {
        return mergedTNT;
    }

    @Override
    public void _$markClone() {
        this.isClone = true;
    }

    @Override
    public void _$addMergedTNT(TNTInterface intf) {
        this.mergedTNT += intf._$getMergedTNT();
    }

    @Override
    public boolean _$canMergeWith(TntEntity t) {
        return (
            this.getFuse() == t.getFuse() &&
            this.getPos().equals(t.getPos()) &&
            this.getVelocity().equals(t.getVelocity()) &&
            this.getBoundingBox().equals(t.getBoundingBox())
        );
    }
}
