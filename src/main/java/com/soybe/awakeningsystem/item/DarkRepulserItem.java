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
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 14, -2.2F))); // 18 Daño, 1.8 Velocidad
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof ServerPlayer sp && (isSelected || slotId == 40)) {
            // Chequeo de sinergia de doble empuñadura con Elucidator
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
            int baseCd = (lvl >= 50) ? 260 : 400; // 13s o 20s
            sp.getCooldowns().addCooldown(this, baseCd);

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.4F, 1.3F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.2F);

            // Ciclón de viento esmeralda en 360 grados
            for (int i = 0; i < 32; i++) {
                double angle = (i / 32.0D) * Math.PI * 2.0D;
                double px = sp.getX() + Math.cos(angle) * 3.5D;
                double pz = sp.getZ() + Math.sin(angle) * 3.5D;
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, sp.getY() + 0.5D, pz, 2, 0.1D, 0.2D, 0.1D, 0.05D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, px, sp.getY() + 0.8D, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            AABB box = sp.getBoundingBox().inflate(4.5D);
            List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != sp && e.isAlive() && !(e instanceof Player && !sp.canHarmPlayer((Player) e)));

            float dmg = (float) (20.0D * WeaponLevelManager.getDamageMultiplier(stack));
            float totalDamageDealt = 0.0F;

            for (LivingEntity target : targets) {
                target.hurt(sp.damageSources().playerAttack(sp), dmg);
                totalDamageDealt += dmg;
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
            }

            // Regeneración de vida del 15% del daño infligido
            if (totalDamageDealt > 0) {
                sp.heal(Math.min(8.0F, totalDamageDealt * 0.15F));
            }

            WeaponLevelManager.addExp(stack, 35, sp);

            sp.displayClientMessage(Component.literal("§a§l✦ [CICLÓN ESMERALDA] §f¡Corte giratorio de Dark Repulser desatado!"), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
