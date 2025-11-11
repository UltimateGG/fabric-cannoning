package com.evofaction.fc;

import com.evofaction.fc.server.*;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Client and server entry
public class FabricCannoning implements ModInitializer {
    public static final String MOD_ID = "fabric-cannoning";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        TNTFillCommand.register();
        FireCommand.register();

        ExposureCache.init();
        MergeTNT.init();
        ConfigCommand.register();

        FabricCannoning.LOGGER.info("Fabric cannoning loaded");
    }
}
