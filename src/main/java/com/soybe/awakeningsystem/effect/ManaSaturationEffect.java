package com.soybe.awakeningsystem.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

public class ManaSaturationEffect extends MobEffect {
    public ManaSaturationEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // Purposely do not add EffectCures.MILK or any cure: cannot be cured with milk or items
        cures.clear();
    }
}
