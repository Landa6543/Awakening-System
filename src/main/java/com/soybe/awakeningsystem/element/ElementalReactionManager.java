package com.soybe.awakeningsystem.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema Canónico de Reacciones Elementales de Genshin Impact para Minecraft:
 * Administra la aplicación de auras elementales en entidades vivas,
 * desencadena las 11 reacciones elementales oficiales (Vaporización, Derretido,
 * Sobrecarga, Superconductor, Electrocargado, Congelado/Rompehielo, Torbellino,
 * Cristalización, Quemadura, Florecimiento, Aceleración), efectos en área,
 * estados alterados y textos flotantes de combate.
 */
public final class ElementalReactionManager {
    private ElementalReactionManager() {}

    public static class AuraData {
        public ElementType primary;
        public int ticksRemaining;
        public ElementType secondary;
        public int secondaryTicksRemaining;
        public boolean isFrozen;
        public int frozenTicks;
        public boolean isQuicken;
        public int quickenTicks;
        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;

        public AuraData(ElementType element, int duration, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim) {
            this.primary = element;
            this.ticksRemaining = duration;
            this.dimension = dim;
        }
    }

    private static final Map<UUID, AuraData> ACTIVE_AURAS = new ConcurrentHashMap<>();
    private record TextDespawn(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim, long expireTime) {}
    private static final Map<UUID, TextDespawn> FLOATING_TEXT_DESPAWNS = new ConcurrentHashMap<>();

    public record ReactionResult(
            boolean reacted,
            String reactionName,
            int textColor,
            float damageMultiplier,
            float bonusDamage
    ) {
        public static final ReactionResult NONE = new ReactionResult(false, "", 0, 1.0F, 0.0F);
    }

    public static @Nullable AuraData getAura(LivingEntity entity) {
        if (entity == null) return null;
        return ACTIVE_AURAS.get(entity.getUUID());
    }

    public static void clearAura(LivingEntity entity) {
        if (entity != null) {
            ACTIVE_AURAS.remove(entity.getUUID());
        }
    }

    /**
     * Aplica un elemento a un objetivo y evalúa posibles reacciones elementales canónicas.
     */
    public static ReactionResult applyElement(LivingEntity target, ElementType incoming, @Nullable LivingEntity attacker, float baseDamage) {
        if (target == null || !target.isAlive() || incoming == null) {
            return ReactionResult.NONE;
        }

        AuraData aura = ACTIVE_AURAS.get(target.getUUID());
        ServerLevel level = (target.level() instanceof ServerLevel sl) ? sl : null;

        if (aura == null || aura.primary == null || aura.ticksRemaining <= 0) {
            if (incoming != ElementType.ANEMO && incoming != ElementType.GEO) {
                ACTIVE_AURAS.put(target.getUUID(), new AuraData(incoming, 200, target.level().dimension()));
                if (level != null) {
                    spawnAuraParticle(level, target, incoming);
                }
            }
            return ReactionResult.NONE;
        }

        if (aura.primary == incoming) {
            aura.ticksRemaining = Math.max(aura.ticksRemaining, 200);
            return ReactionResult.NONE;
        }

        ElementType current = aura.primary;
        ReactionResult result = evaluateReaction(level, target, attacker, current, incoming, baseDamage, aura);

        if (result.reacted()) {
            if (level != null) {
                spawnReactionText(level, target, result.reactionName(), result.textColor());
            }
        } else {
            if (incoming != ElementType.ANEMO && incoming != ElementType.GEO) {
                aura.primary = incoming;
                aura.ticksRemaining = 200;
            }
        }

        return result;
    }

