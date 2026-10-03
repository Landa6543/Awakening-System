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
    private static final long COOLDOWN_MS = 60_000L; // 60 segundos de recarga

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

        // Restaurar Maná y Qi al 100%
        data.setMana(data.maxMana());
        data.setCultivationQi(data.maxCultivationQi());

        // Otorgar efectos de protección vital
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 25, 2, false, false, true)); // 6 corazones dorados
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1, false, false, true));

        // Partículas y sonido
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 0.6, 0.6, 0.2);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2, player.getZ(), 25, 0.5, 0.5, 0.5, 0.15);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.8F);

        player.sendSystemMessage(Component.literal("§a§l[MANTO DEL DOCTOR] §2¡Canalización cósmica! Maná y Qi restaurados al 100% con escudo vital."));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5§l['Curio' Manto del Doctor]").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Vestidura de erudito del cosmos proveniente del Universo Simulado."));
        tooltip.add(Component.literal("§a✦ Canalización del Sabio:"));
        tooltip.add(Component.literal("§7  Al recibir daño severo (vida por debajo del 50%):"));
        tooltip.add(Component.literal("§2  • Regenera instantáneamente el 100% de tu Maná y Qi."));
        tooltip.add(Component.literal("§e  • Otorga 6 corazones dorados de Absorción y Regeneración."));
        tooltip.add(Component.literal("§8  (Tiempo de recarga: 60 segundos)"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
