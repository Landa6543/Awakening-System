package com.soybe.awakeningsystem.limbo;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.DimensionGenSavedData;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.entity.LimboDoorEntity;
import com.soybe.awakeningsystem.entity.LimboOphanimEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.io.InputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Gestor del Mundo y Dimensión del Limbo (The Limbo Dimension):
 * Purgatorio de vacío plano de oscuridad absoluta (piso de bloque void_black_path mate uniforme).
 * En la inmensidad oscura se encuentra la legendaria Puerta del Limbo que permite regresar al mundo mortal,
 * custodiada por Espectros del Limbo y Ángeles Ofanim.
 */
public final class LimboWorldManager {
    private LimboWorldManager() {}

    public static final ResourceKey<Level> LIMBO_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo")
    );

    public static final BlockPos SPAWN_POS = new BlockPos(0, 2, 0);

    /** Distancia monumental de las puertas cardinales respecto al spawn (800 bloques = 50 chunks). */
    public static final int DOOR_DISTANCE = 1600;

    /**
     * 4 Puertas Monumentales en los 4 Puntos Cardinales (Norte, Sur, Este y Oeste)
     * a 800 bloques de distancia del centro de spawn (1600 bloques entre puertas opuestas).
     * Garantiza una experiencia de travesía mística en la inmensidad del purgatorio oscuro.
     */
    public static final BlockPos[] CARDINAL_DOORS = new BlockPos[]{
            new BlockPos(0, 2, -DOOR_DISTANCE),  // NORTE (-Z)
            new BlockPos(0, 2, DOOR_DISTANCE),   // SUR (+Z)
            new BlockPos(DOOR_DISTANCE, 2, 0),   // ESTE (+X)
            new BlockPos(-DOOR_DISTANCE, 2, 0)   // OESTE (-X)
    };

    public static final float[] CARDINAL_DOOR_YAWS = new float[]{
            0.0F,   // NORTE (-Z): Mira hacia el Sur (0° / +Z), mirando de frente al jugador que viene del spawn
            180.0F, // SUR (+Z): Mira hacia el Norte (180° / -Z)
            90.0F,  // ESTE (+X): Mira hacia el Oeste (90° / -X)
            270.0F  // OESTE (-X): Mira hacia el Este (270° / +X)
    };

    public static final BlockPos DOOR_POS = CARDINAL_DOORS[0]; // Retrocompatibilidad
    private static volatile boolean worldInitialized = false;

    /**
     * Comprueba si un nivel corresponde a la dimensión del Limbo.
     */
    public static boolean isLimboDimension(Level level) {
        return level != null && level.dimension().equals(LIMBO_LEVEL);
    }

    /**
     * Teletransporta al jugador al plano de oscuridad del Limbo.
     */
    public static void teleportToLimbo(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel limboLevel = server.getLevel(LIMBO_LEVEL);
        if (limboLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.limbo.dimension_unavailable"), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
            return;
        }

        // Asegurar que el mundo y las 4 puertas cardinales estén generados
        ensureLimboWorld(limboLevel);

        if (!limboLevel.getBlockState(new BlockPos(0, 0, 0)).is(Blocks.GLOWSTONE) || !limboLevel.getBlockState(new BlockPos(0, 1, 0)).is(Blocks.WHITE_STAINED_GLASS)) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    for (int y = 2; y <= 12; y++) {
                        BlockPos p = new BlockPos(x, y, z);
                        if (!limboLevel.getBlockState(p).isAir()) {
                            limboLevel.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
            placeLimboSpawnStructure(limboLevel);
        }

        // Despejar monstruos en 160 bloques del spawn antes del teletransporte
        limboLevel.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, new net.minecraft.world.phys.AABB(SPAWN_POS).inflate(300.0D))
                .forEach(net.minecraft.world.entity.Entity::discard);

        // Resetear caídas, fuego y restaurar vitalidad
        player.fallDistance = 0.0F;
        player.clearFire();
        player.setHealth(player.getMaxHealth());

        // Teletransportar al suelo negro liso de Y=1
        player.teleportTo(limboLevel, 0.5D, SPAWN_POS.getY(), 0.5D, 0.0F, 0.0F);

        // Efecto sonoro cinematográfico del despertar del Limbo (volumen moderado)
        player.playNotifySound(com.soybe.awakeningsystem.ModSounds.LIMBO_AWAKENING.get(), SoundSource.AMBIENT, 0.55F, 1.0F);

        // Partículas sutiles de vacío y polvo espectral al despertar
        limboLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, 0.5D, 1.2D, 0.5D, 40, 1.0D, 0.5D, 1.0D, 0.02D);
        limboLevel.sendParticles(ParticleTypes.SOUL, 0.5D, 1.1D, 0.5D, 20, 0.8D, 0.3D, 0.8D, 0.01D);

        // Registro de dimensión visitada
        SystemData data = SystemEvents.data(player);
        if (data != null) {
            data.addVisitedDimension(LIMBO_LEVEL.location().toString());
            data.setMana(data.maxMana());
            AwakeningSystemMod.sync(player);
        }

        // Entrega mística de La Calabaza de Jack si el jugador no la posee
        boolean hasPumpkin = false;
        for (ItemStack item : player.getInventory().items) {
            if (!item.isEmpty() && item.is(SystemItems.JACK_PUMPKIN.get())) {
                hasPumpkin = true;
                break;
            }
        }
        if (!hasPumpkin && player.getOffhandItem().is(SystemItems.JACK_PUMPKIN.get())) {
            hasPumpkin = true;
        }

        if (!hasPumpkin) {
            ItemStack pumpkin = new ItemStack(SystemItems.JACK_PUMPKIN.get());
            if (!player.getInventory().add(pumpkin)) {
                player.drop(pumpkin, false);
            }
        }
    }

    /**
     * Regresa al jugador desde el Limbo hacia su punto de reaparición en el Overworld.
     */
    public static void returnFromLimbo(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        BlockPos targetPos = player.getRespawnPosition();
        if (targetPos == null) {
            targetPos = overworld.getSharedSpawnPos();
        }

        player.fallDistance = 0.0F;
        player.clearFire();
        player.teleportTo(overworld, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, player.getYRot(), 0.0F);

        overworld.sendParticles(ParticleTypes.PORTAL, targetPos.getX() + 0.5D, targetPos.getY() + 1.2D, targetPos.getZ() + 0.5D, 80, 1.2D, 1.2D, 1.2D, 0.08D);
        overworld.sendParticles(ParticleTypes.FLASH, targetPos.getX() + 0.5D, targetPos.getY() + 1.5D, targetPos.getZ() + 0.5D, 3, 0, 0, 0, 0);

        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.85F, 1.0F);
        player.displayClientMessage(Component.translatable("awakening_system.limbo.returned_to_overworld"), true);
    }

    /**
     * Garantiza el plano negro uniforme del Limbo y la presencia de la Puerta del Limbo.
     */
    public static synchronized void ensureLimboWorld(ServerLevel level) {
        if (worldInitialized) return;

        BlockState voidPath = SystemBlocks.VOID_BLACK_PATH.get().defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();

        // Limpiar cualquier puerta obsoleta de pruebas anteriores cerca del spawn (<= 1000 bloques)
        level.getEntitiesOfClass(LimboDoorEntity.class, new net.minecraft.world.phys.AABB(SPAWN_POS).inflate(1000.0D))
                .forEach(net.minecraft.world.entity.Entity::discard);

        // Despejar monstruos acumulados en el spawn para un aterrizaje inmersivo
        level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, new net.minecraft.world.phys.AABB(SPAWN_POS).inflate(300.0D))
                .forEach(net.minecraft.world.entity.Entity::discard);

        // 1. Asegurar suelo uniforme de voidPath y bedrock en radio de spawn (compatibilidad con mundos ya creados)
        for (int x = -96; x <= 96; x++) {
            for (int z = -96; z <= 96; z++) {
                level.setBlock(new BlockPos(x, 0, z), bedrock, 2);
                level.setBlock(new BlockPos(x, 1, z), voidPath, 2);
                for (int y = 2; y <= 12; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Colocar la estructura blanca de spawn del Limbo (limbo_spawn.nbt)
        placeLimboSpawnStructure(level);

        // 2. Colocar únicamente los postes de luz cada 200 bloques como balizas lejanas (sin camino continuo en el suelo)
        for (int i = 0; i < CARDINAL_DOORS.length; i++) {
            BlockPos targetPos = CARDINAL_DOORS[i];
            int tX = targetPos.getX();
            int tZ = targetPos.getZ();

            boolean isZAxis = (tX == 0);
            int stepSign = isZAxis ? (tZ > 0 ? 1 : -1) : (tX > 0 ? 1 : -1);
            int maxDist = Math.abs(isZAxis ? tZ : tX);

            for (int dist = 200; dist <= maxDist - 50; dist += 200) {
                int curX = isZAxis ? 0 : dist * stepSign;
                int curZ = isZAxis ? dist * stepSign : 0;

                // Suelo base bajo el poste
                level.setBlock(new BlockPos(curX, 0, curZ), bedrock, 2);
                level.setBlock(new BlockPos(curX, 1, curZ), voidPath, 2);

                // Farol Victoriano del Limbo de 5 bloques de altura completa (Secciones 0..4, Y=2..6)
                BlockPos postPos = new BlockPos(curX, 2, curZ);
                var lampBlock = com.soybe.awakeningsystem.block.SystemBlocks.LIMBO_VICTORIAN_LAMPPOST.get();
                for (int sec = 0; sec <= 4; sec++) {
                    level.setBlock(postPos.above(sec), lampBlock.defaultBlockState()
                            .setValue(com.soybe.awakeningsystem.block.LimboVictorianLamppostBlock.SECTION, sec)
                            .setValue(com.soybe.awakeningsystem.block.LimboVictorianLamppostBlock.LIT, true), 2);
                }

                // Piso uniforme alrededor del farol
                BlockPos r1 = isZAxis ? new BlockPos(curX + 1, 1, curZ) : new BlockPos(curX, 1, curZ + 1);
                BlockPos r2 = isZAxis ? new BlockPos(curX - 1, 1, curZ) : new BlockPos(curX, 1, curZ - 1);
                level.setBlock(r1, voidPath, 2);
                level.setBlock(r2, voidPath, 2);
            }

            // 3. Área de la Puerta Monumental (piso uniforme voidPath en radio 14)
            int dx = targetPos.getX();
            int dz = targetPos.getZ();
            for (int x = dx - 14; x <= dx + 14; x++) {
                for (int z = dz - 14; z <= dz + 14; z++) {
                    double distSq = (x - dx) * (x - dx) + (z - dz) * (z - dz);
                    if (distSq <= 196.0D) {
                        level.setBlock(new BlockPos(x, 0, z), bedrock, 2);
                        level.setBlock(new BlockPos(x, 1, z), voidPath, 2);
                        for (int y = 2; y <= 14; y++) {
                            BlockPos ap = new BlockPos(x, y, z);
                            if (!level.getBlockState(ap).isAir()) {
                                level.setBlock(ap, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

            // Spawnear entidad Puerta y Guardianes en esta puerta
            ensureDoorAt(level, i);
        }

        worldInitialized = true;
        MinecraftServer server = level.getServer();
        if (server != null) {
            DimensionGenSavedData savedData = DimensionGenSavedData.get(server);
            savedData.setLimboSanctuaryGenerated(true);
        }
    }

    /**
     * Verifica y genera la puerta y los guardianes en un índice cardinal específico (0..3).
     */
    public static void ensureDoorAt(ServerLevel level, int index) {
        if (index < 0 || index >= CARDINAL_DOORS.length) return;
        BlockPos doorPos = CARDINAL_DOORS[index];
        float yaw = CARDINAL_DOOR_YAWS[index];

        level.getChunk(doorPos.getX() >> 4, doorPos.getZ() >> 4);

        // Generar los dos imponentes pilares laterales de la Puerta del Limbo (Pillar Door)
        buildLimboDoorPillars(level, doorPos, index);

        AABB doorArea = new AABB(doorPos).inflate(16.0D);
        List<LimboDoorEntity> existingDoors = level.getEntitiesOfClass(LimboDoorEntity.class, doorArea);
        LimboDoorEntity door = existingDoors.isEmpty() ? null : existingDoors.get(0);
        if (door == null) {
            door = new LimboDoorEntity(ModEntities.LIMBO_DOOR.get(), level);
            door.setPos(doorPos.getX() + 0.5D, doorPos.getY(), doorPos.getZ() + 0.5D);
            door.setYRot(yaw);
            door.yRotO = yaw;
            level.addFreshEntity(door);
        } else {
            door.setYRot(yaw);
            door.yRotO = yaw;
        }

        // Guardianes Ofanim patrullando en la puerta:
        // Solo se generan los 2 guardianes iniciales si la puerta aún está sellada y no ha empezado el combate.
        // NUNCA reaparecen en bucle mientras el jugador está luchando contra ellos.
        if (!door.isUnlocked() && door.getGuardiansDefeated() == 0 && door.getRespawnCooldownTicks() <= 0) {
            List<LimboOphanimEntity> existingOphanim = level.getEntitiesOfClass(LimboOphanimEntity.class, doorArea.inflate(30.0D));
            if (existingOphanim.isEmpty()) {
                for (int s = 0; s < 2; s++) {
                    LimboOphanimEntity ophanim = new LimboOphanimEntity(ModEntities.LIMBO_OPHANIM.get(), level);
                    double ox = (s == 0 ? -12.0D : 12.0D);
                    double oz = (s == 0 ? -12.0D : 12.0D);
                    ophanim.setPos(doorPos.getX() + ox, 4.0D, doorPos.getZ() + oz);
                    setupGuardianAngel(ophanim, level);
                    level.addFreshEntity(ophanim);
                }
            }
        }
    }

    /**
     * Reaparece los 2 Ángeles Ofanim guardianes protegiendo una puerta del Limbo
     * cuando un jugador ha cruzado victorioso, preparando el desafío para futuros cazadores.
     */
    private static void setupGuardianAngel(LimboOphanimEntity ophanim, ServerLevel level) {
        ophanim.setCustomName(Component.literal("§6Ángel Guardián"));
        ophanim.setCustomNameVisible(true);
        // Escala orgánicamente con los cazadores en el Limbo sin exhibir un nivel numérico
        int maxHunterLevel = 1;
        for (ServerPlayer sp : level.players()) {
            var data = sp.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null && data.level() > maxHunterLevel) {
                maxHunterLevel = data.level();
            }
        }
        double scaledHp = 160.0D + maxHunterLevel * 12.0D;
        double scaledDmg = 14.0D + maxHunterLevel * 0.8D;
        double scaledArmor = Math.min(26.0D, 10.0D + maxHunterLevel * 0.25D);

        var hpAttr = ophanim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.setBaseValue(scaledHp);
            ophanim.setHealth((float) scaledHp);
        }
        var dmgAttr = ophanim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) dmgAttr.setBaseValue(scaledDmg);
        var armAttr = ophanim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        if (armAttr != null) armAttr.setBaseValue(scaledArmor);
    }

    public static void respawnGuardiansAt(ServerLevel level, BlockPos doorPos) {
        AABB doorArea = new AABB(doorPos).inflate(36.0D);
        List<LimboOphanimEntity> aliveOphanim = level.getEntitiesOfClass(LimboOphanimEntity.class, doorArea, net.minecraft.world.entity.Entity::isAlive);
        if (aliveOphanim.size() < 2) {
            int toSpawn = 2 - aliveOphanim.size();
            for (int s = 0; s < toSpawn; s++) {
                LimboOphanimEntity ophanim = new LimboOphanimEntity(ModEntities.LIMBO_OPHANIM.get(), level);
                double ox = (s == 0 ? -12.0D : 12.0D);
                double oz = (s == 0 ? -12.0D : 12.0D);
                ophanim.setPos(doorPos.getX() + ox, 6.0D, doorPos.getZ() + oz);
                setupGuardianAngel(ophanim, level);
                level.addFreshEntity(ophanim);
            }
        }
    }

    /**
     * Comprueba si el jugador está a menos de 96 bloques de alguna puerta cardinal,
     * asegurando que la puerta y sus guardianes estén presentes en dicho santuario.
     */
    public static void checkNearDoors(ServerLevel level, BlockPos playerPos) {
        for (int i = 0; i < CARDINAL_DOORS.length; i++) {
            if (playerPos.closerThan(CARDINAL_DOORS[i], 96.0D)) {
                ensureDoorAt(level, i);
                break;
            }
        }
    }

    /**
     * Garantiza que las 4 Puertas Monumentales y sus ángeles guardianes existan
     * en los 4 puntos cardinales.
     */
    public static void ensureCardinalDoorsExist(ServerLevel level) {
        for (int i = 0; i < CARDINAL_DOORS.length; i++) {
            ensureDoorAt(level, i);
        }
    }

    private static StructureTemplate pillarDoorTemplate = null;

    private static StructureTemplate getPillarDoorTemplate(ServerLevel level) {
        if (pillarDoorTemplate != null) return pillarDoorTemplate;
        try {
            ResourceLocation templateId = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "pillar_door");
            StructureTemplateManager stm = level.getStructureManager();
            Optional<StructureTemplate> opt = stm.get(templateId);
            if (opt.isPresent()) {
                pillarDoorTemplate = opt.get();
                return pillarDoorTemplate;
            }
            try (InputStream in = LimboWorldManager.class.getResourceAsStream("/data/awakening_system/structures/pillar_door.nbt")) {
                if (in != null) {
                    CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                    pillarDoorTemplate = new StructureTemplate();
                    pillarDoorTemplate.load(level.holderLookup(Registries.BLOCK), tag);
                    return pillarDoorTemplate;
                }
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al cargar pillar_door.nbt para las puertas del Limbo: ", e);
        }
        return null;
    }

    /**
     * Construye las dos torres monumentales (Pillar Door) flanqueando la Puerta del Limbo
     * utilizando la estructura auténtica creada en pillar_door.nbt.
     */
    public static void buildLimboDoorPillars(ServerLevel level, BlockPos doorPos, int index) {
        StructureTemplate template = getPillarDoorTemplate(level);
        if (template != null) {
            Vec3i size = template.getSize();
            BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
            Rotation rotation = switch (index) {
                case 0 -> Rotation.NONE;                  // NORTE (-Z)
                case 1 -> Rotation.CLOCKWISE_180;         // SUR (+Z)
                case 2 -> Rotation.CLOCKWISE_90;          // ESTE (+X)
                case 3 -> Rotation.COUNTERCLOCKWISE_90;   // OESTE (-X)
                default -> Rotation.NONE;
            };

            BlockPos origin = doorPos.offset(-pivot.getX(), 0, -pivot.getZ());
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rotation)
                    .setRotationPivot(pivot)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(false)
                    .setKnownShape(true);

            template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);
            AwakeningSystemMod.LOGGER.info("Pilares monumentales de la Puerta del Limbo generados en puerta " + index + " (" + doorPos + ")");
        }
    }


    /**
     * Coloca la estructura blanca de reaparición del Limbo (limbo_spawn.nbt) centrada en (0, 1, 0).
     */
    public static void placeLimboSpawnStructure(ServerLevel level) {
        try {
            StructureTemplate template = null;
            try (InputStream in = LimboWorldManager.class.getResourceAsStream("/data/awakening_system/structures/limbo_spawn.nbt")) {
                if (in != null) {
                    CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                    template = new StructureTemplate();
                    template.load(level.holderLookup(Registries.BLOCK), tag);
                }
            }
            if (template == null) {
                try (InputStream in = LimboWorldManager.class.getResourceAsStream("/data/awakening_system/structure/limbo_spawn.nbt")) {
                    if (in != null) {
                        CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                        template = new StructureTemplate();
                        template.load(level.holderLookup(Registries.BLOCK), tag);
                    }
                }
            }
            if (template == null) {
                ResourceLocation templateId = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_spawn");
                StructureTemplateManager stm = level.getStructureManager();
                Optional<StructureTemplate> opt = stm.get(templateId);
                if (opt.isPresent()) {
                    template = opt.get();
                }
            }

            if (template != null) {
                BlockPos origin = new BlockPos(-5, 0, -5);
                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setIgnoreEntities(false)
                        .setKnownShape(true)
                        .setMirror(Mirror.NONE)
                        .setRotation(Rotation.NONE);

                template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);
                AwakeningSystemMod.LOGGER.info("Estructura de spawn del Limbo colocada exitosamente en {}", origin);
            } else {
                AwakeningSystemMod.LOGGER.warn("No se pudo cargar la estructura limbo_spawn.nbt para el spawn del Limbo.");
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al colocar la estructura limbo_spawn.nbt: ", e);
        }
    }

}
