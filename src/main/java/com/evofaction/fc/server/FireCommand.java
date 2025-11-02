package com.evofaction.fc.server;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.ButtonBlock;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.UUID;


public class FireCommand {
    private static final HashMap<UUID, BlockPointer> LOCATIONS = new HashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register(FireCommand::registerCommand);

        UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> {
            if (world.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof ButtonBlock) {
                LOCATIONS.put(player.getUuid(), new BlockPointerImpl((ServerWorld) world, blockHitResult.getBlockPos()));
            }

            return ActionResult.PASS;
        });
    }

    private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
        dispatcher.register(
            CommandManager.literal("fire")
                .requires(source -> source.getPlayer() != null && source.hasPermissionLevel(4))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

                    if (!LOCATIONS.containsKey(player.getUuid())) {
                        player.sendMessage(Text.literal("You have not pressed a button yet!").formatted(Formatting.RED));
                        return 0;
                    }

                    var lastPressed = LOCATIONS.get(player.getUuid());
                    var blockState = lastPressed.getBlockState();
                    var block = blockState.getBlock();

                    if (!(block instanceof ButtonBlock)) {
                        player.sendMessage(Text.literal("Block is no longer a button!").formatted(Formatting.RED));
                        return 0;
                    }

                    if (blockState.get(ButtonBlock.POWERED)) {
                        player.sendMessage(Text.literal("Already firing!").formatted(Formatting.RED));
                        return 0;
                    }

                    ((ButtonBlock) block).onUse(
                        blockState,
                        lastPressed.getWorld(),
                        lastPressed.getPos(),
                        player,
                        // These two aren't used in button, doesn't matter what we give it
                        Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ZERO, Direction.NORTH, lastPressed.getPos(), false)
                    );

                    player.sendMessage(Text.literal("Firing!").formatted(Formatting.BLUE));
                    return 1;
                })
        );
    }
}
