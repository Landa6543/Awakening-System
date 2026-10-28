package com.soybe.awakeningsystem.casino.darts;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.casino.CasinoChipHelper;
import com.soybe.awakeningsystem.casino.CasinoTrophyManager;
import com.soybe.awakeningsystem.casino.darts.network.DartboardActionC2SPayload;
import com.soybe.awakeningsystem.casino.darts.network.OpenDartboardPayload;
import com.soybe.awakeningsystem.item.CasinoTrophyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DartboardServerHandler {

    public enum SessionMode { SOLO, PVP }
    public enum SessionState { WAITING_FOR_OPPONENT, P1_TURN, P2_TURN, FINISHED }

    public static class DartSession {
        public final BlockPos pos;
        public final SessionMode mode;
        public SessionState state;
        public final int bet;
        public final UUID p1Uuid;
        public final String p1Name;
        public UUID p2Uuid;
        public String p2Name = "";
        public int p1ThrowsLeft = 3;
        public int p1Score = 0;
        public int p2ThrowsLeft = 3;
        public int p2Score = 0;
        public long lastActionTime = System.currentTimeMillis();

        public DartSession(BlockPos pos, SessionMode mode, SessionState state, int bet, ServerPlayer p1) {
            this.pos = pos;
            this.mode = mode;
            this.state = state;
            this.bet = bet;
            this.p1Uuid = p1.getUUID();
            this.p1Name = p1.getName().getString();
        }
    }

    private static final Map<BlockPos, DartSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    private DartboardServerHandler() {}

    public static void openDartboard(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null || player.level() == null) return;
        long chips = CasinoChipHelper.getTotalChipValue(player, false);

        DartSession session = ACTIVE_SESSIONS.get(pos);
        if (session != null && (System.currentTimeMillis() - session.lastActionTime > 180000)) {
            ACTIVE_SESSIONS.remove(pos);
            session = null;
        }

        int pvpState = 0;
        String challengerName = "";
        int challengerBet = 0;
        int pot = 0;
        String statusMsg = "";

        if (session != null) {
            if (session.state == SessionState.WAITING_FOR_OPPONENT) {
                pvpState = 1;
                challengerName = session.p1Name;
                challengerBet = session.bet;
                pot = session.bet * 2;
                statusMsg = challengerName;
            } else if (session.state == SessionState.P1_TURN || session.state == SessionState.P2_TURN) {
                pvpState = 2;
                challengerName = session.p1Name;
                challengerBet = session.bet;
                pot = session.bet * 2;
                String activeThrower = (session.state == SessionState.P1_TURN) ? session.p1Name : session.p2Name;
                statusMsg = activeThrower;
            }
        }

        PacketDistributor.sendToPlayer(player, new OpenDartboardPayload(
                pos, chips, pvpState, challengerName, challengerBet, pot, statusMsg
        ));
    }

    public static void handleAction(ServerPlayer player, DartboardActionC2SPayload payload) {
        if (player == null || payload == null || player.level() == null) return;
        BlockPos pos = payload.pos();

        switch (payload.action()) {
            case DartboardActionC2SPayload.ACTION_START_SOLO -> {
                int bet = Math.max(0, payload.bet());
                if (bet > 0) {
                    if (!CasinoChipHelper.hasEnoughChips(player, bet, false) || !CasinoChipHelper.consumeChipValue(player, bet, false)) {
                        player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
                        return;
                    }
                }

                ensurePlayerHasDarts(player);
                DartSession session = new DartSession(pos, SessionMode.SOLO, SessionState.P1_TURN, bet, player);
                ACTIVE_SESSIONS.put(pos, session);

                player.displayClientMessage(Component.translatable("awakening_system.casino.darts.solo_started"), false);
                player.playNotifySound(com.soybe.awakeningsystem.ModSounds.DART_ANNOUNCER_GAMEON.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
            }

            case DartboardActionC2SPayload.ACTION_CREATE_PVP -> {
                int bet = Math.max(1000, payload.bet());
                if (!CasinoChipHelper.hasEnoughChips(player, bet, false) || !CasinoChipHelper.consumeChipValue(player, bet, false)) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
                    return;
                }

                DartSession session = new DartSession(pos, SessionMode.PVP, SessionState.WAITING_FOR_OPPONENT, bet, player);
                ACTIVE_SESSIONS.put(pos, session);

                ensurePlayerHasDarts(player);
                player.displayClientMessage(Component.translatable("awakening_system.casino.darts.pvp_created", String.format(Locale.US, "%,d", bet)), false);

                for (ServerPlayer other : player.serverLevel().players()) {
                    if (other.distanceToSqr(player) <= 576.0 && !other.getUUID().equals(player.getUUID())) {
                        other.displayClientMessage(Component.translatable("awakening_system.casino.darts.pvp_announcement", player.getName().getString(), String.format(Locale.US, "%,d", bet)), false);
                        other.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }

            case DartboardActionC2SPayload.ACTION_JOIN_PVP -> {
                DartSession session = ACTIVE_SESSIONS.get(pos);
                if (session == null || session.state != SessionState.WAITING_FOR_OPPONENT) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.darts.no_active_challenge"), true);
                    return;
                }
                if (session.p1Uuid.equals(player.getUUID())) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.darts.cannot_join_self"), true);
                    return;
                }

                if (!CasinoChipHelper.hasEnoughChips(player, session.bet, false) || !CasinoChipHelper.consumeChipValue(player, session.bet, false)) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.darts.insufficient_chips_join", String.format(Locale.US, "%,d", session.bet)), true);
                    return;
                }

                session.p2Uuid = player.getUUID();
                session.p2Name = player.getName().getString();
                session.state = SessionState.P1_TURN;
                session.lastActionTime = System.currentTimeMillis();

                ensurePlayerHasDarts(player);

                ServerPlayer p1 = player.serverLevel().getServer().getPlayerList().getPlayer(session.p1Uuid);
                int pot = session.bet * 2;
                Component startMsg = Component.translatable("awakening_system.casino.darts.duel_started", session.p1Name, session.p2Name, String.format(Locale.US, "%,d", pot));
                Component turnMsg = Component.translatable("awakening_system.casino.darts.turn_1_notice", session.p1Name);

                if (p1 != null) {
                    p1.displayClientMessage(startMsg, false);
                    p1.displayClientMessage(turnMsg, false);
                    p1.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                }
                player.displayClientMessage(startMsg, false);
                player.displayClientMessage(turnMsg, false);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
            }

            case DartboardActionC2SPayload.ACTION_CANCEL_PVP -> {
                DartSession session = ACTIVE_SESSIONS.get(pos);
                if (session != null && session.state == SessionState.WAITING_FOR_OPPONENT && session.p1Uuid.equals(player.getUUID())) {
                    CasinoChipHelper.giveChips(player, session.bet, false);
                    ACTIVE_SESSIONS.remove(pos);
                    player.displayClientMessage(Component.translatable("awakening_system.casino.darts.pvp_cancelled", String.format(Locale.US, "%,d", session.bet)), true);
                    player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    public static void onDartHit(BlockPos pos, ServerPlayer shooter, int score, Component hitDesc) {
        if (pos == null || shooter == null) return;
        DartSession session = ACTIVE_SESSIONS.get(pos);
        if (session == null) return;

        session.lastActionTime = System.currentTimeMillis();

        if (session.mode == SessionMode.SOLO) {
            if (!shooter.getUUID().equals(session.p1Uuid)) return;

            session.p1ThrowsLeft--;
            session.p1Score += score;
            int throwIdx = 3 - session.p1ThrowsLeft;

            shooter.displayClientMessage(Component.translatable("awakening_system.casino.darts.throw_status", throwIdx, hitDesc, session.p1Score), true);

            if (session.p1ThrowsLeft <= 0) {
                concludeSoloSession(shooter, session);
                ACTIVE_SESSIONS.remove(pos);
            }
        } else if (session.mode == SessionMode.PVP) {
            ServerPlayer p1 = shooter.serverLevel().getServer().getPlayerList().getPlayer(session.p1Uuid);
            ServerPlayer p2 = (session.p2Uuid != null) ? shooter.serverLevel().getServer().getPlayerList().getPlayer(session.p2Uuid) : null;

            if (session.state == SessionState.P1_TURN && shooter.getUUID().equals(session.p1Uuid)) {
                session.p1ThrowsLeft--;
                session.p1Score += score;
                int throwIdx = 3 - session.p1ThrowsLeft;

                Component throwMsg = Component.translatable("awakening_system.casino.darts.pvp_throw_status", session.p1Name, throwIdx, hitDesc, session.p1Score);
                shooter.displayClientMessage(throwMsg, true);
                if (p2 != null) p2.displayClientMessage(throwMsg, false);

                if (session.p1ThrowsLeft <= 0) {
                    session.state = SessionState.P2_TURN;
                    Component switchMsg = Component.translatable("awakening_system.casino.darts.pvp_turn_switch", session.p1Name, session.p1Score, session.p2Name);
                    shooter.displayClientMessage(switchMsg, false);
                    if (p2 != null) {
                        p2.displayClientMessage(switchMsg, false);
                        p2.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                    }
                }
            } else if (session.state == SessionState.P2_TURN && session.p2Uuid != null && shooter.getUUID().equals(session.p2Uuid)) {
                session.p2ThrowsLeft--;
                session.p2Score += score;
                int throwIdx = 3 - session.p2ThrowsLeft;

                Component throwMsg = Component.translatable("awakening_system.casino.darts.pvp_throw_status", session.p2Name, throwIdx, hitDesc, session.p2Score);
                shooter.displayClientMessage(throwMsg, true);
                if (p1 != null) p1.displayClientMessage(throwMsg, false);

                if (session.p2ThrowsLeft <= 0) {
                    concludePvpMatch(p1, p2, session);
                    ACTIVE_SESSIONS.remove(pos);
                }
            }
        }
    }

    private static void concludeSoloSession(ServerPlayer player, DartSession session) {
        int finalScore = session.p1Score;
        int bet = session.bet;
        long payout = 0;
        String gradeKey;

        SystemData data = SystemEvents.data(player);
        data.recordDartScore(finalScore);

        if (finalScore >= 160) {
            gradeKey = "awakening_system.casino.darts.grade.legendary";
            payout = (bet > 0) ? (bet * 25L) : 25000L;
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (finalScore == 180) {
                for (ServerPlayer other : player.serverLevel().players()) {
                    if (other.distanceToSqr(player) <= 1024.0) {
                        other.sendSystemMessage(Component.translatable("awakening_system.casino.darts.announcer_180", player.getName().getString()));
                        other.playNotifySound(com.soybe.awakeningsystem.ModSounds.DART_ANNOUNCER_180.get(), SoundSource.PLAYERS, 1.4F, 1.0F);
                    }
                }
                ItemStack trophy = CasinoTrophyItem.createTrophy(
                        1, player.getName().getString(), "180 Puntos Perfectos (3x Triple 20) en Diana de Sisal"
                );
                if (!player.getInventory().add(trophy)) {
                    player.drop(trophy, false);
                }
                player.sendSystemMessage(Component.translatable("awakening_system.casino.darts.trophy_received"));
            }
        } else if (finalScore >= 120) {
            gradeKey = "awakening_system.casino.darts.grade.master";
            payout = (bet > 0) ? (bet * 8L) : 10000L;
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        } else if (finalScore >= 80) {
            gradeKey = "awakening_system.casino.darts.grade.expert";
            payout = (bet > 0) ? (bet * 3L) : 5000L;
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        } else if (finalScore >= 45) {
            gradeKey = "awakening_system.casino.darts.grade.solid";
            payout = (bet > 0) ? (long)(bet * 1.5) : 2000L;
        } else {
            gradeKey = "awakening_system.casino.darts.grade.finished";
            payout = 0;
        }

        if (payout > 0) {
            CasinoChipHelper.giveChips(player, payout, false);
            data.addCasinoWinnings(payout);
            AwakeningSystemMod.sync(player);
        }

        Component gradeComp = Component.translatable(gradeKey);
        Component rewardComp = payout > 0
                ? Component.translatable("awakening_system.casino.darts.reward_obtained", String.format(Locale.US, "%,d", payout))
                : Component.translatable("awakening_system.casino.darts.no_reward");

        player.displayClientMessage(Component.translatable("awakening_system.casino.darts.solo_summary",
                finalScore, data.getDartHighScore(), gradeComp, rewardComp), false);
    }

    private static void concludePvpMatch(ServerPlayer p1, ServerPlayer p2, DartSession session) {
        int pot = session.bet * 2;

        if (session.p1Score > session.p2Score) {
            Component banner = Component.translatable("awakening_system.casino.darts.pvp_winner",
                    session.p1Name, session.p1Score, session.p2Score, String.format(Locale.US, "%,d", pot));
            if (p1 != null) {
                CasinoChipHelper.giveChips(p1, pot, false);
                SystemData data = SystemEvents.data(p1);
                data.addCasinoWinnings(pot);
                data.recordDartScore(session.p1Score);
                AwakeningSystemMod.sync(p1);
                p1.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (pot >= 20000 && p1.level() instanceof ServerLevel sl) {
                    CasinoTrophyManager.tryClaimTrophy(sl, p1, 5, "PvP Dart Duel Champion (" + String.format(Locale.US, "%,d", pot) + " Chips)");
                }
            }
            if (p2 != null) {
                SystemEvents.data(p2).recordDartScore(session.p2Score);
                p2.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
            if (p1 != null) p1.displayClientMessage(banner, false);
            if (p2 != null) p2.displayClientMessage(banner, false);
        } else if (session.p2Score > session.p1Score) {
            Component banner = Component.translatable("awakening_system.casino.darts.pvp_winner",
                    session.p2Name, session.p2Score, session.p1Score, String.format(Locale.US, "%,d", pot));
            if (p2 != null) {
                CasinoChipHelper.giveChips(p2, pot, false);
                SystemData data = SystemEvents.data(p2);
                data.addCasinoWinnings(pot);
                data.recordDartScore(session.p2Score);
                AwakeningSystemMod.sync(p2);
                p2.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (pot >= 20000 && p2.level() instanceof ServerLevel sl) {
                    CasinoTrophyManager.tryClaimTrophy(sl, p2, 5, "PvP Dart Duel Champion (" + String.format(Locale.US, "%,d", pot) + " Chips)");
                }
            }
            if (p1 != null) {
                SystemEvents.data(p1).recordDartScore(session.p1Score);
                p1.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
            if (p1 != null) p1.displayClientMessage(banner, false);
            if (p2 != null) p2.displayClientMessage(banner, false);
        } else {
            Component banner = Component.translatable("awakening_system.casino.darts.pvp_tie", session.p1Score, session.p2Score);
            if (p1 != null) {
                CasinoChipHelper.giveChips(p1, session.bet, false);
                p1.displayClientMessage(banner, false);
            }
            if (p2 != null) {
                CasinoChipHelper.giveChips(p2, session.bet, false);
                p2.displayClientMessage(banner, false);
            }
        }
    }

    private static void ensurePlayerHasDarts(ServerPlayer player) {
        int dartCount = 0;
        int dartSlot = -1;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack st = player.getInventory().items.get(i);
            if (!st.isEmpty() && st.is(SystemItems.DART.get())) {
                dartCount += st.getCount();
                if (dartSlot < 0) dartSlot = i;
            }
        }
        if (dartCount < 3) {
            ItemStack addStack = new ItemStack(SystemItems.DART.get(), 3 - dartCount);
            if (!player.getInventory().add(addStack)) {
                player.drop(addStack, false);
            }
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(SystemItems.DART.get())) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack st = player.getInventory().items.get(i);
                if (!st.isEmpty() && st.is(SystemItems.DART.get())) {
                    dartSlot = i;
                    break;
                }
            }
            if (dartSlot >= 0) {
                ItemStack darts = player.getInventory().items.get(dartSlot);
                player.getInventory().items.set(dartSlot, mainHand);
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, darts);
            }
        }

        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }
}
