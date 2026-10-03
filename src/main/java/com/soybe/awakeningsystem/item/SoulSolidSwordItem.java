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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SoulSolidSwordItem extends SwordItem implements AwakeningWeaponItem {

    public SoulSolidSwordItem(Properties properties) {
        // Daño base elevado, velocidad de ataque alta de estoque (-1.6F => 2.4 golpes/segundo)
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -1.6F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Pasiva: Frío del Inframundo (Cold of the Underworld)
        target.setTicksFrozen(200); // Efecto de congelamiento de nieve en polvo
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false, true));

        Level level = target.level();
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.4D, 0.6D, 0.4D, 0.05D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.02D);
            sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.4F, 1.3F);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        // Habilidad Activa: Aubade Coup Droit / Estocada del Inframundo
        player.getCooldowns().addCooldown(this, 160); // 8 segundos de enfriamiento

        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, Math.max(0.1D, look.y * 0.4D), look.z).normalize();

        // Impulso frontal rápido
        player.setDeltaMovement(forward.scale(1.75D));
        player.hurtMarked = true;

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;

            // Sonidos cortantes de hielo de inframundo
            sl.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.8F, 1.6F);
            sl.playSound(null, sp.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 1.4F);
            sl.playSound(null, sp.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.6F, 1.2F);

            // Trazado de partículas y daño a lo largo del avance (hasta 8 bloques)
            Vec3 start = sp.position();
            for (int i = 1; i <= 8; i++) {
                Vec3 p = start.add(forward.scale(i));
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y + 0.9D, p.z, 6, 0.2D, 0.2D, 0.2D, 0.02D);
                sl.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y + 0.9D, p.z, 10, 0.3D, 0.3D, 0.3D, 0.05D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y + 0.9D, p.z, 1, 0, 0, 0, 0);

                AABB hitBox = new AABB(p.x - 1.2D, p.y - 0.5D, p.z - 1.2D, p.x + 1.2D, p.y + 1.8D, p.z + 1.2D);
                List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != sp && e.isAlive());

                for (LivingEntity enemy : enemies) {
                    enemy.hurt(sl.damageSources().playerAttack(sp), 22.0F);
                    enemy.setTicksFrozen(360);
                    enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3, false, true, true));
                    enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, true, true));

                    sl.sendParticles(ParticleTypes.FLASH, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(), 1, 0, 0, 0, 0);
                }
            }

            sp.displayClientMessage(Component.literal("§b✦ [Soul Solid] §f¡Aubade Coup Droit! Estocada de Hielo del Inframundo desatada."), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§b✦ Espada-Bastón Shikomizue de Brook").withStyle(ChatFormatting.ITALIC));
        tooltipComponents.add(Component.literal("§7\"El frío que emana del reino de los muertos congelará hasta tu alma.\""));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§e✦ Pasiva: Frío del Inframundo").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("  §7Tus tajos congelan intensamente al objetivo, aplicando lentitud y escarcha helada."));
        tooltipComponents.add(Component.literal("§6✦ Activa (Clic Derecho): Aubade Coup Droit").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("  §7Arremete al frente en una estocada sónica perforante que congela y destruye a todos los enemigos en línea recta."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
