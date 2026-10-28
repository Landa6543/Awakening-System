package com.soybe.awakeningsystem.compat.iceandfire;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ShadowRank;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.config.BossCompatibilityManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Módulo de compatibilidad integral con Ice and Fire: Community Edition y entidades con cadáveres persistentes.
 * Permite extraer sombras directamente de los cadáveres caídos en el suelo (Dragones, Serpientes Marinas,
 * Withers, Cíclopes, Gorgonas, etc.), transfiriendo su etapa, edad, variante y majestuosidad al Soldado de Sombra.
 */
public final class IceAndFireCompat {

    private IceAndFireCompat() {}

    /**
     * Determina si una entidad es hostil o extraíble como sombra, superando las limitaciones
     * donde criaturas míticas (como Dragones de Ice and Fire o bestias de Mo' Creatures)
     * son registradas internamente como MobCategory.CREATURE (animales dóciles).
     */
    public static boolean isHostileOrExtractable(LivingEntity entity) {
        if (entity == null || entity.isRemoved() || entity instanceof ServerPlayer) return false;
        if (entity.getTags().contains("shadow_soldier")) return false;
        if (entity.getTags().contains("no_shadow_extraction")) return false;

        String typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
        if (typeKey.contains("celestial_dragon")) return false;

        // Soporte nativo para Ice and Fire: CE
        if (typeKey.startsWith("iceandfire:")) {
            // No permitir pixies domesticadas por el jugador
            if (typeKey.contains("pixie") && entity.getPersistentData().getBoolean("Tamed")) {
                return false;
            }
            return true;
        }

        // Mods de jefes y criaturas míticas
        if (typeKey.startsWith("cataclysm:") || typeKey.startsWith("mowziesmobs:")
                || typeKey.startsWith("mocreatures:") || typeKey.startsWith("twilightforest:")) {
            return true;
        }

        if (entity instanceof Enemy) return true;
        if (BossCompatibilityManager.isBoss(entity)) return true;
        if (entity.getMaxHealth() >= 50.0F) return true;

        // Si tiene objetivo agresivo activo
        if (entity instanceof Mob mob && mob.getTarget() != null) {
            return true;
        }

        // Comprobación de categoría vanilla
        return !entity.getType().getCategory().isFriendly();
    }

    /**
     * Determina si una entidad en el mundo es un cadáver persistente (como un dragón de Ice and Fire
     * que permanece en el suelo tras morir, o un mob en fase de descomposición/muerte).
     */
    public static boolean isCorpseEntity(Entity entity) {
        if (entity == null || entity.isRemoved()) return false;
        if (entity.getTags().contains("shadow_soldier")) return false;
        if (entity.getTags().contains("shadow_extracted")) return false;
        if (entity.getTags().contains("no_shadow_extraction")) return false;

        String typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
        if (typeKey.contains("celestial_dragon")) return false;

        // 1. Detección por NBT de Ice and Fire (ModelDead, DeadProg, DeathTime)
        CompoundTag persistent = entity.getPersistentData();
        if (persistent.getBoolean("ModelDead") || persistent.getInt("DeadProg") > 0 || persistent.getInt("DeathTime") > 0 || persistent.getBoolean("Dead")) {
            return true;
        }

        CompoundTag fullNbt = new CompoundTag();
        try {
            entity.saveWithoutId(fullNbt);
            if (fullNbt.getBoolean("ModelDead") || fullNbt.getInt("DeadProg") > 0 || fullNbt.getInt("DeathTime") > 0 || fullNbt.getBoolean("Dead")) {
                return true;
            }
        } catch (Throwable ignored) {}

        // 2. Detección por reflexión del método isModelDead() de Ice and Fire
        try {
            Method m = entity.getClass().getMethod("isModelDead");
            Object res = m.invoke(entity);
            if (Boolean.TRUE.equals(res)) return true;
        } catch (Throwable ignored) {}

        // 3. Detección de estado vital en LivingEntity (muerto, en animación de muerte, o vida <= 0)
        if (entity instanceof LivingEntity living) {
            if (living.isDeadOrDying() || living.getHealth() <= 0.0F || living.deathTime > 0) {
                return true;
            }
            if (!living.isAlive() && !living.isRemoved()) {
                return true;
            }
            // Caso especial para Withers y jefes en animación de colapso
            if ((typeKey.contains("wither") || typeKey.contains("dragon")) && (living.getHealth() <= 1.0F || living.isDeadOrDying())) {
                return true;
            }
        }

        // 4. Detección por nombre de clase de entidades cadáver (mods de cadáveres/esqueletos)
        String simpleName = entity.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        if (simpleName.contains("corpse") || simpleName.contains("deadbody") || simpleName.contains("dragoncorpse")) {
            return true;
        }

        return false;
    }

