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
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.roaringmind.chestlogger.callback.OpenDonkeyCallback;

public class ChestLogger implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "chestlogger";
  public static final String MOD_NAME = "ChestLogger";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");
    registerEvents();
    //registerCommands();
  }

  private void registerEvents() {
    ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
      String filePath = server.getSavePath(WorldSavePath.ROOT).resolve("chestlogger.txt").toString();
      File file = new File(filePath);
      ;

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

      String dimStr = getDimString(world);
      String timeStamp = getTimeStamp();
      String blockstr = getBlockString(block);
      BlockPos pos = hitResult.getBlockPos();

      chestLog(timeStamp, player.getName().asString(), dimStr, pos.getX(), pos.getY(), pos.getZ(), blockstr,
          world.getServer().getSavePath(WorldSavePath.ROOT).resolve("chestlogger.txt").toString());
      return ActionResult.PASS;
    });
    UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
      if (world.isClient || !shouldBeLogged(entity)) {
        return ActionResult.PASS;
      }

      String dimStr = getDimString(world);
      String timeStamp = getTimeStamp();
      String entityStr = getEntityString(entity);
      BlockPos pos = entity.getBlockPos();

      chestLog(timeStamp, player.getName().asString(), dimStr, pos.getX(), pos.getY(), pos.getZ(), entityStr,
          world.getServer().getSavePath(WorldSavePath.ROOT).resolve("chestlogger.txt").toString());
      return ActionResult.PASS;
    });
    OpenDonkeyCallback.EVENT.register((player, entity) -> {
      if (player.world.isClient || player == null) {
        log(Level.INFO, "i was here");
        return;
      }

      String dimStr = getDimString(player.world);
      String timeStamp = getTimeStamp();
      String entityStr = getEntityString(entity);
      BlockPos pos = entity.getBlockPos();

      chestLog(timeStamp, player.getName().asString(), dimStr, pos.getX(), pos.getY(), pos.getZ(), entityStr,
          player.world.getServer().getSavePath(WorldSavePath.ROOT).resolve("chestlogger.txt").toString());
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

  private String getEntityString(Entity entity) {
    if (entity instanceof AbstractDonkeyEntity) {
      if (entity instanceof DonkeyEntity) {
        return "donkey";
      }
      if (entity instanceof LlamaEntity) {
        return "llama";
      }
      if (entity instanceof MuleEntity) {
        return "mule";
      }
      return "error_occured: entity unidentified";
    }

    if (entity instanceof ChestMinecartEntity) {
      return "chest_minecart";
    }
    if (entity instanceof HopperMinecartEntity) {
      return "hopper_minecart";
    }
    return "error_occured: entity unidentified";
  }

  private String getDimString(World world) {
    MutableRegistry<DimensionType> dimReg = world.getRegistryManager().getMutable(Registry.DIMENSION_TYPE_KEY);
    int dimId = dimReg.getRawId(world.getDimension());

    if (dimId == dimReg.getRawId(dimReg.get(DimensionType.OVERWORLD_ID))) {
      return "overworld";
    }

    if (dimId == dimReg.getRawId(dimReg.get(DimensionType.THE_NETHER_ID))) {
      return "nether";
    }

    if (dimId == dimReg.getRawId(dimReg.get(DimensionType.THE_END_ID))) {
      return "end";
    }

    return "unsupportedDimension";
  }

  private String getTimeStamp() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy  HH:mm");
    return String.format("[%s]", dtf.format(LocalDateTime.now()));
  }

  private void chestLog(String timeStamp, String playerName, String dimStr, int x, int y, int z, String id,
      String path) {
    try {
      String logStr = timeStamp + " " + playerName + " " + dimStr + " x: " + x + " y: " + y + " z: " + z + " " + id
          + "\n";
      log(Level.INFO, logStr);

      FileWriter fileWriter = new FileWriter(new File(path), true);
      fileWriter.write(logStr);
      fileWriter.close();
    } catch (IOException e) {
      log(Level.FATAL, "error writing log");
      e.printStackTrace();
    }
  }

  private static final Set<Block> chestBlocks = new HashSet<>(
      Arrays.asList(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.HOPPER, Blocks.ENDER_CHEST));

  private boolean shouldBeLogged(Block block) {
    if (!chestBlocks.contains(block)) {
      return block instanceof ShulkerBoxBlock;
    }

    return true;
  }

  private boolean shouldBeLogged(Entity entity) {
    if (entity instanceof StorageMinecartEntity) {
      return true;
    }
    return false;
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
