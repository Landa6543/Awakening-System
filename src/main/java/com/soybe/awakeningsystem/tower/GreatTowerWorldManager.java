package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.DimensionGenSavedData;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.block.AscensionPortalBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TeyvatCrystalBlock;
import com.soybe.awakeningsystem.block.MountainStoneRubbleBlock;
import com.soybe.awakeningsystem.block.AscensionPortalBlock.PortalType;
import com.soybe.awakeningsystem.entity.AbyssalBeholderEntity;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
import com.soybe.awakeningsystem.entity.AntSoldierEntity;
import com.soybe.awakeningsystem.entity.AstralDragonEntity;
import com.soybe.awakeningsystem.entity.BathysmalVishapEntity;
import com.soybe.awakeningsystem.entity.BeastmanWarriorEntity;
import com.soybe.awakeningsystem.entity.BloodSectDiscipleEntity;
import com.soybe.awakeningsystem.entity.ColossalSculkWardenEntity;
import com.soybe.awakeningsystem.entity.DemonicCultivatorEntity;
import com.soybe.awakeningsystem.entity.FrostElfAssassinEntity;
import com.soybe.awakeningsystem.entity.FrostarmLawachurlEntity;
import com.soybe.awakeningsystem.entity.GeovishapEntity;
import com.soybe.awakeningsystem.entity.GoblinScoutEntity;
import com.soybe.awakeningsystem.entity.GoblinShamanEntity;
import com.soybe.awakeningsystem.entity.GoblinWarriorEntity;
import com.soybe.awakeningsystem.entity.IsekaiSlimeEntity;
import com.soybe.awakeningsystem.entity.JadeArmoredBeastEntity;
import com.soybe.awakeningsystem.entity.JiangshiEntity;
import com.soybe.awakeningsystem.entity.ManaPixieEntity;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.MotherSpiderBossEntity;
import com.soybe.awakeningsystem.entity.MotherSpiderEntity;
import com.soybe.awakeningsystem.entity.MountHuaDiscipleEntity;
import com.soybe.awakeningsystem.entity.RuinGraderEntity;
import com.soybe.awakeningsystem.entity.RuinGuardEntity;
import com.soybe.awakeningsystem.entity.RuinHunterEntity;
import com.soybe.awakeningsystem.entity.RuinSentinelEntity;
import com.soybe.awakeningsystem.entity.ScreamingMandrakeEntity;
import com.soybe.awakeningsystem.entity.SculkGolemEntity;
import com.soybe.awakeningsystem.entity.ShadowDirewolfEntity;
import com.soybe.awakeningsystem.entity.SpiritFoxEntity;
import com.soybe.awakeningsystem.entity.ThunderhelmLawachurlEntity;
import com.soybe.awakeningsystem.entity.WhiteThunderTigerEntity;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;

public final class GreatTowerWorldManager {
   public static final ResourceKey<Level>[] TOWER_FLOORS = new ResourceKey[20];
   public static final ResourceKey<DimensionType>[] TOWER_FLOOR_TYPES = new ResourceKey[20];
   public static final ResourceKey<Level> TOWER_FLOOR_1;
   public static final ResourceKey<Level> TOWER_FLOOR_2;
   public static final ResourceKey<Level> TOWER_FLOOR_3;
   public static final ResourceKey<Level> TOWER_FLOOR_4;
   public static final ResourceKey<Level> TOWER_FLOOR_5;
   public static final ResourceKey<Level> TOWER_FLOOR_6;
   public static final ResourceKey<Level> TOWER_FLOOR_7;
   public static final ResourceKey<Level> TOWER_FLOOR_8;
   public static final ResourceKey<Level> TOWER_FLOOR_9;
   public static final ResourceKey<Level> TOWER_FLOOR_10;
   public static final ResourceKey<Level> TOWER_FLOOR_11;
   public static final ResourceKey<Level> TOWER_FLOOR_12;
   public static final ResourceKey<Level> TOWER_FLOOR_13;
   public static final ResourceKey<Level> TOWER_FLOOR_14;
   public static final ResourceKey<Level> TOWER_FLOOR_15;
   public static final ResourceKey<Level> TOWER_FLOOR_16;
   public static final ResourceKey<Level> TOWER_FLOOR_17;
   public static final ResourceKey<Level> TOWER_FLOOR_18;
   public static final ResourceKey<Level> TOWER_FLOOR_19;
   public static final ResourceKey<Level> TOWER_FLOOR_20;
   public static final ResourceKey<Level> TOWER_SOLAR;
   public static final ResourceKey<Level> TOWER_LUNAR;
   public static final ResourceKey<DimensionType> TOWER_SOLAR_TYPE;
   public static final ResourceKey<DimensionType> TOWER_LUNAR_TYPE;
   public static final double BORDER_SIZE = 640.0;
   public static final int BORDER_RADIUS = 320;
   private static final TowerSimplexNoise NOISE;
   public static final GreatTowerWorldManager.FloorLayout[] FLOOR_LAYOUTS;

   private GreatTowerWorldManager() {
   }

   public static GreatTowerWorldManager.FloorLayout getLayout(int floor) {
      int index = Math.max(0, Math.min(FLOOR_LAYOUTS.length - 1, floor - 1));
      return FLOOR_LAYOUTS[index];
   }

   public static boolean isGreatTowerDimension(Level level) {
      if (level == null) {
         return false;
      } else {
         ResourceKey<Level> dim = level.dimension();

         for (ResourceKey<Level> floorKey : TOWER_FLOORS) {
            if (dim.equals(floorKey)) {
               return true;
            }
         }

         return false;
      }
   }

   public static ResourceKey<Level> getDimensionForFloor(int floor) {
      int index = Math.max(0, Math.min(TOWER_FLOORS.length - 1, floor - 1));
      return TOWER_FLOORS[index];
   }

   public static int getFloorForPos(Level level, BlockPos pos) {
      if (level == null) {
         return 1;
      } else {
         ResourceKey<Level> dim = level.dimension();

         for (int i = 0; i < TOWER_FLOORS.length; i++) {
            if (dim.equals(TOWER_FLOORS[i])) {
               return i + 1;
            }
         }

         return 1;
      }
   }

   public static BlockPos getFloorCenter(int floor) {
      return new BlockPos(0, 64, 0);
   }

   public static void ensureWorldBorder(ServerLevel level) {
      if (level != null) {
         WorldBorder border = level.getWorldBorder();
         if (border.getSize() != 640.0 || border.getCenterX() != 0.0 || border.getCenterZ() != 0.0) {
            border.setCenter(0.0, 0.0);
            border.setSize(640.0);
            border.setDamagePerBlock(2.0);
            border.setDamageSafeZone(5.0);
            border.setWarningBlocks(24);
            border.setWarningTime(15);
         }
      }
   }

   public static void syncBorderToPlayer(ServerPlayer player) {
      if (player != null && player.serverLevel() != null) {
         ServerLevel level = player.serverLevel();
         if (isGreatTowerDimension(level)) {
            ensureWorldBorder(level);
            player.connection.send(new ClientboundInitializeBorderPacket(level.getWorldBorder()));
         }
      }
   }

   public static int findSafeGroundY(ServerLevel level, int x, int z, int defaultY) {
      int floor = getFloorForPos(level, new BlockPos(x, 64, z));
      if (!level.dimensionType().hasCeiling()) {
         int height = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         if (height > level.getMinBuildHeight() + 5 && height < level.getMaxBuildHeight() - 5) {
            BlockPos checkPos = new BlockPos(x, height, z);
            if (level.getBlockState(checkPos).isAir() && level.getBlockState(checkPos.above()).isAir() && !level.getBlockState(checkPos.below()).liquid()) {
               return height;
            }
         }

         // Escaneo desde la cota maxima con vista directa al cielo (evita cuevas subterraneas bajo montanas, ej. Piso 16)
         for (int y = Math.min(260, level.getMaxBuildHeight() - 5); y >= Math.max(level.getMinBuildHeight() + 5, 45); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()
               && !state.liquid()
               && !state.is(Blocks.BEDROCK)
               && level.getBlockState(pos.above()).isAir()
               && level.getBlockState(pos.above(2)).isAir()
               && !level.getBlockState(pos.above()).liquid()) {
               if (level.canSeeSky(pos.above())) {
                  return y + 1;
               }
            }
         }

         for (int y = Math.min(260, level.getMaxBuildHeight() - 5); y >= level.getMinBuildHeight() + 5; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()
               && !state.liquid()
               && !state.is(Blocks.BEDROCK)
               && level.getBlockState(pos.above()).isAir()
               && level.getBlockState(pos.above(2)).isAir()) {
               return y + 1;
            }
         }

