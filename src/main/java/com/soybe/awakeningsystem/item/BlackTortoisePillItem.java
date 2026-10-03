package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BlackTortoisePillItem extends Item {
    public BlackTortoisePillItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);

            sp.removeAllEffects();
            sp.heal(sp.getMaxHealth());
            data.restoreEnergy(data.maxEnergy());
            data.addCultivationQi(3500);

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 12000, 3));

            int currentVit = sp.getPersistentData().getInt("black_tortoise_vitality");
            sp.getPersistentData().putInt("black_tortoise_vitality", currentVit + 1);

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.6D, 0.8D, 0.6D, 0.1D);
            sl.sendParticles(ParticleTypes.HEART, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.05D);

            sp.playNotifySound(SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 0.9F);
            sp.playNotifySound(SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.2F, 1.1F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.black_tortoise_pill.used"), true);
            AwakeningSystemMod.notify(sp, "awakening_system.item.black_tortoise_pill.title", "awakening_system.item.black_tortoise_pill.desc", 0xFF00CED1);

            AwakeningSystemMod.sync(sp);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.black_tortoise_pill.desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
