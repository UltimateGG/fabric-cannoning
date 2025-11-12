package com.evofaction.fc.mixin;

import com.evofaction.fc.Config;
import com.evofaction.fc.server.ExposureCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


/**
 * This is the equivalent of paper spigot's old optimize-explosions flag.
 */
@Mixin(Explosion.class)
public class ExposureCacheMixin {
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
}
