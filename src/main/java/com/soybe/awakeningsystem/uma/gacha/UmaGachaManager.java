package com.soybe.awakeningsystem.uma.gacha;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.uma.UmaProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestor oficial del Gacha de Uma Musume Pretty Derby (Tracen Academy Recruitment).
 * Permite invocar Umas de 1★, 2★ y 3★ usando Carats (🥕).
 * Incluye banners temáticos por fases con Rate-Up, garantía de 2★+ en x10,
 * conversión en Estatuas de Diosa (Clover Spark) y Shards oficiales (x90, x10, x5).
 */
public final class UmaGachaManager {

    public static final int COST_SINGLE_PULL = 150;
    public static final int COST_MULTI_PULL = 1500;

    public enum BannerPhase {
        PHASE_1(
                "Golden Generation & Tracen Idols",
                "Special Week, Silence Suzuka, Tokai Teio, Oguri Cap",
                List.of("special_week", "silence_suzuka", "tokai_teio", "oguri_cap"),
                List.of("daiwa_scarlet", "vodka", "gold_ship"),
                List.of("haru_urara", "king_halo")
        ),
        PHASE_2(
                "Derby Empresses & The Three Theorists",
                "Symboli Rudolf, Mejiro McQueen, Rice Shower, Kitasan Black",
                List.of("symboli_rudolf", "mejiro_mcqueen", "rice_shower", "kitasan_black"),
                List.of("grass_wonder", "el_condor_pasa", "nice_nature"),
                List.of("twin_turbo", "matikanetannhauser")
        ),
        PHASE_3(
                "The Lone Beast & Track Aces",
                "Narita Brian, Seiun Sky, Maruzensky, Tamamo Cross",
                List.of("narita_brian", "seiun_sky", "maruzensky", "tamamo_cross"),
                List.of("agnes_tachyon", "mayano_top_gun", "tm_opera_o"),
                List.of("manhattan_cafe", "satono_diamond")
        ),
        PHASE_4(
                "Speed Queens & Tracen Crowns",
                "Mihono Bourbon, Super Creek, Curren Chan, Biwa Hayahide",
                List.of("mihono_bourbon", "super_creek", "curren_chan", "biwa_hayahide"),
                List.of("hishi_amazon", "daitaku_helios", "smart_falcon"),
                List.of("copano_rickey", "air_groove", "mejiro_palmer", "cheval_grand", "mejiro_dober", "transcend")
        );

        private final String title;
        private final String subtitle;
        private final List<String> rateUp3Star;
        private final List<String> rateUp2Star;
        private final List<String> rateUp1Star;

        BannerPhase(String title, String subtitle, List<String> rateUp3Star, List<String> rateUp2Star, List<String> rateUp1Star) {
            this.title = title;
            this.subtitle = subtitle;
            this.rateUp3Star = rateUp3Star;
            this.rateUp2Star = rateUp2Star;
            this.rateUp1Star = rateUp1Star;
        }

        public String getTitle() { return title; }
        public net.minecraft.network.chat.Component getTitleComponent() { return net.minecraft.network.chat.Component.translatable("awakening_system.uma.gacha.phase." + (ordinal() + 1) + ".title"); }
        public String getSubtitle() { return subtitle; }
        public List<String> getRateUp3Star() { return rateUp3Star; }
        public List<String> getRateUp2Star() { return rateUp2Star; }
        public List<String> getRateUp1Star() { return rateUp1Star; }
    }

    public static final List<String> THREE_STAR_POOL = List.of(
            "special_week", "silence_suzuka", "tokai_teio", "oguri_cap",
            "symboli_rudolf", "mejiro_mcqueen", "rice_shower", "kitasan_black",
            "narita_brian", "seiun_sky", "maruzensky", "tamamo_cross",
            "mihono_bourbon", "super_creek", "curren_chan"
    );

    public static final List<String> TWO_STAR_POOL = List.of(
            "daiwa_scarlet", "vodka", "gold_ship", "grass_wonder",
            "mayano_top_gun", "agnes_tachyon",
            "el_condor_pasa", "hishi_amazon", "nice_nature",
            "daitaku_helios", "tm_opera_o", "biwa_hayahide", "smart_falcon"
    );

    public static final List<String> ONE_STAR_POOL = List.of(
            "twin_turbo", "mejiro_dober", "transcend", "cheval_grand",
            "satono_diamond", "manhattan_cafe",
            "haru_urara", "king_halo", "matikanetannhauser",
            "mejiro_palmer", "copano_rickey", "air_groove"
    );

    private static final Random RANDOM = new Random();

    public record PullResult(String umaId, int rarity, boolean isNew, int shardsGained, int statuesGained) {}

    private UmaGachaManager() {}

    public static BannerPhase getActivePhase(long dayTime) {
        long inGameDays = Math.max(0, dayTime / 24000L);
        int phaseIndex = (int) ((inGameDays / 22L) % BannerPhase.values().length);
        return BannerPhase.values()[Math.abs(phaseIndex)];
    }

    public static String getRemainingTimeString(long dayTime) {
        long ticksPerPhase = 22L * 24000L;
        long elapsedInPhase = Math.max(0, dayTime % ticksPerPhase);
        long ticksRemaining = ticksPerPhase - elapsedInPhase;
        long days = ticksRemaining / 24000L;
        long hours = (ticksRemaining % 24000L) / 1000L;
        return days + "d " + hours + "h";
    }

