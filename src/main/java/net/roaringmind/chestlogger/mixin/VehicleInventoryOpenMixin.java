package net.roaringmind.chestlogger.mixin;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.world.event.GameEvent;
import net.roaringmind.chestlogger.callback.VehicleInventoryOpenCallback;

@Mixin(VehicleInventory.class)
public abstract class VehicleInventoryOpenMixin {
  @Inject(method = "open", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
  public void openMixin(BiConsumer<GameEvent, Entity> gameEventEmitter, PlayerEntity player, CallbackInfo ci) {
    VehicleInventoryOpenCallback.EVENT.invoker().interact(player, (Entity)(Object)this);
  }
}
