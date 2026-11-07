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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;
import com.soybe.awakeningsystem.CameraShakePayload;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class GiganticGreatswordItem extends SwordItem implements AwakeningWeaponItem {
    public GiganticGreatswordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 14, -3.15F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("greatsword_cross_impact_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.greatsword.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("greatsword_cross_impact_cd", gameTime + 280L);
            sp.getCooldowns().addCooldown(this, 280);

            if (sp.onGround()) {
                sp.setDeltaMovement(sp.getLookAngle().x * 0.8D, 0.65D, sp.getLookAngle().z * 0.8D);
            } else {
                sp.setDeltaMovement(sp.getLookAngle().x * 0.5D, -1.2D, sp.getLookAngle().z * 0.5D);
            }
            sp.hurtMarked = true;

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLEAVE.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.EXPLOSION, sp.getX(), sp.getY(), sp.getZ(), 5, 1.5D, 0.5D, 1.5D, 0.1D);
                sl.sendParticles(ParticleTypes.LAVA, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 35, 1.8D, 0.4D, 1.8D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.9F, 0.65F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 0.7F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5F, 0.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.8F, 0.6F);

                CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 38.0D, 0.90F, 15);

                float dmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));
                SlashWaveEntity seismicWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_PYRO_FLAME, SlashWaveEntity.STYLE_HEAVY_CLEAVE, dmg, 5.6F, 2.2F, 0.0F, 26, 12);
                seismicWave.setDeltaMovement(look.scale(2.1D));
                sl.addFreshEntity(seismicWave);

                sp.swing(hand, true);

                AABB box = sp.getBoundingBox().inflate(6.0D, 3.0D, 6.0D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), dmg * 0.8F);
                        nearby.setDeltaMovement(0.0D, 0.85D, 0.0D);
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    }
                }

                WeaponLevelManager.addExp(stack, 30, sp);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        if (level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                if (player.onGround()) {
                    player.setDeltaMovement(player.getLookAngle().x * 0.8D, 0.65D, player.getLookAngle().z * 0.8D);
                } else {
                    player.setDeltaMovement(player.getLookAngle().x * 0.5D, -1.2D, player.getLookAngle().z * 0.5D);
                }
                player.getCooldowns().addCooldown(this, 280);
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLEAVE.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.4F, 0.7F);
            CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 18.0D, 0.3F, 4);

            sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5D, target.getZ(), 1, 0.2D, 0.2D, 0.2D, 0.0D);
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.2D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.2F, 0.7F);

            AABB cleave = target.getBoundingBox().inflate(3.5D, 1.5D, 3.5D);
            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cleave)) {
                if (nearby != attacker && nearby != target && (nearby instanceof Enemy)) {
                    if (attacker instanceof Player p) {
                        nearby.hurt(nearby.damageSources().playerAttack(p), 8.0F);
                    }
                    nearby.knockback(0.7D, attacker.getX() - nearby.getX(), attacker.getZ() - nearby.getZ());
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.lore"));
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.passive"));
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
