package com.soybe.awakeningsystem.casino;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.casino.blackjack.BlackjackGame;
import com.soybe.awakeningsystem.casino.blackjack.BlackjackPvPGame;
import com.soybe.awakeningsystem.casino.network.BlackjackActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.BlackjackPvPActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.BlackjackPvPStateS2CPayload;
import com.soybe.awakeningsystem.casino.network.BlackjackStateS2CPayload;
import com.soybe.awakeningsystem.casino.network.OpenBlackjackPayload;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BlackjackServerHandler {
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);
    private static final Map<UUID, BlackjackGame> ACTIVE_GAMES = new ConcurrentHashMap<>();
    private static final Map<BlockPos, BlackjackPvPGame> TABLE_PVP_GAMES = new ConcurrentHashMap<>();

    private BlackjackServerHandler() {}

    public static void openTable(ServerPlayer player, BlockPos pos) {
        boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;
        boolean isVip = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.VipBlackjackTableBlock;
        long chipBalance = CasinoChipHelper.getTotalChipValue(player, isIllegal);

        int bronze = CasinoChipHelper.countChips(player, CasinoChipItem.ChipTier.BRONZE);
        int silver = CasinoChipHelper.countChips(player, CasinoChipItem.ChipTier.SILVER);
        int gold = CasinoChipHelper.countChips(player, CasinoChipItem.ChipTier.GOLD);
        int platinum = CasinoChipHelper.countChips(player, CasinoChipItem.ChipTier.PLATINUM);
        int black = CasinoChipHelper.countChips(player, CasinoChipItem.ChipTier.BLACK);

        BlackjackGame existingGame = ACTIVE_GAMES.get(player.getUUID());
        if (existingGame == null || existingGame.getState() == BlackjackGame.State.FINISHED) {
            existingGame = new BlackjackGame();
            ACTIVE_GAMES.put(player.getUUID(), existingGame);
        }

        PacketDistributor.sendToPlayer(player, new OpenBlackjackPayload(
                pos, chipBalance, bronze, silver, gold, platinum, isIllegal ? black : 0, isIllegal, isVip
        ));

        // If the player had an ongoing active hand, restore and synchronize it so it doesn't close or cancel
        if (existingGame.getState() != BlackjackGame.State.FINISHED && existingGame.getState() != BlackjackGame.State.BETTING) {
            syncState(player, pos, existingGame);
        }

        BlackjackPvPGame pvpGame = TABLE_PVP_GAMES.get(pos);
        if (pvpGame != null) {
            if (pvpGame.getState() == BlackjackPvPGame.State.WAITING_FOR_OPPONENT ||
                player.getUUID().equals(pvpGame.getHostUuid()) ||
                player.getUUID().equals(pvpGame.getOpponentUuid()) ||
                pvpGame.getState() != BlackjackPvPGame.State.FINISHED) {
                syncPvPState(player, pos, pvpGame);
            } else {
                TABLE_PVP_GAMES.remove(pos);
            }
        }
    }

    public static void handleAction(ServerPlayer player, BlackjackActionC2SPayload payload) {
        if (player == null || payload == null || player.level() == null) return;
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;

        BlackjackGame game = ACTIVE_GAMES.computeIfAbsent(player.getUUID(), k -> new BlackjackGame());
        boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;

        switch (payload.actionType()) {
            case BlackjackActionC2SPayload.ACTION_BET -> {
                long bet = payload.betAmount();
                if (bet <= 0) return;

                if (payload.chipTier() >= 0 && payload.chipTier() < CasinoChipItem.ChipTier.values().length) {
                    CasinoChipItem.ChipTier tier = CasinoChipItem.ChipTier.values()[payload.chipTier()];
                    if (!isIllegal && tier == CasinoChipItem.ChipTier.BLACK) {
                        player.displayClientMessage(Component.translatable("awakening_system.casino.illegal_chip_rejected"), true);
                        return;
                    }
                    if (!CasinoChipHelper.removeChips(player, tier, 1)) {
                        player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
                        return;
                    }
                    bet = tier.getMoraValue();
                } else {
                    if (!CasinoChipHelper.consumeChipValue(player, bet, isIllegal)) {
                        player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
                        return;
                    }
                }

                game.startRound(bet);
                player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9F, 1.1F);
                checkRoundEndRewards(player, game, payload.pos());
            }

            case BlackjackActionC2SPayload.ACTION_HIT -> {
                if (game.getState() == BlackjackGame.State.PLAYER_TURN) {
                    game.hit();
                    player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9F, 1.1F);
                    checkRoundEndRewards(player, game, payload.pos());
                }
            }

            case BlackjackActionC2SPayload.ACTION_STAND -> {
                if (game.getState() == BlackjackGame.State.PLAYER_TURN) {
                    game.stand();
                    player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
                    checkRoundEndRewards(player, game, payload.pos());
                }
            }

            case BlackjackActionC2SPayload.ACTION_DOUBLE -> {
                if (game.getState() == BlackjackGame.State.PLAYER_TURN && game.getPlayerHand().size() == 2) {
                    long additionalBet = game.getCurrentBet();
                    if (!CasinoChipHelper.consumeChipValue(player, additionalBet, isIllegal)) {
                        player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips_double"), true);
                        return;
                    }
                    game.doubleDown();
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                    checkRoundEndRewards(player, game, payload.pos());
                }
            }

            case BlackjackActionC2SPayload.ACTION_NEW_ROUND -> {
                game = new BlackjackGame();
                ACTIVE_GAMES.put(player.getUUID(), game);
            }
        }

        syncState(player, payload.pos(), game);
    }

    private static void checkRoundEndRewards(ServerPlayer player, BlackjackGame game, BlockPos pos) {
        if (game.getState() == BlackjackGame.State.FINISHED) {
            long payout = game.getTotalPayout();
            SystemData data = SystemEvents.data(player);
            boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;
            boolean isVip = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.VipBlackjackTableBlock;

            if (isVip && game.getResult() == BlackjackGame.Result.PLAYER_BLACKJACK) {
                // Natural Blackjack VIP paga 3.0x apuesta base (en lugar de 1.5x)
                payout = (long) (game.getCurrentBet() * 3.0);
            }

            if (isVip && payout > 0 && game.getResult() != BlackjackGame.Result.PUSH) {
                // Bono de Realeza VIP de Alta Gama: +50% adicional en Fichas
                long vipBonus = (long) (payout * 0.50);
                payout += vipBonus;
                player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack_vip_bonus", NF.format(vipBonus), NF.format(payout)), true);
            }

            if (payout > 0) {
                CasinoChipHelper.giveChips(player, payout, isIllegal);
                data.addCasinoWinnings(payout);
                AwakeningSystemMod.sync(player);
                if (game.getResult() == BlackjackGame.Result.PLAYER_BLACKJACK) {
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    spawnWinParticles(player, pos, true);
                    if (player.level() instanceof ServerLevel sl) {
                        CasinoTrophyManager.tryClaimTrophy(sl, player, 2, "Blackjack Natural 21 " + (isVip ? "VIP" : "Oficial") + " (" + NF.format(payout) + " Fichas)");
                    }
                } else if (game.getResult() == BlackjackGame.Result.PUSH) {
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
                } else {
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.2F);
                    spawnWinParticles(player, pos, isVip);
                }
            } else {
                player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 0.9F);
            }
        }
    }

    public static void handlePvPAction(ServerPlayer player, BlackjackPvPActionC2SPayload payload) {
        if (player == null || payload == null || player.level() == null) return;
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;

        switch (payload.action()) {
            case BlackjackPvPActionC2SPayload.ACTION_CREATE -> {
                long bet = Math.max(1000, payload.betAmount());
                if (!CasinoChipHelper.hasEnoughChips(player, bet, isIllegal)) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.not_enough_chips_create", NF.format(bet)), true);
                    return;
                }

                BlackjackPvPGame existing = TABLE_PVP_GAMES.get(pos);
                if (existing != null && existing.getState() != BlackjackPvPGame.State.FINISHED) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.game_in_progress"), true);
                    return;
                }

                if (!CasinoChipHelper.consumeChipValue(player, bet, isIllegal)) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
                    return;
                }

                BlackjackPvPGame newGame = new BlackjackPvPGame(player.getUUID(), player.getGameProfile().getName(), bet);
                TABLE_PVP_GAMES.put(pos, newGame);
                syncPvPState(player, pos, newGame);

                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
                player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.created", NF.format(bet)), false);
            }

            case BlackjackPvPActionC2SPayload.ACTION_JOIN -> {
                BlackjackPvPGame game = TABLE_PVP_GAMES.get(pos);
                if (game == null || game.getState() != BlackjackPvPGame.State.WAITING_FOR_OPPONENT) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.no_challenge"), true);
                    return;
                }
                if (player.getUUID().equals(game.getHostUuid())) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.cannot_join_self"), true);
                    return;
                }
                if (!CasinoChipHelper.hasEnoughChips(player, game.getBetAmount(), isIllegal)) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.not_enough_chips_join", NF.format(game.getBetAmount())), true);
                    return;
                }

                if (!CasinoChipHelper.consumeChipValue(player, game.getBetAmount(), isIllegal)) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
                    return;
                }

                game.startMatch(player.getUUID(), player.getGameProfile().getName());

                ServerPlayer host = server.getPlayerList().getPlayer(game.getHostUuid());
                if (host != null) {
                    syncPvPState(host, pos, game);
                    host.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                    host.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.opponent_joined", player.getName().getString()), false);
                }

                syncPvPState(player, pos, game);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

                checkPvPRoundEnd(server, game, pos);
            }

            case BlackjackPvPActionC2SPayload.ACTION_CANCEL -> {
                BlackjackPvPGame game = TABLE_PVP_GAMES.get(pos);
                if (game != null && game.getState() == BlackjackPvPGame.State.WAITING_FOR_OPPONENT && player.getUUID().equals(game.getHostUuid())) {
                    CasinoChipHelper.giveChips(player, game.getBetAmount(), isIllegal);
                    TABLE_PVP_GAMES.remove(pos);

                    long chips = CasinoChipHelper.getTotalChipValue(player, isIllegal);
                    PacketDistributor.sendToPlayer(player, new BlackjackPvPStateS2CPayload(
                            pos, -1, 0, "", "", false, false, 0, 0, 0, 0, new int[0], new int[0], "Duel cancelled", chips
                    ));
                    player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
                    player.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.cancelled"), true);
                }
            }

            case BlackjackPvPActionC2SPayload.ACTION_HIT -> {
                BlackjackPvPGame game = TABLE_PVP_GAMES.get(pos);
                if (game != null && game.isPlayerTurn(player.getUUID())) {
                    game.hit(player.getUUID());
                    player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9F, 1.2F);

                    if (game.getState() == BlackjackPvPGame.State.FINISHED) {
                        checkPvPRoundEnd(server, game, pos);
                    } else {
                        broadcastPvPState(server, game, pos);
                    }
                }
            }

            case BlackjackPvPActionC2SPayload.ACTION_STAND -> {
                BlackjackPvPGame game = TABLE_PVP_GAMES.get(pos);
                if (game != null && game.isPlayerTurn(player.getUUID())) {
                    game.stand(player.getUUID());
                    player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.0F);

                    if (game.getState() == BlackjackPvPGame.State.FINISHED) {
                        checkPvPRoundEnd(server, game, pos);
                    } else {
                        broadcastPvPState(server, game, pos);
                    }
                }
            }
        }
    }

    private static void broadcastPvPState(MinecraftServer server, BlackjackPvPGame game, BlockPos pos) {
        ServerPlayer host = server.getPlayerList().getPlayer(game.getHostUuid());
        ServerPlayer opp = game.getOpponentUuid() != null ? server.getPlayerList().getPlayer(game.getOpponentUuid()) : null;

        if (host != null) syncPvPState(host, pos, game);
        if (opp != null) syncPvPState(opp, pos, game);
    }

    private static void checkPvPRoundEnd(MinecraftServer server, BlackjackPvPGame game, BlockPos pos) {
        if (game.getState() != BlackjackPvPGame.State.FINISHED) return;

        ServerPlayer host = server.getPlayerList().getPlayer(game.getHostUuid());
        ServerPlayer opp = game.getOpponentUuid() != null ? server.getPlayerList().getPlayer(game.getOpponentUuid()) : null;

        Level level = host != null ? host.level() : (opp != null ? opp.level() : null);
        boolean isIllegal = level != null && level.getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;

        long pot = game.getPot();
        long bet = game.getBetAmount();

        if (game.getResult() == BlackjackPvPGame.Result.PLAYER1_WINS) {
            if (host != null) {
                CasinoChipHelper.giveChips(host, pot, isIllegal);
                host.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                spawnWinParticles(host, pos, true);
                host.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.win", NF.format(pot)), false);
            }
            if (opp != null) {
                opp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 0.9F);
                opp.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.lose", game.getHostName()), false);
            }
        } else if (game.getResult() == BlackjackPvPGame.Result.PLAYER2_WINS) {
            if (opp != null) {
                CasinoChipHelper.giveChips(opp, pot, isIllegal);
                opp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                spawnWinParticles(opp, pos, true);
                opp.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.win", NF.format(pot)), false);
            }
            if (host != null) {
                host.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 0.9F);
                host.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.lose", game.getOpponentName()), false);
            }
        } else if (game.getResult() == BlackjackPvPGame.Result.TIE) {
            if (host != null) {
                CasinoChipHelper.giveChips(host, bet, isIllegal);
                host.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
                host.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.tie", NF.format(bet)), false);
            }
            if (opp != null) {
                CasinoChipHelper.giveChips(opp, bet, isIllegal);
                opp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
                opp.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.tie", NF.format(bet)), false);
            }
        }

        if (host != null) syncPvPState(host, pos, game);
        if (opp != null) syncPvPState(opp, pos, game);
    }

    public static void syncPvPState(ServerPlayer target, BlockPos pos, BlackjackPvPGame game) {
        if (target == null || game == null) return;
        boolean isIllegal = target.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;
        boolean isHost = target.getUUID().equals(game.getHostUuid());
        boolean isMyTurn = game.isPlayerTurn(target.getUUID());

        int myScore = isHost ? game.getHostHand().getScore() : (game.getOpponentHand() != null ? game.getOpponentHand().getScore() : 0);
        int oppScore = isHost ? (game.getOpponentHand() != null ? game.getOpponentHand().getScore() : 0) : game.getHostHand().getScore();

        int[] myCards = isHost ? game.getHostCardIds() : game.getOpponentCardIds();
        int[] oppCards = isHost ? game.getOpponentCardIds() : game.getHostCardIds();

        long currentChips = CasinoChipHelper.getTotalChipValue(target, isIllegal);

        PacketDistributor.sendToPlayer(target, new BlackjackPvPStateS2CPayload(
                pos,
                game.getState().ordinal(),
                game.getResult().ordinal(),
                game.getHostName(),
                game.getOpponentName() != null ? game.getOpponentName() : "",
                isHost,
                isMyTurn,
                game.getBetAmount(),
                game.getPot(),
                myScore,
                oppScore,
                myCards,
                oppCards,
                game.getResultMessage(),
                currentChips
        ));
    }

    private static void spawnWinParticles(ServerPlayer player, BlockPos pos, boolean isBlackjack) {
        if (player.level() instanceof ServerLevel sl) {
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 1.0D;
            double z = pos.getZ() + 0.5D;
            int count = isBlackjack ? 35 : 18;
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, count, 0.4D, 0.4D, 0.4D, 0.1D);
            sl.sendParticles(ParticleTypes.WAX_ON, x, y + 0.2D, z, count / 2, 0.3D, 0.3D, 0.3D, 0.05D);
            if (isBlackjack) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y + 0.5D, z, 20, 0.3D, 0.3D, 0.3D, 0.15D);
            }
        }
    }

    private static void syncState(ServerPlayer player, BlockPos pos, BlackjackGame game) {
        boolean hideDealerCard = (game.getState() == BlackjackGame.State.PLAYER_TURN);
        boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;
        long currentChips = CasinoChipHelper.getTotalChipValue(player, isIllegal);

        PacketDistributor.sendToPlayer(player, new BlackjackStateS2CPayload(
                pos,
                game.getState().ordinal(),
                game.getResult().ordinal(),
                game.getCurrentBet(),
                game.getTotalPayout(),
                game.getPlayerHand().toCardIds(),
                game.getDealerHand().toCardIds(),
                hideDealerCard,
                currentChips
        ));
    }

    public static void onPlayerLogout(ServerPlayer player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        ACTIVE_GAMES.remove(uuid);

        for (Map.Entry<BlockPos, BlackjackPvPGame> entry : TABLE_PVP_GAMES.entrySet()) {
            BlockPos pos = entry.getKey();
            BlackjackPvPGame game = entry.getValue();
            boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalBlackjackTableBlock;
            if (uuid.equals(game.getHostUuid())) {
                if (game.getState() == BlackjackPvPGame.State.WAITING_FOR_OPPONENT) {
                    CasinoChipHelper.giveChips(player, game.getBetAmount(), isIllegal);
                    TABLE_PVP_GAMES.remove(pos);
                } else if (game.getState() != BlackjackPvPGame.State.FINISHED) {
                    MinecraftServer server = player.getServer();
                    if (server != null && game.getOpponentUuid() != null) {
                        ServerPlayer opp = server.getPlayerList().getPlayer(game.getOpponentUuid());
                        if (opp != null) {
                            long pot = game.getPot();
                            CasinoChipHelper.giveChips(opp, pot, isIllegal);
                            opp.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.opponent_disconnected", NF.format(pot)), false);
                        }
                    }
                    TABLE_PVP_GAMES.remove(pos);
                }
            } else if (uuid.equals(game.getOpponentUuid())) {
                if (game.getState() != BlackjackPvPGame.State.FINISHED) {
                    MinecraftServer server = player.getServer();
                    if (server != null) {
                        ServerPlayer host = server.getPlayerList().getPlayer(game.getHostUuid());
                        if (host != null) {
                            long pot = game.getPot();
                            CasinoChipHelper.giveChips(host, pot, isIllegal);
                            host.displayClientMessage(Component.translatable("awakening_system.casino.blackjack.pvp.opponent_disconnected", NF.format(pot)), false);
                        }
                    }
                    TABLE_PVP_GAMES.remove(pos);
                }
            }
        }
    }
}
