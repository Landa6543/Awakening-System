package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.OpenClassSelectionPayload;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PillOfPrimordialRebirthItem extends Item {
    public PillOfPrimordialRebirthItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);

            // Reiniciar clase y talentos sin perder nivel base ni estadísticas
            data.resetHunterClass();
            AwakeningSystemMod.sync(sp);

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.0D);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 1.0D, 1.2D, 1.0D, 0.15D);
            sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 100, 1.5D, 1.5D, 1.5D, 0.2D);

            sp.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
            sp.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);

            sp.sendSystemMessage(Component.literal("§6§m=================================================="));
            sp.sendSystemMessage(Component.literal("§6§l✦ [RENACIMIENTO PRIMORDIAL DEL DESTINO] ✦"));
            sp.sendSystemMessage(Component.literal("§f\"Los hilos del destino cósmico se han deshecho y reconfigurado. Tus talentos de clase han sido disueltos en el éter mientras tu cuerpo y alma conservan todo su poder acumulado.\""));
            sp.sendSystemMessage(Component.literal("§e✔ Elige tu nuevo sendero marcial y trascendente."));
            sp.sendSystemMessage(Component.literal("§6§m=================================================="));

            // Abrir de inmediato la pantalla de selección de clase en el cliente
            PacketDistributor.sendToPlayer(sp, new OpenClassSelectionPayload());

            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6✦ Píldora del Renacimiento Primordial").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("§7\"El elixir supremo capaz de recrear el destino del cazador.\""));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§e✦ Efecto Único de Trascendencia:").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("  §fAl consumirla, disuelve tu clase actual y abre la pantalla"));
        tooltipComponents.add(Component.literal("  §fde §6Selección de Clase §fpara escoger un nuevo destino."));
        tooltipComponents.add(Component.literal("  §a✔ No se pierde nivel base, estadísticas ni progreso de cultivo."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
