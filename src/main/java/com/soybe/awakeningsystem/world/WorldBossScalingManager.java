package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.DungeonManager;
import com.soybe.awakeningsystem.PagodaTrialManager;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.TowerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

public final class WorldBossScalingManager {

    private WorldBossScalingManager() {}

    /**
     * Determina si una entidad califica como Jefe de Mundo (World Boss).
     * Los jefes de dungeon, torre o pagoda se excluyen para mantener su balance fijo ("los de dungeon se mantienen igual").
     */
    public static boolean isWorldBoss(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return false;
        Level level = entity.level();

        // Excluir dimensiones de calabozos, torres y pagodas
        if (level.dimension().equals(DungeonManager.DUNGEON_REALM)
                || level.dimension().equals(TowerManager.RANKER_TOWER)
                || level.dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
            return false;
        }

        // Excluir entidades marcadas como tropas o jefes de dungeon / torre / sombras
        if (entity.getTags().contains("dungeon_mob")
                || entity.getTags().contains("dungeon_boss")
                || entity.getTags().contains("tower_mob")
                || entity.getTags().contains("pagoda_trial_mob")
                || entity.getTags().contains("shadow_soldier")) {
            return false;
        }

        // Instancias específicas de jefes de mundo del mod y vanilla
        if (entity instanceof com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.AzhdahaBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.ChildeBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.RaidenBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.GoldenWolflordBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.GreatSnowboarKingBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.PerpetualMechanicalArrayBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.RhodeiaOceanidBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.RuinSerpentBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.PyroRegisvineEntity
                || entity instanceof com.soybe.awakeningsystem.entity.CryoRegisvineEntity
                || entity instanceof com.soybe.awakeningsystem.entity.ElectroRegisvineEntity
                || entity instanceof com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.JadeGuardianBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.ElementalLordBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.AncientDragonEntity
                || entity instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity
                || entity instanceof com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.ShadowMonarchBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.BaranBossEntity
                || entity instanceof com.soybe.awakeningsystem.entity.IgrisBossEntity
                || entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            return true;
        }

        // Jefes generales con vida base alta persistente en el mundo abierto
        return entity.getMaxHealth() >= 400.0F && entity instanceof Mob mob && mob.isPersistenceRequired();
    }

    /**
     * Nivel base de inicio para cada tipo de Jefe de Mundo.
     */
    public static int getBaseBossLevel(Mob boss) {
        if (boss instanceof com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.AzhdahaBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.RaidenBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.ShadowMonarchBossEntity) {
            return 80;
        }
        if (boss instanceof com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.PyroRegisvineEntity
                || boss instanceof com.soybe.awakeningsystem.entity.CryoRegisvineEntity
                || boss instanceof com.soybe.awakeningsystem.entity.ElectroRegisvineEntity
                || boss instanceof com.soybe.awakeningsystem.entity.ChildeBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.GoldenWolflordBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.PerpetualMechanicalArrayBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.RhodeiaOceanidBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.RuinSerpentBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity
                || boss instanceof com.soybe.awakeningsystem.entity.AncientDragonEntity
                || boss instanceof com.soybe.awakeningsystem.entity.JadeGuardianBossEntity
                || boss instanceof com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity) {
            return 60;
        }
        return 50;
    }

