package com.soybe.awakeningsystem.p2p;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Caché local del cliente para los datos del Mercado P2P.
 */
public final class P2PClientData {
    private static List<P2PListing> listings = new ArrayList<>();
    private static long unclaimedEarnings = 0;

    private P2PClientData() {}

    public static synchronized void update(List<P2PListing> newListings, long earnings) {
        listings = new ArrayList<>(newListings != null ? newListings : Collections.emptyList());
        unclaimedEarnings = Math.max(0, earnings);
    }

    public static synchronized List<P2PListing> getListings() {
        return Collections.unmodifiableList(listings);
    }

    public static synchronized long getUnclaimedEarnings() {
        return unclaimedEarnings;
    }
}
