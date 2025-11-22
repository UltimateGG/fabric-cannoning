package com.evofaction.fc;

public class Config {
    //
    // Essential Cannon Fixes
    //
    /**
     * Controls if TNT moves in water/lava
     */
    public static boolean LIQUIDS_MOVE_TNT = false;

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
     * When false, makes moving piston blocks (Block 36) not block ray traces for collision.
     * This fixes carpet comps. Fence gate comps still work on default 1.20 since
     * they already have no collision (when open). This does not affect player/entity
     * movement collision.
     */
    public static boolean BLOCK36_RESOLVES_COLLISION = false;

    //
    // General/Optional Cannon Settings
    //
    /**
     * @value true = East/West velocity based triangles patch
     * @value false = Vanilla 1.8 behavior, which will ALWAYS do YXZ triangles
     */
    public static boolean EAST_WEST_CANNONING_FIX = false;

    /**
     * In newer versions like 1.21.10, pistons can one-push entities out of cobwebs.
     * This enables that behavior, so sand will be pushed out in one go.
     * In 1.8 it took multiple pulses.
     */
    public static boolean ONE_PUSH_WEBS = false;

    /**
     * If enabled, any blocks above or below a protection block
     * will not be blown up by explosions. For cannon testing only.
     */
    public static boolean PROTECTION_BLOCK_ENABLED = true;

    /**
     * If enabled, prevents liquids from breaking things like redstone,
     * repeaters, comparators, redstone torches, etc.
     */
    public static boolean WATER_PROTECTED_REDSTONE = true;

    //
    // Optimizations
    //
    /**
     * When true, if TNT is in the same exact position, same fuse, and same velocity
     * as another, it is "merged" and only one entity is kept. At fuse 0, the
     * number of merged TNT explosions are created.
     */
    public static boolean MERGE_TNT = true;

    /**
     * Caches entity exposure to explosions on a per-tick level.
     * This is the equivalent of paper spigot's old optimize-explosions flag.
     */
    public static boolean CACHE_EXPLOSION_EXPOSURE = true;

    // Flag for random/testing things for hot reloading mixins
    public static boolean WIP = true;
}
