package com.soybe.awakeningsystem;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class AwakeningSystemClient {
    public static final KeyMapping OPEN_SYSTEM = new KeyMapping("key.awakening_system.open_system", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.awakening_system");
    public static final KeyMapping OPEN_GACHA = new KeyMapping("key.awakening_system.open_gacha", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F4, "key.categories.awakening_system");
    public static final KeyMapping ACTIVATE_SKILL = new KeyMapping("key.awakening_system.activate_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.awakening_system");
    public static final KeyMapping SKILL_SLOT_2 = new KeyMapping("key.awakening_system.skill_slot_2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.awakening_system");
    public static final KeyMapping SKILL_SLOT_3 = new KeyMapping("key.awakening_system.skill_slot_3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.awakening_system");
    public static final KeyMapping SKILL_SLOT_4 = new KeyMapping("key.awakening_system.skill_slot_4", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.awakening_system");
    public static final KeyMapping SKILL_SLOT_5 = new KeyMapping("key.awakening_system.skill_slot_5", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.awakening_system");
    public static final KeyMapping ACTIVATE_STIGMA = new KeyMapping("key.awakening_system.activate_stigma", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.awakening_system");
    public static final KeyMapping CULTIVATE_QI = new KeyMapping("key.awakening_system.cultivate_qi", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.awakening_system");
    public static final KeyMapping ACTIVATE_ARMOR_SKILL = new KeyMapping("key.awakening_system.armor_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.awakening_system");
    public static final KeyMapping PHYSIQUE_SKILL = new KeyMapping("key.awakening_system.physique_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.awakening_system");
    public static final KeyMapping DAO_SKILL = new KeyMapping("key.awakening_system.dao_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.awakening_system");
    public static final KeyMapping WEAPON_SKILL = new KeyMapping("key.awakening_system.weapon_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.awakening_system");
    public static final KeyMapping OPEN_GUILD = new KeyMapping("key.awakening_system.open_guild", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, "key.categories.awakening_system");
    public static final java.util.List<LeaderboardPayload.LeaderboardEntry> LEADERBOARD = new java.util.ArrayList<>();
    public static final java.util.List<SyncRiftsPayload.RiftEntry> ACTIVE_RIFTS = new java.util.concurrent.CopyOnWriteArrayList<>();
    public static final net.minecraft.resources.ResourceLocation GATE_BLUE_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_blue.png");
    public static final net.minecraft.resources.ResourceLocation GATE_RED_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_red.png");
    public static final net.minecraft.resources.ResourceLocation GATE_CYAN_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_cyan.png");
    public static final net.minecraft.resources.ResourceLocation GATE_GREEN_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_green.png");
    public static final net.minecraft.resources.ResourceLocation GATE_AMBER_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_amber.png");
    public static final net.minecraft.resources.ResourceLocation GATE_CRIMSON_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_crimson.png");
    public static final net.minecraft.resources.ResourceLocation GATE_PURPLE_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_purple.png");
    public static final net.minecraft.resources.ResourceLocation GATE_GOLD_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_gold.png");
    public static final net.minecraft.resources.ResourceLocation GATE_ISEKAI_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_isekai.png");
    public static final net.minecraft.resources.ResourceLocation GATE_MURIM_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/gate_murim.png");
    public static final net.minecraft.resources.ResourceLocation MAGIC_CIRCLE_ARCANE = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/magic/magic_circle_arcane.png");
    public static final net.minecraft.resources.ResourceLocation MAGIC_CIRCLE_MURIM = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/magic/magic_circle_murim.png");
    public static final net.minecraft.resources.ResourceLocation CELESTIA_TEX = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/environment/celestia_sky.png");

    public static void registerDimensionSpecialEffects(net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "teyvat_world"),
                new com.soybe.awakeningsystem.client.renderer.TeyvatSpecialEffects()
        );
    }

    public static final class ActiveMagicCircle {
        public final int casterEntityId;
        public double x, y, z;
        public float lookX, lookY, lookZ;
        public final float maxRadius;
        public final int maxTicks;
        public final int color;
        public final int circleType; // 0=ground arcane, 1=vertical arcane, 2=ground murim, 3=vertical murim
        public final boolean isCharging;
        public int currentTick = 0;
        public final float spinSpeed;

        public ActiveMagicCircle(int casterEntityId, double x, double y, double z, float lookX, float lookY, float lookZ, float maxRadius, int maxTicks, int color, int circleType, boolean isCharging) {
            this.casterEntityId = casterEntityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.lookX = lookX;
            this.lookY = lookY;
            this.lookZ = lookZ;
            this.maxRadius = maxRadius;
            this.maxTicks = maxTicks;
            this.color = color;
            this.circleType = circleType;
            this.isCharging = isCharging;
            this.spinSpeed = (circleType == 1 || circleType == 3) ? 2.8F : 1.8F;
        }

        public boolean isFinished() {
            return currentTick >= maxTicks;
        }
    }
    public static final java.util.List<ActiveMagicCircle> ACTIVE_MAGIC_CIRCLES = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void spawnMagicCircle(SpawnMagicCirclePayload payload) {
        ACTIVE_MAGIC_CIRCLES.add(new ActiveMagicCircle(
                payload.casterEntityId(),
                payload.x(), payload.y(), payload.z(),
                payload.lookX(), payload.lookY(), payload.lookZ(),
                payload.radius(), payload.durationTicks(), payload.color(),
                payload.circleType(), payload.isCharging()
        ));
    }

    public static void cancelMagicCircle(CancelMagicCirclePayload payload) {
        if (payload.casterEntityId() > 0) {
            ACTIVE_MAGIC_CIRCLES.removeIf(c -> c.casterEntityId == payload.casterEntityId() && c.isCharging);
        }
    }
    private static String notificationTitle;
    private static String notificationMessage;
    private static int notificationColor;
    private static int notificationTicks;

    // Sincronización de Emergencia y Resplandor de Sombras
    public record ShadowEmergenceData(long startTimeMs, long durationMs, double x, double y, double z, float height) {}
    public static final java.util.Set<Integer> SHADOW_SOLDIER_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public static final java.util.Set<Integer> NON_SHADOW_ENTITY_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public static final java.util.Set<Integer> CONFIGURED_SHADOW_TEAM_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public static final java.util.Map<Integer, ShadowEmergenceData> SHADOW_EMERGENCE_MAP = new java.util.concurrent.ConcurrentHashMap<>();

    public static void triggerShadowEmergence(int entityId, double x, double y, double z, int durationTicks, float height) {
        SHADOW_SOLDIER_IDS.add(entityId);
        long durationMs = (long) durationTicks * 50L;
        SHADOW_EMERGENCE_MAP.put(entityId, new ShadowEmergenceData(System.currentTimeMillis(), durationMs, x, y, z, height));

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.playLocalSound(x, y, z, net.minecraft.sounds.SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.PLAYERS, 0.85F, 1.35F, false);
            mc.level.playLocalSound(x, y, z, net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.1F, 0.7F, false);
            mc.level.playLocalSound(x, y, z, net.minecraft.sounds.SoundEvents.WARDEN_ROAR, net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.4F, false);
        }
    }

    // Estado de Cinemática del Dragón Celestial Ancestral
    private static boolean bossCinematicActive = false;
    private static int bossCinematicTicks = 0;
    private static int bossCinematicTotalTicks = 0;
    private static BlockPos bossCinematicAltarPos = BlockPos.ZERO;
    private static int bossCinematicDragonId = -1;
    private static float cinematicCurrentYaw = 0.0F;
    private static float cinematicCurrentPitch = 0.0F;
    private static boolean cinematicAnglesInitialized = false;

    public static void triggerBossCinematic(BlockPos altarPos, int durationTicks, int dragonEntityId) {
        bossCinematicActive = true;
        bossCinematicTicks = durationTicks;
        bossCinematicTotalTicks = durationTicks;
        bossCinematicAltarPos = altarPos;
        bossCinematicDragonId = dragonEntityId;
        cinematicAnglesInitialized = false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.5F, 0.7F);
        }
    }

    private AwakeningSystemClient() {}

    public static net.minecraft.resources.ResourceLocation getGateTexture(SyncRiftsPayload.RiftEntry rift) {
        if ("TEYVAT_ABYSS_BLOCKED".equalsIgnoreCase(rift.rank()) || rift.type() == 96) return GATE_CRIMSON_TEX;
        if ("TEYVAT_ABYSS".equalsIgnoreCase(rift.rank()) || rift.type() == 97) return GATE_CYAN_TEX;
        if ("MURIM".equalsIgnoreCase(rift.rank()) || rift.type() == 98) return GATE_MURIM_TEX;
        if ("ISEKAI".equalsIgnoreCase(rift.rank()) || rift.type() == 99) return GATE_ISEKAI_TEX;
        if (rift.isRedGate()) return GATE_RED_TEX;
        String rk = rift.rank() != null ? rift.rank().toUpperCase() : "E";
        if (rk.contains("GOD") || rk.contains("CREATOR") || rk.contains("ABSOLUTE") || rk.contains("LIMITLESS") || rk.contains("SINGULARITY") || rk.contains("NATIONAL") || rk.contains("RULER") || rk.contains("MONARCH") || rk.contains("SOVEREIGN") || rk.contains("ASTRAL") || rk.contains("DEITY") || rk.contains("COSMIC") || rk.contains("TRANSCENDENT") || rk.contains("OMNIPOTENT") || rk.contains("INFINITE") || rk.contains("OMNIVERSAL") || rk.contains("MULTIVERSAL") || rk.contains("PRIMORDIAL")) {
            return GATE_GOLD_TEX;
        }
        if (rk.contains("SSS") || rk.contains("SS") || rk.contains("UR") || rk.contains("LR") || rk.contains("MR") || rk.contains("DEMIGOD")) {
            return GATE_PURPLE_TEX;
        }
        if (rk.contains("S") || rk.contains("EX")) {
            return GATE_CRIMSON_TEX;
        }
        if (rk.contains("A")) {
            return GATE_AMBER_TEX;
        }
        if (rk.contains("B") || rk.contains("C")) {
            return GATE_GREEN_TEX;
        }
        if (rk.contains("D")) {
            return GATE_BLUE_TEX;
        }
        return GATE_CYAN_TEX;
    }

    public static float getGateSize(SyncRiftsPayload.RiftEntry rift) {
        if (rift.isRedGate()) return 5.8F;
        String rk = rift.rank() != null ? rift.rank().toUpperCase(java.util.Locale.ROOT) : "E";
        if (rk.contains("GOD") || rk.contains("CREATOR") || rk.contains("ABSOLUTE") || rk.contains("LIMITLESS") || rk.contains("SINGULARITY") || rk.contains("NATIONAL") || rk.contains("RULER") || rk.contains("MONARCH") || rk.contains("SOVEREIGN") || rk.contains("ASTRAL") || rk.contains("DEITY") || rk.contains("COSMIC") || rk.contains("TRANSCENDENT") || rk.contains("OMNIPOTENT") || rk.contains("INFINITE") || rk.contains("OMNIVERSAL") || rk.contains("MULTIVERSAL") || rk.contains("PRIMORDIAL")) {
            return 6.5F;
        }
        if (rk.contains("SSS") || rk.contains("SS") || rk.contains("UR") || rk.contains("LR") || rk.contains("MR") || rk.contains("DEMIGOD")) {
            return 5.6F;
        }
        if (rk.contains("S") || rk.contains("EX")) {
            return 5.2F;
        }
        if (rk.contains("A")) {
            return 4.6F;
        }
        if (rk.contains("B") || rk.contains("C")) {
            return 4.0F;
        }
        return 3.5F;
    }

    public static void setLeaderboard(java.util.List<LeaderboardPayload.LeaderboardEntry> entries) {
        LEADERBOARD.clear();
        LEADERBOARD.addAll(entries);
    }

    public static void syncRifts(java.util.List<SyncRiftsPayload.RiftEntry> rifts) {
        ACTIVE_RIFTS.clear();
        ACTIVE_RIFTS.addAll(rifts);
    }

    public static void requestLeaderboard() {
        PacketDistributor.sendToServer(new RequestLeaderboardPayload());
    }

    public static void openClassSelectionScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new ClassSelectionScreen(mc.screen));
        }
    }

    public static com.soybe.awakeningsystem.guild.Guild cachedClientGuild = null;
    public static net.minecraft.nbt.CompoundTag cachedClientClaims = new net.minecraft.nbt.CompoundTag();

    public static void openGuildScreen(net.minecraft.nbt.CompoundTag data) {
        if (data != null) {
            if (data.getBoolean("InGuild") && data.contains("Guild")) {
                cachedClientGuild = com.soybe.awakeningsystem.guild.Guild.load(data.getCompound("Guild"));
            } else {
                cachedClientGuild = null;
            }
            if (data.contains("AllClaims")) {
                cachedClientClaims = data.getCompound("AllClaims");
            }
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof com.soybe.awakeningsystem.guild.GuildChunkMapScreen mapScreen) {
            mapScreen.updateData(cachedClientGuild, cachedClientClaims);
            return;
        }
        if (mc.player != null) {
            mc.setScreen(new com.soybe.awakeningsystem.guild.GuildScreen(data));
        }
    }

    public static void requestOpenGuild() {
        PacketDistributor.sendToServer(new com.soybe.awakeningsystem.guild.GuildActionPayload("OPEN", "", 0));
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SYSTEM);
        event.register(OPEN_GACHA);
        event.register(ACTIVATE_SKILL);
        event.register(SKILL_SLOT_2);
        event.register(SKILL_SLOT_3);
        event.register(SKILL_SLOT_4);
        event.register(SKILL_SLOT_5);
        event.register(ACTIVATE_STIGMA);
        event.register(CULTIVATE_QI);
        event.register(ACTIVATE_ARMOR_SKILL);
        event.register(PHYSIQUE_SKILL);
        event.register(DAO_SKILL);
        event.register(WEAPON_SKILL);
        event.register(OPEN_GUILD);
    }

    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.ABYSSAL_FURNACE_MENU.get(), com.soybe.awakeningsystem.furnace.CustomTierFurnaceScreen::new);
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.MITHRIL_FURNACE_MENU.get(), com.soybe.awakeningsystem.furnace.CustomTierFurnaceScreen::new);
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.ADAMANTITE_FURNACE_MENU.get(), com.soybe.awakeningsystem.furnace.CustomTierFurnaceScreen::new);
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.SPIRIT_JADE_FURNACE_MENU.get(), com.soybe.awakeningsystem.furnace.CustomTierFurnaceScreen::new);
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.ASTRAL_FURNACE_MENU.get(), com.soybe.awakeningsystem.furnace.CustomTierFurnaceScreen::new);
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.SUBSPACE_MENU.get(), com.soybe.awakeningsystem.subspace.SubspaceScreen::new);
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.POUCH_MENU.get(), com.soybe.awakeningsystem.item.pouch.PouchScreen::new);
        event.register(com.soybe.awakeningsystem.furnace.ModMenus.FISHING_TRAP_MENU.get(), com.soybe.awakeningsystem.block.entity.FishingTrapScreen::new);
    }


    public static void registerEntityRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SHADOW_INFANTRY.get(), com.soybe.awakeningsystem.entity.client.ShadowInfantryRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_ORC.get(), com.soybe.awakeningsystem.entity.client.AbyssalOrcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SHADOW_MONARCH_BOSS.get(), com.soybe.awakeningsystem.entity.client.ShadowMonarchBossRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_NECROMANCER.get(), com.soybe.awakeningsystem.entity.client.AbyssalNecromancerRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.FROST_ELF_ASSASSIN.get(), com.soybe.awakeningsystem.entity.client.FrostElfAssassinRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SHADOW_SOUL.get(), com.soybe.awakeningsystem.entity.client.ShadowSoulRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MEDITATION_SEAT.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.TAVERN_SEAT.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.FLYING_SWORD.get(), com.soybe.awakeningsystem.entity.client.FlyingSwordRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SUBWAY_PILOT_SEAT.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SUBWAY_TRAIN_CARRIAGE.get(), com.soybe.awakeningsystem.entity.client.SubwayTrainCarriageRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.AWAKENING_COIN_PROJECTILE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RUIN_GUARD.get(), com.soybe.awakeningsystem.entity.client.RuinGuardRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RUIN_HUNTER.get(), com.soybe.awakeningsystem.entity.client.RuinHunterRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RUIN_MISSILE.get(), com.soybe.awakeningsystem.entity.client.RuinMissileRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.STONEHIDE_LAWACHURL.get(), com.soybe.awakeningsystem.entity.client.StonehideLawachurlRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CRYO_REGISVINE.get(), com.soybe.awakeningsystem.entity.client.CryoRegisvineRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.PYRO_REGISVINE.get(), com.soybe.awakeningsystem.entity.client.PyroRegisvineRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_REGISVINE.get(), com.soybe.awakeningsystem.entity.client.ElectroRegisvineRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.HOARFROST_SHARD.get(), com.soybe.awakeningsystem.entity.client.HoarfrostShardRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CHILDE_BOSS.get(), com.soybe.awakeningsystem.entity.client.ChildeBossRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.AZHDAHA_BOSS.get(), com.soybe.awakeningsystem.entity.client.AzhdahaRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RAIDEN_SHOGUN_BOSS.get(), com.soybe.awakeningsystem.entity.client.RaidenBossRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GOLDEN_WOLFLORD_BOSS.get(), com.soybe.awakeningsystem.entity.client.GoldenWolflordRenderer::new);

        // Renderers de NPCs de Series Manhwa
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SUNG_JINWOO.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CHA_HAEIN.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.KIM_DOKJA.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.YOO_JOONGHYUK.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.LLOYD_FRONTERA.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.JAVIER_ASRAHAN.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CHEON_YEOWOON.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ARTHUR_LEYWIN.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CHA_YEONWOO.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CHUNG_MYUNG.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RYU_HANBIN.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.TWENTY_FIFTH_BAAM.get(), com.soybe.awakeningsystem.entity.client.SeriesNpcRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.DOKKAEBI.get(), com.soybe.awakeningsystem.entity.client.DokkaebiRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.PAIMON.get(), com.soybe.awakeningsystem.entity.client.PaimonRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.COMPANION.get(), com.soybe.awakeningsystem.entity.client.CompanionRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.WYVERN.get(), com.soybe.awakeningsystem.entity.client.WyvernRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.IGRIS_BOSS.get(), com.soybe.awakeningsystem.entity.client.IgrisBossRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.BARAN_BOSS.get(), com.soybe.awakeningsystem.entity.client.BaranBossRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ISEKAI_SLIME.get(), com.soybe.awakeningsystem.entity.client.IsekaiSlimeRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), com.soybe.awakeningsystem.entity.client.SpiritFoxRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ASTRAL_DRAGON.get(), com.soybe.awakeningsystem.entity.client.AstralDragonRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ASTRAL_BREATH.get(), com.soybe.awakeningsystem.entity.client.AstralBreathRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SHADOW_DIREWOLF.get(), com.soybe.awakeningsystem.entity.client.ShadowDirewolfRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.WHITE_THUNDER_TIGER.get(), com.soybe.awakeningsystem.entity.client.WhiteThunderTigerRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), com.soybe.awakeningsystem.entity.client.CloudCraneRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ANT_SOLDIER.get(), com.soybe.awakeningsystem.entity.client.AntSoldierRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_BEHOLDER.get(), com.soybe.awakeningsystem.entity.client.AbyssalBeholderRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MANA_PIXIE.get(), com.soybe.awakeningsystem.entity.client.ManaPixieRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SCREAMING_MANDRAKE.get(), com.soybe.awakeningsystem.entity.client.ScreamingMandrakeRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CELESTIAL_DRAGON.get(), com.soybe.awakeningsystem.entity.client.CelestialDragonRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_CELESTIAL_DRAGON.get(), com.soybe.awakeningsystem.entity.client.AncientCelestialDragonBossRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CELESTIAL_DRAGON_BREATH.get(), com.soybe.awakeningsystem.entity.client.CelestialDragonBreathRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.BLOOD_QI_TOTEM.get(), com.soybe.awakeningsystem.entity.client.BloodQiTotemRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.BLOOD_SECT_DISCIPLE.get(), com.soybe.awakeningsystem.entity.client.BloodSectDiscipleRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS.get(), com.soybe.awakeningsystem.entity.client.BloodPatriarchRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS.get(), com.soybe.awakeningsystem.entity.client.YggdrasilTreantRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.JADE_GUARDIAN_BOSS.get(), com.soybe.awakeningsystem.entity.client.JadeGuardianRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_DEMON_KING.get(), com.soybe.awakeningsystem.entity.client.ElementalDemonKingRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_LORD.get(), com.soybe.awakeningsystem.entity.client.ElementalLordRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_SCOUT.get(), com.soybe.awakeningsystem.entity.client.GoblinScoutRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_WARRIOR.get(), com.soybe.awakeningsystem.entity.client.GoblinWarriorRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_SHAMAN.get(), com.soybe.awakeningsystem.entity.client.GoblinShamanRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.BEASTMAN_WARRIOR.get(), com.soybe.awakeningsystem.entity.client.BeastmanWarriorRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.JIANGSHI.get(), com.soybe.awakeningsystem.entity.client.JiangshiRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.DEMONIC_CULTIVATOR.get(), com.soybe.awakeningsystem.entity.client.DemonicCultivatorRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.DEMONIC_SWORD_QI.get(), com.soybe.awakeningsystem.entity.client.DemonicSwordQiRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.JADE_FEIJIAN.get(), com.soybe.awakeningsystem.entity.client.JadeFeijianRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ASTRAL_DRAGON_CRESCENT.get(), com.soybe.awakeningsystem.entity.client.AstralDragonCrescentRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.JADE_ARMORED_BEAST.get(), com.soybe.awakeningsystem.entity.client.JadeArmoredBeastRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MOUNT_HUA_DISCIPLE.get(), com.soybe.awakeningsystem.entity.client.MountHuaDiscipleRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MANA_COW.get(), com.soybe.awakeningsystem.entity.client.ManaCowRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MANA_PIG.get(), com.soybe.awakeningsystem.entity.client.ManaPigRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MANA_SHEEP.get(), com.soybe.awakeningsystem.entity.client.ManaSheepRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MANA_CHICKEN.get(), com.soybe.awakeningsystem.entity.client.ManaChickenRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON.get(), com.soybe.awakeningsystem.entity.client.AncientDragonRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON_BREATH.get(), com.soybe.awakeningsystem.entity.client.AncientDragonBreathRenderer::new);

        // Mobs Canónicos de Genshin Impact
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GENSHIN_SLIME.get(), com.soybe.awakeningsystem.entity.client.GenshinSlimeRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.HILICHURL.get(), com.soybe.awakeningsystem.entity.client.HilichurlRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.MITACHURL.get(), com.soybe.awakeningsystem.entity.client.MitachurlRenderer::new);

        // Hipóstasis Canónicos de Genshin Impact
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ANEMO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.client.AnemoHypostasisRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.client.ElectroHypostasisRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GEO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.client.GeoHypostasisRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.CRYO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.client.CryoHypostasisRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.PYRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.client.PyroHypostasisRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.HYDRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.client.HydroHypostasisRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.DENDRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.client.DendroHypostasisRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ANEMO_REVIVAL_ORB.get(), com.soybe.awakeningsystem.entity.hypostasis.client.AnemoRevivalOrbRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_REVIVAL_PRISM.get(), com.soybe.awakeningsystem.entity.hypostasis.client.ElectroRevivalPrismRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.DENDRO_REVIVAL_PITH.get(), com.soybe.awakeningsystem.entity.hypostasis.client.DendroRevivalPithRenderer::new);

        // Elementos Emblemáticos del Abismo de Genshin Impact
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.LEY_LINE_MONOLITH.get(), com.soybe.awakeningsystem.entity.client.LeyLineMonolithRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSS_CHALLENGE_KEY.get(), com.soybe.awakeningsystem.entity.client.AbyssChallengeKeyRenderer::new);

        // Nuevos Mobs y Jefes
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.LUNAR_SHADOW.get(), com.soybe.awakeningsystem.entity.client.LunarShadowRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.LUNAR_GUARDIAN.get(), com.soybe.awakeningsystem.entity.client.LunarGuardianRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.WILDFIRE.get(), com.soybe.awakeningsystem.entity.client.WildfireRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GLARE.get(), com.soybe.awakeningsystem.entity.client.GlareRenderer::new);

        // Magos del Abismo de Teyvat y Burbuja Aprisionadora
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_PYRO.get(), com.soybe.awakeningsystem.entity.client.AbyssMageRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_HYDRO.get(), com.soybe.awakeningsystem.entity.client.AbyssMageRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_CRYO.get(), com.soybe.awakeningsystem.entity.client.AbyssMageRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_ELECTRO.get(), com.soybe.awakeningsystem.entity.client.AbyssMageRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.ABYSS_BUBBLE.get(), com.soybe.awakeningsystem.entity.client.AbyssBubbleRenderer::new);

        // Nuevas Criaturas y Jefes de Genshin Impact (Árbol 407 Criaturas)
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GREAT_SNOWBOAR_KING.get(), com.soybe.awakeningsystem.entity.client.GreatSnowboarKingRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RHODEIA_OCEANID.get(), com.soybe.awakeningsystem.entity.client.RhodeiaOceanidRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.WATER_MIMIC.get(), com.soybe.awakeningsystem.entity.client.WaterMimicRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.PERPETUAL_MECHANICAL_ARRAY.get(), com.soybe.awakeningsystem.entity.client.PerpetualMechanicalArrayRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RUIN_SENTINEL.get(), com.soybe.awakeningsystem.entity.client.RuinSentinelRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RUIN_GRADER.get(), com.soybe.awakeningsystem.entity.client.RuinGraderRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RUIN_DRAKE.get(), com.soybe.awakeningsystem.entity.client.RuinDrakeRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.RUIN_SERPENT.get(), com.soybe.awakeningsystem.entity.client.RuinSerpentRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.FROSTARM_LAWACHURL.get(), com.soybe.awakeningsystem.entity.client.FrostarmLawachurlRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.THUNDERHELM_LAWACHURL.get(), com.soybe.awakeningsystem.entity.client.ThunderhelmLawachurlRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.HILICHURL_SHOOTER.get(), com.soybe.awakeningsystem.entity.client.HilichurlRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.HILICHURL_BERSERKER.get(), com.soybe.awakeningsystem.entity.client.HilichurlRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.GEOVISHAP.get(), com.soybe.awakeningsystem.entity.client.GeovishapRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.BATHYSMAL_VISHAP.get(), com.soybe.awakeningsystem.entity.client.BathysmalVishapRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.SPECTER.get(), com.soybe.awakeningsystem.entity.client.SpecterRenderer::new);
        event.registerEntityRenderer(com.soybe.awakeningsystem.entity.ModEntities.WHOPPERFLOWER.get(), com.soybe.awakeningsystem.entity.client.WhopperflowerRenderer::new);

        event.registerBlockEntityRenderer(com.soybe.awakeningsystem.block.entity.ModBlockEntities.ROULETTE_ALTAR.get(), com.soybe.awakeningsystem.client.renderer.RouletteAltarRenderer::new);
        event.registerBlockEntityRenderer(com.soybe.awakeningsystem.block.entity.ModBlockEntities.LOCKED_TREASURE_CHEST.get(), com.soybe.awakeningsystem.client.renderer.LockedTreasureChestRenderer::new);
        event.registerBlockEntityRenderer(com.soybe.awakeningsystem.block.entity.ModBlockEntities.DRAGON_NEST.get(), com.soybe.awakeningsystem.client.renderer.DragonNestRenderer::new);
        event.registerBlockEntityRenderer(com.soybe.awakeningsystem.block.entity.ModBlockEntities.ANCIENT_DRAGON_EGG.get(), com.soybe.awakeningsystem.client.renderer.AncientDragonEggRenderer::new);
        event.registerBlockEntityRenderer(com.soybe.awakeningsystem.block.entity.ModBlockEntities.PLUSHIE.get(), com.soybe.awakeningsystem.client.renderer.PlushieBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.soybe.awakeningsystem.block.entity.ModBlockEntities.SUBWAY_DOOR.get(), com.soybe.awakeningsystem.client.renderer.SubwayDoorRenderer::new);
    }

    public static void registerLayerDefinitions(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.StonehideLawachurlRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.StonehideLawachurlModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.CryoRegisvineRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.CryoRegisvineModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.PyroRegisvineRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.PyroRegisvineModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ElectroRegisvineRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ElectroRegisvineModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AzhdahaModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AzhdahaModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ChildePhase2Model.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ChildePhase2Model::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RaidenShogunModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RaidenShogunModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RaidenPuppetModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RaidenPuppetModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GoldenWolflordModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GoldenWolflordModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RuinGuardRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RuinGuardModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RuinHunterRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RuinHunterModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GlareModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GlareModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.LunarShadowModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.LunarShadowModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.LunarGuardianModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.LunarGuardianModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.WildfireModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.WildfireModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.LeyLineMonolithModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.LeyLineMonolithModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AbyssChallengeKeyModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AbyssChallengeKeyModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AbyssMageModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AbyssMageModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AbyssMageShieldedModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AbyssMageShieldedModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.client.renderer.DragonNestModel.LAYER_LOCATION, com.soybe.awakeningsystem.client.renderer.DragonNestModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.client.renderer.AncientDragonEggModel.LAYER_LOCATION, com.soybe.awakeningsystem.client.renderer.AncientDragonEggModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AncientDragonModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AncientDragonModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.item.client.ElementalDemonCrownModel.LAYER_LOCATION, com.soybe.awakeningsystem.item.client.ElementalDemonCrownModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ElementalLordModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ElementalLordModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ElementalDemonKingModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ElementalDemonKingModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.JadeGuardianModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.JadeGuardianModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.client.renderer.LockedTreasureChestModel.LAYER_LOCATION, com.soybe.awakeningsystem.client.renderer.LockedTreasureChestModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.BloodQiTotemModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.BloodQiTotemModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.client.renderer.RouletteWheelModel.LAYER_LOCATION, com.soybe.awakeningsystem.client.renderer.RouletteWheelModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ShadowInfantryModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ShadowInfantryModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AbyssalOrcModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AbyssalOrcModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ShadowMonarchBossModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ShadowMonarchBossModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ClassicMonarchBossModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ClassicMonarchBossModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AbyssalNecromancerModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AbyssalNecromancerModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.FrostElfAssassinModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.FrostElfAssassinModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.SeriesNpcModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.SeriesNpcModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.DokkaebiModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.DokkaebiModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.PaimonModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.PaimonModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.CompanionModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.CompanionModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.CompanionModel.SLIM_LAYER_LOCATION, () -> com.soybe.awakeningsystem.entity.client.CompanionModel.createBodyLayer(true));
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.WyvernModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.WyvernModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.IsekaiSlimeModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.IsekaiSlimeModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.SpiritFoxModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.SpiritFoxModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AstralDragonModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AstralDragonModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ShadowDirewolfModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ShadowDirewolfModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.WhiteThunderTigerModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.WhiteThunderTigerModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.CloudCraneModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.CloudCraneModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AntSoldierModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AntSoldierModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AbyssalBeholderModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AbyssalBeholderModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ManaPixieModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ManaPixieModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ScreamingMandrakeModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ScreamingMandrakeModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.CelestialDragonModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.CelestialDragonModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.AncientCelestialDragonBossModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.AncientCelestialDragonBossModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.YggdrasilTreantModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.YggdrasilTreantModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GoblinScoutModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GoblinScoutModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GoblinWarriorModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GoblinWarriorModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GoblinShamanModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GoblinShamanModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.BeastmanWarriorModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.BeastmanWarriorModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.JiangshiModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.JiangshiModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.DemonicCultivatorModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.DemonicCultivatorModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.JadeArmoredBeastModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.JadeArmoredBeastModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.ManaPigModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.ManaPigModel::createBodyLayer);

        // Capas 3D de Mobs Canónicos de Genshin Impact
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GenshinSlimeModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GenshinSlimeModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GenshinLargeSlimeModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GenshinLargeSlimeModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.HilichurlModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.HilichurlModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.MitachurlModel.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.MitachurlModel::createBodyLayer);

        // Capas 3D de Hipóstasis
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.AnemoHypostasisRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.HypostasisModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.ElectroHypostasisRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.HypostasisModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.GeoHypostasisRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.HypostasisModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.CryoHypostasisRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.HypostasisModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.PyroHypostasisRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.HypostasisModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.HydroHypostasisRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.HypostasisModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.DendroHypostasisRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.HypostasisModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.AnemoRevivalOrbRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.RevivalOrbModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.ElectroRevivalPrismRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.RevivalPrismModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.hypostasis.client.DendroRevivalPithRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.hypostasis.client.RevivalOrbModel::createBodyLayer);

        // Capas 3D de Criaturas y Jefes de Genshin Impact (Árbol 407 Criaturas)
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GreatSnowboarKingRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GreatSnowboarKingModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RhodeiaOceanidRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RhodeiaOceanidModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.WaterMimicRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.WaterMimicModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.PerpetualMechanicalArrayRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.PerpetualMechanicalArrayModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RuinSentinelRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RuinSentinelModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RuinGraderRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RuinGraderModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RuinDrakeRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RuinDrakeModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.RuinSerpentRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.RuinSerpentModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.GeovishapRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GeovishapModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.BathysmalVishapRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.GeovishapModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.SpecterRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.SpecterModel::createBodyLayer);
        event.registerLayerDefinition(com.soybe.awakeningsystem.entity.client.WhopperflowerRenderer.LAYER_LOCATION, com.soybe.awakeningsystem.entity.client.WhopperflowerModel::createBodyLayer);
    }

    public static void registerParticleProviders(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(com.soybe.awakeningsystem.particle.ModParticles.SPIRIT_FOXFIRE.get(),
                com.soybe.awakeningsystem.particle.client.SpiritFoxfireParticle.Provider::new);
    }

    public static void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        com.soybe.awakeningsystem.client.gui.GuiConfig.load();
        event.enqueueWork(() -> {
            registerBowProperties(SystemItems.AMOS_BOW.get());
            registerBowProperties(SystemItems.ASTRAL_ARCHERS_BOW.get());
            registerBowProperties(SystemItems.THUNDERING_PULSE.get());
            registerBowProperties(SystemItems.SKYWARD_HARP.get());
            registerBowProperties(SystemItems.AQUA_SIMULACRA.get());
            registerBowProperties(SystemItems.POLAR_STAR.get());
            registerBowProperties(SystemItems.HUNTER_PATH.get());
            registerBowProperties(SystemItems.HAMAYUMI.get());
            registerBowProperties(SystemItems.END_OF_THE_LINE.get());

            if (SystemItems.LA_DONCELLA.isBound()) {
                net.minecraft.client.renderer.item.ItemProperties.register(
                        SystemItems.LA_DONCELLA.get(),
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "transformed"),
                        (stack, level, entity, seed) -> com.soybe.awakeningsystem.item.LaDoncellaSwordItem.isTransformed(stack) ? 1.0F : 0.0F
                );
                net.minecraft.client.renderer.item.ItemProperties.register(
                        SystemItems.LA_DONCELLA.get(),
                        net.minecraft.resources.ResourceLocation.withDefaultNamespace("transformed"),
                        (stack, level, entity, seed) -> com.soybe.awakeningsystem.item.LaDoncellaSwordItem.isTransformed(stack) ? 1.0F : 0.0F
                );
            }
        });
    }

    private static void registerBowProperties(net.minecraft.world.item.Item item) {
        net.minecraft.client.renderer.item.ItemProperties.register(item,
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    return entity.getUseItem() != stack ? 0.0F :
                            (float)(stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
                });

        net.minecraft.client.renderer.item.ItemProperties.register(item,
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    public static void registerClientExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        net.neoforged.neoforge.client.extensions.common.IClientItemExtensions crownExt = new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            private com.soybe.awakeningsystem.item.client.ElementalDemonCrownModel crownModel;

            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(net.minecraft.world.entity.LivingEntity livingEntity, net.minecraft.world.item.ItemStack itemStack, net.minecraft.world.entity.EquipmentSlot equipmentSlot, net.minecraft.client.model.HumanoidModel<?> original) {
                if (crownModel == null) {
                    crownModel = new com.soybe.awakeningsystem.item.client.ElementalDemonCrownModel(
                            net.minecraft.client.Minecraft.getInstance().getEntityModels().bakeLayer(com.soybe.awakeningsystem.item.client.ElementalDemonCrownModel.LAYER_LOCATION));
                }
                ((net.minecraft.client.model.HumanoidModel) original).copyPropertiesTo(crownModel);
                crownModel.head.visible = true;
                crownModel.hat.visible = false;
                crownModel.body.visible = false;
                crownModel.rightArm.visible = false;
                crownModel.leftArm.visible = false;
                crownModel.rightLeg.visible = false;
                crownModel.leftLeg.visible = false;
                return crownModel;
            }
        };

        if (SystemItems.ELEMENTAL_DEMON_CROWN.isBound()) {
            event.registerItem(crownExt, SystemItems.ELEMENTAL_DEMON_CROWN.get());
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (notificationTicks > 0) notificationTicks--;
        if (coinNotificationTicks > 0) coinNotificationTicks--;
        if (bossCinematicActive) {
            bossCinematicTicks--;
            if (bossCinematicTicks <= 0) {
                bossCinematicActive = false;
                cinematicAnglesInitialized = false;
            }
        }
        if (minecraft.player != null) {
            com.soybe.awakeningsystem.client.renderer.SwordAuraRenderer.tickParticles(minecraft);
            SystemData clientData = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            clientData.tickCombat();

            if (minecraft.player.getVehicle() instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity dragon) {
                boolean space = minecraft.options.keyJump.isDown();
                boolean shift = minecraft.options.keyShift.isDown();
                boolean sprint = minecraft.options.keySprint.isDown();
                dragon.setFlightInput(space, shift, sprint);
                PacketDistributor.sendToServer(new DragonFlightControlPayload(space, shift, sprint));
            } else if (minecraft.player.getVehicle() instanceof com.soybe.awakeningsystem.entity.AncientDragonEntity dragon) {
                boolean space = minecraft.options.keyJump.isDown();
                boolean shift = minecraft.options.keyShift.isDown();
                boolean sprint = minecraft.options.keySprint.isDown();
                dragon.setFlightInput(space, shift, sprint);
                PacketDistributor.sendToServer(new DragonFlightControlPayload(space, shift, sprint));
            } else if (minecraft.player.getVehicle() instanceof com.soybe.awakeningsystem.entity.WyvernEntity wyvern) {
                boolean space = minecraft.options.keyJump.isDown();
                boolean shift = minecraft.options.keyShift.isDown();
                boolean sprint = minecraft.options.keySprint.isDown();
                wyvern.setFlightInput(space, shift, sprint);
                PacketDistributor.sendToServer(new DragonFlightControlPayload(space, shift, sprint));
            }
        }
        if (minecraft.level != null && minecraft.player != null) {
            if (!ACTIVE_MAGIC_CIRCLES.isEmpty()) {
                for (ActiveMagicCircle circle : ACTIVE_MAGIC_CIRCLES) {
                    circle.currentTick++;
                    if (circle.isFinished()) {
                        ACTIVE_MAGIC_CIRCLES.remove(circle);
                        continue;
                    }
                    if (circle.casterEntityId > 0) {
                        net.minecraft.world.entity.Entity caster = (minecraft.player != null && minecraft.player.getId() == circle.casterEntityId)
                                ? minecraft.player
                                : minecraft.level.getEntity(circle.casterEntityId);
                        if (caster != null && caster.isAlive()) {
                            if (circle.circleType == 0 || circle.circleType == 2) {
                                circle.x = caster.getX();
                                circle.y = caster.getY();
                                circle.z = caster.getZ();
                            } else {
                                var look = (caster instanceof net.minecraft.world.entity.LivingEntity le) ? le.getViewVector(1.0F).normalize() : caster.getLookAngle().normalize();
                                double dist = (circle.maxRadius > 4.0F) ? Math.max(2.5D, Math.min(5.0D, circle.maxRadius * 0.40D)) : 1.65D;

                                circle.x = caster.getX() + look.x * dist;
                                circle.y = caster.getEyeY() + look.y * dist;
                                circle.z = caster.getZ() + look.z * dist;
                                circle.lookX = (float) look.x;
                                circle.lookY = (float) look.y;
                                circle.lookZ = (float) look.z;
                            }
                        }
                    }

                    if (circle.isCharging) {
                        for (int i = 0; i < 2; i++) {
                            double angle = Math.random() * Math.PI * 2.0;
                            double rad = circle.maxRadius * (1.1D + Math.random() * 0.9D);
                            double sx = circle.x + Math.cos(angle) * rad;
                            double sy = circle.y + 0.1D + Math.random() * 1.2D;
                            double sz = circle.z + Math.sin(angle) * rad;
                            double vx = (circle.x - sx) * 0.12D;
                            double vy = (circle.y + 0.3D - sy) * 0.12D;
                            double vz = (circle.z - sz) * 0.12D;
                            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, sx, sy, sz, vx, vy, vz);
                            if (Math.random() < 0.35) {
                                minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, sx, sy, sz, vx * 0.6D, vy * 0.6D, vz * 0.6D);
                            }
                        }
                    } else if (circle.currentTick % 2 == 0) {
                        double angle = Math.random() * Math.PI * 2.0;
                        double r = circle.maxRadius * (0.3D + Math.random() * 0.7D);
                        double px = circle.x + Math.cos(angle) * r;
                        double py = circle.y + 0.1D + Math.random() * 0.8D;
                        double pz = circle.z + Math.sin(angle) * r;
                        minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, px, py, pz, 0, 0.05D, 0);
                        if (Math.random() < 0.25) {
                            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, px, py, pz, 0, 0.02D, 0);
                        }
                    }
                }
            }

            long now = System.currentTimeMillis();

            // 1. Partículas de emergencia saliendo activamente desde las profundidades del suelo
            for (var it = SHADOW_EMERGENCE_MAP.entrySet().iterator(); it.hasNext(); ) {
                var entry = it.next();
                int entityId = entry.getKey();
                ShadowEmergenceData data = entry.getValue();
                long elapsed = now - data.startTimeMs();
                if (elapsed > data.durationMs()) {
                    it.remove();
                    continue;
                }

                net.minecraft.world.entity.Entity ent = minecraft.level.getEntity(entityId);
                double ex = ent != null ? ent.getX() : data.x();
                double ey = ent != null ? ent.getY() : data.y();
                double ez = ent != null ? ent.getZ() : data.z();

                // Charco de tinta oscura hirviente y llamas de alma en la superficie del suelo
                if (minecraft.level.getGameTime() % 2 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double angle = (Math.PI * 2 * i) / 3.0D + (Math.random() * 0.4D);
                        double dist = 0.35D + Math.random() * 0.45D;
                        double px = ex + Math.cos(angle) * dist;
                        double pz = ez + Math.sin(angle) * dist;
                        minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.SQUID_INK, px, ey + 0.03D, pz, 0, 0.015D, 0);
                        if (Math.random() < 0.5D) {
                            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, px, ey + 0.03D, pz, (Math.random() - 0.5D) * 0.01D, 0.025D + Math.random() * 0.015D, (Math.random() - 0.5D) * 0.01D);
                        }
                    }
                    minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL, ex + (Math.random() - 0.5D) * 0.3D, ey + 0.1D, ez + (Math.random() - 0.5D) * 0.3D, 0, 0.035D, 0);
                }
            }

            // 2. Aura sutil y distintiva para todas las sombras activas (iterando solo sobre sombras registradas)
            if (minecraft.level.getGameTime() % 4 == 0 && !SHADOW_SOLDIER_IDS.isEmpty()) {
                var it = SHADOW_SOLDIER_IDS.iterator();
                while (it.hasNext()) {
                    int sid = it.next();
                    net.minecraft.world.entity.Entity ent = minecraft.level.getEntity(sid);
                    if (ent == null || !ent.isAlive()) {
                        it.remove();
                        CONFIGURED_SHADOW_TEAM_IDS.remove(sid);
                        continue;
                    }
                    if (ent instanceof net.minecraft.world.entity.LivingEntity living) {
                        if (minecraft.player != null && minecraft.player.distanceToSqr(living) > 40.0D * 40.0D) continue;
                        double px = living.getX() + (living.getRandom().nextDouble() - 0.5D) * living.getBbWidth() * 0.9D;
                        double py = living.getY() + living.getRandom().nextDouble() * living.getBbHeight() * 0.8D;
                        double pz = living.getZ() + (living.getRandom().nextDouble() - 0.5D) * living.getBbWidth() * 0.9D;
                        minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.015D, 0);
                        // Ojos de fuego azul en la mirada del soldado
                        minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                                living.getX(), living.getEyeY() - 0.04D, living.getZ(), 0, 0.005D, 0);
                        if (living.getRandom().nextBoolean()) {
                            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, px, py, pz, 0, 0.01D, 0);
                        }
                    }
                }
            }

            // Limpieza periódica de caché de entidades no sombras para evitar retención prolongada de IDs
            if (minecraft.level.getGameTime() % 600 == 0) {
                NON_SHADOW_ENTITY_IDS.clear();
            }

            // 2. Partículas ambientales místicas para portales dimensionales activos
            for (SyncRiftsPayload.RiftEntry rift : ACTIVE_RIFTS) {
                boolean isIsekai = "ISEKAI".equalsIgnoreCase(rift.rank()) || rift.type() == 99;
                boolean isMurim = "MURIM".equalsIgnoreCase(rift.rank()) || rift.type() == 98;
                boolean isAbyss = "TEYVAT_ABYSS".equalsIgnoreCase(rift.rank()) || rift.type() == 97;
                boolean isAbyssBlocked = "TEYVAT_ABYSS_BLOCKED".equalsIgnoreCase(rift.rank()) || rift.type() == 96;
                if (!isIsekai && !isMurim && !isAbyss && !isAbyssBlocked) continue;

                double px = rift.pos().getX() + 0.5D;
                double py = rift.pos().getY() + (isAbyss || isAbyssBlocked ? 0.5D : 2.5D);
                double pz = rift.pos().getZ() + 0.5D;
                if (minecraft.player.distanceToSqr(px, py, pz) <= 35.0D * 35.0D) {
                    var rnd = minecraft.level.random;
                    if (isAbyss) {
                        if (rnd.nextFloat() < 0.70F) {
                            double angle = rnd.nextDouble() * Math.PI * 2.0;
                            double rad = 1.0 + rnd.nextDouble() * 2.5;
                            double ox = Math.cos(angle) * rad;
                            double oy = Math.sin(angle) * rad;
                            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, px + ox, py + oy, pz, 0, 0.03D, 0);
                            if (rnd.nextBoolean()) {
                                minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, px + ox, py + oy, pz, 0, 0.05D, 0);
                            }
                        }
                    } else if (isAbyssBlocked) {
                        if (rnd.nextFloat() < 0.45F) {
                            double angle = rnd.nextDouble() * Math.PI * 2.0;
                            double rad = 3.2 + rnd.nextDouble() * 0.4;
                            double ox = Math.cos(angle) * rad;
                            double oy = Math.sin(angle) * rad;
                            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE, px + ox, py + oy, pz, ox * 0.01D, oy * 0.01D, 0);
                            if (rnd.nextBoolean()) {
                                minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, px + ox, py + oy, pz, 0, 0.01D, 0);
                            }
                        }
                    } else {
                        if (rnd.nextFloat() < 0.65F) {
                            double offX = (rnd.nextDouble() - 0.5D) * 2.8D;
                            double offY = (rnd.nextDouble() - 0.5D) * 3.6D;
                            double offZ = (rnd.nextDouble() - 0.5D) * 0.6D;
                            if (isIsekai) {
                                if (rnd.nextBoolean()) {
                                    minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.CHERRY_LEAVES, px + offX, py + offY, pz + offZ, offX * 0.02D, rnd.nextDouble() * 0.02D, offZ * 0.02D);
                                } else {
                                    minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, px + offX, py + offY, pz + offZ, 0, 0.02D, 0);
                                }
                            } else {
                                if (rnd.nextBoolean()) {
                                    minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, px + offX, py + offY, pz + offZ, offX * 0.01D, 0.01D, offZ * 0.01D);
                                } else {
                                    minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, px + offX, py + offY, pz + offZ, 0, 0.03D, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
        while (OPEN_SYSTEM.consumeClick()) {
            if (minecraft.screen == null) {
                if (minecraft.player != null) {
                    SystemData pData = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                    if (pData.isAnySystemUpdating()) {
                        minecraft.setScreen(new SystemMaintenanceScreen());
                        continue;
                    }
                }
                minecraft.setScreen(new SystemScreen());
            }
            else if (minecraft.screen instanceof SystemScreen || minecraft.screen instanceof SystemMaintenanceScreen) minecraft.setScreen(null);
            else if (minecraft.screen instanceof SkillDetailScreen) minecraft.setScreen(null);
        }
        while (OPEN_GACHA.consumeClick()) {
            if (minecraft.screen == null) {
                if (minecraft.player != null) {
                    SystemData d = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                    boolean has40 = d.system40Updated() || d.isSystem40Updating() || d.teyvatUnlocked();
                    if (!has40) {
                        minecraft.player.displayClientMessage(Component.literal("§c✦ El Sistema de Deseos (Gacha) se desbloquea en la Versión 4.0 (Teyvat)."), true);
                        playUiSound(ModSounds.SYSTEM_ERROR.value(), 1.0F, 1.0F);
                        break;
                    }
                    java.util.List<String> hist = new java.util.ArrayList<>();
                    for (SystemData.WishLogEntry e : d.wishHistory()) {
                        hist.add(e.itemName() + "::" + e.stars() + "::" + e.bannerName() + "::" + e.timestamp());
                    }
                    minecraft.setScreen(new GachaWishScreen(
                            d.primogems(),
                            d.intertwinedFates(),
                            d.acquaintFates(),
                            d.stardust(),
                            d.starglitter(),
                            d.gachaPity5Star(),
                            d.gachaPity4Star(),
                            d.gachaGuaranteedRateUp(),
                            d.beginnerPulls(),
                            d.mora(),
                            d.weaponBannerTarget(),
                            d.weaponBannerFatePoints(),
                            0,
                            d.coins(),
                            d.usdBalance(),
                            d.genesisCrystals(),
                            d.stellarJade(),
                            d.welkinDays(),
                            hist
                    ));
                }
            } else if (minecraft.screen instanceof GachaWishScreen) {
                minecraft.setScreen(null);
            }
        }
        while (ACTIVATE_SKILL.consumeClick()) {
            activateSkillSlot(0);
        }
        while (SKILL_SLOT_2.consumeClick()) {
            activateSkillSlot(1);
        }
        while (SKILL_SLOT_3.consumeClick()) {
            activateSkillSlot(2);
        }
        while (SKILL_SLOT_4.consumeClick()) {
            activateSkillSlot(3);
        }
        while (SKILL_SLOT_5.consumeClick()) {
            activateSkillSlot(4);
        }
        while (ACTIVATE_STIGMA.consumeClick()) {
            activateStigma();
        }
        while (CULTIVATE_QI.consumeClick()) {
            performCultivationAction("toggle_meditation", 0);
        }
        while (ACTIVATE_ARMOR_SKILL.consumeClick()) {
            activateArmorSkill();
        }
        while (PHYSIQUE_SKILL.consumeClick()) {
            performCultivationAction("use_physique_skill", 0);
        }
        while (DAO_SKILL.consumeClick()) {
            performCultivationAction("use_dao_skill", -1);
        }
        while (WEAPON_SKILL.consumeClick()) {
            activateWeaponSkill();
        }
        while (OPEN_GUILD.consumeClick()) {
            if (minecraft.screen == null) {
                requestOpenGuild();
            }
        }
        if (minecraft.player != null && minecraft.level != null && minecraft.player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity) {
            double t = (minecraft.player.tickCount) * 0.18D;
            double r = 0.95D;
            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    minecraft.player.getX() + Math.cos(t) * r,
                    minecraft.player.getY() + 0.15D,
                    minecraft.player.getZ() + Math.sin(t) * r,
                    0, 0.02D, 0);
            minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    minecraft.player.getX() + Math.cos(t + Math.PI) * r,
                    minecraft.player.getY() + 0.25D,
                    minecraft.player.getZ() + Math.sin(t + Math.PI) * r,
                    0, 0.03D, 0);
        }
    }

    public static void activateArmorSkill() {
        PacketDistributor.sendToServer(new ActivateArmorSkillPayload());
    }

    public static void activateWeaponSkill() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        net.minecraft.world.item.ItemStack stack = mc.player.getMainHandItem();
        if (!com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(stack)) {
            net.minecraft.world.item.ItemStack offStack = mc.player.getOffhandItem();
            if (com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(offStack)) {
                hand = net.minecraft.world.InteractionHand.OFF_HAND;
                stack = offStack;
            }
        }
        if (com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(stack)) {
            stack.use(mc.level, mc.player, hand);
            mc.player.swing(hand);
            int handOrdinal = hand == net.minecraft.world.InteractionHand.OFF_HAND ? 1 : 0;
            PacketDistributor.sendToServer(new ActivateWeaponSkillPayload(handOrdinal));
        }
    }

    public static void onMouseButtonPre(net.neoforged.neoforge.client.event.InputEvent.MouseButton.Pre event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null && mc.screen == null) {
                // Tendido de Vías Continuo con Guía Holográfica Estilo Create
                if (com.soybe.awakeningsystem.client.track.TrackPlacementPreview.isHoldingTrackItem(mc.player)) {
                    if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult bhr) {
                        if (com.soybe.awakeningsystem.client.track.TrackPlacementPreview.onRightClick(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, bhr.getBlockPos())) {
                            event.setCanceled(true);
                            return;
                        }
                    } else if (com.soybe.awakeningsystem.client.track.TrackPlacementPreview.hasAnchor()) {
                        com.soybe.awakeningsystem.client.track.TrackPlacementPreview.reset();
                        event.setCanceled(true);
                        return;
                    }
                }

                boolean mainHasWeapon = com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(mc.player.getMainHandItem());
                boolean offHasWeapon = com.soybe.awakeningsystem.item.AwakeningWeaponItem.isAwakeningWeapon(mc.player.getOffhandItem());
                if (mainHasWeapon || offHasWeapon) {
                    if (mc.player.isShiftKeyDown()) {
                        event.setCanceled(true);
                        activateWeaponSkill();
                    } else if (mainHasWeapon && mc.player.getOffhandItem().isEmpty() && (mc.hitResult == null || mc.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK)) {
                        activateWeaponSkill();
                    }
                }
            }
        }
    }

    public static void activateSkillSlot(int slot) {
        PacketDistributor.sendToServer(new ActivateSkillSlotPayload(slot));
    }

    public static void equipSkillSlot(boolean isPassive, int slot, String skillId) {
        PacketDistributor.sendToServer(new EquipSkillSlotPayload(isPassive, slot, skillId));
    }

    public static void activateStigma() {
        PacketDistributor.sendToServer(new ActivateStigmaPayload());
    }

    public static void performCultivationAction(String action, int targetIndex) {
        PacketDistributor.sendToServer(new CultivationActionPayload(action, targetIndex));
    }

    public static void spend(String attribute) {
        PacketDistributor.sendToServer(new SpendAttributePayload(attribute));
    }

    public static void evolve(String skillId) {
        PacketDistributor.sendToServer(new EvolveSkillPayload(skillId));
    }

    public static void equipSkill(String skillId) {
        PacketDistributor.sendToServer(new EquipSkillPayload(skillId));
    }

    public static void toggleSkill(String skillId) {
        PacketDistributor.sendToServer(new ToggleSkillPayload(skillId));
    }

    public static void onMovementInputUpdate(net.neoforged.neoforge.client.event.MovementInputUpdateEvent event) {
        if (event.getEntity().getVehicle() instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity dragon) {
            // Mientras esté en el aire, evitar que shift desmonte automáticamente al jugador
            if (!dragon.onGround()) {
                event.getInput().shiftKeyDown = false;
            }
        } else if (event.getEntity().getVehicle() instanceof com.soybe.awakeningsystem.entity.AncientDragonEntity dragon) {
            // Mientras esté en el aire, evitar que shift desmonte automáticamente al jugador
            if (!dragon.onGround()) {
                event.getInput().shiftKeyDown = false;
            }
        }
    }

    public static void equipTitle(String titleId) {
        PacketDistributor.sendToServer(new EquipTitlePayload(titleId));
    }

    public static void buyShopItem(String entryId) {
        PacketDistributor.sendToServer(new BuyShopItemPayload(entryId));
    }

    public static void sellShopItem(String entryId, boolean sellAll) {
        PacketDistributor.sendToServer(new SellShopItemPayload(entryId, sellAll));
    }

    public static void activateSkill(String skillId) {
        PacketDistributor.sendToServer(new ActivateSkillPayload(skillId));
    }

    public static void prestige() {
        PacketDistributor.sendToServer(new PrestigePayload());
    }

    public static void selectConstellation(String constellationId) {
        PacketDistributor.sendToServer(new SelectConstellationPayload(constellationId));
    }

    private static net.minecraft.network.chat.Component notificationTitleComp;
    private static net.minecraft.network.chat.Component notificationMessageComp;
    private static net.minecraft.network.chat.Component coinNotificationComp;
    private static int coinNotificationTicks = 0;
    private static boolean isConstellationNotif = false;

    public static void claimScenario() {
        PacketDistributor.sendToServer(new ClaimScenarioPayload());
    }

    private static Object parseNotificationArgument(String text) {
        if (text == null || text.isEmpty()) return "";
        if (text.startsWith("awakening_system.") || text.startsWith("block.awakening_system.")
                || text.startsWith("item.awakening_system.") || text.startsWith("entity.awakening_system.")
                || (text.contains(".") && !text.contains(" ") && !text.matches("^[0-9.]+$"))) {
            return net.minecraft.network.chat.Component.translatable(text);
        }
        return text;
    }

    public static void showNotification(String titleKey, String messageKey, String argument, int color) {
        Object[] arguments;
        if (argument == null || argument.isEmpty()) {
            arguments = new Object[0];
        } else if (argument.contains(":::")) {
            String[] parts = argument.split(":::", -1);
            arguments = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                arguments[i] = parseNotificationArgument(parts[i]);
            }
        } else {
            arguments = new Object[]{parseNotificationArgument(argument)};
        }

        // Mini notificación independiente para monedas (evita sobreescribir el diálogo de patrocinador)
        if (titleKey != null && titleKey.contains("coin")) {
            coinNotificationComp = net.minecraft.network.chat.Component.translatable(messageKey, arguments);
            coinNotificationTicks = 70;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.65F, 1.4F);
            }
            return;
        }

        // Máxima prioridad para los mensajes de constelaciones / patrocinadores: no pueden ser sobreescritos
        if (notificationTicks > 0 && notificationTitleComp != null) {
            String curr = notificationTitleComp.getString().toLowerCase(java.util.Locale.ROOT);
            if (curr.contains("constelaci") || curr.contains("constellation") || curr.contains("patrocin") || curr.contains("sponsor")) {
                if (titleKey == null || (!titleKey.contains("constellation") && !titleKey.contains("sponsor"))) {
                    return; // Preservar el mensaje de la constelación sin interrupción
                }
            }
        }

        if (titleKey != null && (titleKey.startsWith("literal:") || titleKey.startsWith("§") || !titleKey.contains("."))) {
            String raw = titleKey.startsWith("literal:") ? titleKey.substring(8) : titleKey;
            notificationTitleComp = net.minecraft.network.chat.Component.literal(raw);
        } else {
            notificationTitleComp = net.minecraft.network.chat.Component.translatable(titleKey, arguments);
        }

        if (messageKey != null && (messageKey.startsWith("literal:") || messageKey.startsWith("§") || !messageKey.contains("."))) {
            String raw = messageKey.startsWith("literal:") ? messageKey.substring(8) : messageKey;
            notificationMessageComp = net.minecraft.network.chat.Component.literal(raw);
        } else {
            notificationMessageComp = net.minecraft.network.chat.Component.translatable(messageKey, arguments);
        }

        notificationTitle = notificationTitleComp.getString();
        notificationMessage = notificationMessageComp.getString();
        notificationColor = color;
        isConstellationNotif = (titleKey != null && (titleKey.contains("constellation") || titleKey.contains("sponsor")
                || titleKey.contains("reaction_") || titleKey.contains("star_stream")
                || titleKey.contains("ESTELAR") || titleKey.contains("PATROCINIO")));

        // Aumentar duración a 160 ticks (~8 segundos completos) para constelaciones y mensajes importantes
        notificationTicks = isConstellationNotif ? 160 : (notificationMessage.length() > 60 ? 140 : 100);

        playNotificationSound(titleKey, messageKey);
    }

    public static void playUiSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        if (sound == null || !com.soybe.awakeningsystem.client.gui.GuiConfig.uiSoundsEnabled) return;
        Minecraft.getInstance().getSoundManager().play(
            new net.minecraft.client.resources.sounds.SimpleSoundInstance(
                sound.getLocation(),
                net.minecraft.sounds.SoundSource.PLAYERS,
                volume,
                pitch,
                net.minecraft.util.RandomSource.create(),
                false,
                0,
                net.minecraft.client.resources.sounds.SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
            )
        );
    }

    private static void playNotificationSound(String titleKey, String messageKey) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (titleKey.contains("error") || titleKey.contains("denied") || titleKey.contains("insufficient")) {
            playUiSound(ModSounds.SYSTEM_ERROR.value(), 0.7F, 1.0F);
        } else if (titleKey.contains("constellation") || titleKey.contains("ESTELAR") || titleKey.contains("PATROCINIO")) {
            playUiSound(ModSounds.SYSTEM_SKILL_UNLOCK.value(), 0.8F, 1.05F);
        } else if (titleKey.contains("coin")) {
            playUiSound(ModSounds.SYSTEM_COINS.value(), 0.75F, 1.0F);
        } else if (titleKey.equals("awakening_system.notification.shop.title")) {
            playUiSound(ModSounds.SYSTEM_COINS.value(), 0.75F, 1.0F);
        } else if (titleKey.contains("skill_level") || titleKey.equals("awakening_system.notification.skill.title")) {
            playUiSound(ModSounds.SYSTEM_SKILL_UNLOCK.value(), 0.8F, 1.0F);
        } else if (titleKey.contains("level_up")) {
            playUiSound(ModSounds.SYSTEM_LEVEL_UP.value(), 0.85F, 1.0F);
        } else if (titleKey.contains("rank_reward")) {
            playUiSound(ModSounds.SYSTEM_RANK_UP.value(), 0.85F, 1.0F);
        } else if (titleKey.contains("daily_complete") || titleKey.contains("secret_complete") || titleKey.contains("emergency_complete")) {
            playUiSound(ModSounds.SYSTEM_QUEST_COMPLETE.value(), 0.8F, 1.0F);
        } else if (titleKey.contains("evolution_complete") || titleKey.contains("class_complete")) {
            playUiSound(ModSounds.SYSTEM_AWAKENING.value(), 0.85F, 1.0F);
        } else if (titleKey.contains("awakening_complete") || titleKey.contains("awakened")) {
            playUiSound(ModSounds.SYSTEM_AWAKENING.value(), 0.85F, 1.0F);
        } else if (titleKey.contains("evolved")) {
            playUiSound(ModSounds.SYSTEM_AWAKENING.value(), 0.85F, 1.0F);
        } else {
            playUiSound(ModSounds.SYSTEM_QUEST_ALERT.value(), 0.75F, 1.0F);
        }
    }

    public static void renderNotification(net.minecraft.client.gui.GuiGraphics graphics, int width, int height) {
        var font = Minecraft.getInstance().font;

        boolean sys20 = false;
        boolean sys30 = false;
        var clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            var pData = clientPlayer.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (pData != null) {
                if (pData.system40Updated() || pData.isSystem40Updating()) {
                    sys30 = true; // Use gilded / celestial
                } else if (pData.system30Updated()) {
                    sys30 = true;
                } else if (pData.systemUpdated() || pData.isSystemUpdating()) {
                    sys20 = true;
                }
            }
        }

        // Mini-indicador de monedas independiente en la esquina inferior derecha (Dorado brillante y ónix)
        if (coinNotificationTicks > 0 && coinNotificationComp != null) {
            int coinAlpha = Math.min(255, coinNotificationTicks * 12);
            int coinW = font.width(coinNotificationComp) + 16;
            int coinH = 16;
            int coinX = width - coinW - 14;
            int coinY = height - 44;
            graphics.fill(coinX - 1, coinY - 1, coinX + coinW + 1, coinY + coinH + 1, (coinAlpha << 24) | 0xFFFFD700);
            graphics.fill(coinX, coinY, coinX + coinW, coinY + coinH, (Math.min(coinAlpha, 225) << 24) | 0x121008);
            int coinCorner = (coinAlpha << 24) | 0xFFFFF0A0;
            graphics.fill(coinX, coinY, coinX + 3, coinY + 1, coinCorner);
            graphics.fill(coinX + coinW - 3, coinY, coinX + coinW, coinY + 1, coinCorner);
            graphics.drawString(font, coinNotificationComp, coinX + 8, coinY + 4, (coinAlpha << 24) | 0xFFFFE066, false);
        }

        if (notificationTicks <= 0 || notificationTitleComp == null) return;
        int alpha = Math.min(255, notificationTicks * 12);

        if (isConstellationNotif) {
            // Notificación Star Stream: escalable y reubicable según GuiConfig
            float scale = Math.max(0.4F, Math.min(1.5F, com.soybe.awakeningsystem.client.gui.GuiConfig.constellationNotifScale));
            int boxWidth = Math.min(com.soybe.awakeningsystem.client.gui.GuiConfig.compactConstellationPopups ? 200 : 260, (int)((width - 32) / scale));
            var lines = font.split(notificationMessageComp != null ? notificationMessageComp : net.minecraft.network.chat.Component.empty(), boxWidth - 16);
            int boxHeight = 24 + lines.size() * 11;

            int defaultLeft = width - (int)(boxWidth * scale) - 10;
            int defaultTop = 10;
            int left = com.soybe.awakeningsystem.client.gui.GuiConfig.constellationNotifX >= 0 ? com.soybe.awakeningsystem.client.gui.GuiConfig.constellationNotifX : defaultLeft;
            int top = com.soybe.awakeningsystem.client.gui.GuiConfig.constellationNotifY >= 0 ? com.soybe.awakeningsystem.client.gui.GuiConfig.constellationNotifY : defaultTop;

            graphics.pose().pushPose();
            graphics.pose().translate(left, top, 0);
            graphics.pose().scale(scale, scale, 1.0F);

            int borderCol = (sys20 || sys30) ? 0xFFFFD700 : 0x276A82;
            int bgCol = (sys20 || sys30) ? 0x0D111A : 0x10202D;
            int barCol = (sys20 || sys30) ? 0xFFFFD700 : 0xFF00E5FF;
            graphics.fill(-1, -1, boxWidth + 1, boxHeight + 1, (alpha << 24) | borderCol);
            graphics.fill(0, 0, boxWidth, boxHeight, (Math.min(alpha, 225) << 24) | bgCol);
            graphics.fill(0, 0, boxWidth, 2, (alpha << 24) | barCol);

            // Esquinas tech
            int cornerCol = (alpha << 24) | ((sys20 || sys30) ? 0xFFFFE066 : 0xFF00E5FF);
            graphics.fill(0, 0, 3, 1, cornerCol);
            graphics.fill(0, 0, 1, 3, cornerCol);
            graphics.fill(boxWidth - 3, 0, boxWidth, 1, cornerCol);
            graphics.fill(boxWidth - 1, 0, boxWidth, 3, cornerCol);

            int titleC = (sys20 || sys30) ? 0xFFFFD700 : 0xFF70E8FF;
            graphics.drawString(font, notificationTitleComp, 8, 6, (alpha << 24) | titleC, false);
            int lineY = 18;
            for (net.minecraft.util.FormattedCharSequence line : lines) {
                int lineC = (sys20 || sys30) ? 0xFFF6F0DD : 0xFFDDECF2;
                graphics.drawString(font, line, 8, lineY, (alpha << 24) | lineC, false);
                lineY += 11;
            }

            graphics.pose().popPose();
            return;
        }

        // Notificaciones centrales del Sistema (Despertar de rango, Ocupación, etc.)
        float sysScale = Math.max(0.4F, Math.min(1.5F, com.soybe.awakeningsystem.client.gui.GuiConfig.systemNotificationScale));
        int titleW = font.width(notificationTitleComp);
        int msgW = font.width(notificationMessageComp != null ? notificationMessageComp : net.minecraft.network.chat.Component.empty());
        int maxTextW = Math.max(titleW + 28, Math.min(380, (int) Math.sqrt(msgW * 180)));
        int boxWidth = Math.min(Math.max(250, maxTextW), width - 32);
        var lines = font.split(notificationMessageComp != null ? notificationMessageComp : net.minecraft.network.chat.Component.empty(), boxWidth - 16);
        int boxHeight = 32 + lines.size() * 12;
        int left = (width - boxWidth) / 2;
        int top = calculateNotificationTop();

        graphics.pose().pushPose();
        if (Math.abs(sysScale - 1.0F) > 0.01F) {
            graphics.pose().translate(left + boxWidth / 2.0F, top + boxHeight / 2.0F, 0);
            graphics.pose().scale(sysScale, sysScale, 1.0F);
            graphics.pose().translate(-(left + boxWidth / 2.0F), -(top + boxHeight / 2.0F), 0);
        }

        int borderCol = sys30 ? 0xD4AF37 : (sys20 ? 0xFFD700 : 0x276A82);
        int bgCol = sys30 ? 0x0A1815 : (sys20 ? 0x140B22 : 0x10202D);
        int topBarCol;
        int titleCol;
        if (sys30) {
            topBarCol = 0x50FA7B;
            titleCol = 0xFF50FA7B;
        } else if (sys20) {
            topBarCol = 0x9D4EDD;
            titleCol = 0xFFFFD700;
        } else {
            // Sistema 1.0 (Solo Leveling): Mantiene estética azul eléctrico/cian, filtrando tonos amarillos
            boolean isDanger = ((notificationColor >> 16) & 0xFF) > 200 && ((notificationColor >> 8) & 0xFF) < 100 && (notificationColor & 0xFF) < 100;
            if (isDanger) {
                topBarCol = 0xFF4444;
                titleCol = 0xFFFF4444;
            } else if (isYellowTone(notificationColor)) {
                topBarCol = 0x00E5FF;
                titleCol = 0xFF70E8FF;
            } else {
                topBarCol = (notificationColor & 0xFFFFFF);
                titleCol = (notificationColor & 0xFFFFFF);
            }
        }

        graphics.fill(left - 1, top - 1, left + boxWidth + 1, top + boxHeight + 1, (alpha << 24) | borderCol);
        graphics.fill(left, top, left + boxWidth, top + boxHeight, (Math.min(alpha, 220) << 24) | bgCol);
        graphics.fill(left, top, left + boxWidth, top + 2, (alpha << 24) | topBarCol);

        if (sys30 || sys20) {
            // Pips de alta jerarquía Multiversal en las cuatro esquinas
            int cornerPips = (alpha << 24) | (sys30 ? 0xFF50FA7B : 0xFFFFD700);
            graphics.fill(left - 1, top - 1, left + 5, top + 1, cornerPips);
            graphics.fill(left - 1, top - 1, left + 1, top + 5, cornerPips);
            graphics.fill(left + boxWidth - 5, top - 1, left + boxWidth + 1, top + 1, cornerPips);
            graphics.fill(left + boxWidth - 1, top - 1, left + boxWidth + 1, top + 5, cornerPips);
            graphics.fill(left - 1, top + boxHeight - 5, left + 1, top + boxHeight + 1, cornerPips);
            graphics.fill(left - 1, top + boxHeight - 1, left + 5, top + boxHeight + 1, cornerPips);
            graphics.fill(left + boxWidth - 1, top + boxHeight - 5, left + boxWidth + 1, top + boxHeight + 1, cornerPips);
            graphics.fill(left + boxWidth - 5, top + boxHeight - 1, left + boxWidth + 1, top + boxHeight + 1, cornerPips);
        }

        graphics.drawCenteredString(font, notificationTitleComp, width / 2, top + 8,
                (alpha << 24) | titleCol);
        int lineY = top + 22;
        for (net.minecraft.util.FormattedCharSequence line : lines) {
            int msgCol = sys30 ? 0xFFD8FBE4 : (sys20 ? 0xFFF2D6 : 0xDDECF2);
            graphics.drawCenteredString(font, line, width / 2, lineY, (alpha << 24) | msgCol);
            lineY += 12;
        }

        graphics.pose().popPose();
    }

    private static boolean isYellowTone(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return r > 180 && g > 150 && b < 140;
    }

    public static int calculateNotificationTop() {
        Minecraft mc = Minecraft.getInstance();
        int bossBottom = SystemHudOverlay.getBossBarOffset(mc);
        int top = bossBottom > 0 ? (bossBottom + 8) : 18;

        SystemData data = mc.player != null ? SystemEvents.data(mc.player) : null;
        if (data != null) {
            // Si la actualizacion del Sistema 2.0 o 3.0 esta activa en la parte superior
            if (data.isAnySystemUpdating()) {
                int bannerY = bossBottom > 0 ? (bossBottom + 4) : 8;
                top = Math.max(top, bannerY + 22);
            }

            // Si el escenario de supervivencia del Dokkaebi esta activo en la parte superior
            if (data.isDokkaebiScenarioActive()) {
                int bannerOffset = data.isAnySystemUpdating() ? 32 : 0;
                int dokkaebiBottom = Math.max(12, bossBottom + bannerOffset + 8) + 28;
                top = Math.max(top, dokkaebiBottom + 8);
            }
        }
        return top;
    }

    public static int getNotificationHeight() {
        if (notificationTicks <= 0 || notificationTitleComp == null || isConstellationNotif) return 0;
        var font = Minecraft.getInstance().font;
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int titleW = font.width(notificationTitleComp);
        int msgW = font.width(notificationMessageComp != null ? notificationMessageComp : net.minecraft.network.chat.Component.empty());
        int maxTextW = Math.max(titleW + 28, Math.min(380, (int) Math.sqrt(msgW * 180)));
        int boxWidth = Math.min(Math.max(250, maxTextW), width - 32);
        var lines = font.split(notificationMessageComp != null ? notificationMessageComp : net.minecraft.network.chat.Component.empty(), boxWidth - 16);
        int top = calculateNotificationTop();
        return top + 32 + lines.size() * 12;
    }

    public static void onChestReward(ChestRewardPayload payload) {
        if (Minecraft.getInstance().screen instanceof ChestOpeningScreen chestScreen) {
            chestScreen.receiveReward(payload);
        } else {
            ChestOpeningScreen screen = new ChestOpeningScreen(Minecraft.getInstance().screen, payload.boxId(), "awakening_system.screen.roulette.title", payload.tier(), 1000);
            Minecraft.getInstance().setScreen(screen);
            screen.receiveReward(payload);
        }
    }

    public static void openDungeonChestScreen(OpenDungeonChestPayload payload) {
        Minecraft.getInstance().setScreen(new ChestOpeningScreen(null, payload.boxId(), payload.nameKey(), payload.tier(), payload.cost()));
    }

    public static void openSponsorScreen(boolean forced) {
        Minecraft.getInstance().setScreen(new SponsorSelectionScreen(null, forced));
    }

    public static void openDungeonKeyScreen(int playerLevel, String currentRank) {
        Minecraft.getInstance().setScreen(new DungeonKeyScreen(playerLevel, currentRank));
    }

    public static void openGuidebookScreen() {
        Minecraft.getInstance().setScreen(new SystemGuidebookScreen());
    }

    public static void openBestiaryScreen() {
        Minecraft.getInstance().setScreen(new SystemBestiaryScreen());
    }

    public static void openBountyBoardScreen(OpenBountyBoardPayload payload) {
        Minecraft.getInstance().setScreen(new BountyBoardScreen(payload));
    }

    public static void openFishingStationScreen(OpenFishingStationPayload payload) {
        Minecraft.getInstance().setScreen(new FishingStationScreen(payload));
    }

    public static void openGuildReceptionScreen(OpenGuildReceptionPayload payload) {
        Minecraft.getInstance().setScreen(new GuildReceptionScreen(payload));
    }

    public static void openMurimAlchemyScreen(OpenMurimAlchemyPayload payload) {
        if (Minecraft.getInstance().screen instanceof MurimAlchemyScreen screen) {
            screen.updateState(payload.hasHeat());
        } else {
            Minecraft.getInstance().setScreen(new MurimAlchemyScreen(payload));
        }
    }

    public static void openWornBookScreen(int volume) {
        Minecraft.getInstance().setScreen(new WornBookScreen(volume));
    }

    public static void openTrilunarGuideScreen() {
        Minecraft.getInstance().setScreen(new TrilunarGuideScreen());
    }

    public static void openConstellationChannelScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            SystemData d = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (!d.isConstellation()) return;
        }
        Minecraft.getInstance().setScreen(new ConstellationChannelScreen());
    }

    public static void syncStarStream(java.util.List<SyncStarStreamPayload.IncarnationEntry> entries) {
        if (Minecraft.getInstance().screen instanceof ConstellationChannelScreen ccs) {
            ccs.updateIncarnations(entries);
        }
    }

    public static void openGachaScreen(OpenGachaScreenPayload payload) {
        if (Minecraft.getInstance().screen instanceof GachaWishScreen screen) {
            screen.updateAllData(
                    payload.primogems(),
                    payload.intertwinedFates(),
                    payload.acquaintFates(),
                    payload.stardust(),
                    payload.starglitter(),
                    payload.pity5Star(),
                    payload.pity4Star(),
                    payload.guaranteed(),
                    payload.beginnerPulls(),
                    payload.mora(),
                    payload.weaponBannerTarget(),
                    payload.weaponBannerFatePoints(),
                    payload.characterBannerTarget(),
                    payload.coins(),
                    payload.usdBalance(),
                    payload.genesisCrystals(),
                    payload.stellarJade(),
                    payload.welkinDays(),
                    payload.totalWishes(),
                    payload.wishHistory()
            );
        } else {
            Minecraft.getInstance().setScreen(new GachaWishScreen(
                    payload.primogems(),
                    payload.intertwinedFates(),
                    payload.acquaintFates(),
                    payload.stardust(),
                    payload.starglitter(),
                    payload.pity5Star(),
                    payload.pity4Star(),
                    payload.guaranteed(),
                    payload.beginnerPulls(),
                    payload.mora(),
                    payload.weaponBannerTarget(),
                    payload.weaponBannerFatePoints(),
                    payload.characterBannerTarget(),
                    payload.coins(),
                    payload.usdBalance(),
                    payload.genesisCrystals(),
                    payload.stellarJade(),
                    payload.welkinDays(),
                    payload.totalWishes(),
                    payload.wishHistory()
            ));
        }
    }

    public static void handleGachaWishResult(GachaWishResultPayload payload) {
        if (Minecraft.getInstance().screen instanceof GachaWishScreen screen) {
            screen.showResults(
                    payload.highestRarity(),
                    payload.wonItems(),
                    payload.itemRarities(),
                    payload.newPity5(),
                    payload.newGuaranteed(),
                    payload.newPrimos(),
                    payload.newIntertwined(),
                    payload.newAcquaint(),
                    payload.newStardust(),
                    payload.newStarglitter(),
                    payload.newBeginnerPulls(),
                    payload.newMora(),
                    payload.newWeaponTarget(),
                    payload.newWeaponFatePoints(),
                    payload.newCharTarget(),
                    payload.newTotalWishes()
            );
        }
    }

    public static void openSpiralAbyssScreen(com.soybe.awakeningsystem.abyss.OpenSpiralAbyssPayload payload) {
        if (Minecraft.getInstance().screen instanceof com.soybe.awakeningsystem.abyss.SpiralAbyssScreen screen) {
            screen.updateData(payload);
        } else {
            Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.abyss.SpiralAbyssScreen(payload));
        }
    }

    public static void openWelkinScreen(OpenWelkinScreenPayload payload) {
        Minecraft.getInstance().setScreen(new WelkinMoonScreen(
                payload.daysRemaining(),
                payload.rewardPrimos(),
                payload.canClaimToday(),
                payload.isEnhanced(),
                payload.questStep(),
                payload.questProgress()
        ));
    }

    public static void openCompanionScreen(com.soybe.awakeningsystem.network.OpenCompanionScreenPayload payload) {
        Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.client.gui.CompanionScreen(payload.entityId(), payload.data()));
    }

    public static void openCompanionAscensionScreen(com.soybe.awakeningsystem.network.OpenCompanionAscensionPayload payload) {
        if (Minecraft.getInstance().screen instanceof com.soybe.awakeningsystem.client.gui.CompanionAscensionScreen current) {
            current.updateData(payload.data());
        } else {
            Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.client.gui.CompanionAscensionScreen(payload.data()));
        }
    }

    public static void openSlotMachineScreen(com.soybe.awakeningsystem.casino.network.OpenSlotMachinePayload payload) {
        Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.casino.screen.SlotMachineScreen(
                payload.pos(), payload.moraBalance(), payload.betBase(), payload.linesMode()
        ));
    }

    public static void handleSlotSpinResult(com.soybe.awakeningsystem.casino.network.SlotSpinResultS2CPayload payload) {
        if (Minecraft.getInstance().screen instanceof com.soybe.awakeningsystem.casino.screen.SlotMachineScreen screen) {
            java.util.List<com.soybe.awakeningsystem.casino.slot.SlotLineResult> lines = new java.util.ArrayList<>();
            for (var w : payload.wins()) {
                lines.add(w.toLineResult());
            }
            screen.onSpinResult(
                    payload.stop1(), payload.stop2(), payload.stop3(),
                    payload.totalMoraWon(), payload.newMoraBalance(), payload.linesMode(),
                    payload.isJackpot(), lines
            );
        }
    }

    public static boolean isShadowSoldier(net.minecraft.world.entity.LivingEntity entity) {
        if (entity == null) return false;
        int id = entity.getId();
        if (SHADOW_SOLDIER_IDS.contains(id)) return true;
        if (NON_SHADOW_ENTITY_IDS.contains(id)) return false;

        if (SHADOW_EMERGENCE_MAP.containsKey(id)) {
            SHADOW_SOLDIER_IDS.add(id);
            return true;
        }
        net.minecraft.network.chat.Component customName = entity.getCustomName();
        if (customName != null && customName.getString().contains("[SOMBRA]")) {
            SHADOW_SOLDIER_IDS.add(id);
            return true;
        }
        if (entity.getTeam() != null && "shadows".equals(entity.getTeam().getName())) {
            SHADOW_SOLDIER_IDS.add(id);
            return true;
        }
        NON_SHADOW_ENTITY_IDS.add(id);
        return false;
    }

    public static void onRenderLivingPre(net.neoforged.neoforge.client.event.RenderLivingEvent.Pre<?, ?> event) {
        net.minecraft.world.entity.LivingEntity living = event.getEntity();
        int entityId = living.getId();

        // 1. Cinemática de emergencia: saliendo físicamente desde las profundidades del suelo
        ShadowEmergenceData emergence = SHADOW_EMERGENCE_MAP.get(entityId);
        if (emergence != null) {
            long elapsedMs = System.currentTimeMillis() - emergence.startTimeMs();
            if (elapsedMs < emergence.durationMs()) {
                float progress = Math.min(1.0F, Math.max(0.0F, (float) elapsedMs / (float) emergence.durationMs()));
                // Curva suave de elevación solemne (smoothstep)
                float smoothProgress = progress * progress * (3.0F - 2.0F * progress);
                float height = emergence.height() > 0.2F ? emergence.height() : living.getBbHeight();
                if (height < 0.5F) height = 1.8F;

                // Comienza sumergido (a -1.05 * altura bajo tierra) y asciende hasta el nivel del suelo (0.0)
                double submergeY = -(height * 1.05D * (1.0F - smoothProgress));
                event.getPoseStack().translate(0.0D, submergeY, 0.0D);
            } else {
                SHADOW_EMERGENCE_MAP.remove(entityId);
            }
        }

        // 2. Glow azulado/cian eléctrico distintivo y registro en equipo del scoreboard local (solo 1 vez por sombra)
        if (isShadowSoldier(living)) {
            if (CONFIGURED_SHADOW_TEAM_IDS.add(entityId)) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    net.minecraft.world.scores.Scoreboard csb = mc.level.getScoreboard();
                    net.minecraft.world.scores.PlayerTeam cteam = csb.getPlayerTeam("shadows");
                    if (cteam == null) {
                        cteam = csb.addPlayerTeam("shadows");
                        cteam.setColor(net.minecraft.ChatFormatting.AQUA);
                        cteam.setDisplayName(net.minecraft.network.chat.Component.literal("Sombras"));
                    } else if (cteam.getColor() != net.minecraft.ChatFormatting.AQUA) {
                        cteam.setColor(net.minecraft.ChatFormatting.AQUA);
                    }
                    String member = living.getScoreboardName();
                    if (!cteam.getPlayers().contains(member)) {
                        csb.addPlayerToTeam(member, cteam);
                    }
                    String uuidStr = living.getStringUUID();
                    if (!cteam.getPlayers().contains(uuidStr)) {
                        csb.addPlayerToTeam(uuidStr, cteam);
                    }
                }
            }
            // Forzar glowing en el cliente para asegurar la silueta AQUA
            living.setGlowingTag(true);
        }
    }

    public record PlayerBadgeData(
            java.util.UUID playerId,
            String hunterClass,
            int level,
            String rank,
            String guildRank,
            String constellationModifier,
            boolean isConstellation
    ) {}

    public static final java.util.Map<java.util.UUID, PlayerBadgeData> PLAYER_BADGES = new java.util.concurrent.ConcurrentHashMap<>();

    public static void handleSyncPlayerBadge(SyncPlayerBadgePayload payload) {
        PLAYER_BADGES.put(payload.playerId(), new PlayerBadgeData(
                payload.playerId(),
                payload.hunterClass(),
                payload.level(),
                payload.rank(),
                payload.guildRank(),
                payload.constellationModifier(),
                payload.isConstellation()
        ));
    }

    public static void onRenderNameTag(net.neoforged.neoforge.client.event.RenderNameTagEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            boolean isLocal = (player == mc.player);
            // Si es el jugador local y está en primera persona, NO renderizar para no obstruir la vista en pantalla
            if (isLocal && mc.options.getCameraType().isFirstPerson()) {
                return;
            }
            // En tercera persona (F5) para el jugador local, forzar que la placa se procese y dibuje
            if (isLocal) {
                event.setCanRender(net.neoforged.neoforge.common.util.TriState.TRUE);
            }

            // Si el jugador es invisible para el cliente local, no mostrar las placas
            if (player.isInvisibleTo(mc.player)) {
                return;
            }

            // Comprobar distancia de renderizado según si se agacha o no (32 bloques agachado, 64 normal)
            boolean isSneaking = player.isDiscrete();
            double maxDistSqr = isSneaking ? 1024.0D : 4096.0D;
            if (mc.getCameraEntity() != null && player.distanceToSqr(mc.getCameraEntity()) > maxDistSqr) {
                return;
            }

            // Asegurar que el nombre vanilla también se dibuje para otros jugadores
            if (!isLocal) {
                event.setCanRender(net.neoforged.neoforge.common.util.TriState.TRUE);
            }

            PlayerBadgeData badge = PLAYER_BADGES.get(player.getUUID());
            if (isLocal) {
                SystemData myData = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (myData != null) {
                    String gRank = (badge != null && badge.guildRank() != null && !badge.guildRank().isEmpty()) ? badge.guildRank() : "F";
                    badge = new PlayerBadgeData(
                            player.getUUID(),
                            myData.hunterClass(),
                            myData.level(),
                            myData.rank(),
                            gRank,
                            myData.constellationModifier(),
                            myData.isConstellation()
                    );
                }
            }

            if (badge == null) {
                badge = new PlayerBadgeData(
                        player.getUUID(),
                        "",
                        1,
                        "F",
                        "F",
                        "",
                        false
                );
            }

            renderPlayerBadges(event, player, badge);
        }
    }

    private static void renderPlayerBadges(net.neoforged.neoforge.client.event.RenderNameTagEvent event, net.minecraft.world.entity.player.Player player, PlayerBadgeData badge) {
        var poseStack = event.getPoseStack();
        var bufferSource = event.getMultiBufferSource();
        int light = event.getPackedLight();
        var font = Minecraft.getInstance().font;

        double tagX = 0.0D;
        double tagY = player.getBbHeight() + 0.5D;
        double tagZ = 0.0D;
        var attachments = player.getAttachments();
        if (attachments != null) {
            var tagPos = attachments.getNullable(net.minecraft.world.entity.EntityAttachment.NAME_TAG, 0, player.getViewYRot(event.getPartialTick()));
            if (tagPos != null) {
                tagX = tagPos.x;
                tagY = tagPos.y + 0.5D;
                tagZ = tagPos.z;
            }
        }

        poseStack.pushPose();
        poseStack.translate(tagX, tagY, tagZ);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);

        Component classComp = (badge.hunterClass() == null || badge.hunterClass().isEmpty()) ? Component.translatable("awakening_system.nametag.no_class") : switch (badge.hunterClass()) {
            case "shadow_monarch" -> Component.literal("§9👑 [").append(Component.translatable("awakening_system.class.shadow_monarch.name")).append(Component.literal("§9]"));
            case "sword_saint" -> Component.literal("§a⚔ [").append(Component.translatable("awakening_system.class.sword_saint.name")).append(Component.literal("§a]"));
            case "star_apostle" -> Component.literal("§e📜 [").append(Component.translatable("awakening_system.class.star_apostle.name")).append(Component.literal("§e]"));
            case "colossal_berserker" -> Component.literal("§c🪓 [").append(Component.translatable("awakening_system.class.colossal_berserker.name")).append(Component.literal("§c]"));
            case "demonic_cultivator" -> Component.literal("§5🔥 [").append(Component.translatable("awakening_system.class.demonic_cultivator.name")).append(Component.literal("§5]"));
            case "void_assassin" -> Component.literal("§8🗡 [").append(Component.translatable("awakening_system.class.void_assassin.name")).append(Component.literal("§8]"));
            case "grand_archmage" -> Component.literal("§d🔮 [").append(Component.translatable("awakening_system.class.grand_archmage.name")).append(Component.literal("§d]"));
            case "dragon_monarch" -> Component.literal("§4🐉 [").append(Component.translatable("awakening_system.class.dragon_monarch.name")).append(Component.literal("§4]"));
            default -> Component.literal("§b[").append(Component.literal(badge.hunterClass())).append(Component.literal("§b]"));
        };

        String rank = (badge.rank() == null || badge.rank().isEmpty()) ? "F" : badge.rank();
        String guildRank = (badge.guildRank() == null || badge.guildRank().isEmpty()) ? "F" : badge.guildRank();

        Component rankComp = Component.translatable("awakening_system.nametag.rank_level", rank, badge.level());
        Component guildComp = Component.translatable("awakening_system.nametag.guild", guildRank);

        java.util.List<Component> lines = new java.util.ArrayList<>();
        lines.add(guildComp);

        if (badge.isConstellation() && badge.constellationModifier() != null && !badge.constellationModifier().isEmpty()) {
            lines.add(Component.literal("§e✨ «").append(ConstellationAscension.getTranslatedTitleComponent(badge.constellationModifier())).append(Component.literal("§e»")));
        }

        lines.add(classComp);
        lines.add(rankComp);

        boolean isSneaking = player.isDiscrete();
        float baseOffset = "deadmau5".equals(player.getName().getString()) ? -10.0F : 0.0F;
        var matrix = poseStack.last().pose();

        // En Minecraft, el nombre vanilla del jugador se renderiza en Y = 0.
        // Las líneas de títulos y rangos deben colocarse en coordenadas Y negativas para flotar ARRIBA del nombre:
        float curY = baseOffset - 10.0F;
        for (int i = lines.size() - 1; i >= 0; i--) {
            drawNameplateLine(font, bufferSource, matrix, light, lines.get(i), curY, isSneaking);
            curY -= 10.0F;
        }

        poseStack.popPose();
    }

    private static void drawNameplateLine(net.minecraft.client.gui.Font font, net.minecraft.client.renderer.MultiBufferSource bufferSource, org.joml.Matrix4f matrix, int light, Component text, float y, boolean isSneaking) {
        float x = font.width(text) / -2.0F;
        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int) (opacity * 255.0F) << 24;
        if (isSneaking) {
            font.drawInBatch(text, x, y, 553648127, false, matrix, bufferSource, net.minecraft.client.gui.Font.DisplayMode.NORMAL, bgColor, light);
        } else {
            font.drawInBatch(text, x, y, 553648127, false, matrix, bufferSource, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, bgColor, light);
            font.drawInBatch(text, x, y, -1, false, matrix, bufferSource, net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, light);
        }
    }

    public static void onRenderPlayerPre(net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof com.soybe.awakeningsystem.entity.FlyingSwordEntity) {
            var model = event.getRenderer().getModel();
            model.riding = false;
            // Postura erguida de cultivador sobre la espada voladora
            model.leftLeg.xRot = 0.0F;
            model.leftLeg.yRot = 0.0F;
            model.leftLeg.zRot = 0.0F;
            model.rightLeg.xRot = 0.0F;
            model.rightLeg.yRot = 0.0F;
            model.rightLeg.zRot = 0.0F;
        } else if (event.getEntity().getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity) {
            var model = event.getRenderer().getModel();
            model.riding = true;
            // Padmasana: postura de piernas cruzadas en loto
            model.leftLeg.xRot = -1.45F;
            model.leftLeg.yRot = 0.52F;
            model.leftLeg.zRot = 0.35F;
            model.rightLeg.xRot = -1.45F;
            model.rightLeg.yRot = -0.52F;
            model.rightLeg.zRot = -0.35F;
            // Dhyana Mudra: postura de meditación con palmas sobre las rodillas
            model.leftArm.xRot = -0.70F;
            model.leftArm.yRot = 0.35F;
            model.leftArm.zRot = -0.25F;
            model.rightArm.xRot = -0.70F;
            model.rightArm.yRot = -0.35F;
            model.rightArm.zRot = 0.25F;
        }

        var player = event.getEntity();
        if (isPlayerCastingAttack(player.getId())) {
            var model = event.getRenderer().getModel();
            float pitch = player.getXRot();
            if (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT) {
                model.leftArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW;
                model.leftArm.xRot = (float) Math.toRadians(-90.0F + pitch);
                model.leftArm.yRot = (float) Math.toRadians(4.0F);
                model.leftArm.zRot = 0.0F;
            } else {
                model.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW;
                model.rightArm.xRot = (float) Math.toRadians(-90.0F + pitch);
                model.rightArm.yRot = (float) Math.toRadians(-4.0F);
                model.rightArm.zRot = 0.0F;
            }
        }
    }

    public static boolean isPlayerCastingAttack(int entityId) {
        if (entityId <= 0) return false;
        for (ActiveMagicCircle circle : ACTIVE_MAGIC_CIRCLES) {
            if (circle.casterEntityId == entityId && (circle.circleType == 1 || circle.circleType == 3)) {
                return true;
            }
        }
        return false;
    }

    public static void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
        if (event.getStage() != net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var cam = event.getCamera();
        var camPos = cam.getPosition();
        var poseStack = event.getPoseStack();
        var bufferSource = mc.renderBuffers().bufferSource();

        float gameTime = mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(true);

        // Renderizado Volumétrico de Auras 3D (Ki de Cultivo, Modo Saiyan y Creador)
        com.soybe.awakeningsystem.client.renderer.VolumetricAuraRenderer.renderAuras(event);

        // Previsualización Holográfica de Tendido de Vías Estilo Create (Línea láser y balizas 3D)
        com.soybe.awakeningsystem.client.track.TrackPlacementPreview.renderPreview(event);

        if (ACTIVE_RIFTS.isEmpty() && ACTIVE_MAGIC_CIRCLES.isEmpty()) return;

        for (SyncRiftsPayload.RiftEntry rift : ACTIVE_RIFTS) {
            double rx = rift.pos().getX() + 0.5D - camPos.x;
            double rz = rift.pos().getZ() + 0.5D - camPos.z;
            boolean isIsekai = "ISEKAI".equalsIgnoreCase(rift.rank()) || rift.type() == 99;
            boolean isMurim = "MURIM".equalsIgnoreCase(rift.rank()) || rift.type() == 98;
            boolean isTeyvatAbyss = "TEYVAT_ABYSS".equalsIgnoreCase(rift.rank()) || rift.type() == 97;
            boolean isTeyvatAbyssBlocked = "TEYVAT_ABYSS_BLOCKED".equalsIgnoreCase(rift.rank()) || rift.type() == 96;
            boolean isDimensional = isIsekai || isMurim || isTeyvatAbyss || isTeyvatAbyssBlocked;
            boolean isRedGate = rift.isRedGate();
            boolean isDungeonExit = (rift.type() == 1);

            float hw, hh;
            double ry;
            if (isTeyvatAbyss || isTeyvatAbyssBlocked) {
                // Vórtice Celestial de la Espiral del Abismo de Teyvat
                // Escala ampliada (hw = 5.0F) para cubrir completamente el vano circular (7 bloques)
                // y ocultar al 100% los bordes/esquinas giratorias de la textura tras el marco de cobre
                hw = 5.0F;
                hh = 5.0F;
                ry = rift.pos().getY() + 0.5D - camPos.y;
            } else if (isDimensional) {
                // Arco Monumental Dimensional (Torii Isekai / Murim Pagoda):
                // Centrado geométricamente en el vano interior (suelo cy=0, techo cy=5, centro vertical cy=2.55)
                hw = 1.45F;
                hh = 2.0F;
                ry = rift.pos().getY() + 2.55D - camPos.y;
            } else if (isDungeonExit) {
                // Portal de Salida en la Sala 1 del calabozo: proporciones que encajan en el templo
                hw = 1.4F;
                hh = 1.9F;
                ry = rift.pos().getY() + 2.2D - camPos.y;
            } else {
                // Puerta de Mazmorra en el Overworld: colosal e imponente como en Solo Leveling (escalada por rango)
                float size = getGateSize(rift);
                hw = size;
                hh = size;
                ry = rift.pos().getY() + size + 0.3D - camPos.y;
            }

            double distSq = rx * rx + ry * ry + rz * rz;
            double maxDist = isDimensional ? 360.0D : 240.0D;
            if (distSq > maxDist * maxDist) continue;

            poseStack.pushPose();
            poseStack.translate(rx, ry, rz);

            // 1. Orientación fija anclada en el mundo 3D (NO sigue la cámara del jugador)
            if (isTeyvatAbyss || isTeyvatAbyssBlocked) {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rift.yaw()));
            } else if (isDimensional || isDungeonExit) {
                // Alineado perfectamente con el marco del templo/arco (plano X-Y)
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0.0F));
            } else {
                // Portales del Overworld: orientación anclada mirando al jugador que invocó la puerta con la llave
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rift.yaw()));
            }

            // 2. Animación orgánica de respiración dimensional (sutil pulso de vida)
            float pulse = 1.0F + 0.02F * (float) Math.sin(gameTime * 0.10D);
            float curHw = hw * pulse;
            float curHh = hh * pulse;

            // 3. Suave oscilación de flotación vertical (solo portales del Overworld al aire libre)
            if (!isDimensional && !isDungeonExit) {
                double bob = Math.sin(gameTime * 0.08D) * 0.04D;
                poseStack.translate(0.0D, bob, 0.0D);
            }

            // 4. Animación fluida de vórtice (100 frames del GIF original en sprite-sheet 10x10)
            int frameIndex = Math.floorMod((int) (gameTime * 3.33F), 100);
            int col = frameIndex % 10;
            int row = frameIndex / 10;
            float u0 = col * 0.10F;
            float u1 = (col + 1) * 0.10F;
            float v0 = row * 0.10F;
            float v1 = (row + 1) * 0.10F;

            // Opacidad según estado: Activo (245, opaco/denso) vs Bloqueado (55, tenue/translúcido permitiendo ver el cielo)
            int mainAlpha = isTeyvatAbyssBlocked ? 55 : 245;
            int secAlpha = isTeyvatAbyssBlocked ? 35 : 120;

            var texture = getGateTexture(rift);
            var builder = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucentCull(texture));
            var matrix = poseStack.last().pose();

            // Vórtice Dimensional Limpio (Cara Frontal)
            builder.addVertex(matrix, -curHw, -curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u0, v1)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            builder.addVertex(matrix, curHw, -curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u1, v1)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            builder.addVertex(matrix, curHw, curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u1, v0)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            builder.addVertex(matrix, -curHw, curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u0, v0)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);

            // Vórtice Dimensional Limpio (Cara Trasera)
            builder.addVertex(matrix, curHw, -curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u0, v1)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
            builder.addVertex(matrix, -curHw, -curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u1, v1)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
            builder.addVertex(matrix, -curHw, curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u1, v0)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
            builder.addVertex(matrix, curHw, curHh, 0.0F).setColor(255, 255, 255, mainAlpha).setUv(u0, v0)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);

            // Capa secundaria de resonancia dimensional contra-rotativa para portales de dimensiones ("mezcla de colores" viva en 3D)
            if (isDimensional) {
                int counterFrame = Math.floorMod((int) (-gameTime * 1.85F), 100);
                int cCol = counterFrame % 10;
                int cRow = counterFrame / 10;
                float cu0 = cCol * 0.10F;
                float cu1 = (cCol + 1) * 0.10F;
                float cv0 = cRow * 0.10F;
                float cv1 = (cRow + 1) * 0.10F;
                float cHw = curHw * 0.94F;
                float cHh = curHh * 0.94F;
                float zOff = 0.015F;

                // Frente secundario
                builder.addVertex(matrix, -cHw, -cHh, zOff).setColor(255, 255, 255, secAlpha).setUv(cu0, cv1)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
                builder.addVertex(matrix, cHw, -cHh, zOff).setColor(255, 255, 255, secAlpha).setUv(cu1, cv1)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
                builder.addVertex(matrix, cHw, cHh, zOff).setColor(255, 255, 255, secAlpha).setUv(cu1, cv0)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
                builder.addVertex(matrix, -cHw, cHh, zOff).setColor(255, 255, 255, secAlpha).setUv(cu0, cv0)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);

                // Dorso secundario
                builder.addVertex(matrix, cHw, -cHh, -zOff).setColor(255, 255, 255, secAlpha).setUv(cu0, cv1)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
                builder.addVertex(matrix, -cHw, -cHh, -zOff).setColor(255, 255, 255, secAlpha).setUv(cu1, cv1)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
                builder.addVertex(matrix, -cHw, cHh, -zOff).setColor(255, 255, 255, secAlpha).setUv(cu1, cv0)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
                builder.addVertex(matrix, cHw, cHh, -zOff).setColor(255, 255, 255, secAlpha).setUv(cu0, cv0)
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
            }

            poseStack.popPose();
        }

        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        for (ActiveMagicCircle circle : ACTIVE_MAGIC_CIRCLES) {
            double cx = circle.x;
            double cy = circle.y;
            double cz = circle.z;
            float lookX = circle.lookX;
            float lookY = circle.lookY;
            float lookZ = circle.lookZ;

            float yaw, pitch;
            if (circle.casterEntityId > 0 && mc.level != null) {
                net.minecraft.world.entity.Entity caster = (mc.player != null && mc.player.getId() == circle.casterEntityId)
                        ? mc.player
                        : mc.level.getEntity(circle.casterEntityId);

                if (caster != null && caster.isAlive()) {
                    boolean isGround = (circle.circleType == 0 || circle.circleType == 2);
                    if (isGround) {
                        cx = net.minecraft.util.Mth.lerp((double) pt, caster.xo, caster.getX());
                        cy = net.minecraft.util.Mth.lerp((double) pt, caster.yo, caster.getY());
                        cz = net.minecraft.util.Mth.lerp((double) pt, caster.zo, caster.getZ());
                        yaw = 0.0F;
                        pitch = 0.0F;
                    } else {
                        // Círculo vertical en frente del usuario / caster
                        double dist = (circle.maxRadius > 4.0F) ? Math.max(2.5D, Math.min(5.0D, circle.maxRadius * 0.40D)) : 1.65D;

                        Vec3 eyePos;
                        Vec3 look;
                        if (caster == mc.player) {
                            eyePos = mc.options.getCameraType().isFirstPerson() ? camPos : mc.player.getEyePosition(pt);
                            look = mc.player.getViewVector(pt).normalize();
                            yaw = mc.player.getViewYRot(pt);
                            pitch = mc.player.getViewXRot(pt);
                        } else if (caster instanceof net.minecraft.world.entity.LivingEntity living) {
                            eyePos = living.getEyePosition(pt);
                            look = living.getViewVector(pt).normalize();
                            yaw = living.getViewYRot(pt);
                            pitch = living.getViewXRot(pt);
                        } else {
                            eyePos = caster.position().add(0, caster.getEyeHeight(), 0);
                            look = caster.getLookAngle().normalize();
                            yaw = caster.getYRot();
                            pitch = caster.getXRot();
                        }

                        cx = eyePos.x + look.x * dist;
                        cy = eyePos.y + look.y * dist;
                        cz = eyePos.z + look.z * dist;
                        lookX = (float) look.x;
                        lookY = (float) look.y;
                        lookZ = (float) look.z;
                    }
                } else {
                    yaw = (float) Math.toDegrees(Math.atan2(-lookX, lookZ));
                    pitch = (float) Math.toDegrees(Math.asin(Math.max(-1.0F, Math.min(1.0F, -lookY))));
                }
            } else {
                yaw = (float) Math.toDegrees(Math.atan2(-lookX, lookZ));
                pitch = (float) Math.toDegrees(Math.asin(Math.max(-1.0F, Math.min(1.0F, -lookY))));
            }

            double rx = cx - camPos.x;
            double ry = cy - camPos.y;
            double rz = cz - camPos.z;
            double distSq = rx * rx + ry * ry + rz * rz;
            if (distSq > 96.0D * 96.0D) continue;

            float currentAge = circle.currentTick + pt;
            float currentRadius;
            float spinMult = 1.0F;

            if (circle.isCharging) {
                float chargeRatio = Math.min(1.0F, currentAge / (float) Math.max(1, circle.maxTicks));
                currentRadius = circle.maxRadius * (0.35F + 0.65F * (float) Math.sin(chargeRatio * (Math.PI / 2.0)));
                spinMult = 1.0F + 2.5F * chargeRatio;
            } else {
                float scaleProgress = Math.min(1.0F, currentAge / 5.0F);
                currentRadius = circle.maxRadius * (0.2F + 0.8F * (float) Math.sin(scaleProgress * (Math.PI / 2.0)));
            }

            float alphaFactor = 1.0F;
            if (circle.currentTick < 4) {
                alphaFactor = (circle.currentTick + pt) / 4.0F;
            } else if (circle.maxTicks - circle.currentTick < 12) {
                alphaFactor = Math.max(0.0F, (circle.maxTicks - (circle.currentTick + pt)) / 12.0F);
            }

            int color = circle.color;
            int cr = (color >> 16) & 0xFF;
            int cg = (color >> 8) & 0xFF;
            int cb = color & 0xFF;
            int ca = (int) (240 * alphaFactor);
            if (ca <= 0) continue;

            boolean isMurim = (circle.circleType == 2 || circle.circleType == 3);
            var texture = isMurim ? MAGIC_CIRCLE_MURIM : MAGIC_CIRCLE_ARCANE;
            var builder = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(texture));

            poseStack.pushPose();

            boolean isGround = (circle.circleType == 0 || circle.circleType == 2);
            if (isGround) {
                // Ground horizontal circle
                poseStack.translate(rx, ry + 0.05D, rz);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(gameTime * circle.spinSpeed * spinMult * 6.0F));

                var matrix = poseStack.last().pose();
                // Face A
                renderMagicQuad(builder, matrix, -currentRadius, -currentRadius, currentRadius, currentRadius, 0.0F, cr, cg, cb, ca);
                // Face B
                renderMagicQuad(builder, matrix, currentRadius, -currentRadius, -currentRadius, currentRadius, 0.0F, cr, cg, cb, ca);

                // Contra-rotating inner concentric seal for extra magical anime complexity
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-gameTime * circle.spinSpeed * spinMult * 14.0F));
                float innerR = currentRadius * 0.65F;
                int inR = Math.min(255, cr + 30);
                int inG = Math.min(255, cg + 30);
                int inB = Math.min(255, cb + 30);
                int inA = (int) (ca * 0.85F);
                var innerMatrix = poseStack.last().pose();
                renderMagicQuad(builder, innerMatrix, -innerR, -innerR, innerR, innerR, 0.005F, inR, inG, inB, inA);
                renderMagicQuad(builder, innerMatrix, innerR, -innerR, -innerR, innerR, 0.005F, inR, inG, inB, inA);
            } else {
                // Vertical casting circle floating in 3D space: perfectly faces user/look direction
                poseStack.translate(rx, ry, rz);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yaw));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitch));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(gameTime * circle.spinSpeed * spinMult * 8.0F));

                var matrix = poseStack.last().pose();
                renderMagicQuad(builder, matrix, -currentRadius, -currentRadius, currentRadius, currentRadius, 0.0F, cr, cg, cb, ca);
                renderMagicQuad(builder, matrix, currentRadius, -currentRadius, -currentRadius, currentRadius, 0.0F, cr, cg, cb, ca);

                // Contra-rotating inner concentric seal for vertical glyphs
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-gameTime * circle.spinSpeed * spinMult * 16.0F));
                float innerR = currentRadius * 0.65F;
                int inR = Math.min(255, cr + 30);
                int inG = Math.min(255, cg + 30);
                int inB = Math.min(255, cb + 30);
                int inA = (int) (ca * 0.85F);
                var innerMatrix = poseStack.last().pose();
                renderMagicQuad(builder, innerMatrix, -innerR, -innerR, innerR, innerR, 0.005F, inR, inG, inB, inA);
                renderMagicQuad(builder, innerMatrix, innerR, -innerR, -innerR, innerR, 0.005F, inR, inG, inB, inA);
            }

            poseStack.popPose();
        }
    }

    private static void renderMagicQuad(com.mojang.blaze3d.vertex.VertexConsumer builder, org.joml.Matrix4f matrix,
                                        float x0, float y0, float x1, float y1, float z,
                                        int r, int g, int b, int a) {
        builder.addVertex(matrix, x0, y0, z).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        builder.addVertex(matrix, x1, y0, z).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        builder.addVertex(matrix, x1, y1, z).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        builder.addVertex(matrix, x0, y1, z).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
    }

    public static void onComputeCameraAngles(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        if (!bossCinematicActive) {
            cinematicAnglesInitialized = false;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (!cinematicAnglesInitialized) {
            cinematicCurrentYaw = mc.player.getYRot();
            cinematicCurrentPitch = mc.player.getXRot();
            cinematicAnglesInitialized = true;
        }

        // Transición de entrada (primeros 20 ticks) y salida (últimos 20 ticks)
        float blendIn = Math.min(1.0F, (bossCinematicTotalTicks - bossCinematicTicks) / 20.0F);
        float blendOut = Math.min(1.0F, bossCinematicTicks / 20.0F);
        float blendWeight = blendIn * blendOut;

        double targetX = bossCinematicAltarPos.getX() + 0.5D;
        double targetY = bossCinematicAltarPos.getY() + 140.0D; // Mirar directamente al cenit celestial del cielo
        double targetZ = bossCinematicAltarPos.getZ() + 0.5D;

        // Si el dragón jefe está cargado en el cliente, enfocar la cámara directamente a él conforme baja en picada
        net.minecraft.world.entity.Entity dragon = mc.level.getEntity(bossCinematicDragonId);
        if (dragon != null) {
            targetX = dragon.getX();
            targetY = dragon.getY();
            targetZ = dragon.getZ();
        }

        double dx = targetX - mc.player.getX();
        double dy = targetY - (mc.player.getY() + mc.player.getEyeHeight());
        double dz = targetZ - mc.player.getZ();
        double horizDist = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));

        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float targetPitch = (float) (-(Mth.atan2(dy, horizDist) * (180.0D / Math.PI)));
        targetPitch = Mth.clamp(targetPitch, -86.0F, 86.0F);

        // Fijar y rastrear rápida y fluidamente
        float step = 0.22F;
        cinematicCurrentYaw += Mth.wrapDegrees(targetYaw - cinematicCurrentYaw) * step;
        cinematicCurrentPitch = Mth.lerp(step, cinematicCurrentPitch, targetPitch);

        // Mezclar con la orientación libre del jugador según blendWeight
        float finalYaw = Mth.rotLerp(blendWeight, event.getYaw(), cinematicCurrentYaw);
        float finalPitch = Mth.lerp(blendWeight, event.getPitch(), cinematicCurrentPitch);

        // Sincronizar el jugador para que al salir no dé un salto brusco de vista
        mc.player.setYRot(finalYaw);
        mc.player.setXRot(finalPitch);

        // Temblor de pantalla cósmico (Screen rumble) por la tormenta y el rugido del titán
        float time = (float) mc.level.getGameTime() + (float) event.getPartialTick();
        float rumble = (float) Math.sin(time * 1.8D) * (1.2F * blendWeight);
        float rollRumble = (float) Math.cos(time * 1.4D) * (0.8F * blendWeight);

        event.setYaw(finalYaw);
        event.setPitch(finalPitch + rumble);
        event.setRoll(event.getRoll() + rollRumble);
    }

    public static void registerClientCommands(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                net.minecraft.commands.Commands.literal("hud")
                        .executes(ctx -> {
                            SystemHudOverlay.HUD_POSITION = switch (SystemHudOverlay.HUD_POSITION) {
                                case TOP_LEFT -> SystemHudOverlay.HudPosition.TOP_RIGHT;
                                case TOP_RIGHT -> SystemHudOverlay.HudPosition.BOTTOM_LEFT;
                                case BOTTOM_LEFT -> SystemHudOverlay.HudPosition.ABOVE_HOTBAR;
                                case ABOVE_HOTBAR -> SystemHudOverlay.HudPosition.TOP_LEFT;
                            };
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                        net.minecraft.network.chat.Component.translatable("awakening_system.client.hud_position_changed", SystemHudOverlay.HUD_POSITION), false);
                            }
                            return 1;
                        })
                        .then(net.minecraft.commands.Commands.literal("edit").executes(ctx -> {
                            Minecraft.getInstance().tell(() -> com.soybe.awakeningsystem.client.gui.HudEditorScreen.open(null));
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("top_left").executes(ctx -> {
                            SystemHudOverlay.HUD_POSITION = SystemHudOverlay.HudPosition.TOP_LEFT;
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                        net.minecraft.network.chat.Component.translatable("awakening_system.hud.pos.top_left"), false);
                            }
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("top_right").executes(ctx -> {
                            SystemHudOverlay.HUD_POSITION = SystemHudOverlay.HudPosition.TOP_RIGHT;
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                        net.minecraft.network.chat.Component.translatable("awakening_system.hud.pos.top_right"), false);
                            }
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("bottom_left").executes(ctx -> {
                            SystemHudOverlay.HUD_POSITION = SystemHudOverlay.HudPosition.BOTTOM_LEFT;
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                        net.minecraft.network.chat.Component.translatable("awakening_system.hud.pos.bottom_left"), false);
                            }
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("above_hotbar").executes(ctx -> {
                            SystemHudOverlay.HUD_POSITION = SystemHudOverlay.HudPosition.ABOVE_HOTBAR;
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                        net.minecraft.network.chat.Component.translatable("awakening_system.hud.pos.above_hotbar"), false);
                            }
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("skills")
                                .executes(ctx -> {
                                    SystemHudOverlay.SKILL_BAR_POSITION = switch (SystemHudOverlay.SKILL_BAR_POSITION) {
                                        case BOTTOM_LEFT -> SystemHudOverlay.SkillBarPosition.ABOVE_HOTBAR;
                                        case ABOVE_HOTBAR -> SystemHudOverlay.SkillBarPosition.BOTTOM_LEFT;
                                    };
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.skills.changed", SystemHudOverlay.SKILL_BAR_POSITION.toString()), false);
                                    }
                                    return 1;
                                })
                                .then(net.minecraft.commands.Commands.literal("bottom_left").executes(ctx -> {
                                    SystemHudOverlay.SKILL_BAR_POSITION = SystemHudOverlay.SkillBarPosition.BOTTOM_LEFT;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.skills.bottom_left"), false);
                                    }
                                    return 1;
                                }))
                                .then(net.minecraft.commands.Commands.literal("above_hotbar").executes(ctx -> {
                                    SystemHudOverlay.SKILL_BAR_POSITION = SystemHudOverlay.SkillBarPosition.ABOVE_HOTBAR;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.skills.above_hotbar"), false);
                                    }
                                    return 1;
                                }))
                        )
                        .then(net.minecraft.commands.Commands.literal("scale")
                                .executes(ctx -> {
                                    SystemHudOverlay.CUSTOM_HUD_SCALE = 0.0F;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.scale.auto"), false);
                                    }
                                    return 1;
                                })
                                .then(net.minecraft.commands.Commands.literal("auto").executes(ctx -> {
                                    SystemHudOverlay.CUSTOM_HUD_SCALE = 0.0F;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.scale.auto"), false);
                                    }
                                    return 1;
                                }))
                                .then(net.minecraft.commands.Commands.literal("small").executes(ctx -> {
                                    SystemHudOverlay.CUSTOM_HUD_SCALE = 0.75F;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.scale.small"), false);
                                    }
                                    return 1;
                                }))
                                .then(net.minecraft.commands.Commands.literal("medium").executes(ctx -> {
                                    SystemHudOverlay.CUSTOM_HUD_SCALE = 1.0F;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.scale.normal"), false);
                                    }
                                    return 1;
                                }))
                                .then(net.minecraft.commands.Commands.literal("large").executes(ctx -> {
                                    SystemHudOverlay.CUSTOM_HUD_SCALE = 1.25F;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.scale.large"), false);
                                    }
                                    return 1;
                                }))
                                .then(net.minecraft.commands.Commands.argument("value", com.mojang.brigadier.arguments.FloatArgumentType.floatArg(0.4F, 2.0F))
                                        .executes(ctx -> {
                                            float v = com.mojang.brigadier.arguments.FloatArgumentType.getFloat(ctx, "value");
                                            SystemHudOverlay.CUSTOM_HUD_SCALE = v;
                                            if (Minecraft.getInstance().player != null) {
                                                Minecraft.getInstance().player.displayClientMessage(
                                                        net.minecraft.network.chat.Component.translatable("awakening_system.hud.scale.custom", String.valueOf(v)), false);
                                            }
                                            return 1;
                                        }))
                        )
                        .then(net.minecraft.commands.Commands.literal("hearts")
                                .executes(ctx -> {
                                    SystemNotificationOverlay.HIDE_VANILLA_HEARTS = !SystemNotificationOverlay.HIDE_VANILLA_HEARTS;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                SystemNotificationOverlay.HIDE_VANILLA_HEARTS
                                                        ? net.minecraft.network.chat.Component.translatable("awakening_system.hud.hearts.hidden")
                                                        : net.minecraft.network.chat.Component.translatable("awakening_system.hud.hearts.vanilla"), false);
                                    }
                                    return 1;
                                })
                                .then(net.minecraft.commands.Commands.literal("vanilla").executes(ctx -> {
                                    SystemNotificationOverlay.HIDE_VANILLA_HEARTS = false;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.hearts.vanilla"), false);
                                    }
                                    return 1;
                                }))
                                .then(net.minecraft.commands.Commands.literal("hide").executes(ctx -> {
                                    SystemNotificationOverlay.HIDE_VANILLA_HEARTS = true;
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.displayClientMessage(
                                                net.minecraft.network.chat.Component.translatable("awakening_system.hud.hearts.hidden"), false);
                                    }
                                    return 1;
                                }))
                        )
        );
    }

    public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        if (event.getItemStack() != null && com.soybe.awakeningsystem.item.WeaponLevelManager.isLevelableWeapon(event.getItemStack())) {
            com.soybe.awakeningsystem.item.WeaponLevelManager.appendWeaponLevelTooltip(event.getItemStack(), event.getToolTip());
        }
    }
}

