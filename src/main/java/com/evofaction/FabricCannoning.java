package com.evofaction;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		TNTFillCommand.register();
		CommandRegistrationCallback.EVENT.register(this::registerCommand);

		LOGGER.info("Fabric cannoning loaded");
	}

	private void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
		dispatcher.register(
				CommandManager.literal("ewpatch")
						.requires(source -> source.hasPermissionLevel(2)) // only ops
						.executes(ctx -> {
							FabricCannoning.EAST_WEST_CANNONING_FIX = !FabricCannoning.EAST_WEST_CANNONING_FIX;
							ctx.getSource().sendMessage(Text.literal("E/W Patch is now: " + FabricCannoning.EAST_WEST_CANNONING_FIX));

							return 0;
						})
		);
	}
}