    /**
     * Retorna si el cadáver es apto para ser extraído por el Monarca de las Sombras.
     */
    public static boolean isExtractableCorpse(Entity entity) {
        return isCorpseEntity(entity)
                && !entity.getTags().contains("shadow_soldier")
                && !entity.getTags().contains("shadow_extracted")
                && !entity.getTags().contains("no_shadow_extraction");
    }

    /**
     * Localiza todos los cadáveres extraíbles en un radio alrededor del jugador.
     */
    public static List<Entity> findNearbyCorpses(ServerLevel level, ServerPlayer player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        List<Entity> corpses = level.getEntities((Entity) null, box, IceAndFireCompat::isExtractableCorpse);
        corpses.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
        return corpses;
    }

    /**
     * Ejecuta el proceso de extracción de sombra sobre un cadáver en el suelo.
     * Retorna true si la sombra fue extraída exitosamente y el cadáver se transformó.
     */
    public static boolean attemptExtractCorpse(ServerPlayer master, Entity corpse, boolean isUnique, int intel, int will) {
        if (!(master.level() instanceof ServerLevel sl)) return false;
        if (!isExtractableCorpse(corpse)) return false;

        sl.playSound(null, master.getX(), master.getY(), master.getZ(), com.soybe.awakeningsystem.ModSounds.ARISE_VOICE.get(), SoundSource.PLAYERS, 1.8F, 1.0F);
        sl.playSound(null, corpse.getX(), corpse.getY(), corpse.getZ(), com.soybe.awakeningsystem.ModSounds.SHADOW_EXTRACT.get(), SoundSource.PLAYERS, 1.3F, 1.0F);

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(corpse.getType()).toString().toLowerCase(Locale.ROOT);
        String corpseName = corpse.getCustomName() != null ? corpse.getCustomName().getString() : corpse.getType().getDescription().getString();

        int currentAttempt = corpse.getPersistentData().getInt("shadow_corpse_attempts") + 1;

        // Determinar nivel de dificultad del cadáver
        int mobTier = 5;
        if (entityId.contains("dragon")) {
            int stage = getDragonStage(corpse);
            mobTier = (stage >= 4) ? 10 : (stage == 3 ? 8 : 6);
        } else if (entityId.contains("wither") || entityId.contains("warden") || entityId.contains("hydra") || entityId.contains("sea_serpent")) {
            mobTier = 9;
        } else if (entityId.contains("boss") || (corpse instanceof LivingEntity l && l.getMaxHealth() >= 200.0F)) {
            mobTier = 8;
        }

        double bonus = isUnique ? 45.0D : 0.0D;
        double chance = (intel * 0.35D + will * 0.35D + bonus) - (mobTier * 5.5D) + 35.0D;
        chance = Math.max(15.0D, Math.min(95.0D, chance));
        if (isUnique) chance = 100.0D; // Extracción Absoluta siempre triunfa

        boolean success = (master.getRandom().nextDouble() * 100.0D) < chance;

        if (success) {
            Mob shadowMob = createShadowFromCorpse(master, corpse);
            if (shadowMob != null) {
                corpse.addTag("shadow_extracted");
                corpse.discard();

                // Efectos climáticos y sonoros épicos
                sl.sendParticles(ParticleTypes.SCULK_SOUL, corpse.getX(), corpse.getY() + 0.8D, corpse.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.08D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, corpse.getX(), corpse.getY() + 0.5D, corpse.getZ(), 50, 0.5D, 0.7D, 0.5D, 0.06D);
                sl.sendParticles(ParticleTypes.SQUID_INK, corpse.getX(), corpse.getY() + 0.6D, corpse.getZ(), 60, 0.7D, 0.9D, 0.7D, 0.1D);
                sl.playSound(null, corpse.getX(), corpse.getY(), corpse.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.2F, 0.85F);

                master.displayClientMessage(Component.translatable("awakening_system.shadow.arise_chat_success", Component.literal(corpseName)), true);
                AwakeningSystemMod.notify(master, "awakening_system.notification.shadow_corpse_extracted.title",
                        "awakening_system.notification.shadow_corpse_extracted.desc", 0xFF9966FF, corpseName);
                return true;
            }
        }

        // Intento fallido
        corpse.getPersistentData().putInt("shadow_corpse_attempts", currentAttempt);
        if (currentAttempt < 3) {
            sl.sendParticles(ParticleTypes.SCULK_SOUL, corpse.getX(), corpse.getY() + 0.6D, corpse.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.05D);
            sl.playSound(null, corpse.getX(), corpse.getY(), corpse.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
            master.displayClientMessage(Component.translatable("awakening_system.shadow.arise_attempt_fail", currentAttempt, Component.literal(corpseName)), true);
        } else {
            // El alma del cadáver se fractura permanentemente
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, corpse.getX(), corpse.getY() + 0.8D, corpse.getZ(), 50, 0.6D, 0.8D, 0.6D, 0.05D);
            sl.playSound(null, corpse.getX(), corpse.getY(), corpse.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.0F, 0.65F);
            master.displayClientMessage(Component.translatable("awakening_system.shadow.arise_final_fail", Component.literal(corpseName)), true);
            corpse.addTag("no_shadow_extraction");
        }
        return false;
    }

