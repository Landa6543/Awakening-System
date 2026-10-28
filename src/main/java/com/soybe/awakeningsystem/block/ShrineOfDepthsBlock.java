package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Bloque sagrado de los Santuarios de las Profundidades de Teyvat (Anemo, Geo, Electro, Dendro).
 * Al hacer clic derecho con la llave regional correspondiente, rompe el sello elemental
 * y otorga 40 Protogemas, Destinos Entrelazados, Mora, Minerales de Refinamiento y EXP.
 */
public class ShrineOfDepthsBlock extends Block {
    public enum ShrineElement {
        ANEMO("Anemo", "§b", "anemo_shrine_key"),
        GEO("Geo", "§6", "geo_shrine_key"),
        ELECTRO("Electro", "§d", "electro_shrine_key"),
        DENDRO("Dendro", "§a", "dendro_shrine_key");

        public final String name;
        public final String colorCode;
        public final String keyId;

        ShrineElement(String name, String colorCode, String keyId) {
            this.name = name;
            this.colorCode = colorCode;
            this.keyId = keyId;
        }
    }

    private final ShrineElement element;

    public ShrineOfDepthsBlock(ShrineElement element, BlockBehaviour.Properties properties) {
        super(properties);
        this.element = element;
    }

    public ShrineElement getElement() {
        return element;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            Item requiredKey = switch (element) {
                case ANEMO -> SystemItems.ANEMO_SHRINE_KEY.get();
                case GEO -> SystemItems.GEO_SHRINE_KEY.get();
                case ELECTRO -> SystemItems.ELECTRO_SHRINE_KEY.get();
                case DENDRO -> SystemItems.DENDRO_SHRINE_KEY.get();
            };

            ItemStack mainHand = sp.getMainHandItem();
            ItemStack offHand = sp.getOffhandItem();
            boolean hasKeyInHand = (!mainHand.isEmpty() && mainHand.is(requiredKey)) || (!offHand.isEmpty() && offHand.is(requiredKey));

            if (hasKeyInHand) {
                if (!mainHand.isEmpty() && mainHand.is(requiredKey)) {
                    mainHand.shrink(1);
                } else {
                    offHand.shrink(1);
                }

                double cx = pos.getX() + 0.5D;
                double cy = pos.getY() + 1.2D;
                double cz = pos.getZ() + 0.5D;

                switch (element) {
                    case ANEMO -> {
                        sl.sendParticles(ParticleTypes.CLOUD, cx, cy, cz, 40, 0.6D, 0.8D, 0.6D, 0.1D);
                        sl.sendParticles(ParticleTypes.POOF, cx, cy, cz, 25, 0.4D, 0.4D, 0.4D, 0.05D);
                    }
                    case GEO -> {
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, 50, 0.6D, 0.8D, 0.6D, 0.2D);
                        sl.sendParticles(ParticleTypes.CRIT, cx, cy, cz, 30, 0.5D, 0.5D, 0.5D, 0.15D);
                    }
                    case ELECTRO -> {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy, cz, 60, 0.6D, 0.8D, 0.6D, 0.25D);
                        sl.sendParticles(ParticleTypes.FLASH, cx, cy, cz, 2, 0, 0, 0, 0);
                    }
                    case DENDRO -> {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy, cz, 45, 0.6D, 0.8D, 0.6D, 0.1D);
                        sl.sendParticles(ParticleTypes.COMPOSTER, cx, cy, cz, 30, 0.5D, 0.5D, 0.5D, 0.05D);
                    }
                }

                sl.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.2F, 1.0F);
                sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.4F, 1.2F);

                giveItem(sp, new ItemStack(SystemItems.PRIMOGEM.get(), 40));
                giveItem(sp, new ItemStack(SystemItems.INTERTWINED_FATE.get(), 1));
                giveItem(sp, new ItemStack(SystemItems.MYSTIC_ENHANCEMENT_ORE.get(), 5));

                SystemData data = SystemEvents.data(sp);
                if (data != null) {
                    data.addCoins(5000);
                    data.addExperience(600);
                    AwakeningSystemMod.sync(sp);
                }

                sp.sendSystemMessage(Component.translatable("block.awakening_system.shrine_of_depths.seal_broken"));
                sp.sendSystemMessage(Component.translatable("block.awakening_system.shrine_of_depths.reward", element.colorCode + element.name));
                return ItemInteractionResult.SUCCESS;
            } else {
                sl.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 0.8F);
                sp.sendSystemMessage(Component.translatable("block.awakening_system.shrine_of_depths.locked"));
                sp.sendSystemMessage(Component.translatable("block.awakening_system.shrine_of_depths.key_required", requiredKey.getDescription()));
                return ItemInteractionResult.FAIL;
            }
        }

        return ItemInteractionResult.FAIL;
    }

    private static void giveItem(ServerPlayer sp, ItemStack stack) {
        if (!sp.getInventory().add(stack)) {
            sp.drop(stack, false);
        }
        sp.containerMenu.broadcastChanges();
    }
}
