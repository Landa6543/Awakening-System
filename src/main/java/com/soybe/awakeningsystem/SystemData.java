package com.soybe.awakeningsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class SystemData implements INBTSerializable<CompoundTag> {
    public static final int ATTR_BASE = 5;
    public static final int ATTR_POINTS_PER_LEVEL = 3;
    public static final int SKILL_UNLOCK_AFFINITY = 100;
    public static final int SKILL_MAX_LEVEL = 5;
    public static final int SKILL_EVOLVED_LEVEL = 6;
    public static final int EVOLUTION_AFFINITY = 250;
    public static final int EVOLUTION_COST = 3;
    public static final long EVOLUTION_COOLDOWN_TICKS = 30L * 20L;
    public static final int REPETITION_SOFT_CAP = 100;
    public static final int REPETITION_HARD_CAP = 300;
    public static final int REPETITION_DISABLE = 600;
    public static final int MAX_ENERGY = 100;
    public static final int ACTIVE_SKILL_COST = 35;
    public static final long ACTIVE_SKILL_COOLDOWN_TICKS = 20L * 20L;
    public static final long WEEK_TICKS = 7L * 24000L;
    public static final int SUBSPACE_SIZE = 54;
    private final NonNullList<ItemStack> subspaceItems = createResizableSubspaceList(SUBSPACE_SIZE);

    private static NonNullList<ItemStack> createResizableSubspaceList(int size) {
        NonNullList<ItemStack> list = NonNullList.createWithCapacity(size);
        for (int i = 0; i < size; i++) {
            list.add(ItemStack.EMPTY);
        }
        return list;
    }
    public static final int ARTIFACT_SLOTS = 5;
    private final NonNullList<ItemStack> artifactSlots = NonNullList.withSize(ARTIFACT_SLOTS, ItemStack.EMPTY);

    private ItemStack equippedLightCone = ItemStack.EMPTY;

    public static final int ACTIVE_RUNE_SLOTS = 2;
    public static final int PASSIVE_RUNE_SLOTS = 2;
    private final NonNullList<ItemStack> activeRuneSlots = NonNullList.withSize(ACTIVE_RUNE_SLOTS, ItemStack.EMPTY);
    private final NonNullList<ItemStack> passiveRuneSlots = NonNullList.withSize(PASSIVE_RUNE_SLOTS, ItemStack.EMPTY);
    private final long[] activeRuneCooldownUntil = new long[ACTIVE_RUNE_SLOTS];

    private boolean hasChosenInitialVision = false;
    private long visionResonanceCooldownUntil = 0L;

    private int carats = 1500;
    private final Map<String, com.soybe.awakeningsystem.uma.model.OwnedUma> ownedUmas = new LinkedHashMap<>() {{
        put("special_week", new com.soybe.awakeningsystem.uma.model.OwnedUma("special_week", 3));
    }};
    private String activeUmaId = "special_week";

    private int dartHighScore = 0;
    private long casinoTotalWon = 0L;
    private int casinoMaxPayout = 0;
    private int poolMatchesWon = 0;

    public int getDartHighScore() { return dartHighScore; }
    public void recordDartScore(int score) {
        if (score > this.dartHighScore) {
            this.dartHighScore = score;
        }
    }
    public long getCasinoTotalWon() { return casinoTotalWon; }
    public void addCasinoWinnings(long amount) {
        if (amount > 0) {
            this.casinoTotalWon += amount;
            if (amount > this.casinoMaxPayout) {
                this.casinoMaxPayout = (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }
    }
    public int getCasinoMaxPayout() { return casinoMaxPayout; }
    public int getPoolMatchesWon() { return poolMatchesWon; }
    public void incrementPoolMatchesWon() { this.poolMatchesWon++; }

    private int level = 1;
    private int experience;
    private int attributePoints;
    private int coins;
    private int strength = ATTR_BASE;
    private int vitality = ATTR_BASE;
    private int agility = ATTR_BASE;
    private int perception = ATTR_BASE;
    private int intelligence = ATTR_BASE;
    private int willpower = ATTR_BASE;
    private boolean agilitySpeedEnabled = true;
    private final Map<String, Integer> affinities = new LinkedHashMap<>();
    private final Map<String, Integer> skills = new LinkedHashMap<>();
    private final Map<String, Integer> skillProgress = new LinkedHashMap<>();
    private final Map<String, Long> evolutionCooldowns = new LinkedHashMap<>();
    private final Map<String, Long> activeCooldowns = new LinkedHashMap<>();
    private int energy = 100;
    private int awakeningMining;
    private int awakeningCombat;
    private int blocksMined;
    private int oresMined;
    private int creaturesDefeated;
    private int damageDealt;
    private int damageTaken;
    private int itemsCrafted;
    private int skillsActivated;
    private int missionsCompleted;
    private int purchases;
    private int coinsSpent;
    private int prestige;
    private final Map<String, Boolean> achievements = new LinkedHashMap<>();
    private long weeklyWeek = -1;
    private int weeklyType;
    private int weeklyProgress;
    private boolean weeklyComplete;
    private boolean awakeningComplete;
    private long dailyDay = -1;
    private int dailyType;
    private int dailyProgress;
    private boolean dailyComplete;
    private boolean dailyFailed;
    private long dailyPenaltyUntil;
    private int emergencySurvival;
    private int bossKills;
    private int distanceMoved;
    private int deaths;
    private long lastDailyLoginDay = -1;
    private boolean emergencyComplete;
    private boolean emergencyWounded;
    private boolean evolutionMissionComplete;
    private boolean secretMissionComplete;
    private boolean classMissionComplete;
    private int highestRankReward;
    private String equippedTitle = "";
    private boolean isCreator = false;
    private boolean isNanda = false;
    private boolean isMisticPotato = false;
    private boolean isSchvis = false;
    private boolean isTomcraft = false;
    private final Map<String, Integer> repeatedActions = new LinkedHashMap<>();

    private int comboCount;
    private int comboTicks;
    private int tranceTicks;
    private String sponsorConstellation = "";
    private boolean isConstellation;
    private String constellationModifier = "";
    private String activeScenario = "night_hunt";
    private int scenarioProgress;
    private int scenarioTarget = 10;
    private boolean scenarioComplete;
    private long phoenixCooldownUntil;
    private long ironWillCooldownUntil;
    private String equippedSkill = "";
    private String activeVision = "NONE";
    private final java.util.Set<String> disabledSkills = new java.util.HashSet<>();
    private final java.util.List<CompoundTag> storedShadowArmy = new java.util.ArrayList<>();

    private final java.util.List<String> equippedActiveSkills = new java.util.ArrayList<>(java.util.List.of("", "", "", "", ""));
    private final java.util.List<String> equippedPassiveSkills = new java.util.ArrayList<>(java.util.List.of("", "", ""));

    private int dantianRealm = 0;
    private int cultivationSubLevel = 1;
    private int cultivationQi = 0;
    private final boolean[] meridians = new boolean[12];
    private int selectedDaoSkill = 0;
    private int tribulationShieldTicks = 0;
    private int daoSkillCooldown = 0;
    private long stigmaCooldownUntil = 0;
    private boolean conqueredRankerTower = false;
    private int savedTowerFloor = 1;
    private boolean conqueredNineTribulationsPagoda = false;
    private int savedPagodaStage = 1;
    private boolean conqueredBloodSect = false;
    private boolean conqueredYggdrasilLabyrinth = false;
    private boolean systemUpdated = false;
    private boolean isekaiUnlocked = false;
    private boolean systemUpdating = false;
    private int systemUpdateRemainingTicks = 0;
    private String murimPhysique = "";
    private boolean murimUnlocked = false;
    private boolean system30Updated = false;
    private boolean system30Updating = false;
    private int system30UpdateRemainingTicks = 0;
    private boolean system40Updated = false;
    private boolean system40Updating = false;
    private int system40UpdateRemainingTicks = 0;
    private boolean teyvatUnlocked = false;
    private int anemoStatueLevel = 1;
    private int anemoOculiProgress = 0;
    private int geoStatueLevel = 1;
    private int geoOculiProgress = 0;
    private int electroStatueLevel = 1;
    private int electroOculiProgress = 0;
    private int dendroStatueLevel = 1;
    private int dendroOculiProgress = 0;
    private int statueEnergyBonus = 0;
    private final Set<String> visitedDimensions = new HashSet<>();

    private int tribulationTicks = 0;
    private int tribulationStrikesRemaining = 0;
    private int tribulationTotalStrikes = 0;
    private int pendingTribulationRealm = 0;

    private int physiqueSkillCooldown = 0;

    private int skillPoints = 0;
    private int spentSkillPoints = 0;

    private int gachaPity5Star = 0;
    private int gachaPity4Star = 0;
    private boolean gachaGuaranteedRateUp = false;
    private int primogems = 0;
    private int intertwinedFates = 0;
    private int acquaintFates = 0;
    private int totalGachaPulls = 0;

    public int gachaPity5Star() { return gachaPity5Star; }
    public void setGachaPity5Star(int v) { this.gachaPity5Star = Math.max(0, v); }
    public void incrementGachaPity5Star() { this.gachaPity5Star++; }
    public int gachaPity4Star() { return gachaPity4Star; }
    public void setGachaPity4Star(int v) { this.gachaPity4Star = Math.max(0, v); }
    public void incrementGachaPity4Star() { this.gachaPity4Star++; }
    public boolean gachaGuaranteedRateUp() { return gachaGuaranteedRateUp; }
    public void setGachaGuaranteedRateUp(boolean v) { this.gachaGuaranteedRateUp = v; }
    public int primogems() { return primogems; }
    public void setPrimogems(int v) { this.primogems = Math.max(0, v); }
    public void addPrimogems(int v) { this.primogems = Math.max(0, this.primogems + v); }
    public boolean spendPrimogems(int v) {
        if (this.primogems >= v) {
            this.primogems -= v;
            return true;
        }
        return false;
    }
    public int intertwinedFates() { return intertwinedFates; }
    public void setIntertwinedFates(int v) { this.intertwinedFates = Math.max(0, v); }
    public void addIntertwinedFates(int v) { this.intertwinedFates = Math.max(0, this.intertwinedFates + v); }
    public boolean spendIntertwinedFates(int v) {
        if (this.intertwinedFates >= v) {
            this.intertwinedFates -= v;
            return true;
        }
        return false;
    }
    public int acquaintFates() { return acquaintFates; }
    public void setAcquaintFates(int v) { this.acquaintFates = Math.max(0, v); }
    public void addAcquaintFates(int v) { this.acquaintFates = Math.max(0, this.acquaintFates + v); }
    public boolean spendAcquaintFates(int v) {
        if (this.acquaintFates >= v) {
            this.acquaintFates -= v;
            return true;
        }
        return false;
    }
    public int totalGachaPulls() { return totalGachaPulls; }
    public void incrementTotalGachaPulls(int v) { this.totalGachaPulls += v; }

    private int subspaceLevel = 1;
    private int subspaceBuiltLevel = 0;
    private String subspaceReturnDim = "minecraft:overworld";
    private double subspaceReturnX = 0.0;
    private double subspaceReturnY = 64.0;
    private double subspaceReturnZ = 0.0;
    private float subspaceReturnYaw = 0.0F;
    private float subspaceReturnPitch = 0.0F;

    public int subspaceLevel() { return Math.max(1, Math.min(5, subspaceLevel)); }
    public void setSubspaceLevel(int lvl) { this.subspaceLevel = Math.max(1, Math.min(5, lvl)); }
    public int subspaceBuiltLevel() { return subspaceBuiltLevel; }
    public void setSubspaceBuiltLevel(int lvl) { this.subspaceBuiltLevel = lvl; }
    public String subspaceReturnDim() { return subspaceReturnDim; }
    public double subspaceReturnX() { return subspaceReturnX; }
    public double subspaceReturnY() { return subspaceReturnY; }
    public double subspaceReturnZ() { return subspaceReturnZ; }
    public float subspaceReturnYaw() { return subspaceReturnYaw; }
    public float subspaceReturnPitch() { return subspaceReturnPitch; }

    private int subspaceUnlockedSlots = 54;
    private int subspaceChestCount = 2;

    public int subspaceUnlockedSlots() { return Math.max(54, subspaceUnlockedSlots); }
    public void setSubspaceUnlockedSlots(int slots) { this.subspaceUnlockedSlots = Math.max(54, slots); }
    public int subspaceChestCount() { return Math.max(2, subspaceChestCount); }
    public void setSubspaceChestCount(int count) { this.subspaceChestCount = Math.max(2, count); }

    public void ensureSubspaceCapacity(int requiredSlots) {
        while (subspaceItems.size() < requiredSlots) {
            subspaceItems.add(ItemStack.EMPTY);
        }
    }

    public void setSubspaceReturnLocation(String dim, double x, double y, double z, float yaw, float pitch) {
        this.subspaceReturnDim = (dim != null && !dim.isEmpty()) ? dim : "minecraft:overworld";
        this.subspaceReturnX = x;
        this.subspaceReturnY = y;
        this.subspaceReturnZ = z;
        this.subspaceReturnYaw = yaw;
        this.subspaceReturnPitch = pitch;
    }

    private int stardust = 0;
    private int starglitter = 0;
    private int beginnerPulls = 0;
    private final java.util.List<WishLogEntry> wishHistory = new java.util.ArrayList<>();

    public record WishLogEntry(String itemName, int stars, String bannerName, long timestamp) {
        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("item", itemName);
            tag.putInt("stars", stars);
            tag.putString("banner", bannerName);
            tag.putLong("time", timestamp);
            return tag;
        }

        public static WishLogEntry fromNBT(CompoundTag tag) {
            return new WishLogEntry(
                    tag.getString("item"),
                    tag.getInt("stars"),
                    tag.getString("banner"),
                    tag.getLong("time")
            );
        }
    }

    public int stardust() { return stardust; }
    public void setStardust(int v) { this.stardust = Math.max(0, v); }
    public void addStardust(int v) { this.stardust = Math.max(0, this.stardust + v); }
    public boolean spendStardust(int v) {
        if (this.stardust >= v) {
            this.stardust -= v;
            return true;
        }
        return false;
    }

    public int starglitter() { return starglitter; }
    public void setStarglitter(int v) { this.starglitter = Math.max(0, v); }
    public void addStarglitter(int v) { this.starglitter = Math.max(0, this.starglitter + v); }
    public boolean spendStarglitter(int v) {
        if (this.starglitter >= v) {
            this.starglitter -= v;
            return true;
        }
        return false;
    }

    private int mora = 0;
    private int weaponBannerTarget = 0;
    private int weaponBannerFatePoints = 0;
    private int characterBannerTarget = 0;

    private double usdBalance = 0.0;
    private int genesisCrystals = 0;
    private int stellarJade = 0;
    private int welkinDays = 0;
    private long lastWelkinClaimDay = -1;
    private boolean welkinEnhanced = false;
    private int welkinQuestStep = 0;
    private int welkinQuestProgress = 0;
    private int firstTopUpMask = 0;
    private int firstJadeTopUpMask = 0;
    private int lastClaimedMaintenanceVersion = 0;
    private long gachaEpochTick = 0L;
    private long casinoVipUntilTick = 0L;

    public int lastClaimedMaintenanceVersion() { return lastClaimedMaintenanceVersion; }
    public void setLastClaimedMaintenanceVersion(int v) { this.lastClaimedMaintenanceVersion = v; }
    public long gachaEpochTick() { return gachaEpochTick; }
    public void setGachaEpochTick(long v) { this.gachaEpochTick = Math.max(0L, v); }
    public long casinoVipUntilTick() { return casinoVipUntilTick; }
    public void setCasinoVipUntilTick(long v) { this.casinoVipUntilTick = v; }
    public boolean isCasinoVip() { return casinoVipUntilTick == -1L || casinoVipUntilTick > 0L; }
    public boolean isCasinoVip(long currentTick) { return casinoVipUntilTick == -1L || (casinoVipUntilTick > 0L && currentTick < casinoVipUntilTick); }

    public boolean hasClaimedFirstTopUp(int pack) {
        if (pack < 0 || pack > 5) return false;
        return (firstTopUpMask & (1 << pack)) != 0;
    }

    public void setClaimedFirstTopUp(int pack) {
        if (pack >= 0 && pack <= 5) {
            firstTopUpMask |= (1 << pack);
        }
    }

    public int firstTopUpMask() {
        return firstTopUpMask;
    }

    public void setFirstTopUpMask(int mask) {
        this.firstTopUpMask = mask;
    }

    public boolean hasClaimedFirstJadeTopUp(int pack) {
        if (pack < 0 || pack > 5) return false;
        return (firstJadeTopUpMask & (1 << pack)) != 0;
    }

    public void setClaimedFirstJadeTopUp(int pack) {
        if (pack >= 0 && pack <= 5) {
            firstJadeTopUpMask |= (1 << pack);
        }
    }

    public int firstJadeTopUpMask() {
        return firstJadeTopUpMask;
    }

    public void setFirstJadeTopUpMask(int mask) {
        this.firstJadeTopUpMask = mask;
    }

    public double usdBalance() { return usdBalance; }
    public void setUsdBalance(double v) { this.usdBalance = Math.max(0.0, v); }
    public void addUsdBalance(double v) { this.usdBalance = Math.max(0.0, this.usdBalance + v); }
    public boolean spendUsdBalance(double v) {
        if (this.usdBalance >= v - 0.001) {
            this.usdBalance = Math.max(0.0, this.usdBalance - v);
            return true;
        }
        return false;
    }

    public int genesisCrystals() { return genesisCrystals; }
    public void setGenesisCrystals(int v) { this.genesisCrystals = Math.max(0, v); }
    public void addGenesisCrystals(int v) { this.genesisCrystals = Math.max(0, this.genesisCrystals + v); }
    public boolean spendGenesisCrystals(int v) {
        if (this.genesisCrystals >= v) {
            this.genesisCrystals -= v;
            return true;
        }
        return false;
    }

    public int stellarJade() { return stellarJade; }
    public void setStellarJade(int v) { this.stellarJade = Math.max(0, v); }
    public void addStellarJade(int v) { this.stellarJade = Math.max(0, this.stellarJade + v); }
    public boolean spendStellarJade(int v) {
        if (this.stellarJade >= v) {
            this.stellarJade -= v;
            return true;
        }
        return false;
    }

    public int welkinDays() { return welkinDays; }
    public void setWelkinDays(int v) { this.welkinDays = Math.max(0, v); }
    public void addWelkinDays(int v) { this.welkinDays = Math.max(0, this.welkinDays + v); }
    public long lastWelkinClaimDay() { return lastWelkinClaimDay; }
    public void setLastWelkinClaimDay(long v) { this.lastWelkinClaimDay = v; }
    public boolean isWelkinEnhanced() { return welkinEnhanced; }
    public void setWelkinEnhanced(boolean v) { this.welkinEnhanced = v; }
    public int welkinQuestStep() { return welkinQuestStep; }
    public void setWelkinQuestStep(int v) { this.welkinQuestStep = Math.max(0, v); }
    public int welkinQuestProgress() { return welkinQuestProgress; }
    public void setWelkinQuestProgress(int v) { this.welkinQuestProgress = Math.max(0, v); }
    public void addWelkinQuestProgress(int v) { this.welkinQuestProgress = Math.max(0, this.welkinQuestProgress + v); }

    public int mora() { return mora; }
    public void setMora(int v) { this.mora = Math.max(0, v); }
    public void addMora(int v) { this.mora = Math.max(0, this.mora + v); }
    public boolean spendMora(int v) {
        if (this.mora >= v) {
            this.mora -= v;
            return true;
        }
        return false;
    }

    public int weaponBannerTarget() { return weaponBannerTarget; }
    public void setWeaponBannerTarget(int v) { this.weaponBannerTarget = v; }
    public int weaponBannerFatePoints() { return weaponBannerFatePoints; }
    public void setWeaponBannerFatePoints(int v) { this.weaponBannerFatePoints = Math.max(0, Math.min(1, v)); }
    public int characterBannerTarget() { return characterBannerTarget; }
    public void setCharacterBannerTarget(int v) { this.characterBannerTarget = v; }

    public int beginnerPulls() { return beginnerPulls; }
    public void setBeginnerPulls(int v) { this.beginnerPulls = Math.max(0, Math.min(20, v)); }
    public void incrementBeginnerPulls(int v) { this.beginnerPulls = Math.min(20, this.beginnerPulls + v); }

    public java.util.List<WishLogEntry> wishHistory() { return wishHistory; }
    public void addWishHistoryEntry(String itemName, int stars, String bannerName) {
        wishHistory.add(0, new WishLogEntry(itemName, stars, bannerName, System.currentTimeMillis()));
        while (wishHistory.size() > 2000) {
            wishHistory.remove(wishHistory.size() - 1);
        }
    }

    public int skillPoints() {
        int totalEarned = level >= 30 ? Math.max(0, (level - 25) / 5) : 0;
        return Math.max(skillPoints, totalEarned - spentSkillPoints);
    }
    public void setSkillPoints(int sp) { this.skillPoints = Math.max(0, sp); }
    public void addSkillPoints(int sp) { this.skillPoints = Math.max(0, this.skillPoints + sp); }
    public boolean spendSkillPoints(int sp) {
        if (skillPoints() >= sp) {
            this.spentSkillPoints += sp;
            this.skillPoints = Math.max(0, skillPoints() - sp);
            return true;
        }
        return false;
    }
    public int spentSkillPoints() { return spentSkillPoints; }

    public int mana() { return energy(); }
    public int maxMana() { return maxEnergy(); }
    public int manaRegenRate() { return energyRegenRate(); }
    public boolean spendMana(int amount) { return spendEnergy(amount); }
    public boolean restoreMana(int amount) { return restoreEnergy(amount); }
    public void setMana(int amount) { this.energy = Math.max(0, Math.min(maxMana(), amount)); }

    public boolean conqueredRankerTower() { return conqueredRankerTower; }
    public void setConqueredRankerTower(boolean conquered) { this.conqueredRankerTower = conquered; }
    public int savedTowerFloor() { return savedTowerFloor; }
    public void setSavedTowerFloor(int floor) { this.savedTowerFloor = Math.max(1, Math.min(100, floor)); }
    public boolean conqueredNineTribulationsPagoda() { return conqueredNineTribulationsPagoda; }
    public void setConqueredNineTribulationsPagoda(boolean conquered) { this.conqueredNineTribulationsPagoda = conquered; }
    public int savedPagodaStage() { return savedPagodaStage; }
    public void setSavedPagodaStage(int stage) { this.savedPagodaStage = Math.max(1, Math.min(9, stage)); }
    public boolean conqueredBloodSect() { return conqueredBloodSect; }
    public void setConqueredBloodSect(boolean conquered) { this.conqueredBloodSect = conquered; }
    public boolean conqueredYggdrasilLabyrinth() { return conqueredYggdrasilLabyrinth; }
    public void setConqueredYggdrasilLabyrinth(boolean conquered) { this.conqueredYggdrasilLabyrinth = conquered; }
    public boolean isSystemUpdating() { return systemUpdating; }
    public void setSystemUpdating(boolean updating) { this.systemUpdating = updating; }
    public int systemUpdateRemainingTicks() { return systemUpdateRemainingTicks; }
    public void setSystemUpdateRemainingTicks(int ticks) { this.systemUpdateRemainingTicks = Math.max(0, ticks); }
    public void decrementSystemUpdateTicks() { if (systemUpdateRemainingTicks > 0) systemUpdateRemainingTicks--; }
    public boolean isSystem30Updating() { return system30Updating; }
    public void setSystem30Updating(boolean updating) { this.system30Updating = updating; }
    public int system30UpdateRemainingTicks() { return system30UpdateRemainingTicks; }
    public void setSystem30UpdateRemainingTicks(int ticks) { this.system30UpdateRemainingTicks = Math.max(0, ticks); }
    public void decrementSystem30UpdateTicks() { if (system30UpdateRemainingTicks > 0) system30UpdateRemainingTicks--; }
    public boolean isSystem40Updating() { return system40Updating; }
    public void setSystem40Updating(boolean updating) { this.system40Updating = updating; }
    public int system40UpdateRemainingTicks() { return system40UpdateRemainingTicks; }
    public void setSystem40UpdateRemainingTicks(int ticks) { this.system40UpdateRemainingTicks = Math.max(0, ticks); }
    public void decrementSystem40UpdateTicks() { if (system40UpdateRemainingTicks > 0) system40UpdateRemainingTicks--; }
    public boolean isAnySystemUpdating() { return systemUpdating || system30Updating || system40Updating; }

    public boolean isSkillEnabled(String skillId) {
        if (!skills.containsKey(skillId) || disabledSkills.contains(skillId)) return false;
        if (SkillDefinitions.isPassive(skillId)) {
            return isPassiveEquipped(skillId);
        }
        return true;
    }

    public boolean toggleSkill(String skillId) {
        if (!skills.containsKey(skillId)) return false;
        if (disabledSkills.contains(skillId)) {
            disabledSkills.remove(skillId);
        } else {
            disabledSkills.add(skillId);
        }
        return true;
    }

    public boolean hasSkill(String skillId) {
        return skills.containsKey(skillId);
    }

    public void unlockSkill(String skillId) {
        skills.putIfAbsent(skillId, 1);
    }

    private final java.util.List<String> unlockedFables = new java.util.ArrayList<>();
    private int fableWeight = 0;

    public java.util.List<String> unlockedFables() {
        return java.util.Collections.unmodifiableList(unlockedFables);
    }

    public boolean hasFable(String fableId) {
        return fableId != null && unlockedFables.contains(fableId);
    }

    public boolean unlockFable(String fableId, int weight) {
        if (fableId == null || fableId.isEmpty() || unlockedFables.contains(fableId)) return false;
        unlockedFables.add(fableId);
        fableWeight += Math.max(1, weight);
        return true;
    }

    public int fableWeight() {
        return fableWeight;
    }

    public void enableSkill(String skillId) {
        disabledSkills.remove(skillId);
    }

    private boolean receivedHunterGuide = false;

    public boolean hasReceivedHunterGuide() {
        return receivedHunterGuide;
    }

    public void setReceivedHunterGuide(boolean received) {
        this.receivedHunterGuide = received;
    }

    private String hunterClass = "";

    public String hunterClass() {
        return hunterClass == null ? "" : hunterClass;
    }

    public void setHunterClass(String hunterClass) {
        this.hunterClass = hunterClass == null ? "" : hunterClass;
    }

    public boolean hasHunterClass() {
        return hunterClass != null && !hunterClass.isEmpty();
    }

    public void resetHunterClass() {
        String cls = hunterClass();
        if (cls != null && !cls.isEmpty() && !"none".equalsIgnoreCase(cls)) {
            java.util.List<ClassSkillTree.SkillNode> nodes = ClassSkillTree.getNodesForClass(cls);
            if (nodes != null) {
                for (ClassSkillTree.SkillNode node : nodes) {
                    skills.remove(node.skillId());
                }
            }
        }
        this.hunterClass = "";
        ensureEquippedActiveSlots();
        for (int i = 0; i < equippedActiveSkills.size(); i++) {
            if (!skills.containsKey(equippedActiveSkills.get(i))) {
                equippedActiveSkills.set(i, "");
            }
        }
        ensureEquippedPassiveSlots();
        for (int i = 0; i < equippedPassiveSkills.size(); i++) {
            if (!skills.containsKey(equippedPassiveSkills.get(i))) {
                equippedPassiveSkills.set(i, "");
            }
        }
    }

    private boolean dokkaebiScenarioActive = false;
    private int dokkaebiScenarioTimer = 0;
    private int dokkaebiScenarioKills = 0;
    private int dokkaebiScenarioTargetKills = 10;
    private String dokkaebiScenarioTitle = "";

    public boolean isDokkaebiScenarioActive() { return dokkaebiScenarioActive; }
    public int dokkaebiScenarioTimer() { return dokkaebiScenarioTimer; }
    public int dokkaebiScenarioKills() { return dokkaebiScenarioKills; }
    public int dokkaebiScenarioTargetKills() { return Math.max(1, dokkaebiScenarioTargetKills); }
    public String dokkaebiScenarioTitle() { return dokkaebiScenarioTitle == null ? "" : dokkaebiScenarioTitle; }

    public void startDokkaebiScenario(String title, int durationTicks, int targetKills) {
        this.dokkaebiScenarioActive = true;
        this.dokkaebiScenarioTitle = title;
        this.dokkaebiScenarioTimer = durationTicks;
        this.dokkaebiScenarioKills = 0;
        this.dokkaebiScenarioTargetKills = targetKills;
    }

    public void stopDokkaebiScenario() {
        this.dokkaebiScenarioActive = false;
        this.dokkaebiScenarioTimer = 0;
    }

    public void recordDokkaebiKill() {
        if (dokkaebiScenarioActive) {
            dokkaebiScenarioKills++;
        }
    }

    public void addDokkaebiScenarioKills(int amount) {
        if (dokkaebiScenarioActive && amount > 0) {
            this.dokkaebiScenarioKills += amount;
        }
    }

    public void setDokkaebiScenarioKills(int amount) {
        this.dokkaebiScenarioKills = Math.max(0, amount);
    }

    public void tickDokkaebiTimer() {
        if (dokkaebiScenarioActive && dokkaebiScenarioTimer > 0) {
            dokkaebiScenarioTimer--;
        }
    }

    public java.util.Set<String> disabledSkills() {
        return java.util.Collections.unmodifiableSet(disabledSkills);
    }

    public long ironWillCooldownUntil() { return ironWillCooldownUntil; }
    public void setIronWillCooldownUntil(long until) { this.ironWillCooldownUntil = until; }

    public java.util.List<CompoundTag> getStoredShadowArmy() {
        return storedShadowArmy;
    }

    public int storedShadowCount() {
        return storedShadowArmy.size();
    }

    public void storeShadow(CompoundTag shadowTag) {
        if (shadowTag != null && storedShadowArmy.size() < 100) {
            String type = shadowTag.getString("MobEntityTypeId");
            if (type != null && type.toLowerCase().contains("celestial_dragon")) {
                return;
            }
            storedShadowArmy.add(shadowTag);
        }
    }

    public CompoundTag popStoredShadow() {
        if (storedShadowArmy.isEmpty()) return null;
        return storedShadowArmy.remove(storedShadowArmy.size() - 1);
    }

    public void clearStoredShadows() {
        storedShadowArmy.clear();
    }

    public int maxEnergy() {
        int base = 100;
        int intBonus = Math.max(0, (intelligence - ATTR_BASE) * 5);
        int willBonus = Math.max(0, (willpower - ATTR_BASE) * 2);
        int classBonus = 0;
        if ("grand_archmage".equalsIgnoreCase(hunterClass)) classBonus = 120;
        else if ("shadow_monarch".equalsIgnoreCase(hunterClass) || "necromancer".equalsIgnoreCase(hunterClass)) classBonus = 100;
        else if ("spiritual_healer".equalsIgnoreCase(hunterClass) || "healer".equalsIgnoreCase(hunterClass)) classBonus = 90;
        else if ("mage".equalsIgnoreCase(hunterClass)) classBonus = 75;
        else if ("lightning_emperor".equalsIgnoreCase(hunterClass)) classBonus = 70;
        else if ("star_apostle".equalsIgnoreCase(hunterClass)) classBonus = 60;
        else if ("demonic_cultivator".equalsIgnoreCase(hunterClass)) classBonus = 50;
        else if ("dragon_monarch".equalsIgnoreCase(hunterClass)) classBonus = 40;
        else if ("blood_emperor".equalsIgnoreCase(hunterClass)) classBonus = 40;
        else if ("void_assassin".equalsIgnoreCase(hunterClass) || "sword_saint".equalsIgnoreCase(hunterClass) || "assassin".equalsIgnoreCase(hunterClass) || "swordsman".equalsIgnoreCase(hunterClass)) classBonus = 30;
        else if ("beast_monarch".equalsIgnoreCase(hunterClass)) classBonus = 30;
        else if ("colossal_berserker".equalsIgnoreCase(hunterClass)) classBonus = 20;
        int treeBonus = (int) ClassSkillTree.getTotalAttributeBonus(this, ClassSkillTree.StatType.MAX_MANA);
        return Math.max(100, base + intBonus + willBonus + classBonus + treeBonus + statueEnergyBonus);
    }

    public int energyRegenRate() {
        int baseRegen = 5;
        int intRegen = Math.max(0, (intelligence - ATTR_BASE) / 4);
        int willRegen = Math.max(0, (willpower - ATTR_BASE) / 8);
        int classRegen = 0;
        if ("grand_archmage".equalsIgnoreCase(hunterClass) || "shadow_monarch".equalsIgnoreCase(hunterClass) || "demonic_cultivator".equalsIgnoreCase(hunterClass) || "mage".equalsIgnoreCase(hunterClass) || "necromancer".equalsIgnoreCase(hunterClass)) {
            classRegen = 2;
        } else if ("star_apostle".equalsIgnoreCase(hunterClass)) {
            classRegen = 1;
        }
        return Math.max(1, baseRegen + intRegen + willRegen + classRegen);
    }

    public boolean spendEnergy(int amount) {
        if (amount <= 0) return true;
        if (this.energy < amount) return false;
        this.energy -= amount;
        return true;
    }

    public int learn(String affinity, int amount) {
        int repetitions = repeatedActions.merge(affinity, 1, Integer::sum);
        if (repetitions > REPETITION_DISABLE) return 0;
        int adjustedAmount;
        if (repetitions > REPETITION_HARD_CAP) adjustedAmount = Math.max(1, amount / 4);
        else if (repetitions > REPETITION_SOFT_CAP) adjustedAmount = Math.max(1, amount / 2);
        else adjustedAmount = amount;
        affinities.merge(affinity, adjustedAmount, Integer::sum);
        int progress = affinities.get(affinity);
        if ("mining".equals(affinity) && progress >= 250) skills.putIfAbsent("stone_strike", 1);
        if ("combat".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("combat_instinct", 1);
        if ("survival".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("enduring_flesh", 1);
        if ("mobility".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("windrunner", 1);
        if ("mobility".equals(affinity) && progress >= 180) skills.putIfAbsent("shadow_step", 1);
        if ("depth".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("deep_breath", 1);
        if ("perception".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("ore_sense", 1);
        if ("perception".equals(affinity) && progress >= 160) skills.putIfAbsent("magnetic_pull", 1);
        if ("fire".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("fire_ward", 1);
        if ("nature".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("green_thumb", 1);
        if ("crafting".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("artisan_hand", 1);
        if ("alchemy".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("alchemist_eye", 1);
        if ("protection".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("guardian_stance", 1);
        if ("ranged".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("deadeye", 1);
        if ("system".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY) skills.putIfAbsent("system_core", 1);
        if ("combat".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY && SkillDefinitions.isAvailable("beast_bond")) skills.putIfAbsent("beast_bond", 1);
        if ("combat".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY && SkillDefinitions.isAvailable("cataclysm_breaker")) skills.putIfAbsent("cataclysm_breaker", 1);
        if ("depth".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY && SkillDefinitions.isAvailable("twilight_attunement")) skills.putIfAbsent("twilight_attunement", 1);
        if ("mobility".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY && SkillDefinitions.isAvailable("aether_ascension")) skills.putIfAbsent("aether_ascension", 1);
        if ("perception".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY && SkillDefinitions.isAvailable("deep_dark_resonance")) skills.putIfAbsent("deep_dark_resonance", 1);
        if ("eclipse".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY
                && SkillDefinitions.isAvailable("eclipse_edge")) skills.putIfAbsent("eclipse_edge", 1);
        if ("eclipse".equals(affinity) && progress >= SKILL_UNLOCK_AFFINITY
                && SkillDefinitions.isAvailable("eclipse_core")) skills.putIfAbsent("eclipse_core", 1);
        return adjustedAmount;
    }

    public boolean addExperience(int amount) {
        if (amount <= 0) return false;
        experience += amount;
        boolean leveled = false;
        while (experience >= experienceToNextLevel()) {
            if (level >= 1000) {
                level = 1000;
                experience = 0;
                break;
            }
            experience -= experienceToNextLevel();
            level++;
            attributePoints += ATTR_POINTS_PER_LEVEL;
            leveled = true;
        }
        return leveled;
    }

    private int experienceToNextLevel() { return 100 + (level - 1) * 50; }

    /**
     * Ritmo de subida pensado para una sesion de juego normal:
     * 20 acciones de afinidad = 1 nivel, con incrementos progresivos cada 10 niveles.
     */
    public int affinityExperience(int amount) {
        if (amount <= 0) return 0;
        return Math.max(1, amount * Math.max(5, 10 - level / 10));
    }

    public void resetRepetition(String affinity) { repeatedActions.remove(affinity); }

    public boolean canAdvanceSkill(String skill) {
        String affinity = SkillDefinitions.affinity(skill);
        return affinity == null || repetition(affinity) < REPETITION_DISABLE;
    }

    public boolean gainSkillProgress(String skill, int amount) {
        if (!skills.containsKey(skill) || skills.get(skill) >= SKILL_MAX_LEVEL) return false;
        int progress = skillProgress.merge(skill, amount, Integer::sum);
        int required = skills.get(skill) * 100;
        if (progress < required) return false;
        skillProgress.put(skill, progress - required);
        skills.computeIfPresent(skill, (id, level) -> level + 1);
        return true;
    }

    public int skillProgress(String skill) { return skillProgress.getOrDefault(skill, 0); }
    public int skillProgressRequired(String skill) { return skills.getOrDefault(skill, 1) * 100; }
    public int affinity(String affinity) { return affinities.getOrDefault(affinity, 0); }
    public int repetition(String affinity) { return repeatedActions.getOrDefault(affinity, 0); }
    public boolean isEvolved(String skill) { return skills.getOrDefault(skill, 0) >= SKILL_EVOLVED_LEVEL; }
    public int evolutionCooldownSeconds(String skill, long gameTime) {
        return (int) Math.max(0, (evolutionCooldowns.getOrDefault(skill, 0L) - gameTime + 19) / 20);
    }

    public int activeCooldownSeconds(String skill, long gameTime) {
        if ("shadow_monarch".equals(skill) || "unique_absolute_extraction".equals(skill)) return 0;
        return (int) Math.max(0, (activeCooldowns.getOrDefault(skill, 0L) - gameTime + 19) / 20);
    }

    public boolean activateSkill(String skill, long gameTime) {
        int cost = "architect_of_the_system".equals(sponsorConstellation) ? 20 : ACTIVE_SKILL_COST;
        if (!skills.containsKey(skill) || !SkillDefinitions.isAvailable(skill)
                || activeCooldownSeconds(skill, gameTime) > 0 || energy < cost) return false;
        energy -= cost;
        boolean isShadowSkill = "shadow_monarch".equals(skill) || "unique_absolute_extraction".equals(skill);
        if (!isShadowSkill) {
            activeCooldowns.put(skill, gameTime + ACTIVE_SKILL_COOLDOWN_TICKS);
        } else {
            activeCooldowns.remove(skill);
        }
        skillsActivated++;
        return true;
    }

    public boolean resetAttributes() {
        int spent = (strength - ATTR_BASE) + (vitality - ATTR_BASE) + (agility - ATTR_BASE)
                + (perception - ATTR_BASE) + (intelligence - ATTR_BASE) + (willpower - ATTR_BASE);
        if (spent <= 0) return false;
        strength = vitality = agility = perception = intelligence = willpower = ATTR_BASE;
        attributePoints += spent;
        return true;
    }

    public boolean restoreEnergy(int amount) {
        int max = maxEnergy();
        if (energy >= max) return false;
        energy = Math.min(max, energy + Math.max(1, amount));
        return true;
    }

    public void setEnergy(int amount) {
        this.energy = Math.max(0, Math.min(maxEnergy(), amount));
    }

    public void addEnergy(int amount) {
        restoreEnergy(amount);
    }

    public boolean consumeEnergy(int amount) {
        if (this.energy >= amount) {
            this.energy -= amount;
            return true;
        }
        return false;
    }

    public boolean regenerateEnergy() {
        int max = maxEnergy();
        if (energy >= max) return false;
        int regen = energyRegenRate();
        energy = Math.min(max, energy + regen);
        return true;
    }

    public void clearActiveCooldown(String skill) {
        if (skill != null) activeCooldowns.remove(skill);
    }

    public void setActiveCooldown(String skill, long gameTime, long cooldownTicks) {
        if (skill != null) activeCooldowns.put(skill, gameTime + cooldownTicks);
    }

    public int bossKills() { return bossKills; }
    public int distanceMoved() { return distanceMoved; }
    public int deaths() { return deaths; }
    public long lastDailyLoginDay() { return lastDailyLoginDay; }
    public void setLastDailyLoginDay(long day) { this.lastDailyLoginDay = day; }
    public int energy() { return energy; }
    public int blocksMined() { return blocksMined; }
    public int oresMined() { return oresMined; }
    public int creaturesDefeated() { return creaturesDefeated; }
    public int damageDealt() { return damageDealt; }
    public int damageTaken() { return damageTaken; }
    public int itemsCrafted() { return itemsCrafted; }
    public int skillsActivated() { return skillsActivated; }
    public int missionsCompleted() { return missionsCompleted; }
    public int purchases() { return purchases; }
    public int coinsSpent() { return coinsSpent; }
    public int prestigeLevel() { return prestige; }
    public int weeklyType(long week) { return weeklyWeek == week ? weeklyType : (int) Math.floorMod(week, 3); }
    public int weeklyProgress(long week) { return weeklyWeek == week ? weeklyProgress : 0; }
    public int weeklyTarget(long week) { return switch (weeklyType(week)) { case 1 -> 25; case 2 -> 10; default -> 50; }; }
    public boolean weeklyComplete(long week) { return weeklyWeek == week && weeklyComplete; }

    public boolean recordWeeklyProgress(long week, int type, int amount) {
        if (weeklyWeek != week) {
            weeklyWeek = week;
            weeklyType = (int) Math.floorMod(week, 3);
            weeklyProgress = 0;
            weeklyComplete = false;
        }
        if (weeklyComplete || weeklyType != type) return false;
        int before = weeklyProgress;
        weeklyProgress = Math.min(weeklyTarget(week), weeklyProgress + Math.max(1, amount));
        return before != weeklyProgress;
    }

    public boolean completeWeekly(long week) {
        if (weeklyComplete(week) || weeklyProgress(week) < weeklyTarget(week)) return false;
        weeklyComplete = true;
        missionsCompleted++;
        addExperience(250);
        addCoins(50);
        return true;
    }

    public boolean recordStatistic(String stat, int amount) {
        if (amount <= 0) return false;
        switch (stat) {
            case "blocks_mined" -> blocksMined += amount;
            case "ores_mined" -> oresMined += amount;
            case "creatures_defeated" -> creaturesDefeated += amount;
            case "damage_dealt" -> damageDealt += amount;
            case "damage_taken" -> damageTaken += amount;
            case "items_crafted" -> itemsCrafted += amount;
            case "boss_kills" -> bossKills += amount;
            case "distance_moved" -> distanceMoved += amount;
            case "deaths" -> deaths += amount;
            default -> { return false; }
        }
        return true;
    }

    public boolean recordPurchase(int cost) {
        purchases++;
        coinsSpent += Math.max(0, cost);
        return true;
    }

    public boolean unlockAchievement(String id) {
        if (achievements.getOrDefault(id, false)) return false;
        achievements.put(id, true);
        return true;
    }

    public boolean hasAchievement(String id) { return achievements.getOrDefault(id, false); }

    public static final int MAX_PRESTIGE = 100;

    public boolean canPrestige() { return level >= 1000 && prestige < MAX_PRESTIGE; }

    public boolean prestige() {
        if (!canPrestige()) return false;
        prestige++;
        level = 1;
        experience = 0;
        attributePoints = 0;
        int permanentBase = ATTR_BASE + (prestige * 2);
        strength = vitality = agility = perception = intelligence = willpower = permanentBase;
        affinities.clear();

        if (prestige >= 1 && !skills.isEmpty()) {
            String preservedSkill = skills.keySet().iterator().next();
            int preservedLvl = skills.get(preservedSkill);
            skills.clear();
            skills.put(preservedSkill, preservedLvl);
        } else {
            skills.clear();
        }

        skillProgress.clear();
        evolutionCooldowns.clear();
        activeCooldowns.clear();
        energy = MAX_ENERGY;
        awakeningMining = awakeningCombat = 0;
        awakeningComplete = false;
        if (!isConstellation) equippedTitle = "";
        return true;
    }

    public boolean canEvolveSkill(String skill, long gameTime) {
        String affinity = SkillDefinitions.affinity(skill);
        return SkillDefinitions.isAvailable(skill) && affinity != null && skills.getOrDefault(skill, 0) == SKILL_MAX_LEVEL
                && affinity(affinity) >= EVOLUTION_AFFINITY && attributePoints >= EVOLUTION_COST
                && evolutionCooldownSeconds(skill, gameTime) <= 0;
    }

    public boolean evolveSkill(String skill, long gameTime) {
        if (!canEvolveSkill(skill, gameTime)) return false;
        skills.put(skill, SKILL_EVOLVED_LEVEL);
        attributePoints -= EVOLUTION_COST;
        evolutionCooldowns.put(skill, gameTime + EVOLUTION_COOLDOWN_TICKS);
        return true;
    }

    public boolean recordAwakeningMining() {
        if (awakeningComplete || awakeningMining >= 16) return false;
        awakeningMining++;
        return true;
    }

    public boolean recordAwakeningCombat() {
        if (awakeningComplete || awakeningCombat >= 5) return false;
        awakeningCombat++;
        return true;
    }

    public boolean completeAwakening() {
        if (awakeningComplete || awakeningMining < 16 || awakeningCombat < 5) return false;
        awakeningComplete = true;
        skills.putIfAbsent("system_core", 1);
        return true;
    }

    public int awakeningMining() { return awakeningMining; }
    public int awakeningCombat() { return awakeningCombat; }
    public boolean awakeningComplete() { return awakeningComplete; }

    public boolean recordDailyProgress(long day, int type, int amount) {
        if (dailyDay != day) {
            if (dailyDay >= 0 && !dailyComplete && !dailyFailed && dailyProgress > 0) dailyFailed = true;
            dailyDay = day;
            dailyType = (int) Math.floorMod(day, 5);
            dailyProgress = 0;
            dailyComplete = false;
            dailyFailed = false;
        }
        if (level() >= 40 || dailyComplete || dailyFailed || dailyType != type) return false;
        int before = dailyProgress;
        dailyProgress = Math.min(dailyTarget(), dailyProgress + Math.max(1, amount));
        return before != dailyProgress;
    }

    public int dailyType(long day) { return dailyDay == day ? dailyType : (int) Math.floorMod(day, 5); }
    public int dailyProgress(long day) { return dailyDay == day ? dailyProgress : 0; }
    public int dailyTarget() { return dailyTarget(dailyDay); }
    public int dailyTarget(long day) {
        return switch (dailyType(day)) {
            case 1 -> 8;
            case 2 -> 12;
            case 3 -> 6;
            case 4 -> 300;
            default -> 32;
        };
    }
    public boolean dailyFailed(long day) { return dailyDay == day && dailyFailed; }
    public int dailyPenaltySeconds(long gameTime) { return (int) Math.max(0, (dailyPenaltyUntil - gameTime + 19) / 20); }
    public boolean dailyPenaltyActive(long gameTime) { return dailyPenaltySeconds(gameTime) > 0; }

    public boolean completeDaily(long gameTime) {
        if (level() >= 40 || dailyComplete || dailyFailed || dailyProgress < dailyTarget()) return false;
        dailyComplete = true;
        addExperience(50 + dailyType * 10);
        addCoins(12 + dailyType * 4);
        return true;
    }

    public void failDaily(long gameTime) {
        if (level() >= 40 || dailyComplete || dailyFailed) return;
        dailyFailed = true;
        dailyPenaltyUntil = Math.max(dailyPenaltyUntil, gameTime + 12000L);
    }

    public int dailyMining(long day) { return dailyType(day) == 0 ? dailyProgress(day) : 0; }
    public boolean recordDailyMining(long day) { return recordDailyProgress(day, 0, 1); }

    public boolean completeDailyMining() { return completeDaily(0L); }

    public boolean recordEmergencySurvival() {
        if (!awakeningComplete || emergencyComplete || emergencySurvival >= 3 || emergencyWounded) return false;
        emergencyWounded = true;
        return true;
    }

    public boolean confirmEmergencyRecovery() {
        if (!emergencyWounded) return false;
        emergencyWounded = false;
        emergencySurvival++;
        return emergencySurvival >= 3;
    }

    public void resetEmergencySurvival() {
        if (!emergencyComplete && emergencySurvival > 0) {
            emergencySurvival = 0;
        }
        emergencyWounded = false;
    }

    public boolean completeEmergency() {
        if (emergencyComplete || emergencySurvival < 3) return false;
        emergencyComplete = true;
        attributePoints++;
        emergencyWounded = false;
        return true;
    }

    public boolean emergencyWounded() { return emergencyWounded; }

    public boolean completeEvolutionMission() {
        if (evolutionMissionComplete) return false;
        evolutionMissionComplete = true;
        attributePoints += 2;
        return true;
    }

    public boolean completeSecretMission() {
        if (!awakeningComplete || secretMissionComplete) return false;
        secretMissionComplete = true;
        addExperience(75);
        return true;
    }

    public boolean completeClassMission() {
        if (!awakeningComplete || classMissionComplete) return false;
        String dominant = dominantAffinity();
        if (dominant == null || affinity(dominant) < 250) return false;
        classMissionComplete = true;
        attributePoints += 2;
        return true;
    }

    public int claimRankReward() {
        int milestone = nextClaimableRankMilestone();
        if (milestone == 0) return 0;
        highestRankReward = milestone;
        attributePoints++;
        return milestone;
    }

    private int nextClaimableRankMilestone() {
        int[] milestones = {5, 10, 20, 30, 40, 50, 70, 90, 110, 130, 150, 170, 190, 210, 230, 250, 270, 290, 310, 330, 350, 370, 400, 430, 460, 490, 520, 550, 580, 610, 640, 670, 700, 730, 760, 800, 840, 880, 920, 960, 1000};
        for (int milestone : milestones) {
            if (level >= milestone && milestone > highestRankReward) return milestone;
        }
        return 0;
    }

    public static String rankForLevel(int level) {
        if (level >= 1000) return "ABSOLUTE_GOD";
        if (level >= 960) return "LIMITLESS";
        if (level >= 920) return "SINGULARITY";
        if (level >= 880) return "ASTRAL_DEITY";
        if (level >= 840) return "TIMELESS_MONARCH";
        if (level >= 800) return "CHAOS_SOVEREIGN";
        if (level >= 760) return "ABSOLUTE_CREATOR";
        if (level >= 730) return "SUPREME_CREATOR";
        if (level >= 700) return "ABSOLUTE";
        if (level >= 670) return "ABSOLUTE_BEING";
        if (level >= 640) return "OMNIPOTENT";
        if (level >= 610) return "INFINITE";
        if (level >= 580) return "PRIMORDIAL";
        if (level >= 550) return "OMNIVERSAL";
        if (level >= 520) return "MULTIVERSAL";
        if (level >= 490) return "COSMIC";
        if (level >= 460) return "ANCESTRAL";
        if (level >= 430) return "DEITY";
        if (level >= 400) return "GOD";
        if (level >= 370) return "DEMIGOD";
        if (level >= 350) return "SUPREME_BEING";
        if (level >= 330) return "RULER";
        if (level >= 310) return "MONARCH";
        if (level >= 290) return "TRANSCENDENTAL";
        if (level >= 270) return "TRANSCENDENT";
        if (level >= 250) return "GR";
        if (level >= 230) return "MR";
        if (level >= 210) return "SLR";
        if (level >= 190) return "LR";
        if (level >= 170) return "UR+";
        if (level >= 150) return "UR";
        if (level >= 130) return "NATIONAL";
        if (level >= 110) return "SSS";
        if (level >= 90) return "SS";
        if (level >= 70) return "EX";
        if (level >= 50) return "S";
        if (level >= 40) return "A";
        if (level >= 30) return "B";
        if (level >= 20) return "C";
        if (level >= 10) return "D";
        if (level >= 5) return "E";
        return "F";
    }

    public static String rankTranslationKey(int level) {
        String raw = rankForLevel(level).toLowerCase(java.util.Locale.ROOT).replace("+", "_plus");
        return "awakening_system.rank." + raw;
    }

    public static net.minecraft.network.chat.Component rankComponent(int level) {
        return net.minecraft.network.chat.Component.translatable(rankTranslationKey(level));
    }

    public String rankTranslationKey() {
        return rankTranslationKey(level);
    }

    public net.minecraft.network.chat.Component rankComponent() {
        return rankComponent(level);
    }

    private String dominantAffinity() {
        String dominant = null;
        int best = -1;
        for (Map.Entry<String, Integer> entry : affinities.entrySet()) {
            if (entry.getValue() > best) {
                best = entry.getValue();
                dominant = entry.getKey();
            }
        }
        return dominant;
    }

    public boolean dailyComplete(long day) { return dailyDay == day && dailyComplete; }
    public int emergencySurvival() { return emergencySurvival; }
    public boolean emergencyComplete() { return emergencyComplete; }
    public boolean evolutionMissionComplete() { return evolutionMissionComplete; }
    public boolean secretMissionComplete() { return secretMissionComplete; }
    public int classAffinity() {
        String dominant = dominantAffinity();
        return dominant == null ? 0 : affinity(dominant);
    }

    public boolean classMissionComplete() { return classMissionComplete; }

    public boolean addAttribute(String attribute) {
        if (attributePoints <= 0 || attribute == null) return false;
        String normalized = java.text.Normalizer.normalize(attribute.trim().toLowerCase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        switch (normalized) {
            case "fuerza", "strength" -> strength++;
            case "vitalidad", "vitality" -> vitality++;
            case "agilidad", "agility" -> agility++;
            case "percepcion", "perception", "per" -> perception++;
            case "inteligencia", "intelligence", "int" -> intelligence++;
            case "voluntad", "willpower", "vol" -> willpower++;
            default -> { return false; }
        }
        attributePoints--;
        return true;
    }

    public void grantAll() {
        level = 1000;
        experience = 0;
        awakeningComplete = true;
        awakeningMining = 16;
        awakeningCombat = 5;
        dailyComplete = true;
        emergencyComplete = true;
        emergencySurvival = 3;
        evolutionMissionComplete = true;
        secretMissionComplete = true;
        classMissionComplete = true;
        highestRankReward = 1000;
        attributePoints = Math.max(attributePoints, 5000);
        strength = Math.max(strength, 100);
        vitality = Math.max(vitality, 100);
        agility = Math.max(agility, 100);
        perception = Math.max(perception, 100);
        intelligence = Math.max(intelligence, 100);
        willpower = Math.max(willpower, 100);

        creaturesDefeated = Math.max(creaturesDefeated, 1000);
        blocksMined = Math.max(blocksMined, 2000);
        oresMined = Math.max(oresMined, 500);
        itemsCrafted = Math.max(itemsCrafted, 100);
        skillsActivated = Math.max(skillsActivated, 100);
        purchases = Math.max(purchases, 50);
        missionsCompleted = Math.max(missionsCompleted, 50);
        bossKills = Math.max(bossKills, 25);
        distanceMoved = Math.max(distanceMoved, 10000);

        for (AchievementDefinitions.Definition achievement : AchievementDefinitions.definitions()) {
            achievements.put(achievement.id(), true);
        }

        String[] allAffinities = {"mining", "combat", "survival", "mobility", "depth", "perception", "fire", "nature", "crafting", "alchemy", "protection", "ranged", "eclipse", "system"};
        for (String affinity : allAffinities) affinities.put(affinity, 250);

        disabledSkills.clear();

        for (String skill : SkillDefinitions.ALL_BASE_SKILLS) {
            if (SkillDefinitions.isAvailable(skill)) {
                skills.put(skill, SKILL_EVOLVED_LEVEL);
                skillProgress.put(skill, 0);
            }
        }
        equippedTitle = "rank_absolute_creator";
        if (equippedSkill == null || equippedSkill.isEmpty()) {
            equippedSkill = "rulers_authority";
        }
        if (sponsorConstellation == null) {
            sponsorConstellation = "";
        }
        ensureEquippedActiveSlots();
        ensureEquippedPassiveSlots();
    }

    public void removeUnavailableContent() {
        skills.keySet().removeIf(skill -> {
            if (skill == null || skill.isEmpty()) return true;
            if (ClassSkillTree.isSkillTreeNode(skill)) return false;
            return !SkillDefinitions.isAvailable(skill);
        });
        if (!equippedTitle.isEmpty() && !TitleDefinitions.available(this).contains(equippedTitle)) equippedTitle = "";
    }

    public int level() { return level; }
    public int experience() { return experience; }
    public int nextLevelExperience() { return experienceToNextLevel(); }
    public int attributePoints() { return attributePoints; }
    public int coins() { return coins; }
    public void setCoins(int amount) { coins = Math.max(0, amount); }
    public void addCoins(int amount) { coins = Math.max(0, coins + amount); }
    public boolean spendCoins(int amount) {
        if (amount < 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }
    public int getCultivationStatBonus() {
        return (dantianRealm * 10) + Math.max(0, cultivationSubLevel - 1) * 2;
    }

    public int rawStrength() { return strength; }
    public int rawVitality() { return vitality; }
    public int rawAgility() { return agility; }
    public int rawPerception() { return perception; }
    public int rawIntelligence() { return intelligence; }
    public int rawWillpower() { return willpower; }

    public int strength() { return strength + getCultivationStatBonus(); }
    public int vitality() { return vitality + getCultivationStatBonus(); }
    public int agility() { return agility + getCultivationStatBonus(); }
    public int perception() { return perception + getCultivationStatBonus(); }
    public int intelligence() { return intelligence + getCultivationStatBonus(); }
    public int willpower() { return willpower + getCultivationStatBonus(); }
    public boolean agilitySpeedEnabled() { return agilitySpeedEnabled; }
    public void setAgilitySpeedEnabled(boolean enabled) { this.agilitySpeedEnabled = enabled; }
    public boolean hasNoAffinityProgress() { return affinities.isEmpty(); }
    public Map<String, Integer> skills() { return skills; }
    public boolean hasEvolvedSkill() { return skills.values().stream().anyMatch(level -> level >= 6); }

    public String rank() {
        return rankForLevel(level);
    }

    public int nextRankLevel() {
        if (level < 5) return 5;
        if (level < 10) return 10;
        if (level < 20) return 20;
        if (level < 30) return 30;
        if (level < 40) return 40;
        if (level < 50) return 50;
        if (level < 70) return 70;
        if (level < 90) return 90;
        if (level < 110) return 110;
        if (level < 130) return 130;
        if (level < 150) return 150;
        if (level < 170) return 170;
        if (level < 190) return 190;
        if (level < 210) return 210;
        if (level < 230) return 230;
        if (level < 250) return 250;
        if (level < 270) return 270;
        if (level < 290) return 290;
        if (level < 310) return 310;
        if (level < 330) return 330;
        if (level < 350) return 350;
        if (level < 370) return 370;
        if (level < 390) return 390;
        if (level < 410) return 410;
        if (level < 430) return 430;
        if (level < 450) return 450;
        if (level < 470) return 470;
        if (level < 490) return 490;
        if (level < 510) return 510;
        if (level < 540) return 540;
        if (level < 570) return 570;
        if (level < 600) return 600;
        if (level < 630) return 630;
        if (level < 660) return 660;
        if (level < 700) return 700;
        if (level < 750) return 750;
        if (level < 800) return 800;
        if (level < 850) return 850;
        if (level < 900) return 900;
        if (level < 950) return 950;
        if (level < 1000) return 1000;
        return 0;
    }

    public String equippedTitle() { return equippedTitle; }

    public boolean canEquipTitle(String titleId) {
        return TitleDefinitions.available(this).contains(titleId);
    }

    public boolean equipTitle(String titleId) {
        if (!canEquipTitle(titleId)) return false;
        equippedTitle = titleId;
        return true;
    }

    public void setEquippedTitle(String title) {
        this.equippedTitle = title == null ? "" : title;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public void setCreator(boolean creator) {
        this.isCreator = creator;
    }

    public boolean isNanda() {
        return isNanda;
    }

    public void setNanda(boolean nanda) {
        this.isNanda = nanda;
    }

    public boolean isMisticPotato() {
        return isMisticPotato;
    }

    public void setMisticPotato(boolean misticPotato) {
        this.isMisticPotato = misticPotato;
    }

    public boolean isSchvis() {
        return isSchvis;
    }

    public void setSchvis(boolean schvis) {
        this.isSchvis = schvis;
    }

    public boolean isTomcraft() {
        return isTomcraft;
    }

    public void setTomcraft(boolean tomcraft) {
        this.isTomcraft = tomcraft;
    }

    public String classId() {
        String dominant = null;
        int best = -1;
        for (Map.Entry<String, Integer> affinity : affinities.entrySet()) {
            if (affinity.getValue() > best) {
                best = affinity.getValue();
                dominant = affinity.getKey();
            }
        }
        if (best < 100) return "potential";
        return switch (dominant == null ? "" : dominant) {
            case "mining" -> "miner";
            case "combat", "ranged", "protection" -> "warrior";
            case "nature", "alchemy", "fire" -> "sage";
            default -> "wanderer";
        };
    }

    public String classKey() {
        return "awakening_system.screen.titles.class." + classId();
    }

    public int comboCount() { return comboCount; }
    public int comboTicks() { return comboTicks; }
    public int tranceTicks() { return tranceTicks; }
    public boolean isInTrance() { return tranceTicks > 0; }

    public boolean addCombo() {
        comboTicks = 80;
        if (tranceTicks > 0) {
            comboCount++;
            return false;
        }
        comboCount++;
        if (comboCount >= 10) {
            comboCount = 10;
            tranceTicks = 200;
            return true;
        }
        return false;
    }

    public void resetCombo() {
        comboCount = 0;
        comboTicks = 0;
    }

    public void tickCombat() {
        if (tranceTicks > 0) {
            tranceTicks--;
            if (tranceTicks == 0) {
                comboCount = 0;
                comboTicks = 0;
            }
        } else if (comboTicks > 0) {
            comboTicks--;
            if (comboTicks == 0) {
                comboCount = 0;
            }
        } else if (comboCount > 0) {
            comboCount = 0;
        }
    }

    public String sponsorConstellation() {
        return sponsorConstellation == null ? "" : sponsorConstellation;
    }

    public boolean setSponsorConstellation(String constellationId) {
        if (constellationId == null || constellationId.isEmpty()) {
            this.sponsorConstellation = "";
            return true;
        }
        if (ConstellationDefinitions.get(constellationId) != null) {
            this.sponsorConstellation = constellationId;
            return true;
        }
        return false;
    }

    public boolean hasSponsor() {
        return sponsorConstellation != null && !sponsorConstellation.isEmpty();
    }

    public String activeVision() {
        return activeVision != null ? activeVision : "NONE";
    }

    public void setActiveVision(String vision) {
        this.activeVision = (vision == null || vision.isBlank()) ? "NONE" : vision.trim().toUpperCase(java.util.Locale.ROOT);
    }

    public boolean hasActiveVision() {
        return activeVision != null && !activeVision.isEmpty() && !"NONE".equalsIgnoreCase(activeVision);
    }

    public boolean isConstellation() {
        return isConstellation;
    }

    public void setIsConstellation(boolean isConstellation) {
        this.isConstellation = isConstellation;
    }

    public String constellationModifier() {
        return constellationModifier == null ? "" : constellationModifier;
    }

    public void setConstellationModifier(String modifier) {
        this.constellationModifier = (modifier == null) ? "" : modifier;
    }

    public String activeScenario() {
        if ("depth_expedition".equals(activeScenario) || "abyssal_conquest".equals(activeScenario) || "survival_trial".equals(activeScenario)) {
            return activeScenario;
        }
        return "night_hunt";
    }

    public int scenarioProgress() { return scenarioProgress; }
    public int scenarioTarget() { return Math.max(1, scenarioTarget); }
    public boolean isScenarioComplete() { return scenarioComplete; }

    public boolean progressScenario(String scenarioId, int amount) {
        if (scenarioComplete || !activeScenario().equals(scenarioId)) return false;
        scenarioProgress = Math.min(scenarioTarget(), scenarioProgress + amount);
        if (scenarioProgress >= scenarioTarget()) {
            scenarioComplete = true;
            return true;
        }
        return false;
    }

    public boolean claimScenarioReward() {
        if (!scenarioComplete) return false;
        advanceToNextScenario();
        return true;
    }

    public void advanceToNextScenario() {
        String current = activeScenario();
        String next = switch (current) {
            case "night_hunt" -> "depth_expedition";
            case "depth_expedition" -> "abyssal_conquest";
            case "abyssal_conquest" -> "survival_trial";
            default -> "night_hunt";
        };
        int nextTarget = switch (next) {
            case "depth_expedition" -> 15;
            case "abyssal_conquest" -> 1;
            case "survival_trial" -> 20;
            default -> 10;
        };
        this.activeScenario = next;
        this.scenarioTarget = nextTarget;
        this.scenarioProgress = 0;
        this.scenarioComplete = false;
    }

    public void setLevel(int targetLevel) {
        int oldLevel = this.level;
        this.level = Math.max(1, targetLevel);
        if (this.level > oldLevel) {
            this.attributePoints += (this.level - oldLevel) * ATTR_POINTS_PER_LEVEL;
        }
        this.experience = 0;
    }

    public void triggerTranceDirect(int ticks) {
        this.tranceTicks = ticks;
        this.comboCount = Math.max(10, this.comboCount);
    }

    public void setComboCount(int count) {
        this.comboCount = Math.max(0, count);
        this.comboTicks = 80;
    }

    public void completeScenarioDirect() {
        this.scenarioProgress = scenarioTarget();
        this.scenarioComplete = true;
    }

    public void setScenarioDirect(String id, int target) {
        this.activeScenario = id;
        this.scenarioTarget = Math.max(1, target);
        this.scenarioProgress = 0;
        this.scenarioComplete = false;
    }

    public long phoenixCooldownUntil() {
        return phoenixCooldownUntil;
    }

    public void setPhoenixCooldownUntil(long until) {
        this.phoenixCooldownUntil = until;
    }

    public String equippedSkill() {
        return getEquippedActiveSkill(0);
    }

    public String defaultActiveSkill() {
        return skills.keySet().stream()
                .filter(SkillDefinitions::isAvailable)
                .filter(SkillDefinitions::isActive)
                .findFirst().orElse(skills.keySet().stream().filter(SkillDefinitions::isAvailable).findFirst().orElse(""));
    }

    public boolean setEquippedSkill(String skillId) {
        return setEquippedActiveSkill(0, skillId);
    }

    public int getUnlockedTreeSkillsCount() {
        String cls = hunterClass();
        if (cls == null || cls.isEmpty() || "none".equals(cls)) return 0;
        java.util.List<ClassSkillTree.SkillNode> nodes = ClassSkillTree.getNodesForClass(cls);
        int count = 0;
        for (ClassSkillTree.SkillNode node : nodes) {
            if (skills.containsKey(node.skillId())) {
                count++;
            }
        }
        return count;
    }

    public int getUnlockedActiveSkillSlots() {
        int count = getUnlockedTreeSkillsCount();
        if (count >= 5) return 5;
        if (count >= 3) return 4;
        if (count >= 1) return 3;
        return 2;
    }

    public int getUnlockedPassiveSkillSlots() {
        int count = getUnlockedTreeSkillsCount();
        if (count >= 4) return 3;
        if (count >= 2) return 2;
        return 1;
    }

    public java.util.List<String> equippedActiveSkills() {
        ensureEquippedActiveSlots();
        return java.util.Collections.unmodifiableList(equippedActiveSkills);
    }

    public String getEquippedActiveSkill(int slot) {
        ensureEquippedActiveSlots();
        if (slot >= 0 && slot < getUnlockedActiveSkillSlots() && slot < equippedActiveSkills.size()) {
            String s = equippedActiveSkills.get(slot);
            if (s != null && !s.isEmpty() && skills.containsKey(s) && SkillDefinitions.isAvailable(s)) {
                return s;
            }
        }
        return "";
    }

    public String equippedActiveSkill(int slot) {
        return getEquippedActiveSkill(slot);
    }

    public boolean setEquippedActiveSkill(int slot, String skillId) {
        ensureEquippedActiveSlots();
        if (slot < 0 || slot >= 5) return false;
        if (slot >= getUnlockedActiveSkillSlots()) return false;
        if (skillId == null || skillId.isEmpty()) {
            equippedActiveSkills.set(slot, "");
            if (slot == 0) this.equippedSkill = "";
            return true;
        }
        if (skills.containsKey(skillId) && SkillDefinitions.isAvailable(skillId) && SkillDefinitions.isActive(skillId)) {
            for (int i = 0; i < 5; i++) {
                if (i != slot && skillId.equals(equippedActiveSkills.get(i))) {
                    equippedActiveSkills.set(i, "");
                }
            }
            equippedActiveSkills.set(slot, skillId);
            if (slot == 0) this.equippedSkill = skillId;
            return true;
        }
        return false;
    }

    public java.util.List<String> equippedPassiveSkills() {
        ensureEquippedPassiveSlots();
        return java.util.Collections.unmodifiableList(equippedPassiveSkills);
    }

    public String getEquippedPassiveSkill(int slot) {
        ensureEquippedPassiveSlots();
        if (slot >= 0 && slot < getUnlockedPassiveSkillSlots() && slot < equippedPassiveSkills.size()) {
            String s = equippedPassiveSkills.get(slot);
            if (s != null && !s.isEmpty() && skills.containsKey(s) && SkillDefinitions.isAvailable(s)) {
                return s;
            }
        }
        return "";
    }

    public String equippedPassiveSkill(int slot) {
        return getEquippedPassiveSkill(slot);
    }

    public boolean setEquippedPassiveSkill(int slot, String skillId) {
        ensureEquippedPassiveSlots();
        if (slot < 0 || slot >= 3) return false;
        if (slot >= getUnlockedPassiveSkillSlots()) return false;
        if (skillId == null || skillId.isEmpty()) {
            equippedPassiveSkills.set(slot, "");
            return true;
        }
        if (skills.containsKey(skillId) && SkillDefinitions.isAvailable(skillId) && SkillDefinitions.isPassive(skillId)) {
            for (int i = 0; i < 3; i++) {
                if (i != slot && skillId.equals(equippedPassiveSkills.get(i))) {
                    equippedPassiveSkills.set(i, "");
                }
            }
            equippedPassiveSkills.set(slot, skillId);
            return true;
        }
        return false;
    }

    public boolean isPassiveEquipped(String skillId) {
        ensureEquippedPassiveSlots();
        int maxSlots = getUnlockedPassiveSkillSlots();
        String base = skillId.endsWith("_evolution") ? skillId.substring(0, skillId.length() - 10) : skillId;
        for (int i = 0; i < maxSlots && i < equippedPassiveSkills.size(); i++) {
            String s = equippedPassiveSkills.get(i);
            if (s != null && !s.isEmpty()) {
                String sBase = s.endsWith("_evolution") ? s.substring(0, s.length() - 10) : s;
                if (s.equals(skillId) || sBase.equals(base)) return true;
            }
        }
        return false;
    }

    private void ensureEquippedActiveSlots() {
        while (equippedActiveSkills.size() < 5) equippedActiveSkills.add("");
        if (equippedActiveSkills.get(0).isEmpty()) {
            if (equippedSkill != null && !equippedSkill.isEmpty() && skills.containsKey(equippedSkill)) {
                equippedActiveSkills.set(0, equippedSkill);
            } else {
                String def = defaultActiveSkill();
                if (!def.isEmpty()) {
                    equippedActiveSkills.set(0, def);
                    equippedSkill = def;
                }
            }
        }
    }

    private void ensureEquippedPassiveSlots() {
        while (equippedPassiveSkills.size() < 3) equippedPassiveSkills.add("");
        boolean allEmpty = true;
        for (String s : equippedPassiveSkills) {
            if (s != null && !s.isEmpty()) { allEmpty = false; break; }
        }
        if (allEmpty) {
            int idx = 0;
            for (String sk : skills.keySet()) {
                if (sk != null && !sk.isEmpty() && SkillDefinitions.isAvailable(sk) && SkillDefinitions.isPassive(sk) && !disabledSkills.contains(sk)) {
                    while (equippedPassiveSkills.size() <= idx) equippedPassiveSkills.add("");
                    equippedPassiveSkills.set(idx++, sk);
                    if (idx >= 3) break;
                }
            }
        }
    }

    public int dantianRealm() { return dantianRealm; }
    public void setDantianRealm(int realm) { this.dantianRealm = Math.max(0, Math.min(9, realm)); }
    public int cultivationSubLevel() { return cultivationSubLevel; }
    public void setCultivationSubLevel(int subLevel) { this.cultivationSubLevel = Math.max(1, Math.min(9, subLevel)); }
    public int cultivationQi() { return cultivationQi; }
    public void setCultivationQi(int qi) { this.cultivationQi = Math.max(0, Math.min(maxCultivationQi(), qi)); }
    public int maxCultivationQi() {
        int realmFactor = dantianRealm * dantianRealm * 180;
        int base = 120 * (dantianRealm + 1) + (cultivationSubLevel * 50) + realmFactor + (getOpenedMeridiansCount() * 35);
        if (systemUpdated) base = (int)(base * 1.5);
        if (system30Updated) base = (int)(base * 1.8);
        if (system40Updated) base = (int)(base * 2.2);
        if ("primordial_chaos".equals(murimPhysique())) base *= 2;
        else if ("celestial_dragon".equals(murimPhysique())) base = (int)(base * 1.35);
        return base;
    }
    public void addCultivationQi(int amount) { this.cultivationQi = Math.max(0, Math.min(maxCultivationQi(), this.cultivationQi + amount)); }
    public boolean consumeCultivationQi(int amount) {
        if (this.cultivationQi >= amount) {
            this.cultivationQi -= amount;
            return true;
        }
        return false;
    }

    public int selectedDaoSkill() { return selectedDaoSkill; }
    public void setSelectedDaoSkill(int skill) { this.selectedDaoSkill = Math.max(0, Math.min(4, skill)); }
    public int tribulationShieldTicks() { return tribulationShieldTicks; }
    public void setTribulationShieldTicks(int ticks) { this.tribulationShieldTicks = Math.max(0, ticks); }
    public void tickTribulationShield() { if (tribulationShieldTicks > 0) tribulationShieldTicks--; }
    public int daoSkillCooldown() { return daoSkillCooldown; }
    public void setDaoSkillCooldown(int ticks) { this.daoSkillCooldown = Math.max(0, ticks); }
    public void tickDaoSkillCooldown() { if (daoSkillCooldown > 0) daoSkillCooldown--; }
    public boolean systemUpdated() { return systemUpdated; }
    public void setSystemUpdated(boolean updated) { this.systemUpdated = updated; }
    public boolean isekaiUnlocked() { return isekaiUnlocked; }
    public void setIsekaiUnlocked(boolean unlocked) { this.isekaiUnlocked = unlocked; }

    public String murimPhysique() { return murimPhysique == null ? "" : murimPhysique; }
    public void setMurimPhysique(String physique) { this.murimPhysique = physique == null ? "" : physique; }
    public boolean murimUnlocked() { return murimUnlocked; }
    public void setMurimUnlocked(boolean unlocked) { this.murimUnlocked = unlocked; }
    public boolean system30Updated() { return system30Updated; }
    public void setSystem30Updated(boolean updated) { this.system30Updated = updated; }
    public boolean system40Updated() { return system40Updated; }
    public void setSystem40Updated(boolean updated) { this.system40Updated = updated; }
    public boolean teyvatUnlocked() { return teyvatUnlocked; }
    public void setTeyvatUnlocked(boolean unlocked) { this.teyvatUnlocked = unlocked; }

    public int anemoStatueLevel() { return Math.max(1, anemoStatueLevel); }
    public void setAnemoStatueLevel(int v) { this.anemoStatueLevel = Math.min(10, Math.max(1, v)); }
    public int anemoOculiProgress() { return Math.max(0, anemoOculiProgress); }
    public void setAnemoOculiProgress(int v) { this.anemoOculiProgress = Math.max(0, v); }

    public int geoStatueLevel() { return Math.max(1, geoStatueLevel); }
    public void setGeoStatueLevel(int v) { this.geoStatueLevel = Math.min(10, Math.max(1, v)); }
    public int geoOculiProgress() { return Math.max(0, geoOculiProgress); }
    public void setGeoOculiProgress(int v) { this.geoOculiProgress = Math.max(0, v); }

    public int electroStatueLevel() { return Math.max(1, electroStatueLevel); }
    public void setElectroStatueLevel(int v) { this.electroStatueLevel = Math.min(10, Math.max(1, v)); }
    public int electroOculiProgress() { return Math.max(0, electroOculiProgress); }
    public void setElectroOculiProgress(int v) { this.electroOculiProgress = Math.max(0, v); }

    public int dendroStatueLevel() { return Math.max(1, dendroStatueLevel); }
    public void setDendroStatueLevel(int v) { this.dendroStatueLevel = Math.min(10, Math.max(1, v)); }
    public int dendroOculiProgress() { return Math.max(0, dendroOculiProgress); }
    public void setDendroOculiProgress(int v) { this.dendroOculiProgress = Math.max(0, v); }

    public int statueEnergyBonus() { return statueEnergyBonus; }
    public void setStatueEnergyBonus(int v) { this.statueEnergyBonus = Math.max(0, v); }
    public void addStatueEnergyBonus(int v) { this.statueEnergyBonus = Math.max(0, this.statueEnergyBonus + v); }

    public static int getOculiRequiredForLevel(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> 1;
            case 3 -> 2;
            case 4 -> 4;
            case 5 -> 6;
            case 6 -> 8;
            case 7 -> 10;
            case 8 -> 12;
            case 9 -> 15;
            case 10 -> 20;
            default -> 999;
        };
    }

    public boolean hasVisitedDimension(String dim) {
        if (dim == null || dim.isEmpty()) return true;
        if ("minecraft:overworld".equals(dim) || "overworld".equals(dim)) return true;
        if (("awakening_system:isekai".equals(dim) || "isekai".equals(dim)) && (systemUpdated || isekaiUnlocked)) return true;
        if (("awakening_system:murim".equals(dim) || "murim".equals(dim)) && (system30Updated || murimUnlocked)) return true;
        if (("awakening_system:teyvat_world".equals(dim) || "awakening_system:teyvat".equals(dim) || "teyvat_world".equals(dim) || "teyvat".equals(dim)) && (system40Updated || teyvatUnlocked)) return true;
        return visitedDimensions.contains(dim) || visitedDimensions.contains(dim.replace("minecraft:", ""));
    }

    public void addVisitedDimension(String dim) {
        if (dim != null && !dim.isEmpty()) {
            visitedDimensions.add(dim);
        }
    }

    public Set<String> visitedDimensions() {
        return Collections.unmodifiableSet(visitedDimensions);
    }

    public float getPhysiqueDamageMultiplier() {
        return switch (murimPhysique()) {
            case "pure_yang" -> 0.25F;
            case "innate_sword" -> 0.35F;
            case "primordial_chaos" -> 0.20F;
            case "heavenly_demon" -> 0.30F;
            default -> 0.0F;
        };
    }

    public float getPhysiqueSpeedBonus() {
        return switch (murimPhysique()) {
            case "nine_yin" -> 0.30F;
            case "innate_sword" -> 0.15F;
            case "primordial_chaos" -> 0.15F;
            default -> 0.0F;
        };
    }

    public int getPhysiqueMaxHpBonus() {
        return switch (murimPhysique()) {
            case "celestial_dragon" -> 50;
            case "primordial_chaos" -> 30;
            case "heavenly_demon" -> 20;
            default -> 0;
        };
    }

    public float getPhysiqueQiRegenMultiplier() {
        return switch (murimPhysique()) {
            case "pure_yang" -> 1.4F;
            case "primordial_chaos" -> 1.6F;
            case "nine_yin" -> 1.35F;
            case "celestial_dragon" -> 1.25F;
            default -> 1.0F;
        };
    }

    public boolean isUnderTribulation() { return tribulationStrikesRemaining > 0 || tribulationTicks > 0; }
    public int getTribulationTicks() { return tribulationTicks; }
    public int getTribulationStrikesRemaining() { return tribulationStrikesRemaining; }
    public int getTribulationTotalStrikes() { return tribulationTotalStrikes; }
    public int getPendingTribulationRealm() { return pendingTribulationRealm; }
    public void startTribulation(int targetRealm, int strikes) {
        this.pendingTribulationRealm = targetRealm;
        this.tribulationStrikesRemaining = strikes;
        this.tribulationTotalStrikes = strikes;
        this.tribulationTicks = 40;
    }
    public void tickTribulation() { if (tribulationTicks > 0) tribulationTicks--; }
    public void resetTribulationTicks(int nextDelay) { this.tribulationTicks = nextDelay; }
    public void decrementTribulationStrikes() { if (tribulationStrikesRemaining > 0) tribulationStrikesRemaining--; }
    public void clearTribulation() {
        this.tribulationTicks = 0;
        this.tribulationStrikesRemaining = 0;
        this.tribulationTotalStrikes = 0;
        this.pendingTribulationRealm = 0;
    }

    public int getPhysiqueSkillCooldown() { return physiqueSkillCooldown; }
    public void setPhysiqueSkillCooldown(int cd) { this.physiqueSkillCooldown = Math.max(0, cd); }
    public void tickPhysiqueSkillCooldown() { if (physiqueSkillCooldown > 0) physiqueSkillCooldown--; }

    public boolean isMeridianOpen(int index) {
        if (index >= 0 && index < 12) return meridians[index];
        return false;
    }
    public boolean openMeridian(int index) {
        if (index >= 0 && index < 12 && !meridians[index]) {
            meridians[index] = true;
            return true;
        }
        return false;
    }
    public int getOpenedMeridiansCount() {
        int count = 0;
        for (boolean b : meridians) if (b) count++;
        return count;
    }
    public boolean[] getMeridians() { return meridians.clone(); }

    public long stigmaCooldownUntil() { return stigmaCooldownUntil; }
    public void setStigmaCooldownUntil(long until) { this.stigmaCooldownUntil = Math.max(0L, until); }
    public NonNullList<ItemStack> getSubspaceItems() {
        return subspaceItems;
    }

    public ItemStack addToSubspace(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack toAdd = stack.copy();

        int limit = Math.min(subspaceItems.size(), Math.max(27, subspaceUnlockedSlots));

        for (int i = 0; i < limit; i++) {
            ItemStack existing = subspaceItems.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, toAdd)) {
                int maxStack = Math.min(existing.getMaxStackSize(), 64);
                int space = maxStack - existing.getCount();
                if (space > 0) {
                    int move = Math.min(space, toAdd.getCount());
                    existing.grow(move);
                    toAdd.shrink(move);
                    if (toAdd.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }

        for (int i = 0; i < limit; i++) {
            if (subspaceItems.get(i).isEmpty()) {
                subspaceItems.set(i, toAdd.copy());
                return ItemStack.EMPTY;
            }
        }

        return toAdd;
    }

    public NonNullList<ItemStack> getArtifactSlots() {
        return artifactSlots;
    }

    public ItemStack getArtifact(int slot) {
        if (slot >= 0 && slot < ARTIFACT_SLOTS) {
            return artifactSlots.get(slot);
        }
        return ItemStack.EMPTY;
    }

    public void setArtifact(int slot, ItemStack stack) {
        if (slot >= 0 && slot < ARTIFACT_SLOTS) {
            artifactSlots.set(slot, stack == null ? ItemStack.EMPTY : stack);
        }
    }

    public float getArtifactBonusHp() {
        ItemStack flower = getArtifact(0);
        if (!flower.isEmpty() && flower.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
            return art.getHpBonus(flower);
        }
        return 0.0F;
    }

    public float getArtifactBonusAttack() {
        ItemStack plume = getArtifact(1);
        float atk = 0.0F;
        if (!plume.isEmpty() && plume.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
            atk += art.getAttackBonus(plume);
        }
        if (hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE, 2)
                || hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet.BRAVE_HEART, 2)) {
            atk += 10.0F;
        }
        return atk;
    }

    public float getArtifactBonusCritRate() {
        ItemStack circlet = getArtifact(4);
        float crit = 0.0F;
        if (!circlet.isEmpty() && circlet.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
            crit += art.getCritRatePercent(circlet);
        }
        if (hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet.BERSERKER, 2)) {
            crit += 12.0F;
        }
        return crit;
    }

    public float getArtifactBonusCritDmg() {
        ItemStack circlet = getArtifact(4);
        if (!circlet.isEmpty() && circlet.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
            return art.getCritDmgPercent(circlet);
        }
        return 0.0F;
    }

    public Map<com.soybe.awakeningsystem.artifact.ArtifactSet, Integer> getActiveArtifactSetPieces() {
        Map<com.soybe.awakeningsystem.artifact.ArtifactSet, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < ARTIFACT_SLOTS; i++) {
            ItemStack stack = artifactSlots.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
                counts.put(art.getArtifactSet(), counts.getOrDefault(art.getArtifactSet(), 0) + 1);
            }
        }
        return counts;
    }

    public boolean hasArtifactSetBonus(com.soybe.awakeningsystem.artifact.ArtifactSet set, int count) {
        return getActiveArtifactSetPieces().getOrDefault(set, 0) >= count;
    }

    public ItemStack getEquippedLightCone() {
        return equippedLightCone != null ? equippedLightCone : ItemStack.EMPTY;
    }

    public void setEquippedLightCone(ItemStack stack) {
        this.equippedLightCone = stack == null ? ItemStack.EMPTY : stack;
    }

    public boolean hasEquippedLightCone() {
        return equippedLightCone != null && !equippedLightCone.isEmpty();
    }

    public float getLightConeBonusAttack() {
        if (!hasEquippedLightCone()) return 0.0F;
        net.minecraft.world.item.Item item = equippedLightCone.getItem();
        if (item == SystemItems.LIGHT_CONE_PASSING_SHORE.get()) return 0.25F;
        if (item == SystemItems.LIGHT_CONE_DREAMS_REST.get()) return 0.25F;
        if (item == SystemItems.LIGHT_CONE_PATIENCE.get()) return 0.24F;
        if (item == SystemItems.LIGHT_CONE_MILKY_WAY.get()) return 0.20F;
        if (item == SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) return 0.20F;
        return 0.15F;
    }

    public float getLightConeBonusCritRate() {
        if (!hasEquippedLightCone()) return 0.0F;
        net.minecraft.world.item.Item item = equippedLightCone.getItem();
        if (item == SystemItems.LIGHT_CONE_PASSING_SHORE.get()) return 0.12F;
        if (item == SystemItems.LIGHT_CONE_PATIENCE.get()) return 0.10F;
        if (item == SystemItems.LIGHT_CONE_MILKY_WAY.get()) return 0.08F;
        return 0.05F;
    }

    public float getLightConeBonusCritDmg() {
        if (!hasEquippedLightCone()) return 0.0F;
        net.minecraft.world.item.Item item = equippedLightCone.getItem();
        if (item == SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) return 0.35F;
        if (item == SystemItems.LIGHT_CONE_PASSING_SHORE.get()) return 0.30F;
        if (item == SystemItems.LIGHT_CONE_DREAMS_REST.get()) return 0.20F;
        if (item == SystemItems.LIGHT_CONE_MILKY_WAY.get()) return 0.15F;
        return 0.15F;
    }

    public float getLightConeBonusEnergyRecharge() {
        if (!hasEquippedLightCone()) return 0.0F;
        net.minecraft.world.item.Item item = equippedLightCone.getItem();
        if (item == SystemItems.LIGHT_CONE_MILKY_WAY.get()) return 0.20F;
        if (item == SystemItems.LIGHT_CONE_DREAMS_REST.get()) return 0.25F;
        return 0.10F;
    }

    public ItemStack getActiveRune(int slot) {
        if (slot >= 0 && slot < ACTIVE_RUNE_SLOTS) return activeRuneSlots.get(slot);
        return ItemStack.EMPTY;
    }

    public void setActiveRune(int slot, ItemStack stack) {
        if (slot >= 0 && slot < ACTIVE_RUNE_SLOTS) {
            activeRuneSlots.set(slot, stack == null ? ItemStack.EMPTY : stack);
        }
    }

    public ItemStack getPassiveRune(int slot) {
        if (slot >= 0 && slot < PASSIVE_RUNE_SLOTS) return passiveRuneSlots.get(slot);
        return ItemStack.EMPTY;
    }

    public void setPassiveRune(int slot, ItemStack stack) {
        if (slot >= 0 && slot < PASSIVE_RUNE_SLOTS) {
            passiveRuneSlots.set(slot, stack == null ? ItemStack.EMPTY : stack);
        }
    }

    public NonNullList<ItemStack> getActiveRuneSlots() {
        return activeRuneSlots;
    }

    public NonNullList<ItemStack> getPassiveRuneSlots() {
        return passiveRuneSlots;
    }

    public boolean hasPassiveRune(com.soybe.awakeningsystem.item.RuneStoneItem.RuneType type) {
        for (ItemStack s : passiveRuneSlots) {
            if (!s.isEmpty() && s.getItem() instanceof com.soybe.awakeningsystem.item.RuneStoneItem rune && rune.getRuneType() == type) {
                return true;
            }
        }
        return false;
    }

    public long getActiveRuneCooldownUntil(int slot) {
        if (slot >= 0 && slot < ACTIVE_RUNE_SLOTS) return activeRuneCooldownUntil[slot];
        return 0L;
    }

    public void setActiveRuneCooldownUntil(int slot, long cdUntil) {
        if (slot >= 0 && slot < ACTIVE_RUNE_SLOTS) {
            activeRuneCooldownUntil[slot] = cdUntil;
        }
    }

    public int getActiveRuneCooldownSeconds(int slot, long gameTime) {
        if (slot >= 0 && slot < ACTIVE_RUNE_SLOTS) {
            long remainingTicks = activeRuneCooldownUntil[slot] - gameTime;
            return remainingTicks > 0 ? (int) Math.ceil(remainingTicks / 20.0) : 0;
        }
        return 0;
    }

    public boolean hasChosenInitialVision() {
        return hasChosenInitialVision;
    }

    public void setChosenInitialVision(boolean chosen) {
        this.hasChosenInitialVision = chosen;
    }

    public long getVisionResonanceCooldownUntil() {
        return visionResonanceCooldownUntil;
    }

    public void setVisionResonanceCooldownUntil(long until) {
        this.visionResonanceCooldownUntil = until;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putInt("experience", experience);
        tag.putInt("attribute_points", attributePoints);
        tag.putInt("coins", coins);
        tag.putInt("strength", strength);
        tag.putInt("vitality", vitality);
        tag.putInt("agility", agility);
        tag.putInt("perception", perception);
        tag.putInt("intelligence", intelligence);
        tag.putInt("willpower", willpower);
        tag.putBoolean("agilitySpeedEnabled", agilitySpeedEnabled);
        CompoundTag affinityTag = new CompoundTag();
        affinities.forEach(affinityTag::putInt);
        tag.put("affinities", affinityTag);
        CompoundTag skillTag = new CompoundTag();
        skills.forEach(skillTag::putInt);
        tag.put("skills", skillTag);
        CompoundTag skillProgressTag = new CompoundTag();
        skillProgress.forEach(skillProgressTag::putInt);
        tag.put("skill_progress", skillProgressTag);
        CompoundTag cooldownTag = new CompoundTag();
        evolutionCooldowns.forEach(cooldownTag::putLong);
        tag.put("evolution_cooldowns", cooldownTag);
        CompoundTag activeCooldownTag = new CompoundTag();
        activeCooldowns.forEach(activeCooldownTag::putLong);
        tag.put("active_cooldowns", activeCooldownTag);
        tag.putInt("energy", energy);
        tag.putInt("blocks_mined", blocksMined);
        tag.putInt("ores_mined", oresMined);
        tag.putInt("creatures_defeated", creaturesDefeated);
        tag.putInt("damage_dealt", damageDealt);
        tag.putInt("damage_taken", damageTaken);
        tag.putInt("items_crafted", itemsCrafted);
        tag.putInt("skills_activated", skillsActivated);
        tag.putInt("missions_completed", missionsCompleted);
        tag.putInt("purchases", purchases);
        tag.putInt("coins_spent", coinsSpent);
        tag.putInt("boss_kills", bossKills);
        tag.putInt("distance_moved", distanceMoved);
        tag.putInt("deaths", deaths);
        tag.putLong("last_daily_login_day", lastDailyLoginDay);
        tag.putInt("prestige", prestige);
        CompoundTag achievementTag = new CompoundTag();
        achievements.forEach(achievementTag::putBoolean);
        tag.put("achievements", achievementTag);
        tag.putLong("weekly_week", weeklyWeek);
        tag.putInt("weekly_type", weeklyType);
        tag.putInt("weekly_progress", weeklyProgress);
        tag.putBoolean("weekly_complete", weeklyComplete);
        tag.putInt("awakening_mining", awakeningMining);
        tag.putInt("awakening_combat", awakeningCombat);
        tag.putBoolean("awakening_complete", awakeningComplete);
        tag.putLong("daily_day", dailyDay);
        tag.putInt("daily_type", dailyType);
        tag.putInt("daily_progress", dailyProgress);
        tag.putBoolean("daily_complete", dailyComplete);
        tag.putBoolean("daily_failed", dailyFailed);
        tag.putLong("daily_penalty_until", dailyPenaltyUntil);
        tag.putInt("emergency_survival", emergencySurvival);
        tag.putBoolean("emergency_complete", emergencyComplete);
        tag.putBoolean("emergency_wounded", emergencyWounded);
        tag.putBoolean("evolution_mission_complete", evolutionMissionComplete);
        tag.putBoolean("secret_mission_complete", secretMissionComplete);
        tag.putBoolean("class_mission_complete", classMissionComplete);
        tag.putInt("highest_rank_reward", highestRankReward);
        tag.putString("equipped_title", equippedTitle);
        tag.putBoolean("is_creator", isCreator);
        tag.putBoolean("is_nanda", isNanda);
        tag.putBoolean("is_mistic_potato", isMisticPotato);
        tag.putBoolean("is_schvis", isSchvis);
        tag.putBoolean("is_tomcraft", isTomcraft);
        CompoundTag repetitionTag = new CompoundTag();
        repeatedActions.forEach(repetitionTag::putInt);
        tag.put("repeated_actions", repetitionTag);
        tag.putInt("combo_count", comboCount);
        tag.putInt("combo_ticks", comboTicks);
        tag.putInt("trance_ticks", tranceTicks);
        tag.putString("sponsor_constellation", sponsorConstellation == null ? "" : sponsorConstellation);
        tag.putBoolean("is_constellation", isConstellation);
        tag.putString("constellation_modifier", constellationModifier == null ? "" : constellationModifier);
        tag.putString("active_scenario", activeScenario == null ? "night_hunt" : activeScenario);
        tag.putInt("scenario_progress", scenarioProgress);
        tag.putInt("scenario_target", scenarioTarget);
        tag.putBoolean("scenario_complete", scenarioComplete);
        tag.putLong("phoenix_cooldown_until", phoenixCooldownUntil);
        tag.putLong("iron_will_cooldown_until", ironWillCooldownUntil);
        tag.putString("equipped_skill", equippedSkill == null ? "" : equippedSkill);
        for (int i = 0; i < 5; i++) {
            tag.putString("equipped_active_" + i, getEquippedActiveSkill(i));
        }
        for (int i = 0; i < 3; i++) {
            tag.putString("equipped_passive_" + i, getEquippedPassiveSkill(i));
        }
        tag.putInt("dantian_realm", dantianRealm);
        tag.putInt("cultivation_qi", cultivationQi);
        tag.putInt("selected_dao_skill", selectedDaoSkill);
        tag.putInt("tribulation_shield_ticks", tribulationShieldTicks);
        tag.putInt("dao_skill_cooldown", daoSkillCooldown);
        tag.putInt("skill_points", skillPoints);
        tag.putInt("spent_skill_points", spentSkillPoints);
        int meridianBits = 0;
        for (int i = 0; i < 12; i++) {
            if (meridians[i]) meridianBits |= (1 << i);
        }
        tag.putInt("meridians_mask", meridianBits);
        tag.putLong("stigma_cooldown_until", stigmaCooldownUntil);
        net.minecraft.nbt.ListTag disabledTag = new net.minecraft.nbt.ListTag();
        for (String s : disabledSkills) {
            disabledTag.add(net.minecraft.nbt.StringTag.valueOf(s));
        }
        tag.put("disabled_skills", disabledTag);
        net.minecraft.nbt.ListTag shadowArmyTag = new net.minecraft.nbt.ListTag();
        for (CompoundTag shadow : storedShadowArmy) {
            shadowArmyTag.add(shadow.copy());
        }
        tag.putString("hunter_class", hunterClass == null ? "" : hunterClass);
        tag.putBoolean("dokkaebi_scenario_active", dokkaebiScenarioActive);
        tag.putInt("dokkaebi_scenario_timer", dokkaebiScenarioTimer);
        tag.putInt("dokkaebi_scenario_kills", dokkaebiScenarioKills);
        tag.putInt("dokkaebi_scenario_target", dokkaebiScenarioTargetKills);
        tag.putString("dokkaebi_scenario_title", dokkaebiScenarioTitle == null ? "" : dokkaebiScenarioTitle);
        tag.put("stored_shadow_army", shadowArmyTag);
        tag.putBoolean("conquered_ranker_tower", conqueredRankerTower);
        tag.putInt("saved_tower_floor", savedTowerFloor);
        tag.putBoolean("conquered_nine_tribulations_pagoda", conqueredNineTribulationsPagoda);
        tag.putInt("saved_pagoda_stage", savedPagodaStage);
        tag.putBoolean("conquered_blood_sect", conqueredBloodSect);
        tag.putBoolean("conquered_yggdrasil_labyrinth", conqueredYggdrasilLabyrinth);
        tag.putInt("cultivation_sublevel", cultivationSubLevel);
        tag.putBoolean("system_updated", systemUpdated);
        tag.putBoolean("isekai_unlocked", isekaiUnlocked);
        tag.putBoolean("system_updating", systemUpdating);
        tag.putInt("system_update_remaining_ticks", systemUpdateRemainingTicks);
        tag.putString("murim_physique", murimPhysique == null ? "" : murimPhysique);
        tag.putBoolean("murim_unlocked", murimUnlocked);
        tag.putBoolean("system_30_updated", system30Updated);
        tag.putBoolean("system_30_updating", system30Updating);
        tag.putInt("system_30_update_remaining_ticks", system30UpdateRemainingTicks);
        tag.putBoolean("system_40_updated", system40Updated);
        tag.putBoolean("system_40_updating", system40Updating);
        tag.putInt("system_40_update_remaining_ticks", system40UpdateRemainingTicks);
        tag.putBoolean("teyvat_unlocked", teyvatUnlocked);
        tag.putInt("anemo_statue_lvl", anemoStatueLevel);
        tag.putInt("anemo_oculi_prog", anemoOculiProgress);
        tag.putInt("geo_statue_lvl", geoStatueLevel);
        tag.putInt("geo_oculi_prog", geoOculiProgress);
        tag.putInt("electro_statue_lvl", electroStatueLevel);
        tag.putInt("electro_oculi_prog", electroOculiProgress);
        tag.putInt("dendro_statue_lvl", dendroStatueLevel);
        tag.putInt("dendro_oculi_prog", dendroOculiProgress);
        tag.putInt("statue_energy_bonus", statueEnergyBonus);
        ListTag dimsTag = new ListTag();
        for (String dim : visitedDimensions) {
            dimsTag.add(StringTag.valueOf(dim));
        }
        tag.put("visited_dimensions", dimsTag);
        tag.putInt("tribulation_ticks", tribulationTicks);
        tag.putInt("tribulation_strikes", tribulationStrikesRemaining);
        tag.putInt("tribulation_total", tribulationTotalStrikes);
        tag.putInt("pending_trib_realm", pendingTribulationRealm);
        tag.putInt("physique_skill_cd", physiqueSkillCooldown);
        tag.putInt("gacha_pity_5", gachaPity5Star);
        tag.putInt("gacha_pity_4", gachaPity4Star);
        tag.putBoolean("gacha_guaranteed", gachaGuaranteedRateUp);
        tag.putInt("primogems", primogems);
        tag.putInt("intertwined_fates", intertwinedFates);
        tag.putInt("acquaint_fates", acquaintFates);
        tag.putInt("total_gacha_pulls", totalGachaPulls);
        tag.putInt("stardust", stardust);
        tag.putInt("starglitter", starglitter);
        tag.putInt("beginner_pulls", beginnerPulls);
        tag.putInt("mora", mora);
        tag.putInt("weapon_target", weaponBannerTarget);
        tag.putInt("weapon_fate_pts", weaponBannerFatePoints);
        tag.putInt("char_target", characterBannerTarget);
        tag.putDouble("usd_balance", usdBalance);
        tag.putInt("genesis_crystals", genesisCrystals);
        tag.putInt("stellar_jade", stellarJade);
        tag.putInt("welkin_days", welkinDays);
        tag.putLong("last_welkin_claim_day", lastWelkinClaimDay);
        tag.putBoolean("welkin_enhanced", welkinEnhanced);
        tag.putInt("welkin_quest_step", welkinQuestStep);
        tag.putInt("welkin_quest_progress", welkinQuestProgress);
        tag.putInt("first_topup_mask", firstTopUpMask);
        tag.putInt("first_jade_topup_mask", firstJadeTopUpMask);
        tag.putInt("last_claimed_maintenance_version", lastClaimedMaintenanceVersion);
        tag.putLong("gacha_epoch_tick", gachaEpochTick);
        tag.putLong("casino_vip_until_tick", casinoVipUntilTick);
        net.minecraft.nbt.ListTag wishList = new net.minecraft.nbt.ListTag();
        for (WishLogEntry entry : wishHistory) {
            wishList.add(entry.toNBT());
        }
        tag.put("wish_history", wishList);
        net.minecraft.nbt.ListTag fablesTag = new net.minecraft.nbt.ListTag();
        for (String f : unlockedFables) {
            fablesTag.add(net.minecraft.nbt.StringTag.valueOf(f));
        }
        tag.put("unlocked_fables", fablesTag);
        tag.putInt("fable_weight", fableWeight);
        tag.putBoolean("received_hunter_guide", receivedHunterGuide);
        CompoundTag subspaceTag = new CompoundTag();
        net.minecraft.nbt.ListTag subspaceList = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < subspaceItems.size(); ++i) {
            ItemStack itemstack = subspaceItems.get(i);
            if (!itemstack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                subspaceList.add(itemstack.save(registries, itemTag));
            }
        }
        subspaceTag.put("Items", subspaceList);
        tag.put("subspace_inventory", subspaceTag);
        tag.putInt("subspace_level", subspaceLevel);
        tag.putInt("subspace_built_level", subspaceBuiltLevel);
        tag.putInt("subspace_unlocked_slots", subspaceUnlockedSlots);
        tag.putInt("subspace_chest_count", subspaceChestCount);
        tag.putString("subspace_ret_dim", subspaceReturnDim);
        tag.putDouble("subspace_ret_x", subspaceReturnX);
        tag.putDouble("subspace_ret_y", subspaceReturnY);
        tag.putDouble("subspace_ret_z", subspaceReturnZ);
        tag.putFloat("subspace_ret_yaw", subspaceReturnYaw);
        tag.putFloat("subspace_ret_pitch", subspaceReturnPitch);
        CompoundTag artifactTag = new CompoundTag();
        ContainerHelper.saveAllItems(artifactTag, artifactSlots, registries);
        tag.put("artifact_inventory", artifactTag);

        if (equippedLightCone != null && !equippedLightCone.isEmpty()) {
            tag.put("equipped_light_cone", equippedLightCone.save(registries));
        }

        CompoundTag activeRunesTag = new CompoundTag();
        ContainerHelper.saveAllItems(activeRunesTag, activeRuneSlots, registries);
        tag.put("active_runes", activeRunesTag);

        CompoundTag passiveRunesTag = new CompoundTag();
        ContainerHelper.saveAllItems(passiveRunesTag, passiveRuneSlots, registries);
        tag.put("passive_runes", passiveRunesTag);
        tag.putLong("active_rune_cd_0", activeRuneCooldownUntil[0]);
        tag.putLong("active_rune_cd_1", activeRuneCooldownUntil[1]);

        tag.putString("active_vision", activeVision != null ? activeVision : "NONE");
        tag.putBoolean("has_chosen_initial_vision", hasChosenInitialVision);
        tag.putLong("vision_cooldown_until", visionResonanceCooldownUntil);

        tag.putInt("uma_carats", carats);
        tag.putString("active_uma_id", activeUmaId != null ? activeUmaId : "special_week");
        ListTag umaListTag = new ListTag();
        for (com.soybe.awakeningsystem.uma.model.OwnedUma ou : ownedUmas.values()) {
            umaListTag.add(ou.toNBT());
        }
        tag.put("owned_umas", umaListTag);

        tag.putInt("dart_high_score", dartHighScore);
        tag.putLong("casino_total_won", casinoTotalWon);
        tag.putInt("casino_max_payout", casinoMaxPayout);
        tag.putInt("pool_matches_won", poolMatchesWon);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        level = Math.max(1, tag.getInt("level"));
        experience = tag.getInt("experience");
        attributePoints = tag.getInt("attribute_points");
        coins = Math.max(0, tag.getInt("coins"));
        strength = Math.max(1, tag.getInt("strength"));
        vitality = Math.max(1, tag.getInt("vitality"));
        agility = Math.max(1, tag.getInt("agility"));
        perception = Math.max(1, tag.getInt("perception"));
        intelligence = Math.max(1, tag.getInt("intelligence"));
        willpower = Math.max(1, tag.getInt("willpower"));
        agilitySpeedEnabled = !tag.contains("agilitySpeedEnabled") || tag.getBoolean("agilitySpeedEnabled");
        affinities.clear();
        CompoundTag affinityTag = tag.getCompound("affinities");
        affinityTag.getAllKeys().forEach(key -> affinities.put(key, affinityTag.getInt(key)));
        skills.clear();
        CompoundTag skillTag = tag.getCompound("skills");
        skillTag.getAllKeys().forEach(key -> skills.put(key, skillTag.getInt(key)));
        skillProgress.clear();
        CompoundTag skillProgressTag = tag.getCompound("skill_progress");
        skillProgressTag.getAllKeys().forEach(key -> skillProgress.put(key, skillProgressTag.getInt(key)));
        evolutionCooldowns.clear();
        CompoundTag cooldownTag = tag.getCompound("evolution_cooldowns");
        cooldownTag.getAllKeys().forEach(key -> evolutionCooldowns.put(key, cooldownTag.getLong(key)));
        activeCooldowns.clear();
        CompoundTag activeCooldownTag = tag.getCompound("active_cooldowns");
        activeCooldownTag.getAllKeys().forEach(key -> activeCooldowns.put(key, activeCooldownTag.getLong(key)));
        energy = Math.max(0, Math.min(maxEnergy(), tag.getInt("energy")));
        blocksMined = Math.max(0, tag.getInt("blocks_mined"));
        oresMined = Math.max(0, tag.getInt("ores_mined"));
        creaturesDefeated = Math.max(0, tag.getInt("creatures_defeated"));
        damageDealt = Math.max(0, tag.getInt("damage_dealt"));
        damageTaken = Math.max(0, tag.getInt("damage_taken"));
        itemsCrafted = Math.max(0, tag.getInt("items_crafted"));
        skillsActivated = Math.max(0, tag.getInt("skills_activated"));
        missionsCompleted = Math.max(0, tag.getInt("missions_completed"));
        purchases = Math.max(0, tag.getInt("purchases"));
        coinsSpent = Math.max(0, tag.getInt("coins_spent"));
        bossKills = Math.max(0, tag.getInt("boss_kills"));
        distanceMoved = Math.max(0, tag.getInt("distance_moved"));
        deaths = Math.max(0, tag.getInt("deaths"));
        lastDailyLoginDay = tag.contains("last_daily_login_day") ? tag.getLong("last_daily_login_day") : -1L;
        prestige = Math.max(0, tag.getInt("prestige"));
        achievements.clear();
        CompoundTag achievementTag = tag.getCompound("achievements");
        achievementTag.getAllKeys().forEach(key -> achievements.put(key, achievementTag.getBoolean(key)));
        weeklyWeek = tag.getLong("weekly_week");
        weeklyType = Math.max(0, Math.min(2, tag.getInt("weekly_type")));
        weeklyProgress = Math.max(0, tag.getInt("weekly_progress"));
        weeklyComplete = tag.getBoolean("weekly_complete");
        awakeningMining = tag.getInt("awakening_mining");
        awakeningCombat = tag.getInt("awakening_combat");
        awakeningComplete = tag.getBoolean("awakening_complete");
        dailyDay = tag.getLong("daily_day");
        dailyType = Math.max(0, Math.min(4, tag.getInt("daily_type")));
        dailyProgress = Math.max(0, tag.getInt("daily_progress"));
        dailyComplete = tag.getBoolean("daily_complete");
        dailyFailed = tag.getBoolean("daily_failed");
        dailyPenaltyUntil = Math.max(0L, tag.getLong("daily_penalty_until"));
        emergencySurvival = tag.getInt("emergency_survival");
        emergencyComplete = tag.getBoolean("emergency_complete");
        emergencyWounded = tag.getBoolean("emergency_wounded");
        evolutionMissionComplete = tag.getBoolean("evolution_mission_complete");
        secretMissionComplete = tag.getBoolean("secret_mission_complete");
        classMissionComplete = tag.getBoolean("class_mission_complete");
        highestRankReward = tag.getInt("highest_rank_reward");
        equippedTitle = tag.contains("equipped_title") ? tag.getString("equipped_title") : "";
        isCreator = tag.getBoolean("is_creator");
        isNanda = tag.getBoolean("is_nanda");
        isMisticPotato = tag.getBoolean("is_mistic_potato");
        isSchvis = tag.getBoolean("is_schvis");
        isTomcraft = tag.getBoolean("is_tomcraft");
        CompoundTag repetitionTag = tag.getCompound("repeated_actions");
        repetitionTag.getAllKeys().forEach(key -> repeatedActions.put(key, repetitionTag.getInt(key)));
        comboCount = tag.getInt("combo_count");
        comboTicks = tag.getInt("combo_ticks");
        tranceTicks = tag.getInt("trance_ticks");
        sponsorConstellation = tag.getString("sponsor_constellation");
        isConstellation = tag.getBoolean("is_constellation");
        constellationModifier = tag.contains("constellation_modifier") ? tag.getString("constellation_modifier") : "";
        activeScenario = tag.contains("active_scenario") ? tag.getString("active_scenario") : "night_hunt";
        scenarioProgress = tag.getInt("scenario_progress");
        scenarioTarget = tag.contains("scenario_target") ? Math.max(1, tag.getInt("scenario_target")) : 10;
        scenarioComplete = tag.getBoolean("scenario_complete");
        phoenixCooldownUntil = tag.getLong("phoenix_cooldown_until");
        ironWillCooldownUntil = tag.getLong("iron_will_cooldown_until");
        equippedSkill = tag.getString("equipped_skill");
        for (int i = 0; i < 5; i++) {
            if (tag.contains("equipped_active_" + i)) {
                String s = tag.getString("equipped_active_" + i);
                if (i < equippedActiveSkills.size()) equippedActiveSkills.set(i, s);
                else equippedActiveSkills.add(s);
            }
        }
        for (int i = 0; i < 3; i++) {
            if (tag.contains("equipped_passive_" + i)) {
                String s = tag.getString("equipped_passive_" + i);
                if (i < equippedPassiveSkills.size()) equippedPassiveSkills.set(i, s);
                else equippedPassiveSkills.add(s);
            }
        }
        dantianRealm = tag.getInt("dantian_realm");
        cultivationQi = tag.getInt("cultivation_qi");
        selectedDaoSkill = tag.getInt("selected_dao_skill");
        tribulationShieldTicks = tag.getInt("tribulation_shield_ticks");
        daoSkillCooldown = tag.getInt("dao_skill_cooldown");
        skillPoints = tag.getInt("skill_points");
        spentSkillPoints = tag.getInt("spent_skill_points");
        int meridianBits = tag.getInt("meridians_mask");
        for (int i = 0; i < 12; i++) {
            meridians[i] = (meridianBits & (1 << i)) != 0;
        }
        stigmaCooldownUntil = tag.getLong("stigma_cooldown_until");
        disabledSkills.clear();
        if (tag.contains("disabled_skills", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag disabledTag = tag.getList("disabled_skills", net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < disabledTag.size(); i++) {
                disabledSkills.add(disabledTag.getString(i));
            }
        }
        storedShadowArmy.clear();
        if (tag.contains("stored_shadow_army", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag shadowArmyTag = tag.getList("stored_shadow_army", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < shadowArmyTag.size(); i++) {
                net.minecraft.nbt.CompoundTag c = shadowArmyTag.getCompound(i);
                String mobType = c.getString("MobEntityTypeId").toLowerCase(java.util.Locale.ROOT);
                if (!mobType.contains("celestial_dragon")) {
                    storedShadowArmy.add(c);
                }
            }
        }
        hunterClass = tag.getString("hunter_class");
        dokkaebiScenarioActive = tag.getBoolean("dokkaebi_scenario_active");
        dokkaebiScenarioTimer = tag.getInt("dokkaebi_scenario_timer");
        dokkaebiScenarioKills = tag.getInt("dokkaebi_scenario_kills");
        dokkaebiScenarioTargetKills = tag.contains("dokkaebi_scenario_target") ? tag.getInt("dokkaebi_scenario_target") : 10;
        dokkaebiScenarioTitle = tag.getString("dokkaebi_scenario_title");
        conqueredRankerTower = tag.getBoolean("conquered_ranker_tower");
        savedTowerFloor = tag.contains("saved_tower_floor") ? Math.max(1, Math.min(100, tag.getInt("saved_tower_floor"))) : 1;
        conqueredNineTribulationsPagoda = tag.getBoolean("conquered_nine_tribulations_pagoda");
        savedPagodaStage = tag.contains("saved_pagoda_stage") ? Math.max(1, Math.min(9, tag.getInt("saved_pagoda_stage"))) : 1;
        conqueredBloodSect = tag.getBoolean("conquered_blood_sect");
        conqueredYggdrasilLabyrinth = tag.getBoolean("conquered_yggdrasil_labyrinth");
        cultivationSubLevel = tag.contains("cultivation_sublevel") ? Math.max(1, Math.min(9, tag.getInt("cultivation_sublevel"))) : 1;
        systemUpdated = tag.getBoolean("system_updated");
        isekaiUnlocked = tag.getBoolean("isekai_unlocked");
        systemUpdating = tag.getBoolean("system_updating");
        systemUpdateRemainingTicks = tag.getInt("system_update_remaining_ticks");
        murimPhysique = tag.contains("murim_physique") ? tag.getString("murim_physique") : "";
        murimUnlocked = tag.getBoolean("murim_unlocked");
        system30Updated = tag.getBoolean("system_30_updated");
        system30Updating = tag.getBoolean("system_30_updating");
        system30UpdateRemainingTicks = tag.getInt("system_30_update_remaining_ticks");
        system40Updated = tag.getBoolean("system_40_updated");
        system40Updating = tag.getBoolean("system_40_updating");
        system40UpdateRemainingTicks = tag.getInt("system_40_update_remaining_ticks");
        teyvatUnlocked = tag.getBoolean("teyvat_unlocked");
        anemoStatueLevel = tag.contains("anemo_statue_lvl") ? Math.max(1, tag.getInt("anemo_statue_lvl")) : 1;
        anemoOculiProgress = tag.getInt("anemo_oculi_prog");
        geoStatueLevel = tag.contains("geo_statue_lvl") ? Math.max(1, tag.getInt("geo_statue_lvl")) : 1;
        geoOculiProgress = tag.getInt("geo_oculi_prog");
        electroStatueLevel = tag.contains("electro_statue_lvl") ? Math.max(1, tag.getInt("electro_statue_lvl")) : 1;
        electroOculiProgress = tag.getInt("electro_oculi_prog");
        dendroStatueLevel = tag.contains("dendro_statue_lvl") ? Math.max(1, tag.getInt("dendro_statue_lvl")) : 1;
        dendroOculiProgress = tag.getInt("dendro_oculi_prog");
        statueEnergyBonus = tag.getInt("statue_energy_bonus");
        visitedDimensions.clear();
        if (tag.contains("visited_dimensions", Tag.TAG_LIST)) {
            ListTag list = tag.getList("visited_dimensions", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                visitedDimensions.add(list.getString(i));
            }
        }
        tribulationTicks = tag.getInt("tribulation_ticks");
        tribulationStrikesRemaining = tag.getInt("tribulation_strikes");
        tribulationTotalStrikes = tag.getInt("tribulation_total");
        pendingTribulationRealm = tag.getInt("pending_trib_realm");
        physiqueSkillCooldown = tag.getInt("physique_skill_cd");
        gachaPity5Star = tag.getInt("gacha_pity_5");
        gachaPity4Star = tag.getInt("gacha_pity_4");
        gachaGuaranteedRateUp = tag.getBoolean("gacha_guaranteed");
        primogems = tag.getInt("primogems");
        intertwinedFates = tag.getInt("intertwined_fates");
        acquaintFates = tag.getInt("acquaint_fates");
        totalGachaPulls = tag.getInt("total_gacha_pulls");
        stardust = tag.getInt("stardust");
        starglitter = tag.getInt("starglitter");
        beginnerPulls = tag.getInt("beginner_pulls");
        mora = tag.getInt("mora");
        weaponBannerTarget = tag.getInt("weapon_target");
        weaponBannerFatePoints = tag.getInt("weapon_fate_pts");
        characterBannerTarget = tag.getInt("char_target");
        usdBalance = tag.contains("usd_balance") ? tag.getDouble("usd_balance") : 0.0;
        genesisCrystals = tag.contains("genesis_crystals") ? tag.getInt("genesis_crystals") : 0;
        stellarJade = tag.contains("stellar_jade") ? tag.getInt("stellar_jade") : 0;
        welkinDays = tag.contains("welkin_days") ? tag.getInt("welkin_days") : 0;
        lastWelkinClaimDay = tag.contains("last_welkin_claim_day") ? tag.getLong("last_welkin_claim_day") : -1;
        welkinEnhanced = tag.contains("welkin_enhanced") && tag.getBoolean("welkin_enhanced");
        welkinQuestStep = tag.contains("welkin_quest_step") ? tag.getInt("welkin_quest_step") : 0;
        welkinQuestProgress = tag.contains("welkin_quest_progress") ? tag.getInt("welkin_quest_progress") : 0;
        firstTopUpMask = tag.contains("first_topup_mask") ? tag.getInt("first_topup_mask") : 0;
        firstJadeTopUpMask = tag.contains("first_jade_topup_mask") ? tag.getInt("first_jade_topup_mask") : 0;
        lastClaimedMaintenanceVersion = tag.contains("last_claimed_maintenance_version") ? tag.getInt("last_claimed_maintenance_version") : 0;
        gachaEpochTick = tag.contains("gacha_epoch_tick") ? tag.getLong("gacha_epoch_tick") : 0L;
        casinoVipUntilTick = tag.contains("casino_vip_until_tick") ? tag.getLong("casino_vip_until_tick") : 0L;
        wishHistory.clear();
        if (tag.contains("wish_history", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag wishList = tag.getList("wish_history", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < wishList.size(); i++) {
                wishHistory.add(WishLogEntry.fromNBT(wishList.getCompound(i)));
            }
        }
        unlockedFables.clear();
        if (tag.contains("unlocked_fables", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag fablesTag = tag.getList("unlocked_fables", net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < fablesTag.size(); i++) {
                unlockedFables.add(fablesTag.getString(i));
            }
        }
        fableWeight = tag.getInt("fable_weight");
        java.util.Collections.fill(subspaceItems, ItemStack.EMPTY);
        if (tag.contains("subspace_inventory", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag subInvTag = tag.getCompound("subspace_inventory");
            if (subInvTag.contains("Items", net.minecraft.nbt.Tag.TAG_LIST)) {
                net.minecraft.nbt.ListTag listtag = subInvTag.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < listtag.size(); ++i) {
                    CompoundTag itemTag = listtag.getCompound(i);
                    int slot = itemTag.getInt("Slot");
                    while (subspaceItems.size() <= slot) {
                        subspaceItems.add(ItemStack.EMPTY);
                    }
                    subspaceItems.set(slot, ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
                }
            } else {
                ContainerHelper.loadAllItems(subInvTag, subspaceItems, registries);
            }
        }
        subspaceLevel = tag.contains("subspace_level") ? Math.max(1, Math.min(5, tag.getInt("subspace_level"))) : 1;
        subspaceBuiltLevel = tag.contains("subspace_built_level") ? tag.getInt("subspace_built_level") : 0;
        subspaceUnlockedSlots = tag.contains("subspace_unlocked_slots") ? tag.getInt("subspace_unlocked_slots") : 54;
        subspaceChestCount = tag.contains("subspace_chest_count") ? tag.getInt("subspace_chest_count") : 2;
        ensureSubspaceCapacity(Math.max(54, subspaceUnlockedSlots));
        if (tag.contains("subspace_ret_dim")) subspaceReturnDim = tag.getString("subspace_ret_dim");
        if (tag.contains("subspace_ret_x")) subspaceReturnX = tag.getDouble("subspace_ret_x");
        if (tag.contains("subspace_ret_y")) subspaceReturnY = tag.getDouble("subspace_ret_y");
        if (tag.contains("subspace_ret_z")) subspaceReturnZ = tag.getDouble("subspace_ret_z");
        if (tag.contains("subspace_ret_yaw")) subspaceReturnYaw = tag.getFloat("subspace_ret_yaw");
        if (tag.contains("subspace_ret_pitch")) subspaceReturnPitch = tag.getFloat("subspace_ret_pitch");
        java.util.Collections.fill(artifactSlots, ItemStack.EMPTY);
        if (tag.contains("artifact_inventory", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            ContainerHelper.loadAllItems(tag.getCompound("artifact_inventory"), artifactSlots, registries);
        }

        if (tag.contains("equipped_light_cone", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            this.equippedLightCone = ItemStack.parse(registries, tag.getCompound("equipped_light_cone")).orElse(ItemStack.EMPTY);
        } else {
            this.equippedLightCone = ItemStack.EMPTY;
        }

        java.util.Collections.fill(activeRuneSlots, ItemStack.EMPTY);
        if (tag.contains("active_runes", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            ContainerHelper.loadAllItems(tag.getCompound("active_runes"), activeRuneSlots, registries);
        }

        java.util.Collections.fill(passiveRuneSlots, ItemStack.EMPTY);
        if (tag.contains("passive_runes", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            ContainerHelper.loadAllItems(tag.getCompound("passive_runes"), passiveRuneSlots, registries);
        }
        activeRuneCooldownUntil[0] = tag.getLong("active_rune_cd_0");
        activeRuneCooldownUntil[1] = tag.getLong("active_rune_cd_1");

        if (tag.contains("received_hunter_guide")) {
            this.receivedHunterGuide = tag.getBoolean("received_hunter_guide");
        }
        if (tag.contains("active_vision")) {
            this.activeVision = tag.getString("active_vision");
        } else {
            this.activeVision = "NONE";
        }
        this.hasChosenInitialVision = tag.getBoolean("has_chosen_initial_vision");
        this.visionResonanceCooldownUntil = tag.getLong("vision_cooldown_until");

        this.carats = tag.contains("uma_carats") ? tag.getInt("uma_carats") : 1500;
        this.activeUmaId = tag.contains("active_uma_id") ? tag.getString("active_uma_id") : "special_week";
        this.ownedUmas.clear();
        if (tag.contains("owned_umas", net.minecraft.nbt.Tag.TAG_LIST)) {
            ListTag umaList = tag.getList("owned_umas", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < umaList.size(); i++) {
                com.soybe.awakeningsystem.uma.model.OwnedUma ou = new com.soybe.awakeningsystem.uma.model.OwnedUma(umaList.getCompound(i));
                this.ownedUmas.put(ou.getUmaId(), ou);
            }
        }
        if (this.ownedUmas.isEmpty()) {
            com.soybe.awakeningsystem.uma.model.OwnedUma starter = new com.soybe.awakeningsystem.uma.model.OwnedUma("special_week", 3);
            this.ownedUmas.put("special_week", starter);
            this.activeUmaId = "special_week";
        }

        this.dartHighScore = tag.contains("dart_high_score") ? tag.getInt("dart_high_score") : 0;
        this.casinoTotalWon = tag.contains("casino_total_won") ? tag.getLong("casino_total_won") : 0L;
        this.casinoMaxPayout = tag.contains("casino_max_payout") ? tag.getInt("casino_max_payout") : 0;
        this.poolMatchesWon = tag.contains("pool_matches_won") ? tag.getInt("pool_matches_won") : 0;
    }

    public int getCarats() { return carats; }
    public void setCarats(int amount) { this.carats = Math.max(0, amount); }
    public void addCarats(int amount) { if (amount > 0) this.carats += amount; }
    public boolean spendCarats(int amount) {
        if (amount > 0 && this.carats >= amount) {
            this.carats -= amount;
            return true;
        }
        return false;
    }
    public Map<String, com.soybe.awakeningsystem.uma.model.OwnedUma> getOwnedUmas() { return ownedUmas; }
    public com.soybe.awakeningsystem.uma.model.OwnedUma getOwnedUma(String id) { return ownedUmas.get(id); }
    public String getActiveUmaId() { return activeUmaId; }
    public void setActiveUmaId(String id) {
        if (ownedUmas.containsKey(id)) {
            this.activeUmaId = id;
        }
    }
    public com.soybe.awakeningsystem.uma.model.OwnedUma getActiveUma() {
        return ownedUmas.getOrDefault(activeUmaId, ownedUmas.values().stream().findFirst().orElse(null));
    }

    /**
     * Añade una nueva Uma al establo o le otorga shards si ya la posee (dupe).
     * Retorna true si es nueva, false si fue duplicado convertido en shards.
     */
    public boolean addOrUpgradeUma(String umaId, int baseRarity) {
        if (ownedUmas.containsKey(umaId)) {
            int shardBonus = switch (baseRarity) {
                case 3 -> 20;
                case 2 -> 10;
                default -> 5;
            };
            ownedUmas.get(umaId).addShards(shardBonus);
            return false;
        } else {
            ownedUmas.put(umaId, new com.soybe.awakeningsystem.uma.model.OwnedUma(umaId, baseRarity));
            return true;
        }
    }

    public boolean trainUmaStat(String umaId, String statName, int costCarats, int costCoins) {
        com.soybe.awakeningsystem.uma.model.OwnedUma uma = ownedUmas.get(umaId);
        if (uma == null) return false;
        if (this.carats < costCarats || this.coins < costCoins) return false;

        boolean trained = uma.trainStat(statName, 15 + level / 10);
        if (trained) {
            this.spendCarats(costCarats);
            this.spendCoins(costCoins);
            return true;
        }
        return false;
    }

    public boolean bloomUma(String umaId) {
        com.soybe.awakeningsystem.uma.model.OwnedUma uma = ownedUmas.get(umaId);
        if (uma == null) return false;
        return uma.bloom();
    }
}
