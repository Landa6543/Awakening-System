package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.effect.ModMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class ManaPotionItem extends Item {

    public ManaPotionItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        super.finishUsingItem(stack, level, entityLiving);

        if (entityLiving instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(this));

            SystemData data = SystemEvents.data(player);
            if (data != null) {
                int maxMana = data.maxMana();
                int baseRestore = Math.max(120, (int) Math.round(maxMana * 0.35D));

                MobEffectInstance existing = player.getEffect(ModMobEffects.MANA_SATURATION);
                int actualRestore;
                int newAmp;
                int newDuration;

                if (existing == null) {
                    actualRestore = baseRestore;
                    newAmp = 0; // Level I
                    newDuration = 600; // 30 seconds

                    data.restoreEnergy(actualRestore);
                    AwakeningSystemMod.sync(player);

                    MobEffectInstance inst = new MobEffectInstance(ModMobEffects.MANA_SATURATION, newDuration, newAmp, false, true, true);
                    inst.getCures().clear();
                    player.addEffect(inst);

                    player.displayClientMessage(
                            Component.translatable("awakening_system.potion.mana_restored", actualRestore)
                                    .withStyle(ChatFormatting.AQUA),
                            true
                    );
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.8F);
                } else {
                    int currentAmp = existing.getAmplifier();
                    int currentDur = existing.getDuration();

                    // Diminishing returns based on toxicity / saturation level:
                    // Level I: 50%
                    // Level II: 25%
                    // Level III+: 10%
                    float factor = (currentAmp == 0) ? 0.50F : (currentAmp == 1 ? 0.25F : 0.10F);
                    actualRestore = Math.max(15, (int) Math.round(baseRestore * factor));

                    newAmp = Math.min(3, currentAmp + 1);
                    newDuration = Math.min(2400, currentDur + 400); // Extends duration by 20s (max 2 minutes)

                    data.restoreEnergy(actualRestore);
                    AwakeningSystemMod.sync(player);

                    MobEffectInstance inst = new MobEffectInstance(ModMobEffects.MANA_SATURATION, newDuration, newAmp, false, true, true);
                    inst.getCures().clear();
                    player.addEffect(inst);

                    player.displayClientMessage(
                            Component.translatable("awakening_system.potion.mana_saturated_warning",
                                    actualRestore,
                                    toRoman(newAmp + 1))
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 0.7F, 0.7F);
                }

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.0D, player.getZ(),
                            12, 0.25D, 0.4D, 0.25D, 0.05D);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.8D, player.getZ(),
                            8, 0.2D, 0.3D, 0.2D, 0.02D);
                }
            }
        } else if (entityLiving instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        if (entityLiving instanceof Player player && player.getAbilities().instabuild) {
            return stack;
        }

        return ItemUtils.createFilledResult(stack, (Player) entityLiving, new ItemStack(Items.GLASS_BOTTLE));
    }

    private static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> String.valueOf(number);
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.mana_potion.desc1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.mana_potion.desc2").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.awakening_system.mana_potion.desc3").withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
