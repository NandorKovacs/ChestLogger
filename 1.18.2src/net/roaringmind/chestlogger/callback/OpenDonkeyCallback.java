package net.roaringmind.chestlogger.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public interface OpenDonkeyCallback {
  Event<OpenDonkeyCallback> EVENT = EventFactory.createArrayBacked(OpenDonkeyCallback.class, (listeners) -> (player, entity) -> {
    for (OpenDonkeyCallback listener : listeners) {
      listener.interact(player, entity);
    }
  });

  void interact(PlayerEntity player, Entity entity);
}
