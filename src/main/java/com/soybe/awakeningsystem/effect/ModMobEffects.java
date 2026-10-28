package com.soybe.awakeningsystem.effect;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AwakeningSystemMod.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> PYRO_FERVOR = MOB_EFFECTS.register("pyro_fervor",
            () -> new TeyvatElementalEffect(MobEffectCategory.BENEFICIAL, 0xFF3D00));
    public static final DeferredHolder<MobEffect, MobEffect> HYDRO_SOOTHING = MOB_EFFECTS.register("hydro_soothing",
            () -> new TeyvatElementalEffect(MobEffectCategory.BENEFICIAL, 0x0091EA));
    public static final DeferredHolder<MobEffect, MobEffect> CRYO_SHATTER = MOB_EFFECTS.register("cryo_shatter",
            () -> new TeyvatElementalEffect(MobEffectCategory.BENEFICIAL, 0x80D8FF));
    public static final DeferredHolder<MobEffect, MobEffect> ELECTRO_RESONANCE = MOB_EFFECTS.register("electro_resonance",
            () -> new TeyvatElementalEffect(MobEffectCategory.BENEFICIAL, 0xAA00FF));
    public static final DeferredHolder<MobEffect, MobEffect> ANEMO_SWIFTNESS = MOB_EFFECTS.register("anemo_swiftness",
            () -> new TeyvatElementalEffect(MobEffectCategory.BENEFICIAL, 0x00E676));
    public static final DeferredHolder<MobEffect, MobEffect> GEO_ENDURANCE = MOB_EFFECTS.register("geo_endurance",
            () -> new TeyvatElementalEffect(MobEffectCategory.BENEFICIAL, 0xFFD600));
    public static final DeferredHolder<MobEffect, MobEffect> DENDRO_VITALITY = MOB_EFFECTS.register("dendro_vitality",
            () -> new TeyvatElementalEffect(MobEffectCategory.BENEFICIAL, 0x76FF03));

    public static final DeferredHolder<MobEffect, MobEffect> SHEER_COLD = MOB_EFFECTS.register("sheer_cold",
            () -> new PeriodicHarmfulEffect(MobEffectCategory.HARMFUL, 0x00E5FF, 40, 2.0F));
    public static final DeferredHolder<MobEffect, MobEffect> CORROSION = MOB_EFFECTS.register("corrosion",
            () -> new PeriodicHarmfulEffect(MobEffectCategory.HARMFUL, 0x4A148C, 25, 1.5F));
    public static final DeferredHolder<MobEffect, MobEffect> DELUSION_SURGE = MOB_EFFECTS.register("delusion_surge",
            () -> new PeriodicHarmfulEffect(MobEffectCategory.NEUTRAL, 0xD50000, 30, 1.0F));
    public static final DeferredHolder<MobEffect, MobEffect> ELECTRIFIED = MOB_EFFECTS.register("electrified",
            () -> new PeriodicHarmfulEffect(MobEffectCategory.HARMFUL, 0x7C4DFF, 35, 1.5F));
    public static final DeferredHolder<MobEffect, MobEffect> WET = MOB_EFFECTS.register("wet",
            () -> new TeyvatElementalEffect(MobEffectCategory.NEUTRAL, 0x2979FF));
    public static final DeferredHolder<MobEffect, MobEffect> MANA_SATURATION = MOB_EFFECTS.register("mana_saturation",
            () -> new ManaSaturationEffect(MobEffectCategory.HARMFUL, 0x00D4FF));

    public static class TeyvatElementalEffect extends MobEffect {
        public TeyvatElementalEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    public static class PeriodicHarmfulEffect extends MobEffect {
        private final int tickInterval;
        private final float damagePerTick;

        public PeriodicHarmfulEffect(MobEffectCategory category, int color, int tickInterval, float damagePerTick) {
            super(category, color);
            this.tickInterval = tickInterval;
            this.damagePerTick = damagePerTick;
        }

        @Override
        public boolean applyEffectTick(LivingEntity entity, int amplifier) {
            entity.hurt(entity.damageSources().magic(), damagePerTick * (amplifier + 1));
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            int interval = Math.max(5, tickInterval >> amplifier);
            return duration % interval == 0;
        }
    }
}