    private static ReactionResult evaluateReaction(
            @Nullable ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity attacker,
            ElementType current,
            ElementType incoming,
            float baseDamage,
            AuraData aura
    ) {
        Vec3 pos = target.position();

        if ((current == ElementType.PYRO && incoming == ElementType.HYDRO) || (current == ElementType.HYDRO && incoming == ElementType.PYRO)) {
            float mult = (incoming == ElementType.HYDRO) ? 2.0F : 1.5F;
            consumeAura(aura);
            if (level != null) {
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y + 1.0D, pos.z, 20, 0.4D, 0.6D, 0.4D, 0.05D);
                level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1.0D, pos.z, 15, 0.3D, 0.5D, 0.3D, 0.05D);
                level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + 1.0D, pos.z, 25, 0.4D, 0.6D, 0.4D, 0.1D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.2F, 1.3F);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            return new ReactionResult(true, "Vaporize", 0xFF0288D1, mult, 0.0F);
        }

        if ((current == ElementType.CRYO && incoming == ElementType.PYRO) || (current == ElementType.PYRO && incoming == ElementType.CRYO)) {
            float mult = (incoming == ElementType.PYRO) ? 2.0F : 1.5F;
            consumeAura(aura);
            if (level != null) {
                level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1.0D, pos.z, 25, 0.4D, 0.6D, 0.4D, 0.08D);
                level.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 1.0D, pos.z, 25, 0.4D, 0.6D, 0.4D, 0.05D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.2F, 1.4F);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.6F);
            }
            return new ReactionResult(true, "Melt", 0xFFFF5722, mult, 0.0F);
        }

        if ((current == ElementType.PYRO && incoming == ElementType.ELECTRO) || (current == ElementType.ELECTRO && incoming == ElementType.PYRO)) {
            consumeAura(aura);
            float aoeDmg = 26.0F;
            if (level != null) {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 1.0D, pos.z, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0D, pos.z, 30, 0.6D, 0.8D, 0.6D, 0.2D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.2F);
                damageNearby(level, target, attacker, 4.5D, aoeDmg, 0.7D);
            }
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.65D, 0));
            return new ReactionResult(true, "Overloaded", 0xFFE91E63, 1.0F, aoeDmg);
        }

        if ((current == ElementType.CRYO && incoming == ElementType.ELECTRO) || (current == ElementType.ELECTRO && incoming == ElementType.CRYO)) {
            consumeAura(aura);
            float aoeDmg = 16.0F;
            if (level != null) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 1.0D, pos.z, 30, 0.6D, 0.8D, 0.6D, 0.1D);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0D, pos.z, 30, 0.6D, 0.8D, 0.6D, 0.15D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.2F, 1.8F);
                damageNearby(level, target, attacker, 5.0D, aoeDmg, 0.2D);
            }
            target.getPersistentData().putLong("superconduct_vuln_until", target.level().getGameTime() + 240L);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1));
            return new ReactionResult(true, "Superconduct", 0xFF00E5FF, 1.0F, aoeDmg);
        }

        if ((current == ElementType.HYDRO && incoming == ElementType.ELECTRO) || (current == ElementType.ELECTRO && incoming == ElementType.HYDRO)) {
            aura.secondary = (current == ElementType.HYDRO) ? ElementType.ELECTRO : ElementType.HYDRO;
            aura.secondaryTicksRemaining = 60;
            if (level != null) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0D, pos.z, 25, 0.5D, 0.8D, 0.5D, 0.15D);
                level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + 1.0D, pos.z, 20, 0.4D, 0.6D, 0.4D, 0.1D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 2.0F);
                chainLightningToWetEntities(level, target, attacker, 6.0D, 14.0F);
            }
            return new ReactionResult(true, "Electro-Charged", 0xFF9C27B0, 1.0F, 12.0F);
        }

        if ((current == ElementType.HYDRO && incoming == ElementType.CRYO) || (current == ElementType.CRYO && incoming == ElementType.HYDRO)) {
            aura.isFrozen = true;
            aura.frozenTicks = 80;
            aura.primary = ElementType.CRYO;
            aura.ticksRemaining = 120;
            target.setTicksFrozen(200);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 10, false, false));
            if (level != null) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 1.0D, pos.z, 35, 0.5D, 0.8D, 0.5D, 0.05D);
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL, pos.x, pos.y + 1.0D, pos.z, 20, 0.4D, 0.7D, 0.4D, 0.05D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.4F, 1.2F);
            }
            return new ReactionResult(true, "Frozen", 0xFF80DEEA, 1.0F, 0.0F);
        }

        if (incoming == ElementType.ANEMO && isSwirlable(current)) {
            ElementType absorbed = current;
            consumeAura(aura);
            float swirlDmg = 18.0F;
            if (level != null) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y + 1.0D, pos.z, 8, 0.4D, 0.4D, 0.4D, 0.1D);
                level.sendParticles(absorbed.getParticle(), pos.x, pos.y + 1.0D, pos.z, 40, 0.8D, 0.8D, 0.8D, 0.15D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.3F, 1.2F);
                spreadElementAoE(level, target, attacker, 6.0D, absorbed, swirlDmg);
            }
            return new ReactionResult(true, "Swirl", absorbed.getColor(), 1.0F, swirlDmg);
        }

        if (incoming == ElementType.GEO && isSwirlable(current)) {
            ElementType crystalElement = current;
            consumeAura(aura);
            if (level != null) {
                level.sendParticles(ParticleTypes.CRIT, pos.x, pos.y + 1.0D, pos.z, 30, 0.5D, 0.7D, 0.5D, 0.2D);
                level.sendParticles(crystalElement.getParticle(), pos.x, pos.y + 1.0D, pos.z, 20, 0.4D, 0.6D, 0.4D, 0.1D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.4F);

                if (attacker instanceof Player player) {
                    grantElementalShield(player, crystalElement, 35.0F);
                }
            }
            return new ReactionResult(true, "Crystallize", 0xFFFFB300, 1.0F, 10.0F);
        }

        if ((current == ElementType.DENDRO && incoming == ElementType.PYRO) || (current == ElementType.PYRO && incoming == ElementType.DENDRO)) {
            aura.primary = ElementType.PYRO;
            aura.ticksRemaining = 140;
            target.setRemainingFireTicks(140);
            if (level != null) {
                level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1.0D, pos.z, 30, 0.4D, 0.8D, 0.4D, 0.08D);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y + 1.0D, pos.z, 15, 0.4D, 0.6D, 0.4D, 0.05D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.2F, 1.2F);
            }
            return new ReactionResult(true, "Burning", 0xFFFF3D00, 1.2F, 15.0F);
        }

        if ((current == ElementType.DENDRO && incoming == ElementType.HYDRO) || (current == ElementType.HYDRO && incoming == ElementType.DENDRO)) {
            consumeAura(aura);
            if (level != null) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y + 0.5D, pos.z, 25, 0.5D, 0.5D, 0.5D, 0.1D);
                level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + 0.5D, pos.z, 20, 0.4D, 0.6D, 0.4D, 0.1D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_ON, SoundSource.PLAYERS, 1.5F, 1.6F);
                spawnDendroCore(level, pos, attacker);
            }
            return new ReactionResult(true, "Bloom", 0xFF4CAF50, 1.0F, 18.0F);
        }

        if ((current == ElementType.DENDRO && incoming == ElementType.ELECTRO) || (current == ElementType.ELECTRO && incoming == ElementType.DENDRO)) {
            aura.isQuicken = true;
            aura.quickenTicks = 160;
            aura.primary = ElementType.DENDRO;
            if (level != null) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0D, pos.z, 25, 0.5D, 0.7D, 0.5D, 0.15D);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y + 1.0D, pos.z, 20, 0.4D, 0.6D, 0.4D, 0.1D);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.3F, 1.5F);
            }
            return new ReactionResult(true, "Quicken", 0xFF76FF03, 1.0F, 12.0F);
        }

        if (aura.isQuicken) {
            if (incoming == ElementType.ELECTRO) {
                if (level != null) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0D, pos.z, 30, 0.4D, 0.7D, 0.4D, 0.2D);
                    level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.8F);
                }
                return new ReactionResult(true, "Aggravate", 0xFFE040FB, 1.4F, 22.0F);
            } else if (incoming == ElementType.DENDRO) {
                if (level != null) {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y + 1.0D, pos.z, 30, 0.5D, 0.7D, 0.5D, 0.15D);
                    level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.5F, 1.4F);
                }
                return new ReactionResult(true, "Spread", 0xFF00E676, 1.5F, 24.0F);
            }
        }

        return ReactionResult.NONE;
    }

    /**
     * Rompehielo (Shatter): Si una entidad congelada recibe un ataque físico pesado o de Geo, el hielo revienta violentamente.
     */
    public static boolean checkShatter(LivingEntity target, @Nullable LivingEntity attacker, float damage) {
        AuraData aura = ACTIVE_AURAS.get(target.getUUID());
        if (aura != null && aura.isFrozen) {
            aura.isFrozen = false;
            aura.frozenTicks = 0;
            aura.primary = null;
            aura.ticksRemaining = 0;

            if (target.level() instanceof ServerLevel sl) {
                Vec3 p = target.position();
                sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, p.x, p.y + 1.0D, p.z, 40, 0.6D, 0.8D, 0.6D, 0.1D);
                sl.sendParticles(ParticleTypes.CRIT, p.x, p.y + 1.0D, p.z, 25, 0.5D, 0.7D, 0.5D, 0.2D);
                sl.playSound(null, p.x, p.y, p.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.6F, 0.9F);
                sl.playSound(null, p.x, p.y, p.z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 1.4F);
                spawnReactionText(sl, target, "Rompehielo", 0xFFFFFFFF);
            }
            return true;
        }
        return false;
    }

    private static boolean isSwirlable(ElementType e) {
        return e == ElementType.PYRO || e == ElementType.HYDRO || e == ElementType.ELECTRO || e == ElementType.CRYO;
    }

    private static void consumeAura(AuraData aura) {
        aura.primary = null;
        aura.ticksRemaining = 0;
        aura.secondary = null;
        aura.secondaryTicksRemaining = 0;
        aura.isFrozen = false;
        aura.frozenTicks = 0;
    }

    private static void damageNearby(ServerLevel level, LivingEntity center, @Nullable LivingEntity attacker, double radius, float damage, double knockbackY) {
        AABB box = center.getBoundingBox().inflate(radius, 3.0D, radius);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != center && e.isAlive() && (attacker == null || e != attacker));
        DamageSource src = attacker instanceof Player p ? level.damageSources().playerAttack(p) : level.damageSources().magic();
        for (LivingEntity e : list) {
            e.hurt(src, damage);
            if (knockbackY > 0) {
                e.setDeltaMovement(e.getDeltaMovement().add(0, knockbackY, 0));
            }
        }
    }

    private static void chainLightningToWetEntities(ServerLevel level, LivingEntity source, @Nullable LivingEntity attacker, double radius, float damage) {
        AABB box = source.getBoundingBox().inflate(radius);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != source && e.isAlive() && (attacker == null || e != attacker));
        DamageSource src = attacker instanceof Player p ? level.damageSources().playerAttack(p) : level.damageSources().lightningBolt();
        for (LivingEntity e : list) {
            AuraData a = ACTIVE_AURAS.get(e.getUUID());
            if (a != null && a.primary == ElementType.HYDRO) {
                e.hurt(src, damage);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + 1.0D, e.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.15D);
            }
        }
    }

    private static void spreadElementAoE(ServerLevel level, LivingEntity center, @Nullable LivingEntity attacker, double radius, ElementType element, float damage) {
        AABB box = center.getBoundingBox().inflate(radius);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != center && e.isAlive() && (attacker == null || e != attacker));
        DamageSource src = attacker instanceof Player p ? level.damageSources().playerAttack(p) : level.damageSources().magic();
        for (LivingEntity e : list) {
            e.hurt(src, damage);
            applyElement(e, element, attacker, damage);
        }
    }

    private static void grantElementalShield(Player player, ElementType element, float shieldAmount) {
        player.getPersistentData().putString("genshin_shield_element", element.getId());
        player.getPersistentData().putFloat("genshin_shield_amount", shieldAmount);
        player.getPersistentData().putLong("genshin_shield_until", player.level().getGameTime() + 300L);

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, false));
        player.displayClientMessage(Component.translatable("elemental_reaction.awakening_system.crystallize_shield", element.getName()), true);
    }

    private static void spawnDendroCore(ServerLevel level, Vec3 pos, @Nullable LivingEntity attacker) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y + 0.2D, pos.z, 15, 0.3D, 0.2D, 0.3D, 0.05D);
        level.getServer().execute(() -> {
        });
    }

    private static void spawnAuraParticle(ServerLevel level, LivingEntity target, ElementType element) {
        level.sendParticles(element.getParticle(), target.getX(), target.getY() + target.getBbHeight() + 0.3D, target.getZ(), 8, 0.2D, 0.1D, 0.2D, 0.02D);
    }

    /**
     * Despliega el rótulo flotante estilizado de la Reacción Elemental sobre el objetivo (estilo Genshin Impact)
     * y notifica en la barra de acción de los jugadores cercanos.
     */
    public static void spawnReactionText(ServerLevel level, LivingEntity target, String reactionName, int color) {
        try {
            double rx = target.getX() + (level.random.nextDouble() - 0.5D) * 0.3D;
            double ry = target.getY() + target.getBbHeight() + 0.35D;
            double rz = target.getZ() + (level.random.nextDouble() - 0.5D) * 0.3D;

            ArmorStand indicator = new ArmorStand(level, rx, ry, rz);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Marker", true);
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Small", true);
            tag.putBoolean("NoBasePlate", true);
            indicator.readAdditionalSaveData(tag);
            indicator.setInvisible(true);
            indicator.setNoGravity(true);
            indicator.setInvulnerable(true);
            indicator.setCustomName(Component.translatable("elemental_reaction.awakening_system.banner", reactionName.toUpperCase()).withStyle(s -> s.withColor(color)));
            indicator.setCustomNameVisible(true);
            level.addFreshEntity(indicator);

            long expireTime = level.getGameTime() + 25L;
            FLOATING_TEXT_DESPAWNS.put(indicator.getUUID(), new TextDespawn(level.dimension(), expireTime));

            AABB nearbyBox = target.getBoundingBox().inflate(16.0D);
            for (Player player : level.getEntitiesOfClass(Player.class, nearbyBox)) {
                player.displayClientMessage(Component.translatable("elemental_reaction.awakening_system.banner", reactionName.toUpperCase()).withStyle(s -> s.withColor(color)), true);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Ciclo de actualización en el servidor para las auras activas y limpieza de textos de combate.
     */
    public static void clearAll() {
        ACTIVE_AURAS.clear();
        FLOATING_TEXT_DESPAWNS.clear();
    }

    public static void tickServer(ServerLevel level) {
        long gameTime = level.getGameTime();
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = level.dimension();

        Iterator<Map.Entry<UUID, TextDespawn>> textIt = FLOATING_TEXT_DESPAWNS.entrySet().iterator();
        while (textIt.hasNext()) {
            Map.Entry<UUID, TextDespawn> entry = textIt.next();
            if (!entry.getValue().dim().equals(dim)) {
                continue;
            }
            if (gameTime >= entry.getValue().expireTime()) {
                var ent = level.getEntity(entry.getKey());
                if (ent != null) {
                    ent.discard();
                }
                textIt.remove();
            }
        }

        if (gameTime % 5 == 0) {
            Iterator<Map.Entry<UUID, AuraData>> auraIt = ACTIVE_AURAS.entrySet().iterator();
            while (auraIt.hasNext()) {
                Map.Entry<UUID, AuraData> entry = auraIt.next();
                AuraData aura = entry.getValue();
                if (aura.dimension != null && !aura.dimension.equals(dim)) {
                    continue;
                }
                var entity = level.getEntity(entry.getKey());
                if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                    auraIt.remove();
                    continue;
                }

                if (aura.primary != null && level.random.nextFloat() < 0.4F) {
                    level.sendParticles(aura.primary.getParticle(), living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ(), 2, 0.25D, 0.3D, 0.25D, 0.01D);
                }

                if (aura.isFrozen) {
                    aura.frozenTicks -= 5;
                    living.setDeltaMovement(0, Math.min(0, living.getDeltaMovement().y), 0);
                    if (aura.frozenTicks <= 0) {
                        aura.isFrozen = false;
                    }
                }

                if (aura.secondary == ElementType.ELECTRO || (aura.primary == ElementType.ELECTRO && aura.secondary == ElementType.HYDRO)) {
                    if (gameTime % 10 == 0) {
                        living.hurt(level.damageSources().lightningBolt(), 4.0F);
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, living.getX(), living.getY() + 1.0D, living.getZ(), 10, 0.3D, 0.5D, 0.3D, 0.1D);
                    }
                    aura.secondaryTicksRemaining -= 5;
                    if (aura.secondaryTicksRemaining <= 0) {
                        aura.secondary = null;
                    }
                }

                aura.ticksRemaining -= 5;
                if (aura.ticksRemaining <= 0) {
                    auraIt.remove();
                }
            }
        }
    }
}
