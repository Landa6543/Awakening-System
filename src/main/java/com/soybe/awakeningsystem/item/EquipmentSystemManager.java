package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.network.ActivateRunePayload;
import com.soybe.awakeningsystem.network.EquipLightConePayload;
import com.soybe.awakeningsystem.network.EquipRunePayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Gestor del Sistema de Equipamiento de Conos de Luz (Honkai: Star Rail)
 * y Engarce Rúnico (Solo Leveling / Runas Activas y Pasivas).
 */
public final class EquipmentSystemManager {
    private EquipmentSystemManager() {}

    public static boolean isLightCone(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var item = stack.getItem();
        if (item instanceof LightConeNightOnTheMilkyWayItem
                || item instanceof LightConeAlongThePassingShoreItem
                || item instanceof LightConePatienceIsAllYouNeedItem
                || item instanceof LightConeWhereDreamsShouldRestItem
                || item instanceof LightConeBeforeDawnItem) {
            return true;
        }
        String name = item.toString().toLowerCase(java.util.Locale.ROOT);
        return name.contains("light_cone");
    }

    public static boolean is5StarLightCone(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var item = stack.getItem();
        return item instanceof LightConeNightOnTheMilkyWayItem
                || item instanceof LightConeAlongThePassingShoreItem
                || item instanceof LightConePatienceIsAllYouNeedItem
                || item instanceof LightConeWhereDreamsShouldRestItem
                || item instanceof LightConeBeforeDawnItem;
    }

