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
                // Vórtice giratorio de 360 grados de pétalos de ciruelo en espiral
                for (double r = 1.0; r <= 5.5; r += 0.9D) {
                    int count = (int) (r * 12);
                    for (int i = 0; i < count; i++) {
                        double angle = (2.0 * Math.PI * i / count) + (r * 0.4D);
                        double px = sp.getX() + r * Math.cos(angle);
                        double pz = sp.getZ() + r * Math.sin(angle);
                        sl.sendParticles(ParticleTypes.CHERRY_LEAVES, px, sp.getY() + 0.1D + (r * 0.25D), pz, 1, 0, 0.04D, 0, 0.02D);
                    }
                }

                // Efecto de vacío y corte masivo a los enemigos en el área
                AABB area = sp.getBoundingBox().inflate(6.5D, 3.0D, 6.5D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (nearby != sp && (nearby instanceof Enemy || (nearby.isAlive() && !nearby.getTags().contains("shadow_soldier")))) {
                        Vec3 pull = sp.position().subtract(nearby.position()).normalize().scale(0.38D);
                        nearby.setDeltaMovement(nearby.getDeltaMovement().add(pull.x, 0.15D, pull.z));
                        nearby.hurtMarked = true;
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 20.0F);
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    }
                }

                // Explosión floral culminante
                sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 40, 1.0D, 0.8D, 1.0D, 0.15D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 8, 1.0D, 0.5D, 1.0D, 0.0D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CHERRY_WOOD_BREAK, SoundSource.PLAYERS, 1.4F, 1.6F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.4F, 0.8F);

                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 1, false, true));
                sp.displayClientMessage(Component.translatable("awakening_system.item.plum_blossom.whirlwind"), true);
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
