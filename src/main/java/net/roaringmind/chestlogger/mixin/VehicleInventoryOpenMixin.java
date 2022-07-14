package net.roaringmind.chestlogger.mixin;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.world.event.GameEvent;
import net.roaringmind.chestlogger.callback.VehicleInventoryOpenCallback;

@Mixin(VehicleInventory.class)
public abstract interface VehicleInventoryOpenMixin extends Inventory, NamedScreenHandlerFactory {

  default public ActionResult open(BiConsumer<GameEvent, Entity> gameEventEmitter, PlayerEntity player) {
    VehicleInventoryOpenCallback.EVENT.invoker().interact(player, (Entity) (Object) this);

    player.openHandledScreen(((VehicleInventory) (Object) this));
    if (!player.world.isClient) {
      gameEventEmitter.accept(GameEvent.CONTAINER_OPEN, player);
      PiglinBrain.onGuardedBlockInteracted(player, true);
      return ActionResult.CONSUME;
    }
    return ActionResult.SUCCESS;
  }

}
