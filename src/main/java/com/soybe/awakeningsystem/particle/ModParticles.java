package com.soybe.awakeningsystem.particle;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModParticles {
    private ModParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, AwakeningSystemMod.MOD_ID);

    public static final Supplier<SimpleParticleType> SPIRIT_FOXFIRE =
            PARTICLE_TYPES.register("spirit_foxfire", () -> new SimpleParticleType(false));
}
