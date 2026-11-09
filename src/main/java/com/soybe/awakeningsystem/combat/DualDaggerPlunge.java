package com.soybe.awakeningsystem.combat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dual-dagger skill, "Plunging Fangs": with a dagger in each hand, right-click to spring into the air, then
 * dive and drive both blades into the ground, hurting and throwing back everything around the landing.
 * <p>
 * The moves are Epic Fight's own animations (Demolition Leap for the jump, Meteor Slam for the dive), played
 * through {@link EpicFightBridge}; the jump, the dive and the impact are this class. Without Epic Fight the
 * skill still works, just with the vanilla look.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class DualDaggerPlunge {
    private static final TagKey<Item> DAGGERS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "daggers"));

    private static final int COOLDOWN_TICKS = 160;        // 8 seconds
    private static final float STAMINA_COST = 4.0F;
    private static final int RISE_TICKS = 8;              // how long the jump climbs before the dive
    private static final int MAX_DIVE_TICKS = 40;         // gives up waiting for the ground after 2 seconds
    private static final double RADIUS = 3.5;

    private enum Phase { RISE, DIVE }

    private static final class State {
        Phase phase = Phase.RISE;
        int ticks;
        Vec3 dir;
    }

    private static final Map<UUID, State> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> READY_AT = new HashMap<>();
    /** Players who just landed: their fall damage is ignored for a moment. */
    private static final Map<UUID, Long> SAFE_LANDING_UNTIL = new HashMap<>();

    private DualDaggerPlunge() {}

    /** A dagger by the common tag or by Epic Fight's own weapon category. */
    public static boolean isDagger(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(DAGGERS) || EpicFightBridge.isDagger(stack));
    }

    public static boolean holdsTwoDaggers(Player player) {
        return isDagger(player.getMainHandItem()) && isDagger(player.getOffhandItem());
    }

    /** Starts the skill if the player holds two daggers and it is ready. Returns whether it started. */
    public static boolean tryStart(ServerPlayer player) {
        if (!holdsTwoDaggers(player) || ACTIVE.containsKey(player.getUUID()) || player.isPassenger() || player.isSpectator()) return false;
        long now = player.serverLevel().getGameTime();
        long ready = READY_AT.getOrDefault(player.getUUID(), 0L);
        if (now < ready) {
            player.displayClientMessage(Component.translatable("awakening_system.skill.dual_plunge.cooldown", (ready - now + 19) / 20), true);
            return false;
        }
        if (!EpicFightBridge.consumeStamina(player, STAMINA_COST)) {
            player.displayClientMessage(Component.translatable("awakening_system.skill.dual_plunge.no_stamina"), true);
            return false;
        }
        READY_AT.put(player.getUUID(), now + COOLDOWN_TICKS);
        State s = new State();
        Vec3 look = player.getLookAngle();
        s.dir = new Vec3(look.x, 0, look.z).normalize();
        ACTIVE.put(player.getUUID(), s);

        EpicFightBridge.play(player, EpicFightBridge.Anim.LEAP);
        push(player, s.dir.scale(0.45).add(0, 1.05, 0));
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1, player.getZ(), 12, 0.4, 0.05, 0.4, 0.02);
        return true;
    }

    /** Sets the player's motion from the server; marking them hurt makes the server send it to their client. */
    private static void push(ServerPlayer player, Vec3 motion) {
        player.setDeltaMovement(motion);
        player.hurtMarked = true;
        player.fallDistance = 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        State s = ACTIVE.get(player.getUUID());
        if (s == null) return;
        s.ticks++;
        player.fallDistance = 0;
        if (!player.isAlive() || player.isSpectator()) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        if (s.phase == Phase.RISE) {
            if (s.ticks >= RISE_TICKS) {
                s.phase = Phase.DIVE;
                s.ticks = 0;
                EpicFightBridge.play(player, EpicFightBridge.Anim.SLAM);
                push(player, s.dir.scale(0.3).add(0, -1.7, 0));
                player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 0.8F, 1.3F);
            }
            return;
        }
        // diving: keep the speed up, trail sparks, and strike on touching the ground
        if (!player.onGround() && s.ticks < MAX_DIVE_TICKS) {
            if (player.getDeltaMovement().y > -1.2) push(player, new Vec3(player.getDeltaMovement().x, -1.7, player.getDeltaMovement().z));
            player.serverLevel().sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.2, 0.3, 0.2, 0.05);
            return;
        }
        ACTIVE.remove(player.getUUID());
        SAFE_LANDING_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + 10);
        if (player.onGround()) impact(player);
    }

    private static void impact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 at = player.position();
        double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = (float) (attack * 1.8 + 6.0);
        int hit = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(RADIUS, 2.0, RADIUS),
                e -> e != player && e.isAlive() && !(e instanceof ArmorStand) && !isFriend(player, e))) {
            if (target.distanceToSqr(at) > RADIUS * RADIUS) continue;
            if (target.hurt(player.damageSources().playerAttack(player), damage)) hit++;
            Vec3 away = target.position().subtract(at).multiply(1, 0, 1);
            away = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize();
            target.push(away.x * 0.9, 0.45, away.z * 0.9);
            target.hurtMarked = true;
        }
        // the ground cracks where both blades go in
        BlockState ground = level.getBlockState(BlockPos.containing(at).below());
        if (!ground.isAir()) level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.1, at.z, 60, 1.4, 0.1, 1.4, 0.15);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI * 2 / 12;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x + Math.cos(a) * 2.2, at.y + 0.3, at.z + Math.sin(a) * 2.2, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.2, at.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CRIT, at.x, at.y + 0.5, at.z, 30, 1.2, 0.4, 1.2, 0.3);
        level.playSound(null, BlockPos.containing(at), SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.0F, 1.1F);
        level.playSound(null, BlockPos.containing(at), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (hit > 0) level.playSound(null, BlockPos.containing(at), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** Other players, and pets and summons belonging to the player, are not hit. */
    private static boolean isFriend(ServerPlayer player, LivingEntity e) {
        if (e instanceof Player) return true;
        if (e instanceof OwnableEntity owned && player.getUUID().equals(owned.getOwnerUUID())) return true;
        return e.getTags().contains("shadow_soldier");
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Long until = SAFE_LANDING_UNTIL.get(player.getUUID());
        if (ACTIVE.containsKey(player.getUUID()) || until != null && player.serverLevel().getGameTime() <= until) {
            event.setDistance(0);
            event.setDamageMultiplier(0);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        ACTIVE.remove(id);
        READY_AT.remove(id);
        SAFE_LANDING_UNTIL.remove(id);
    }
}
