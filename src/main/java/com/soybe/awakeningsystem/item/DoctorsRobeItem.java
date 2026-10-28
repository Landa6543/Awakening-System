package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DoctorsRobeItem extends Item {

    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 60_000L;

    public DoctorsRobeItem() {
        super(new Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    public static boolean trigger(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long last = COOLDOWNS.get(player.getUUID());
        if (last != null && (now - last) < COOLDOWN_MS) {
            return false;
        }

        COOLDOWNS.put(player.getUUID(), now);
        SystemData data = com.soybe.awakeningsystem.SystemEvents.data(player);

        data.setMana(data.maxMana());
        data.setCultivationQi(data.maxCultivationQi());

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 25, 2, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1, false, false, true));

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 0.6, 0.6, 0.2);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2, player.getZ(), 25, 0.5, 0.5, 0.5, 0.15);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.8F);

        player.sendSystemMessage(Component.translatable("item.awakening_system.doctors_robe.activation"));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.desc"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.skill_title"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.cond"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.effect2"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.cd"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
