package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.entity.AstralDragonCrescentEntity;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AstralGuandaoItem extends SwordItem implements AwakeningWeaponItem {

    public AstralGuandaoItem(Properties properties) {
        super(CustomToolTiers.ASTRAL, properties
                .fireResistant()
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(CustomToolTiers.ASTRAL, 4, -2.8F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
            target.setRemainingFireTicks(60);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cd = sp.getPersistentData().getLong("astral_guandao_cd");
            if (gameTime < cd) {
                long rem = (cd - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.guandao.cooldown", rem), true);
                return InteractionResultHolder.fail(stack);
            }
            sp.getPersistentData().putLong("astral_guandao_cd", gameTime + 80L); // 4s cooldown
            sp.getCooldowns().addCooldown(this, 80);

            Vec3 look = sp.getLookAngle();

            // 1. Sello vertical del Dragón Astral en el aire
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                    (ServerLevel) level, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                    new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                            sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                            (float) look.x, (float) look.y, (float) look.z,
                            3.5F, 30, 0x9933FF, 3, false
                    )
            );

            // 2. Creciente Astral de Dragón Celestial (SlashWaveEntity Cósmica)
            float dmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));
            SlashWaveEntity crescent = new SlashWaveEntity(level, sp, SlashWaveEntity.TYPE_COSMIC_AMETHYST, dmg, 4.8F, 1.8F, 0.0F, 26, 10);
            crescent.setDeltaMovement(look.scale(2.2D));
            level.addFreshEntity(crescent);

            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby((ServerLevel) level, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.60F, 9);

            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 0.7F);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.9F, 1.7F);

            sp.swing(hand, true);
            WeaponLevelManager.addExp(stack, 30, sp);

            sp.displayClientMessage(Component.literal("§d§l✦ [CRECIENTE DRACÓNICO] §5¡Corte cósmico del Dragón Celestial desatado!"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.astral_guandao.lore"));
        tooltip.add(Component.translatable("item.awakening_system.astral_guandao.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.astral_guandao.passive"));
        tooltip.add(Component.translatable("item.awakening_system.astral_guandao.active_title"));
        tooltip.add(Component.translatable("item.awakening_system.astral_guandao.active_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.astral_guandao.active_desc2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
