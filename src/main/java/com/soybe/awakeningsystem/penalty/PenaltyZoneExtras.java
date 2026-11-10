package com.soybe.awakeningsystem.penalty;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

/**
 * The penalty zone is a desert: its biomes sit on the Overworld's terrain rules, which only lay sand in
 * Minecraft's own deserts, so every new chunk has its grass and dirt turned to sand and sandstone.
 * Also /penalty leave and /penalty start for operators.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class PenaltyZoneExtras {
    private PenaltyZoneExtras() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level) || !PenaltyZoneManager.isPenaltyDimension(level)) return;
        desertify(event.getChunk());
    }

    private static void desertify(ChunkAccess chunk) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                int depth = 0;
                for (int y = top + 1; y > top - 10 && y > chunk.getMinBuildHeight(); y--) {
                    p.set(x0 + x, y, z0 + z);
                    BlockState s = chunk.getBlockState(p);
                    if (s.isAir()) continue;
                    if (s.is(Blocks.DEAD_BUSH) || s.is(Blocks.CACTUS)) continue;
                    if (s.is(BlockTags.REPLACEABLE) || s.is(BlockTags.FLOWERS) || s.is(BlockTags.SAPLINGS) || s.is(Blocks.SWEET_BERRY_BUSH)) {
                        chunk.setBlockState(p, Blocks.AIR.defaultBlockState(), false);
                        continue;
                    }
                    if (s.is(Blocks.GRASS_BLOCK) || s.is(BlockTags.DIRT) || s.is(Blocks.GRAVEL) || s.is(Blocks.MUD) || s.is(Blocks.CLAY)) {
                        chunk.setBlockState(p, depth < 4 ? Blocks.SAND.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState(), false);
                    }
                    depth++;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("penalty").requires(s -> s.hasPermission(2))
                .then(Commands.literal("leave").executes(ctx -> {
                    // the master way out: counts as surviving it
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    if (!PenaltyZoneManager.isInPenaltyZone(p)) {
                        ctx.getSource().sendFailure(Component.literal("You are not in the penalty zone"));
                        return 0;
                    }
                    PenaltyZoneManager.completePenaltyZone(p, false);
                    return 1;
                }))
                .then(Commands.literal("start").executes(ctx -> {
                    PenaltyZoneManager.teleportToPenaltyZone(ctx.getSource().getPlayerOrException());
                    return 1;
                })));
    }
}
