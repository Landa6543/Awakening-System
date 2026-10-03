package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.world.TeyvatAbyssPortalBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

public final class TeyvatWorldManager {
    private TeyvatWorldManager() {}

    public static final ResourceKey<Level> TEYVAT_WORLD = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "teyvat_world")
    );

    /** Posición central del Arrecife de Musk (Musk Reef) en la dimensión de Teyvat */
    public static final BlockPos MUSK_REEF_ISLAND_POS = new BlockPos(160, 64, 160);
    public static final Direction ABYSS_FACING = Direction.SOUTH;

    // Coordenadas canónicas de estructuras emblemáticas y jefes de Teyvat
    public static final BlockPos GOLDEN_HOUSE_POS = new BlockPos(500, 75, 500);       // Casa Dorada (Childe / Tartaglia)
    public static final BlockPos TENSHUKAKU_POS = new BlockPos(500, 75, -500);        // Tenshukaku (Raiden Shogun)
    public static final BlockPos WOLFLORD_POS = new BlockPos(-500, 80, 500);          // Enkanomiya (Rey Acechador Dorado)
    public static final BlockPos AZHDAHA_POS = new BlockPos(700, 28, 700);             // Dominio Subterráneo Azhdaha
    public static final BlockPos WINDRISE_POS = new BlockPos(90, 68, -140);           // Roble Sagrado de Windrise
    public static final BlockPos JADE_CHAMBER_POS = new BlockPos(200, 140, 400);      // Cámara de Jade Flotante
    public static final BlockPos WINERY_POS = new BlockPos(-300, 72, -300);            // Viñedo del Amanecer
    public static final BlockPos ALCOR_POS = new BlockPos(400, 64, -200);              // El Alcor (Beidou)
    public static final BlockPos WITCH_ISLAND_POS = new BlockPos(-600, 65, 300);       // Isla de la Bruja
    public static final BlockPos TREEHOUSES_POS = new BlockPos(300, 75, 600);          // Aldea de Casas en el Árbol

    // Hipóstasis elementales y Regisvid
    public static final BlockPos ANEMO_HYPOSTASIS_POS = new BlockPos(-400, 72, -400);
    public static final BlockPos ELECTRO_HYPOSTASIS_POS = new BlockPos(-400, 72, 400);
    public static final BlockPos GEO_HYPOSTASIS_POS = new BlockPos(400, 72, 800);
    public static final BlockPos CRYO_HYPOSTASIS_POS = new BlockPos(800, 85, -200);
    public static final BlockPos PYRO_HYPOSTASIS_POS = new BlockPos(-800, 75, -200);
    public static final BlockPos HYDRO_HYPOSTASIS_POS = new BlockPos(-600, 70, -600);
    public static final BlockPos CRYO_REGISVINE_POS = new BlockPos(300, 65, -600);

    private static boolean memoryAbyssActive = true;

    public static class TeyvatAbyssSavedData extends SavedData {
        public static final String DATA_NAME = "awakening_teyvat_abyss_data";
        private boolean active = true; // Por defecto ACTIVO como en Genshin Impact

        public static TeyvatAbyssSavedData get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(TeyvatAbyssSavedData::new, TeyvatAbyssSavedData::load, null),
                    DATA_NAME
            );
        }

        public static TeyvatAbyssSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            TeyvatAbyssSavedData data = new TeyvatAbyssSavedData();
            data.active = tag.contains("AbyssActive") ? tag.getBoolean("AbyssActive") : true;
            memoryAbyssActive = data.active;

            if (tag.contains("Portals", net.minecraft.nbt.Tag.TAG_LIST)) {
                net.minecraft.nbt.ListTag list = tag.getList("Portals", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag pTag = list.getCompound(i);
                    try {
                        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(pTag.getString("Dim")));
                        BlockPos pos = new BlockPos(pTag.getInt("X"), pTag.getInt("Y"), pTag.getInt("Z"));
                        Direction facing = Direction.byName(pTag.getString("Facing"));
                        if (facing == null) facing = Direction.SOUTH;
                        boolean pActive = pTag.contains("Active") ? pTag.getBoolean("Active") : data.active;
                        registerAbyssPortalInternal(dim, pos, facing, pActive);
                    } catch (Exception ignored) {}
                }
            }
            registerAbyssPortalInternal(TEYVAT_WORLD, MUSK_REEF_ISLAND_POS, ABYSS_FACING, data.active);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("AbyssActive", active);
            net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
            for (AbyssPortalEntry p : REGISTERED_PORTALS) {
                CompoundTag pTag = new CompoundTag();
                pTag.putString("Dim", p.dimension.location().toString());
                pTag.putInt("X", p.basePos.getX());
                pTag.putInt("Y", p.basePos.getY());
                pTag.putInt("Z", p.basePos.getZ());
                pTag.putString("Facing", p.facing.getName());
                pTag.putBoolean("Active", p.active);
                list.add(pTag);
            }
            tag.put("Portals", list);
            return tag;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean val) {
            this.active = val;
            memoryAbyssActive = val;
            setDirty();
        }
    }

    public static boolean isAbyssActive() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            try {
                return TeyvatAbyssSavedData.get(server).isActive();
            } catch (Exception ignored) {}
        }
        return memoryAbyssActive;
    }

    public static class AbyssPortalEntry {
        public final ResourceKey<Level> dimension;
        public final BlockPos basePos;
        public final BlockPos centerPos;
        public final Direction facing;
        public boolean active;

        public AbyssPortalEntry(ResourceKey<Level> dimension, BlockPos basePos, Direction facing, boolean active) {
            this.dimension = dimension;
            this.basePos = basePos;
            this.centerPos = basePos.above(7);
            this.facing = facing;
            this.active = active;
        }
    }

    public static final java.util.List<AbyssPortalEntry> REGISTERED_PORTALS = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        REGISTERED_PORTALS.add(new AbyssPortalEntry(TEYVAT_WORLD, MUSK_REEF_ISLAND_POS, ABYSS_FACING, true));
    }

    public static void registerAbyssPortal(ResourceKey<Level> dimension, BlockPos basePos, Direction facing, boolean active) {
        registerAbyssPortalInternal(dimension, basePos, facing, active);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            try {
                TeyvatAbyssSavedData.get(server).setDirty();
            } catch (Exception ignored) {}
        }
    }

    private static void registerAbyssPortalInternal(ResourceKey<Level> dimension, BlockPos basePos, Direction facing, boolean active) {
        for (AbyssPortalEntry entry : REGISTERED_PORTALS) {
            if (entry.dimension.equals(dimension) && entry.basePos.closerThan(basePos, 12)) {
                entry.active = active;
                return;
            }
        }
        REGISTERED_PORTALS.add(new AbyssPortalEntry(dimension, basePos, facing, active));
    }

    public static AbyssPortalEntry findNearPortal(ResourceKey<Level> dimension, BlockPos pos, double maxDist) {
        double maxDistSq = maxDist * maxDist;
        for (AbyssPortalEntry entry : REGISTERED_PORTALS) {
            if (entry.dimension.equals(dimension)) {
                if (entry.centerPos.distSqr(pos) <= maxDistSq || entry.basePos.distSqr(pos) <= maxDistSq) {
                    return entry;
                }
            }
        }
        if (dimension.equals(TEYVAT_WORLD) && MUSK_REEF_ISLAND_POS.distSqr(pos) <= maxDistSq) {
            return REGISTERED_PORTALS.isEmpty() ? null : REGISTERED_PORTALS.get(0);
        }
        return null;
    }

    public static void setAbyssActive(MinecraftServer server, boolean active) {
        memoryAbyssActive = active;
        for (AbyssPortalEntry entry : REGISTERED_PORTALS) {
            entry.active = active;
            if (server != null) {
                ServerLevel lvl = server.getLevel(entry.dimension);
                if (lvl != null) {
                    TeyvatAbyssPortalBuilder.updatePortalState(lvl, entry.basePos, entry.facing, active);
                }
            }
        }
        if (server != null) {
            try {
                TeyvatAbyssSavedData.get(server).setActive(active);
            } catch (Exception ignored) {}

            // Sincronizar vórtice visual en clientes
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                DungeonManager.syncRiftsToPlayer(player);
            }
        }
    }

    public static BlockPos getAbyssPortalCenter() {
        // El centro geométrico de la apertura circular 'O' está 7 bloques arriba de la base del portal
        return MUSK_REEF_ISLAND_POS.above(7);
    }

    public static Direction getAbyssPortalFacing() {
        return ABYSS_FACING;
    }

    public static BlockPos getSafeTeyvatSpawn(ServerLevel level) {
        BlockPos center = new BlockPos(0, 70, 0);
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center);
        if (top.getY() > level.getMinBuildHeight() && top.getY() < level.getMaxBuildHeight()) {
            return top.above();
        }
        return new BlockPos(0, 72, 0);
    }

    public static void teleportToTeyvat(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel teyvatLevel = server.getLevel(TEYVAT_WORLD);
        if (teyvatLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.teyvat.dimension_unavailable"), true);
            return;
        }

        BlockPos spawnPos = getSafeTeyvatSpawn(teyvatLevel);
        ensureTeyvatCityAndStructures(teyvatLevel);

        if (TowerManager.isInTower(player.getUUID())) {
            TowerManager.exitTowerToDimension(player, teyvatLevel, spawnPos);
        }

        player.teleportTo(teyvatLevel, spawnPos.getX() + 0.5D, (double) spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);

        teyvatLevel.sendParticles(ParticleTypes.END_ROD, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.5D, spawnPos.getZ() + 0.5D, 50, 1.0D, 1.0D, 1.0D, 0.08D);
        teyvatLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.5D, spawnPos.getZ() + 0.5D, 40, 1.5D, 1.0D, 1.5D, 0.2D);
        teyvatLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, spawnPos.getX() + 0.5D, spawnPos.getY() + 2.0D, spawnPos.getZ() + 0.5D, 60, 1.2D, 1.0D, 1.2D, 0.2D);

        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.3F);
        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 1.0F);

        // Activar el protocolo de Actualización 4.0 del Sistema solo la primera vez (5 minutos = 6000 ticks)
        SystemData data = SystemEvents.data(player);
        if (!data.system40Updated() && !data.isSystem40Updating()) {
            data.setSystem40Updating(true);
            data.setSystem40UpdateRemainingTicks(5 * 60 * 20);
            AwakeningSystemMod.sync(player);

            teyvatLevel.sendParticles(ParticleTypes.FLASH, spawnPos.getX() + 0.5D, spawnPos.getY() + 2.0D, spawnPos.getZ() + 0.5D, 8, 0, 0, 0, 0);
            teyvatLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, spawnPos.getX() + 0.5D, spawnPos.getY() + 2.0D, spawnPos.getZ() + 0.5D, 120, 2.0D, 1.5D, 2.0D, 0.2D);

            player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.4F, 0.9F);
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.3F);

            AwakeningSystemMod.notify(player,
                    "awakening_system.system40.protocol_started_title",
                    "awakening_system.system40.protocol_started_desc",
                    0xFFDFB76C);

            player.displayClientMessage(Component.translatable("awakening_system.hud.system40_update_protocol_started"), true);
        } else {
            player.displayClientMessage(Component.literal("§b§l✦ [DIMENSIÓN DE TEYVAT] §f¡Bienvenido a las tierras naturales de Teyvat!"), true);
        }
    }

    public static void completeSystem40Update(ServerPlayer player, SystemData data) {
        data.setSystem40Updating(false);
        data.setSystem40UpdateRemainingTicks(0);
        data.setSystemUpdated(true);
        data.setIsekaiUnlocked(true);
        data.setSystem30Updated(true);
        data.setMurimUnlocked(true);
        data.setSystem40Updated(true);
        data.setTeyvatUnlocked(true);
        data.addCoins(250000);
        data.addExperience(50000);
        data.addPrimogems(1600);
        data.addIntertwinedFates(10);
        AwakeningSystemMod.sync(player);

        ServerLevel sl = player.serverLevel();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py + 1.5D, pz, 300, 2.5D, 2.0D, 2.5D, 0.4D);
        sl.sendParticles(ParticleTypes.FLASH, px, py + 1.5D, pz, 10, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.FIREWORK, px, py + 2.0D, pz, 100, 1.5D, 1.5D, 1.5D, 0.2D);

        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
        player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.0F);

        AwakeningSystemMod.notify(player,
                "awakening_system.system40.update_complete_title",
                "awakening_system.system40.update_complete_desc",
                0xFFDFB76C);

        AwakeningSystemMod.notifyAll(player.getServer(),
                "awakening_system.system40.universal_broadcast_title",
                "awakening_system.system40.universal_broadcast_desc",
                0xFFDFB76C, player.getName().getString());
    }

    /**
     * Teletransporta al jugador directamente al Arrecife de Musk frente al portal de la Espiral del Abismo.
     */
    public static void teleportToAbyss(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel teyvatLevel = server.getLevel(TEYVAT_WORLD);
        if (teyvatLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.teyvat.dimension_unavailable"), true);
            return;
        }

        ensureTeyvatCityAndStructures(teyvatLevel);

        // Posición frente a los escalones de la plaza de la Espiral del Abismo
        BlockPos target = MUSK_REEF_ISLAND_POS.offset(0, 3, -6);

        if (TowerManager.isInTower(player.getUUID())) {
            TowerManager.exitTowerToDimension(player, teyvatLevel, target);
        }

        player.teleportTo(teyvatLevel, target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 0.0F, 0.0F);

        teyvatLevel.sendParticles(ParticleTypes.END_ROD, target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D, 40, 1.0D, 1.0D, 1.0D, 0.05D);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);

        player.displayClientMessage(Component.literal("§b§l✦ [ESPIRAL DEL ABISMO] §fHas llegado al solitario Arrecife de Musk."), true);
    }

    public static void ensureTeyvatCityAndStructures(ServerLevel level) {
        TeyvatAbyssPortalBuilder.ensureAbyssPortalInTeyvat(level);
        com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.ensureTeyvatArrivalPlaza(level);
        // Limpiar cualquier residuo de plataformas o cajas procedurales antiguas
        com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.cleanupProceduralCitadelAndPlaza(level);
    }

    public static void forceRebuildStructures(ServerLevel level) {
        TeyvatAbyssPortalBuilder.generateMuskReefIsland(level, MUSK_REEF_ISLAND_POS, ABYSS_FACING, isAbyssActive());
        com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.cleanupProceduralCitadelAndPlaza(level);
    }

    /**
     * Comprueba si un jugador está cruzando el vano de algún portal de la Espiral del Abismo registrado.
     * Utiliza un volumen planar/caja preciso acorde al vano circular (13x13 estructura, abertura 7x7).
     */
    public static void checkPlayerAbyssPortal(ServerPlayer player) {
        if (player == null || player.isDeadOrDying()) return;
        if (com.soybe.awakeningsystem.abyss.SpiralAbyssManager.isInAbyss(player.getUUID())) return;

        ResourceKey<Level> dim = player.level().dimension();
        for (AbyssPortalEntry portal : REGISTERED_PORTALS) {
            if (!portal.dimension.equals(dim)) continue;

            double px = player.getX() - (portal.centerPos.getX() + 0.5D);
            double py = player.getY() - portal.basePos.getY(); // Altura sobre el suelo base
            double pz = player.getZ() - (portal.centerPos.getZ() + 0.5D);

            // Distancia perpendicular a través del plano del portal
            double distFacing = Math.abs(px * portal.facing.getStepX() + pz * portal.facing.getStepZ());

            // Distancia lateral a lo ancho del vano
            Direction rightDir = portal.facing.getClockWise();
            double distLateral = Math.abs(px * rightDir.getStepX() + pz * rightDir.getStepZ());

            // Rango de paso: ancho apertura circular (distLateral <= 4.0), grosor (distFacing <= 2.5), altura (py 0.5 a 12.5)
            boolean insidePortal = (distFacing <= 2.5D && distLateral <= 4.0D && py >= 0.5D && py <= 12.5D);

            if (insidePortal) {
                // Si estaba bloqueado, activarlo automáticamente
                if (!portal.active) {
                    portal.active = true;
                    setAbyssActive(player.getServer(), true);
                }

                long now = System.currentTimeMillis();
                long lastOpen = player.getPersistentData().getLong("abyss_last_open_ms");
                if (now - lastOpen > 3000) {
                    player.getPersistentData().putLong("abyss_last_open_ms", now);
                    player.playNotifySound(ModSounds.ABYSS_ENTER.value(), SoundSource.PLAYERS, 0.9F, 1.0F);
                    player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.1D);
                    com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
                }
                break;
            }
        }
    }

    /**
     * Ciclo de actualización del servidor para partículas, ciclo día/noche e interacción con el portal.
     */
    public static void tick(MinecraftServer server) {
        // Actualizar sesiones activas de la Espiral del Abismo
        com.soybe.awakeningsystem.abyss.SpiralAbyssManager.tick(server);

        // Actualizar generación de estructuras y características interactivas de Teyvat (Estatuas, Waypoints, Updrafts)
        com.soybe.awakeningsystem.world.TeyvatWorldGenManager.tick(server);

        // Sincronizar ciclo día/noche natural y descanso en cama en Teyvat
        ServerLevel teyvatLevel = server.getLevel(TEYVAT_WORLD);
        if (teyvatLevel != null) {
            ServerLevel overworld = server.overworld();
            if (overworld != null) {
                boolean anySleepingInTeyvat = teyvatLevel.players().stream().anyMatch(net.minecraft.world.entity.player.Player::isSleeping);
                boolean anySleepingInOw = overworld.players().stream().anyMatch(net.minecraft.world.entity.player.Player::isSleeping);

                // Solo sincronizar el tiempo del overworld a Teyvat si nadie está durmiendo
                if (!anySleepingInTeyvat && !anySleepingInOw) {
                    long owDayTime = overworld.getDayTime();
                    long teyvatDayTime = teyvatLevel.getDayTime();
                    if (Math.abs(teyvatDayTime - owDayTime) > 60L) {
                        teyvatLevel.setDayTime(owDayTime);
                    }
                }
            }
        }

        for (AbyssPortalEntry portal : REGISTERED_PORTALS) {
            ServerLevel lvl = server.getLevel(portal.dimension);
            if (lvl == null) continue;

            // 1. Partículas ambientales del portal
            TeyvatAbyssPortalBuilder.tickParticles(lvl, portal.centerPos, portal.facing, portal.active);

            // 2. Comprobar colisión de jugadores en el nivel del portal
            for (ServerPlayer player : lvl.players()) {
                checkPlayerAbyssPortal(player);
            }
        }
    }

    public static void teleportToNamedStructure(ServerPlayer player, String structureKey, BlockPos defaultPos, String displayName, Vec3 playerOffset, float yaw) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel teyvatLevel = server.getLevel(TEYVAT_WORLD);
        if (teyvatLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.teyvat.dimension_unavailable"), true);
            return;
        }

        ensureTeyvatCityAndStructures(teyvatLevel);

        var tData = com.soybe.awakeningsystem.world.TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        BlockPos targetPos = tData.getNamedStructure(structureKey);
        if (targetPos == null) {
            targetPos = defaultPos;
            com.soybe.awakeningsystem.world.TeyvatWorldGenManager.spawnTeyvatStructure(teyvatLevel, targetPos, structureKey);
            BlockPos updated = tData.getNamedStructure(structureKey);
            if (updated != null) targetPos = updated;
        }

        // Corrección de mundos existentes si Golden House se había guardado con el Y del sótano (<= 77)
        if (structureKey.equals("golden_house")) {
            if (targetPos.getY() <= 77) {
                targetPos = targetPos.offset(0, 7, 4);
                tData.recordNamedStructure("golden_house", targetPos);
                tData.recordNamedStructure("childe", targetPos);
                tData.recordNamedStructure("tartaglia", targetPos);
                tData.setDirty();
            }

            // Rescatar a Tartaglia si quedó atrapado en el sótano bajo los cristales
            final BlockPos rescueTarget = targetPos;
            List<com.soybe.awakeningsystem.entity.ChildeBossEntity> stuckChildes = teyvatLevel.getEntitiesOfClass(
                    com.soybe.awakeningsystem.entity.ChildeBossEntity.class,
                    new net.minecraft.world.phys.AABB(rescueTarget).inflate(35.0D),
                    c -> c.getY() < rescueTarget.getY() - 1.5D
            );
            for (com.soybe.awakeningsystem.entity.ChildeBossEntity stuck : stuckChildes) {
                stuck.teleportTo(rescueTarget.getX() + 0.5D, rescueTarget.getY(), rescueTarget.getZ() + 0.5D);
            }
        }

        if (TowerManager.isInTower(player.getUUID())) {
            TowerManager.exitTowerToDimension(player, teyvatLevel, targetPos);
        }

        double px = targetPos.getX() + 0.5D + (playerOffset != null ? playerOffset.x : 0.0D);
        double py = targetPos.getY() + 1.0D + (playerOffset != null ? playerOffset.y : 0.0D);
        double pz = targetPos.getZ() + 0.5D + (playerOffset != null ? playerOffset.z : 0.0D);

        player.teleportTo(teyvatLevel, px, py, pz, yaw, 0.0F);

        teyvatLevel.sendParticles(ParticleTypes.END_ROD, px, py + 1.0D, pz, 40, 1.0D, 1.0D, 1.0D, 0.05D);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.displayClientMessage(Component.literal("§b§l✦ [TEYVAT] §fHas llegado a §e" + displayName + "§f."), true);
    }

    public static void teleportToNation(ServerPlayer player, BlockPos capitalPos, String nationName) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel teyvatLevel = server.getLevel(TEYVAT_WORLD);
        if (teyvatLevel == null) return;

        ensureTeyvatCityAndStructures(teyvatLevel);

        int targetY = teyvatLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, capitalPos.getX(), capitalPos.getZ());
        if (targetY < 60 || targetY > 150) targetY = capitalPos.getY();

        BlockPos target = new BlockPos(capitalPos.getX(), targetY + 1, capitalPos.getZ());
        player.teleportTo(teyvatLevel, target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 0.0F, 0.0F);

        teyvatLevel.sendParticles(ParticleTypes.END_ROD, target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D, 35, 0.8D, 1.0D, 0.8D, 0.05D);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.displayClientMessage(Component.literal("§b§l✦ [TEYVAT] §fHas llegado a §e" + nationName + "§f."), true);
    }

    public static void teleportToGoldenHouse(ServerPlayer player) {
        teleportToNamedStructure(player, "golden_house", GOLDEN_HOUSE_POS, "La Casa Dorada (Jefe Tartaglia / Childe)", new Vec3(0, 0, -10), 0.0F);
    }

    public static void teleportToTenshukaku(ServerPlayer player) {
        teleportToNamedStructure(player, "tenshukaku", TENSHUKAKU_POS, "Palacio Tenshukaku (Jefe Raiden Shogun)", new Vec3(0, 1, -10), 0.0F);
    }

    public static void teleportToWolflord(ServerPlayer player) {
        teleportToNamedStructure(player, "wolflord", WOLFLORD_POS, "Dominio del Rey Acechador Dorado (Enkanomiya)", new Vec3(0, 1, -15), 0.0F);
    }

    public static void teleportToAzhdaha(ServerPlayer player) {
        teleportToNamedStructure(player, "azhdaha", AZHDAHA_POS, "Dominio Subterráneo del Dragón Azhdaha", new Vec3(0, 1, -15), 0.0F);
    }

    public static void teleportToWindrise(ServerPlayer player) {
        teleportToNamedStructure(player, "windrise", WINDRISE_POS, "Gran Roble Sagrado de Windrise", new Vec3(0, 1, -8), 0.0F);
    }

    public static void teleportToJadeChamber(ServerPlayer player) {
        teleportToNamedStructure(player, "jade_chamber", JADE_CHAMBER_POS, "Cámara de Jade Flotante", new Vec3(0, 2, 0), 0.0F);
    }

    public static void teleportToWinery(ServerPlayer player) {
        teleportToNamedStructure(player, "dawn_winery", WINERY_POS, "Viñedo del Amanecer (Dawn Winery)", new Vec3(0, 1, 0), 0.0F);
    }

    public static void teleportToAlcor(ServerPlayer player) {
        teleportToNamedStructure(player, "beidou_ship", ALCOR_POS, "El Alcor (Navío de Beidou)", new Vec3(0, 2, 0), 0.0F);
    }

    public static void teleportToWitchIsland(ServerPlayer player) {
        teleportToNamedStructure(player, "witch_island", WITCH_ISLAND_POS, "Isla de la Bruja", new Vec3(0, 1, 0), 0.0F);
    }

    public static void teleportToTreehouses(ServerPlayer player) {
        teleportToNamedStructure(player, "teyvat_treehouses", TREEHOUSES_POS, "Aldea Arbórea de Teyvat", new Vec3(0, 1, 0), 0.0F);
    }

    public static void teleportToHypostasis(ServerPlayer player, String element) {
        switch (element.toLowerCase(java.util.Locale.ROOT)) {
            case "anemo" -> teleportToNamedStructure(player, "anemo_hypostasis", ANEMO_HYPOSTASIS_POS, "Hipóstasis Anemo (Beth)", new Vec3(0, 1, -10), 0.0F);
            case "electro" -> teleportToNamedStructure(player, "electro_hypostasis", ELECTRO_HYPOSTASIS_POS, "Hipóstasis Electro (Aleph)", new Vec3(0, 1, -10), 0.0F);
            case "geo" -> teleportToNamedStructure(player, "geo_hypostasis", GEO_HYPOSTASIS_POS, "Hipóstasis Geo (Gimel)", new Vec3(0, 1, -10), 0.0F);
            case "cryo" -> teleportToNamedStructure(player, "cryo_hypostasis", CRYO_HYPOSTASIS_POS, "Hipóstasis Cryo (Daleth)", new Vec3(0, 1, -10), 0.0F);
            case "pyro" -> teleportToNamedStructure(player, "pyro_hypostasis", PYRO_HYPOSTASIS_POS, "Hipóstasis Pyro (Ayin)", new Vec3(0, 1, -10), 0.0F);
            case "hydro" -> teleportToNamedStructure(player, "hydro_hypostasis", HYDRO_HYPOSTASIS_POS, "Hipóstasis Hydro (He)", new Vec3(0, 1, -8), 0.0F);
            case "regisvine", "cryo_regisvine" -> teleportToNamedStructure(player, "cryo_regisvine", CRYO_REGISVINE_POS, "Regisvid Cryo", new Vec3(0, 1, -10), 0.0F);
            default -> teleportToNamedStructure(player, "anemo_hypostasis", ANEMO_HYPOSTASIS_POS, "Hipóstasis Anemo", new Vec3(0, 1, -10), 0.0F);
        }
    }

    public static void teleportToMondstadt(ServerPlayer player) {
        teleportToNation(player, com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.MONDSTADT_POS, "Mondstadt (Ciudadela de Favonius)");
    }
    public static void teleportToLiyue(ServerPlayer player) {
        teleportToGoldenHouse(player);
    }
    public static void teleportToInazuma(ServerPlayer player) {
        teleportToTenshukaku(player);
    }
    public static void teleportToSumeru(ServerPlayer player) {
        teleportToNation(player, com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.SUMERU_POS, "Sumeru (La Gran Academiya)");
    }
    public static void teleportToFontaine(ServerPlayer player) {
        teleportToNation(player, com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.FONTAINE_POS, "Fontaine (Palais Mermonia)");
    }
    public static void ensureMondstadtOnServerStart(MinecraftServer server) {
        // Auto-aprovisionamiento de regiones externas desactivado
    }

    public static void checkPlayerAnemoUpdraft(ServerPlayer player) {}
}
