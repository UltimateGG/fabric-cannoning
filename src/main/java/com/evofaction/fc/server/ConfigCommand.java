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
                    .then(CommandManager.literal("LIQUIDS_MOVE_TNT").executes(ctx -> {
                        Config.LIQUIDS_MOVE_TNT = !Config.LIQUIDS_MOVE_TNT;
                        return sendToggleMsg(ctx, "Liquids move TNT", Config.LIQUIDS_MOVE_TNT, false);
                    }))
                    .then(CommandManager.literal("PISTON_ENTITY_PULLBACK_FIX").executes(ctx -> {
                        Config.PISTON_ENTITY_PULLBACK_FIX = !Config.PISTON_ENTITY_PULLBACK_FIX;
                        return sendToggleMsg(ctx, "Piston entity pullback fix", Config.PISTON_ENTITY_PULLBACK_FIX, false);
                    }))
                    .then(CommandManager.literal("OLD_PISTON_RETRACTION").executes(ctx -> {
                        Config.OLD_PISTON_RETRACTION = !Config.OLD_PISTON_RETRACTION;
                        return sendToggleMsg(ctx, "Old piston retraction", Config.OLD_PISTON_RETRACTION, false);
                    }))
                    .then(CommandManager.literal("OLD_BLOCK36_EXPOSURE").executes(ctx -> {
                        Config.OLD_BLOCK36_EXPOSURE = !Config.OLD_BLOCK36_EXPOSURE;
                        return sendToggleMsg(ctx, "Old block 36 exposure", Config.OLD_BLOCK36_EXPOSURE, false);
                    }))
                    .then(CommandManager.literal("OLD_LADDER_EXPOSURE").executes(ctx -> {
                        Config.OLD_LADDER_EXPOSURE = !Config.OLD_LADDER_EXPOSURE;
                        return sendToggleMsg(ctx, "Old ladder exposure", Config.OLD_LADDER_EXPOSURE, false);
                    }))
                    .then(CommandManager.literal("EAST_WEST_CANNONING_FIX").executes(ctx -> {
                        Config.EAST_WEST_CANNONING_FIX = !Config.EAST_WEST_CANNONING_FIX;
                        return sendToggleMsg(ctx, "E/W Patch", Config.EAST_WEST_CANNONING_FIX, false);
                    }))
                    .then(CommandManager.literal("ONE_PUSH_WEBS").executes(ctx -> {
                        Config.ONE_PUSH_WEBS = !Config.ONE_PUSH_WEBS;
                        return sendToggleMsg(ctx, "One push webs", Config.ONE_PUSH_WEBS, true);
                    }))
                    .then(CommandManager.literal("PROTECTION_BLOCK_ENABLED").executes(ctx -> {
                        Config.PROTECTION_BLOCK_ENABLED = !Config.PROTECTION_BLOCK_ENABLED;
                        return sendToggleMsg(ctx, "Protection blocks", Config.PROTECTION_BLOCK_ENABLED, true);
                    }))
                    .then(CommandManager.literal("WATER_PROTECTED_REDSTONE").executes(ctx -> {
                        Config.WATER_PROTECTED_REDSTONE = !Config.WATER_PROTECTED_REDSTONE;
                        return sendToggleMsg(ctx, "Water protected redstone", Config.WATER_PROTECTED_REDSTONE, false);
                    }))
                    .then(CommandManager.literal("MERGE_TNT").executes(ctx -> {
                        Config.MERGE_TNT = !Config.MERGE_TNT;
                        return sendToggleMsg(ctx, "Merge TNT", Config.MERGE_TNT, false);
                    }))
                    .then(CommandManager.literal("CACHE_EXPLOSION_EXPOSURE").executes(ctx -> {
                        Config.CACHE_EXPLOSION_EXPOSURE = !Config.CACHE_EXPLOSION_EXPOSURE;
                        return sendToggleMsg(ctx, "Exposure cache", Config.CACHE_EXPLOSION_EXPOSURE, false);
                    }))
                    .then(CommandManager.literal("WIP").executes(ctx -> {
                        Config.WIP = !Config.WIP;
                        return sendToggleMsg(ctx, "WIP", Config.WIP, false);
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
