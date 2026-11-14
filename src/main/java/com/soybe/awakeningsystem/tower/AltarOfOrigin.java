package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.block.PotentialCrystalBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * The measuring ritual of the Altar of Origin. The crystal climbs rank by rank (F, E, D...) lighting up in
 * each colour until it reaches the player's potential; an EX potential overloads it and the cube cracks.
 * Measuring awakens the System, and the potential stays for good.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class AltarOfOrigin {
    private AltarOfOrigin() {}

    private static final int CHARGE_TICKS = 40;
    private static final int STEP_TICKS = 14;
    private static final int OVERLOAD_TICKS = 40;
    private static final int HOLD_TICKS = 120;
    /** Odds of each potential, F..EX, in percent. */
    private static final int[] WEIGHTS = {20, 22, 20, 16, 11, 6, 4, 1};

    private static final class Measurement {
        final ServerLevel level;
        final BlockPos pos;
        final UUID player;
        final int target;        // 0..7 = F..EX
        final boolean replay;    // already measured: only show it again
        int tick;
        int shown;               // current RANK on the block
        boolean revealed;

        Measurement(ServerLevel level, BlockPos pos, UUID player, int target, boolean replay) {
            this.level = level;
            this.pos = pos;
            this.player = player;
            this.target = target;
            this.replay = replay;
        }
    }

    private static final Map<BlockPos, Measurement> ACTIVE = new HashMap<>();

    public static void touch(ServerPlayer player, BlockPos pos) {
        if (ACTIVE.containsKey(pos)) {
            player.displayClientMessage(Component.translatable("awakening_system.altar.busy"), true);
            return;
        }
        SystemData data = SystemEvents.data(player);
        int target;
        boolean replay = data.potentialMeasured();
        if (replay) {
            target = Math.max(0, SystemData.potentialIndex(data.potentialRank()));
        } else {
            target = roll(player.getRandom());
        }
        ACTIVE.put(pos.immutable(), new Measurement(player.serverLevel(), pos.immutable(), player.getUUID(), target, replay));
        ServerLevel level = player.serverLevel();
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 0.7F);
        if (!replay) player.displayClientMessage(Component.translatable("awakening_system.altar.start"), true);
    }

    /** For testing: measures with a chosen result. */
    public static void touchWithResult(ServerPlayer player, BlockPos pos, int target) {
        ACTIVE.put(pos.immutable(), new Measurement(player.serverLevel(), pos.immutable(), player.getUUID(), target, false));
    }

    private static int roll(RandomSource random) {
        int total = 0;
        for (int w : WEIGHTS) total += w;
        int r = random.nextInt(total);
        for (int i = 0; i < WEIGHTS.length; i++) {
            r -= WEIGHTS[i];
            if (r < 0) return i;
        }
        return 0;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();
        Iterator<Measurement> it = ACTIVE.values().iterator();
        while (it.hasNext()) {
            Measurement m = it.next();
            if (!m.level.isLoaded(m.pos) || !m.level.getBlockState(m.pos).is(SystemBlocks.POTENTIAL_CRYSTAL.get())) {
                it.remove();
                continue;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(m.player);
            if (!m.revealed && (player == null || player.level() != m.level || player.distanceToSqr(m.pos.getCenter()) > 24 * 24)) {
                setShown(m, 0);
                it.remove();
                continue;
            }
            if (tick(m, player)) {
                // once the ritual is over, the System greets its new Player
                if (!m.replay && player != null) SystemEvents.data(player).setPendingWelcomePacket(true);
                setShown(m, 0);
                it.remove();
            }
        }
    }

    /** One tick of a measurement; true when it is over. */
    private static boolean tick(Measurement m, ServerPlayer player) {
        m.tick++;
        ServerLevel level = m.level;
        double cx = m.pos.getX() + 0.5D, cy = m.pos.getY() + 1.62D, cz = m.pos.getZ() + 0.5D;
        boolean ex = m.target == 7;
        int climbEnd = CHARGE_TICKS + STEP_TICKS * (Math.min(m.target, 6) + 1);
        int revealAt = climbEnd + (ex ? OVERLOAD_TICKS : 10);

        if (m.tick <= CHARGE_TICKS) {
            // mana gathering into the cube
            if (m.tick % 4 == 0) level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.6D, cz, 5, 1.2D, 0.8D, 1.2D, 0.6D);
            if (m.tick == CHARGE_TICKS / 2) level.playSound(null, m.pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.4F, 0.6F);
            return false;
        }
        if (m.tick <= climbEnd) {
            int step = (m.tick - CHARGE_TICKS - 1) / STEP_TICKS;          // 0..min(target,6)
            int rank = Math.min(step, 6) + 1;                              // RANK 1..7 = F..S
            if (rank != m.shown) {
                setShown(m, rank);
                float pitch = 0.6F + step * 0.18F;
                level.playSound(null, m.pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2.0F, pitch);
                level.playSound(null, m.pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.0F, pitch);
                burst(level, cx, cy, cz, PotentialCrystalBlock.COLORS[rank], 14 + step * 4, 0.5D + step * 0.08D);
            }
            if (m.tick % 8 == 0) ring(level, cx, cy, cz, PotentialCrystalBlock.COLORS[m.shown], 0.8D + (m.tick % 30) / 30.0D);
            return false;
        }
        if (!m.revealed && ex && m.tick < revealAt) {
            // the cube cannot hold it: it trembles, flickers and screams
            int t = m.tick - climbEnd;
            if (t % 4 == 0) setShown(m, t % 8 == 0 ? 7 : 8);
            if (t % 5 == 0) level.playSound(null, m.pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.5F, 0.5F + t * 0.02F);
            if (t % 3 == 0) level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 1, 0.3D, 0.3D, 0.3D, 0.15D);
            if (t % 2 == 0) level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy, cz, 2, 0.4D, 0.4D, 0.4D, 0.3D);
            return false;
        }
        if (!m.revealed && m.tick >= revealAt) {
            m.revealed = true;
            reveal(m, player);
            return false;
        }
        // hold the final colour a while, then the cube goes dormant again
        if (m.tick % 12 == 0) ring(level, cx, cy, cz, PotentialCrystalBlock.COLORS[m.shown], 1.0D);
        if (ex && m.tick % 6 == 0) level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy, cz, 2, 0.3D, 0.3D, 0.3D, 0.1D);
        return m.tick > revealAt + HOLD_TICKS + (ex ? 100 : 0);
    }

    private static void reveal(Measurement m, ServerPlayer player) {
        ServerLevel level = m.level;
        double cx = m.pos.getX() + 0.5D, cy = m.pos.getY() + 1.62D, cz = m.pos.getZ() + 0.5D;
        String rank = SystemData.POTENTIAL_RANKS[m.target];
        boolean ex = m.target == 7;
        setShown(m, ex ? 8 : m.target + 1);
        int color = PotentialCrystalBlock.COLORS[ex ? 8 : m.target + 1];
        if (ex) {
            level.playSound(null, m.pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 3.0F, 0.5F);
            level.playSound(null, m.pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.2F, 1.4F);
            level.playSound(null, m.pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.6F, 1.6F);
            level.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 2, 0.2D, 0.2D, 0.2D, 0.0D);
            level.sendParticles(ParticleTypes.FLASH, cx, cy, cz, 1, 0, 0, 0, 0);
        } else {
            level.playSound(null, m.pos, m.target >= 5 ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.5F, 1.0F);
        }
        burst(level, cx, cy, cz, color, 60 + m.target * 15, 0.9D);
        if (player == null) return;

        Component rankText = Component.translatable("awakening_system.altar.rank", rank).withColor(color);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
        player.connection.send(new ClientboundSetTitleTextPacket(rankText));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(
                m.replay ? "awakening_system.altar.subtitle_replay" : "awakening_system.altar.subtitle")));
        if (m.replay) return;

        SystemData data = SystemEvents.data(player);
        // a greeting may already be waiting since the player first joined; it must not cover the rank
        // the cube is showing now. The greeting is asked for again once the cube has gone quiet.
        data.setPendingWelcomePacket(false);
        data.awakenWithPotential(rank);
        player.sendSystemMessage(Component.translatable("awakening_system.altar.result." + rank.toLowerCase(java.util.Locale.ROOT)).withColor(color));
        player.sendSystemMessage(Component.translatable("awakening_system.altar.bonus",
                String.valueOf(SystemData.potentialBonusPoints(rank)),
                String.valueOf(Math.round((SystemData.potentialExpMultiplier(rank) - 1.0D) * 100.0D)),
                String.valueOf(SystemData.potentialPointsPerLevel(rank))));
        AwakeningSystemMod.notify(player, "awakening_system.notification.awakening_complete.title",
                "awakening_system.altar.awakened", 0xFF70E8FF);
        SystemEvents.refreshPlayer(player);    // the potential's passive bonuses take hold now
        AwakeningSystemMod.sync(player);
        if (m.target >= 6) {
            // S and EX are news for everyone on the server
            Component news = Component.translatable("awakening_system.altar.broadcast", player.getDisplayName(), rank).withColor(color);
            for (ServerPlayer other : level.getServer().getPlayerList().getPlayers()) if (other != player) other.sendSystemMessage(news);
        }
    }

    private static void setShown(Measurement m, int rank) {
        m.shown = rank;
        BlockState state = m.level.getBlockState(m.pos);
        if (state.is(SystemBlocks.POTENTIAL_CRYSTAL.get()) && state.getValue(PotentialCrystalBlock.RANK) != rank)
            m.level.setBlock(m.pos, state.setValue(PotentialCrystalBlock.RANK, rank), 3);
    }

    private static DustParticleOptions dust(int color, float size) {
        return new DustParticleOptions(new Vector3f(((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F, (color & 255) / 255.0F), size);
    }

    private static void burst(ServerLevel level, double x, double y, double z, int color, int count, double speed) {
        level.sendParticles(dust(color, 1.6F), x, y, z, Math.max(3, count / 3), 0.4D, 0.4D, 0.4D, speed);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, Math.max(1, count / 10), 0.2D, 0.2D, 0.2D, speed * 0.3D);
    }

    private static void ring(ServerLevel level, double x, double y, double z, int color, double r) {
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0D + level.getGameTime() * 0.15D;
            level.sendParticles(dust(color, 1.0F), x + Math.cos(a) * r, y - 0.3D, z + Math.sin(a) * r, 1, 0, 0.05D, 0, 0.0D);
        }
    }
}
