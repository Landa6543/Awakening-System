package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TribulationShieldPillItem extends Item {
    public TribulationShieldPillItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            ServerLevel sl = sp.serverLevel();

            // Otorga 3 minutos (3600 ticks) de Escudo de Tribulación Celestial
            data.setTribulationShieldTicks(3600);
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));

            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.6D, 0.8D, 0.6D, 0.15D);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 30, 0.8D, 0.8D, 0.8D, 0.2D);
            sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.1D);

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.4F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 0.9F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.tribulation_shield_pill.consumed"), true);
            AwakeningSystemMod.notify(sp, "item.awakening_system.tribulation_shield_pill.title",
                    "item.awakening_system.tribulation_shield_pill.active_desc", 0xFF55FFFF, "3m");

            AwakeningSystemMod.sync(sp);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.tribulation_shield_pill.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.tribulation_shield_pill.desc2"));
        tooltip.add(Component.translatable("item.awakening_system.tribulation_shield_pill.desc3"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
