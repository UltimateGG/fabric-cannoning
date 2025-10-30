package com.evofaction.fc.mixin;

import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;


@Mixin(TntEntity.class)
public class FixCannonsMixin {
	@Redirect(
	method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/TntEntity;setVelocity(DDD)V"
		)
	)
	private void removeInitialXZRandomness(TntEntity instance, double x, double y, double z) {
		instance.setVelocity(0.0F, 0.2F, 0.0F);
	}

	@Redirect(
		method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/TntEntity;setFuse(I)V"
		)
	)
	private void fixFuseTime(TntEntity instance, int original) {
		// 1.8 actually would explode when tick was -1 because of a post-decrement check
		// so this counters the new "fixed" behavior.
		instance.setFuse(81);
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/TntEntity;updateWaterState()Z"
		)
	)
	private boolean cancelUpdateWaterState(TntEntity instance) {
//		if (FactionsCore.TNT_MOVES_IN_WATER)
//			return instance.updateWaterState();
		return false;
	}
//
//	@Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
//	private void onGetEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
//		cir.setReturnValue(0.0f); // override the return value
//	}
}