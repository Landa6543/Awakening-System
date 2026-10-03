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

public class LightConeBeforeDawnItem extends Item {
    public LightConeBeforeDawnItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("before_dawn_skill_cd");
            if (gameTime < cdUntil) {
                long sec = Math.max(1, (cdUntil - gameTime + 19) / 20);
                sp.displayClientMessage(Component.literal("§e§l✦ Cono de Luz en recarga: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("before_dawn_skill_cd", gameTime + 400L); // 20s cooldown

            AABB area = sp.getBoundingBox().inflate(16.0D, 8.0D, 16.0D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != sp && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));

            float celestialDamage = 50.0F;

            for (LivingEntity enemy : targets) {
                enemy.hurt(sp.damageSources().playerAttack(sp), celestialDamage);
                enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.5D, enemy.getZ(), 30, 0.4D, 1.2D, 0.4D, 0.2D);
                }
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 60, 1.5D, 1.0D, 1.5D, 0.3D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.6F, 1.1F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.8F, 0.9F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.5F, 1.3F);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1)); // Strength II
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1)); // Absorption II
            sp.displayClientMessage(Component.literal("§e§l✦ [ANTES DEL AMANECER] §6¡¡Juicio Celestial del Señor del Rayo ejecutado!! (" + targets.size() + " objetivos)"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Cono de Luz Legendario 5★ - Erudición]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7El cono de luz exclusivo de Jing Yuan (Honkai: Star Rail).").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Aquel que comanda a los soldados divinos observa con calma antes del alba.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§e▸ Habilidad Activa (Clic derecho): Señor del Rayo Celestial").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §7Descarga truenos divinos a todos los enemigos en 16 bloques infligiendo 50 de daño.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("  §7Otorga al usuario Fuerza II y Absorción II durante 10 segundos.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
