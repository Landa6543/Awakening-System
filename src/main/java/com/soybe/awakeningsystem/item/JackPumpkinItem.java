package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.limbo.LimboWorldManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * La Calabaza de Jack (Stingy Jack's Soul Pumpkin Item):
 * Reliquia eterna portada por Jack en el purgatorio entre el Cielo y el Infierno.
 * En la oscuridad del Limbo, su fuego de alma es la única fuente de luz que no se extingue.
 * Proporciona iluminación dinámica tanto en la mano principal como secundaria.
 */
public class JackPumpkinItem extends BlockItem {

    private static final Map<UUID, BlockPos> ACTIVE_LIGHT_POSITIONS = new ConcurrentHashMap<>();

    public JackPumpkinItem(Properties properties) {
        super(SystemBlocks.JACK_PUMPKIN.get(), properties.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player)) return;

        boolean isHeld = (player.getMainHandItem() == stack || player.getOffhandItem() == stack);

        if (isHeld) {
            boolean inLimbo = LimboWorldManager.isLimboDimension(level);

            // Inmunidad a ceguera y oscuridad mientras se sostiene la calabaza
            if (inLimbo) {
                if (player.hasEffect(MobEffects.BLINDNESS)) {
                    player.removeEffect(MobEffects.BLINDNESS);
                }
                if (player.hasEffect(MobEffects.DARKNESS)) {
                    player.removeEffect(MobEffects.DARKNESS);
                }
            }

            // Luz dinámica en el servidor
            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                BlockPos currentHeadPos = player.blockPosition().above();
                UUID uuid = player.getUUID();
                BlockPos prevPos = ACTIVE_LIGHT_POSITIONS.get(uuid);

                if (prevPos != null && !prevPos.equals(currentHeadPos)) {
                    // Limpiar luz anterior
                    BlockState prevState = sl.getBlockState(prevPos);
                    if (prevState.is(Blocks.LIGHT)) {
                        sl.setBlock(prevPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }

                // Colocar bloque de luz dinámica invisible nivel 15
                BlockState currentState = sl.getBlockState(currentHeadPos);
                if (currentState.isAir()) {
                    sl.setBlock(currentHeadPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
                    ACTIVE_LIGHT_POSITIONS.put(uuid, currentHeadPos);
                } else if (currentState.is(Blocks.LIGHT)) {
                    ACTIVE_LIGHT_POSITIONS.put(uuid, currentHeadPos);
                }

                // Partículas flotantes de fuego de alma alrededor de la linterna
                if (level.getGameTime() % 10 == 0) {
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            player.getX(), player.getY() + 1.2D, player.getZ(),
                            2, 0.25D, 0.2D, 0.25D, 0.01D);
                }
            }
        } else {
            // Si el jugador no sostiene esta copia de la calabaza, limpiar luz activa si corresponde
            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                UUID uuid = player.getUUID();
                BlockPos prevPos = ACTIVE_LIGHT_POSITIONS.remove(uuid);
                if (prevPos != null) {
                    BlockState prevState = sl.getBlockState(prevPos);
                    if (prevState.is(Blocks.LIGHT)) {
                        sl.setBlock(prevPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    public static void clearDynamicLight(Player player, Level level) {
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            BlockPos prevPos = ACTIVE_LIGHT_POSITIONS.remove(player.getUUID());
            if (prevPos != null && sl.getBlockState(prevPos).is(Blocks.LIGHT)) {
                sl.setBlock(prevPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.jack_pumpkin.lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.jack_pumpkin.feature_1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.jack_pumpkin.feature_2").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.jack_pumpkin.hand_guide").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
