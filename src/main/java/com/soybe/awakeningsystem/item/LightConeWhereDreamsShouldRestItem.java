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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LightConeWhereDreamsShouldRestItem extends Item {
    public LightConeWhereDreamsShouldRestItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("dreams_rest_skill_cd");
            if (gameTime < cdUntil) {
                long sec = Math.max(1, (cdUntil - gameTime + 19) / 20);
                sp.displayClientMessage(Component.literal("§6§l✦ Cono de Luz en recarga: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("dreams_rest_skill_cd", gameTime + 360L); // 18s cooldown

            // Propulsión frontal con ignición de la armadura SAM
            Vec3 look = sp.getLookAngle();
            sp.setDeltaMovement(look.x * 1.6D, 0.4D + Math.max(0.0D, look.y * 0.8D), look.z * 1.6D);
            sp.hurtMarked = true;

            AABB area = sp.getBoundingBox().inflate(12.0D, 6.0D, 12.0D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != sp && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));

            float blastDamage = 55.0F;

            for (LivingEntity enemy : targets) {
                enemy.hurt(sp.damageSources().playerAttack(sp), blastDamage);
                enemy.setRemainingFireTicks(180);
                enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));

                Vec3 knockback = enemy.position().subtract(sp.position()).normalize().scale(1.2D);
                enemy.setDeltaMovement(knockback.x, 0.4D, knockback.z);

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.LAVA, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 15, 0.3D, 0.5D, 0.3D, 0.1D);
                    sl.sendParticles(ParticleTypes.FLAME, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.15D);
                }
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.1D);
                sl.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 50, 1.5D, 0.5D, 1.5D, 0.2D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.1F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.4F, 0.8F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 1.4F);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1)); // Resistance II
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0)); // Fire Resistance
            sp.displayClientMessage(Component.literal("§6§l✦ [DONDE REPOSAN LOS SUEÑOS] §c¡¡Prenderé fuego a los mares hasta que florezca la última luciérnaga!!"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Cono de Luz Legendario 5★ - Destrucción]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7El cono de luz exclusivo de Firefly / SAM (Honkai: Star Rail).").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7El despertar de la Caballería de Hierro bajo un manto de cenizas incandescentes.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§6▸ Habilidad Activa (Clic derecho): Combustión Completa SAM").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  §7Propulsa al usuario al frente, causando una explosión de 55 daño ígneo en 12 bloques.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("  §7Otorga al usuario Resistencia II e Inmunidad al Fuego por 10 segundos.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
