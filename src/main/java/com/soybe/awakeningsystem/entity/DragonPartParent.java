package com.soybe.awakeningsystem.entity;

import net.minecraft.world.damagesource.DamageSource;

public interface DragonPartParent {
    boolean hurtFromPart(DragonBodyPart part, DamageSource source, float amount);
}
