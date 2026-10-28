package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Piedras de Resonancia de Óculos de Teyvat.
 * Emite pulsos luminosos arcanos para rastrear la energía elemental del óculo más cercano.
 */
public class OculusResonanceStoneItem extends Item {

    private final OculusItem.OculusElement element;

    public OculusResonanceStoneItem(OculusItem.OculusElement element, Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(16));
        this.element = element;
    }

    public OculusItem.OculusElement getElement() {
        return element;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            Block targetBlock = switch (element) {
                case GEO -> SystemBlocks.GEOCULUS_BLOCK.get();
                case ELECTRO -> SystemBlocks.ELECTROCULUS_BLOCK.get();
                case DENDRO -> SystemBlocks.DENDROCULUS_BLOCK.get();
                default -> SystemBlocks.ANEMOCULUS_BLOCK.get();
            };

            // Escanear en un radio de 140 bloques alrededor del jugador
            BlockPos nearest = null;
            double nearestDistSq = Double.MAX_VALUE;
            BlockPos pPos = sp.blockPosition();

            int radius = 140;
            int step = 2; // paso optimizado de muestreo
            for (int dx = -radius; dx <= radius; dx += step) {
                for (int dz = -radius; dz <= radius; dz += step) {
                    double distSq = dx * dx + dz * dz;
                    if (distSq > radius * radius || distSq >= nearestDistSq) continue;

                    int surfaceY = sl.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pPos.offset(dx, 0, dz)).getY();
                    int minY = Math.max(sl.getMinBuildHeight(), surfaceY - 2);
                    int maxY = Math.min(sl.getMaxBuildHeight(), surfaceY + 14);

                    for (int dy = minY; dy <= maxY; dy++) {
                        BlockPos check = new BlockPos(pPos.getX() + dx, dy, pPos.getZ() + dz);
                        if (sl.getBlockState(check).is(targetBlock)) {
                            nearest = check;
                            nearestDistSq = distSq;
                            break;
                        }
                    }
                }
            }

            if (nearest != null) {
                int dist = (int) Math.sqrt(nearestDistSq);
                int dx = nearest.getX() - pPos.getX();
                int dz = nearest.getZ() - pPos.getZ();
                String dir = getDirectionName(dx, dz);

                // Sendero de partículas hacia la dirección del óculo
                Vec3 dirVec = new Vec3(dx, 0, dz).normalize();
                for (int p = 1; p <= 16; p++) {
                    sl.sendParticles(ParticleTypes.END_ROD,
                            sp.getX() + dirVec.x * p * 0.75D,
                            sp.getY() + 1.2D,
                            sp.getZ() + dirVec.z * p * 0.75D,
                            2, 0.08D, 0.08D, 0.08D, 0.02D);
                }

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.6F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.oculus_resonance_stone.detected", element.title, dir, dist), true);
            } else {
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.8F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.oculus_resonance_stone.not_detected", element.title), true);
            }

            sp.getCooldowns().addCooldown(this, 60);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static String getDirectionName(int dx, int dz) {
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360.0;

        if (angle >= 337.5 || angle < 22.5) return "South";
        if (angle >= 22.5 && angle < 67.5) return "Southwest";
        if (angle >= 67.5 && angle < 112.5) return "West";
        if (angle >= 112.5 && angle < 157.5) return "Northwest";
        if (angle >= 157.5 && angle < 202.5) return "North";
        if (angle >= 202.5 && angle < 247.5) return "Northeast";
        if (angle >= 247.5 && angle < 292.5) return "East";
        return "Southeast";
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.oculus_resonance_stone.element", element.format + element.title));
        tooltip.add(Component.translatable("item.awakening_system.oculus_resonance_stone.region", element.format + element.region));
        tooltip.add(Component.translatable("item.awakening_system.oculus_resonance_stone.use", element.title));
    }
}
