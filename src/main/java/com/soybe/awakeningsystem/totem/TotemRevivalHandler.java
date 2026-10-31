package com.soybe.awakeningsystem.totem;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.item.AwakeningTotemItem;
import com.soybe.awakeningsystem.network.TotemActivationPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Manejador central del Árbol de Tótems Trascendentes por Mundo y División.
 * Intercepta daños letales y activa resurrecciones avanzadas con efectos cinemáticos de manhwa/RPG.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class TotemRevivalHandler {

    private TotemRevivalHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim == null || victim.level().isClientSide()) return;

        boolean isVoid = event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD);
        boolean isKillCommand = event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !isVoid;

        // Si es comando /kill administrativo, respetar la muerte salvo que no sea bypass estricto
        if (isKillCommand) return;

        ItemStack totemStack = findAndConsumeTotem(victim, isVoid);
        if (totemStack.isEmpty() || !(totemStack.getItem() instanceof AwakeningTotemItem totemItem)) {
            return;
        }

        // Cancelar muerte letal
        event.setCanceled(true);

        ServerLevel level = (ServerLevel) victim.level();

        // Limpiar efectos nocivos comunes
        victim.removeEffect(MobEffects.POISON);
        victim.removeEffect(MobEffects.WITHER);
        victim.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        victim.removeEffect(MobEffects.BLINDNESS);
        victim.removeEffect(MobEffects.CONFUSION);
        victim.removeEffect(MobEffects.WEAKNESS);
        victim.removeEffect(MobEffects.DIG_SLOWDOWN);
        victim.removeEffect(MobEffects.DARKNESS);

        // Notificar y animar
        level.broadcastEntityEvent(victim, (byte) 35);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (victim instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new TotemActivationPayload(totemStack.copy()));
        }

        AwakeningTotemItem.TotemType type = totemItem.getTotemType();
        switch (type) {
            case OVERWORLD -> applyOverworldTotem(victim, level);
            case GLACIAL_FROST -> applyGlacialFrostTotem(victim, level);
            case MURIM_QI -> applyMurimQiTotem(victim, level);
            case TEYVAT_ARCHON -> applyTeyvatArchonTotem(victim, level);
            case ABYSSAL_RESONANT -> applyAbyssalResonantTotem(victim, level);
            case SOULFORGE -> applySoulforgeTotem(victim, level);
            case ASTRALITE -> applyAstraliteTotem(victim, level);
            case MITHRIL -> applyMithrilTotem(victim, level);
            case ADAMANTITE -> applyAdamantiteTotem(victim, level);
            case SPIRIT_JADE -> applySpiritJadeTotem(victim, level);
            case ASTRAL -> applyAstralTotem(victim, level);
            case CELESTIAL -> applyCelestialTotem(victim, level);
            case PRIMORDIAL -> applyPrimordialTotem(victim, level);
            case AWAKENING_MONARCH -> applyAwakeningMonarchTotem(victim, level);
        }
    }

    private static ItemStack findAndConsumeTotem(LivingEntity victim, boolean isVoid) {
        // 1. Mano principal
        ItemStack main = victim.getMainHandItem();
        if (isValidTotem(main, isVoid)) {
            ItemStack copy = main.copy();
            copy.setCount(1);
            main.shrink(1);
            return copy;
        }

        // 2. Mano secundaria
        ItemStack off = victim.getOffhandItem();
        if (isValidTotem(off, isVoid)) {
            ItemStack copy = off.copy();
            copy.setCount(1);
            off.shrink(1);
            return copy;
        }

        // 3. Curios / Accesorios si está instalado
        ItemStack curioTotem = findAndConsumeCurios(victim, isVoid);
        if (!curioTotem.isEmpty()) {
            return curioTotem;
        }

        // 4. Inventario del jugador (hotbar y cuerpo)
        if (victim instanceof Player player) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack stack = player.getInventory().items.get(i);
                if (isValidTotem(stack, isVoid)) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    stack.shrink(1);
                    return copy;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isValidTotem(ItemStack stack, boolean isVoid) {
        if (stack.isEmpty() || !(stack.getItem() instanceof AwakeningTotemItem totem)) return false;
        if (isVoid) {
            // El vacío cósmico solo puede ser desafiado por el Desfase Astral y la Vida Primordial
            return totem.getTotemType() == AwakeningTotemItem.TotemType.ASTRALITE
                    || totem.getTotemType() == AwakeningTotemItem.TotemType.PRIMORDIAL;
        }
        return true;
    }

    private static ItemStack findAndConsumeCurios(LivingEntity entity, boolean isVoid) {
        if (!ModList.get().isLoaded("curios")) return ItemStack.EMPTY;
        try {
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getInvMethod = apiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object optInv = getInvMethod.invoke(null, entity);
            if (optInv instanceof Optional<?> opt && opt.isPresent()) {
                Object handler = opt.get();
                Method getEquippedMethod = handler.getClass().getMethod("getEquippedCurios");
                Object itemHandler = getEquippedMethod.invoke(handler);
                if (itemHandler != null) {
                    Method getSlotsMethod = itemHandler.getClass().getMethod("getSlots");
                    Method getStackMethod = itemHandler.getClass().getMethod("getStackInSlot", int.class);
                    Method extractItemMethod = itemHandler.getClass().getMethod("extractItem", int.class, int.class, boolean.class);

                    int slots = (int) getSlotsMethod.invoke(itemHandler);
                    for (int i = 0; i < slots; i++) {
                        ItemStack stack = (ItemStack) getStackMethod.invoke(itemHandler, i);
                        if (isValidTotem(stack, isVoid)) {
                            ItemStack copy = stack.copy();
                            copy.setCount(1);
                            extractItemMethod.invoke(itemHandler, i, 1, false);
                            return copy;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    // --- EFECTOS TEMÁTICOS ---

    private static void applyOverworldTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(6.0F, victim.getMaxHealth()));
        victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, victim.getX(), victim.getY() + 1.0, victim.getZ(), 40, 0.5, 0.5, 0.5, 0.1);
    }

    private static void applyGlacialFrostTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(8.0F, victim.getMaxHealth()));
        victim.setTicksFrozen(0);
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2)); // 6 corazones de escarcha
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.2F, 0.8F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.5F);
        level.sendParticles(ParticleTypes.SNOWFLAKE, victim.getX(), victim.getY() + 0.8, victim.getZ(), 80, 2.5, 1.0, 2.5, 0.15);
        level.sendParticles(ParticleTypes.ITEM_SNOWBALL, victim.getX(), victim.getY() + 0.8, victim.getZ(), 40, 2.0, 0.8, 2.0, 0.1);

        AABB box = victim.getBoundingBox().inflate(10.0, 5.0, 10.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.setTicksFrozen(300);
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
            mob.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 2));
            mob.hurt(victim.damageSources().freeze(), 8.0F);
        }
    }

    private static void applyMurimQiTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(16.0F, victim.getMaxHealth()));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2)); // Piel de Hierro (Resistencia III)
        victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));

        if (victim instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.setCultivationQi(data.maxCultivationQi());
            data.restoreMana(100);
            AwakeningSystemMod.sync(sp);
        }

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.5F, 0.7F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.SONIC_BOOM, victim.getX(), victim.getY() + 0.5, victim.getZ(), 2, 0.1, 0.1, 0.1, 0.0);
        level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0, victim.getZ(), 60, 2.0, 1.0, 2.0, 0.3);
        level.sendParticles(ParticleTypes.WAX_OFF, victim.getX(), victim.getY() + 1.0, victim.getZ(), 40, 2.0, 1.0, 2.0, 0.2);

        // Desviar proyectiles cercanos
        level.getEntitiesOfClass(Projectile.class, victim.getBoundingBox().inflate(12.0)).forEach(Entity::discard);

        // Estallido de palma que empuja con fuerza violenta
        AABB box = victim.getBoundingBox().inflate(12.0, 4.0, 12.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            Vec3 diff = mob.position().subtract(victim.position());
            Vec3 push = diff.lengthSqr() > 0.01 ? diff.normalize().scale(1.8).add(0, 0.4, 0) : new Vec3(0, 1.0, 0);
            mob.setDeltaMovement(push);
            mob.hurtMarked = true;
            mob.hurt(victim.damageSources().magic(), 12.0F);
        }
    }

    private static void applyTeyvatArchonTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(12.0F, victim.getMaxHealth()));
        // Escudo de Jade Absoluto (Resistencia V = inmunidad total 3.5s)
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 70, 4));
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));

        // Corriente de viento ascendente para escapar de peligros
        victim.setDeltaMovement(victim.getDeltaMovement().x, 0.85, victim.getDeltaMovement().z);
        victim.hurtMarked = true;

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.4F, 1.0F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.2F);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, victim.getX(), victim.getY() + 0.5, victim.getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, victim.getX(), victim.getY() + 1.0, victim.getZ(), 40, 2.0, 1.0, 2.0, 0.1);

        AABB box = victim.getBoundingBox().inflate(10.0, 4.0, 10.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            Vec3 diff = mob.position().subtract(victim.position());
            Vec3 push = diff.lengthSqr() > 0.01 ? diff.normalize().scale(1.2).add(0, 0.3, 0) : new Vec3(0, 0.6, 0);
            mob.setDeltaMovement(push);
            mob.hurtMarked = true;
        }
    }

    private static void applyAbyssalResonantTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(victim.getMaxHealth()); // 100% de la vida máxima
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 3)); // Resistencia IV
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 5)); // 12 corazones dorados
        victim.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 900, 0));

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 0.9F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.3F, 1.1F);
        level.sendParticles(ParticleTypes.SONIC_BOOM, victim.getX(), victim.getY() + 1.0, victim.getZ(), 4, 0.5, 0.5, 0.5, 0.0);
        level.sendParticles(ParticleTypes.SCULK_SOUL, victim.getX(), victim.getY() + 1.0, victim.getZ(), 60, 2.5, 1.5, 2.5, 0.1);

        AABB box = victim.getBoundingBox().inflate(14.0, 5.0, 14.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.hurt(victim.damageSources().sonicBoom(victim), 20.0F);
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
            mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
        }

        // Visión del vacío: revelar enemigos cercanos a través de muros
        AABB senseBox = victim.getBoundingBox().inflate(25.0, 10.0, 25.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, senseBox, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false, true));
        }
    }

    private static void applySoulforgeTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(20.0F, victim.getMaxHealth()));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4)); // Inmunidad 5 segundos
        victim.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0));
        victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 3));

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.6F, 0.6F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.2F, 1.1F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, victim.getX(), victim.getY() + 1.0, victim.getZ(), 80, 2.5, 1.2, 2.5, 0.15);
        level.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY() + 1.0, victim.getZ(), 50, 2.0, 1.0, 2.0, 0.1);

        int drainedCount = 0;
        AABB box = victim.getBoundingBox().inflate(10.0, 4.0, 10.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.igniteForSeconds(6);
            mob.hurt(victim.damageSources().magic(), 15.0F);
            drainedCount++;
        }

        int bonusAbs = Math.min(5, 1 + drainedCount);
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, bonusAbs));
    }

    private static void applyAstraliteTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(20.0F, victim.getMaxHealth()));

        double targetX = victim.getX();
        double targetY = victim.getY();
        double targetZ = victim.getZ();

        if (targetY < -40.0) {
            // Rescate de caída al vacío
            targetY = Math.max(75.0, level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) targetX, (int) targetZ) + 1.0);
        } else {
            // Desfase hacia atrás o en dirección segura
            Vec3 look = victim.getLookAngle();
            targetX -= look.x * 12.0;
            targetZ -= look.z * 12.0;
            targetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) targetX, (int) targetZ) + 1.0;
        }

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5F, 0.8F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.4F);
        level.sendParticles(ParticleTypes.PORTAL, victim.getX(), victim.getY() + 1.0, victim.getZ(), 70, 1.5, 1.5, 1.5, 0.8);
        level.sendParticles(ParticleTypes.FLASH, victim.getX(), victim.getY() + 1.0, victim.getZ(), 2, 0, 0, 0, 0);

        // Golpe de cometas defensivos en la posición de peligro
        AABB box = victim.getBoundingBox().inflate(8.0, 4.0, 8.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.hurt(victim.damageSources().magic(), 18.0F);
        }

        victim.teleportTo(targetX, targetY, targetZ);

        // Estado incorpóreo defensivo
        victim.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 4));
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
    }

    private static void applyMithrilTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(24.0F, victim.getMaxHealth()));
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 800, 5)); // 12 corazones
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2)); // Resistencia III
        victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 3));

        if (victim instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.restoreMana(data.maxMana() / 2);
            AwakeningSystemMod.sync(sp);
        }

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.4F, 1.1F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 1.5F);
        level.sendParticles(ParticleTypes.ENCHANT, victim.getX(), victim.getY() + 1.0, victim.getZ(), 80, 2.0, 1.0, 2.0, 0.4);
        level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY() + 1.0, victim.getZ(), 30, 1.5, 1.0, 1.5, 0.1);

        level.getEntitiesOfClass(Projectile.class, victim.getBoundingBox().inflate(10.0)).forEach(Entity::discard);
    }

    private static void applyAdamantiteTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(28.0F, victim.getMaxHealth()));
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 7)); // 16 corazones
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 3)); // Resistencia IV
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 3)); // Fuerza IV

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5F, 0.6F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 0.8F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, victim.getX(), victim.getY() + 0.5, victim.getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0, victim.getZ(), 70, 2.5, 1.0, 2.5, 0.4);

        AABB box = victim.getBoundingBox().inflate(10.0, 4.0, 10.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.hurt(victim.damageSources().generic(), 22.0F);
            Vec3 diff = mob.position().subtract(victim.position());
            Vec3 push = diff.lengthSqr() > 0.01 ? diff.normalize().scale(2.2).add(0, 0.5, 0) : new Vec3(0, 1.0, 0);
            mob.setDeltaMovement(push);
            mob.hurtMarked = true;
        }
    }

    private static void applySpiritJadeTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(32.0F, victim.getMaxHealth()));
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 7));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 3));
        victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 4));

        if (victim instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.setCultivationQi(data.maxCultivationQi());
            data.restoreMana(data.maxMana());
            AwakeningSystemMod.sync(sp);
        }

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.6F, 0.8F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, victim.getX(), victim.getY() + 1.0, victim.getZ(), 60, 2.5, 1.5, 2.5, 0.1);
        level.sendParticles(ParticleTypes.COMPOSTER, victim.getX(), victim.getY() + 1.0, victim.getZ(), 40, 2.0, 1.0, 2.0, 0.1);

        AABB box = victim.getBoundingBox().inflate(15.0, 5.0, 15.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            Vec3 diff = mob.position().subtract(victim.position());
            Vec3 push = diff.lengthSqr() > 0.01 ? diff.normalize().scale(1.5).add(0, 0.4, 0) : new Vec3(0, 0.8, 0);
            mob.setDeltaMovement(push);
            mob.hurtMarked = true;
        }
    }

    private static void applyAstralTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(Math.min(36.0F, victim.getMaxHealth()));
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 9)); // 20 corazones
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4)); // Inmunidad cósmica 5s
        victim.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0));

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4F, 1.2F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2F, 1.1F);
        level.sendParticles(ParticleTypes.FIREWORK, victim.getX(), victim.getY() + 1.0, victim.getZ(), 60, 2.5, 1.5, 2.5, 0.2);
        level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY() + 1.0, victim.getZ(), 50, 2.0, 1.2, 2.0, 0.1);

        AABB box = victim.getBoundingBox().inflate(16.0, 6.0, 16.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.hurt(victim.damageSources().magic(), 25.0F);
        }
    }

    private static void applyCelestialTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(victim.getMaxHealth()); // 100% de la vida máxima
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 11)); // 24 corazones
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 3)); // Resistencia IV
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 900, 2)); // Velocidad III

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.3F, 1.3F);
        level.sendParticles(ParticleTypes.FLASH, victim.getX(), victim.getY() + 1.0, victim.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
        level.sendParticles(ParticleTypes.WAX_ON, victim.getX(), victim.getY() + 1.0, victim.getZ(), 80, 3.0, 1.5, 3.0, 0.2);

        // Juicio sagrado: calcinar demonios y monstruos cercanos
        AABB box = victim.getBoundingBox().inflate(20.0, 8.0, 20.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.hurt(victim.damageSources().magic(), 35.0F);
            mob.igniteForSeconds(10);
        }
    }

    private static void applyPrimordialTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(victim.getMaxHealth()); // 100% vida
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1800, 14)); // 30 corazones adicionales
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 4)); // Inmunidad absoluta por 8s
        victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 4));

        if (victim.getY() < -40.0) {
            double safeY = Math.max(75.0, level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) victim.getX(), (int) victim.getZ()) + 1.0);
            victim.teleportTo(victim.getX(), safeY, victim.getZ());
        }

        if (victim instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.restoreMana(data.maxMana());
            data.setCultivationQi(data.maxCultivationQi());
            sp.getFoodData().setFoodLevel(20);
            sp.getFoodData().setSaturation(20.0F);
            AwakeningSystemMod.sync(sp);
        }

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 1.8F, 0.9F);
        level.sendParticles(ParticleTypes.SPLASH, victim.getX(), victim.getY() + 1.0, victim.getZ(), 100, 3.0, 2.0, 3.0, 0.3);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, victim.getX(), victim.getY() + 1.0, victim.getZ(), 50, 2.5, 1.5, 2.5, 0.1);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, victim.getX(), victim.getY() + 1.0, victim.getZ(), 100, 3.0, 2.0, 3.0, 0.2);

        AABB box = victim.getBoundingBox().inflate(25.0, 8.0, 25.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.hurt(victim.damageSources().magic(), 30.0F);
            Vec3 diff = mob.position().subtract(victim.position());
            Vec3 push = diff.lengthSqr() > 0.01 ? diff.normalize().scale(2.5).add(0, 0.6, 0) : new Vec3(0, 1.2, 0);
            mob.setDeltaMovement(push);
            mob.hurtMarked = true;
        }
    }

    private static void applyAwakeningMonarchTotem(LivingEntity victim, ServerLevel level) {
        victim.setHealth(victim.getMaxHealth());
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 4)); // Inmunidad 6 segundos
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 4)); // Fuerza V (+50% daño) por 20s

        if (victim instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.restoreMana(data.maxMana());
            AwakeningSystemMod.sync(sp);
        }

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.5F, 0.7F);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
        level.sendParticles(ParticleTypes.SQUID_INK, victim.getX(), victim.getY() + 1.0, victim.getZ(), 90, 2.5, 1.5, 2.5, 0.1);
        level.sendParticles(ParticleTypes.PORTAL, victim.getX(), victim.getY() + 1.0, victim.getZ(), 80, 2.5, 1.5, 2.5, 0.3);
        level.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY() + 1.0, victim.getZ(), 50, 2.0, 1.2, 2.0, 0.15);

        // Supernova de Sombras: aturdir y dañar enemigos cercanos
        AABB box = victim.getBoundingBox().inflate(15.0, 6.0, 15.0);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != victim && e.isAlive() && !e.isAlliedTo(victim))) {
            mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3));
            mob.hurt(victim.damageSources().magic(), 25.0F);
        }
    }
}
