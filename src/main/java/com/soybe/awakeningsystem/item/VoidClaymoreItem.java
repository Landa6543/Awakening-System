package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Gran Espada del Vacío (Void Claymore):
 * Espada titánica forjada en el tejido entre dimensiones.
 * Su hoja vibra con una corriente perpetua de plasma abisal animado.
 *
 * Habilidad Activa (Clic Derecho): "Corte Dimensional del Vacío"
 * Desata un tajo expansivo de energía del vacío que arrasa a los enemigos frente al portador,
 * infligiendo daño devastador y atrayéndolos hacia la línea de corte.
 */
public class VoidClaymoreItem extends SwordItem implements AwakeningWeaponItem {

    public VoidClaymoreItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(3600)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 28, -2.8F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && !target.level().isClientSide()) {
            // Pasiva: Corrupción del Vacío
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));

            if (target.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(),
                        20, 0.4D, 0.5D, 0.4D, 0.1D);
            }
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            ServerLevel sl = (ServerLevel) level;
            Vec3 look = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            // Sonido de desgarro dimensional
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2F, 1.4F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.8F);

            // Generar rastro de corte del vacío
            for (int i = 2; i <= 12; i += 2) {
                Vec3 slashPoint = eyePos.add(look.scale(i));
                sl.sendParticles(ParticleTypes.SONIC_BOOM, slashPoint.x, slashPoint.y, slashPoint.z, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, slashPoint.x, slashPoint.y, slashPoint.z, 25, 0.6D, 0.6D, 0.6D, 0.05D);
            }

            // Dañar a las criaturas en la línea de corte
            AABB area = player.getBoundingBox().expandTowards(look.scale(12.0D)).inflate(2.5D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());

            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().playerAttack(player), 55.0F);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));

                // Atraer ligeramente hacia la línea de corte
                Vec3 pull = eyePos.add(look.scale(6.0D)).subtract(target.position()).normalize().scale(0.6D);
                target.setDeltaMovement(target.getDeltaMovement().add(pull));
                target.hurtMarked = true;
            }

            player.displayClientMessage(
                    Component.translatableWithFallback("item.awakening_system.void_claymore.skill_cast",
                            "§5§l[Corte del Vacío] §d¡El tejido espacial ha colapsado frente a ti!"), true);

            player.getCooldowns().addCooldown(this, 200); // 10 segundos de cooldown
            stack.hurtAndBreak(3, player, LivingEntity.getSlotForHand(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.void_claymore.lore")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.void_claymore.passive_header")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.void_claymore.passive_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.void_claymore.active_header")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.void_claymore.active_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.void_claymore.cd")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
