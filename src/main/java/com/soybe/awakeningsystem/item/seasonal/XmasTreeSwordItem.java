package com.soybe.awakeningsystem.item.seasonal;

import com.soybe.awakeningsystem.item.AwakeningWeaponItem;
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
import net.minecraft.world.phys.AABB;

import java.util.List;

public class XmasTreeSwordItem extends SwordItem implements AwakeningWeaponItem {

    public XmasTreeSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .durability(2500)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 7, -2.4F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Snowflake burst
            sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0D, target.getZ(),
                    20, 0.35D, 0.35D, 0.35D, 0.05D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.3F);

            // Frostbite & freeze ticks
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 80, target.getTicksFrozen() + 100));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isChristmasActive()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("§c§l[ESPÍRITU INVERNAL EN LETARGO] §7El Vórtice Invernal de la espada solo puede desatarse durante la temporada navideña (1 Dic - 7 Ene)."), true);
            }
            return InteractionResultHolder.fail(held);
        }

        if (!player.getCooldowns().isOnCooldown(this)) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel sl) {
                // Active Skill: Blizzard Whirlwind
                double px = player.getX();
                double py = player.getY();
                double pz = player.getZ();

                // Sound & Blizzard particle storm
                sl.playSound(null, px, py, pz, SoundEvents.SNOW_GOLEM_HURT, SoundSource.PLAYERS, 1.2F, 0.8F);
                sl.playSound(null, px, py, pz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.4F);

                for (int i = 0; i < 40; i++) {
                    double angle = (i / 40.0D) * Math.PI * 2.0D;
                    double dist = 2.0D + sl.random.nextDouble() * 3.0D;
                    double ox = Math.cos(angle) * dist;
                    double oz = Math.sin(angle) * dist;
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, px + ox, py + 0.5D + sl.random.nextDouble(), pz + oz,
                            2, 0.1D, 0.1D, 0.1D, 0.02D);
                }

                // Affect nearby enemies in 5 block radius
                AABB aabb = new AABB(px - 5.0D, py - 2.0D, pz - 5.0D, px + 5.0D, py + 3.0D, pz + 5.0D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != player && e.isAlive());

                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sl.damageSources().playerAttack(serverPlayer), 15.0F);
                    enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3));
                    enemy.setTicksFrozen(Math.min(enemy.getTicksRequiredToFreeze() + 140, enemy.getTicksFrozen() + 160));

                    // Knockback push away from center
                    double dx = enemy.getX() - px;
                    double dz = enemy.getZ() - pz;
                    double mag = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                    enemy.setDeltaMovement(enemy.getDeltaMovement().add((dx / mag) * 0.7D, 0.35D, (dz / mag) * 0.7D));
                    enemy.hurtMarked = true;
                }

                serverPlayer.sendSystemMessage(Component.literal("§b§l[VÓRTICE INVERNAL] §f¡Tormenta de nieve desatada congelando a " + enemies.size() + " objetivos!")
                        .withStyle(ChatFormatting.AQUA));

                // 15 seconds cooldown
                player.getCooldowns().addCooldown(this, 300);
            }

            return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§b§l[ESPADA DE NAVIDAD: ÁRBOL FESTIVO]"));
        tooltip.add(Component.literal("§7Forjada con ramas de abeto ancestral, escarcha eterna y luces festivas."));
        tooltip.add(Component.literal("§9Pasiva: §fLos tajos congelan y ralentizan al objetivo."));
        tooltip.add(Component.literal("§bClic Derecho: §fVórtice Invernal (Tormenta gélida en área de 5 bloques)."));
        if (!com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isChristmasActive()) {
            tooltip.add(Component.literal("§c§l[PODER EN LETARGO FUERA DE TEMPORADA]"));
            tooltip.add(Component.literal("§7Habilidad activa solo disponible del 1 de Diciembre al 7 de Enero."));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
