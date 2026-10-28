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
import com.soybe.awakeningsystem.entity.SlashWaveEntity;

import java.util.List;

public class LaDoncellaSwordItem extends SwordItem implements AwakeningWeaponItem {

    public LaDoncellaSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(10000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 36, -2.8F)));
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
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.5F, 0.6F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.3F, 0.85F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.35F);

                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 1.5D, 1.2D, 1.5D, 0.15D);
                    sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 1.0D, 1.0D, 1.0D, 0.2D);
                    sl.sendParticles(ParticleTypes.CHERRY_LEAVES, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 30, 1.2D, 0.8D, 1.2D, 0.08D);
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, sp.getX(), sp.getY() + 0.5D, sp.getZ(), 25, 1.0D, 0.6D, 1.0D, 0.08D);

                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, false, true));
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 360, 2, false, false, true));

                    sp.displayClientMessage(Component.translatable("item.awakening_system.la_doncella.msg.awakened"), true);
                } else {
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.2F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.3F, 0.8F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.6F);

                    sl.sendParticles(ParticleTypes.CHERRY_LEAVES, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.08D);
                    sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 25, 0.8D, 0.8D, 0.8D, 0.08D);

                    sp.displayClientMessage(Component.translatable("item.awakening_system.la_doncella.msg.pure"), true);
                }
            }
            return InteractionResultHolder.sidedSuccess(newStack, level.isClientSide());
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        boolean transformed = isTransformed(stack);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            long gameTime = level.getGameTime();

            if (transformed) {
                long cdUntil = sp.getPersistentData().getLong("doncella_briar_cataclysm_cd");
                if (gameTime < cdUntil) {
                    long sec = (cdUntil - gameTime) / 20;
                    sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.la_doncella.msg.cd", "§c§l✦ Cataclysm of Thorns on cooldown: §f%ss", sec), true);
                    return InteractionResultHolder.fail(stack);
                }

                sp.getPersistentData().putLong("doncella_briar_cataclysm_cd", gameTime + 240L);
                sp.getCooldowns().addCooldown(this, 240);

                Vec3 look = sp.getLookAngle();

                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                                (float) look.x, (float) look.y, (float) look.z,
                                5.0F, 50, 0x880015, 0, false
                        )
                );

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.8F, 0.65F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 1.25F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.6F, 0.6F);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 36.0D, 0.85F, 14);

                SlashWaveEntity bloodWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRIMSON_BLOOD, 75.0F, 5.4F, 2.2F, 0.0F, 30, 12);
                bloodWave.setDeltaMovement(look.scale(1.95D));
                sl.addFreshEntity(bloodWave);

                SlashWaveEntity verticalWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRIMSON_BLOOD, 45.0F, 3.8F, 2.6F, 90.0F, 24, 6);
                verticalWave.setDeltaMovement(look.scale(1.85D));
                sl.addFreshEntity(verticalWave);

                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 2, false, true));
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 340, 3, false, true));

                sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.la_doncella.msg.blood_wave", "§4§l🌹 [THRONE OF ASH] §cColossal crimson crescent and ascending slash unleashed with lifesteal!"), true);
                return InteractionResultHolder.consume(stack);

            } else {
                long cdUntil = sp.getPersistentData().getLong("doncella_silence_perfume_cd");
                if (gameTime < cdUntil) {
                    long sec = (cdUntil - gameTime) / 20;
                    sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.la_doncella.msg.silence_cd", "§d§l✦ Fragrance of Silence on cooldown: §f%ss", sec), true);
                    return InteractionResultHolder.fail(stack);
                }

                sp.getPersistentData().putLong("doncella_silence_perfume_cd", gameTime + 160L);
                sp.getCooldowns().addCooldown(this, 160);

                Vec3 look = sp.getLookAngle();

                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                                (float) look.x, (float) look.y, (float) look.z,
                                4.0F, 40, 0xFFD1DC, 0, false
                        )
                );

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.6F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 1.2F);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.45F, 8);

                SlashWaveEntity silverWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_SILVER_ROSE, 55.0F, 4.4F, 1.8F, -20.0F, 26, 8);
                silverWave.setDeltaMovement(look.scale(2.05D));
                sl.addFreshEntity(silverWave);

                sp.removeEffect(MobEffects.WITHER);
                sp.removeEffect(MobEffects.POISON);
                sp.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                sp.removeEffect(MobEffects.WEAKNESS);
                sp.removeEffect(MobEffects.BLINDNESS);

                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 1, false, false, true));
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 1, false, false, true));
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, false, true));

                sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.la_doncella.msg.silence", "§d§l✦ [FRAGRANCE OF SILENCE] §fFragrance purifies your spirit and blesses your senses (65 cone damage)."), true);
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
                if (attacker instanceof Player player) {
                    target.hurt(target.damageSources().playerAttack(player), 20.0F);
                }

                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));

                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.2F, 0.9F);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.7D, 0.7D, 0.7D, 0.12D);

            } else {
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

        tooltip.add(Component.translatable("item.awakening_system.la_doncella.title"));
        tooltip.add(Component.translatable("item.awakening_system.la_doncella.quote").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.empty());

        if (!transformed) {
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.pure.header"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.pure.dmg"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.pure.petal"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.pure.active"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.pure.cd"));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.awakened.header"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.awakened.dmg"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.awakened.thorns"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.awakened.wither"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.awakened.active"));
            tooltip.add(Component.translatable("item.awakening_system.la_doncella.awakened.cd"));
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.la_doncella.hint.shift"));
        tooltip.add(Component.translatable("item.awakening_system.la_doncella.hint.gacha"));
    }
}