    /**
     * Escala dinámicamente un Jefe de Mundo al nivel del jugador más fuerte cercano o atacante.
     */
    public static void scaleWorldBossToPlayer(Mob boss, ServerPlayer player) {
        if (boss == null || player == null || !boss.isAlive()) return;

        int playerLevel = 1;
        try {
            playerLevel = Math.max(player.experienceLevel, SystemEvents.data(player).level());
        } catch (Exception ignored) {
            playerLevel = Math.max(1, player.experienceLevel);
        }

        int baseLevel = boss.getPersistentData().contains("rpg_base_boss_level")
                ? boss.getPersistentData().getInt("rpg_base_boss_level")
                : getBaseBossLevel(boss);
        boss.getPersistentData().putInt("rpg_base_boss_level", baseLevel);

        int targetLevel = Math.max(baseLevel, playerLevel);
        int currentScaledLevel = boss.getPersistentData().getInt("world_boss_scaled_level");

        // Si ya está escalado a este nivel o superior, no degradar
        if (targetLevel <= currentScaledLevel) return;

        // Registrar estadísticas base originales si aún no existen
        if (!boss.getPersistentData().contains("rpg_base_boss_hp")) {
            boss.getPersistentData().putDouble("rpg_base_boss_hp", boss.getMaxHealth());
            var dmgAttr = boss.getAttribute(Attributes.ATTACK_DAMAGE);
            boss.getPersistentData().putDouble("rpg_base_boss_dmg", dmgAttr != null ? dmgAttr.getBaseValue() : 20.0D);
        }

        double baseHp = boss.getPersistentData().getDouble("rpg_base_boss_hp");
        double baseDmg = boss.getPersistentData().getDouble("rpg_base_boss_dmg");

        int diff = targetLevel - baseLevel;
        // Escalar vida: +6.5% de vida extra por cada nivel por encima del base
        // (Ejemplo: nivel 500 con base 60 = 440 niveles = 28.6x de vida base -> ~110,000 HP)
        double hpMult = 1.0D + (diff * 0.065D);
        double newMaxHp = baseHp * hpMult;

        // Escalar daño: +1.5% de daño extra por cada nivel
        double dmgMult = 1.0D + (diff * 0.015D);
        double newDmg = baseDmg * dmgMult;

        // Escalar armadura y tenacidad
        double bonusArmor = Math.min(36.0D, diff * 0.08D);
        double bonusToughness = Math.min(24.0D, diff * 0.05D);

        // Aplicar manteniendo el porcentaje de vida actual
        float currentRatio = Math.max(0.05F, boss.getHealth() / boss.getMaxHealth());

        var hpAttr = boss.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.setBaseValue(newMaxHp);
            boss.setHealth((float) (newMaxHp * currentRatio));
        }

