package net.roaringmind.chestlogger.mixin.rideableinventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.roaringmind.chestlogger.callback.RideableInventoryCallback;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseMixin extends Entity {
  public AbstractHorseMixin(EntityType<?> type, World world) {
    super(type, world);
  }

  @Inject(method = "openInventory", at = @At(value = "HEAD"))
  public void openInventoryMixin(PlayerEntity player, CallbackInfo info) {
    RideableInventoryCallback.EVENT.invoker().interact(player, this);  
  }
}
