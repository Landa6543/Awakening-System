package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.io.InputStream;
import java.util.Optional;

public final class IsekaiWorldManager {
    private IsekaiWorldManager() {}

    public static final ResourceKey<Level> ISEKAI_WORLD = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "isekai_world")
    );

    private static volatile boolean worldTreeGenerated = false;

    public static void teleportToIsekai(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel isekaiLevel = server.getLevel(ISEKAI_WORLD);
        if (isekaiLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.tower.dimension_unavailable"), true);
            return;
        }

        if (TowerManager.isInTower(player.getUUID())) {
            TowerManager.exitTowerToDimension(player, isekaiLevel, new BlockPos(0, YggdrasilTreeGenerator.BASE_Y + 1, 26));
        }

        ensureWorldTree(isekaiLevel);

        player.teleportTo(isekaiLevel, 0.5D, YggdrasilTreeGenerator.BASE_Y + 1.0D, 26.0D, 180.0F, 0.0F);

        DungeonManager.syncRiftsToPlayer(player);

        SystemData data = SystemEvents.data(player);
        if (!data.systemUpdated() && !data.isSystemUpdating()) {
            data.setSystemUpdating(true);
            data.setSystemUpdateRemainingTicks(5 * 60 * 20);
            AwakeningSystemMod.sync(player);

            isekaiLevel.sendParticles(ParticleTypes.FLASH, 0.5D, YggdrasilTreeGenerator.BASE_Y + 2.0D, 26.0D, 6, 0, 0, 0, 0);
            isekaiLevel.sendParticles(ParticleTypes.END_ROD, 0.5D, YggdrasilTreeGenerator.BASE_Y + 1.5D, 26.0D, 120, 2.5D, 1.5D, 2.5D, 0.08D);
            isekaiLevel.sendParticles(ParticleTypes.PORTAL, 0.5D, YggdrasilTreeGenerator.BASE_Y + 1.5D, 26.0D, 80, 2.0D, 1.5D, 2.0D, 0.1D);

            player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.4F, 0.9F);
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.3F);

            AwakeningSystemMod.notify(player,
                    "awakening_system.tower.isekai_arrived_title",
                    "awakening_system.tower.isekai_arrived_desc",
                    0xFFFFD700);

            player.displayClientMessage(Component.translatable("awakening_system.hud.system_update_protocol_started"), true);
        } else {
            isekaiLevel.sendParticles(ParticleTypes.PORTAL, 0.5D, YggdrasilTreeGenerator.BASE_Y + 1.5D, 26.0D, 30, 1.0D, 0.5D, 1.0D, 0.05D);
            player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.dimension.isekai_welcome"), true);
        }
    }

    public static synchronized void ensureWorldTree(ServerLevel level) {
        ensureWorldTreeDirect(level, false);
    }

    public static synchronized void ensureWorldTreeDirect(ServerLevel level, boolean forceRebuild) {
        if (!forceRebuild && worldTreeGenerated) return;

        MinecraftServer server = level.getServer();
        if (server != null) {
            DimensionGenSavedData savedData = DimensionGenSavedData.get(server);
            if (!forceRebuild && savedData.isIsekaiTreeGenerated()) {
                worldTreeGenerated = true;
                return;
            }
        }

        BlockPos trunkCheck = new BlockPos(0, YggdrasilTreeGenerator.BASE_Y + 15, 3);
        if (!forceRebuild && (level.getBlockState(trunkCheck).is(Blocks.DARK_OAK_WOOD) || level.getBlockState(trunkCheck).is(Blocks.DARK_OAK_LOG))) {
            worldTreeGenerated = true;
            if (server != null) {
                DimensionGenSavedData.get(server).setIsekaiTreeGenerated(true);
            }
            ensureSanctuaryPortals(level);
            return;
        }

        for (int cx = -4; cx <= 4; cx++) {
            for (int cz = -4; cz <= 4; cz++) {
                level.getChunk(cx, cz);
            }
        }

        generateWorldTree(level);
        generateArrivalSanctuary(level, new BlockPos(0, YggdrasilTreeGenerator.BASE_Y, 26));
        worldTreeGenerated = true;
        if (server != null) {
            DimensionGenSavedData.get(server).setIsekaiTreeGenerated(true);
        }
    }

    public static void completeSystemUpdate(ServerPlayer player, SystemData data) {
        data.setSystemUpdating(false);
        data.setSystemUpdateRemainingTicks(0);
        data.setSystemUpdated(true);
        data.setIsekaiUnlocked(true);
        data.addCoins(145000);
        data.addExperience(25000);

        data.unlockSkill("isekai_predator");
        data.unlockSkill("isekai_dimensional_slash");
        data.unlockSkill("isekai_dragon_roar");
        data.unlockSkill("isekai_mana_burst");
        AwakeningSystemMod.sync(player);

        ServerLevel sl = player.serverLevel();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py + 1.5D, pz, 250, 2.5D, 2.0D, 2.5D, 0.4D);
        sl.sendParticles(ParticleTypes.FLASH, px, py + 1.5D, pz, 8, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.FIREWORK, px, py + 2.0D, pz, 80, 1.5D, 1.5D, 1.5D, 0.2D);

        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
        player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.0F);
        player.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.5F);

        AwakeningSystemMod.notify(player,
                "awakening_system.tower.system_update_complete_title",
                "awakening_system.tower.system_update_complete_desc",
                0xFFFFD700);

        AwakeningSystemMod.notify(player, "awakening_system.tower.reaction_dokja_title", "awakening_system.tower.reaction_dokja_msg", 0xFFFFD700);
        AwakeningSystemMod.notify(player, "awakening_system.tower.reaction_dragon_title", "awakening_system.tower.reaction_dragon_msg", 0xFFFF5555);
        AwakeningSystemMod.notify(player, "awakening_system.tower.reaction_wukong_title", "awakening_system.tower.reaction_wukong_msg", 0xFF50FA7B);
        AwakeningSystemMod.notify(player, "awakening_system.tower.reaction_persephone_title", "awakening_system.tower.reaction_persephone_msg", 0xFFBD93F9);
        AwakeningSystemMod.notify(player, "awakening_system.tower.reaction_plotter_title", "awakening_system.tower.reaction_plotter_msg", 0xFF8BE9FD);

        AwakeningSystemMod.notifyAll(player.getServer(),
                "awakening_system.isekai.universal_broadcast_title",
                "awakening_system.isekai.universal_broadcast_desc",
                0xFFFFD700, player.getName().getString());
    }

    public static void generateArrivalSanctuary(ServerLevel level, BlockPos plazaCenter) {
        int r = 6;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= r) {
                    BlockPos p = plazaCenter.offset(dx, 0, dz);
                    boolean border = dist >= r - 1.2D;
                    var mat = border ? Blocks.GILDED_BLACKSTONE : (dist <= 2 ? Blocks.CHISELED_QUARTZ_BLOCK : Blocks.QUARTZ_BRICKS);
                    level.setBlock(p, mat.defaultBlockState(), 2);
                    level.setBlock(p.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);

                    for (int h = 1; h <= 5; h++) {
                        BlockPos above = p.above(h);
                        if (!level.getBlockState(above).isAir()) {
                            level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 360; i += 45) {
            double rad = Math.toRadians(i);
            int px = (int) Math.round(Math.cos(rad) * 5.0D);
            int pz = (int) Math.round(Math.sin(rad) * 5.0D);
            level.setBlock(plazaCenter.offset(px, 1, pz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(plazaCenter.offset(px, 0, pz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        }

        buildOverworldReturnGate(level, YggdrasilTreeGenerator.OVERWORLD_RETURN_GATE_POS);
    }

    public static void ensureSanctuaryPortals(ServerLevel level) {
        BlockPos overworldGate = YggdrasilTreeGenerator.OVERWORLD_RETURN_GATE_POS;

        if (!level.getBlockState(overworldGate.offset(2, 2, 0)).is(Blocks.CHISELED_QUARTZ_BLOCK)) {
            buildOverworldReturnGate(level, overworldGate);
        }
    }

    public static void buildOverworldReturnGate(ServerLevel level, BlockPos gateBase) {
        int cx = gateBase.getX();
        int cy = gateBase.getY();
        int cz = gateBase.getZ();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 1; dy <= 8; dy++) {
                    BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floorPos = new BlockPos(cx + dx, cy, cz + dz);
                boolean edge = (Math.abs(dx) == 2 || Math.abs(dz) == 2);
                level.setBlock(floorPos, edge ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                level.setBlock(floorPos.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        for (int sign : new int[]{-2, 2}) {
            int px = cx + sign;
            level.setBlock(new BlockPos(px, cy + 1, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 2, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 3, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 4, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 5, cz), Blocks.QUARTZ_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int h = 1; h <= 4; h++) {
                BlockPos p = new BlockPos(cx + dx, cy + h, cz);
                if (!level.getBlockState(p).isAir()) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    public static void ensureSacredBiome(ServerLevel level) {
    }

    public static volatile boolean USE_PROCEDURAL_YGGDRASIL = true;

    public static void setUseProceduralYggdrasil(boolean procedural) {
        USE_PROCEDURAL_YGGDRASIL = procedural;
    }

    public static boolean isUseProceduralYggdrasil() {
        return USE_PROCEDURAL_YGGDRASIL;
    }

    public static void generateWorldTree(ServerLevel level) {
        if (USE_PROCEDURAL_YGGDRASIL) {
            YggdrasilTreeGenerator.generate(level, new BlockPos(0, YggdrasilTreeGenerator.BASE_Y, 0));
        } else {
            generateWorldTreeFromNBT(level);
        }
    }

    /**
     * Carga y coloca la estructura NBT oficial del Gran Arbol del Mundo,
     * generando un cimiento montanoso natural debajo para que quede perfectamente
     * anclado a tierra firme y no flote sobre agua ni vacio.
     */
    public static void generateWorldTreeFromNBT(ServerLevel level) {
        try {
            int treeRadius = 75;
            int blendRadius = 98;
            for (int dx = -blendRadius; dx <= blendRadius; dx++) {
                for (int dz = -blendRadius; dz <= blendRadius; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > blendRadius) continue;

                    int targetY;
                    if (dist <= treeRadius) {
                        targetY = 70;
                    } else {
                        float t = (float) (dist - treeRadius) / (blendRadius - treeRadius);
                        float smooth = (float) (1.0 - Math.sin(t * Math.PI * 0.5));
                        targetY = (int) Math.round(63 + (70 - 63) * smooth);
                    }

                    BlockPos surfPos = new BlockPos(dx, targetY, dz);
                    if (dist > treeRadius - 2) {
                        level.setBlock(surfPos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        for (int h = 1; h <= 8; h++) {
                            BlockPos above = surfPos.above(h);
                            if (!level.getBlockState(above).isAir()) {
                                level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }

                    for (int y = targetY - 1; y >= 38; y--) {
                        BlockPos bp = new BlockPos(dx, y, dz);
                        BlockState existing = level.getBlockState(bp);
                        if (existing.isAir() || existing.is(Blocks.WATER) || existing.is(Blocks.SEAGRASS) || existing.is(Blocks.TALL_SEAGRASS) || existing.is(Blocks.KELP) || existing.is(Blocks.KELP_PLANT)) {
                            if (y >= targetY - 3) {
                                level.setBlock(bp, Blocks.DIRT.defaultBlockState(), 2);
                            } else if (y >= targetY - 10) {
                                level.setBlock(bp, ((dx * 13 + dz * 29 + y) % 7 == 0) ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState(), 2);
                            } else {
                                level.setBlock(bp, Blocks.STONE.defaultBlockState(), 2);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }

            ResourceLocation templateId = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "world_tree");
            StructureTemplateManager stm = level.getStructureManager();
            Optional<StructureTemplate> opt = stm.get(templateId);

            StructureTemplate template = null;
            if (opt.isPresent()) {
                template = opt.get();
            } else {
                try (InputStream in = IsekaiWorldManager.class.getResourceAsStream("/data/awakening_system/structure/world_tree.nbt")) {
                    if (in != null) {
                        CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                        template = new StructureTemplate();
                        template.load(level.holderLookup(Registries.BLOCK), tag);
                    }
                }
            }

            if (template != null) {
                BlockPos origin = new BlockPos(-73, 70, -72);
                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setIgnoreEntities(false)
                        .setKnownShape(true)
                        .setMirror(Mirror.NONE)
                        .setRotation(Rotation.NONE);

                template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);
                AwakeningSystemMod.LOGGER.info("Gran Arbol del Mundo NBT colocado exitosamente sobre su base montanosa en {}", origin);
            } else {
                AwakeningSystemMod.LOGGER.error("No se pudo cargar la estructura world_tree.nbt para el Gran Arbol del Mundo.");
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al generar el Gran Arbol del Mundo desde NBT: ", e);
        }
    }
}
