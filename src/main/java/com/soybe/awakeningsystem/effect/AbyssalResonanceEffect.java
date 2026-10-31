package com.soybe.awakeningsystem.effect;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Efecto de Poción: Resonancia Abisal.
 * Canaliza la energía sónica del vacío:
 * - Incrementa el daño físico y mágico en un 20% (+35% en nivel II).
 * - Regenera Maná de forma continua cada segundo (+12 maná/s, duplicado a +25 maná/s en profundidades Y <= -10).
 */
public class AbyssalResonanceEffect extends MobEffect {

    private static final ResourceLocation ATTACK_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_resonance_attack_boost");

    public AbyssalResonanceEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_BOOST_ID,
                0.20D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                boolean deepAbyss = player.getY() <= -10.0D;
                int baseRegen = 12 + (amplifier * 8);
                int actualRegen = deepAbyss ? (baseRegen * 2) : baseRegen;

                data.restoreEnergy(actualRegen);
                AwakeningSystemMod.sync(player);

                if (player.level() instanceof ServerLevel sl && sl.random.nextFloat() < 0.65F) {
                    sl.sendParticles(ModParticles.MANA_AURA.get(),
                            player.getX(), player.getY() + 0.8D, player.getZ(),
                            3, 0.25D, 0.35D, 0.25D, 0.03D);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.3F, 1.8F);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
