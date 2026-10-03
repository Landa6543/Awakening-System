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
 * Lambent Light (Luz Radiante / Rapier de Asuna): Estoque supremo de Sword Art Online.
 * Habilidad Activa: Linear (Estocada de Destello Lineal).
 * Deslizamiento ultrarrápido y perforación de 50% de armadura.
 */
public class LambentLightItem extends SwordItem implements AwakeningWeaponItem {

    public LambentLightItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(12000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 13, -1.8F))); // 17 Daño, 2.2 Velocidad de ataque
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
            int baseCd = (lvl >= 50) ? 240 : 360; // 12s o 18s
            sp.getCooldowns().addCooldown(this, baseCd);

            Vec3 look = sp.getLookAngle();
            Vec3 start = sp.position();

            // Propulsión instantánea hacia adelante (Dash veloz de 10 bloques)
            Vec3 dashVec = new Vec3(look.x * 2.2D, 0.2D, look.z * 2.2D);
            sp.setDeltaMovement(dashVec);
            sp.hurtMarked = true;

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5F, 1.5F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.2F, 1.6F);

            // Estela de luz blanca y destellos
            for (int i = 0; i < 25; i++) {
                double t = i * 0.4D;
                double px = start.x + look.x * t;
                double py = start.y + 0.8D + look.y * t;
                double pz = start.z + look.z * t;
                sl.sendParticles(ParticleTypes.END_ROD, px, py, pz, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                sl.sendParticles(ParticleTypes.FLASH, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            AABB box = sp.getBoundingBox().inflate(3.0D).expandTowards(look.scale(9.0D));
            List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != sp && e.isAlive() && !(e instanceof Player && !sp.canHarmPlayer((Player) e)));

            float dmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));

            for (LivingEntity target : targets) {
                target.hurt(sp.damageSources().playerAttack(sp), dmg);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true, true));
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.2D);
            }

            WeaponLevelManager.addExp(stack, 35, sp);

            sp.displayClientMessage(Component.literal("§b§l✦ [LINEAR] §f¡Estocada a la velocidad de la luz desatada con Lambent Light!"), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
