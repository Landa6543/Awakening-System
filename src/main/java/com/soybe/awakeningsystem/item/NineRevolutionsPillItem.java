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

public class NineRevolutionsPillItem extends Item {
    public NineRevolutionsPillItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.addCultivationQi(10000);

            int subLevel = data.cultivationSubLevel();
            int realm = data.dantianRealm();
            if (data.cultivationQi() >= data.maxCultivationQi() && subLevel < 9) {
                data.setCultivationSubLevel(subLevel + 1);
                data.setCultivationQi(0);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
                sp.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_success", data.cultivationSubLevel(), SystemEvents.getDantianRealmComponent(realm)), true);
            } else {
                sp.displayClientMessage(Component.literal("§6✦ Has refinado la §ePíldora de las Nueve Revoluciones §6(+10,000 Qi)."), true);
            }

            // Buffs supremos de iluminación
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 2));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 3));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.1D);
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 40, 0.6D, 0.6D, 0.6D, 0.05D);

            sp.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
            sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

            AwakeningSystemMod.notify(sp, "awakening_system.item.nine_revolutions_pill.title", "awakening_system.item.nine_revolutions_pill.desc", 0xFFFFD700);
            AwakeningSystemMod.sync(sp);

            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6✦ Píldora Legendaria de Alquimia Celestial").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("§7\"Refinada a través de nueve ciclos de fuego solar y rocío astral.\""));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§e✦ Efectos de Refinamiento:").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("  §a✔ +10,000 Puntos de Qi de Cultivo Inmediato"));
        tooltipComponents.add(Component.literal("  §b✔ Estado de Iluminación: Regeneración III y Fuerza III por 5 min"));
        tooltipComponents.add(Component.literal("  §d✔ Facilita el avance de sub-niveles sin cuello de botella"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
