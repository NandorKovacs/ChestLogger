package net.roaringmind.chestlogger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.roaringmind.chestlogger.callback.OpenDonkeyCallback;

@Mixin(AbstractHorseEntity.class)
public abstract class HorseInventoryMixin {
  @Inject(method = "openInventory", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
  public void openInventoryMixin(PlayerEntity player, CallbackInfo ci) {
    if ((AbstractHorseEntity)(Object)this instanceof AbstractDonkeyEntity && ((AbstractDonkeyEntity)(Object)this).hasChest()) {
      OpenDonkeyCallback.EVENT.invoker().interact(player, (Entity)(Object)this);
    }
  }
}
