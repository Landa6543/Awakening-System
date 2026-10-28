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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class GenshinBowItem extends BowItem implements AwakeningWeaponItem {

    public enum BowType {
        AMOS_BOW("amos_bow", "Amos' Bow", 5, "Genshin Impact", 14.0F, "Strong-Willed: Projectile damage increases by up to +50% based on flight distance."),
        AQUA_SIMULACRA("aqua_simulacra", "Aqua Simulacra", 5, "Genshin Impact", 15.0F, "The Cleansing Form: Hydro-infused shots deal increased damage and boost movement speed."),
        THUNDERING_PULSE("thundering_pulse", "Thundering Pulse", 5, "Genshin Impact", 16.0F, "Rule by Thunder: Charged shots emit Electro sparks dealing area-of-effect damage."),
        ASTRAL_ARCHERS_BOW("astral_archers_bow", "Astral Archer's Bow", 4, "Honkai: Star Rail", 11.0F, "Power of Preservation: Crystal arrows slow and freeze enemies upon impact."),
        SKYWARD_HARP("skyward_harp", "Skyward Harp", 5, "Genshin Impact", 15.0F, "Echoing Ballad: Anemo-infused shots summon celestial gusts of cutting wind."),
        POLAR_STAR("polar_star", "Polar Star", 5, "Genshin Impact", 16.0F, "Daylight's Augury: Fatui glacial arrows grant increased critical hit chance."),
        HUNTER_PATH("hunter_path", "Hunter's Path", 5, "Genshin Impact", 15.0F, "At the End of the Beast-Paths: Dendro-infused shots deal increased elemental damage."),
        HAMAYUMI("hamayumi", "Hamayumi", 4, "Genshin Impact", 12.0F, "Full Draw: Exorcism fire arrows scale in damage based on current energy."),
        END_OF_THE_LINE("end_of_the_line", "End of the Line", 4, "Genshin Impact", 11.0F, "Net Snapper: Water arrows push targets with rapid oceanic currents.");

        public final String id;
        public final String displayName;
        public final int stars;
        public final String series;
        public final float bonusDamage;
        public final String passiveDesc;

        BowType(String id, String displayName, int stars, String series, float bonusDamage, String passiveDesc) {
            this.id = id;
            this.displayName = displayName;
            this.stars = stars;
            this.series = series;
            this.bonusDamage = bonusDamage;
            this.passiveDesc = passiveDesc;
        }
    }

    private final BowType bowType;

    public GenshinBowItem(BowType bowType, Properties properties) {
        super(properties.durability(2500).rarity(bowType.stars == 5 ? Rarity.EPIC : Rarity.RARE));
        this.bowType = bowType;
    }

    public BowType getBowType() {
        return bowType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) return;

        ItemStack ammo = player.getProjectile(stack);
        boolean creative = player.getAbilities().instabuild;

        int duration = this.getUseDuration(stack, livingEntity) - timeLeft;
        float power = getPowerForTime(duration);

        if ((double) power >= 0.1D) {
            boolean hasRealAmmo = !ammo.isEmpty();
            boolean generatedAmmo = false;
            if (ammo.isEmpty()) {
                ammo = new ItemStack(Items.ARROW);
                generatedAmmo = true;
            }

            if (!level.isClientSide()) {
                ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
                AbstractArrow arrow = arrowItem.createArrow(level, ammo, player, stack);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.2F, 0.5F);

                if (generatedAmmo || creative) {
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                } else {
                    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                }

                double baseDmg = arrow.getBaseDamage() + (this.bowType.bonusDamage / 2.0D);
                if (power >= 1.0F) {
                    arrow.setCritArrow(true);
                    baseDmg += 3.0D;
                }
                arrow.setBaseDamage(baseDmg);

                if (this.bowType == BowType.THUNDERING_PULSE) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, arrow.getX(), arrow.getY(), arrow.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                    }
                } else if (this.bowType == BowType.AMOS_BOW) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CRIT, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.1, 0.1, 0.1, 0.02);
                    }
                } else if (this.bowType == BowType.AQUA_SIMULACRA) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SPLASH, arrow.getX(), arrow.getY(), arrow.getZ(), 12, 0.2, 0.2, 0.2, 0.05);
                    }
                } else if (this.bowType == BowType.ASTRAL_ARCHERS_BOW) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, arrow.getX(), arrow.getY(), arrow.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                    }
                } else if (this.bowType == BowType.SKYWARD_HARP) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CLOUD, arrow.getX(), arrow.getY(), arrow.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                    }
                } else if (this.bowType == BowType.POLAR_STAR) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, arrow.getX(), arrow.getY(), arrow.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.1, 0.1, 0.1, 0.02);
                    }
                } else if (this.bowType == BowType.HUNTER_PATH) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                    }
                } else if (this.bowType == BowType.HAMAYUMI) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FLAME, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.1, 0.1, 0.1, 0.02);
                    }
                } else if (this.bowType == BowType.END_OF_THE_LINE) {
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.BUBBLE_POP, arrow.getX(), arrow.getY(), arrow.getZ(), 12, 0.2, 0.2, 0.2, 0.05);
                    }
                }

                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                level.addFreshEntity(arrow);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                    1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

            if (!creative && hasRealAmmo) {
                ammo.shrink(1);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String starString = switch (bowType.stars) {
            case 5 -> "§6★★★★★";
            case 4 -> "§d★★★★";
            default -> "§b★★★";
        };
        tooltip.add(Component.literal(starString + " §e[" + bowType.series + "]"));
        tooltip.add(Component.translatable("item.awakening_system.genshin_bow.base_dmg", String.format("%.1f", bowType.bonusDamage)));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.genshin_bow.passive_header"));
        tooltip.add(Component.translatableWithFallback("item.awakening_system.genshin_bow." + bowType.id + ".passive", "§7" + bowType.passiveDesc));
    }
}
