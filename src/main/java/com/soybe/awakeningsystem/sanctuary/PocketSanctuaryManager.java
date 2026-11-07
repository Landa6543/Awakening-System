package com.soybe.awakeningsystem.sanctuary;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.PocketDoorEntity;
import com.soybe.awakeningsystem.limbo.LimboWorldManager;
import com.soybe.awakeningsystem.penalty.PenaltyZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Gestor integral de la Dimensión de Bolsillo y Santuario Personal (Sección 29).
 * Maneja el ciclo completo de la Puerta Física Dimensional, generación del Santuario
 * en un paisaje natural procedural de cerezos, praderas y flores, y el retorno seguro.
 */
public final class PocketSanctuaryManager {

    public static final ResourceKey<Level> SANCTUARY_REALM = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "pocket_sanctuary")
    );

    private PocketSanctuaryManager() {}

    public static ServerLevel getSanctuaryLevel(MinecraftServer server) {
        if (server == null) return null;
        ServerLevel lvl = server.getLevel(SANCTUARY_REALM);
        if (lvl != null) return lvl;
        return server.overworld();
    }

    public static boolean isSanctuaryDimension(Level level) {
        return level != null && level.dimension().equals(SANCTUARY_REALM);
    }

    public static int getPlotOriginX(Player player) {
        int plotIndex = Math.abs(player.getUUID().hashCode() % 10000);
        return (plotIndex % 100) * 1024;
    }

    public static int getPlotOriginZ(Player player) {
        int plotIndex = Math.abs(player.getUUID().hashCode() % 10000);
        return (plotIndex / 100) * 1024;
    }

    /**
     * Encuentra una superficie sólida y segura (tierra/césped) sobre el nivel del mar en la dimensión procedural.
     */
    public static BlockPos findSafeSurfacePos(ServerLevel level, int ox, int oz) {
        // Asegurar que el chunk principal esté generado y cargado
        level.getChunk(ox >> 4, oz >> 4);

        for (int r = 0; r <= 32; r += 4) {
            for (int dx = -r; dx <= r; dx += 4) {
                for (int dz = -r; dz <= r; dz += 4) {
                    int x = ox + dx;
                    int z = oz + dz;
                    level.getChunk(x >> 4, z >> 4);
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (y >= 64 && y < 250) {
                        BlockPos ground = new BlockPos(x, y - 1, z);
                        BlockState state = level.getBlockState(ground);
                        if (!state.isAir() && !state.is(Blocks.WATER) && !state.is(Blocks.LAVA)) {
                            return new BlockPos(x, y, z);
                        }
                    }
                }
            }
        }
        int fallbackY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ox, oz);
        return new BlockPos(ox, fallbackY > 0 ? fallbackY : 80, oz);
    }

    /**
     * Verifica si el jugador se encuentra en una dimensión de confinamiento donde está prohibido escapar (Roadmap 29.1).
     */
    public static boolean isDimensionSealed(Player player) {
        if (PenaltyZoneManager.isInPenaltyZone(player)) return true;
        if (LimboWorldManager.isLimboDimension(player.level())) return true;
        if (com.soybe.awakeningsystem.DungeonManager.isInDungeon(player.getUUID())) return true;
        if (com.soybe.awakeningsystem.TowerManager.isInTower(player.getUUID())) return true;
        if (com.soybe.awakeningsystem.PagodaTrialManager.isInTrial(player.getUUID())) return true;
        return false;
    }

    /**
     * Invoca la puerta física tridimensional frente a la posición de la mirada del jugador en el mundo exterior.
     */
    public static PocketDoorEntity summonExteriorDoor(ServerPlayer player) {
        if (isSanctuaryDimension(player.level())) {
            player.displayClientMessage(Component.literal("§cYa te encuentras dentro de tu Santuario personal. Usa la puerta interior para regresar."), true);
            return null;
        }

        if (isDimensionSealed(player)) {
            player.displayClientMessage(Component.literal("§c§l[SANTUARIO SELLADO] §7Las leyes espaciales de esta dimensión impiden abrir la puerta de bolsillo."), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.7F);
            return null;
        }

        ServerLevel level = player.serverLevel();

        // 1.8 bloques frente a la mirada del jugador
        float yaw = player.getYRot();
        double radians = Math.toRadians(yaw);
        double spawnX = player.getX() - Math.sin(radians) * 1.8D;
        double spawnZ = player.getZ() + Math.cos(radians) * 1.8D;
        double spawnY = player.getY();

        // Purgar rigurosamente cualquier puerta exterior previa de este jugador en un radio amplio
        AABB searchBox = new AABB(spawnX - 32, spawnY - 16, spawnZ - 32, spawnX + 32, spawnY + 16, spawnZ + 32);
        List<PocketDoorEntity> nearbyDoors = level.getEntitiesOfClass(PocketDoorEntity.class, searchBox,
                d -> !d.isSanctuarySide() && player.getUUID().equals(d.getOwnerUUID()));
        for (PocketDoorEntity old : nearbyDoors) {
            old.discard();
        }

        PocketDoorEntity door = ModEntities.POCKET_DOOR.get().create(level);
        if (door != null) {
            door.setPos(spawnX, spawnY, spawnZ);
            door.setYRot(yaw + 180.0F); // Mirando de frente al jugador
            door.setOwner(player.getUUID());
            door.setSanctuarySide(false);
            door.setOpen(false);
            level.addFreshEntity(door);
            SystemEvents.data(player).setSanctuaryExteriorDoorActive(true);

            level.playSound(null, spawnX, spawnY, spawnZ, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.9F);
            player.displayClientMessage(Component.literal("§a[Puerta de Bolsillo] Puerta física invocada. Haz clic derecho para abrirla y cruzar."), true);
        }
        return door;
    }

    /**
     * Invocación de la habilidad activa desde el Sistema de Habilidades (Roadmap Sección 29).
     */
    public static void handleSkillActivation(ServerPlayer player) {
        if (isSanctuaryDimension(player.level())) {
            player.displayClientMessage(Component.literal("§eYa estás dentro de tu dimensión de bolsillo. Abre la puerta interior para regresar."), true);
            return;
        }

        if (isDimensionSealed(player)) {
            player.displayClientMessage(Component.literal("§c§l[SANTUARIO SELLADO] §7Las leyes espaciales impiden abrir la puerta de bolsillo aquí."), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.7F);
            return;
        }

        summonExteriorDoor(player);
    }

    /**
     * Entrada del jugador a la Dimensión de Bolsillo:
     * 1. Guarda la posición y orientación de origen del mundo exterior.
     * 2. Localiza o asegura la terraza del Santuario en el mundo natural procedural.
     * 3. Deja la puerta interior abierta para recibir al cazador.
     * 4. Teletransporta al jugador de forma segura frente a la puerta sobre la terraza.
     */
    public static void enterSanctuary(ServerPlayer player, PocketDoorEntity exteriorDoor) {
        if (isSanctuaryDimension(player.level())) {
            player.displayClientMessage(Component.literal("§eYa estás dentro de tu dimensión de bolsillo."), true);
            return;
        }

        if (isDimensionSealed(player)) {
            player.displayClientMessage(Component.literal("§c§l[SANTUARIO SELLADO] §7No puedes acceder a tu santuario desde este confinamiento."), true);
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel sanctuaryLevel = getSanctuaryLevel(server);
        if (sanctuaryLevel == null) return;

        SystemData data = SystemEvents.data(player);
        double saveX = exteriorDoor != null ? exteriorDoor.getX() : player.getX();
        double saveY = exteriorDoor != null ? exteriorDoor.getY() : player.getY();
        double saveZ = exteriorDoor != null ? exteriorDoor.getZ() : player.getZ();
        float saveYaw = exteriorDoor != null ? exteriorDoor.getYRot() : (player.getYRot() + 180.0F);
        float savePitch = player.getXRot();

        data.setSanctuaryReturnLocation(
                player.level().dimension().location().toString(),
                saveX, saveY, saveZ, saveYaw, savePitch
        );

        int plotX, plotY, plotZ;
        if (data.hasSanctuaryPlot()) {
            plotX = data.sanctuaryPlotX();
            plotY = data.sanctuaryPlotY();
            plotZ = data.sanctuaryPlotZ();
        } else {
            int ox = getPlotOriginX(player);
            int oz = getPlotOriginZ(player);
            BlockPos surfacePos = findSafeSurfacePos(sanctuaryLevel, ox, oz);
            plotX = surfacePos.getX();
            plotY = surfacePos.getY();
            plotZ = surfacePos.getZ();
            data.setSanctuaryPlotPos(plotX, plotY, plotZ);
            ensureSanctuaryPlot(sanctuaryLevel, player, plotX, plotY, plotZ);
            data.setSanctuaryBuiltLevel(Math.max(1, data.sanctuaryBuiltLevel()));
        }

        // Asegurar exactamente 1 puerta interior abierta para recibir al jugador con cooldown
        PocketDoorEntity interiorDoor = getInteriorDoor(sanctuaryLevel, player, plotX, plotY, plotZ);
        if (interiorDoor != null) {
            interiorDoor.setOpen(true);
            interiorDoor.cooldownTicks = 80;
        }

        // Teletransporte seguro a 1.5 bloques frente a la puerta sobre el césped natural
        player.teleportTo(sanctuaryLevel, plotX + 0.5D, plotY + 0.1D, plotZ + 0.5D, 0.0F, 0.0F);
        player.fallDistance = 0.0F;
        player.getPersistentData().putLong("pocket_door_cooldown", sanctuaryLevel.getGameTime() + 80);

        player.sendSystemMessage(Component.literal("§6§l[SANTUARIO PERSONAL] §fHas entrado a tu dimensión de bolsillo."));
        player.sendSystemMessage(Component.literal("§eCierra la puerta desde adentro con clic derecho para que el acceso desaparezca del mundo exterior."));

        AwakeningSystemMod.sync(player);
    }

    /**
     * Paso 2 del Roadmap: El cazador cierra la puerta desde adentro del Santuario.
     * Al cerrarse, la puerta del mundo exterior se desvanece por completo al instante (Privacidad y seguridad total).
     */
    public static void onSanctuaryDoorClosed(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        data.setSanctuaryExteriorDoorActive(false);

        ResourceLocation dimLoc = ResourceLocation.tryParse(data.sanctuaryReturnDim());
        MinecraftServer server = player.getServer();
        if (server == null || dimLoc == null) return;

        ServerLevel extLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimLoc));
        if (extLevel == null) return;

        double rx = data.sanctuaryReturnX();
        double ry = data.sanctuaryReturnY();
        double rz = data.sanctuaryReturnZ();

        // Cargar el chunk de origen para garantizar la purga inmediata
        extLevel.getChunk((int) rx >> 4, (int) rz >> 4);
        AABB box = new AABB(rx - 64, ry - 32, rz - 64, rx + 64, ry + 32, rz + 64);
        List<PocketDoorEntity> nearbyDoors = extLevel.getEntitiesOfClass(PocketDoorEntity.class, box,
                d -> !d.isSanctuarySide() && player.getUUID().equals(d.getOwnerUUID()));
        for (PocketDoorEntity d : nearbyDoors) {
            extLevel.playSound(null, d.getX(), d.getY(), d.getZ(), SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.9F);
            d.discard();
        }

        // Purgar cualquier puerta exterior residual en cualquier dimensión no santuario
        for (ServerLevel sLevel : server.getAllLevels()) {
            if (!isSanctuaryDimension(sLevel)) {
                List<? extends PocketDoorEntity> residuals = sLevel.getEntities(
                        net.minecraft.world.level.entity.EntityTypeTest.forClass(PocketDoorEntity.class),
                        d -> !d.isSanctuarySide() && player.getUUID().equals(d.getOwnerUUID())
                );
                for (PocketDoorEntity d : residuals) {
                    d.discard();
                }
            }
        }
    }

    /**
     * Paso 3 del Roadmap: El cazador abre la puerta desde adentro del Santuario para salir.
     * En ese instante exacto, la puerta reaparece abierta en el Overworld en las coordenadas de origen.
     */
    public static void onSanctuaryDoorOpened(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        data.setSanctuaryExteriorDoorActive(true);

        ResourceLocation dimLoc = ResourceLocation.tryParse(data.sanctuaryReturnDim());
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel returnLevel = null;
        if (dimLoc != null) {
            returnLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimLoc));
        }
        if (returnLevel == null) {
            returnLevel = server.overworld();
        }
        if (returnLevel == null) return;

        double rx = data.sanctuaryReturnX();
        double ry = data.sanctuaryReturnY();
        double rz = data.sanctuaryReturnZ();
        float rYaw = data.sanctuaryReturnYaw();
        float rPitch = data.sanctuaryReturnPitch();

        // Buscar puertas existentes y purgar duplicados, garantizando exactamente 1 puerta abierta
        AABB box = new AABB(rx - 16, ry - 8, rz - 16, rx + 16, ry + 8, rz + 16);
        List<PocketDoorEntity> existing = returnLevel.getEntitiesOfClass(PocketDoorEntity.class, box,
                d -> !d.isSanctuarySide() && player.getUUID().equals(d.getOwnerUUID()));
        if (existing.isEmpty()) {
            PocketDoorEntity door = ModEntities.POCKET_DOOR.get().create(returnLevel);
            if (door != null) {
                door.setPos(rx, ry, rz);
                door.setYRot(rYaw);
                door.setXRot(rPitch);
                door.setOwner(player.getUUID());
                door.setSanctuarySide(false);
                door.setOpen(true); // Reaparece abierta
                returnLevel.addFreshEntity(door);
                returnLevel.playSound(null, rx, ry, rz, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            PocketDoorEntity main = existing.get(0);
            main.setOpen(true);
            for (int i = 1; i < existing.size(); i++) {
                existing.get(i).discard();
            }
        }
    }

    /**
     * Retorno del jugador al mundo exterior (Paso 4 del Roadmap):
     * - Cruza la puerta abierta y reaparece frente a la puerta exterior en el Overworld.
     */
    public static void exitSanctuary(ServerPlayer player, PocketDoorEntity interiorDoor) {
        SystemData data = SystemEvents.data(player);
        ResourceLocation dimLoc = ResourceLocation.tryParse(data.sanctuaryReturnDim());
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel returnLevel = null;
        if (dimLoc != null) {
            returnLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimLoc));
        }
        if (returnLevel == null) {
            returnLevel = server.overworld();
        }
        if (returnLevel == null) return;

        double rx = data.sanctuaryReturnX();
        double ry = data.sanctuaryReturnY();
        double rz = data.sanctuaryReturnZ();
        float rYaw = data.sanctuaryReturnYaw();
        float rPitch = data.sanctuaryReturnPitch();

        // Seguridad ante alturas inválidas
        if (ry <= -60 || ry >= 320) {
            BlockPos spawn = returnLevel.getSharedSpawnPos();
            rx = spawn.getX() + 0.5D;
            ry = spawn.getY() + 1.0D;
            rz = spawn.getZ() + 0.5D;
        }

        // Asegurar que la puerta exterior esté activa y abierta en el Overworld
        onSanctuaryDoorOpened(player);

        // Posición segura frente a la puerta exterior (1.8 bloques hacia el frente)
        double rad = Math.toRadians(rYaw);
        double stepX = -Math.sin(rad) * 1.8D;
        double stepZ = Math.cos(rad) * 1.8D;

        player.teleportTo(returnLevel, rx + stepX, ry, rz + stepZ, rYaw, rPitch);
        player.fallDistance = 0.0F;
        player.getPersistentData().putLong("pocket_door_cooldown", returnLevel.getGameTime() + 80);

        player.displayClientMessage(Component.literal("§aHas regresado al mundo exterior. Cierra la puerta con clic derecho para retirarla."), true);
        AwakeningSystemMod.sync(player);
    }

    /**
     * Asegura que el espacio donde se ubica la puerta interior esté limpio y sobre césped natural.
     * Solo aparece la puerta física y nada más: sin plataformas de piedra, sin faroles artificiales.
     */
    public static void ensureSanctuaryPlot(ServerLevel level, ServerPlayer player, int ox, int oy, int oz) {
        BlockPos doorGround = new BlockPos(ox, oy - 1, oz - 1);
        if (level.getBlockState(doorGround).isAir()) {
            level.setBlock(doorGround, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        }
        // Despejar el vano de la puerta y el paso inmediato solo si son bloques naturales, NUNCA bloques con inventario/BlockEntity
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos p1 = new BlockPos(ox, oy + dy, oz - 1);
            BlockState st1 = level.getBlockState(p1);
            if (!st1.isAir() && !(st1.getBlock() instanceof net.minecraft.world.level.block.EntityBlock)) {
                level.setBlock(p1, Blocks.AIR.defaultBlockState(), 3);
            }
            BlockPos p2 = new BlockPos(ox, oy + dy, oz);
            BlockState st2 = level.getBlockState(p2);
            if (!st2.isAir() && !(st2.getBlock() instanceof net.minecraft.world.level.block.EntityBlock)) {
                level.setBlock(p2, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static boolean isPlotEmpty(ServerLevel level, int ox, int oy, int oz) {
        BlockPos doorPos = new BlockPos(ox, oy, oz - 1);
        return level.getBlockState(doorPos).isAir();
    }

    /**
     * Obtiene o crea la única puerta interior del santuario en las coordenadas dadas.
     */
    public static PocketDoorEntity getInteriorDoor(ServerLevel level, ServerPlayer player, int ox, int oy, int oz) {
        AABB box = new AABB(ox - 8, oy - 4, oz - 8, ox + 8, oy + 8, oz + 8);
        List<PocketDoorEntity> doors = level.getEntitiesOfClass(PocketDoorEntity.class, box,
                d -> d.isSanctuarySide() && player.getUUID().equals(d.getOwnerUUID()));
        if (!doors.isEmpty()) {
            PocketDoorEntity main = doors.get(0);
            for (int i = 1; i < doors.size(); i++) {
                doors.get(i).discard();
            }
            return main;
        }
        PocketDoorEntity door = ModEntities.POCKET_DOOR.get().create(level);
        if (door != null) {
            door.setPos(ox + 0.5D, oy, oz - 1.0D);
            door.setYRot(0.0F); // Mirando hacia la pradera y bosques del santuario
            door.setOwner(player.getUUID());
            door.setSanctuarySide(true);
            door.setOpen(false);
            level.addFreshEntity(door);
            return door;
        }
        return null;
    }



    /**
     * Fronteras y Domo Celeste (Roadmap Sección 29.2):
     * Delimita el espacio según el nivel del Santuario e impide caídas al vacío.
     */
    public static void enforceSanctuaryBoundary(ServerPlayer player) {
        if (!isSanctuaryDimension(player.level())) return;
        SystemData data = SystemEvents.data(player);
        int sancLvl = Math.max(1, data.sanctuaryLevel());
        int radius = switch (sancLvl) {
            case 1 -> 64;
            case 2 -> 128;
            default -> 256;
        };

        int ox = getPlotOriginX(player);
        int oz = getPlotOriginZ(player);

        double dx = player.getX() - ox;
        double dz = player.getZ() - oz;
        double distSq = dx * dx + dz * dz;

        // Domo Celeste y Frontera Territorial
        if (distSq > (double) (radius * radius)) {
            double dist = Math.sqrt(distSq);
            double targetX = ox + (dx / dist) * (radius - 1.5D);
            double targetZ = oz + (dz / dist) * (radius - 1.5D);
            player.teleportTo(targetX, player.getY(), targetZ);
            player.setDeltaMovement(0.0, player.getDeltaMovement().y, 0.0);
            player.displayClientMessage(Component.literal("§e§l[DOMO CELESTE] §7Has alcanzado la frontera de tu territorio actual (Nivel " + sancLvl + ")."), true);
        }

        // Protección contra caída al vacío
        if (player.getY() < -50.0D) {
            int targetX = data.hasSanctuaryPlot() ? data.sanctuaryPlotX() : ox;
            int targetY = data.hasSanctuaryPlot() ? data.sanctuaryPlotY() : 80;
            int targetZ = data.hasSanctuaryPlot() ? data.sanctuaryPlotZ() : oz;
            player.teleportTo(targetX + 0.5D, targetY + 1.0D, targetZ + 0.5D);
            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.fallDistance = 0.0F;
            player.displayClientMessage(Component.literal("§a§l[SANTUARIO] §7El domo celestial te ha salvado de caer al vacío."), true);
        }
    }

    /**
     * Mejora del nivel de Santuario (desbloquea parcelas más amplias y estructuras).
     */
    public static boolean upgradeSanctuary(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        int currentLvl = data.sanctuaryLevel();
        if (currentLvl >= 3) {
            player.sendSystemMessage(Component.literal("§e[Santuario] §bNivel 3 (Pabellón Celestial)§e ya alcanzado. Es el nivel máximo de desarrollo actual."));
            return false;
        }

        int nextLvl = currentLvl + 1;
        int coinCost = switch (nextLvl) {
            case 2 -> 5000;
            case 3 -> 15000;
            default -> 30000;
        };

        if (data.coins() < coinCost) {
            player.sendSystemMessage(Component.literal("§c[Santuario] Requiere §6" + coinCost + " Monedas del Sistema§c para expandir el Santuario a Nivel " + nextLvl + ". (Tienes: " + data.coins() + ")"));
            return false;
        }

        data.addCoins(-coinCost);
        data.setSanctuaryLevel(nextLvl);

        MinecraftServer server = player.getServer();
        if (server != null) {
            ServerLevel sanctuaryLevel = getSanctuaryLevel(server);
            if (!data.hasSanctuaryPlot()) {
                int ox = getPlotOriginX(player);
                int oz = getPlotOriginZ(player);
                BlockPos surfacePos = findSafeSurfacePos(sanctuaryLevel, ox, oz);
                data.setSanctuaryPlotPos(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());
                ensureSanctuaryPlot(sanctuaryLevel, player, surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());
            }
            data.setSanctuaryBuiltLevel(nextLvl);
        }

        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.literal("§a§l[SANTUARIO EXPANDIDO] §fTu territorio ha alcanzado el §eNivel " + nextLvl + "§f. ¡Nuevas tierras y estructuras generadas!"));
        AwakeningSystemMod.sync(player);
        return true;
    }
}