        var dmgAttr = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null && baseDmg > 0) {
            dmgAttr.setBaseValue(newDmg);
        }

        var armorAttr = boss.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(armorAttr.getBaseValue() + bonusArmor);
        }

        var toughAttr = boss.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughAttr != null) {
            toughAttr.setBaseValue(toughAttr.getBaseValue() + bonusToughness);
        }

        // Actualizar etiqueta de nombre visible con el nivel escalado
        String originalName = boss.getPersistentData().getString("rpg_boss_original_name");
        if (originalName.isEmpty()) {
            originalName = boss.getType().getDescription().getString();
            boss.getPersistentData().putString("rpg_boss_original_name", originalName);
        }

        String titleColor = targetLevel >= 500 ? "§5§l" : (targetLevel >= 250 ? "§4§l" : (targetLevel >= 100 ? "§c§l" : "§6§l"));
        boss.setCustomName(Component.literal(titleColor + "[JEFE DE MUNDO - Nv. " + targetLevel + "] §f" + originalName));
        boss.setCustomNameVisible(true);

        boss.getPersistentData().putInt("world_boss_scaled_level", targetLevel);
    }

    /**
     * Busca los jugadores cercanos en un radio de 64 bloques y escala el jefe al más fuerte.
     */
    public static void scaleWorldBossToNearbyPlayers(Mob boss, ServerLevel level) {
        if (boss == null || level == null || !boss.isAlive()) return;
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(
                ServerPlayer.class,
                boss.getBoundingBox().inflate(64.0D),
                p -> p.isAlive() && !p.isCreative() && !p.isSpectator()
        );
        if (nearbyPlayers.isEmpty()) return;

        ServerPlayer strongestPlayer = null;
        int maxLvl = -1;
        for (ServerPlayer sp : nearbyPlayers) {
            int pLvl = Math.max(sp.experienceLevel, SystemEvents.data(sp).level());
            if (pLvl > maxLvl) {
                maxLvl = pLvl;
                strongestPlayer = sp;
            }
        }

        if (strongestPlayer != null) {
            scaleWorldBossToPlayer(boss, strongestPlayer);
        }
    }

    /**
     * Aplica la protección contra muerte instantánea (One-Shot Prevention) y escala dinámica al recibir daño.
     */
    public static void handleWorldBossIncomingDamage(LivingDamageEvent.Pre event, Mob boss, ServerPlayer attacker) {
        if (boss == null || attacker == null) return;

        // 1. Escalar dinámicamente si el atacante tiene nivel superior
        scaleWorldBossToPlayer(boss, attacker);

        // 2. Tope de daño por golpe (Máximo 5% de la salud máxima del jefe por golpe)
        // Esto previene que armas rotas o combinaciones infinitas eliminen al jefe de 1-2 golpes.
        float maxSingleHit = (float) (boss.getMaxHealth() * 0.05D);
        if (event.getNewDamage() > maxSingleHit) {
            event.setNewDamage(maxSingleHit);
        }

        // 3. Resistencia a spam masivo de golpes (Defensa Adaptativa)
        long now = boss.level().getGameTime();
        long lastHitTime = boss.getPersistentData().getLong("wb_last_hit_tick");
        int burstHitCount = boss.getPersistentData().getInt("wb_burst_hit_count");

        if (now - lastHitTime < 15) { // Golpes en menos de 0.75 segundos
            burstHitCount++;
            if (burstHitCount >= 3) {
                // Reducir daño de spam continuo para forzar interacción y tiempos de recarga
                event.setNewDamage(event.getNewDamage() * 0.65F);
            }
        } else {
            burstHitCount = 1;
        }

        boss.getPersistentData().putLong("wb_last_hit_tick", now);
        boss.getPersistentData().putInt("wb_burst_hit_count", burstHitCount);
    }

    /**
     * Sistema de correa y anclaje (Leash/Tether) para Jefes y Minijefes:
     * Si la criatura se aleja de su arena asignada:
     * - A más de maxRadius (35 bloques): desengancha al jugador agresor y le ordena regresar al centro.
     * - A más de resetRadius (47 bloques) o si es sacado verticalmente (> 28 bloques): teletransporte instantáneo al centro,
     *   regeneración total de vida (100%), reseteo de agresividad y efectos visuales/sonoros de castigo/reinicio.
     */
    public static void handleBossLeash(Mob boss) {
        if (boss == null || boss.level().isClientSide() || !boss.isAlive()) return;
        net.minecraft.nbt.CompoundTag tag = boss.getPersistentData();

        // Si no tiene centro explícito registrado pero califica como World Boss, anclar a su ubicación actual
        if (!tag.contains("arena_center_x")) {
            if (isWorldBoss(boss) || tag.getBoolean("is_overworld_miniboss")) {
                tag.putDouble("arena_center_x", boss.getX());
                tag.putDouble("arena_center_y", boss.getY());
                tag.putDouble("arena_center_z", boss.getZ());
                tag.putDouble("arena_max_radius", 35.0D);
            } else {
                return;
            }
        }

        double cx = tag.getDouble("arena_center_x");
        double cy = tag.getDouble("arena_center_y");
        double cz = tag.getDouble("arena_center_z");
        double maxRadius = tag.contains("arena_max_radius") ? tag.getDouble("arena_max_radius") : 35.0D;
        double resetRadius = maxRadius + 12.0D; // ~47 bloques

        double dx = boss.getX() - cx;
        double dy = boss.getY() - cy;
        double dz = boss.getZ() - cz;
        double distSq = dx * dx + dz * dz;

        if (distSq > resetRadius * resetRadius || Math.abs(dy) > 28.0D) {
            // Reset total por fuga/abuso de rango
            boss.setTarget(null);
            boss.teleportTo(cx, cy, cz);
            boss.setDeltaMovement(0, 0, 0);
            boss.setHealth(boss.getMaxHealth());

            if (boss.level() instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, cx, cy + 1.2D, cz, 60, 1.5D, 1.5D, 1.5D, 0.2D);
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, cx, cy + 1.2D, cz, 3, 0.5D, 0.5D, 0.5D, 0.0D);
                sl.playSound(null, cx, cy, cz, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.HOSTILE, 1.8F, 0.8F);
                sl.playSound(null, cx, cy, cz, net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 1.2F);
            }
        } else if (distSq > maxRadius * maxRadius) {
            // Fuera de la arena permitida: Retorno forzado hacia el centro
            boss.setTarget(null);
            boss.getNavigation().moveTo(cx, cy, cz, 1.25D);
            if (boss.level() instanceof ServerLevel sl && boss.tickCount % 40 == 0) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, boss.getX(), boss.getY() + 1.0D, boss.getZ(), 10, 0.3D, 0.5D, 0.3D, 0.02D);
            }
        }
    }
}
