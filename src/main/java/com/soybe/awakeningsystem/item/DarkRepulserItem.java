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
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                                (float) look.x, (float) look.y, (float) look.z,
                                4.2F, 50, 0x00FFCC, 0, false
                        )
                );
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                                (float) look.x, (float) look.y, (float) look.z,
                                3.2F, 40, 0x1A0933, 1, false
                        )
                );

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.6F);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 36.0D, 0.75F, 12);

                AABB pullBox = sp.getBoundingBox().inflate(8.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, pullBox,
                        e -> e != sp && e.isAlive() && !(e instanceof Player && !sp.canHarmPlayer((Player) e)))) {
                    Vec3 toPlayer = sp.position().subtract(nearby.position()).normalize().scale(0.45D);
                    nearby.setDeltaMovement(nearby.getDeltaMovement().add(toPlayer.x, 0.2D, toPlayer.z));
                    nearby.hurtMarked = true;
                }

                SlashWaveEntity dualCrystal = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, baseDmg * 1.3F, 3.8F, 1.4F, -30.0F, 26, 6);
                dualCrystal.setDeltaMovement(look.scale(2.2D));
                sl.addFreshEntity(dualCrystal);

                SlashWaveEntity dualVoid = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 1.4F, 3.8F, 1.4F, 30.0F, 26, 6);
                dualVoid.setDeltaMovement(look.scale(2.2D));
                sl.addFreshEntity(dualVoid);

                SlashWaveEntity dualCross = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, baseDmg * 1.8F, 4.6F, 4.6F, 0.0F, 30, 8);
                dualCross.setCross(true);
                dualCross.setDeltaMovement(look.scale(2.4D));
                sl.addFreshEntity(dualCross);

                sp.heal(10.0F);
                WeaponLevelManager.addExp(stack, 50, sp);

                sp.displayClientMessage(Component.translatable("item.awakening_system.dark_repulser.dual_eclipse"), true);

            } else {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                                (float) look.x, (float) look.y, (float) look.z,
                                3.0F, 35, 0x00FFCC, 0, false
                        )
                );

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.3F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.6F, 1.3F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.1F, 1.6F);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.5F, 8);

                SlashWaveEntity wave1 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, baseDmg * 1.1F, 3.6F, 1.3F, 0.0F, 24, 6);
                wave1.setDeltaMovement(look.scale(2.1D));
                sl.addFreshEntity(wave1);

                SlashWaveEntity wave2 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, baseDmg * 0.9F, 2.8F, 1.8F, 90.0F, 22, 4);
                wave2.setDeltaMovement(look.scale(2.0D));
                sl.addFreshEntity(wave2);

                sp.heal(6.0F);
                WeaponLevelManager.addExp(stack, 35, sp);

                sp.displayClientMessage(Component.translatable("item.awakening_system.dark_repulser.crystal_resonance"), true);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
