/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.EnderMan
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.monster.MagmaCube
 *  net.minecraft.world.entity.monster.Slime
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;

import com.soybe.awakeningsystem.DungeonManager;
import com.soybe.awakeningsystem.IsekaiWorldManager;
import com.soybe.awakeningsystem.MurimWorldManager;
import com.soybe.awakeningsystem.PagodaTrialManager;
import com.soybe.awakeningsystem.RpgAffixManager;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.TowerManager;
import com.soybe.awakeningsystem.entity.AbstractAbyssMageEntity;
import com.soybe.awakeningsystem.entity.CryoRegisvineEntity;
import com.soybe.awakeningsystem.entity.GenshinSlimeEntity;
import com.soybe.awakeningsystem.entity.HilichurlEntity;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import com.soybe.awakeningsystem.entity.RuinGuardEntity;
import com.soybe.awakeningsystem.entity.RuinHunterEntity;
import com.soybe.awakeningsystem.entity.StonehideLawachurlEntity;
import com.soybe.awakeningsystem.entity.WyvernEntity;
import com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity;
import com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity;
import com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;
import com.soybe.awakeningsystem.world.WorldBossScalingManager;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import java.util.Locale;

public final class RpgLevelZoneManager {
    private static final Random RNG = new Random();

    private RpgLevelZoneManager() {
    }

