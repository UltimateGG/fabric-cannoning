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
    public static boolean DISABLE_PISTON_ENTITY_PULLBACK = true;

    @Override
    public void onInitialize() {

    }
}