    /**
     * Procesa equipar o desequipar un Cono de Luz en la ranura de SystemData.
     */
    public static void handleEquipLightCone(ServerPlayer player, EquipLightConePayload payload) {
        SystemData data = SystemEvents.data(player);
        int invSlot = payload.invSlot();

        if (invSlot >= 0 && invSlot < player.getInventory().items.size()) {
            ItemStack held = player.getInventory().getItem(invSlot);
            if (!held.isEmpty() && isLightCone(held)) {
                ItemStack currentlyEquipped = data.getEquippedLightCone();
                data.setEquippedLightCone(held.copy());
                player.getInventory().setItem(invSlot, currentlyEquipped);
                boolean is5Star = is5StarLightCone(held);

                if (is5Star) {
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
                    player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
                    player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.4F);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
                    if (player.serverLevel() != null) {
                        player.serverLevel().sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.2, player.getZ(), 35, 0.5, 0.6, 0.5, 0.12);
                        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 45, 0.6, 0.6, 0.6, 0.2);
                        player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.3, player.getZ(), 25, 0.4, 0.5, 0.4, 0.15);
                    }
                    player.sendSystemMessage(Component.translatable("awakening_system.light_cone.msg.legendary_resonance_chat", held.getHoverName()));
                    player.displayClientMessage(Component.translatable("awakening_system.light_cone.msg.legendary_resonance"), true);
                } else {
                    player.playNotifySound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1.2F, 1.1F);
                    player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.8F);
                    if (player.serverLevel() != null) {
                        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.4, 0.6, 0.4, 0.05);
                        player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.2, player.getZ(), 28, 0.5, 0.5, 0.5, 0.1);
                    }
                    player.displayClientMessage(Component.translatable("awakening_system.light_cone.msg.equipped", held.getHoverName()), true);
                }
                AwakeningSystemMod.sync(player);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.light_cone.msg.invalid"), true);
            }
        } else if (invSlot == -1) {
            ItemStack currentlyEquipped = data.getEquippedLightCone();
            if (!currentlyEquipped.isEmpty()) {
                data.setEquippedLightCone(ItemStack.EMPTY);
                if (!player.getInventory().add(currentlyEquipped)) {
                    player.drop(currentlyEquipped, false);
                }
                player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                AwakeningSystemMod.sync(player);
                player.displayClientMessage(Component.translatable("awakening_system.light_cone.msg.unequipped"), true);
            }
        }
    }

    /**
     * Procesa engarzar o desengarzar una Runa Activa o Pasiva en SystemData.
     */
    public static void handleEquipRune(ServerPlayer player, EquipRunePayload payload) {
        SystemData data = SystemEvents.data(player);
        int slot = payload.slot();
        if (slot < 0 || slot >= 2) return;

        if (payload.invSlot() >= 0 && payload.invSlot() < player.getInventory().items.size()) {
            ItemStack held = player.getInventory().getItem(payload.invSlot());
            if (!held.isEmpty() && held.getItem() instanceof RuneStoneItem) {
                ItemStack oldRune = payload.isActiveRune() ? data.getActiveRune(slot) : data.getPassiveRune(slot);
                if (payload.isActiveRune()) {
                    data.setActiveRune(slot, held.copy());
                } else {
                    data.setPassiveRune(slot, held.copy());
                }
                player.getInventory().setItem(payload.invSlot(), oldRune);
                player.playNotifySound(SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 1.2F, 1.2F);
                player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9F, 1.3F);
                AwakeningSystemMod.sync(player);
                Component slotType = payload.isActiveRune()
                        ? Component.translatable("awakening_system.rune.msg.slot_active", (slot + 1))
                        : Component.translatable("awakening_system.rune.msg.slot_passive", (slot + 1));
                player.displayClientMessage(Component.translatable("awakening_system.rune.msg.socketed", held.getHoverName(), slotType), true);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.rune.msg.invalid"), true);
            }
        } else if (payload.invSlot() == -1) {
            ItemStack oldRune = payload.isActiveRune() ? data.getActiveRune(slot) : data.getPassiveRune(slot);
            if (!oldRune.isEmpty()) {
                if (payload.isActiveRune()) {
                    data.setActiveRune(slot, ItemStack.EMPTY);
                } else {
                    data.setPassiveRune(slot, ItemStack.EMPTY);
                }
                if (!player.getInventory().add(oldRune)) {
                    player.drop(oldRune, false);
                }
                player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                AwakeningSystemMod.sync(player);
                Component slotType = payload.isActiveRune()
                        ? Component.translatable("awakening_system.rune.msg.slot_active", (slot + 1))
                        : Component.translatable("awakening_system.rune.msg.slot_passive", (slot + 1));
                Component runeName = oldRune.getHoverName();
                player.displayClientMessage(Component.translatable("awakening_system.rune.msg.removed", runeName, slotType), true);
            }
        }
    }

    /**
     * Activa la Runa Activa equipada en la ranura correspondiente (0 o 1).
     */
    public static void handleActivateRune(ServerPlayer player, ActivateRunePayload payload) {
        SystemData data = SystemEvents.data(player);
        int slot = payload.slot();
        if (slot < 0 || slot >= 2) return;

        ItemStack runeStack = data.getActiveRune(slot);
        if (runeStack.isEmpty() || !(runeStack.getItem() instanceof RuneStoneItem runeItem)) {
            player.displayClientMessage(Component.translatable("awakening_system.rune.msg.empty_slot", (slot + 1)), true);
            return;
        }

        long now = player.level().getGameTime();
        String cdKey = "active_rune_cd_" + slot;
        long cdUntil = player.getPersistentData().getLong(cdKey);
        if (now < cdUntil) {
            long remainingSec = Math.max(1, (cdUntil - now + 19) / 20);
            player.displayClientMessage(Component.translatable("awakening_system.rune.msg.cooldown", (slot + 1), remainingSec), true);
            return;
        }

        RuneStoneItem.RuneType runeType = runeItem.getRuneType();
        ServerLevel sl = player.serverLevel();
        long cdTicks = 120L;

        switch (runeType) {
            case RULERS_AUTHORITY -> {
                cdTicks = 120L;
                boolean push = player.isShiftKeyDown();
                AABB box = player.getBoundingBox().inflate(12.0D, 5.0D, 12.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != player && e.isAlive() && !e.getTags().contains("shadow_soldier"));

                for (LivingEntity nearby : enemies) {
                    if (push) {
                        Vec3 force = nearby.position().subtract(player.position()).normalize().scale(1.9D);
                        nearby.setDeltaMovement(force.x, 0.5D, force.z);
                        nearby.hurt(nearby.damageSources().playerAttack(player), 15.0F);
                        sl.sendParticles(ParticleTypes.SONIC_BOOM, nearby.getX(), nearby.getY() + 0.5D, nearby.getZ(), 1, 0, 0, 0, 0);
                    } else {
                        Vec3 force = player.position().subtract(nearby.position()).normalize().scale(1.5D);
                        nearby.setDeltaMovement(force.x, 0.4D, force.z);
                        nearby.hurt(nearby.damageSources().playerAttack(player), 10.0F);
                        sl.sendParticles(ParticleTypes.PORTAL, nearby.getX(), nearby.getY() + 0.5D, nearby.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.1D);
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.9F, 1.7F);
                player.displayClientMessage(Component.translatable(push ? "awakening_system.rune.msg.rulers_authority_push" : "awakening_system.rune.msg.rulers_authority_pull"), true);
            }
            case SHADOW_STEP -> {
                cdTicks = 160L;
                AABB searchBox = player.getBoundingBox().inflate(16.0D, 6.0D, 16.0D);
                List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, searchBox,
                        e -> e != player && e.isAlive() && (e instanceof Enemy || !e.getType().getCategory().isFriendly()));

                LivingEntity target = null;
                double closestDist = Double.MAX_VALUE;
                for (LivingEntity e : targets) {
                    double d = player.distanceToSqr(e);
                    if (d < closestDist) {
                        closestDist = d;
                        target = e;
                    }
                }

                if (target != null) {
                    Vec3 enemyLook = target.getLookAngle();
                    double behindX = target.getX() - enemyLook.x * 1.5D;
                    double behindZ = target.getZ() - enemyLook.z * 1.5D;
                    double behindY = target.getY();

                    sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.4D, 0.6D, 0.4D, 0.1D);
                    player.teleportTo(behindX, behindY, behindZ);
                    sl.sendParticles(ParticleTypes.SQUID_INK, behindX, behindY + 1.0D, behindZ, 50, 0.4D, 0.6D, 0.4D, 0.1D);
                    sl.sendParticles(ParticleTypes.PORTAL, behindX, behindY + 1.0D, behindZ, 30, 0.4D, 0.6D, 0.4D, 0.15D);

                    target.hurt(target.damageSources().playerAttack(player), 20.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0));
                    sl.playSound(null, behindX, behindY, behindZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 1.3F);
                    player.displayClientMessage(Component.translatable("awakening_system.rune.msg.shadow_step_teleport"), true);
                } else {
                    Vec3 look = player.getLookAngle().scale(1.8D);
                    player.setDeltaMovement(look.x, 0.3D, look.z);
                    player.hurtMarked = true;
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0));
                    sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.3D, 0.5D, 0.3D, 0.05D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.5F);
                    player.displayClientMessage(Component.translatable("awakening_system.rune.msg.shadow_step_dash"), true);
                }
            }
            case DRAGON_EYES -> {
                cdTicks = 300L;
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, true));
                player.heal(6.0F);
                AABB scanBox = player.getBoundingBox().inflate(24.0D, 12.0D, 24.0D);
                for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, scanBox, e -> e != player && e.isAlive())) {
                    le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false, false));
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.6F);
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.2D, player.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.05D);
                player.displayClientMessage(Component.translatable("awakening_system.rune.msg.dragon_eyes"), true);
            }
            case FOURTH_WALL -> {
                cdTicks = 1200L;
                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 3, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, false, true));
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5F, 1.2F);
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
                player.displayClientMessage(Component.translatable("awakening_system.rune.msg.fourth_wall"), true);
            }
            case IRON_BODY -> {
                cdTicks = 400L;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false, false));
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.4F);
                sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.1D);
                player.displayClientMessage(Component.translatable("awakening_system.rune.msg.iron_body"), true);
            }
            case BLOODTHIRST -> {
                cdTicks = 360L;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, false, true));
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8F, 1.5F);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.2D, player.getZ(), 50, 0.5D, 0.5D, 0.5D, 0.1D);
                player.displayClientMessage(Component.translatable("awakening_system.rune.msg.bloodthirst"), true);
            }
        }

        player.getPersistentData().putLong(cdKey, now + cdTicks);
        data.setActiveRuneCooldownUntil(slot, now + cdTicks);
        AwakeningSystemMod.sync(player);
    }
}
