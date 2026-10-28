package com.soybe.awakeningsystem.casino.blackjack;

public record Card(Suit suit, Rank rank) {

    public enum Suit {
        HEARTS("♥", true, "Hearts"),
        DIAMONDS("♦", true, "Diamonds"),
        CLUBS("♣", false, "Clubs"),
        SPADES("♠", false, "Spades");

        private final String symbol;
        private final boolean red;
        private final String displayName;

        Suit(String symbol, boolean red, String displayName) {
            this.symbol = symbol;
            this.red = red;
            this.displayName = displayName;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean isRed() {
            return red;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Rank {
        TWO("2", 2),
        THREE("3", 3),
        FOUR("4", 4),
        FIVE("5", 5),
        SIX("6", 6),
        SEVEN("7", 7),
        EIGHT("8", 8),
        NINE("9", 9),
        TEN("10", 10),
        JACK("J", 10),
        QUEEN("Q", 10),
        KING("K", 10),
        ACE("A", 11);

        private final String display;
        private final int value;

        Rank(String display, int value) {
            this.display = display;
            this.value = value;
        }

        public String getDisplay() {
            return display;
        }

        public int getValue() {
            return value;
        }
    }

    public int getId() {
        return suit.ordinal() * 13 + rank.ordinal();
    }

    public static Card fromId(int id) {
        if (id < 0 || id >= 52) {
            return new Card(Suit.SPADES, Rank.ACE);
        }
        Suit s = Suit.values()[id / 13];
        Rank r = Rank.values()[id % 13];
        return new Card(s, r);
    }

    @Override
    public String toString() {
        return rank.getDisplay() + suit.getSymbol();
    }
}
