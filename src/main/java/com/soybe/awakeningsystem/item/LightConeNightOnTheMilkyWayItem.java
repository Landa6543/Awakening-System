package com.soybe.awakeningsystem.item;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LightConeNightOnTheMilkyWayItem extends Item {
    public LightConeNightOnTheMilkyWayItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("milky_way_skill_cd");
            if (gameTime < cdUntil) {
                long sec = Math.max(1, (cdUntil - gameTime + 19) / 20);
                sp.displayClientMessage(Component.literal("§d§l✦ Cono de Luz en recarga: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("milky_way_skill_cd", gameTime + 360L); // 18s cooldown

            // Bombardeo Estelar Orbital
            AABB area = sp.getBoundingBox().inflate(14.0D, 6.0D, 14.0D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != sp && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));

            float baseDamage = 45.0F + Math.min(30.0F, targets.size() * 6.0F);

            for (LivingEntity enemy : targets) {
                enemy.hurt(sp.damageSources().playerAttack(sp), baseDamage);
                enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, enemy.getX(), enemy.getY() + 2.0D, enemy.getZ(), 20, 0.4D, 0.8D, 0.4D, 0.1D);
                    sl.sendParticles(ParticleTypes.FIREWORK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.05D);
                }
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 4, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 2.0D, sp.getZ(), 50, 1.5D, 1.5D, 1.5D, 0.15D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.5F, 0.9F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.6F, 1.7F);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            sp.displayClientMessage(Component.literal("§d§l✦ [NOCHE EN LA VÍA LÁCTEA] §f¡Bombardeo orbital cósmico desatado! (" + targets.size() + " objetivos)"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Cono de Luz Legendario 5★ - Erudición]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7El cono de luz exclusivo de Himeko (Honkai: Star Rail).").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Capta la luz de una taza de café bajo un mar interminable de galaxias.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§d▸ Habilidad Activa (Clic derecho): Bombardeo Orbital").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("  §7Impacta a todos los enemigos en 14 bloques con 45+ daño estelar.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("  §7El daño escala +6 por cada enemigo adicional presente.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
