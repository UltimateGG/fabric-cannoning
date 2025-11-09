package com.evofaction.fc;

import com.evofaction.fc.server.*;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
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
        CommandRegistrationCallback.EVENT.register(this::registerCommand);

        ProtectionBlock.init();
        ExposureCache.init();
        MergeTNT.init();

        FabricCannoning.LOGGER.info("Fabric cannoning loaded");
    }

    private void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
        dispatcher.register(
            CommandManager.literal("ewpatch")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    Config.EAST_WEST_CANNONING_FIX = !Config.EAST_WEST_CANNONING_FIX;
                    ctx.getSource().sendMessage(Text.literal("E/W Patch is now: " + Config.EAST_WEST_CANNONING_FIX));

                    return 0;
                })
        );
        dispatcher.register(
            CommandManager.literal("mergedtnt")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    Config.MERGE_TNT = !Config.MERGE_TNT;
                    ctx.getSource().sendMessage(Text.literal("Merge TNT is now: " + Config.MERGE_TNT));

                    return 0;
                })
        );
        dispatcher.register(
            CommandManager.literal("pb")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    Config.PISTON_PULLBACK_FIX = !Config.PISTON_PULLBACK_FIX;
                    ctx.getSource().sendMessage(Text.literal("Piston Patch is now: " + Config.PISTON_PULLBACK_FIX));

                    return 0;
                })
        );
    }
}