    /**
     * Crea y despierta al Soldado de Sombra a partir de los datos biológicos y mágicos del cadáver.
     */
    public static Mob createShadowFromCorpse(ServerPlayer master, Entity corpse) {
        if (!(master.level() instanceof ServerLevel sl)) return null;

        EntityType<?> type = corpse.getType();
        Entity created = type.create(sl);
        if (!(created instanceof Mob shadowMob)) {
            return null;
        }

        // Clonar NBT y propiedades vitales
        CompoundTag corpseNbt = new CompoundTag();
        try {
            corpse.saveWithoutId(corpseNbt);
        } catch (Throwable ignored) {}

        // Limpiar indicadores de muerte y reanimar por completo
        corpseNbt.putBoolean("ModelDead", false);
        corpseNbt.putInt("DeadProg", 0);
        corpseNbt.putInt("DeathTime", 0);
        corpseNbt.putBoolean("NoAI", false);
        corpseNbt.remove("DeathTime");

        // Doma al Monarca si la criatura es domesticable
        corpseNbt.putBoolean("Tamed", true);
        corpseNbt.putUUID("Owner", master.getUUID());
        corpseNbt.putUUID("OwnerUUID", master.getUUID());

        try {
            shadowMob.readAdditionalSaveData(corpseNbt);
        } catch (Throwable ignored) {}

        shadowMob.setPos(corpse.getX(), corpse.getY(), corpse.getZ());
        shadowMob.setYRot(corpse.getYRot());
        shadowMob.setXRot(corpse.getXRot());
        shadowMob.setDeltaMovement(0, 0, 0);
        shadowMob.clearFire();
        shadowMob.setHealth(shadowMob.getMaxHealth());

        if (shadowMob instanceof TamableAnimal tamable) {
            try {
                tamable.setTame(true, false);
                tamable.setOwnerUUID(master.getUUID());
            } catch (Throwable ignored) {}
        }

        // Determinar Rango Sombra apropiado
        ShadowRank rank = determineCorpseShadowRank(corpse);
        shadowMob.getPersistentData().putInt("shadow_rank", rank.getLevel());
        shadowMob.getPersistentData().putInt("shadow_kills", 0);

        String baseName = corpse.getCustomName() != null ? corpse.getCustomName().getString() : type.getDescription().getString();
        baseName = baseName.replaceAll("§[0-9a-fk-or]", "").replaceAll("\\[.*?\\]", "").trim();
        if (baseName.isEmpty()) baseName = type.getDescription().getString();

        int corpseLvl = corpse.getPersistentData().getInt("rpg_mob_level");
        if (corpseLvl > 0) {
            shadowMob.getPersistentData().putInt("rpg_mob_level", corpseLvl);
        }

        if (corpse instanceof LivingEntity livingCorpse) {
            double maxHp = livingCorpse.getMaxHealth();
            shadowMob.getPersistentData().putDouble("shadow_original_max_hp", maxHp);
            net.minecraft.world.entity.ai.attributes.AttributeInstance atk = livingCorpse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            if (atk != null && atk.getValue() > 0.0) {
                shadowMob.getPersistentData().putDouble("shadow_original_dmg", atk.getValue());
            } else if (corpseLvl > 1) {
                shadowMob.getPersistentData().putDouble("shadow_original_dmg", 3.0 * (1.0 + corpseLvl * 0.05) + corpseLvl * 0.35);
            }
            net.minecraft.world.entity.ai.attributes.AttributeInstance arm = livingCorpse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
            if (arm != null) {
                shadowMob.getPersistentData().putDouble("shadow_original_armor", arm.getValue());
            }
            net.minecraft.world.entity.ai.attributes.AttributeInstance tgh = livingCorpse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
            if (tgh != null) {
                shadowMob.getPersistentData().putDouble("shadow_original_toughness", tgh.getValue());
            }
            net.minecraft.world.entity.ai.attributes.AttributeInstance kb = livingCorpse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) {
                shadowMob.getPersistentData().putDouble("shadow_original_kb", kb.getValue());
            }
            net.minecraft.world.entity.ai.attributes.AttributeInstance spd = livingCorpse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            if (spd != null) {
                shadowMob.getPersistentData().putDouble("shadow_original_speed", spd.getValue());
            }
        }