         return defaultY;
      } else {
         int topScan = Math.min(110, level.getMaxBuildHeight() - 10);
         int minScan = floor == 6 ? 48 : (floor != 5 && floor != 15 ? Math.max(20, level.getMinBuildHeight() + 3) : 12);

         for (int r = 0; r <= 24; r += 2) {
            int dxSteps = r == 0 ? 1 : Math.max(1, r * 2);
            int dzSteps = r == 0 ? 1 : Math.max(1, r * 2);

            for (int dx = -r; dx <= r; dx += dxSteps) {
               for (int dz = -r; dz <= r; dz += dzSteps) {
                  int cx = x + dx;
                  int cz = z + dz;

                  for (int yx = topScan; yx >= minScan; yx--) {
                     BlockPos pos = new BlockPos(cx, yx, cz);
                     BlockState state = level.getBlockState(pos);
                     BlockState above1 = level.getBlockState(pos.above());
                     BlockState above2 = level.getBlockState(pos.above(2));
                     BlockState above3 = level.getBlockState(pos.above(3));
                     if (!state.isAir() && !state.liquid() && !state.is(Blocks.BEDROCK) && above1.isAir() && above2.isAir() && !above3.liquid()) {
                        return yx + 1;
                     }
                  }
               }
            }
         }

         return floor == 6 ? 64 : (floor != 5 && floor != 15 ? (floor != 10 && floor != 20 ? 20 : 28) : 32);
      }
   }

   public static boolean enterFloor(ServerPlayer player, int floor) {
      MinecraftServer server = player.getServer();
      if (server == null) {
         return false;
      } else {
         int targetFloor = Math.max(1, Math.min(20, floor));
         ResourceKey<Level> targetDim = getDimensionForFloor(targetFloor);
         ServerLevel targetLevel = server.getLevel(targetDim);
         if (targetLevel == null) {
            player.displayClientMessage(Component.literal("§c[Gran Torre] La dimensión del Piso " + targetFloor + " aún no está disponible."), true);
            return false;
         } else {
            ensureWorldBorder(targetLevel);
            syncBorderToPlayer(player);
            if (!isGreatTowerDimension(player.level())) {
               player.getPersistentData().putString("tower_return_dim", player.level().dimension().location().toString());
               player.getPersistentData().putDouble("tower_return_x", player.getX());
               player.getPersistentData().putDouble("tower_return_y", player.getY());
               player.getPersistentData().putDouble("tower_return_z", player.getZ());
               player.getPersistentData().putFloat("tower_return_yaw", player.getYRot());
               player.getPersistentData().putFloat("tower_return_pitch", player.getXRot());
            }

            GreatTowerWorldManager.FloorLayout layout = getLayout(targetFloor);
            int spawnX = layout.returnX();
            int spawnZ = layout.returnZ();
            int advX = layout.advanceX();
            int advZ = layout.advanceZ();
            int spawnChunkX = spawnX >> 4;
            int spawnChunkZ = spawnZ >> 4;

            for (int dx = -2; dx <= 2; dx++) {
               for (int dz = -2; dz <= 2; dz++) {
                  targetLevel.getChunkSource().getChunk(spawnChunkX + dx, spawnChunkZ + dz, ChunkStatus.FULL, true);
               }
            }

            targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(spawnChunkX, spawnChunkZ), 2, player.getId());
            int advChunkX = advX >> 4;
            int advChunkZ = advZ >> 4;

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  targetLevel.getChunkSource().getChunk(advChunkX + dx, advChunkZ + dz, ChunkStatus.FULL, true);
               }
            }

            if (targetFloor == 10 || targetFloor == 20) {
               for (int cx = -9; cx <= 9; cx++) {
                  for (int cz = -2; cz <= 2; cz++) {
                     targetLevel.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, true);
                  }
               }

               if (targetFloor == 10) {
                  buildFloor10ArenaAndBridges(targetLevel);
               } else {
                  buildFloor20ArenaAndBridges(targetLevel);
               }
            }

            int groundY = targetFloor != 10 && targetFloor != 20
               ? findSafeGroundY(targetLevel, spawnX, spawnZ, targetFloor == 6 ? 64 : (targetFloor != 5 && targetFloor != 15 ? 64 : 32))
               : 28;
            int groundYAdv = targetFloor != 10 && targetFloor != 20
               ? findSafeGroundY(targetLevel, advX, advZ, targetFloor == 6 ? 64 : (targetFloor != 5 && targetFloor != 15 ? 64 : 32))
               : 28;
            buildThemedReturnPavilion(targetLevel, targetFloor, spawnX, groundY, spawnZ, layout.returnFacing());
            buildThemedAdvancePavilion(targetLevel, targetFloor, advX, groundYAdv, advZ, layout.advanceFacing());
            double targetX = (double)spawnX + 0.5;
            double targetY = (double)groundY;
            double targetZ = (double)spawnZ + 0.5;
            float yaw = (float)(Math.atan2((double)(-spawnZ), (double)(-spawnX)) * (180.0 / Math.PI)) - 90.0F;
            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.fallDistance = 0.0F;
            player.teleportTo(targetLevel, targetX, targetY, targetZ, yaw, 0.0F);
            player.setPortalCooldown();
            syncBorderToPlayer(player);
            ensureFloorSpawns(targetLevel, new BlockPos(spawnX, groundY, spawnZ), targetFloor);
            player.playNotifySound((SoundEvent)ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.9F, 1.0F);
            player.displayClientMessage(
               Component.literal("§d§l[Gran Torre] §fHas ascendido al §e§lPiso " + targetFloor + " §7(" + layout.themeName() + "§7)"), false
            );
            player.displayClientMessage(Component.literal("§7El portal de avance se encuentra en las coordenadas §e[" + advX + ", " + advZ + "]§7."), false);
            if (targetFloor == 5) {
               player.displayClientMessage(
                  Component.literal("§b§l[La Gran Caverna] §7Te encuentras en una inmensa boveda subterranea natural de mas de 80 bloques de altura."), false
               );
            } else if (targetFloor == 10) {
               player.displayClientMessage(Component.literal("§c§l[ALERTA DE JEFE] §6¡Has entrado a la Boveda del Titan Colosal de Sculk (Piso 10)!"), false);
               player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8F, 0.9F);
               ensureFloor10Boss(targetLevel);
               ensureFloor10Spawns(targetLevel, player.blockPosition());
            } else if (targetFloor == 11) {
               player.displayClientMessage(
                  Component.literal("§a§l[Aldea Serena] §fHas llegado a un pacífico refugio habitado por aldeanos dentro de la Gran Torre."), false
               );
            } else if (targetFloor == 15) {
               player.displayClientMessage(
                  Component.literal(
                     "§b§l[Caverna Mineral Ancestral] §7Inmensa cueva pacífica enriquecida con abundantes vetas de minerales y cristales de dimensiones."
                  ),
                  false
               );
            } else if (targetFloor == 20) {
               player.displayClientMessage(Component.literal("§c§l[ALERTA DE JEFE] §6¡Has entrado a la Camara Real de la Reina de los Nidos (Piso 20)!"), false);
               player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8F, 0.9F);
               ensureFloor20Boss(targetLevel);
            }

            return true;
         }
      }
   }

   public static void exitTower(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server != null) {
         String returnDimStr = player.getPersistentData().getString("tower_return_dim");
         ResourceKey<Level> returnDimKey = returnDimStr.isEmpty()
            ? Level.OVERWORLD
            : ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(returnDimStr));
         ServerLevel returnLevel = server.getLevel(returnDimKey);
         if (returnLevel == null) {
            returnLevel = server.overworld();
         }

         double x = player.getPersistentData().contains("tower_return_x")
            ? player.getPersistentData().getDouble("tower_return_x")
            : (double)returnLevel.getSharedSpawnPos().getX() + 0.5;
         double y = player.getPersistentData().contains("tower_return_y")
            ? player.getPersistentData().getDouble("tower_return_y")
            : (double)returnLevel.getSharedSpawnPos().getY();
         double z = player.getPersistentData().contains("tower_return_z")
            ? player.getPersistentData().getDouble("tower_return_z")
            : (double)returnLevel.getSharedSpawnPos().getZ() + 0.5;
         float yaw = player.getPersistentData().getFloat("tower_return_yaw");
         float pitch = player.getPersistentData().getFloat("tower_return_pitch");
         player.setDeltaMovement(0.0, 0.0, 0.0);
         player.fallDistance = 0.0F;
         player.teleportTo(returnLevel, x, y, z, yaw, pitch);
         player.setPortalCooldown();
         player.playNotifySound(SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
         player.displayClientMessage(Component.literal("§d§l[Gran Torre] §7Has descendido de la Torre hacia el mundo exterior."), true);
      }
   }

   public static void buildThemedReturnPavilion(ServerLevel level, int floor, int x, int groundY, int z, Direction facing) {
      BlockState floorBlock = getThemedFloorBlock(floor);
      BlockState pillarBlock = getThemedPillarBlock(floor);
      BlockState lightBlock = getThemedLightBlock(floor);
      BlockState rock = ((Block)SystemBlocks.ANCIENT_BROWN_ROCK.get()).defaultBlockState();
      BlockState air = Blocks.AIR.defaultBlockState();
      MutableBlockPos mPos = new MutableBlockPos();
      int baseDepth = Math.max(level.getMinBuildHeight() + 1, groundY - 18);

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            for (int y = baseDepth; y < groundY - 1; y++) {
               mPos.set(x + dx, y, z + dz);
               BlockState cur = level.getBlockState(mPos);
               if (cur.isAir() || cur.liquid()) {
                  level.setBlock(mPos, rock, 2);
               }
            }

            mPos.set(x + dx, groundY - 1, z + dz);
            level.setBlock(mPos, floorBlock, 2);

            for (int h = 0; h <= 4; h++) {
               mPos.set(x + dx, groundY + h, z + dz);
               level.setBlock(mPos, air, 2);
            }
         }
      }

      int[][] corners = new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};

      for (int[] c : corners) {
         for (int h = 0; h <= 2; h++) {
            level.setBlock(new BlockPos(x + c[0], groundY + h, z + c[1]), pillarBlock, 2);
         }

         level.setBlock(new BlockPos(x + c[0], groundY + 3, z + c[1]), lightBlock, 2);
      }

      BlockState portal = (BlockState)((BlockState)((BlockState)((AscensionPortalBlock)SystemBlocks.ASCENSION_PORTAL.get())
               .defaultBlockState()
               .setValue(AscensionPortalBlock.FACING, facing))
            .setValue(AscensionPortalBlock.AXIS, facing.getAxis() == Axis.X ? Axis.Z : Axis.X))
         .setValue(AscensionPortalBlock.PORTAL_TYPE, PortalType.RETURN);
      BlockPos pPos = new BlockPos(x - facing.getStepX() * 2, groundY, z - facing.getStepZ() * 2);
      level.setBlock(pPos, portal, 2);
      // Techo protector abovedado y despeje universal de liquidos (agua y lava) para TODOS los pisos
      BlockState canopy = floor == 6 ? Blocks.POLISHED_BLACKSTONE.defaultBlockState() : pillarBlock;
      BlockState parapet = floor == 6 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : floorBlock;

      for (int dx = -4; dx <= 4; dx++) {
         for (int dz = -4; dz <= 4; dz++) {
            for (int vy = groundY; vy <= Math.min(level.getMaxBuildHeight() - 5, groundY + 28); vy++) {
               mPos.set(x + dx, vy, z + dz);
               if (level.getBlockState(mPos).liquid()) {
                  level.setBlock(mPos, air, 2);
               }
            }

            if (Math.abs(dx) <= 3 && Math.abs(dz) <= 3) {
               level.setBlock(new BlockPos(x + dx, groundY + 4, z + dz), canopy, 2);
               if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                  boolean isDoor = dx == facing.getStepX() * 3 && dz == facing.getStepZ() * 3;
                  if (!isDoor) {
                     level.setBlock(new BlockPos(x + dx, groundY, z + dz), parapet, 2);
                  }
               }
            }
         }
      }
   }

   public static void buildThemedAdvancePavilion(ServerLevel level, int floor, int x, int groundY, int z, Direction facing) {
      BlockState floorBlock = getThemedFloorBlock(floor);
      BlockState pillarBlock = getThemedPillarBlock(floor);
      BlockState lightBlock = getThemedLightBlock(floor);
      BlockState rock = ((Block)SystemBlocks.ANCIENT_BROWN_ROCK.get()).defaultBlockState();
      BlockState air = Blocks.AIR.defaultBlockState();
      MutableBlockPos mPos = new MutableBlockPos();
      int baseDepth = Math.max(level.getMinBuildHeight() + 1, groundY - 18);

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            for (int y = baseDepth; y < groundY - 1; y++) {
               mPos.set(x + dx, y, z + dz);
               BlockState cur = level.getBlockState(mPos);
               if (cur.isAir() || cur.liquid()) {
                  level.setBlock(mPos, rock, 2);
               }
            }

            mPos.set(x + dx, groundY - 1, z + dz);
            level.setBlock(mPos, floorBlock, 2);

            for (int h = 0; h <= 4; h++) {
               mPos.set(x + dx, groundY + h, z + dz);
               level.setBlock(mPos, air, 2);
            }
         }
      }

      int[][] corners = new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};

      for (int[] c : corners) {
         for (int h = 0; h <= 2; h++) {
            level.setBlock(new BlockPos(x + c[0], groundY + h, z + c[1]), pillarBlock, 2);
         }

         level.setBlock(new BlockPos(x + c[0], groundY + 3, z + c[1]), lightBlock, 2);
      }

      BlockState portal = (BlockState)((BlockState)((BlockState)((AscensionPortalBlock)SystemBlocks.ASCENSION_PORTAL.get())
               .defaultBlockState()
               .setValue(AscensionPortalBlock.FACING, facing))
            .setValue(AscensionPortalBlock.AXIS, facing.getAxis() == Axis.X ? Axis.Z : Axis.X))
         .setValue(AscensionPortalBlock.PORTAL_TYPE, PortalType.ASCEND);
      BlockPos pPos = new BlockPos(x - facing.getStepX() * 2, groundY, z - facing.getStepZ() * 2);
      level.setBlock(pPos, portal, 2);
      // Techo protector abovedado y despeje universal de liquidos (agua y lava) para TODOS los pisos
      BlockState canopy = floor == 6 ? Blocks.POLISHED_BLACKSTONE.defaultBlockState() : pillarBlock;
      BlockState parapet = floor == 6 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : floorBlock;

      for (int dx = -4; dx <= 4; dx++) {
         for (int dz = -4; dz <= 4; dz++) {
            for (int vy = groundY; vy <= Math.min(level.getMaxBuildHeight() - 5, groundY + 28); vy++) {
               mPos.set(x + dx, vy, z + dz);
               if (level.getBlockState(mPos).liquid()) {
                  level.setBlock(mPos, air, 2);
               }
            }

            if (Math.abs(dx) <= 3 && Math.abs(dz) <= 3) {
               level.setBlock(new BlockPos(x + dx, groundY + 4, z + dz), canopy, 2);
               if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                  boolean isDoor = dx == facing.getStepX() * 3 && dz == facing.getStepZ() * 3;
                  if (!isDoor) {
                     level.setBlock(new BlockPos(x + dx, groundY, z + dz), parapet, 2);
                  }
               }
            }
         }
      }
   }

   private static BlockState getThemedFloorBlock(int floor) {
      return switch (floor) {
         case 1 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
         case 2 -> Blocks.POLISHED_DEEPSLATE.defaultBlockState();
         case 3 -> Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState();
         case 4 -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
         case 5 -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();
         case 6 -> Blocks.POLISHED_BASALT.defaultBlockState();
         case 7 -> Blocks.CUT_SANDSTONE.defaultBlockState();
         case 8 -> Blocks.PACKED_ICE.defaultBlockState();
         case 9 -> Blocks.CHERRY_PLANKS.defaultBlockState();
         case 10 -> Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
         case 11 -> Blocks.OAK_PLANKS.defaultBlockState();
         case 12 -> Blocks.CRIMSON_PLANKS.defaultBlockState();
         case 13 -> Blocks.POLISHED_ANDESITE.defaultBlockState();
         case 14 -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
         case 15 -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();
         case 16 -> Blocks.END_STONE_BRICKS.defaultBlockState();
         case 17 -> Blocks.BAMBOO_PLANKS.defaultBlockState();
         case 18 -> Blocks.CUT_RED_SANDSTONE.defaultBlockState();
         case 19 -> Blocks.POLISHED_DEEPSLATE.defaultBlockState();
         case 20 -> Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
         default -> Blocks.SMOOTH_STONE.defaultBlockState();
      };
   }

   private static BlockState getThemedPillarBlock(int floor) {
      return switch (floor) {
         case 1 -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
         case 2 -> Blocks.DEEPSLATE_TILES.defaultBlockState();
         case 3 -> Blocks.CHISELED_RED_SANDSTONE.defaultBlockState();
         case 4 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
         case 5 -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
         case 6 -> Blocks.BLACKSTONE.defaultBlockState();
         case 7 -> Blocks.CHISELED_SANDSTONE.defaultBlockState();
         case 8 -> Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
         case 9 -> Blocks.STRIPPED_CHERRY_LOG.defaultBlockState();
         case 10 -> Blocks.CHISELED_DEEPSLATE.defaultBlockState();
         case 11 -> Blocks.STRIPPED_OAK_LOG.defaultBlockState();
         case 12 -> Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState();
         case 13 -> Blocks.SMOOTH_STONE.defaultBlockState();
         case 14 -> Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
         case 15 -> Blocks.COBBLED_DEEPSLATE.defaultBlockState();
         case 16 -> Blocks.PURPUR_PILLAR.defaultBlockState();
         case 17 -> Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState();
         case 18 -> Blocks.CHISELED_RED_SANDSTONE.defaultBlockState();
         case 19 -> Blocks.BONE_BLOCK.defaultBlockState();
         case 20 -> Blocks.DEEPSLATE_TILES.defaultBlockState();
         default -> Blocks.STONE_BRICKS.defaultBlockState();
      };
   }

   private static BlockState getThemedLightBlock(int floor) {
      return switch (floor) {
         case 2, 6, 10 -> Blocks.SOUL_LANTERN.defaultBlockState();
         default -> Blocks.LANTERN.defaultBlockState();
         case 4 -> Blocks.SHROOMLIGHT.defaultBlockState();
         case 5 -> Blocks.SEA_LANTERN.defaultBlockState();
      };
   }

   private static final java.util.Set<Long> DECORATED_CHUNKS = java.util.concurrent.ConcurrentHashMap.newKeySet();

   public static void onChunkLoaded(ServerLevel level, ChunkAccess chunk) {
      if (level == null || chunk == null || !isGreatTowerDimension(level)) return;
      ChunkPos cPos = chunk.getPos();
      long key = ((long) level.dimension().location().hashCode() << 32) ^ (ChunkPos.asLong(cPos.x, cPos.z));
      if (DECORATED_CHUNKS.contains(key)) return;
      DECORATED_CHUNKS.add(key);

      int minX = cPos.getMinBlockX();
      int minZ = cPos.getMinBlockZ();
      if (minX < -330 || minX > 330 || minZ < -330 || minZ > 330) return;

      int floor = getFloorForPos(level, new BlockPos(minX, 64, minZ));

      // 1. Esculpido de cavernas para pisos subterráneos
      if (floor == 5 || floor == 10 || floor == 15 || floor == 20) {
         processSectorChunk(level, chunk);
      }

      // 2. Generación estática única de decoraciones de terreno (escombros, matorrales, cristales, minerales)
      decorateSectorChunk(level, chunk, floor);

      // 3. Población inicial de criaturas del piso a distancia segura
      populateChunkMobs(level, chunk, floor);
   }

   private static void decorateSectorChunk(ServerLevel level, ChunkAccess chunk, int floor) {
      ChunkPos cPos = chunk.getPos();
      int minX = cPos.getMinBlockX();
      int minZ = cPos.getMinBlockZ();
      RandomSource rand = level.random;

      // Montañas / cavernas: escombros y rocas de montaña
      if (floor == 13 || floor == 5 || floor == 10 || floor == 15 || floor == 16) {
         int rubbleAttempts = floor == 13 ? 3 : 2;
         Direction[] horiz = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
         for (int i = 0; i < rubbleAttempts; i++) {
            int cx = minX + rand.nextInt(16);
            int cz = minZ + rand.nextInt(16);
            int cy = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
            BlockPos targetPos = new BlockPos(cx, cy, cz);
            if (level.getBlockState(targetPos).isAir()) {
               BlockState rubbleState = SystemBlocks.MOUNTAIN_STONE_RUBBLE.get().defaultBlockState()
                     .setValue(MountainStoneRubbleBlock.FACING, horiz[rand.nextInt(horiz.length)]);
               if (rubbleState.canSurvive(level, targetPos)) {
                  level.setBlock(targetPos, rubbleState, 2);
               }
            }
         }
      }

      // Bosques / naturaleza: arbustos y matorrales silvestres
      if (floor == 2 || floor == 4 || floor == 9 || floor == 11 || floor == 14 || floor == 17) {
         int shrubAttempts = 2 + rand.nextInt(2);
         for (int i = 0; i < shrubAttempts; i++) {
            int cx = minX + rand.nextInt(16);
            int cz = minZ + rand.nextInt(16);
            int cy = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
            BlockPos targetPos = new BlockPos(cx, cy, cz);
            if (level.getBlockState(targetPos).isAir()) {
               BlockState shrubState = SystemBlocks.FOREST_WILD_SHRUB.get().defaultBlockState();
               if (shrubState.canSurvive(level, targetPos)) {
                  level.setBlock(targetPos, shrubState, 2);
               }
            }
         }
      }

      // Cristales de Maná Direccionales
      if (floor == 5 || floor == 13 || floor == 15 || floor == 19 || floor == 20) {
         int crystalCount = floor == 15 ? 3 : 1;
         for (int i = 0; i < crystalCount; i++) {
            int cx = minX + rand.nextInt(16);
            int cz = minZ + rand.nextInt(16);
            int cy = 12 + rand.nextInt(floor == 5 || floor == 10 || floor == 15 || floor == 20 ? 80 : 40);
            BlockPos checkPos = new BlockPos(cx, cy, cz);
            if (level.getBlockState(checkPos).isAir()) {
               BlockState crystalState = getRandomManaCrystalState(rand, floor);
               tryPlaceDirectionalCrystal(level, checkPos, crystalState);
            }
         }
      }

      // Minerales de Teyvat
      if (floor == 13 || floor == 15) {
         BlockState[] teyvatOres = new BlockState[]{
            ((TeyvatCrystalBlock)SystemBlocks.COR_LAPIS.get()).defaultBlockState(),
            ((TeyvatCrystalBlock)SystemBlocks.NOCTILUCOUS_JADE.get()).defaultBlockState(),
            ((TeyvatCrystalBlock)SystemBlocks.STARSILVER.get()).defaultBlockState(),
            ((TeyvatCrystalBlock)SystemBlocks.CRYSTAL_CHUNK_BLOCK.get()).defaultBlockState(),
            ((TeyvatCrystalBlock)SystemBlocks.MAGICAL_CRYSTAL_BLOCK.get()).defaultBlockState()
         };
         int oreCount = floor == 15 ? 2 : 1;
         for (int i = 0; i < oreCount; i++) {
            int cx = minX + rand.nextInt(16);
            int cz = minZ + rand.nextInt(16);
            int cy = 14 + rand.nextInt(40);
            BlockPos orePos = new BlockPos(cx, cy, cz);
            if (level.getBlockState(orePos).isAir()) {
               tryPlaceDirectionalCrystal(level, orePos, teyvatOres[rand.nextInt(teyvatOres.length)]);
            }
         }
      }
   }

   private static void populateChunkMobs(ServerLevel level, ChunkAccess chunk, int floor) {
      if (floor == 10 || floor == 11 || floor == 20) return;
      ChunkPos cPos = chunk.getPos();
      int cx = cPos.getMiddleBlockX();
      int cz = cPos.getMiddleBlockZ();
      FloorLayout layout = getLayout(floor);
      if (Math.hypot(cx - layout.returnX(), cz - layout.returnZ()) < 36.0) return;
      if (Math.hypot(cx - layout.advanceX(), cz - layout.advanceZ()) < 36.0) return;

      int nearbyCount = level.getEntitiesOfClass(
            Mob.class,
            new AABB(cx - 32, 0, cz - 32, cx + 32, 256, cz + 32),
            e -> e.isAlive() && BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("awakening_system")
         ).size();

      if (nearbyCount < 3 && level.random.nextFloat() < 0.60F) {
         int sy = findSafeGroundY(level, cx, cz, floor == 5 || floor == 15 ? 32 : 64);
         BlockPos spawnPos = new BlockPos(cx, sy, cz);
         if (!level.getBlockState(spawnPos.below()).liquid() && !level.getBlockState(spawnPos).liquid()) {
            LivingEntity mob = createFloorMob(level, floor, level.random);
            if (mob != null) {
               mob.moveTo((double)cx + 0.5, (double)sy, (double)cz + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
               level.addFreshEntity(mob);
            }
         }
      }
   }

   public static void processSectorChunk(ServerLevel level, ChunkAccess chunk) {
      if (isGreatTowerDimension(level)) {
         ensureWorldBorder(level);
         int floor = getFloorForPos(level, new BlockPos(chunk.getPos().getMinBlockX(), 64, chunk.getPos().getMinBlockZ()));
         if (floor == 5 || floor == 10 || floor == 15 || floor == 20) {
            ChunkPos cPos = chunk.getPos();
            int minX = cPos.getMinBlockX();
            int maxX = cPos.getMaxBlockX();
            int minZ = cPos.getMinBlockZ();
            int maxZ = cPos.getMaxBlockZ();
            BlockState testCeil = chunk.getBlockState(new BlockPos(minX + 8, 126, minZ + 8));
            BlockState testMid = chunk.getBlockState(new BlockPos(minX + 8, 64, minZ + 8));
            if (!testCeil.is(Blocks.BEDROCK) || testMid.is(Blocks.NETHERRACK)) {
               BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
               BlockState stone = Blocks.STONE.defaultBlockState();
               BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
               BlockState dripstoneBlock = Blocks.DRIPSTONE_BLOCK.defaultBlockState();
               BlockState pointedDripDown = (BlockState)Blocks.POINTED_DRIPSTONE
                  .defaultBlockState()
                  .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN);
               BlockState water = Blocks.WATER.defaultBlockState();
               BlockState lava = Blocks.LAVA.defaultBlockState();
               BlockState air = Blocks.AIR.defaultBlockState();
               BlockState sculk = Blocks.SCULK.defaultBlockState();
               BlockState sculkCatalyst = Blocks.SCULK_CATALYST.defaultBlockState();
               BlockState soulSand = Blocks.SOUL_SAND.defaultBlockState();
               MutableBlockPos mPos = new MutableBlockPos();

               for (int x = minX; x <= maxX; x++) {
                  for (int z = minZ; z <= maxZ; z++) {
                     double floorNoise = NOISE.fbm2D((double)x * 0.012, (double)z * 0.012, 3, 2.0, 0.5);
                     int floorY = 12 + (int)(floorNoise * 16.0);
                     double ceilNoise = NOISE.fbm2D((double)x * 0.01 + 100.0, (double)z * 0.01 + 100.0, 3, 2.0, 0.5);
                     int ceilY = 108 + (int)(ceilNoise * 14.0);

                     for (int y = 0; y <= 2; y++) {
                        mPos.set(x, y, z);
                        chunk.setBlockState(mPos, bedrock, false);
                     }

                     for (int y = 3; y <= floorY; y++) {
                        mPos.set(x, y, z);
                        if (floor == 10) {
                           chunk.setBlockState(mPos, y == floorY && (x + z) % 3 == 0 ? sculk : deepslate, false);
                        } else {
                           chunk.setBlockState(mPos, y < 16 ? deepslate : stone, false);
                        }
                     }

                     if (floorY <= 11) {
                        mPos.set(x, floorY + 1, z);
                        chunk.setBlockState(mPos, lava, false);
                     } else if (floorY >= 12 && floorY <= 14 && (x + z) % 2 == 0) {
                        mPos.set(x, floorY + 1, z);
                        chunk.setBlockState(mPos, water, false);
                     }

                     for (int yx = floorY + 2; yx < ceilY; yx++) {
                        mPos.set(x, yx, z);
                        if (!chunk.getBlockState(mPos).isAir()) {
                           chunk.setBlockState(mPos, air, false);
                        }
                     }

                     for (int yxx = ceilY; yxx <= 125; yxx++) {
                        mPos.set(x, yxx, z);
                        chunk.setBlockState(mPos, yxx == ceilY && floor == 5 ? dripstoneBlock : (floor == 10 ? deepslate : stone), false);
                     }

                     for (int yxx = 126; yxx <= 127; yxx++) {
                        mPos.set(x, yxx, z);
                        chunk.setBlockState(mPos, bedrock, false);
                     }
                  }
               }

               Random rand = new Random((long)cPos.x * 341873128712L + (long)cPos.z * 132897987541L);
               if (rand.nextInt(3) == 0) {
                  int pillarX = minX + 4 + rand.nextInt(8);
                  int pillarZ = minZ + 4 + rand.nextInt(8);
                  int pFloorY = 12 + (int)(NOISE.fbm2D((double)pillarX * 0.012, (double)pillarZ * 0.012, 3, 2.0, 0.5) * 16.0);
                  int pCeilY = 108 + (int)(NOISE.fbm2D((double)pillarX * 0.01 + 100.0, (double)pillarZ * 0.01 + 100.0, 3, 2.0, 0.5) * 14.0);
                  BlockState pMat = floor == 10 ? deepslate : stone;

                  for (int yxx = pFloorY; yxx <= pCeilY; yxx++) {
                     int rad = yxx >= pFloorY + 5 && yxx <= pCeilY - 5 ? 1 : 2;

                     for (int dx = -rad; dx <= rad; dx++) {
                        for (int dz = -rad; dz <= rad; dz++) {
                           if (Math.abs(dx) != rad || Math.abs(dz) != rad || rad <= 1) {
                              mPos.set(pillarX + dx, yxx, pillarZ + dz);
                              chunk.setBlockState(mPos, pMat, false);
                           }
                        }
                     }
                  }
               }

               int stalactiteCount = 2 + rand.nextInt(4);

               for (int i = 0; i < stalactiteCount; i++) {
                  int sx = minX + 2 + rand.nextInt(12);
                  int sz = minZ + 2 + rand.nextInt(12);
                  int ceilY = 108 + (int)(NOISE.fbm2D((double)sx * 0.01 + 100.0, (double)sz * 0.01 + 100.0, 3, 2.0, 0.5) * 14.0);
                  int length = 8 + rand.nextInt(18);

                  for (int dy = 0; dy <= length; dy++) {
                     int yxx = ceilY - dy;
                     mPos.set(sx, yxx, sz);
                     if (dy == length) {
                        chunk.setBlockState(mPos, pointedDripDown, false);
                     } else {
                        chunk.setBlockState(mPos, floor == 10 ? deepslate : dripstoneBlock, false);
                     }
                  }
               }

               if (rand.nextInt(6) == 0) {
                  int fx = minX + 6 + rand.nextInt(4);
                  int fz = minZ + 6 + rand.nextInt(4);
                  int fCeilY = 108 + (int)(NOISE.fbm2D((double)fx * 0.01 + 100.0, (double)fz * 0.01 + 100.0, 3, 2.0, 0.5) * 14.0);
                  int fFloorY = 12 + (int)(NOISE.fbm2D((double)fx * 0.012, (double)fz * 0.012, 3, 2.0, 0.5) * 16.0);
                  BlockState fluid = rand.nextBoolean() ? lava : water;

                  for (int yxx = fFloorY + 1; yxx <= fCeilY; yxx++) {
                     mPos.set(fx, yxx, fz);
                     chunk.setBlockState(mPos, fluid, false);
                  }
               }

               if (floor == 5) {
                  int count = 1 + rand.nextInt(3);

                  for (int k = 0; k < count; k++) {
                     int cx = minX + 2 + rand.nextInt(12);
                     int cz = minZ + 2 + rand.nextInt(12);
                     int cFloorY = 12 + (int)(NOISE.fbm2D((double)cx * 0.012, (double)cz * 0.012, 3, 2.0, 0.5) * 16.0);
                     mPos.set(cx, cFloorY + 1, cz);
                     if (chunk.getBlockState(mPos).isAir()) {
                        float r = rand.nextFloat();
                        BlockState crystalState;
                        if (r < 0.5F) {
                           crystalState = ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_E.get()).defaultBlockState();
                        } else if (r < 0.85F) {
                           crystalState = ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_D.get()).defaultBlockState();
                        } else {
                           crystalState = ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get()).defaultBlockState();
                        }

                        chunk.setBlockState(mPos, crystalState, false);
                     }
                  }
               } else if (floor == 10) {
                  int count = 1 + rand.nextInt(3);

                  for (int kx = 0; kx < count; kx++) {
                     int cx = minX + 2 + rand.nextInt(12);
                     int cz = minZ + 2 + rand.nextInt(12);
                     int cFloorY = 12 + (int)(NOISE.fbm2D((double)cx * 0.012, (double)cz * 0.012, 3, 2.0, 0.5) * 16.0);
                     mPos.set(cx, cFloorY + 1, cz);
                     if (chunk.getBlockState(mPos).isAir()) {
                        float r = rand.nextFloat();
                        BlockState crystalState;
                        if (r < 0.35F) {
                           crystalState = ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get()).defaultBlockState();
                        } else if (r < 0.7F) {
                           crystalState = ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_B.get()).defaultBlockState();
                        } else if (r < 0.93F) {
                           crystalState = ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_A.get()).defaultBlockState();
                        } else {
                           crystalState = ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_S.get()).defaultBlockState();
                        }

                        chunk.setBlockState(mPos, crystalState, false);
                     }
                  }
               }

               if (floor == 10 && Math.abs(cPos.x) <= 2 && Math.abs(cPos.z) <= 2) {
                  buildColossusArenaChunk(chunk, minX, maxX, minZ, maxZ, mPos);
               }
            }
         }
      }
   }

   private static void buildColossusArenaChunk(ChunkAccess chunk, int minX, int maxX, int minZ, int maxZ, MutableBlockPos mPos) {
      BlockState reinforced = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
      BlockState chiseled = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
      BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState();
      int arenaY = 25;

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            double dist = Math.sqrt((double)(x * x + z * z));
            if (dist <= 26.0) {
               mPos.set(x, arenaY, z);
               chunk.setBlockState(mPos, dist >= 24.0 ? reinforced : chiseled, false);
               if (dist >= 24.5 && (Math.abs(x) % 6 == 0 || Math.abs(z) % 6 == 0)) {
                  for (int h = 1; h <= 3; h++) {
                     mPos.set(x, arenaY + h, z);
                     chunk.setBlockState(mPos, reinforced, false);
                  }

                  mPos.set(x, arenaY + 4, z);
                  chunk.setBlockState(mPos, soulLantern, false);
               }
            }
         }
      }
   }

   public static void enforcePlayerBoundary(ServerPlayer player) {
      if (isGreatTowerDimension(player.level())) {
         int floor = getFloorForPos(player.level(), player.blockPosition());
         boolean aboveCeiling = player.level().dimensionType().hasCeiling() && player.getY() > (floor == 5 ? 122.0 : 125.0);
         boolean inVoid = player.getY() < (double)(player.level().getMinBuildHeight() + 2);
         if (inVoid || aboveCeiling) {
            GreatTowerWorldManager.FloorLayout layout = getLayout(floor);
            int groundY = floor == 10
               ? 28
               : findSafeGroundY(player.serverLevel(), layout.returnX(), layout.returnZ(), floor == 6 ? 64 : (floor == 5 ? 32 : 64));
            player.teleportTo((double)layout.returnX() + 0.5, (double)groundY, (double)layout.returnZ() + 0.5);
            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.fallDistance = 0.0F;
            player.displayClientMessage(Component.literal("§e§l[Gran Torre] §7Has sido devuelto a la zona segura del piso."), true);
            player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
         }
      }
   }

   public static String getFloorTitle(int floor) {
      return getLayout(floor).themeName();
   }

   public static void ensureFloorSpawns(ServerLevel level, BlockPos centerPos, int floor) {
      if (level != null && centerPos != null) {
         if (floor == 10) {
            ensureFloor10Spawns(level, centerPos);
         } else if (floor != 11) {
            if (floor != 15) {
               if (floor == 20) {
                  ensureFloor20Boss(level);
               } else {

                  int nearbyCount = level.getEntitiesOfClass(
                        Mob.class,
                        new AABB(centerPos).inflate(64.0),
                        e -> e.isAlive() && BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("awakening_system")
                     )
                     .size();
                  if (nearbyCount < 6) {
                     int toSpawn = Math.min(3, 6 - nearbyCount);
                     RandomSource rand = level.random;

                     for (int i = 0; i < toSpawn; i++) {
                        double angle = rand.nextDouble() * Math.PI * 2.0;
                        double dist = 38.0 + rand.nextDouble() * 26.0;
                        int sx = centerPos.getX() + (int)(Math.cos(angle) * dist);
                        int sz = centerPos.getZ() + (int)(Math.sin(angle) * dist);
                        sx = Math.max(-310, Math.min(310, sx));
                        sz = Math.max(-310, Math.min(310, sz));
                        int sy = findSafeGroundY(level, sx, sz, centerPos.getY());
                        BlockPos spawnPos = new BlockPos(sx, sy, sz);
                        if (!level.getBlockState(spawnPos.below()).liquid() && !level.getBlockState(spawnPos).liquid()) {
                           LivingEntity mob = createFloorMob(level, floor, rand);
                           if (mob != null) {
                              mob.moveTo((double)sx + 0.5, (double)sy, (double)sz + 0.5, rand.nextFloat() * 360.0F, 0.0F);
                              level.addFreshEntity(mob);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static LivingEntity createFloorMob(ServerLevel level, int floor, RandomSource rand) {
      return (LivingEntity)(switch (floor) {
         case 1 -> {
            float r = rand.nextFloat();
            yield r < 0.45F
               ? new GoblinScoutEntity((EntityType)ModEntities.GOBLIN_SCOUT.get(), level)
               : (
                  r < 0.8F
                     ? new IsekaiSlimeEntity((EntityType)ModEntities.ISEKAI_SLIME.get(), level)
                     : new SpiritFoxEntity((EntityType)ModEntities.SPIRIT_FOX.get(), level)
               );
         }
         case 2 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new GoblinWarriorEntity((EntityType)ModEntities.GOBLIN_WARRIOR.get(), level)
               : (
                  r < 0.7F
                     ? new ShadowDirewolfEntity((EntityType)ModEntities.SHADOW_DIREWOLF.get(), level)
                     : (
                        r < 0.9F
                           ? new JiangshiEntity((EntityType)ModEntities.JIANGSHI.get(), level)
                           : new ScreamingMandrakeEntity((EntityType)ModEntities.SCREAMING_MANDRAKE.get(), level)
                     )
               );
         }
         case 3 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new GoblinShamanEntity((EntityType)ModEntities.GOBLIN_SHAMAN.get(), level)
               : (
                  r < 0.7F
                     ? new BeastmanWarriorEntity((EntityType)ModEntities.BEASTMAN_WARRIOR.get(), level)
                     : new JadeArmoredBeastEntity((EntityType)ModEntities.JADE_ARMORED_BEAST.get(), level)
               );
         }
         case 4 -> {
            float r = rand.nextFloat();
            yield r < 0.45F
               ? new AbyssalOrcEntity((EntityType)ModEntities.ABYSSAL_ORC.get(), level)
               : (
                  r < 0.75F
                     ? new DemonicCultivatorEntity((EntityType)ModEntities.DEMONIC_CULTIVATOR.get(), level)
                     : new ManaPixieEntity((EntityType)ModEntities.MANA_PIXIE.get(), level)
               );
         }
         case 5 -> {
            float r = rand.nextFloat();
            yield r < 0.45F
               ? new AbyssalOrcEntity((EntityType)ModEntities.ABYSSAL_ORC.get(), level)
               : (
                  r < 0.75F
                     ? new GeovishapEntity((EntityType)ModEntities.GEOVISHAP.get(), level)
                     : (
                        r < 0.9F
                           ? new IsekaiSlimeEntity((EntityType)ModEntities.ISEKAI_SLIME.get(), level)
                           : new AbyssalBeholderEntity((EntityType)ModEntities.ABYSSAL_BEHOLDER.get(), level)
                     )
               );
         }
         case 6 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new BloodSectDiscipleEntity((EntityType)ModEntities.BLOOD_SECT_DISCIPLE.get(), level)
               : (
                  r < 0.7F
                     ? new MitachurlEntity((EntityType)ModEntities.MITACHURL.get(), level)
                     : (
                        r < 0.85F
                           ? new AntSoldierEntity((EntityType)ModEntities.ANT_SOLDIER.get(), level)
                           : new RuinSentinelEntity((EntityType)ModEntities.RUIN_SENTINEL.get(), level)
                     )
               );
         }
         case 7 -> {
            float r = rand.nextFloat();
            yield r < 0.35F
               ? new RuinSentinelEntity((EntityType)ModEntities.RUIN_SENTINEL.get(), level)
               : (
                  r < 0.6F
                     ? new RuinHunterEntity((EntityType)ModEntities.RUIN_HUNTER.get(), level)
                     : (
                        r < 0.8F
                           ? new RuinGuardEntity((EntityType)ModEntities.RUIN_GUARD.get(), level)
                           : new ThunderhelmLawachurlEntity((EntityType)ModEntities.THUNDERHELM_LAWACHURL.get(), level)
                     )
               );
         }
         case 8 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new FrostElfAssassinEntity((EntityType)ModEntities.FROST_ELF_ASSASSIN.get(), level)
               : (
                  r < 0.7F
                     ? new FrostarmLawachurlEntity((EntityType)ModEntities.FROSTARM_LAWACHURL.get(), level)
                     : (
                        r < 0.85F
                           ? new BathysmalVishapEntity((EntityType)ModEntities.BATHYSMAL_VISHAP.get(), level)
                           : new ShadowDirewolfEntity((EntityType)ModEntities.SHADOW_DIREWOLF.get(), level)
                     )
               );
         }
         case 9 -> {
            float r = rand.nextFloat();
            yield r < 0.35F
               ? new AstralDragonEntity((EntityType)ModEntities.ASTRAL_DRAGON.get(), level)
               : (
                  r < 0.65F
                     ? new MountHuaDiscipleEntity((EntityType)ModEntities.MOUNT_HUA_DISCIPLE.get(), level)
                     : (
                        r < 0.85F
                           ? new WhiteThunderTigerEntity((EntityType)ModEntities.WHITE_THUNDER_TIGER.get(), level)
                           : new RuinGraderEntity((EntityType)ModEntities.RUIN_GRADER.get(), level)
                     )
               );
         }
         default -> null;
         case 11 -> null;
         case 12 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new BloodSectDiscipleEntity((EntityType)ModEntities.BLOOD_SECT_DISCIPLE.get(), level)
               : (
                  r < 0.75F
                     ? new AntSoldierEntity((EntityType)ModEntities.ANT_SOLDIER.get(), level)
                     : new MitachurlEntity((EntityType)ModEntities.MITACHURL.get(), level)
               );
         }
         case 13 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new RuinSentinelEntity((EntityType)ModEntities.RUIN_SENTINEL.get(), level)
               : (
                  r < 0.7F
                     ? new RuinGuardEntity((EntityType)ModEntities.RUIN_GUARD.get(), level)
                     : new BathysmalVishapEntity((EntityType)ModEntities.BATHYSMAL_VISHAP.get(), level)
               );
         }
         case 14 -> {
            float r = rand.nextFloat();
            yield r < 0.35F
               ? new ShadowDirewolfEntity((EntityType)ModEntities.SHADOW_DIREWOLF.get(), level)
               : (
                  r < 0.65F
                     ? new AbyssalOrcEntity((EntityType)ModEntities.ABYSSAL_ORC.get(), level)
                     : (
                        r < 0.85F
                           ? new JiangshiEntity((EntityType)ModEntities.JIANGSHI.get(), level)
                           : new AbyssalBeholderEntity((EntityType)ModEntities.ABYSSAL_BEHOLDER.get(), level)
                     )
               );
         }
         case 15 -> null;
         case 16 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new RuinHunterEntity((EntityType)ModEntities.RUIN_HUNTER.get(), level)
               : (
                  r < 0.75F
                     ? new DemonicCultivatorEntity((EntityType)ModEntities.DEMONIC_CULTIVATOR.get(), level)
                     : new AbyssalBeholderEntity((EntityType)ModEntities.ABYSSAL_BEHOLDER.get(), level)
               );
         }
         case 17 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new WhiteThunderTigerEntity((EntityType)ModEntities.WHITE_THUNDER_TIGER.get(), level)
               : (
                  r < 0.75F
                     ? new MountHuaDiscipleEntity((EntityType)ModEntities.MOUNT_HUA_DISCIPLE.get(), level)
                     : new DemonicCultivatorEntity((EntityType)ModEntities.DEMONIC_CULTIVATOR.get(), level)
               );
         }
         case 18 -> {
            float r = rand.nextFloat();
            yield r < 0.4F
               ? new ThunderhelmLawachurlEntity((EntityType)ModEntities.THUNDERHELM_LAWACHURL.get(), level)
               : (
                  r < 0.7F
                     ? new RuinGraderEntity((EntityType)ModEntities.RUIN_GRADER.get(), level)
                     : new AstralDragonEntity((EntityType)ModEntities.ASTRAL_DRAGON.get(), level)
               );
         }
         case 19 -> {
            float r = rand.nextFloat();
            yield r < 0.35F
               ? new FrostElfAssassinEntity((EntityType)ModEntities.FROST_ELF_ASSASSIN.get(), level)
               : (
                  r < 0.65F
                     ? new ShadowDirewolfEntity((EntityType)ModEntities.SHADOW_DIREWOLF.get(), level)
                     : (
                        r < 0.85F
                           ? new AbyssalOrcEntity((EntityType)ModEntities.ABYSSAL_ORC.get(), level)
                           : new AntSoldierEntity((EntityType)ModEntities.ANT_SOLDIER.get(), level)
                     )
               );
         }
      });
   }

   public static int buildFloor10ArenaAndBridges(ServerLevel level) {
      int arenaY = 28;
      BlockPos checkPos = new BlockPos(0, arenaY, 0);
      if (level.getBlockState(checkPos).is(Blocks.SCULK_CATALYST)) {
         return arenaY;
      }

      // Pre-cargar todos los chunks necesarios para la arena y puentes (-140 a +140)
      for (int cx = -9; cx <= 9; cx++) {
         for (int cz = -2; cz <= 2; cz++) {
            level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, true);
         }
      }

      BlockState sculkCatalyst = Blocks.SCULK_CATALYST.defaultBlockState();
      BlockState reinforced = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
      BlockState polished = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
      BlockState chiseled = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
      BlockState bricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
      BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
      BlockState air = Blocks.AIR.defaultBlockState();
      BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState();
      BlockState soulTorch = Blocks.SOUL_TORCH.defaultBlockState();
      MutableBlockPos mPos = new MutableBlockPos();
      int setFlags = 18; // UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE

      for (int x = -26; x <= 26; x++) {
         for (int z = -26; z <= 26; z++) {
            double dist = Math.sqrt((double)(x * x + z * z));
            if (!(dist > 26.0)) {
               for (int y = arenaY + 1; y <= arenaY + 22; y++) {
                  mPos.set(x, y, z);
                  if (!level.getBlockState(mPos).isAir()) {
                     level.setBlock(mPos, air, setFlags);
                  }
               }

               mPos.set(x, arenaY, z);
               if (dist <= 6.0) {
                  level.setBlock(mPos, sculkCatalyst, setFlags);
               } else if (dist <= 22.0) {
                  level.setBlock(mPos, (int)dist % 2 == 0 ? polished : chiseled, setFlags);
               } else {
                  level.setBlock(mPos, reinforced, setFlags);
               }

               for (int yx = arenaY - 1; yx >= Math.max(16, arenaY - 6); yx--) {
                  mPos.set(x, yx, z);
                  if (level.getBlockState(mPos).isAir() || level.getBlockState(mPos).liquid()) {
                     level.setBlock(mPos, deepslate, setFlags);
                  }
               }

               if (dist >= 24.5 && dist <= 26.0 && (Math.abs(x) % 6 == 0 || Math.abs(z) % 6 == 0)) {
                  for (int h = 1; h <= 3; h++) {
                     mPos.set(x, arenaY + h, z);
                     level.setBlock(mPos, reinforced, setFlags);
                  }

                  mPos.set(x, arenaY + 4, z);
                  level.setBlock(mPos, soulLantern, setFlags);
               }
            }
         }
      }

      for (int x = -137; x <= -25; x++) {
         for (int zx = -2; zx <= 2; zx++) {
            for (int yxx = arenaY + 1; yxx <= arenaY + 4; yxx++) {
               mPos.set(x, yxx, zx);
               if (!level.getBlockState(mPos).isAir()) {
                  level.setBlock(mPos, air, setFlags);
               }
            }

            mPos.set(x, arenaY, zx);
            level.setBlock(mPos, bricks, setFlags);

            for (int yxxx = arenaY - 1; yxxx >= Math.max(16, arenaY - 5); yxxx--) {
               mPos.set(x, yxxx, zx);
               if (level.getBlockState(mPos).isAir() || level.getBlockState(mPos).liquid()) {
                  level.setBlock(mPos, deepslate, setFlags);
               }
            }

            if ((zx == -2 || zx == 2) && x % 8 == 0) {
               mPos.set(x, arenaY + 1, zx);
               level.setBlock(mPos, soulTorch, setFlags);
            }
         }
      }

      for (int x = 25; x <= 137; x++) {
         for (int zx = -2; zx <= 2; zx++) {
            for (int yxxxx = arenaY + 1; yxxxx <= arenaY + 4; yxxxx++) {
               mPos.set(x, yxxxx, zx);
               if (!level.getBlockState(mPos).isAir()) {
                  level.setBlock(mPos, air, setFlags);
               }
            }

            mPos.set(x, arenaY, zx);
            level.setBlock(mPos, bricks, setFlags);

            for (int yxxxxx = arenaY - 1; yxxxxx >= Math.max(16, arenaY - 5); yxxxxx--) {
               mPos.set(x, yxxxxx, zx);
               if (level.getBlockState(mPos).isAir() || level.getBlockState(mPos).liquid()) {
                  level.setBlock(mPos, deepslate, setFlags);
               }
            }

            if ((zx == -2 || zx == 2) && x % 8 == 0) {
               mPos.set(x, arenaY + 1, zx);
               level.setBlock(mPos, soulTorch, setFlags);
            }
         }
      }

      return arenaY;
   }

   public static void ensureFloor10Boss(ServerLevel level) {
      if (level != null) {
         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               level.getChunkSource().getChunk(dx, dz, ChunkStatus.FULL, true);
            }
         }

         int arenaY = buildFloor10ArenaAndBridges(level);
         List<? extends ColossalSculkWardenEntity> bosses = level.getEntities((EntityTypeTest)ModEntities.COLOSSAL_SCULK_WARDEN.get(), LivingEntity::isAlive);
         if (bosses.size() > 1) {
            for (int i = 1; i < bosses.size(); i++) {
               bosses.get(i).discard();
            }
         } else if (bosses.size() != 1) {
            if (!DimensionGenSavedData.get(level.getServer()).isTowerFloor10BossDefeated()) {
               ColossalSculkWardenEntity boss = new ColossalSculkWardenEntity((EntityType)ModEntities.COLOSSAL_SCULK_WARDEN.get(), level);
               boss.moveTo(0.5, (double)arenaY + 1.0, 0.5, 0.0F, 0.0F);
               level.addFreshEntity(boss);
            }
         }
      }
   }

   public static void ensureFloor10Spawns(ServerLevel level, BlockPos centerPos) {
      if (level != null && centerPos != null) {
         int existingGolems = level.getEntities(
               (EntityTypeTest)ModEntities.SCULK_GOLEM.get(),
               e -> e.distanceToSqr((double)centerPos.getX(), (double)centerPos.getY(), (double)centerPos.getZ()) <= 6400.0
            )
            .size();
         if (existingGolems < 4) {
            int toSpawn = 4 - existingGolems;

            for (int i = 0; i < toSpawn; i++) {
               int rx = centerPos.getX() + (level.random.nextInt(60) - 30);
               int rz = centerPos.getZ() + (level.random.nextInt(60) - 30);
               int groundY = findSafeGroundY(level, rx, rz, 28);
               SculkGolemEntity golem = new SculkGolemEntity((EntityType)ModEntities.SCULK_GOLEM.get(), level);
               golem.moveTo((double)rx + 0.5, (double)groundY, (double)rz + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
               golem.setStatue(true);
               level.addFreshEntity(golem);
            }
         }

         int existingWardens = level.getEntities(
               EntityType.WARDEN, e -> e.distanceToSqr((double)centerPos.getX(), (double)centerPos.getY(), (double)centerPos.getZ()) <= 6400.0
            )
            .size();
         if (existingWardens < 2) {
            int rx = centerPos.getX() + (level.random.nextInt(70) - 35);
            int rz = centerPos.getZ() + (level.random.nextInt(70) - 35);
            int groundY = findSafeGroundY(level, rx, rz, 28);
            Warden warden = (Warden)EntityType.WARDEN.create(level);
            if (warden != null) {
               warden.moveTo((double)rx + 0.5, (double)groundY, (double)rz + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
               level.addFreshEntity(warden);
            }
         }
      }
   }

   public static void buildFloor11Village(ServerLevel level) {
      if (level != null) {
         BlockPos checkPos = new BlockPos(0, 64, 0);
         if (!level.getBlockState(checkPos).is(Blocks.WATER)) {
            BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
            BlockState dirtPath = Blocks.DIRT_PATH.defaultBlockState();
            BlockState oakPlanks = Blocks.OAK_PLANKS.defaultBlockState();
            BlockState oakLog = Blocks.OAK_LOG.defaultBlockState();
            BlockState oakStairs = Blocks.OAK_STAIRS.defaultBlockState();
            BlockState oakFence = Blocks.OAK_FENCE.defaultBlockState();
            BlockState glassPane = Blocks.GLASS_PANE.defaultBlockState();
            BlockState lantern = Blocks.LANTERN.defaultBlockState();
            BlockState water = Blocks.WATER.defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockState farmland = Blocks.FARMLAND.defaultBlockState();
            BlockState wheat = Blocks.WHEAT.defaultBlockState();
            BlockState carrots = Blocks.CARROTS.defaultBlockState();
            MutableBlockPos mPos = new MutableBlockPos();

            for (int dx = -2; dx <= 2; dx++) {
               for (int dz = -2; dz <= 2; dz++) {
                  mPos.set(dx, 63, dz);
                  level.setBlock(mPos, cobblestone, 2);
                  mPos.set(dx, 64, dz);
                  if (Math.abs(dx) != 2 && Math.abs(dz) != 2) {
                     level.setBlock(mPos, water, 2);
                  } else {
                     level.setBlock(mPos, cobblestone, 2);
                  }
               }
            }

            int[][] wellPosts = new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};

            for (int[] p : wellPosts) {
               for (int h = 1; h <= 3; h++) {
                  level.setBlock(new BlockPos(p[0], 64 + h, p[1]), oakFence, 2);
               }
            }

            for (int dx = -2; dx <= 2; dx++) {
               for (int dzx = -2; dzx <= 2; dzx++) {
                  level.setBlock(new BlockPos(dx, 68, dzx), Blocks.COBBLESTONE_SLAB.defaultBlockState(), 2);
               }
            }

            level.setBlock(new BlockPos(0, 67, 0), lantern, 2);

            for (int d = 3; d <= 25; d++) {
               for (int w = -1; w <= 1; w++) {
                  level.setBlock(new BlockPos(w, 63, d), dirtPath, 2);
                  level.setBlock(new BlockPos(w, 63, -d), dirtPath, 2);
                  level.setBlock(new BlockPos(d, 63, w), dirtPath, 2);
                  level.setBlock(new BlockPos(-d, 63, w), dirtPath, 2);
               }
            }

            int[][] lampCoords = new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}, {15, 3}, {-15, 3}, {3, 15}, {3, -15}};

            for (int[] lp : lampCoords) {
               BlockPos base = new BlockPos(lp[0], 64, lp[1]);
               level.setBlock(base, Blocks.COBBLESTONE_WALL.defaultBlockState(), 2);
               level.setBlock(base.above(), oakFence, 2);
               level.setBlock(base.above(2), lantern, 2);
            }

            buildSimpleHouse(level, 8, 64, 10, 5, 5, Blocks.BLAST_FURNACE.defaultBlockState(), Blocks.GRINDSTONE.defaultBlockState());
            buildSimpleHouse(level, 10, 64, -12, 5, 5, Blocks.COMPOSTER.defaultBlockState(), null);

            for (int fx = 10; fx <= 16; fx++) {
               for (int fz = -6; fz <= -2; fz++) {
                  BlockPos fPos = new BlockPos(fx, 63, fz);
                  if (fx == 13) {
                     level.setBlock(fPos, water, 2);
                     level.setBlock(fPos.above(), Blocks.OAK_SLAB.defaultBlockState(), 2);
                  } else {
                     level.setBlock(fPos, farmland, 2);
                     level.setBlock(fPos.above(), (fx + fz) % 2 == 0 ? wheat : carrots, 2);
                  }
               }
            }

            buildSimpleHouse(level, -14, 64, -10, 5, 5, Blocks.BREWING_STAND.defaultBlockState(), Blocks.CAULDRON.defaultBlockState());
            buildSimpleHouse(level, -12, 64, 12, 5, 5, Blocks.LECTERN.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState());
            spawnVillageNpc(level, 0, 64, 4, VillagerProfession.ARMORER);
            spawnVillageNpc(level, 12, 64, -8, VillagerProfession.FARMER);
            spawnVillageNpc(level, -10, 64, -8, VillagerProfession.CLERIC);
            spawnVillageNpc(level, -8, 64, 12, VillagerProfession.LIBRARIAN);
            spawnVillageGolem(level, 4, 64, 0);
            spawnVillageGolem(level, -4, 64, 0);
         }
      }
   }

   private static void buildSimpleHouse(
      ServerLevel level, int startX, int startY, int startZ, int width, int length, BlockState workstation1, BlockState workstation2
   ) {
      BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
      BlockState oakPlanks = Blocks.OAK_PLANKS.defaultBlockState();
      BlockState oakLog = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
      BlockState glassPane = Blocks.GLASS_PANE.defaultBlockState();
      BlockState air = Blocks.AIR.defaultBlockState();
      MutableBlockPos mPos = new MutableBlockPos();

      for (int x = 0; x < width; x++) {
         for (int z = 0; z < length; z++) {
            mPos.set(startX + x, startY - 1, startZ + z);
            level.setBlock(mPos, cobblestone, 2);
         }
      }

      for (int y = 0; y < 4; y++) {
         for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
               mPos.set(startX + x, startY + y, startZ + z);
               boolean isCorner = (x == 0 || x == width - 1) && (z == 0 || z == length - 1);
               boolean isWall = x == 0 || x == width - 1 || z == 0 || z == length - 1;
               if (isCorner) {
                  level.setBlock(mPos, oakLog, 2);
               } else if (isWall) {
                  if (y != 1 || x != 2 && z != 2) {
                     if (y == 0 && x == 1 && z == 0) {
                        level.setBlock(mPos, air, 2);
                     } else {
                        level.setBlock(mPos, oakPlanks, 2);
                     }
                  } else {
                     level.setBlock(mPos, glassPane, 2);
                  }
               } else {
                  level.setBlock(mPos, air, 2);
               }
            }
         }
      }

      for (int x = -1; x <= width; x++) {
         for (int zx = -1; zx <= length; zx++) {
            level.setBlock(new BlockPos(startX + x, startY + 4, startZ + zx), Blocks.OAK_SLAB.defaultBlockState(), 2);
         }
      }

      level.setBlock(new BlockPos(startX + 1, startY, startZ + length - 2), Blocks.RED_BED.defaultBlockState(), 2);
      level.setBlock(new BlockPos(startX + width - 2, startY, startZ + length - 2), Blocks.CHEST.defaultBlockState(), 2);
      if (workstation1 != null) {
         level.setBlock(new BlockPos(startX + width - 2, startY, startZ + 1), workstation1, 2);
      }

      if (workstation2 != null) {
         level.setBlock(new BlockPos(startX + 1, startY, startZ + 1), workstation2, 2);
      }

      level.setBlock(new BlockPos(startX + width / 2, startY + 3, startZ + length / 2), Blocks.LANTERN.defaultBlockState(), 2);
   }

   private static void spawnVillageNpc(ServerLevel level, int x, int y, int z, VillagerProfession profession) {
      Villager villager = (Villager)EntityType.VILLAGER.create(level);
      if (villager != null) {
         villager.moveTo((double)x + 0.5, (double)y, (double)z + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         villager.setVillagerData(villager.getVillagerData().setProfession(profession).setLevel(2));
         villager.setPersistenceRequired();
         level.addFreshEntity(villager);
      }
   }

   private static void spawnVillageGolem(ServerLevel level, int x, int y, int z) {
      IronGolem golem = (IronGolem)EntityType.IRON_GOLEM.create(level);
      if (golem != null) {
         golem.moveTo((double)x + 0.5, (double)y, (double)z + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         golem.setPersistenceRequired();
         level.addFreshEntity(golem);
      }
   }

   public static int buildFloor20ArenaAndBridges(ServerLevel level) {
      int arenaY = 28;
      BlockPos checkPos = new BlockPos(0, arenaY, 0);
      if (level.getBlockState(checkPos).is(Blocks.BONE_BLOCK)) {
         return arenaY;
      }

      // Pre-cargar todos los chunks necesarios para la arena y los puentes continuos (-140 a +140)
      for (int cx = -9; cx <= 9; cx++) {
         for (int cz = -2; cz <= 2; cz++) {
            level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, true);
         }
      }

      BlockState boneBlock = Blocks.BONE_BLOCK.defaultBlockState();
      BlockState reinforced = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
      BlockState chiseled = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
      BlockState tiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
      BlockState bricks = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
      BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
      BlockState cobweb = Blocks.COBWEB.defaultBlockState();
      BlockState air = Blocks.AIR.defaultBlockState();
      BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState();
      BlockState soulTorch = Blocks.SOUL_TORCH.defaultBlockState();
      MutableBlockPos mPos = new MutableBlockPos();
      int setFlags = 18; // UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE (evita recálculos en cadena de vecinos)

      for (int x = -28; x <= 28; x++) {
         for (int z = -28; z <= 28; z++) {
            double dist = Math.sqrt((double)(x * x + z * z));
            if (!(dist > 28.0)) {
               for (int y = arenaY + 1; y <= arenaY + 24; y++) {
                  mPos.set(x, y, z);
                  if (!level.getBlockState(mPos).isAir()) {
                     level.setBlock(mPos, air, setFlags);
                  }
               }

               mPos.set(x, arenaY, z);
               if (dist > 26.0) {
                  level.setBlock(mPos, reinforced, setFlags);
               } else if (dist > 22.0) {
                  level.setBlock(mPos, bricks, setFlags);
               } else if (x == 0 && z == 0) {
                  level.setBlock(mPos, boneBlock, setFlags);
               } else {
                  level.setBlock(mPos, (x + z) % 3 == 0 ? chiseled : tiles, setFlags);
               }

               for (int yx = arenaY - 1; yx >= Math.max(16, arenaY - 6); yx--) {
                  mPos.set(x, yx, z);
                  if (level.getBlockState(mPos).isAir() || level.getBlockState(mPos).liquid()) {
                     level.setBlock(mPos, deepslate, setFlags);
                  }
               }

               if (dist > 25.0 && dist <= 27.5 && x * z % 5 == 0) {
                  mPos.set(x, arenaY + 1, z);
                  level.setBlock(mPos, cobweb, setFlags);
               }
            }
         }
      }

      int[] pillarAngles = new int[]{0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330};

      for (int deg : pillarAngles) {
         double rad = Math.toRadians((double)deg);
         int px = (int)Math.round(Math.cos(rad) * 25.0);
         int pz = (int)Math.round(Math.sin(rad) * 25.0);

         for (int h = 1; h <= 8; h++) {
            mPos.set(px, arenaY + h, pz);
            level.setBlock(mPos, h % 3 == 0 ? chiseled : boneBlock, setFlags);
         }

         level.setBlock(new BlockPos(px, arenaY + 9, pz), soulLantern, setFlags);
         level.setBlock(new BlockPos(px, arenaY + 7, pz), cobweb, setFlags);
      }

      for (int x = -137; x <= -27; x++) {
         for (int zx = -2; zx <= 2; zx++) {
            for (int yxx = arenaY + 1; yxx <= arenaY + 4; yxx++) {
               mPos.set(x, yxx, zx);
               if (!level.getBlockState(mPos).isAir()) {
                  level.setBlock(mPos, air, setFlags);
               }
            }

            mPos.set(x, arenaY, zx);
            level.setBlock(mPos, bricks, setFlags);

            for (int yxxx = arenaY - 1; yxxx >= Math.max(16, arenaY - 5); yxxx--) {
               mPos.set(x, yxxx, zx);
               if (level.getBlockState(mPos).isAir() || level.getBlockState(mPos).liquid()) {
                  level.setBlock(mPos, deepslate, setFlags);
               }
            }

            if ((zx == -2 || zx == 2) && x % 8 == 0) {
               mPos.set(x, arenaY + 1, zx);
               level.setBlock(mPos, soulTorch, setFlags);
            }
         }
      }

      for (int x = 27; x <= 137; x++) {
         for (int zx = -2; zx <= 2; zx++) {
            for (int yxxxx = arenaY + 1; yxxxx <= arenaY + 4; yxxxx++) {
               mPos.set(x, yxxxx, zx);
               if (!level.getBlockState(mPos).isAir()) {
                  level.setBlock(mPos, air, setFlags);
               }
            }

            mPos.set(x, arenaY, zx);
            level.setBlock(mPos, bricks, setFlags);

            for (int yxxxxx = arenaY - 1; yxxxxx >= Math.max(16, arenaY - 5); yxxxxx--) {
               mPos.set(x, yxxxxx, zx);
               if (level.getBlockState(mPos).isAir() || level.getBlockState(mPos).liquid()) {
                  level.setBlock(mPos, deepslate, setFlags);
               }
            }

            if ((zx == -2 || zx == 2) && x % 8 == 0) {
               mPos.set(x, arenaY + 1, zx);
               level.setBlock(mPos, soulTorch, setFlags);
            }
         }
      }

      return arenaY;
   }

   public static void ensureFloor20Boss(ServerLevel level) {
      if (level != null) {
         int arenaY = buildFloor20ArenaAndBridges(level);
         if (!DimensionGenSavedData.get(level.getServer()).isTowerFloor20BossDefeated()) {
            List<? extends MotherSpiderBossEntity> existingQueens = level.getEntities((EntityTypeTest)ModEntities.MOTHER_SPIDER_BOSS.get(), LivingEntity::isAlive);
            if (existingQueens.isEmpty()) {
               MotherSpiderBossEntity boss = new MotherSpiderBossEntity((EntityType)ModEntities.MOTHER_SPIDER_BOSS.get(), level);
               boss.moveTo(0.5, (double)arenaY + 1.0, 0.5, 0.0F, 0.0F);
               boss.setCustomName(Component.literal("§c§l[Jefe] Reina Araña Madre - Nvl. 150"));
               boss.setCustomNameVisible(true);
               level.addFreshEntity(boss);
               int[][] nestOffsets = new int[][]{{-15, -15}, {15, -15}, {-15, 15}, {15, 15}};

               for (int[] off : nestOffsets) {
                  MotherSpiderEntity minion = new MotherSpiderEntity((EntityType)ModEntities.MOTHER_SPIDER.get(), level);
                  minion.moveTo((double)off[0] + 0.5, (double)arenaY + 1.0, (double)off[1] + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                  level.addFreshEntity(minion);
               }
            }
         }
      }
   }

   static {
      for (int i = 1; i <= 20; i++) {
         TOWER_FLOORS[i - 1] = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("awakening_system", "tower_floor_" + i));
         TOWER_FLOOR_TYPES[i - 1] = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath("awakening_system", "tower_floor_" + i));
      }

      TOWER_FLOOR_1 = TOWER_FLOORS[0];
      TOWER_FLOOR_2 = TOWER_FLOORS[1];
      TOWER_FLOOR_3 = TOWER_FLOORS[2];
      TOWER_FLOOR_4 = TOWER_FLOORS[3];
      TOWER_FLOOR_5 = TOWER_FLOORS[4];
      TOWER_FLOOR_6 = TOWER_FLOORS[5];
      TOWER_FLOOR_7 = TOWER_FLOORS[6];
      TOWER_FLOOR_8 = TOWER_FLOORS[7];
      TOWER_FLOOR_9 = TOWER_FLOORS[8];
      TOWER_FLOOR_10 = TOWER_FLOORS[9];
      TOWER_FLOOR_11 = TOWER_FLOORS[10];
      TOWER_FLOOR_12 = TOWER_FLOORS[11];
      TOWER_FLOOR_13 = TOWER_FLOORS[12];
      TOWER_FLOOR_14 = TOWER_FLOORS[13];
      TOWER_FLOOR_15 = TOWER_FLOORS[14];
      TOWER_FLOOR_16 = TOWER_FLOORS[15];
      TOWER_FLOOR_17 = TOWER_FLOORS[16];
      TOWER_FLOOR_18 = TOWER_FLOORS[17];
      TOWER_FLOOR_19 = TOWER_FLOORS[18];
      TOWER_FLOOR_20 = TOWER_FLOORS[19];
      TOWER_SOLAR = TOWER_FLOOR_1;
      TOWER_LUNAR = TOWER_FLOOR_2;
      TOWER_SOLAR_TYPE = TOWER_FLOOR_TYPES[0];
      TOWER_LUNAR_TYPE = TOWER_FLOOR_TYPES[1];
      NOISE = new TowerSimplexNoise(9823451076241L);
      FLOOR_LAYOUTS = new GreatTowerWorldManager.FloorLayout[]{
         new GreatTowerWorldManager.FloorLayout(-210, -210, Direction.SOUTH, 220, 230, Direction.NORTH, "Llanuras Doradas del Amanecer Celestial"),
         new GreatTowerWorldManager.FloorLayout(0, -240, Direction.NORTH, 30, 240, Direction.SOUTH, "Valle de Niebla Espectral y Arboleda Mística"),
         new GreatTowerWorldManager.FloorLayout(-240, 40, Direction.EAST, 240, -40, Direction.WEST, "Picos Celestiales y Cañones Espirituales"),
         new GreatTowerWorldManager.FloorLayout(-200, 210, Direction.SOUTH, 210, -200, Direction.NORTH, "Arboleda Bioluminiscente del Abismo"),
         new GreatTowerWorldManager.FloorLayout(-160, 160, Direction.SOUTH, 160, -160, Direction.NORTH, "Gran Caverna Profunda de Cristales de Maná"),
         new GreatTowerWorldManager.FloorLayout(-220, -180, Direction.EAST, 190, 210, Direction.WEST, "Bastión Volcánico de Ceniza y Magma"),
         new GreatTowerWorldManager.FloorLayout(220, 40, Direction.WEST, -220, -40, Direction.EAST, "Meseta de la Tormenta Astral y Dunas Arcanas"),
         new GreatTowerWorldManager.FloorLayout(30, 220, Direction.SOUTH, -30, -220, Direction.NORTH, "Picos Glaciares de la Escarcha y Nieve Boreal"),
         new GreatTowerWorldManager.FloorLayout(-180, -180, Direction.SOUTH, 190, 190, Direction.NORTH, "Santuario Sagrado del Flor de Ciruelo y Cerezos"),
         new GreatTowerWorldManager.FloorLayout(-140, 0, Direction.EAST, 140, 0, Direction.WEST, "Bóveda Monumental del Titán Colosal de Sculk [PISO DE JEFE]"),
         new GreatTowerWorldManager.FloorLayout(
            -180, -180, Direction.SOUTH, 180, 180, Direction.NORTH, "Aldea Serena de los Primeros Ascendidos [PISO PACIFICO]"
         ),
         new GreatTowerWorldManager.FloorLayout(-220, 40, Direction.EAST, 220, -40, Direction.WEST, "Meseta Carmesi de las Ruinas de Sangre"),
         new GreatTowerWorldManager.FloorLayout(0, -230, Direction.NORTH, 0, 230, Direction.SOUTH, "Paramo de Cristal Celeste de Teyvat"),
         new GreatTowerWorldManager.FloorLayout(-200, 200, Direction.SOUTH, 200, -200, Direction.NORTH, "Valle Sombrio de las Sombras Arcanas"),
         new GreatTowerWorldManager.FloorLayout(
            -160, -160, Direction.SOUTH, 160, 160, Direction.NORTH, "Caverna Mineral Ancestral de los Diez Reinos [MINERIA]"
         ),
         new GreatTowerWorldManager.FloorLayout(-230, -50, Direction.EAST, 230, 50, Direction.WEST, "Bastion de Obsidiana del Vacio"),
         new GreatTowerWorldManager.FloorLayout(50, -240, Direction.NORTH, -50, 240, Direction.SOUTH, "Cumbres Celestiales del Dao Inmortal"),
         new GreatTowerWorldManager.FloorLayout(-210, -210, Direction.SOUTH, 210, 210, Direction.NORTH, "Templo Dorado de la Calamidad Solar"),
         new GreatTowerWorldManager.FloorLayout(-190, 190, Direction.SOUTH, 190, -190, Direction.NORTH, "Anticamara de las Telaranas Abisales"),
         new GreatTowerWorldManager.FloorLayout(-140, 0, Direction.EAST, 140, 0, Direction.WEST, "Camara Real de la Reina de los Nidos [PISO DE JEFE]")
      };
   }


   public static void populateFloorManaCrystals(ServerLevel level, BlockPos centerPos, int floor) {
      if (level != null && centerPos != null) {
         int attempts = switch (floor) {
            case 5 -> 32;
            case 13 -> 18;
            case 15 -> 55;
            case 19 -> 22;
            case 20 -> 16;
            default -> floor % 2 == 1 ? 8 : 0;
         };
         if (attempts > 0) {
            RandomSource rand = level.random;

            for (int i = 0; i < attempts; i++) {
               double angle = rand.nextDouble() * Math.PI * 2.0;
               double dist = 10.0 + rand.nextDouble() * 45.0;
               int cx = centerPos.getX() + (int)(Math.cos(angle) * dist);
               int cz = centerPos.getZ() + (int)(Math.sin(angle) * dist);
               int cyBase = centerPos.getY() + (rand.nextInt(28) - 10);
               int cy = Math.max(level.getMinBuildHeight() + 3, Math.min(level.getMaxBuildHeight() - 5, cyBase));
               BlockPos checkPos = new BlockPos(cx, cy, cz);
               if (level.getBlockState(checkPos).isAir()) {
                  BlockState crystalState = getRandomManaCrystalState(rand, floor);
                  tryPlaceDirectionalCrystal(level, checkPos, crystalState);
               }
            }

            if (floor == 15 || floor == 13) {
               populateTeyvatOres(level, centerPos, floor, rand);
            }
            populateTerrainDecorations(level, centerPos, floor, rand);
         }
      }
   }

   public static boolean tryPlaceDirectionalCrystal(ServerLevel level, BlockPos pos, BlockState crystalBase) {
      if (!level.getBlockState(pos).isAir() || level.getBlockState(pos).liquid()) {
         return false;
      } else {
         BlockPos below = pos.below();
         BlockState belowState = level.getBlockState(below);
         if (belowState.isFaceSturdy(level, below, Direction.UP) && !belowState.liquid()) {
            level.setBlock(pos, (BlockState)crystalBase.setValue(TeyvatCrystalBlock.FACING, Direction.UP), 2);
            return true;
         } else {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.isFaceSturdy(level, above, Direction.DOWN) && !aboveState.liquid()) {
               level.setBlock(pos, (BlockState)crystalBase.setValue(TeyvatCrystalBlock.FACING, Direction.DOWN), 2);
               return true;
            } else {
               for (Direction dir : Direction.Plane.HORIZONTAL) {
                  BlockPos wall = pos.relative(dir.getOpposite());
                  BlockState wallState = level.getBlockState(wall);
                  if (wallState.isFaceSturdy(level, wall, dir) && !wallState.liquid()) {
                     level.setBlock(pos, (BlockState)crystalBase.setValue(TeyvatCrystalBlock.FACING, dir), 2);
                     return true;
                  }
               }
               return false;
            }
         }
      }
   }

   private static BlockState getRandomManaCrystalState(RandomSource rand, int floor) {
      float r = rand.nextFloat();
      if (floor == 15) {
         // Piso 15: Minería de rango bajo/medio (E abundante, D común, C raro). NUNCA Rangos A ni S.
         if (r < 0.60F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_E.get()).defaultBlockState();
         if (r < 0.90F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_D.get()).defaultBlockState();
         return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get()).defaultBlockState();
      } else if (floor >= 20) {
         // Piso 20 (Cámara de la Reina / Calamidad): B común, A raro, S muy raro (1%)
         if (r < 0.50F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_B.get()).defaultBlockState();
         if (r < 0.85F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_A.get()).defaultBlockState();
         if (r < 0.96F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_A.get()).defaultBlockState();
         return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_S.get()).defaultBlockState();
      } else if (floor >= 18) {
         // Pisos 18-19: C común, B intermedio, A raro (3%), NUNCA Rango S.
         if (r < 0.55F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get()).defaultBlockState();
         if (r < 0.92F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_B.get()).defaultBlockState();
         return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_A.get()).defaultBlockState();
      } else {
         // Otros pisos inferiores
         if (r < 0.65F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_E.get()).defaultBlockState();
         if (r < 0.92F) return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_D.get()).defaultBlockState();
         return ((TeyvatCrystalBlock)SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get()).defaultBlockState();
      }
   }

   private static void populateTeyvatOres(ServerLevel level, BlockPos centerPos, int floor, RandomSource rand) {
      BlockState[] teyvatOres = new BlockState[]{
         ((TeyvatCrystalBlock)SystemBlocks.COR_LAPIS.get()).defaultBlockState(),
         ((TeyvatCrystalBlock)SystemBlocks.NOCTILUCOUS_JADE.get()).defaultBlockState(),
         ((TeyvatCrystalBlock)SystemBlocks.STARSILVER.get()).defaultBlockState(),
         ((TeyvatCrystalBlock)SystemBlocks.CRYSTAL_CHUNK_BLOCK.get()).defaultBlockState(),
         ((TeyvatCrystalBlock)SystemBlocks.MAGICAL_CRYSTAL_BLOCK.get()).defaultBlockState()
      };
      int count = floor == 15 ? 22 : 12;

      for (int i = 0; i < count; i++) {
         double angle = rand.nextDouble() * Math.PI * 2.0;
         double dist = 8.0 + rand.nextDouble() * 45.0;
         int cx = centerPos.getX() + (int)(Math.cos(angle) * dist);
         int cz = centerPos.getZ() + (int)(Math.sin(angle) * dist);
         int cy = centerPos.getY() + (rand.nextInt(26) - 10);
         BlockPos orePos = new BlockPos(cx, cy, cz);
         BlockState selectedOre = teyvatOres[rand.nextInt(teyvatOres.length)];
         tryPlaceDirectionalCrystal(level, orePos, selectedOre);
      }
   }

   private static void populateTerrainDecorations(ServerLevel level, BlockPos centerPos, int floor, RandomSource rand) {
      if (floor == 13 || floor == 5 || floor == 10 || floor == 15 || floor == 16) {
         // Formaciones de rocas y escombros decorativos para pisos montañosos y cavernas
         int rubbleCount = floor == 13 ? 45 : 25;
         Direction[] horiz = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
         for (int i = 0; i < rubbleCount; i++) {
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double dist = 4.0 + rand.nextDouble() * 55.0;
            int cx = centerPos.getX() + (int)(Math.cos(angle) * dist);
            int cz = centerPos.getZ() + (int)(Math.sin(angle) * dist);
            int cy = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
            BlockPos targetPos = new BlockPos(cx, cy, cz);
            if (level.getBlockState(targetPos).isAir()) {
               BlockState rubbleState = SystemBlocks.MOUNTAIN_STONE_RUBBLE.get().defaultBlockState()
                       .setValue(MountainStoneRubbleBlock.FACING, horiz[rand.nextInt(horiz.length)]);
               if (rubbleState.canSurvive(level, targetPos)) {
                  level.setBlock(targetPos, rubbleState, 2);
               }
            }
         }
      }

      if (floor == 2 || floor == 4 || floor == 9 || floor == 11 || floor == 14 || floor == 17) {
         // Matorrales y arbustos silvestres decorativos para pisos de bosque y naturaleza
         int shrubCount = 35;
         for (int i = 0; i < shrubCount; i++) {
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double dist = 4.0 + rand.nextDouble() * 55.0;
            int cx = centerPos.getX() + (int)(Math.cos(angle) * dist);
            int cz = centerPos.getZ() + (int)(Math.sin(angle) * dist);
            int cy = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
            BlockPos targetPos = new BlockPos(cx, cy, cz);
            if (level.getBlockState(targetPos).isAir()) {
               BlockState shrubState = SystemBlocks.FOREST_WILD_SHRUB.get().defaultBlockState();
               if (shrubState.canSurvive(level, targetPos)) {
                  level.setBlock(targetPos, shrubState, 2);
               }
            }
         }
      }
   }

   public static record FloorLayout(int returnX, int returnZ, Direction returnFacing, int advanceX, int advanceZ, Direction advanceFacing, String themeName) {
   }
}
