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
import net.minecraft.world.phys.Vec3;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;

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
                sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.staff_of_homa.cd", "§c§l✦ Staff of Homa on cooldown: §f%ss", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("homa_skill_cd", gameTime + 280L);

            float currentHp = sp.getHealth();
            float sacrifice = currentHp * 0.15F;
            if (currentHp > 4.0F) {
                sp.setHealth(Math.max(2.0F, currentHp - sacrifice));
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, false, false));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1, false, false, true));

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                                (float) look.x, (float) look.y, (float) look.z,
                                3.8F, 40, 0xFF4500, 0, false
                        )
                );

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 1.1F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.4F, 0.9F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.5F);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.60F, 10);

                float dmg = (float) (32.0D * WeaponLevelManager.getDamageMultiplier(stack));

                SlashWaveEntity pyroSlash = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_PYRO_FLAME, dmg, 4.6F, 1.6F, 0.0F, 26, 10);
                pyroSlash.setDeltaMovement(look.scale(2.3D));
                sl.addFreshEntity(pyroSlash);

                WeaponLevelManager.addExp(stack, 35, sp);
            }

            sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.staff_of_homa.msg", "§6§l✦ [STAFF OF HOMA] §c§lNETHERWORLD BUTTERFLY UNLEASHED! §e(Spectral flame slash +45% ATK)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float hpRatio = player.getHealth() / player.getMaxHealth();
            if (hpRatio <= 0.50F) {
                target.setRemainingFireTicks(120);
                target.hurt(attacker.damageSources().inFire(), 8.0F);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.0D, target.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.05D);
                }
            }
            player.heal(player.getMaxHealth() * 0.03F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.staff_of_homa.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.staff_of_homa.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.staff_of_homa.active.title").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.staff_of_homa.active.desc").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.staff_of_homa.passive.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.staff_of_homa.passive.desc1").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.staff_of_homa.passive.desc2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
