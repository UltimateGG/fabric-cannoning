package com.evofaction.fc;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Client and server entry
public class FabricCannoning implements ModInitializer {
    public static final String MOD_ID = "fabric-cannoning";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean LIQUIDS_MOVE_TNT = false;
    /**
     * @value true = East/West velocity based triangles patch
     * @value false = Vanilla 1.8 behavior, which will ALWAYS do YXZ triangles
     */
    public static boolean EAST_WEST_CANNONING_FIX = false;
    /**
     * If a piston is retracting and an entity is intersecting the collision box of
     * the block (or piston head) that is retracting, don't pull it backwards.
     *
     * This fixes sand comps that push sand out of webs (It won't push and pull sand
     * back and forth), as well as something like pushing TNT against a wall to align it.
     * If this is false something like a piston aligner would push TNT against the wall,
     * then pull it back on the retraction.
     *
     * It only affects things already glitched inside of the block being pulled back.
     */
    public static boolean PISTON_PULLBACK_FIX = true;

    @Override
    public void onInitialize() {

    }
}
