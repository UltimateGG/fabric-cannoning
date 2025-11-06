package com.evofaction.fc.server;

import com.evofaction.fc.FabricCannoning;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;


public class ServerStartup implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        TNTFillCommand.register();
        FireCommand.register();
        CommandRegistrationCallback.EVENT.register(this::registerCommand);

        FabricCannoning.LOGGER.info("Fabric cannoning loaded");
    }

    private void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
        dispatcher.register(
            CommandManager.literal("ewpatch")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    FabricCannoning.EAST_WEST_CANNONING_FIX = !FabricCannoning.EAST_WEST_CANNONING_FIX;
                    ctx.getSource().sendMessage(Text.literal("E/W Patch is now: " + FabricCannoning.EAST_WEST_CANNONING_FIX));

                    return 0;
                })
        );
        dispatcher.register(
            CommandManager.literal("pb")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    FabricCannoning.PISTON_PULLBACK_FIX = !FabricCannoning.PISTON_PULLBACK_FIX;
                    ctx.getSource().sendMessage(Text.literal("Piston Patch is now: " + FabricCannoning.PISTON_PULLBACK_FIX));

                    return 0;
                })
        );
    }
}
