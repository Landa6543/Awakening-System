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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Raíz de Mandrágora Mágica (Magic Mandrake Root).
 * Ingrediente alquímico místico obtenido de las Mandrágoras Cantarinas.
 * Al consumirse directamente, restaura +120 Maná instantáneamente y confiere Regeneración vital.
 */
public class MandrakeRootItem extends Item {

    public static final FoodProperties MANDRAKE_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.8F)
            .alwaysEdible()
            .build();

    public MandrakeRootItem(Properties properties) {
        super(properties.food(MANDRAKE_FOOD).rarity(Rarity.UNCOMMON).stacksTo(32));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.restoreEnergy(50);
                data.addCultivationQi(100);
                AwakeningSystemMod.sync(player);
            }
            player.heal(14.0F);

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 0, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 1.0D, player.getZ(),
                        20, 0.5D, 0.5D, 0.5D, 0.05D);
            }

            player.playNotifySound(SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.9F, 1.2F);
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.mandrake_root.desc_1"));
        tooltip.add(Component.translatable("item.awakening_system.mandrake_root.desc_2"));
    }
}
