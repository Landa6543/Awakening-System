package com.soybe.awakeningsystem.casino.blackjack;

import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BlackjackPvPGame {

    public enum State {
        WAITING_FOR_OPPONENT,
        PLAYER1_TURN,
        PLAYER2_TURN,
        FINISHED
    }

    public enum Result {
        NONE,
        PLAYER1_WINS,
        PLAYER2_WINS,
        TIE
    }

    private final UUID hostUuid;
    private final String hostName;
    private UUID opponentUuid;
    private String opponentName;

    private final long betAmount;
    private long pot;

    private final BlackjackHand hostHand = new BlackjackHand();
    private final BlackjackHand opponentHand = new BlackjackHand();
    private final List<Card> deck = new ArrayList<>();

    private State state = State.WAITING_FOR_OPPONENT;
    private Result result = Result.NONE;
    private String resultMessage = "";

    public BlackjackPvPGame(UUID hostUuid, String hostName, long betAmount) {
        this.hostUuid = hostUuid;
        this.hostName = hostName;
        this.betAmount = betAmount;
        this.pot = betAmount;
    }

    private void resetDeck() {
        deck.clear();
        for (int d = 0; d < 4; d++) {
            for (Card.Suit s : Card.Suit.values()) {
                for (Card.Rank r : Card.Rank.values()) {
                    deck.add(new Card(s, r));
                }
            }
        }
        Collections.shuffle(deck);
    }

    public Card drawCard() {
        if (deck.isEmpty()) {
            resetDeck();
        }
        return deck.remove(deck.size() - 1);
    }

    public void startMatch(UUID opponentUuid, String opponentName) {
        this.opponentUuid = opponentUuid;
        this.opponentName = opponentName;
        this.pot = this.betAmount * 2;

        resetDeck();
        hostHand.clear();
        opponentHand.clear();

        hostHand.addCard(drawCard());
        opponentHand.addCard(drawCard());
        hostHand.addCard(drawCard());
        opponentHand.addCard(drawCard());

        this.state = State.PLAYER1_TURN;
        this.result = Result.NONE;
        this.resultMessage = "Turno de " + hostName;

        if (hostHand.isBlackjack()) {
            this.state = State.PLAYER2_TURN;
            this.resultMessage = hostName + " tiene 21 Natural. Turno de " + opponentName;
        }
    }

    public boolean isPlayerTurn(UUID playerUuid) {
        if (state == State.PLAYER1_TURN) {
            return hostUuid.equals(playerUuid);
        }
        if (state == State.PLAYER2_TURN) {
            return opponentUuid != null && opponentUuid.equals(playerUuid);
        }
        return false;
    }

    public void hit(UUID playerUuid) {
        if (!isPlayerTurn(playerUuid)) return;

        if (state == State.PLAYER1_TURN) {
            hostHand.addCard(drawCard());
            if (hostHand.isBust()) {
                this.state = State.PLAYER2_TURN;
                this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.bust_turn", hostName, opponentName).getString();
            } else if (hostHand.getScore() == 21) {
                this.state = State.PLAYER2_TURN;
                this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.reached21_turn", hostName, opponentName).getString();
            }
        } else if (state == State.PLAYER2_TURN) {
            opponentHand.addCard(drawCard());
            if (opponentHand.isBust() || opponentHand.getScore() == 21) {
                evaluateEnd();
            }
        }
    }

    public void stand(UUID playerUuid) {
        if (!isPlayerTurn(playerUuid)) return;

        if (state == State.PLAYER1_TURN) {
            this.state = State.PLAYER2_TURN;
            this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.stand_turn", hostName, hostHand.getScore(), opponentName).getString();
        } else if (state == State.PLAYER2_TURN) {
            evaluateEnd();
        }
    }

    private void evaluateEnd() {
        this.state = State.FINISHED;

        boolean p1Bust = hostHand.isBust();
        boolean p2Bust = opponentHand.isBust();
        int p1Score = hostHand.getScore();
        int p2Score = opponentHand.getScore();

        if (p1Bust && p2Bust) {
            this.result = Result.TIE;
            this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.both_bust").getString();
        } else if (p1Bust) {
            this.result = Result.PLAYER2_WINS;
            this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.win_bust", opponentName, hostName).getString();
        } else if (p2Bust) {
            this.result = Result.PLAYER1_WINS;
            this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.win_bust", hostName, opponentName).getString();
        } else {
            if (p1Score > p2Score) {
                this.result = Result.PLAYER1_WINS;
                this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.win_score", hostName, p1Score, p2Score).getString();
            } else if (p2Score > p1Score) {
                this.result = Result.PLAYER2_WINS;
                this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.win_score", opponentName, p2Score, p1Score).getString();
            } else {
                if (hostHand.isBlackjack() && !opponentHand.isBlackjack()) {
                    this.result = Result.PLAYER1_WINS;
                    this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.win_blackjack", hostName).getString();
                } else if (!hostHand.isBlackjack() && opponentHand.isBlackjack()) {
                    this.result = Result.PLAYER2_WINS;
                    this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.win_blackjack", opponentName).getString();
                } else {
                    this.result = Result.TIE;
                    this.resultMessage = Component.translatable("gui.awakening_system.blackjack_pvp.tie_score", p1Score, p2Score).getString();
                }
            }
        }
    }

    public UUID getHostUuid() {
        return hostUuid;
    }

    public String getHostName() {
        return hostName;
    }

    public UUID getOpponentUuid() {
        return opponentUuid;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public long getBetAmount() {
        return betAmount;
    }

    public long getPot() {
        return pot;
    }

    public BlackjackHand getHostHand() {
        return hostHand;
    }

    public BlackjackHand getOpponentHand() {
        return opponentHand;
    }

    public State getState() {
        return state;
    }

    public Result getResult() {
        return result;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public int[] getHostCardIds() {
        return hostHand.getCards().stream().mapToInt(Card::getId).toArray();
    }

    public int[] getOpponentCardIds() {
        return opponentHand.getCards().stream().mapToInt(Card::getId).toArray();
    }
}
