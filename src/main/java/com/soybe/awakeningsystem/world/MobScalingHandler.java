package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemConfig;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class MobScalingHandler {

    private static final String SCALED_TAG = "aw_scaled_tier";

    private static final ResourceLocation HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "distance_scaling_health");
    private static final ResourceLocation DAMAGE_MOD_ID = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "distance_scaling_damage");
    private static final ResourceLocation ARMOR_MOD_ID = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "distance_scaling_armor");
    private static final ResourceLocation SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "distance_scaling_speed");

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob)) return;

        if (!SystemConfig.MOB_SCALING_ENABLED.get()) return;

        if (level.dimension() != Level.OVERWORLD) return;

        if (mob instanceof Npc || mob instanceof Animal) return;
        if (!(mob instanceof Enemy) && mob.getTarget() == null && mob.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
            return;
        }

        if (mob.getTags().contains("dungeon_mob")
                || mob.getTags().contains("rpg_scaled")
                || mob.getTags().contains("tower_mob")
                || mob.getTags().contains("pagoda_trial_mob")
                || mob.getTags().contains("no_distance_scaling")) {
            return;
        }

        if (SystemConfig.MOB_SCALING_EXCLUDE_BOSSES.get()) {
            if (mob instanceof EnderDragon || mob instanceof WitherBoss || mob.getMaxHealth() >= 250.0f) {
                return;
            }
        }

        CompoundTag persistent = mob.getPersistentData();
        if (persistent.contains(SCALED_TAG)) return;

        double dist = Math.sqrt(mob.getX() * mob.getX() + mob.getZ() * mob.getZ());
        int blocksPerTier = Math.max(50, SystemConfig.MOB_SCALING_BLOCKS_PER_TIER.get());
        int tier = (int) (dist / blocksPerTier);
        int maxTier = SystemConfig.MOB_SCALING_MAX_TIER.get();
        tier = Math.min(tier, maxTier);

        persistent.putInt(SCALED_TAG, tier);
        if (tier <= 0) return;

        double healthBonusPerTier = SystemConfig.MOB_SCALING_HEALTH_PER_TIER.get();
        if (healthBonusPerTier > 0) {
            AttributeInstance hpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (hpAttr != null) {
                hpAttr.removeModifier(HEALTH_MOD_ID);
                hpAttr.addPermanentModifier(new AttributeModifier(
                        HEALTH_MOD_ID,
                        tier * healthBonusPerTier,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                mob.setHealth(mob.getMaxHealth());
            }
        }

        double damageBonusPerTier = SystemConfig.MOB_SCALING_DAMAGE_PER_TIER.get();
        if (damageBonusPerTier > 0) {
            AttributeInstance dmgAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmgAttr != null) {
                dmgAttr.removeModifier(DAMAGE_MOD_ID);
                dmgAttr.addPermanentModifier(new AttributeModifier(
                        DAMAGE_MOD_ID,
                        tier * damageBonusPerTier,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }

        double armorBonusPerTier = SystemConfig.MOB_SCALING_ARMOR_PER_TIER.get();
        if (armorBonusPerTier > 0) {
            AttributeInstance armorAttr = mob.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                armorAttr.removeModifier(ARMOR_MOD_ID);
                armorAttr.addPermanentModifier(new AttributeModifier(
                        ARMOR_MOD_ID,
                        tier * armorBonusPerTier,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        AttributeInstance speedAttr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MOD_ID);
            double speedBonus = Math.min(0.25, tier * 0.01);
            if (speedBonus > 0) {
                speedAttr.addPermanentModifier(new AttributeModifier(
                        SPEED_MOD_ID,
                        speedBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }

        if (SystemConfig.MOB_SCALING_SHOW_NAME_TAG.get() && (!mob.hasCustomName() || mob.getCustomName().getString().contains("[Nv."))) {
            ChatFormatting color;
            String rankLetter;
            if (tier <= 5) {
                color = ChatFormatting.GREEN;
                rankLetter = "E";
            } else if (tier <= 12) {
                color = ChatFormatting.AQUA;
                rankLetter = "D";
            } else if (tier <= 22) {
                color = ChatFormatting.GOLD;
                rankLetter = "C";
            } else if (tier <= 35) {
                color = ChatFormatting.RED;
                rankLetter = "B";
            } else if (tier <= 45) {
                color = ChatFormatting.DARK_RED;
                rankLetter = "A";
            } else {
                color = ChatFormatting.LIGHT_PURPLE;
                rankLetter = "S";
            }

            Component nameTag = Component.literal(color + "[" + rankLetter + " - Nv. " + tier + "] ")
                    .append(Component.translatable(mob.getType().getDescriptionId()).withStyle(ChatFormatting.WHITE));
            mob.setCustomName(nameTag);
            mob.setCustomNameVisible(false);
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide()) return;

        CompoundTag persistent = entity.getPersistentData();
        if (persistent.contains(SCALED_TAG)) {
            int tier = persistent.getInt(SCALED_TAG);
            if (tier > 0) {
                double expMultiplier = Math.min(3.0, 1.0 + (tier * SystemConfig.MOB_SCALING_EXP_BONUS_PER_TIER.get()));
                int original = event.getDroppedExperience();
                event.setDroppedExperience((int) Math.round(original * expMultiplier));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide()) return;

        CompoundTag persistent = entity.getPersistentData();
        if (persistent.contains(SCALED_TAG)) {
            int tier = persistent.getInt(SCALED_TAG);
            if (tier >= 10) {
                float coinChance = Math.min(0.35f, 0.08f + (tier * 0.01f));
                if (entity.getRandom().nextFloat() < coinChance && SystemItems.AWAKENING_COIN != null && SystemItems.AWAKENING_COIN.isBound()) {
                    ItemStack coin = new ItemStack(SystemItems.AWAKENING_COIN.get(), 1 + entity.getRandom().nextInt(Math.max(1, tier / 15)));
                    ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), coin);
                    event.getDrops().add(itemEntity);
                }
            }
        }
    }
}
