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
import net.minecraft.world.entity.Entity;
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
 * Dark Repulser (Espada Ciclón Esmeralda): Espada cristalina de Kirito.
 * Habilidad Activa: Ciclón Esmeralda (Emerald Cyclone).
 * Sinergia Pasiva: Maestría de Doble Empuñadura si se porta junto a Elucidator.
 */
public class DarkRepulserItem extends SwordItem implements AwakeningWeaponItem {

    public DarkRepulserItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(12000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 14, -2.2F)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof ServerPlayer sp && (isSelected || slotId == 40)) {
            ItemStack main = sp.getMainHandItem();
            ItemStack off = sp.getOffhandItem();
            boolean hasDualBlades = (main.getItem() instanceof ElucidatorItem && off.getItem() instanceof DarkRepulserItem)
                    || (main.getItem() instanceof DarkRepulserItem && off.getItem() instanceof ElucidatorItem);

            if (hasDualBlades && sp.tickCount % 40 == 0) {
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false, false));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, true, false, false));
                if (level instanceof ServerLevel sl && sl.random.nextFloat() < 0.35F) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 4, 0.4D, 0.5D, 0.4D, 0.05D);
                }
            }
        }
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
            int baseCd = (lvl >= 50) ? 260 : 400;
            sp.getCooldowns().addCooldown(this, baseCd);

            ItemStack main = sp.getMainHandItem();
            ItemStack off = sp.getOffhandItem();
            boolean hasDualBlades = (main.getItem() instanceof ElucidatorItem && off.getItem() instanceof DarkRepulserItem)
                    || (main.getItem() instanceof DarkRepulserItem && off.getItem() instanceof ElucidatorItem);

            Vec3 look = sp.getLookAngle();
            float baseDmg = (float) (20.0D * WeaponLevelManager.getDamageMultiplier(stack));

            if (hasDualBlades) {
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.DOMAIN_OF_SANCTION_SLASH.get(), SoundSource.PLAYERS, 1.9F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.3F, 1.6F);

                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLAW.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 40, 1.2D, 0.8D, 1.2D, 0.15D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 1.2D, 0.8D, 1.2D, 0.1D);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 36.0D, 0.80F, 14);

                AABB pullBox = sp.getBoundingBox().inflate(8.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, pullBox,
                        e -> e != sp && e.isAlive() && !(e instanceof Player && !sp.canHarmPlayer((Player) e)))) {
                    Vec3 toPlayer = sp.position().subtract(nearby.position()).normalize().scale(0.45D);
                    nearby.setDeltaMovement(nearby.getDeltaMovement().add(toPlayer.x, 0.2D, toPlayer.z));
                    nearby.hurtMarked = true;
                }

                SlashWaveEntity dualCrystal = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, SlashWaveEntity.STYLE_CELESTIAL, baseDmg * 1.3F, 4.2F, 1.5F, -30.0F, 26, 8);
                dualCrystal.setDeltaMovement(look.scale(2.25D));
                sl.addFreshEntity(dualCrystal);

                SlashWaveEntity dualVoid = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, SlashWaveEntity.STYLE_SHADOW_CLAW, baseDmg * 1.4F, 4.2F, 1.5F, 30.0F, 26, 8);
                dualVoid.setDeltaMovement(look.scale(2.25D));
                sl.addFreshEntity(dualVoid);

                SlashWaveEntity dualCross = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, SlashWaveEntity.STYLE_CROSS, baseDmg * 1.8F, 4.8F, 4.8F, 0.0F, 30, 10);
                dualCross.setCross(true);
                dualCross.setDeltaMovement(look.scale(2.45D));
                sl.addFreshEntity(dualCross);

                sp.swing(hand, true);
                sp.heal(10.0F);
                WeaponLevelManager.addExp(stack, 50, sp);

                sp.displayClientMessage(Component.translatable("item.awakening_system.dark_repulser.dual_eclipse"), true);

            } else {
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.8F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.6F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 1.6F);

                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 1.0D, 0.6D, 1.0D, 0.08D);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.55F, 8);

                SlashWaveEntity wave1 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, SlashWaveEntity.STYLE_CELESTIAL, baseDmg * 1.1F, 4.0F, 1.4F, 0.0F, 24, 6);
                wave1.setDeltaMovement(look.scale(2.15D));
                sl.addFreshEntity(wave1);

                SlashWaveEntity wave2 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, SlashWaveEntity.STYLE_CELESTIAL, baseDmg * 0.9F, 3.2F, 2.0F, 90.0F, 22, 6);
                wave2.setDeltaMovement(look.scale(2.05D));
                sl.addFreshEntity(wave2);

                sp.swing(hand, true);
                sp.heal(6.0F);
                WeaponLevelManager.addExp(stack, 35, sp);

                sp.displayClientMessage(Component.translatable("item.awakening_system.dark_repulser.crystal_resonance"), true);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.3F, 1.4F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 16.0D, 0.25F, 4);

            sl.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
