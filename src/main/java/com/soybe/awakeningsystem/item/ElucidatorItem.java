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
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2, false, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false, true));

            // Starburst Stream: Ráfaga en cono frontal
            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.position().add(0, sp.getEyeHeight() * 0.5D, 0);

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.2F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0F, 1.4F);

            // Partículas de estela oscura y fuego de almas
            for (int i = 0; i < 20; i++) {
                double px = pos.x + look.x * (1.5D + i * 0.25D) + (sl.random.nextDouble() - 0.5D) * 1.5D;
                double py = pos.y + (sl.random.nextDouble() - 0.5D) * 1.2D;
                double pz = pos.z + look.z * (1.5D + i * 0.25D) + (sl.random.nextDouble() - 0.5D) * 1.5D;
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 3, 0.1D, 0.1D, 0.1D, 0.05D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            AABB box = sp.getBoundingBox().inflate(4.5D).move(look.scale(2.0D));
            List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != sp && e.isAlive() && !(e instanceof Player && !sp.canHarmPlayer((Player) e)));

            float dmg = (float) (24.0D * WeaponLevelManager.getDamageMultiplier(stack));
            for (LivingEntity target : targets) {
                target.hurt(sp.damageSources().playerAttack(sp), dmg);
                target.setDeltaMovement(target.getDeltaMovement().add(look.x * 0.4D, 0.25D, look.z * 0.4D));
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.1D);
            }

            WeaponLevelManager.addExp(stack, 40, sp);

            sp.displayClientMessage(Component.literal("§9§l✦ [STARBURST STREAM] §f¡Has desatado la ráfaga de 16 cortes con Elucidator!"), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
