package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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

public class CommandSpellItem extends Item {
    public CommandSpellItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    public static int getSeals(ItemStack stack) {
        CompoundTag tag = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != null
                ? stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();
        return tag.contains("command_seals") ? tag.getInt("command_seals") : 3;
    }

    public static void setSeals(ItemStack stack, int seals) {
        CompoundTag tag = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != null
                ? stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();
        tag.putInt("command_seals", Math.max(0, Math.min(3, seals)));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            int currentSeals = getSeals(stack);
            if (currentSeals <= 0) {
                sp.displayClientMessage(Component.translatable("item.awakening_system.command_spell.depleted"), true);
                sp.playNotifySound(SoundEvents.CHAIN_FALL, SoundSource.PLAYERS, 0.8F, 1.2F);
                return InteractionResultHolder.fail(stack);
            }

            setSeals(stack, currentSeals - 1);
            int remaining = currentSeals - 1;

            SystemData data = SystemEvents.data(sp);
            boolean isLowHealth = sp.getHealth() < sp.getMaxHealth() * 0.5F;

            if (isLowHealth) {
                sp.setHealth(sp.getMaxHealth());
                sp.removeAllEffects();
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 4));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
                data.restoreEnergy(data.maxEnergy());
                sp.displayClientMessage(Component.translatable("item.awakening_system.command_spell.absolute_restoration", remaining), true);
            } else {
                data.restoreEnergy(data.maxEnergy());
                sp.getPersistentData().remove("asrahan_holy_cd");
                sp.getPersistentData().remove("homa_skill_cd");
                sp.getPersistentData().remove("milky_way_skill_cd");
                sp.getPersistentData().remove("passing_shore_skill_cd");
                sp.getPersistentData().remove("patience_skill_cd");
                sp.getPersistentData().remove("dreams_rest_skill_cd");
                sp.getPersistentData().remove("before_dawn_skill_cd");
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
                sp.displayClientMessage(Component.translatable("item.awakening_system.command_spell.cooldown_reset", remaining), true);
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getEyeY(), sp.getZ(), 4, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.8D, 1.2D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 1.0D, 1.0D, 1.0D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.1F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.6F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int seals = getSeals(stack);
        tooltip.add(Component.translatable("item.awakening_system.command_spell.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.seals", seals).withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.low_hp_title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.low_hp_desc").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.high_hp_title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.high_hp_desc").withStyle(ChatFormatting.DARK_GRAY));
    }
}
