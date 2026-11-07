package com.soybe.awakeningsystem.item;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;

import java.util.List;

/**
 * Lambent Light (Luz Radiante / Rapier de Asuna): Estoque supremo de Sword Art Online.
 * Habilidad Activa: Linear (Estocada de Destello Lineal).
 * Deslizamiento ultrarrápido y perforación de 50% de armadura.
 */
public class LambentLightItem extends SwordItem implements AwakeningWeaponItem {

    public LambentLightItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(12000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 13, -1.8F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;

            int lvl = WeaponLevelManager.getLevel(stack);
            int baseCd = (lvl >= 50) ? 240 : 360;
            sp.getCooldowns().addCooldown(this, baseCd);

            Vec3 look = sp.getLookAngle();
            Vec3 start = sp.position();

            sp.swing(hand, true);
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.1D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX() + look.x * 1.2D, sp.getY() + 1.1D, sp.getZ() + look.z * 1.2D, 20, 0.4D, 0.4D, 0.4D, 0.12D);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, sp.getX() + look.x * 1.2D, sp.getY() + 1.1D, sp.getZ() + look.z * 1.2D, 15, 0.3D, 0.3D, 0.3D, 0.08D);

            Vec3 dashVec = new Vec3(look.x * 2.2D, 0.22D, look.z * 2.2D);
            sp.setDeltaMovement(dashVec);
            sp.hurtMarked = true;

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.8F, 1.6F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.4F, 1.5F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5F, 1.6F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.3F, 1.7F);

            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.5F, 7);

            float dmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));

            SlashWaveEntity thrustBeam = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_SOLAR_LIGHT, dmg, 1.4F, 3.6F, 0.0F, 20, 10);
            thrustBeam.setSlashStyle(SlashWaveEntity.STYLE_THRUST);
            thrustBeam.setDeltaMovement(look.scale(2.8D));
            thrustBeam.setArmorPenetration(0.50F);
            sl.addFreshEntity(thrustBeam);

            WeaponLevelManager.addExp(stack, 35, sp);

            sp.displayClientMessage(Component.translatable("item.awakening_system.lambent_light.unleashed"), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.08D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.4F, 1.55F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 16.0D, 0.22F, 4);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
