package com.soybe.awakeningsystem.casino.blackjack;

import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackjackGame {

    public enum State {
        BETTING,
        PLAYER_TURN,
        DEALER_TURN,
        FINISHED
    }

    public enum Result {
        NONE("gui.awakening_system.blackjack.in_progress", 0.0F),
        PLAYER_BLACKJACK("gui.awakening_system.blackjack.natural", 2.5F),
        PLAYER_WIN("gui.awakening_system.blackjack.win", 2.0F),
        DEALER_WIN("gui.awakening_system.blackjack.dealer_win", 0.0F),
        PUSH("gui.awakening_system.blackjack.push", 1.0F),
        PLAYER_BUST("gui.awakening_system.blackjack.player_bust", 0.0F),
        DEALER_BUST("gui.awakening_system.blackjack.dealer_bust", 2.0F);

        private final String translationKey;
        private final float payoutMultiplier;

        Result(String translationKey, float payoutMultiplier) {
            this.translationKey = translationKey;
            this.payoutMultiplier = payoutMultiplier;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public Component getComponent() {
            return Component.translatable(translationKey);
        }

        public String getMessage() {
            return getComponent().getString();
        }

        public float getPayoutMultiplier() {
            return payoutMultiplier;
        }
    }

    private final List<Card> deck = new ArrayList<>();
    private final BlackjackHand playerHand = new BlackjackHand();
    private final BlackjackHand dealerHand = new BlackjackHand();

    private State state = State.BETTING;
    private Result result = Result.NONE;
    private long currentBet = 0;
    private long totalPayout = 0;

    public BlackjackGame() {
        resetDeck();
    }

    public void resetDeck() {
        deck.clear();
        for (int d = 0; d < 4; d++) {
            for (int i = 0; i < 52; i++) {
                deck.add(Card.fromId(i));
            }
        }
        Collections.shuffle(deck);
    }

    public Card drawCard() {
        if (deck.size() < 15) {
            resetDeck();
        }
        return deck.remove(deck.size() - 1);
    }

    public void startRound(long bet) {
        this.currentBet = bet;
        this.totalPayout = 0;
        this.result = Result.NONE;
        this.playerHand.clear();
        this.dealerHand.clear();

        playerHand.addCard(drawCard());
        dealerHand.addCard(drawCard());
        playerHand.addCard(drawCard());
        dealerHand.addCard(drawCard());

        boolean playerBj = playerHand.isBlackjack();
        boolean dealerBj = dealerHand.isBlackjack();

        if (playerBj || dealerBj) {
            this.state = State.FINISHED;
            if (playerBj && dealerBj) {
                this.result = Result.PUSH;
            } else if (playerBj) {
                this.result = Result.PLAYER_BLACKJACK;
            } else {
                this.result = Result.DEALER_WIN;
            }
            this.totalPayout = (long) (currentBet * result.getPayoutMultiplier());
        } else {
            this.state = State.PLAYER_TURN;
        }
    }

    public Card hit() {
        if (state != State.PLAYER_TURN) return null;

        Card card = drawCard();
        playerHand.addCard(card);

        if (playerHand.isBust()) {
            this.state = State.FINISHED;
            this.result = Result.PLAYER_BUST;
            this.totalPayout = 0;
        } else if (playerHand.getScore() == 21) {
            stand();
        }

        return card;
    }

    public void stand() {
        if (state != State.PLAYER_TURN) return;

        this.state = State.DEALER_TURN;
        playDealerTurn();
    }

    public Card doubleDown() {
        if (state != State.PLAYER_TURN || playerHand.size() != 2) return null;

        this.currentBet *= 2;
        Card card = drawCard();
        playerHand.addCard(card);

        if (playerHand.isBust()) {
            this.state = State.FINISHED;
            this.result = Result.PLAYER_BUST;
            this.totalPayout = 0;
        } else {
            this.state = State.DEALER_TURN;
            playDealerTurn();
        }

        return card;
    }

    public void playDealerTurn() {
        while (dealerHand.getScore() < 17) {
            dealerHand.addCard(drawCard());
        }

        this.state = State.FINISHED;
        evaluateWinner();
    }

    private void evaluateWinner() {
        int playerScore = playerHand.getScore();
        int dealerScore = dealerHand.getScore();

        if (playerHand.isBust()) {
            this.result = Result.PLAYER_BUST;
        } else if (dealerHand.isBust()) {
            this.result = Result.DEALER_BUST;
        } else if (playerScore > dealerScore) {
            this.result = Result.PLAYER_WIN;
        } else if (playerScore < dealerScore) {
            this.result = Result.DEALER_WIN;
        } else {
            this.result = Result.PUSH;
        }

        this.totalPayout = (long) (currentBet * result.getPayoutMultiplier());
    }

    public State getState() {
        return state;
    }

    public Result getResult() {
        return result;
    }

    public long getCurrentBet() {
        return currentBet;
    }

    public long getTotalPayout() {
        return totalPayout;
    }

    public BlackjackHand getPlayerHand() {
        return playerHand;
    }

    public BlackjackHand getDealerHand() {
        return dealerHand;
    }
}
