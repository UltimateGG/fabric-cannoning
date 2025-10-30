package com.evofaction.fc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TNTFillCommand {

	public static void register() {
		// Register command on server startup
		CommandRegistrationCallback.EVENT.register(TNTFillCommand::registerCommand);
	}

	private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
		dispatcher.register(
				CommandManager.literal("tntfill")
						.requires(source -> source.hasPermissionLevel(2)) // only ops
						.executes(ctx -> fillDispensers(ctx.getSource(), 64)) // default radius
						.then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 150))
								.executes(ctx -> {
									int radius = IntegerArgumentType.getInteger(ctx, "radius");
									return fillDispensers(ctx.getSource(), radius);
								})
						)
		);
	}

	private static int fillDispensers(ServerCommandSource source, int radius) {
		ServerPlayerEntity player;
		try {
			player = source.getPlayer();
		} catch (Exception e) {
			source.sendFeedback(() -> Text.literal("This command can only be used by a player."), false);
			return 0;
		}

		assert player != null;
		World world = player.getWorld();
		Vec3d pos = player.getPos();
		int filled = 0;

		source.sendFeedback(() -> Text.literal("Filling dispensers within " + radius + " blocks..."), false);

		final ItemStack tnt = new ItemStack(Items.TNT, 64);

		// Iterate through nearby blocks
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					mutable.set(pos.x + x, pos.y + y, pos.z + z);
					Block block = world.getBlockState(mutable).getBlock();

					if (block == Blocks.DISPENSER) {
						BlockEntity be = world.getBlockEntity(mutable);
						if (be instanceof DispenserBlockEntity dispenser) {
							dispenser.clear();
							for (int i = 0; i < dispenser.size(); i++) {
								dispenser.setStack(i, tnt.copy());
							}
							filled++;
						}
					}
				}
			}
		}

		int finalFilled = filled;
		source.sendFeedback(() ->
				Text.literal("§aSuccessfully filled §7" + finalFilled + " §adispensers within §7" + radius + " §ablocks!"), false);
		return filled;
	}
}
