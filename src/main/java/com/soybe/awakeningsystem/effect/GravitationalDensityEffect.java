package com.soybe.awakeningsystem.effect;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Efecto de Poción: Densidad Gravitacional.
 * Vuelve el cuerpo del cazador ultra denso y firme:
 * - 100% Inmunidad a empuje (Knockback Resistance +1.0).
 * - 75% Reducción de daño por caída.
 * - Onda de Choque Sísmica: Al caer desde alturas (>= 2.5 bloques), descarga la energía
 *   gravitacional acumulada al tocar tierra, dañando, empujando y aturdiendo a todos los enemigos circundantes.
 */
public class GravitationalDensityEffect extends MobEffect {

    private static final ResourceLocation KNOCKBACK_RESIST_ID =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gravitational_knockback_resistance");
    private static final ResourceLocation FALL_DAMAGE_REDUCTION_ID =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gravitational_fall_reduction");

    public GravitationalDensityEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESIST_ID,
                1.0D, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.FALL_DAMAGE_MULTIPLIER, FALL_DAMAGE_REDUCTION_ID,
                -0.75D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel sl) {
            if (sl.random.nextFloat() < 0.45F) {
                double angle = sl.random.nextDouble() * Math.PI * 2.0D;
                double r = 0.35D + sl.random.nextDouble() * 0.35D;
                sl.sendParticles(ModParticles.SHADOW_AURA.get(),
                        entity.getX() + Math.cos(angle) * r,
                        entity.getY() + 0.1D,
                        entity.getZ() + Math.sin(angle) * r,
                        1, 0.0D, -0.04D, 0.0D, 0.01D);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    /**
     * Descarga sísmica al aterrizar. Llamado desde el evento de caída del jugador.
     */
    public static void triggerSeismicImpact(LivingEntity entity, float fallDistance, int amplifier) {
        if (entity.level().isClientSide || !(entity.level() instanceof ServerLevel sl)) return;

        double radius = 4.5D + (amplifier * 1.5D);
        float damage = Math.min(32.0F, 5.0F + (fallDistance * 1.8F) * (1.0F + amplifier * 0.4F));

        AABB area = entity.getBoundingBox().inflate(radius, 2.0D, radius);
        List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area,
                target -> target != entity && target.isAlive() && !target.isAlliedTo(entity) && !target.isSpectator());

        for (LivingEntity target : enemies) {
            target.hurt(entity.damageSources().mobAttack(entity), damage);

            // Empuje radial hacia afuera
            Vec3 knockbackDir = target.position().subtract(entity.position()).normalize();
            target.push(knockbackDir.x * 0.8D, 0.35D, knockbackDir.z * 0.8D);

            // Aturdimiento y debilidad
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true, true));
        }

        // Efectos de sonido del impacto gravitacional masivo
        sl.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.75F, 1.8F);
        sl.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.HEAVY_CORE_HIT, SoundSource.PLAYERS, 1.2F, 0.6F);

        // Anillo de partículas de impacto telúrico gravitacional
        int particleCount = 36 + (amplifier * 16);
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double px = entity.getX() + Math.cos(angle) * (radius * 0.5D);
            double pz = entity.getZ() + Math.sin(angle) * (radius * 0.5D);
            double vx = Math.cos(angle) * 0.28D;
            double vz = Math.sin(angle) * 0.28D;
            sl.sendParticles(ModParticles.SHADOW_AURA.get(), px, entity.getY() + 0.15D, pz, 1, vx, 0.05D, vz, 0.1D);
        }
    }
}
