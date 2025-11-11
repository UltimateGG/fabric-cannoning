package com.evofaction.fc;

public class Config {
    //
    // General Cannon Settings
    //

    /**
     * Controls if TNT moves in water/lava
     */
    public static boolean LIQUIDS_MOVE_TNT = false;

    /**
     * @value true = East/West velocity based triangles patch
     * @value false = Vanilla 1.8 behavior, which will ALWAYS do YXZ triangles
     */
    public static boolean EAST_WEST_CANNONING_FIX = false;

    /**
     * If a piston is retracting and an entity is intersecting the collision box of
     * the block (or piston head) that is retracting, don't pull it backwards.
     * <p>
     * This fixes sand comps that push sand out of webs (It won't push and pull sand
     * back and forth), as well as something like pushing TNT against a wall to align it.
     * If this is false something like a piston aligner would push TNT against the wall,
     * then pull it back on the retraction.
     * <p>
     * It only affects things already glitched inside of the block being pulled back.
     */
    public static boolean PISTON_PULLBACK_FIX = true;

    /**
     * If enabled, any blocks above or below a protection block
     * will not be blown up by explosions. For cannon testing only.
     */
    public static boolean PROTECTION_BLOCK_ENABLED = true;

    //
    // Optimizations
    //

    /**
     * Significant Optimization: If TNT is in the same exact position, same fuse, and same velocity
     * as another, it is "merged" and only one entity is kept. At fuse 0, the
     * number of merged TNT explosions are created.
     */
    public static boolean MERGE_TNT = true;

    /**
     * Optimization: Caches entity exposure to explosions on a per-tick level.
     * This is the equivalent of paper spigot's old optimize-explosions flag.
     */
    public static boolean CACHE_EXPLOSION_EXPOSURE = true;
}
