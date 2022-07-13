package net.roaringmind.chestlogger.mixin.rideableinventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.roaringmind.chestlogger.callback.RideableInventoryCallback;

@Mixin(ChestBoatEntity.class)
public abstract class ChestBoatMixin {
  @Inject(method = "openInventory", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
  public void openInventoryMixin(PlayerEntity player, CallbackInfo ci) {
    RideableInventoryCallback.EVENT.invoker().interact(player, (Entity)(Object)this);  
  }
}
