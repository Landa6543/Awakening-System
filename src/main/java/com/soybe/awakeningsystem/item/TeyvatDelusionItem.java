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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Representa los Engaños (Delusions) creados por los Fatui.
 * Otorgan inmenso poder elemental a cambio de vitalidad del usuario (corrosión/drenaje).
 */
public class TeyvatDelusionItem extends Item {

    public enum DelusionElement {
        PYRO("Pyro", "Infernal Flame", 0xFFD32F2F, ChatFormatting.DARK_RED),
        HYDRO("Hydro", "Abyssal Tide", 0xFF1976D2, ChatFormatting.BLUE),
        CRYO("Cryo", "Severe Frost", 0xFF00ACC1, ChatFormatting.DARK_AQUA),
        ELECTRO("Electro", "Lightning Judgment", 0xFF7B1FA2, ChatFormatting.DARK_PURPLE),
        ANEMO("Anemo", "Cutting Vortex", 0xFF388E3C, ChatFormatting.DARK_GREEN),
        GEO("Geo", "Basalt Rupture", 0xFFFFA000, ChatFormatting.GOLD),
        DENDRO("Dendro", "Toxic Rot", 0xFF689F38, ChatFormatting.GREEN),
        BLANK("Corrupt", "Unstable Power", 0xFF424242, ChatFormatting.DARK_GRAY);

        public final String id;
        public final String title;
        public final int color;
        public final ChatFormatting format;

        DelusionElement(String id, String title, int color, ChatFormatting format) {
            this.id = id;
            this.title = title;
            this.color = color;
            this.format = format;
        }
    }

    private final DelusionElement element;

    public TeyvatDelusionItem(DelusionElement element, Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
        this.element = element;
    }

    public DelusionElement getElement() {
        return element;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (element == DelusionElement.BLANK) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.awakening_system.delusion.blank_info")
                        .withStyle(ChatFormatting.DARK_GRAY), true);
            }
            return InteractionResultHolder.pass(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            Vec3 pos = sp.position();
            AABB area = new AABB(pos.x - 8, pos.y - 4, pos.z - 8, pos.x + 8, pos.y + 6, pos.z + 8);
            List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != sp && e.isAlive());

            sp.hurt(sp.damageSources().magic(), 4.0F);
            sp.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, false, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2, false, true));

            switch (element) {
                case PYRO -> {
                    sl.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1, pos.z, 80, 2.5, 1.0, 2.5, 0.2);
                    sl.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 1, pos.z, 30, 2.0, 1.0, 2.0, 0.1);
                    sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.2F);
                    for (LivingEntity e : enemies) {
                        e.hurt(sl.damageSources().playerAttack(sp), 24.0F);
                        e.setRemainingFireTicks(160);
                    }
                }
                case HYDRO -> {
                    sl.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + 1, pos.z, 100, 3.0, 1.5, 3.0, 0.3);
                    sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.5F, 0.8F);
                    for (LivingEntity e : enemies) {
                        e.hurt(sl.damageSources().indirectMagic(sp, sp), 20.0F);
                        e.knockback(1.8, pos.x - e.getX(), pos.z - e.getZ());
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                    }
                }
                case CRYO -> {
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 1, pos.z, 90, 3.0, 1.0, 3.0, 0.15);
                    sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.6F);
                    for (LivingEntity e : enemies) {
                        e.hurt(sl.damageSources().freeze(), 22.0F);
                        e.setTicksFrozen(240);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                    }
                }
                case ELECTRO -> {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1, pos.z, 90, 2.5, 2.0, 2.5, 0.3);
                    sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.5F);
                    for (LivingEntity e : enemies) {
                        LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                        if (bolt != null) {
                            bolt.moveTo(e.position());
                            bolt.setVisualOnly(true);
                            sl.addFreshEntity(bolt);
                        }
                        e.hurt(sl.damageSources().lightningBolt(), 26.0F);
                        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                    }
                }
                case ANEMO -> {
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y + 1, pos.z, 40, 2.0, 1.0, 2.0, 0.2);
                    sl.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 1, pos.z, 50, 2.5, 1.0, 2.5, 0.1);
                    sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2F, 1.6F);
                    for (LivingEntity e : enemies) {
                        e.hurt(sl.damageSources().indirectMagic(sp, sp), 18.0F);
                        Vec3 dir = pos.subtract(e.position()).normalize().scale(1.2);
                        e.setDeltaMovement(dir.x, 0.5, dir.z);
                        e.hurtMarked = true;
                    }
                }
                case GEO -> {
                    sl.sendParticles(ParticleTypes.CRIT, pos.x, pos.y + 0.5, pos.z, 80, 3.0, 0.5, 3.0, 0.2);
                    sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5F, 0.8F);
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 2));
                    for (LivingEntity e : enemies) {
                        e.hurt(sl.damageSources().playerAttack(sp), 25.0F);
                        e.setDeltaMovement(0, 0.8, 0);
                        e.hurtMarked = true;
                    }
                }
                case DENDRO -> {
                    sl.sendParticles(ParticleTypes.COMPOSTER, pos.x, pos.y + 1, pos.z, 70, 2.5, 1.5, 2.5, 0.1);
                    sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.x, pos.y + 1, pos.z, 60, 3.0, 1.5, 3.0, 0.1);
                    sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 1.5F, 1.1F);
                    for (LivingEntity e : enemies) {
                        e.hurt(sl.damageSources().magic(), 20.0F);
                        e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
                    }
                }
            }

            sp.displayClientMessage(Component.translatable("item.awakening_system.teyvat_delusion.unleashed")
                    .withStyle(ChatFormatting.RED), true);
            player.getCooldowns().addCooldown(this, 160);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.element").append(Component.literal(element.format + element.title)));
        if (element != DelusionElement.BLANK) {
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.active"));
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.warn"));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.blank"));
        }
    }
}
