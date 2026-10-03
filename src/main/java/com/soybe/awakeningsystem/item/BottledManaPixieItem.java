package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.entity.ManaPixieEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Hada de Maná en Frasco (Bottled Mana Pixie).
 * - Linterna viva y catalizador de maná.
 * - Al llevarla equipada en la mano secundaria o principal, regenera +6 Maná cada 1.5s continuamente.
 * - Clic derecho en un bloque libera al hada viva en el mundo, devolviendo un frasco de vidrio vacío.
 */
public class BottledManaPixieItem extends Item {

    public BottledManaPixieItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            boolean isEquipped = isSelected || player.getOffhandItem() == stack;
            if (isEquipped && player.tickCount % 30 == 0) {
                SystemData data = SystemEvents.data(player);
                if (data != null && data.energy() < data.maxEnergy()) {
                    data.restoreEnergy(6);
                    AwakeningSystemMod.sync(player);
                }

                if (level instanceof ServerLevel sl && player.tickCount % 60 == 0) {
                    sl.sendParticles(ParticleTypes.GLOW,
                            player.getX() + (player.getRandom().nextDouble() - 0.5D) * 0.5D,
                            player.getY() + 0.8D + (player.getRandom().nextDouble() - 0.5D) * 0.3D,
                            player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 0.5D,
                            2, 0.1D, 0.1D, 0.1D, 0.02D);
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = pos.relative(face);

        if (!level.isClientSide()) {
            ManaPixieEntity pixie = new ManaPixieEntity(ModEntities.MANA_PIXIE.get(), level);
            pixie.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.2D, spawnPos.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(pixie);

            level.playSound(null, spawnPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.4F);
            level.playSound(null, spawnPos, ModSounds.MANA_PIXIE_BLESS.get(), SoundSource.PLAYERS, 1.0F, 1.3F);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD,
                        spawnPos.getX() + 0.5D, spawnPos.getY() + 0.4D, spawnPos.getZ() + 0.5D,
                        25, 0.3D, 0.3D, 0.3D, 0.08D);
            }

            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.addItem(bottle)) {
                    player.drop(bottle, false);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.bottled_mana_pixie.desc_1"));
        tooltip.add(Component.translatable("item.awakening_system.bottled_mana_pixie.desc_2"));
    }
}
