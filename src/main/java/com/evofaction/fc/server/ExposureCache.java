package com.evofaction.fc.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;


public class ExposureCache {
    private static final HashMap<CacheKey, Float> exposureCache = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            exposureCache.clear();
        });
    }

    public static Float getCachedExposure(CacheKey key) {
        return exposureCache.get(key);
    }

    public static void cacheExposure(CacheKey key, float value) {
        exposureCache.put(key, value);
    }

    public static class CacheKey {
        private final World world;
        private final double explosionX;
        private final double explosionY;
        private final double explosionZ;
        private final Box box;

        public CacheKey(World world, Vec3d source, Box box) {
            this.world = world;
            this.explosionX = source.x;
            this.explosionY = source.y;
            this.explosionZ = source.z;
            this.box = box;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof CacheKey cacheKey)) return false;

            return Double.compare(explosionX, cacheKey.explosionX) == 0
                && Double.compare(explosionY, cacheKey.explosionY) == 0
                && Double.compare(explosionZ, cacheKey.explosionZ) == 0
                && world == cacheKey.world
                && box.equals(cacheKey.box);
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + Double.hashCode(explosionX);
            result = 31 * result + Double.hashCode(explosionY);
            result = 31 * result + Double.hashCode(explosionZ);
            result = 31 * result + box.hashCode();
            return result;
        }
    }
}
