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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
            return Math.min(100, Math.max(5, floor * 10 - 5 + RNG.nextInt(6)));
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
            return 1 + (int)(dist / 500.0 * 19.0);
        }
        if (dist <= 1500.0) {
            return 20 + (int)((dist - 500.0) / 1000.0 * 30.0);
        }
        if (dist <= 3500.0) {
            return 50 + (int)((dist - 1500.0) / 2000.0 * 30.0);
        }
        return 80 + Math.min(25, (int)((dist - 3500.0) / 3000.0 * 20.0));
    }

    public static String getZoneName(int level) {
        if (level < 20) {
            return "\u00a7aNovice Plains (Lv. 1-20)";
        }
        if (level < 50) {
            return "\u00a7eForbidden Forest & Ruins (Lv. 20-50)";
        }
        if (level < 80) {
            return "\u00a7cAshen & Fury Lands (Lv. 50-80)";
        }
        return "\u00a74\u00a7lAbyss of Calamities (Lv. 80-100+)";
    }

    public static void scaleMobOnSpawn(Mob mob, ServerLevel level) {
        int mobLevel;
        if (mob.getTags().contains("rpg_scaled") || mob.getTags().contains("shadow_soldier") || mob.getTags().contains("dungeon_mob") || mob.getTags().contains("tower_mob") || mob.getTags().contains("pagoda_trial_mob")) {
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
        if (!(mob instanceof Enemy)) {
            return;
        }
        if (mob instanceof HypostasisBaseBossEntity || mob instanceof AnemoRevivalOrbEntity || mob instanceof ElectroRevivalPrismEntity || mob instanceof EnderMan && level.dimension() == Level.END) {
            return;
        }
        mob.addTag("rpg_scaled");
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, mob.getBoundingBox().inflate(96.0), p -> p.isAlive() && !p.isSpectator());
        net.minecraft.core.BlockPos spawnPos = level.getSharedSpawnPos();
        double distFromSpawn = Math.sqrt(mob.distanceToSqr(spawnPos.getX(), mob.getY(), spawnPos.getZ()));
        double distanceFactor = Math.min(0.40D, distFromSpawn / 5000.0D);

        if (!nearbyPlayers.isEmpty()) {
            int maxPlayerLvl = 1;
            for (ServerPlayer sp : nearbyPlayers) {
                int pLvl = Math.max(1, SystemEvents.data((Player)sp).level());
                if (pLvl <= maxPlayerLvl) continue;
                maxPlayerLvl = pLvl;
            }
            int baseScaled = (int) Math.round(maxPlayerLvl * (1.0D + distanceFactor));
            int variance = RNG.nextInt(5) - 2;
            mobLevel = Math.max(1, baseScaled + variance);
            if (level.dimension() == Level.NETHER) {
                mobLevel = Math.max(mobLevel, 35 + RNG.nextInt(15));
            } else if (level.dimension() == Level.END) {
                mobLevel = Math.max(mobLevel, 70 + RNG.nextInt(20));
            } else if (level.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
                mobLevel = Math.max(mobLevel, 45 + RNG.nextInt(20));
            } else if (level.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
                mobLevel = Math.max(mobLevel, 55 + RNG.nextInt(20));
            }
        } else {
            mobLevel = RpgLevelZoneManager.getZoneLevel((Level)level, mob.blockPosition());
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
            String name = mob.getType().getDescription().getString();
            mob.getPersistentData().putString("rpg_orig_name", name);
        }
        double baseHp = mob.getPersistentData().getDouble("rpg_orig_hp");
        double baseDmg = mob.getPersistentData().getDouble("rpg_orig_dmg");
        String originalName = mob.getPersistentData().getString("rpg_orig_name");
        mob.getPersistentData().putInt("rpg_mob_level", mobLevel);
        double hpMultiplier = 1.0 + (double)mobLevel * 0.14 + (double)Math.max(0, mobLevel - 20) * 0.08 + (double)Math.max(0, mobLevel - 50) * 0.06;
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
            double dmgMultiplier = 1.0 + (double)mobLevel * 0.05;
            attackAttr.setBaseValue(baseDmg * dmgMultiplier + (double)mobLevel * 0.35);
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
                .append(Component.translatable(mob.getType().getDescriptionId()).withStyle(ChatFormatting.WHITE)));
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
        if (!(mob instanceof Enemy)) {
            return;
        }
        int playerLevel = Math.max(1, SystemEvents.data((Player)player).level());
        net.minecraft.core.BlockPos spawnPos = mob.level().getSharedSpawnPos();
        double distFromSpawn = Math.sqrt(mob.distanceToSqr(spawnPos.getX(), mob.getY(), spawnPos.getZ()));
        double distanceFactor = Math.min(0.40D, distFromSpawn / 5000.0D);
        int targetLevel = Math.max(1, (int) Math.round(playerLevel * (1.0D + distanceFactor)));

        int currentMobLevel = mob.getPersistentData().getInt("rpg_mob_level");
        if (currentMobLevel < targetLevel) {
            int newLevel = targetLevel + RNG.nextInt(3);
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
            ItemStack dropStack = RpgLevelZoneManager.rollRandomRpgEquipment(mobLevel);
            RpgAffixManager.rollAffixes(dropStack, mobLevel);
            entity.spawnAtLocation(dropStack);
        }
    }

    public static ItemStack rollRandomRpgEquipment(int mobLevel) {
        boolean isArmor = RNG.nextInt(100) < 65;
        if (isArmor) {
            int slot = RNG.nextInt(4);
            // Tier 1: Nivel 1 - 34 (Cuero, Malla, Hierro)
            if (mobLevel < 35) {
                int tier = RNG.nextInt(3);
                if (tier == 0) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)Items.LEATHER_HELMET);
                        case 1 -> new ItemStack((ItemLike)Items.LEATHER_CHESTPLATE);
                        case 2 -> new ItemStack((ItemLike)Items.LEATHER_LEGGINGS);
                        default -> new ItemStack((ItemLike)Items.LEATHER_BOOTS);
                    };
                }
                if (tier == 1) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)Items.CHAINMAIL_HELMET);
                        case 1 -> new ItemStack((ItemLike)Items.CHAINMAIL_CHESTPLATE);
                        case 2 -> new ItemStack((ItemLike)Items.CHAINMAIL_LEGGINGS);
                        default -> new ItemStack((ItemLike)Items.CHAINMAIL_BOOTS);
                    };
                }
                return switch (slot) {
                    case 0 -> new ItemStack((ItemLike)Items.IRON_HELMET);
                    case 1 -> new ItemStack((ItemLike)Items.IRON_CHESTPLATE);
                    case 2 -> new ItemStack((ItemLike)Items.IRON_LEGGINGS);
                    default -> new ItemStack((ItemLike)Items.IRON_BOOTS);
                };
            }
            // Tier 2: Nivel 35 - 79 (Hierro, Oro, Diamante)
            if (mobLevel < 80) {
                int tier = RNG.nextInt(3);
                if (tier == 0) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)Items.IRON_HELMET);
                        case 1 -> new ItemStack((ItemLike)Items.IRON_CHESTPLATE);
                        case 2 -> new ItemStack((ItemLike)Items.IRON_LEGGINGS);
                        default -> new ItemStack((ItemLike)Items.IRON_BOOTS);
                    };
                }
                if (tier == 1) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)Items.GOLDEN_HELMET);
                        case 1 -> new ItemStack((ItemLike)Items.GOLDEN_CHESTPLATE);
                        case 2 -> new ItemStack((ItemLike)Items.GOLDEN_LEGGINGS);
                        default -> new ItemStack((ItemLike)Items.GOLDEN_BOOTS);
                    };
                }
                return switch (slot) {
                    case 0 -> new ItemStack((ItemLike)Items.DIAMOND_HELMET);
                    case 1 -> new ItemStack((ItemLike)Items.DIAMOND_CHESTPLATE);
                    case 2 -> new ItemStack((ItemLike)Items.DIAMOND_LEGGINGS);
                    default -> new ItemStack((ItemLike)Items.DIAMOND_BOOTS);
                };
            }
            // Tier 3: Nivel 80 - 179 (Diamante y Netherite vanilla puro)
            if (mobLevel < 180) {
                int tier = RNG.nextInt(3);
                if (tier < 2) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)Items.DIAMOND_HELMET);
                        case 1 -> new ItemStack((ItemLike)Items.DIAMOND_CHESTPLATE);
                        case 2 -> new ItemStack((ItemLike)Items.DIAMOND_LEGGINGS);
                        default -> new ItemStack((ItemLike)Items.DIAMOND_BOOTS);
                    };
                }
                return switch (slot) {
                    case 0 -> new ItemStack((ItemLike)Items.NETHERITE_HELMET);
                    case 1 -> new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE);
                    case 2 -> new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS);
                    default -> new ItemStack((ItemLike)Items.NETHERITE_BOOTS);
                };
            }
            // Tier 4: Nivel 180 - 239 (Netherite y Mithril)
            if (mobLevel < 240) {
                int tier = RNG.nextInt(2);
                if (tier == 0 || !SystemItems.MITHRIL_HELMET.isBound()) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)Items.NETHERITE_HELMET);
                        case 1 -> new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE);
                        case 2 -> new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS);
                        default -> new ItemStack((ItemLike)Items.NETHERITE_BOOTS);
                    };
                }
                return switch (slot) {
                    case 0 -> new ItemStack((ItemLike)SystemItems.MITHRIL_HELMET.get());
                    case 1 -> new ItemStack((ItemLike)SystemItems.MITHRIL_CHESTPLATE.get());
                    case 2 -> new ItemStack((ItemLike)SystemItems.MITHRIL_LEGGINGS.get());
                    default -> new ItemStack((ItemLike)SystemItems.MITHRIL_BOOTS.get());
                };
            }
            // Tier 5: Nivel 240 - 299 (Mithril y Adamantita)
            if (mobLevel < 300) {
                int tier = RNG.nextInt(2);
                if (tier == 0 && SystemItems.MITHRIL_HELMET.isBound()) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.MITHRIL_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.MITHRIL_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.MITHRIL_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.MITHRIL_BOOTS.get());
                    };
                }
                if (SystemItems.ADAMANTITE_HELMET.isBound()) {
                    return switch (slot) {
                        case 0 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_HELMET.get());
                        case 1 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_CHESTPLATE.get());
                        case 2 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_LEGGINGS.get());
                        default -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_BOOTS.get());
                    };
                }
                return switch (slot) {
                    case 0 -> new ItemStack((ItemLike)Items.NETHERITE_HELMET);
                    case 1 -> new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE);
                    case 2 -> new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS);
                    default -> new ItemStack((ItemLike)Items.NETHERITE_BOOTS);
                };
            }
            // Tier 6: Nivel 300+ (Adamantita y Jade Espiritual)
            int tier = RNG.nextInt(2);
            if (tier == 0 && SystemItems.ADAMANTITE_HELMET.isBound()) {
                return switch (slot) {
                    case 0 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_HELMET.get());
                    case 1 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_CHESTPLATE.get());
                    case 2 -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_LEGGINGS.get());
                    default -> new ItemStack((ItemLike)SystemItems.ADAMANTITE_BOOTS.get());
                };
            }
            if (SystemItems.SPIRIT_JADE_HELMET.isBound()) {
                return switch (slot) {
                    case 0 -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_HELMET.get());
                    case 1 -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_CHESTPLATE.get());
                    case 2 -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_LEGGINGS.get());
                    default -> new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_BOOTS.get());
                };
            }
            return switch (slot) {
                case 0 -> new ItemStack((ItemLike)Items.NETHERITE_HELMET);
                case 1 -> new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE);
                case 2 -> new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS);
                default -> new ItemStack((ItemLike)Items.NETHERITE_BOOTS);
            };
        }

        // Armas (Espadas y Hachas)
        boolean isAxe = RNG.nextBoolean();
        if (mobLevel < 35) {
            return isAxe ? new ItemStack((ItemLike)Items.IRON_AXE) : new ItemStack((ItemLike)Items.IRON_SWORD);
        }
        if (mobLevel < 80) {
            return isAxe
                    ? (RNG.nextBoolean() ? new ItemStack((ItemLike)Items.IRON_AXE) : new ItemStack((ItemLike)Items.DIAMOND_AXE))
                    : (RNG.nextBoolean() ? new ItemStack((ItemLike)Items.IRON_SWORD) : new ItemStack((ItemLike)Items.DIAMOND_SWORD));
        }
        if (mobLevel < 180) {
            return isAxe
                    ? (RNG.nextBoolean() ? new ItemStack((ItemLike)Items.DIAMOND_AXE) : new ItemStack((ItemLike)Items.NETHERITE_AXE))
                    : (RNG.nextBoolean() ? new ItemStack((ItemLike)Items.DIAMOND_SWORD) : new ItemStack((ItemLike)Items.NETHERITE_SWORD));
        }
        if (mobLevel < 240) {
            if (isAxe) {
                return SystemItems.MITHRIL_AXE.isBound() && RNG.nextBoolean()
                        ? new ItemStack((ItemLike)SystemItems.MITHRIL_AXE.get())
                        : new ItemStack((ItemLike)Items.NETHERITE_AXE);
            } else {
                return SystemItems.MITHRIL_SWORD.isBound() && RNG.nextBoolean()
                        ? new ItemStack((ItemLike)SystemItems.MITHRIL_SWORD.get())
                        : new ItemStack((ItemLike)Items.NETHERITE_SWORD);
            }
        }
        if (mobLevel < 300) {
            if (isAxe) {
                return SystemItems.ADAMANTITE_AXE.isBound() && RNG.nextBoolean()
                        ? new ItemStack((ItemLike)SystemItems.ADAMANTITE_AXE.get())
                        : (SystemItems.MITHRIL_AXE.isBound() ? new ItemStack((ItemLike)SystemItems.MITHRIL_AXE.get()) : new ItemStack((ItemLike)Items.NETHERITE_AXE));
            } else {
                return SystemItems.ADAMANTITE_SWORD.isBound() && RNG.nextBoolean()
                        ? new ItemStack((ItemLike)SystemItems.ADAMANTITE_SWORD.get())
                        : (SystemItems.MITHRIL_SWORD.isBound() ? new ItemStack((ItemLike)SystemItems.MITHRIL_SWORD.get()) : new ItemStack((ItemLike)Items.NETHERITE_SWORD));
            }
        }
        // Nivel 300+: Jade Espiritual o Adamantita
        if (isAxe) {
            return SystemItems.SPIRIT_JADE_AXE.isBound() && RNG.nextBoolean()
                    ? new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_AXE.get())
                    : (SystemItems.ADAMANTITE_AXE.isBound() ? new ItemStack((ItemLike)SystemItems.ADAMANTITE_AXE.get()) : new ItemStack((ItemLike)Items.NETHERITE_AXE));
        } else {
            return SystemItems.SPIRIT_JADE_SWORD.isBound() && RNG.nextBoolean()
                    ? new ItemStack((ItemLike)SystemItems.SPIRIT_JADE_SWORD.get())
                    : (SystemItems.ADAMANTITE_SWORD.isBound() ? new ItemStack((ItemLike)SystemItems.ADAMANTITE_SWORD.get()) : new ItemStack((ItemLike)Items.NETHERITE_SWORD));
        }
    }
}
