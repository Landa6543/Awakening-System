package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SpiritQiCarrotItem extends Item {

    public SpiritQiCarrotItem() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(10)
                        .saturationModifier(2.0F) // 20 puntos de saturación
                        .alwaysEdible()
                        .build())
                .rarity(Rarity.RARE)
                .stacksTo(64));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                // Restaura al instante un 25% de la reserva espiritual total (Qi o Maná)
                int maxQi = data.maxCultivationQi();
                if (maxQi > 0) {
                    int qiRestore = (int) Math.round(maxQi * 0.25);
                    data.addCultivationQi(Math.max(qiRestore, 15));
                }

                int maxMana = data.maxMana();
                if (maxMana > 0) {
                    int manaRestore = (int) Math.round(maxMana * 0.25);
                    data.restoreMana(Math.max(manaRestore, 25));
                }

                AwakeningSystemMod.sync(player);
            }

            // Efectos sonoros y partículas de Qi
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.5, 0.4, 0.05);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 0.8, player.getZ(), 25, 0.5, 0.6, 0.5, 0.08);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.spirit_qi_carrot.desc1").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.spirit_qi_carrot.desc2").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(" "));
        tooltip.add(Component.translatable("item.awakening_system.spirit_qi_carrot.effect1").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.awakening_system.spirit_qi_carrot.effect2").withStyle(ChatFormatting.AQUA));
    }
}
