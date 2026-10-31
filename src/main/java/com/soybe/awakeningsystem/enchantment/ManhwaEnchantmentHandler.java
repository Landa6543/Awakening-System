package com.soybe.awakeningsystem.enchantment;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

import java.util.List;

@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class ManhwaEnchantmentHandler {

    private ManhwaEnchantmentHandler() {}

    public static int getEnchantmentLevel(ItemStack stack, String enchantmentPath) {
        if (stack.isEmpty()) return 0;
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null) return 0;
        for (Holder<Enchantment> holder : enchants.keySet()) {
            if (holder.unwrapKey().map(k -> k.location().getPath().equals(enchantmentPath)).orElse(false)) {
                return enchants.getLevel(holder);
            }
        }
        return 0;
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) return;

        ServerLevel sl = (ServerLevel) target.level();

        // 1. Resonancia Abisal (I - V): Pulso sónico y penetración de armadura en golpes críticos
        int lvlResonance = getEnchantmentLevel(weapon, "abyssal_resonance");
        if (lvlResonance > 0) {
            boolean isCrit = attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable()
                    && !attacker.isInWater() && !attacker.hasEffect(MobEffects.BLINDNESS)
                    && !attacker.isPassenger() && !attacker.isSprinting();
            if (isCrit) {
                float armorPierceDmg = event.getAmount() * (0.15F * lvlResonance);
                event.setAmount(event.getAmount() + armorPierceDmg);

                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.35F, 1.8F);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.7F);

                AABB waveBox = target.getBoundingBox().inflate(2.5);
                List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, waveBox,
                        e -> e != attacker && e != target && e.isAlive() && !e.isAlliedTo(attacker));
                for (LivingEntity mob : nearby) {
                    if (attacker instanceof ServerPlayer sp) {
                        mob.hurt(sl.damageSources().playerAttack(sp), 2.0F * lvlResonance);
                    } else {
                        mob.hurt(sl.damageSources().mobAttack(attacker), 2.0F * lvlResonance);
                    }
                }
            }
        }

        // 2. Colapso Espacial (I - III): Micro-vórtice gravitacional que atrae a enemigos cercanos
        int lvlCollapse = getEnchantmentLevel(weapon, "spatial_collapse");
        if (lvlCollapse > 0) {
            triggerSpatialCollapse(sl, attacker, target, lvlCollapse);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile proj = event.getProjectile();
        if (proj.level().isClientSide()) return;
        if (!(proj.getOwner() instanceof LivingEntity owner)) return;

        ItemStack bow = owner.getMainHandItem();
        int lvlCollapse = getEnchantmentLevel(bow, "spatial_collapse");
        if (lvlCollapse <= 0) {
            lvlCollapse = getEnchantmentLevel(owner.getOffhandItem(), "spatial_collapse");
        }

        if (lvlCollapse > 0 && event.getRayTraceResult() instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof LivingEntity target) {
                triggerSpatialCollapse((ServerLevel) proj.level(), owner, target, lvlCollapse);
            }
        }
    }

    private static void triggerSpatialCollapse(ServerLevel sl, LivingEntity attacker, LivingEntity target, int lvlCollapse) {
        long now = sl.getGameTime();
        long last = target.getPersistentData().getLong("spatial_collapse_cd");
        if (now - last < 15) return;
        target.getPersistentData().putLong("spatial_collapse_cd", now);

        double radius = 3.5 + 1.0 * lvlCollapse;
        sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 0.5, target.getZ(), 20, 0.5, 0.5, 0.5, 0.8);
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.4, 0.4, 0.4, 0.05);
        sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
        sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 0.6F);

        AABB pullBox = target.getBoundingBox().inflate(radius);
        List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, pullBox,
                e -> e != attacker && e != target && e.isAlive() && !e.isAlliedTo(attacker)
                        && !(e instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon));

        for (LivingEntity mob : nearby) {
            Vec3 diff = target.position().subtract(mob.position());
            double dist = diff.length();
            if (dist > 0.4 && dist <= radius) {
                Vec3 pull = diff.normalize().scale(0.35 + 0.12 * lvlCollapse);
                mob.setDeltaMovement(mob.getDeltaMovement().add(pull.x, 0.12, pull.z));
                mob.hurtMarked = true;
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, lvlCollapse - 1, false, false, true));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer sp)) return;

        ItemStack weapon = sp.getMainHandItem();
        int lvlReaper = getEnchantmentLevel(weapon, "essence_reaper");
        if (lvlReaper > 0) {
            ServerLevel sl = (ServerLevel) victim.level();
            SystemData data = sp.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            int manaGain = 15 * lvlReaper;
            if (data != null) {
                data.restoreMana(manaGain);
                if (data.murimUnlocked() || data.cultivationQi() > 0 || data.dantianRealm() > 0) {
                    data.addCultivationQi(10 * lvlReaper);
                }
                AwakeningSystemMod.sync(sp);
            }

            sl.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY() + 0.6, victim.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, victim.getX(), victim.getY() + 0.6, victim.getZ(), 6, 0.2, 0.3, 0.2, 0.04);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.5F, 1.6F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4F, 1.2F);
            sp.displayClientMessage(Component.translatable("enchantment.awakening_system.essence_reaper.harvest", manaGain), true);
        }
    }
}
