package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.item.CelestialDragonTributeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Altar del Dragón Ancestral:
 * Bloque ceremonial sagrado de los Nueve Cielos ubicado en el Santuario de Murim.
 * Al ofrendar el Tributo del Dragón Celestial, desata la tormenta de Qi e invoca
 * al Dragón Celestial Ancestral de más de 100 bloques de longitud.
 */
public class CelestialDragonAltarBlock extends Block {
    public static final MapCodec<CelestialDragonAltarBlock> CODEC = simpleCodec(CelestialDragonAltarBlock::new);

    public CelestialDragonAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof CelestialDragonTributeItem || (SystemItems.CELESTIAL_DRAGON_TRIBUTE.isBound() && stack.is(SystemItems.CELESTIAL_DRAGON_TRIBUTE.get()))) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }

            ServerLevel serverLevel = (ServerLevel) level;

            // Verificar si el dragón jefe ya existe en el área (radio 350 bloques)
            AABB searchBox = new AABB(pos).inflate(350.0D);
            List<AncientCelestialDragonBossEntity> existingDragons = serverLevel.getEntitiesOfClass(
                    AncientCelestialDragonBossEntity.class, searchBox, AncientCelestialDragonBossEntity::isAlive);

            if (!existingDragons.isEmpty()) {
                player.sendSystemMessage(Component.translatable("awakening_system.altar.celestial_dragon_active").withStyle(ChatFormatting.RED));
                serverLevel.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 0.8F);
                return ItemInteractionResult.FAIL;
            }

            // Consumir el ítem de tributo
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            // 1. Desatar tormenta cósmica de Qi
            serverLevel.setWeatherParameters(0, 12000, true, true);

            // 2. Rayos ceremoniales en los 4 puntos cardinales del santuario
            int[][] offsets = {{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
            for (int[] off : offsets) {
                BlockPos strikePos = pos.offset(off[0], 0, off[1]);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
                    bolt.setVisualOnly(true);
                    serverLevel.addFreshEntity(bolt);
                }
            }

            // 3. Sonidos climáticos retumbantes
            serverLevel.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.AMBIENT, 3.0F, 0.8F);
            serverLevel.playSound(null, pos, ModSounds.CELESTIAL_DRAGON_ROAR.get(), SoundSource.HOSTILE, 4.0F, 0.7F);

            // 4. Invocar al Titán: Dragón Celestial Ancestral en picada vertical desde el cielo (cabeza abajo)
            double spawnX = pos.getX() + 0.5D;
            double spawnY = Math.min(serverLevel.getMaxBuildHeight() - 10, Math.max(pos.getY() + 90, 225));
            double spawnZ = pos.getZ() + 0.5D;

            AncientCelestialDragonBossEntity boss = ModEntities.ANCIENT_CELESTIAL_DRAGON.get().create(serverLevel);
            if (boss != null) {
                boss.moveTo(spawnX, spawnY, spawnZ, player.getYRot(), 85.0F); // Pitch 85 = cabeza apuntando hacia abajo en picada
                boss.setHomeSanctuaryPos(pos);
                boss.triggerDescentRoar();
                serverLevel.addFreshEntity(boss);

                // Enviar cinemática de cámara con vista al cielo y temblor de pantalla a jugadores cercanos
                com.soybe.awakeningsystem.BossCinematicPayload cinematicPayload =
                        new com.soybe.awakeningsystem.BossCinematicPayload(pos, 160, boss.getId());
                for (ServerPlayer sp : serverLevel.players()) {
                    if (sp.distanceToSqr(Vec3.atCenterOf(pos)) <= 250.0D * 250.0D) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, cinematicPayload);
                    }
                }
            }

            // 5. Anuncio en toda la dimensión
            Component announcement = Component.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    .append(Component.translatable("awakening_system.altar.celestial_dragon_descended_title").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                    .append(Component.translatable("awakening_system.altar.celestial_dragon_descended_desc"))
                    .append(Component.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

            for (ServerPlayer sp : serverLevel.players()) {
                sp.sendSystemMessage(announcement);
                sp.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5F, 0.8F);
            }

            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 1.0D;
        double pz = pos.getZ() + 0.5D;

        // Estela mística de Qi ascendente
        if (random.nextFloat() < 0.45F) {
            double rx = px + (random.nextDouble() - 0.5D) * 0.8D;
            double rz = pz + (random.nextDouble() - 0.5D) * 0.8D;
            level.addParticle(ParticleTypes.END_ROD, rx, py, rz, 0.0D, 0.05D, 0.0D);
            level.addParticle(ParticleTypes.DRAGON_BREATH, rx, py, rz, (random.nextDouble() - 0.5D) * 0.02D, 0.04D, (random.nextDouble() - 0.5D) * 0.02D);
        }

        if (random.nextFloat() < 0.2F) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py + 0.2D, pz, 0.0D, 0.03D, 0.0D);
        }
    }
}
