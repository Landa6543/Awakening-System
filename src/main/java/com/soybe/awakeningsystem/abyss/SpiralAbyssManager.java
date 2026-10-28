package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.entity.GenshinSlimeEntity;
import com.soybe.awakeningsystem.entity.HilichurlEntity;
import com.soybe.awakeningsystem.entity.IsekaiSlimeEntity;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import com.soybe.awakeningsystem.entity.LeyLineMonolithEntity;
import com.soybe.awakeningsystem.entity.AbyssChallengeKeyEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestor del Servidor para la Espiral del Abismo (Spiral Abyss) fiel a Genshin Impact:
 * - Dimensión Cósmica Astral: "awakening_system:spiral_abyss" con cielo de The End (sin sol, sin nubes).
 * - Arquitectura Canónica de Dominio Antiguo (Khaenri'ah):
 *   - Entrada monumental con portal ceremonial de piedra, balcón elevado y escalinatas de acceso.
 *   - Arena de combate en piedra antigua oscura (Pizarra pulida, ladrillos de piedra tallada, obsidiana llorosa, oro y vetas luminosas).
 *   - Sin paneles de cristal baratos; rodeada de balustradas de piedra labrada y braseros de fuego de almas azul etéreo.
 *   - 4 Columnatas Colosales en ruinas (16 bloques de altura) con arcos abovedados superiores y cadenas colgantes.
 *   - Altar Central con la legendaria Espada/Llave del Dominio flotante giratoria interactiva.
 */
public final class SpiralAbyssManager {
    private SpiralAbyssManager() {}

    public static final int TOTAL_FLOORS = 12;
    public static final int MAX_STARS_PER_FLOOR = 9;

    /** Dimensión dedicada de la Espiral del Abismo */
    public static final ResourceKey<Level> SPIRAL_ABYSS_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spiral_abyss")
    );

    public static class PlayerAbyssData {
        public int unlockedFloor = 1;
        public final int[] starsPerFloor = new int[TOTAL_FLOORS];
        public final boolean[] claimedRewards = new boolean[TOTAL_FLOORS];

        public int getTotalStars() {
            int sum = 0;
            for (int s : starsPerFloor) sum += s;
            return sum;
        }
    }

    public static class SpiralAbyssSavedData extends SavedData {
        public static final String DATA_NAME = "awakening_spiral_abyss_data";
        private final Map<UUID, PlayerAbyssData> players = new HashMap<>();

        public static SpiralAbyssSavedData get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(SpiralAbyssSavedData::new, SpiralAbyssSavedData::load, null),
                    DATA_NAME
            );
        }

        public PlayerAbyssData getOrCreate(UUID uuid) {
            return players.computeIfAbsent(uuid, k -> new PlayerAbyssData());
        }

        public static SpiralAbyssSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            SpiralAbyssSavedData data = new SpiralAbyssSavedData();
            ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag pTag = list.getCompound(i);
                UUID uuid = pTag.getUUID("UUID");
                PlayerAbyssData pData = new PlayerAbyssData();
                pData.unlockedFloor = Math.max(1, pTag.getInt("UnlockedFloor"));

                int[] stars = pTag.getIntArray("Stars");
                for (int f = 0; f < Math.min(TOTAL_FLOORS, stars.length); f++) {
                    pData.starsPerFloor[f] = stars[f];
                }

                byte[] claimed = pTag.getByteArray("Claimed");
                for (int f = 0; f < Math.min(TOTAL_FLOORS, claimed.length); f++) {
                    pData.claimedRewards[f] = claimed[f] == 1;
                }
                data.players.put(uuid, pData);
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (Map.Entry<UUID, PlayerAbyssData> e : players.entrySet()) {
                CompoundTag pTag = new CompoundTag();
                pTag.putUUID("UUID", e.getKey());
                pTag.putInt("UnlockedFloor", e.getValue().unlockedFloor);
                pTag.putIntArray("Stars", e.getValue().starsPerFloor);

                byte[] claimed = new byte[TOTAL_FLOORS];
                for (int f = 0; f < TOTAL_FLOORS; f++) {
                    claimed[f] = (byte) (e.getValue().claimedRewards[f] ? 1 : 0);
                }
                pTag.putByteArray("Claimed", claimed);
                list.add(pTag);
            }
            tag.put("Players", list);
            return tag;
        }
    }

    public enum SessionState {
        WAITING_FOR_TRIGGER,
        COMBAT,
        CHAMBER_CLEARED
    }

    public static class PendingAbyssSpawn {
        public final BlockPos pos;
        public final Mob mob;
        public int ticksRemaining;

        public PendingAbyssSpawn(BlockPos pos, Mob mob, int ticksRemaining) {
            this.pos = pos;
            this.mob = mob;
            this.ticksRemaining = ticksRemaining;
        }
    }

    public static class AbyssSession {
        public final UUID playerId;
        public final int floorNumber;
        public int currentChamber = 1;
        public SessionState state = SessionState.WAITING_FOR_TRIGGER;

        public int chamberSecondsRemaining = 300;
        public int[] chamberStarsEarned = new int[3];

        public final ResourceKey<Level> returnDimension;
        public final Vec3 returnPos;
        public final float returnYaw;
        public final float returnPitch;

        public final BlockPos arenaCenter;
        public final Set<UUID> activeEnemies = ConcurrentHashMap.newKeySet();
        public final List<PendingAbyssSpawn> pendingSpawns = new CopyOnWriteArrayList<>();
        public int totalEnemiesInWave = 0;

        public UUID challengeKeyEntityId;
        public ServerBossEvent bossEvent;
        public int ticksInChamber = 0;

        public boolean isMonolithChamber = false;
        public UUID monolithEntityId = null;
        public float monolithMaxHp = 3000.0F;
        public boolean monolithSpawned = false;

        public int currentHalf = 1;

        public BlockPos rewardChestPos = null;
        public UUID rewardChestMarkerId = null;
        public boolean rewardChestClaimed = false;

        public BlockPos descentPortalPos = null;
        public UUID descentPortalMarkerId = null;
        public final Set<BlockPos> descentPortalBlocks = new HashSet<>();

        public AbyssSession(UUID playerId, int floorNumber, ResourceKey<Level> returnDimension, Vec3 returnPos, float returnYaw, float returnPitch, BlockPos arenaCenter) {
            this.playerId = playerId;
            this.floorNumber = floorNumber;
            this.returnDimension = returnDimension;
            this.returnPos = returnPos;
            this.returnYaw = returnYaw;
            this.returnPitch = returnPitch;
            this.arenaCenter = arenaCenter;
        }
    }

    public static boolean isTwoHalvesFloor(int floor) {
        return floor >= 5;
    }

    private static final Map<UUID, AbyssSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> PENDING_MENU_OPENS = new ConcurrentHashMap<>();
    private static final AtomicInteger ARENA_COUNTER = new AtomicInteger(1);

    public static boolean isInAbyss(UUID playerId) {
        return ACTIVE_SESSIONS.containsKey(playerId);
    }

    public static AbyssSession getSession(UUID playerId) {
        return ACTIVE_SESSIONS.get(playerId);
    }

    public static ServerLevel getAbyssLevel(MinecraftServer server) {
        ServerLevel abyss = server.getLevel(SPIRAL_ABYSS_DIMENSION);
        if (abyss != null) return abyss;

        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat != null) return teyvat;

        return server.overworld();
    }

    public static void openAbyssScreen(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        SpiralAbyssSavedData savedData = SpiralAbyssSavedData.get(server);
        PlayerAbyssData pData = savedData.getOrCreate(player.getUUID());
        SystemData sysData = SystemEvents.data(player);

        PacketDistributor.sendToPlayer(player, new OpenSpiralAbyssPayload(
                pData.unlockedFloor,
                pData.starsPerFloor.clone(),
                pData.claimedRewards.clone(),
                pData.getTotalStars(),
                sysData.level(),
                sysData.primogems(),
                sysData.coins(),
                sysData.intertwinedFates()
        ));
    }

    public static void handleClaimReward(ServerPlayer player, int floorNumber) {
        if (floorNumber < 1 || floorNumber > TOTAL_FLOORS) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        SpiralAbyssSavedData savedData = SpiralAbyssSavedData.get(server);
        PlayerAbyssData pData = savedData.getOrCreate(player.getUUID());
        SystemData sysData = SystemEvents.data(player);

        int floorIdx = floorNumber - 1;
        if (pData.claimedRewards[floorIdx]) {
            player.displayClientMessage(Component.translatable("abyss.awakening_system.floor_already_claimed", floorNumber), true);
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
            return;
        }

        int stars = pData.starsPerFloor[floorIdx];
        if (stars < 3) {
            player.displayClientMessage(Component.translatable("abyss.awakening_system.floor_stars_needed", floorNumber), true);
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
            return;
        }

        int rewardedPrimos = 0;
        int rewardedCoins = 0;
        int rewardedFates = 0;

        if (stars >= 3) {
            rewardedPrimos += 100;
            rewardedCoins += 20000;
        }
        if (stars >= 6) {
            rewardedPrimos += 100;
            rewardedCoins += 30000;
            rewardedFates += 1;
        }
        if (stars >= 9) {
            rewardedPrimos += 100;
            rewardedCoins += 50000;
            rewardedFates += 2;
        }

        sysData.addPrimogems(rewardedPrimos);
        sysData.addCoins(rewardedCoins);
        sysData.addIntertwinedFates(rewardedFates);
        pData.claimedRewards[floorIdx] = true;
        savedData.setDirty();

        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);
        player.displayClientMessage(Component.translatable("abyss.awakening_system.floor_reward_claimed",
                floorNumber, rewardedPrimos, rewardedCoins, rewardedFates), false);

        openAbyssScreen(player);
    }

    public static void handleStartFloor(ServerPlayer player, int floorNumber) {
        if (floorNumber < 1 || floorNumber > TOTAL_FLOORS) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        SpiralAbyssSavedData savedData = SpiralAbyssSavedData.get(server);
        PlayerAbyssData pData = savedData.getOrCreate(player.getUUID());

        if (floorNumber > pData.unlockedFloor) {
            player.displayClientMessage(Component.translatable("abyss.awakening_system.floor_locked", floorNumber), true);
            player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 0.8F);
            return;
        }

        ServerLevel abyssLevel = getAbyssLevel(server);

        int arenaIdx = ARENA_COUNTER.getAndIncrement();
        BlockPos arenaCenter = new BlockPos(10000 + (arenaIdx % 40) * 180, 80, 10000 + (arenaIdx / 40) * 180);

        buildAbyssArena(abyssLevel, arenaCenter);

        AbyssSession session = new AbyssSession(
                player.getUUID(),
                floorNumber,
                player.level().dimension(),
                player.position(),
                player.getYRot(),
                player.getXRot(),
                arenaCenter
        );

        ACTIVE_SESSIONS.put(player.getUUID(), session);

        spawnChallengeSword(abyssLevel, session);

        BlockPos spawnPos = arenaCenter.offset(0, 1, -14);
        player.teleportTo(abyssLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);

        player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.9F, 1.0F);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.8F);

        abyssLevel.sendParticles(ParticleTypes.PORTAL, spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5, 50, 1.0, 1.5, 1.0, 0.1);

        player.displayClientMessage(Component.translatable("abyss.awakening_system.descended_chamber", floorNumber), false);
        player.displayClientMessage(Component.translatable("abyss.awakening_system.interact_key_prompt"), false);
    }

    private static void buildAbyssArena(ServerLevel level, BlockPos center) {
        int r = 18;
        int centerY = center.getY();
        int chasmY = centerY - 38;

        for (int dx = -55; dx <= 55; dx++) {
            for (int dz = -55; dz <= 55; dz++) {
                int wx = center.getX() + dx;
                int wz = center.getZ() + dz;
                for (int y = centerY - 45; y <= centerY + 50; y++) {
                    BlockPos p = new BlockPos(wx, y, wz);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int dx = -52; dx <= 52; dx++) {
            for (int dz = -52; dz <= 52; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 52.0) continue;
                int wx = center.getX() + dx;
                int wz = center.getZ() + dz;
                BlockPos cp = new BlockPos(wx, chasmY, wz);

                if ((dx % 8 == 0 || dz % 8 == 0) && (Math.abs(dx) > 8 || Math.abs(dz) > 8)) {
                    level.setBlock(cp, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                    if ((dx + dz) % 16 == 0) {
                        level.setBlock(cp.below(1), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        level.setBlock(cp, Blocks.CYAN_STAINED_GLASS.defaultBlockState(), 2);
                    }
                } else if ((dx * dx + dz * dz) % 43 == 0) {
                    level.setBlock(cp, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(cp.above(1), Blocks.SOUL_FIRE.defaultBlockState(), 2);
                } else if (dist <= 28.0) {
                    level.setBlock(cp, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(cp, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
            }
        }

        int pillarRadius = 36;
        for (int pIdx = 0; pIdx < 8; pIdx++) {
            double angle = (2.0 * Math.PI / 8.0) * pIdx;
            int px = center.getX() + (int) Math.round(Math.cos(angle) * pillarRadius);
            int pz = center.getZ() + (int) Math.round(Math.sin(angle) * pillarRadius);

            for (int colY = chasmY; colY <= centerY + 42; colY++) {
                for (int ox = -1; ox <= 2; ox++) {
                    for (int oz = -1; oz <= 2; oz++) {
                        BlockPos tp = new BlockPos(px + ox, colY, pz + oz);
                        boolean isEdge = (ox == -1 || ox == 2 || oz == -1 || oz == 2);
                        if (colY % 10 == 0) {
                            level.setBlock(tp, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                        } else if (colY % 5 == 0 && !isEdge) {
                            level.setBlock(tp, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        } else if (isEdge) {
                            level.setBlock(tp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(tp, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                        }
                    }
                }
            }

            for (int ox = -2; ox <= 3; ox++) {
                for (int oz = -2; oz <= 3; oz++) {
                    BlockPos capPos = new BlockPos(px + ox, centerY + 42, pz + oz);
                    level.setBlock(capPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
            level.setBlock(new BlockPos(px, centerY + 43, pz), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);

            for (int step = 1; step <= 14; step++) {
                double archFraction = (double) step / 14.0;
                int ax = px + (int) Math.round((center.getX() - px) * archFraction * 0.42);
                int az = pz + (int) Math.round((center.getZ() - pz) * archFraction * 0.42);
                int ay = centerY + 40 - (int) Math.round(Math.sin(archFraction * Math.PI) * 4.0);
                BlockPos archPos = new BlockPos(ax, ay, az);
                level.setBlock(archPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);

                if (step == 14) {
                    level.setBlock(archPos.below(1), Blocks.CHAIN.defaultBlockState(), 2);
                    level.setBlock(archPos.below(2), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                }
            }
        }

        int stairRadius = 28;
        int totalSteps = 160;
        for (int step = 0; step < totalSteps; step++) {
            double angle = (4.0 * Math.PI / totalSteps) * step;
            int sy = centerY + 30 - (int) (step * 0.42);

            int sx = center.getX() + (int) Math.round(Math.cos(angle) * stairRadius);
            int sz = center.getZ() + (int) Math.round(Math.sin(angle) * stairRadius);

            for (int w = -1; w <= 2; w++) {
                int wx = center.getX() + (int) Math.round(Math.cos(angle) * (stairRadius + w));
                int wz = center.getZ() + (int) Math.round(Math.sin(angle) * (stairRadius + w));
                BlockPos sp = new BlockPos(wx, sy, wz);

                if (w == 2) {
                    level.setBlock(sp.above(1), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
                }
                if (step % 12 == 0 && w == 0) {
                    level.setBlock(sp, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    level.setBlock(sp.above(1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                } else if (step % 6 == 0) {
                    level.setBlock(sp, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                } else {
                    level.setBlock(sp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        int ringZ = center.getZ() + 35;
        int ringCenterY = centerY + 18;
        for (int rx = -16; rx <= 16; rx++) {
            for (int ry = -16; ry <= 16; ry++) {
                double ringDist = Math.sqrt(rx * rx + ry * ry);
                if (ringDist >= 13.0 && ringDist <= 16.0) {
                    BlockPos rp = new BlockPos(center.getX() + rx, ringCenterY + ry, ringZ);
                    if (ringDist >= 14.8) {
                        level.setBlock(rp, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                    } else if (ringDist >= 13.8) {
                        level.setBlock(rp, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    } else {
                        level.setBlock(rp, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                int wx = center.getX() + dx;
                int wz = center.getZ() + dz;

                if (dist <= 1.8) {
                    level.setBlock(new BlockPos(wx, centerY - 14, wz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, centerY - 13, wz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }
                if (dist <= 3.5) {
                    level.setBlock(new BlockPos(wx, centerY - 12, wz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, centerY - 11, wz), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                }
                if (dist <= 6.0) {
                    level.setBlock(new BlockPos(wx, centerY - 10, wz), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, centerY - 9, wz), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
                if (dist <= 9.0) {
                    level.setBlock(new BlockPos(wx, centerY - 8, wz), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, centerY - 7, wz), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
                if (dist <= 12.5) {
                    level.setBlock(new BlockPos(wx, centerY - 6, wz), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, centerY - 5, wz), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
                if (dist <= 15.5) {
                    level.setBlock(new BlockPos(wx, centerY - 4, wz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, centerY - 3, wz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                if (dist <= 17.5) {
                    level.setBlock(new BlockPos(wx, centerY - 2, wz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, centerY - 1, wz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > r + 0.5) continue;

                int wx = center.getX() + dx;
                int wz = center.getZ() + dz;
                BlockPos floorPos = new BlockPos(wx, centerY, wz);

                if (dist < r - 0.5) {
                    for (int ay = 1; ay <= 6; ay++) {
                        level.setBlock(floorPos.above(ay), Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                if (dist >= r - 0.8) {
                    level.setBlock(floorPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(floorPos.above(1), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
                    for (int by = 2; by <= 16; by++) {
                        level.setBlock(floorPos.above(by), Blocks.BARRIER.defaultBlockState(), 2);
                    }
                }
                else if (dist <= 1.8) {
                    level.setBlock(floorPos, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                }
                else if (dist <= 4.2) {
                    boolean isRay = (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                    if (isRay) {
                        level.setBlock(floorPos, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                    } else if (dist <= 3.2) {
                        level.setBlock(floorPos.below(1), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        level.setBlock(floorPos, Blocks.CYAN_STAINED_GLASS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(floorPos, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    }
                }
                else if (dist <= 7.5) {
                    if (dist >= 6.8) {
                        level.setBlock(floorPos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                    } else {
                        level.setBlock(floorPos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                    }
                }
                else if (dist <= 12.0) {
                    boolean isNode = (dx == 0 && Math.abs(dz) == 9) || (dz == 0 && Math.abs(dx) == 9) || (Math.abs(dx) == 6 && Math.abs(dz) == 6);
                    if (isNode) {
                        level.setBlock(floorPos, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
                    } else if (dist >= 11.2) {
                        level.setBlock(floorPos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                    } else {
                        level.setBlock(floorPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    }
                }
                else {
                    boolean isOuterRing = (dist >= 14.8 && dist <= 16.2);
                    boolean isConduitRay = (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                    if (isOuterRing || isConduitRay) {
                        level.setBlock(floorPos.below(1), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        level.setBlock(floorPos, Blocks.CYAN_STAINED_GLASS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(floorPos, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            double angle = (2.0 * Math.PI / 8.0) * i;
            int bx = center.getX() + (int) Math.round(Math.cos(angle) * 17.5);
            int bz = center.getZ() + (int) Math.round(Math.sin(angle) * 17.5);
            BlockPos bp = new BlockPos(bx, centerY + 1, bz);
            level.setBlock(bp, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(bp.above(1), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);
        }

        for (int sx = -2; sx <= 2; sx++) {
            for (int sz = -2; sz <= 2; sz++) {
                BlockPos ap = center.offset(sx, 0, sz);
                double d = Math.sqrt(sx * sx + sz * sz);
                if (d <= 1.5) {
                    level.setBlock(ap, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }
                for (int sy = 1; sy <= 6; sy++) {
                    level.setBlock(center.offset(sx, sy, sz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist >= r - 0.6 && dist <= r + 1.2) {
                    int wx = center.getX() + dx;
                    int wz = center.getZ() + dz;
                    for (int by = 1; by <= 16; by++) {
                        BlockPos bp = new BlockPos(wx, centerY + by, wz);
                        level.setBlock(bp, Blocks.BARRIER.defaultBlockState(), 2);
                    }
                }
            }
        }

        int domeR = 47;
        for (int dx = -domeR - 1; dx <= domeR + 1; dx++) {
            for (int dz = -domeR - 1; dz <= domeR + 1; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > domeR + 1.2) continue;

                int wx = center.getX() + dx;
                int wz = center.getZ() + dz;

                if (dist >= 32.0 && dist <= domeR + 0.5) {
                    level.setBlock(new BlockPos(wx, chasmY - 2, wz), SystemBlocks.ABYSSAL_VOID_STONE.get().defaultBlockState(), 2);
                    level.setBlock(new BlockPos(wx, chasmY - 1, wz), SystemBlocks.ABYSSAL_VOID_STONE.get().defaultBlockState(), 2);
                }

                if (dist >= domeR - 1.8 && dist <= domeR + 0.5) {
                    for (int wy = chasmY; wy <= centerY + 22; wy++) {
                        if ((wx * 11 + wz * 17 + wy * 13) % 43 == 0) {
                            level.setBlock(new BlockPos(wx, wy, wz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                        } else if ((wx * 7 + wz * 19 + wy * 23) % 59 == 0) {
                            level.setBlock(new BlockPos(wx, wy, wz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        } else {
                            level.setBlock(new BlockPos(wx, wy, wz), SystemBlocks.ABYSSAL_VOID_STONE.get().defaultBlockState(), 2);
                        }
                    }
                }

                for (int wy = centerY + 23; wy <= centerY + 46; wy++) {
                    double hFrac = (double) (wy - (centerY + 22)) / 25.0;
                    double targetR = domeR * Math.sqrt(Math.max(0.0, 1.0 - hFrac * hFrac));
                    if (dist >= targetR - 1.6 && dist <= targetR + 0.6) {
                        if ((wx * 13 + wz * 7 + wy * 19) % 37 == 0) {
                            level.setBlock(new BlockPos(wx, wy, wz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                        } else {
                            level.setBlock(new BlockPos(wx, wy, wz), SystemBlocks.ABYSSAL_VOID_STONE.get().defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Spawnea la auténtica Llave del Desafío del Abismo en el altar central con marcador estático que NUNCA sube al cielo.
     */
    public static void spawnChallengeSword(ServerLevel level, AbyssSession session) {
        if (session.challengeKeyEntityId != null) {
            var old = level.getEntity(session.challengeKeyEntityId);
            if (old != null) old.discard();
            session.challengeKeyEntityId = null;
        }
        BlockPos center = session.arenaCenter;

        level.setBlock(center.above(1), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(2), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(3), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(4), Blocks.AIR.defaultBlockState(), 2);

        AbyssChallengeKeyEntity keyEntity = ModEntities.ABYSS_CHALLENGE_KEY.get().create(level);
        if (keyEntity != null) {
            keyEntity.moveTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 0.0F, 0.0F);
            keyEntity.setCustomName(Component.literal("§c§l✦ START CHALLENGE (CHAMBER " + session.currentChamber + ") ✦"));
            keyEntity.setCustomNameVisible(true);
            level.addFreshEntity(keyEntity);
            session.challengeKeyEntityId = keyEntity.getUUID();
        }
    }

    /**
     * Comprueba si el jugador interactuó (clic derecho) con la Espada/Llave Central para iniciar el combate.
     */
    public static boolean handleKeyInteraction(ServerPlayer player, BlockPos clickedPos) {
        AbyssSession session = ACTIVE_SESSIONS.get(player.getUUID());
        if (session == null || session.state != SessionState.WAITING_FOR_TRIGGER) return false;

        if (clickedPos.distSqr(session.arenaCenter) <= 25.0) {
            ServerLevel level = getAbyssLevel(player.getServer());
            startChamberCombat(player, session, level);
            return true;
        }
        return false;
    }

    /**
     * Dispara la activación de la Llave de Desafío invocada desde la entidad AbyssChallengeKeyEntity.
     */
    public static boolean triggerChallengeKey(ServerPlayer player, AbyssChallengeKeyEntity keyEntity) {
        AbyssSession session = ACTIVE_SESSIONS.get(player.getUUID());
        if (session == null || session.state != SessionState.WAITING_FOR_TRIGGER) return false;

        ServerLevel level = getAbyssLevel(player.getServer());
        startChamberCombat(player, session, level);
        return true;
    }

    private static void cleanUpChamberDecorations(ServerLevel level, AbyssSession session) {
        if (session.rewardChestMarkerId != null) {
            var m = level.getEntity(session.rewardChestMarkerId);
            if (m != null) m.discard();
            session.rewardChestMarkerId = null;
        }
        if (session.rewardChestPos != null) {
            level.setBlock(session.rewardChestPos, Blocks.AIR.defaultBlockState(), 2);
            session.rewardChestPos = null;
        }
        if (session.descentPortalMarkerId != null) {
            var pMarker = level.getEntity(session.descentPortalMarkerId);
            if (pMarker != null) pMarker.discard();
            session.descentPortalMarkerId = null;
        }
        for (BlockPos pb : session.descentPortalBlocks) {
            level.setBlock(pb, Blocks.AIR.defaultBlockState(), 2);
        }
        session.descentPortalBlocks.clear();
        session.descentPortalPos = null;

        if (session.monolithEntityId != null) {
            var mEnt = level.getEntity(session.monolithEntityId);
            if (mEnt != null) mEnt.discard();
            session.monolithEntityId = null;
        }
        BlockPos center = session.arenaCenter;
        level.setBlock(center.above(1), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(2), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(3), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(4), Blocks.AIR.defaultBlockState(), 2);
        session.isMonolithChamber = false;
    }

    private static void startChamberCombat(ServerPlayer player, AbyssSession session, ServerLevel level) {
        session.state = SessionState.COMBAT;
        session.chamberSecondsRemaining = 300;
        session.ticksInChamber = 0;
        session.activeEnemies.clear();
        session.pendingSpawns.clear();

        cleanUpChamberDecorations(level, session);

        if (session.challengeKeyEntityId != null) {
            var keyEnt = level.getEntity(session.challengeKeyEntityId);
            if (keyEnt != null) keyEnt.discard();
            session.challengeKeyEntityId = null;
        }
        BlockPos center = session.arenaCenter;
        level.setBlock(center.above(1), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(2), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(3), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(4), Blocks.AIR.defaultBlockState(), 2);

        session.currentHalf = 1;
        boolean twoHalves = isTwoHalvesFloor(session.floorNumber);
        session.isMonolithChamber = (session.currentChamber == 1);
        session.monolithSpawned = false;
        if (session.isMonolithChamber) {
            LeyLineMonolithEntity monolith = ModEntities.LEY_LINE_MONOLITH.get().create(level);
            if (monolith != null) {
                monolith.moveTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 0.0F, 0.0F);
                if (monolith.getAttribute(Attributes.MAX_HEALTH) != null) {
                    monolith.getAttribute(Attributes.MAX_HEALTH).setBaseValue(session.monolithMaxHp);
                    monolith.setHealth(session.monolithMaxHp);
                }
                level.addFreshEntity(monolith);
                session.monolithEntityId = monolith.getUUID();
                session.monolithSpawned = true;
            }
        }

        applyLeyLineDisorder(player, session.floorNumber);

        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }
        String halfTag = twoHalves ? " §d[1st Half]" : "";
        String initialBarText = session.isMonolithChamber
                ? "§b✦ SPIRAL ABYSS §7| §aMonolith: 100%" + halfTag + " §7(05:00)"
                : "§b✦ SPIRAL ABYSS §7| §eFloor " + session.floorNumber + " - Chamber " + session.currentChamber + halfTag + " §7(05:00)";
        session.bossEvent = new ServerBossEvent(
                Component.literal(initialBarText),
                session.isMonolithChamber ? BossEvent.BossBarColor.GREEN : BossEvent.BossBarColor.BLUE,
                BossEvent.BossBarOverlay.PROGRESS
        );
        session.bossEvent.addPlayer(player);

        player.playNotifySound(SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.4F);

        level.sendParticles(ParticleTypes.EXPLOSION, center.getX() + 0.5, center.getY() + 1.8, center.getZ() + 0.5, 3, 0.5, 0.5, 0.5, 0.0);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.getX() + 0.5, center.getY() + 1.8, center.getZ() + 0.5, 50, 1.5, 1.0, 1.5, 0.1);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.getX() + 0.5, center.getY() + 1.8, center.getZ() + 0.5, 40, 1.5, 1.0, 1.5, 0.1);

        spawnChamberWave(level, session);

        Component halfComp = twoHalves ? Component.translatable("abyss.awakening_system.first_half_tag") : Component.empty();
        Component challengeMsg = session.isMonolithChamber
                ? Component.translatable("abyss.awakening_system.challenge_monolith_started", halfComp)
                : Component.translatable("abyss.awakening_system.challenge_floor_started", session.floorNumber, session.currentChamber, halfComp);
        player.displayClientMessage(challengeMsg, false);
    }

    private static void spawnChamberWave(ServerLevel level, AbyssSession session) {
        int floor = session.floorNumber;
        int chamber = session.currentChamber;
        int half = session.currentHalf;
        BlockPos center = session.arenaCenter;

        int mobCount = 6 + (floor / 2) + chamber * 2 + (half == 2 ? 1 : 0);
        session.totalEnemiesInWave = mobCount;
        session.pendingSpawns.clear();

        Random rnd = new Random();
        for (int i = 0; i < mobCount; i++) {
            double angle = (2.0 * Math.PI / mobCount) * i + (rnd.nextDouble() - 0.5) * 0.3;
            double radius = session.isMonolithChamber ? (14.0 + rnd.nextDouble() * 2.5) : (10.0 + rnd.nextDouble() * 4.0);
            int mx = center.getX() + (int) (Math.cos(angle) * radius);
            int mz = center.getZ() + (int) (Math.sin(angle) * radius);
            BlockPos spawnPos = new BlockPos(mx, center.getY() + 1, mz);

            Mob mob = createAbyssMob(level, floor, chamber, half, i);
            if (mob != null) {
                mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, (float) (angle * 180 / Math.PI), 0.0F);
                mob.setPersistenceRequired();

                double hpScale = 1.0 + (floor - 1) * 0.35 + (chamber - 1) * 0.15 + (half == 2 ? 0.20 : 0.0);
                if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
                    double newHp = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * hpScale;
                    mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newHp);
                    mob.setHealth((float) newHp);
                }
                if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                    double newDmg = mob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * (1.0 + (floor - 1) * 0.20);
                    mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(newDmg);
                }

                int delayTicks = 20 + (i / 2) * 15;
                session.pendingSpawns.add(new PendingAbyssSpawn(spawnPos, mob, delayTicks));
            }
        }
    }

    private static GenshinSlimeEntity createGenshinSlime(ServerLevel level, GenshinSlimeEntity.Element element, boolean isBig) {
        GenshinSlimeEntity slime = new GenshinSlimeEntity(ModEntities.GENSHIN_SLIME.get(), level);
        slime.setElement(element);
        slime.setBig(isBig);
        String name = (isBig ? "§e§lSlime " : "§fSlime ") + element.getDisplayName() + (isBig ? " Gigante" : "");
        slime.setCustomName(Component.literal(name));
        slime.setCustomNameVisible(true);
        return slime;
    }

    private static HilichurlEntity createHilichurl(ServerLevel level, HilichurlEntity.HilichurlRole role) {
        HilichurlEntity h = new HilichurlEntity(ModEntities.HILICHURL.get(), level);
        h.setRole(role);
        return h;
    }

    private static MitachurlEntity createMitachurl(ServerLevel level, boolean fireAxe) {
        MitachurlEntity m = new MitachurlEntity(ModEntities.MITACHURL.get(), level);
        m.setMitachurlType(fireAxe ? MitachurlEntity.MitachurlType.BLAZING_AXE : MitachurlEntity.MitachurlType.SHIELDWALL);
        return m;
    }

    public static GenshinSlimeEntity spawnRandomGenshinSlime(ServerLevel level, BlockPos pos) {
        RandomSource rnd = level.random;
        GenshinSlimeEntity.Element[] elements = GenshinSlimeEntity.Element.values();
        GenshinSlimeEntity.Element elem = elements[rnd.nextInt(elements.length)];
        boolean isBig = rnd.nextFloat() < 0.25F;
        GenshinSlimeEntity mob = createGenshinSlime(level, elem, isBig);
        if (mob != null) {
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rnd.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
        }
        return mob;
    }

    public static HilichurlEntity spawnRandomHilichurl(ServerLevel level, BlockPos pos) {
        RandomSource rnd = level.random;
        HilichurlEntity.HilichurlRole role = switch (rnd.nextInt(4)) {
            case 0 -> HilichurlEntity.HilichurlRole.BERSERKER;
            case 1 -> HilichurlEntity.HilichurlRole.GUARD;
            case 2 -> HilichurlEntity.HilichurlRole.SHOOTER;
            default -> HilichurlEntity.HilichurlRole.FIGHTER;
        };
        HilichurlEntity mob = createHilichurl(level, role);
        if (mob != null) {
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rnd.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
        }
        return mob;
    }

    public static MitachurlEntity spawnRandomMitachurl(ServerLevel level, BlockPos pos) {
        RandomSource rnd = level.random;
        boolean fireAxe = rnd.nextBoolean();
        MitachurlEntity mob = createMitachurl(level, fireAxe);
        if (mob != null) {
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rnd.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
        }
        return mob;
    }

    private static Mob createAbyssMage(ServerLevel level, String element) {
        if ("pyro".equalsIgnoreCase(element)) {
            Blaze b = EntityType.BLAZE.create(level);
            if (b != null) {
                b.setCustomName(Component.translatable("abyss.awakening_system.mob.pyro_abyss_mage"));
                b.setCustomNameVisible(true);
            }
            return b;
        } else if ("cryo".equalsIgnoreCase(element)) {
            Stray s = EntityType.STRAY.create(level);
            if (s != null) {
                s.setCustomName(Component.translatable("abyss.awakening_system.mob.cryo_abyss_mage"));
                s.setCustomNameVisible(true);
                ItemStack helm = new ItemStack(Items.LEATHER_HELMET);
                helm.set(DataComponents.DYED_COLOR, new DyedItemColor(0x81D4FA, false));
                s.setItemSlot(EquipmentSlot.HEAD, helm);
                ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
                chest.set(DataComponents.DYED_COLOR, new DyedItemColor(0x29B6F6, false));
                s.setItemSlot(EquipmentSlot.CHEST, chest);
                for (EquipmentSlot slot : EquipmentSlot.values()) s.setDropChance(slot, 0.0F);
            }
            return s;
        } else {
            Witch w = EntityType.WITCH.create(level);
            if (w != null) {
                w.setCustomName(Component.translatable("abyss.awakening_system.mob.electro_abyss_mage"));
                w.setCustomNameVisible(true);
            }
            return w;
        }
    }

    private static Mob createAbyssMob(ServerLevel level, int floor, int chamber, int half, int index) {
        if (floor <= 2) {
            if (index == 0 && chamber == 3 && floor == 2) {
                return createMitachurl(level, false);
            }
            if (index % 2 == 0) {
                GenshinSlimeEntity.Element[] elements = {
                        GenshinSlimeEntity.Element.PYRO,
                        GenshinSlimeEntity.Element.HYDRO,
                        GenshinSlimeEntity.Element.ELECTRO,
                        GenshinSlimeEntity.Element.ANEMO,
                        GenshinSlimeEntity.Element.CRYO,
                        GenshinSlimeEntity.Element.GEO,
                        GenshinSlimeEntity.Element.DENDRO
                };
                GenshinSlimeEntity.Element elem = elements[(index / 2 + chamber) % elements.length];
                boolean isBig = (index == 0 || chamber == 3);
                return createGenshinSlime(level, elem, isBig);
            } else {
                if (index % 4 == 1) {
                    return createHilichurl(level, HilichurlEntity.HilichurlRole.BERSERKER);
                } else if (index % 4 == 3) {
                    return createHilichurl(level, HilichurlEntity.HilichurlRole.GUARD);
                } else {
                    return createHilichurl(level, HilichurlEntity.HilichurlRole.SHOOTER);
                }
            }
        } else if (floor <= 4) {
            if (index == 0) {
                return createMitachurl(level, (chamber % 2 == 1));
            } else if (index % 3 == 0) {
                return createHilichurl(level, HilichurlEntity.HilichurlRole.SHOOTER);
            } else if (index % 3 == 1) {
                return createHilichurl(level, HilichurlEntity.HilichurlRole.BERSERKER);
            } else {
                GenshinSlimeEntity.Element elem = (chamber % 2 == 0) ? GenshinSlimeEntity.Element.PYRO : GenshinSlimeEntity.Element.CRYO;
                return createGenshinSlime(level, elem, true);
            }
        } else if (floor <= 6) {
            if (half == 1) {
                if (index % 3 == 0) {
                    GenshinSlimeEntity.Element elem = (chamber == 1) ? GenshinSlimeEntity.Element.PYRO : (chamber == 2 ? GenshinSlimeEntity.Element.ELECTRO : GenshinSlimeEntity.Element.ANEMO);
                    return createGenshinSlime(level, elem, true);
                } else if (index % 3 == 1) {
                    return createHilichurl(level, HilichurlEntity.HilichurlRole.BERSERKER);
                } else {
                    return createHilichurl(level, HilichurlEntity.HilichurlRole.GUARD);
                }
            } else {
                if (index == 0 && chamber == 3) {
                    IronGolem golem = EntityType.IRON_GOLEM.create(level);
                    if (golem != null) {
                        golem.setCustomName(Component.translatable("abyss.awakening_system.mob.ancient_ruin_guard"));
                        golem.setCustomNameVisible(true);
                    }
                    return golem;
                }
                if (index % 3 == 0) {
                    return createMitachurl(level, true);
                } else if (index % 3 == 1) {
                    return createAbyssMage(level, (index % 2 == 0) ? "pyro" : "cryo");
                } else {
                    return createMitachurl(level, false);
                }
            }
        } else {
            if (half == 1) {
                if (index % 3 == 0) {
                    return createMitachurl(level, true);
                } else if (index % 3 == 1) {
                    return createAbyssMage(level, (index % 2 == 0) ? "cryo" : "electro");
                } else {
                    return createHilichurl(level, HilichurlEntity.HilichurlRole.BERSERKER);
                }
            } else {
                if (index == 0) {
                    WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level);
                    if (ws != null) {
                        ws.setCustomName(Component.translatable("abyss.awakening_system.mob.abyss_herald_torrent"));
                        ws.setCustomNameVisible(true);
                        ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                        ws.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, 1, false, false));
                    }
                    return ws;
                } else if (index % 2 == 0) {
                    return createMitachurl(level, true);
                } else {
                    return createAbyssMage(level, (index % 2 == 1) ? "cryo" : "pyro");
                }
            }
        }
    }

    private static void applyLeyLineDisorder(ServerPlayer player, int floor) {
        switch (floor) {
            case 1 -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300 * 20, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300 * 20, 1, false, false));
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300 * 20, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300 * 20, 0, false, false));
            }
            case 3 -> {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300 * 20, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300 * 20, 1, false, false));
            }
            case 4 -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300 * 20, 1, false, false));
            }
            case 5 -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300 * 20, 1, false, false));
            }
            case 6 -> {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300 * 20, 3, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300 * 20, 2, false, false));
            }
            case 7 -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300 * 20, 2, false, false));
            }
            default -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300 * 20, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300 * 20, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300 * 20, 1, false, false));
            }
        }
    }

    public static void tick(MinecraftServer server) {
        if (!PENDING_MENU_OPENS.isEmpty()) {
            PENDING_MENU_OPENS.entrySet().removeIf(entry -> {
                int rem = entry.getValue() - 1;
                if (rem <= 0) {
                    ServerPlayer p = server.getPlayerList().getPlayer(entry.getKey());
                    if (p != null) {
                        openAbyssScreen(p);
                    }
                    return true;
                }
                entry.setValue(rem);
                return false;
            });
        }

        if (ACTIVE_SESSIONS.isEmpty()) return;

        ServerLevel abyssLevel = getAbyssLevel(server);

        for (Map.Entry<UUID, AbyssSession> entry : ACTIVE_SESSIONS.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            AbyssSession session = entry.getValue();

            if (player == null || player.isDeadOrDying() || !player.level().dimension().equals(abyssLevel.dimension())) {
                cleanUpSession(server, session);
                ACTIVE_SESSIONS.remove(entry.getKey());
                continue;
            }

            double pDistToCenter = Math.sqrt(
                    Math.pow(player.getX() - (session.arenaCenter.getX() + 0.5), 2) +
                    Math.pow(player.getZ() - (session.arenaCenter.getZ() + 0.5), 2)
            );
            if (player.getY() < session.arenaCenter.getY() - 3.0 || pDistToCenter > 20.0) {
                player.teleportTo(session.arenaCenter.getX() + 0.5, session.arenaCenter.getY() + 1.0, session.arenaCenter.getZ() + 0.5);
                player.setDeltaMovement(0, 0, 0);
                player.fallDistance = 0.0F;
                player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);
                player.displayClientMessage(Component.translatable("abyss.awakening_system.barrier_contained"), true);
            }

            if (server.getTickCount() % 4 == 0) {
                Vec3 pPos = player.position();
                RandomSource rnd = player.getRandom();
                for (int p = 0; p < 8; p++) {
                    double ox = (rnd.nextDouble() - 0.5) * 44.0;
                    double oy = (rnd.nextDouble() - 0.5) * 16.0;
                    double oz = (rnd.nextDouble() - 0.5) * 44.0;
                    abyssLevel.sendParticles(ParticleTypes.WARPED_SPORE, pPos.x + ox, pPos.y + oy, pPos.z + oz, 1, 0, 0, 0, 0.0);
                    if (p % 3 == 0) {
                        abyssLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, pPos.x + ox, pPos.y + oy, pPos.z + oz, 1, 0, 0.02, 0, 0.01);
                    }
                }
            }

            if (session.state == SessionState.WAITING_FOR_TRIGGER) {
                BlockPos keyPos = session.arenaCenter.above(2);
                abyssLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, keyPos.getX() + 0.5, keyPos.getY() + 0.8, keyPos.getZ() + 0.5, 4, 0.2, 0.3, 0.2, 0.02);
                abyssLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, keyPos.getX() + 0.5, keyPos.getY() + 0.5, keyPos.getZ() + 0.5, 3, 0.2, 0.3, 0.2, 0.03);

                double distToKey = player.position().distanceTo(new Vec3(keyPos.getX() + 0.5, keyPos.getY(), keyPos.getZ() + 0.5));
                if (distToKey <= 2.2) {
                    startChamberCombat(player, session, abyssLevel);
                }
                continue;
            }

            if (session.state == SessionState.COMBAT) {
                session.ticksInChamber++;

                if (!session.pendingSpawns.isEmpty()) {
                    List<PendingAbyssSpawn> toRemove = new ArrayList<>();
                    for (PendingAbyssSpawn spawn : session.pendingSpawns) {
                        spawn.ticksRemaining--;

                        BlockPos p = spawn.pos;
                        if (session.ticksInChamber % 2 == 0) {
                            double rot = (server.getTickCount() * 0.15D);
                            for (int a = 0; a < 6; a++) {
                                double angle = (2.0D * Math.PI / 6.0D) * a + rot;
                                double rx = p.getX() + 0.5D + Math.cos(angle) * 1.3D;
                                double rz = p.getZ() + 0.5D + Math.sin(angle) * 1.3D;
                                abyssLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rx, p.getY() + 0.05D, rz, 1, 0, 0.01, 0, 0.005);
                            }
                            abyssLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX() + 0.5D, p.getY() + 0.3D, p.getZ() + 0.5D, 2, 0.2, 0.2, 0.2, 0.01);
                        }

                        if (spawn.ticksRemaining == 10) {
                            player.playNotifySound(SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 0.7F, 1.2F);
                        }

                        if (spawn.ticksRemaining <= 0) {
                            toRemove.add(spawn);
                            Mob mob = spawn.mob;
                            if (mob != null) {
                                abyssLevel.addFreshEntity(mob);
                                session.activeEnemies.add(mob.getUUID());

                                if (session.isMonolithChamber && session.monolithEntityId != null) {
                                    var monoEnt = abyssLevel.getEntity(session.monolithEntityId);
                                    if (monoEnt instanceof LivingEntity livingMono && livingMono.isAlive()) {
                                        mob.setTarget(livingMono);
                                    }
                                }

                                abyssLevel.sendParticles(ParticleTypes.FLASH, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 1, 0, 0, 0, 0);
                                abyssLevel.sendParticles(ParticleTypes.EXPLOSION, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.0);
                                abyssLevel.sendParticles(ParticleTypes.POOF, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.05);
                                player.playNotifySound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 0.5F, 1.6F);
                            }
                        }
                    }
                    session.pendingSpawns.removeAll(toRemove);
                }

                float monolithHpPct = 1.0F;
                if (session.isMonolithChamber) {
                    LeyLineMonolithEntity monolith = null;
                    if (session.monolithEntityId != null) {
                        var ent = abyssLevel.getEntity(session.monolithEntityId);
                        if (ent instanceof LeyLineMonolithEntity llm) {
                            if (llm.isDeadOrDying() || llm.getHealth() <= 0.0F) {
                                player.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.8F, 0.8F);
                                player.displayClientMessage(Component.translatable("abyss.awakening_system.monolith_destroyed"), false);
                                handleChamberDefeat(player, session, server);
                                ACTIVE_SESSIONS.remove(entry.getKey());
                                continue;
                            }
                            if (llm.isAlive()) {
                                monolith = llm;
                            }
                        }
                    }

                    if (monolith != null) {
                        monolithHpPct = Math.max(0.0F, Math.min(1.0F, monolith.getHealth() / session.monolithMaxHp));
                        String col = monolithHpPct >= 0.6F ? "§a" : (monolithHpPct >= 0.3F ? "§e" : "§c");
                        monolith.setCustomName(Component.literal("§b§l✦ Monolito: " + col + (int)(monolithHpPct * 100) + "% §b✦"));

                        if (session.ticksInChamber % 20 == 0) {
                            for (UUID mobId : session.activeEnemies) {
                                var enemyEnt = abyssLevel.getEntity(mobId);
                                if (enemyEnt instanceof Mob enemyMob) {
                                    if (enemyMob.getTarget() instanceof Player) {
                                        continue;
                                    }
                                    if (enemyMob.getTarget() == null) {
                                        enemyMob.setTarget(monolith);
                                    }
                                }
                            }
                        }
                    }
                }

                if (session.ticksInChamber % 20 == 0) {
                    session.chamberSecondsRemaining--;

                    session.activeEnemies.removeIf(uuid -> {
                        var e = abyssLevel.getEntity(uuid);
                        return e == null || !e.isAlive();
                    });

                    int remaining = session.activeEnemies.size() + session.pendingSpawns.size();
                    int mins = Math.max(0, session.chamberSecondsRemaining / 60);
                    int secs = Math.max(0, session.chamberSecondsRemaining % 60);
                    String timeStr = String.format("%02d:%02d", mins, secs);

                    boolean twoHalves = isTwoHalvesFloor(session.floorNumber);
                    String halfTag = twoHalves ? (session.currentHalf == 1 ? " §d[1st Half]" : " §b[2nd Half]") : "";

                    if (session.bossEvent != null) {
                        if (session.isMonolithChamber) {
                            session.bossEvent.setProgress(monolithHpPct);
                            BossEvent.BossBarColor barCol = monolithHpPct >= 0.6F ? BossEvent.BossBarColor.GREEN : (monolithHpPct >= 0.3F ? BossEvent.BossBarColor.YELLOW : BossEvent.BossBarColor.RED);
                            session.bossEvent.setColor(barCol);
                            String pctCol = monolithHpPct >= 0.6F ? "§a" : (monolithHpPct >= 0.3F ? "§e" : "§c");
                            session.bossEvent.setName(Component.literal(
                                    "§b✦ SPIRAL ABYSS §7| §6Monolith: " + pctCol + (int)(monolithHpPct * 100) + "%" + halfTag + " §7(" + timeStr + ") §8| §cMonsters: " + remaining + "/" + session.totalEnemiesInWave
                            ));
                        } else {
                            float progress = Math.max(0.0F, (float) session.chamberSecondsRemaining / 300.0F);
                            session.bossEvent.setProgress(progress);
                            session.bossEvent.setName(Component.literal(
                                    "§b✦ SPIRAL ABYSS §7| §eFloor " + session.floorNumber + " - Chamber " + session.currentChamber + halfTag +
                                            " §7(" + timeStr + ") §8| §cMonsters: " + remaining + "/" + session.totalEnemiesInWave
                            ));
                        }
                    }

                    if (session.chamberSecondsRemaining <= 0) {
                        handleChamberDefeat(player, session, server);
                        ACTIVE_SESSIONS.remove(entry.getKey());
                        continue;
                    }

                    if (remaining == 0) {
                        if (twoHalves && session.currentHalf == 1) {
                            advanceToSecondHalf(player, session, abyssLevel);
                        } else {
                            handleChamberVictory(player, session, abyssLevel, server);
                        }
                    }
                }
            }

            if (session.state == SessionState.CHAMBER_CLEARED) {
                if (session.rewardChestPos != null && !session.rewardChestClaimed && server.getTickCount() % 5 == 0) {
                    BlockPos cp = session.rewardChestPos;
                    abyssLevel.sendParticles(ParticleTypes.WAX_OFF, cp.getX() + 0.5, cp.getY() + 0.7, cp.getZ() + 0.5, 3, 0.25, 0.25, 0.25, 0.02);
                    abyssLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cp.getX() + 0.5, cp.getY() + 0.9, cp.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
                }

                if (session.descentPortalPos != null) {
                    BlockPos pp = session.descentPortalPos;
                    if (server.getTickCount() % 3 == 0) {
                        abyssLevel.sendParticles(ParticleTypes.PORTAL, pp.getX() + 0.5, pp.getY() + 1.2, pp.getZ() + 0.5, 6, 0.4, 0.8, 0.4, 0.05);
                        abyssLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, pp.getX() + 0.5, pp.getY() + 1.2, pp.getZ() + 0.5, 3, 0.3, 0.6, 0.3, 0.02);
                    }

                    double distToPortal = player.position().distanceTo(new Vec3(pp.getX() + 0.5, pp.getY(), pp.getZ() + 0.5));
                    if (distToPortal <= 2.0) {
                        if (session.currentChamber < 3) {
                            advanceToNextChamber(player, session, abyssLevel);
                        } else {
                            handleFloorCompletion(player, session, server);
                            cleanUpSession(server, session);
                            ACTIVE_SESSIONS.remove(player.getUUID());
                        }
                    }
                }
            }
        }
    }

    private static void advanceToSecondHalf(ServerPlayer player, AbyssSession session, ServerLevel level) {
        session.currentHalf = 2;

        if (session.isMonolithChamber && session.monolithEntityId != null) {
            var mEnt = level.getEntity(session.monolithEntityId);
            if (mEnt != null) {
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, mEnt.getX(), mEnt.getY() + 1.5, mEnt.getZ(), 40, 0.5, 0.8, 0.5, 0.15);
                mEnt.discard();
            }
            session.monolithEntityId = null;
            session.isMonolithChamber = false;
        }

        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.3F);

        BlockPos center = session.arenaCenter;
        level.sendParticles(ParticleTypes.FLASH, center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.0);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, 60, 2.0, 1.0, 2.0, 0.1);

        player.displayClientMessage(Component.translatable("abyss.awakening_system.first_half_complete"), false);

        spawnChamberWave(level, session);
    }

    private static void handleChamberVictory(ServerPlayer player, AbyssSession session, ServerLevel level, MinecraftServer server) {
        session.state = SessionState.CHAMBER_CLEARED;

        int stars;
        if (session.isMonolithChamber && session.monolithEntityId != null) {
            var mEnt = level.getEntity(session.monolithEntityId);
            float hpPct = (mEnt instanceof LivingEntity le) ? Math.max(0.0F, Math.min(1.0F, le.getHealth() / session.monolithMaxHp)) : 0.0F;
            stars = (hpPct >= 0.60F) ? 3 : ((hpPct >= 0.40F) ? 2 : 1);
        } else {
            stars = (session.chamberSecondsRemaining >= 180) ? 3 : ((session.chamberSecondsRemaining >= 120) ? 2 : 1);
        }
        session.chamberStarsEarned[session.currentChamber - 1] = stars;

        if (session.monolithEntityId != null) {
            var mEnt = level.getEntity(session.monolithEntityId);
            if (mEnt != null) mEnt.discard();
            session.monolithEntityId = null;
        }
        BlockPos center = session.arenaCenter;
        level.setBlock(center.above(1), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(2), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(3), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(center.above(4), Blocks.AIR.defaultBlockState(), 2);

        String starIcons = "⭐".repeat(stars);
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.displayClientMessage(Component.translatable("abyss.awakening_system.chamber_cleared", session.currentChamber, starIcons, session.chamberSecondsRemaining), false);

        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
            session.bossEvent = null;
        }

        boolean isFloorFinished = (session.currentChamber == 3);
        int totalStarsInFloor = 0;
        boolean canAdvance = false;
        int nextFloor = session.floorNumber + 1;

        if (isFloorFinished) {
            SpiralAbyssSavedData savedData = SpiralAbyssSavedData.get(server);
            PlayerAbyssData pData = savedData.getOrCreate(player.getUUID());
            totalStarsInFloor = session.chamberStarsEarned[0] + session.chamberStarsEarned[1] + session.chamberStarsEarned[2];
            int floorIdx = session.floorNumber - 1;

            if (totalStarsInFloor > pData.starsPerFloor[floorIdx]) {
                pData.starsPerFloor[floorIdx] = totalStarsInFloor;
            }

            if (totalStarsInFloor >= 6 && session.floorNumber < TOTAL_FLOORS) {
                if (pData.unlockedFloor <= session.floorNumber) {
                    pData.unlockedFloor = nextFloor;
                }
                canAdvance = true;
            }
            savedData.setDirty();
        }

        BlockPos chestPos = center.offset(0, 1, 2);
        session.rewardChestPos = chestPos;
        session.rewardChestClaimed = false;
        level.setBlock(chestPos, Blocks.ENDER_CHEST.defaultBlockState(), 2);

        ArmorStand chestMarker = new ArmorStand(level, chestPos.getX() + 0.5, chestPos.getY() + 1.1, chestPos.getZ() + 0.5);
        CompoundTag chestTag = new CompoundTag();
        chestTag.putBoolean("Marker", true);
        chestTag.putBoolean("Invisible", true);
        chestTag.putBoolean("Small", true);
        chestTag.putBoolean("NoBasePlate", true);
        chestMarker.readAdditionalSaveData(chestTag);
        chestMarker.setInvisible(true);
        chestMarker.setNoGravity(true);
        chestMarker.setInvulnerable(true);
        chestMarker.setCustomName(Component.translatable("abyss.awakening_system.chest_marker"));
        chestMarker.setCustomNameVisible(true);
        level.addFreshEntity(chestMarker);
        session.rewardChestMarkerId = chestMarker.getUUID();
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, chestPos.getX() + 0.5, chestPos.getY() + 1.2, chestPos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.15);

        BlockPos portalPos = center.offset(0, 1, 13);
        session.descentPortalPos = portalPos;
        buildDescentPortalArch(level, session, portalPos);

        Component portalTitle = (!isFloorFinished)
                ? Component.translatable("abyss.awakening_system.portal_descent_marker", session.currentChamber + 1)
                : Component.translatable("abyss.awakening_system.portal_astral_exit_marker");

        ArmorStand portalMarker = new ArmorStand(level, portalPos.getX() + 0.5, portalPos.getY() + 2.0, portalPos.getZ() + 0.5);
        CompoundTag portalTag = new CompoundTag();
        portalTag.putBoolean("Marker", true);
        portalTag.putBoolean("Invisible", true);
        portalTag.putBoolean("Small", true);
        portalTag.putBoolean("NoBasePlate", true);
        portalMarker.readAdditionalSaveData(portalTag);
        portalMarker.setInvisible(true);
        portalMarker.setNoGravity(true);
        portalMarker.setInvulnerable(true);
        portalMarker.setCustomName(portalTitle);
        portalMarker.setCustomNameVisible(true);
        level.addFreshEntity(portalMarker);
        session.descentPortalMarkerId = portalMarker.getUUID();

        if (isFloorFinished) {
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("abyss.awakening_system.divider"), false);
            player.displayClientMessage(Component.translatable("abyss.awakening_system.floor_cleared_stars", session.floorNumber, totalStarsInFloor), false);
            if (canAdvance) {
                player.displayClientMessage(Component.translatable("abyss.awakening_system.advance_or_menu_prompt", nextFloor), false);

                MutableComponent advanceBtn = Component.translatable("abyss.awakening_system.btn_advance_floor", nextFloor)
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/abyss advance"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("abyss.awakening_system.hover_advance_floor", nextFloor))));

                MutableComponent menuBtn = Component.translatable("abyss.awakening_system.btn_choose_level_menu")
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/abyss menu"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("abyss.awakening_system.hover_choose_level_menu"))));

                player.displayClientMessage(advanceBtn.append(menuBtn), false);
            } else if (session.floorNumber == TOTAL_FLOORS) {
                player.displayClientMessage(Component.translatable("abyss.awakening_system.fully_conquered"), false);
                MutableComponent menuBtn = Component.translatable("abyss.awakening_system.btn_open_abyss_menu")
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/abyss menu"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("abyss.awakening_system.hover_open_abyss_menu"))));
                player.displayClientMessage(menuBtn, false);
            } else {
                player.displayClientMessage(Component.translatable("abyss.awakening_system.floor_stars_unlock_needed", totalStarsInFloor, nextFloor), false);
                MutableComponent menuBtn = Component.translatable("abyss.awakening_system.btn_retry_level_menu")
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/abyss menu"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("abyss.awakening_system.hover_retry_level_menu"))));
                player.displayClientMessage(menuBtn, false);
            }
            player.displayClientMessage(Component.translatable("abyss.awakening_system.divider"), false);
            player.displayClientMessage(Component.translatable("abyss.awakening_system.claim_chest_astral_exit"), false);
        } else {
            player.displayClientMessage(Component.translatable("abyss.awakening_system.claim_chest_descent_portal"), false);
        }
    }

    private static void buildDescentPortalArch(ServerLevel level, AbyssSession session, BlockPos base) {
        session.descentPortalBlocks.clear();
        for (int dy = 0; dy <= 3; dy++) {
            for (int dx : new int[]{-2, 2}) {
                BlockPos p = base.offset(dx, dy, 0);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState(), 2);
                session.descentPortalBlocks.add(p);
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            BlockPos p = base.offset(dx, 4, 0);
            level.setBlock(p, (dx == 0) ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            session.descentPortalBlocks.add(p);
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 3; dy++) {
                BlockPos p = base.offset(dx, dy, 0);
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                session.descentPortalBlocks.add(p);
            }
        }
    }

    private static void advanceToNextChamber(ServerPlayer player, AbyssSession session, ServerLevel level) {
        cleanUpChamberDecorations(level, session);
        session.currentChamber++;
        session.currentHalf = 1;
        session.chamberSecondsRemaining = 300;
        session.state = SessionState.WAITING_FOR_TRIGGER;

        spawnChallengeSword(level, session);

        BlockPos spawnPos = session.arenaCenter.offset(0, 1, -14);
        player.teleportTo(level, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
        player.playNotifySound(SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.PORTAL, spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.1);

        player.displayClientMessage(Component.translatable("abyss.awakening_system.descended_next_chamber", session.currentChamber), false);
    }

    public static boolean handleChestInteraction(ServerPlayer player, BlockPos clickedPos) {
        AbyssSession session = ACTIVE_SESSIONS.get(player.getUUID());
        if (session == null || session.state != SessionState.CHAMBER_CLEARED) return false;
        if (session.rewardChestPos == null || session.rewardChestClaimed) return false;

        if (clickedPos.equals(session.rewardChestPos) || clickedPos.distSqr(session.rewardChestPos) <= 4.0) {
            session.rewardChestClaimed = true;
            ServerLevel level = getAbyssLevel(player.getServer());

            int floor = session.floorNumber;
            int moraReward = 20000 * floor;
            int primoReward = 100;
            int witCount = 2 + floor;

            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.addMora(moraReward);
                data.addPrimogems(primoReward);
                AwakeningSystemMod.sync(player);
            }

            if (SystemItems.MORA.isBound()) {
                ItemEntity moraDrop = new ItemEntity(level, clickedPos.getX() + 0.5, clickedPos.getY() + 0.8, clickedPos.getZ() + 0.5, new ItemStack(SystemItems.MORA.get(), Math.min(64, 10 * floor)));
                moraDrop.setDefaultPickUpDelay();
                level.addFreshEntity(moraDrop);
            }
            if (SystemItems.PRIMOGEM.isBound()) {
                ItemEntity primoDrop = new ItemEntity(level, clickedPos.getX() + 0.5, clickedPos.getY() + 1.0, clickedPos.getZ() + 0.5, new ItemStack(SystemItems.PRIMOGEM.get(), 2));
                primoDrop.setDefaultPickUpDelay();
                level.addFreshEntity(primoDrop);
            }
            if (SystemItems.HEROS_WIT.isBound()) {
                ItemEntity witDrop = new ItemEntity(level, clickedPos.getX() + 0.5, clickedPos.getY() + 0.9, clickedPos.getZ() + 0.5, new ItemStack(SystemItems.HEROS_WIT.get(), witCount));
                witDrop.setDefaultPickUpDelay();
                level.addFreshEntity(witDrop);
            }

            player.playNotifySound(SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, clickedPos.getX() + 0.5, clickedPos.getY() + 1.2, clickedPos.getZ() + 0.5, 45, 0.4, 0.5, 0.4, 0.2);

            player.displayClientMessage(Component.translatable("abyss.awakening_system.domain_chest_claimed"), false);
            player.displayClientMessage(Component.translatable("abyss.awakening_system.domain_chest_rewards", moraReward, primoReward, witCount), false);

            if (session.rewardChestMarkerId != null) {
                var m = level.getEntity(session.rewardChestMarkerId);
                if (m != null) m.discard();
                session.rewardChestMarkerId = null;
            }
            level.setBlock(clickedPos, Blocks.AIR.defaultBlockState(), 2);
            session.rewardChestPos = null;

            return true;
        }
        return false;
    }

    public static void handleFloorCompletion(ServerPlayer player, AbyssSession session, MinecraftServer server) {
        SpiralAbyssSavedData savedData = SpiralAbyssSavedData.get(server);
        PlayerAbyssData pData = savedData.getOrCreate(player.getUUID());

        int totalStarsInFloor = session.chamberStarsEarned[0] + session.chamberStarsEarned[1] + session.chamberStarsEarned[2];
        int floorIdx = session.floorNumber - 1;

        if (totalStarsInFloor > pData.starsPerFloor[floorIdx]) {
            pData.starsPerFloor[floorIdx] = totalStarsInFloor;
        }

        if (totalStarsInFloor >= 6 && session.floorNumber < TOTAL_FLOORS) {
            if (pData.unlockedFloor <= session.floorNumber) {
                pData.unlockedFloor = session.floorNumber + 1;
            }
        }
        savedData.setDirty();

        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("abyss.awakening_system.floor_completed_stars", session.floorNumber, totalStarsInFloor), false);
        player.displayClientMessage(Component.translatable("abyss.awakening_system.redirecting_to_menu"), false);

        player.getPersistentData().remove("abyss_last_open_ms");

        ServerLevel retLevel = server.getLevel(session.returnDimension);
        if (retLevel != null) {
            Vec3 safePos = session.returnPos;
            if (safePos.distanceTo(new Vec3(TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getX(), safePos.y, TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getZ())) < 20.0) {
                safePos = safePos.add(0, 0, 5.0);
            }
            player.teleportTo(retLevel, safePos.x, safePos.y, safePos.z, session.returnYaw, session.returnPitch);
        } else {
            TeyvatWorldManager.teleportToAbyss(player);
        }

        PENDING_MENU_OPENS.put(player.getUUID(), 4);
    }

    private static void handleChamberDefeat(ServerPlayer player, AbyssSession session, MinecraftServer server) {
        player.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.6F, 0.8F);
        player.displayClientMessage(Component.translatable("abyss.awakening_system.chamber_challenge_failed"), false);

        cleanUpSession(server, session);

        player.getPersistentData().remove("abyss_last_open_ms");

        ServerLevel retLevel = server.getLevel(session.returnDimension);
        if (retLevel != null) {
            Vec3 safePos = session.returnPos;
            if (safePos.distanceTo(new Vec3(TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getX(), safePos.y, TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getZ())) < 20.0) {
                safePos = safePos.add(0, 0, 5.0);
            }
            player.teleportTo(retLevel, safePos.x, safePos.y, safePos.z, session.returnYaw, session.returnPitch);
        } else {
            TeyvatWorldManager.teleportToAbyss(player);
        }

        PENDING_MENU_OPENS.put(player.getUUID(), 4);
    }

    public static void handleAdvanceOrNextFloor(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        AbyssSession session = ACTIVE_SESSIONS.get(player.getUUID());
        SpiralAbyssSavedData savedData = SpiralAbyssSavedData.get(server);
        PlayerAbyssData pData = savedData.getOrCreate(player.getUUID());

        if (session != null) {
            if (session.currentChamber == 3 && session.state == SessionState.CHAMBER_CLEARED) {
                int totalStars = session.chamberStarsEarned[0] + session.chamberStarsEarned[1] + session.chamberStarsEarned[2];
                if (totalStars >= 6 && session.floorNumber < TOTAL_FLOORS) {
                    int nextFloor = session.floorNumber + 1;
                    cleanUpSession(server, session);
                    ACTIVE_SESSIONS.remove(player.getUUID());

                    player.displayClientMessage(Component.translatable("abyss.awakening_system.advancing_next_floor", nextFloor), false);
                    handleStartFloor(player, nextFloor);
                    return;
                } else if (session.floorNumber >= TOTAL_FLOORS) {
                    player.displayClientMessage(Component.translatable("abyss.awakening_system.last_floor_conquered"), false);
                    handleFloorCompletion(player, session, server);
                    cleanUpSession(server, session);
                    ACTIVE_SESSIONS.remove(player.getUUID());
                    return;
                } else {
                    player.displayClientMessage(Component.translatable("abyss.awakening_system.need_6_stars_advance"), false);
                    handleFloorCompletion(player, session, server);
                    cleanUpSession(server, session);
                    ACTIVE_SESSIONS.remove(player.getUUID());
                    return;
                }
            } else {
                player.displayClientMessage(Component.translatable("abyss.awakening_system.must_complete_all_chambers"), true);
                return;
            }
        }

        int targetFloor = Math.min(TOTAL_FLOORS, pData.unlockedFloor);
        handleStartFloor(player, targetFloor);
    }

    public static void openAbyssMenuFromCommand(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        AbyssSession session = ACTIVE_SESSIONS.get(player.getUUID());
        if (session != null) {
            if (session.currentChamber == 3 && session.state == SessionState.CHAMBER_CLEARED) {
                handleFloorCompletion(player, session, server);
                cleanUpSession(server, session);
                ACTIVE_SESSIONS.remove(player.getUUID());
                return;
            } else {
                cleanUpSession(server, session);
                ACTIVE_SESSIONS.remove(player.getUUID());
                ServerLevel retLevel = server.getLevel(session.returnDimension);
                if (retLevel != null) {
                    player.teleportTo(retLevel, session.returnPos.x, session.returnPos.y, session.returnPos.z, session.returnYaw, session.returnPitch);
                } else {
                    TeyvatWorldManager.teleportToAbyss(player);
                }
                PENDING_MENU_OPENS.put(player.getUUID(), 4);
                return;
            }
        }

        openAbyssScreen(player);
    }

    public static void handleLeaveAbyss(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        AbyssSession session = ACTIVE_SESSIONS.remove(player.getUUID());
        if (session != null) {
            cleanUpSession(server, session);
            ServerLevel retLevel = server.getLevel(session.returnDimension);
            if (retLevel != null) {
                player.teleportTo(retLevel, session.returnPos.x, session.returnPos.y, session.returnPos.z, session.returnYaw, session.returnPitch);
            } else {
                TeyvatWorldManager.teleportToAbyss(player);
            }
        } else {
            TeyvatWorldManager.teleportToAbyss(player);
        }
        player.displayClientMessage(Component.translatable("abyss.awakening_system.left_spiral_abyss"), true);
    }

    private static void cleanUpSession(MinecraftServer server, AbyssSession session) {
        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
            session.bossEvent = null;
        }
        ServerLevel abyssLevel = getAbyssLevel(server);
        cleanUpChamberDecorations(abyssLevel, session);

        if (session.challengeKeyEntityId != null) {
            var keyEnt = abyssLevel.getEntity(session.challengeKeyEntityId);
            if (keyEnt != null) keyEnt.discard();
            session.challengeKeyEntityId = null;
        }
        BlockPos center = session.arenaCenter;
        abyssLevel.setBlock(center.above(1), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        abyssLevel.setBlock(center.above(2), Blocks.AIR.defaultBlockState(), 2);
        abyssLevel.setBlock(center.above(3), Blocks.AIR.defaultBlockState(), 2);
        abyssLevel.setBlock(center.above(4), Blocks.AIR.defaultBlockState(), 2);

        for (UUID mobId : session.activeEnemies) {
            var ent = abyssLevel.getEntity(mobId);
            if (ent != null) ent.discard();
        }
        session.activeEnemies.clear();
        session.pendingSpawns.clear();
    }

    public static void clearAll() {
        ACTIVE_SESSIONS.clear();
        PENDING_MENU_OPENS.clear();
    }
}