        shadowMob.getPersistentData().putString("shadow_base_name", baseName);
        String lvlPrefix = corpseLvl > 0 ? "§e[Lv. " + corpseLvl + "] " : "";
        shadowMob.setCustomName(Component.literal(rank.getPrefix() + " §9" + lvlPrefix + baseName));
        shadowMob.setCustomNameVisible(true);

        shadowMob.addTag("shadow_soldier");
        shadowMob.addTag("shadow_master_" + master.getUUID());

        // Asegurar que no quede en modo cadáver
        shadowMob.getPersistentData().putBoolean("ModelDead", false);
        shadowMob.getPersistentData().remove("DeadProg");

        // Registrar y desatar la emergencia del soldado
        SystemEvents.setupShadowMob(sl, master, shadowMob);
        return shadowMob;
    }

    /**
     * Determina el rango sombra inicial para un cadáver.
     * Dragones de Etapa 4 y 5 ascienden como Gran Mariscal o Comandante.
     */
    public static ShadowRank determineCorpseShadowRank(Entity corpse) {
        String typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(corpse.getType()).toString().toLowerCase(Locale.ROOT);

        if (typeKey.contains("dragon")) {
            int stage = getDragonStage(corpse);
            if (stage >= 4) return ShadowRank.GRAND_MARSHAL;
            if (stage == 3) return ShadowRank.COMMANDER;
            return ShadowRank.KNIGHT;
        }

        if (typeKey.contains("wither") || typeKey.contains("warden") || typeKey.contains("hydra") || typeKey.contains("sea_serpent")) {
            return ShadowRank.COMMANDER;
        }

        if (corpse instanceof LivingEntity living && living.getMaxHealth() >= 250.0F) {
            return ShadowRank.COMMANDER;
        }

        if (corpse instanceof Mob mob) {
            return ShadowRank.determineInitialRank(mob);
        }

        return ShadowRank.KNIGHT;
    }

    /**
     * Extrae de forma segura la etapa (Stage 1 a 5) de un dragón de Ice and Fire.
     */
    public static int getDragonStage(Entity dragon) {
        if (dragon == null) return 1;
        try {
            CompoundTag nbt = new CompoundTag();
            dragon.saveWithoutId(nbt);
            if (nbt.contains("Stage")) {
                return Math.max(1, Math.min(5, nbt.getInt("Stage")));
            }
            if (nbt.contains("DragonAge")) {
                int ageDays = nbt.getInt("DragonAge");
                if (ageDays >= 100) return 5;
                if (ageDays >= 75) return 4;
                if (ageDays >= 50) return 3;
                if (ageDays >= 25) return 2;
                return 1;
            }
        } catch (Throwable ignored) {}

        try {
            Method m = dragon.getClass().getMethod("getDragonStage");
            Object res = m.invoke(dragon);
            if (res instanceof Number n) return Math.max(1, Math.min(5, n.intValue()));
        } catch (Throwable ignored) {}

        return 3; // Nivel intermedio por defecto
    }

    /**
     * Guarda atributos biológicos específicos (edad, etapa, variante, género)
     * al almacenar un Soldado Sombra en la sombra del Monarca.
     */
    public static void captureMobData(Mob mob, CompoundTag targetTag) {
        if (mob == null || targetTag == null) return;
        try {
            CompoundTag mobNbt = new CompoundTag();
            mob.saveWithoutId(mobNbt);

            CompoundTag customData = new CompoundTag();
            if (mobNbt.contains("Stage")) customData.putInt("Stage", mobNbt.getInt("Stage"));
            if (mobNbt.contains("DragonAge")) customData.putInt("DragonAge", mobNbt.getInt("DragonAge"));
            if (mobNbt.contains("Variant")) customData.putInt("Variant", mobNbt.getInt("Variant"));
            if (mobNbt.contains("Gender")) customData.putBoolean("Gender", mobNbt.getBoolean("Gender"));
            if (mobNbt.contains("Color")) customData.putInt("Color", mobNbt.getInt("Color"));
            if (mobNbt.contains("Scale")) customData.putFloat("Scale", mobNbt.getFloat("Scale"));

            if (!customData.isEmpty()) {
                targetTag.put("ModdedMobData", customData);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Restaura los atributos biológicos específicos al desplegar el Soldado Sombra.
     */
    public static void restoreMobData(Mob mob, CompoundTag sourceTag) {
        if (mob == null || sourceTag == null || !sourceTag.contains("ModdedMobData")) return;
        try {
            CompoundTag customData = sourceTag.getCompound("ModdedMobData");
            mob.readAdditionalSaveData(customData);
            mob.getPersistentData().putBoolean("ModelDead", false);
            mob.getPersistentData().remove("DeadProg");
        } catch (Throwable ignored) {}
    }
}
