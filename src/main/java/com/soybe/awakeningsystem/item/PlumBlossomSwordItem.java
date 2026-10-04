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

import java.util.List;

public class PlumBlossomSwordItem extends SwordItem implements AwakeningWeaponItem {
    public PlumBlossomSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 10, -2.4F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("plum_blossom_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.literal("§c⏳ Flor de Ciruelo en recarga (" + sec + "s)..."), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("plum_blossom_cd", gameTime + 200L); // 10s cooldown
            sp.getCooldowns().addCooldown(this, 200);

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                // 1. Sello sagrado de la Secta del Monte Hua en el suelo (Círculo de Qi de Murim)
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                                (float) look.x, (float) look.y, (float) look.z,
                                3.8F, 40, 0xFFB7C5, 2, false // circleType = 2 (Ground Murim)
                        )
                );

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CHERRY_WOOD_BREAK, SoundSource.PLAYERS, 1.5F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.6F);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 30.0D, 0.55F, 9);

                float dmg = (float) (24.0D * WeaponLevelManager.getDamageMultiplier(stack));

                // 2. Danza de los 24 Pétalos del Monte Hua (Cuchillas Físicas de Qi Floral)
                // Tajo Horizontal Amplio
                SlashWaveEntity waveH = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CHERRY_BLOSSOM, dmg * 1.1F, 4.2F, 1.4F, 0.0F, 24, 8);
                waveH.setDeltaMovement(look.scale(2.1D));
                sl.addFreshEntity(waveH);

                // Tajo Diagonal Izquierdo
                SlashWaveEntity waveD1 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CHERRY_BLOSSOM, dmg * 0.85F, 3.2F, 1.2F, -40.0F, 22, 6);
                waveD1.setDeltaMovement(look.scale(1.95D));
                sl.addFreshEntity(waveD1);

                // Tajo Diagonal Derecho
                SlashWaveEntity waveD2 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CHERRY_BLOSSOM, dmg * 0.85F, 3.2F, 1.2F, 40.0F, 22, 6);
                waveD2.setDeltaMovement(look.scale(1.95D));
                sl.addFreshEntity(waveD2);

                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true));
                sp.displayClientMessage(Component.literal("§d§l✦ [DANZA DE LOS 24 PÉTALOS] §f¡Cuchillas de Qi de Flor de Ciruelo desatadas!"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);

            // Sistema de Combo de Pétalos de Qi (1 a 5)
            int combo = attacker.getPersistentData().getInt("plum_blossom_combo") + 1;
            if (combo >= 5) {
                attacker.getPersistentData().putInt("plum_blossom_combo", 0);

                // ¡FLORECIMIENTO TOTAL DEL MONTE HUA!
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY() + 1.0D, target.getZ(), 80, 1.2D, 1.0D, 1.2D, 0.2D);
                sl.sendParticles(ParticleTypes.FIREWORK, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.8D, 0.8D, 0.8D, 0.1D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 5, 0.6D, 0.6D, 0.6D, 0.0D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CHERRY_WOOD_BREAK, SoundSource.PLAYERS, 1.4F, 1.4F);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 1.5F);

                if (attacker instanceof Player p) {
                    target.hurt(target.damageSources().playerAttack(p), 15.0F);
                    AABB splash = target.getBoundingBox().inflate(4.0D, 1.5D, 4.0D);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, splash)) {
                        if (nearby != attacker && nearby != target && (nearby instanceof Enemy || !nearby.getTags().contains("shadow_soldier"))) {
                            nearby.hurt(nearby.damageSources().playerAttack(p), 8.0F);
                        }
                    }
                    p.displayClientMessage(Component.translatable("awakening_system.item.plum_blossom.bloom"), true);
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, true));
                }
            } else {
                attacker.getPersistentData().putInt("plum_blossom_combo", combo);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY() + 1.0D, target.getZ(), 15 + combo * 6, 0.4D, 0.4D, 0.4D, 0.08D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CHERRY_LEAVES_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F + (combo * 0.15F));
                if (attacker instanceof Player p) {
                    p.displayClientMessage(Component.translatable("awakening_system.item.plum_blossom.petals", combo), true);
                }
            }
        }
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.plum_blossom_sword.lore"));
        tooltip.add(Component.translatable("item.awakening_system.plum_blossom_sword.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.plum_blossom_sword.passive"));
        tooltip.add(Component.translatable("item.awakening_system.plum_blossom_sword.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
