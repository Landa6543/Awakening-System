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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class StaffOfHomaItem extends SwordItem implements AwakeningWeaponItem {
    public StaffOfHomaItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 14, -2.6F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("homa_skill_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.literal("§c§l✦ Báculo de Homa en enfriamiento: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("homa_skill_cd", gameTime + 280L); // 14s cooldown

            // Sacrifica 15% de salud actual para activar Mariposa del Averno
            float currentHp = sp.getHealth();
            float sacrifice = currentHp * 0.15F;
            if (currentHp > 4.0F) {
                sp.setHealth(Math.max(2.0F, currentHp - sacrifice));
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, true, true)); // Strength III
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, false, false));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1, false, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 0.8D, 1.0D, 0.8D, 0.08D);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 35, 0.6D, 0.8D, 0.6D, 0.1D);
                sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getEyeY(), sp.getZ(), 1, 0, 0, 0, 0);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.4F, 1.1F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.8F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 1.6F);
            }

            sp.displayClientMessage(Component.literal("§6§l✦ [BÁCULO DE HOMA] §c§l¡GUÍA DEL MÁS ALLÁ ACTIVADA! §e(+45% ATQ)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Pasiva: Mariposa del Averno (si la salud está bajo el 50%, inflige daño ígneo adicional masivo)
            float hpRatio = player.getHealth() / player.getMaxHealth();
            if (hpRatio <= 0.50F) {
                target.setRemainingFireTicks(120);
                target.hurt(attacker.damageSources().inFire(), 8.0F);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.0D, target.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.05D);
                }
            }
            // Roba 3% de vida al impactar
            player.heal(player.getMaxHealth() * 0.03F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Arma de Asta Legendaria 5★]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7El legendario cetro de Hu Tao utilizado para purificar las almas.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§c▸ Habilidad: Guía del Más Allá (Clic derecho / ~)").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  §7Sacrifica 15% HP para desatar Fuerza III y llamas fantasmales por 12s.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§6▸ Pasiva: Mariposa del Averno").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  §7Al tener menos del 50% HP, los golpes incineran e infligen daño ígneo extra.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("  §7Restaura 3% de Vida Máxima en cada impacto.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