    /**
     * Ejecuta 1 tirada individual de 150 Carats.
     */
    public static List<PullResult> performSinglePull(ServerPlayer player, SystemData data) {
        return performSinglePull(player, data, getActivePhase(player.serverLevel().getDayTime()));
    }

    public static List<PullResult> performSinglePull(ServerPlayer player, SystemData data, BannerPhase phase) {
        if (!data.spendCarats(COST_SINGLE_PULL)) {
            return List.of();
        }
        PullResult result = rollSingle(data, false, phase);
        rewardGoddessStatues(player, result.statuesGained());
        return List.of(result);
    }

    /**
     * Ejecuta 10 tiradas de 1500 Carats con garantía de al menos una 2★ o superior.
     */
    public static List<PullResult> performMultiPull(ServerPlayer player, SystemData data) {
        return performMultiPull(player, data, getActivePhase(player.serverLevel().getDayTime()));
    }

    public static List<PullResult> performMultiPull(ServerPlayer player, SystemData data, BannerPhase phase) {
        if (!data.spendCarats(COST_MULTI_PULL)) {
            return List.of();
        }

        List<PullResult> results = new ArrayList<>(10);
        boolean gotTwoStarOrHigher = false;
        int totalStatues = 0;

        for (int i = 0; i < 9; i++) {
            PullResult res = rollSingle(data, false, phase);
            if (res.rarity() >= 2) gotTwoStarOrHigher = true;
            totalStatues += res.statuesGained();
            results.add(res);
        }

        PullResult tenth = rollSingle(data, !gotTwoStarOrHigher, phase);
        totalStatues += tenth.statuesGained();
        results.add(tenth);

        rewardGoddessStatues(player, totalStatues);
        return results;
    }

    /**
     * Ejecuta 1 o más tiradas de Uma Musume sin cobrar Carats automáticamente (el cobro de tickets/deseos/carats se realiza en GachaManager).
     */
    public static List<PullResult> performPullWithoutCarats(ServerPlayer player, SystemData data, BannerPhase phase, int count) {
        List<PullResult> results = new ArrayList<>(count);
        boolean gotTwoStarOrHigher = false;
        int totalStatues = 0;

        for (int i = 0; i < count - 1; i++) {
            PullResult res = rollSingle(data, false, phase);
            if (res.rarity() >= 2) gotTwoStarOrHigher = true;
            totalStatues += res.statuesGained();
            results.add(res);
        }

        PullResult last = rollSingle(data, (count >= 10 && !gotTwoStarOrHigher), phase);
        totalStatues += last.statuesGained();
        results.add(last);

        rewardGoddessStatues(player, totalStatues);
        return results;
    }

    private static void rewardGoddessStatues(ServerPlayer player, int count) {
        if (player == null || count <= 0) return;
        if (SystemItems.CLOVER_SPARK != null && SystemItems.CLOVER_SPARK.isBound()) {
            ItemStack clovers = new ItemStack(SystemItems.CLOVER_SPARK.get(), count);
            if (!player.getInventory().add(clovers)) {
                player.drop(clovers, false);
            }
        }
    }

    private static PullResult rollSingle(SystemData data, boolean forceTwoStarOrHigher, BannerPhase phase) {
        int roll = RANDOM.nextInt(100);
        int rarity;
        String umaId;

        if (forceTwoStarOrHigher) {
            if (roll < 15) {
                rarity = 3;
                umaId = selectFromPoolWithRateUp(THREE_STAR_POOL, phase != null ? phase.getRateUp3Star() : null);
            } else {
                rarity = 2;
                umaId = selectFromPoolWithRateUp(TWO_STAR_POOL, phase != null ? phase.getRateUp2Star() : null);
            }
        } else {
            if (roll < 3) {
                rarity = 3;
                umaId = selectFromPoolWithRateUp(THREE_STAR_POOL, phase != null ? phase.getRateUp3Star() : null);
            } else if (roll < 21) {
                rarity = 2;
                umaId = selectFromPoolWithRateUp(TWO_STAR_POOL, phase != null ? phase.getRateUp2Star() : null);
            } else {
                rarity = 1;
                umaId = ONE_STAR_POOL.get(RANDOM.nextInt(ONE_STAR_POOL.size()));
            }
        }

        boolean isNew = data.addOrUpgradeUma(umaId, rarity);
        int shards = switch (rarity) {
            case 3 -> 90;
            case 2 -> 10;
            default -> 5;
        };
        int statues = switch (rarity) {
            case 3 -> 20;
            case 2 -> 3;
            default -> 1;
        };

        return new PullResult(umaId, rarity, isNew, shards, statues);
    }

    private static String selectFromPoolWithRateUp(List<String> fullPool, List<String> rateUpPool) {
        if (rateUpPool != null && !rateUpPool.isEmpty() && RANDOM.nextBoolean()) {
            return rateUpPool.get(RANDOM.nextInt(rateUpPool.size()));
        }
        return fullPool.get(RANDOM.nextInt(fullPool.size()));
    }
}
