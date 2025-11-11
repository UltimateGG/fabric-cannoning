package com.evofaction.fc.server;

import com.evofaction.fc.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public class ConfigCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
            dispatcher.register(
                CommandManager.literal("cc")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.literal("ew").executes(ctx -> {
                        Config.EAST_WEST_CANNONING_FIX = !Config.EAST_WEST_CANNONING_FIX;
                        return sendToggleMsg(ctx, "E/W Patch", Config.EAST_WEST_CANNONING_FIX, false);
                    }))
                    .then(CommandManager.literal("mergetnt").executes(ctx -> {
                        Config.MERGE_TNT = !Config.MERGE_TNT;
                        return sendToggleMsg(ctx, "Merge TNT", Config.MERGE_TNT, false);
                    }))
                    .then(CommandManager.literal("exposurecache").executes(ctx -> {
                        Config.CACHE_EXPLOSION_EXPOSURE = !Config.CACHE_EXPLOSION_EXPOSURE;
                        return sendToggleMsg(ctx, "Exposure cache", Config.CACHE_EXPLOSION_EXPOSURE, false);
                    }))
                    .then(CommandManager.literal("pistonfix").executes(ctx -> {
                        Config.PISTON_PULLBACK_FIX = !Config.PISTON_PULLBACK_FIX;
                        return sendToggleMsg(ctx, "Piston fix", Config.PISTON_PULLBACK_FIX, false);
                    }))
                    .then(CommandManager.literal("protectionblocks").executes(ctx -> {
                        Config.PROTECTION_BLOCK_ENABLED = !Config.PROTECTION_BLOCK_ENABLED;
                        return sendToggleMsg(ctx, "Protection blocks", Config.PROTECTION_BLOCK_ENABLED, true);
                    }))
            );
        });
    }

    private static int sendToggleMsg(CommandContext<ServerCommandSource> ctx, String name, boolean newValue, boolean isAre) {
        ctx.getSource().sendMessage(
            Text.literal("[" + (newValue ? "+" : "-") + "] ")
                .formatted(newValue ? Formatting.GREEN : Formatting.RED)
                .append(
                    Text.literal(name + (isAre ? " are" : " is") + " now: ")
                    .formatted(Formatting.WHITE)
                )
                .append(
                    Text.literal(newValue + "")
                    .formatted(newValue ? Formatting.GREEN : Formatting.RED)
                )
        );

        return Command.SINGLE_SUCCESS;
    }
}
