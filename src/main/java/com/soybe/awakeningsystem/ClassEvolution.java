package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Seven simple base classes that evolve into the advanced classes. A player picks a base class at level 50; at level
 * {@link #EVOLVE_LEVEL} a trial opens (beat the base class's trial boss), and after winning it the player picks
 * one of the base class's evolutions. An evolved player keeps the base class's web and what was learnt in it.
 * Players who already had an advanced class are evolved already: their base class is worked out from the class.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class ClassEvolution {
    private ClassEvolution() {}

    public static final int EVOLVE_LEVEL = 150;
    /** How long a trial boss waits to be beaten. */
    private static final int TRIAL_TICKS = 20 * 60 * 10;

    public record Base(String id, String startSkill, String passiveSkill, int color, List<String> evolutions,
                       Supplier<? extends EntityType<? extends Mob>> trialBoss) {}

    private static final List<Base> BASES = List.of(
            new Base("base_warrior", "wa_power_slash", "wa_battle_spirit", 0xFFFF7043,
                    List.of("sword_saint", "colossal_berserker", "beast_monarch"), com.soybe.awakeningsystem.entity.ModEntities.IGRIS_BOSS),
            new Base("base_assassin", "as_shadow_dash", "as_backstab", 0xFF26C6DA,
                    List.of("void_assassin"), com.soybe.awakeningsystem.entity.ModEntities.BERU_BOSS),
            // the Shadow Monarch comes from the Necromancer, as in Solo Leveling (its trial is Igris, from the job change)
            new Base("base_necromancer", "ne_shadow_extraction", "ne_soul_harvest", 0xFF9966FF,
                    List.of("shadow_monarch"), com.soybe.awakeningsystem.entity.ModEntities.IGRIS_BOSS),
            new Base("base_tank", "ta_provoke", "ta_sturdy", 0xFF90A4AE,
                    List.of("iron_bastion", "dragon_monarch"), com.soybe.awakeningsystem.entity.ModEntities.JADE_GUARDIAN_BOSS),
            new Base("base_mage", "ma_mana_bolt", "ma_mana_flow", 0xFF7C4DFF,
                    List.of("grand_archmage", "star_apostle", "lightning_emperor"), com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_LORD),
            new Base("base_healer", "he_mend", "he_renewal", 0xFF69F0AE,
                    List.of("spiritual_healer"), com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS),
            new Base("base_cultivator", "cu_qi_palm", "cu_inner_breath", 0xFFE040FB,
                    List.of("demonic_cultivator", "blood_emperor"), com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS)
    );

    public static List<Base> bases() {
        return BASES;
    }

    public static Base base(String id) {
        for (Base b : BASES) if (b.id().equals(id)) return b;
        return null;
    }

    public static boolean isBase(String cls) {
        return base(cls) != null;
    }

    /** The base class a class comes from (itself for a base class), or null for an unknown class. */
    public static String baseOf(String cls) {
        if (cls == null || cls.isEmpty()) return null;
        String n = ClassSkillTree.normalizeClassId(cls);
        if (isBase(n)) return n;
        for (Base b : BASES) if (b.evolutions().contains(n)) return b.id();
        return null;
    }

    /**
     * The player's base class. A Shadow Monarch who evolved from the Assassin (before the Necromancer existed)
     * keeps the Assassin's web, with what was learnt in it.
     */
    public static String baseOf(SystemData data) {
        if (data == null) return null;
        String base = baseOf(data.hunterClass());
        if ("base_necromancer".equals(base) && !data.skills().containsKey("ne_shadow_extraction")
                && (data.skills().containsKey("as_shadow_dash")
                    || data.treeNodes().stream().anyMatch(n -> n.startsWith("base_assassin_")))) {
            return "base_assassin";
        }
        return base;
    }

    /** All the base classes' own skill ids (the start actives and the passives). */
    public static boolean isBaseSkill(String skillId) {
        for (Base b : BASES) if (b.startSkill().equals(skillId) || b.passiveSkill().equals(skillId)) return true;
        return false;
    }

    public static boolean isBaseActive(String skillId) {
        for (Base b : BASES) if (b.startSkill().equals(skillId)) return true;
        return false;
    }

    // ---- the trial

    /** trial boss → the player who called it, and the player → when the trial ends. */
    private static final Map<UUID, UUID> BOSS_OWNER = new HashMap<>();
    private static final Map<UUID, Long> DEADLINE = new HashMap<>();
    private static final Map<UUID, UUID> OWNER_BOSS = new HashMap<>();

    public static boolean trialRunning(ServerPlayer player) {
        return OWNER_BOSS.containsKey(player.getUUID());
    }

    /** Calls the trial boss in front of the player. */
    public static boolean startTrial(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        Base b = base(data.hunterClass());
        if (b == null) return false;
        if (data.level() < EVOLVE_LEVEL) {
            AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.need_level", -43691, EVOLVE_LEVEL);
            return false;
        }
        if (data.evolutionTrialPassed()) return false;
        if (trialRunning(player)) {
            AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.trial_running", -43691);
            return false;
        }
        ServerLevel level = player.serverLevel();
        Mob boss = b.trialBoss().get().create(level);
        if (boss == null) return false;
        var look = player.getLookAngle();
        int x = (int) Math.floor(player.getX() + look.x * 8), z = (int) Math.floor(player.getZ() + look.z * 8);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(y - player.getY()) > 6) {
            x = player.getBlockX();
            z = player.getBlockZ();
            y = player.getBlockY();
        }
        boss.moveTo(x + 0.5, y, z + 0.5, player.getYRot() + 180, 0);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(x, y, z)), MobSpawnType.EVENT, null);
        boss.addTag("evolution_trial");
        boss.setPersistenceRequired();
        boss.setCustomName(Component.translatable("awakening_system.evolution.trial_boss_name", boss.getType().getDescription())
                .withStyle(ChatFormatting.GOLD));
        boss.setTarget(player);
        level.addFreshEntity(boss);
        BOSS_OWNER.put(boss.getUUID(), player.getUUID());
        OWNER_BOSS.put(player.getUUID(), boss.getUUID());
        DEADLINE.put(player.getUUID(), level.getGameTime() + TRIAL_TICKS);
        level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 1.2F, 1.0F);
        AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.trial_started", -10496, TRIAL_TICKS / 1200);
        return true;
    }

    private static void endTrial(UUID owner, MinecraftServer server, boolean discardBoss) {
        UUID boss = OWNER_BOSS.remove(owner);
        DEADLINE.remove(owner);
        if (boss == null) return;
        BOSS_OWNER.remove(boss);
        if (discardBoss) {
            for (ServerLevel l : server.getAllLevels()) {
                Entity e = l.getEntity(boss);
                if (e != null) e.discard();
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide() || dead.getServer() == null) return;
        MinecraftServer server = dead.getServer();
        UUID owner = BOSS_OWNER.get(dead.getUUID());
        if (owner != null) {
            endTrial(owner, server, false);
            ServerPlayer player = server.getPlayerList().getPlayer(owner);
            if (player != null) {
                SystemData data = SystemEvents.data(player);
                data.setEvolutionTrialPassed(true);
                AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.trial_won", -10496);
                player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                AwakeningSystemMod.sync(player);
            }
            return;
        }
        if (dead instanceof ServerPlayer player && OWNER_BOSS.containsKey(player.getUUID())) {
            endTrial(player.getUUID(), server, true);
            AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.trial_lost", -43691);
        }
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (DEADLINE.isEmpty() || server.getTickCount() % 20 != 0) return;
        long now = server.overworld().getGameTime();
        for (UUID owner : new ArrayList<>(DEADLINE.keySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(owner);
            UUID bossId = OWNER_BOSS.get(owner);
            Entity boss = null;
            if (bossId != null) {
                for (ServerLevel l : server.getAllLevels()) {
                    boss = l.getEntity(bossId);
                    if (boss != null) break;
                }
            }
            boolean late = now > DEADLINE.get(owner);
            boolean gone = player == null || boss == null || !boss.isAlive() || player.distanceToSqr(boss) > 128 * 128;
            if (late || gone) {
                endTrial(owner, server, true);
                if (player != null) {
                    AwakeningSystemMod.notify(player, "awakening_system.evolution.title",
                            late ? "awakening_system.evolution.trial_timeout" : "awakening_system.evolution.trial_lost", -43691);
                }
            }
        }
    }

    /** Tells a base class player once when the evolution opens up. */
    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 200 != 0) return;
        SystemData data = SystemEvents.data(player);
        if (!isBase(data.hunterClass()) || data.level() < EVOLVE_LEVEL) return;
        var tag = player.getPersistentData();
        if (tag.getBoolean("awakening_evolution_told")) return;
        tag.putBoolean("awakening_evolution_told", true);
        AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.available", -10496, EVOLVE_LEVEL);
    }

    // ---- evolving

    public static boolean canEvolve(SystemData data) {
        return isBase(data.hunterClass()) && data.level() >= EVOLVE_LEVEL && data.evolutionTrialPassed();
    }

    public static boolean evolve(ServerPlayer player, String target) {
        SystemData data = SystemEvents.data(player);
        Base b = base(data.hunterClass());
        if (b == null || !b.evolutions().contains(target)) return false;
        if (!canEvolve(data)) {
            AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.not_ready", -43691, EVOLVE_LEVEL);
            return false;
        }
        data.setHunterClass(target);
        SystemEvents.grantClassStart(data, target);
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), ModSounds.SYSTEM_AWAKENING.value(), SoundSource.PLAYERS, 1.4F, 1.0F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 80, 0.6, 1.2, 0.6, 0.12);
        AwakeningSystemMod.notify(player, "awakening_system.evolution.title", "awakening_system.evolution.done", -10496);
        SystemAttributeEffects.apply(player);
        AwakeningSystemMod.sync(player);
        return true;
    }

    // ---- the request from the evolution window

    /** action "trial" (target unused) or "evolve" (target = the class to become). */
    public record Payload(String action, String target) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "class_evolution"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Payload::action, ByteBufCodecs.STRING_UTF8, Payload::target, Payload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").optional().playToServer(Payload.TYPE, Payload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;
                if ("trial".equals(payload.action())) startTrial(player);
                else if ("evolve".equals(payload.action())) evolve(player, payload.target());
            }));
        }
    }
}
