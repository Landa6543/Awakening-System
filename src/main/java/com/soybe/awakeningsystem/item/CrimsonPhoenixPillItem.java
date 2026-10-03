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

public class CrimsonPhoenixPillItem extends Item {
    public CrimsonPhoenixPillItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.addCultivationQi(6000);

            int subLevel = data.cultivationSubLevel();
            int realm = data.dantianRealm();
            if (data.cultivationQi() >= data.maxCultivationQi() && subLevel < 9) {
                data.setCultivationSubLevel(subLevel + 1);
                data.setCultivationQi(0);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
                sp.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_success", data.cultivationSubLevel(), SystemEvents.getDantianRealmComponent(realm)), true);
            } else {
                sp.displayClientMessage(Component.literal("§c✦ Has refinado la §6Píldora del Fénix Carmesí §c(+6,000 Qi)."), true);
            }

            // Renacimiento del Fénix: Curación total e inmunidad ígnea
            sp.setHealth(sp.getMaxHealth());
            sp.removeAllEffects();
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0)); // 10 min
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 2));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 0.8D, 1.0D, 0.8D, 0.15D);
            sl.sendParticles(ParticleTypes.LAVA, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);

            sp.playNotifySound(SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 1.0F);
            sp.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.9F, 1.3F);

            AwakeningSystemMod.notify(sp, "awakening_system.item.crimson_phoenix_pill.title", "awakening_system.item.crimson_phoenix_pill.desc", 0xFFFF4500);
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
        tooltipComponents.add(Component.literal("§c✦ Píldora del Nirvana del Fénix Carmesí").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("§7\"Destilada con plumas y sangre ardiente del fénix ancestral inmortal.\""));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§e✦ Efectos de Refinamiento:").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("  §a✔ +6,000 Puntos de Qi de Cultivo Inmediato"));
        tooltipComponents.add(Component.literal("  §c✔ Renacimiento: Restaura el 100% de Vida y limpia efectos negativos"));
        tooltipComponents.add(Component.literal("  §6✔ Inmunidad al fuego por 10 minutos"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
