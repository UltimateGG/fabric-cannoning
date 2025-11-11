package com.evofaction.fc.server;


import com.evofaction.fc.Config;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class ProtectionBlock {
    private static final Block PROTECTION_BLOCK = Blocks.IRON_BLOCK;

    // per-tick cache and invalidated at the end of every tick
    // Yes technically this doesn't factor world into the key
    private static final Long2BooleanMap columnCache = new Long2BooleanOpenHashMap();
    private static int cachedTick = 0;

    public static boolean isProtectedColumn(World world, int x, int z) {
        if (!Config.PROTECTION_BLOCK_ENABLED || world.isClient) return false;

        @SuppressWarnings("DataFlowIssue") // Always on server
        int currentTick = world.getServer().getTicks();
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);

        if (currentTick != cachedTick) {
            cachedTick = currentTick;
            columnCache.clear();
        } else if (columnCache.containsKey(key)){
            return columnCache.get(key);
        }

        Chunk chunk = world.getChunk(x >> 4, z >> 4);
        BlockPos.Mutable pos = new BlockPos.Mutable(x, 0, z);
        int height = world.getTopY();

        for (int y = world.getBottomY(); y < height; y++) {
            pos.setY(y);

            if (chunk.getBlockState(pos).getBlock() == PROTECTION_BLOCK) {
                columnCache.put(key, true);
                return true;
            }
        }

        // Found none
        columnCache.put(key, false);
        return false;
    }
}
