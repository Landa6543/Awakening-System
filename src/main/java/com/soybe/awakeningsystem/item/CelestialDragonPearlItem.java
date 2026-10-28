package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.entity.CelestialDragonEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Perla Sagrada del Dragón Celestial de Murim (Shenlong Pearl / 龙珠).
 * Tesoro cósmico de cultivo milenario:
 * - Al usarse directamente en el aire, el cultivador absorbe el núcleo de Qi del dragón (+1,000 Qi permanente, restauración total de salud y energía).
 * - Usado sobre un Dragón Celestial salvaje lo somete instantáneamente como montura divina leal.
 * - Usado sobre un Dragón Celestial propio lo cura completamente y restaura su vitalidad.
 */
public class CelestialDragonPearlItem extends Item {

    public CelestialDragonPearlItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (interactionTarget instanceof CelestialDragonEntity dragon) {
            return dragon.mobInteract(player, hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);

            data.addCultivationQi(1000);
            data.restoreEnergy(data.maxEnergy());
            sp.heal(sp.getMaxHealth());

            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 2));
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0, sp.getZ(), 120, 1.2, 1.2, 1.2, 0.4);
                serverLevel.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2, sp.getZ(), 80, 1.0, 1.0, 1.0, 0.1);
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0, sp.getZ(), 50, 1.0, 0.8, 1.0, 0.05);
            }

            level.playSound(null, sp.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 1.1F);
            level.playSound(null, sp.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);
            level.playSound(null, sp.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 0.9F);

            AwakeningSystemMod.notify(sp,
                    "awakening_system.item.celestial_dragon_pearl.title",
                    "awakening_system.item.celestial_dragon_pearl.desc",
                    0xFFFFD700);

            sp.displayClientMessage(Component.translatable("awakening_system.item.celestial_dragon_pearl.chat"), false);

            AwakeningSystemMod.sync(sp);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.item.celestial_dragon_pearl.tooltip_lore"));
        tooltip.add(Component.translatable("awakening_system.item.celestial_dragon_pearl.tooltip_desc"));
    }
}
