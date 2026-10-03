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

public class LightConeAlongThePassingShoreItem extends Item {
    public LightConeAlongThePassingShoreItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("passing_shore_skill_cd");
            if (gameTime < cdUntil) {
                long sec = Math.max(1, (cdUntil - gameTime + 19) / 20);
                sp.displayClientMessage(Component.literal("§c§l✦ Cono de Luz en recarga: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("passing_shore_skill_cd", gameTime + 360L); // 18s cooldown

            AABB area = sp.getBoundingBox().inflate(12.0D, 6.0D, 12.0D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != sp && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));

            float baseDamage = 52.0F;

            for (LivingEntity enemy : targets) {
                float totalDamage = baseDamage;
                if (enemy.getHealth() < enemy.getMaxHealth() * 0.4F) {
                    totalDamage += 25.0F; // Ejecución de la Nihilidad
                }
                enemy.hurt(sp.damageSources().playerAttack(sp), totalDamage);
                enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3));
                enemy.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);
                    sl.sendParticles(ParticleTypes.PORTAL, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
                }
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 10, 1.2D, 0.2D, 1.2D, 0.1D);
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 40, 1.5D, 0.5D, 1.5D, 0.08D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 0.6F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 0.8F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 1.5F);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            sp.displayClientMessage(Component.literal("§c§l✦ [EN LA ORILLA PASAJERA] §f¡Corte del Río Estigio y Desolación Carmesí desatados! (" + targets.size() + " objetivos)"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Cono de Luz Legendario 5★ - Nihilidad]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7El cono de luz exclusivo de Acheron (Honkai: Star Rail).").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Bajo la lluvia de sangre del Río del Olvido, no queda ni pasado ni futuro.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§c▸ Habilidad Activa (Clic derecho): Tajo del Río Estigio").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  §7Inflige 52 de daño de corte y descomposición a enemigos en 12 bloques.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("  §7Aplica Desolación Carmesí (+25 daño a objetivos con <40% HP) y Descomposición.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
