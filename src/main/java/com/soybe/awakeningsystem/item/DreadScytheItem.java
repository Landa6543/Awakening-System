package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SpawnMagicCirclePayload;
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

public class DreadScytheItem extends SwordItem implements AwakeningWeaponItem {

    public DreadScytheItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(45000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 25, -2.8F))); // 29 Daño Total, 1.2 Velocidad de Ataque
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

            long cdUntil = sp.getPersistentData().getLong("dread_scythe_skill_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.literal("§5§l☠ [Vórtice del Terror] en enfriamiento: §f" + sec + "s"), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("dread_scythe_skill_cd", gameTime + 600L); // 30s cd
            sp.getCooldowns().addCooldown(this, 600);

            // Círculo Mágico Espectral en suelo (Amatista / Púrpura Sombrío)
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp, new SpawnMagicCirclePayload(
                    sp.getId(),
                    sp.getX(), sp.getY() + 0.05D, sp.getZ(),
                    0.0F, 1.0F, 0.0F,
                    12.0F, 50, 0xFF8A2BE2, 0, false
            ));

            // Sonidos de Terror y Almas
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.5F, 1.4F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 2.0F, 0.8F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 1.5F);

            // Partículas de Almas y Vórtice
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 100, 5.0D, 1.5D, 5.0D, 0.15D);
            sl.sendParticles(ParticleTypes.SOUL, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 60, 4.0D, 1.5D, 4.0D, 0.1D);
            sl.sendParticles(ParticleTypes.WITCH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 5.0D, 1.5D, 5.0D, 0.2D);
            sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 120, 6.0D, 2.0D, 6.0D, 0.3D);

            // Radio de atracción y daño necrótico moderado
            AABB area = sp.getBoundingBox().inflate(12.0D, 4.0D, 12.0D);
            int hitCount = 0;
            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, area)) {
                if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                    nearby.hurt(nearby.damageSources().playerAttack(sp), 58.0F); // 58 de daño

                    // Atracción gravitatoria hacia el centro del portador
                    Vec3 pull = sp.position().subtract(nearby.position()).normalize().scale(1.1D);
                    nearby.setDeltaMovement(pull.x, 0.5D, pull.z);

                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2)); // Slowness III
                    nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1)); // Wither II
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1)); // Weakness II
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, nearby.getX(), nearby.getY() + 1.0D, nearby.getZ(), 1, 0, 0, 0, 0);
                    hitCount++;
                }
            }

            // Bendición de Robo de Almas al Portador
            sp.heal(6.0F + (hitCount * 1.5F));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 2, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true));

            com.soybe.awakeningsystem.SystemData sData = com.soybe.awakeningsystem.SystemEvents.data(sp);
            if (sData != null) {
                sData.restoreEnergy(50);
            }

            sp.displayClientMessage(Component.literal("§5§l☠ [VÓRTICE DEL TERROR] §d¡Cosechaste las almas de " + hitCount + " enemigos! (+Salud restaurada)"), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            if (attacker instanceof Player player) {
                // Pasiva: "Cosecha del Terror" (+12 Daño Verdadero de Sombra & Robo de Vida)
                target.hurt(target.damageSources().playerAttack(player), 12.0F);
                player.heal(2.0F); // Roba 1 corazón de vida

                // Aplicar Wither y Ceguera breve
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));

                // Barrido de hoz espectral
                AABB sweepArea = target.getBoundingBox().inflate(5.0D, 1.5D, 5.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, sweepArea)) {
                    if (nearby != target && nearby != attacker && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(player), 11.0F);
                        nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                    }
                }

                // Si el objetivo muere por el golpe
                if (!target.isAlive() || target.getHealth() <= 0) {
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.8F, 1.2F);
                    sl.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.8D, 1.0D, 0.8D, 0.1D);
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 1, false, true)); // Fuerza II (7s)
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 1, false, true)); // Velocidad II (7s)
                    player.displayClientMessage(Component.literal("§5§l✦ ¡ALMA COSECHADA! §dEl terror potencia tu fuerza y velocidad."), true);
                }
            }

            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.6F);
            sl.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.4D, 0.5D, 0.4D, 0.1D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.08D);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5§l✦ ARMA ARCANO DE LAS SOMBRAS // RANGO MONARCA ✦").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§d§lGuadaña del Caballero del Terror").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Forjada con obsidiana abisal y los restos espectrales de un antiguo soberano."));
        tooltip.add(Component.literal("§8Su filo emite un frío mortal que hiela el alma de quienes lo contemplan."));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§5§l[ATRIBUTOS DEL TERROR]"));
        tooltip.add(Component.literal("§c▪ Daño Base: §f29 §8(+12 Daño de Sombra)"));
        tooltip.add(Component.literal("§e▪ Velocidad de Ataque: §f1.2 §8(Hoz Rápida)"));
        tooltip.add(Component.literal("§b▪ Durabilidad: §a45,000 §8(Inmune al Fuego)"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§d§l[PASIVA: COSECHA DE ALMAS]"));
        tooltip.add(Component.literal("§7▪ Cada impacto causa §d+12 de daño de sombra §7e inflige §8Wither II§7."));
        tooltip.add(Component.literal("§7▪ Drena la vitalidad del objetivo, curando al portador §c+1 Corazón§7."));
        tooltip.add(Component.literal("§7▪ Matar a un enemigo libera su alma, otorgando §6Fuerza II §7y §bVelocidad II§7."));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§5§l[HABILIDAD ACTIVA: VÓRTICE DEL TERROR] §8(Clic Derecho)"));
        tooltip.add(Component.literal("§7▪ Despliega un remolino de almas y runas amatistas en un radio de 12 bloques."));
        tooltip.add(Component.literal("§7▪ Atrae a los enemigos y les inflige §c58 de daño necrótico§7."));
        tooltip.add(Component.literal("§7▪ Absorbe vida por cada víctima y otorga §dRegeneración II§7."));
        tooltip.add(Component.literal("§8▪ Enfriamiento: §f30s."));
    }
}
