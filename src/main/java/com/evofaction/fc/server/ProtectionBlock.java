package com.evofaction.fc.server;


import com.evofaction.fc.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;


public class ProtectionBlock {
    private static final Block PROTECTION_BLOCK = Blocks.IRON_BLOCK;

    // per-tick cache and cleared at the end of every tick
    private static final HashMap<CacheKey, Boolean> blockCache = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            blockCache.clear();
        });
    }

    public static boolean isProtectedColumn(World world, int x, int z) {
        if (!Config.PROTECTION_BLOCK_ENABLED) return false;

        CacheKey key = new CacheKey(world, x, z);
        Boolean cachedValue = blockCache.get(key);

        if (cachedValue != null) return cachedValue;

        for (int y = world.getBottomY(); y < world.getHeight(); y++) {
            if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == PROTECTION_BLOCK) {
                blockCache.put(key, true);
                return true;
            }
        }

        // Found none
        blockCache.put(key, false);
        return false;
    }

    private static class CacheKey {
        private final World world;
        private final int x;
        private final int z;

        public CacheKey(World world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof CacheKey cacheKey)) return false;

            // == on world since it should always be the same instance and its just the duration of a tick
            return x == cacheKey.x && z == cacheKey.z && world == cacheKey.world;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + z;
            return result;
        }
    }
}
