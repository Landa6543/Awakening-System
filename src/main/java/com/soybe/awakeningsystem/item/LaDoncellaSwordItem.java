package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LaDoncellaSwordItem extends SwordItem implements AwakeningWeaponItem {

    public LaDoncellaSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(10000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 36, -2.8F))); // 40.0 Daño Total (36 base + 4 Netherite), 1.2 Mandoble Pesado
    }

    public static boolean isTransformed(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            return cd.copyTag().getBoolean("Transformed");
        }
        return false;
    }

    public static void setTransformed(ItemStack stack, boolean transformed) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        tag.putBoolean("Transformed", transformed);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        if (transformed) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(1));
        } else {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. Alternar Forma (Shift + Clic Derecho)
        if (player.isShiftKeyDown()) {
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }

            boolean currentState = isTransformed(stack);
            boolean newState = !currentState;
            ItemStack newStack = stack.copy();
            setTransformed(newStack, newState);
            player.setItemInHand(hand, newStack);
            player.getCooldowns().addCooldown(this, 15);

            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                sp.inventoryMenu.broadcastChanges();
                ServerLevel sl = (ServerLevel) level;
                if (newState) {
                    // Despertar de las espinas y la ceniza
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.5F, 0.6F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.3F, 0.85F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.35F);

                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 1.5D, 1.2D, 1.5D, 0.15D);
                    sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 1.0D, 1.0D, 1.0D, 0.2D);
                    sl.sendParticles(ParticleTypes.CHERRY_LEAVES, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 30, 1.2D, 0.8D, 1.2D, 0.08D);
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 25, 1.0D, 0.6D, 1.0D, 0.08D);

                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, false, true)); // Fuerza III (sin remolinos)
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 360, 2, false, false, true)); // Absorción (sin remolinos)

                    sp.displayClientMessage(Component.literal("§4§l🌹 [LA DONCELLA] §c¡El Trono de Ceniza despierta bajo la Luna Carmesí! Las espinas han brotado."), true);
                } else {
                    // Retorno a la Forma Pura
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.2F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.3F, 0.8F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.6F);

                    sl.sendParticles(ParticleTypes.CHERRY_LEAVES, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.08D);
                    sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 25, 0.8D, 0.8D, 0.8D, 0.08D);

                    sp.displayClientMessage(Component.literal("§f§l✦ [LA DONCELLA] §7Las espinas se repliegan. El acero noble y las rosas puras resplandecen."), true);
                }
            }
            return InteractionResultHolder.sidedSuccess(newStack, level.isClientSide());
        }

        // 2. Habilidad Activa (Clic Derecho Normal)
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        boolean transformed = isTransformed(stack);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            long gameTime = level.getGameTime();

            if (transformed) {
                // Habilidad Despierta: "Jardín del Silencio: Cataclismo de Espinas" (Potenciada)
                long cdUntil = sp.getPersistentData().getLong("doncella_briar_cataclysm_cd");
                if (gameTime < cdUntil) {
                    long sec = (cdUntil - gameTime) / 20;
                    sp.displayClientMessage(Component.literal("§c§l✦ Cataclismo de Espinas en enfriamiento: §f" + sec + "s"), true);
                    return InteractionResultHolder.fail(stack);
                }

                sp.getPersistentData().putLong("doncella_briar_cataclysm_cd", gameTime + 240L); // 12s cd
                sp.getCooldowns().addCooldown(this, 240);

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 0.85F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.6F, 0.65F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.35F);

                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 140, 5.0D, 1.5D, 5.0D, 0.25D);
                sl.sendParticles(ParticleTypes.ASH, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 90, 4.0D, 1.5D, 4.0D, 0.1D);
                sl.sendParticles(ParticleTypes.EXPLOSION, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 8, 3.0D, 0.8D, 3.0D, 0.1D);

                AABB area = sp.getBoundingBox().inflate(12.0D, 4.5D, 12.0D);
                int hitCount = 0;
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 95.0F); // 95 de daño devastador
                        nearby.setDeltaMovement(0.0D, 1.1D, 0.0D);
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 3));
                        nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 180, 2));
                        hitCount++;
                    }
                }

                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 2, false, true)); // Resistencia III
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 340, 3, false, true)); // Absorción IV

                sp.displayClientMessage(Component.literal("§4§l🌹 [JARDÍN DEL SILENCIO] §c¡El Cataclismo de Espinas devasta a " + hitCount + " enemigos infligiendo 95 de daño!"), true);
                return InteractionResultHolder.consume(stack);

            } else {
                // Habilidad Pura: "Fragancia del Silencio" (Potenciada)
                long cdUntil = sp.getPersistentData().getLong("doncella_silence_perfume_cd");
                if (gameTime < cdUntil) {
                    long sec = (cdUntil - gameTime) / 20;
                    sp.displayClientMessage(Component.literal("§d§l✦ Fragancia del Silencio en enfriamiento: §f" + sec + "s"), true);
                    return InteractionResultHolder.fail(stack);
                }

                sp.getPersistentData().putLong("doncella_silence_perfume_cd", gameTime + 160L); // 8s cd
                sp.getCooldowns().addCooldown(this, 160);

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.1F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.4F);

                Vec3 look = sp.getLookAngle();
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, sp.getX() + look.x * 2.8D, sp.getY() + 1.2D, sp.getZ() + look.z * 2.8D, 8, 0.7D, 0.3D, 0.7D, 0.0D);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, sp.getX() + look.x * 2.8D, sp.getY() + 1.0D, sp.getZ() + look.z * 2.8D, 75, 2.2D, 0.9D, 2.2D, 0.1D);

                AABB cone = sp.getBoundingBox().inflate(9.0D, 3.5D, 9.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cone)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 65.0F); // 65 de daño en cono
                        nearby.knockback(1.4D, sp.getX() - nearby.getX(), sp.getZ() - nearby.getZ());
                        nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 2));
                    }
                }

                // Purificación del portador con bendición de rosas
                sp.removeEffect(MobEffects.WITHER);
                sp.removeEffect(MobEffects.POISON);
                sp.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                sp.removeEffect(MobEffects.WEAKNESS);
                sp.removeEffect(MobEffects.BLINDNESS);

                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 1, false, false, true)); // Velocidad II (sin remolinos)
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 1, false, false, true)); // Resistencia II (sin remolinos)
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, false, true)); // Regeneración II (sin remolinos)

                sp.displayClientMessage(Component.literal("§d§l✦ [FRAGANCIA DEL SILENCIO] §fLa fragancia purifica tu espíritu y bendice tus sentidos (65 daño en cono)."), true);
                return InteractionResultHolder.consume(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            boolean transformed = isTransformed(stack);

            if (transformed) {
                // Forma Despierta: Laceración de espinas y marchitamiento (+20 Daño Perforante)
                if (attacker instanceof Player player) {
                    target.hurt(target.damageSources().playerAttack(player), 20.0F); // +20 de daño perforante
                }

                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2)); // Wither III
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2)); // Slowness III

                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.2F, 0.9F);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.7D, 0.7D, 0.7D, 0.12D);

            } else {
                // Forma Pura: Hendidura en área (+28 daño cleave)
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 1.15F);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.1D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 0.8D, target.getZ(), 3, 0.4D, 0.4D, 0.4D, 0.0D);

                AABB cleave = target.getBoundingBox().inflate(6.5D, 2.5D, 6.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cleave)) {
                    if (nearby != attacker && nearby != target && nearby instanceof Enemy) {
                        if (attacker instanceof Player p) {
                            nearby.hurt(nearby.damageSources().playerAttack(p), 28.0F);
                        }
                        nearby.knockback(1.0D, attacker.getX() - nearby.getX(), attacker.getZ() - nearby.getZ());
                    }
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        boolean transformed = isTransformed(stack);

        tooltip.add(Component.literal("§d§l✦ [MANDOBLE DE MAHIRO // MITO SUPREMO] ✦"));
        tooltip.add(Component.literal("§f\"...cuyas Espinas Crecieron alrededor del Corazón de la Doncella...\"").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal(""));

        if (!transformed) {
            tooltip.add(Component.literal("§f§l[FORMA PURA: ACERO DE LAS ROSAS]"));
            tooltip.add(Component.literal("§c▪ Daño Físico Base: §f40.0 §8(Mandoble 1.2 Vel.)"));
            tooltip.add(Component.literal("§a▪ Hendidura de Pétalos: §fGolpes causan §a+28 de Daño en Área §7(6.5 bloques)."));
            tooltip.add(Component.literal("§e▪ Activa (Clic Derecho): §dFragancia del Silencio §7(Cono de §f65 daño §7+ Purificación + Regen II)."));
            tooltip.add(Component.literal("§8▪ Enfriamiento: §f8s."));
        } else {
            tooltip.add(Component.literal("§4§l[FORMA DESPIERTA: LUNA CARMESÍ Y ESPINAS]"));
            tooltip.add(Component.literal("§c▪ Daño Físico Base: §f40.0 §8(+20 Perforación de Espinas = 60 Total)"));
            tooltip.add(Component.literal("§4▪ Corona de Espinas: §fEspinas devuelven §c32 de daño perforante §fa atacantes."));
            tooltip.add(Component.literal("§4▪ Laceración Sombría: §fAplica §5Wither III §7y §8Lentitud III §7al golpear."));
            tooltip.add(Component.literal("§c▪ Activa (Clic Derecho): §4Cataclismo de Espinas §7(§f95 daño en área §7en 12 bloques)."));
            tooltip.add(Component.literal("§8▪ Enfriamiento: §f12s."));
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§8▪ Shift + Clic Derecho: Alternar Forma de la Doncella."));
        tooltip.add(Component.literal("§5▪ Probabilidad de Obtención en Gacha: §d0.020%"));
    }
}
