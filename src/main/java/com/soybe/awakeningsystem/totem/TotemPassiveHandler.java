package com.soybe.awakeningsystem.totem;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.item.AwakeningTotemItem;
import com.soybe.awakeningsystem.item.AwakeningTotemItem.TotemType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * Gestor de Efectos Pasivos del Árbol de Tótems del Sistema.
 * Confiere beneficios pasivos y mecánicas especiales cuando el jugador
 * empuña el tótem en la mano principal o en la mano secundaria (off-hand).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class TotemPassiveHandler {

    private TotemPassiveHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null) return;

        // 1. TÓTEM DE ESCARCHA GLACIAL: Caminar sobre Nieve en Polvo e Inmunidad al Frío
        // Funciona en cliente y servidor para predicción y movimiento suave sin tirones
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.GLACIAL_FROST)) {
            player.setTicksFrozen(0);

            BlockPos feetPos = player.blockPosition();
            BlockState feetState = player.level().getBlockState(feetPos);
            BlockState belowState = player.level().getBlockState(feetPos.below());

            boolean inPowderSnow = feetState.is(Blocks.POWDER_SNOW);
            boolean onPowderSnow = belowState.is(Blocks.POWDER_SNOW);

            if (inPowderSnow || onPowderSnow) {
                player.resetFallDistance();
                Vec3 delta = player.getDeltaMovement();
                if (inPowderSnow) {
                    // Si el jugador entra en nieve en polvo, se impulsa a la superficie (como botas de cuero)
                    player.setDeltaMovement(delta.x, Math.max(delta.y, 0.12), delta.z);
                    player.setOnGround(true);
                } else if (onPowderSnow && delta.y < 0.0) {
                    // Si camina encima de nieve en polvo, se evita que se hunda
                    player.setDeltaMovement(delta.x, 0.0, delta.z);
                    player.setOnGround(true);
                }
            }
        }

        // El resto de efectos continuos se calculan en el servidor cada intervalo de tiempo
        if (player.level().isClientSide()) return;

        SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        boolean tick1s = (player.tickCount % 20 == 0);
        boolean tick2s = (player.tickCount % 40 == 0);

        // 2. OVERWORLD: Velocidad I y Prisa I
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.OVERWORLD)) {
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, true, false, true));
            }
        }

        // 3. MURIM QI: Fuerza I, Velocidad I y Regeneración de Energía
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.MURIM_QI)) {
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false, true));
                if (data != null) {
                    data.addEnergy(2);
                }
            }
        }

        // 4. TEYVAT ARCHON: Salto II, Regeneración I y anulación de daño de caída
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.TEYVAT_ARCHON)) {
            player.resetFallDistance();
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
            }
        }

        // 5. ABYSSAL RESONANT: Visión Nocturna e Inmunidad a Oscuridad/Ceguera
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.ABYSSAL_RESONANT)) {
            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.BLINDNESS);
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, true));
            }
        }

        // 6. SOULFORGE: Resistencia al Fuego. Si está ardiendo o en lava, Fuerza II y Velocidad II
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.SOULFORGE)) {
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
            }
            if (player.isOnFire() || player.isInLava()) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, true, false, true));
            }
        }

        // 7. ASTRALITE: Suerte II y Regeneración de Maná (+4/s)
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.ASTRALITE)) {
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 100, 1, true, false, true));
                if (data != null) {
                    data.restoreMana(4);
                }
            }
        }

        // 8. MITHRIL: Prisa II y Velocidad II
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.MITHRIL)) {
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, true, false, true));
            }
        }

        // 9. ADAMANTITE: Resistencia II (Anti-Knockback en evento)
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.ADAMANTITE)) {
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, true, false, true));
            }
        }

        // 10. SPIRIT JADE: Regeneración II, purificación de debuffs y flujo de Maná/Energía
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.SPIRIT_JADE)) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
            player.removeEffect(MobEffects.HUNGER);
            player.removeEffect(MobEffects.WEAKNESS);
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, false, true));
                if (data != null) {
                    data.restoreMana(2);
                    data.addEnergy(2);
                }
            }
        }

        // 11. ASTRAL: Fuerza II, Resistencia I y Caída Lenta en el aire
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.ASTRAL)) {
            if (!player.onGround() && player.getDeltaMovement().y < 0.0) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, true));
            }
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false, true));
            }
        }

        // 12. CELESTIAL: Absorción II continua (4 corazones), Regeneración II y Aura de Brillo
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.CELESTIAL)) {
            if (player.getAbsorptionAmount() < 4.0F) {
                player.setAbsorptionAmount(4.0F);
            }
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, true, false, true));
            }
            if (tick2s) {
                AABB box = player.getBoundingBox().inflate(12.0);
                for (LivingEntity monster : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Monster)) {
                    monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
                }
            }
        }

        // 13. PRIMORDIAL: Purificación absoluta de todo efecto negativo, Regeneración III, Fuerza III y Saturación
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.PRIMORDIAL)) {
            for (MobEffectInstance inst : List.copyOf(player.getActiveEffects())) {
                if (inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    player.removeEffect(inst.getEffect());
                }
            }
            if (player.getFoodData().needsFood()) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0F);
            }
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 2, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 2, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
            }
        }

        // 14. AWAKENING MONARCH: Fuerza IV, Velocidad II, Resistencia II, Visión Nocturna y Presencia del Monarca
        if (AwakeningTotemItem.isHoldingTotem(player, TotemType.AWAKENING_MONARCH)) {
            if (tick1s) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 3, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, true));

                // Presencia del Monarca: debilita y ralentiza monstruos normales cercanos
                AABB box = player.getBoundingBox().inflate(9.0);
                for (LivingEntity monster : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Monster && e.getMaxHealth() <= 100.0F)) {
                    monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, false, false));
                    monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false, false));
                }

                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.1, player.getZ(), 3, 0.3, 0.05, 0.3, 0.01);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // Reducciones de daño cuando el jugador es el objetivo
        if (event.getEntity() instanceof Player player) {
            // Tótem Abisal: 50% de resistencia a la onda sónica del Warden
            if (AwakeningTotemItem.isHoldingTotem(player, TotemType.ABYSSAL_RESONANT)) {
                if (event.getSource().is(DamageTypes.SONIC_BOOM)) {
                    event.setNewDamage(event.getNewDamage() * 0.5F);
                }
            }
            // Tótem de Mithril: 10% de reducción a todo daño físico/general
            if (AwakeningTotemItem.isHoldingTotem(player, TotemType.MITHRIL)) {
                event.setNewDamage(event.getNewDamage() * 0.9F);
            }
        }

        // Efectos al atacar cuando el jugador es el agresor
        if (event.getSource().getEntity() instanceof Player player) {
            // Tótem Glacial: ralentiza a los enemigos golpeados
            if (AwakeningTotemItem.isHoldingTotem(player, TotemType.GLACIAL_FROST)) {
                LivingEntity target = event.getEntity();
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true, true));
            }
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        // Tótem de Adamantita: 100% Inmunidad al retroceso
        if (event.getEntity() instanceof Player player) {
            if (AwakeningTotemItem.isHoldingTotem(player, TotemType.ADAMANTITE)) {
                event.setCanceled(true);
            }
        }
    }
}