    public static int getZoneLevel(Level level, BlockPos pos) {
        if (level.dimension().equals(DungeonManager.DUNGEON_REALM)) {
            DungeonManager.DungeonSession session = DungeonManager.getSessionAt(pos);
            if (session != null) {
                return DungeonManager.getRankBaseLevel(session.rank) + Math.max(0, session.currentFloor - 1) * 2;
            }
            return 20;
        }
        if (com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(level)) {
            int floor = com.soybe.awakeningsystem.tower.GreatTowerWorldManager.getFloorForPos(level, pos);
            return towerFloorLevel(floor, false);
        }
        if (level.dimension().equals(TowerManager.RANKER_TOWER)) {
            return 50;
        }
        if (level.dimension().equals(PagodaTrialManager.PAGODA_REALM)) {
            return 60;
        }
        if (level.dimension() == Level.NETHER) {
            return 50 + RNG.nextInt(25);
        }
        if (level.dimension() == Level.END) {
            return 80 + RNG.nextInt(21);
        }
        if (level.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
            return 45 + RNG.nextInt(25);
        }
        if (level.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            return 55 + RNG.nextInt(25);
        }
        if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            return 35 + RNG.nextInt(20);
        }
        BlockPos spawn = level.getSharedSpawnPos();
        double dist = Math.sqrt(pos.distSqr((Vec3i)spawn));
        if (dist <= 500.0) {
            return 1 + (int)(dist / 500.0 * 29.0);
        }
        if (dist <= 1500.0) {
            return 30 + (int)((dist - 500.0) / 1000.0 * 50.0);
        }
        if (dist <= 4000.0) {
            return 80 + (int)((dist - 1500.0) / 2500.0 * 100.0);
        }
        if (dist <= 8000.0) {
            return 180 + (int)((dist - 4000.0) / 4000.0 * 140.0);
        }
        if (dist <= 13000.0) {
            return 320 + (int)((dist - 8000.0) / 5000.0 * 100.0);
        }
        return 420 + Math.min(80, (int)((dist - 13000.0) / 5000.0 * 80.0));
    }

    public static int getDimensionMinLevel(Level level) {
        if (level.dimension() == Level.NETHER) {
            return 50;
        }
        if (level.dimension() == Level.END) {
            return 75;
        }
        if (level.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            return 40;
        }
        if (level.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
            return 35;
        }
        if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            return 25;
        }
        return 1;
    }

    /**
     * Techo de nivel dinámico que varía orgánicamente con el nivel del usuario.
     * En el Overworld considera la distancia del spawn. En otras dimensiones escala con el nivel del jugador.
     */
    public static int getMaxWildLevelForPlayer(Level level, int playerLevel, BlockPos pos) {
        int safePlayerLvl = Math.max(1, playerLevel);
        if (level.dimension() == Level.OVERWORLD) {
            BlockPos spawnPos = level.getSharedSpawnPos();
            double distFromSpawn = Math.sqrt(pos.distSqr(new Vec3i(spawnPos.getX(), pos.getY(), spawnPos.getZ())));
            double distanceFactor = Math.min(1.0D, distFromSpawn / 15000.0D);
            // no fixed ceiling: monsters keep up with the player, however high their level
            return Math.max(30, (int) Math.round(safePlayerLvl * (1.0D + distanceFactor + 0.05D)) + 6);
        }
        if (level.dimension() == Level.NETHER) {
            return Math.max(55, safePlayerLvl + 5);
        }
        if (level.dimension() == Level.END) {
            return Math.max(80, safePlayerLvl + 5);
        }
        if (level.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            return Math.max(45, safePlayerLvl + 4);
        }
        if (level.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
            return Math.max(40, safePlayerLvl + 4);
        }
        if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            return Math.max(30, safePlayerLvl + 4);
        }
        return safePlayerLvl + 5;
    }

    public static int getMaxWildLevelForDimension(Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            return 500;
        }
        return 150;
    }

    public static String getZoneName(int level) {
        if (level < 30) {
            return "\u00a7aNovice Plains (Lv. 1-30)";
        }
        if (level < 80) {
            return "\u00a7eForbidden Forest & Ruins (Lv. 30-80)";
        }
        if (level < 180) {
            return "\u00a76Siderite Wastelands (Lv. 80-180)";
        }
        if (level < 320) {
            return "\u00a7bSoulforge & Blazing Lands (Lv. 180-320)";
        }
        if (level < 420) {
            return "\u00a7cAdamantite Peaks (Lv. 320-420)";
        }
        return "\u00a74\u00a7lAbyss of Calamities (Lv. 420-500+)";
    }

    public static boolean isEligibleForRpgScaling(LivingEntity living) {
        if (living == null || !living.isAlive()) return false;
        if (living instanceof Player) return false;
        if (living.getTags().contains("shadow_soldier")) return false;
        if (living.getTags().contains("no_rpg_scaling")) return false;
        if (living instanceof TamableAnimal tamable && tamable.isTame() && tamable.getOwnerUUID() != null) return false;
        if (living.getPersistentData().getBoolean("Tamed") && living.getPersistentData().hasUUID("Owner")) return false;
        if (living instanceof Enemy) return true;
        if (com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(living)) return true;
        String typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).toString().toLowerCase(Locale.ROOT);
        if (typeKey.startsWith("iceandfire:") || living.getClass().getName().contains("iceandfire")) return true;
        if (com.soybe.awakeningsystem.compat.iceandfire.IceAndFireCompat.isHostileOrExtractable(living)) {
            if (living instanceof Mob mob && mob.getTarget() != null) return true;
            if (living.getAttribute(Attributes.ATTACK_DAMAGE) != null && living.getMaxHealth() >= 20.0F) return true;
        }
        return false;
    }

    /**
     * A Great Tower monster's level comes from its floor, not from the player: five levels a floor
     * (floor 1 about 5, floor 10 about 50, floor 20 about 100, floor 100 about 500), bosses ten above.
     */
    public static int towerFloorLevel(int floor, boolean boss) {
        int base = Math.max(1, floor * 5);
        return boss ? base + 10 : base + RNG.nextInt(5);
    }

    public static void scaleMobOnSpawn(Mob mob, ServerLevel level) {
        int mobLevel;
        if (mob.getTags().contains("rpg_scaled") || mob.getTags().contains("shadow_soldier") || mob.getTags().contains("dungeon_mob") || mob.getTags().contains("tower_mob") || mob.getTags().contains("pagoda_trial_mob")) {
            return;
        }
        if (com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(level)) {
            // the tower has its own scale: by floor, whatever the player's level
            if (!isEligibleForRpgScaling(mob)) return;
            mob.addTag("rpg_scaled");
            int floor = com.soybe.awakeningsystem.tower.GreatTowerWorldManager.getFloorForPos(level, mob.blockPosition());
            applyMobLevelAttributes(mob, towerFloorLevel(floor, com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(mob)), false);
            return;
        }
        if (mob instanceof com.soybe.awakeningsystem.entity.LimboStalkerCreature || com.soybe.awakeningsystem.limbo.LimboWorldManager.isLimboDimension(level)) {
            return;
        }
        if (level.dimension().equals(DungeonManager.DUNGEON_REALM)
                || level.dimension().equals(TowerManager.RANKER_TOWER)
                || level.dimension().equals(PagodaTrialManager.PAGODA_REALM)
                || com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(level)) {
            return;
        }
        if (WorldBossScalingManager.isWorldBoss((LivingEntity)mob)) {
            return;
        }
        if (!isEligibleForRpgScaling(mob)) {
            return;
        }
        if (mob instanceof HypostasisBaseBossEntity || mob instanceof AnemoRevivalOrbEntity || mob instanceof ElectroRevivalPrismEntity || mob instanceof EnderMan && level.dimension() == Level.END) {
            return;
        }
        mob.addTag("rpg_scaled");

        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, mob.getBoundingBox().inflate(96.0), p -> p.isAlive() && !p.isSpectator());

        if (!nearbyPlayers.isEmpty()) {
            int maxPlayerLvl = 1;
            for (ServerPlayer sp : nearbyPlayers) {
                int pLvl = Math.max(1, SystemEvents.data((Player)sp).level());
                if (pLvl > maxPlayerLvl) {
                    maxPlayerLvl = pLvl;
                }
            }

            int maxCap = getMaxWildLevelForPlayer(level, maxPlayerLvl, mob.blockPosition());
            int variance = RNG.nextInt(5) - 2; // Variación orgánica de ±2 niveles

            if (level.dimension() == Level.OVERWORLD) {
                // Solo en el Overworld: se mantiene el escalado de distancia respecto al spawn
                BlockPos spawnPos = level.getSharedSpawnPos();
                double distFromSpawn = Math.sqrt(mob.distanceToSqr(spawnPos.getX(), mob.getY(), spawnPos.getZ()));
                double distanceFactor = Math.min(0.40D, distFromSpawn / 5000.0D);
                int baseScaled = (int) Math.round(maxPlayerLvl * (1.0D + distanceFactor));
                int targetLevel = Math.max(1, baseScaled + variance);
                int zoneBase = RpgLevelZoneManager.getZoneLevel((Level)level, mob.blockPosition());
                mobLevel = Math.min(maxCap, Math.max(zoneBase, targetLevel));
            } else {
                // Nether, End, Isekai, Murim, Teyvat y demás dimensiones:
                // Sin inflación artificial por distancia; escalado directo al jugador y piso de la dimensión
                int dimFloor = getDimensionMinLevel(level);
                int targetLevel = Math.max(dimFloor, maxPlayerLvl + variance);
                mobLevel = Math.min(maxCap, targetLevel);
            }
        } else {
            int zoneBase = RpgLevelZoneManager.getZoneLevel((Level)level, mob.blockPosition());
            mobLevel = Math.min(getMaxWildLevelForDimension(level), zoneBase);
        }
        RpgLevelZoneManager.applyMobLevelAttributes(mob, mobLevel, false);
    }

    public static void applyMobLevelAttributes(Mob mob, int mobLevel, boolean preserveHpRatio) {
        AttributeInstance speedAttr;
        AttributeInstance kbAttr;
        AttributeInstance toughAttr;
        AttributeInstance armorAttr;
        AttributeInstance attackAttr;
        if (mob == null || !mob.isAlive()) {
            return;
        }
        if (mob instanceof com.soybe.awakeningsystem.entity.LimboStalkerCreature || com.soybe.awakeningsystem.limbo.LimboWorldManager.isLimboDimension(mob.level())) {
            return;
        }
        if (!mob.getPersistentData().contains("rpg_orig_hp")) {
            AttributeInstance hp = mob.getAttribute(Attributes.MAX_HEALTH);
            mob.getPersistentData().putDouble("rpg_orig_hp", hp != null ? hp.getBaseValue() : 20.0);
        }
        if (!mob.getPersistentData().contains("rpg_orig_dmg")) {
            AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            mob.getPersistentData().putDouble("rpg_orig_dmg", atk != null ? atk.getBaseValue() : 3.0);
        }
        if (!mob.getPersistentData().contains("rpg_orig_name")) {
            String name = com.soybe.awakeningsystem.entity.AbyssalOrcEntity.speciesName(mob).getString();
            mob.getPersistentData().putString("rpg_orig_name", name);
        }
        double baseHp = mob.getPersistentData().getDouble("rpg_orig_hp");
        double baseDmg = mob.getPersistentData().getDouble("rpg_orig_dmg");
        String originalName = mob.getPersistentData().getString("rpg_orig_name");
        mob.getPersistentData().putInt("rpg_mob_level", mobLevel);

        double hpMultiplier;
        if (mobLevel <= 20) {
            hpMultiplier = 1.0 + (double)mobLevel * 0.08;
        } else if (mobLevel <= 60) {
            hpMultiplier = 2.6 + (double)(mobLevel - 20) * 0.06;
        } else if (mobLevel <= 100) {
            hpMultiplier = 5.0 + (double)(mobLevel - 60) * 0.04;
        } else {
            // past level 100 health keeps climbing steadily, so high-level monsters are real fights
            // (a zombie: ~610 HP at level 500, ~2400 at level 2000)
            hpMultiplier = 6.6 + (double)(mobLevel - 100) * 0.06;
        }
        double newHp = baseHp * hpMultiplier;
        AttributeInstance maxHpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHpAttr != null) {
            if (preserveHpRatio) {
                float ratio = Math.max(0.05f, mob.getHealth() / mob.getMaxHealth());
                maxHpAttr.setBaseValue(newHp);
                mob.setHealth((float)(newHp * (double)ratio));
            } else {
                maxHpAttr.setBaseValue(newHp);
                mob.setHealth((float)newHp);
            }
        }
        if ((attackAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE)) != null && baseDmg > 0.0) {
            double dmgMultiplier;
            if (mobLevel <= 20) {
                dmgMultiplier = 1.0 + (double)mobLevel * 0.015;
            } else if (mobLevel <= 60) {
                dmgMultiplier = 1.30 + (double)(mobLevel - 20) * 0.010;
            } else if (mobLevel <= 100) {
                dmgMultiplier = 1.70 + (double)(mobLevel - 60) * 0.006;
            } else {
                dmgMultiplier = 1.94 + (double)(mobLevel - 100) * 0.003;
            }
            double flatDmg = Math.min(18.0, (double)mobLevel * 0.12);
            double finalDmg = Math.min(65.0, baseDmg * dmgMultiplier + flatDmg);
            attackAttr.setBaseValue(finalDmg);
        }
        if ((armorAttr = mob.getAttribute(Attributes.ARMOR)) != null) {
            double armor = Math.min(26.0, (double)mobLevel * 0.3);
            armorAttr.setBaseValue(Math.max(armorAttr.getBaseValue(), armor));
        }
        if ((toughAttr = mob.getAttribute(Attributes.ARMOR_TOUGHNESS)) != null && mobLevel >= 20) {
            double toughness = Math.min(16.0, (double)(mobLevel - 15) * 0.22);
            toughAttr.setBaseValue(Math.max(toughAttr.getBaseValue(), toughness));
        }
        if ((kbAttr = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE)) != null) {
            double kbRes = Math.min(0.7, (double)mobLevel * 0.008);
            kbAttr.setBaseValue(Math.max(kbAttr.getBaseValue(), kbRes));
        }
        if ((speedAttr = mob.getAttribute(Attributes.MOVEMENT_SPEED)) != null && mobLevel >= 25) {
            double speedBonus = Math.min(0.06, (double)(mobLevel - 20) * 0.001);
            speedAttr.setBaseValue(speedAttr.getBaseValue() + speedBonus);
        }
        ChatFormatting color = mobLevel >= 80 ? ChatFormatting.DARK_RED : (mobLevel >= 50 ? ChatFormatting.RED : (mobLevel >= 25 ? ChatFormatting.GOLD : (mobLevel >= 15 ? ChatFormatting.YELLOW : ChatFormatting.GREEN)));
        mob.setCustomName(Component.literal("[Nv. " + mobLevel + "] ").withStyle(color)
                .append(com.soybe.awakeningsystem.entity.AbyssalOrcEntity.speciesName(mob).copy().withStyle(ChatFormatting.WHITE)));
        mob.setCustomNameVisible(false);
    }

    public static void scaleMobToPlayer(Mob mob, ServerPlayer player) {
        if (mob == null || player == null || !mob.isAlive()) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (WorldBossScalingManager.isWorldBoss((LivingEntity)mob)) {
            return;
        }
        if (mob.getTags().contains("shadow_soldier") || mob.getTags().contains("dungeon_mob") || mob.getTags().contains("tower_mob") || mob.getTags().contains("pagoda_trial_mob")) {
            return;
        }
        if (mob instanceof com.soybe.awakeningsystem.entity.LimboStalkerCreature || com.soybe.awakeningsystem.limbo.LimboWorldManager.isLimboDimension(mob.level())) {
            return;
        }
        if (mob.level().dimension().equals(DungeonManager.DUNGEON_REALM)
                || mob.level().dimension().equals(TowerManager.RANKER_TOWER)
                || mob.level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
                || com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(mob.level())) {
            return;
        }
        if (!isEligibleForRpgScaling(mob)) {
            return;
        }
        int playerLevel = Math.max(1, SystemEvents.data((Player)player).level());
        int maxCap = getMaxWildLevelForPlayer(mob.level(), playerLevel, mob.blockPosition());
        int targetLevel;

        if (mob.level().dimension() == Level.OVERWORLD) {
            // Overworld: mantiene el escalado de distancia respecto al spawn
            BlockPos spawnPos = mob.level().getSharedSpawnPos();
            double distFromSpawn = Math.sqrt(mob.distanceToSqr(spawnPos.getX(), mob.getY(), spawnPos.getZ()));
            double distanceFactor = Math.min(0.40D, distFromSpawn / 5000.0D);
            int baseScaled = (int) Math.round(playerLevel * (1.0D + distanceFactor));
            int zoneBase = RpgLevelZoneManager.getZoneLevel(mob.level(), mob.blockPosition());
            targetLevel = Math.min(maxCap, Math.max(zoneBase, baseScaled));
        } else {
            // Nether, End, Isekai, Murim, Teyvat: escalado directo al jugador y piso de la dimensión
            int dimFloor = getDimensionMinLevel(mob.level());
            targetLevel = Math.min(maxCap, Math.max(dimFloor, playerLevel));
        }

        int currentMobLevel = mob.getPersistentData().getInt("rpg_mob_level");
        // Reajusta si el mob supera el cap dinámico del usuario (ej. orcos/bestias previos de nivel 200/300),
        // o si no tenía nivel RPG (<= 0), o si está notablemente desfasado respecto al target (> 6 niveles):
        if (currentMobLevel > maxCap || currentMobLevel <= 0 || Math.abs(currentMobLevel - targetLevel) > 6) {
            int variance = RNG.nextInt(5) - 2;
            int newLevel = Math.min(maxCap, Math.max(1, targetLevel + variance));
            RpgLevelZoneManager.applyMobLevelAttributes(mob, newLevel, true);
        }
    }

    public static void handleMobDrops(LivingEntity entity, ServerLevel level) {
        Player killer;
        long resinUntil;
        if (entity instanceof WyvernEntity) {
            int scales = 2 + RNG.nextInt(4);
            entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.WYVERN_SCALE.get(), scales));
            int fangs = 1 + RNG.nextInt(3);
            entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.WYVERN_FANG.get(), fangs));
            if (RNG.nextDouble() < 0.18) {
                entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.WYVERN_EGG.get(), 1));
            }
        }
        boolean doubleDrops = false;
        LivingEntity livingEntity = entity.getLastHurtByMob();
        if (livingEntity instanceof Player && (resinUntil = (killer = (Player)livingEntity).getPersistentData().getLong("condensed_resin_until")) > level.getGameTime()) {
            doubleDrops = true;
        }

        boolean isTeyvat = level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD);
        if (isTeyvat) {
            if (entity instanceof GenshinSlimeEntity) {
                GenshinSlimeEntity gSlime = (GenshinSlimeEntity)entity;
                boolean isBig = gSlime.isBig();
                int multiplier = doubleDrops ? 2 : 1;
                int condCount = (1 + RNG.nextInt(isBig ? 4 : 2)) * multiplier;
                if (SystemItems.SLIME_CONDENSATE.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.SLIME_CONDENSATE.get(), condCount));
                }
                if (isBig && RNG.nextDouble() < 0.4 && SystemItems.SLIME_SECRETIONS.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.SLIME_SECRETIONS.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
                if (isBig && RNG.nextDouble() < 0.15 && SystemItems.SLIME_CONCENTRATE.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.SLIME_CONCENTRATE.get(), multiplier));
                }
                if (RNG.nextDouble() < 0.12 && SystemItems.COSMIC_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.COSMIC_FRAGMENT.get(), (2 + RNG.nextInt(5)) * multiplier));
                }
            }
            if (entity instanceof HilichurlEntity) {
                int multiplier = doubleDrops ? 2 : 1;
                int maskCount = (1 + RNG.nextInt(2)) * multiplier;
                if (SystemItems.DAMAGED_MASK.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.DAMAGED_MASK.get(), maskCount));
                }
                if (RNG.nextDouble() < 0.2 && SystemItems.STAINED_MASK.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.STAINED_MASK.get(), multiplier));
                }
                if (RNG.nextDouble() < 0.25 && SystemItems.COSMIC_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.COSMIC_FRAGMENT.get(), (3 + RNG.nextInt(6)) * multiplier));
                }
                if (RNG.nextDouble() < 0.08 && SystemItems.PRIMOGEM.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
            }
            if (entity instanceof MitachurlEntity) {
                int multiplier = doubleDrops ? 2 : 1;
                if (SystemItems.HEAVY_HORN.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.HEAVY_HORN.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
                if (SystemItems.STAINED_MASK.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.STAINED_MASK.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
                if (RNG.nextDouble() < 0.35 && SystemItems.OMINOUS_MASK.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.OMINOUS_MASK.get(), multiplier));
                }
                if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.COSMIC_FRAGMENT.get(), (8 + RNG.nextInt(12)) * multiplier));
                }
                if (SystemItems.PRIMOGEM.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), (2 + RNG.nextInt(4)) * multiplier));
                }
                if (SystemItems.MORA_POUCH.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MORA_POUCH.get(), multiplier));
                }
            }
            if (entity instanceof RuinHunterEntity) {
                int multiplier = doubleDrops ? 2 : 1;
                if (SystemItems.CHAOS_DEVICE.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.CHAOS_DEVICE.get(), (3 + RNG.nextInt(4)) * multiplier));
                }
                if (SystemItems.CHAOS_CIRCUIT.isBound() && RNG.nextDouble() < 0.85) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.CHAOS_CIRCUIT.get(), (1 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.CHAOS_CORE.isBound() && RNG.nextDouble() < 0.5) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.CHAOS_CORE.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
                entity.spawnAtLocation(new ItemStack((ItemLike)Items.IRON_INGOT, (2 + RNG.nextInt(4)) * multiplier));
                if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.COSMIC_FRAGMENT.get(), (12 + RNG.nextInt(14)) * multiplier));
                }
                if (SystemItems.PRIMOGEM.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), (3 + RNG.nextInt(4)) * multiplier));
                }
                if (SystemItems.MORA_POUCH.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MORA_POUCH.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
            }
            if (entity instanceof RuinGuardEntity) {
                int multiplier = doubleDrops ? 2 : 1;
                if (SystemItems.CHAOS_DEVICE.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.CHAOS_DEVICE.get(), (2 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.CHAOS_CIRCUIT.isBound() && RNG.nextDouble() < 0.7) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.CHAOS_CIRCUIT.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
                if (SystemItems.CHAOS_CORE.isBound() && RNG.nextDouble() < 0.35) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.CHAOS_CORE.get(), multiplier));
                }
                entity.spawnAtLocation(new ItemStack((ItemLike)Items.IRON_INGOT, (2 + RNG.nextInt(3)) * multiplier));
                if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.COSMIC_FRAGMENT.get(), (8 + RNG.nextInt(12)) * multiplier));
                }
                if (SystemItems.PRIMOGEM.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), (2 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.MORA_POUCH.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MORA_POUCH.get(), multiplier));
                }
            }
            if (entity instanceof StonehideLawachurlEntity) {
                int multiplier = doubleDrops ? 2 : 1;
                if (SystemItems.HEAVY_HORN.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.HEAVY_HORN.get(), (2 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.BLACK_BRONZE_HORN.isBound() && RNG.nextDouble() < 0.75) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.BLACK_BRONZE_HORN.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
                if (SystemItems.BLACK_CRYSTAL_HORN.isBound() && RNG.nextDouble() < 0.4) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.BLACK_CRYSTAL_HORN.get(), multiplier));
                }
                if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.COSMIC_FRAGMENT.get(), (15 + RNG.nextInt(15)) * multiplier));
                }
                if (SystemItems.PRIMOGEM.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), (3 + RNG.nextInt(4)) * multiplier));
                }
                if (SystemItems.MORA_POUCH.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MORA_POUCH.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
            }
            if (entity instanceof CryoRegisvineEntity) {
                int multiplier = doubleDrops ? 2 : 1;
                if (SystemItems.HOARFROST_CORE.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.HOARFROST_CORE.get(), (1 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.SHIVADA_JADE_GEMSTONE.isBound() && RNG.nextDouble() < 0.3) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.SHIVADA_JADE_GEMSTONE.get(), multiplier));
                } else if (SystemItems.SHIVADA_JADE_CHUNK.isBound() && RNG.nextDouble() < 0.7) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.SHIVADA_JADE_CHUNK.get(), (1 + RNG.nextInt(2)) * multiplier));
                } else if (SystemItems.SHIVADA_JADE_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.SHIVADA_JADE_FRAGMENT.get(), (2 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.PRIMOGEM.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), (4 + RNG.nextInt(6)) * multiplier));
                }
                if (SystemItems.MORA_POUCH.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MORA_POUCH.get(), (2 + RNG.nextInt(3)) * multiplier));
                }
            }
            if (entity instanceof AbstractAbyssMageEntity) {
                int multiplier = doubleDrops ? 2 : 1;
                if (SystemItems.DEAD_LEY_LINE_BRANCH.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.DEAD_LEY_LINE_BRANCH.get(), (1 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.DEAD_LEY_LINE_LEAVES.isBound() && RNG.nextDouble() < 0.7) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.DEAD_LEY_LINE_LEAVES.get(), (1 + RNG.nextInt(2)) * multiplier));
                }
                if (SystemItems.LEY_LINE_SPROUT.isBound() && RNG.nextDouble() < 0.35) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.LEY_LINE_SPROUT.get(), multiplier));
                }
                if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.COSMIC_FRAGMENT.get(), (8 + RNG.nextInt(10)) * multiplier));
                }
                if (SystemItems.PRIMOGEM.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), (2 + RNG.nextInt(3)) * multiplier));
                }
                if (SystemItems.MORA_POUCH.isBound()) {
                    entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MORA_POUCH.get(), multiplier));
                }
            }
        }
        if (!entity.getPersistentData().contains("rpg_mob_level")) {
            return;
        }
        int mobLevel = entity.getPersistentData().getInt("rpg_mob_level");
        if (isTeyvat) {
            double primoChance = 0.05 + (double)mobLevel * 0.002;
            if (RNG.nextDouble() < primoChance) {
                int amount = 1 + RNG.nextInt(Math.max(1, mobLevel / 20));
                ItemStack primos = SystemItems.PRIMOGEM.isBound() ? new ItemStack((ItemLike)SystemItems.PRIMOGEM.get(), amount) : new ItemStack((ItemLike)Items.AMETHYST_SHARD, amount);
                entity.spawnAtLocation(primos);
            }
            double moraChance = 0.15 + (double)mobLevel * 0.002;
            if (RNG.nextDouble() < moraChance && SystemItems.MORA.isBound()) {
                int amount = 1 + RNG.nextInt(Math.max(1, mobLevel / 30));
                entity.spawnAtLocation(new ItemStack((ItemLike)SystemItems.MORA.get(), amount));
            }
        }
        double equipChance = Math.min(0.18D, 0.03 + (double)mobLevel * 0.0006);
        if (RNG.nextDouble() < equipChance) {
            ItemStack dropStack = RpgLevelZoneManager.rollRandomRpgEquipment(level, mobLevel);
            RpgAffixManager.rollAffixes(dropStack, mobLevel);
            entity.spawnAtLocation(dropStack);
        }
    }

    public enum EquipTier {
        LEATHER,
        CHAINMAIL,
        IRON,
        GOLD,
        DIAMOND,
        NETHERITE,
        SIDERITE,
        SOUL_ALLOY,
        MITHRIL,
        ADAMANTITE,
        SPIRIT_JADE,
        ASTRAL,
        CELESTIAL,
        ASTRALITE,
        PRIMORDIAL
    }

    private static ItemStack getArmorPiece(EquipTier tier, int slot) {
        return switch (tier) {
            case LEATHER -> switch (slot) {
                case 0 -> new ItemStack((ItemLike)Items.LEATHER_HELMET);
                case 1 -> new ItemStack((ItemLike)Items.LEATHER_CHESTPLATE);
                case 2 -> new ItemStack((ItemLike)Items.LEATHER_LEGGINGS);
                default -> new ItemStack((ItemLike)Items.LEATHER_BOOTS);
            };
            case CHAINMAIL -> switch (slot) {
                case 0 -> new ItemStack((ItemLike)Items.CHAINMAIL_HELMET);
                case 1 -> new ItemStack((ItemLike)Items.CHAINMAIL_CHESTPLATE);
                case 2 -> new ItemStack((ItemLike)Items.CHAINMAIL_LEGGINGS);
                default -> new ItemStack((ItemLike)Items.CHAINMAIL_BOOTS);
            };
            case IRON -> switch (slot) {
                case 0 -> new ItemStack((ItemLike)Items.IRON_HELMET);
                case 1 -> new ItemStack((ItemLike)Items.IRON_CHESTPLATE);
                case 2 -> new ItemStack((ItemLike)Items.IRON_LEGGINGS);
                default -> new ItemStack((ItemLike)Items.IRON_BOOTS);
            };
            case GOLD -> switch (slot) {
                case 0 -> new ItemStack((ItemLike)Items.GOLDEN_HELMET);
                case 1 -> new ItemStack((ItemLike)Items.GOLDEN_CHESTPLATE);
                case 2 -> new ItemStack((ItemLike)Items.GOLDEN_LEGGINGS);
                default -> new ItemStack((ItemLike)Items.GOLDEN_BOOTS);
            };
            case DIAMOND -> switch (slot) {
                case 0 -> new ItemStack((ItemLike)Items.DIAMOND_HELMET);
                case 1 -> new ItemStack((ItemLike)Items.DIAMOND_CHESTPLATE);
                case 2 -> new ItemStack((ItemLike)Items.DIAMOND_LEGGINGS);
                default -> new ItemStack((ItemLike)Items.DIAMOND_BOOTS);
            };
            case NETHERITE -> switch (slot) {
                case 0 -> new ItemStack((ItemLike)Items.NETHERITE_HELMET);
                case 1 -> new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE);
                case 2 -> new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS);
                default -> new ItemStack((ItemLike)Items.NETHERITE_BOOTS);
            };
            case SIDERITE -> {
                if (SystemItems.SIDERITE_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.SIDERITE_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.SIDERITE_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.SIDERITE_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.SIDERITE_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.NETHERITE, slot);
            }
            case SOUL_ALLOY -> {
                if (SystemItems.SOUL_ALLOY_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.SOUL_ALLOY_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.SOUL_ALLOY_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.SOUL_ALLOY_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.SOUL_ALLOY_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.NETHERITE, slot);
            }
            case MITHRIL -> {
                if (SystemItems.MITHRIL_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.MITHRIL_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.MITHRIL_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.MITHRIL_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.MITHRIL_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.NETHERITE, slot);
            }
            case ADAMANTITE -> {
                if (SystemItems.ADAMANTITE_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.MITHRIL, slot);
            }
            case SPIRIT_JADE -> {
                if (SystemItems.SPIRIT_JADE_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.ADAMANTITE, slot);
            }
            case ASTRAL -> {
                if (SystemItems.ASTRAL_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.ASTRAL_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.ASTRAL_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.ASTRAL_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.ASTRAL_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.SPIRIT_JADE, slot);
            }
            case CELESTIAL -> {
                if (SystemItems.CELESTIAL_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.CELESTIAL_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.CELESTIAL_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.CELESTIAL_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.CELESTIAL_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.ASTRAL, slot);
            }
            case ASTRALITE -> {
                if (SystemItems.ASTRALITE_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.ASTRALITE_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.ASTRALITE_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.ASTRALITE_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.ASTRALITE_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.CELESTIAL, slot);
            }
            case PRIMORDIAL -> {
                if (SystemItems.PRIMORDIAL_HELMET.isBound()) {
                    yield switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.PRIMORDIAL_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.PRIMORDIAL_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.PRIMORDIAL_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.PRIMORDIAL_BOOTS.get());
                    };
                }
                yield getArmorPiece(EquipTier.ASTRALITE, slot);
            }
        };
    }

    private static ItemStack getWeaponPiece(EquipTier tier, boolean isAxe) {
        return switch (tier) {
            case LEATHER, CHAINMAIL -> isAxe ? new ItemStack((ItemLike)Items.STONE_AXE) : new ItemStack((ItemLike)Items.STONE_SWORD);
            case IRON -> isAxe ? new ItemStack((ItemLike)Items.IRON_AXE) : new ItemStack((ItemLike)Items.IRON_SWORD);
            case GOLD -> isAxe ? new ItemStack((ItemLike)Items.GOLDEN_AXE) : new ItemStack((ItemLike)Items.GOLDEN_SWORD);
            case DIAMOND -> isAxe ? new ItemStack((ItemLike)Items.DIAMOND_AXE) : new ItemStack((ItemLike)Items.DIAMOND_SWORD);
            case NETHERITE -> isAxe ? new ItemStack((ItemLike)Items.NETHERITE_AXE) : new ItemStack((ItemLike)Items.NETHERITE_SWORD);
            case SIDERITE -> {
                if (isAxe && SystemItems.SIDERITE_HAMAXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.SIDERITE_HAMAXE.get());
                } else if (!isAxe && SystemItems.SIDERITE_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.SIDERITE_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.NETHERITE, isAxe);
            }
            case SOUL_ALLOY -> {
                if (isAxe && SystemItems.SOUL_ALLOY_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.SOUL_ALLOY_AXE.get());
                } else if (!isAxe && SystemItems.SOUL_ALLOY_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.SOUL_ALLOY_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.NETHERITE, isAxe);
            }
            case MITHRIL -> {
                if (isAxe && SystemItems.MITHRIL_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.MITHRIL_AXE.get());
                } else if (!isAxe && SystemItems.MITHRIL_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.MITHRIL_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.NETHERITE, isAxe);
            }
            case ADAMANTITE -> {
                if (isAxe && SystemItems.ADAMANTITE_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.ADAMANTITE_AXE.get());
                } else if (!isAxe && SystemItems.ADAMANTITE_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.ADAMANTITE_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.MITHRIL, isAxe);
            }
            case SPIRIT_JADE -> {
                if (isAxe && SystemItems.SPIRIT_JADE_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_AXE.get());
                } else if (!isAxe && SystemItems.SPIRIT_JADE_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.ADAMANTITE, isAxe);
            }
            case ASTRAL -> {
                if (isAxe && SystemItems.ASTRAL_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.ASTRAL_AXE.get());
                } else if (!isAxe && SystemItems.ASTRAL_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.ASTRAL_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.SPIRIT_JADE, isAxe);
            }
            case CELESTIAL -> {
                if (isAxe && SystemItems.CELESTIAL_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.CELESTIAL_AXE.get());
                } else if (!isAxe && SystemItems.CELESTIAL_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.CELESTIAL_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.ASTRAL, isAxe);
            }
            case ASTRALITE -> {
                if (isAxe && SystemItems.ASTRALITE_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.ASTRALITE_AXE.get());
                } else if (!isAxe && SystemItems.ASTRALITE_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.ASTRALITE_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.CELESTIAL, isAxe);
            }
            case PRIMORDIAL -> {
                if (isAxe && SystemItems.PRIMORDIAL_AXE.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.PRIMORDIAL_AXE.get());
                } else if (!isAxe && SystemItems.PRIMORDIAL_SWORD.isBound()) {
                    yield new ItemStack((ItemLike)SystemItems.PRIMORDIAL_SWORD.get());
                }
                yield getWeaponPiece(EquipTier.ASTRALITE, isAxe);
            }
        };
    }

    public static ItemStack rollRandomRpgEquipment(Level level, int mobLevel) {
        boolean isArmor = RNG.nextInt(100) < 65;
        int slot = RNG.nextInt(4);
        boolean isAxe = RNG.nextBoolean();

        EquipTier chosenTier;

        if (level == null || level.dimension() == Level.OVERWORLD) {
            // ==========================================
            // OVERWORLD (MUNDO 1): Escala 1 a 500+
            // NUNCA suelta minerales de otros mundos
            // ==========================================
            if (mobLevel < 80) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.LEATHER;
                    case 1 -> EquipTier.CHAINMAIL;
                    default -> EquipTier.IRON;
                };
            } else if (mobLevel < 180) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.IRON;
                    case 1 -> EquipTier.GOLD;
                    default -> EquipTier.DIAMOND;
                };
            } else if (mobLevel < 280) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.DIAMOND;
                    case 1 -> EquipTier.NETHERITE;
                    default -> EquipTier.SIDERITE;
                };
            } else {
                // Nivel 280 a 500+ en Overworld: Siderita Meteórica, Forja de Almas / Soul Alloy y Netherite
                chosenTier = switch (RNG.nextInt(4)) {
                    case 0, 1 -> EquipTier.SOUL_ALLOY;
                    case 2 -> EquipTier.SIDERITE;
                    default -> EquipTier.NETHERITE;
                };
            }
        } else if (level.dimension() == Level.NETHER) {
            if (mobLevel < 150) {
                chosenTier = RNG.nextBoolean() ? EquipTier.GOLD : EquipTier.IRON;
            } else if (mobLevel < 280) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.DIAMOND;
                    case 1 -> EquipTier.NETHERITE;
                    default -> EquipTier.SOUL_ALLOY;
                };
            } else {
                chosenTier = RNG.nextBoolean() ? EquipTier.SOUL_ALLOY : EquipTier.NETHERITE;
            }
        } else if (level.dimension() == Level.END) {
            if (mobLevel < 250) {
                chosenTier = RNG.nextBoolean() ? EquipTier.DIAMOND : EquipTier.NETHERITE;
            } else {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.NETHERITE;
                    case 1 -> EquipTier.SIDERITE;
                    default -> EquipTier.SOUL_ALLOY;
                };
            }
        } else if (level.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            // ==========================================
            // ISEKAI (MUNDO 2): Escala 500 a 1000
            // Mithril y Adamantita
            // ==========================================
            if (mobLevel < 750) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.MITHRIL;
                    default -> EquipTier.SOUL_ALLOY;
                };
            } else {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.ADAMANTITE;
                    default -> EquipTier.MITHRIL;
                };
            }
        } else if (level.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
            // ==========================================
            // MURIM (MUNDO 3): Escala 1000 a 1500
            // Jade Espiritual, Astral y Celestial
            // ==========================================
            if (mobLevel < 1250) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.SPIRIT_JADE;
                    default -> EquipTier.ADAMANTITE;
                };
            } else {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.CELESTIAL;
                    case 1 -> EquipTier.ASTRAL;
                    default -> EquipTier.SPIRIT_JADE;
                };
            }
        } else if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)
                || com.soybe.awakeningsystem.limbo.LimboWorldManager.isLimboDimension(level)) {
            // ==========================================
            // TEYVAT / ABISMO / LIMBO (MUNDO 4): Escala 1500 a 2000+
            // Astralita y Primordial
            // ==========================================
            if (mobLevel < 1800) {
                chosenTier = EquipTier.ASTRALITE;
            } else {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.PRIMORDIAL;
                    default -> EquipTier.ASTRALITE;
                };
            }
        } else {
            // ==========================================
            // TORRE / DUNGEONS / MULTI-MUNDO UNIVERSAL:
            // Escala completa de 2000 niveles según el nivel del enemigo
            // ==========================================
            if (mobLevel < 80) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.LEATHER;
                    case 1 -> EquipTier.CHAINMAIL;
                    default -> EquipTier.IRON;
                };
            } else if (mobLevel < 180) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.IRON;
                    case 1 -> EquipTier.GOLD;
                    default -> EquipTier.DIAMOND;
                };
            } else if (mobLevel < 280) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.DIAMOND;
                    case 1 -> EquipTier.NETHERITE;
                    default -> EquipTier.SIDERITE;
                };
            } else if (mobLevel < 500) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.SOUL_ALLOY;
                    case 1 -> EquipTier.SIDERITE;
                    default -> EquipTier.NETHERITE;
                };
            } else if (mobLevel < 750) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.MITHRIL;
                    default -> EquipTier.SOUL_ALLOY;
                };
            } else if (mobLevel < 1000) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.ADAMANTITE;
                    default -> EquipTier.MITHRIL;
                };
            } else if (mobLevel < 1250) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.SPIRIT_JADE;
                    default -> EquipTier.ADAMANTITE;
                };
            } else if (mobLevel < 1500) {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0 -> EquipTier.CELESTIAL;
                    case 1 -> EquipTier.ASTRAL;
                    default -> EquipTier.SPIRIT_JADE;
                };
            } else if (mobLevel < 1800) {
                chosenTier = EquipTier.ASTRALITE;
            } else {
                chosenTier = switch (RNG.nextInt(3)) {
                    case 0, 1 -> EquipTier.PRIMORDIAL;
                    default -> EquipTier.ASTRALITE;
                };
            }
        }

        return isArmor ? getArmorPiece(chosenTier, slot) : getWeaponPiece(chosenTier, isAxe);
    }

    public static ItemStack rollRandomRpgEquipment(int mobLevel) {
        return rollRandomRpgEquipment(null, mobLevel);
    }
}
