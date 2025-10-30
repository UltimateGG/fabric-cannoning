package com.evofaction.mixin;

import com.evofaction.FabricCannoning;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PistonBlockEntity.class)
public class PistonMixin {
	@Inject(
		method = "pushEntities(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLnet/minecraft/block/entity/PistonBlockEntity;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void onPullEntity(World world, BlockPos pos, float f, PistonBlockEntity blockEntity, CallbackInfo ci) {
		if (!blockEntity.isExtending() && FabricCannoning.DISABLE_PISTON_ENTITY_PULLBACK)
			ci.cancel();
	}
}
