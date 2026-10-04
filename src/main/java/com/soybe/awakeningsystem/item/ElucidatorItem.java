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
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 15, -2.4F))); // 19 Daño, 1.6 Velocidad
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
            int baseCd = (lvl >= 50) ? 320 : 500; // 16s o 25s

            sp.getCooldowns().addCooldown(this, baseCd);

            // Efectos de buf al usuario: Resistencia al empuje y celeridad de ataque
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 2, false, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, false, false, true));

            Vec3 look = sp.getLookAngle();

            // Invocar sello arcano oscuro en el suelo a los pies del espadachín
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                    sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                    new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                            sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                            (float) look.x, (float) look.y, (float) look.z,
                            3.4F, 45, 0x1A0933, 0, false
                    )
            );

            // Audio sónico marcial
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 1.3F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.5F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0F, 1.4F);

            // Sacudida cinemática de cámara
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 32.0D, 0.65F, 10);

            float baseDmg = (float) (22.0D * WeaponLevelManager.getDamageMultiplier(stack));

            // Ráfaga Física de Tajos de Vacío (Starburst Stream V2)
            // Tajo 1: Diagonal Izquierdo
            SlashWaveEntity slash1 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 0.75F, 2.6F, 1.3F, -38.0F, 22, 4);
            slash1.setDeltaMovement(look.scale(1.9D));
            sl.addFreshEntity(slash1);

            // Tajo 2: Diagonal Derecho
            SlashWaveEntity slash2 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 0.75F, 2.6F, 1.3F, 38.0F, 22, 4);
            slash2.setDeltaMovement(look.scale(1.85D).add(look.y * 0.05D, 0.05D, 0));
            sl.addFreshEntity(slash2);

            // Tajo 3: Horizontal Rápido de Corte
            SlashWaveEntity slash3 = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 0.9F, 3.2F, 1.2F, 0.0F, 24, 6);
            slash3.setDeltaMovement(look.scale(2.1D));
            sl.addFreshEntity(slash3);

            // Tajo 4 (FINALIZADOR 16º GOLPE): Titánica 'X' Cruzada de Fuego Oscuro
            SlashWaveEntity slashCross = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, baseDmg * 1.6F, 4.2F, 4.2F, 0.0F, 28, 8);
            slashCross.setCross(true);
            slashCross.setDeltaMovement(look.scale(2.35D));
            sl.addFreshEntity(slashCross);

            WeaponLevelManager.addExp(stack, 40, sp);

            sp.displayClientMessage(Component.literal("§9§l✦ [STARBURST STREAM V2] §f¡Flurry de 16 cortes de vacío y Cruz Titánica desatados!"), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
