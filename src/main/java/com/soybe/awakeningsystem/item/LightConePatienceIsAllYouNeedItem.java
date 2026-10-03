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

public class LightConePatienceIsAllYouNeedItem extends Item {
    public LightConePatienceIsAllYouNeedItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("patience_skill_cd");
            if (gameTime < cdUntil) {
                long sec = Math.max(1, (cdUntil - gameTime + 19) / 20);
                sp.displayClientMessage(Component.literal("§d§l✦ Cono de Luz en recarga: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("patience_skill_cd", gameTime + 320L); // 16s cooldown

            AABB area = sp.getBoundingBox().inflate(14.0D, 6.0D, 14.0D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != sp && e.isAlive() && !e.getType().getCategory().isFriendly() && !e.getTags().contains("shadow_soldier"));

            float baseDamage = 42.0F;
            Vec3 center = sp.position();

            for (LivingEntity enemy : targets) {
                enemy.hurt(sp.damageSources().playerAttack(sp), baseDamage);
                enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 140, 0));

                // Atracción electromagnética sutil por los hilos del destino
                Vec3 pull = center.subtract(enemy.position()).normalize().scale(0.8D);
                enemy.setDeltaMovement(pull.x, 0.3D, pull.z);

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.15D);
                    sl.sendParticles(ParticleTypes.WITCH, enemy.getX(), enemy.getY() + 1.2D, enemy.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
                }
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 3, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 1.2D, 0.8D, 1.2D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.2F, 1.6F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.5F, 1.8F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LEASH_KNOT_BREAK, SoundSource.PLAYERS, 1.4F, 0.7F);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2)); // Speed III
            sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1)); // Haste II
            sp.displayClientMessage(Component.literal("§d§l✦ [SOLO HAY QUE ESPERAR] §f¡Hilos del Estelaron y Electrocución desatados! (Boom)"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Cono de Luz Legendario 5★ - Nihilidad]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7El cono de luz exclusivo de Kafka (Honkai: Star Rail).").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Bajo una lluvia tranquila, escucha el latir de tu corazón... Boom.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§d▸ Habilidad Activa (Clic derecho): Telaraña del Destino").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("  §7Atrae a los enemigos en 14 bloques, infligiendo 42 de daño de rayo.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("  §7Otorga al usuario Velocidad III y Prisa minera II por 10 segundos.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
