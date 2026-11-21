package com.soybe.awakeningsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * The skills of the six base classes: one active each (the start of the base class's web) and one passive (a
 * notable of the web). Simple on purpose: the evolutions bring the big skills. Actives scale with the stats and
 * with the skill's rank (the {@code mult} of the tree), like the other class skills.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class BaseClassSkills {
    private BaseClassSkills() {}

    private static boolean enemy(ServerPlayer player, LivingEntity e) {
        return e != player && e.isAlive() && !e.getTags().contains("shadow_soldier") && !(e instanceof Player p && !player.canHarmPlayer(p));
    }

    public static void activate(ServerPlayer player, SystemData data, String skillId, float mult) {
        ServerLevel sl = player.serverLevel();
        Vec3 look = player.getLookAngle();
        switch (skillId) {
            case "wa_power_slash" -> {
                // a wide slash in front: everything within 5 blocks and 70 degrees of the look
                float dmg = (24.0F + data.strength() * 0.5F) * mult;
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0), e -> enemy(player, e))) {
                    Vec3 to = e.position().subtract(player.position()).normalize();
                    if (to.dot(look) < 0.35) continue;
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.knockback(0.8, -look.x, -look.z);
                }
                Vec3 c = player.position().add(look.scale(2.0)).add(0, 1.0, 0);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 5, 1.2, 0.2, 1.2, 0.0);
                sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.8F);
            }
            case "as_shadow_dash" -> {
                // a dash forward, cutting what is on the way, then a moment unseen
                Vec3 start = player.position();
                Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                player.setDeltaMovement(flat.x * 1.8, 0.25, flat.z * 1.8);
                player.hurtMarked = true;
                float dmg = (18.0F + data.agility() * 0.45F) * mult;
                AABB path = new AABB(start, start.add(flat.scale(7))).inflate(1.2, 1.0, 1.2);
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, path, e -> enemy(player, e))) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                }
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, start.x, start.y + 1, start.z, 20, 0.3, 0.5, 0.3, 0.02);
                sl.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.4F);
            }
            case "ta_provoke" -> {
                // the monsters around turn on the tank, who braces for it
                for (Mob m : sl.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(10.0), m -> enemy(player, m))) {
                    m.setTarget(player);
                }
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Math.round(120 * mult), 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Math.round(120 * mult), 1));
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 2.2, player.getZ(), 8, 0.6, 0.3, 0.6, 0.0);
                sl.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            case "ma_mana_bolt" -> {
                // a bolt along the look: the first creature within 24 blocks takes it
                Vec3 eye = player.getEyePosition();
                Vec3 end = eye.add(look.scale(24));
                var blockHit = sl.clip(new net.minecraft.world.level.ClipContext(eye, end, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                Vec3 stop = blockHit.getLocation();
                var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(sl, player, eye, stop,
                        new AABB(eye, stop).inflate(1.0), e -> e instanceof LivingEntity le && enemy(player, le));
                if (hit != null) stop = hit.getLocation();
                double len = stop.distanceTo(eye);
                for (double d = 0; d < len; d += 0.5) {
                    Vec3 p = eye.add(look.scale(d));
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                if (hit != null && hit.getEntity() instanceof LivingEntity target) {
                    float dmg = (22.0F + data.intelligence() * 0.6F) * mult;
                    target.hurt(player.damageSources().indirectMagic(player, player), dmg);
                    sl.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                }
                sl.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            case "he_mend" -> {
                // heals the healer and the players nearby, and clears poison and wither
                float heal = (6.0F + data.willpower() * 0.2F) * mult;
                List<Player> near = sl.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(8.0), Player::isAlive);
                for (Player p : near) {
                    p.heal(heal);
                    p.removeEffect(MobEffects.POISON);
                    p.removeEffect(MobEffects.WITHER);
                    sl.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + 2.0, p.getZ(), 4, 0.4, 0.2, 0.4, 0.0);
                }
                sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.2F);
            }
            case "cu_qi_palm" -> {
                // a palm strike that throws back what is in front
                float dmg = (20.0F + (data.strength() + data.willpower()) * 0.3F) * mult;
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4.0), e -> enemy(player, e))) {
                    Vec3 to = e.position().subtract(player.position()).normalize();
                    if (to.dot(look) < 0.5) continue;
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    e.knockback(2.0, -look.x, -look.z);
                }
                Vec3 c = player.position().add(look.scale(2.0)).add(0, 1.2, 0);
                sl.sendParticles(ParticleTypes.CLOUD, c.x, c.y, c.z, 25, 0.5, 0.5, 0.5, 0.15);
                sl.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6F, 1.6F);
            }
            default -> {}
        }
    }

    // ---- the passives

    private static boolean has(Player player, String skillId) {
        return SystemEvents.data(player).skills().containsKey(skillId);
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        // attacker side: backstab and battle spirit
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity()) {
            LivingEntity target = event.getEntity();
            if (has(attacker, "as_backstab")) {
                Vec3 facing = target.getLookAngle();
                Vec3 fromTarget = attacker.position().subtract(target.position()).normalize();
                if (facing.dot(fromTarget) < -0.3) event.setAmount(event.getAmount() * 1.35F);
            }
            if (has(attacker, "wa_battle_spirit") && attacker.getHealth() < attacker.getMaxHealth() * 0.5F) {
                event.setAmount(event.getAmount() * 1.2F);
            }
        }
        // defender side: sturdy
        if (event.getEntity() instanceof ServerPlayer defender && has(defender, "ta_sturdy")) {
            event.setAmount(event.getAmount() * 0.88F);
        }
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer killer && killer != event.getEntity() && has(killer, "cu_inner_breath")) {
            killer.heal(2.0F);
            SystemEvents.data(killer).restoreEnergy(5);
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) return;
        SystemData data = SystemEvents.data(player);
        if (data.skills().containsKey("ma_mana_flow")) data.restoreEnergy(3);
        // renewal: out of a fight for five seconds, a heart back every four
        if (player.tickCount % 80 == 0 && data.skills().containsKey("he_renewal")
                && player.tickCount - player.getLastHurtByMobTimestamp() > 100 && player.getHealth() < player.getMaxHealth()) {
            player.heal(2.0F);
        }
    }
}
