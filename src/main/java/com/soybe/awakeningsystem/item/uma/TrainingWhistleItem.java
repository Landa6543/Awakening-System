package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Silbato de Entrenamiento (Whistle).
 * Utilitario oficial de entrenadores de la Academia Tracen.
 * Reinicia profesiones, niveles y tablas de comercio de aldeanos en un radio de 10 bloques.
 */
public class TrainingWhistleItem extends Item {

    public TrainingWhistleItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1).durability(250));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 30);

        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            AABB area = player.getBoundingBox().inflate(10.0D);
            List<Villager> villagers = sl.getEntitiesOfClass(Villager.class, area);

            int count = 0;
            for (Villager v : villagers) {
                if (v.isBaby()) continue;

                v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.NONE).setLevel(1));
                v.setVillagerXp(0);
                v.setOffers(new MerchantOffers());

                sl.sendParticles(ParticleTypes.NOTE, v.getX(), v.getY() + 1.2, v.getZ(), 8, 0.4, 0.4, 0.4, 0.1);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, v.getX(), v.getY() + 1.0, v.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
                count++;
            }

            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 1.8F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.5F, 1.4F);

            stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));

            if (count > 0) {
                player.displayClientMessage(Component.translatable("item.awakening_system.training_whistle.success", count), true);
            } else {
                player.displayClientMessage(Component.translatable("item.awakening_system.training_whistle.no_villagers"), true);
            }

            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc1").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc2").withStyle(ChatFormatting.GREEN));
    }
}
