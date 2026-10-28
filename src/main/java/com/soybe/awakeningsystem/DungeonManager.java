package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.block.DungeonCoreBlock;
import com.soybe.awakeningsystem.block.LockedTreasureChestBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.entity.AbyssalBeholderEntity;
import com.soybe.awakeningsystem.entity.AbyssalNecromancerEntity;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
import com.soybe.awakeningsystem.entity.AntSoldierEntity;
import com.soybe.awakeningsystem.entity.BaranBossEntity;
import com.soybe.awakeningsystem.entity.BeruBossEntity;
import com.soybe.awakeningsystem.entity.FrostElfAssassinEntity;
import com.soybe.awakeningsystem.entity.IgrisBossEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.ShadowInfantryEntity;
import com.soybe.awakeningsystem.guild.Guild;
import com.soybe.awakeningsystem.guild.GuildSavedData;
import com.soybe.awakeningsystem.limbo.LimboWorldManager;
import com.soybe.awakeningsystem.world.SeriesStructureWorldGen;
import com.soybe.awakeningsystem.world.TeyvatStructureManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class DungeonManager {
   public static final ResourceKey<Level> DUNGEON_REALM = ResourceKey.create(
      Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("awakening_system", "dungeon_realm")
   );
   public static final long DEFAULT_RIFT_LIFETIME_TICKS = 720000L;
   public static final Map<UUID, com.soybe.awakeningsystem.DungeonManager.DungeonSession> SESSIONS = new ConcurrentHashMap<>();
   private static final List<com.soybe.awakeningsystem.DungeonManager.WorldRift> WORLD_RIFTS = new CopyOnWriteArrayList<>();
   public static final Map<UUID, Long> EXIT_COOLDOWNS = new ConcurrentHashMap<>();
   private static final Map<BlockPos, Long> RECENT_BREAK_LOCATIONS = new ConcurrentHashMap<>();
   public static final double MIN_NATURAL_RIFT_SEPARATION = 180.0;
   public static final double CLUMPED_RIFT_CLEANUP_DIST = 160.0;
   private static final Set<Long> PROCESSED_EXPLORATION_CHUNKS = ConcurrentHashMap.newKeySet();

   private DungeonManager() {
   }

   public static void clearAll() {
      for (com.soybe.awakeningsystem.DungeonManager.DungeonSession session : SESSIONS.values()) {
         if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
         }
      }
      SESSIONS.clear();
      WORLD_RIFTS.clear();
      EXIT_COOLDOWNS.clear();
      RECENT_BREAK_LOCATIONS.clear();
      PROCESSED_EXPLORATION_CHUNKS.clear();
   }

   public static boolean isTooCloseToExistingRift(ResourceKey<Level> dimension, int x, int z, double minDistance) {
      double minDistSq = minDistance * minDistance;

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
         if (rift.dimension.equals(dimension)) {
            double dx = rift.pos.getX() - x;
            double dz = rift.pos.getZ() - z;
            if (dx * dx + dz * dz < minDistSq) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isValidNaturalGroundForRift(ServerLevel level, BlockPos pos) {
      if (level != null && pos != null) {
         if (level.dimension().equals(Level.OVERWORLD)) {
            if (pos.getY() < 55) {
               return false;
            }

            if (pos.getY() > 140) {
               Holder<Biome> biome = level.getBiome(pos);
               if (!biome.is(BiomeTags.IS_MOUNTAIN)) {
                  return false;
               }

               if (pos.getY() > 195) {
                  return false;
               }
            }
         }

         BlockState below = level.getBlockState(pos.below());
         if (below.isAir() || below.is(Blocks.LAVA) || below.is(Blocks.WATER) || below.liquid()) {
            return false;
         } else if (!below.is(BlockTags.PLANKS)
            && !below.is(BlockTags.WOODEN_SLABS)
            && !below.is(BlockTags.WOODEN_STAIRS)
            && !below.is(BlockTags.WOODEN_FENCES)
            && !below.is(BlockTags.WOODEN_DOORS)
            && !below.is(BlockTags.WOODEN_TRAPDOORS)
            && !below.is(BlockTags.WALLS)
            && !below.is(BlockTags.SLABS)
            && !below.is(BlockTags.STAIRS)
            && !below.is(BlockTags.WOOL)
            && !below.is(BlockTags.SHULKER_BOXES)
            && !below.is(BlockTags.ANVIL)) {
            String blockPath = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
            if (!blockPath.contains("brick")
               && !blockPath.contains("tile")
               && !blockPath.contains("pillar")
               && !blockPath.contains("chiseled")
               && !blockPath.contains("polished")
               && !blockPath.contains("cut_")
               && !blockPath.contains("smooth_")
               && !blockPath.contains("purpur")
               && !blockPath.contains("prismarine")
               && !blockPath.contains("quartz")
               && !blockPath.contains("concrete")
               && !blockPath.contains("glazed")
               && !blockPath.contains("copper")
               && !blockPath.contains("iron_")
               && !blockPath.contains("glass")
               && !blockPath.contains("sculk")) {
               boolean isNatural = below.is(BlockTags.DIRT)
                  || below.is(BlockTags.SAND)
                  || below.is(BlockTags.BASE_STONE_OVERWORLD)
                  || below.is(BlockTags.TERRACOTTA)
                  || below.is(BlockTags.SNOW)
                  || below.is(Blocks.GRAVEL)
                  || below.is(Blocks.CLAY)
                  || below.is(Blocks.MOSS_BLOCK)
                  || below.is(Blocks.MUD)
                  || below.is(Blocks.PACKED_ICE)
                  || below.is(Blocks.ICE)
                  || below.is(Blocks.SNOW_BLOCK);
               if (!isNatural) {
                  return false;
               } else {
                  for (int dy = 0; dy < 3; dy++) {
                     BlockPos check = pos.above(dy);
                     BlockState st = level.getBlockState(check);
                     if (!st.isAir() && st.blocksMotion()) {
                        return false;
                     }
                  }

                  if (level.getServer() != null) {
                     SeriesStructureWorldGen.SeriesStructureData seriesData = SeriesStructureWorldGen.SeriesStructureData.get(level.getServer());
                     if (seriesData.hasStructureNearby(pos, level.dimension(), 55.0)) {
                        return false;
                     }
                  }

                  try {
                     if (level.structureManager() != null && level.structureManager().hasAnyStructureAt(pos)) {
                        return false;
                     }
                  } catch (Exception var8) {
                  }

                  if (level.getServer() != null) {
                     GuildSavedData guildData = GuildSavedData.get(level.getServer());
                     if (guildData != null && guildData.getGuildByChunk(level.dimension().location().toString(), pos.getX() >> 4, pos.getZ() >> 4) != null) {
                        return false;
                     }
                  }

                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void sanitizeRiftsAgainstStructures(MinecraftServer server) {
      if (!WORLD_RIFTS.isEmpty() && server != null) {
         List<com.soybe.awakeningsystem.DungeonManager.WorldRift> invalidRifts = new ArrayList<>();
         SeriesStructureWorldGen.SeriesStructureData seriesData = SeriesStructureWorldGen.SeriesStructureData.get(server);

         for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
            if (rift.activeHostSessionPlayerUUID == null && !rift.isPlayerSummoned && !rift.isPermanent) {
               ServerLevel level = server.getLevel(rift.dimension);
               if (level != null) {
                  if (seriesData != null && seriesData.hasStructureNearby(rift.pos, rift.dimension, 55.0)) {
                     invalidRifts.add(rift);
                  } else if (rift.dimension.equals(Level.OVERWORLD) && rift.pos.getY() > 195) {
                     invalidRifts.add(rift);
                  } else if (level.isLoaded(rift.pos) && !isValidNaturalGroundForRift(level, rift.pos)) {
                     boolean isGuildOwned = rift.owningGuildId != null && !rift.owningGuildId.isEmpty();
                     if (!isGuildOwned || seriesData == null || seriesData.hasStructureNearby(rift.pos, rift.dimension, 50.0)) {
                        invalidRifts.add(rift);
                     }
                  }
               }
            }
         }

         if (!invalidRifts.isEmpty()) {
            WORLD_RIFTS.removeAll(invalidRifts);
            com.soybe.awakeningsystem.DungeonWorldSavedData.get(server).setDirty();
            syncRiftsToAll(server);
            com.soybe.awakeningsystem.AwakeningSystemMod.LOGGER
               .info("[DungeonManager] Se purgaron {} portales dimensionales que se encontraban sobre techos o estructuras.", invalidRifts.size());
         }
      }
   }

   public static void enforceRiftSeparation(MinecraftServer server) {
      if (server != null) {
         sanitizeRiftsAgainstStructures(server);
      }

      if (WORLD_RIFTS.size() > 1) {
         List<com.soybe.awakeningsystem.DungeonManager.WorldRift> protectedRifts = new ArrayList<>();
         List<com.soybe.awakeningsystem.DungeonManager.WorldRift> naturalCandidates = new ArrayList<>();

         for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
            boolean isProtected = rift.activeHostSessionPlayerUUID != null
               || rift.owningGuildId != null && !rift.owningGuildId.isEmpty()
               || rift.isPlayerSummoned
               || rift.isPermanent
               || rift.isRedGate
               || getRankTier(rift.rank) >= 7;
            if (isProtected) {
               protectedRifts.add(rift);
            } else {
               naturalCandidates.add(rift);
            }
         }

         if (!naturalCandidates.isEmpty()) {
            naturalCandidates.sort((a, b) -> {
               double minDistA = Double.MAX_VALUE;
               double minDistB = Double.MAX_VALUE;

               for (com.soybe.awakeningsystem.DungeonManager.WorldRift p : protectedRifts) {
                  if (p.dimension.equals(a.dimension)) {
                     double dxa = p.pos.getX() - a.pos.getX();
                     double dza = p.pos.getZ() - a.pos.getZ();
                     minDistA = Math.min(minDistA, dxa * dxa + dza * dza);
                  }

                  if (p.dimension.equals(b.dimension)) {
                     double dxb = p.pos.getX() - b.pos.getX();
                     double dzb = p.pos.getZ() - b.pos.getZ();
                     minDistB = Math.min(minDistB, dxb * dxb + dzb * dzb);
                  }
               }

               return Double.compare(minDistB, minDistA);
            });
            List<com.soybe.awakeningsystem.DungeonManager.WorldRift> keptNatural = new ArrayList<>();
            List<com.soybe.awakeningsystem.DungeonManager.WorldRift> toRemove = new ArrayList<>();
            double clumpedDistSq = 25600.0;
            double protectedMinDistSq = 1225.0;

            for (com.soybe.awakeningsystem.DungeonManager.WorldRift cand : naturalCandidates) {
               boolean tooClose = false;

               for (com.soybe.awakeningsystem.DungeonManager.WorldRift p : protectedRifts) {
                  if (p.dimension.equals(cand.dimension)) {
                     double dx = p.pos.getX() - cand.pos.getX();
                     double dz = p.pos.getZ() - cand.pos.getZ();
                     if (dx * dx + dz * dz < protectedMinDistSq) {
                        tooClose = true;
                        break;
                     }
                  }
               }

               if (!tooClose) {
                  for (com.soybe.awakeningsystem.DungeonManager.WorldRift k : keptNatural) {
                     if (k.dimension.equals(cand.dimension)) {
                        double dx = k.pos.getX() - cand.pos.getX();
                        double dz = k.pos.getZ() - cand.pos.getZ();
                        if (dx * dx + dz * dz < clumpedDistSq) {
                           tooClose = true;
                           break;
                        }
                     }
                  }
               }

               if (tooClose) {
                  toRemove.add(cand);
               } else {
                  keptNatural.add(cand);
               }
            }

            if (!toRemove.isEmpty()) {
               WORLD_RIFTS.removeAll(toRemove);
               if (server != null) {
                  com.soybe.awakeningsystem.DungeonWorldSavedData.get(server).setDirty();
                  syncRiftsToAll(server);
               }
            }
         }
      }
   }

   public static List<com.soybe.awakeningsystem.DungeonManager.WorldRift> getPermanentRifts() {
      List<com.soybe.awakeningsystem.DungeonManager.WorldRift> result = new ArrayList<>();

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
         if (rift.isPermanent) {
            result.add(rift);
         }
      }

      return result;
   }

   public static com.soybe.awakeningsystem.DungeonManager.WorldRift findNearestPermanentRift(BlockPos pos, ResourceKey<Level> dimension, double maxDist) {
      com.soybe.awakeningsystem.DungeonManager.WorldRift nearest = null;
      double bestDistSq = maxDist * maxDist;

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
         if (rift.isPermanent && rift.dimension.equals(dimension)) {
            double distSq = rift.pos.distSqr(pos);
            if (distSq <= bestDistSq) {
               bestDistSq = distSq;
               nearest = rift;
            }
         }
      }

      return nearest;
   }

   public static com.soybe.awakeningsystem.DungeonManager.WorldRift findNearestRift(BlockPos pos, ResourceKey<Level> dimension, double maxDist) {
      com.soybe.awakeningsystem.DungeonManager.WorldRift nearest = null;
      double bestDistSq = maxDist * maxDist;

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
         if (rift.dimension.equals(dimension)) {
            double distSq = rift.pos.distSqr(pos);
            if (distSq <= bestDistSq) {
               bestDistSq = distSq;
               nearest = rift;
            }
         }
      }

      return nearest;
   }

   public static com.soybe.awakeningsystem.DungeonManager.WorldRift getRiftAt(BlockPos pos, ResourceKey<Level> dimension) {
      for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
         if (rift.dimension.equals(dimension) && rift.pos.equals(pos)) {
            return rift;
         }
      }

      return null;
   }

   public static void registerPermanentRiftDirect(com.soybe.awakeningsystem.DungeonManager.WorldRift rift) {
      for (com.soybe.awakeningsystem.DungeonManager.WorldRift existing : WORLD_RIFTS) {
         if (existing.pos.equals(rift.pos) && existing.dimension.equals(rift.dimension)) {
            return;
         }
      }

      WORLD_RIFTS.add(rift);
   }

   public static boolean isInDungeon(UUID playerId) {
      if (SESSIONS.containsKey(playerId)) {
         return true;
      } else {
         MinecraftServer server = getServer();
         if (server == null) {
            return false;
         } else {
            ServerPlayer sp = server.getPlayerList().getPlayer(playerId);
            if (sp == null) {
               return false;
            } else if (!sp.level().dimension().equals(DUNGEON_REALM)) {
               return false;
            } else {
               for (com.soybe.awakeningsystem.DungeonManager.DungeonSession session : SESSIONS.values()) {
                  if (session.dungeonDimension.equals(sp.level().dimension())) {
                     double distSq = sp.distanceToSqr(session.arenaCenter.getX() + 0.5, sp.getY(), session.arenaCenter.getZ() + 0.5);
                     double chestDistSq = session.chestPos != null
                        ? sp.distanceToSqr(session.chestPos.getX() + 0.5, sp.getY(), session.chestPos.getZ() + 0.5)
                        : Double.MAX_VALUE;
                     if (distSq <= 40000.0 || chestDistSq <= 10000.0) {
                        session.coopPlayers.add(playerId);
                        SESSIONS.put(playerId, session);
                        if (session.bossEvent != null) {
                           session.bossEvent.addPlayer(sp);
                        }

                        return true;
                     }
                  }
               }

               return false;
            }
         }
      }
   }

   public static com.soybe.awakeningsystem.DungeonManager.DungeonSession getSession(UUID playerId) {
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session = SESSIONS.get(playerId);
      if (session != null) {
         return session;
      } else {
         MinecraftServer server = getServer();
         if (server == null) {
            return null;
         } else {
            ServerPlayer sp = server.getPlayerList().getPlayer(playerId);
            if (sp == null) {
               return null;
            } else {
               for (com.soybe.awakeningsystem.DungeonManager.DungeonSession s : SESSIONS.values()) {
                  if (s.dungeonDimension.equals(sp.level().dimension())) {
                     double distSq = sp.distanceToSqr(s.arenaCenter.getX() + 0.5, sp.getY(), s.arenaCenter.getZ() + 0.5);
                     double chestDistSq = s.chestPos != null ? sp.distanceToSqr(s.chestPos.getX() + 0.5, sp.getY(), s.chestPos.getZ() + 0.5) : Double.MAX_VALUE;
                     if (distSq <= 40000.0 || chestDistSq <= 10000.0) {
                        s.coopPlayers.add(playerId);
                        SESSIONS.put(playerId, s);
                        if (s.bossEvent != null) {
                           s.bossEvent.addPlayer(sp);
                        }

                        return s;
                     }
                  }
               }

               return null;
            }
         }
      }
   }

   public static com.soybe.awakeningsystem.DungeonManager.DungeonSession getSessionByChestPos(BlockPos pos) {
      for (com.soybe.awakeningsystem.DungeonManager.DungeonSession session : SESSIONS.values()) {
         if (session.chestPos != null && session.chestPos.equals(pos)) {
            return session;
         }
      }

      return null;
   }

   public static com.soybe.awakeningsystem.DungeonManager.DungeonSession getSessionAt(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         for (com.soybe.awakeningsystem.DungeonManager.DungeonSession session : SESSIONS.values()) {
            if (session.bunkerBoundsCalculated) {
               if (pos.getX() >= session.bunkerMinX && pos.getX() <= session.bunkerMaxX && pos.getZ() >= session.bunkerMinZ && pos.getZ() <= session.bunkerMaxZ
                  )
                {
                  return session;
               }
            } else if (session.arenaCenter != null
               && Math.abs(pos.getX() - session.arenaCenter.getX()) <= 200
               && Math.abs(pos.getZ() - session.arenaCenter.getZ()) <= 400) {
               return session;
            }
         }

         return null;
      }
   }

   public static int getRankBaseLevel(String rank) {
      int tier = getRankTier(rank);

      return switch (tier) {
         case 2 -> 8;
         case 3 -> 15;
         case 4 -> 25;
         case 5 -> 40;
         case 6 -> 55;
         case 7 -> 70;
         case 8 -> 80;
         case 9 -> 90;
         case 10 -> 100;
         case 11 -> 120;
         case 12 -> 135;
         case 13 -> 150;
         default -> 3;
      };
   }

   public static String getRankLevelColor(int level) {
      if (level < 20) {
         return "§a";
      } else if (level < 40) {
         return "§e";
      } else if (level < 60) {
         return "§6";
      } else if (level < 80) {
         return "§c";
      } else {
         return level < 100 ? "§4§l" : "§d§l";
      }
   }

   public static void applyDungeonMobMetadata(Monster mob, String rank, int floor, boolean isBoss) {
      if (mob != null) {
         int baseLvl = getRankBaseLevel(rank);
         int mobLevel = isBoss ? baseLvl + 6 : baseLvl + Math.max(0, floor - 1) * 2;
         mob.addTag("dungeon_mob");
         mob.addTag("rpg_scaled");
         mob.getPersistentData().putInt("rpg_mob_level", mobLevel);
         mob.getPersistentData().putString("dungeon_rank", rank != null ? rank : "D");
         mob.getPersistentData().putInt("dungeon_floor", floor);
         String color = getRankLevelColor(mobLevel);
         Component currentName = mob.getCustomName();
         if (currentName == null) {
            currentName = mob.getType().getDescription();
         }

         String nameStr = currentName.getString();
         if (!nameStr.contains("[Nv.")) {
            Component formattedName = Component.literal(color + "[Nv. " + mobLevel + "] §r").append(currentName);
            mob.setCustomName(formattedName);
         }

         mob.setCustomNameVisible(true);
      }
   }

   public static BlockPos getCurrentRoomCenter(com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      if (session == null) {
         return null;
      } else {
         return switch (session.currentFloor) {
            case 2 -> session.room2Center != null ? session.room2Center : session.arenaCenter;
            case 3 -> session.room3Center != null ? session.room3Center : session.arenaCenter;
            case 4 -> session.room4Center != null ? session.room4Center : session.arenaCenter;
            case 5 -> session.room5Center != null ? session.room5Center : session.arenaCenter;
            default -> session.arenaCenter;
         };
      }
   }

   public static boolean isPlayerOutOfBounds(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, ServerPlayer player) {
      if (session != null && player != null) {
         if (!session.bunkerBoundsCalculated) {
            session.calculateBunkerBounds();
         }

         double x = player.getX();
         double y = player.getY();
         double z = player.getZ();
         if (!(y < session.bunkerMinY - 2.0) && !(y < -50.0)) {
            if (x <= session.bunkerMinX + 1.0 || x >= session.bunkerMaxX - 1.0) {
               return true;
            } else if (z <= session.bunkerMinZ + 1.0 || z >= session.bunkerMaxZ - 1.0) {
               return true;
            } else if (y >= session.bunkerMaxY + 2.0) {
               return true;
            } else {
               BlockPos headPos = BlockPos.containing(x, y + 1.0, z);
               BlockPos feetPos = BlockPos.containing(x, y, z);
               return player.level().getBlockState(headPos).is(Blocks.BEDROCK) || player.level().getBlockState(feetPos).is(Blocks.BEDROCK);
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isTargetOutOfBounds(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, double x, double y, double z) {
      if (session == null) {
         return false;
      } else {
         if (!session.bunkerBoundsCalculated) {
            session.calculateBunkerBounds();
         }

         if (y < session.bunkerMinY - 2.0 || y > session.bunkerMaxY + 2.0) {
            return true;
         } else {
            return x <= session.bunkerMinX + 1.5 || x >= session.bunkerMaxX - 1.5 ? true : z <= session.bunkerMinZ + 1.5 || z >= session.bunkerMaxZ - 1.5;
         }
      }
   }

   public static void rescuePlayerToBounds(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, ServerPlayer player) {
      if (session != null && player != null) {
         MinecraftServer server = player.getServer();
         if (server != null) {
            ServerLevel level = server.getLevel(session.dungeonDimension);
            if (level == null) {
               level = player.serverLevel();
            }

            BlockPos center = getCurrentRoomCenter(session);
            if (center == null) {
               center = session.arenaCenter;
            }

            double safeY = findSafeFloorY(level, center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.fallDistance = 0.0F;
            player.teleportTo(level, center.getX() + 0.5, safeY, center.getZ() + 0.5, player.getYRot(), player.getXRot());
            player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("awakening_system.dungeon.out_of_bounds_rescued"), true);
         }
      }
   }

   public static void rescueStrandedPlayer(ServerPlayer player) {
      if (player != null) {
         MinecraftServer server = player.getServer();
         if (server != null) {
            ResourceKey<Level> respawnDim = player.getRespawnDimension();
            ServerLevel returnLevel = respawnDim != null
                  && !respawnDim.equals(DUNGEON_REALM)
                  && !respawnDim.equals(com.soybe.awakeningsystem.TowerManager.RANKER_TOWER)
                  && !respawnDim.equals(com.soybe.awakeningsystem.PagodaTrialManager.PAGODA_REALM)
               ? server.getLevel(respawnDim)
               : server.overworld();
            if (returnLevel == null) {
               returnLevel = server.overworld();
            }

            BlockPos respawnPos = player.getRespawnPosition();
            float yaw = player.getYRot();
            Vec3 targetVec;
            if (respawnPos != null) {
               targetVec = new Vec3(respawnPos.getX() + 0.5, respawnPos.getY() + 0.1, respawnPos.getZ() + 0.5);
            } else {
               BlockPos shared = returnLevel.getSharedSpawnPos();
               targetVec = new Vec3(shared.getX() + 0.5, shared.getY() + 0.1, shared.getZ() + 0.5);
            }

            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.fallDistance = 0.0F;
            player.teleportTo(returnLevel, targetVec.x, targetVec.y, targetVec.z, yaw, 0.0F);
            player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.9F, 1.0F);
            player.displayClientMessage(Component.translatable("awakening_system.dungeon.stranded_rescued"), false);
         }
      }
   }

   public static void onPlayerLogin(ServerPlayer player) {
      if (player.level().dimension().equals(DUNGEON_REALM) && !isInDungeon(player.getUUID()) && !player.isSpectator()) {
         rescueStrandedPlayer(player);
      }
   }

   public static int getRankTier(String rank) {
      if (rank != null && !rank.isEmpty()) {
         String var1 = rank.toUpperCase(Locale.ROOT);

         return switch (var1) {
            case "ABSOLUTE_GOD", "LIMITLESS", "SINGULARITY", "ASTRAL_DEITY", "TIMELESS_MONARCH", "CHAOS_SOVEREIGN" -> 13;
            case "ABSOLUTE_CREATOR", "SUPREME_CREATOR", "ABSOLUTE", "ABSOLUTE_BEING", "OMNIPOTENT", "INFINITE" -> 12;
            case "PRIMORDIAL", "OMNIVERSAL", "MULTIVERSAL", "COSMIC", "ANCESTRAL", "DEITY", "GOD", "DEMIGOD" -> 11;
            case "SUPREME_BEING", "RULER", "MONARCH" -> 10;
            case "TRANSCENDENTAL", "TRANSCENDENT", "GR", "MR", "SLR", "LR", "UR+", "UR", "NATIONAL" -> 9;
            case "SSS", "SS", "EX" -> 8;
            case "S" -> 7;
            case "A" -> 6;
            case "B" -> 5;
            case "C" -> 4;
            case "D" -> 3;
            case "E" -> 2;
            default -> 1;
         };
      } else {
         return 3;
      }
   }

   public static String getRepresentativeRankForTier(int tier) {
      return switch (tier) {
         case 2 -> "E";
         case 3 -> "D";
         case 4 -> "C";
         case 5 -> "B";
         case 6 -> "A";
         case 7 -> "S";
         case 8 -> "SS";
         case 9 -> "UR";
         case 10 -> "MONARCH";
         case 11 -> "GOD";
         case 12 -> "ABSOLUTE";
         case 13 -> "ABSOLUTE_GOD";
         default -> "F";
      };
   }

   public static BossBarColor getGateBossBarColor(String rank, boolean isRedGate) {
      if (isRedGate) {
         return BossBarColor.RED;
      } else {
         int tier = getRankTier(rank);
         if (tier >= 12) {
            return BossBarColor.YELLOW;
         } else if (tier >= 10) {
            return BossBarColor.PURPLE;
         } else if (tier >= 7) {
            return BossBarColor.RED;
         } else if (tier == 6) {
            return BossBarColor.YELLOW;
         } else {
            return tier >= 4 ? BossBarColor.GREEN : BossBarColor.BLUE;
         }
      }
   }

   public static Component getGatePrefix(String rank, boolean isRedGate) {
      if (isRedGate) {
         return Component.translatable("awakening_system.dungeon.gate.red_gate_prefix", new Object[]{rank});
      } else {
         int tier = getRankTier(rank);
         if (tier >= 12) {
            return Component.translatable("awakening_system.dungeon.gate.celestial_prefix", new Object[]{rank});
         } else if (tier >= 10) {
            return Component.translatable("awakening_system.dungeon.gate.divine_prefix", new Object[]{rank});
         } else if (tier >= 8) {
            return Component.translatable("awakening_system.dungeon.gate.abyssal_prefix", new Object[]{rank});
         } else if (tier == 7) {
            return Component.translatable("awakening_system.dungeon.gate.crimson_prefix", new Object[]{rank});
         } else if (tier == 6) {
            return Component.translatable("awakening_system.dungeon.gate.amber_prefix", new Object[]{rank});
         } else {
            return tier >= 4
               ? Component.translatable("awakening_system.dungeon.gate.emerald_prefix", new Object[]{rank})
               : Component.translatable("awakening_system.dungeon.gate.default_prefix", new Object[]{rank});
         }
      }
   }

   public static Component getGateTitle(String rank, boolean isRedGate) {
      if (isRedGate) {
         return Component.translatable("awakening_system.dungeon.gate.title.red_gate", new Object[]{rank});
      } else {
         int tier = getRankTier(rank);
         if (tier >= 12) {
            return Component.translatable("awakening_system.dungeon.gate.title.celestial", new Object[]{rank});
         } else if (tier >= 10) {
            return Component.translatable("awakening_system.dungeon.gate.title.divine", new Object[]{rank});
         } else if (tier >= 8) {
            return Component.translatable("awakening_system.dungeon.gate.title.abyssal", new Object[]{rank});
         } else if (tier == 7) {
            return Component.translatable("awakening_system.dungeon.gate.title.crimson", new Object[]{rank});
         } else if (tier == 6) {
            return Component.translatable("awakening_system.dungeon.gate.title.amber", new Object[]{rank});
         } else {
            return tier >= 4
               ? Component.translatable("awakening_system.dungeon.gate.title.emerald", new Object[]{rank})
               : Component.translatable("awakening_system.dungeon.gate.title.default", new Object[]{rank});
         }
      }
   }

   public static Component formatPhaseBossName(String rank, boolean isRedGate, int floor, int enemies, String timeStr) {
      return floor == 5
         ? getGatePrefix(rank, isRedGate).copy().append(Component.translatable("awakening_system.dungeon.boss_bar.monarch"))
         : getGatePrefix(rank, isRedGate).copy().append(Component.translatable("awakening_system.dungeon.boss_bar.enemies", new Object[]{enemies}));
   }

   public static int getPhase5BossTimer(int tier, boolean isRedGate) {
      int baseTicks = switch (tier) {
         case 2 -> 5400;
         case 3 -> 6000;
         case 4 -> 6600;
         case 5 -> 7200;
         case 6 -> 7800;
         case 7 -> 8400;
         case 8 -> 9000;
         case 9 -> 9600;
         case 10 -> 10200;
         case 11 -> 10800;
         case 12 -> 11400;
         case 13 -> 12000;
         default -> 4800;
      };
      return isRedGate ? (int)(baseTicks * 1.2) : baseTicks;
   }

   public static int getFloorTimer(String rank, int floor) {
      int tier = getRankTier(rank);
      if (floor >= 5) {
         return getPhase5BossTimer(tier, false);
      } else {
         int base = floor == 2 ? 1800 : (floor == 3 ? 2400 : (floor == 4 ? 3000 : 1800));
         if (tier >= 9) {
            base += (tier - 8) * 600;
         }

         return base;
      }
   }

   public static void startDungeon(ServerPlayer player, String rank) {
      startDungeon(player, rank, "crypt", false, null);
   }

   public static void startDungeon(ServerPlayer player, String rank, String biome) {
      startDungeon(player, rank, biome, false, null);
   }

   public static void startDungeon(ServerPlayer player, String rank, String biome, boolean isRedGate) {
      startDungeon(player, rank, biome, isRedGate, null);
   }

   public static void startDungeon(
      ServerPlayer player, String rank, String biome, boolean isRedGate, com.soybe.awakeningsystem.DungeonManager.WorldRift sourceRift
   ) {
      if (isInDungeon(player.getUUID())) {
         player.displayClientMessage(Component.translatable("awakening_system.dungeon.already_in_dungeon"), true);
      } else {
         String r = rank != null && !rank.isEmpty() ? rank.toUpperCase() : "D";
         String b = biome != null && !biome.isEmpty() ? biome : "crypt";
         int tier = getRankTier(r);
         com.soybe.awakeningsystem.SystemData pData = com.soybe.awakeningsystem.SystemEvents.data(player);
         int playerTier = getRankTier(com.soybe.awakeningsystem.SystemData.rankForLevel(pData.level()));
         int minRequiredTier = Math.max(1, tier - 1);
         if (playerTier < minRequiredTier) {
            String minRankName = getRepresentativeRankForTier(minRequiredTier);
            player.displayClientMessage(Component.translatable("dungeon.awakening_system.excessive_pressure", new Object[]{minRankName}), true);
         } else {
            if (!isRedGate && tier >= 5 && player.getRandom().nextFloat() < 0.15F) {
               isRedGate = true;
               com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                  player, "awakening_system.notification.red_gate.title", "awakening_system.notification.red_gate.message", -65536
               );
               player.displayClientMessage(Component.translatable("awakening_system.notification.red_gate.message"), true);
               player.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 0.6F);
            }

            MinecraftServer server = player.getServer();
            if (server != null) {
               ServerLevel dungeonLevel = server.getLevel(DUNGEON_REALM);
               if (dungeonLevel == null) {
                  dungeonLevel = server.getLevel(Level.NETHER);
                  if (dungeonLevel == null) {
                     dungeonLevel = server.getLevel(Level.OVERWORLD);
                  }
               }

               if (dungeonLevel != null) {
                  ResourceKey<Level> dimKey = dungeonLevel.dimension();
                  int runId = com.soybe.awakeningsystem.DungeonWorldSavedData.get(server).getNextRunIndexAndIncrement();
                  int gridCol = runId % 1000;
                  int gridRow = runId / 1000;
                  int arenaX = 10000 + gridCol * 2000;
                  int arenaY = 60;
                  int arenaZ = 10000 + gridRow * 2000;
                  BlockPos center = new BlockPos(arenaX, arenaY, arenaZ);
                  purgeDungeonVolume(dungeonLevel, center);
                  int maxFloors = 5;
                  com.soybe.awakeningsystem.DungeonManager.DungeonSession session = new com.soybe.awakeningsystem.DungeonManager.DungeonSession(
                     player.getUUID(),
                     r,
                     b,
                     player.level().dimension(),
                     player.position(),
                     player.getYRot(),
                     player.getXRot(),
                     dimKey,
                     center,
                     maxFloors,
                     isRedGate
                  );
                  session.sourceRift = sourceRift;
                  if (sourceRift != null) {
                     sourceRift.activeHostSessionPlayerUUID = player.getUUID();
                  }

                  String theme = b != null && !b.isEmpty() ? b.toLowerCase(Locale.ROOT) : "";
                  if (theme.isEmpty() || "random".equals(theme)) {
                     if (isRedGate) {
                        int rollTheme = player.getRandom().nextInt(3);
                        theme = rollTheme == 0 ? "baran" : (rollTheme == 1 ? "beru" : "igris");
                     } else if (tier >= 10) {
                        int rollTheme = player.getRandom().nextInt(3);
                        theme = rollTheme == 0 ? "baran" : (rollTheme == 1 ? "beru" : "igris");
                     } else if (tier >= 7) {
                        theme = player.getRandom().nextBoolean() ? "beru" : "igris";
                     } else {
                        theme = "crypt";
                     }
                  }

                  buildContinuousDungeon(dungeonLevel, session, center, r, theme);
                  BlockPos spawnPos = session.portalRoomPos != null ? session.portalRoomPos : center.offset(0, 0, -8);
                  ensureSolidSpawnPlatform(dungeonLevel, session, spawnPos);
                  BlockPos exitFloorPos = session.portalRoomPos != null ? session.portalRoomPos.offset(0, 0, 6) : spawnPos.offset(0, 0, 6);
                  session.exitPortalPos = exitFloorPos;
                  buildExitPortal(dungeonLevel, session, exitFloorPos, isRedGate);
                  player.setDeltaMovement(0.0, 0.0, 0.0);
                  player.fallDistance = 0.0F;
                  player.teleportTo(dungeonLevel, spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                  dungeonLevel.playSound(
                     null,
                     spawnPos.getX(),
                     spawnPos.getY() + 1,
                     spawnPos.getZ(),
                     (SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_GATE_OPEN.value(),
                     SoundSource.PLAYERS,
                     1.3F,
                     1.0F
                  );
                  dungeonLevel.sendParticles(ParticleTypes.PORTAL, spawnPos.getX() + 0.5, spawnPos.getY() + 1.5, spawnPos.getZ() + 0.5, 120, 3.0, 1.5, 3.0, 0.5);
                  session.combatTimer = 30000;
                  session.maxCombatTimer = session.combatTimer;
                  session.combatActive = true;
                  int secRemaining = (session.combatTimer + 19) / 20;
                  int mins = secRemaining / 60;
                  int secs = secRemaining % 60;
                  String timeStr = String.format("%02d:%02d", mins, secs);
                  ServerBossEvent bossEvent = new ServerBossEvent(
                     formatPhaseBossName(r, isRedGate, 1, session.totalEnemies, timeStr), getGateBossBarColor(r, isRedGate), BossBarOverlay.PROGRESS
                  );
                  bossEvent.setProgress(1.0F);
                  bossEvent.addPlayer(player);
                  session.bossEvent = bossEvent;
                  SESSIONS.put(player.getUUID(), session);
                  int notifyCol = isRedGate ? -52429 : (tier >= 12 ? -10496 : (tier >= 10 ? -3713537 : -9377537));
                  com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                     player, "awakening_system.notification.dungeon_gate.title", "awakening_system.notification.dungeon_gate.message", notifyCol, r
                  );
                  com.soybe.awakeningsystem.SystemEvents.triggerConstellationReaction(
                     player, com.soybe.awakeningsystem.ConstellationDefinitions.ReactionType.DUNGEON_ENTER
                  );
                  syncRiftsToPlayer(player);
               }
            }
         }
      }
   }

   public static void joinDungeonCoop(ServerPlayer coopPlayer, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      MinecraftServer server = coopPlayer.getServer();
      if (server != null && session != null) {
         ServerLevel dungeonLevel = server.getLevel(session.dungeonDimension);
         if (dungeonLevel != null) {
            com.soybe.awakeningsystem.SystemData pData = com.soybe.awakeningsystem.SystemEvents.data(coopPlayer);
            int playerTier = getRankTier(com.soybe.awakeningsystem.SystemData.rankForLevel(pData.level()));
            int dungeonTier = getRankTier(session.rank);
            int minRequiredTier = Math.max(1, dungeonTier - 1);
            if (playerTier < minRequiredTier) {
               String minRankName = getRepresentativeRankForTier(minRequiredTier);
               coopPlayer.displayClientMessage(Component.translatable("dungeon.awakening_system.coop_excessive_pressure", new Object[]{minRankName}), true);
            } else {
               session.coopPlayers.add(coopPlayer.getUUID());
               SESSIONS.put(coopPlayer.getUUID(), session);
               BlockPos spawnPos = session.portalRoomPos != null ? session.portalRoomPos.offset(0, 1, 0) : session.arenaCenter.offset(0, 1, -8);
               coopPlayer.setDeltaMovement(0.0, 0.0, 0.0);
               coopPlayer.fallDistance = 0.0F;
               coopPlayer.teleportTo(dungeonLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 180.0F, 0.0F);
               coopPlayer.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
               com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                  coopPlayer, "awakening_system.notification.dungeon_coop.title", "awakening_system.notification.dungeon_coop.joined", -16722689
               );
               ServerPlayer hostPlayer = server.getPlayerList().getPlayer(session.playerId);
               if (hostPlayer != null) {
                  hostPlayer.displayClientMessage(
                     Component.translatable("awakening_system.dungeon.coop_ally_arrived", new Object[]{coopPlayer.getName().getString()}), false
                  );
                  hostPlayer.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 1.2F);
               }

               if (session.bossEvent != null) {
                  session.bossEvent.addPlayer(coopPlayer);
               }
            }
         }
      }
   }

   private static void ensureSolidSpawnPlatform(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos spawnPos) {
      for (int sx = -3; sx <= 3; sx++) {
         for (int sz = -3; sz <= 3; sz++) {
            for (int sy = -3; sy <= 0; sy++) {
               BlockPos bp = spawnPos.offset(sx, sy, sz);
               level.setBlock(bp, Blocks.DEEPSLATE.defaultBlockState(), 2);
               session.arenaBlocks.add(bp);
            }

            for (int cy = 1; cy <= 4; cy++) {
               BlockPos airP = spawnPos.offset(sx, cy, sz);
               level.setBlock(airP, Blocks.AIR.defaultBlockState(), 2);
               session.arenaBlocks.add(airP);
            }
         }
      }
   }

   public static void buildExitPortal(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos portalPos, boolean isRedGate) {
      String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
      boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme);
      BlockState baseBlock = isBaran ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
      BlockState pillarBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
      BlockState stairsBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState();
      BlockState floorTile = isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
      BlockState runeBlock = isRedGate
         ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
         : (isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState());

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            for (int dy = -3; dy <= 0; dy++) {
               BlockPos p = portalPos.offset(dx, dy, dz);
               level.setBlock(p, dy == 0 ? floorTile : pillarBlock, 2);
               session.arenaBlocks.add(p);
            }
         }
      }

      for (int dy = 1; dy <= 5; dy++) {
         BlockPos leftCol = portalPos.offset(-2, dy, 0);
         BlockPos rightCol = portalPos.offset(2, dy, 0);
         BlockState st = dy != 1 && dy != 5 ? pillarBlock : baseBlock;
         level.setBlock(leftCol, st, 2);
         level.setBlock(rightCol, st, 2);
         session.arenaBlocks.add(leftCol);
         session.arenaBlocks.add(rightCol);
      }

      BlockPos archL = portalPos.offset(-1, 6, 0);
      BlockPos archM = portalPos.offset(0, 6, 0);
      BlockPos archR = portalPos.offset(1, 6, 0);
      level.setBlock(archL, stairsBlock, 2);
      level.setBlock(archM, runeBlock, 2);
      level.setBlock(archR, stairsBlock, 2);
      session.arenaBlocks.add(archL);
      session.arenaBlocks.add(archM);
      session.arenaBlocks.add(archR);

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = 1; dy <= 5; dy++) {
            BlockPos backP = portalPos.offset(dx, dy, 1);
            level.setBlock(backP, runeBlock, 2);
            session.arenaBlocks.add(backP);
         }
      }

      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -4; dz <= 0; dz++) {
            for (int dy = 1; dy <= 5; dy++) {
               if (dz != 0 || Math.abs(dx) != 2) {
                  BlockPos airP = portalPos.offset(dx, dy, dz);
                  level.setBlock(airP, Blocks.AIR.defaultBlockState(), 2);
                  session.arenaBlocks.add(airP);
               }
            }
         }
      }
   }

   public static double getDungeonBossHp(int tier, boolean isRedGate) {
      double baseHp = switch (tier) {
         case 2 -> 300.0;
         case 3 -> 500.0;
         case 4 -> 850.0;
         case 5 -> 1400.0;
         case 6 -> 2200.0;
         case 7 -> 3500.0;
         case 8 -> 5000.0;
         case 9 -> 6800.0;
         case 10 -> 9000.0;
         case 11 -> 12000.0;
         case 12 -> 16000.0;
         case 13 -> 22000.0;
         default -> 180.0;
      };
      return isRedGate ? baseHp * 1.35 : baseHp;
   }

   public static double getDungeonBossDmg(int tier, boolean isRedGate) {
      double baseDmg = switch (tier) {
         case 2 -> 5.5;
         case 3 -> 8.0;
         case 4 -> 11.0;
         case 5 -> 15.0;
         case 6 -> 20.0;
         case 7 -> 26.0;
         case 8 -> 32.0;
         case 9 -> 38.0;
         case 10 -> 46.0;
         case 11 -> 56.0;
         case 12 -> 68.0;
         case 13 -> 80.0;
         default -> 4.0;
      };
      return isRedGate ? baseDmg * 1.25 : baseDmg;
   }

   private static Monster createFinalBoss(ServerLevel level, String rank) {
      return createFinalBoss(level, rank, false);
   }

   private static Monster createFinalBoss(ServerLevel level, String rank, boolean isRedGate) {
      int tier = getRankTier(rank);
      double hp = getDungeonBossHp(tier, isRedGate);
      double dmg = getDungeonBossDmg(tier, isRedGate);
      double armor = Math.min(28.0, 8.0 + tier * 1.6);
      double kbRes = Math.min(0.95, 0.35 + tier * 0.05);
      Component prefixComp = isRedGate
         ? Component.translatable("awakening_system.dungeon.mob.anomaly_prefix", new Object[]{rank})
         : (
            tier >= 12
               ? Component.translatable("awakening_system.dungeon.mob.divine_boss_prefix", new Object[]{rank})
               : Component.translatable("awakening_system.dungeon.mob.boss_prefix", new Object[]{rank})
         );
      int bossRoll = level.random.nextInt(3);
      Monster boss;
      if (bossRoll == 0) {
         BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
         b.setCustomName(prefixComp.copy().append(Component.translatable("awakening_system.dungeon.boss.baran_flames")));
         b.setCustomNameVisible(true);
         buffMob(b, hp, dmg, armor, kbRes);
         boss = b;
      } else if (bossRoll == 1) {
         IgrisBossEntity igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
         igris.setCustomName(prefixComp.copy().append(Component.translatable("awakening_system.dungeon.boss.igris_commander")));
         igris.setCustomNameVisible(true);
         buffMob(igris, hp, dmg, armor, kbRes);
         boss = igris;
      } else {
         BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), level);
         beru.setCustomName(prefixComp.copy().append(Component.translatable("awakening_system.dungeon.boss.beru_predator")));
         beru.setCustomNameVisible(true);
         buffMob(beru, hp, dmg, armor, kbRes);
         boss = beru;
      }

      boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, tier >= 12 ? 3 : (tier >= 9 ? 2 : (tier >= 5 ? 1 : 0))));
      boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, tier >= 12 ? 3 : (tier >= 10 ? 2 : (tier >= 6 ? 1 : 0))));
      boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, tier >= 12 ? 2 : 1));
      boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 99999, 0));
      boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, 99999, 0));
      return boss;
   }

   private static Monster createFloorMinion(ServerLevel level, String rank, int floor, int index) {
      if (floor == 2 && index == 0) {
         return createFloor2Elite(level, rank);
      } else {
         int tier = getRankTier(rank);
         double hpMult = 1.0 + Math.max(0, tier - 3) * 0.35;
         double dmgMult = 1.0 + Math.max(0, tier - 3) * 0.28;
         if (tier >= 7) {
            hpMult = 1.0 + (tier - 7) * 0.5;
            dmgMult = 1.0 + (tier - 7) * 0.38;
            if (floor == 1) {
               if (index % 2 == 0) {
                  ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                  infantry.setCustomName(
                     Component.translatable(
                        tier >= 12 ? "awakening_system.dungeon.mob.deicide_shadow_infantry" : "awakening_system.dungeon.mob.vanguard_shadow_infantry"
                     )
                  );
                  buffMob(infantry, 220.0 * hpMult, 14.0 * dmgMult, Math.min(22.0, 10.0 + (tier - 7) * 1.5), 0.45);
                  return infantry;
               } else {
                  ShadowInfantryEntity inf = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                  inf.setCustomName(
                     Component.translatable(
                        tier >= 12 ? "awakening_system.dungeon.mob.abyssal_blood_knight" : "awakening_system.dungeon.mob.vanguard_crimson_knight"
                     )
                  );
                  inf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                  buffMob(inf, 200.0 * hpMult, 13.0 * dmgMult, Math.min(20.0, 9.0 + (tier - 7) * 1.5), 0.35);
                  return inf;
               }
            } else if (floor == 2) {
               if (index % 2 == 0) {
                  FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
                  assassin.setCustomName(
                     Component.translatable(
                        tier >= 12 ? "awakening_system.dungeon.mob.supreme_spectral_elf_assassin" : "awakening_system.dungeon.mob.frost_elf_assassin"
                     )
                  );
                  buffMob(assassin, 240.0 * hpMult, 19.0 * dmgMult, Math.min(22.0, 12.0 + (tier - 7) * 1.5), 0.5);
                  return assassin;
               } else {
                  ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                  infantry.setCustomName(
                     Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.void_shadow_stalker" : "awakening_system.dungeon.mob.shadow_stalker")
                  );
                  buffMob(infantry, 230.0 * hpMult, 15.0 * dmgMult, Math.min(20.0, 10.0 + (tier - 7) * 1.5), 0.45);
                  return infantry;
               }
            } else if (floor == 3) {
               if (index % 2 == 0) {
                  AbyssalNecromancerEntity necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
                  necro.setCustomName(
                     Component.translatable(
                        tier >= 12 ? "awakening_system.dungeon.mob.dark_celestial_necromancer" : "awakening_system.dungeon.mob.dark_abyssal_necromancer"
                     )
                  );
                  buffMob(necro, 270.0 * hpMult, 16.0 * dmgMult, Math.min(22.0, 12.0 + (tier - 7) * 1.5), 0.5);
                  return necro;
               } else {
                  AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                  orc.setCustomName(
                     Component.translatable(
                        tier >= 12 ? "awakening_system.dungeon.mob.deicide_orc_warrior" : "awakening_system.dungeon.mob.abyssal_orc_warrior"
                     )
                  );
                  buffMob(orc, 350.0 * hpMult, 18.0 * dmgMult, Math.min(24.0, 14.0 + (tier - 7) * 1.5), 0.6);
                  return orc;
               }
            } else if (index % 2 == 0) {
               AntSoldierEntity ant = new AntSoldierEntity(ModEntities.ANT_SOLDIER.get(), level);
               ant.setCustomName(
                  Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.deicide_chimera_ant" : "awakening_system.dungeon.mob.black_swarm_ant")
               );
               buffMob(ant, 300.0 * hpMult, 19.0 * dmgMult, Math.min(25.0, 14.0 + (tier - 7) * 1.5), 0.55);
               return ant;
            } else {
               AbyssalBeholderEntity beholder = new AbyssalBeholderEntity(ModEntities.ABYSSAL_BEHOLDER.get(), level);
               beholder.setCustomName(
                  Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.deicide_void_eye" : "awakening_system.dungeon.mob.primordial_abyssal_eye")
               );
               buffMob(beholder, 280.0 * hpMult, 18.0 * dmgMult, Math.min(23.0, 13.0 + (tier - 7) * 1.5), 0.7);
               return beholder;
            }
         } else if (tier == 6) {
            if (floor == 1) {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.vanguard_shadow_infantry"));
               buffMob(infantry, 160.0, 12.0, 8.0, 0.35);
               return infantry;
            } else if (floor == 2) {
               FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
               assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.glacial_assassin"));
               buffMob(assassin, 190.0, 15.0, 10.0, 0.4);
               return assassin;
            } else if (floor == 3) {
               AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
               orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.bloody_orc_brute"));
               buffMob(orc, 250.0, 16.0, 12.0, 0.45);
               return orc;
            } else if (index % 2 == 0) {
               AntSoldierEntity ant = new AntSoldierEntity(ModEntities.ANT_SOLDIER.get(), level);
               ant.setCustomName(Component.translatable("awakening_system.dungeon.mob.jeju_chimera_ant"));
               buffMob(ant, 220.0, 15.0, 11.0, 0.4);
               return ant;
            } else {
               AbyssalBeholderEntity beholder = new AbyssalBeholderEntity(ModEntities.ABYSSAL_BEHOLDER.get(), level);
               beholder.setCustomName(Component.translatable("awakening_system.dungeon.mob.abyssal_watcher_eye"));
               buffMob(beholder, 200.0, 14.0, 10.0, 0.65);
               return beholder;
            }
         } else if (tier == 5) {
            if (floor == 1) {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_soldier"));
               buffMob(infantry, 130.0, 9.0, 6.0, 0.3);
               return infantry;
            } else if (floor == 2) {
               FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
               assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.frost_stalker"));
               buffMob(assassin, 140.0, 11.0, 7.0, 0.35);
               return assassin;
            } else if (floor == 3) {
               AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
               orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.abyssal_orc"));
               buffMob(orc, 160.0, 11.0, 8.0, 0.35);
               return orc;
            } else {
               AbyssalNecromancerEntity necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
               necro.setCustomName(Component.translatable("awakening_system.dungeon.mob.abyssal_summoner"));
               buffMob(necro, 170.0, 11.0, 8.0, 0.35);
               return necro;
            }
         } else if (tier == 4) {
            if (floor <= 2) {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.lesser_shadow_infantry"));
               buffMob(infantry, 90.0, 7.0, 5.0, 0.25);
               return infantry;
            } else {
               FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
               assassin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
               buffMob(assassin, 85.0, 7.5, 5.0, 0.25);
               return assassin;
            }
         } else if (tier == 3) {
            if (floor <= 2) {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_wraith"));
               buffMob(infantry, 60.0, 5.0, 3.0, 0.15);
               return infantry;
            } else {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_soldier_glider"));
               infantry.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
               buffMob(infantry, 55.0, 4.5, 2.5, 0.1);
               return infantry;
            }
         } else if (floor <= 2) {
            ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.lesser_shadow"));
            buffMob(infantry, 35.0, 3.0, 1.0, 0.0);
            return infantry;
         } else {
            ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.corrupt_minor_shadow"));
            infantry.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            buffMob(infantry, 32.0, 2.8, 0.5, 0.0);
            return infantry;
         }
      }
   }

   private static Monster createFloor2Elite(ServerLevel level, String rank) {
      int tier = getRankTier(rank);
      if (tier >= 7) {
         double hpMult = 1.0 + (tier - 7) * 0.55;
         double dmgMult = 1.0 + (tier - 7) * 0.38;
         AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
         Component prefix = tier >= 12
            ? Component.translatable("awakening_system.dungeon.mob.deicide_elite_prefix", new Object[]{rank})
            : Component.translatable("awakening_system.dungeon.mob.elite_prefix", new Object[]{rank});
         Component name = tier >= 12
            ? Component.translatable("awakening_system.dungeon.mob.orc_titan_doomsday")
            : Component.translatable("awakening_system.dungeon.mob.orc_overlord_abyssal");
         orc.setCustomName(prefix.copy().append(name));
         orc.setCustomNameVisible(true);
         buffMob(orc, 500.0 * hpMult, 22.0 * dmgMult, Math.min(26.0, 15.0 + (tier - 7) * 2.0), 0.85);
         orc.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, tier >= 12 ? 2 : (tier >= 9 ? 1 : 0)));
         orc.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, tier >= 12 ? 1 : 0));
         return orc;
      } else if (tier == 6) {
         AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
         orc.setCustomName(
            Component.translatable("awakening_system.dungeon.mob.elite_prefix", new Object[]{"A"})
               .append(Component.translatable("awakening_system.dungeon.mob.abyssal_orc_warrior"))
         );
         orc.setCustomNameVisible(true);
         buffMob(orc, 300.0, 16.0, 12.0, 0.55);
         orc.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 0));
         return orc;
      } else if (tier == 5) {
         ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
         infantry.setCustomName(
            Component.translatable("awakening_system.dungeon.mob.elite_prefix", new Object[]{"B"})
               .append(Component.translatable("awakening_system.dungeon.mob.shadow_infantry_commander"))
         );
         infantry.setCustomNameVisible(true);
         buffMob(infantry, 200.0, 12.0, 9.0, 0.45);
         infantry.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 0));
         return infantry;
      } else if (tier == 4) {
         AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
         orc.setCustomName(
            Component.translatable("awakening_system.dungeon.mob.elite_prefix", new Object[]{"C"})
               .append(Component.translatable("awakening_system.dungeon.mob.blood_berserker_orc"))
         );
         orc.setCustomNameVisible(true);
         buffMob(orc, 140.0, 9.0, 6.0, 0.35);
         return orc;
      } else if (tier == 3) {
         ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
         infantry.setCustomName(
            Component.translatable("awakening_system.dungeon.mob.elite_prefix", new Object[]{rank})
               .append(Component.translatable("awakening_system.dungeon.mob.veteran_shadow_infantry"))
         );
         infantry.setCustomNameVisible(true);
         buffMob(infantry, 95.0, 6.0, 4.0, 0.2);
         return infantry;
      } else {
         ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
         infantry.setCustomName(
            Component.translatable("awakening_system.dungeon.mob.elite_prefix", new Object[]{rank})
               .append(Component.translatable("awakening_system.dungeon.mob.veteran_shadow_infantry"))
         );
         infantry.setCustomNameVisible(true);
         buffMob(infantry, 65.0, 4.5, 2.0, 0.15);
         return infantry;
      }
   }

   private static void buffMob(Monster mob, double maxHp, double totalDmg, double armor, double kbRes) {
      mob.setPersistenceRequired();
      mob.setCanPickUpLoot(false);
      AttributeInstance hpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
      if (hpAttr != null) {
         hpAttr.setBaseValue(maxHp);
         mob.setHealth((float)maxHp);
      }

      AttributeInstance dmgAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
      if (dmgAttr != null) {
         dmgAttr.setBaseValue(totalDmg);
      }

      AttributeInstance armorAttr = mob.getAttribute(Attributes.ARMOR);
      if (armorAttr != null) {
         armorAttr.setBaseValue(armor);
      }

      AttributeInstance kbAttr = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
      if (kbAttr != null) {
         kbAttr.setBaseValue(kbRes);
      }

      mob.addTag("dungeon_mob");
      mob.addTag("rpg_scaled");
   }

   public static void onEntityKilled(UUID entityId) {
      for (com.soybe.awakeningsystem.DungeonManager.DungeonSession session : SESSIONS.values()) {
         if (session.enemyUUIDs.remove(entityId)) {
            if (session.lastEnemyUUID != null && session.lastEnemyUUID.equals(entityId)) {
               session.lastEnemyNotified = false;
               session.lastEnemyUUID = null;
            }

            int remaining = session.enemyUUIDs.size();
            MinecraftServer server = getServer();
            if (server == null) {
               return;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(session.playerId);
            boolean isElite = session.currentFloor == 2 && remaining == session.totalEnemies - 1;
            int timeBonus = isElite ? 400 : 160;
            session.combatTimer = Math.min(session.maxCombatTimer + 2400, session.combatTimer + timeBonus);

            for (ServerPlayer sp : session.getAllPlayers(server)) {
               if (sp != null) {
                  Component bonusText = isElite
                     ? Component.translatable("awakening_system.action.elite_purified")
                     : (
                        remaining > 0
                           ? Component.translatable("awakening_system.dungeon.labyrinth_purified", new Object[]{remaining, session.totalEnemies})
                           : Component.translatable("awakening_system.action.enemy_purified")
                     );
                  sp.displayClientMessage(bonusText, true);
                  sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.4F);
               }
            }

            if (session.bossEvent != null) {
               session.bossEvent.setProgress((float)remaining / Math.max(1, session.totalEnemies));
               int secRemaining = Math.max(0, (session.combatTimer + 19) / 20);
               int mins = secRemaining / 60;
               int secs = secRemaining % 60;
               String timeStr = String.format("%02d:%02d", mins, secs);
               session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, session.currentFloor, remaining, timeStr));
            }

            if (session.bossUUID != null && session.bossUUID.equals(entityId)) {
               session.bossDefeated = true;
            }

            if (remaining == 0 || session.bossDefeated) {
               completeDungeonCombat(session, player);
            }

            return;
         }
      }
   }

   public static double findSafeFloorY(ServerLevel level, double x, int baseY, double z) {
      int ix = (int)Math.floor(x);
      int iz = (int)Math.floor(z);

      for (int y = baseY + 4; y >= baseY - 4; y--) {
         BlockPos p = new BlockPos(ix, y, iz);
         if (!level.isEmptyBlock(p) && level.isEmptyBlock(p.above()) && level.isEmptyBlock(p.above(2))) {
            return y + 1.0;
         }
      }

      return baseY + 1.0;
   }

   public static boolean isPlainsTheme(String theme) {
      if (theme == null) {
         return false;
      } else {
         String t = theme.toLowerCase(Locale.ROOT);
         return t.contains("plain") || t.contains("cherry") || t.contains("sakura") || t.contains("autumn") || t.contains("forest");
      }
   }

   private static void buildOuterBunkerEnvelope(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, String theme
   ) {
      if (!session.bunkerBoundsCalculated) {
         session.calculateBunkerBounds();
      }

      int minX = session.bunkerMinX;
      int maxX = session.bunkerMaxX;
      int minY = session.bunkerMinY;
      int maxY = session.bunkerMaxY;
      int minZ = session.bunkerMinZ;
      int maxZ = session.bunkerMaxZ;
      boolean plains = isPlainsTheme(theme);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            BlockPos pFloor1 = new BlockPos(x, minY, z);
            BlockPos pFloor2 = new BlockPos(x, minY + 1, z);
            level.setBlock(pFloor1, Blocks.BEDROCK.defaultBlockState(), 2);
            level.setBlock(pFloor2, Blocks.BEDROCK.defaultBlockState(), 2);
            session.arenaBlocks.add(pFloor1);
            session.arenaBlocks.add(pFloor2);
            BlockPos pCeil = new BlockPos(x, maxY, z);
            level.setBlock(pCeil, Blocks.BEDROCK.defaultBlockState(), 2);
            session.arenaBlocks.add(pCeil);
            if (x == minX || x == maxX || z == minZ || z == maxZ) {
               for (int y = minY + 1; y < maxY; y++) {
                  BlockPos pWall = new BlockPos(x, y, z);
                  level.setBlock(pWall, Blocks.BEDROCK.defaultBlockState(), 2);
                  session.arenaBlocks.add(pWall);
               }
            }
         }
      }
   }

   public static ResourceKey<LootTable> getDungeonLootTable(String rank, String theme) {
      int tier = getRankTier(rank);
      if (!"baran".equalsIgnoreCase(theme) && !"nether".equalsIgnoreCase(theme)) {
         if (tier >= 10) {
            return BuiltInLootTables.END_CITY_TREASURE;
         } else if (tier >= 7) {
            return BuiltInLootTables.ANCIENT_CITY;
         } else {
            return tier >= 4 ? BuiltInLootTables.STRONGHOLD_CORRIDOR : BuiltInLootTables.SIMPLE_DUNGEON;
         }
      } else {
         return tier >= 7 ? BuiltInLootTables.BASTION_TREASURE : BuiltInLootTables.NETHER_BRIDGE;
      }
   }

   public static void placeBossBarrierThreshold(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos pos, int barrierIndex
   ) {
      if (session != null && pos != null) {
         switch (barrierIndex) {
            case 1:
               session.barrierBossPos1 = pos;
               break;
            case 2:
               session.barrierBossPos2 = pos;
               break;
            case 3:
               session.barrierBossPos3 = pos;
         }

         int cx = pos.getX();
         int cy = pos.getY();
         int cz = pos.getZ();

         for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
               if (dx * dx + dz * dz <= 25) {
                  for (int dy = -2; dy <= 8; dy++) {
                     BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                     BlockState st = level.getBlockState(p);
                     if (st.isAir() || !st.isSolid() || st.canBeReplaced() || st.is(Blocks.COBWEB) || st.is(Blocks.WATER) || st.is(Blocks.LAVA)) {
                        level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 2);
                        session.barrierBossBlocks.add(p);
                        session.arenaBlocks.add(p);
                     }
                  }
               }
            }
         }
      }
   }

   private static void buildEliteTreasureSanctum(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, String rank, String theme
   ) {
      if (center != null) {
         boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme);
         BlockState floorBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
         BlockState pillarBlock = isBaran ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
         BlockState lightBlock = isBaran ? Blocks.SOUL_LANTERN.defaultBlockState() : Blocks.LANTERN.defaultBlockState();

         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos p = center.offset(dx, 0, dz);
               level.setBlock(p, floorBlock, 3);
               session.arenaBlocks.add(p);
            }
         }

         BlockPos c1 = center.offset(-1, 1, 0);
         BlockPos c2 = center.offset(1, 1, 0);
         BlockPos c3 = center.offset(0, 1, 1);

         for (BlockPos cp : new BlockPos[]{c1, c2, c3}) {
            level.setBlock(cp, (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 3);
            session.arenaBlocks.add(cp);
            if (level.getBlockEntity(cp) instanceof ChestBlockEntity chest) {
               chest.setLootTable(getDungeonLootTable(rank, theme), level.random.nextLong());
            }
         }

         BlockPos altar = center.offset(0, 1, -1);
         level.setBlock(altar, pillarBlock, 3);
         session.arenaBlocks.add(altar);
         BlockPos lockedChestPos = altar.above();
         level.setBlock(
            lockedChestPos,
            (BlockState)((LockedTreasureChestBlock)SystemBlocks.LOCKED_TREASURE_CHEST.get())
               .defaultBlockState()
               .setValue(LockedTreasureChestBlock.FACING, Direction.SOUTH),
            3
         );
         session.arenaBlocks.add(lockedChestPos);
         level.setBlock(center.offset(-2, 1, -2), lightBlock, 3);
         level.setBlock(center.offset(2, 1, -2), lightBlock, 3);
         level.setBlock(center.offset(-2, 1, 2), lightBlock, 3);
         level.setBlock(center.offset(2, 1, 2), lightBlock, 3);
         spawnEliteGuardian(level, session, center);
      }
   }

   private static void buildMinibossChamber(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, String rank, String theme, int mbId
   ) {
      if (center != null) {
         boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme);
         BlockState floorBlock = isBaran ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
         BlockState lightBlock = isBaran ? Blocks.SOUL_FIRE.defaultBlockState() : Blocks.SOUL_LANTERN.defaultBlockState();

         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos p = center.offset(dx, 0, dz);
               level.setBlock(p, floorBlock, 3);
               session.arenaBlocks.add(p);
            }
         }

         BlockPos chestPos = center.offset(0, 1, -3);
         level.setBlock(chestPos, (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 3);
         session.arenaBlocks.add(chestPos);
         if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(getDungeonLootTable(rank, theme), level.random.nextLong());
         }

         level.setBlock(center.offset(-2, 1, -2), lightBlock, 3);
         level.setBlock(center.offset(2, 1, -2), lightBlock, 3);
         spawnMiniboss(level, session, center, mbId);
      }
   }

   public static void spawnBarrierDoorAmbush(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos gatePos) {
      if (level != null && session != null && gatePos != null) {
         String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
         boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme) || "magma_forge".equalsIgnoreCase(theme);
         int tier = getRankTier(session.rank);
         double hpMult = 1.0 + Math.max(0, tier - 3) * 0.35;
         double dmgMult = 1.0 + Math.max(0, tier - 3) * 0.28;
         int guardCount = 4 + level.random.nextInt(3);

         for (int i = 0; i < guardCount; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 6.0;
            double oz = (level.random.nextDouble() - 0.5) * 6.0;
            BlockPos spawnP = gatePos.offset((int)ox, 0, (int)oz);
            double sy = findSafeFloorY(level, spawnP.getX() + 0.5, spawnP.getY(), spawnP.getZ() + 0.5);
            Monster guard;
            if (isBaran) {
               if (i % 2 == 0) {
                  AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                  orc.setCustomName(Component.literal("Â§cÂ§l[GUARDIA DE BARAN] Â§6Demonio Guerrero Ã\u008dgneo"));
                  orc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
                  buffMob(orc, 180.0 * hpMult, 12.0 * dmgMult, 10.0, 0.4);
                  orc.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 99999, 0));
                  guard = orc;
               } else {
                  ShadowInfantryEntity inf = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                  inf.setCustomName(Component.translatable("awakening_system.dungeon.mob.baran_guard_flame"));
                  inf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                  buffMob(inf, 150.0 * hpMult, 11.0 * dmgMult, 8.0, 0.35);
                  inf.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 99999, 0));
                  guard = inf;
               }
            } else if (i % 2 == 0) {
               ShadowInfantryEntity inf = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               inf.setCustomName(Component.literal("Â§4Â§l[GUARDIA CARMESÃ\u008d] Â§cCaballero de Igris"));
               inf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
               buffMob(inf, 160.0 * hpMult, 12.0 * dmgMult, 9.0, 0.4);
               guard = inf;
            } else {
               FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
               assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.igris_crimson_assassin"));
               assassin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
               buffMob(assassin, 140.0 * hpMult, 13.0 * dmgMult, 8.0, 0.3);
               guard = assassin;
            }

            guard.setCustomNameVisible(true);
            guard.setPersistenceRequired();
            guard.setCanPickUpLoot(false);
            guard.moveTo(spawnP.getX() + 0.5, sy, spawnP.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            applyDungeonMobMetadata(guard, session.rank, 4, session.isRedGate);
            level.addFreshEntity(guard);
            session.enemyUUIDs.add(guard.getUUID());
         }

         if (isBaran) {
            level.playSound(null, gatePos.getX(), gatePos.getY(), gatePos.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.2F, 0.9F);
            level.sendParticles(ParticleTypes.FLAME, gatePos.getX() + 0.5, gatePos.getY() + 1.5, gatePos.getZ() + 0.5, 40, 1.5, 1.5, 1.5, 0.1);
            level.sendParticles(ParticleTypes.LAVA, gatePos.getX() + 0.5, gatePos.getY() + 1.0, gatePos.getZ() + 0.5, 15, 1.0, 1.0, 1.0, 0.05);
            MinecraftServer s = level.getServer();
            if (s != null) {
               for (ServerPlayer ally : session.getAllPlayers(s)) {
                  if (ally != null) {
                     ally.sendSystemMessage(Component.translatable("awakening_system.dungeon.ambush.baran"));
                  }
               }
            }
         } else {
            level.playSound(null, gatePos.getX(), gatePos.getY(), gatePos.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.1F, 0.85F);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, gatePos.getX() + 0.5, gatePos.getY() + 1.5, gatePos.getZ() + 0.5, 40, 1.5, 1.5, 1.5, 0.1);
            level.sendParticles(ParticleTypes.FLASH, gatePos.getX() + 0.5, gatePos.getY() + 1.0, gatePos.getZ() + 0.5, 4, 0.5, 0.5, 0.5, 0.0);
            MinecraftServer s = level.getServer();
            if (s != null) {
               for (ServerPlayer allyx : session.getAllPlayers(s)) {
                  if (allyx != null) {
                     allyx.sendSystemMessage(Component.translatable("awakening_system.dungeon.ambush.igris"));
                  }
               }
            }
         }
      }
   }

   public static void spawnSanctumBoss(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos pos, String rank, String theme
   ) {
      if (level != null && session != null && pos != null) {
         int tier = getRankTier(rank);
         double hp = getDungeonBossHp(tier, session.isRedGate) * 0.75;
         double dmg = getDungeonBossDmg(tier, session.isRedGate) * 0.85;
         double bossArmor = Math.min(16.0, 5.0 + tier * 1.5);
         double bossKb = Math.min(0.8, 0.25 + tier * 0.06);
         Monster mb;
         if ("baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme) || "magma_forge".equalsIgnoreCase(theme)) {
            BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
            b.setCustomName(Component.translatable("awakening_system.dungeon.boss.chamber_baran"));
            mb = b;
         } else if ("igris".equalsIgnoreCase(theme) || "crypt".equalsIgnoreCase(theme) || "castle".equalsIgnoreCase(theme)) {
            IgrisBossEntity igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
            igris.setCustomName(Component.translatable("awakening_system.dungeon.boss.chamber_igris"));
            mb = igris;
         } else if (!"beru".equalsIgnoreCase(theme)
            && !"jeju".equalsIgnoreCase(theme)
            && !"ant".equalsIgnoreCase(theme)
            && !"insect".equalsIgnoreCase(theme)
            && !"hive".equalsIgnoreCase(theme)) {
            int roll = level.random.nextInt(3);
            if (roll == 0) {
               BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
               b.setCustomName(Component.translatable("awakening_system.dungeon.boss.chamber_baran"));
               mb = b;
            } else if (roll == 1) {
               IgrisBossEntity igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
               igris.setCustomName(Component.translatable("awakening_system.dungeon.boss.chamber_igris"));
               mb = igris;
            } else {
               BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), level);
               beru.setCustomName(Component.literal("§5§l[JEFE DE CÁMARA] §dBeru, Rey Hormiga"));
               mb = beru;
            }
         } else {
            BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), level);
            beru.setCustomName(Component.translatable("awakening_system.dungeon.boss.chamber_beru"));
            mb = beru;
         }

         mb.setCustomNameVisible(true);
         mb.setPersistenceRequired();
         mb.setCanPickUpLoot(false);
         mb.addTag("dungeon_mob");
         mb.addTag("dungeon_miniboss");
         mb.addTag("dungeon_boss");
         mb.addTag("dungeon_chamber_boss");
         buffMob(mb, hp, dmg, bossArmor, bossKb);
         double sy = findSafeFloorY(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         mb.moveTo(pos.getX() + 0.5, sy, pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         level.addFreshEntity(mb);
         session.enemyUUIDs.add(mb.getUUID());
      }
   }

   public static void spawnMiniboss(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos pos, int minibossId) {
      if (level != null && session != null && pos != null) {
         String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
         int tier = getRankTier(session.rank);
         double hp = 160.0 + tier * 65.0;
         double dmg = 8.0 + tier * 3.2;
         Monster mb;
         if ("baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme)) {
            AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
            orc.setCustomName(Component.translatable("awakening_system.dungeon.boss.miniboss_baran"));
            orc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            mb = orc;
         } else if ("igris".equalsIgnoreCase(theme)) {
            ShadowInfantryEntity inf = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            inf.setCustomName(Component.translatable("awakening_system.dungeon.boss.miniboss_igris"));
            inf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            mb = inf;
         } else if ("beru".equalsIgnoreCase(theme) || "ant".equalsIgnoreCase(theme) || "jeju".equalsIgnoreCase(theme)) {
            AntSoldierEntity ant = new AntSoldierEntity(ModEntities.ANT_SOLDIER.get(), level);
            ant.setCustomName(Component.translatable("awakening_system.dungeon.boss.miniboss_ant"));
            mb = ant;
         } else {
            ShadowInfantryEntity inf = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            inf.setCustomName(Component.translatable("awakening_system.dungeon.boss.miniboss_shadow"));
            inf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            mb = inf;
         }

         mb.setCustomNameVisible(true);
         mb.setPersistenceRequired();
         mb.addTag("dungeon_mob");
         mb.addTag("dungeon_miniboss");
         mb.addTag("dungeon_miniboss_" + minibossId);
         buffMob(mb, hp, dmg, 10.0, 0.6);
         double sy = findSafeFloorY(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         mb.moveTo(pos.getX() + 0.5, sy, pos.getZ() + 0.5, 0.0F, 0.0F);
         level.addFreshEntity(mb);
         session.enemyUUIDs.add(mb.getUUID());

         for (int k = 0; k < 4; k++) {
            int ox = k == 0 ? -3 : (k == 1 ? 3 : 0);
            int oz = k == 2 ? 3 : (k == 3 ? -3 : 0);
            Monster guard = createFloorMinion(level, session.rank, 2, k);
            if (guard != null) {
               applyDungeonMobMetadata(guard, session.rank, 2, false);
               double gSy = findSafeFloorY(level, pos.getX() + ox + 0.5, pos.getY(), pos.getZ() + oz + 0.5);
               guard.moveTo(pos.getX() + ox + 0.5, gSy, pos.getZ() + oz + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
               guard.setPersistenceRequired();
               guard.setCanPickUpLoot(false);
               level.addFreshEntity(guard);
               session.enemyUUIDs.add(guard.getUUID());
            }
         }
      }
   }

   public static void spawnEliteGuardian(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos pos) {
      if (level != null && session != null && pos != null) {
         int tier = getRankTier(session.rank);
         double hp = 100.0 + tier * 40.0;
         double dmg = 6.0 + tier * 2.5;
         Monster g = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
         g.setCustomName(Component.translatable("awakening_system.dungeon.boss.guardian_sanctum"));
         g.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
         g.setCustomNameVisible(true);
         g.setPersistenceRequired();
         g.addTag("dungeon_mob");
         g.addTag("dungeon_miniboss");
         g.addTag("dungeon_guardian");
         buffMob(g, hp, dmg, 8.0, 0.5);
         double sy = findSafeFloorY(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         g.moveTo(pos.getX() + 0.5, sy, pos.getZ() + 0.5, 0.0F, 0.0F);
         level.addFreshEntity(g);
         session.enemyUUIDs.add(g.getUUID());

         for (int k = 0; k < 3; k++) {
            int ox = k == 0 ? -2 : (k == 1 ? 2 : 0);
            int oz = k == 2 ? 2 : -2;
            Monster guard = createFloorMinion(level, session.rank, 3, k);
            if (guard != null) {
               applyDungeonMobMetadata(guard, session.rank, 3, false);
               double gSy = findSafeFloorY(level, pos.getX() + ox + 0.5, pos.getY(), pos.getZ() + oz + 0.5);
               guard.moveTo(pos.getX() + ox + 0.5, gSy, pos.getZ() + oz + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
               guard.setPersistenceRequired();
               guard.setCanPickUpLoot(false);
               level.addFreshEntity(guard);
               session.enemyUUIDs.add(guard.getUUID());
            }
         }
      }
   }

   public static void spawnChamberMinions(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos pos, String rank, int count
   ) {
      if (level != null && session != null && pos != null) {
         for (int i = 0; i < count; i++) {
            Monster mob = createFloorMinion(level, rank, Math.min(3, 1 + i % 3), i);
            if (mob != null) {
               applyDungeonMobMetadata(mob, rank, 3, false);
               double ox = (level.random.nextDouble() - 0.5) * 6.0;
               double oz = (level.random.nextDouble() - 0.5) * 6.0;
               double sy = findSafeFloorY(level, pos.getX() + ox, pos.getY(), pos.getZ() + oz);
               mob.moveTo(pos.getX() + ox, sy, pos.getZ() + oz, level.random.nextFloat() * 360.0F, 0.0F);
               mob.setPersistenceRequired();
               mob.setCanPickUpLoot(false);
               level.addFreshEntity(mob);
               session.enemyUUIDs.add(mob.getUUID());
            }
         }
      }
   }

   public static void buildContinuousDungeon(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, String rank, String theme
   ) {
      buildOuterBunkerEnvelope(level, session, center, theme);
      carveOrganicDungeonRoom(level, session, session.roomP, 10, 10, 8, theme, 0, com.soybe.awakeningsystem.DungeonRoomType.PORTAL);
      carveOrganicDungeonRoom(level, session, session.roomC1, 12, 12, 9, theme, 1, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomC2, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomC3, 14, 14, 10, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomMB_Center, 16, 16, 12, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.MINIBOSS);
      carveOrganicDungeonRoom(level, session, session.roomC5, 12, 12, 9, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomL2_1, 12, 12, 9, theme, 1, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomL2_2, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomB_Mid, 18, 18, 13, theme, 4, com.soybe.awakeningsystem.DungeonRoomType.BOSS_CANDIDATE);
      carveOrganicDungeonRoom(level, session, session.roomL2_4, 14, 14, 10, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomB_Top, 18, 18, 13, theme, 4, com.soybe.awakeningsystem.DungeonRoomType.BOSS_CANDIDATE);
      carveOrganicDungeonRoom(level, session, session.roomL2_6, 11, 11, 8, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomMB_Left, 16, 16, 12, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.MINIBOSS);
      carveOrganicDungeonRoom(level, session, session.roomL1_2, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomL1_3, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomL1_4, 13, 13, 10, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomL1_5, 12, 12, 9, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR4_1, 12, 12, 9, theme, 1, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR4_2, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR4_3, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR4_4, 13, 13, 10, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR4_5, 12, 12, 9, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomB_Right, 18, 18, 13, theme, 4, com.soybe.awakeningsystem.DungeonRoomType.BOSS_CANDIDATE);
      carveOrganicDungeonRoom(level, session, session.roomR5_2, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR5_3, 12, 12, 9, theme, 2, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR5_4, 13, 13, 10, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveOrganicDungeonRoom(level, session, session.roomR5_5, 12, 12, 9, theme, 3, com.soybe.awakeningsystem.DungeonRoomType.NORMAL);
      carveCaveCorridor(level, session, session.roomP, session.roomC1, theme);
      carveCaveCorridor(level, session, session.roomC1, session.roomL2_1, theme);
      carveCaveCorridor(level, session, session.roomC1, session.roomC2, theme);
      carveCaveCorridor(level, session, session.roomC1, session.roomR4_1, theme);
      carveCaveCorridor(level, session, session.roomL2_1, session.roomL2_2, theme);
      carveCaveCorridor(level, session, session.roomL2_2, session.roomL1_2, theme);
      carveCaveCorridor(level, session, session.roomL1_2, session.roomMB_Left, theme);
      carveCaveCorridor(level, session, session.roomL2_2, session.roomL1_3, theme);
      carveCaveCorridor(level, session, session.roomL1_3, session.roomL1_4, theme);
      carveCaveCorridor(level, session, session.roomL1_4, session.roomL2_4, theme);
      carveCaveCorridor(level, session, session.roomL2_4, session.roomB_Mid, theme);
      carveCaveCorridor(level, session, session.roomC5, session.roomL2_6, theme);
      carveCaveCorridor(level, session, session.roomL2_6, session.roomL1_5, theme);
      carveCaveCorridor(level, session, session.roomL1_5, session.roomB_Top, theme);
      carveCaveCorridor(level, session, session.roomMB_Center, session.roomC5, theme);
      carveCaveCorridor(level, session, session.roomC2, session.roomR4_2, theme);
      carveCaveCorridor(level, session, session.roomR4_1, session.roomR4_2, theme);
      carveCaveCorridor(level, session, session.roomR4_2, session.roomR4_3, theme);
      carveCaveCorridor(level, session, session.roomR4_3, session.roomC3, theme);
      carveCaveCorridor(level, session, session.roomC3, session.roomMB_Center, theme);
      carveCaveCorridor(level, session, session.roomMB_Center, session.roomR4_4, theme);
      carveCaveCorridor(level, session, session.roomR4_4, session.roomR5_4, theme);
      carveCaveCorridor(level, session, session.roomR5_4, session.roomR5_5, theme);
      carveCaveCorridor(level, session, session.roomR5_5, session.roomR4_5, theme);
      carveCaveCorridor(level, session, session.roomR5_3, session.roomR5_2, theme);
      carveCaveCorridor(level, session, session.roomR5_2, session.roomB_Right, theme);
      carveCaveCorridor(level, session, session.roomMB_Center, session.roomL2_4, theme);
      carveCaveCorridor(level, session, session.roomC5, session.roomR4_5, theme);
      placeCandidateBossBarrier(level, session, session.roomB_Mid, 1);
      placeCandidateBossBarrier(level, session, session.roomB_Top, 2);
      placeCandidateBossBarrier(level, session, session.roomB_Right, 3);
      buildNaturalBossArena(level, session, session.thronePos, rank, theme);
      if (session.eliteSanctum1Pos != null) {
         buildEliteTreasureSanctum(level, session, session.eliteSanctum1Pos, rank, theme);
      }

      if (session.eliteSanctum2Pos != null) {
         buildEliteTreasureSanctum(level, session, session.eliteSanctum2Pos, rank, theme);
      }

      if (session.roomMB_Left != null) {
         buildMinibossChamber(level, session, session.roomMB_Left, rank, theme, 1);
      }

      if (session.roomMB_Center != null) {
         buildMinibossChamber(level, session, session.roomMB_Center, rank, theme, 2);
      }

      if (session.eliteSanctum1Pos != null) {
         spawnChamberMinions(level, session, session.eliteSanctum1Pos, rank, 4);
      }

      if (session.eliteSanctum2Pos != null) {
         spawnChamberMinions(level, session, session.eliteSanctum2Pos, rank, 4);
      }

      if (session.thronePos != null) {
         spawnPhase5Boss(level, session);
      }

      session.totalEnemies = session.enemyUUIDs.size();
   }

   public static void carveOrganicDungeonRoom(
      ServerLevel level,
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session,
      BlockPos center,
      int rx,
      int rz,
      int height,
      String theme,
      int stage,
      com.soybe.awakeningsystem.DungeonRoomType type
   ) {
      if (center != null) {
         if (!"baran".equalsIgnoreCase(theme) && !"nether".equalsIgnoreCase(theme)) {
            boolean var39 = false;
         } else {
            boolean var10000 = true;
         }

         int cx = center.getX();
         int cy = center.getY();
         int cz = center.getZ();

         for (int dx = -rx - 3; dx <= rx + 3; dx++) {
            for (int dz = -rz - 3; dz <= rz + 3; dz++) {
               double normX = dx / (rx + 0.5);
               double normZ = dz / (rz + 0.5);
               double distH = normX * normX + normZ * normZ;
               if (!(distH > 1.45)) {
                  double wave = 1.0 + 0.12 * Math.sin(dx * 0.45 + dyFloorWave(cx, cz)) + 0.12 * Math.cos(dz * 0.45);

                  for (int dy = -3; dy <= height + 5; dy++) {
                     BlockPos bp = new BlockPos(cx + dx, cy + dy, cz + dz);
                     if (dy <= -3) {
                        level.setBlock(bp, Blocks.BEDROCK.defaultBlockState(), 2);
                        session.arenaBlocks.add(bp);
                     } else if (dy <= 0) {
                        BlockState f = pickNaturalCavernFloor(dx, dy, dz, theme);
                        level.setBlock(bp, f, 2);
                        session.arenaBlocks.add(bp);
                     } else if (dy < height) {
                        double normY = dy / (height + 0.5);
                        double domeDist = (distH + normY * normY) / wave;
                        if (domeDist < 1.0) {
                           level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                           session.arenaBlocks.add(bp);
                        } else {
                           BlockState w = pickNaturalCavernWall(dx, dy, dz, theme);
                           level.setBlock(bp, w, 2);
                           session.arenaBlocks.add(bp);
                        }
                     } else if (dy >= height + 3 && distH <= 1.25) {
                        level.setBlock(bp, Blocks.BEDROCK.defaultBlockState(), 2);
                        session.arenaBlocks.add(bp);
                     } else {
                        BlockState w = pickNaturalCavernWall(dx, dy, dz, theme);
                        level.setBlock(bp, w, 2);
                        session.arenaBlocks.add(bp);
                     }
                  }
               }
            }
         }

         if (type == com.soybe.awakeningsystem.DungeonRoomType.NORMAL) {
            int mobCount = 5 + level.random.nextInt(4);

            for (int i = 0; i < mobCount; i++) {
               double angle = (Math.PI * 2) * i / mobCount;
               double dist = 3.0 + i % 3 * 1.5;
               int ox_offset = (int)Math.round(Math.cos(angle) * dist);
               int oz_offset = (int)Math.round(Math.sin(angle) * dist);
               BlockPos p = center.offset(ox_offset, 0, oz_offset);
               Monster mob = createFloorMinion(level, session.rank, stage, i);
               if (mob != null) {
                  double sy = findSafeFloorY(level, p.getX() + 0.5, center.getY(), p.getZ() + 0.5);
                  mob.moveTo(p.getX() + 0.5, sy, p.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                  mob.setPersistenceRequired();
                  mob.setCanPickUpLoot(false);
                  applyDungeonMobMetadata(mob, session.rank, stage, false);
                  level.addFreshEntity(mob);
                  session.enemyUUIDs.add(mob.getUUID());
               }
            }
         }
      }
   }

   private static double dyFloorWave(int x, int z) {
      return Math.sin(x * 0.08) + Math.cos(z * 0.08);
   }

   private static void placeNaturalLanternPost(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos pos) {
   }

   private static BlockState pickNaturalCavernFloor(int dx, int dy, int dz, String theme) {
      return getThemeFloorBlock(theme, 1, dx, dy, dz);
   }

   private static BlockState pickNaturalCavernWall(int dx, int dy, int dz, String theme) {
      return getThemeWallBlock(theme, 1, dx, dy, dz, 8);
   }

   public static void carveCaveCorridor(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos from, BlockPos to, String theme
   ) {
      if (from != null && to != null) {
         carveOrganicTunnelDirect(level, session, from, to, theme);
      }
   }

   private static void carveOrganicTunnelDirect(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos from, BlockPos to, String theme
   ) {
      double x1 = from.getX();
      double z1 = from.getZ();
      double x2 = to.getX();
      double z2 = to.getZ();
      int oy = from.getY();
      double dx = x2 - x1;
      double dz = z2 - z1;
      double len = Math.hypot(dx, dz);
      if (!(len < 0.001)) {
         double ux = dx / len;
         double uz = dz / len;
         int minX = (int)Math.floor(Math.min(x1, x2) - 5.0);
         int maxX = (int)Math.ceil(Math.max(x1, x2) + 5.0);
         int minZ = (int)Math.floor(Math.min(z1, z2) - 5.0);
         int maxZ = (int)Math.ceil(Math.max(z1, z2) + 5.0);

         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               double vx = x - x1;
               double vz = z - z1;
               double proj = vx * ux + vz * uz;
               if (!(proj < -2.0) && !(proj > len + 2.0)) {
                  double t = Math.max(0.0, Math.min(len, proj));
                  double cx = x1 + t * ux;
                  double cz = z1 + t * uz;
                  double distH = Math.hypot(x - cx, z - cz);
                  if (!(distH > 4.5)) {
                     for (int dy = -3; dy <= 9; dy++) {
                        BlockPos bp = new BlockPos(x, oy + dy, z);
                        boolean inChamber = isInsideChamberVolume(session, bp);
                        if (inChamber) {
                           if (dy >= 1 && dy <= 4 && distH <= 2.2) {
                              level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                              session.arenaBlocks.add(bp);
                           }
                        } else {
                           double normH = distH / 3.4;
                           double normY = (dy - 1.2) / 3.2;
                           double dist = normH * normH + normY * normY;
                           double wave = 1.0 + 0.08 * Math.sin(x * 0.35 + z * 0.35);
                           double effDist = dist / wave;
                           if (dy <= -2) {
                              if (distH <= 3.4) {
                                 level.setBlock(bp, Blocks.BEDROCK.defaultBlockState(), 2);
                                 session.arenaBlocks.add(bp);
                              }
                           } else if (dy <= 0) {
                              if (distH <= 3.4) {
                                 BlockState f = pickNaturalCavernFloor(x, dy, z, theme);
                                 level.setBlock(bp, f, 2);
                                 session.arenaBlocks.add(bp);
                              }
                           } else if (dy <= 4 && effDist < 1.0) {
                              level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                              session.arenaBlocks.add(bp);
                           } else if (dy <= 6 && effDist <= 1.85) {
                              BlockState w = pickNaturalCavernWall(x, dy, z, theme);
                              level.setBlock(bp, w, 2);
                              session.arenaBlocks.add(bp);
                           } else if (dy >= 5 && distH <= 3.2) {
                              if (dy >= 7) {
                                 level.setBlock(bp, Blocks.BEDROCK.defaultBlockState(), 2);
                              } else {
                                 level.setBlock(bp, pickNaturalCavernWall(x, dy, z, theme), 2);
                              }

                              session.arenaBlocks.add(bp);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void placeCandidateBossBarrier(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos roomCenter, int index
   ) {
      if (roomCenter != null) {
         int oy = roomCenter.getY();
         if (index == 2) {
            BlockPos gateCenter = new BlockPos(roomCenter.getX() - 17, oy, roomCenter.getZ());

            for (int dz = -4; dz <= 4; dz++) {
               for (int dy = 1; dy <= 6; dy++) {
                  BlockPos bp = new BlockPos(gateCenter.getX(), oy + dy, gateCenter.getZ() + dz);
                  BlockState cur = level.getBlockState(bp);
                  if (cur.isAir() || cur.canBeReplaced() || cur.is(Blocks.BARRIER) || cur.is(Blocks.IRON_BARS)) {
                     if (Math.abs(dz) != 4 && dy != 6) {
                        level.setBlock(bp, Blocks.IRON_BARS.defaultBlockState(), 2);
                        session.barrierBossBlocks.add(bp);
                     } else {
                        level.setBlock(bp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                     }

                     session.arenaBlocks.add(bp);
                  }
               }
            }

            session.barrierBossPos2 = gateCenter;
         } else if (index == 1) {
            BlockPos gateCenter = new BlockPos(roomCenter.getX(), oy, roomCenter.getZ() - 17);

            for (int dx = -4; dx <= 4; dx++) {
               for (int dyx = 1; dyx <= 6; dyx++) {
                  BlockPos bp = new BlockPos(gateCenter.getX() + dx, oy + dyx, gateCenter.getZ());
                  BlockState cur = level.getBlockState(bp);
                  if (cur.isAir() || cur.canBeReplaced() || cur.is(Blocks.BARRIER) || cur.is(Blocks.IRON_BARS)) {
                     if (Math.abs(dx) != 4 && dyx != 6) {
                        level.setBlock(bp, Blocks.IRON_BARS.defaultBlockState(), 2);
                        session.barrierBossBlocks.add(bp);
                     } else {
                        level.setBlock(bp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                     }

                     session.arenaBlocks.add(bp);
                  }
               }
            }

            session.barrierBossPos1 = gateCenter;
         } else {
            BlockPos gateCenter = new BlockPos(roomCenter.getX(), oy, roomCenter.getZ() - 17);

            for (int dx = -4; dx <= 4; dx++) {
               for (int dyxx = 1; dyxx <= 6; dyxx++) {
                  BlockPos bp = new BlockPos(gateCenter.getX() + dx, oy + dyxx, gateCenter.getZ());
                  BlockState cur = level.getBlockState(bp);
                  if (cur.isAir() || cur.canBeReplaced() || cur.is(Blocks.BARRIER) || cur.is(Blocks.IRON_BARS)) {
                     if (Math.abs(dx) != 4 && dyxx != 6) {
                        level.setBlock(bp, Blocks.IRON_BARS.defaultBlockState(), 2);
                        session.barrierBossBlocks.add(bp);
                     } else {
                        level.setBlock(bp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                     }

                     session.arenaBlocks.add(bp);
                  }
               }
            }

            session.barrierBossPos3 = gateCenter;
         }
      }
   }

   private static void buildLabyrinthPartitions(
      ServerLevel level,
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session,
      BlockPos chamberCenter,
      int rx,
      int rz,
      int h,
      String theme,
      int stage,
      List<BlockPos> doorways
   ) {
   }

   private static void buildLabyrinthDoorway(
      ServerLevel level,
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session,
      int wallCoord,
      int cy,
      int midOrthCoord,
      boolean isVertical,
      BlockState base,
      BlockState stair,
      BlockState slab,
      BlockState lantern,
      int wallH,
      Random rng
   ) {
      for (int offset = -1; offset <= 1; offset++) {
         int x = isVertical ? wallCoord : midOrthCoord + offset;
         int z = isVertical ? midOrthCoord + offset : wallCoord;

         for (int dy = 1; dy <= 3; dy++) {
            BlockPos p = new BlockPos(x, cy + dy, z);
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            session.arenaBlocks.add(p);
         }

         BlockPos pArch = new BlockPos(x, cy + 4, z);
         level.setBlock(pArch, offset == 0 ? slab : stair, 2);
         session.arenaBlocks.add(pArch);

         for (int dy = 5; dy <= wallH; dy++) {
            BlockPos pTop = new BlockPos(x, cy + dy, z);
            level.setBlock(pTop, base, 2);
            session.arenaBlocks.add(pTop);
         }
      }
   }

   private static boolean isNearDoorway(int px, int pz, List<BlockPos> doorways) {
      if (doorways != null && !doorways.isEmpty()) {
         for (BlockPos d : doorways) {
            if (Math.abs(px - d.getX()) <= 2 && Math.abs(pz - d.getZ()) <= 2) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static long packGridCoord(int gx, int gz) {
      return (long)(gx & 65535) << 16 | gz & 65535;
   }

   private static int unpackGridX(long coord) {
      return (short)(coord >> 16 & 65535L);
   }

   private static int unpackGridZ(long coord) {
      return (short)(coord & 65535L);
   }

   private static long packEdge(int gx1, int gz1, int gx2, int gz2) {
      long c1 = packGridCoord(gx1, gz1);
      long c2 = packGridCoord(gx2, gz2);
      return c1 < c2 ? c1 << 32 | c2 & 4294967295L : c2 << 32 | c1 & 4294967295L;
   }

   private static void buildDeadEndAmbushChamber(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, String rank, String theme, boolean isWest
   ) {
      if (center != null) {
         boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme);
         BlockState foundationBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
         BlockState runeBlock = isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState();
         BlockPos chestPos = center.above();
         level.setBlock(center, foundationBlock, 3);
         session.arenaBlocks.add(center);
         level.setBlock(chestPos, (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 3);
         session.arenaBlocks.add(chestPos);

         try {
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
               chestEntity.setLootTable(getDungeonLootTable(rank, theme), level.random.nextLong());
            }
         } catch (Exception var13) {
         }

         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dz != 0) {
                  BlockPos pFloor = center.offset(dx, 0, dz);
                  level.setBlock(pFloor, runeBlock, 3);
                  session.arenaBlocks.add(pFloor);
               }
            }
         }

         BlockPos web1 = center.offset(-3, 1, 0);
         BlockPos web2 = center.offset(3, 1, 0);
         level.setBlock(web1, Blocks.COBWEB.defaultBlockState(), 3);
         level.setBlock(web2, Blocks.COBWEB.defaultBlockState(), 3);
         session.arenaBlocks.add(web1);
         session.arenaBlocks.add(web2);
      }
   }

   private static void buildSecretTreasureCache(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, String rank, String theme
   ) {
      BlockPos chestPos = center.above();
      level.setBlock(center, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
      session.arenaBlocks.add(center);
      level.setBlock(
         chestPos,
         (BlockState)((LockedTreasureChestBlock)SystemBlocks.LOCKED_TREASURE_CHEST.get())
            .defaultBlockState()
            .setValue(LockedTreasureChestBlock.FACING, Direction.SOUTH),
         3
      );
      session.arenaBlocks.add(chestPos);
      BlockPos pedestalPos = center.offset(0, 0, -2);
      level.setBlock(pedestalPos.below(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
      level.setBlock(pedestalPos, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 3);
      session.arenaBlocks.add(pedestalPos.below());
      session.arenaBlocks.add(pedestalPos);
      BlockPos[] corners = new BlockPos[]{center.offset(-3, 1, -3), center.offset(3, 1, -3), center.offset(-3, 1, 3), center.offset(3, 1, 3)};

      for (BlockPos cp : corners) {
         level.setBlock(cp, Blocks.COBWEB.defaultBlockState(), 3);
         session.arenaBlocks.add(cp);
      }

      BlockPos torch1 = center.offset(-2, 1, 0);
      BlockPos torch2 = center.offset(2, 1, 0);
      level.setBlock(torch1, Blocks.SOUL_TORCH.defaultBlockState(), 3);
      level.setBlock(torch2, Blocks.SOUL_TORCH.defaultBlockState(), 3);
      session.arenaBlocks.add(torch1);
      session.arenaBlocks.add(torch2);
   }

   private static BlockState getThemeFloorBlock(String theme, int stage, int dx, int dy, int dz) {
      double n = Math.sin(dx * 0.15) * Math.cos(dz * 0.15);
      if ("tundra".equalsIgnoreCase(theme)) {
         if (dy == 0) {
            if (n > 0.65) {
               return Blocks.BLUE_ICE.defaultBlockState();
            } else {
               return n < -0.5 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
            }
         } else {
            return Blocks.PACKED_ICE.defaultBlockState();
         }
      } else if ("lush_plains".equalsIgnoreCase(theme)) {
         if (dy == 0) {
            if (n > 0.5) {
               return Blocks.MOSS_BLOCK.defaultBlockState();
            } else {
               return n < -0.6 ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            }
         } else {
            return Blocks.ROOTED_DIRT.defaultBlockState();
         }
      } else if ("cherry_plains".equalsIgnoreCase(theme)) {
         if (dy == 0) {
            if (n > 0.55) {
               return Blocks.MOSS_BLOCK.defaultBlockState();
            } else {
               return n < -0.65 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            }
         } else {
            return Blocks.DIRT.defaultBlockState();
         }
      } else if ("autumn_plains".equalsIgnoreCase(theme)) {
         if (dy == 0) {
            if (n > 0.5) {
               return Blocks.PODZOL.defaultBlockState();
            } else {
               return n < -0.55 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            }
         } else {
            return Blocks.DIRT.defaultBlockState();
         }
      } else if ("plains".equalsIgnoreCase(theme)) {
         if (dy == 0) {
            if (n > 0.6) {
               return Blocks.MOSS_BLOCK.defaultBlockState();
            } else {
               return n < -0.6 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            }
         } else {
            return Blocks.DIRT.defaultBlockState();
         }
      } else if (!"baran".equalsIgnoreCase(theme) && !"nether".equalsIgnoreCase(theme)) {
         if ("ancient_temple".equalsIgnoreCase(theme)) {
            if (dy != 0) {
               return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            } else if (n > 0.55) {
               return Blocks.CUT_SANDSTONE.defaultBlockState();
            } else {
               return n < -0.6 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
            }
         } else if ("crystal_cave".equalsIgnoreCase(theme)) {
            if (dy != 0) {
               return Blocks.DEEPSLATE.defaultBlockState();
            } else if (n > 0.5) {
               return Blocks.AMETHYST_BLOCK.defaultBlockState();
            } else {
               return n < -0.6 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
            }
         } else if (dy != 0) {
            return Blocks.DEEPSLATE.defaultBlockState();
         } else if (stage >= 4) {
            if (n > 0.55) {
               return Blocks.DEEPSLATE_TILES.defaultBlockState();
            } else {
               return n < -0.65 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
            }
         } else if (n > 0.58) {
            return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
         } else {
            return n < -0.62 ? Blocks.TUFF.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
         }
      } else if (dy == 0) {
         if (n > 0.55) {
            return Blocks.CRIMSON_NYLIUM.defaultBlockState();
         } else {
            return n < -0.6 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
         }
      } else {
         return Blocks.BLACKSTONE.defaultBlockState();
      }
   }

   private static BlockState getThemeWallBlock(String theme, int stage, int dx, int dy, int dz, int h) {
      int rMod = Math.abs(dx * 11 + dz * 13 + dy * 7) % 24;
      if ("tundra".equalsIgnoreCase(theme)) {
         if (rMod <= 5) {
            return Blocks.PACKED_ICE.defaultBlockState();
         } else if (rMod <= 9) {
            return Blocks.BLUE_ICE.defaultBlockState();
         } else {
            return rMod <= 13 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
         }
      } else if ("lush_plains".equalsIgnoreCase(theme)) {
         if (rMod <= 4) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
         } else if (rMod <= 7) {
            return Blocks.CLAY.defaultBlockState();
         } else if (rMod <= 10) {
            return Blocks.ROOTED_DIRT.defaultBlockState();
         } else {
            return rMod <= 15 ? Blocks.STONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
         }
      } else if ("cherry_plains".equalsIgnoreCase(theme)) {
         if (rMod <= 4) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
         } else if (rMod <= 7) {
            return Blocks.CHERRY_WOOD.defaultBlockState();
         } else {
            return rMod <= 12 ? Blocks.STONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
         }
      } else if ("autumn_plains".equalsIgnoreCase(theme)) {
         if (rMod <= 4) {
            return Blocks.PODZOL.defaultBlockState();
         } else if (rMod <= 7) {
            return Blocks.PACKED_MUD.defaultBlockState();
         } else {
            return rMod <= 12 ? Blocks.STONE.defaultBlockState() : Blocks.MUD_BRICKS.defaultBlockState();
         }
      } else if ("plains".equalsIgnoreCase(theme)) {
         if (rMod <= 3) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
         } else if (rMod <= 6) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
         } else if (rMod <= 8) {
            return Blocks.COAL_ORE.defaultBlockState();
         } else if (rMod <= 10) {
            return Blocks.IRON_ORE.defaultBlockState();
         } else {
            return rMod <= 16 ? Blocks.STONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
         }
      } else if (!"baran".equalsIgnoreCase(theme) && !"nether".equalsIgnoreCase(theme)) {
         if ("ancient_temple".equalsIgnoreCase(theme)) {
            if (rMod <= 4) {
               return Blocks.CUT_SANDSTONE.defaultBlockState();
            } else if (rMod <= 8) {
               return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            } else if (rMod <= 11) {
               return Blocks.CHISELED_SANDSTONE.defaultBlockState();
            } else {
               return rMod == 12 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
            }
         } else if ("crystal_cave".equalsIgnoreCase(theme)) {
            if (rMod <= 5) {
               return Blocks.AMETHYST_BLOCK.defaultBlockState();
            } else if (rMod <= 8) {
               return Blocks.BUDDING_AMETHYST.defaultBlockState();
            } else if (rMod <= 12) {
               return Blocks.CALCITE.defaultBlockState();
            } else {
               return rMod <= 16 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
            }
         } else if (rMod == 0) {
            return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
         } else if (rMod == 1) {
            return Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
         } else if (rMod <= 3) {
            return Blocks.DEEPSLATE_TILES.defaultBlockState();
         } else if (rMod <= 6) {
            return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
         } else if (rMod <= 9) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
         } else if (rMod <= 12) {
            return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
         } else {
            return rMod == 13 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
         }
      } else if (rMod <= 4) {
         return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
      } else if (rMod <= 7) {
         return Blocks.CRIMSON_NYLIUM.defaultBlockState();
      } else if (rMod <= 9) {
         return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
      } else if (rMod <= 11) {
         return Blocks.RED_NETHER_BRICKS.defaultBlockState();
      } else {
         return rMod == 12 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
      }
   }

   private static void placeFloorDecorations(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos p, String theme, int dx, int dz
   ) {
      if ("plains".equalsIgnoreCase(theme)) {
         int flowerMod = Math.abs(dx * 13 + dz * 17) % 18;
         if (flowerMod == 0) {
            level.setBlock(p, Blocks.POPPY.defaultBlockState(), 3);
         } else if (flowerMod == 1) {
            level.setBlock(p, Blocks.DANDELION.defaultBlockState(), 3);
         } else if (flowerMod == 2) {
            level.setBlock(p, Blocks.CORNFLOWER.defaultBlockState(), 3);
         } else if (flowerMod <= 5) {
            level.setBlock(p, Blocks.SHORT_GRASS.defaultBlockState(), 3);
         }
      } else if ("tundra".equalsIgnoreCase(theme) && Math.abs(dx * 5 + dz * 9) % 6 == 0) {
         level.setBlock(p, Blocks.SNOW.defaultBlockState(), 3);
      }
   }

   private static BlockState getThemeLeavesBlock(String theme, int randSeed) {
      if ("cherry_plains".equalsIgnoreCase(theme)) {
         return (BlockState)Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
      } else if ("lush_plains".equalsIgnoreCase(theme)) {
         return randSeed % 3 == 0
            ? (BlockState)Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true)
            : (BlockState)Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
      } else if ("autumn_plains".equalsIgnoreCase(theme)) {
         return randSeed % 2 == 0
            ? (BlockState)Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true)
            : (BlockState)Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
      } else {
         return randSeed % 3 == 0
            ? (BlockState)Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true)
            : (BlockState)Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
      }
   }

   private static void carveOrganicCaveChamber(
      ServerLevel level,
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session,
      BlockPos chamberCenter,
      int rx,
      int rz,
      int h,
      String theme,
      int stage
   ) {
      int cx = chamberCenter.getX();
      int cy = chamberCenter.getY();
      int cz = chamberCenter.getZ();
      boolean plains = isPlainsTheme(theme);
      double s1Radius = Math.max(3.6, Math.min(rx, rz) * 0.52);
      int s2X = stage % 2 == 0 ? -3 : 3;
      int s2Z = stage % 3 == 0 ? -2 : 2;
      double s2Radius = Math.max(2.6, Math.min(rx, rz) * 0.36);

      for (int dx = -rx - 3; dx <= rx + 3; dx++) {
         for (int dz = -rz - 3; dz <= rz + 3; dz++) {
            double normX = (double)dx / rx;
            double normZ = (double)dz / rz;
            double hDist = normX * normX + normZ * normZ;
            double dShaft1 = Math.sqrt(dx * dx + dz * dz);
            double dShaft2 = Math.sqrt((dx - s2X) * (dx - s2X) + (dz - s2Z) * (dz - s2Z));
            boolean inSkylight = plains && (dShaft1 < s1Radius || dShaft2 < s2Radius);
            boolean isSkylightRim = plains
               && (dShaft1 >= s1Radius - 1.4 && dShaft1 <= s1Radius + 1.2 || dShaft2 >= s2Radius - 1.3 && dShaft2 <= s2Radius + 1.1);

            for (int dy = -4; dy <= h + 4; dy++) {
               BlockPos p = chamberCenter.offset(dx, dy, dz);
               if (dy <= -3) {
                  level.setBlock(p, Blocks.BEDROCK.defaultBlockState(), 2);
                  session.arenaBlocks.add(p);
               } else if (dy >= h + 3) {
                  level.setBlock(p, plains ? Blocks.BARRIER.defaultBlockState() : Blocks.BEDROCK.defaultBlockState(), 2);
                  session.arenaBlocks.add(p);
               } else if (dy <= 0) {
                  level.setBlock(p, getThemeFloorBlock(theme, stage, dx, dy, dz), 2);
                  session.arenaBlocks.add(p);
               } else if (dy <= h) {
                  double normY = (double)(dy - 1) / Math.max(1, h - 1);
                  double domeDist = hDist + normY * normY;
                  double noise = Math.sin(dx * 0.42 + stage) * Math.cos(dz * 0.42 + stage) * 0.12;
                  double effective = domeDist + noise;
                  if (inSkylight && dy >= h - 1) {
                     if (dy == h) {
                        if (isSkylightRim) {
                           BlockState leaves = getThemeLeavesBlock(theme, Math.abs(dx * 13 + dz * 17 + dy * 7));
                           level.setBlock(p, leaves, 2);
                        } else {
                           level.setBlock(p, Blocks.BARRIER.defaultBlockState(), 2);
                        }

                        session.arenaBlocks.add(p);
                     } else {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        session.arenaBlocks.add(p);
                        if (isSkylightRim) {
                           BlockState leaves = getThemeLeavesBlock(theme, Math.abs(dx * 13 + dz * 17 + dy * 7));
                           level.setBlock(p, leaves, 2);
                           session.arenaBlocks.add(p);
                        }
                     }
                  } else if (effective < 0.82) {
                     level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                     session.arenaBlocks.add(p);
                     if (dy == 1) {
                        placeFloorDecorations(level, session, p, theme, dx, dz);
                     }
                  } else {
                     level.setBlock(p, getThemeWallBlock(theme, stage, dx, dy, dz, h), 2);
                     session.arenaBlocks.add(p);
                  }
               } else if (inSkylight) {
                  if (isSkylightRim) {
                     BlockState leaves = getThemeLeavesBlock(theme, Math.abs(dx * 13 + dz * 17 + dy * 7));
                     level.setBlock(p, leaves, 2);
                  } else {
                     level.setBlock(p, Blocks.BARRIER.defaultBlockState(), 2);
                  }

                  session.arenaBlocks.add(p);
               } else {
                  level.setBlock(p, getThemeWallBlock(theme, stage, dx, dy, dz, h), 2);
                  session.arenaBlocks.add(p);
               }
            }
         }
      }
   }

   private static void clearThresholdAir(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, double radius, int height
   ) {
      if (center != null) {
         int r = (int)Math.ceil(radius);

         for (int ox = -r; ox <= r; ox++) {
            for (int oz = -r; oz <= r; oz++) {
               if (ox * ox + oz * oz <= radius * radius) {
                  for (int oy = 1; oy <= height; oy++) {
                     BlockPos p = center.offset(ox, oy, oz);
                     level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                     session.arenaBlocks.add(p);
                  }
               }
            }
         }
      }
   }

   private static BlockPos carveOrganic3DWindingTunnel(
      ServerLevel level,
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session,
      BlockPos start,
      BlockPos end,
      double tunnelRadius,
      int tunnelHeight,
      String theme,
      int barrierIndex
   ) {
      double dx = end.getX() - start.getX();
      double dy = end.getY() - start.getY();
      double dz = end.getZ() - start.getZ();
      double horizDist = Math.max(0.01, Math.sqrt(dx * dx + dz * dz));
      double totalDist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      int steps = (int)Math.ceil(totalDist * 1.6);
      steps = Math.max(12, steps);
      double perpX = -dz / horizDist;
      double perpZ = dx / horizDist;
      double sideAmplitude = horizDist <= 34.0 ? 1.0 : 3.0;
      BlockPos midBarrierPos = null;
      int intR = (int)Math.ceil(tunnelRadius + 2.0);

      for (int i = 0; i <= steps; i++) {
         double t = (double)i / steps;
         double meander = Math.sin(t * Math.PI) * sideAmplitude + Math.sin(t * Math.PI * 2.0) * (sideAmplitude * 0.35);
         double cx = start.getX() + dx * t + perpX * meander;
         double cy = start.getY() + dy * t;
         double cz = start.getZ() + dz * t + perpZ * meander;
         int intCx = (int)Math.round(cx);
         int intCy = (int)Math.round(cy);
         int intCz = (int)Math.round(cz);
         if (i == steps / 2) {
            midBarrierPos = new BlockPos(intCx, intCy, intCz);
         }

         for (int ox = -intR; ox <= intR; ox++) {
            for (int oz = -intR; oz <= intR; oz++) {
               double dH = Math.sqrt(ox * ox + oz * oz);
               if (!(dH > tunnelRadius + 2.0)) {
                  for (int oy = -3; oy <= tunnelHeight + 4; oy++) {
                     BlockPos p = new BlockPos(intCx + ox, intCy + oy, intCz + oz);
                     double dist = Math.sqrt(dH * dH + Math.max(0, oy - 2) * Math.max(0, oy - 2));
                     if (oy <= -3) {
                        level.setBlock(p, Blocks.BEDROCK.defaultBlockState(), 2);
                        session.arenaBlocks.add(p);
                     } else if (oy <= 0) {
                        if (!isInsideChamberVolume(session, p)) {
                           level.setBlock(p, getThemeFloorBlock(theme, 1, ox, oy, intCz), 2);
                           session.arenaBlocks.add(p);
                        }
                     } else if (oy >= tunnelHeight + 3) {
                        if (!isInsideChamberVolume(session, p)) {
                           level.setBlock(p, Blocks.BEDROCK.defaultBlockState(), 2);
                           session.arenaBlocks.add(p);
                        }
                     } else if (dist >= tunnelRadius && dist <= tunnelRadius + 2.5 && !isInsideChamberVolume(session, p)) {
                        BlockState curState = level.getBlockState(p);
                        if (!curState.isAir() && !curState.is(Blocks.BEDROCK)) {
                           level.setBlock(p, getThemeWallBlock(theme, 1, ox, oy, intCz, tunnelHeight), 2);
                           session.arenaBlocks.add(p);
                        }
                     }
                  }
               }
            }
         }
      }

      int intAirR = (int)Math.ceil(tunnelRadius);

      for (int i = 0; i <= steps; i++) {
         double t = (double)i / steps;
         double meander = Math.sin(t * Math.PI) * sideAmplitude + Math.sin(t * Math.PI * 2.0) * (sideAmplitude * 0.35);
         double cx = start.getX() + dx * t + perpX * meander;
         double cy = start.getY() + dy * t;
         double cz = start.getZ() + dz * t + perpZ * meander;
         int intCx = (int)Math.round(cx);
         int intCy = (int)Math.round(cy);
         int intCz = (int)Math.round(cz);

         for (int ox = -intAirR; ox <= intAirR; ox++) {
            for (int ozx = -intAirR; ozx <= intAirR; ozx++) {
               double dH = Math.sqrt(ox * ox + ozx * ozx);
               if (!(dH >= tunnelRadius)) {
                  for (int oyx = 1; oyx <= tunnelHeight - 1; oyx++) {
                     BlockPos p = new BlockPos(intCx + ox, intCy + oyx, intCz + ozx);
                     double dist = Math.sqrt(dH * dH + Math.max(0, oyx - 2) * Math.max(0, oyx - 2));
                     if (dist < tunnelRadius - 0.2) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        session.arenaBlocks.add(p);
                     }
                  }
               }
            }
         }
      }

      clearThresholdAir(level, session, start, tunnelRadius + 1.2, tunnelHeight);
      clearThresholdAir(level, session, end, tunnelRadius + 1.2, tunnelHeight);
      return midBarrierPos;
   }

   public static boolean isInsideChamberVolume(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos p) {
      if (session != null && p != null) {
         BlockPos[] rooms = session.getAllRoomCenters();
         if (rooms != null) {
            for (BlockPos c : rooms) {
               if (c != null) {
                  int dx = p.getX() - c.getX();
                  int dy = p.getY() - c.getY();
                  int dz = p.getZ() - c.getZ();
                  int rx = c == session.roomB_Mid || c == session.roomB_Top || c == session.roomB_Right
                     ? 18
                     : (
                        c == session.roomMB_Left || c == session.roomMB_Center
                           ? 16
                           : (
                              c == session.roomP
                                 ? 10
                                 : (c == session.roomL2_6 ? 11 : (c != session.roomC3 && c != session.roomL2_4 ? (c == session.roomL1_4 ? 13 : 12) : 14))
                           )
                     );
                  int h = rx >= 18 ? 13 : (rx >= 16 ? 12 : (rx >= 14 ? 10 : (rx <= 11 ? 8 : 9)));
                  if (dy >= -3 && dy <= h + 3) {
                     double nX = dx / (rx - 1.2);
                     double nZ = dz / (rx - 1.2);
                     if (nX * nX + nZ * nZ < 1.0) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static BlockPos getActiveBarrierPos(com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      if (session == null) {
         return null;
      } else if (!session.barrier1Open) {
         return session.barrier1Pos;
      } else if (!session.barrier2Open) {
         return session.barrier2Pos;
      } else if (!session.barrier3Open) {
         return session.barrier3Pos;
      } else {
         return !session.barrier4Open ? session.barrier4Pos : null;
      }
   }

   private static void enforceEnemyBarrierContainment(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos roomCenter, BlockPos barrierPos
   ) {
      if (session != null && barrierPos != null && roomCenter != null && !session.enemyUUIDs.isEmpty()) {
         double barrierToCenter = barrierPos.distSqr(roomCenter);

         for (UUID enemyId : new ArrayList<>(session.enemyUUIDs)) {
            Entity ent = level.getEntity(enemyId);
            if (ent instanceof LivingEntity mob && mob.isAlive()) {
               double distToCenter = mob.distanceToSqr(roomCenter.getX() + 0.5, mob.getY(), roomCenter.getZ() + 0.5);
               double distToBarrier = mob.distanceToSqr(barrierPos.getX() + 0.5, barrierPos.getY(), barrierPos.getZ() + 0.5);
               if (distToCenter > barrierToCenter + 2.0 && distToBarrier < 2500.0) {
                  double rx = roomCenter.getX() + (level.random.nextDouble() - 0.5) * 8.0;
                  double rz = roomCenter.getZ() + (level.random.nextDouble() - 0.5) * 8.0;
                  double ry = findSafeFloorY(level, rx, roomCenter.getY(), rz);
                  mob.teleportTo(rx, ry, rz);
                  mob.setDeltaMovement(0.0, 0.0, 0.0);
                  level.sendParticles(ParticleTypes.PORTAL, rx, ry + 1.0, rz, 15, 0.3, 0.5, 0.3, 0.1);
               }
            } else if (ent == null || !ent.isAlive()) {
               session.enemyUUIDs.remove(enemyId);
            }
         }
      }
   }

   private static void placeMagicalBarrierThreshold(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos pos, int barrierIndex, String theme
   ) {
      if (barrierIndex > 0) {
         Set<BlockPos> targetSet = switch (barrierIndex) {
            case 1 -> session.barrier1Blocks;
            case 2 -> session.barrier2Blocks;
            case 3 -> session.barrier3Blocks;
            case 4 -> session.barrier4Blocks;
            default -> null;
         };
         if (targetSet != null) {
            switch (barrierIndex) {
               case 1:
                  session.barrier1Pos = pos;
                  break;
               case 2:
                  session.barrier2Pos = pos;
                  break;
               case 3:
                  session.barrier3Pos = pos;
                  break;
               case 4:
                  if (session.barrier4Pos == null) {
                     session.barrier4Pos = pos;
                  }
            }

            int cx = pos.getX();
            int cy = pos.getY();
            int cz = pos.getZ();

            for (int dx = -6; dx <= 6; dx++) {
               for (int dz = -6; dz <= 6; dz++) {
                  if (dx * dx + dz * dz <= 36) {
                     for (int dy = -2; dy <= 9; dy++) {
                        BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                        BlockState st = level.getBlockState(p);
                        if (st.isAir() || !st.isSolid() || st.canBeReplaced() || st.is(Blocks.COBWEB) || st.is(Blocks.WATER) || st.is(Blocks.LAVA)) {
                           level.setBlock(p, Blocks.BARRIER.defaultBlockState(), 2);
                           targetSet.add(p);
                           session.arenaBlocks.add(p);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void buildNaturalBossArena(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, String rank, String theme
   ) {
      if (center != null) {
         boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme);
         BlockState veinBlock = isBaran ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState();

         for (int i = 0; i < 8; i++) {
            int vx = (i % 2 == 0 ? 6 : -6) + i / 2;
            int vz = i % 3 == 0 ? 7 : -7;
            BlockPos vp = center.offset(vx, -1, vz);
            level.setBlock(vp, veinBlock, 2);
            session.arenaBlocks.add(vp);
         }
      }
   }

   private static void dissolveBarrierSweep(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, Set<BlockPos> barrierBlocks, BlockPos barrierPos
   ) {
      if (barrierPos == null) {
         dissolveBarrierSweep(level, session, barrierBlocks, session.arenaCenter.getZ() + 20);
      } else {
         int cx = barrierPos.getX();
         int cy = barrierPos.getY();
         int cz = barrierPos.getZ();

         for (BlockPos p : barrierBlocks) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            level.sendParticles(ParticleTypes.FLASH, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.0);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.05);
         }

         barrierBlocks.clear();

         for (int dx = -7; dx <= 7; dx++) {
            for (int dy = -3; dy <= 10; dy++) {
               for (int dz = -7; dz <= 7; dz++) {
                  BlockPos bp = new BlockPos(cx + dx, cy + dy, cz + dz);
                  if (level.getBlockState(bp).is(Blocks.BARRIER)) {
                     level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                  }
               }
            }
         }
      }
   }

   private static void dissolveBarrierSweep(
      ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, Set<BlockPos> barrierBlocks, int barrierZ
   ) {
      int cx = session.arenaCenter.getX();
      int cy = session.arenaCenter.getY();

      for (BlockPos p : barrierBlocks) {
         level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
         level.sendParticles(ParticleTypes.FLASH, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.0);
         level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.05);
      }

      barrierBlocks.clear();

      for (int dx = -6; dx <= 6; dx++) {
         for (int dy = 0; dy <= 8; dy++) {
            for (int dz = barrierZ - 3; dz <= barrierZ + 3; dz++) {
               BlockPos bp = new BlockPos(cx + dx, cy + dy, dz);
               if (level.getBlockState(bp).is(Blocks.BARRIER)) {
                  level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
               }
            }
         }
      }
   }

   public static void spawnPhase1Enemies(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      session.enemyUUIDs.clear();
      BlockPos center = session.roomC1 != null ? session.roomC1 : session.arenaCenter;
      String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
      int tier = getRankTier(session.rank);
      double tierRatio = Math.max(0.3, tier / 7.0);
      double hpMult = (1.0 + Math.max(0, tier - 3) * 0.35) * tierRatio;
      double dmgMult = (1.0 + Math.max(0, tier - 3) * 0.25) * tierRatio;
      int count = 6 + Math.min(tier, 4);

      for (int i = 0; i < count; i++) {
         double angle = (Math.PI * 2) * i / count;
         double dist = 4.0 + i % 3 * 1.5;
         double sx = center.getX() + Math.cos(angle) * dist;
         double sz = center.getZ() + Math.sin(angle) * dist;
         double sy = findSafeFloorY(level, sx, center.getY(), sz);
         Monster mob;
         if ("baran".equalsIgnoreCase(theme)) {
            if (i % 2 == 0) {
               AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
               orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.flame_demon_warrior"));
               buffMob(orc, 180.0 * hpMult, 12.0 * dmgMult, 10.0, 0.4);
               mob = orc;
            } else {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.baran_fiery_legionnaire"));
               buffMob(infantry, 140.0 * hpMult, 10.0 * dmgMult, 8.0, 0.3);
               mob = infantry;
            }
         } else if ("igris".equalsIgnoreCase(theme)) {
            if (i % 2 == 0) {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_infantry"));
               buffMob(infantry, 150.0 * hpMult, 10.0 * dmgMult, 8.0, 0.3);
               mob = infantry;
            } else {
               ShadowInfantryEntity inf = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               inf.setCustomName(Component.translatable("awakening_system.dungeon.mob.igris_crimson_knight"));
               inf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
               buffMob(inf, 140.0 * hpMult, 12.0 * dmgMult, 8.0, 0.3);
               mob = inf;
            }
         } else {
            mob = createFloorMinion(level, session.rank, 1, i);
         }

         applyDungeonMobMetadata(mob, session.rank, 1, false);
         mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
         mob.setPersistenceRequired();
         mob.setCanPickUpLoot(false);
         level.addFreshEntity(mob);
         session.enemyUUIDs.add(mob.getUUID());
      }

      if (session.deadEndCentralChamber != null) {
         for (int k = 0; k < 3; k++) {
            BlockPos p = session.deadEndCentralChamber.offset(k == 0 ? -2 : (k == 1 ? 2 : 0), 0, k == 2 ? 2 : -1);
            Monster ambushMob = createFloorMinion(level, session.rank, 1, k);
            applyDungeonMobMetadata(ambushMob, session.rank, 1, false);
            double sy = findSafeFloorY(level, p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
            ambushMob.moveTo(p.getX() + 0.5, sy, p.getZ() + 0.5, 180.0F, 0.0F);
            ambushMob.setPersistenceRequired();
            ambushMob.setCanPickUpLoot(false);
            level.addFreshEntity(ambushMob);
            session.enemyUUIDs.add(ambushMob.getUUID());
         }
      }

      session.totalEnemies = session.enemyUUIDs.size();
   }

   public static void spawnPhase2Enemies(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      session.enemyUUIDs.clear();
      BlockPos center = session.room2Center != null ? session.room2Center : session.arenaCenter.offset(0, 0, 40);
      String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
      int tier = getRankTier(session.rank);
      double tierRatio = Math.max(0.3, tier / 7.0);
      double hpMult = (1.0 + Math.max(0, tier - 3) * 0.4) * tierRatio;
      double dmgMult = (1.0 + Math.max(0, tier - 3) * 0.3) * tierRatio;
      int count = 8 + Math.min(tier, 5);
      List<BlockPos> spawnPatrols = new ArrayList<>();
      spawnPatrols.add(center.offset(-6, 0, 5));
      spawnPatrols.add(center.offset(6, 0, 5));
      spawnPatrols.add(center.offset(-6, 0, -4));
      spawnPatrols.add(center.offset(6, 0, -4));
      if (session.sideWestChamber != null) {
         spawnPatrols.add(session.sideWestChamber.offset(0, 0, 0));
         spawnPatrols.add(session.sideWestChamber.offset(-4, 0, 3));
         spawnPatrols.add(session.sideWestChamber.offset(4, 0, -3));
      } else {
         spawnPatrols.add(center.offset(-8, 0, 0));
         spawnPatrols.add(center.offset(-9, 0, 3));
      }

      if (session.sideEastChamber != null) {
         spawnPatrols.add(session.sideEastChamber.offset(0, 0, 0));
         spawnPatrols.add(session.sideEastChamber.offset(-3, 0, 3));
         spawnPatrols.add(session.sideEastChamber.offset(3, 0, -3));
      } else {
         spawnPatrols.add(center.offset(8, 0, 0));
         spawnPatrols.add(center.offset(9, 0, 3));
      }

      for (int i = 0; i < count; i++) {
         BlockPos patrolTarget = spawnPatrols.get(i % spawnPatrols.size());
         double sx = patrolTarget.getX() + (level.random.nextDouble() - 0.5) * 2.2;
         double sz = patrolTarget.getZ() + (level.random.nextDouble() - 0.5) * 2.2;
         double sy = findSafeFloorY(level, sx, patrolTarget.getY(), sz);
         Monster mob;
         if ("baran".equalsIgnoreCase(theme)) {
            FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
            assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_demon_assassin"));
            buffMob(assassin, 200.0 * hpMult, 15.0 * dmgMult, 10.0, 0.4);
            mob = assassin;
         } else if ("igris".equalsIgnoreCase(theme)) {
            if (i % 2 == 0) {
               FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
               assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.throne_crimson_assassin"));
               buffMob(assassin, 210.0 * hpMult, 16.0 * dmgMult, 10.0, 0.4);
               mob = assassin;
            } else {
               ShadowInfantryEntity inf = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               inf.setCustomName(Component.translatable("awakening_system.dungeon.mob.crypt_veteran"));
               inf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
               buffMob(inf, 190.0 * hpMult, 15.0 * dmgMult, 10.0, 0.35);
               mob = inf;
            }
         } else {
            mob = createFloorMinion(level, session.rank, 2, i);
         }

         applyDungeonMobMetadata(mob, session.rank, 2, i == 0);
         mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
         mob.setPersistenceRequired();
         mob.setCanPickUpLoot(false);
         level.addFreshEntity(mob);
         session.enemyUUIDs.add(mob.getUUID());
      }

      session.totalEnemies = session.enemyUUIDs.size();
      MinecraftServer server = getServer();
      if (server != null) {
         for (ServerPlayer sp : session.getAllPlayers(server)) {
            sp.displayClientMessage(Component.translatable("dungeon.awakening_system.phase2_title"), false);
            sp.displayClientMessage(Component.translatable("dungeon.awakening_system.phase2_desc"), false);
         }
      }
   }

   public static void spawnPhase3Enemies(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      session.enemyUUIDs.clear();
      BlockPos center = session.room3Center != null ? session.room3Center : session.arenaCenter.offset(0, 0, 84);
      String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
      int tier = getRankTier(session.rank);
      double tierRatio = Math.max(0.3, tier / 7.0);
      double hpMult = (1.0 + Math.max(0, tier - 3) * 0.45) * tierRatio;
      double dmgMult = (1.0 + Math.max(0, tier - 3) * 0.35) * tierRatio;
      int count = 7 + Math.min(tier, 5);
      List<BlockPos> spawnPatrols = new ArrayList<>();
      spawnPatrols.add(center);
      spawnPatrols.add(center.offset(-3, 0, 2));
      spawnPatrols.add(center.offset(3, 0, 2));
      spawnPatrols.add(center.offset(-7, 0, 6));
      spawnPatrols.add(center.offset(7, 0, 6));
      spawnPatrols.add(center.offset(-6, 0, -5));
      if (session.sideEastChamber != null) {
         spawnPatrols.add(session.sideEastChamber.offset(0, 0, 0));
         spawnPatrols.add(session.sideEastChamber.offset(4, 0, 3));
      } else {
         spawnPatrols.add(center.offset(8, 0, 0));
      }

      if (session.secretTreasureChamber != null) {
         spawnPatrols.add(session.secretTreasureChamber.offset(0, 0, 0));
      } else {
         spawnPatrols.add(center.offset(-8, 0, -3));
      }

      for (int i = 0; i < count; i++) {
         BlockPos patrolTarget = spawnPatrols.get(i % spawnPatrols.size());
         double sx = patrolTarget.getX() + (level.random.nextDouble() - 0.5) * 2.0;
         double sz = patrolTarget.getZ() + (level.random.nextDouble() - 0.5) * 2.0;
         double sy = findSafeFloorY(level, sx, patrolTarget.getY(), sz);
         Monster mob;
         if ("baran".equalsIgnoreCase(theme)) {
            if (i == 0) {
               AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
               orc.setCustomName(
                  Component.translatable("awakening_system.dungeon.mob.mini_boss_prefix")
                     .append(Component.translatable("awakening_system.dungeon.mob.baran_thunder_commander"))
               );
               buffMob(orc, 450.0 * hpMult, 20.0 * dmgMult, 14.0, 0.7);
               mob = orc;
            } else {
               AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
               orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.pit_elite_guard"));
               buffMob(orc, 240.0 * hpMult, 16.0 * dmgMult, 10.0, 0.45);
               mob = orc;
            }
         } else if ("igris".equalsIgnoreCase(theme)) {
            if (i == 0) {
               AbyssalOrcEntity orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
               orc.setCustomName(
                  Component.translatable("awakening_system.dungeon.mob.mini_boss_prefix")
                     .append(Component.translatable("awakening_system.dungeon.mob.crimson_guard_captain"))
               );
               buffMob(orc, 420.0 * hpMult, 19.0 * dmgMult, 14.0, 0.65);
               mob = orc;
            } else {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.pit_crimson_guardian"));
               buffMob(infantry, 220.0 * hpMult, 15.0 * dmgMult, 10.0, 0.4);
               mob = infantry;
            }
         } else {
            mob = createFloorMinion(level, session.rank, 3, i);
         }

         boolean isMiniBoss = i == 0 && ("baran".equalsIgnoreCase(theme) || "igris".equalsIgnoreCase(theme));
         applyDungeonMobMetadata(mob, session.rank, 3, isMiniBoss);
         mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
         mob.setPersistenceRequired();
         mob.setCanPickUpLoot(false);
         level.addFreshEntity(mob);
         session.enemyUUIDs.add(mob.getUUID());
      }

      session.totalEnemies = session.enemyUUIDs.size();
      MinecraftServer server = getServer();
      if (server != null) {
         for (ServerPlayer sp : session.getAllPlayers(server)) {
            sp.displayClientMessage(Component.translatable("dungeon.awakening_system.phase3_title"), false);
            sp.displayClientMessage(Component.translatable("dungeon.awakening_system.phase3_desc"), false);
         }
      }
   }

   public static void spawnPhase4Enemies(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      session.enemyUUIDs.clear();
      BlockPos center = session.room4Center != null ? session.room4Center : session.arenaCenter.offset(0, 0, 135);
      String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
      int tier = getRankTier(session.rank);
      double tierRatio = Math.max(0.3, tier / 7.0);
      double hpMult = (1.0 + Math.max(0, tier - 3) * 0.5) * tierRatio;
      double dmgMult = (1.0 + Math.max(0, tier - 3) * 0.4) * tierRatio;
      int count = 8 + Math.min(tier, 5);
      List<BlockPos> spawnPatrols = new ArrayList<>();
      spawnPatrols.add(center.offset(0, 0, 4));
      spawnPatrols.add(center.offset(-5, 0, 2));
      spawnPatrols.add(center.offset(5, 0, 2));
      spawnPatrols.add(center.offset(-7, 0, 7));
      spawnPatrols.add(center.offset(7, 0, 7));
      spawnPatrols.add(center.offset(-6, 0, -4));
      spawnPatrols.add(center.offset(6, 0, -4));

      for (int i = 0; i < count; i++) {
         BlockPos patrolTarget = spawnPatrols.get(i % spawnPatrols.size());
         double sx = patrolTarget.getX() + (level.random.nextDouble() - 0.5) * 2.0;
         double sz = patrolTarget.getZ() + (level.random.nextDouble() - 0.5) * 2.0;
         double sy = findSafeFloorY(level, sx, patrolTarget.getY(), sz);
         Monster mob;
         if ("baran".equalsIgnoreCase(theme)) {
            if (i % 2 == 0) {
               AbyssalNecromancerEntity necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
               necro.setCustomName(Component.translatable("awakening_system.dungeon.mob.arch_lightning_necromancer"));
               buffMob(necro, 300.0 * hpMult, 16.0 * dmgMult, 10.0, 0.45);
               mob = necro;
            } else {
               FrostElfAssassinEntity assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
               assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.cathedral_executioner"));
               buffMob(assassin, 250.0 * hpMult, 18.0 * dmgMult, 11.0, 0.45);
               mob = assassin;
            }
         } else if ("igris".equalsIgnoreCase(theme)) {
            if (i % 2 == 0) {
               AbyssalNecromancerEntity necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
               necro.setCustomName(Component.translatable("awakening_system.dungeon.mob.crimson_blood_necromancer"));
               buffMob(necro, 300.0 * hpMult, 16.0 * dmgMult, 10.0, 0.45);
               mob = necro;
            } else {
               ShadowInfantryEntity infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
               infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.imperial_guard_paladin"));
               buffMob(infantry, 260.0 * hpMult, 17.0 * dmgMult, 12.0, 0.45);
               mob = infantry;
            }
         } else {
            mob = createFloorMinion(level, session.rank, 4, i);
         }

         applyDungeonMobMetadata(mob, session.rank, 4, false);
         mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
         mob.setPersistenceRequired();
         mob.setCanPickUpLoot(false);
         level.addFreshEntity(mob);
         session.enemyUUIDs.add(mob.getUUID());
      }

      session.totalEnemies = session.enemyUUIDs.size();
      MinecraftServer server = getServer();
      if (server != null) {
         for (ServerPlayer sp : session.getAllPlayers(server)) {
            sp.displayClientMessage(Component.translatable("dungeon.awakening_system.phase4_title"), false);
            sp.displayClientMessage(Component.translatable("dungeon.awakening_system.phase4_desc"), false);
         }
      }
   }

   public static void spawnSanctumBoss(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos center, int bossIndex) {
      if (level != null && session != null && center != null) {
         int tier = getRankTier(session.rank);
         BlockPos spawnP = center.offset(0, 0, -2);
         double hp = getDungeonBossHp(tier, session.isRedGate) * 0.85;
         double dmg = getDungeonBossDmg(tier, session.isRedGate) * 0.85;
         double armor = Math.min(16.0, 4.0 + tier * 1.5);
         double kb = Math.min(0.8, 0.2 + tier * 0.06);
         int bType = bossIndex % 3;
         Monster boss;
         if (bType == 0) {
            BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
            b.setCustomName(Component.translatable("awakening_system.dungeon.boss.major_baran"));
            b.setCustomNameVisible(true);
            buffMob(b, hp, dmg, armor, kb);
            boss = b;
         } else if (bType == 1) {
            IgrisBossEntity igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
            igris.setCustomName(Component.translatable("awakening_system.dungeon.boss.major_igris"));
            igris.setCustomNameVisible(true);
            buffMob(igris, hp, dmg, armor, kb);
            boss = igris;
         } else {
            BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), level);
            beru.setCustomName(Component.translatable("awakening_system.dungeon.boss.major_beru"));
            beru.setCustomNameVisible(true);
            buffMob(beru, hp, dmg, armor, kb);
            boss = beru;
         }

         double sy = findSafeFloorY(level, spawnP.getX() + 0.5, spawnP.getY(), spawnP.getZ() + 0.5);
         boss.moveTo(spawnP.getX() + 0.5, sy, spawnP.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         boss.setPersistenceRequired();
         boss.setCanPickUpLoot(false);
         applyDungeonMobMetadata(boss, session.rank, 5, session.isRedGate);
         level.addFreshEntity(boss);
         session.enemyUUIDs.add(boss.getUUID());
      }
   }

   public static void spawnPhase5Boss(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      BlockPos pos = session.thronePos != null ? session.thronePos.offset(0, 0, -2) : session.room5Center.offset(0, 2, 11);
      String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
      int tier = getRankTier(session.rank);
      double bossArmor = Math.min(18.0, 6.0 + tier * 1.8);
      double bossKb = Math.min(0.9, 0.3 + tier * 0.08);
      double hp = getDungeonBossHp(tier, session.isRedGate);
      double dmg = getDungeonBossDmg(tier, session.isRedGate);
      Component prefix = session.isRedGate
         ? Component.translatable("awakening_system.dungeon.mob.anomaly_prefix", new Object[]{session.rank})
         : Component.translatable("awakening_system.dungeon.mob.boss_prefix", new Object[]{session.rank});
      Monster boss;
      if ("baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme) || "magma_forge".equalsIgnoreCase(theme)) {
         BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
         b.setCustomName(prefix.copy().append(Component.translatable("awakening_system.dungeon.boss.baran_flames")));
         b.setCustomNameVisible(true);
         buffMob(b, hp, dmg, bossArmor, bossKb);
         boss = b;
      } else if ("igris".equalsIgnoreCase(theme) || "crypt".equalsIgnoreCase(theme) || "castle".equalsIgnoreCase(theme)) {
         IgrisBossEntity igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
         igris.setCustomName(prefix.copy().append(Component.translatable("awakening_system.dungeon.boss.igris_commander")));
         igris.setCustomNameVisible(true);
         buffMob(igris, hp, dmg, bossArmor, bossKb);
         boss = igris;
      } else if (!"beru".equalsIgnoreCase(theme)
         && !"jeju".equalsIgnoreCase(theme)
         && !"ant".equalsIgnoreCase(theme)
         && !"insect".equalsIgnoreCase(theme)
         && !"hive".equalsIgnoreCase(theme)) {
         int roll = level.random.nextInt(3);
         if (roll == 0) {
            BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
            b.setCustomName(prefix.copy().append(Component.translatable("awakening_system.dungeon.boss.baran_flames")));
            b.setCustomNameVisible(true);
            buffMob(b, hp, dmg, bossArmor, bossKb);
            boss = b;
         } else if (roll == 1) {
            IgrisBossEntity igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
            igris.setCustomName(prefix.copy().append(Component.translatable("awakening_system.dungeon.boss.igris_commander")));
            igris.setCustomNameVisible(true);
            buffMob(igris, hp, dmg, bossArmor, bossKb);
            boss = igris;
         } else {
            BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), level);
            beru.setCustomName(prefix.copy().append(Component.translatable("awakening_system.dungeon.boss.beru_predator")));
            beru.setCustomNameVisible(true);
            buffMob(beru, hp, dmg, bossArmor, bossKb);
            boss = beru;
         }
      } else {
         BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), level);
         beru.setCustomName(prefix.copy().append(Component.translatable("awakening_system.dungeon.boss.beru_predator")));
         beru.setCustomNameVisible(true);
         buffMob(beru, hp, dmg, bossArmor, bossKb);
         boss = beru;
      }

      applyDungeonMobMetadata(boss, session.rank, 5, true);
      boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180.0F, 0.0F);
      boss.setPersistenceRequired();
      boss.setCanPickUpLoot(false);
      level.addFreshEntity(boss);
      session.bossUUID = boss.getUUID();
      session.enemyUUIDs.add(boss.getUUID());

      for (int k = 0; k < 4; k++) {
         int ox = k == 0 ? -5 : (k == 1 ? 5 : 0);
         int oz = k == 2 ? 5 : (k == 3 ? -5 : 0);
         Monster guard = createFloorMinion(level, session.rank, 4, k);
         if (guard != null) {
            applyDungeonMobMetadata(guard, session.rank, 4, false);
            double gSy = findSafeFloorY(level, pos.getX() + ox + 0.5, pos.getY(), pos.getZ() + oz + 0.5);
            guard.moveTo(pos.getX() + ox + 0.5, gSy, pos.getZ() + oz + 0.5, 180.0F, 0.0F);
            guard.setPersistenceRequired();
            guard.setCanPickUpLoot(false);
            level.addFreshEntity(guard);
            session.enemyUUIDs.add(guard.getUUID());
         }
      }

      level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.2F, 0.8F);
      level.sendParticles(ParticleTypes.FLASH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 3, 0.0, 0.0, 0.0, 0.0);
   }

   public static void openPhase1Barrier(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, ServerPlayer player) {
      MinecraftServer server = getServer();
      if (server != null) {
         ServerLevel level = server.getLevel(session.dungeonDimension);
         if (level != null) {
            session.barrier1Open = true;
            dissolveBarrierSweep(level, session, session.barrier1Blocks, session.barrier1Pos);

            for (ServerPlayer sp : session.getAllPlayers(server)) {
               if (sp != null) {
                  sp.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.0F);
                  sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.2F);
                  com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                     sp, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_1_unlocked", -11470213, 1
                  );
               }
            }

            session.currentFloor = 2;
            spawnPhase2Enemies(level, session);
            session.combatTimer = Math.min(session.combatTimer + 1800, 5400);
            session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
            if (session.bossEvent != null) {
               session.bossEvent.setProgress(1.0F);
               int sec = Math.max(0, (session.combatTimer + 19) / 20);
               String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
               session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 2, session.totalEnemies, timeStr));
            }
         }
      }
   }

   public static void openPhase2Barrier(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, ServerPlayer player) {
      MinecraftServer server = getServer();
      if (server != null) {
         ServerLevel level = server.getLevel(session.dungeonDimension);
         if (level != null) {
            session.barrier2Open = true;
            dissolveBarrierSweep(level, session, session.barrier2Blocks, session.barrier2Pos);

            for (ServerPlayer sp : session.getAllPlayers(server)) {
               if (sp != null) {
                  sp.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 0.8F);
                  com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                     sp, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_2_unlocked", -11470213, 2
                  );
               }
            }

            session.currentFloor = 3;
            spawnPhase3Enemies(level, session);
            session.combatTimer = Math.min(session.combatTimer + 2400, 6000);
            session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
            if (session.bossEvent != null) {
               session.bossEvent.setProgress(1.0F);
               int sec = Math.max(0, (session.combatTimer + 19) / 20);
               String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
               session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 3, session.totalEnemies, timeStr));
            }
         }
      }
   }

   public static void openPhase3Barrier(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, ServerPlayer player) {
      MinecraftServer server = getServer();
      if (server != null) {
         ServerLevel level = server.getLevel(session.dungeonDimension);
         if (level != null) {
            session.barrier3Open = true;
            dissolveBarrierSweep(level, session, session.barrier3Blocks, session.barrier3Pos);

            for (ServerPlayer sp : session.getAllPlayers(server)) {
               if (sp != null) {
                  sp.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 0.6F);
                  com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                     sp, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_3_unlocked", -11470213, 3
                  );
               }
            }

            session.currentFloor = 4;
            spawnPhase4Enemies(level, session);
            session.combatTimer = Math.min(session.combatTimer + 2400, 6600);
            session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
            if (session.bossEvent != null) {
               session.bossEvent.setProgress(1.0F);
               int sec = Math.max(0, (session.combatTimer + 19) / 20);
               String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
               session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 4, session.totalEnemies, timeStr));
            }
         }
      }
   }

   public static void openPhase4Barrier(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, ServerPlayer player) {
      MinecraftServer server = getServer();
      if (server != null) {
         ServerLevel level = server.getLevel(session.dungeonDimension);
         if (level != null) {
            session.barrier4Open = true;
            dissolveBarrierSweep(level, session, session.barrier4Blocks, session.barrier4Pos);

            for (ServerPlayer sp : session.getAllPlayers(server)) {
               if (sp != null) {
                  sp.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0F, 0.75F);
                  com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                     sp, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_4_unlocked", -48060, 4
                  );
               }
            }

            session.currentFloor = 5;
            spawnPhase5Boss(level, session);
            int tier = getRankTier(session.rank);
            int bossTimer = getPhase5BossTimer(tier, session.isRedGate);
            session.combatTimer = Math.max(session.combatTimer, bossTimer);
            session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
            if (session.bossEvent != null) {
               session.bossEvent.setProgress(1.0F);
               session.bossEvent.setColor(session.isRedGate ? BossBarColor.RED : BossBarColor.PURPLE);
               int sec = Math.max(0, (session.combatTimer + 19) / 20);
               String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
               session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 5, 1, timeStr));
            }
         }
      }
   }

   private static void buildDungeonCoreDais(ServerLevel level, com.soybe.awakeningsystem.DungeonManager.DungeonSession session, BlockPos corePos, String theme) {
      boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme);
      BlockState foundationBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
      BlockState centerBlock = isBaran ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
      BlockState slabBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState() : Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState();
      BlockState cornerPillar = isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
      BlockPos basePos = corePos.below();

      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            for (int dy = 0; dy <= 4; dy++) {
               BlockPos pAir = corePos.offset(dx, dy, dz);
               level.setBlock(pAir, Blocks.AIR.defaultBlockState(), 2);
               session.arenaBlocks.add(pAir);
            }
         }
      }

      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            for (int dy = -2; dy <= -1; dy++) {
               BlockPos pSub = basePos.offset(dx, dy, dz);
               level.setBlock(pSub, foundationBlock, 2);
               session.arenaBlocks.add(pSub);
            }
         }
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            BlockPos pPlat = basePos.offset(dx, 0, dz);
            if (dx == 0 && dz == 0) {
               level.setBlock(pPlat, centerBlock, 2);
            } else if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
               level.setBlock(pPlat, cornerPillar, 2);
            } else {
               level.setBlock(pPlat, foundationBlock, 2);
            }

            session.arenaBlocks.add(pPlat);
         }
      }

      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
               BlockPos pBorder = basePos.offset(dx, 0, dz);
               level.setBlock(pBorder, slabBlock, 2);
               session.arenaBlocks.add(pBorder);
            }
         }
      }

      int[][] corners = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

      for (int[] c : corners) {
         BlockPos lanternPos = corePos.offset(c[0], 0, c[1]);
         level.setBlock(lanternPos, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
         session.arenaBlocks.add(lanternPos);
      }

      level.setBlock(
         corePos,
         (BlockState)((BlockState)((DungeonCoreBlock)SystemBlocks.DUNGEON_CORE.get()).defaultBlockState().setValue(DungeonCoreBlock.ACTIVATED, false))
            .setValue(DungeonCoreBlock.CLAIMED, false),
         3
      );
      session.arenaBlocks.add(corePos);
   }

   private static void completeDungeonCombat(com.soybe.awakeningsystem.DungeonManager.DungeonSession session, ServerPlayer player) {
      MinecraftServer server = getServer();
      if (server != null) {
         ServerLevel level = server.getLevel(session.dungeonDimension);
         if (level != null) {
            BlockPos throne = session.thronePos != null ? session.thronePos : (session.room5Center != null ? session.room5Center : session.arenaCenter);
            int floorY = session.room5Center != null ? session.room5Center.getY() : throne.getY() - 2;
            BlockPos daisBase = new BlockPos(throne.getX(), floorY, throne.getZ() - 7);
            BlockPos altarPos = daisBase.above();
            session.chestPos = altarPos;
            session.chestSpawned = true;
            String theme = session.biome != null ? session.biome.toLowerCase(Locale.ROOT) : "crypt";
            buildDungeonCoreDais(level, session, altarPos, theme);
            if (session.isRedGate && session.exitPortalPos != null) {
               for (int dx = -3; dx <= 3; dx++) {
                  for (int dy = 1; dy <= 7; dy++) {
                     level.setBlock(session.exitPortalPos.offset(dx, dy, 0), Blocks.AIR.defaultBlockState(), 3);
                  }
               }

               level.playSound(
                  null,
                  session.exitPortalPos.getX(),
                  session.exitPortalPos.getY() + 2,
                  session.exitPortalPos.getZ(),
                  SoundEvents.GLASS_BREAK,
                  SoundSource.BLOCKS,
                  1.2F,
                  0.8F
               );
               level.playSound(
                  null,
                  session.exitPortalPos.getX(),
                  session.exitPortalPos.getY() + 2,
                  session.exitPortalPos.getZ(),
                  SoundEvents.BEACON_ACTIVATE,
                  SoundSource.BLOCKS,
                  1.0F,
                  1.2F
               );
               level.sendParticles(
                  ParticleTypes.EXPLOSION,
                  session.exitPortalPos.getX() + 0.5,
                  session.exitPortalPos.getY() + 2.0,
                  session.exitPortalPos.getZ() + 0.5,
                  15,
                  0.8,
                  1.0,
                  0.5,
                  0.1
               );
               if (player != null) {
                  com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                     player, "awakening_system.notification.seal_broken.title", "awakening_system.notification.seal_broken.desc", -11470213
                  );
                  player.displayClientMessage(Component.translatable("awakening_system.action.red_gate_open"), true);
               }
            }

            session.combatActive = false;

            for (ServerPlayer p : session.getAllPlayers(server)) {
               p.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.SYSTEM_QUEST_COMPLETE.value(), SoundSource.PLAYERS, 1.2F, 1.0F);
               p.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.SYSTEM_RANK_UP.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
               p.displayClientMessage(Component.translatable("awakening_system.action.dungeon_cleared"), true);
               com.soybe.awakeningsystem.SystemEvents.triggerConstellationReaction(p, com.soybe.awakeningsystem.ConstellationDefinitions.ReactionType.BOSS_KILL);
               com.soybe.awakeningsystem.SystemEvents.grantFable(p, "dungeon_initiate");
               if (getRankTier(session.rank) >= 10) {
                  com.soybe.awakeningsystem.SystemEvents.grantFable(p, "gate_breaker_s_rank");
               }

               if (session.isRedGate) {
                  com.soybe.awakeningsystem.ConstellationAscension.checkRedGateClear(p, session.rank);
               }
            }

            if (session.bossEvent != null) {
               session.bossEvent.setColor(BossBarColor.GREEN);
               session.bossEvent.setProgress(1.0F);
               session.bossEvent.setName(Component.translatable("awakening_system.dungeon.total_victory"));
            }

            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, altarPos.getX() + 0.5, altarPos.getY() + 1.2, altarPos.getZ() + 0.5, 120, 0.8, 1.2, 0.8, 0.4);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, altarPos.getX() + 0.5, altarPos.getY() + 0.6, altarPos.getZ() + 0.5, 60, 0.5, 0.6, 0.5, 0.05);
            level.sendParticles(ParticleTypes.PORTAL, altarPos.getX() + 0.5, altarPos.getY() + 1.0, altarPos.getZ() + 0.5, 80, 0.6, 0.8, 0.6, 0.1);
         }
      }
   }

   public static boolean onRightClickChest(ServerPlayer player, BlockPos clickedPos) {
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session = SESSIONS.get(player.getUUID());
      if (session == null) {
         for (com.soybe.awakeningsystem.DungeonManager.DungeonSession s : SESSIONS.values()) {
            if (s.dungeonDimension.equals(player.level().dimension())
               && (
                  s.chestPos != null && s.chestPos.equals(clickedPos)
                     || s.chestPos != null && s.chestPos.distSqr(clickedPos) <= 16.0
                     || s.arenaCenter.distSqr(clickedPos) <= 40000.0
               )) {
               session = s;
               s.coopPlayers.add(player.getUUID());
               SESSIONS.put(player.getUUID(), s);
               if (s.bossEvent != null) {
                  s.bossEvent.addPlayer(player);
               }
               break;
            }
         }
      }

      if (session == null) {
         return false;
      } else {
         if (session.exitPortalPos != null
            && Math.abs(clickedPos.getX() - session.exitPortalPos.getX()) <= 2
            && Math.abs(clickedPos.getZ() - session.exitPortalPos.getZ()) <= 2
            && clickedPos.getY() >= session.exitPortalPos.getY()
            && clickedPos.getY() <= session.exitPortalPos.getY() + 4) {
            MinecraftServer server = player.getServer();
            if (server != null) {
               if (session.isRedGate && !session.chestSpawned && !session.chestClaimed) {
                  player.displayClientMessage(Component.translatable("awakening_system.action.red_gate_blocked"), true);
                  player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 1.0F, 0.8F);
                  player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 0.7F);
               } else {
                  if (session.awaitingGambleDecision && !session.chestClaimed) {
                     claimDungeonRewardSafe(player);
                  } else if (session.chestClaimed && !session.claimedPlayers.contains(player.getUUID())) {
                     deliverRewardsToPlayer(player, session);
                     session.claimedPlayers.add(player.getUUID());
                     player.sendSystemMessage(Component.translatable("dungeon.awakening_system.reward_secured_chat"));
                     player.displayClientMessage(Component.translatable("dungeon.awakening_system.reward_secured"), true);
                  }

                  player.displayClientMessage(Component.translatable("awakening_system.action.crossing_exit"), true);
                  exitDungeon(server, player.getUUID());
               }

               return true;
            }
         }

         if (!session.chestSpawned) {
            return false;
         } else if (session.chestPos == null || !session.chestPos.equals(clickedPos)) {
            return false;
         } else if (session.chestClaimed) {
            if (session.claimedPlayers.contains(player.getUUID())) {
               player.displayClientMessage(Component.translatable("dungeon.awakening_system.player_already_claimed"), true);
               player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            } else {
               deliverRewardsToPlayer(player, session);
               session.claimedPlayers.add(player.getUUID());
               player.sendSystemMessage(Component.translatable("dungeon.awakening_system.reward_secured_chat"));
               player.displayClientMessage(Component.translatable("dungeon.awakening_system.reward_secured"), true);
               player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
               player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.ROULETTE_WIN.value(), SoundSource.PLAYERS, 1.2F, 1.0F);
            }

            return true;
         } else if (session.rouletteSpinning) {
            player.displayClientMessage(Component.translatable("awakening_system.action.roulette_spinning"), true);
            return true;
         } else if (session.awaitingGambleDecision) {
            if (player.isShiftKeyDown()) {
               gambleDungeonReward(player);
            } else {
               claimDungeonRewardSafe(player);
            }

            return true;
         } else {
            startInWorldRoulette(player, session);
            return true;
         }
      }
   }

   private static void startInWorldRoulette(ServerPlayer player, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      session.rouletteSpinning = true;
      session.rouletteTicks = 0;
      session.claimedTicks = 0;
      session.returnCountdown = -1;
      if (session.bossEvent != null) {
         session.bossEvent.setColor(BossBarColor.YELLOW);
         session.bossEvent.setProgress(1.0F);
         session.bossEvent.setName(Component.translatable("awakening_system.action.roulette_spinning_short"));
      }

      ServerLevel level = (ServerLevel)player.level();
      RandomSource rng = player.getRandom();
      int tier = getRankTier(session.rank);

      int coinsWon = switch (tier) {
         case 1 -> 2000;
         case 2 -> 5000;
         case 3 -> 15000;
         case 4 -> 35000;
         case 5 -> 75000;
         case 6 -> 140000;
         case 7 -> 250000;
         case 8 -> 320000;
         case 9 -> 420000;
         case 10 -> 550000;
         case 11 -> 700000;
         case 12 -> 900000;
         case 13 -> 1200000;
         default -> 1000;
      };

      int expWon = switch (tier) {
         case 1 -> 500;
         case 2 -> 900;
         case 3 -> 1600;
         case 4 -> 3200;
         case 5 -> 6000;
         case 6 -> 9000;
         case 7 -> 14000;
         case 8 -> 20000;
         case 9 -> 26000;
         case 10 -> 38000;
         case 11 -> 55000;
         case 12 -> 85000;
         case 13 -> 120000;
         default -> 250;
      };
      session.finalCoinsWon = coinsWon;
      session.finalExpWon = expWon;
      session.finalRewardStacks.clear();
      List<ItemStack> pool = new ArrayList<>();
      if (tier >= 12) {
         pool.add(new ItemStack(Items.NETHERITE_BLOCK, tier >= 13 ? 8 : 5));
         pool.add(new ItemStack(Items.MACE, 1));
         pool.add(new ItemStack(Items.HEAVY_CORE, tier >= 13 ? 4 : 3));
         pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, tier >= 13 ? 32 : 24));
         pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, tier >= 13 ? 8 : 6));
         pool.add(new ItemStack(Items.NETHER_STAR, tier >= 13 ? 8 : 6));
         pool.add(new ItemStack(Items.ELYTRA, 2));
         pool.add(new ItemStack(Items.DIAMOND_BLOCK, tier >= 13 ? 32 : 20));
      } else if (tier >= 10) {
         pool.add(new ItemStack(Items.NETHERITE_BLOCK, 3));
         pool.add(new ItemStack(Items.MACE, 1));
         pool.add(new ItemStack(Items.HEAVY_CORE, 2));
         pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 16));
         pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 4));
         pool.add(new ItemStack(Items.NETHER_STAR, 4));
         pool.add(new ItemStack(Items.ELYTRA, 1));
         pool.add(new ItemStack(Items.DIAMOND_BLOCK, 12));
      } else if (tier >= 8) {
         pool.add(new ItemStack(Items.NETHERITE_BLOCK, 1));
         pool.add(new ItemStack(Items.NETHERITE_INGOT, 4));
         pool.add(new ItemStack(Items.MACE, 1));
         pool.add(new ItemStack(Items.HEAVY_CORE, 1));
         pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 8));
         pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 3));
         pool.add(new ItemStack(Items.NETHER_STAR, 2));
         pool.add(new ItemStack(Items.ELYTRA, 1));
         pool.add(new ItemStack(Items.DIAMOND_BLOCK, 8));
      } else if (tier == 7) {
         pool.add(new ItemStack(Items.NETHERITE_INGOT, 3));
         pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 6));
         pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 2));
         pool.add(new ItemStack(Items.HEAVY_CORE, 1));
         pool.add(new ItemStack(Items.MACE, 1));
         pool.add(new ItemStack(Items.ELYTRA, 1));
         pool.add(new ItemStack(Items.DIAMOND_BLOCK, 4));
         pool.add(new ItemStack(Items.TRIDENT, 1));
         pool.add(new ItemStack(Items.NETHER_STAR, 1));
      } else if (tier == 6) {
         pool.add(new ItemStack(Items.NETHERITE_SCRAP, 4));
         pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
         pool.add(new ItemStack(Items.DIAMOND_BLOCK, 2));
         pool.add(new ItemStack(Items.GOLDEN_APPLE, 6));
         pool.add(new ItemStack(Items.ENDER_PEARL, 16));
         pool.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 32));
         pool.add(new ItemStack(Items.DIAMOND, 16));
      } else if (tier == 5) {
         pool.add(new ItemStack(Items.DIAMOND, 8));
         pool.add(new ItemStack(Items.GOLDEN_APPLE, 4));
         pool.add(new ItemStack(Items.ENDER_PEARL, 8));
         pool.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 16));
         pool.add(new ItemStack(Items.EMERALD_BLOCK, 3));
         pool.add(new ItemStack(Items.GOLD_BLOCK, 4));
      } else if (tier == 4) {
         pool.add(new ItemStack(Items.DIAMOND, 4));
         pool.add(new ItemStack(Items.GOLDEN_APPLE, 2));
         pool.add(new ItemStack(Items.GOLD_INGOT, 12));
         pool.add(new ItemStack(Items.ENDER_PEARL, 4));
         pool.add(new ItemStack(Items.IRON_BLOCK, 3));
      } else if (tier == 3) {
         pool.add(new ItemStack(Items.GOLD_INGOT, 8));
         pool.add(new ItemStack(Items.IRON_INGOT, 16));
         pool.add(new ItemStack(Items.GOLDEN_APPLE, 1));
         pool.add(new ItemStack(Items.LAPIS_LAZULI, 24));
         pool.add(new ItemStack(Items.COOKED_BEEF, 16));
      } else {
         pool.add(new ItemStack(Items.IRON_INGOT, 10));
         pool.add(new ItemStack(Items.GOLDEN_APPLE, 1));
         pool.add(new ItemStack(Items.BREAD, 16));
         pool.add(new ItemStack(Items.TORCH, 32));
         pool.add(new ItemStack(Items.COAL, 16));
      }

      session.finalWinningStack = pool.get(rng.nextInt(pool.size()));
      session.finalRewardStacks.add(session.finalWinningStack);
      double ix = session.chestPos.getX() + 0.5;
      double iy = session.chestPos.getY() + 1.25;
      double iz = session.chestPos.getZ() + 0.5;
      ItemEntity item = new ItemEntity(level, ix, iy, iz, new ItemStack(Items.DIAMOND));
      item.setNoGravity(true);
      item.setNeverPickUp();
      item.setInvulnerable(true);
      item.setGlowingTag(true);
      item.setDeltaMovement(0.0, 0.015, 0.0);
      level.addFreshEntity(item);
      session.rouletteItemEntity = item;
      if (session.chestPos != null) {
         BlockState coreState = level.getBlockState(session.chestPos);
         if (coreState.is((Block)SystemBlocks.DUNGEON_CORE.get())) {
            level.setBlock(session.chestPos, (BlockState)coreState.setValue(DungeonCoreBlock.ACTIVATED, true), 3);
         }
      }

      level.playSound(null, ix, iy, iz, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.2F, 1.0F);
      level.playSound(null, ix, iy, iz, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.4F);
      player.displayClientMessage(Component.translatable("awakening_system.action.roulette_spinning"), true);
   }

   public static void deliverRewardsToPlayer(ServerPlayer p, com.soybe.awakeningsystem.DungeonManager.DungeonSession session) {
      com.soybe.awakeningsystem.SystemData pData = com.soybe.awakeningsystem.SystemEvents.data(p);
      pData.addCoins(session.finalCoinsWon);
      pData.addExperience(session.finalExpWon);
      pData.recordStatistic("missions_completed", 1);
      com.soybe.awakeningsystem.SystemEvents.checkAchievements(p, pData);
      com.soybe.awakeningsystem.AwakeningSystemMod.sync(p);

      for (ItemStack stack : session.finalRewardStacks) {
         if (!p.getInventory().add(stack.copy())) {
            p.drop(stack.copy(), false);
         }
      }
   }

   public static boolean claimDungeonRewardSafe(ServerPlayer player) {
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session = SESSIONS.get(player.getUUID());
      if (session == null) {
         for (com.soybe.awakeningsystem.DungeonManager.DungeonSession s : SESSIONS.values()) {
            if (s.dungeonDimension.equals(player.level().dimension())) {
               session = s;
               s.coopPlayers.add(player.getUUID());
               SESSIONS.put(player.getUUID(), s);
               break;
            }
         }
      }

      if (session == null) {
         return false;
      } else if (session.chestClaimed) {
         if (!session.claimedPlayers.contains(player.getUUID())) {
            deliverRewardsToPlayer(player, session);
            session.claimedPlayers.add(player.getUUID());
            player.sendSystemMessage(Component.translatable("dungeon.awakening_system.reward_secured_chat"));
            player.displayClientMessage(Component.translatable("dungeon.awakening_system.reward_secured"), true);
            player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.ROULETTE_WIN.value(), SoundSource.PLAYERS, 1.2F, 1.0F);
            return true;
         } else {
            return false;
         }
      } else if (!session.awaitingGambleDecision) {
         return false;
      } else {
         session.awaitingGambleDecision = false;
         session.chestClaimed = true;
         MinecraftServer server = player.getServer();
         if (server == null) {
            return false;
         } else {
            ServerLevel level = server.getLevel(session.dungeonDimension);
            if (level == null) {
               return false;
            } else {
               if (session.chestPos != null) {
                  BlockState coreState = level.getBlockState(session.chestPos);
                  if (coreState.is((Block)SystemBlocks.DUNGEON_CORE.get())) {
                     level.setBlock(
                        session.chestPos,
                        (BlockState)((BlockState)coreState.setValue(DungeonCoreBlock.ACTIVATED, false)).setValue(DungeonCoreBlock.CLAIMED, true),
                        3
                     );
                  }
               }

               if (session.rouletteItemEntity != null) {
                  session.rouletteItemEntity.discard();
                  session.rouletteItemEntity = null;
               }

               level.playSound(
                  null,
                  session.chestPos.getX(),
                  session.chestPos.getY(),
                  session.chestPos.getZ(),
                  (SoundEvent)com.soybe.awakeningsystem.ModSounds.ROULETTE_WIN.value(),
                  SoundSource.PLAYERS,
                  1.2F,
                  1.0F
               );
               level.playSound(
                  null,
                  session.chestPos.getX(),
                  session.chestPos.getY(),
                  session.chestPos.getZ(),
                  (SoundEvent)com.soybe.awakeningsystem.ModSounds.SYSTEM_COINS.value(),
                  SoundSource.PLAYERS,
                  1.0F,
                  1.0F
               );

               for (ServerPlayer p : session.getAllPlayers(server)) {
                  deliverRewardsToPlayer(p, session);
                  session.claimedPlayers.add(p.getUUID());
                  p.sendSystemMessage(Component.translatable("dungeon.awakening_system.reward_secured_chat"));
                  p.displayClientMessage(Component.translatable("dungeon.awakening_system.reward_secured"), true);
                  p.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
               }

               session.returnCountdown = -1;
               session.claimedTicks = 0;
               if (session.bossEvent != null) {
                  session.bossEvent.setColor(BossBarColor.BLUE);
                  session.bossEvent.setProgress(1.0F);
                  session.bossEvent.setName(Component.translatable("awakening_system.dungeon.gate_conquered"));
               }

               return true;
            }
         }
      }
   }

   public static boolean gambleDungeonReward(ServerPlayer player) {
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session = SESSIONS.get(player.getUUID());
      if (session == null) {
         for (com.soybe.awakeningsystem.DungeonManager.DungeonSession s : SESSIONS.values()) {
            if (s.dungeonDimension.equals(player.level().dimension())) {
               session = s;
               s.coopPlayers.add(player.getUUID());
               SESSIONS.put(player.getUUID(), s);
               break;
            }
         }
      }

      if (session != null && session.awaitingGambleDecision && !session.chestClaimed) {
         session.awaitingGambleDecision = false;
         session.isGamblingReward = true;
         session.rouletteSpinning = true;
         session.rouletteTicks = 0;
         MinecraftServer server = player.getServer();
         if (server == null) {
            return false;
         } else {
            ServerLevel level = server.getLevel(session.dungeonDimension);
            if (level == null) {
               return false;
            } else {
               level.playSound(
                  null, session.chestPos.getX(), session.chestPos.getY(), session.chestPos.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.2F
               );

               for (ServerPlayer p : session.getAllPlayers(server)) {
                  p.sendSystemMessage(Component.translatable("dungeon.awakening_system.gamble_spinning_chat"));
                  p.displayClientMessage(Component.translatable("dungeon.awakening_system.gamble_spinning"), true);
               }

               if (session.bossEvent != null) {
                  session.bossEvent.setColor(BossBarColor.PURPLE);
                  session.bossEvent.setProgress(1.0F);
                  session.bossEvent.setName(Component.translatable("dungeon.awakening_system.boss_bar_gambling"));
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static ItemStack upgradeRewardStack(ItemStack original, String rank, RandomSource rng) {
      if (original == null || original.isEmpty()) {
         return new ItemStack(Items.DIAMOND_BLOCK, 2);
      } else if (original.is(Items.DIAMOND)) {
         return new ItemStack(Items.DIAMOND_BLOCK, Math.max(1, original.getCount() / 2));
      } else if (original.is(Items.GOLD_INGOT)) {
         return new ItemStack(Items.GOLD_BLOCK, Math.max(2, original.getCount() / 4));
      } else if (original.is(Items.IRON_INGOT)) {
         return new ItemStack(Items.IRON_BLOCK, Math.max(2, original.getCount() / 4));
      } else if (original.is(Items.GOLDEN_APPLE)) {
         return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, Math.max(1, original.getCount() / 2));
      } else if (original.is(Items.EMERALD)) {
         return new ItemStack(Items.EMERALD_BLOCK, Math.max(2, original.getCount() / 3));
      } else if (original.is(Items.NETHERITE_SCRAP)) {
         return new ItemStack(Items.NETHERITE_INGOT, Math.max(1, original.getCount() / 2));
      } else if (original.is(Items.NETHERITE_INGOT)) {
         return new ItemStack(Items.NETHERITE_BLOCK, Math.max(1, original.getCount()));
      } else if (!original.is(Items.TOTEM_OF_UNDYING)
         && !original.is(Items.ENCHANTED_GOLDEN_APPLE)
         && !original.is(Items.NETHERITE_BLOCK)
         && !original.is(Items.DIAMOND_BLOCK)
         && !original.is(Items.NETHER_STAR)
         && !original.is(Items.ENDER_PEARL)
         && !original.is(Items.EXPERIENCE_BOTTLE)) {
         if (original.getMaxStackSize() == 1) {
            return new ItemStack(Items.HEAVY_CORE, 1);
         } else {
            ItemStack copy = original.copy();
            copy.grow(Math.max(1, original.getCount()));
            return copy;
         }
      } else {
         ItemStack copy = original.copy();
         copy.grow(original.getCount());
         return copy;
      }
   }

   public static void tick(MinecraftServer server) {
      List<UUID> toExit = new ArrayList<>();

      for (com.soybe.awakeningsystem.DungeonManager.DungeonSession session : new HashSet<>(SESSIONS.values())) {
         ServerPlayer player = server.getPlayerList().getPlayer(session.playerId);
         if (player != null && player.isAlive()) {
            for (UUID coopId : session.coopPlayers) {
               ServerPlayer coopP = server.getPlayerList().getPlayer(coopId);
               if (coopP == null || !coopP.isAlive()) {
                  toExit.add(coopId);
               }
            }

            ServerLevel level = server.getLevel(session.dungeonDimension);
            if (level != null) {
               for (ServerPlayer p : session.getAllPlayers(server)) {
                  if (isPlayerOutOfBounds(session, p)) {
                     rescuePlayerToBounds(session, p);
                  }
               }

               if (level.getGameTime() % 20L == 0L) {
                  BlockPos center = getCurrentRoomCenter(session);
                  if (center == null) {
                     center = session.arenaCenter;
                  }

                  if (center != null) {
                     AABB checkBounds = new AABB(center).inflate(150.0, 60.0, 150.0);

                     for (LivingEntity rogue : level.getEntitiesOfClass(
                        LivingEntity.class,
                        checkBounds,
                        e -> !(e instanceof Player)
                           && !e.getTags().contains("dungeon_mob")
                           && !e.getTags().contains("shadow_soldier")
                           && !(e instanceof TamableAnimal tamed && tamed.isTame())
                     )) {
                        rogue.discard();
                     }
                  }
               }

               if (session.combatActive) {
                  if (session.enemyUUIDs != null && !session.enemyUUIDs.isEmpty()) {
                     boolean changed = session.enemyUUIDs.removeIf(uuid -> {
                        Entity ent = level.getEntity(uuid);
                        return ent == null || !ent.isAlive() || ent.isRemoved();
                     });
                     if (changed && session.bossEvent != null) {
                        int remaining = session.enemyUUIDs.size();
                        session.bossEvent.setProgress((float)remaining / Math.max(1, session.totalEnemies));
                     }
                  }

                  if (session.enemyUUIDs != null && session.enemyUUIDs.size() == 1) {
                     UUID lastEnemyId = session.enemyUUIDs.iterator().next();
                     if (level.getEntity(lastEnemyId) instanceof LivingEntity lastLiving && lastLiving.isAlive()) {
                        lastLiving.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));

                        try {
                           Scoreboard sb = level.getScoreboard();
                           PlayerTeam team = sb.getPlayerTeam("dungeon_last_target");
                           if (team == null) {
                              team = sb.addPlayerTeam("dungeon_last_target");
                              team.setColor(ChatFormatting.RED);
                           }

                           String member = lastLiving.getScoreboardName();
                           if (!team.getPlayers().contains(member)) {
                              sb.addPlayerToTeam(member, team);
                           }
                        } catch (Exception var43) {
                        }

                        if (!session.lastEnemyNotified) {
                           session.lastEnemyNotified = true;
                           session.lastEnemyUUID = lastEnemyId;
                           if (player != null) {
                              player.displayClientMessage(Component.translatable("awakening_system.action.last_enemy_marked"), true);
                              player.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.9F, 1.4F);
                              player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 1.2F);
                           }
                        }

                        if (server.getTickCount() % 5 == 0) {
                           level.sendParticles(
                              ParticleTypes.CRIMSON_SPORE,
                              lastLiving.getX(),
                              lastLiving.getY() + lastLiving.getEyeHeight() + 0.4,
                              lastLiving.getZ(),
                              6,
                              0.25,
                              0.4,
                              0.25,
                              0.05
                           );
                           level.sendParticles(ParticleTypes.FLAME, lastLiving.getX(), lastLiving.getY() + 1.0, lastLiving.getZ(), 2, 0.15, 0.2, 0.15, 0.02);
                        }

                        BlockPos curRoom = getCurrentRoomCenter(session);
                        BlockPos curBarrier = getActiveBarrierPos(session);
                        if (curRoom != null && curBarrier != null) {
                           double distToRoom = lastLiving.distanceToSqr(curRoom.getX() + 0.5, lastLiving.getY(), curRoom.getZ() + 0.5);
                           double barrierDist = curBarrier.distSqr(curRoom);
                           if (distToRoom > barrierDist) {
                              double rx = curRoom.getX() + (level.random.nextDouble() - 0.5) * 6.0;
                              double rz = curRoom.getZ() + (level.random.nextDouble() - 0.5) * 6.0;
                              double ry = findSafeFloorY(level, rx, curRoom.getY(), rz);
                              lastLiving.teleportTo(rx, ry, rz);
                              lastLiving.setDeltaMovement(0.0, 0.0, 0.0);
                           }
                        }
                     } else {
                        session.enemyUUIDs.remove(lastEnemyId);
                     }
                  }

                  if ((session.enemyUUIDs != null && session.enemyUUIDs.isEmpty() || session.bossDefeated) && session.combatActive && !session.chestSpawned) {
                     completeDungeonCombat(session, player);
                  }
               }

               if (session.combatActive && !session.enemyUUIDs.isEmpty() && session.bossEvent != null) {
                  int remaining = session.enemyUUIDs.size();
                  int secRemaining = Math.max(0, (session.combatTimer + 19) / 20);
                  String timeStr = String.format("%02d:%02d", secRemaining / 60, secRemaining % 60);
                  if (session.bossUUID != null) {
                     if (level.getEntity(session.bossUUID) instanceof LivingEntity le && le.isAlive()) {
                        session.bossEvent.setName(Component.literal("§4§l[JEFE] " + le.getDisplayName().getString()));
                        session.bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, le.getHealth() / Math.max(1.0F, le.getMaxHealth()))));
                     } else {
                        session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 1, remaining, timeStr));
                        session.bossEvent.setProgress((float)remaining / Math.max(1, session.totalEnemies));
                     }
                  } else {
                     session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 1, remaining, timeStr));
                     session.bossEvent.setProgress((float)remaining / Math.max(1, session.totalEnemies));
                  }
               }

               if (session.rouletteSpinning && session.rouletteItemEntity != null) {
                  session.rouletteTicks++;
                  ItemStack[] candidates = new ItemStack[]{
                     new ItemStack(Items.NETHERITE_INGOT, 2),
                     new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2),
                     new ItemStack(Items.TOTEM_OF_UNDYING, 1),
                     new ItemStack(Items.DIAMOND_BLOCK, 2),
                     new ItemStack(Items.GOLDEN_APPLE, 4),
                     new ItemStack(Items.DIAMOND, 8),
                     new ItemStack(Items.EXPERIENCE_BOTTLE, 16),
                     new ItemStack(Items.NETHERITE_SCRAP, 3),
                     new ItemStack(Items.GOLD_INGOT, 12),
                     new ItemStack(Items.ENDER_PEARL, 6),
                     new ItemStack(Items.EMERALD_BLOCK, 2)
                  };
                  int cycleInterval = session.rouletteTicks < 30 ? 2 : (session.rouletteTicks < 50 ? 4 : (session.rouletteTicks < 65 ? 7 : 12));
                  if (session.rouletteTicks % cycleInterval == 0 && session.rouletteTicks < 75) {
                     ItemStack nextCandidate = candidates[session.rouletteTicks / cycleInterval % candidates.length];
                     session.rouletteItemEntity.setItem(nextCandidate.copy());
                     level.playSound(
                        null,
                        session.chestPos.getX(),
                        session.chestPos.getY(),
                        session.chestPos.getZ(),
                        (SoundEvent)com.soybe.awakeningsystem.ModSounds.ROULETTE_SPIN.value(),
                        SoundSource.BLOCKS,
                        0.7F,
                        1.0F + session.rouletteTicks / 75.0F
                     );
                     if (session.isGamblingReward) {
                        level.sendParticles(
                           ParticleTypes.SOUL_FIRE_FLAME,
                           session.rouletteItemEntity.getX(),
                           session.rouletteItemEntity.getY() + 0.3,
                           session.rouletteItemEntity.getZ(),
                           10,
                           0.3,
                           0.3,
                           0.3,
                           0.05
                        );
                        level.sendParticles(
                           ParticleTypes.PORTAL,
                           session.rouletteItemEntity.getX(),
                           session.rouletteItemEntity.getY() + 0.3,
                           session.rouletteItemEntity.getZ(),
                           8,
                           0.2,
                           0.2,
                           0.2,
                           0.1
                        );
                     } else {
                        level.sendParticles(
                           ParticleTypes.ENCHANT,
                           session.rouletteItemEntity.getX(),
                           session.rouletteItemEntity.getY() + 0.3,
                           session.rouletteItemEntity.getZ(),
                           8,
                           0.2,
                           0.2,
                           0.2,
                           0.1
                        );
                     }
                  }

                  session.rouletteItemEntity.setDeltaMovement(0.0, Math.max(0.002, 0.016 * (1.0 - session.rouletteTicks / 80.0)), 0.0);
                  if (session.rouletteTicks == 75) {
                     session.rouletteSpinning = false;
                     if (!session.isGamblingReward) {
                        session.awaitingGambleDecision = true;
                        session.rouletteItemEntity.setItem(session.finalWinningStack.copy());
                        session.rouletteItemEntity.setDeltaMovement(0.0, 0.0, 0.0);
                        level.sendParticles(
                           ParticleTypes.TOTEM_OF_UNDYING,
                           session.rouletteItemEntity.getX(),
                           session.rouletteItemEntity.getY() + 0.3,
                           session.rouletteItemEntity.getZ(),
                           40,
                           0.4,
                           0.5,
                           0.4,
                           0.15
                        );
                        level.sendParticles(
                           ParticleTypes.ENCHANT,
                           session.rouletteItemEntity.getX(),
                           session.rouletteItemEntity.getY() + 0.3,
                           session.rouletteItemEntity.getZ(),
                           50,
                           0.5,
                           0.6,
                           0.5,
                           0.2
                        );
                        level.playSound(
                           null,
                           session.chestPos.getX(),
                           session.chestPos.getY(),
                           session.chestPos.getZ(),
                           (SoundEvent)com.soybe.awakeningsystem.ModSounds.ROULETTE_WIN.value(),
                           SoundSource.PLAYERS,
                           1.2F,
                           1.0F
                        );
                        String itemName = session.finalWinningStack.getHoverName().getString();
                        int count = session.finalWinningStack.getCount();

                        for (ServerPlayer px : session.getAllPlayers(server)) {
                           px.sendSystemMessage(Component.literal("§8§m                                                        "));
                           px.sendSystemMessage(Component.translatable("dungeon.awakening_system.reward_header"));
                           px.sendSystemMessage(
                              Component.translatable(
                                 "dungeon.awakening_system.reward_discovered", new Object[]{itemName, count, session.finalCoinsWon, session.finalExpWon}
                              )
                           );
                           px.sendSystemMessage(Component.translatable("dungeon.awakening_system.gamble_prompt"));
                           px.sendSystemMessage(Component.literal(""));
                           MutableComponent claimBtn = Component.translatable("dungeon.awakening_system.gamble_btn_claim")
                              .withStyle(
                                 style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dungeon claim"))
                                    .withHoverEvent(
                                       new HoverEvent(
                                          net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                          Component.translatable("dungeon.awakening_system.gamble_btn_claim_hover")
                                       )
                                    )
                              );
                           MutableComponent gambleBtn = Component.translatable("dungeon.awakening_system.gamble_btn_gamble")
                              .withStyle(
                                 style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dungeon gamble"))
                                    .withHoverEvent(
                                       new HoverEvent(
                                          net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                          Component.translatable("dungeon.awakening_system.gamble_btn_gamble_hover")
                                       )
                                    )
                              );
                           px.sendSystemMessage(claimBtn.append(gambleBtn));
                           px.sendSystemMessage(Component.translatable("dungeon.awakening_system.gamble_hint"));
                           px.sendSystemMessage(Component.literal("§8§m                                                        "));
                           px.displayClientMessage(Component.translatable("dungeon.awakening_system.reward_ready"), true);
                        }

                        if (session.bossEvent != null) {
                           session.bossEvent.setColor(BossBarColor.YELLOW);
                           session.bossEvent.setProgress(1.0F);
                           session.bossEvent.setName(Component.translatable("dungeon.awakening_system.boss_bar_gamble_choice"));
                        }
                     } else {
                        session.isGamblingReward = false;
                        session.chestClaimed = true;
                        if (session.chestPos != null) {
                           BlockState coreState = level.getBlockState(session.chestPos);
                           if (coreState.is((Block)SystemBlocks.DUNGEON_CORE.get())) {
                              level.setBlock(
                                 session.chestPos,
                                 (BlockState)((BlockState)coreState.setValue(DungeonCoreBlock.ACTIVATED, false)).setValue(DungeonCoreBlock.CLAIMED, true),
                                 3
                              );
                           }
                        }

                        boolean gambleWon = level.random.nextFloat() < 0.55F;
                        if (gambleWon) {
                           session.finalCoinsWon *= 2;
                           session.finalExpWon *= 2;
                           ItemStack upgraded = upgradeRewardStack(session.finalWinningStack, session.rank, level.random);
                           session.finalWinningStack = upgraded;
                           session.finalRewardStacks.clear();
                           session.finalRewardStacks.add(upgraded);
                           session.rouletteItemEntity.setItem(upgraded.copy());
                           session.rouletteItemEntity.setDeltaMovement(0.0, 0.0, 0.0);
                           level.sendParticles(
                              ParticleTypes.FIREWORK,
                              session.rouletteItemEntity.getX(),
                              session.rouletteItemEntity.getY() + 0.3,
                              session.rouletteItemEntity.getZ(),
                              60,
                              0.4,
                              0.6,
                              0.4,
                              0.2
                           );
                           level.sendParticles(
                              ParticleTypes.TOTEM_OF_UNDYING,
                              session.rouletteItemEntity.getX(),
                              session.rouletteItemEntity.getY() + 0.3,
                              session.rouletteItemEntity.getZ(),
                              80,
                              0.5,
                              0.6,
                              0.5,
                              0.3
                           );
                           level.playSound(
                              null,
                              session.chestPos.getX(),
                              session.chestPos.getY(),
                              session.chestPos.getZ(),
                              (SoundEvent)com.soybe.awakeningsystem.ModSounds.ROULETTE_WIN.value(),
                              SoundSource.PLAYERS,
                              1.4F,
                              1.0F
                           );
                           level.playSound(
                              null,
                              session.chestPos.getX(),
                              session.chestPos.getY(),
                              session.chestPos.getZ(),
                              SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                              SoundSource.PLAYERS,
                              1.2F,
                              1.0F
                           );

                           for (ServerPlayer px : session.getAllPlayers(server)) {
                              deliverRewardsToPlayer(px, session);
                              session.claimedPlayers.add(px.getUUID());
                              px.sendSystemMessage(Component.translatable("dungeon.awakening_system.gamble_won_chat_1"));
                              px.sendSystemMessage(
                                 Component.translatable(
                                    "dungeon.awakening_system.gamble_won_chat_2",
                                    new Object[]{upgraded.getHoverName().getString(), upgraded.getCount(), session.finalCoinsWon, session.finalExpWon}
                                 )
                              );
                              px.displayClientMessage(Component.translatable("dungeon.awakening_system.gamble_won"), true);
                           }
                        } else {
                           session.finalCoinsWon = Math.max(60, session.finalCoinsWon / 4);
                           session.finalExpWon = Math.max(100, session.finalExpWon / 4);
                           ItemStack consolation = new ItemStack(Items.COAL, 8);
                           consolation.set(DataComponents.CUSTOM_NAME, Component.translatable("item.awakening_system.consolation_coal_name"));
                           session.finalWinningStack = consolation;
                           session.finalRewardStacks.clear();
                           session.finalRewardStacks.add(consolation);
                           session.rouletteItemEntity.setItem(consolation.copy());
                           session.rouletteItemEntity.setDeltaMovement(0.0, 0.0, 0.0);
                           level.sendParticles(
                              ParticleTypes.LARGE_SMOKE,
                              session.rouletteItemEntity.getX(),
                              session.rouletteItemEntity.getY() + 0.3,
                              session.rouletteItemEntity.getZ(),
                              40,
                              0.3,
                              0.4,
                              0.3,
                              0.05
                           );
                           level.playSound(
                              null,
                              session.chestPos.getX(),
                              session.chestPos.getY(),
                              session.chestPos.getZ(),
                              SoundEvents.WITHER_DEATH,
                              SoundSource.PLAYERS,
                              0.8F,
                              0.5F
                           );
                           level.playSound(
                              null,
                              session.chestPos.getX(),
                              session.chestPos.getY(),
                              session.chestPos.getZ(),
                              SoundEvents.ITEM_BREAK,
                              SoundSource.PLAYERS,
                              1.2F,
                              0.7F
                           );

                           for (ServerPlayer px : session.getAllPlayers(server)) {
                              deliverRewardsToPlayer(px, session);
                              session.claimedPlayers.add(px.getUUID());
                              px.sendSystemMessage(Component.translatable("dungeon.awakening_system.gamble_lost_chat_1"));
                              px.sendSystemMessage(
                                 Component.translatable("dungeon.awakening_system.gamble_lost_chat_2", new Object[]{session.finalCoinsWon, session.finalExpWon})
                              );
                              px.displayClientMessage(Component.translatable("dungeon.awakening_system.gamble_lost"), true);
                           }
                        }

                        session.returnCountdown = -1;
                        session.claimedTicks = 0;
                        if (session.bossEvent != null) {
                           session.bossEvent.setColor(BossBarColor.BLUE);
                           session.bossEvent.setProgress(1.0F);
                           session.bossEvent.setName(Component.translatable("awakening_system.dungeon.gate_conquered"));
                        }
                     }
                  }
               }

               if (!session.barrier1Open && !session.barrier1Blocks.isEmpty()) {
                  if (server.getTickCount() % 10 == 0 && session.barrier1Pos != null) {
                     enforceEnemyBarrierContainment(level, session, session.arenaCenter, session.barrier1Pos);
                  }

                  if (server.getTickCount() % 10 == 0) {
                     for (BlockPos bp : session.barrier1Blocks) {
                        if (level.random.nextFloat() < 0.2F) {
                           level.sendParticles(ParticleTypes.ELECTRIC_SPARK, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.02);
                           level.sendParticles(ParticleTypes.PORTAL, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                     }
                  }

                  if (player != null && session.barrier1Pos != null) {
                     double distSq = player.distanceToSqr(session.barrier1Pos.getX() + 0.5, session.barrier1Pos.getY() + 1.0, session.barrier1Pos.getZ() + 0.5);
                     if (distSq <= 25.0) {
                        if (session.enemyUUIDs.isEmpty()) {
                           openPhase1Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           int rem = session.enemyUUIDs.size();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_1", new Object[]{rem}), true);
                           player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                     }

                     BlockPos prevCenter = session.arenaCenter;
                     double distToPrev = player.distanceToSqr(prevCenter.getX(), player.getY(), prevCenter.getZ());
                     double barrierToPrev = session.barrier1Pos.distSqr(prevCenter);
                     if (distToPrev > barrierToPrev + 4.0 && distSq <= 36.0 && !session.enemyUUIDs.isEmpty()) {
                        Vec3 dir = new Vec3(prevCenter.getX() - session.barrier1Pos.getX(), 0.0, prevCenter.getZ() - session.barrier1Pos.getZ())
                           .normalize()
                           .scale(3.0);
                        player.teleportTo(
                           level,
                           session.barrier1Pos.getX() + 0.5 + dir.x,
                           session.barrier1Pos.getY() + 1.0,
                           session.barrier1Pos.getZ() + 0.5 + dir.z,
                           player.getYRot(),
                           player.getXRot()
                        );
                        if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_1"), true);
                        }
                     }
                  }
               } else if (session.barrier1Open && !session.barrier2Open && !session.barrier2Blocks.isEmpty()) {
                  if (server.getTickCount() % 10 == 0 && session.barrier2Pos != null) {
                     enforceEnemyBarrierContainment(
                        level, session, session.room2Center != null ? session.room2Center : session.arenaCenter, session.barrier2Pos
                     );
                  }

                  if (server.getTickCount() % 10 == 0) {
                     for (BlockPos bpx : session.barrier2Blocks) {
                        if (level.random.nextFloat() < 0.2F) {
                           level.sendParticles(ParticleTypes.ELECTRIC_SPARK, bpx.getX() + 0.5, bpx.getY() + 0.5, bpx.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.02);
                           level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, bpx.getX() + 0.5, bpx.getY() + 0.5, bpx.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                     }
                  }

                  if (player != null && session.barrier2Pos != null) {
                     double distSqx = player.distanceToSqr(session.barrier2Pos.getX() + 0.5, session.barrier2Pos.getY() + 1.0, session.barrier2Pos.getZ() + 0.5);
                     if (distSqx <= 25.0) {
                        if (session.enemyUUIDs.isEmpty()) {
                           openPhase2Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           int rem = session.enemyUUIDs.size();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_2", new Object[]{rem}), true);
                           player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                     }

                     BlockPos prevCenter = session.room2Center != null ? session.room2Center : session.arenaCenter;
                     double distToPrev = player.distanceToSqr(prevCenter.getX(), player.getY(), prevCenter.getZ());
                     double barrierToPrev = session.barrier2Pos.distSqr(prevCenter);
                     if (distToPrev > barrierToPrev + 4.0 && distSqx <= 36.0 && !session.enemyUUIDs.isEmpty()) {
                        Vec3 dir = new Vec3(prevCenter.getX() - session.barrier2Pos.getX(), 0.0, prevCenter.getZ() - session.barrier2Pos.getZ())
                           .normalize()
                           .scale(3.0);
                        player.teleportTo(
                           level,
                           session.barrier2Pos.getX() + 0.5 + dir.x,
                           session.barrier2Pos.getY() + 1.0,
                           session.barrier2Pos.getZ() + 0.5 + dir.z,
                           player.getYRot(),
                           player.getXRot()
                        );
                        if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_2"), true);
                        }
                     }
                  }
               } else if (session.barrier2Open && !session.barrier3Open && !session.barrier3Blocks.isEmpty()) {
                  if (server.getTickCount() % 10 == 0 && session.barrier3Pos != null) {
                     enforceEnemyBarrierContainment(
                        level, session, session.room3Center != null ? session.room3Center : session.arenaCenter, session.barrier3Pos
                     );
                  }

                  if (server.getTickCount() % 10 == 0) {
                     for (BlockPos bpxx : session.barrier3Blocks) {
                        if (level.random.nextFloat() < 0.2F) {
                           level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, bpxx.getX() + 0.5, bpxx.getY() + 0.5, bpxx.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.02);
                           level.sendParticles(ParticleTypes.FLAME, bpxx.getX() + 0.5, bpxx.getY() + 0.5, bpxx.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                     }
                  }

                  if (player != null && session.barrier3Pos != null) {
                     double distSqxx = player.distanceToSqr(
                        session.barrier3Pos.getX() + 0.5, session.barrier3Pos.getY() + 1.0, session.barrier3Pos.getZ() + 0.5
                     );
                     if (distSqxx <= 25.0) {
                        if (session.enemyUUIDs.isEmpty()) {
                           openPhase3Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           int rem = session.enemyUUIDs.size();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_3", new Object[]{rem}), true);
                           player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                     }

                     BlockPos prevCenter = session.room3Center != null ? session.room3Center : session.arenaCenter;
                     double distToPrev = player.distanceToSqr(prevCenter.getX(), player.getY(), prevCenter.getZ());
                     double barrierToPrev = session.barrier3Pos.distSqr(prevCenter);
                     if (distToPrev > barrierToPrev + 4.0 && distSqxx <= 36.0 && !session.enemyUUIDs.isEmpty()) {
                        Vec3 dir = new Vec3(prevCenter.getX() - session.barrier3Pos.getX(), 0.0, prevCenter.getZ() - session.barrier3Pos.getZ())
                           .normalize()
                           .scale(3.0);
                        player.teleportTo(
                           level,
                           session.barrier3Pos.getX() + 0.5 + dir.x,
                           session.barrier3Pos.getY() + 1.0,
                           session.barrier3Pos.getZ() + 0.5 + dir.z,
                           player.getYRot(),
                           player.getXRot()
                        );
                        if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_3"), true);
                        }
                     }
                  }
               } else if (session.barrier3Open && !session.barrier4Open && !session.barrier4Blocks.isEmpty()) {
                  if (server.getTickCount() % 10 == 0 && session.barrier4Pos != null) {
                     enforceEnemyBarrierContainment(
                        level, session, session.room4Center != null ? session.room4Center : session.arenaCenter, session.barrier4Pos
                     );
                  }

                  if (server.getTickCount() % 10 == 0) {
                     for (BlockPos bpxxx : session.barrier4Blocks) {
                        if (level.random.nextFloat() < 0.2F) {
                           level.sendParticles(ParticleTypes.ELECTRIC_SPARK, bpxxx.getX() + 0.5, bpxxx.getY() + 0.5, bpxxx.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.02);
                           level.sendParticles(ParticleTypes.DRAGON_BREATH, bpxxx.getX() + 0.5, bpxxx.getY() + 0.5, bpxxx.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                     }
                  }

                  if (player != null && session.barrier4Pos != null) {
                     double distSqxxx = player.distanceToSqr(
                        session.barrier4Pos.getX() + 0.5, session.barrier4Pos.getY() + 1.0, session.barrier4Pos.getZ() + 0.5
                     );
                     if (distSqxxx <= 25.0) {
                        if (session.enemyUUIDs.isEmpty()) {
                           openPhase4Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           int rem = session.enemyUUIDs.size();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_4", new Object[]{rem}), true);
                           player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                     }

                     BlockPos prevCenter = session.room4Center != null ? session.room4Center : session.arenaCenter;
                     double distToPrev = player.distanceToSqr(prevCenter.getX(), player.getY(), prevCenter.getZ());
                     double barrierToPrev = session.barrier4Pos.distSqr(prevCenter);
                     if (distToPrev > barrierToPrev + 4.0 && distSqxxx <= 36.0 && !session.enemyUUIDs.isEmpty()) {
                        Vec3 dir = new Vec3(prevCenter.getX() - session.barrier4Pos.getX(), 0.0, prevCenter.getZ() - session.barrier4Pos.getZ())
                           .normalize()
                           .scale(3.0);
                        player.teleportTo(
                           level,
                           session.barrier4Pos.getX() + 0.5 + dir.x,
                           session.barrier4Pos.getY() + 1.0,
                           session.barrier4Pos.getZ() + 0.5 + dir.z,
                           player.getYRot(),
                           player.getXRot()
                        );
                        if (server.getTickCount() - session.lastBarrierWarningTick > 30L) {
                           session.lastBarrierWarningTick = server.getTickCount();
                           player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_4"), true);
                        }
                     }
                  }
               }

               if (session.bossBarrierOpen && !session.barrierBossBlocks.isEmpty()) {
                  for (BlockPos b : session.barrierBossBlocks) {
                     level.setBlock(b, Blocks.AIR.defaultBlockState(), 2);
                  }

                  session.barrierBossBlocks.clear();
               }

               if (!session.bossBarrierOpen && !session.barrierBossBlocks.isEmpty()) {
                  if (server.getTickCount() % 10 == 0) {
                     for (BlockPos bpxxxx : session.barrierBossBlocks) {
                        if (level.random.nextFloat() < 0.15F) {
                           level.sendParticles(
                              ParticleTypes.ELECTRIC_SPARK, bpxxxx.getX() + 0.5, bpxxxx.getY() + 0.5, bpxxxx.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.02
                           );
                           level.sendParticles(ParticleTypes.PORTAL, bpxxxx.getX() + 0.5, bpxxxx.getY() + 0.5, bpxxxx.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                     }
                  }

                  BlockPos[] bPositions = new BlockPos[]{session.barrierBossPos1, session.barrierBossPos2, session.barrierBossPos3};

                  for (ServerPlayer px : session.getAllPlayers(server)) {
                     if (px != null) {
                        for (BlockPos bPos : bPositions) {
                           if (bPos != null) {
                              double distSqxxxx = px.distanceToSqr(bPos.getX() + 0.5, bPos.getY() + 1.0, bPos.getZ() + 0.5);
                              if (distSqxxxx <= 25.0) {
                                 boolean hasKey = false;

                                 for (int slot = 0; slot < px.getInventory().getContainerSize(); slot++) {
                                    ItemStack it = px.getInventory().getItem(slot);
                                    if (!it.isEmpty() && it.is((Item)com.soybe.awakeningsystem.SystemItems.DUNGEON_KEY.get())) {
                                       hasKey = true;
                                       if (!px.isCreative()) {
                                          it.shrink(1);
                                       }
                                       break;
                                    }
                                 }

                                 if (hasKey) {
                                    session.bossBarrierOpen = true;

                                    for (BlockPos b : session.barrierBossBlocks) {
                                       level.setBlock(b, Blocks.AIR.defaultBlockState(), 2);
                                       level.sendParticles(ParticleTypes.FLASH, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.0);
                                       level.sendParticles(
                                          ParticleTypes.TOTEM_OF_UNDYING, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.05
                                       );
                                    }

                                    session.barrierBossBlocks.clear();
                                    spawnBarrierDoorAmbush(level, session, bPos);

                                    for (ServerPlayer ally : session.getAllPlayers(server)) {
                                       if (ally != null) {
                                          ally.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.0F);
                                          ally.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.2F);
                                          ally.sendSystemMessage(Component.translatable("awakening_system.dungeon.barrier.unlocked"));
                                       }
                                    }
                                    break;
                                 }

                                 if (server.getTickCount() - session.lastBarrierWarningTick > 35L) {
                                    session.lastBarrierWarningTick = server.getTickCount();
                                    px.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier.locked_hint"), true);
                                    px.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                                 }
                              }
                           }
                        }

                        if (session.bossBarrierOpen) {
                           break;
                        }
                     }
                  }
               }

               if (session.bossBarrierOpen && session.currentFloor < 5 && !session.chestSpawned && session.thronePos != null) {
                  for (ServerPlayer pxx : session.getAllPlayers(server)) {
                     if (pxx != null && pxx.distanceToSqr(session.thronePos.getX() + 0.5, pxx.getY(), session.thronePos.getZ() + 0.5) <= 576.0) {
                        session.currentFloor = 5;
                        if (session.bossUUID == null || level.getEntity(session.bossUUID) == null || !level.getEntity(session.bossUUID).isAlive()) {
                           spawnPhase5Boss(level, session);
                        }

                        for (ServerPlayer allyx : session.getAllPlayers(server)) {
                           if (allyx != null) {
                              allyx.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 0.8F);
                              allyx.sendSystemMessage(Component.translatable("awakening_system.dungeon.boss_alert"));
                           }
                        }
                        break;
                     }
                  }
               }

               if (session.chestSpawned && !session.chestClaimed && session.chestPos != null && server.getTickCount() % 4 == 0) {
                  level.sendParticles(
                     ParticleTypes.PORTAL, session.chestPos.getX() + 0.5, session.chestPos.getY() + 0.8, session.chestPos.getZ() + 0.5, 8, 0.4, 0.5, 0.4, 0.1
                  );
                  level.sendParticles(
                     ParticleTypes.SOUL_FIRE_FLAME,
                     session.chestPos.getX() + 0.5,
                     session.chestPos.getY() + 0.6,
                     session.chestPos.getZ() + 0.5,
                     4,
                     0.3,
                     0.3,
                     0.3,
                     0.02
                  );
               }

               if (session.chestClaimed && session.chestPos != null) {
                  session.claimedTicks++;
                  if (server.getTickCount() % 4 == 0) {
                     level.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        session.chestPos.getX() + 0.5,
                        session.chestPos.getY() + 0.8,
                        session.chestPos.getZ() + 0.5,
                        6,
                        0.4,
                        0.5,
                        0.4,
                        0.05
                     );
                     level.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        session.chestPos.getX() + 0.5,
                        session.chestPos.getY() + 0.6,
                        session.chestPos.getZ() + 0.5,
                        2,
                        0.2,
                        0.2,
                        0.2,
                        0.01
                     );
                  }
               }

               if (session.exitPortalPos != null) {
                  for (ServerPlayer pxxx : session.getAllPlayers(server)) {
                     double dx = pxxx.getX() - (session.exitPortalPos.getX() + 0.5);
                     double dy = pxxx.getY() - session.exitPortalPos.getY();
                     double dz = pxxx.getZ() - (session.exitPortalPos.getZ() + 0.5);
                     double horizDistSq = dx * dx + dz * dz;
                     if (pxxx.getUUID().equals(session.playerId) && server.getTickCount() % 6 == 0) {
                        if (session.isRedGate && !session.chestSpawned && !session.chestClaimed) {
                           level.sendParticles(
                              ParticleTypes.CRIMSON_SPORE,
                              session.exitPortalPos.getX() + 0.5,
                              session.exitPortalPos.getY() + 2.0,
                              session.exitPortalPos.getZ() + 0.5,
                              5,
                              0.6,
                              0.8,
                              0.2,
                              0.02
                           );
                           level.sendParticles(
                              ParticleTypes.SMOKE,
                              session.exitPortalPos.getX() + 0.5,
                              session.exitPortalPos.getY() + 2.0,
                              session.exitPortalPos.getZ() + 0.5,
                              3,
                              0.4,
                              0.6,
                              0.2,
                              0.01
                           );
                        } else {
                           level.sendParticles(
                              ParticleTypes.PORTAL,
                              session.exitPortalPos.getX() + 0.5,
                              session.exitPortalPos.getY() + 2.0,
                              session.exitPortalPos.getZ() + 0.5,
                              8,
                              0.6,
                              0.8,
                              0.2,
                              0.05
                           );
                           level.sendParticles(
                              ParticleTypes.REVERSE_PORTAL,
                              session.exitPortalPos.getX() + 0.5,
                              session.exitPortalPos.getY() + 2.0,
                              session.exitPortalPos.getZ() + 0.5,
                              4,
                              0.5,
                              0.7,
                              0.2,
                              0.02
                           );
                        }
                     }

                     if (horizDistSq <= 1.0 && dy >= -0.5 && dy <= 3.2) {
                        if (session.isRedGate && !session.chestSpawned && !session.chestClaimed) {
                           if (server.getTickCount() - session.lastExitWarningTick > 40L) {
                              session.lastExitWarningTick = server.getTickCount();
                              pxxx.displayClientMessage(Component.translatable("awakening_system.action.red_gate_blocked"), true);
                              pxxx.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 1.0F, 0.8F);
                              pxxx.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 0.7F);
                           }
                        } else {
                           if (session.awaitingGambleDecision && !session.chestClaimed) {
                              claimDungeonRewardSafe(pxxx);
                           } else if (session.chestClaimed && !session.claimedPlayers.contains(pxxx.getUUID())) {
                              deliverRewardsToPlayer(pxxx, session);
                              session.claimedPlayers.add(pxxx.getUUID());
                              pxxx.sendSystemMessage(Component.translatable("dungeon.awakening_system.reward_secured_chat"));
                              pxxx.displayClientMessage(Component.translatable("dungeon.awakening_system.reward_secured"), true);
                           }

                           pxxx.displayClientMessage(Component.translatable("awakening_system.action.crossing_exit"), true);
                           toExit.add(pxxx.getUUID());
                        }
                     }
                  }
               }
            }
         } else {
            toExit.add(session.playerId);
         }
      }

      for (UUID id : toExit) {
         exitDungeon(server, id);
      }

      if (server.getTickCount() % 20 == 0) {
         ServerLevel dLevel = server.getLevel(DUNGEON_REALM);
         if (dLevel != null) {
            for (ServerPlayer sp : new ArrayList<>(dLevel.players())) {
               if (!sp.isSpectator() && !isInDungeon(sp.getUUID())) {
                  rescueStrandedPlayer(sp);
               }
            }
         }
      }

      long currentTick = server.overworld().getGameTime();
      com.soybe.awakeningsystem.DungeonWorldSavedData.get(server).ensureRiftsLoaded(server);
      if (server.getTickCount() % 40 == 0) {
         enforceRiftSeparation(server);
      }

      List<com.soybe.awakeningsystem.DungeonManager.WorldRift> expiredRifts = new ArrayList<>();

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
         if (!rift.isPermanent && currentTick > rift.expireTick) {
            expiredRifts.add(rift);
         }
      }

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift expired : expiredRifts) {
         triggerDungeonBreak(server, expired);
      }

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift riftx : WORLD_RIFTS) {
         ServerLevel riftLevel = server.getLevel(riftx.dimension);
         if (riftLevel != null) {
            double cx = riftx.pos.getX() + 0.5;
            double cy = riftx.pos.getY() + 2.2;
            double cz = riftx.pos.getZ() + 0.5;
            boolean isRedGate = riftx.isRedGate;
            if (riftLevel.getGameTime() % 40L == 0L) {
               riftLevel.playSound(
                  null, cx, cy, cz, (SoundEvent)com.soybe.awakeningsystem.ModSounds.GATE_AMBIENT.value(), SoundSource.AMBIENT, 0.45F, isRedGate ? 0.7F : 0.85F
               );
            }

            for (ServerPlayer player : riftLevel.players()) {
               if (!isInDungeon(player.getUUID())) {
                  double dxx = player.getX() - cx;
                  double dyx = player.getY() + 1.0 - cy;
                  double dzx = player.getZ() - cz;
                  double horizDistSqx = dxx * dxx + dzx * dzx;
                  com.soybe.awakeningsystem.SystemData pData = com.soybe.awakeningsystem.SystemEvents.data(player);
                  String playerRankStr = com.soybe.awakeningsystem.SystemData.rankForLevel(pData.level());
                  int playerTier = getRankTier(playerRankStr);
                  int riftTier = getRankTier(riftx.rank);
                  int minRequiredTier = Math.max(1, riftTier - 1);
                  if (playerTier < minRequiredTier) {
                     if (horizDistSqx <= 7.839999999999999 && Math.abs(dyx) < 3.5) {
                        double pushX = player.getX() - cx;
                        double pushZ = player.getZ() - cz;
                        double pushDist = Math.sqrt(pushX * pushX + pushZ * pushZ);
                        if (pushDist < 0.001) {
                           pushX = 1.0;
                           pushDist = 1.0;
                        }

                        player.setDeltaMovement(pushX / pushDist * 1.5, 0.42, pushZ / pushDist * 1.5);
                        player.hurtMarked = true;
                        long lastShock = player.getPersistentData().getLong("LastRiftShockwave");
                        long nowGameTime = player.level().getGameTime();
                        if (nowGameTime - lastShock >= 40L) {
                           player.getPersistentData().putLong("LastRiftShockwave", nowGameTime);
                           riftLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.3F, 0.5F);
                           riftLevel.playSound(
                              null,
                              player.getX(),
                              player.getY(),
                              player.getZ(),
                              (SoundEvent)com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(),
                              SoundSource.PLAYERS,
                              0.8F,
                              0.6F
                           );
                           riftLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 0.8, 0.5, 0.15);
                           String minRankName = getRepresentativeRankForTier(minRequiredTier);
                           player.displayClientMessage(
                              Component.translatable("dungeon.awakening_system.dimensional_shockwave", new Object[]{playerRankStr, minRankName}), true
                           );
                        }
                     }
                  } else {
                     Guild controllingGuild = null;
                     GuildSavedData guildData = GuildSavedData.get(server);
                     if (riftx.owningGuildId != null && !riftx.owningGuildId.isEmpty()) {
                        controllingGuild = guildData.getGuildById(riftx.owningGuildId);
                     } else {
                        String dimStr = riftx.dimension.location().toString();
                        int riftChunkX = riftx.pos.getX() >> 4;
                        int riftChunkZ = riftx.pos.getZ() >> 4;
                        controllingGuild = guildData.getGuildByChunk(dimStr, riftChunkX, riftChunkZ);
                     }

                     if (controllingGuild != null) {
                        Guild playerGuild = guildData.getGuildByPlayer(player.getUUID());
                        boolean isMember = playerGuild != null && playerGuild.getId().equals(controllingGuild.getId());
                        boolean isAlly = playerGuild != null && controllingGuild.isAlliedWith(playerGuild.getId());
                        boolean hasRental = riftx.isRenterAuthorized(player.getUUID(), riftLevel.getGameTime());
                        if (!isMember && !isAlly && !hasRental) {
                           if (horizDistSqx <= 4.840000000000001 && Math.abs(dyx) < 3.5) {
                              double pushXx = player.getX() - cx;
                              double pushZx = player.getZ() - cz;
                              double pushDistx = Math.max(0.001, Math.sqrt(pushXx * pushXx + pushZx * pushZx));
                              player.setDeltaMovement(pushXx / pushDistx * 0.5, 0.15, pushZx / pushDistx * 0.5);
                              player.hurtMarked = true;
                              if (player.tickCount % 50 == 0) {
                                 sendRentalPrompt(player, riftx, controllingGuild);
                              }
                              continue;
                           }

                           if (horizDistSqx <= 36.0 && player.tickCount % 40 == 0) {
                              player.displayClientMessage(
                                 Component.translatable("dungeon.awakening_system.portal_rented", new Object[]{controllingGuild.getName(), riftx.rentalFeeMora}),
                                 true
                              );
                           }
                           continue;
                        }
                     }

                     if (horizDistSqx <= 1.0 && Math.abs(dyx) < 3.5) {
                        player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        if (riftx.activeHostSessionPlayerUUID != null) {
                           com.soybe.awakeningsystem.DungeonManager.DungeonSession hostSession = SESSIONS.get(riftx.activeHostSessionPlayerUUID);
                           if (hostSession != null && hostSession.combatActive) {
                              player.displayClientMessage(
                                 Component.translatable("awakening_system.dungeon.entering_rift_coop", new Object[]{getGateTitle(riftx.rank, riftx.isRedGate)}),
                                 true
                              );
                              joinDungeonCoop(player, hostSession);
                              break;
                           }

                           riftx.activeHostSessionPlayerUUID = null;
                        }

                        player.displayClientMessage(
                           Component.translatable("awakening_system.dungeon.entering_rift", new Object[]{getGateTitle(riftx.rank, riftx.isRedGate)}), true
                        );
                        startDungeon(player, riftx.rank, riftx.biome, riftx.isRedGate, riftx);
                        break;
                     }

                     if (horizDistSqx < 36.0 && player.tickCount % 80 == 0) {
                        if (controllingGuild != null) {
                           player.displayClientMessage(
                              Component.translatable("dungeon.awakening_system.portal_guild_territory", new Object[]{controllingGuild.getName(), riftx.rank}),
                              true
                           );
                        } else if (riftx.isPermanent) {
                           player.displayClientMessage(Component.translatable("dungeon.awakening_system.portal_unclaimed", new Object[]{riftx.rank}), true);
                        } else {
                           player.displayClientMessage(
                              Component.translatable("awakening_system.dungeon.rift_nearby_prompt", new Object[]{getGateTitle(riftx.rank, riftx.isRedGate)}),
                              true
                           );
                        }
                     }
                  }
               }
            }
         }
      }

      if (server.getTickCount() % 40 == 0) {
         syncRiftsToAll(server);
      }
   }

   public static void sendRentalPrompt(ServerPlayer player, com.soybe.awakeningsystem.DungeonManager.WorldRift rift, Guild guild) {
      Component currName = Component.translatable(rift.feeInCoins ? "currency.awakening_system.system_coins" : "currency.awakening_system.mora");
      Component prompt = Component.literal("§6═══════════════════════════════════════════\n")
         .append(Component.translatable("dungeon.awakening_system.rental.title"))
         .append("\n")
         .append(Component.translatable("dungeon.awakening_system.rental.belongs_to", new Object[]{guild.getName()}))
         .append("\n")
         .append(Component.translatable("dungeon.awakening_system.rental.fee", new Object[]{rift.rentalFeeMora, currName}))
         .append("\n\n")
         .append(
            Component.literal("  ")
               .append(
                  Component.translatable("dungeon.awakening_system.rental.button")
                     .withStyle(
                        style -> style.withColor(ChatFormatting.GREEN)
                           .withBold(true)
                           .withClickEvent(
                              new ClickEvent(
                                 Action.RUN_COMMAND, "/guild portal rent request " + rift.pos.getX() + " " + rift.pos.getY() + " " + rift.pos.getZ()
                              )
                           )
                           .withHoverEvent(
                              new HoverEvent(
                                 net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("dungeon.awakening_system.rental.hover")
                              )
                           )
                     )
               )
         )
         .append(Component.literal("\n§6═══════════════════════════════════════════"));
      player.sendSystemMessage(prompt);
      player.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
   }

   public static void exitDungeonSilently(ServerPlayer player) {
      if (player != null) {
         UUID playerId = player.getUUID();
         com.soybe.awakeningsystem.DungeonManager.DungeonSession session = SESSIONS.get(playerId);
         if (session != null) {
            EXIT_COOLDOWNS.put(playerId, 0L);
            if (session.bossEvent != null) {
               session.bossEvent.removePlayer(player);
            }

            SESSIONS.remove(playerId);
            session.coopPlayers.remove(playerId);
            boolean isHost = session.playerId.equals(playerId);
            if (isHost) {
               MinecraftServer server = player.getServer();
               if (server != null) {
                  UUID newHostId = null;

                  for (UUID coopId : session.coopPlayers) {
                     ServerPlayer coopP = server.getPlayerList().getPlayer(coopId);
                     if (coopP != null && coopP.isAlive() && coopP.level().dimension().equals(session.dungeonDimension)) {
                        newHostId = coopId;
                        break;
                     }
                  }

                  if (newHostId != null) {
                     session.coopPlayers.remove(newHostId);
                     session.playerId = newHostId;
                     SESSIONS.put(newHostId, session);
                     ServerPlayer newHostPlayer = server.getPlayerList().getPlayer(newHostId);
                     if (newHostPlayer != null) {
                        newHostPlayer.sendSystemMessage(Component.translatable("awakening_system.dungeon.new_leader_limbo"));
                     }

                     return;
                  }

                  ServerLevel dungeonLevel = server.getLevel(session.dungeonDimension);
                  if (dungeonLevel != null && session.enemyUUIDs != null) {
                     for (UUID enemyId : session.enemyUUIDs) {
                        Entity e = dungeonLevel.getEntity(enemyId);
                        if (e != null && e.isAlive()) {
                           e.discard();
                        }
                     }
                  }
               }

               if (session.bossEvent != null) {
                  session.bossEvent.removeAllPlayers();
                  session.bossEvent.setVisible(false);
               }
            }
         }
      }
   }

   public static void exitDungeon(MinecraftServer server, UUID playerId) {
      com.soybe.awakeningsystem.DungeonManager.DungeonSession session = SESSIONS.get(playerId);
      if (session != null) {
         boolean isHost = session.playerId.equals(playerId);
         ServerPlayer player = server.getPlayerList().getPlayer(playerId);
         ServerLevel returnLevel = server.getLevel(session.returnDimension);
         if (returnLevel == null) {
            returnLevel = server.getLevel(Level.OVERWORLD);
         }

         EXIT_COOLDOWNS.put(playerId, 0L);
         if (session.bossEvent != null && player != null) {
            session.bossEvent.removePlayer(player);
         }

         double spawnX = session.returnPos.x;
         double spawnY = session.returnPos.y;
         double spawnZ = session.returnPos.z;
         if (session.sourceRift != null && session.sourceRift.pos != null) {
            spawnX = session.sourceRift.pos.getX() + 0.5;
            spawnZ = session.sourceRift.pos.getZ() - 4.5;
            spawnY = findSafeFloorY(returnLevel, spawnX, session.sourceRift.pos.getY(), spawnZ);
         }

         if (player != null && returnLevel != null) {
            player.teleportTo(returnLevel, spawnX, spawnY, spawnZ, session.returnYaw, session.returnPitch);
            player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.7F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.action.returned_safe"), true);
         }

         SESSIONS.remove(playerId);
         session.coopPlayers.remove(playerId);
         if (!isHost) {
            ServerPlayer host = server.getPlayerList().getPlayer(session.playerId);
            if (host != null && player != null) {
               host.displayClientMessage(Component.translatable("awakening_system.dungeon.coop_ally_left", new Object[]{player.getName().getString()}), false);
            }
         } else {
            UUID newHostId = null;

            for (UUID coopId : session.coopPlayers) {
               ServerPlayer coopP = server.getPlayerList().getPlayer(coopId);
               if (coopP != null && coopP.isAlive() && coopP.level().dimension().equals(session.dungeonDimension)) {
                  newHostId = coopId;
                  break;
               }
            }

            if (newHostId != null) {
               session.coopPlayers.remove(newHostId);
               session.playerId = newHostId;
               SESSIONS.put(newHostId, session);
               ServerPlayer newHostPlayer = server.getPlayerList().getPlayer(newHostId);
               if (newHostPlayer != null) {
                  newHostPlayer.sendSystemMessage(Component.translatable("awakening_system.dungeon.new_leader_left"));
                  newHostPlayer.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
               }
            } else {
               for (UUID coopIdx : new ArrayList<>(session.coopPlayers)) {
                  exitDungeon(server, coopIdx);
               }

               session.coopPlayers.clear();
               if (session.bossEvent != null) {
                  session.bossEvent.removeAllPlayers();
                  session.bossEvent.setVisible(false);
               }

               if (session.lastEnemyUUID != null) {
                  try {
                     Scoreboard sb = server.getScoreboard();
                     PlayerTeam team = sb.getPlayerTeam("dungeon_last_target");
                     if (team != null) {
                        sb.removePlayerFromTeam(session.lastEnemyUUID.toString(), team);
                     }
                  } catch (Exception var17) {
                  }
               }

               if (session.rouletteItemEntity != null) {
                  session.rouletteItemEntity.discard();
                  session.rouletteItemEntity = null;
               }

               if (session.sourceRift != null) {
                  if (session.sourceRift.isPermanent) {
                     session.sourceRift.cooldownUntil = 0L;
                     session.sourceRift.activeHostSessionPlayerUUID = null;
                     syncRiftsToAll(server);
                     if (returnLevel != null && session.sourceRift.pos != null) {
                        returnLevel.playSound(
                           null,
                           session.sourceRift.pos.getX() + 0.5,
                           session.sourceRift.pos.getY() + 1.0,
                           session.sourceRift.pos.getZ() + 0.5,
                           (SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_GATE_OPEN.value(),
                           SoundSource.BLOCKS,
                           1.0F,
                           0.9F
                        );
                        returnLevel.sendParticles(
                           ParticleTypes.HAPPY_VILLAGER,
                           session.sourceRift.pos.getX() + 0.5,
                           session.sourceRift.pos.getY() + 2.0,
                           session.sourceRift.pos.getZ() + 0.5,
                           35,
                           1.5,
                           2.0,
                           1.5,
                           0.1
                        );
                     }

                     if (player != null) {
                        player.displayClientMessage(Component.translatable("dungeon.awakening_system.portal_conquered"), false);
                     }
                  } else if (session.chestClaimed || !session.combatActive) {
                     WORLD_RIFTS.remove(session.sourceRift);
                     syncRiftsToAll(server);
                     if (returnLevel != null && session.sourceRift.pos != null) {
                        returnLevel.playSound(
                           null,
                           session.sourceRift.pos.getX() + 0.5,
                           session.sourceRift.pos.getY() + 1.0,
                           session.sourceRift.pos.getZ() + 0.5,
                           (SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_GATE_OPEN.value(),
                           SoundSource.BLOCKS,
                           1.0F,
                           0.9F
                        );
                        returnLevel.sendParticles(
                           ParticleTypes.END_ROD,
                           session.sourceRift.pos.getX() + 0.5,
                           session.sourceRift.pos.getY() + 2.0,
                           session.sourceRift.pos.getZ() + 0.5,
                           60,
                           1.5,
                           2.0,
                           1.5,
                           0.1
                        );
                     }
                  } else if (player != null) {
                     player.displayClientMessage(Component.translatable("awakening_system.dungeon.left_unconquered"), false);
                  }
               }

               ServerLevel dungeonLevel = server.getLevel(session.dungeonDimension);
               if (dungeonLevel != null) {
                  for (UUID enemyId : session.enemyUUIDs) {
                     Entity enemy = dungeonLevel.getEntity(enemyId);
                     if (enemy != null) {
                        enemy.discard();
                     }
                  }

                  for (BlockPos p : session.arenaBlocks) {
                     dungeonLevel.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                  }

                  if (session.arenaCenter != null) {
                     purgeDungeonVolume(dungeonLevel, session.arenaCenter);
                  }
               }
            }
         }
      }
   }

   public static void purgeDungeonVolume(ServerLevel dungeonLevel, BlockPos center) {
      if (dungeonLevel != null && center != null) {
         int minX = center.getX() - 95;
         int maxX = center.getX() + 95;
         int minY = 20;
         int maxY = 115;
         int minZ = center.getZ() - 65;
         int maxZ = center.getZ() + 330;
         AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

         for (Entity e : dungeonLevel.getEntities((Entity)null, box, entity -> !(entity instanceof Player))) {
            e.discard();
         }

         int minChunkX = minX >> 4;
         int maxChunkX = maxX >> 4;
         int minChunkZ = minZ >> 4;
         int maxChunkZ = maxZ >> 4;
         MutableBlockPos mbp = new MutableBlockPos();

         for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
               if (dungeonLevel.hasChunk(cx, cz)) {
                  LevelChunk chunk = dungeonLevel.getChunk(cx, cz);
                  LevelChunkSection[] sections = chunk.getSections();

                  for (int secIdx = 0; secIdx < sections.length; secIdx++) {
                     LevelChunkSection sec = sections[secIdx];
                     if (sec != null && !sec.hasOnlyAir()) {
                        int secMinY = dungeonLevel.getMinBuildHeight() + (secIdx << 4);
                        int secMaxY = secMinY + 15;
                        if (secMaxY >= minY && secMinY <= maxY) {
                           int startY = Math.max(minY, secMinY);
                           int endY = Math.min(maxY, secMaxY);
                           int startX = Math.max(minX, cx << 4);
                           int endX = Math.min(maxX, (cx << 4) + 15);
                           int startZ = Math.max(minZ, cz << 4);
                           int endZ = Math.min(maxZ, (cz << 4) + 15);

                           for (int bx = startX; bx <= endX; bx++) {
                              for (int bz = startZ; bz <= endZ; bz++) {
                                 for (int by = startY; by <= endY; by++) {
                                    mbp.set(bx, by, bz);
                                    if (!dungeonLevel.isEmptyBlock(mbp)) {
                                       dungeonLevel.setBlock(mbp, Blocks.AIR.defaultBlockState(), 2);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void triggerDungeonBreak(MinecraftServer server, com.soybe.awakeningsystem.DungeonManager.WorldRift rift) {
      if (rift != null && !rift.isPermanent) {
         WORLD_RIFTS.remove(rift);
         syncRiftsToAll(server);
         ServerLevel riftLevel = server.getLevel(rift.dimension);
         if (riftLevel == null) {
            riftLevel = server.getLevel(Level.OVERWORLD);
         }

         if (riftLevel != null) {
            BlockPos center = rift.pos;

            for (com.soybe.awakeningsystem.DungeonManager.DungeonSession session : new ArrayList<>(SESSIONS.values())) {
               if (session.sourceRift == rift) {
                  ServerPlayer sp = server.getPlayerList().getPlayer(session.playerId);
                  if (sp != null) {
                     sp.displayClientMessage(Component.translatable("awakening_system.dungeon.dungeon_break_imminent"), false);
                     sp.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_BREAK.value(), SoundSource.PLAYERS, 1.5F, 0.8F);
                  }

                  exitDungeon(server, session.playerId);
               }
            }

            Component breakMsg = Component.translatable(
                  "awakening_system.dungeon.break_broadcast", new Object[]{rift.rank, center.getX(), center.getY(), center.getZ()}
               )
               .withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD});

            for (ServerPlayer player : riftLevel.players()) {
               player.sendSystemMessage(breakMsg);
               player.playNotifySound((SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_BREAK.value(), SoundSource.HOSTILE, 1.8F, 0.9F);
            }

            int bChunkX = center.getX() >> 4;
            int bChunkZ = center.getZ() >> 4;

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  riftLevel.getChunkSource().getChunk(bChunkX + dx, bChunkZ + dz, ChunkStatus.FULL, true);
               }
            }

            LightningBolt bolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create(riftLevel);
            if (bolt != null) {
               bolt.moveTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
               bolt.setVisualOnly(true);
               riftLevel.addFreshEntity(bolt);
            }

            riftLevel.playSound(
               null,
               center.getX() + 0.5,
               center.getY() + 1.5,
               center.getZ() + 0.5,
               (SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_BREAK.value(),
               SoundSource.HOSTILE,
               2.0F,
               0.8F
            );
            riftLevel.playSound(null, center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5F, 0.6F);
            riftLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5, 5, 1.0, 1.0, 1.0, 0.1);
            riftLevel.sendParticles(ParticleTypes.LARGE_SMOKE, center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5, 80, 2.5, 2.0, 2.5, 0.15);
            riftLevel.sendParticles(ParticleTypes.DRAGON_BREATH, center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5, 100, 2.0, 2.5, 2.0, 0.2);
            riftLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5, 120, 3.0, 2.5, 3.0, 0.25);
            int tier = getRankTier(rift.rank);
            int mobCount = 12 + Math.min(16, tier * 2);
            RandomSource rng = riftLevel.random;

            for (int i = 0; i < mobCount; i++) {
               double ox = (rng.nextDouble() - 0.5) * 8.0;
               double oz = (rng.nextDouble() - 0.5) * 8.0;
               double oy = rng.nextDouble() * 1.5;
               String riftTheme = rift.biome != null ? rift.biome.toLowerCase(Locale.ROOT) : "crypt";
               boolean isBaranRift = "baran".equalsIgnoreCase(riftTheme) || "nether".equalsIgnoreCase(riftTheme) || "magma_forge".equalsIgnoreCase(riftTheme);
               Monster mob = null;
               if (isBaranRift) {
                  float pick = rng.nextFloat();
                  if (pick < 0.45F) {
                     mob = (Monster)ModEntities.ABYSSAL_ORC.get().create(riftLevel);
                  } else {
                     mob = (Monster)ModEntities.SHADOW_INFANTRY.get().create(riftLevel);
                  }
               } else {
                  float pick = rng.nextFloat();
                  if (pick < 0.4F) {
                     mob = (Monster)ModEntities.SHADOW_INFANTRY.get().create(riftLevel);
                  } else if (pick < 0.7F) {
                     mob = (Monster)ModEntities.FROST_ELF_ASSASSIN.get().create(riftLevel);
                  } else {
                     mob = (Monster)ModEntities.ABYSSAL_ORC.get().create(riftLevel);
                  }
               }

               if (mob != null) {
                  mob.moveTo(center.getX() + 0.5 + ox, center.getY() + 1.0 + oy, center.getZ() + 0.5 + oz, rng.nextFloat() * 360.0F, 0.0F);
                  double vx = (ox != 0.0 ? ox / Math.abs(ox) : 1.0) * (0.3 + rng.nextDouble() * 0.4);
                  double vy = 0.25 + rng.nextDouble() * 0.35;
                  double vz = (oz != 0.0 ? oz / Math.abs(oz) : 1.0) * (0.3 + rng.nextDouble() * 0.4);
                  mob.setDeltaMovement(vx, vy, vz);
                  mob.setPersistenceRequired();
                  if (mob instanceof Zombie || mob instanceof AbstractSkeleton) {
                     mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                     mob.setDropChance(EquipmentSlot.HEAD, 0.05F);
                  }

                  mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0, false, false));
                  mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1, false, false));
                  applyDungeonMobMetadata(mob, rift.rank, 1, false);
                  riftLevel.addFreshEntity(mob);
               }
            }

            String rTheme = rift.biome != null ? rift.biome.toLowerCase(Locale.ROOT) : "crypt";
            boolean isBaranDoor = "baran".equalsIgnoreCase(rTheme) || "nether".equalsIgnoreCase(rTheme) || "magma_forge".equalsIgnoreCase(rTheme);
            boolean isBeruDoor = "beru".equalsIgnoreCase(rTheme)
               || "jeju".equalsIgnoreCase(rTheme)
               || "ant".equalsIgnoreCase(rTheme)
               || "insect".equalsIgnoreCase(rTheme);
            Monster leader = null;
            if (isBaranDoor) {
               BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), riftLevel);
               b.setCustomName(Component.translatable("awakening_system.dungeon.break_leader_baran"));
               leader = b;
            } else if (isBeruDoor) {
               BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), riftLevel);
               beru.setCustomName(Component.translatable("awakening_system.dungeon.break_leader_beru"));
               leader = beru;
            } else {
               int roll = rng.nextInt(3);
               if (roll == 0) {
                  BaranBossEntity b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), riftLevel);
                  b.setCustomName(Component.translatable("awakening_system.dungeon.break_leader_baran"));
                  leader = b;
               } else if (roll == 1) {
                  IgrisBossEntity igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), riftLevel);
                  igris.setCustomName(Component.translatable("awakening_system.dungeon.break_leader_igris"));
                  leader = igris;
               } else {
                  BeruBossEntity beru = new BeruBossEntity(ModEntities.BERU_BOSS.get(), riftLevel);
                  beru.setCustomName(Component.translatable("awakening_system.dungeon.break_leader_beru"));
                  leader = beru;
               }
            }

            if (leader != null) {
               leader.setCustomNameVisible(true);
               leader.setPersistenceRequired();
               leader.moveTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, rng.nextFloat() * 360.0F, 0.0F);
               leader.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1));
               leader.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
               applyDungeonMobMetadata(leader, rift.rank, 2, false);
               riftLevel.addFreshEntity(leader);
            }

            RECENT_BREAK_LOCATIONS.put(center, riftLevel.getGameTime() + 12000L);
            com.soybe.awakeningsystem.DungeonWorldSavedData.get(server).setDirty();
         }
      }
   }

   public static void onChunkLoadedForExploration(ServerLevel level, ChunkPos chunkPos) {
      if (level.dimension() == Level.OVERWORLD) {
         long chunkKey = chunkPos.toLong();
         if (PROCESSED_EXPLORATION_CHUNKS.add(chunkKey)) {
            if (PROCESSED_EXPLORATION_CHUNKS.size() > 50000) {
               PROCESSED_EXPLORATION_CHUNKS.clear();
            }

            long seed = level.getSeed();
            long chunkHash = chunkPos.x * 341873128712L + chunkPos.z * 132897987541L ^ seed;
            RandomSource rng = RandomSource.create(chunkHash);
            if (rng.nextInt(160) == 0) {
               int blockX = chunkPos.getMinBlockX() + 4 + rng.nextInt(8);
               int blockZ = chunkPos.getMinBlockZ() + 4 + rng.nextInt(8);
               if (!isTooCloseToExistingRift(Level.OVERWORLD, blockX, blockZ, 450.0)) {
                  int surfaceY = level.getHeight(Types.WORLD_SURFACE, blockX, blockZ);
                  if (surfaceY > level.getMinBuildHeight() + 10 && surfaceY < level.getMaxBuildHeight() - 10) {
                     BlockPos target = new BlockPos(blockX, surfaceY, blockZ);
                     if (isValidNaturalGroundForRift(level, target)) {
                        float r = rng.nextFloat();
                        String rank;
                        if (r < 0.35F) {
                           rank = "E";
                        } else if (r < 0.63F) {
                           rank = "D";
                        } else if (r < 0.83F) {
                           rank = "C";
                        } else if (r < 0.94F) {
                           rank = "B";
                        } else if (r < 0.985F) {
                           rank = "A";
                        } else {
                           rank = "S";
                        }

                        boolean isRedGate = false;
                        String[] THEMES = new String[]{
                           "crypt",
                           "plains",
                           "lush_plains",
                           "cherry_plains",
                           "autumn_plains",
                           "tundra",
                           "ancient_temple",
                           "crystal_cave",
                           "abyssal_chasm",
                           "magma_forge",
                           "castle",
                           "ice",
                           "ant",
                           "goblins",
                           "hobgoblin",
                           "high_orc",
                           "hell",
                           "temple"
                        };
                        String biome = THEMES[rng.nextInt(THEMES.length)];
                        ensureRiftGround(level, target);
                        com.soybe.awakeningsystem.DungeonManager.WorldRift rift = new com.soybe.awakeningsystem.DungeonManager.WorldRift(
                           target, rank, biome, Level.OVERWORLD, Long.MAX_VALUE, isRedGate, rng.nextFloat() * 360.0F, true
                        );
                        rift.isNewlySpawned = false;
                        rift.spawnTimeMs = 0L;
                        WORLD_RIFTS.add(rift);
                        if (level.getServer() != null) {
                           com.soybe.awakeningsystem.DungeonWorldSavedData.get(level.getServer()).setDirty();
                           syncRiftsToAll(level.getServer());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void checkSpawnWorldRift(ServerPlayer player) {
      if (!isInDungeon(player.getUUID())) {
         if (player.level().dimension() == Level.OVERWORLD) {
            if (WORLD_RIFTS.size() < 15) {
               if (!isTooCloseToExistingRift(Level.OVERWORLD, player.getBlockX(), player.getBlockZ(), 130.0)) {
                  long nowTick = player.level().getGameTime();

                  for (Entry<BlockPos, Long> entry : RECENT_BREAK_LOCATIONS.entrySet()) {
                     if (entry.getValue() > nowTick && player.blockPosition().closerThan((Vec3i)entry.getKey(), 180.0)) {
                        return;
                     }
                  }

                  RandomSource rng = player.getRandom();
                  if (!(rng.nextFloat() > 0.2F)) {
                     com.soybe.awakeningsystem.SystemData data = com.soybe.awakeningsystem.SystemEvents.data(player);
                     int lvl = data.level();
                     String rank = lvl >= 70
                        ? (rng.nextBoolean() ? "S" : "A")
                        : (
                           lvl >= 50
                              ? (rng.nextBoolean() ? "A" : "B")
                              : (lvl >= 30 ? (rng.nextBoolean() ? "B" : "C") : (lvl >= 15 ? (rng.nextBoolean() ? "C" : "D") : "D"))
                        );
                     ServerLevel level = (ServerLevel)player.level();
                     BlockPos target = null;

                     label179:
                     for (int attempt = 0; attempt < 24; attempt++) {
                        double angle = rng.nextDouble() * Math.PI * 2.0;
                        double dist = 65.0 + rng.nextDouble() * 75.0;
                        int rx = (int)(player.getX() + Math.cos(angle) * dist);
                        int rz = (int)(player.getZ() + Math.sin(angle) * dist);
                        if (!isTooCloseToExistingRift(Level.OVERWORLD, rx, rz, 180.0)) {
                           boolean nearBreak = false;

                           for (Entry<BlockPos, Long> entryx : RECENT_BREAK_LOCATIONS.entrySet()) {
                              if (entryx.getValue() > nowTick && entryx.getKey().closerThan(new BlockPos(rx, player.getBlockY(), rz), 180.0)) {
                                 nearBreak = true;
                                 break;
                              }
                           }

                           if (!nearBreak) {
                              level.getChunk(rx >> 4, rz >> 4, ChunkStatus.FULL, true);
                              int surfaceY = level.getHeight(Types.WORLD_SURFACE, rx, rz);
                              if (surfaceY > level.getMinBuildHeight() + 5) {
                                 BlockPos cand = null;
                                 int y = surfaceY + 2;

                                 while (true) {
                                    if (y >= surfaceY - 6) {
                                       BlockPos checkPos = new BlockPos(rx, y, rz);
                                       BlockState cur = level.getBlockState(checkPos);
                                       BlockState below = level.getBlockState(checkPos.below());
                                       if (!cur.isAir() && cur.blocksMotion()
                                          || !below.isSolid() && !below.blocksMotion() && !below.is(Blocks.WATER)
                                          || below.is(Blocks.LAVA)) {
                                          y--;
                                          continue;
                                       }

                                       cand = checkPos;
                                    }

                                    if (cand != null
                                       && isValidNaturalGroundForRift(level, cand)
                                       && (level.canSeeSky(cand.above()) || level.canSeeSky(cand) || attempt > 16)) {
                                       target = cand;
                                       break label179;
                                    }
                                    break;
                                 }
                              }
                           }
                        }
                     }

                     if (target != null) {
                        ensureRiftGround(level, target);
                        boolean isPermanent = rng.nextFloat() < 0.15F;
                        long expireTick = isPermanent ? Long.MAX_VALUE : level.getGameTime() + 720000L;
                        boolean isRed = false;
                        String[] THEMES = new String[]{
                           "crypt",
                           "plains",
                           "lush_plains",
                           "cherry_plains",
                           "autumn_plains",
                           "tundra",
                           "ancient_temple",
                           "crystal_cave",
                           "abyssal_chasm",
                           "magma_forge"
                        };
                        String biome = THEMES[rng.nextInt(THEMES.length)];
                        com.soybe.awakeningsystem.DungeonManager.WorldRift rift = new com.soybe.awakeningsystem.DungeonManager.WorldRift(
                           target, rank, biome, Level.OVERWORLD, expireTick, isRed, 0.0F, isPermanent
                        );
                        rift.isNewlySpawned = true;
                        rift.spawnTimeMs = System.currentTimeMillis();
                        WORLD_RIFTS.add(rift);
                        if (isPermanent && player.getServer() != null) {
                           com.soybe.awakeningsystem.DungeonWorldSavedData.get(player.getServer()).setDirty();
                        }

                        if (player.getServer() != null) {
                           syncRiftsToAll(player.getServer());
                        }

                        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 3.0F, 0.85F);
                        level.playSound(
                           null,
                           target.getX(),
                           target.getY(),
                           target.getZ(),
                           (SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_GATE_OPEN.value(),
                           SoundSource.AMBIENT,
                           2.5F,
                           1.0F
                        );
                        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 1.0F, 1.1F);

                        for (int py = 0; py < 120; py += 2) {
                           level.sendParticles(
                              isRed ? ParticleTypes.FLAME : ParticleTypes.PORTAL,
                              target.getX() + 0.5,
                              target.getY() + py,
                              target.getZ() + 0.5,
                              3,
                              0.3,
                              0.3,
                              0.3,
                              0.03
                           );
                           level.sendParticles(ParticleTypes.END_ROD, target.getX() + 0.5, target.getY() + py, target.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.01);
                        }

                        if (!isPermanent) {
                           com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                              player, "awakening_system.notification.dungeon_gate.title", "awakening_system.notification.rift_spawn.message", -3123713, rank
                           );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void ensureRiftGround(ServerLevel level, BlockPos target) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            BlockPos floorPos = target.offset(dx, -1, dz);

            for (int down = 0; down < 4; down++) {
               BlockPos p = floorPos.below(down);
               if (!level.getBlockState(p).isAir() && !level.getBlockState(p).liquid()) {
                  break;
               }

               level.setBlockAndUpdate(p, Blocks.STONE.defaultBlockState());
            }
         }
      }
   }

   public static void buildDungeonGateRuins(ServerLevel level, BlockPos target) {
      for (int dx = -4; dx <= 4; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            BlockPos basePos = target.offset(dx, -2, dz);

            for (int down = 0; down < 6; down++) {
               BlockPos fillPos = basePos.below(down);
               if (!level.getBlockState(fillPos).isAir() && !level.getBlockState(fillPos).liquid()) {
                  break;
               }

               level.setBlockAndUpdate(fillPos, Blocks.STONE.defaultBlockState());
            }
         }
      }

      boolean placed = TeyvatStructureManager.placeNbtStructure(level, target, "dungeon_gate_ruins", -4, -1, -2);
      if (!placed) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos floorPos = target.offset(dx, -1, dz);
               if (level.getBlockState(floorPos).isAir() || level.getBlockState(floorPos).liquid()) {
                  level.setBlockAndUpdate(floorPos, Blocks.STONE.defaultBlockState());
               }
            }
         }
      }

      for (int dx = -4; dx <= 4; dx++) {
         for (int dzx = -2; dzx <= 2; dzx++) {
            for (int dy = 0; dy <= 5; dy++) {
               BlockPos clearPos = target.offset(dx, dy, dzx);
               BlockState bs = level.getBlockState(clearPos);
               if (!bs.isAir() && (bs.getBlock() instanceof LeavesBlock || bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.TALL_GRASS) || bs.is(Blocks.VINE))) {
                  level.removeBlock(clearPos, false);
               }
            }

            for (int dyx = 6; dyx <= 30; dyx++) {
               BlockPos skyPos = target.offset(dx, dyx, dzx);
               BlockState bs = level.getBlockState(skyPos);
               if (bs.getBlock() instanceof LeavesBlock || bs.is(Blocks.VINE)) {
                  level.removeBlock(skyPos, false);
               }
            }
         }
      }
   }

   public static void spawnRiftNear(ServerPlayer player, String rank) {
      spawnRiftNear(player, rank, "crypt", false);
   }

   public static void spawnRiftNear(ServerPlayer player, String rank, String biome) {
      spawnRiftNear(player, rank, biome, false);
   }

   public static void spawnRiftNear(ServerPlayer player, String rank, String biome, boolean permanent) {
      if (LimboWorldManager.isLimboDimension(player.level())) {
         player.displayClientMessage(Component.translatable("awakening_system.limbo.dungeon_blocked"), true);
      } else if (isInDungeon(player.getUUID())) {
         player.displayClientMessage(Component.translatable("awakening_system.action.in_dungeon_already"), true);
      } else {
         if (player.level() instanceof ServerLevel level) {
            double rad = Math.toRadians(player.getYRot());
            double lookX = -Math.sin(rad);
            double lookZ = Math.cos(rad);
            double targetX = player.getX() + lookX * 4.8;
            double targetZ = player.getZ() + lookZ * 4.8;
            int ry = (int)player.getY();
            int rx = (int)Math.floor(targetX);
            int rz = (int)Math.floor(targetZ);
            BlockPos target = null;

            for (int dy = 3; dy >= -5; dy--) {
               BlockPos p = new BlockPos(rx, ry + dy, rz);
               if (level.getBlockState(p).isSolid() && level.getBlockState(p.above()).isAir() && level.getBlockState(p.above(2)).isAir()) {
                  target = p.above();
                  break;
               }
            }

            if (target == null) {
               target = new BlockPos(rx, ry, rz);
            }

            ensureRiftGround(level, target);
            long expireTick = permanent ? Long.MAX_VALUE : level.getGameTime() + 720000L;
            com.soybe.awakeningsystem.DungeonManager.WorldRift rift = new com.soybe.awakeningsystem.DungeonManager.WorldRift(
               target, rank, biome, level.dimension(), expireTick, false, player.getYRot(), permanent
            );
            rift.isNewlySpawned = true;
            rift.isPlayerSummoned = true;
            rift.spawnTimeMs = System.currentTimeMillis();
            WORLD_RIFTS.add(rift);
            if (permanent && player.getServer() != null) {
               com.soybe.awakeningsystem.DungeonWorldSavedData.get(player.getServer()).setDirty();
            }

            if (player.getServer() != null) {
               syncRiftsToAll(player.getServer());
            }

            level.playSound(
               null,
               target.getX(),
               target.getY(),
               target.getZ(),
               (SoundEvent)com.soybe.awakeningsystem.ModSounds.DUNGEON_GATE_OPEN.value(),
               SoundSource.AMBIENT,
               1.2F,
               1.0F
            );
            if (permanent) {
               player.sendSystemMessage(Component.translatable("awakening_system.dungeon.permanent_gate_created", new Object[]{rank}));
            } else {
               com.soybe.awakeningsystem.AwakeningSystemMod.notify(
                  player, "awakening_system.notification.dungeon_gate.title", "awakening_system.notification.rift_spawn.message", -3123713, rank
               );
            }
         }
      }
   }

   public static void syncRiftsToAll(MinecraftServer server) {
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         syncRiftsToPlayer(player);
      }
   }

   public static void syncRiftsToPlayer(ServerPlayer player) {
      List<com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry> entries = new ArrayList<>();

      for (com.soybe.awakeningsystem.DungeonManager.WorldRift rift : WORLD_RIFTS) {
         if (rift.dimension.equals(player.level().dimension())) {
            int type = rift.isPermanent ? 2 : 0;
            int beamTicks = 0;
            if (rift.isNewlySpawned && rift.spawnTimeMs > 0L) {
               long elapsed = System.currentTimeMillis() - rift.spawnTimeMs;
               if (elapsed < 5000L) {
                  beamTicks = (int)Math.max(0L, (5000L - elapsed) / 50L);
               } else {
                  rift.isNewlySpawned = false;
               }
            }

            int remainingTicks = rift.isPermanent ? -1 : (int)Math.max(0L, rift.expireTick - player.serverLevel().getGameTime());
            entries.add(
               new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(rift.pos, rift.rank, rift.isRedGate, type, rift.yaw, beamTicks, remainingTicks)
            );
         }
      }

      if (com.soybe.awakeningsystem.TowerManager.isInTower(player.getUUID())) {
         com.soybe.awakeningsystem.TowerManager.TowerSession ts = com.soybe.awakeningsystem.TowerManager.getSession(player.getUUID());
         if (ts != null && ts.isekaiPortalSpawned && ts.isekaiPortalPos != null) {
            entries.add(new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(ts.isekaiPortalPos, "ISEKAI", false, 99));
         }
      }

      if (player.level().dimension().equals(com.soybe.awakeningsystem.IsekaiWorldManager.ISEKAI_WORLD)) {
         entries.add(
            new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(
               com.soybe.awakeningsystem.YggdrasilTreeGenerator.OVERWORLD_RETURN_GATE_POS, "ISEKAI", false, 99
            )
         );
      }

      if (player.level().dimension().equals(com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD)) {
         entries.add(
            new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(com.soybe.awakeningsystem.MurimWorldManager.RETURN_PORTAL_POS.below(), "MURIM", false, 98)
         );
      }

      for (com.soybe.awakeningsystem.TeyvatWorldManager.AbyssPortalEntry portal : com.soybe.awakeningsystem.TeyvatWorldManager.REGISTERED_PORTALS) {
         if (player.level().dimension().equals(portal.dimension)) {
            if (portal.active) {
               entries.add(new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(portal.centerPos, "TEYVAT_ABYSS", false, 97, portal.facing.toYRot()));
            } else {
               entries.add(
                  new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(portal.centerPos, "TEYVAT_ABYSS_BLOCKED", false, 96, portal.facing.toYRot())
               );
            }
         }
      }

      com.soybe.awakeningsystem.DungeonManager.DungeonSession session = SESSIONS.get(player.getUUID());
      if (session != null && session.exitPortalPos != null && player.level().dimension().equals(session.dungeonDimension)) {
         entries.add(new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(session.exitPortalPos, session.rank, session.isRedGate, 1));
      }

      PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.SyncRiftsPayload(entries), new CustomPacketPayload[0]);
   }

   private static MinecraftServer getServer() {
      return ServerLifecycleHooks.getCurrentServer();
   }

   public static final class DungeonSession {
      public UUID playerId;
      public final String rank;
      public final ResourceKey<Level> returnDimension;
      public final Vec3 returnPos;
      public final float returnYaw;
      public final float returnPitch;
      public final ResourceKey<Level> dungeonDimension;
      public final BlockPos arenaCenter;
      public final Set<UUID> enemyUUIDs = new HashSet<>();
      public int totalEnemies;
      public ServerBossEvent bossEvent;
      public BlockPos chestPos;
      public BlockPos gatePos;
      public boolean chestSpawned = false;
      public boolean chestClaimed = false;
      public int returnCountdown = -1;
      public int maxReturnCountdown = 1200;
      public int claimedTicks = 0;
      public boolean combatActive = true;
      public int combatTimer = 3600;
      public int maxCombatTimer = 3600;
      public final Set<BlockPos> arenaBlocks = new HashSet<>();
      public UUID bossUUID = null;
      public boolean bossDefeated = false;
      public int currentFloor = 1;
      public int maxFloors = 3;
      public BlockPos descentPortalPos = null;
      public boolean floorCleared = false;
      public final Set<BlockPos> barrier1Blocks = new HashSet<>();
      public final Set<BlockPos> barrier2Blocks = new HashSet<>();
      public final Set<BlockPos> barrier3Blocks = new HashSet<>();
      public final Set<BlockPos> barrier4Blocks = new HashSet<>();
      public boolean barrier1Open = false;
      public boolean barrier2Open = false;
      public boolean barrier3Open = false;
      public boolean barrier4Open = false;
      public BlockPos barrier1Pos = null;
      public BlockPos barrier2Pos = null;
      public BlockPos barrier3Pos = null;
      public BlockPos barrier4Pos = null;
      public BlockPos roomP = null;
      public BlockPos roomC1 = null;
      public BlockPos roomC2 = null;
      public BlockPos roomC3 = null;
      public BlockPos roomMB_Center = null;
      public BlockPos roomC5 = null;
      public BlockPos roomL2_1 = null;
      public BlockPos roomL2_2 = null;
      public BlockPos roomB_Mid = null;
      public BlockPos roomL2_4 = null;
      public BlockPos roomB_Top = null;
      public BlockPos roomL2_6 = null;
      public BlockPos roomMB_Left = null;
      public BlockPos roomL1_2 = null;
      public BlockPos roomL1_3 = null;
      public BlockPos roomL1_4 = null;
      public BlockPos roomL1_5 = null;
      public BlockPos roomR4_1 = null;
      public BlockPos roomR4_2 = null;
      public BlockPos roomR4_3 = null;
      public BlockPos roomR4_4 = null;
      public BlockPos roomR4_5 = null;
      public BlockPos roomB_Right = null;
      public BlockPos roomR5_2 = null;
      public BlockPos roomR5_3 = null;
      public BlockPos roomR5_4 = null;
      public BlockPos roomR5_5 = null;
      public BlockPos portalRoomPos = null;
      public BlockPos roomC_W1 = null;
      public BlockPos miniboss1Pos = null;
      public BlockPos roomC_W2 = null;
      public BlockPos roomC_W3 = null;
      public BlockPos roomC_MidW = null;
      public BlockPos bossCandidate2Pos = null;
      public BlockPos roomC_W4 = null;
      public BlockPos roomC_W5 = null;
      public BlockPos bossCandidate1Pos = null;
      public BlockPos roomC_N1 = null;
      public BlockPos miniboss2Pos = null;
      public BlockPos roomC_Top1 = null;
      public BlockPos roomC_NE1 = null;
      public BlockPos roomC_NE2 = null;
      public BlockPos roomC_E1 = null;
      public BlockPos roomC_E2 = null;
      public BlockPos roomC_E3 = null;
      public BlockPos roomC_E4 = null;
      public BlockPos roomC_E5 = null;
      public BlockPos bossCandidate3Pos = null;
      public BlockPos eliteSanctum1Pos = null;
      public BlockPos eliteSanctum2Pos = null;
      public final Set<BlockPos> barrierBossBlocks = new HashSet<>();
      public BlockPos barrierBossPos1 = null;
      public BlockPos barrierBossPos2 = null;
      public BlockPos barrierBossPos3 = null;
      public boolean bossBarrierOpen = false;
      public int dungeonKeysDropped = 0;
      public int minibossesDefeated = 0;

      public void openBossBarrierDirectly(ServerLevel level) {
         if (this.bossBarrierOpen) return;
         this.bossBarrierOpen = true;

         for (BlockPos b : this.barrierBossBlocks) {
            level.setBlock(b, Blocks.AIR.defaultBlockState(), 2);
            level.sendParticles(ParticleTypes.FLASH, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.0);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.05);
         }
         this.barrierBossBlocks.clear();

         BlockPos ambushPos = this.barrierBossPos1 != null ? this.barrierBossPos1 : (this.barrierBossPos2 != null ? this.barrierBossPos2 : this.barrierBossPos3);
         if (ambushPos != null) {
            spawnBarrierDoorAmbush(level, this, ambushPos);
         }

         if (level.getServer() != null) {
            for (ServerPlayer ally : this.getAllPlayers(level.getServer())) {
               if (ally != null) {
                  ally.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.0F);
                  ally.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.2F);
                  ally.sendSystemMessage(Component.translatable("awakening_system.dungeon.minibosses_cleared"));
               }
            }
         }
      }
      public BlockPos room2Center = null;
      public BlockPos room3Center = null;
      public BlockPos room4Center = null;
      public BlockPos room5Center = null;
      public BlockPos thronePos = null;
      public BlockPos deadEndCentralChamber = null;
      public BlockPos sideWestChamber = null;
      public BlockPos deadEndWestChamber = null;
      public BlockPos sideEastChamber = null;
      public BlockPos deadEndEastChamber = null;
      public BlockPos secretTreasureChamber = null;
      public com.soybe.awakeningsystem.DungeonManager.WorldRift sourceRift = null;
      public long lastBarrierWarningTick = 0L;
      public final Set<UUID> coopPlayers = ConcurrentHashMap.newKeySet();
      public final Set<UUID> claimedPlayers = ConcurrentHashMap.newKeySet();
      public boolean rouletteSpinning = false;
      public int rouletteTicks = 0;
      public ItemEntity rouletteItemEntity = null;
      public ItemStack finalWinningStack = ItemStack.EMPTY;
      public int finalCoinsWon = 0;
      public int finalExpWon = 0;
      public final List<ItemStack> finalRewardStacks = new ArrayList<>();
      public boolean awaitingGambleDecision = false;
      public boolean isGamblingReward = false;
      public final String biome;
      public final boolean isRedGate;
      public BlockPos exitPortalPos = null;
      public long lastExitWarningTick = 0L;
      public boolean lastEnemyNotified = false;
      public UUID lastEnemyUUID = null;
      public int bunkerMinX = Integer.MIN_VALUE;
      public int bunkerMaxX = Integer.MAX_VALUE;
      public int bunkerMinY = Integer.MIN_VALUE;
      public int bunkerMaxY = Integer.MAX_VALUE;
      public int bunkerMinZ = Integer.MIN_VALUE;
      public int bunkerMaxZ = Integer.MAX_VALUE;
      public boolean bunkerBoundsCalculated = false;

      public List<ServerPlayer> getAllPlayers(MinecraftServer server) {
         List<ServerPlayer> list = new ArrayList<>();
         if (server == null) {
            return list;
         } else {
            ServerPlayer host = server.getPlayerList().getPlayer(this.playerId);
            if (host != null) {
               list.add(host);
            }

            for (UUID cid : this.coopPlayers) {
               ServerPlayer cp = server.getPlayerList().getPlayer(cid);
               if (cp != null && !list.contains(cp)) {
                  list.add(cp);
               }
            }

            ServerLevel dLevel = server.getLevel(this.dungeonDimension);
            if (dLevel != null) {
               for (ServerPlayer sp : dLevel.players()) {
                  if (!list.contains(sp) && !sp.isSpectator()) {
                     double distSq = sp.distanceToSqr(this.arenaCenter.getX() + 0.5, sp.getY(), this.arenaCenter.getZ() + 0.5);
                     double chestDistSq = this.chestPos != null
                        ? sp.distanceToSqr(this.chestPos.getX() + 0.5, sp.getY(), this.chestPos.getZ() + 0.5)
                        : Double.MAX_VALUE;
                     if (distSq <= 40000.0 || chestDistSq <= 10000.0) {
                        list.add(sp);
                        this.coopPlayers.add(sp.getUUID());
                        com.soybe.awakeningsystem.DungeonManager.SESSIONS.put(sp.getUUID(), this);
                        if (this.bossEvent != null) {
                           this.bossEvent.addPlayer(sp);
                        }
                     }
                  }
               }
            }

            return list;
         }
      }

      public DungeonSession(
         UUID playerId,
         String rank,
         String biome,
         ResourceKey<Level> returnDimension,
         Vec3 returnPos,
         float returnYaw,
         float returnPitch,
         ResourceKey<Level> dungeonDimension,
         BlockPos arenaCenter,
         int maxFloors,
         boolean isRedGate
      ) {
         this.playerId = playerId;
         this.rank = rank;
         this.biome = biome != null && !biome.isEmpty() ? biome : "crypt";
         this.returnDimension = returnDimension;
         this.returnPos = returnPos;
         this.returnYaw = returnYaw;
         this.returnPitch = returnPitch;
         this.dungeonDimension = dungeonDimension;
         this.arenaCenter = arenaCenter;
         this.maxFloors = maxFloors;
         this.isRedGate = isRedGate;
         Random rng = new Random(playerId.getLeastSignificantBits() ^ System.currentTimeMillis());
         int ox = arenaCenter.getX();
         int oy = arenaCenter.getY();
         int oz = arenaCenter.getZ();
         int S = 32;
         this.roomP = new BlockPos(ox, oy, oz);
         this.portalRoomPos = this.roomP;
         this.roomC1 = new BlockPos(ox, oy, oz - S);
         this.roomC2 = new BlockPos(ox, oy, oz - 2 * S);
         this.roomC3 = new BlockPos(ox, oy, oz - 3 * S);
         this.roomMB_Center = new BlockPos(ox, oy, oz - 4 * S);
         this.roomC5 = new BlockPos(ox, oy, oz - 5 * S);
         this.roomL2_1 = new BlockPos(ox - S, oy, oz - S);
         this.roomL2_2 = new BlockPos(ox - S, oy, oz - 2 * S);
         this.roomB_Mid = new BlockPos(ox - S, oy, oz - 3 * S);
         this.roomL2_4 = new BlockPos(ox - S, oy, oz - 4 * S);
         this.roomB_Top = new BlockPos(ox - S, oy, oz - 5 * S);
         this.roomL2_6 = new BlockPos(ox - S, oy, oz - 6 * S);
         this.roomMB_Left = new BlockPos(ox - 2 * S, oy, oz - S);
         this.roomL1_2 = new BlockPos(ox - 2 * S, oy, oz - 2 * S);
         this.roomL1_3 = new BlockPos(ox - 2 * S, oy, oz - 3 * S);
         this.roomL1_4 = new BlockPos(ox - 2 * S, oy, oz - 4 * S);
         this.roomL1_5 = new BlockPos(ox - 2 * S, oy, oz - 5 * S);
         this.roomR4_1 = new BlockPos(ox + S, oy, oz - S);
         this.roomR4_2 = new BlockPos(ox + S, oy, oz - 2 * S);
         this.roomR4_3 = new BlockPos(ox + S, oy, oz - 3 * S);
         this.roomR4_4 = new BlockPos(ox + S, oy, oz - 4 * S);
         this.roomR4_5 = new BlockPos(ox + S, oy, oz - 5 * S);
         this.roomB_Right = new BlockPos(ox + 2 * S, oy, oz - S);
         this.roomR5_2 = new BlockPos(ox + 2 * S, oy, oz - 2 * S);
         this.roomR5_3 = new BlockPos(ox + 2 * S, oy, oz - 3 * S);
         this.roomR5_4 = new BlockPos(ox + 2 * S, oy, oz - 4 * S);
         this.roomR5_5 = new BlockPos(ox + 2 * S, oy, oz - 5 * S);
         this.miniboss1Pos = this.roomMB_Left;
         this.miniboss2Pos = this.roomMB_Center;
         this.bossCandidate1Pos = this.roomB_Mid;
         this.bossCandidate2Pos = this.roomB_Top;
         this.bossCandidate3Pos = this.roomB_Right;
         int chosenBoss = rng.nextInt(3);
         if (chosenBoss == 0) {
            this.thronePos = this.bossCandidate1Pos;
            this.eliteSanctum1Pos = this.bossCandidate2Pos;
            this.eliteSanctum2Pos = this.bossCandidate3Pos;
         } else if (chosenBoss == 1) {
            this.thronePos = this.bossCandidate2Pos;
            this.eliteSanctum1Pos = this.bossCandidate1Pos;
            this.eliteSanctum2Pos = this.bossCandidate3Pos;
         } else {
            this.thronePos = this.bossCandidate3Pos;
            this.eliteSanctum1Pos = this.bossCandidate1Pos;
            this.eliteSanctum2Pos = this.bossCandidate2Pos;
         }

         this.roomC_W1 = this.roomL2_1;
         this.roomC_W2 = this.roomL1_2;
         this.roomC_W3 = this.roomL1_3;
         this.roomC_MidW = this.roomL2_4;
         this.roomC_W4 = this.roomL1_4;
         this.roomC_W5 = this.roomL1_5;
         this.roomC_N1 = this.roomC2;
         this.roomC_Top1 = this.roomC5;
         this.roomC_NE1 = this.roomR4_2;
         this.roomC_NE2 = this.roomR4_3;
         this.roomC_E1 = this.roomR4_1;
         this.roomC_E2 = this.roomR4_2;
         this.roomC_E3 = this.roomR5_3;
         this.roomC_E4 = this.roomR5_4;
         this.roomC_E5 = this.roomR5_2;
         this.room2Center = this.roomC_W1;
         this.room3Center = this.miniboss1Pos;
         this.room4Center = this.roomC_N1;
         this.room5Center = this.thronePos;
         this.deadEndCentralChamber = this.miniboss2Pos;
         this.deadEndWestChamber = this.eliteSanctum1Pos;
         this.deadEndEastChamber = this.eliteSanctum2Pos;
         this.secretTreasureChamber = this.roomC_E3;
         this.calculateBunkerBounds();
      }

      public void calculateBunkerBounds() {
         int minX = this.arenaCenter.getX();
         int maxX = this.arenaCenter.getX();
         int minY = this.arenaCenter.getY() - 8;
         int maxY = this.arenaCenter.getY() + 18;
         int minZ = this.arenaCenter.getZ() - 220;
         int maxZ = this.arenaCenter.getZ() + 30;

         for (BlockPos r : this.getAllRoomCenters()) {
            if (r != null) {
               minX = Math.min(minX, r.getX() - 32);
               maxX = Math.max(maxX, r.getX() + 32);
               minZ = Math.min(minZ, r.getZ() - 32);
               maxZ = Math.max(maxZ, r.getZ() + 32);
            }
         }

         this.bunkerMinX = minX;
         this.bunkerMaxX = maxX;
         this.bunkerMinY = minY;
         this.bunkerMaxY = maxY;
         this.bunkerMinZ = minZ;
         this.bunkerMaxZ = maxZ;
         this.bunkerBoundsCalculated = true;
      }

      public BlockPos[] getAllRoomCenters() {
         return new BlockPos[]{
            this.roomP,
            this.roomC1,
            this.roomC2,
            this.roomC3,
            this.roomMB_Center,
            this.roomC5,
            this.roomL2_1,
            this.roomL2_2,
            this.roomB_Mid,
            this.roomL2_4,
            this.roomB_Top,
            this.roomL2_6,
            this.roomMB_Left,
            this.roomL1_2,
            this.roomL1_3,
            this.roomL1_4,
            this.roomL1_5,
            this.roomR4_1,
            this.roomR4_2,
            this.roomR4_3,
            this.roomR4_4,
            this.roomR4_5,
            this.roomB_Right,
            this.roomR5_2,
            this.roomR5_3,
            this.roomR5_4,
            this.roomR5_5
         };
      }

      public DungeonSession(
         UUID playerId,
         String rank,
         String biome,
         ResourceKey<Level> returnDimension,
         Vec3 returnPos,
         float returnYaw,
         float returnPitch,
         ResourceKey<Level> dungeonDimension,
         BlockPos arenaCenter,
         int maxFloors
      ) {
         this(playerId, rank, biome, returnDimension, returnPos, returnYaw, returnPitch, dungeonDimension, arenaCenter, maxFloors, false);
      }

      public DungeonSession(
         UUID playerId,
         String rank,
         ResourceKey<Level> returnDimension,
         Vec3 returnPos,
         float returnYaw,
         float returnPitch,
         ResourceKey<Level> dungeonDimension,
         BlockPos arenaCenter,
         int maxFloors
      ) {
         this(playerId, rank, "crypt", returnDimension, returnPos, returnYaw, returnPitch, dungeonDimension, arenaCenter, maxFloors, false);
      }
   }

   public static final class WorldRift {
      public final BlockPos pos;
      public final String rank;
      public final String biome;
      public final ResourceKey<Level> dimension;
      public final long expireTick;
      public final boolean isRedGate;
      public final float yaw;
      public UUID activeHostSessionPlayerUUID = null;
      public boolean isPermanent = false;
      public String owningGuildId = null;
      public int rentalFeeMora = 500;
      public boolean feeInCoins = true;
      public final Map<UUID, Long> rentalAccessMap = new ConcurrentHashMap<>();
      public long cooldownUntil = 0L;
      public long spawnTimeMs = 0L;
      public boolean isNewlySpawned = false;
      public boolean isPlayerSummoned = false;

      public WorldRift(
         BlockPos pos, String rank, String biome, ResourceKey<Level> dimension, long expireTick, boolean isRedGate, float yaw, boolean isPermanent
      ) {
         this.pos = pos;
         this.rank = rank;
         this.biome = biome != null && !biome.isEmpty() ? biome : "crypt";
         this.dimension = dimension;
         this.expireTick = isPermanent ? Long.MAX_VALUE : expireTick;
         this.isRedGate = isRedGate;
         this.yaw = yaw;
         this.isPermanent = isPermanent;
         this.feeInCoins = true;
         if (isPermanent) {
            this.rentalFeeMora = Math.max(100, com.soybe.awakeningsystem.DungeonManager.getRankTier(rank) * 200);
         }
      }

      public WorldRift(BlockPos pos, String rank, String biome, ResourceKey<Level> dimension, long expireTick, boolean isRedGate, float yaw) {
         this(pos, rank, biome, dimension, expireTick, isRedGate, yaw, false);
      }

      public WorldRift(BlockPos pos, String rank, String biome, ResourceKey<Level> dimension, long expireTick, boolean isRedGate) {
         this(pos, rank, biome, dimension, expireTick, isRedGate, Math.abs(pos.getX() * 31 + pos.getZ()) % 2 == 0 ? 0.0F : 90.0F, false);
      }

      public WorldRift(BlockPos pos, String rank, String biome, ResourceKey<Level> dimension, long expireTick) {
         this(pos, rank, biome, dimension, expireTick, false);
      }

      public WorldRift(BlockPos pos, String rank, ResourceKey<Level> dimension, long expireTick) {
         this(pos, rank, "crypt", dimension, expireTick, false);
      }

      public boolean isRenterAuthorized(UUID playerUuid, long currentTick) {
         Long expiry = this.rentalAccessMap.get(playerUuid);
         return expiry != null && currentTick < expiry;
      }

      public void authorizeRenter(UUID playerUuid, long expireTick) {
         this.rentalAccessMap.put(playerUuid, expireTick);
      }
   }
}
