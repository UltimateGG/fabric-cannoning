package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import com.evofaction.fc.server.ExposureCache;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Explosion.class)
public class ExplosionMixin {
    @Shadow @Final
    private ObjectArrayList<BlockPos> affectedBlocks;

    // Does not actually return 0, just here for compilation.
    @Shadow
    public static float getExposure(Vec3d source, Entity entity) {
        return 0;
    }

    @Redirect(
        method = "collectBlocksAndDamageEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/explosion/Explosion;getExposure(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)F"
        )
    )
    private float cachedGetExposure(Vec3d source, Entity entity) {
        if (!Config.CACHE_EXPLOSION_EXPOSURE) return getExposure(source, entity);

        ExposureCache.CacheKey key = new ExposureCache.CacheKey(entity.getWorld(), source, entity.getBoundingBox());
        Float cachedValue = ExposureCache.getCachedExposure(key);
        if (cachedValue != null) return cachedValue;

        float value = getExposure(source, entity);
        ExposureCache.cacheExposure(key, value);
        return value;
    }

    /**
     * Old MC MathHelper.sqrt casted to a float and back to a double.
     * This causes tiny decimal differences compared to 1.20.
     */
    @Redirect(
        method = "collectBlocksAndDamageEntities",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;sqrt(D)D",
            remap = false
        )
    )
    private double legacySqrt(double dist) {
        return (float) Math.sqrt(dist);
    }

    // Effectivley removes mojang instanceof tnt entity check to align with 1.8
    @Redirect(
        method = "collectBlocksAndDamageEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getY()D"
        )
    )
    private double fixEyeHeight(Entity instance) {
        return instance.getEyeY();
    }

    @Inject(
        method = "collectBlocksAndDamageEntities",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;addAll(Ljava/util/Collection;)Z",
            shift = At.Shift.AFTER,
            remap = false
        )
    )
    private void cannonProtection(CallbackInfo ci) {
        if (!Config.PROTECTION_BLOCK_ENABLED) return;

        // TODO
    }
}
