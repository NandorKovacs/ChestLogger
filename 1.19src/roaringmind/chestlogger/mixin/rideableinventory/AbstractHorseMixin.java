package net.roaringmind.chestlogger.mixin.rideableinventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.roaringmind.chestlogger.callback.RideableInventoryCallback;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseMixin {
  @Inject(method = "openInventory", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
  public void openInventoryMixin(PlayerEntity player, CallbackInfo ci) {
    RideableInventoryCallback.EVENT.invoker().interact(player, (Entity)(Object)this);  
  }
}
