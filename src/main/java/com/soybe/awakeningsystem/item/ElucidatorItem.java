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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;

import java.util.List;

/**
 * Elucidator (Espada de la Noche Negra): Espada mítica de Kirito (Sword Art Online).
 * Habilidad Activa: Starburst Stream (Ráfaga de la Estrella Destellante).
 */
public class ElucidatorItem extends SwordItem implements AwakeningWeaponItem {

    public ElucidatorItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(12000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 15, -2.4F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            long gameTime = level.getGameTime();

            int lvl = WeaponLevelManager.getLevel(stack);
            int baseCd = (lvl >= 50) ? 320 : 500;

            sp.getCooldowns().addCooldown(this, baseCd);

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 2, false, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, false, false, true));

            Vec3 look = sp.getLookAngle();

            // Swing anim & particles
            sp.swing(hand, true);
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLAW.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.08D);
            sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 1.0D, 0.8D, 1.0D, 0.15D);

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.8F, 0.9F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.5F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0F, 1.4F);

            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 32.0D, 0.65F, 10);

            float baseDmg = (float) (22.0D * WeaponLevelManager.getDamageMultiplier(stack));

            SlashWaveEntity slash1 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 0.75F, 2.6F, 1.3F, -38.0F, 22, 4);
            slash1.setSlashStyle(SlashWaveEntity.STYLE_SHADOW_CLAW);
            slash1.setDeltaMovement(look.scale(1.9D));
            sl.addFreshEntity(slash1);

            SlashWaveEntity slash2 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 0.75F, 2.6F, 1.3F, 38.0F, 22, 4);
            slash2.setSlashStyle(SlashWaveEntity.STYLE_SHADOW_CLAW);
            slash2.setDeltaMovement(look.scale(1.85D).add(look.y * 0.05D, 0.05D, 0));
            sl.addFreshEntity(slash2);

            SlashWaveEntity slash3 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 0.9F, 3.2F, 1.2F, 0.0F, 24, 6);
            slash3.setSlashStyle(SlashWaveEntity.STYLE_SHADOW_CLAW);
            slash3.setDeltaMovement(look.scale(2.1D));
            sl.addFreshEntity(slash3);

            SlashWaveEntity slashCross = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 1.6F, 4.2F, 4.2F, 0.0F, 28, 8);
            slashCross.setSlashStyle(SlashWaveEntity.STYLE_CROSS);
            slashCross.setCross(true);
            slashCross.setDeltaMovement(look.scale(2.35D));
            sl.addFreshEntity(slashCross);

            WeaponLevelManager.addExp(stack, 40, sp);

            sp.displayClientMessage(Component.translatable("item.awakening_system.elucidator.starburst_stream"), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLAW.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.4F, 0.9F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 16.0D, 0.25F, 4);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
