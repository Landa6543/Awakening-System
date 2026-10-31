package com.soybe.awakeningsystem.potion;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.effect.ModMobEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, AwakeningSystemMod.MOD_ID);

    // --- ECO ABISAL (Abyssal Echo) ---
    // Base: 3:00 (3600 ticks)
    public static final DeferredHolder<Potion, Potion> ABYSSAL_ECHO = POTIONS.register("abyssal_echo",
            () -> new Potion("abyssal_echo", new MobEffectInstance(ModMobEffects.ABYSSAL_ECHO, 3600, 0)));
    // Prolongada: 8:00 (9600 ticks)
    public static final DeferredHolder<Potion, Potion> LONG_ABYSSAL_ECHO = POTIONS.register("long_abyssal_echo",
            () -> new Potion("abyssal_echo", new MobEffectInstance(ModMobEffects.ABYSSAL_ECHO, 9600, 0)));
    // Fuerte: Nivel II (amp 1), 1:30 (1800 ticks)
    public static final DeferredHolder<Potion, Potion> STRONG_ABYSSAL_ECHO = POTIONS.register("strong_abyssal_echo",
            () -> new Potion("abyssal_echo", new MobEffectInstance(ModMobEffects.ABYSSAL_ECHO, 1800, 1)));

    // --- DENSIDAD GRAVITACIONAL (Gravitational Density) ---
    // Base: 3:00 (3600 ticks)
    public static final DeferredHolder<Potion, Potion> GRAVITATIONAL_DENSITY = POTIONS.register("gravitational_density",
            () -> new Potion("gravitational_density", new MobEffectInstance(ModMobEffects.GRAVITATIONAL_DENSITY, 3600, 0)));
    // Prolongada: 8:00 (9600 ticks)
    public static final DeferredHolder<Potion, Potion> LONG_GRAVITATIONAL_DENSITY = POTIONS.register("long_gravitational_density",
            () -> new Potion("gravitational_density", new MobEffectInstance(ModMobEffects.GRAVITATIONAL_DENSITY, 9600, 0)));
    // Fuerte: Nivel II (amp 1), 1:30 (1800 ticks)
    public static final DeferredHolder<Potion, Potion> STRONG_GRAVITATIONAL_DENSITY = POTIONS.register("strong_gravitational_density",
            () -> new Potion("gravitational_density", new MobEffectInstance(ModMobEffects.GRAVITATIONAL_DENSITY, 1800, 1)));

    // --- RESONANCIA ABISAL (Abyssal Resonance) ---
    // Base: 3:00 (3600 ticks)
    public static final DeferredHolder<Potion, Potion> ABYSSAL_RESONANCE = POTIONS.register("abyssal_resonance",
            () -> new Potion("abyssal_resonance", new MobEffectInstance(ModMobEffects.ABYSSAL_RESONANCE, 3600, 0)));
    // Prolongada: 8:00 (9600 ticks)
    public static final DeferredHolder<Potion, Potion> LONG_ABYSSAL_RESONANCE = POTIONS.register("long_abyssal_resonance",
            () -> new Potion("abyssal_resonance", new MobEffectInstance(ModMobEffects.ABYSSAL_RESONANCE, 9600, 0)));
    // Fuerte: Nivel II (amp 1), 1:30 (1800 ticks)
    public static final DeferredHolder<Potion, Potion> STRONG_ABYSSAL_RESONANCE = POTIONS.register("strong_abyssal_resonance",
            () -> new Potion("abyssal_resonance", new MobEffectInstance(ModMobEffects.ABYSSAL_RESONANCE, 1800, 1)));

    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();

        // 1. Eco Abisal: Poción Rara (Torpe/Awkward) + Fragmento de Resonancia Abisal
        builder.addMix(Potions.AWKWARD, SystemItems.ABYSSAL_RESONANT_SHARD.get(), ABYSSAL_ECHO);
        builder.addMix(ABYSSAL_ECHO, Items.REDSTONE, LONG_ABYSSAL_ECHO);
        builder.addMix(ABYSSAL_ECHO, Items.GLOWSTONE_DUST, STRONG_ABYSSAL_ECHO);

        // 2. Densidad Gravitacional: Poción de Eco Abisal + Roca Abisal
        builder.addMix(ABYSSAL_ECHO, SystemBlocks.ABYSSAL_STONE.get().asItem(), GRAVITATIONAL_DENSITY);
        builder.addMix(GRAVITATIONAL_DENSITY, Items.REDSTONE, LONG_GRAVITATIONAL_DENSITY);
        builder.addMix(GRAVITATIONAL_DENSITY, Items.GLOWSTONE_DUST, STRONG_GRAVITATIONAL_DENSITY);

        // 3. Resonancia Abisal: Poción de Eco Abisal + Fragmento de Cristal de Maná Rango C
        builder.addMix(ABYSSAL_ECHO, SystemItems.MANA_CRYSTAL_SHARD_C.get(), ABYSSAL_RESONANCE);
        builder.addMix(ABYSSAL_RESONANCE, Items.REDSTONE, LONG_ABYSSAL_RESONANCE);
        builder.addMix(ABYSSAL_RESONANCE, Items.GLOWSTONE_DUST, STRONG_ABYSSAL_RESONANCE);
    }

    private ModPotions() {}
}
