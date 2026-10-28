package com.soybe.awakeningsystem.uma.model;

import com.soybe.awakeningsystem.uma.UmaProfile;
import net.minecraft.nbt.CompoundTag;

/**
 * Representa una Uma Musume en el establo del jugador.
 * Contiene rareza (estrellas 1-5), fragmentos de despertar (shards),
 * y las cinco estadísticas principales entrenables (Speed, Stamina, Power, Guts, Wisdom)
 * que determinan su desempeño y rango de competición.
 */
public class OwnedUma {

    private final String umaId;
    private int rarity;
    private int shards;
    private int speed;
    private int stamina;
    private int power;
    private int guts;
    private int wisdom;
    private int trainingLevel;
    private int totalRaces;
    private int totalWins;

    public OwnedUma(String umaId, int baseRarity) {
        this.umaId = umaId;
        this.rarity = Math.max(1, Math.min(5, baseRarity));
        this.shards = 0;
        this.trainingLevel = 1;
        this.totalRaces = 0;
        this.totalWins = 0;

        UmaProfile profile = UmaProfile.byId(umaId);
        int bonus = (this.rarity - 1) * 40;
        this.speed = Math.round(profile.getBaseSpeed() * 600.0f) + bonus;
        this.stamina = Math.round(profile.getStamina() * 600.0f) + bonus;
        this.power = Math.round(profile.getAcceleration() * 600.0f) + bonus;
        this.guts = 250 + bonus;
        this.wisdom = 250 + bonus;
    }

    public OwnedUma(CompoundTag tag) {
        this.umaId = tag.getString("id");
        this.rarity = Math.max(1, Math.min(5, tag.getInt("rarity")));
        this.shards = tag.getInt("shards");
        this.speed = Math.max(50, Math.min(1200, tag.getInt("speed")));
        this.stamina = Math.max(50, Math.min(1200, tag.getInt("stamina")));
        this.power = Math.max(50, Math.min(1200, tag.getInt("power")));
        this.guts = Math.max(50, Math.min(1200, tag.getInt("guts")));
        this.wisdom = Math.max(50, Math.min(1200, tag.getInt("wisdom")));
        this.trainingLevel = Math.max(1, tag.getInt("training_level"));
        this.totalRaces = tag.getInt("total_races");
        this.totalWins = tag.getInt("total_wins");
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", umaId);
        tag.putInt("rarity", rarity);
        tag.putInt("shards", shards);
        tag.putInt("speed", speed);
        tag.putInt("stamina", stamina);
        tag.putInt("power", power);
        tag.putInt("guts", guts);
        tag.putInt("wisdom", wisdom);
        tag.putInt("training_level", trainingLevel);
        tag.putInt("total_races", totalRaces);
        tag.putInt("total_wins", totalWins);
        return tag;
    }

    public String getUmaId() {
        return umaId;
    }

    public UmaProfile getProfile() {
        return UmaProfile.byId(umaId);
    }

    public int getRarity() {
        return rarity;
    }

    public int getShards() {
        return shards;
    }

    public void addShards(int count) {
        this.shards += count;
    }

    public int getSpeed() {
        return speed;
    }

    public int getStamina() {
        return stamina;
    }

    public int getPower() {
        return power;
    }

    public int getGuts() {
        return guts;
    }

    public int getWisdom() {
        return wisdom;
    }

    public int getTrainingLevel() {
        return trainingLevel;
    }

    public int getTotalRaces() {
        return totalRaces;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void recordRace(boolean won) {
        this.totalRaces++;
        if (won) this.totalWins++;
    }

    /**
     * Calcula la media de estadísticas.
     */
    public int getStatAverage() {
        return (speed + stamina + power + guts + wisdom) / 5;
    }

    /**
     * Rango de la Uma (F -> SS) fiel a Uma Musume Pretty Derby.
     */
    public String getRankLetter() {
        int avg = getStatAverage();
        if (avg >= 1150) return "SS";
        if (avg >= 1000) return "S";
        if (avg >= 850) return "A";
        if (avg >= 700) return "B";
        if (avg >= 550) return "C";
        if (avg >= 400) return "D";
        if (avg >= 250) return "E";
        return "F";
    }

    public int getRankColorHex() {
        int avg = getStatAverage();
        if (avg >= 1150) return 0xFFFF4444;
        if (avg >= 1000) return 0xFFFFD700;
        if (avg >= 850) return 0xFFE67E22;
        if (avg >= 700) return 0xFF2ECC71;
        if (avg >= 550) return 0xFF3498DB;
        if (avg >= 400) return 0xFF9B59B6;
        if (avg >= 250) return 0xFF95A5A6;
        return 0xFF7F8C8D;
    }

    /**
     * Costo de fragmentos para subir una estrella.
     */
    public int getShardsRequiredForBloom() {
        return switch (rarity) {
            case 1 -> 15;
            case 2 -> 30;
            case 3 -> 50;
            case 4 -> 100;
            default -> 0;
        };
    }

    public boolean canBloom() {
        int req = getShardsRequiredForBloom();
        return rarity < 5 && req > 0 && shards >= req;
    }

    public boolean bloom() {
        if (!canBloom()) return false;
        int req = getShardsRequiredForBloom();
        this.shards -= req;
        this.rarity++;
        this.speed = Math.min(1200, this.speed + 35);
        this.stamina = Math.min(1200, this.stamina + 35);
        this.power = Math.min(1200, this.power + 35);
        this.guts = Math.min(1200, this.guts + 35);
        this.wisdom = Math.min(1200, this.wisdom + 35);
        return true;
    }

    /**
     * Entrena una estadística específica.
     */
    public boolean trainStat(String statName, int gain) {
        if (gain <= 0) return false;
        boolean success = false;
        switch (statName.toLowerCase()) {
            case "speed" -> {
                if (this.speed < 1200) {
                    this.speed = Math.min(1200, this.speed + gain);
                    success = true;
                }
            }
            case "stamina" -> {
                if (this.stamina < 1200) {
                    this.stamina = Math.min(1200, this.stamina + gain);
                    success = true;
                }
            }
            case "power" -> {
                if (this.power < 1200) {
                    this.power = Math.min(1200, this.power + gain);
                    success = true;
                }
            }
            case "guts" -> {
                if (this.guts < 1200) {
                    this.guts = Math.min(1200, this.guts + gain);
                    success = true;
                }
            }
            case "wisdom" -> {
                if (this.wisdom < 1200) {
                    this.wisdom = Math.min(1200, this.wisdom + gain);
                    success = true;
                }
            }
        }
        if (success) {
            this.trainingLevel++;
        }
        return success;
    }

    public boolean isStatMaxed(String statName) {
        return switch (statName.toLowerCase()) {
            case "speed" -> this.speed >= 1200;
            case "stamina" -> this.stamina >= 1200;
            case "power" -> this.power >= 1200;
            case "guts" -> this.guts >= 1200;
            case "wisdom" -> this.wisdom >= 1200;
            default -> false;
        };
    }
}
