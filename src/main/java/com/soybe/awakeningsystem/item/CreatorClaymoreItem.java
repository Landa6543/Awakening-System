package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.SpawnMagicCirclePayload;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class CreatorClaymoreItem extends SwordItem implements AwakeningWeaponItem {

    public CreatorClaymoreItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(60000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 48, -2.8F)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
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

            long cdUntil = sp.getPersistentData().getLong("creator_claymore_skill_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("item.awakening_system.creator_claymore.cd", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("creator_claymore_skill_cd", gameTime + 600L);
            sp.getCooldowns().addCooldown(this, 600);

            Vec3 look = sp.getLookAngle();
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp, new SpawnMagicCirclePayload(
                    sp.getId(),
                    sp.getX(), sp.getY() + 0.05D, sp.getZ(),
                    0.0F, 1.0F, 0.0F,
                    18.0F, 70, 0xFF9D4EDD, 0, false
            ));
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp, new SpawnMagicCirclePayload(
                    sp.getId(),
                    sp.getX() + look.x * 4.0D, sp.getEyeY() + look.y * 4.0D, sp.getZ() + look.z * 4.0D,
                    (float) look.x, (float) look.y, (float) look.z,
                    14.0F, 70, 0xFFE0AAFF, 1, false
            ));

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.2F, 0.8F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.2F, 0.6F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0F, 1.1F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.8F, 1.3F);

            sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 12, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 220, 9.0D, 2.5D, 9.0D, 0.25D);
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 140, 8.0D, 2.0D, 8.0D, 0.18D);

            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 48.0D, 1.0F, 18);

            SlashWaveEntity genesisSlash = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_COSMIC_AMETHYST, 120.0F, 6.4F, 2.8F, 0.0F, 36, 20);
            genesisSlash.setDeltaMovement(look.scale(2.4D));
            sl.addFreshEntity(genesisSlash);

            AABB cataclysmArea = sp.getBoundingBox().inflate(18.0D, 6.0D, 18.0D);
            int hitCount = 0;
            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cataclysmArea)) {
                if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                    nearby.hurt(nearby.damageSources().playerAttack(sp), 160.0F);

                    Vec3 dir = sp.position().subtract(nearby.position()).normalize().scale(1.0D);
                    nearby.setDeltaMovement(dir.x, 1.6D, dir.z);

                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 4));
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 3));
                    nearby.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, nearby.getX(), nearby.getY() + 1.0D, nearby.getZ(), 1, 0, 0, 0, 0);
                    hitCount++;
                }
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 4, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 4, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 3, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 3, false, true));

            com.soybe.awakeningsystem.SystemData sData = com.soybe.awakeningsystem.SystemEvents.data(sp);
            if (sData != null) {
                sData.restoreEnergy(120);
                sData.addCultivationQi(60);
            }

            WeaponLevelManager.addExp(stack, 50, sp);

            sp.displayClientMessage(Component.translatable("item.awakening_system.creator_claymore.msg", hitCount), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            if (attacker instanceof Player player) {
                target.hurt(target.damageSources().playerAttack(player), 36.0F);

                AABB sweepArea = target.getBoundingBox().inflate(8.0D, 2.5D, 8.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, sweepArea)) {
                    if (nearby != target && nearby != attacker && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(player), 30.0F);
                        nearby.knockback(1.2D, attacker.getX() - nearby.getX(), attacker.getZ() - nearby.getZ());
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, nearby.getX(), nearby.getY() + 1.0D, nearby.getZ(), 1, 0, 0, 0, 0);
                    }
                }

                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 3));

                if (target.getHealth() < target.getMaxHealth() * 0.35F && target.isAlive()) {
                    target.hurt(target.damageSources().playerAttack(player), 9999.0F);

                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.0F, 1.1F);
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.5F);
                    sl.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 6, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, target.getX(), target.getY() + 1.0D, target.getZ(), 100, 1.5D, 1.8D, 1.5D, 0.3D);
                    sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 70, 1.2D, 1.5D, 1.2D, 0.2D);

                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 4, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, true));

                    com.soybe.awakeningsystem.SystemData sData = com.soybe.awakeningsystem.SystemEvents.data(player);
                    if (sData != null) {
                        sData.restoreEnergy(100);
                        sData.addCultivationQi(40);
                    }

                    player.displayClientMessage(Component.translatable("item.awakening_system.creator_claymore.exec_msg"), true);
                }
            }

            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.6F, 1.4F);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5F, 0.85F);
            sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.5D, 0.7D, 0.5D, 0.1D);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.15D);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.header").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.desc2"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.attr_header"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.attr_dmg"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.attr_speed"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.attr_dura"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.passive1_header"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.passive1_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.passive1_desc2"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.passive2_header"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.passive2_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.passive2_desc2"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.passive2_desc3"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.active_header"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.active_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.active_desc2"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.active_desc3"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.active_desc4"));
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.active_cd"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.creator_claymore.jackpot"));
    }
}
