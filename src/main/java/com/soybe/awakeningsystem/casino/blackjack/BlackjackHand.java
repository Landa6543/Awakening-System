package com.soybe.awakeningsystem.casino.blackjack;

import java.util.ArrayList;
import java.util.List;
public class BlackjackHand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        if (card != null) {
            cards.add(card);
        }
    }

    public List<Card> getCards() {
        return cards;
    }

    public int size() {
        return cards.size();
    }

    public void clear() {
        cards.clear();
    }

    /**
     * Calcula la mejor puntuación de la mano.
     * Los Ases valen inicialmente 11, pero se reducen a 1 de forma inteligente si la mano supera 21.
     */
    public int getScore() {
        int score = 0;
        int aceCount = 0;

        for (Card card : cards) {
            score += card.rank().getValue();
            if (card.rank() == Card.Rank.ACE) {
                aceCount++;
            }
        }

        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }

        return score;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getScore() == 21;
    }

    public boolean isBust() {
        return getScore() > 21;
    }

    public int[] toCardIds() {
        int[] ids = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            ids[i] = cards.get(i).getId();
        }
        return ids;
    }

    public static BlackjackHand fromCardIds(int[] ids) {
        BlackjackHand hand = new BlackjackHand();
        if (ids != null) {
            for (int id : ids) {
                hand.addCard(Card.fromId(id));
            }
        }
        return hand;
    }
}
