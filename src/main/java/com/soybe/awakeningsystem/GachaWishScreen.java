package com.soybe.awakeningsystem;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import com.soybe.awakeningsystem.entity.CompanionProfile;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.item.CompanionContractItem;
import com.soybe.awakeningsystem.item.HsrLightConeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class GachaWishScreen extends Screen {
    private static final int STATE_BANNER = 0;
    private static final int STATE_METEOR = 1;
    private static final int STATE_REVEAL_SINGLE = 2;
    private static final int STATE_SUMMARY = 3;

    private int screenState = STATE_BANNER;

    private int primogems;
    private int intertwinedFates;
    private int acquaintFates;
    private int stardust;
    private int starglitter;
    private int pity5Star;
    private int pity4Star;
    private boolean guaranteed;
    private int beginnerPulls;
    private int mora = 0;
    private int coins = 0;
    private double usdBalance = 0.0;
    private int genesisCrystals = 0;
    private int stellarJade = 0;
    private int welkinDays = 0;
    private int totalWishes = 0;
    private boolean showTopUpModal = false;
    private boolean showNewsModal = false;
    private int topUpTab = 0;
    private int weaponBannerTarget = 0;
    private int weaponBannerFatePoints = 0;
    private int characterBannerTarget = 0;
    private List<String> wishHistory = new ArrayList<>();

    public static final int BANNER_UMA = 11;
    public static final int BANNER_STELLAR_WARP = 12;
    public static final int BANNER_PROMO_2 = 13;
    private int activeBanner = 1;
    private int carats = 1500;
    private PromotionalBanner.Franchise activeFranchise = PromotionalBanner.Franchise.GENSHIN;

    public com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.BannerPhase getSelectedUmaPhase() {
        long dayTime = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getDayTime() : 0L;
        return com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.getActivePhase(dayTime);
    }

    public void setActiveBanner(int banner) {
        this.activeBanner = banner;
        this.showTopUpModal = false;
        this.showNewsModal = false;
    }

    public boolean isPromotionalCharacterBanner() {
        return activeBanner == 1 || activeBanner == BANNER_PROMO_2 || (activeBanner >= 4 && activeBanner <= 10);
    }

    public boolean isPromotional() {
        return activeBanner == 0 || isPromotionalCharacterBanner();
    }

    public static int getBannerForFranchise(PromotionalBanner.Franchise franchise) {
        return switch (franchise) {
            case HONKAI_ZZZ -> 4;
            case SOLO_LEVELING -> 5;
            case JUJUTSU_KAISEN -> 6;
            case FATE -> 7;
            case DEMON_SLAYER -> 8;
            case WUTHERING_WAVES -> 9;
            case NTE -> 10;
            default -> 1;
        };
    }

    public static PromotionalBanner.Franchise getFranchiseForBanner(int banner) {
        return switch (banner) {
            case 4 -> PromotionalBanner.Franchise.HONKAI_ZZZ;
            case 5 -> PromotionalBanner.Franchise.SOLO_LEVELING;
            case 6 -> PromotionalBanner.Franchise.JUJUTSU_KAISEN;
            case 7 -> PromotionalBanner.Franchise.FATE;
            case 8 -> PromotionalBanner.Franchise.DEMON_SLAYER;
            case 9 -> PromotionalBanner.Franchise.WUTHERING_WAVES;
            case 10 -> PromotionalBanner.Franchise.NTE;
            default -> PromotionalBanner.Franchise.GENSHIN;
        };
    }

    private int lastPullCount = 1;
    private static final long METEOR_DURATION_MS = 6060L;
    private long meteorStartTime = 0L;
    private long revealStartTime = 0L;
    private int highestWonRarity = 3;
    private List<ItemStack> resultItems = new ArrayList<>();
    private List<Integer> itemRarities = new ArrayList<>();
    private int currentRevealIndex = 0;

    private boolean showHistoryModal = false;
    private int historyPage = 0;
    private boolean showShopModal = false;
    private boolean showPathModal = false;
    private int firstTopUpMask = 0;
    private int firstJadeTopUpMask = 0;

    private SoundInstance ambientSoundInstance = null;
    private SoundInstance currentMeteorSoundInstance = null;
    private SoundInstance currentVoiceSoundInstance = null;

    private final List<ItemStack> weaponBannerPreviews = new ArrayList<>();
    private final List<ItemStack> shonenBannerPreviews = new ArrayList<>();
    private final List<ItemStack> standardBannerPreviews = new ArrayList<>();
    private final List<ItemStack> noviceBannerPreviews = new ArrayList<>();
    private final Map<CompanionProfile, CompanionEntity> dummyCompanions = new EnumMap<>(CompanionProfile.class);

    private CompanionEntity getOrCreateDummyCompanion(CompanionProfile profile) {
        if (this.minecraft == null || this.minecraft.level == null) return null;
        return dummyCompanions.computeIfAbsent(profile, p -> {
            CompanionEntity ent = new CompanionEntity(ModEntities.COMPANION.get(), this.minecraft.level);
            ent.setCompanionProfile(p);
            ent.setSilent(true);
            ent.setNoAi(true);
            return ent;
        });
    }

    private final Map<com.soybe.awakeningsystem.uma.UmaProfile, com.soybe.awakeningsystem.uma.UmaRacerEntity> dummyUmas = new EnumMap<>(com.soybe.awakeningsystem.uma.UmaProfile.class);

    private com.soybe.awakeningsystem.uma.UmaRacerEntity getOrCreateDummyUma(com.soybe.awakeningsystem.uma.UmaProfile profile) {
        if (this.minecraft == null || this.minecraft.level == null) return null;
        return dummyUmas.computeIfAbsent(profile, p -> {
            com.soybe.awakeningsystem.uma.UmaRacerEntity racer = new com.soybe.awakeningsystem.uma.UmaRacerEntity(
                    ModEntities.UMA_RACER.get(), this.minecraft.level);
            racer.setUmaProfile(p);
            racer.setSilent(true);
            racer.setNoAi(true);
            return racer;
        });
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, int coins, double usdBalance, int genesisCrystals, int stellarJade, int welkinDays, int totalWishes, List<String> wishHistory) {
        super(Component.translatable("gui.awakening_system.gacha.title"));
        this.primogems = primogems;
        this.intertwinedFates = intertwinedFates;
        this.acquaintFates = acquaintFates;
        this.stardust = stardust;
        this.starglitter = starglitter;
        this.pity5Star = pity5Star;
        this.pity4Star = pity4Star;
        this.guaranteed = guaranteed;
        this.beginnerPulls = beginnerPulls;
        this.mora = mora;
        this.weaponBannerTarget = weaponBannerTarget;
        this.weaponBannerFatePoints = weaponBannerFatePoints;
        this.characterBannerTarget = characterBannerTarget;
        this.coins = coins;
        this.usdBalance = usdBalance;
        this.genesisCrystals = genesisCrystals;
        this.stellarJade = stellarJade;
        this.welkinDays = welkinDays;
        this.totalWishes = totalWishes;
        if (wishHistory != null) {
            this.wishHistory = new ArrayList<>(wishHistory);
        }

        if (Minecraft.getInstance().player != null) {
            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (d != null) {
                if (this.coins == 0 && d.coins() > 0) this.coins = d.coins();
                if (this.usdBalance == 0.0 && d.usdBalance() > 0.0) this.usdBalance = d.usdBalance();
                if (this.genesisCrystals == 0 && d.genesisCrystals() > 0) this.genesisCrystals = d.genesisCrystals();
                if (this.stellarJade == 0 && d.stellarJade() > 0) this.stellarJade = d.stellarJade();
                if (this.welkinDays == 0 && d.welkinDays() > 0) this.welkinDays = d.welkinDays();
                if (this.mora == 0 && d.mora() > 0) this.mora = d.mora();
                this.carats = d.getCarats();
                if (this.totalWishes == 0 && d.totalGachaPulls() > 0) this.totalWishes = d.totalGachaPulls();
            }
        }

        if (this.beginnerPulls < 20) {
            this.activeBanner = 3;
        } else {
            this.activeBanner = 1;
        }

        initBannerPreviews();
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, int coins, double usdBalance, int genesisCrystals, int stellarJade, int welkinDays, List<String> wishHistory) {
        this(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, mora, weaponBannerTarget, weaponBannerFatePoints, characterBannerTarget, coins, usdBalance, genesisCrystals, stellarJade, welkinDays, 0, wishHistory);
    }

    public void syncDataFromPlayer() {
        if (Minecraft.getInstance().player != null) {
            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (d != null) {
                this.coins = d.coins();
                this.usdBalance = d.usdBalance();
                this.genesisCrystals = d.genesisCrystals();
                this.stellarJade = d.stellarJade();
                this.welkinDays = d.welkinDays();
                this.mora = d.mora();
                this.carats = d.getCarats();
                this.primogems = d.primogems();
                this.intertwinedFates = d.intertwinedFates();
                this.acquaintFates = d.acquaintFates();
                this.stardust = d.stardust();
                this.starglitter = d.starglitter();
                this.pity5Star = d.gachaPity5Star();
                this.pity4Star = d.gachaPity4Star();
                this.guaranteed = d.gachaGuaranteedRateUp();
                this.beginnerPulls = d.beginnerPulls();
                this.firstTopUpMask = d.firstTopUpMask();
                this.firstJadeTopUpMask = d.firstJadeTopUpMask();
                this.totalWishes = d.totalGachaPulls();
            }
        }
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, List<String> wishHistory) {
        this(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, mora, weaponBannerTarget, weaponBannerFatePoints, characterBannerTarget, 0, 0.0, 0, 0, 0, wishHistory);
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, List<String> wishHistory) {
        this(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, 0, 0, 0, 0, 0, 0.0, 0, 0, 0, wishHistory);
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int pity5Star, int pity4Star, boolean guaranteed) {
        this(primogems, intertwinedFates, acquaintFates, 0, 0, pity5Star, pity4Star, guaranteed, 0, 0, 0, 0, 0, 0, 0.0, 0, 0, 0, new ArrayList<>());
    }

    private void initBannerPreviews() {
        if (SystemItems.STAFF_OF_HOMA.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.STAFF_OF_HOMA.get()));
        if (SystemItems.SKYWARD_BLADE.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.SKYWARD_BLADE.get()));
        if (SystemItems.MISTSPLITTER_REFORGED.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()));
        if (SystemItems.ENGULFING_LIGHTNING.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()));
        if (SystemItems.WOLFS_GRAVESTONE.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()));
        if (SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()));
        if (SystemItems.VERDICT.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.VERDICT.get()));
        if (SystemItems.HERTA_HAMMER.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.HERTA_HAMMER.get()));
        if (SystemItems.QINGMING_SWORD.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.QINGMING_SWORD.get()));
        if (SystemItems.TAILLESS.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.TAILLESS.get()));
        if (SystemItems.SHUNGUANG_SWORD.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.SHUNGUANG_SWORD.get()));
        if (SystemItems.DOMAIN_OF_SANCTION.isBound()) weaponBannerPreviews.add(new ItemStack(SystemItems.DOMAIN_OF_SANCTION.get()));

        noviceBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.NOELLE));
        noviceBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.CHA_HAEIN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.RAIDEN_SHOGUN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.HU_TAO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ZHONGLI));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.FURINA));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.NAHIDA));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.KAZUHA));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ACHERON));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.KAFKA));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ELLEN_JOE));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.SUNG_JINWOO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.IGRIS));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.BERU));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.GOJO_SATORU));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.SUKUNA));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.YUJI_ITADORI));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ARTORIA_PENDRAGON));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.GILGAMESH));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.RENGOKU));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.TANJIRO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.NEZUKO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.XIAO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ARLECCHINO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.FIREFLY));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.MEGUMI));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ARCHER));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.MAVUIKA));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.COLUMBINA));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.CLORINDE));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.KINICH));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.XILONEN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.SKIRK));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.GANYU));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.DAN_HENG_IL));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.JING_YUAN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.FEIXIAO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ROBIN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.AVENTURINE));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.JANE_DOE));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ZHU_YUAN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.HOSHIMI_MIYABI));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.CAESAR_KING));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.JINSHI));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.CHANGLI));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.JIYAN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.YINLIN));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.MARCH_7TH));
        if (SystemItems.LIGHT_CONE_MILKY_WAY.isBound()) shonenBannerPreviews.add(new ItemStack(SystemItems.LIGHT_CONE_MILKY_WAY.get()));
        if (SystemItems.COMMAND_SPELL.isBound()) shonenBannerPreviews.add(new ItemStack(SystemItems.COMMAND_SPELL.get()));

        if (SystemItems.ASRAHAN_HOLY_SWORD.isBound()) standardBannerPreviews.add(new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()));
        if (SystemItems.BLACK_DRAGON_SCYTHE.isBound()) standardBannerPreviews.add(new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get()));
        if (SystemItems.DAWNS_BALLAD.isBound()) standardBannerPreviews.add(new ItemStack(SystemItems.DAWNS_BALLAD.get()));
        if (SystemItems.THE_CATCH.isBound()) standardBannerPreviews.add(new ItemStack(SystemItems.THE_CATCH.get()));
        if (SystemItems.SHIYUAN_SWORD.isBound()) standardBannerPreviews.add(new ItemStack(SystemItems.SHIYUAN_SWORD.get()));
    }

    @Override
    protected void init() {
        super.init();
        syncDataFromPlayer();
        startAmbientSound();
    }

    private void startAmbientSound() {
        if (ambientSoundInstance == null && ModSounds.WISH_AMBIENT != null && ModSounds.WISH_AMBIENT.isBound()) {
            ambientSoundInstance = new SimpleSoundInstance(
                    ModSounds.WISH_AMBIENT.get().getLocation(),
                    SoundSource.PLAYERS,
                    0.85F,
                    1.0F,
                    RandomSource.create(),
                    true,
                    0,
                    SoundInstance.Attenuation.NONE,
                    0.0, 0.0, 0.0,
                    true
            );
            Minecraft.getInstance().getSoundManager().play(ambientSoundInstance);
        }
    }

    private void stopAmbientSound() {
        if (ambientSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(ambientSoundInstance);
            ambientSoundInstance = null;
        }
    }

    private void stopCurrentVoiceSound() {
        Minecraft.getInstance().getSoundManager().stop(null, SoundSource.VOICE);
        if (currentVoiceSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(currentVoiceSoundInstance);
            currentVoiceSoundInstance = null;
        }
    }

    private void playVoiceSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        stopCurrentVoiceSound();
        if (sound == null) return;
        currentVoiceSoundInstance = new SimpleSoundInstance(
                sound.getLocation(),
                SoundSource.VOICE,
                volume,
                pitch,
                RandomSource.create(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
        );
        Minecraft.getInstance().getSoundManager().play(currentVoiceSoundInstance);
    }

    @Override
    public void onClose() {
        stopAmbientSound();
        stopMeteorSound();
        stopCurrentVoiceSound();
        super.onClose();
    }

    public void updateData(int primogems, int intertwinedFates, int acquaintFates, int pity5Star, boolean guaranteed) {
        this.primogems = primogems;
        this.intertwinedFates = intertwinedFates;
        this.acquaintFates = acquaintFates;
        this.pity5Star = pity5Star;
        this.guaranteed = guaranteed;
    }

    public void updateAllData(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, int coins, double usdBalance, int genesisCrystals, int stellarJade, int welkinDays, int totalWishes, List<String> wishHistory) {
        this.primogems = primogems;
        this.intertwinedFates = intertwinedFates;
        this.acquaintFates = acquaintFates;
        this.stardust = stardust;
        this.starglitter = starglitter;
        this.pity5Star = pity5Star;
        this.pity4Star = pity4Star;
        this.guaranteed = guaranteed;
        this.beginnerPulls = beginnerPulls;
        this.mora = mora;
        this.weaponBannerTarget = weaponBannerTarget;
        this.weaponBannerFatePoints = weaponBannerFatePoints;
        this.characterBannerTarget = characterBannerTarget;
        this.coins = coins;
        this.usdBalance = usdBalance;
        this.genesisCrystals = genesisCrystals;
        this.stellarJade = stellarJade;
        this.welkinDays = welkinDays;
        this.totalWishes = totalWishes;
        if (wishHistory != null) {
            this.wishHistory = new ArrayList<>(wishHistory);
        }
    }

    public void updateAllData(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, int coins, double usdBalance, int genesisCrystals, int stellarJade, int welkinDays, List<String> wishHistory) {
        updateAllData(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, mora, weaponBannerTarget, weaponBannerFatePoints, characterBannerTarget, coins, usdBalance, genesisCrystals, stellarJade, welkinDays, this.totalWishes, wishHistory);
    }

    public void updateAllData(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, List<String> wishHistory) {
        updateAllData(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, mora, weaponBannerTarget, weaponBannerFatePoints, characterBannerTarget, this.coins, this.usdBalance, this.genesisCrystals, this.stellarJade, this.welkinDays, this.totalWishes, wishHistory);
    }

    public void updateAllData(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, List<String> wishHistory) {
        updateAllData(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, this.mora, this.weaponBannerTarget, this.weaponBannerFatePoints, this.characterBannerTarget, wishHistory);
    }

    public void showResults(int highestRarity, List<ItemStack> items, List<Integer> rarities, int newPity5, boolean newGuaranteed, int newPrimos, int newIntertwined, int newAcquaint, int newStardust, int newStarglitter, int newBeginnerPulls, int newMora, int newWeaponTarget, int newWeaponFatePoints, int newCharTarget, int newTotalWishes) {
        this.highestWonRarity = highestRarity;
        this.resultItems = new ArrayList<>(items);
        this.itemRarities = new ArrayList<>(rarities);
        this.lastPullCount = Math.max(1, items.size());
        this.pity5Star = newPity5;
        this.guaranteed = newGuaranteed;
        this.primogems = newPrimos;
        this.intertwinedFates = newIntertwined;
        this.acquaintFates = newAcquaint;
        this.stardust = newStardust;
        this.starglitter = newStarglitter;
        this.beginnerPulls = newBeginnerPulls;
        this.mora = newMora;
        this.weaponBannerTarget = newWeaponTarget;
        this.weaponBannerFatePoints = newWeaponFatePoints;
        this.characterBannerTarget = newCharTarget;
        this.totalWishes = newTotalWishes;
        if (this.beginnerPulls >= 20 && this.activeBanner == 3) {
            this.activeBanner = getBannerForFranchise(this.activeFranchise);
        }
        this.currentRevealIndex = 0;
        this.screenState = STATE_METEOR;
        this.meteorStartTime = System.currentTimeMillis();
        this.revealStartTime = 0L;
        stopAmbientSound();
        stopCurrentVoiceSound();
        playMeteorSound(highestRarity);
    }

    public void showResults(int highestRarity, List<ItemStack> items, List<Integer> rarities, int newPity5, boolean newGuaranteed, int newPrimos, int newIntertwined, int newAcquaint, int newStardust, int newStarglitter, int newBeginnerPulls, int newMora, int newWeaponTarget, int newWeaponFatePoints, int newCharTarget) {
        showResults(highestRarity, items, rarities, newPity5, newGuaranteed, newPrimos, newIntertwined, newAcquaint, newStardust, newStarglitter, newBeginnerPulls, newMora, newWeaponTarget, newWeaponFatePoints, newCharTarget, this.totalWishes + items.size());
    }

    public void showResults(int highestRarity, List<ItemStack> items, List<Integer> rarities, int newPity5, boolean newGuaranteed, int newPrimos, int newIntertwined, int newAcquaint, int newStardust, int newStarglitter, int newBeginnerPulls) {
        showResults(highestRarity, items, rarities, newPity5, newGuaranteed, newPrimos, newIntertwined, newAcquaint, newStardust, newStarglitter, newBeginnerPulls, this.mora, this.weaponBannerTarget, this.weaponBannerFatePoints, this.characterBannerTarget);
    }

    public void showResults(int highestRarity, List<ItemStack> items, List<Integer> rarities, int newPity5, boolean newGuaranteed) {
        showResults(highestRarity, items, rarities, newPity5, newGuaranteed, this.primogems, this.intertwinedFates, this.acquaintFates, this.stardust, this.starglitter, this.beginnerPulls, this.mora, this.weaponBannerTarget, this.weaponBannerFatePoints, this.characterBannerTarget);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getGachaGuiScale() {
        float scaleX = (float) this.width / 700.0F;
        float scaleY = (float) this.height / 360.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.35F, scale));
    }

    public int getVirtW() {
        float s = getGachaGuiScale();
        return s < 1.0F ? (int) Math.ceil(this.width / s) : this.width;
    }

    public int getVirtH() {
        float s = getGachaGuiScale();
        return s < 1.0F ? (int) Math.ceil(this.height / s) : this.height;
    }

    public double transformMouseX(double mouseX) {
        float s = getGachaGuiScale();
        return s < 1.0F ? mouseX / s : mouseX;
    }

    public double transformMouseY(double mouseY) {
        float s = getGachaGuiScale();
        return s < 1.0F ? mouseY / s : mouseY;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float scale = getGachaGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        graphics.pose().pushPose();
        if (scale < 1.0F) {
            graphics.pose().scale(scale, scale, 1.0F);
        }

        renderCosmicSky(graphics, mX, mY);

        switch (screenState) {
            case STATE_METEOR -> {
                if (activeBanner == BANNER_UMA) {
                    renderUmaStartingGateCinematic(graphics, mX, mY);
                } else if (activeBanner == BANNER_STELLAR_WARP) {
                    renderAstralExpressWarpCinematic(graphics, mX, mY);
                } else {
                    renderMeteorAnimation(graphics, mX, mY);
                }
            }
            case STATE_REVEAL_SINGLE -> renderSingleReveal(graphics, mX, mY);
            case STATE_SUMMARY -> renderSummaryGrid(graphics, mX, mY);
            default -> {
                if (showHistoryModal) {
                    renderHistoryModal(graphics, mX, mY);
                } else if (showShopModal) {
                    renderShopModal(graphics, mX, mY);
                } else if (showTopUpModal) {
                    renderTopUpModal(graphics, mX, mY);
                } else if (showPathModal) {
                    renderPathModal(graphics, mX, mY);
                } else if (showNewsModal) {
                    renderNewsModal(graphics, mX, mY);
                } else {
                    renderBannerSelection(graphics, mX, mY);
                }
            }
        }

        graphics.pose().popPose();
    }

    private int getClientItemCount(net.minecraft.world.item.Item item) {
        if (this.minecraft == null || this.minecraft.player == null || item == null) return 0;
        int count = 0;
        for (ItemStack s : this.minecraft.player.getInventory().items) {
            if (!s.isEmpty() && s.is(item)) {
                count += s.getCount();
            }
        }
        return count;
    }

    private void renderCosmicSky(GuiGraphics graphics, int mouseX, int mouseY) {
        int w = getVirtW();
        int h = getVirtH();
        if (activeBanner == BANNER_UMA) {
            renderTracenAcademyBackground(graphics, w, h);
            return;
        } else if (activeBanner == BANNER_STELLAR_WARP) {
            graphics.fillGradient(0, 0, w, h, 0xFF0A071E, 0xFF1D1140);
            graphics.fillGradient(0, h - 100, w, h, 0x00000000, 0x446C5CE7);
            long time = System.currentTimeMillis();
            for (int i = 0; i < 36; i++) {
                int sx = (i * 67 + 23) % Math.max(1, w);
                int sy = (i * 47 + 13) % Math.max(1, h);
                float twinkle = (float) Math.sin((time + i * 190) / 300.0);
                int alpha = twinkle > 0 ? (int) (80 + twinkle * 160) : 50;
                int sz = (i % 5 == 0) ? 2 : 1;
                graphics.fill(sx, sy, sx + sz, sy + sz, (alpha << 24) | 0x00D6A2E8);
            }
            return;
        }

        long time = System.currentTimeMillis();

        int topBg = highestWonRarity == 5 && screenState == STATE_METEOR ? 0xFF1C1303 : 0xFF080B1C;
        int botBg = highestWonRarity == 5 && screenState == STATE_METEOR ? 0xFF352006 : 0xFF161432;
        graphics.fillGradient(0, 0, w, h, topBg, botBg);

        graphics.fillGradient(0, h - 90, w, h, 0x00000000, 0x2E100A24);

        for (int i = 0; i < 32; i++) {
            int sx = (i * 73 + 17) % Math.max(1, w);
            int sy = (i * 43 + 29) % Math.max(1, h);
            float twinkle = (float) Math.sin((time + i * 211) / 350.0);
            int alpha = twinkle > 0 ? (int) (70 + twinkle * 160) : 40;
            int sz = (i % 6 == 0) ? 2 : 1;
            graphics.fill(sx, sy, sx + sz, sy + sz, (alpha << 24) | 0x00FFFDD0);
        }

        if (w > 400) {
            int colW = 16;
            graphics.fill(0, 0, colW, h, 0x44080C1A);
            graphics.fill(colW - 1, 0, colW, h, 0x2264B5F6);

            graphics.fill(w - colW, 0, w, h, 0x44080C1A);
            graphics.fill(w - colW, 0, w - colW + 1, h, 0x2264B5F6);
        }
    }

    private void renderTracenAcademyBackground(GuiGraphics graphics, int w, int h) {
        long time = System.currentTimeMillis();
        int horizonY = (int) (h * 0.56F);

        graphics.fillGradient(0, 0, w, horizonY, 0xFF1E88E5, 0xFFB3E5FC);

        int sunX = (int) (w * 0.82F);
        int sunY = (int) (h * 0.16F);
        graphics.fill(sunX - 35, sunY - 35, sunX + 35, sunY + 35, 0x22FFF59D);
        graphics.fill(sunX - 20, sunY - 20, sunX + 20, sunY + 20, 0x44FFF9C4);
        graphics.fill(sunX - 10, sunY - 10, sunX + 10, sunY + 10, 0x88FFFFFF);

        for (int c = 0; c < 5; c++) {
            float speed = 0.012F + (c % 3) * 0.006F;
            int cloudW = 75 + (c * 23) % 45;
            int cloudH = 22 + (c * 11) % 15;
            int cx = (int) ((time * speed + c * 150) % (w + cloudW * 2)) - cloudW;
            int cy = 20 + (c * 31) % Math.max(1, horizonY - 45);

            graphics.fill(cx + 8, cy, cx + cloudW - 8, cy + cloudH, 0x66FFFFFF);
            graphics.fill(cx, cy + 6, cx + cloudW, cy + cloudH, 0x88FFFFFF);
            graphics.fill(cx + 14, cy - 5, cx + cloudW - 20, cy + 8, 0x77FFFFFF);
        }

        for (int x = 0; x < w; x += 16) {
            int hillHeight = (int) (Math.sin((x + 40) * 0.015) * 8.0 + 8.0);
            graphics.fill(x, horizonY - hillHeight, x + 16, horizonY, 0xFF388E3C);
        }

        int standW = Math.min(240, (int) (w * 0.46F));
        int standX = (int) (w * 0.06F);
        int standTop = horizonY - 28;
        graphics.fill(standX, standTop, standX + standW, standTop + 4, 0xFFECEFF1);
        graphics.fill(standX + 5, standTop + 4, standX + standW - 10, standTop + 7, 0xFFCFD8DC);
        graphics.fill(standX + 10, standTop + 7, standX + standW - 15, horizonY, 0xFF455A64);
        for (int tier = 0; tier < 4; tier++) {
            int tierY = standTop + 9 + tier * 4;
            int tierCol = (tier % 2 == 0) ? 0xFF2E7D32 : 0xFFC62828;
            graphics.fill(standX + 12, tierY, standX + standW - 18, tierY + 2, tierCol);
        }
        for (int f = 0; f < 4; f++) {
            int fx = standX + 15 + f * (standW / 4);
            graphics.fill(fx, standTop - 8, fx + 1, standTop, 0xFF37474F);
            graphics.fill(fx + 1, standTop - 8, fx + 6, standTop - 4, (f % 2 == 0) ? 0xFFFFD700 : 0xFF1E88E5);
        }

        int turfH = h - horizonY;
        for (int s = 0; s < 12; s++) {
            int sy1 = horizonY + (int) ((float) s / 12.0F * turfH);
            int sy2 = horizonY + (int) ((float) (s + 1) / 12.0F * turfH);
            int stripeCol = (s % 2 == 0) ? 0xFF2E7D32 : 0xFF388E3C;
            graphics.fill(0, sy1, w, sy2, stripeCol);
        }

        graphics.fill(0, horizonY + 3, w, horizonY + 8, 0xFF8D6E63);

        graphics.fill(0, horizonY - 4, w, horizonY - 2, 0xFFFFFFFF);
        graphics.fill(0, horizonY + 1, w, horizonY + 3, 0xFFFFFFFF);
        for (int px = 0; px < w; px += 36) {
            graphics.fill(px, horizonY - 6, px + 2, horizonY + 4, 0xFFFFFFFF);
            graphics.fill(px + 2, horizonY - 4, px + 3, horizonY + 4, 0x66B0BEC5);
        }

        for (int p = 0; p < 18; p++) {
            float pSpeed = 0.035F + (p % 4) * 0.015F;
            int px = (int) ((time * pSpeed + p * 67) % (w + 40)) - 20;
            int py = (int) ((Math.sin(time * 0.002 + p) * 18.0) + (p * 23) % h);
            int pCol = (p % 3 == 0) ? 0xFFFF80AB : ((p % 3 == 1) ? 0xFFF48FB1 : 0xFF81C784);
            graphics.fill(px, py, px + 3, py + 2, pCol);
        }
    }

    private int getCardW() {
        return Math.min(680, getVirtW() - 24);
    }

    private int getCardX() {
        return (getVirtW() - getCardW()) / 2;
    }

    private int getCardY() {
        return 38;
    }

    private int getCardH() {
        return Math.max(160, Math.min(235, getVirtH() - 86));
    }

    private int getBottomY() {
        return Math.max(getCardY() + getCardH() + 6, getVirtH() - 38);
    }

    private boolean isHistBtnHover(double mouseX, double mouseY) {
        int x = getCardX();
        int y = getBottomY();
        return mouseX >= x && mouseX <= x + 75 && mouseY >= y && mouseY <= y + 26;
    }

    private boolean isShopBtnHover(double mouseX, double mouseY) {
        int x = getCardX() + 80;
        int y = getBottomY();
        return mouseX >= x && mouseX <= x + 95 && mouseY >= y && mouseY <= y + 26;
    }

    private boolean isPathBtnHover(double mouseX, double mouseY) {
        if (activeBanner != 0) return false;
        int x = getCardX() + 180;
        int y = getBottomY();
        return mouseX >= x && mouseX <= x + 88 && mouseY >= y && mouseY <= y + 26;
    }

    private boolean isSubspaceBtnHover(double mouseX, double mouseY) {
        int x = (activeBanner == 0) ? (getCardX() + 273) : (getCardX() + 180);
        int y = getBottomY();
        return mouseX >= x && mouseX <= x + 85 && mouseY >= y && mouseY <= y + 26;
    }

    private boolean isWish10BtnHover(double mouseX, double mouseY) {
        int w = (activeBanner == 3) ? 145 : ((activeBanner == BANNER_UMA || activeBanner == BANNER_STELLAR_WARP) ? 140 : 135);
        int x = getCardX() + getCardW() - w;
        int y = getBottomY() - 1;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 28;
    }

    private boolean isWish1BtnHover(double mouseX, double mouseY) {
        if (activeBanner == 3) return false;
        int wish10W = (activeBanner == BANNER_UMA || activeBanner == BANNER_STELLAR_WARP) ? 140 : 135;
        int w = (activeBanner == BANNER_UMA || activeBanner == BANNER_STELLAR_WARP) ? 125 : 120;
        int x = getCardX() + getCardW() - wish10W - 8 - w;
        int y = getBottomY() - 1;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 28;
    }

    private PromotionalBanner.BannerPhase getPromoPhase(int index) {
        long dayTime = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getDayTime() : 0L;
        return PromotionalBanner.getActivePhase(dayTime, index);
    }

    private void playFranchiseSwitchSound(PromotionalBanner.Franchise franchise) {
        stopCurrentVoiceSound();
        if (franchise == null) return;
        switch (franchise) {
            case GENSHIN -> {
                PromotionalBanner.BannerPhase phase = (activeBanner == BANNER_PROMO_2) ? getPromoPhase(1) : getPromoPhase(0);
                CompanionProfile featured = phase != null ? phase.featuredCharacter() : null;
                if (featured != null && featured.getSummonSound() != null) {
                    playVoiceSound(featured.getSummonSound(), 1.25F, 1.0F);
                } else {
                    playVoiceSound(ModSounds.COMPANION_HU_TAO_SUMMON.get(), 1.25F, 1.0F);
                }
            }
            case SOLO_LEVELING -> playVoiceSound(ModSounds.COMPANION_SUNG_JINWOO_SUMMON.get(), 1.25F, 1.0F);
            case JUJUTSU_KAISEN -> playVoiceSound(ModSounds.COMPANION_GOJO_SATORU_SUMMON.get(), 1.25F, 1.0F);
            case HONKAI_ZZZ -> playVoiceSound(ModSounds.HERTA_KURURIN.get(), 1.25F, 1.0F);
            case FATE -> playVoiceSound(SoundEvents.BELL_RESONATE, 1.0F, 1.2F);
            case DEMON_SLAYER -> playVoiceSound(SoundEvents.FIRECHARGE_USE, 1.1F, 1.0F);
            case WUTHERING_WAVES -> playVoiceSound(SoundEvents.PLAYER_LEVELUP, 1.1F, 1.3F);
            case NTE -> playVoiceSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.2F, 1.4F);
            default -> {
                PromotionalBanner.BannerPhase phase = (activeBanner == BANNER_PROMO_2) ? getPromoPhase(1) : getPromoPhase(0);
                CompanionProfile featured = phase != null ? phase.featuredCharacter() : null;
                if (featured != null && featured.getSummonSound() != null) {
                    playVoiceSound(featured.getSummonSound(), 1.25F, 1.0F);
                }
            }
        }
    }

    private void renderPedestal(GuiGraphics graphics, int cx, int cy, int radiusX, int radiusY, int glowColor, int ringColor) {
        graphics.fillGradient(cx - radiusX, cy - radiusY, cx + radiusX, cy + radiusY, glowColor & 0x44FFFFFF, 0x00000000);
        graphics.fill(cx - radiusX * 3 / 4, cy, cx + radiusX * 3 / 4, cy + 2, ringColor & 0x88FFFFFF);
        graphics.fill(cx - radiusX / 2, cy - 1, cx + radiusX / 2, cy + 3, ringColor);
    }

    private void renderBadge(GuiGraphics graphics, String text, int x, int y, int w, int h, int textColor, int bgColor, int borderColor) {
        graphics.fill(x, y, x + w, y + h, bgColor);
        graphics.renderOutline(x, y, w, h, borderColor);
        renderCenteredFittedText(graphics, text, x + w / 2, y + (h - 8) / 2, w - 6, textColor);
    }

    private void render4StarCharacterCard(GuiGraphics graphics, CompanionProfile profile, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hover ? 0xDD2A143D : 0xAA170E26;
        int border = hover ? 0xFFFFD700 : 0xFFDA70D6;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);

        renderCompanionFace(graphics, profile.getTextureLocation(), x + 2, y + (h - 16) / 2, 16);

        int textX = x + 20;
        int availTextW = Math.max(10, w - 22);
        renderFittedText(graphics, profile.displayName(), textX, y + 2, availTextW, hover ? 0xFFFFFF : 0xFFE082, false);
        renderFittedText(graphics, "§d★★★★ §7" + profile.element(), textX, y + 11, availTextW, 0xFFDA70D6, false);
    }

    private void renderUmaMiniCard(GuiGraphics graphics, com.soybe.awakeningsystem.uma.UmaProfile profile, int rarity, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hover ? 0xDD0B301B : 0xAA061D10;
        int border = hover ? 0xFFFFD700 : 0xFF2ECC71;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);

        renderCompanionFace(graphics, profile.getTextureLocation(), x + 2, y + (h - 16) / 2, 16);

        int textX = x + 20;
        int availTextW = Math.max(10, w - 22);
        renderFittedText(graphics, profile.getDisplayName(), textX, y + 2, availTextW, hover ? 0xFFFFFF : 0xA5D6A7, false);
        renderFittedText(graphics, "§6" + "★".repeat(rarity) + " §7" + profile.getDistance(), textX, y + 11, availTextW, 0xFFFFD700, false);
    }

    private void render4StarWeaponCard(GuiGraphics graphics, ItemStack weapon, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hover ? 0xDD1B2945 : 0xAA0D172B;
        int border = hover ? 0xFFFFD700 : 0xFF64B5F6;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);

        graphics.renderItem(weapon, x + 2, y + (h - 16) / 2);

        int textX = x + 20;
        int availTextW = Math.max(10, w - 22);
        renderFittedText(graphics, weapon.getHoverName().getString(), textX, y + 2, availTextW, hover ? 0xFFFFFF : 0xBBDEFB, false);
        renderFittedText(graphics, Component.translatable("gui.awakening_system.gacha.card.4star_weapon").getString(), textX, y + 11, availTextW, 0xFF64B5F6, false);
    }

    private void renderBannerSelection(GuiGraphics graphics, int mouseX, int mouseY) {
        long dayTime = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getDayTime() : 0L;
        PromotionalBanner.BannerPhase promoPhase1 = PromotionalBanner.getActivePhase(dayTime, 0);
        PromotionalBanner.BannerPhase promoPhase2 = PromotionalBanner.getActivePhase(dayTime, 1);
        int currentPromoIndex = (activeBanner == BANNER_PROMO_2) ? 1 : 0;
        PromotionalBanner.BannerPhase promoPhase = (currentPromoIndex == 1) ? promoPhase2 : promoPhase1;
        activeFranchise = promoPhase.franchise();
        String promoTimeLeft = PromotionalBanner.getFormattedTimeRemaining(dayTime);
        long currentMCDay = PromotionalBanner.getCurrentDay(dayTime);
        int currentPhaseNum = PromotionalBanner.getPhaseNumber(dayTime);
        int totalPhases = PromotionalBanner.getTotalPhases();

        int exitX = 10;
        int exitY = 6;
        int exitW = 54;
        int exitH = 17;
        boolean hoverExit = mouseX >= exitX && mouseX <= exitX + exitW && mouseY >= exitY && mouseY <= exitY + exitH;
        graphics.fill(exitX, exitY, exitX + exitW, exitY + exitH, hoverExit ? 0xCC334155 : 0x881E293B);
        graphics.renderOutline(exitX, exitY, exitW, exitH, hoverExit ? 0xFFFFFFFF : 0xFF475569);
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.tab.exit"), exitX + exitW / 2, exitY + 5, 0xFFFFFF);

        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 9 : 8;
        int subTabH = 17;
        int subModeStartX = exitX + exitW + 4;
        int rightReserved = (activeBanner == BANNER_UMA) ? 140 : ((activeBanner == BANNER_STELLAR_WARP) ? 145 : 185);
        int availW = getVirtW() - subModeStartX - rightReserved - 8;
        int subTabW = Math.max(42, Math.min(64, availW / numSubTabs));

        String tab1Name = "✦ " + promoPhase1.featuredCharacter().displayName();
        String tab2Name = "✦ " + promoPhase2.featuredCharacter().displayName();

        String[] subTitles = hasNovice
                ? new String[]{
                    tab1Name,
                    tab2Name,
                    Component.translatable("gui.awakening_system.gacha.tab.weapons_5star").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.hsr_warp").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.permanent").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.umas").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.news").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.novice").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.topup").getString()
                }
                : new String[]{
                    tab1Name,
                    tab2Name,
                    Component.translatable("gui.awakening_system.gacha.tab.weapons_5star").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.hsr_warp").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.permanent").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.umas").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.news").getString(),
                    Component.translatable("gui.awakening_system.gacha.tab.topup").getString()
                };

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            boolean isSelected;
            if (i == 0) isSelected = (activeBanner == 1) && !showTopUpModal && !showNewsModal;
            else if (i == 1) isSelected = (activeBanner == BANNER_PROMO_2) && !showTopUpModal && !showNewsModal;
            else if (i == 2) isSelected = (activeBanner == 0) && !showTopUpModal && !showNewsModal;
            else if (i == 3) isSelected = (activeBanner == BANNER_STELLAR_WARP) && !showTopUpModal && !showNewsModal;
            else if (i == 4) isSelected = (activeBanner == 2) && !showTopUpModal && !showNewsModal;
            else if (i == 5) isSelected = (activeBanner == BANNER_UMA) && !showTopUpModal && !showNewsModal;
            else if (i == 6) isSelected = showNewsModal;
            else if (hasNovice && i == 7) isSelected = (activeBanner == 3) && !showTopUpModal && !showNewsModal;
            else isSelected = showTopUpModal;

            boolean isHover = mouseX >= sx && mouseX <= sx + subTabW && mouseY >= exitY && mouseY <= exitY + subTabH;

            if (isSelected) {
                int topGrad = (activeBanner == BANNER_UMA) ? 0xFF082815 : ((activeBanner == BANNER_STELLAR_WARP) ? 0xFF2A1040 : (showNewsModal ? 0xFF102840 : 0xFF3D2706));
                int botGrad = (activeBanner == BANNER_UMA) ? 0xFF04140A : ((activeBanner == BANNER_STELLAR_WARP) ? 0xFF140822 : (showNewsModal ? 0xFF081422 : 0xFF1F1403));
                int borderCol = (activeBanner == BANNER_UMA) ? 0xFF2ECC71 : ((activeBanner == BANNER_STELLAR_WARP) ? 0xFFBA68C8 : (showNewsModal ? 0xFF64B5F6 : 0xFFFFD700));
                graphics.fillGradient(sx, exitY, sx + subTabW, exitY + subTabH, topGrad, botGrad);
                graphics.renderOutline(sx, exitY, subTabW, subTabH, borderCol);
                graphics.fill(sx + 4, exitY + subTabH - 1, sx + subTabW - 4, exitY + subTabH + 1, borderCol);
                renderCenteredFittedText(graphics, subTitles[i], sx + subTabW / 2, exitY + 4, subTabW - 4, borderCol);
            } else {
                graphics.fill(sx, exitY, sx + subTabW, exitY + subTabH, isHover ? 0xCC1A233A : 0x880C1220);
                graphics.renderOutline(sx, exitY, subTabW, subTabH, isHover ? 0xFF94A3B8 : 0x44475569);
                renderCenteredFittedText(graphics, subTitles[i], sx + subTabW / 2, exitY + 5, subTabW - 4, isHover ? 0xEEEEEE : 0x94A3B8);
            }
        }

        int pillH = 17;
        int pillY = 6;
        boolean usesIntertwined = (activeBanner == 0 || activeBanner == 1 || isPromotionalCharacterBanner());
        if (activeBanner == 2 || activeBanner == 3) {
            usesIntertwined = false;
        }
        boolean isUma = (activeBanner == BANNER_UMA);
        boolean isStellar = (activeBanner == BANNER_STELLAR_WARP);

        int fateCount = usesIntertwined ? intertwinedFates : acquaintFates;
        String sFate = String.valueOf(fateCount);
        int pill1W = 18 + this.font.width(sFate) + 6;
        int pill1X = getVirtW() - pill1W - 10;

        String sPrimo = String.format(java.util.Locale.US, "%,d", primogems);
        int pill2W = 18 + this.font.width(sPrimo) + 6;
        int pill2X = pill1X - pill2W - 6;

        String sCarats = String.format(java.util.Locale.US, "%,d", carats);
        int pillCaratW = 18 + this.font.width(sCarats) + 18;
        int pillCaratX = getVirtW() - pillCaratW - 10;

        String sCoins = String.format(java.util.Locale.US, "%,d", coins);
        int pillCoinW = 16 + this.font.width(sCoins) + 8;
        int pillCoinX = pillCaratX - pillCoinW - 6;

        int pill3W = 0;
        int pill3X = 0;
        int glitterX = 0;
        int pill4W = 84;
        int pill4X = 0;
        int totalSubModeW = subModeStartX + numSubTabs * (subTabW + 4);

        if (isUma) {
            graphics.fill(pillCaratX, pillY, pillCaratX + pillCaratW, pillY + pillH, 0xDD0B0F1A);
            graphics.renderOutline(pillCaratX, pillY, pillCaratW, pillH, 0xFF334155);
            ItemStack caratStack = SystemItems.CARAT.isBound() ? new ItemStack(SystemItems.CARAT.get()) : new ItemStack(Items.CARROT);
            graphics.renderItem(caratStack, pillCaratX + 1, pillY + 1);
            graphics.drawString(this.font, sCarats, pillCaratX + 19, pillY + 4, 0xFFFFA726, false);

            int plusX = pillCaratX + pillCaratW - 14;
            boolean hoverPlus = mouseX >= plusX && mouseX <= plusX + 12 && mouseY >= pillY + 2 && mouseY <= pillY + 14;
            graphics.drawString(this.font, hoverPlus ? "§a[+]" : "§7[+]", plusX - 2, pillY + 4, 0xFFFFFFFF, false);

            graphics.fill(pillCoinX, pillY, pillCoinX + pillCoinW, pillY + pillH, 0xDD0B0F1A);
            graphics.renderOutline(pillCoinX, pillY, pillCoinW, pillH, 0xFF334155);
            graphics.drawString(this.font, "🪙 " + sCoins, pillCoinX + 5, pillY + 4, 0xFFFFE082, false);
        } else if (isStellar) {
            String sJade = String.format(java.util.Locale.US, "%,d", stellarJade);
            int pillJadeW = 18 + this.font.width(sJade) + 18;
            int pillJadeX = getVirtW() - pillJadeW - 10;

            graphics.fill(pillJadeX, pillY, pillJadeX + pillJadeW, pillY + pillH, 0xDD0B0F1A);
            graphics.renderOutline(pillJadeX, pillY, pillJadeW, pillH, 0xFF9C27B0);
            ItemStack jadeStack = SystemItems.STELLAR_JADE.isBound() ? new ItemStack(SystemItems.STELLAR_JADE.get()) : new ItemStack(Items.DIAMOND);
            graphics.renderItem(jadeStack, pillJadeX + 1, pillY + 1);
            graphics.drawString(this.font, sJade, pillJadeX + 19, pillY + 4, 0xFFCE93D8, false);

            int plusX = pillJadeX + pillJadeW - 14;
            boolean hoverPlus = mouseX >= plusX && mouseX <= plusX + 12 && mouseY >= pillY + 2 && mouseY <= pillY + 14;
            graphics.drawString(this.font, hoverPlus ? "§a[+]" : "§7[+]", plusX - 2, pillY + 4, 0xFFFFFFFF, false);

            int pillPrimoX = pillJadeX - pill2W - 6;
            graphics.fill(pillPrimoX, pillY, pillPrimoX + pill2W, pillY + pillH, 0xDD0B0F1A);
            graphics.renderOutline(pillPrimoX, pillY, pill2W, pillH, 0xFF334155);
            ItemStack primoStack = SystemItems.PRIMOGEM.isBound() ? new ItemStack(SystemItems.PRIMOGEM.get()) : ItemStack.EMPTY;
            if (!primoStack.isEmpty()) {
                graphics.renderItem(primoStack, pillPrimoX + 1, pillY + 1);
            }
            graphics.drawString(this.font, sPrimo, pillPrimoX + 19, pillY + 4, 0xFFFFFFFF, false);
        } else {
            graphics.fill(pill1X, pillY, pill1X + pill1W, pillY + pillH, 0xDD0B0F1A);
            graphics.renderOutline(pill1X, pillY, pill1W, pillH, 0xFF334155);
            ItemStack fateStack = usesIntertwined
                    ? (SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : ItemStack.EMPTY)
                    : (SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : ItemStack.EMPTY);
            int fateColor = usesIntertwined ? 0xFFFFD700 : 0xFF80D8FF;
            if (!fateStack.isEmpty()) {
                graphics.renderItem(fateStack, pill1X + 1, pillY + 1);
            }
            graphics.drawString(this.font, sFate, pill1X + 19, pillY + 4, fateColor, false);

            graphics.fill(pill2X, pillY, pill2X + pill2W, pillY + pillH, 0xDD0B0F1A);
            graphics.renderOutline(pill2X, pillY, pill2W, pillH, 0xFF334155);
            ItemStack primoStack = SystemItems.PRIMOGEM.isBound() ? new ItemStack(SystemItems.PRIMOGEM.get()) : ItemStack.EMPTY;
            if (!primoStack.isEmpty()) {
                graphics.renderItem(primoStack, pill2X + 1, pillY + 1);
            }
            graphics.drawString(this.font, sPrimo, pill2X + 19, pillY + 4, 0xFFFFFFFF, false);

            ItemStack stardustStack = SystemItems.MASTERLESS_STARDUST.isBound() ? new ItemStack(SystemItems.MASTERLESS_STARDUST.get()) : ItemStack.EMPTY;
            ItemStack starglitterStack = SystemItems.MASTERLESS_STARGLITTER.isBound() ? new ItemStack(SystemItems.MASTERLESS_STARGLITTER.get()) : ItemStack.EMPTY;
            String sDust = String.valueOf(stardust);
            String sGlitter = String.valueOf(starglitter);
            int wDust = this.font.width(sDust);
            int wGlitter = this.font.width(sGlitter);
            pill3W = 18 + wDust + 4 + 16 + wGlitter + 4;
            pill3X = pill2X - pill3W - 5;
            glitterX = pill3X + 18 + wDust + 4;

            if (pill3X > totalSubModeW + 8) {
                graphics.fill(pill3X, pillY, pill3X + pill3W, pillY + pillH, 0xDD0B0F1A);
                graphics.renderOutline(pill3X, pillY, pill3W, pillH, 0xFF334155);

                if (!stardustStack.isEmpty()) {
                    graphics.renderItem(stardustStack, pill3X + 1, pillY + 1);
                }
                graphics.drawString(this.font, sDust, pill3X + 18, pillY + 4, 0xFF80D8FF, false);

                if (!starglitterStack.isEmpty()) {
                    graphics.renderItem(starglitterStack, glitterX, pillY + 1);
                }
                graphics.drawString(this.font, sGlitter, glitterX + 16, pillY + 4, 0xFFFFD700, false);
            }

            pill4X = pill3X - pill4W - 5;
            if (pill4X > totalSubModeW + 8) {
                graphics.fill(pill4X, pillY, pill4X + pill4W, pillY + pillH, 0xDD0B0F1A);
                graphics.renderOutline(pill4X, pillY, pill4W, pillH, 0xFF334155);
                ItemStack moraItem = SystemItems.MORA.isBound() ? new ItemStack(SystemItems.MORA.get()) : ItemStack.EMPTY;
                if (!moraItem.isEmpty()) {
                    graphics.renderItem(moraItem, pill4X + 1, pillY + 1);
                    renderFittedText(graphics, String.format(java.util.Locale.US, "%,d", mora), pill4X + 19, pillY + 4, pill4W - 22, 0xFFFFE082, false);
                } else {
                    renderFittedText(graphics, "🪙 " + String.format(java.util.Locale.US, "%,d", mora), pill4X + 4, pillY + 4, pill4W - 8, 0xFFFFE082, false);
                }
            }
        }

        int cardX = getCardX();
        int cardY = getCardY();
        int cardW = getCardW();
        int cardH = getCardH();

        int cardBorder;
        int cardBgTop;
        int cardBgBot;
        int pedestalGlow;
        String bannerTag;
        String mainTitle;
        String subtitle;
        String timerTag;
        int titleColor;

        if (activeBanner == 0) {
            cardBorder = 0xFFFFD700;
            cardBgTop = 0xFF1C1306;
            cardBgBot = 0xFF2A1C0A;
            pedestalGlow = 0x66FFD700;
            bannerTag = Component.translatable("gui.awakening_system.gacha.banner_weapons_tag").getString();
            timerTag = "⏳ " + promoTimeLeft;
            mainTitle = Component.translatable("gui.awakening_system.gacha.banner_weapons_title").getString();
            subtitle = Component.translatable("gui.awakening_system.gacha.banner_weapons_sub").getString();
            titleColor = 0xFFFFD700;
        } else if (activeBanner == BANNER_STELLAR_WARP) {
            cardBorder = 0xFFBA68C8;
            cardBgTop = 0xFF0D0B24;
            cardBgBot = 0xFF1B1440;
            pedestalGlow = 0x669C27B0;
            bannerTag = Component.translatable("gui.awakening_system.gacha.banner_hsr_tag").getString();
            timerTag = Component.translatable("gui.awakening_system.gacha.banner_hsr_timer").getString();
            mainTitle = Component.translatable("gui.awakening_system.gacha.banner_hsr_title").getString();
            subtitle = Component.translatable("gui.awakening_system.gacha.banner_hsr_sub").getString();
            titleColor = 0xFFCE93D8;
        } else if (activeBanner == BANNER_UMA) {
            com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.BannerPhase umaPhase = getSelectedUmaPhase();
            String umaTimeLeft = com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.getRemainingTimeString(dayTime);
            int phaseNum = (umaPhase.ordinal() + 1);
            cardBorder = 0xFF2ECC71;
            cardBgTop = 0xFF082015;
            cardBgBot = 0xFF123524;
            pedestalGlow = 0x662ECC71;
            bannerTag = Component.translatable("gui.awakening_system.gacha.banner_uma_tag", phaseNum).getString();
            timerTag = Component.translatable("gui.awakening_system.gacha.rotation_timer", umaTimeLeft).getString();
            mainTitle = umaPhase.getTitle().toUpperCase();
            subtitle = "§a" + umaPhase.getSubtitle();
            titleColor = 0xFF2ECC71;
        } else if (isPromotionalCharacterBanner()) {
            int promoNumber = (activeBanner == BANNER_PROMO_2) ? 2 : 1;
            cardBorder = promoPhase.themeColor();
            cardBgTop = 0xFF190B26;
            cardBgBot = 0xFF28103D;
            pedestalGlow = promoPhase.themeGlowColor();
            bannerTag = Component.translatable("gui.awakening_system.gacha.banner_promo_tag", activeFranchise.displayName().toUpperCase(), promoNumber, currentPhaseNum, totalPhases).getString();
            timerTag = Component.translatable("gui.awakening_system.gacha.rotation_timer", promoTimeLeft).getString();
            mainTitle = promoPhase.featuredCharacter().displayName().toUpperCase();
            subtitle = "§d" + promoPhase.bannerTitle() + " · " + promoPhase.bannerSubtitle();
            titleColor = promoPhase.themeColor();
        } else if (activeBanner == 3) {
            cardBorder = 0xFFFFE082;
            cardBgTop = 0xFF231A0B;
            cardBgBot = 0xFF352610;
            pedestalGlow = 0x66FFE082;
            bannerTag = Component.translatable("gui.awakening_system.gacha.banner_novice_tag").getString();
            timerTag = Component.translatable("gui.awakening_system.gacha.banner_novice_remaining", (20 - beginnerPulls)).getString();
            mainTitle = Component.translatable("gui.awakening_system.gacha.banner_novice_title").getString();
            subtitle = Component.translatable("gui.awakening_system.gacha.banner_novice_sub").getString();
            titleColor = 0xFFFFE082;
        } else {
            cardBorder = 0xFF64B5F6;
            cardBgTop = 0xFF0B1428;
            cardBgBot = 0xFF142446;
            pedestalGlow = 0x6664B5F6;
            bannerTag = Component.translatable("gui.awakening_system.gacha.banner_standard_tag").getString();
            timerTag = Component.translatable("gui.awakening_system.gacha.banner_standard_timer").getString();
            mainTitle = Component.translatable("gui.awakening_system.gacha.banner_standard_title").getString();
            subtitle = Component.translatable("gui.awakening_system.gacha.banner_standard_sub").getString();
            titleColor = 0xFF80D8FF;
        }

        graphics.fillGradient(cardX, cardY, cardX + cardW, cardY + cardH, cardBgTop, cardBgBot);
        graphics.renderOutline(cardX, cardY, cardW, cardH, cardBorder);
        graphics.renderOutline(cardX + 2, cardY + 2, cardW - 4, cardH - 4, 0x33FFFFFF);

        graphics.fill(cardX, cardY, cardX + 8, cardY + 2, cardBorder);
        graphics.fill(cardX, cardY, cardX + 2, cardY + 8, cardBorder);
        graphics.fill(cardX + cardW - 8, cardY, cardX + cardW, cardY + 2, cardBorder);
        graphics.fill(cardX + cardW - 2, cardY, cardX + cardW, cardY + 8, cardBorder);
        graphics.fill(cardX, cardY + cardH - 2, cardX + 8, cardY + cardH, cardBorder);
        graphics.fill(cardX, cardY + cardH - 8, cardX + 2, cardY + cardH, cardBorder);
        graphics.fill(cardX + cardW - 8, cardY + cardH - 2, cardX + cardW, cardY + cardH, cardBorder);
        graphics.fill(cardX + cardW - 2, cardY + cardH - 8, cardX + cardW, cardY + cardH, cardBorder);

        int leftX = cardX + 16;
        int stageW = Math.min(160, (int) (cardW * 0.35F));
        int stageX = cardX + cardW - stageW - 14;
        int stageY = cardY + 12;
        int stageH = cardH - 34;
        int leftW = stageX - leftX - 10;

        int tagW = this.font.width(bannerTag) + 14;
        renderBadge(graphics, bannerTag, leftX, cardY + 10, tagW, 14, cardBorder, 0x66000000, cardBorder);

        int timerW = this.font.width(timerTag) + 12;
        int timerX = stageX - timerW - 6;
        renderBadge(graphics, timerTag, timerX, cardY + 10, timerW, 13, 0xFFFFAA, 0x66000000, 0xFFFFD700);

        graphics.pose().pushPose();
        graphics.pose().translate(leftX, cardY + 26, 0);
        graphics.pose().scale(1.25F, 1.25F, 1.0F);
        graphics.drawString(this.font, mainTitle, 0, 0, titleColor, true);
        graphics.pose().popPose();

        renderFittedText(graphics, subtitle, leftX, cardY + 42, leftW, 0xEEEEEE, false);

        if (activeBanner == BANNER_UMA) {
            com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.BannerPhase phase = getSelectedUmaPhase();
            String umaTimeLeft = com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.getRemainingTimeString(dayTime);

            String phaseBadge = Component.translatable("gui.awakening_system.gacha.uma_rotation_tag", phase.getTitle(), umaTimeLeft).getString();
            int pBadgeW = this.font.width(phaseBadge) + 12;
            renderBadge(graphics, phaseBadge, leftX, cardY + 54, pBadgeW, 14, 0xFF81C784, 0x66000000, 0xFF2ECC71);

            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.uma_rate_up_header"), leftX, cardY + 70, 0xFF2ECC71, false);

            List<String> rateUp3 = phase.getRateUp3Star();
            int c4Count = Math.min(4, rateUp3.size());
            int c4W = Math.max(65, (leftW - (c4Count - 1) * 6) / Math.max(1, c4Count));
            int c4H = 26;
            int cX = leftX;

            for (int i = 0; i < c4Count; i++) {
                com.soybe.awakeningsystem.uma.UmaProfile up = com.soybe.awakeningsystem.uma.UmaProfile.byId(rateUp3.get(i));
                if (up != null) {
                    renderUmaMiniCard(graphics, up, 3, cX, cardY + 83, c4W, c4H, mouseX, mouseY);
                    cX += c4W + 6;
                }
            }

        } else if (activeBanner == BANNER_STELLAR_WARP) {
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.hsr_pool_header"), leftX, cardY + 54, 0xFFFFD700, false);
            int thirdW = (leftW - 12) / 3;
            renderBadge(graphics, "✦ Acheron / Firefly 5★", leftX, cardY + 66, thirdW, 15, 0xFFFFD700, 0x336A1B9A, 0xFFBA68C8);
            renderBadge(graphics, "✦ Dan Heng • IL / Feixiao 5★", leftX + thirdW + 6, cardY + 66, thirdW, 15, 0xFFFFD700, 0x3300838F, 0xFF4DD0E1);
            renderBadge(graphics, Component.translatable("gui.awakening_system.gacha.badge.light_cones_5star").getString(), leftX + (thirdW + 6) * 2, cardY + 66, thirdW, 15, 0xFFFFD700, 0x33B8860B, 0xFFFFD700);

            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.hsr_4star_members"), leftX, cardY + 85, 0xFFDA70D6, false);
            int c4Count = 4;
            int c4W = Math.max(65, (leftW - (c4Count - 1) * 6) / c4Count);
            int c4H = 24;
            render4StarCharacterCard(graphics, CompanionProfile.DAN_HENG, leftX, cardY + 97, c4W, c4H, mouseX, mouseY);
            render4StarCharacterCard(graphics, CompanionProfile.HERTA, leftX + c4W + 6, cardY + 97, c4W, c4H, mouseX, mouseY);
            render4StarCharacterCard(graphics, CompanionProfile.MARCH_7TH, leftX + (c4W + 6) * 2, cardY + 97, c4W, c4H, mouseX, mouseY);
            if (SystemItems.LIGHT_CONE_DANCE_DANCE_DANCE.isBound()) {
                render4StarWeaponCard(graphics, new ItemStack(SystemItems.LIGHT_CONE_DANCE_DANCE_DANCE.get()), leftX + (c4W + 6) * 3, cardY + 97, c4W, c4H, mouseX, mouseY);
            } else {
                render4StarCharacterCard(graphics, CompanionProfile.TINGYUN, leftX + (c4W + 6) * 3, cardY + 97, c4W, c4H, mouseX, mouseY);
            }

        } else if (isPromotionalCharacterBanner()) {
            int promoNumber = (activeBanner == BANNER_PROMO_2) ? 2 : 1;
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.promo_banner_header", promoNumber, promoPhase.featuredCharacter().displayName(), promoPhase.elementBadge()), leftX, cardY + 54, 0xFFFFD700, false);

            int toggleBtnW = Math.min(130, (leftW - 10) / 2);
            int toggleBtnH = 16;
            int toggleY = cardY + 66;
            int t1X = leftX;
            int t2X = leftX + toggleBtnW + 8;

            boolean hoverT1 = mouseX >= t1X && mouseX <= t1X + toggleBtnW && mouseY >= toggleY && mouseY <= toggleY + toggleBtnH;
            boolean hoverT2 = mouseX >= t2X && mouseX <= t2X + toggleBtnW && mouseY >= toggleY && mouseY <= toggleY + toggleBtnH;

            boolean sel1 = (activeBanner == 1);
            graphics.fill(t1X, toggleY, t1X + toggleBtnW, toggleY + toggleBtnH, sel1 ? 0xDD3D1414 : (hoverT1 ? 0x882A1010 : 0x551A0A0A));
            graphics.renderOutline(t1X, toggleY, toggleBtnW, toggleBtnH, sel1 ? 0xFFFF7043 : (hoverT1 ? 0xFFFFAA88 : 0x88773333));
            renderCenteredFittedText(graphics, (sel1 ? "§e▶ " : "") + "✦ " + promoPhase1.featuredCharacter().displayName() + " 5★", t1X + toggleBtnW / 2, toggleY + 4, toggleBtnW - 4, sel1 ? 0xFFFFD700 : 0xCCCCCC);

            boolean sel2 = (activeBanner == BANNER_PROMO_2);
            graphics.fill(t2X, toggleY, t2X + toggleBtnW, toggleY + toggleBtnH, sel2 ? 0xDD0D2A4A : (hoverT2 ? 0x880A1F38 : 0x55061526));
            graphics.renderOutline(t2X, toggleY, toggleBtnW, toggleBtnH, sel2 ? 0xFF4FC3F7 : (hoverT2 ? 0xFF81D4FA : 0x881E3A5F));
            renderCenteredFittedText(graphics, (sel2 ? "§b▶ " : "") + "✦ " + promoPhase2.featuredCharacter().displayName() + " 5★", t2X + toggleBtnW / 2, toggleY + 4, toggleBtnW - 4, sel2 ? 0xFF80D8FF : 0xCCCCCC);

            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.promo_rate_up_4star"), leftX, cardY + 84, 0xFFDA70D6, false);

            List<CompanionProfile> r4Chars = promoPhase.rateUp4StarCharacters();
            List<Supplier<ItemStack>> r4Weaps = promoPhase.rateUp4StarWeapons();
            int numC = r4Chars.size();
            int numW = r4Weaps.size();

            if (numC > 0) {
                int c4W = Math.max(48, (leftW - (numC - 1) * 5) / numC);
                int cX = leftX;
                for (CompanionProfile p : r4Chars) {
                    render4StarCharacterCard(graphics, p, cX, cardY + 95, c4W, 20, mouseX, mouseY);
                    cX += c4W + 5;
                }
            }

            if (numW > 0) {
                int w4W = Math.max(55, (leftW - (numW - 1) * 5) / numW);
                int wX = leftX;
                for (Supplier<ItemStack> wpSup : r4Weaps) {
                    ItemStack wp = wpSup.get();
                    render4StarWeaponCard(graphics, wp, wX, cardY + 117, w4W, 20, mouseX, mouseY);
                    wX += w4W + 5;
                }
            }

        } else if (activeBanner == 0) {
            ItemStack wp1 = promoPhase.primaryFeaturedWeapon().get();
            ItemStack wp2 = promoPhase.secondaryFeaturedWeapon() != null ? promoPhase.secondaryFeaturedWeapon().get() : ItemStack.EMPTY;

            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.weapon_rate_up_header"), leftX, cardY + 54, 0xFFFFD700, false);
            int halfW = (leftW - 6) / 2;
            renderBadge(graphics, "✦ " + wp1.getHoverName().getString() + " 5★", leftX, cardY + 66, halfW, 15, 0xFFFFAA, 0x33B8860B, 0xFFFFD700);
            if (!wp2.isEmpty()) {
                renderBadge(graphics, "✦ " + wp2.getHoverName().getString() + " 5★", leftX + halfW + 6, cardY + 66, halfW, 15, 0xFFFFAA, 0x33B8860B, 0xFFFFD700);
            }

            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.featured_4star_weapons"), leftX, cardY + 85, 0xFFDA70D6, false);
            int c4Count = 4;
            int c4W = Math.max(65, (leftW - (c4Count - 1) * 6) / c4Count);
            int c4H = 24;
            int cX = leftX;
            for (Supplier<ItemStack> wpSup : promoPhase.rateUp4StarWeapons()) {
                ItemStack wp = wpSup.get();
                render4StarWeaponCard(graphics, wp, cX, cardY + 97, c4W, c4H, mouseX, mouseY);
                cX += c4W + 6;
            }

        } else if (activeBanner == 3) {
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.novice_featured_chars"), leftX, cardY + 54, 0xFFFFE082, false);
            int halfW = (leftW - 6) / 2;
            renderBadge(graphics, Component.translatable("gui.awakening_system.gacha.novice_badge_noelle").getString(), leftX, cardY + 66, halfW, 15, 0xFFFFD700, 0x33B8860B, 0xFFFFD700);
            renderBadge(graphics, Component.translatable("gui.awakening_system.gacha.novice_badge_cha").getString(), leftX + halfW + 6, cardY + 66, halfW, 15, 0xFFFFF59D, 0x33F57F17, 0xFFFFE082);

            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.promo_pool_4star"), leftX, cardY + 85, 0xFFDA70D6, false);
            int c4Count = 4;
            int c4W = Math.max(65, (leftW - (c4Count - 1) * 6) / c4Count);
            int c4H = 24;
            render4StarCharacterCard(graphics, CompanionProfile.NOELLE, leftX, cardY + 97, c4W, c4H, mouseX, mouseY);
            render4StarCharacterCard(graphics, CompanionProfile.CHA_HAEIN, leftX + c4W + 6, cardY + 97, c4W, c4H, mouseX, mouseY);
            if (SystemItems.THE_CATCH.isBound()) {
                render4StarWeaponCard(graphics, new ItemStack(SystemItems.THE_CATCH.get()), leftX + (c4W + 6) * 2, cardY + 97, c4W, c4H, mouseX, mouseY);
            }
            if (SystemItems.RISING_DRAGON_PILL.isBound()) {
                render4StarWeaponCard(graphics, new ItemStack(SystemItems.RISING_DRAGON_PILL.get()), leftX + (c4W + 6) * 3, cardY + 97, c4W, c4H, mouseX, mouseY);
            }

        } else {
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.standard_arsenal_5star"), leftX, cardY + 54, 0xFF80D8FF, false);
            int chipW = (leftW - 12) / 3;
            String asrahanName = SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? "✦ " + SystemItems.ASRAHAN_HOLY_SWORD.get().getDescription().getString() : "✦ Asrahan";
            String balladName = SystemItems.DAWNS_BALLAD.isBound() ? "✦ " + SystemItems.DAWNS_BALLAD.get().getDescription().getString() : "✦ Dawn's Ballad";
            String scytheName = SystemItems.BLACK_DRAGON_SCYTHE.isBound() ? "✦ " + SystemItems.BLACK_DRAGON_SCYTHE.get().getDescription().getString() : "✦ Dragon Scythe";
            renderBadge(graphics, asrahanName, leftX, cardY + 66, chipW, 15, 0xFFFFAA, 0x331565C0, 0xFF64B5F6);
            renderBadge(graphics, balladName, leftX + chipW + 6, cardY + 66, chipW, 15, 0xFFFFAA, 0x331565C0, 0xFF64B5F6);
            renderBadge(graphics, scytheName, leftX + (chipW + 6) * 2, cardY + 66, chipW, 15, 0xFFFFAA, 0x331565C0, 0xFF64B5F6);

            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.standard_arsenal_4star"), leftX, cardY + 85, 0xFFDA70D6, false);
            int c4Count = 4;
            int c4W = Math.max(65, (leftW - (c4Count - 1) * 6) / c4Count);
            int c4H = 24;
            render4StarCharacterCard(graphics, CompanionProfile.NOELLE, leftX, cardY + 97, c4W, c4H, mouseX, mouseY);
            render4StarCharacterCard(graphics, CompanionProfile.CHA_HAEIN, leftX + c4W + 6, cardY + 97, c4W, c4H, mouseX, mouseY);
            if (SystemItems.SHADOW_DAGGERS.isBound()) {
                render4StarWeaponCard(graphics, new ItemStack(SystemItems.SHADOW_DAGGERS.get()), leftX + (c4W + 6) * 2, cardY + 97, c4W, c4H, mouseX, mouseY);
            }
            if (SystemItems.PLUM_BLOSSOM_SWORD.isBound()) {
                render4StarWeaponCard(graphics, new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()), leftX + (c4W + 6) * 3, cardY + 97, c4W, c4H, mouseX, mouseY);
            }
        }

        int cx = stageX + stageW / 2;
        int cy = stageY + stageH - 16;
        int radX = stageW / 2 - 8;
        int radY = 11;

        renderPedestal(graphics, cx, cy, radX, radY, pedestalGlow, cardBorder);

        long now = System.currentTimeMillis();
        for (int s = 0; s < 6; s++) {
            int seed = s * 73;
            float pProgress = ((now + seed * 120) % 2200) / 2200.0F;
            int px = cx - 30 + ((seed * 17) % 60);
            int py = cy - 4 - (int) (pProgress * (stageH - 24));
            int pAlpha = (int) (Math.sin(pProgress * Math.PI) * 180);
            if (pAlpha > 10) {
                graphics.fill(px, py, px + 2, py + 2, (pAlpha << 24) | (cardBorder & 0x00FFFFFF));
            }
        }

        if (activeBanner == BANNER_UMA) {
            com.soybe.awakeningsystem.uma.gacha.UmaGachaManager.BannerPhase phase = getSelectedUmaPhase();
            List<String> stageUmas = phase.getRateUp3Star();
            String showId = stageUmas.get((int) ((now / 4000L) % stageUmas.size()));
            com.soybe.awakeningsystem.uma.UmaProfile showcaseUma = com.soybe.awakeningsystem.uma.UmaProfile.byId(showId);
            com.soybe.awakeningsystem.uma.UmaRacerEntity dummyUma = getOrCreateDummyUma(showcaseUma != null ? showcaseUma : com.soybe.awakeningsystem.uma.UmaProfile.SPECIAL_WEEK);
            if (dummyUma != null) {
                int entScale = Math.max(26, Math.min(46, (stageH - 24) / 3));
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, stageX, stageY + 6, stageX + stageW, cy, entScale, 0.0625F,
                        (float) mouseX, (float) mouseY, dummyUma
                );
            }
            int plateW = Math.min(stageW - 6, 135);
            int plateX = cx - plateW / 2;
            int plateY = cy + 2;
            graphics.fill(plateX, plateY, plateX + plateW, plateY + 14, 0xCC000000);
            graphics.renderOutline(plateX, plateY, plateW, 14, 0xFF2ECC71);
            renderCenteredFittedText(graphics, "§a★★★ " + showcaseUma.getDisplayName(), cx, plateY + 3, plateW - 4, 0xFFFFFF);

        } else if (activeBanner == BANNER_STELLAR_WARP) {
            CompanionProfile[] hsrShowcase = {
                    CompanionProfile.ACHERON, CompanionProfile.FIREFLY,
                    CompanionProfile.DAN_HENG_IL, CompanionProfile.FEIXIAO,
                    CompanionProfile.AVENTURINE, CompanionProfile.KAFKA,
                    CompanionProfile.HERTA
            };
            CompanionProfile showcaseHsr = hsrShowcase[(int) ((now / 4000L) % hsrShowcase.length)];
            CompanionEntity dummy = getOrCreateDummyCompanion(showcaseHsr);
            if (dummy != null) {
                int entScale = Math.max(26, Math.min(46, (stageH - 24) / 3));
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, stageX, stageY + 6, stageX + stageW, cy, entScale, 0.0625F,
                        (float) mouseX, (float) mouseY, dummy
                );
            }
            int plateW = Math.min(stageW - 6, 135);
            int plateX = cx - plateW / 2;
            int plateY = cy + 2;
            graphics.fill(plateX, plateY, plateX + plateW, plateY + 14, 0xCC000000);
            graphics.renderOutline(plateX, plateY, plateW, 14, 0xFFBA68C8);
            renderCenteredFittedText(graphics, "§6★★★★★ " + showcaseHsr.displayName(), cx, plateY + 3, plateW - 4, 0xFFFFFF);

        } else if (isPromotionalCharacterBanner()) {
            CompanionProfile showcase = promoPhase.featuredCharacter();

            CompanionEntity dummy = getOrCreateDummyCompanion(showcase);
            if (dummy != null) {
                int entScale = Math.max(26, Math.min(46, (stageH - 24) / 3));
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, stageX, stageY + 6, stageX + stageW, cy, entScale, 0.0625F,
                        (float) mouseX, (float) mouseY, dummy
                );
            }
            int plateW = Math.min(stageW - 6, 135);
            int plateX = cx - plateW / 2;
            int plateY = cy + 2;
            int elemCol = getElementBgColor(showcase.element());
            graphics.fill(plateX, plateY, plateX + plateW, plateY + 14, 0xCC000000);
            graphics.renderOutline(plateX, plateY, plateW, 14, elemCol);
            renderCenteredFittedText(graphics, "§6★★★★★ " + showcase.displayName(), cx, plateY + 3, plateW - 4, 0xFFFFFF);

        } else if (activeBanner == 3) {
            CompanionEntity dummy = getOrCreateDummyCompanion(CompanionProfile.NOELLE);
            if (dummy != null) {
                int entScale = Math.max(26, Math.min(46, (stageH - 24) / 3));
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, stageX, stageY + 6, stageX + stageW, cy, entScale, 0.0625F,
                        (float) mouseX, (float) mouseY, dummy
                );
            }
            int plateW = Math.min(stageW - 6, 135);
            int plateX = cx - plateW / 2;
            int plateY = cy + 2;
            graphics.fill(plateX, plateY, plateX + plateW, plateY + 14, 0xCC000000);
            graphics.renderOutline(plateX, plateY, plateW, 14, 0xFFFFE082);
            renderCenteredFittedText(graphics, "§d★★★★ NOELLE (Favonius)", cx, plateY + 3, plateW - 4, 0xFFFFE082);

        } else if (activeBanner == 0) {
            ItemStack wpShowcase = (promoPhase.secondaryFeaturedWeapon() != null && ((now / 3000L) % 2 == 1))
                    ? promoPhase.secondaryFeaturedWeapon().get()
                    : promoPhase.primaryFeaturedWeapon().get();

            float bob = (float) Math.sin((now % 2200) / 2200.0 * Math.PI * 2) * 4.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(cx, stageY + stageH / 2 - 6 + (int) bob, 100);
            graphics.pose().scale(3.4F, 3.4F, 3.4F);
            graphics.renderItem(wpShowcase, -8, -8);
            graphics.pose().popPose();

            int plateW = Math.min(stageW - 6, 135);
            int plateX = cx - plateW / 2;
            int plateY = cy + 2;
            graphics.fill(plateX, plateY, plateX + plateW, plateY + 14, 0xCC000000);
            graphics.renderOutline(plateX, plateY, plateW, 14, cardBorder);
            renderCenteredFittedText(graphics, wpShowcase.getHoverName().getString(), cx, plateY + 3, plateW - 4, 0xFFFFD700);

        } else {
            List<ItemStack> previews = standardBannerPreviews;
            if (!previews.isEmpty()) {
                int pIdx = (int) ((now / 2800) % previews.size());
                ItemStack wpItem = previews.get(pIdx);
                float bob = (float) Math.sin((now % 2200) / 2200.0 * Math.PI * 2) * 4.0F;

                graphics.pose().pushPose();
                graphics.pose().translate(cx, stageY + stageH / 2 - 6 + (int) bob, 100);
                graphics.pose().scale(3.4F, 3.4F, 3.4F);
                graphics.renderItem(wpItem, -8, -8);
                graphics.pose().popPose();

                int plateW = Math.min(stageW - 6, 135);
                int plateX = cx - plateW / 2;
                int plateY = cy + 2;
                graphics.fill(plateX, plateY, plateX + plateW, plateY + 14, 0xCC000000);
                graphics.renderOutline(plateX, plateY, plateW, 14, cardBorder);
                renderCenteredFittedText(graphics, wpItem.getHoverName().getString(), cx, plateY + 3, plateW - 4, 0xFFFFD700);
            }
        }

        int barY = cardY + cardH - 20;
        int barX = cardX + 12;
        int barW = cardW - 24;
        int barH = 12;

        graphics.fill(barX, barY, barX + barW, barY + barH, 0xDD070B16);
        graphics.renderOutline(barX, barY, barW, barH, 0xFF2A3D66);

        if (activeBanner == BANNER_UMA) {
            float caratRatio = Math.min(1.0F, (float) carats / 1500.0F);
            int fillW = (int) (caratRatio * (barW - 4));
            if (fillW > 0) graphics.fill(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2, 0xFF2ECC71);
            String umaOddsLabel = Component.translatable("gui.awakening_system.gacha.uma_odds_label").getString();
            graphics.drawCenteredString(this.font, umaOddsLabel, barX + barW / 2, barY + 2, 0xFFFFFF);
        } else if (activeBanner == 3) {
            float noviceRatio = Math.min(1.0F, (float) beginnerPulls / 20.0F);
            int fillW = (int) (noviceRatio * (barW - 4));
            if (fillW > 0) graphics.fill(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2, 0xFF81C784);
            String novLabel = Component.translatable("gui.awakening_system.gacha.novice_pulls_label", beginnerPulls, (20 - beginnerPulls)).getString();
            graphics.drawCenteredString(this.font, novLabel, barX + barW / 2, barY + 2, 0xFFFFFF);
        } else if (activeBanner == BANNER_STELLAR_WARP) {
            float pityRatio = Math.min(1.0F, (float) pity5Star / 80.0F);
            int fillW = (int) (pityRatio * (barW - 4));
            if (fillW > 0) graphics.fill(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2, 0xFF9C27B0);
            String guarText = Component.translatable(guaranteed ? "gui.awakening_system.gacha.guaranteed_100" : "gui.awakening_system.gacha.fifty_fifty").getString();
            String hsrLabel = Component.translatable("gui.awakening_system.gacha.hsr_pity_label", pity5Star, pity4Star, guarText).getString();
            graphics.drawCenteredString(this.font, hsrLabel, barX + barW / 2, barY + 2, 0xFFFFFF);
        } else {
            float pity5Ratio = Math.min(1.0F, (float) pity5Star / 90.0F);
            int fillW = (int) (pity5Ratio * (barW - 4));
            int pityColor = (pity5Star >= 73) ? 0xCCFFB300 : 0xCC1E88E5;
            if (fillW > 0) graphics.fill(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2, pityColor);
            String guarText = Component.translatable(guaranteed ? "gui.awakening_system.gacha.guaranteed_100" : "gui.awakening_system.gacha.fifty_fifty").getString();
            String softPityNotice = pity5Star >= 73 ? Component.translatable("gui.awakening_system.gacha.soft_pity_notice").getString() : "";
            String pityLabel = Component.translatable("gui.awakening_system.gacha.pity_label", pity5Star, softPityNotice, pity4Star, guarText).getString();
            renderCenteredFittedText(graphics, pityLabel, barX + barW / 2, barY + 2, barW - 10, 0xFFFFFF);
        }

        if (VersionBannerManager.isUnderMaintenance(dayTime)) {
            int mBarH = 24;
            int mBarY = cardY + cardH / 2 - mBarH / 2;
            graphics.fill(cardX + 12, mBarY, cardX + cardW - 12, mBarY + mBarH, 0xEE1E0B0B);
            graphics.renderOutline(cardX + 12, mBarY, cardW - 24, mBarH, 0xFFFF5555);
            String mTitle = Component.translatable("gui.awakening_system.gacha.maint_title", VersionBannerManager.getMaintenanceRemainingFormatted(dayTime)).getString();
            String mSub = Component.translatable("gui.awakening_system.gacha.maint_desc").getString();
            graphics.drawCenteredString(this.font, mTitle, cardX + cardW / 2, mBarY + 3, 0xFFFFAA);
            graphics.drawCenteredString(this.font, mSub, cardX + cardW / 2, mBarY + 13, 0xFFFFFF);
        }

        int bottomY = getBottomY();

        boolean hoverHist = isHistBtnHover(mouseX, mouseY);
        int histX = cardX;
        int histW = 75;
        int histH = 26;
        graphics.fill(histX, bottomY, histX + histW, bottomY + histH, hoverHist ? 0xFF283B68 : 0xDD141F36);
        graphics.renderOutline(histX, bottomY, histW, histH, hoverHist ? 0xFF64B5F6 : 0xFF2A3D66);
        graphics.drawCenteredString(this.font, "✦ " + Component.translatable("awakening_system.gacha.btn.history").getString(), histX + histW / 2, bottomY + 8, hoverHist ? 0xFFFFFF : 0xBBDEFB);

        boolean hoverShop = isShopBtnHover(mouseX, mouseY);
        int shopX = cardX + 80;
        int shopW = 95;
        int shopH = 26;
        graphics.fill(shopX, bottomY, shopX + shopW, bottomY + shopH, hoverShop ? 0xFF4A1F52 : 0xDD28102E);
        graphics.renderOutline(shopX, bottomY, shopW, shopH, hoverShop ? 0xFFDA70D6 : 0xFF5C246B);
        renderCenteredFittedText(graphics, "✦ " + Component.translatable("awakening_system.gacha.btn.shop").getString(), shopX + shopW / 2, bottomY + 8, shopW - 6, hoverShop ? 0xFFFFFF : 0xE1BEE7);

        if (activeBanner == 0) {
            boolean hoverPath = isPathBtnHover(mouseX, mouseY);
            int pathX = cardX + 180;
            int pathW = 88;
            int pathH = 26;
            graphics.fill(pathX, bottomY, pathX + pathW, bottomY + pathH, hoverPath ? 0xFF5D4037 : 0xDD3E2723);
            graphics.renderOutline(pathX, bottomY, pathW, pathH, hoverPath ? 0xFFFFD700 : 0xFF8D6E63);
            String pathLabel = weaponBannerTarget > 0
                    ? ("✦ " + Component.translatable("awakening_system.gacha.btn.path_short").getString() + " (" + weaponBannerFatePoints + "/1)")
                    : ("✦ " + Component.translatable("awakening_system.gacha.btn.path").getString());
            renderCenteredFittedText(graphics, pathLabel, pathX + pathW / 2, bottomY + 8, pathW - 4, hoverPath ? 0xFFFFFF : 0xFFFFD700);
        }

        boolean hoverSubspace = isSubspaceBtnHover(mouseX, mouseY);
        int subspaceX = (activeBanner == 0) ? (cardX + 273) : (cardX + 180);
        int subspaceW = 85;
        int subspaceH = 26;
        graphics.fill(subspaceX, bottomY, subspaceX + subspaceW, bottomY + subspaceH, hoverSubspace ? 0xFF1B4F72 : 0xDD0E2E47);
        graphics.renderOutline(subspaceX, bottomY, subspaceW, subspaceH, hoverSubspace ? 0xFF5DADE2 : 0xFF2874A6);
        renderCenteredFittedText(graphics, "✦ " + Component.translatable("awakening_system.gacha.btn.subspace").getString(), subspaceX + subspaceW / 2, bottomY + 8, subspaceW - 4, hoverSubspace ? 0xFFFFFFFF : 0xFF85C1E9);

        ItemStack currentFate = (usesIntertwined && SystemItems.INTERTWINED_FATE.isBound())
                ? new ItemStack(SystemItems.INTERTWINED_FATE.get())
                : (SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : new ItemStack(Items.EMERALD));

        int wish10W = (activeBanner == 3) ? 145 : ((activeBanner == BANNER_UMA) ? 140 : 135);
        int wish10X = cardX + cardW - wish10W;
        int wish10H = 28;
        boolean hover10 = isWish10BtnHover(mouseX, mouseY);

        boolean underMaint = VersionBannerManager.isUnderMaintenance(dayTime);

        if (activeBanner == BANNER_UMA) {
            int ticketCount = getClientItemCount(SystemItems.GACHA_TICKET.isBound() ? SystemItems.GACHA_TICKET.get() : null)
                    + getClientItemCount(SystemItems.GOLDEN_GACHA_TICKET.isBound() ? SystemItems.GOLDEN_GACHA_TICKET.get() : null)
                    + intertwinedFates;
            ItemStack carrotStack = ticketCount > 0 && SystemItems.GACHA_TICKET.isBound()
                    ? new ItemStack(SystemItems.GACHA_TICKET.get())
                    : (SystemItems.CARAT.isBound() ? new ItemStack(SystemItems.CARAT.get()) : new ItemStack(Items.CARROT));

            int wish1W = 125;
            int wish1X = wish10X - 8 - wish1W;
            boolean hover1 = isWish1BtnHover(mouseX, mouseY);

            graphics.fillGradient(wish1X, bottomY - 1, wish1X + wish1W, bottomY - 1 + wish10H, hover1 ? 0xFF145A32 : 0xDD0E3A21, hover1 ? 0xFF0E3A21 : 0xDD092415);
            graphics.renderOutline(wish1X, bottomY - 1, wish1W, wish10H, hover1 ? 0xFFFFFFFF : 0xFF2ECC71);
            graphics.renderItem(carrotStack, wish1X + 6, bottomY + 5);
            if (underMaint) {
                graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.btn.under_maintenance").getString(), wish1X + 26, bottomY + 3, 0xFF888888, true);
                renderFittedText(graphics, "§c" + VersionBannerManager.getMaintenanceRemainingFormatted(dayTime), wish1X + 26, bottomY + 15, wish1W - 28, 0xFFFF5555, false);
            } else {
                graphics.drawString(this.font, Component.translatable("awakening_system.gacha.btn.wish_1").getString(), wish1X + 26, bottomY + 3, 0xFFFFFFFF, true);
                String cost1Text;
                int cost1Color;
                if (ticketCount >= 1) {
                    cost1Text = Component.translatable("gui.awakening_system.gacha.btn.cost_1_ticket").getString();
                    cost1Color = 0xFF80D8FF;
                } else if (carats >= 150) {
                    cost1Text = Component.translatable("gui.awakening_system.gacha.btn.cost_carats", 150).getString();
                    cost1Color = 0xFFFFD700;
                } else if (primogems >= 160) {
                    cost1Text = Component.translatable("gui.awakening_system.gacha.btn.cost_primos", 160).getString();
                    cost1Color = 0xFF80D8FF;
                } else {
                    cost1Text = Component.translatable("gui.awakening_system.gacha.btn.cost_short", "150 🥕 / 160 ✦").getString();
                    cost1Color = 0xFFE57373;
                }
                renderFittedText(graphics, cost1Text, wish1X + 26, bottomY + 15, wish1W - 28, cost1Color, false);
            }

            graphics.fillGradient(wish10X, bottomY - 1, wish10X + wish10W, bottomY - 1 + wish10H, hover10 ? 0xFFD4AC0D : 0xFFB7950B, hover10 ? 0xFFB7950B : 0xFF7D6608);
            graphics.renderOutline(wish10X, bottomY - 1, wish10W, wish10H, hover10 ? 0xFFFFFFFF : 0xFFFFD700);
            if (hover10) graphics.renderOutline(wish10X - 1, bottomY - 2, wish10W + 2, wish10H + 2, 0x66FFE082);
            graphics.renderItem(carrotStack, wish10X + 6, bottomY + 5);
            if (underMaint) {
                graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.btn.under_maintenance").getString(), wish10X + 26, bottomY + 3, 0xFF888888, true);
                renderFittedText(graphics, "§c" + VersionBannerManager.getMaintenanceRemainingFormatted(dayTime), wish10X + 26, bottomY + 15, wish10W - 28, 0xFFFF5555, false);
            } else {
                graphics.drawString(this.font, Component.translatable("awakening_system.gacha.btn.wish_10").getString(), wish10X + 26, bottomY + 3, 0xFFFFFFFF, true);
                String cost10Text;
                int cost10Color;
                if (ticketCount >= 10) {
                    cost10Text = Component.translatable("gui.awakening_system.gacha.btn.cost_10_tickets").getString();
                    cost10Color = 0xFF80D8FF;
                } else if (carats >= 1500) {
                    cost10Text = Component.translatable("gui.awakening_system.gacha.btn.cost_carats", "1,500").getString();
                    cost10Color = 0xFFFFD700;
                } else if (primogems >= 1600) {
                    cost10Text = Component.translatable("gui.awakening_system.gacha.btn.cost_primos", "1,600").getString();
                    cost10Color = 0xFF80D8FF;
                } else {
                    cost10Text = "§c1,500 🥕 / 1,600 ✦";
                    cost10Color = 0xFFE57373;
                }
                renderFittedText(graphics, cost10Text, wish10X + 26, bottomY + 15, wish10W - 28, cost10Color, false);
            }

            graphics.fill(wish10X + wish10W - 44, bottomY - 4, wish10X + wish10W, bottomY + 7, 0xFF2E7D32);
            graphics.renderOutline(wish10X + wish10W - 44, bottomY - 4, 44, 11, 0xFFFFD700);
            graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.btn.guar_2star"), wish10X + wish10W - 22, bottomY - 3, 0xFFFFFF);

        } else if (activeBanner == BANNER_STELLAR_WARP) {
            int passCount = getClientItemCount(SystemItems.STAR_RAIL_SPECIAL_PASS.isBound() ? SystemItems.STAR_RAIL_SPECIAL_PASS.get() : null) + intertwinedFates;
            ItemStack jadeStack = passCount > 0 && SystemItems.STAR_RAIL_SPECIAL_PASS.isBound()
                    ? new ItemStack(SystemItems.STAR_RAIL_SPECIAL_PASS.get())
                    : (SystemItems.STELLAR_JADE.isBound() ? new ItemStack(SystemItems.STELLAR_JADE.get()) : new ItemStack(Items.DIAMOND));
            int wish1W = 125;
            int wish1X = wish10X - 8 - wish1W;
            boolean hover1 = isWish1BtnHover(mouseX, mouseY);

            graphics.fillGradient(wish1X, bottomY - 1, wish1X + wish1W, bottomY - 1 + wish10H, hover1 ? 0xFF3A1C52 : 0xDD28103A, hover1 ? 0xFF28103A : 0xDD1B0929);
            graphics.renderOutline(wish1X, bottomY - 1, wish1W, wish10H, hover1 ? 0xFFFFFFFF : 0xFF9C27B0);
            graphics.renderItem(jadeStack, wish1X + 6, bottomY + 5);
            if (underMaint) {
                graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.btn.under_maintenance").getString(), wish1X + 26, bottomY + 3, 0xFF888888, true);
                renderFittedText(graphics, "§c" + VersionBannerManager.getMaintenanceRemainingFormatted(dayTime), wish1X + 26, bottomY + 15, wish1W - 28, 0xFFFF5555, false);
            } else {
                graphics.drawString(this.font, Component.translatable("awakening_system.gacha.btn.wish_1").getString(), wish1X + 26, bottomY + 3, 0xFFFFFFFF, true);
                String cost1Text;
                int cost1Color;
                if (passCount >= 1) {
                    cost1Text = Component.translatable("gui.awakening_system.gacha.btn.cost_1_pass").getString();
                    cost1Color = 0xFF80D8FF;
                } else if (stellarJade >= 160) {
                    cost1Text = "§d160 💠 Jade Estelar";
                    cost1Color = 0xFFCE93D8;
                } else if (primogems >= 160) {
                    cost1Text = Component.translatable("gui.awakening_system.gacha.btn.cost_primos", 160).getString();
                    cost1Color = 0xFF80D8FF;
                } else {
                    cost1Text = Component.translatable("gui.awakening_system.gacha.btn.cost_short", "160 💠 / ✦").getString();
                    cost1Color = 0xFFE57373;
                }
                renderFittedText(graphics, cost1Text, wish1X + 26, bottomY + 15, wish1W - 28, cost1Color, false);
            }

            graphics.fillGradient(wish10X, bottomY - 1, wish10X + wish10W, bottomY - 1 + wish10H, hover10 ? 0xFF5B1A78 : 0xFF421257, hover10 ? 0xFF421257 : 0xFF2C0A3B);
            graphics.renderOutline(wish10X, bottomY - 1, wish10W, wish10H, hover10 ? 0xFFFFFFFF : 0xFFFFD700);
            if (hover10) graphics.renderOutline(wish10X - 1, bottomY - 2, wish10W + 2, wish10H + 2, 0x66CE93D8);
            graphics.renderItem(jadeStack, wish10X + 6, bottomY + 5);
            if (underMaint) {
                graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.btn.under_maintenance").getString(), wish10X + 26, bottomY + 3, 0xFF888888, true);
                renderFittedText(graphics, "§c" + VersionBannerManager.getMaintenanceRemainingFormatted(dayTime), wish10X + 26, bottomY + 15, wish1W - 28, 0xFFFF5555, false);
            } else {
                graphics.drawString(this.font, Component.translatable("awakening_system.gacha.btn.wish_10").getString(), wish10X + 26, bottomY + 3, 0xFFFFFFFF, true);
                String cost10Text;
                int cost10Color;
                if (passCount >= 10) {
                    cost10Text = Component.translatable("gui.awakening_system.gacha.btn.cost_10_passes").getString();
                    cost10Color = 0xFF80D8FF;
                } else if (stellarJade >= 1600) {
                    cost10Text = "§d1,600 💠 Jade Estelar";
                    cost10Color = 0xFFCE93D8;
                } else if (primogems >= 1600) {
                    cost10Text = Component.translatable("gui.awakening_system.gacha.btn.cost_primos", "1,600").getString();
                    cost10Color = 0xFF80D8FF;
                } else {
                    cost10Text = "§c1,600 💠 / ✦";
                    cost10Color = 0xFFE57373;
                }
                renderFittedText(graphics, cost10Text, wish10X + 26, bottomY + 15, wish1W - 28, cost10Color, false);
            }

            graphics.fill(wish10X + wish10W - 44, bottomY - 4, wish10X + wish10W, bottomY + 7, 0xFF6A1B9A);
            graphics.renderOutline(wish10X + wish10W - 44, bottomY - 4, 44, 11, 0xFFFFD700);
            graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.btn.guar_4star"), wish10X + wish10W - 22, bottomY - 3, 0xFFFFFF);

        } else if (activeBanner == 3) {
            graphics.fillGradient(wish10X, bottomY - 1, wish10X + wish10W, bottomY - 1 + wish10H, hover10 ? 0xFFE5B036 : 0xFFB88220, hover10 ? 0xFFB88220 : 0xFF8A5C10);
            graphics.renderOutline(wish10X, bottomY - 1, wish10W, wish10H, hover10 ? 0xFFFFFFFF : 0xFFFFE082);
            if (hover10) graphics.renderOutline(wish10X - 1, bottomY - 2, wish10W + 2, wish10H + 2, 0x66FFE082);
            graphics.renderItem(currentFate, wish10X + 6, bottomY + 5);
            if (underMaint) {
                graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.btn.under_maintenance").getString(), wish10X + 26, bottomY + 3, 0xFF888888, true);
                renderFittedText(graphics, "§c" + VersionBannerManager.getMaintenanceRemainingFormatted(dayTime), wish10X + 26, bottomY + 15, wish10W - 28, 0xFFFF5555, false);
            } else {
                graphics.drawString(this.font, Component.translatable("awakening_system.gacha.btn.wish_10").getString(), wish10X + 26, bottomY + 3, 0xFFFFFFFF, true);
                boolean hasFates10 = acquaintFates >= 8;
                String cost10Text = hasFates10 ? Component.translatable("awakening_system.gacha.cost_fates", 8).getString() : Component.translatable("awakening_system.gacha.cost_primos", 1280).getString();
                renderFittedText(graphics, cost10Text, wish10X + 26, bottomY + 15, wish10W - 28, hasFates10 ? 0xFF80D8FF : 0xFFFFE082, false);
            }

            graphics.fill(wish10X + wish10W - 38, bottomY - 4, wish10X + wish10W, bottomY + 7, 0xFFD32F2F);
            graphics.renderOutline(wish10X + wish10W - 38, bottomY - 4, 38, 11, 0xFFFFD700);
            graphics.drawCenteredString(this.font, "§f§l-20%", wish10X + wish10W - 19, bottomY - 3, 0xFFFFFF);

        } else {
            int wish1W = 120;
            int wish1X = wish10X - 8 - wish1W;
            boolean hover1 = isWish1BtnHover(mouseX, mouseY);

            graphics.fillGradient(wish1X, bottomY - 1, wish1X + wish1W, bottomY - 1 + wish10H, hover1 ? 0xFF2A3D66 : 0xDD14203A, hover1 ? 0xFF1B2945 : 0xDD0D1526);
            graphics.renderOutline(wish1X, bottomY - 1, wish1W, wish10H, hover1 ? 0xFFFFFFFF : 0xFF354C75);
            graphics.renderItem(currentFate, wish1X + 6, bottomY + 5);
            if (underMaint) {
                graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.btn.under_maintenance").getString(), wish1X + 26, bottomY + 3, 0xFF888888, true);
                renderFittedText(graphics, "§c" + VersionBannerManager.getMaintenanceRemainingFormatted(dayTime), wish1X + 26, bottomY + 15, wish1W - 28, 0xFFFF5555, false);
            } else {
                graphics.drawString(this.font, Component.translatable("awakening_system.gacha.btn.wish_1").getString(), wish1X + 26, bottomY + 3, 0xFFFFFFFF, true);
                boolean hasFates1 = (usesIntertwined ? intertwinedFates : acquaintFates) >= 1;
                String cost1Text = hasFates1 ? Component.translatable("awakening_system.gacha.cost_fates", 1).getString() : Component.translatable("awakening_system.gacha.cost_primos", 160).getString();
                renderFittedText(graphics, cost1Text, wish1X + 26, bottomY + 15, wish1W - 28, hasFates1 ? 0xFF80D8FF : 0xFFFFE082, false);
            }

            graphics.fillGradient(wish10X, bottomY - 1, wish10X + wish10W, bottomY - 1 + wish10H, hover10 ? 0xFFE5B036 : 0xFFB88220, hover10 ? 0xFFB88220 : 0xFF8A5C10);
            graphics.renderOutline(wish10X, bottomY - 1, wish10W, wish10H, hover10 ? 0xFFFFFFFF : 0xFFFFE082);
            if (hover10) graphics.renderOutline(wish10X - 1, bottomY - 2, wish10W + 2, wish10H + 2, 0x66FFE082);
            graphics.renderItem(currentFate, wish10X + 6, bottomY + 5);
            if (underMaint) {
                graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.btn.under_maintenance").getString(), wish10X + 26, bottomY + 3, 0xFF888888, true);
                renderFittedText(graphics, "§c" + VersionBannerManager.getMaintenanceRemainingFormatted(dayTime), wish10X + 26, bottomY + 15, wish1W - 28, 0xFFFF5555, false);
            } else {
                graphics.drawString(this.font, Component.translatable("awakening_system.gacha.btn.wish_10").getString(), wish10X + 26, bottomY + 3, 0xFFFFFFFF, true);
                boolean hasFates10 = (usesIntertwined ? intertwinedFates : acquaintFates) >= 10;
                String cost10Text = hasFates10 ? Component.translatable("awakening_system.gacha.cost_fates", 10).getString() : Component.translatable("awakening_system.gacha.cost_primos", 1600).getString();
                renderFittedText(graphics, cost10Text, wish10X + 26, bottomY + 15, wish1W - 28, hasFates10 ? 0xFF80D8FF : 0xFFFFE082, false);
            }
        }

        int leftEdgeBtns = (activeBanner == 0) ? (cardX + 273 + 85) : (cardX + 180 + 85);
        int wishLeftEdge = (activeBanner == 3) ? wish10X : (wish10X - 8 - ((activeBanner == BANNER_UMA) ? 125 : 120));
        int paimonX = leftEdgeBtns + 6;
        int paimonW = wishLeftEdge - paimonX - 6;
        if (paimonW > 50) {
            String speaker = (activeBanner == BANNER_UMA) ? "§a§l✦ Tazuna:" : ((activeBanner == BANNER_STELLAR_WARP) ? "§d§l✦ Pom-Pom:" : "§d§l✦ Paimon:");
            int speakerCol = (activeBanner == BANNER_UMA) ? 0xFF2ECC71 : ((activeBanner == BANNER_STELLAR_WARP) ? 0xFFBA68C8 : 0xFFDA70D6);
            int bubbleBorder = (activeBanner == BANNER_UMA) ? 0xFF2ECC71 : ((activeBanner == BANNER_STELLAR_WARP) ? 0xFF9C27B0 : 0xFFFFD700);
            graphics.fill(paimonX, bottomY, paimonX + paimonW, bottomY + 26, 0xDD140D26);
            graphics.renderOutline(paimonX, bottomY, paimonW, 26, bubbleBorder);
            graphics.drawString(this.font, speaker, paimonX + 6, bottomY + 4, speakerCol, false);
            String shortAdvice;
            if (activeBanner == BANNER_UMA) {
                shortAdvice = carats >= 150 ? Component.translatable("gui.awakening_system.gacha.slogan.uma_ready").getString() : Component.translatable("gui.awakening_system.gacha.slogan.uma_short").getString();
            } else if (activeBanner == BANNER_STELLAR_WARP) {
                shortAdvice = stellarJade >= 160 ? Component.translatable("gui.awakening_system.gacha.slogan.hsr_ready").getString() : Component.translatable("gui.awakening_system.gacha.slogan.hsr_short").getString();
            } else if (activeBanner == 0) {
                shortAdvice = weaponBannerTarget > 0 ? ("§eSenda fijada (" + weaponBannerFatePoints + "/1)") : Component.translatable("gui.awakening_system.gacha.slogan.chart_course").getString();
            } else if (isPromotionalCharacterBanner()) {
                shortAdvice = guaranteed ? Component.translatable("gui.awakening_system.gacha.slogan.guaranteed_5star").getString() : Component.translatable("gui.awakening_system.gacha.slogan.fifty_fifty").getString();
            } else if (activeBanner == 3) {
                shortAdvice = Component.translatable("gui.awakening_system.gacha.slogan.novice_discount").getString();
            } else {
                shortAdvice = Component.translatable("gui.awakening_system.gacha.slogan.permanent_arsenal").getString();
            }
            renderFittedText(graphics, shortAdvice, paimonX + 6, bottomY + 14, paimonW - 12, 0xFFFFFF, false);

            if (mouseX >= paimonX && mouseX <= paimonX + paimonW && mouseY >= bottomY && mouseY <= bottomY + 26) {
                String fullMsg;
                if (activeBanner == BANNER_UMA) {
                    fullMsg = "§a[Tazuna] §f\"¡Bienvenido a la Academia Tracen! Invoca a tus corredoras con Carats y prepárate para la gloria en el Hipódromo.\"";
                } else if (activeBanner == BANNER_STELLAR_WARP) {
                    fullMsg = "§d[Pom-Pom] §f\"¡Pasajeros a bordo! Usa tu Jade Estelar para invocar a las leyendas del Expreso Astral (Acheron, Firefly, Sparkle y más).\"";
                } else if (activeBanner == 0) {
                    fullMsg = "§d[Paimon] §f\"¡En este banner tienes un 75% de probabilidad de armas promocionales! Usa la Senda Divina para asegurar tu favorita.\"";
                } else if (isPromotionalCharacterBanner()) {
                    fullMsg = "§d[Paimon] §f\"¡El evento de " + promoPhase.featuredCharacter().displayName() + " (" + activeFranchise.displayName() + ") dura " + promoTimeLeft + "! Si pierdes el 50/50, el siguiente 5★ será 100% garantizado.\"";
                } else if (activeBanner == 3) {
                    fullMsg = "§d[Paimon] §f\"¡Aprovecha el 20% de descuento en el Gacha de Principiantes! Noelle te protegerá en tus aventuras.\"";
                } else {
                    fullMsg = "§d[Paimon] §f\"¡El banner permanente contiene todas las armas legendarias y compañeros estándar!\"";
                }
                graphics.renderTooltip(this.font, Component.literal(fullMsg), (int) mouseX, (int) mouseY);
            }
        }

        if (mouseY >= pillY && mouseY <= pillY + pillH) {
            if (activeBanner == BANNER_UMA) {
                if (mouseX >= pillCaratX && mouseX <= pillCaratX + pillCaratW) {
                    graphics.renderTooltip(this.font, Component.literal("§6🥕 Tracen Carats: §f" + String.format(java.util.Locale.US, "%,d", carats) + " §7(Click [+] to recharge)"), (int) mouseX, (int) mouseY);
                } else if (mouseX >= pillCoinX && mouseX <= pillCoinX + pillCoinW) {
                    graphics.renderTooltip(this.font, Component.literal("§e🪙 System Coins: §f" + String.format(java.util.Locale.US, "%,d", coins)), (int) mouseX, (int) mouseY);
                }
            } else if (activeBanner == BANNER_STELLAR_WARP) {
                String sJade = String.format(java.util.Locale.US, "%,d", stellarJade);
                int pillJadeW = 18 + this.font.width(sJade) + 18;
                int pillJadeX = getVirtW() - pillJadeW - 10;
                int pillPrimoX = pillJadeX - pill2W - 6;
                if (mouseX >= pillJadeX && mouseX <= pillJadeX + pillJadeW) {
                    graphics.renderTooltip(this.font, Component.literal("§d💠 Stellar Jade: §f" + sJade + " §7(Click [+] to recharge)"), (int) mouseX, (int) mouseY);
                } else if (mouseX >= pillPrimoX && mouseX <= pillPrimoX + pill2W) {
                    String primoName = Component.translatable("awakening_system.gacha.currency.primogems").getString();
                    graphics.renderTooltip(this.font, Component.literal("§6✦ " + primoName + ": §f" + String.format(java.util.Locale.US, "%,d", primogems)), (int) mouseX, (int) mouseY);
                }
            } else {
                int currentFateCount = usesIntertwined ? intertwinedFates : acquaintFates;
                if (mouseX >= pill1X && mouseX <= pill1X + pill1W) {
                    String fateName = usesIntertwined
                            ? (SystemItems.INTERTWINED_FATE.isBound() ? Component.translatable(SystemItems.INTERTWINED_FATE.get().getDescriptionId()).getString() : "Intertwined Fate")
                            : (SystemItems.ACQUAINT_FATE.isBound() ? Component.translatable(SystemItems.ACQUAINT_FATE.get().getDescriptionId()).getString() : "Acquaint Fate");
                    graphics.renderTooltip(this.font, Component.literal("§e✦ " + fateName + ": §f" + currentFateCount), (int) mouseX, (int) mouseY);
                } else if (mouseX >= pill2X && mouseX <= pill2X + pill2W) {
                    String primoName = Component.translatable("awakening_system.gacha.currency.primogems").getString();
                    graphics.renderTooltip(this.font, Component.literal("§6✦ " + primoName + ": §f" + String.format(java.util.Locale.US, "%,d", primogems)), (int) mouseX, (int) mouseY);
                } else if (mouseX >= pill3X && mouseX <= pill3X + pill3W && pill3X > totalSubModeW + 8) {
                    if (mouseX < glitterX) {
                        String sdName = SystemItems.MASTERLESS_STARDUST.isBound() ? Component.translatable(SystemItems.MASTERLESS_STARDUST.get().getDescriptionId()).getString() : "Masterless Stardust";
                        graphics.renderTooltip(this.font, Component.literal("§b✦ " + sdName + ": §f" + String.format(java.util.Locale.US, "%,d", stardust)), (int) mouseX, (int) mouseY);
                    } else {
                        String sgName = SystemItems.MASTERLESS_STARGLITTER.isBound() ? Component.translatable(SystemItems.MASTERLESS_STARGLITTER.get().getDescriptionId()).getString() : "Masterless Starglitter";
                        graphics.renderTooltip(this.font, Component.literal("§6✦ " + sgName + ": §f" + String.format(java.util.Locale.US, "%,d", starglitter)), (int) mouseX, (int) mouseY);
                    }
                } else if (mouseX >= pill4X && mouseX <= pill4X + pill4W && pill4X > totalSubModeW + 8) {
                    String moraName = SystemItems.MORA.isBound() ? Component.translatable(SystemItems.MORA.get().getDescriptionId()).getString() : "Mora";
                    graphics.renderTooltip(this.font, Component.literal("§6🪙 " + moraName + ": §f" + String.format(java.util.Locale.US, "%,d", mora)), (int) mouseX, (int) mouseY);
                }
            }
        }
    }

    private void renderHistoryModal(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, getVirtW(), getVirtH(), 0xEE05070E);

        int mW = 460;
        int mH = 260;
        int mX = getVirtW() / 2 - mW / 2;
        int mY = getVirtH() / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF10172A);
        graphics.renderOutline(mX, mY, mW, mH, 0xFF3B82F6);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x33FFFFFF);

        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.title"), mX + 14, mY + 12, 0xFFFFD700, false);

        int closeX = mX + mW - 22;
        int closeY = mY + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        int statY = mY + 30;
        int statH = 50;
        graphics.fill(mX + 12, statY, mX + mW - 12, statY + statH, 0xFF1A243D);
        graphics.renderOutline(mX + 12, statY, mW - 24, statH, 0xFF2A3A5E);

        String softPityNotice = pity5Star >= 73 ? Component.translatable("gui.awakening_system.gacha.soft_pity_notice").getString() : "";
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.pity5", pity5Star, softPityNotice), mX + 18, statY + 5, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.pity4", pity4Star), mX + 250, statY + 5, 0xFFFFFF, false);
        String guaranteeStatus = guaranteed ? Component.translatable("gui.awakening_system.gacha.guaranteed_100").getString() : Component.translatable("gui.awakening_system.gacha.fifty_fifty").getString();
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.guarantee", guaranteeStatus), mX + 18, statY + 19, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.beginner", beginnerPulls), mX + 250, statY + 19, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.total_wishes", totalWishes), mX + 18, statY + 33, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.primos_spent", String.format(java.util.Locale.US, "%,d", (long) totalWishes * 160)), mX + 250, statY + 33, 0xFFFFFF, false);

        int tableY = statY + 58;
        int totalWishesCount = wishHistory.size();
        int visibleRows = 7;
        int totalPages = Math.max(1, (int) Math.ceil((double) totalWishesCount / (double) visibleRows));
        if (historyPage >= totalPages) historyPage = totalPages - 1;
        if (historyPage < 0) historyPage = 0;

        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.history.table_header", (historyPage + 1), totalPages, totalWishesCount), mX + 14, tableY, 0xAAAAAA, false);

        int rowY = tableY + 14;
        if (wishHistory.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.history.empty").getString(), mX + mW / 2, rowY + 30, 0x888888);
        } else {
            int startIndex = historyPage * visibleRows;
            int endIndex = Math.min(startIndex + visibleRows, wishHistory.size());
            for (int i = startIndex; i < endIndex; i++) {
                int displayIndex = i - startIndex;
                String raw = wishHistory.get(i);
                String[] parts = raw.split("::");
                String name = parts.length > 0 ? parts[0] : Component.translatable("gui.awakening_system.gacha.history.unknown_item").getString();
                int stars = 3;
                try {
                    stars = parts.length > 1 ? Integer.parseInt(parts[1]) : 3;
                } catch (NumberFormatException e) {
                    AwakeningSystemMod.LOGGER.debug("Invalid star count format in history row: {}", raw, e);
                }
                String banner = parts.length > 2 ? parts[2] : Component.translatable("gui.awakening_system.gacha.history.wish_banner_label").getString();

                int rowColor = (displayIndex % 2 == 0) ? 0x221E293B : 0x111E293B;
                graphics.fill(mX + 12, rowY + displayIndex * 18, mX + mW - 12, rowY + (displayIndex + 1) * 18, rowColor);

                String starPrefix = stars == 5 ? "§6⭐⭐⭐⭐⭐" : (stars == 4 ? "§d⭐⭐⭐⭐" : "§b⭐⭐⭐");
                graphics.drawString(this.font, starPrefix, mX + 18, rowY + displayIndex * 18 + 5, 0xFFFFFF, false);
                renderFittedText(graphics, name, mX + 105, rowY + displayIndex * 18 + 5, 200, stars == 5 ? 0xFFFFD700 : (stars == 4 ? 0xFFDA70D6 : 0xFFBBDEFB), false);
                graphics.drawString(this.font, "§8[" + banner + "]", mX + 315, rowY + displayIndex * 18 + 5, 0x888888, false);
            }

            int btnPageY = mY + mH - 24;
            int btnPrevX = mX + 14;
            int btnPrevW = 85;
            int btnNextX = mX + mW - 14 - 85;
            int btnNextW = 85;
            int btnH = 16;

            boolean hoverPrev = historyPage > 0 && mouseX >= btnPrevX && mouseX <= btnPrevX + btnPrevW && mouseY >= btnPageY && mouseY <= btnPageY + btnH;
            boolean hoverNext = historyPage < totalPages - 1 && mouseX >= btnNextX && mouseX <= btnNextX + btnNextW && mouseY >= btnPageY && mouseY <= btnPageY + btnH;

            int prevBg = historyPage > 0 ? (hoverPrev ? 0xFF2563EB : 0xFF1D4ED8) : 0xFF1E293B;
            graphics.fill(btnPrevX, btnPageY, btnPrevX + btnPrevW, btnPageY + btnH, prevBg);
            graphics.renderOutline(btnPrevX, btnPageY, btnPrevW, btnH, historyPage > 0 ? (hoverPrev ? 0xFF60A5FA : 0xFF3B82F6) : 0xFF334155);
            String prevText = historyPage > 0 ? Component.translatable("gui.awakening_system.gacha.history.btn_prev").getString() : "§7" + Component.translatable("gui.awakening_system.gacha.history.btn_prev").getString();
            graphics.drawCenteredString(this.font, prevText, btnPrevX + btnPrevW / 2, btnPageY + 4, historyPage > 0 ? 0xFFFFFF : 0x777777);

            graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.history.page_fmt", (historyPage + 1), totalPages).getString(), mX + mW / 2, btnPageY + 4, 0xFFE2E8F0);

            int nextBg = historyPage < totalPages - 1 ? (hoverNext ? 0xFF2563EB : 0xFF1D4ED8) : 0xFF1E293B;
            graphics.fill(btnNextX, btnPageY, btnNextX + btnNextW, btnPageY + btnH, nextBg);
            graphics.renderOutline(btnNextX, btnPageY, btnNextW, btnH, historyPage < totalPages - 1 ? (hoverNext ? 0xFF60A5FA : 0xFF3B82F6) : 0xFF334155);
            String nextText = historyPage < totalPages - 1 ? Component.translatable("gui.awakening_system.gacha.history.btn_next").getString() : "§7" + Component.translatable("gui.awakening_system.gacha.history.btn_next").getString();
            graphics.drawCenteredString(this.font, nextText, btnNextX + btnNextW / 2, btnPageY + 4, historyPage < totalPages - 1 ? 0xFFFFFF : 0x777777);
        }
    }

    private void renderTopUpModal(GuiGraphics graphics, int mouseX, int mouseY) {
        syncDataFromPlayer();
        graphics.fill(0, 0, getVirtW(), getVirtH(), 0xEE05070E);

        int virtW = getVirtW();
        int virtH = getVirtH();
        int mW = Math.min(520, virtW - 16);
        int mH = Math.min(270, virtH - 16);
        int mX = virtW / 2 - mW / 2;
        int mY = virtH / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF141926);
        graphics.renderOutline(mX, mY, mW, mH, 0xFFFFD700);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x33FFFFFF);

        int backBtnX = mX + 10;
        int backBtnY = mY + 8;
        int backBtnW = 60;
        int backBtnH = 14;
        boolean hoverBack = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH;
        graphics.fill(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + backBtnH, hoverBack ? 0xFF3D2706 : 0xAA101624);
        graphics.renderOutline(backBtnX, backBtnY, backBtnW, backBtnH, hoverBack ? 0xFFFFD700 : 0xFF334155);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.back").getString(), backBtnX + backBtnW / 2, backBtnY + 3, backBtnW - 4, hoverBack ? 0xFFFFE082 : 0xCBD5E1);

        int closeX = mX + mW - 22;
        int closeY = mY + 8;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        int tabGap = 6;
        int tabW = Math.max(100, (mW - 120) / 3);
        int tabH = 14;
        int tab0X = mX + 76;
        int tab1X = tab0X + tabW + tabGap;
        int tab2X = tab1X + tabW + tabGap;
        int tabY = mY + 8;

        boolean hoverTab0 = mouseX >= tab0X && mouseX <= tab0X + tabW && mouseY >= tabY && mouseY <= tabY + tabH;
        boolean hoverTab1 = mouseX >= tab1X && mouseX <= tab1X + tabW && mouseY >= tabY && mouseY <= tabY + tabH;
        boolean hoverTab2 = mouseX >= tab2X && mouseX <= tab2X + tabW && mouseY >= tabY && mouseY <= tabY + tabH;

        graphics.fill(tab0X, tabY, tab0X + tabW, tabY + tabH, (topUpTab == 0) ? 0xFF0D47A1 : (hoverTab0 ? 0xDD1B2538 : 0x88121A28));
        graphics.renderOutline(tab0X, tabY, tabW, tabH, (topUpTab == 0) ? 0xFF64B5F6 : 0xFF334155);
        renderCenteredFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.tab_crystals").getString(), tab0X + tabW / 2, tabY + 3, tabW - 4, (topUpTab == 0) ? 0xFFFFFFFF : 0x90A4AE);

        graphics.fill(tab1X, tabY, tab1X + tabW, tabY + tabH, (topUpTab == 1) ? 0xFF4A148C : (hoverTab1 ? 0xDD1B2538 : 0x88121A28));
        graphics.renderOutline(tab1X, tabY, tabW, tabH, (topUpTab == 1) ? 0xFFCE93D8 : 0xFF334155);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.stellar_tab").getString(), tab1X + tabW / 2, tabY + 3, tabW - 4, (topUpTab == 1) ? 0xFFFFFFFF : 0x90A4AE);

        graphics.fill(tab2X, tabY, tab2X + tabW, tabY + tabH, (topUpTab == 2) ? 0xFF1B5E20 : (hoverTab2 ? 0xDD1B2538 : 0x88121A28));
        graphics.renderOutline(tab2X, tabY, tabW, tabH, (topUpTab == 2) ? 0xFF2ECC71 : 0xFF334155);
        renderCenteredFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.tab_uma").getString(), tab2X + tabW / 2, tabY + 3, tabW - 4, (topUpTab == 2) ? 0xFFFFFFFF : 0x90A4AE);

        boolean isWelkinEnh = false;
        if (Minecraft.getInstance().player != null) {
            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (d != null) isWelkinEnh = d.isWelkinEnhanced();
        }
        String balStr;
        if (topUpTab == 2) {
            balStr = Component.translatable("gui.awakening_system.gacha.topup.bal_tab2",
                    String.format(java.util.Locale.US, "%,d", carats),
                    String.format(java.util.Locale.US, "%,d", coins),
                    String.format(java.util.Locale.US, "%.2f", usdBalance),
                    genesisCrystals).getString();
        } else if (topUpTab == 1) {
            String expressPart = (welkinDays > 0 ? Component.translatable("gui.awakening_system.gacha.topup.express_days", welkinDays).getString() : "");
            balStr = Component.translatable("gui.awakening_system.gacha.topup.bal_tab1",
                    String.format(java.util.Locale.US, "%,d", stellarJade),
                    String.format(java.util.Locale.US, "%,d", coins),
                    String.format(java.util.Locale.US, "%.2f", usdBalance),
                    genesisCrystals,
                    expressPart).getString();
        } else {
            String welkinPart = (welkinDays > 0 ? Component.translatable("gui.awakening_system.gacha.topup.welkin_days", welkinDays, (isWelkinEnh ? " §d(+50%)" : "")).getString() : "");
            balStr = Component.translatable("gui.awakening_system.gacha.topup.bal_tab0",
                    String.format(java.util.Locale.US, "%,d", coins),
                    String.format(java.util.Locale.US, "%.2f", usdBalance),
                    genesisCrystals,
                    stellarJade,
                    welkinPart).getString();
        }
        renderCenteredFittedText(graphics, balStr, mX + mW / 2, mY + 24, mW - 24, 0xFFE0E0E0);

        int colGap = 10;
        int colW = (mW - 20 - colGap) / 2;
        int leftX = mX + 10;
        int rightX = leftX + colW + colGap;
        int panelY = mY + 38;
        int convH = 22;
        int convY = mY + mH - convH - 8;
        int panelH = convY - panelY - 6;

        if (topUpTab == 1) {
            graphics.fill(leftX, panelY, leftX + colW, panelY + panelH, 0xAA0D121D);
            graphics.renderOutline(leftX, panelY, colW, panelH, 0xFF4A148C);
            renderCenteredFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.coin_packs_jade").getString(), leftX + colW / 2, panelY + 6, colW - 12, 0xFFFFD700);

            int[] jadeAmounts = {160, 1600, 4800, 16000};
            int[] jadeCoins = {10000, 75000, 200000, 500000};
            String[] jadeNotes = {
                    Component.translatable("gui.awakening_system.gacha.topup.warp_n", 1).getString(),
                    Component.translatable("gui.awakening_system.gacha.topup.warp_n", 10).getString(),
                    Component.translatable("gui.awakening_system.gacha.topup.warp_n", 30).getString(),
                    Component.translatable("gui.awakening_system.gacha.topup.warp_n", 100).getString()
            };

            int jR_X = leftX + 4;
            int jR_W = colW - 8;
            int jBtnW = Math.min(52, Math.max(40, jR_W / 5));
            int jBtnH = 16;
            int jBtnX = jR_X + jR_W - jBtnW - 4;

            ItemStack jadeStack = SystemItems.STELLAR_JADE.isBound() ? new ItemStack(SystemItems.STELLAR_JADE.get()) : new ItemStack(net.minecraft.world.item.Items.DIAMOND);

            for (int i = 0; i < 4; i++) {
                int rowY = panelY + 22 + i * 36;
                boolean hoverRow = mouseX >= jR_X && mouseX <= jR_X + jR_W && mouseY >= rowY && mouseY <= rowY + 32;
                graphics.fill(jR_X, rowY, jR_X + jR_W, rowY + 32, hoverRow ? 0xDD1B2538 : 0x88121A28);
                graphics.renderOutline(jR_X, rowY, jR_W, 32, 0xFF9C27B0);

                graphics.renderItem(jadeStack, jR_X + 4, rowY + 8);

                int textX = jR_X + 24;
                int textMaxW = jBtnX - textX - 4;
                renderFittedText(graphics, "§d§l" + String.format(java.util.Locale.US, "%,d", jadeAmounts[i]) + " 💠 §7" + jadeNotes[i], textX, rowY + 5, textMaxW, 0xFFCE93D8, false);
                renderFittedText(graphics, "§e" + String.format(java.util.Locale.US, "%,d", jadeCoins[i]) + " 🪙 Coins", textX, rowY + 17, textMaxW, 0xFFE0E0E0, false);

                int btnY = rowY + 8;
                boolean canBuy = coins >= jadeCoins[i];
                boolean hoverBtn = mouseX >= jBtnX && mouseX <= jBtnX + jBtnW && mouseY >= btnY && mouseY <= btnY + jBtnH;
                graphics.fill(jBtnX, btnY, jBtnX + jBtnW, btnY + jBtnH, canBuy ? (hoverBtn ? 0xFF6A1B9A : 0xFF4A148C) : 0xFF37474F);
                graphics.renderOutline(jBtnX, btnY, jBtnW, jBtnH, canBuy ? (hoverBtn ? 0xFFCE93D8 : 0xFFAB47BC) : 0xFF546E7A);
                renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.buy").getString(), jBtnX + jBtnW / 2, btnY + 4, jBtnW - 4, canBuy ? 0xFFFFFF : 0x90A4AE);
            }

            graphics.fill(rightX, panelY, rightX + colW, panelY + panelH, 0xAA0D121D);
            graphics.renderOutline(rightX, panelY, colW, panelH, 0xFF6A1B9A);
            renderCenteredFittedText(graphics, "💠 USD BALANCE PACKS 💳", rightX + colW / 2, panelY + 6, colW - 12, 0xFFCE93D8);

            String[] packJadeNames = {"60", "330", "1,090", "2,240", "3,880", "8,080", "🚂"};
            String[] packPrices = {"$0.99", "$4.99", "$14.99", "$29.99", "$49.99", "$99.99", "$4.99"};
            double[] packUsd = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};

            int crR_X = rightX + 4;
            int crR_W = colW - 8;
            int crBtnW = Math.min(56, Math.max(44, (crR_W - 80) / 3));
            int crBtnH = 15;
            int crBtnUsdX = crR_X + crR_W - crBtnW - 4;

            List<Component> jadeHoverTooltip = null;

            for (int i = 0; i < 7; i++) {
                int rowY = panelY + 18 + i * 23;
                boolean hoverRow = mouseX >= crR_X && mouseX <= crR_X + crR_W && mouseY >= rowY && mouseY <= rowY + 21;
                graphics.fill(crR_X, rowY, crR_X + crR_W, rowY + 21, hoverRow ? 0xDD1B2538 : (i == 6 ? 0x66311B92 : 0x66121A28));
                graphics.renderOutline(crR_X, rowY, crR_W, 21, i == 6 ? 0xFFBA68C8 : 0xFF2A3950);

                boolean isFirstBonus = (i < 6) && ((firstJadeTopUpMask & (1 << i)) == 0);

                String packDisplayName = (i == 6)
                        ? "Express Supply Pass"
                        : packJadeNames[i] + " Stellar Jade";

                int packTextX = crR_X + 4;
                int packTextMaxW = crBtnUsdX - packTextX - 4;
                String bonusPrefix = isFirstBonus ? "§6§l✦2X " : (i == 6 ? "§d" : "§b");
                renderFittedText(graphics, bonusPrefix + packDisplayName, packTextX, rowY + 6, packTextMaxW, 0xFFFFFF, false);

                if (hoverRow && isFirstBonus) {
                    int[] baseJade = {60, 330, 1090, 2240, 3880, 8080};
                    int dbl = baseJade[i] * 2;
                    jadeHoverTooltip = List.of(
                            Component.translatable("gui.awakening_system.gacha.first_topup_bonus_title"),
                            Component.translatable("gui.awakening_system.gacha.first_topup_bonus_desc"),
                            Component.translatable("gui.awakening_system.gacha.first_topup_bonus_amount", String.format(java.util.Locale.US, "%,d", dbl))
                    );
                } else if (hoverRow && i == 6) {
                    jadeHoverTooltip = List.of(
                            Component.translatable("gui.awakening_system.gacha.welkin_pass_title"),
                            Component.translatable("gui.awakening_system.gacha.welkin_pass_desc"),
                            Component.translatable("gui.awakening_system.gacha.welkin_pass_total")
                    );
                }

                int btnY = rowY + 3;
                boolean canUsd = usdBalance >= packUsd[i] - 0.001;
                boolean hoverUsd = mouseX >= crBtnUsdX && mouseX <= crBtnUsdX + crBtnW && mouseY >= btnY && mouseY <= btnY + crBtnH;
                graphics.fill(crBtnUsdX, btnY, crBtnUsdX + crBtnW, btnY + crBtnH, canUsd ? (hoverUsd ? 0xFF6A1B9A : 0xFF4A148C) : 0xFF263238);
                graphics.renderOutline(crBtnUsdX, btnY, crBtnW, crBtnH, canUsd ? 0xFFCE93D8 : 0xFF37474F);
                renderCenteredFittedText(graphics, packPrices[i], crBtnUsdX + crBtnW / 2, btnY + 4, crBtnW - 4, canUsd ? 0xFFFFFF : 0x78909C);
            }

            int btnConvW = colW;
            int b1X = leftX;
            boolean canConv1 = genesisCrystals > 0;
            boolean hoverB1 = mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + convH;
            graphics.fill(b1X, convY, b1X + btnConvW, convY + convH, canConv1 ? (hoverB1 ? 0xFF0D47A1 : 0xFF0A2B66) : 0xFF1C2230);
            graphics.renderOutline(b1X, convY, btnConvW, convH, canConv1 ? (hoverB1 ? 0xFF64B5F6 : 0xFF1976D2) : 0xFF37474F);
            renderCenteredFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.convert_crystals_to_jade", genesisCrystals).getString(), b1X + btnConvW / 2, convY + 7, btnConvW - 8, canConv1 ? 0xFF80D8FF : 0x78909C);

            int b2X = rightX;
            boolean canConv2 = stellarJade > 0;
            boolean hoverB2 = mouseX >= b2X && mouseX <= b2X + btnConvW && mouseY >= convY && mouseY <= convY + convH;
            graphics.fill(b2X, convY, b2X + btnConvW, convY + convH, canConv2 ? (hoverB2 ? 0xFF4A148C : 0xFF2A0852) : 0xFF1C2230);
            graphics.renderOutline(b2X, convY, btnConvW, convH, canConv2 ? (hoverB2 ? 0xFFBA68C8 : 0xFF7B1FA2) : 0xFF37474F);
            renderCenteredFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.convert_jade_to_primos", stellarJade).getString(), b2X + btnConvW / 2, convY + 7, btnConvW - 8, canConv2 ? 0xFFE1BEE7 : 0x78909C);

            if (jadeHoverTooltip != null) {
                graphics.renderComponentTooltip(this.font, jadeHoverTooltip, mouseX, mouseY);
            }
            return;
        }

        if (topUpTab == 2) {
            graphics.fill(leftX, panelY, leftX + colW, panelY + panelH, 0xAA0D121D);
            graphics.renderOutline(leftX, panelY, colW, panelH, 0xFF1B5E20);
            renderCenteredFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.coin_packs_carats").getString(), leftX + colW / 2, panelY + 6, colW - 12, 0xFFFFD700);

            int[] caratAmounts = {150, 1500, 4500, 15000};
            int[] caratCoins = {10000, 75000, 200000, 500000};
            String[] caratNotes = {
                    Component.translatable("gui.awakening_system.gacha.topup.pull_n", 1).getString(),
                    Component.translatable("gui.awakening_system.gacha.topup.pull_n", 10).getString(),
                    Component.translatable("gui.awakening_system.gacha.topup.pull_n", 30).getString(),
                    Component.translatable("gui.awakening_system.gacha.topup.pull_n", 100).getString()
            };

            int cR_X = leftX + 4;
            int cR_W = colW - 8;
            int cBtnW = Math.min(52, Math.max(40, cR_W / 5));
            int cBtnH = 16;
            int cBtnX = cR_X + cR_W - cBtnW - 4;

            ItemStack carrotStack = SystemItems.CARAT.isBound() ? new ItemStack(SystemItems.CARAT.get()) : new ItemStack(net.minecraft.world.item.Items.CARROT);

            for (int i = 0; i < 4; i++) {
                int rowY = panelY + 22 + i * 36;
                boolean hoverRow = mouseX >= cR_X && mouseX <= cR_X + cR_W && mouseY >= rowY && mouseY <= rowY + 32;
                graphics.fill(cR_X, rowY, cR_X + cR_W, rowY + 32, hoverRow ? 0xDD1B2538 : 0x88121A28);
                graphics.renderOutline(cR_X, rowY, cR_W, 32, 0xFF2ECC71);

                graphics.renderItem(carrotStack, cR_X + 4, rowY + 8);

                int textX = cR_X + 24;
                int textMaxW = cBtnX - textX - 4;
                renderFittedText(graphics, "§6§l" + String.format(java.util.Locale.US, "%,d", caratAmounts[i]) + " 🥕 §7" + caratNotes[i], textX, rowY + 5, textMaxW, 0xFFFFD54F, false);
                renderFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.coins_fmt", String.format(java.util.Locale.US, "%,d", caratCoins[i])).getString(), textX, rowY + 17, textMaxW, 0xFFE0E0E0, false);

                int btnY = rowY + 8;
                boolean canBuy = coins >= caratCoins[i];
                boolean hoverBtn = mouseX >= cBtnX && mouseX <= cBtnX + cBtnW && mouseY >= btnY && mouseY <= btnY + cBtnH;
                graphics.fill(cBtnX, btnY, cBtnX + cBtnW, btnY + cBtnH, canBuy ? (hoverBtn ? 0xFF2E7D32 : 0xFF1B5E20) : 0xFF37474F);
                graphics.renderOutline(cBtnX, btnY, cBtnW, cBtnH, canBuy ? (hoverBtn ? 0xFF81C784 : 0xFF4CAF50) : 0xFF546E7A);
                renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.buy").getString(), cBtnX + cBtnW / 2, btnY + 4, cBtnW - 4, canBuy ? 0xFFFFFF : 0x90A4AE);
            }

            graphics.fill(rightX, panelY, rightX + colW, panelY + panelH, 0xAA0D121D);
            graphics.renderOutline(rightX, panelY, colW, panelH, 0xFF0277BD);
            renderCenteredFittedText(graphics, "🥕 USD BALANCE PACKS 💳", rightX + colW / 2, panelY + 6, colW - 12, 0xFF80D8FF);

            int[] usdCaratAmounts = {150, 1500, 5000, 12000, 25000};
            String[] usdPrices = {"$0.99", "$4.99", "$14.99", "$29.99", "$49.99"};
            double[] usdCosts = {0.99, 4.99, 14.99, 29.99, 49.99};

            int crR_X = rightX + 4;
            int crR_W = colW - 8;
            int crBtnW = Math.min(56, Math.max(44, (crR_W - 80) / 3));
            int crBtnH = 15;
            int crBtnUsdX = crR_X + crR_W - crBtnW - 4;

            for (int i = 0; i < 5; i++) {
                int rowY = panelY + 20 + i * 28;
                boolean hoverRow = mouseX >= crR_X && mouseX <= crR_X + crR_W && mouseY >= rowY && mouseY <= rowY + 24;
                graphics.fill(crR_X, rowY, crR_X + crR_W, rowY + 24, hoverRow ? 0xDD1B2538 : 0x66121A28);
                graphics.renderOutline(crR_X, rowY, crR_W, 24, 0xFF2A3950);

                graphics.renderItem(carrotStack, crR_X + 2, rowY + 4);

                int packTextX = crR_X + 22;
                int packTextMaxW = crBtnUsdX - packTextX - 4;
                renderFittedText(graphics, "§6§l" + String.format(java.util.Locale.US, "%,d", usdCaratAmounts[i]) + " Carats 🥕", packTextX, rowY + 7, packTextMaxW, 0xFFFFFF, false);

                int btnY = rowY + 4;
                boolean canUsd = usdBalance >= usdCosts[i] - 0.001;
                boolean hoverUsd = mouseX >= crBtnUsdX && mouseX <= crBtnUsdX + crBtnW && mouseY >= btnY && mouseY <= btnY + crBtnH;
                graphics.fill(crBtnUsdX, btnY, crBtnUsdX + crBtnW, btnY + crBtnH, canUsd ? (hoverUsd ? 0xFF0277BD : 0xFF01579B) : 0xFF263238);
                graphics.renderOutline(crBtnUsdX, btnY, crBtnW, crBtnH, canUsd ? 0xFF4FC3F7 : 0xFF37474F);
                renderCenteredFittedText(graphics, usdPrices[i], crBtnUsdX + crBtnW / 2, btnY + 4, crBtnW - 4, canUsd ? 0xFFFFFF : 0x78909C);
            }

            int btnConvW = mW - 20;
            int b1X = leftX;
            boolean canConv = genesisCrystals > 0;
            boolean hoverB1 = mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + convH;
            graphics.fill(b1X, convY, b1X + btnConvW, convY + convH, canConv ? (hoverB1 ? 0xFF1B5E20 : 0xFF0E3D14) : 0xFF1C2230);
            graphics.renderOutline(b1X, convY, btnConvW, convH, canConv ? (hoverB1 ? 0xFF81C784 : 0xFF2ECC71) : 0xFF37474F);
            renderCenteredFittedText(graphics, Component.translatable("gui.awakening_system.gacha.topup.convert_crystals_to_carats", genesisCrystals, genesisCrystals).getString(), b1X + btnConvW / 2, convY + 7, btnConvW - 8, canConv ? 0xFF81C784 : 0x78909C);
            return;
        }

        graphics.fill(leftX, panelY, leftX + colW, panelY + panelH, 0xAA0D121D);
        graphics.renderOutline(leftX, panelY, colW, panelH, 0xFF334155);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.gift_cards").getString(), leftX + colW / 2, panelY + 6, colW - 12, 0xFF81C784);

        int[] gcVals = {5, 15, 30, 50, 100};
        int[] gcCosts = {25000, 75000, 150000, 250000, 500000};
        int[] gcColors = {0xFF4CAF50, 0xFF2196F3, 0xFF9C27B0, 0xFFFF9800, 0xFFE91E63};

        int gcR_X = leftX + 4;
        int gcR_W = colW - 8;
        int gcBtnW = Math.min(48, Math.max(38, gcR_W / 5));
        int gcBtnH = 16;
        int gcBtnX = gcR_X + gcR_W - gcBtnW - 4;

        for (int i = 0; i < 5; i++) {
            int rowY = panelY + 20 + i * 32;
            boolean hoverRow = mouseX >= gcR_X && mouseX <= gcR_X + gcR_W && mouseY >= rowY && mouseY <= rowY + 28;
            graphics.fill(gcR_X, rowY, gcR_X + gcR_W, rowY + 28, hoverRow ? 0xDD1B2538 : 0x88121A28);
            graphics.renderOutline(gcR_X, rowY, gcR_W, 28, gcColors[i]);

            ItemStack cardItem = switch (i) {
                case 1 -> SystemItems.GIFT_CARD_15.isBound() ? new ItemStack(SystemItems.GIFT_CARD_15.get()) : ItemStack.EMPTY;
                case 2 -> SystemItems.GIFT_CARD_30.isBound() ? new ItemStack(SystemItems.GIFT_CARD_30.get()) : ItemStack.EMPTY;
                case 3 -> SystemItems.GIFT_CARD_50.isBound() ? new ItemStack(SystemItems.GIFT_CARD_50.get()) : ItemStack.EMPTY;
                case 4 -> SystemItems.GIFT_CARD_100.isBound() ? new ItemStack(SystemItems.GIFT_CARD_100.get()) : ItemStack.EMPTY;
                default -> SystemItems.GIFT_CARD_5.isBound() ? new ItemStack(SystemItems.GIFT_CARD_5.get()) : ItemStack.EMPTY;
            };
            if (!cardItem.isEmpty()) {
                graphics.renderItem(cardItem, gcR_X + 2, rowY + 6);
            }

            int textX = gcR_X + 22;
            int textMaxW = gcBtnX - textX - 4;
            String cardTitle = Component.translatable("awakening_system.gacha.topup.card_title", gcVals[i]).getString();
            renderFittedText(graphics, "§f" + cardTitle, textX, rowY + 5, textMaxW, 0xFFFFFF, false);
            renderFittedText(graphics, "§6" + String.format(java.util.Locale.US, "%,d", gcCosts[i]) + " 🪙", textX, rowY + 15, textMaxW, 0xFFFFD54F, false);

            int btnY = rowY + 6;
            boolean canBuy = coins >= gcCosts[i];
            boolean hoverBtn = mouseX >= gcBtnX && mouseX <= gcBtnX + gcBtnW && mouseY >= btnY && mouseY <= btnY + gcBtnH;
            graphics.fill(gcBtnX, btnY, gcBtnX + gcBtnW, btnY + gcBtnH, canBuy ? (hoverBtn ? 0xFF2E7D32 : 0xFF1B5E20) : 0xFF37474F);
            graphics.renderOutline(gcBtnX, btnY, gcBtnW, gcBtnH, canBuy ? (hoverBtn ? 0xFF81C784 : 0xFF4CAF50) : 0xFF546E7A);
            renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.buy").getString(), gcBtnX + gcBtnW / 2, btnY + 4, gcBtnW - 4, canBuy ? 0xFFFFFF : 0x90A4AE);
        }

        graphics.fill(rightX, panelY, rightX + colW, panelY + panelH, 0xAA0D121D);
        graphics.renderOutline(rightX, panelY, colW, panelH, 0xFF334155);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.crystals_welkin").getString(), rightX + colW / 2, panelY + 6, colW - 12, 0xFF80D8FF);

        String[] packRawNames = {
                "60", "330", "1,090", "2,240", "3,880", "8,080", "🌙"
        };
        String[] packPrices = {"$0.99", "$4.99", "$14.99", "$29.99", "$49.99", "$99.99", "$4.99"};
        double[] packUsd = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};

        int crR_X = rightX + 4;
        int crR_W = colW - 8;
        int crBtnW = Math.min(56, Math.max(44, (crR_W - 80) / 3));
        int crBtnH = 15;
        int crBtnUsdX = crR_X + crR_W - crBtnW - 4;

        List<Component> hoverTooltip = null;

        for (int i = 0; i < 7; i++) {
            int rowY = panelY + 18 + i * 23;
            boolean hoverRow = mouseX >= crR_X && mouseX <= crR_X + crR_W && mouseY >= rowY && mouseY <= rowY + 21;
            graphics.fill(crR_X, rowY, crR_X + crR_W, rowY + 21, hoverRow ? 0xDD1B2538 : (i == 6 ? (isWelkinEnh ? 0x66311B92 : 0x664A3510) : 0x66121A28));
            graphics.renderOutline(crR_X, rowY, crR_W, 21, i == 6 ? (isWelkinEnh ? 0xFFCE93D8 : 0xFFFFD700) : 0xFF2A3950);

            boolean isFirstBonus = (i < 6) && ((firstTopUpMask & (1 << i)) == 0);

            String packDisplayName = (i == 6)
                    ? (isWelkinEnh
                        ? Component.translatable("awakening_system.gacha.topup.welkin_enhanced_name").getString()
                        : Component.translatable("awakening_system.gacha.topup.welkin_name").getString())
                    : Component.translatable("awakening_system.gacha.topup.crystals_count", packRawNames[i]).getString();

            int packTextX = crR_X + 4;
            int packTextMaxW = crBtnUsdX - packTextX - 4;
            String bonusPrefix = isFirstBonus ? "§6§l✦2X " : (i == 6 ? (isWelkinEnh ? "§d" : "§6") : "§b");
            renderFittedText(graphics, bonusPrefix + packDisplayName, packTextX, rowY + 6, packTextMaxW, 0xFFFFFF, false);

            if (hoverRow && isFirstBonus) {
                int[] baseCrystals = {60, 330, 1090, 2240, 3880, 8080};
                int dbl = baseCrystals[i] * 2;
                hoverTooltip = List.of(
                        Component.translatable("awakening_system.gacha.topup.first_bonus_tooltip_title"),
                        Component.translatable("awakening_system.gacha.topup.first_bonus_tooltip_desc"),
                        Component.translatable("gui.awakening_system.gacha.topup.first_bonus_crystals", String.format(java.util.Locale.US, "%,d", dbl))
                );
            }

            int btnY = rowY + 3;
            boolean canUsd = usdBalance >= packUsd[i] - 0.001;
            boolean hoverUsd = mouseX >= crBtnUsdX && mouseX <= crBtnUsdX + crBtnW && mouseY >= btnY && mouseY <= btnY + crBtnH;
            graphics.fill(crBtnUsdX, btnY, crBtnUsdX + crBtnW, btnY + crBtnH, canUsd ? (hoverUsd ? 0xFF0277BD : 0xFF01579B) : 0xFF263238);
            graphics.renderOutline(crBtnUsdX, btnY, crBtnW, crBtnH, canUsd ? 0xFF4FC3F7 : 0xFF37474F);
            renderCenteredFittedText(graphics, packPrices[i], crBtnUsdX + crBtnW / 2, btnY + 4, crBtnW - 4, canUsd ? 0xFFFFFF : 0x78909C);
        }

        int btnConvW = colW;

        int b1X = leftX;
        boolean canConv1 = genesisCrystals > 0;
        boolean hoverB1 = mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + convH;
        graphics.fill(b1X, convY, b1X + btnConvW, convY + convH, canConv1 ? (hoverB1 ? 0xFF0D47A1 : 0xFF0A2B66) : 0xFF1C2230);
        graphics.renderOutline(b1X, convY, btnConvW, convH, canConv1 ? (hoverB1 ? 0xFF64B5F6 : 0xFF1976D2) : 0xFF37474F);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.convert_genesis", genesisCrystals).getString(), b1X + btnConvW / 2, convY + 7, btnConvW - 8, canConv1 ? 0xFF80D8FF : 0x78909C);

        int b2X = rightX;
        boolean canConv2 = stellarJade > 0;
        boolean hoverB2 = mouseX >= b2X && mouseX <= b2X + btnConvW && mouseY >= convY && mouseY <= convY + convH;
        graphics.fill(b2X, convY, b2X + btnConvW, convY + convH, canConv2 ? (hoverB2 ? 0xFF4A148C : 0xFF2A0852) : 0xFF1C2230);
        graphics.renderOutline(b2X, convY, btnConvW, convH, canConv2 ? (hoverB2 ? 0xFFBA68C8 : 0xFF7B1FA2) : 0xFF37474F);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.convert_stellar", stellarJade).getString(), b2X + btnConvW / 2, convY + 7, btnConvW - 8, canConv2 ? 0xFFE1BEE7 : 0x78909C);

        if (hoverTooltip != null) {
            graphics.renderComponentTooltip(this.font, hoverTooltip, mouseX, mouseY);
        }
    }

    private void renderShopModal(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, getVirtW(), getVirtH(), 0xEE05070E);

        int mW = 500;
        int mH = 265;
        int mX = getVirtW() / 2 - mW / 2;
        int mY = getVirtH() / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF170E28);
        graphics.renderOutline(mX, mY, mW, mH, 0xFFDA70D6);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x33FFFFFF);

        graphics.drawString(this.font, Component.translatable("awakening_system.gacha.shop.title").getString(), mX + 14, mY + 12, 0xFFDA70D6, false);

        int closeX = mX + mW - 22;
        int closeY = mY + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        ItemStack stardustStack = SystemItems.MASTERLESS_STARDUST.isBound() ? new ItemStack(SystemItems.MASTERLESS_STARDUST.get()) : ItemStack.EMPTY;
        ItemStack starglitterStack = SystemItems.MASTERLESS_STARGLITTER.isBound() ? new ItemStack(SystemItems.MASTERLESS_STARGLITTER.get()) : ItemStack.EMPTY;

        String sgStr = String.valueOf(starglitter);
        String sdStr = String.valueOf(stardust);
        int sgW = this.font.width(sgStr);
        int sdW = this.font.width(sdStr);

        int sgTextX = closeX - 8 - sgW;
        int sgIconX = sgTextX - 18;
        if (!starglitterStack.isEmpty()) {
            graphics.renderItem(starglitterStack, sgIconX, mY + 8);
        }
        graphics.drawString(this.font, sgStr, sgTextX, mY + 12, 0xFFFFD700, false);

        int sepX = sgIconX - 8;
        graphics.drawString(this.font, "§7|", sepX, mY + 12, 0x888888, false);

        int sdTextX = sepX - 8 - sdW;
        int sdIconX = sdTextX - 18;
        if (!stardustStack.isEmpty()) {
            graphics.renderItem(stardustStack, sdIconX, mY + 8);
        }
        graphics.drawString(this.font, sdStr, sdTextX, mY + 12, 0xFF80D8FF, false);

        ItemStack[] leftItemStacks = {
                SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : ItemStack.EMPTY,
                SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : ItemStack.EMPTY,
                SystemItems.MORA.isBound() ? new ItemStack(SystemItems.MORA.get()) : ItemStack.EMPTY,
                new ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE)
        };
        Component[] leftNames = {
                Component.translatable("awakening_system.gacha.shop.item.intertwined"),
                Component.translatable("awakening_system.gacha.shop.item.acquaint"),
                Component.translatable("awakening_system.gacha.shop.item.mora"),
                Component.translatable("awakening_system.gacha.shop.item.exp_bottles")
        };
        int[] leftCosts = {75, 75, 15, 10};

        ItemStack[] rightItemStacks = {
                SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : ItemStack.EMPTY,
                SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : ItemStack.EMPTY,
                CompanionContractItem.createContract(CompanionProfile.NOELLE),
                CompanionContractItem.createContract(CompanionProfile.CHA_HAEIN)
        };
        Component[] rightNames = {
                Component.translatable("awakening_system.gacha.shop.item.intertwined"),
                Component.translatable("awakening_system.gacha.shop.item.acquaint"),
                Component.translatable("awakening_system.gacha.shop.item.pact_noelle"),
                Component.translatable("awakening_system.gacha.shop.item.pact_cha")
        };
        int[] rightCosts = {5, 5, 34, 34};

        int colW = 230;
        int startY = mY + 36;

        if (!stardustStack.isEmpty()) {
            graphics.renderItem(stardustStack, mX + 14, startY - 4);
        }
        graphics.drawString(this.font, Component.translatable("awakening_system.gacha.shop.stardust_section").getString(), mX + 34, startY, 0x80D8FF, false);
        for (int i = 0; i < 4; i++) {
            int y = startY + 14 + i * 48;
            int offerId = (i == 0) ? 0 : ((i == 1) ? 1 : ((i == 2) ? 6 : 7));
            boolean canAfford = stardust >= leftCosts[i];

            graphics.fill(mX + 14, y, mX + 14 + colW, y + 42, 0xFF0E1324);
            graphics.renderOutline(mX + 14, y, colW, 42, 0xFF2A3D66);

            if (!leftItemStacks[i].isEmpty()) {
                graphics.renderItem(leftItemStacks[i], mX + 18, y + 13);
            }

            renderFittedText(graphics, "§f" + leftNames[i].getString(), mX + 38, y + 6, colW - 95, 0xFFFFFF, false);
            String costLabel = Component.translatable("awakening_system.gacha.shop.cost", leftCosts[i]).getString();
            graphics.drawString(this.font, "§b" + costLabel, mX + 38, y + 24, 0x80D8FF, false);
            int cNumW = this.font.width("§b" + costLabel);
            String stardustName = Component.translatable("awakening_system.gacha.shop.stardust").getString();
            if (!stardustStack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(mX + 38 + cNumW + 3, y + 22, 0);
                graphics.pose().scale(0.7F, 0.7F, 0.7F);
                graphics.renderItem(stardustStack, 0, 0);
                graphics.pose().popPose();
                graphics.drawString(this.font, stardustName, mX + 38 + cNumW + 16, y + 24, 0x80D8FF, false);
            } else {
                graphics.drawString(this.font, stardustName, mX + 38 + cNumW + 4, y + 24, 0x80D8FF, false);
            }

            int bX = mX + 14 + colW - 68;
            int bY = y + 10;
            boolean hoverBuy = mouseX >= bX && mouseX <= bX + 60 && mouseY >= bY && mouseY <= bY + 22;
            int btnColor = canAfford ? (hoverBuy ? 0xFF3558A8 : 0xFF1B2F63) : 0xFF222233;
            graphics.fill(bX, bY, bX + 60, bY + 22, btnColor);
            graphics.renderOutline(bX, bY, 60, 22, canAfford ? (hoverBuy ? 0xFFFFFFFF : 0xFF4A7BD4) : 0xFF444455);
            String btnText = canAfford ? Component.translatable("awakening_system.gacha.shop.exchange").getString() : Component.translatable("awakening_system.gacha.shop.insufficient").getString();
            graphics.drawCenteredString(this.font, btnText, bX + 30, bY + 7, canAfford ? 0xFFFFFF : 0x777788);
        }

        int rightColX = mX + 14 + colW + 12;
        if (!starglitterStack.isEmpty()) {
            graphics.renderItem(starglitterStack, rightColX, startY - 4);
        }
        graphics.drawString(this.font, Component.translatable("awakening_system.gacha.shop.starglitter_section").getString(), rightColX + 20, startY, 0xFFE082, false);
        for (int i = 0; i < 4; i++) {
            int y = startY + 14 + i * 48;
            int offerId = (i == 0) ? 2 : ((i == 1) ? 3 : ((i == 2) ? 4 : 5));
            boolean canAfford = starglitter >= rightCosts[i];

            graphics.fill(rightColX, y, rightColX + colW, y + 42, 0xFF1E0E28);
            graphics.renderOutline(rightColX, y, colW, 42, 0xFF5C246B);

            if (!rightItemStacks[i].isEmpty()) {
                graphics.renderItem(rightItemStacks[i], rightColX + 4, y + 13);
            }

            renderFittedText(graphics, "§f" + rightNames[i].getString(), rightColX + 24, y + 6, colW - 85, 0xFFFFFF, false);
            String costLabel = Component.translatable("awakening_system.gacha.shop.cost", rightCosts[i]).getString();
            graphics.drawString(this.font, "§6" + costLabel, rightColX + 24, y + 24, 0xFFFFD700, false);
            int cNumW2 = this.font.width("§6" + costLabel);
            String glitterName = Component.translatable("awakening_system.gacha.shop.starglitter").getString();
            if (!starglitterStack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(rightColX + 24 + cNumW2 + 3, y + 22, 0);
                graphics.pose().scale(0.7F, 0.7F, 0.7F);
                graphics.renderItem(starglitterStack, 0, 0);
                graphics.pose().popPose();
                graphics.drawString(this.font, glitterName, rightColX + 24 + cNumW2 + 16, y + 24, 0xFFFFD700, false);
            } else {
                graphics.drawString(this.font, glitterName, rightColX + 24 + cNumW2 + 4, y + 24, 0xFFFFD700, false);
            }

            int bX = rightColX + colW - 68;
            int bY = y + 10;
            boolean hoverBuy = mouseX >= bX && mouseX <= bX + 60 && mouseY >= bY && mouseY <= bY + 22;
            int btnColor = canAfford ? (hoverBuy ? 0xFFA07825 : 0xFF664810) : 0xFF2B2222;
            graphics.fill(bX, bY, bX + 60, bY + 22, btnColor);
            graphics.renderOutline(bX, bY, 60, 22, canAfford ? (hoverBuy ? 0xFFFFFFFF : 0xFFFFD700) : 0xFF554444);
            String btnText = canAfford ? (i >= 2 ? Component.translatable("awakening_system.gacha.shop.recruit").getString() : Component.translatable("awakening_system.gacha.shop.exchange").getString()) : Component.translatable("awakening_system.gacha.shop.insufficient").getString();
            graphics.drawCenteredString(this.font, btnText, bX + 30, bY + 7, canAfford ? 0xFFFFD700 : 0x887777);
        }

        for (int i = 0; i < 4; i++) {
            int y = startY + 14 + i * 48;
            if (mouseX >= mX + 18 && mouseX <= mX + 34 && mouseY >= y + 13 && mouseY <= y + 29 && !leftItemStacks[i].isEmpty()) {
                graphics.renderTooltip(this.font, leftItemStacks[i], (int) mouseX, (int) mouseY);
            }
            if (mouseX >= rightColX + 4 && mouseX <= rightColX + 20 && mouseY >= y + 13 && mouseY <= y + 29 && !rightItemStacks[i].isEmpty()) {
                graphics.renderTooltip(this.font, rightItemStacks[i], (int) mouseX, (int) mouseY);
            }
        }
    }

    private void renderPathModal(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, getVirtW(), getVirtH(), 0xEE05070E);

        int mW = 460;
        int mH = 260;
        int mX = getVirtW() / 2 - mW / 2;
        int mY = getVirtH() / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF181105);
        graphics.renderOutline(mX, mY, mW, mH, 0xFFFFD700);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x33FFE082);

        graphics.drawString(this.font, Component.translatable("awakening_system.gacha.path.title").getString(), mX + 14, mY + 12, 0xFFFFD700, false);

        int closeX = mX + mW - 22;
        int closeY = mY + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        int statY = mY + 28;
        graphics.fill(mX + 12, statY, mX + mW - 12, statY + 36, 0xFF2A1C0A);
        graphics.renderOutline(mX + 12, statY, mW - 24, 36, 0xFF5D4037);

        String fateStr = Component.translatable("awakening_system.gacha.path.fate_points", weaponBannerFatePoints).getString();
        graphics.drawString(this.font, fateStr, mX + 18, statY + 6, 0xFFFFFF, false);

        String statusNotice;
        if (weaponBannerTarget <= 0) {
            statusNotice = Component.translatable("awakening_system.gacha.path.no_target").getString();
        } else if (weaponBannerFatePoints >= 1) {
            statusNotice = Component.translatable("awakening_system.gacha.path.guaranteed").getString();
        } else {
            statusNotice = Component.translatable("awakening_system.gacha.path.not_guaranteed").getString();
        }
        graphics.drawString(this.font, statusNotice, mX + 18, statY + 20, 0xFFFFFF, false);

        long dayTime = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getDayTime() : 0L;
        PromotionalBanner.BannerPhase promoPhase = PromotionalBanner.getActivePhase(dayTime);

        ItemStack wp1 = promoPhase.primaryFeaturedWeapon().get();
        ItemStack wp2 = promoPhase.secondaryFeaturedWeapon() != null ? promoPhase.secondaryFeaturedWeapon().get() : ItemStack.EMPTY;

        int cardW = 205;
        int cardH = 135;
        int cardY = statY + 44;

        renderPathWeaponCard(graphics, wp1, 1, mX + 18, cardY, cardW, cardH, mouseX, mouseY);
        if (!wp2.isEmpty()) {
            renderPathWeaponCard(graphics, wp2, 2, mX + mW - 18 - cardW, cardY, cardW, cardH, mouseX, mouseY);
        }

        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.gacha.path.footer").getString(), mX + mW / 2, mY + mH - 14, 0x888888);
    }

    private void renderPathWeaponCard(GuiGraphics graphics, ItemStack weapon, int targetIdx, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean isSelected = (weaponBannerTarget == targetIdx);
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        int bg = isSelected ? 0xFF352006 : (hover ? 0xFF2A1C0A : 0xFF1C1306);
        int border = isSelected ? 0xFFFFD700 : (hover ? 0xFFB8860B : 0xFF4A3515);

        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);

        graphics.pose().pushPose();
        graphics.pose().translate(x + w / 2, y + 35, 100);
        graphics.pose().scale(2.5F, 2.5F, 2.5F);
        graphics.renderItem(weapon, -8, -8);
        graphics.pose().popPose();

        renderCenteredFittedText(graphics, weapon.getHoverName().getString(), x + w / 2, y + 68, w - 12, isSelected ? 0xFFFFD700 : 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.gacha.path.weapon_5star").getString(), x + w / 2, y + 80, 0xFFFFD700);

        int btnW = 120;
        int btnH = 20;
        int btnX = x + (w - btnW) / 2;
        int btnY = y + h - 26;
        boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

        if (isSelected) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF2E7D32);
            graphics.renderOutline(btnX, btnY, btnW, btnH, 0xFF81C784);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.gacha.path.selected").getString(), btnX + btnW / 2, btnY + 6, 0xFFFFFF);
        } else {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverBtn ? 0xFFB8860B : 0xFF5D4037);
            graphics.renderOutline(btnX, btnY, btnW, btnH, hoverBtn ? 0xFFFFD700 : 0xFF8D6E63);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.gacha.path.select_btn").getString(), btnX + btnW / 2, btnY + 6, hoverBtn ? 0xFFFFFF : 0xFFE082);
        }
    }

    private void renderNewsModal(GuiGraphics graphics, int mouseX, int mouseY) {
        long dayTime = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getDayTime() : 0L;
        VersionBannerManager.VersionInfo current = VersionBannerManager.getCurrentVersionInfo(dayTime);
        int currentPhase = VersionBannerManager.getPhaseNumber(dayTime);
        boolean underMaint = VersionBannerManager.isUnderMaintenance(dayTime);

        graphics.fill(0, 0, getVirtW(), getVirtH(), 0xEE05070E);

        int mW = Math.min(540, getVirtW() - 16);
        int mH = Math.min(278, getVirtH() - 16);
        int mX = getVirtW() / 2 - mW / 2;
        int mY = getVirtH() / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF0D1527);
        graphics.renderOutline(mX, mY, mW, mH, 0xFFFFD700);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x4464B5F6);

        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.title"), mX + 14, mY + 11, 0xFFFFD700, false);

        int closeX = mX + mW - 22;
        int closeY = mY + 9;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        PromotionalBanner.BannerPhase activeP = PromotionalBanner.getActivePhase(dayTime, 0);
        PromotionalBanner.BannerPhase nextP = PromotionalBanner.getNextPhase(dayTime);
        int phaseNum = PromotionalBanner.getPhaseNumber(dayTime);
        int totalPhases = PromotionalBanner.getTotalPhases();
        String timeLeftStr = PromotionalBanner.getFormattedTimeRemaining(dayTime);

        int statY = mY + 26;
        int statH = 33;
        graphics.fill(mX + 12, statY, mX + mW - 12, statY + statH, 0xFF14203A);
        graphics.renderOutline(mX + 12, statY, mW - 24, statH, 0xFF2A3D66);

        int vMajor = ((phaseNum - 1) / 2) + 1;
        int vMinor = ((phaseNum - 1) % 2) + 1;
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.rotation", phaseNum, totalPhases, vMajor, vMinor), mX + 18, statY + 4, 0xFFFFE082, false);
        if (underMaint) {
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.maint", VersionBannerManager.getMaintenanceRemainingFormatted(dayTime)), mX + 18, statY + 17, 0xFFFF5555, false);
        } else {
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.servers_up", timeLeftStr), mX + 18, statY + 17, 0x81C784, false);
        }

        int colW = (mW - 32) / 2;
        int colH = 138;
        int col1X = mX + 12;
        int col2X = col1X + colW + 8;
        int colY = statY + statH + 5;

        graphics.fill(col1X, colY, col1X + colW, colY + colH, 0xFF14223A);
        graphics.renderOutline(col1X, colY, colW, colH, 0xFF64B5F6);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.in_progress", phaseNum, timeLeftStr), col1X + 6, colY + 5, 0xFF2ECC71, false);
        renderFittedText(graphics, activeP.bannerTitle() + " §7(" + activeP.franchise().displayName() + ")", col1X + 6, colY + 16, colW - 12, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.promo_5star_header"), col1X + 6, colY + 28, 0xFFFFD700, false);
        int p1Y = colY + 39;
        if (activeP.featuredCharacter() != null) {
            graphics.drawString(this.font, "  ✦ " + activeP.featuredCharacter().displayName() + " §7(" + activeP.featuredCharacter().element() + ")", col1X + 6, p1Y, 0xFFFFAA, false);
            p1Y += 10;
        }
        if (activeP.secondaryFeaturedCharacter() != null) {
            graphics.drawString(this.font, "  ✦ " + activeP.secondaryFeaturedCharacter().displayName() + " §7(" + activeP.secondaryFeaturedCharacter().element() + ")", col1X + 6, p1Y, 0xFFFFAA, false);
            p1Y += 10;
        }
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.weapons_5star_header"), col1X + 6, p1Y + 2, 0xFFFFE082, false);
        p1Y += 12;
        if (activeP.primaryFeaturedWeapon() != null) {
            ItemStack w1 = activeP.primaryFeaturedWeapon().get();
            graphics.drawString(this.font, "  ✦ " + w1.getHoverName().getString(), col1X + 6, p1Y, 0xFFE0E0E0, false);
            p1Y += 10;
        }
        if (activeP.secondaryFeaturedWeapon() != null) {
            ItemStack w2 = activeP.secondaryFeaturedWeapon().get();
            graphics.drawString(this.font, "  ✦ " + w2.getHoverName().getString(), col1X + 6, p1Y, 0xFFE0E0E0, false);
            p1Y += 10;
        }
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.rate_up_4star_header"), col1X + 6, p1Y + 2, 0xFFDA70D6, false);
        p1Y += 11;
        StringBuilder sb4Chars = new StringBuilder("  • ");
        for (int i = 0; i < activeP.rateUp4StarCharacters().size(); i++) {
            if (i > 0) sb4Chars.append(", ");
            sb4Chars.append(activeP.rateUp4StarCharacters().get(i).displayName());
        }
        renderFittedText(graphics, sb4Chars.toString(), col1X + 6, p1Y, colW - 12, 0xFFCCCCCC, false);
        p1Y += 10;
        StringBuilder sb4Weaps = new StringBuilder("  • ");
        for (int i = 0; i < activeP.rateUp4StarWeapons().size(); i++) {
            if (i > 0) sb4Weaps.append(", ");
            sb4Weaps.append(activeP.rateUp4StarWeapons().get(i).get().getHoverName().getString());
        }
        renderFittedText(graphics, sb4Weaps.toString(), col1X + 6, p1Y, colW - 12, 0xFFAAAAAA, false);

        int nextNum = (phaseNum % totalPhases) + 1;
        graphics.fill(col2X, colY, col2X + colW, colY + colH, 0xFF0E1728);
        graphics.renderOutline(col2X, colY, colW, colH, 0xFFFFD700);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.next_rotation", nextNum), col2X + 6, colY + 5, 0xFFFFD700, false);
        renderFittedText(graphics, nextP.bannerTitle() + " §7(" + nextP.franchise().displayName() + ")", col2X + 6, colY + 16, colW - 12, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.next_5star_header"), col2X + 6, colY + 28, 0xFFFFD700, false);
        int p2Y = colY + 39;
        if (nextP.featuredCharacter() != null) {
            graphics.drawString(this.font, "  ✦ " + nextP.featuredCharacter().displayName() + " §7(" + nextP.featuredCharacter().element() + ")", col2X + 6, p2Y, 0xFFFFAA, false);
            p2Y += 10;
        }
        if (nextP.secondaryFeaturedCharacter() != null) {
            graphics.drawString(this.font, "  ✦ " + nextP.secondaryFeaturedCharacter().displayName() + " §7(" + nextP.secondaryFeaturedCharacter().element() + ")", col2X + 6, p2Y, 0xFFFFAA, false);
            p2Y += 10;
        }
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.next_weapons_header"), col2X + 6, p2Y + 2, 0xFFFFE082, false);
        p2Y += 12;
        if (nextP.primaryFeaturedWeapon() != null) {
            ItemStack nw1 = nextP.primaryFeaturedWeapon().get();
            graphics.drawString(this.font, "  ✦ " + nw1.getHoverName().getString(), col2X + 6, p2Y, 0xFFE0E0E0, false);
            p2Y += 10;
        }
        if (nextP.secondaryFeaturedWeapon() != null) {
            ItemStack nw2 = nextP.secondaryFeaturedWeapon().get();
            graphics.drawString(this.font, "  ✦ " + nw2.getHoverName().getString(), col2X + 6, p2Y, 0xFFE0E0E0, false);
            p2Y += 10;
        }
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.news.next_4star_header"), col2X + 6, p2Y + 2, 0xFFDA70D6, false);
        p2Y += 11;
        StringBuilder sbNext4Chars = new StringBuilder("  • ");
        for (int i = 0; i < nextP.rateUp4StarCharacters().size(); i++) {
            if (i > 0) sbNext4Chars.append(", ");
            sbNext4Chars.append(nextP.rateUp4StarCharacters().get(i).displayName());
        }
        renderFittedText(graphics, sbNext4Chars.toString(), col2X + 6, p2Y, colW - 12, 0xFFCCCCCC, false);
        p2Y += 10;
        StringBuilder sbNext4Weaps = new StringBuilder("  • ");
        for (int i = 0; i < nextP.rateUp4StarWeapons().size(); i++) {
            if (i > 0) sbNext4Weaps.append(", ");
            sbNext4Weaps.append(nextP.rateUp4StarWeapons().get(i).get().getHoverName().getString());
        }
        renderFittedText(graphics, sbNext4Weaps.toString(), col2X + 6, p2Y, colW - 12, 0xFFAAAAAA, false);

        int footerY = colY + colH + 5;
        int footerH = (mY + mH - 7) - footerY;
        graphics.fill(mX + 12, footerY, mX + mW - 12, footerY + footerH, 0xFF141F36);
        graphics.renderOutline(mX + 12, footerY, mW - 24, footerH, 0xFFFFE082);
        renderFittedText(graphics, Component.translatable("gui.awakening_system.gacha.news.message_prefix").getString() + activeP.archonQuote(), mX + 18, footerY + 3, mW - 36, 0xFFFFE082, false);
        PromotionalBanner.BannerPhase hsrActive = PromotionalBanner.getHsrActivePhase(dayTime);
        renderFittedText(graphics, Component.translatable("gui.awakening_system.gacha.news.hsr_banner_info", hsrActive.bannerTitle(), hsrActive.featuredCharacter().displayName()).getString(), mX + 18, footerY + 13, mW - 36, 0xAAAAAA, false);
        renderFittedText(graphics, Component.translatable("gui.awakening_system.gacha.news.version_comp").getString(), mX + 18, footerY + 23, mW - 36, 0x81C784, false);
    }

    private void renderMeteorAnimation(GuiGraphics graphics, int mouseX, int mouseY) {
        long elapsed = System.currentTimeMillis() - this.meteorStartTime;
        float progress = Math.min(1.0F, (float) elapsed / (float) METEOR_DURATION_MS);

        renderSkipButton(graphics, mouseX, mouseY);

        float flightProgress = Math.min(1.0F, progress / 0.85F);
        float ease = flightProgress * flightProgress * (3.0F - 2.0F * flightProgress);
        int startX = (int) (getVirtW() * 0.94F);
        int startY = (int) (getVirtH() * 0.06F);
        int targetX = (int) (getVirtW() * 0.22F);
        int targetY = (int) (getVirtH() * 0.70F);

        int cometX = (int) (startX - ease * (startX - targetX));
        int cometY = (int) (startY + ease * (targetY - startY));

        int cometColor = highestWonRarity == 5 ? 0xFFFFD700 : (highestWonRarity == 4 ? 0xFFA044FF : 0xFF4499FF);
        int coreColor = 0xFFFFFFFF;
        int glowColor = highestWonRarity == 5 ? 0x55FFD700 : (highestWonRarity == 4 ? 0x55A044FF : 0x554499FF);

        if (lastPullCount > 1) {
            for (int sub = 1; sub <= 9; sub++) {
                double wave = Math.sin(sub * 0.9D + progress * 14.0D) * 14.0D;
                int sx = cometX + (int)(sub * 16 + wave);
                int sy = cometY - (int)(sub * 11 - wave * 0.6D);
                int subRarity = (sub < itemRarities.size()) ? itemRarities.get(sub) : 3;
                int subColor = subRarity == 5 ? 0xFFFFD700 : (subRarity == 4 ? 0xFFBA68C8 : 0xFF64B5F6);

                for (int st = 1; st <= 6; st++) {
                    int stx = sx + st * 5;
                    int sty = sy - st * 3;
                    graphics.fill(stx - 1, sty - 1, stx + 1, sty + 1, (subColor & 0x00FFFFFF) | (Math.max(10, 180 - st * 28) << 24));
                }
                graphics.fill(sx - 2, sy - 2, sx + 2, sy + 2, subColor);
            }
        }

        for (int trail = 1; trail <= 26; trail++) {
            float trailRatio = (float) trail / 26.0F;
            int tx = cometX + (int)(trail * 13.5F * (0.8F + trailRatio * 0.4F));
            int ty = cometY - (int)(trail * 9.5F * (0.8F + trailRatio * 0.4F));
            int trailSize = Math.max(1, (int)((1.0F - trailRatio) * 12.0F));
            int alpha = (int)((1.0F - trailRatio) * 210.0F);
            int segColor = (trail < 4) ? coreColor : cometColor;
            graphics.fill(tx - trailSize, ty - trailSize, tx + trailSize, ty + trailSize, (segColor & 0x00FFFFFF) | (alpha << 24));
        }

        int pulse = (int) (Math.sin(System.currentTimeMillis() * 0.02D) * 8);
        graphics.fill(cometX - 32 - pulse, cometY - 32 - pulse, cometX + 32 + pulse, cometY + 32 + pulse, glowColor);
        graphics.fill(cometX - 18, cometY - 18, cometX + 18, cometY + 18, (cometColor & 0x00FFFFFF) | 0x88000000);
        graphics.fill(cometX - 10, cometY - 10, cometX + 10, cometY + 10, cometColor);
        graphics.fill(cometX - 4, cometY - 4, cometX + 4, cometY + 4, coreColor);

        if (activeBanner == BANNER_UMA) {
            ItemStack caratItem = SystemItems.CARAT.isBound() ? new ItemStack(SystemItems.CARAT.get()) : new ItemStack(Items.CARROT);
            graphics.pose().pushPose();
            graphics.pose().translate(cometX, cometY, 80);
            graphics.pose().scale(2.2F, 2.2F, 2.2F);
            graphics.renderItem(caratItem, -8, -8);
            graphics.pose().popPose();
        } else if (SystemItems.PRIMOGEM.isBound()) {
            graphics.pose().pushPose();
            graphics.pose().translate(cometX, cometY, 80);
            graphics.pose().scale(2.0F, 2.0F, 2.0F);
            graphics.renderItem(new ItemStack(SystemItems.PRIMOGEM.get()), -8, -8);
            graphics.pose().popPose();
        }

        String splash;
        String subNotice;
        if (activeBanner == BANNER_UMA) {
            splash = highestWonRarity >= 5
                    ? Component.translatable("gui.awakening_system.gacha.anim.uma_gold_3star").getString()
                    : (highestWonRarity == 4 ? Component.translatable("gui.awakening_system.gacha.anim.uma_silver_2star").getString() : Component.translatable("gui.awakening_system.gacha.anim.uma_bronze_1star").getString());
            subNotice = Component.translatable("gui.awakening_system.gacha.anim.uma_sub_descend").getString();
        } else {
            splash = highestWonRarity == 5
                    ? Component.translatable("gui.awakening_system.gacha.anim.gold_5star").getString()
                    : (highestWonRarity == 4 ? Component.translatable("gui.awakening_system.gacha.anim.purple_4star").getString() : Component.translatable("gui.awakening_system.gacha.anim.astral_3star").getString());
            subNotice = Component.translatable("gui.awakening_system.gacha.anim.comet_descend").getString();
        }
        graphics.drawCenteredString(this.font, splash, getVirtW() / 2, 28, 0xFFFFFF);
        graphics.drawCenteredString(this.font, subNotice, getVirtW() / 2, 42, 0xCCCCCC);

        if (progress >= 0.85F) {
            float impactRatio = (progress - 0.85F) / 0.15F;
            int ringRadius = (int) (impactRatio * Math.max(getVirtW(), getVirtH()) * 1.2F);
            int ringAlpha = (int) ((1.0F - impactRatio) * 220.0F);
            graphics.renderOutline(cometX - ringRadius, cometY - ringRadius, ringRadius * 2, ringRadius * 2, (cometColor & 0x00FFFFFF) | (ringAlpha << 24));
        }

        if (elapsed >= METEOR_DURATION_MS) {
            this.screenState = STATE_REVEAL_SINGLE;
            this.currentRevealIndex = 0;
            this.revealStartTime = System.currentTimeMillis();
            if (!resultItems.isEmpty()) {
                playRevealSound(resultItems.get(0), (0 < itemRarities.size()) ? itemRarities.get(0) : getItemStarRarity(resultItems.get(0)));
            }
        }
    }

    private void renderUmaStartingGateCinematic(GuiGraphics graphics, int mouseX, int mouseY) {
        long elapsed = System.currentTimeMillis() - this.meteorStartTime;
        float progress = Math.min(1.0F, (float) elapsed / (float) METEOR_DURATION_MS);

        renderSkipButton(graphics, mouseX, mouseY);

        int w = getVirtW();
        int h = getVirtH();

        int glowColor;
        if (highestWonRarity >= 5) {
            float hue = (float) ((System.currentTimeMillis() % 1600L) / 1600.0);
            glowColor = java.awt.Color.HSBtoRGB(hue, 0.9F, 1.0F);
        } else if (highestWonRarity == 4) {
            glowColor = 0xFFFFD700;
        } else {
            glowColor = 0xFFE0E0E0;
        }

        renderTracenAcademyBackground(graphics, w, h);
        int horizonY = (int) (h * 0.56F);

        int centerX = w / 2;
        int gateW = Math.min(380, w - 40);
        int gateH = Math.min(220, h - 80);
        int gateY = horizonY - (gateH * 2 / 3);

        int auraPulse = (int) (10 + Math.sin(System.currentTimeMillis() * 0.01) * 6);
        graphics.fill(centerX - gateW / 2 - auraPulse, gateY - auraPulse, centerX + gateW / 2 + auraPulse, gateY + gateH + auraPulse, (glowColor & 0x00FFFFFF) | 0x33000000);

        int roofH = 34;
        graphics.fill(centerX - gateW / 2, gateY, centerX + gateW / 2, gateY + roofH, 0xFF1B5E20);
        graphics.renderOutline(centerX - gateW / 2, gateY, gateW, roofH, 0xFFFFD700);
        graphics.drawCenteredString(this.font, "§6§l✦ ACADEMIA TRACEN • STARTING GATE #1 ✦", centerX, gateY + 6, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§e★ PRETTY DERBY RECRUITMENT ★", centerX, gateY + 18, 0xFFFFD700);

        for (int l = 0; l < 9; l++) {
            int lx = centerX - (gateW / 2 - 20) + l * ((gateW - 40) / 8);
            int dotColor = (highestWonRarity >= 5) ? java.awt.Color.HSBtoRGB(((float) l / 9.0F + (float) ((System.currentTimeMillis() % 1200L) / 1200.0)) % 1.0F, 0.9F, 1.0F) : glowColor;
            graphics.fill(lx - 4, gateY + roofH + 1, lx + 4, gateY + roofH + 5, dotColor);
        }

        int colW = 14;
        graphics.fill(centerX - gateW / 2, gateY + roofH + 5, centerX - gateW / 2 + colW, gateY + gateH, 0xFFE0E0E0);
        graphics.fill(centerX + gateW / 2 - colW, gateY + roofH + 5, centerX + gateW / 2, gateY + gateH, 0xFFE0E0E0);
        graphics.renderOutline(centerX - gateW / 2, gateY + roofH + 5, colW, gateH - roofH - 5, 0xFF2E7D32);
        graphics.renderOutline(centerX + gateW / 2 - colW, gateY + roofH + 5, colW, gateH - roofH - 5, 0xFF2E7D32);

        float gateOpenProgress = 0.0F;
        if (elapsed > 900L) {
            gateOpenProgress = Math.min(1.0F, (elapsed - 900L) / 1100.0F);
        }

        int totalOpenW = gateW - colW * 2;
        int doorW = totalOpenW / 2;
        int leftHingeX = centerX - doorW;
        int rightHingeX = centerX + doorW;
        int doorTop = gateY + roofH + 5;
        int doorBottom = gateY + gateH;

        double angleRad = gateOpenProgress * (Math.PI / 2.0);
        int visibleW = Math.max(4, (int) (doorW * Math.cos(angleRad)));
        int swingDepth = (int) (Math.sin(angleRad) * 16.0);

        int openLeft = leftHingeX + visibleW;
        int openRight = rightHingeX - visibleW;
        graphics.fill(leftHingeX, doorTop, rightHingeX, doorBottom, 0xFF0A140D);
        if (openRight > openLeft) {
            int lightAlpha = (int) (gateOpenProgress * 190.0F);
            graphics.fillGradient(openLeft, doorTop, openRight, doorBottom, (glowColor & 0x00FFFFFF) | (lightAlpha << 24), 0x22000000);
        }

        int lInnerX = leftHingeX + visibleW;
        graphics.fill(leftHingeX, doorTop - swingDepth / 2, lInnerX, doorBottom + swingDepth / 2, 0xFF144D1B);
        graphics.renderOutline(leftHingeX, doorTop - swingDepth / 2, visibleW, (doorBottom - doorTop) + swingDepth, 0xFFFFFFFF);
        if (visibleW > 14) {
            for (int b = 1; b <= 3; b++) {
                int bx = leftHingeX + b * (visibleW / 4);
                graphics.fill(bx, doorTop - swingDepth / 2, bx + 2, doorBottom + swingDepth / 2, 0xFF9E9E9E);
            }
            graphics.fill(lInnerX - 5, doorTop - swingDepth / 2, lInnerX - 1, doorBottom + swingDepth / 2, 0xFFFFD700);
        }
        graphics.fill(leftHingeX - 3, doorTop + 6, leftHingeX, doorTop + 20, 0xFFFFD700);
        graphics.fill(leftHingeX - 3, doorBottom - 20, leftHingeX, doorBottom - 6, 0xFFFFD700);

        int rInnerX = rightHingeX - visibleW;
        graphics.fill(rInnerX, doorTop - swingDepth / 2, rightHingeX, doorBottom + swingDepth / 2, 0xFF144D1B);
        graphics.renderOutline(rInnerX, doorTop - swingDepth / 2, visibleW, (doorBottom - doorTop) + swingDepth, 0xFFFFFFFF);
        if (visibleW > 14) {
            for (int b = 1; b <= 3; b++) {
                int bx = rInnerX + b * (visibleW / 4);
                graphics.fill(bx, doorTop - swingDepth / 2, bx + 2, doorBottom + swingDepth / 2, 0xFF9E9E9E);
            }
            graphics.fill(rInnerX + 1, doorTop - swingDepth / 2, rInnerX + 5, doorBottom + swingDepth / 2, 0xFFFFD700);
        }
        graphics.fill(rightHingeX, doorTop + 6, rightHingeX + 3, doorTop + 20, 0xFFFFD700);
        graphics.fill(rightHingeX, doorBottom - 20, rightHingeX + 3, doorBottom - 6, 0xFFFFD700);

        if (gateOpenProgress > 0.15F) {
            com.soybe.awakeningsystem.uma.UmaProfile bestProfile = null;
            int bestRarity = 1;
            for (ItemStack res : resultItems) {
                if (CompanionContractItem.isUmaContract(res)) {
                    com.soybe.awakeningsystem.uma.UmaProfile p = CompanionContractItem.getUmaProfile(res);
                    int r = CompanionContractItem.getUmaRarity(res);
                    if (bestProfile == null || r > bestRarity) {
                        bestProfile = p;
                        bestRarity = r;
                    }
                }
            }

            float runProg = Math.min(1.0F, (elapsed - 1200L) / 1800.0F);
            int runnerScale = (int) (32 + runProg * 22);
            int runnerY = (int) (doorBottom - 12 + runProg * 18);

            int auraSize = (int) (32 + Math.sin(elapsed * 0.02) * 10);
            graphics.fill(centerX - auraSize, runnerY - auraSize - 20, centerX + auraSize, runnerY + auraSize, (glowColor & 0x00FFFFFF) | 0x88000000);

            if (bestProfile != null) {
                com.soybe.awakeningsystem.uma.UmaRacerEntity runner = getOrCreateDummyUma(bestProfile);
                if (runner != null) {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            graphics, centerX - 90, runnerY - 145, centerX + 90, runnerY + 35, runnerScale, 0.0625F,
                            (float) mouseX, (float) mouseY, runner
                    );
                }
            } else {
                ItemStack caratStack = SystemItems.CARAT.isBound() ? new ItemStack(SystemItems.CARAT.get()) : new ItemStack(net.minecraft.world.item.Items.CARROT);
                graphics.pose().pushPose();
                graphics.pose().translate(centerX, runnerY, 100);
                graphics.pose().scale(2.2F, 2.2F, 2.2F);
                graphics.renderItem(caratStack, -8, -8);
                graphics.pose().popPose();
            }

            for (int sl = 0; sl < 20; sl++) {
                double angle = sl * (Math.PI * 2.0 / 20.0) + (elapsed * 0.003);
                int r1 = (int) (50 + runProg * 60);
                int r2 = r1 + (int) (40 + (sl % 3) * 35);
                int sx1 = centerX + (int) (Math.cos(angle) * r1 * 1.5);
                int sy1 = runnerY + (int) (Math.sin(angle) * r1 * 0.6);
                int sx2 = centerX + (int) (Math.cos(angle) * r2 * 1.5);
                int sy2 = runnerY + (int) (Math.sin(angle) * r2 * 0.6);
                graphics.fill(Math.min(sx1, sx2), Math.min(sy1, sy2), Math.max(sx1, sx2) + 2, Math.max(sy1, sy2) + 2, glowColor);
            }

            for (int c = 0; c < 24; c++) {
                int cx = (c * 37 + (int) (elapsed * 0.08)) % Math.max(1, w);
                int cy = ((int) (elapsed * 0.15) + c * 29) % Math.max(1, h);
                int cColor = (c % 2 == 0) ? glowColor : 0xFFFFFFFF;
                graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, cColor);
            }
        }

        String splash;
        String subNotice;
        if (elapsed < 1200L) {
            splash = Component.translatable("gui.awakening_system.gacha.anim.uma_ready_title").getString();
            subNotice = Component.translatable("gui.awakening_system.gacha.anim.uma_ready_sub").getString();
        } else {
            if (highestWonRarity >= 5) {
                splash = Component.translatable("gui.awakening_system.gacha.anim.uma_ssr_title").getString();
                subNotice = Component.translatable("gui.awakening_system.gacha.anim.uma_ssr_sub").getString();
            } else if (highestWonRarity == 4) {
                splash = Component.translatable("gui.awakening_system.gacha.anim.uma_sr_title").getString();
                subNotice = Component.translatable("gui.awakening_system.gacha.anim.uma_sr_sub").getString();
            } else {
                splash = Component.translatable("gui.awakening_system.gacha.anim.uma_r_title").getString();
                subNotice = Component.translatable("gui.awakening_system.gacha.anim.uma_r_sub").getString();
            }
        }
        graphics.drawCenteredString(this.font, splash, w / 2, 22, 0xFFFFFF);
        graphics.drawCenteredString(this.font, subNotice, w / 2, 36, 0xCCCCCC);

        if (elapsed >= 3600L) {
            this.screenState = STATE_REVEAL_SINGLE;
            this.currentRevealIndex = 0;
            this.revealStartTime = System.currentTimeMillis();
            if (!resultItems.isEmpty()) {
                playRevealSound(resultItems.get(0), (0 < itemRarities.size()) ? itemRarities.get(0) : getItemStarRarity(resultItems.get(0)));
            }
        }
    }

    private void renderAstralExpressWarpCinematic(GuiGraphics graphics, int mouseX, int mouseY) {
        long elapsed = System.currentTimeMillis() - this.meteorStartTime;
        renderSkipButton(graphics, mouseX, mouseY);

        int w = getVirtW();
        int h = getVirtH();
        int centerX = w / 2;
        int centerY = h / 2;

        int glowColor;
        int coreColor;
        if (highestWonRarity >= 5) {
            glowColor = 0xFFFFD700;
            coreColor = 0xFFFFF8DC;
        } else if (highestWonRarity == 4) {
            glowColor = 0xFFBA68C8;
            coreColor = 0xFFF3E5F5;
        } else {
            glowColor = 0xFF4FC3F7;
            coreColor = 0xFFE1F5FE;
        }

        if (elapsed < 2000L) {
            float t = (float) elapsed / 2000.0F;

            graphics.fillGradient(0, 0, w, h, 0xFF050714, 0xFF180A2E);

            for (int ring = 1; ring <= 4; ring++) {
                int ringR = (int) (ring * 50 + t * 40);
                int ringCol = (glowColor & 0x00FFFFFF) | (0x18000000 / ring);
                graphics.renderOutline(centerX - ringR, centerY - (int)(ringR * 0.75F), ringR * 2, (int)(ringR * 1.5F), ringCol);
            }

            for (int i = 0; i < 54; i++) {
                double angle = (i * (Math.PI * 2.0 / 54.0)) + (elapsed * 0.001);
                float speedMod = 1.0F + (i % 6) * 0.25F;
                float rDist = ((elapsed * 0.55F * speedMod) + i * 37) % (Math.max(w, h) * 0.85F);
                float lineLen = 22.0F + rDist * 0.4F;

                int x1 = centerX + (int) (Math.cos(angle) * rDist);
                int y1 = centerY + (int) (Math.sin(angle) * rDist * 0.75F);
                int x2 = centerX + (int) (Math.cos(angle) * (rDist + lineLen));
                int y2 = centerY + (int) (Math.sin(angle) * (rDist + lineLen) * 0.75F);

                int lineColor = (i % 4 == 0) ? 0xEEFFFFFF : ((i % 4 == 1) ? 0xAA64B5F6 : (glowColor & 0x00FFFFFF | 0x99000000));
                graphics.fill(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2) + 2, Math.max(y1, y2) + 2, lineColor);
            }

            int railVpY = centerY - 25;
            int railBotLeft = centerX - (int) (w * 0.38F);
            int railBotRight = centerX + (int) (w * 0.38F);

            for (int tie = 0; tie < 18; tie++) {
                float tieProgress = ((elapsed * 0.0015F + (float) tie / 18.0F) % 1.0F);
                float tieDepth = tieProgress * tieProgress;
                int ty = railVpY + (int) ((h - railVpY) * tieDepth);
                int txLeft = centerX + (int) ((railBotLeft - centerX) * tieDepth);
                int txRight = centerX + (int) ((railBotRight - centerX) * tieDepth);
                if (txRight > txLeft + 4) {
                    graphics.fill(txLeft, ty, txRight, ty + Math.max(2, (int) (tieDepth * 6)), 0x66FFD700);
                }
            }

            for (int r = 0; r < 2; r++) {
                int rTarget = (r == 0) ? railBotLeft : railBotRight;
                for (int seg = 0; seg < 22; seg++) {
                    float s1 = (float) seg / 22.0F;
                    float s2 = (float) (seg + 1) / 22.0F;
                    int sy1 = railVpY + (int) ((h - railVpY) * (s1 * s1));
                    int sy2 = railVpY + (int) ((h - railVpY) * (s2 * s2));
                    int sx1 = centerX + (int) ((rTarget - centerX) * (s1 * s1));
                    int sx2 = centerX + (int) ((rTarget - centerX) * (s2 * s2));
                    graphics.fill(Math.min(sx1, sx2) - 1, sy1, Math.max(sx1, sx2) + 3, sy2, 0xFFFFD700);
                    graphics.fill(Math.min(sx1, sx2), sy1, Math.max(sx1, sx2) + 1, sy2, 0xFFFFFFFF);
                }
            }

            float trainScale = 0.18F + t * 0.98F;
            int trainW = (int) (240 * trainScale);
            int trainH = (int) (150 * trainScale);
            int trainY = railVpY + (int) ((centerY - railVpY + 28) * (t * t));

            int tl = centerX - trainW / 2;
            int tr = centerX + trainW / 2;
            int tt = trainY - trainH / 2;
            int tb = trainY + trainH / 2;

            graphics.fill(tl, tt + (int) (trainH * 0.22F), tr, tb, 0xFF141724);
            graphics.fillGradient(tl + (int)(15 * trainScale), tt, tr - (int)(15 * trainScale), tt + (int) (trainH * 0.42F), 0xFF2D324D, 0xFF161928);
            graphics.renderOutline(tl, tt + (int) (trainH * 0.22F), trainW, (int) (trainH * 0.78F), 0xFFFFD700);

            int wingW = (int) (24 * trainScale);
            graphics.fill(tl - wingW, tb - (int) (30 * trainScale), tl, tb, 0xFFFFD700);
            graphics.fill(tr, tb - (int) (30 * trainScale), tr + wingW, tb, 0xFFFFD700);

            graphics.fill(tl + 5, tb - (int) (14 * trainScale), tr - 5, tb, 0xFFFFD700);
            graphics.fill(tl + 12, tb - (int) (7 * trainScale), tr - 12, tb, 0xFFFFFFFF);

            int winW = (int) (44 * trainScale);
            int winH = (int) (22 * trainScale);
            int winY = tt + (int) (18 * trainScale);
            graphics.fill(centerX - winW - (int) (8 * trainScale), winY, centerX - (int) (8 * trainScale), winY + winH, 0xFF81D4FA);
            graphics.fill(centerX + (int) (8 * trainScale), winY, centerX + winW + (int) (8 * trainScale), winY + winH, 0xFF81D4FA);

            int starSize = (int) (14 * trainScale);
            int starY = tt + (int) (trainH * 0.48F);
            graphics.fill(centerX - starSize, starY - 2, centerX + starSize, starY + 2, 0xFFFFD700);
            graphics.fill(centerX - 2, starY - starSize, centerX + 2, starY + starSize, 0xFFFFD700);
            graphics.fill(centerX - (int)(starSize * 0.65F), starY - (int)(starSize * 0.65F), centerX + (int)(starSize * 0.65F), starY + (int)(starSize * 0.65F), 0xFFFFF9C4);

            int lampRadius = (int) (16 * trainScale + Math.sin(elapsed * 0.02) * 4);
            int lampY = tt + (int) (trainH * 0.62F);

            int beamAlpha = Math.min(180, (int) (t * 220));
            graphics.fill(0, lampY - 2, w, lampY + 2, (glowColor & 0x00FFFFFF) | (beamAlpha << 24));
            graphics.fill(centerX - (int)(w * 0.3F), lampY - 1, centerX + (int)(w * 0.3F), lampY + 1, 0xFFFFFFFF);

            int auraR = (int) (lampRadius * 4.5F);
            graphics.fill(centerX - auraR, lampY - auraR, centerX + auraR, lampY + auraR, (glowColor & 0x00FFFFFF) | 0x44000000);
            graphics.fill(centerX - lampRadius * 2, lampY - lampRadius * 2, centerX + lampRadius * 2, lampY + lampRadius * 2, (coreColor & 0x00FFFFFF) | 0x99000000);
            graphics.fill(centerX - lampRadius, lampY - lampRadius, centerX + lampRadius, lampY + lampRadius, 0xFFFFFFFF);

            graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.anim.hsr_warp_title").getString(), centerX, 24, 0xFFFFFF);
            graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.anim.hsr_warp_sub").getString(), centerX, 38, 0xFFFFD700);

        } else {
            long doorElapsed = elapsed - 2000L;

            graphics.fillGradient(0, 0, w, h, 0xFF0D101D, 0xFF1F172E);

            int doorW = Math.min(340, w - 40);
            int doorH = Math.min(230, h - 70);
            int doorTop = centerY - doorH / 2 + 10;
            int doorBottom = doorTop + doorH;
            int doorLeft = centerX - doorW / 2;
            int doorRight = centerX + doorW / 2;

            graphics.fill(doorLeft - 10, doorTop - 12, doorRight + 10, doorBottom + 6, 0xFF141824);
            graphics.renderOutline(doorLeft - 10, doorTop - 12, doorW + 20, doorH + 18, 0xFFFFD700);

            graphics.fill(centerX - 60, doorTop - 20, centerX + 60, doorTop - 4, 0xFF2A1B40);
            graphics.renderOutline(centerX - 60, doorTop - 20, 120, 16, 0xFFFFD700);
            graphics.drawCenteredString(this.font, "§e★ ASTRAL EXPRESS ★", centerX, doorTop - 17, 0xFFFFD700);

            float openProgress = 0.0F;
            if (doorElapsed >= 600L) {
                openProgress = Math.min(1.0F, (doorElapsed - 600L) / 900.0F);
            }
            int slideOffset = (int) ((doorW / 2 - 8) * openProgress);

            int openLeft = centerX - slideOffset;
            int openRight = centerX + slideOffset;
            graphics.fill(doorLeft, doorTop, doorRight, doorBottom, 0xFF050711);

            if (openRight > openLeft) {
                int lightAlpha = (int) (openProgress * 230);
                graphics.fillGradient(openLeft, doorTop, openRight, doorBottom, (coreColor & 0x00FFFFFF) | (lightAlpha << 24), (glowColor & 0x00FFFFFF) | 0x22000000);

                for (int b = 0; b < 7; b++) {
                    int bx = openLeft + (int) ((openRight - openLeft) * (b / 6.0F));
                    graphics.fill(bx - 3, doorTop, bx + 3, doorBottom, (coreColor & 0x00FFFFFF) | 0x55000000);
                }

                int pomY = centerY + 24;
                int pomBodyW = (int) (16 * openProgress);
                int pomBodyH = (int) (20 * openProgress);
                graphics.fill(centerX - pomBodyW, pomY - pomBodyH, centerX + pomBodyW, pomY, 0xFFE0E0E0);
                graphics.fill(centerX - pomBodyW + 2, pomY - (int)(pomBodyH * 0.7F), centerX + pomBodyW - 2, pomY, 0xFFC62828);
                graphics.fill(centerX - pomBodyW - 2, pomY - pomBodyH - 7, centerX + pomBodyW + 2, pomY - pomBodyH, 0xFFB71C1C);
                graphics.fill(centerX - pomBodyW, pomY - pomBodyH - 11, centerX + pomBodyW, pomY - pomBodyH - 7, 0xFFC62828);
                graphics.fill(centerX - 3, pomY - pomBodyH - 9, centerX + 3, pomY - pomBodyH - 6, 0xFFFFD700);
                graphics.fill(centerX - pomBodyW - 5, pomY - pomBodyH - 3, centerX - pomBodyW - 1, pomY - 3, 0xFFECEFF1);
                graphics.fill(centerX + pomBodyW + 1, pomY - pomBodyH - 3, centerX + pomBodyW + 5, pomY - 3, 0xFFECEFF1);

                int ticketW = (int) (26 * openProgress);
                int ticketH = (int) (15 * openProgress);
                int ticketY = centerY - 24;
                graphics.fill(centerX - ticketW, ticketY - ticketH, centerX + ticketW, ticketY + ticketH, (glowColor & 0x00FFFFFF) | 0xDD000000);
                graphics.renderOutline(centerX - ticketW, ticketY - ticketH, ticketW * 2, ticketH * 2, 0xFFFFFFFF);
                graphics.drawCenteredString(this.font, (highestWonRarity >= 5) ? "§6§l★ 5★ ★" : ((highestWonRarity == 4) ? "§d§l★ 4★ ★" : "§b§l★ 3★ ★"), centerX, ticketY - 4, 0xFFFFFF);

                for (int sp = 0; sp < 28; sp++) {
                    double spAngle = sp * (Math.PI * 2.0 / 28.0) + (doorElapsed * 0.004);
                    int spDist = (int) ((openProgress * 80) + (sp % 4) * 18);
                    int sx = centerX + (int) (Math.cos(spAngle) * spDist * 1.3);
                    int sy = centerY + (int) (Math.sin(spAngle) * spDist * 0.85);
                    int spCol = (sp % 2 == 0) ? coreColor : glowColor;
                    graphics.fill(sx - 2, sy - 2, sx + 2, sy + 2, spCol);
                }
            }

            int halfW = doorW / 2;

            int lDoorLeft = doorLeft - slideOffset;
            int lDoorRight = centerX - slideOffset;
            if (lDoorRight > doorLeft) {
                graphics.fill(doorLeft, doorTop, lDoorRight, doorBottom, 0xFF1C2236);
                graphics.renderOutline(doorLeft, doorTop, lDoorRight - doorLeft, doorH, 0xFF607D8B);
                graphics.fill(lDoorRight - 6, doorTop, lDoorRight, doorBottom, 0xFFFFD700);
            }

            int rDoorLeft = centerX + slideOffset;
            int rDoorRight = doorRight + slideOffset;
            if (rDoorLeft < doorRight) {
                graphics.fill(rDoorLeft, doorTop, doorRight, doorBottom, 0xFF1C2236);
                graphics.renderOutline(rDoorLeft, doorTop, doorRight - rDoorLeft, doorH, 0xFF607D8B);
                graphics.fill(rDoorLeft, doorTop, rDoorLeft + 6, doorBottom, 0xFFFFD700);
            }

            if (openProgress < 0.6F) {
                float wheelAlpha = 1.0F - (openProgress / 0.6F);
                int wheelCol = ((int) (wheelAlpha * 255) << 24) | 0x00FFD700;
                float rotAngle = Math.min(1.0F, doorElapsed / 500.0F) * (float) (Math.PI * 1.5);
                int wheelR = 24;

                graphics.fill(centerX - wheelR, centerY - wheelR, centerX + wheelR, centerY + wheelR, (wheelCol & 0x00FFFFFF) | 0x33000000);
                graphics.renderOutline(centerX - wheelR, centerY - wheelR, wheelR * 2, wheelR * 2, wheelCol);

                for (int spk = 0; spk < 4; spk++) {
                    double sa = rotAngle + spk * (Math.PI / 2.0);
                    int spx = centerX + (int) (Math.cos(sa) * (wheelR - 3));
                    int spy = centerY + (int) (Math.sin(sa) * (wheelR - 3));
                    graphics.fill(Math.min(centerX, spx), Math.min(centerY, spy), Math.max(centerX, spx) + 2, Math.max(centerY, spy) + 2, wheelCol);
                }
                graphics.fill(centerX - 5, centerY - 5, centerX + 5, centerY + 5, wheelCol);
            }

            if (doorElapsed >= 450L && doorElapsed <= 1600L) {
                for (int v = 0; v < 16; v++) {
                    int vx = (v % 2 == 0) ? (openLeft - 8 - (v * 3)) : (openRight + 8 + (v * 3));
                    int vy = doorTop + 20 + (v * 11) + (int) (Math.sin(doorElapsed * 0.03 + v) * 8);
                    graphics.fill(vx - 4, vy - 4, vx + 4, vy + 4, 0x55FFFFFF);
                }
            }

            String titleText;
            String subText;
            if (doorElapsed < 600L) {
                titleText = Component.translatable("gui.awakening_system.gacha.anim.hsr_door_title").getString();
                subText = Component.translatable("gui.awakening_system.gacha.anim.hsr_door_sub").getString();
            } else {
                if (highestWonRarity >= 5) {
                    titleText = Component.translatable("gui.awakening_system.gacha.anim.hsr_gold_title").getString();
                    subText = Component.translatable("gui.awakening_system.gacha.anim.hsr_gold_sub").getString();
                } else if (highestWonRarity == 4) {
                    titleText = Component.translatable("gui.awakening_system.gacha.anim.hsr_purple_title").getString();
                    subText = Component.translatable("gui.awakening_system.gacha.anim.hsr_purple_sub").getString();
                } else {
                    titleText = Component.translatable("gui.awakening_system.gacha.anim.hsr_blue_title").getString();
                    subText = Component.translatable("gui.awakening_system.gacha.anim.hsr_blue_sub").getString();
                }
            }

            graphics.drawCenteredString(this.font, titleText, centerX, 22, 0xFFFFFF);
            graphics.drawCenteredString(this.font, subText, centerX, 36, 0xCCCCCC);
        }

        if (elapsed >= 3800L) {
            this.screenState = STATE_REVEAL_SINGLE;
            this.currentRevealIndex = 0;
            this.revealStartTime = System.currentTimeMillis();
            if (!resultItems.isEmpty()) {
                playRevealSound(resultItems.get(0), (0 < itemRarities.size()) ? itemRarities.get(0) : getItemStarRarity(resultItems.get(0)));
            }
        }
    }

    private void renderSingleReveal(GuiGraphics graphics, int mouseX, int mouseY) {
        if (resultItems.isEmpty() || currentRevealIndex >= resultItems.size()) {
            this.screenState = STATE_SUMMARY;
            playResultListSound();
            return;
        }

        ItemStack item = resultItems.get(currentRevealIndex);
        int stars = (currentRevealIndex < itemRarities.size()) ? itemRarities.get(currentRevealIndex) : getItemStarRarity(item);

        renderSkipButton(graphics, mouseX, mouseY);

        boolean isUma = (activeBanner == BANNER_UMA) || CompanionContractItem.isUmaContract(item);
        if (isUma) {
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.reveal.uma_count", (currentRevealIndex + 1), resultItems.size()), 20, 20, 0x81C784, false);
            graphics.drawCenteredString(this.font, "§6§l✦ OFFICIAL FILE • TRACEN ACADEMY PRETTY DERBY ✦", getVirtW() / 2, 20, 0xFFFFD700);
        } else {
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.reveal.item_count", (currentRevealIndex + 1), resultItems.size()), 20, 20, 0xAAAAAA, false);
        }
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.reveal.click_continue"), getVirtW() / 2, getVirtH() - 28, 0x888888);

        long timeSinceReveal = System.currentTimeMillis() - this.revealStartTime;
        float pulse = (float) Math.sin(timeSinceReveal * 0.005D);

        int cx = getVirtW() / 2;
        int cy = getVirtH() / 2 - 24;

        if (!isUma) {
            int auraOuter = (int) (84 + pulse * 6);
            int auraInner = (int) (58 + pulse * 4);
            int auraColorOuter = (stars == 5 ? 0x22FFD700 : (stars == 4 ? 0x24BA68C8 : 0x1A64B5F6));
            int auraColorInner = (stars == 5 ? 0x44FFD700 : (stars == 4 ? 0x3EBA68C8 : 0x2C64B5F6));
            graphics.fill(cx - auraOuter, cy - auraOuter, cx + auraOuter, cy + auraOuter, auraColorOuter);
            graphics.fill(cx - auraInner, cy - auraInner, cx + auraInner, cy + auraInner, auraColorInner);
        }

        if (CompanionContractItem.isUmaContract(item)) {
            com.soybe.awakeningsystem.uma.UmaProfile umaProf = CompanionContractItem.getUmaProfile(item);
            int umaRarity = CompanionContractItem.getUmaRarity(item);
            boolean umaNew = CompanionContractItem.isUmaNew(item);
            int umaShards = CompanionContractItem.getUmaShards(item);

            com.soybe.awakeningsystem.uma.UmaRacerEntity dummyUma = getOrCreateDummyUma(umaProf);
            if (dummyUma != null) {
                int pedY = cy + 42;
                int pedW = 54;
                graphics.fill(cx - pedW, pedY, cx + pedW, pedY + 3, umaRarity == 3 ? 0x88FFD700 : (umaRarity == 2 ? 0x88BA68C8 : 0x882ECC71));
                graphics.fill(cx - pedW + 10, pedY - 1, cx + pedW - 10, pedY + 4, 0xCC2ECC71);
                graphics.fill(cx - pedW + 22, pedY, cx + pedW - 22, pedY + 3, 0xFFFFFFFF);

                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, cx - 80, cy - 120, cx + 80, cy + 42, 50, 0.0625F,
                        (float) mouseX, (float) mouseY, dummyUma
                );
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(cx, cy, 100);
                graphics.pose().scale(4.5F, 4.5F, 4.5F);
                graphics.renderItem(item, -8, -8);
                graphics.pose().popPose();
            }

            String starsTxt = "★".repeat(umaRarity);
            int starsColor = umaRarity == 3 ? 0xFFFFD700 : (umaRarity == 2 ? 0xFFDA70D6 : 0xFF80D8FF);
            graphics.drawCenteredString(this.font, starsTxt, cx, cy + 50, starsColor);

            String displayName = (umaRarity == 3 ? "§6§l" : (umaRarity == 2 ? "§d§l" : "§b§l")) + umaProf.getDisplayName();
            renderCenteredFittedText(graphics, displayName, cx, cy + 64, getVirtW() - 60, 0xFFFFFF);

            String umaTactics = Component.translatable("gui.awakening_system.gacha.reveal.uma_tactics_fmt", umaProf.getTactique(), umaProf.getDistance(), umaProf.getSurface()).getString();
            graphics.drawCenteredString(this.font, umaTactics, cx, cy + 78, 0xCCCCCC);

            String statusMsg = umaNew ? Component.translatable("gui.awakening_system.gacha.summary.new_racer").getString() : Component.translatable("gui.awakening_system.gacha.reveal.uma_shards_obtained", umaShards).getString();
            graphics.drawCenteredString(this.font, statusMsg, cx, cy + 92, umaNew ? 0xFF81C784 : 0xFFFFD700);
            return;
        }

        if (item.getItem() instanceof CompanionContractItem) {
            CompanionProfile profile = CompanionContractItem.getProfile(item);
            CompanionEntity dummy = getOrCreateDummyCompanion(profile);
            if (dummy != null) {
                int pedY = cy + 42;
                int pedW = 54;
                graphics.fill(cx - pedW, pedY, cx + pedW, pedY + 3, stars == 5 ? 0x88FFD700 : 0x88BA68C8);
                graphics.fill(cx - pedW + 10, pedY - 1, cx + pedW - 10, pedY + 4, stars == 5 ? 0xCCFFD700 : 0xCCBA68C8);
                graphics.fill(cx - pedW + 22, pedY, cx + pedW - 22, pedY + 3, 0xFFFFFFFF);

                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, cx - 70, cy - 80, cx + 70, cy + 42, 56, 0.0625F,
                        (float) mouseX, (float) mouseY, dummy
                );
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(cx, cy, 100);
                graphics.pose().scale(4.5F, 4.5F, 4.5F);
                graphics.renderItem(item, -8, -8);
                graphics.pose().popPose();
            }
        } else {
            graphics.pose().pushPose();
            graphics.pose().translate(cx, cy, 100);
            graphics.pose().scale(4.5F, 4.5F, 4.5F);
            graphics.renderItem(item, -8, -8);
            graphics.pose().popPose();
        }

        String starsTxt = "★".repeat(stars);
        int starsColor = stars == 5 ? 0xFFFFD700 : (stars == 4 ? 0xFFDA70D6 : 0xFF80D8FF);
        graphics.drawCenteredString(this.font, starsTxt, cx, cy + 50, starsColor);

        String displayName;
        if (item.getItem() instanceof CompanionContractItem) {
            CompanionProfile profile = CompanionContractItem.getProfile(item);
            displayName = profile.colorCode() + "§l" + profile.displayName();
        } else {
            displayName = item.getHoverName().getString();
        }
        renderCenteredFittedText(graphics, displayName, cx, cy + 64, getVirtW() - 60, stars == 5 ? 0xFFFFD700 : (stars == 4 ? 0xFFFFFFFF : 0xEEEEEE));

        String weaponBadge = getWeaponTypeBadge(item, stars);
        graphics.drawCenteredString(this.font, weaponBadge, cx, cy + 78, 0xCCCCCC);

        String lore = getWeaponLorePreview(item);
        renderCenteredFittedText(graphics, "§7\"" + lore + "\"", cx, cy + 92, Math.min(480, getVirtW() - 40), 0x999999);
    }

    private void renderUmaGachaSummaryScreen(GuiGraphics graphics, int mouseX, int mouseY) {
        int virtW = getVirtW();
        int virtH = getVirtH();
        int total = resultItems.size();

        int headerW = 210;
        int headerH = 18;
        int headerX = virtW / 2 - headerW / 2;
        int headerY = 10;
        graphics.fill(headerX, headerY, headerX + headerW, headerY + headerH, 0xFFFFFFFF);
        graphics.fill(headerX, headerY, headerX + 6, headerY + headerH, 0xFF2ECC71);
        graphics.renderOutline(headerX, headerY, headerW, headerH, 0xFFD1D5DB);
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.summary.tracen"), virtW / 2, headerY + 5, 0x1B5E20);

        if (total == 1) {
            int cardW = 60;
            int cardH = 56;
            int cX = virtW / 2 - cardW / 2;
            int cY = virtH / 2 - cardH / 2 - 24;
            renderUmaResultCard(graphics, resultItems.get(0), 0, cX, cY, cardW, cardH, mouseX, mouseY);
            renderUmaSparkBar(graphics, virtW, cY + cardH + 18, total);
            renderUmaResultButtons(graphics, mouseX, mouseY, virtW, getUmaResultBtnY(virtH, total));
            if (mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH) {
                graphics.renderTooltip(this.font, resultItems.get(0), mouseX, mouseY);
            }
            return;
        }

        int cardW = 50;
        int cardH = 46;
        int gapX = 18;
        int rowStepY = 58;
        int startY = 32;

        int span3W = 3 * cardW + 2 * gapX;
        int r3X = virtW / 2 - span3W / 2;

        int span2W = 2 * cardW + gapX;
        int r2X = virtW / 2 - span2W / 2;

        ItemStack hoveredCard = null;
        for (int i = 0; i < total; i++) {
            int cX;
            int cY;

            if (i < 3) {
                cX = r3X + i * (cardW + gapX);
                cY = startY;
            } else if (i < 5) {
                cX = r2X + (i - 3) * (cardW + gapX);
                cY = startY + rowStepY;
            } else if (i < 8) {
                cX = r3X + (i - 5) * (cardW + gapX);
                cY = startY + 2 * rowStepY;
            } else if (i < 10) {
                cX = r2X + (i - 8) * (cardW + gapX);
                cY = startY + 3 * rowStepY;
            } else {
                cX = r3X + ((i - 10) % 3) * (cardW + gapX);
                cY = startY + 4 * rowStepY;
            }

            renderUmaResultCard(graphics, resultItems.get(i), i, cX, cY, cardW, cardH, mouseX, mouseY);
            if (mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH) {
                hoveredCard = resultItems.get(i);
            }
        }

        int barY = startY + 4 * rowStepY + 2;
        renderUmaSparkBar(graphics, virtW, barY, total);

        int btnY = getUmaResultBtnY(virtH, total);
        renderUmaResultButtons(graphics, mouseX, mouseY, virtW, btnY);

        if (hoveredCard != null) {
            graphics.renderTooltip(this.font, hoveredCard, mouseX, mouseY);
        }
    }

    private void renderUmaResultCard(GuiGraphics graphics, ItemStack stack, int index, int cX, int cY, int cardW, int cardH, int mouseX, int mouseY) {
        com.soybe.awakeningsystem.uma.UmaProfile up = CompanionContractItem.getUmaProfile(stack);
        int umaRarity = CompanionContractItem.getUmaRarity(stack);
        boolean umaNew = CompanionContractItem.isUmaNew(stack);
        int shards = CompanionContractItem.getUmaShards(stack);
        int statues = CompanionContractItem.getUmaStatues(stack);

        graphics.fill(cX, cY, cX + cardW, cY + cardH, 0xFFFFFFFF);
        graphics.fill(cX + 2, cY + 2, cX + cardW - 2, cY + cardH - 2, 0xFFF8FAFC);

        if (umaRarity == 3) {
            long t = System.currentTimeMillis();
            int[] rainbowCols = { 0xFFFF6B6B, 0xFFFFA07A, 0xFFFFE66D, 0xFF4ECDC4, 0xFF45B7D1, 0xFF96CEB4, 0xFFD4A5A5, 0xFFBA68C8 };
            int cycle = (int) ((t / 140 + index * 2) % rainbowCols.length);
            int r1 = rainbowCols[cycle];
            int r2 = rainbowCols[(cycle + 4) % rainbowCols.length];
            graphics.renderOutline(cX - 1, cY - 1, cardW + 2, cardH + 2, r1);
            graphics.renderOutline(cX, cY, cardW, cardH, r2);

            graphics.drawString(this.font, "✦", cX - 3, cY - 4, 0xFFFFE082, false);
            graphics.drawString(this.font, "✦", cX + cardW - 5, cY - 4, 0xFF80D8FF, false);
            graphics.drawString(this.font, "✦", cX + cardW - 5, cY + cardH - 5, 0xFFFF80BF, false);
            graphics.drawString(this.font, "✦", cX - 3, cY + cardH - 5, 0xFF80FFD0, false);
        } else if (umaRarity == 2) {
            graphics.renderOutline(cX, cY, cardW, cardH, 0xFFBA68C8);
            graphics.renderOutline(cX + 1, cY + 1, cardW - 2, cardH - 2, 0x44BA68C8);
        } else {
            graphics.renderOutline(cX, cY, cardW, cardH, 0xFFCBD5E1);
        }

        int bustSize = 26;
        int bustX = cX + (cardW - bustSize) / 2;
        int bustY = cY + 3;
        renderUmaBust(graphics, up.getTextureLocation(), bustX, bustY, bustSize);

        if (umaNew) {
            graphics.fill(cX - 2, cY - 3, cX + 26, cY + 6, 0xFFE53935);
            graphics.renderOutline(cX - 2, cY - 3, 28, 9, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.gacha.summary.new"), cX, cY - 3, 0xFFFFFF, false);
        }

        String goldenStars = "★".repeat(umaRarity);
        String emptyStars = "☆".repeat(5 - umaRarity);
        graphics.drawCenteredString(this.font, "§6" + goldenStars + "§7" + emptyStars, cX + cardW / 2, cY + cardH - 8, 0xFFFFD700);

        graphics.drawString(this.font, "§e🗽x" + statues, cX + 1, cY + cardH + 2, 0xFFFFFFFF, true);

        int tagX = cX + cardW - 24;
        graphics.fill(tagX, cY + cardH + 2, tagX + 10, cY + cardH + 8, 0xFFE53935);
        graphics.drawString(this.font, "§f+", tagX + 2, cY + cardH + 1, 0xFFFFFF, false);
        graphics.drawString(this.font, "§6x" + shards, tagX + 12, cY + cardH + 2, 0xFFFFA726, true);
    }

    private void renderUmaSparkBar(GuiGraphics graphics, int virtW, int barY, int totalPulls) {
        int barW = 230;
        int barH = 13;
        int barX = virtW / 2 - barW / 2;

        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFFFFFFFF);
        graphics.renderOutline(barX, barY, barW, barH, 0xFFD1D5DB);
        graphics.drawString(this.font, "§8Racer Spark Points", barX + 6, barY + 3, 0x374151, false);
        int prevPt = Math.max(0, totalWishes - totalPulls);
        graphics.drawString(this.font, "§8" + prevPt + " §7▷ §e§l" + totalWishes, barX + barW - 55, barY + 3, 0xD97706, false);

        graphics.drawCenteredString(this.font, "§7Duplicate racers grant Star Pieces and Goddess Statues", virtW / 2, barY + 15, 0x475569);
    }

    private static final int UMA_RESULT_BTN_H = 22;
    private static final int UMA_RESULT_BACK_W = 86;
    private static final int UMA_RESULT_AGAIN_W = 140;

    private int getUmaResultBtnY(int virtH, int total) {
        if (total == 1) {
            int cardH = 56;
            int cY = virtH / 2 - cardH / 2 - 24;
            return cY + cardH + 46;
        } else {
            int startY = 32;
            int rowStepY = 58;
            int barY = startY + 4 * rowStepY + 2;
            return barY + 26;
        }
    }

    private int getUmaResultBackX(int virtW) {
        return virtW / 2 - UMA_RESULT_BACK_W - 10;
    }

    private int getUmaResultAgainX(int virtW) {
        return virtW / 2 + 10;
    }

    private void renderUmaResultButtons(GuiGraphics graphics, int mouseX, int mouseY, int virtW, int btnY) {
        int backX = getUmaResultBackX(virtW);
        boolean hBack = isUmaResultBackHover(mouseX, mouseY, backX, btnY, UMA_RESULT_BACK_W, UMA_RESULT_BTN_H);
        graphics.fill(backX, btnY, backX + UMA_RESULT_BACK_W, btnY + UMA_RESULT_BTN_H, hBack ? 0xFFF8FAFC : 0xFFFFFFFF);
        graphics.fill(backX, btnY + UMA_RESULT_BTN_H - 2, backX + UMA_RESULT_BACK_W, btnY + UMA_RESULT_BTN_H, 0xFFE2E8F0);
        graphics.renderOutline(backX, btnY, UMA_RESULT_BACK_W, UMA_RESULT_BTN_H, hBack ? 0xFF2563EB : 0xFF94A3B8);
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.btn_back_icon"), backX + UMA_RESULT_BACK_W / 2, btnY + 7, 0x1E293B);

        int againX = getUmaResultAgainX(virtW);
        boolean hAgain = isUmaResultAgainHover(mouseX, mouseY, againX, btnY, UMA_RESULT_AGAIN_W, UMA_RESULT_BTN_H);
        graphics.fill(againX, btnY, againX + UMA_RESULT_AGAIN_W, btnY + UMA_RESULT_BTN_H, hAgain ? 0xFFB4F23D : 0xFF84CC16);
        graphics.fill(againX, btnY + UMA_RESULT_BTN_H - 3, againX + UMA_RESULT_AGAIN_W, btnY + UMA_RESULT_BTN_H, 0xFF4D7C0F);
        graphics.renderOutline(againX, btnY, UMA_RESULT_AGAIN_W, UMA_RESULT_BTN_H, hAgain ? 0xFFFFFFFF : 0xFF365314);
        int cost = lastPullCount == 1 ? 150 : 1500;
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.uma_pull_again", cost), againX + UMA_RESULT_AGAIN_W / 2, btnY + 7, 0xFFFFFF);
    }

    private boolean isUmaResultBackHover(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x - 2 && mouseX <= x + w + 2 && mouseY >= y - 2 && mouseY <= y + h + 2;
    }

    private boolean isUmaResultAgainHover(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x - 2 && mouseX <= x + w + 2 && mouseY >= y - 2 && mouseY <= y + h + 2;
    }

    private void renderSummaryGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        if (activeBanner == BANNER_UMA) {
            renderUmaGachaSummaryScreen(graphics, mouseX, mouseY);
            return;
        }
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.summary.general"), getVirtW() / 2, 14, 0xFFFFFF);

        int total = resultItems.size();
        int slotW = 76;
        int slotH = 76;
        int spacing = 84;
        int cols = Math.min(5, total);
        int rows = (total + 4) / 5;

        int startX = getVirtW() / 2 - (cols * spacing) / 2 + 4;
        int startY = total <= 5 ? (getVirtH() / 2 - 40) : (getVirtH() / 2 - 82);

        ItemStack hoveredItem = null;

        for (int i = 0; i < total; i++) {
            int row = i / 5;
            int col = i % 5;
            int x = startX + col * spacing;
            int y = startY + row * 84;

            ItemStack stack = resultItems.get(i);
            int stars = (i < itemRarities.size()) ? itemRarities.get(i) : getItemStarRarity(stack);

            boolean isHover = mouseX >= x && mouseX <= x + slotW && mouseY >= y && mouseY <= y + slotH;
            if (isHover) {
                hoveredItem = stack;
            }

            int topBg = stars == 5 ? 0xFF382309 : (stars == 4 ? 0xFF2D103E : 0xFF121E38);
            int botBg = stars == 5 ? 0xFF191004 : (stars == 4 ? 0xFF15061E : 0xFF080D18);
            if (isHover) {
                topBg = stars == 5 ? 0xFF4E320D : (stars == 4 ? 0xFF3D1654 : 0xFF1A2B50);
                botBg = stars == 5 ? 0xFF281907 : (stars == 4 ? 0xFF20092E : 0xFF0E172B);
            }

            graphics.fillGradient(x, y, x + slotW, y + slotH, topBg, botBg);

            int slotBorder = stars == 5 ? 0xFFFFD700 : (stars == 4 ? 0xFFBA68C8 : 0xFF3F69B8);
            graphics.renderOutline(x, y, slotW, slotH, isHover ? 0xFFFFFFFF : slotBorder);
            graphics.renderOutline(x + 1, y + 1, slotW - 2, slotH - 2, stars == 5 ? 0x66FFD700 : (stars == 4 ? 0x66BA68C8 : 0x443F69B8));

            int cx = x + slotW / 2;

            if (CompanionContractItem.isUmaContract(stack)) {
                com.soybe.awakeningsystem.uma.UmaProfile up = CompanionContractItem.getUmaProfile(stack);
                int umaRarity = CompanionContractItem.getUmaRarity(stack);
                boolean umaNew = CompanionContractItem.isUmaNew(stack);

                int faceSize = 36;
                int faceX = cx - faceSize / 2;
                int faceY = y + 7;

                graphics.fill(faceX, faceY, faceX + faceSize, faceY + faceSize, 0x88000000);

                renderCompanionFace(graphics, up.getTextureLocation(), faceX, faceY, faceSize);

                graphics.renderOutline(faceX - 1, faceY - 1, faceSize + 2, faceSize + 2, isHover ? 0xFFFFFFFF : slotBorder);

                graphics.fill(x + 3, y + 3, x + 15, y + 13, 0xDD0B301B);
                graphics.renderOutline(x + 3, y + 3, 12, 10, 0xAA2ECC71);
                graphics.drawCenteredString(this.font, "🥕", x + 9, y + 4, 0xFFFFFF);

                String starsTxt = "★".repeat(umaRarity);
                int sColor = umaRarity == 3 ? 0xFFFFD700 : (umaRarity == 2 ? 0xFFDA70D6 : 0xFF80D8FF);
                graphics.drawCenteredString(this.font, starsTxt, cx, y + 47, sColor);

                String labelName = up.getDisplayName();
                int nameColor = isHover ? 0xFFFFFFFF : (umaRarity == 3 ? 0xFFFFE082 : (umaRarity == 2 ? 0xFFF3E5F5 : 0xDDDDDD));
                renderCenteredFittedText(graphics, labelName, cx, y + 60, slotW - 6, nameColor);

                if (umaNew) {
                    graphics.fill(x + slotW - 22, y + 2, x + slotW - 2, y + 10, 0xDD2ECC71);
                    graphics.drawCenteredString(this.font, "§fNEW", x + slotW - 12, y + 2, 0xFFFFFF);
                }
            } else if (stack.getItem() instanceof CompanionContractItem) {
                CompanionProfile profile = CompanionContractItem.getProfile(stack);

                int faceSize = 36;
                int faceX = cx - faceSize / 2;
                int faceY = y + 7;

                graphics.fill(faceX, faceY, faceX + faceSize, faceY + faceSize, 0x88000000);

                renderCompanionFace(graphics, profile.getTextureLocation(), faceX, faceY, faceSize);

                graphics.renderOutline(faceX - 1, faceY - 1, faceSize + 2, faceSize + 2, isHover ? 0xFFFFFFFF : slotBorder);

                int elemColor = getElementBgColor(profile.element());
                graphics.fill(x + 3, y + 3, x + 15, y + 13, 0xDD000000 | (elemColor & 0x00FFFFFF));
                graphics.renderOutline(x + 3, y + 3, 12, 10, 0xAAFFFFFF);
                String elemInitial = profile.element().substring(0, 1).toUpperCase();
                graphics.drawCenteredString(this.font, elemInitial, x + 9, y + 4, 0xFFFFFF);
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(cx, y + 25, 50);
                graphics.pose().scale(2.1F, 2.1F, 2.1F);
                graphics.renderItem(stack, -8, -8);
                graphics.pose().popPose();
            }

            if (!CompanionContractItem.isUmaContract(stack)) {
                String starsTxt = "★".repeat(stars);
                int sColor = stars == 5 ? 0xFFFFD700 : (stars == 4 ? 0xFFDA70D6 : 0xFF80D8FF);
                graphics.drawCenteredString(this.font, starsTxt, cx, y + 47, sColor);

                String labelName;
                if (stack.getItem() instanceof CompanionContractItem) {
                    CompanionProfile profile = CompanionContractItem.getProfile(stack);
                    labelName = profile != null ? profile.displayName() : stack.getHoverName().getString();
                } else {
                    labelName = stack.getHoverName().getString();
                    labelName = labelName.replaceAll("\\[[★\\d\\s]+\\]", "").trim();
                }
                int nameColor = isHover ? 0xFFFFFFFF : (stars == 5 ? 0xFFFFE082 : (stars == 4 ? 0xFFF3E5F5 : 0xDDDDDD));
                renderCenteredFittedText(graphics, labelName, cx, y + 60, slotW - 6, nameColor);
            }
        }

        int paimonSummaryY = getVirtH() - 76;
        int paimonW = Math.min(520, getVirtW() - 32);
        int paimonX = getVirtW() / 2 - paimonW / 2;
        String paimonReaction;
        if (activeBanner == BANNER_UMA) {
            paimonReaction = highestWonRarity >= 5
                    ? "§a[Tazuna] §f\"¡Felicidades! ¡Has conseguido una corredora 3★ legendaria! Prepárate para entrenarla en el establo.\""
                    : (highestWonRarity == 4
                    ? "§a[Tazuna] §f\"¡Excelente tirada! Una prometedora corredora 2★ se une a tu establo de Tracen.\""
                    : "§a[Tazuna] §f\"¡Buen intento! Las corredoras de 1★ también pueden ascender de estrellas con Talent Bloom.\"");
        } else {
            paimonReaction = highestWonRarity == 5
                    ? Component.translatable("awakening_system.gacha.paimon.reaction_5star").getString()
                    : (highestWonRarity == 4
                    ? Component.translatable("awakening_system.gacha.paimon.reaction_4star").getString()
                    : Component.translatable("awakening_system.gacha.paimon.reaction_3star").getString());
        }
        graphics.fill(paimonX, paimonSummaryY, paimonX + paimonW, paimonSummaryY + 24, 0xFF140D26);
        graphics.renderOutline(paimonX, paimonSummaryY, paimonW, 24, (activeBanner == BANNER_UMA) ? 0xFF2ECC71 : (highestWonRarity == 5 ? 0xFFFFD700 : 0xFFDA70D6));
        renderFittedText(graphics, paimonReaction, paimonX + 12, paimonSummaryY + 8, paimonW - 24, 0xFFFFFF, false);

        int btnW = 125;
        int btnH = 26;
        int btnY = getVirtH() - 42;
        int totalW = btnW * 3 + 16;
        int btnBackX = getVirtW() / 2 - totalW / 2;
        int btnSubspaceX = btnBackX + btnW + 8;
        int btnAgainX = btnSubspaceX + btnW + 8;

        boolean hoverBack = mouseX >= btnBackX && mouseX <= btnBackX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        boolean hoverSubspace = mouseX >= btnSubspaceX && mouseX <= btnSubspaceX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        boolean hoverAgain = mouseX >= btnAgainX && mouseX <= btnAgainX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

        graphics.fill(btnBackX, btnY, btnBackX + btnW, btnY + btnH, hoverBack ? 0xFF3558A8 : 0xFF1B2F63);
        graphics.renderOutline(btnBackX, btnY, btnW, btnH, hoverBack ? 0xFFFFFFFF : 0xFF4A7BD4);
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.btn_confirm_back"), btnBackX + btnW / 2, btnY + 9, 0xFFFFFF);

        graphics.fill(btnSubspaceX, btnY, btnSubspaceX + btnW, btnY + btnH, hoverSubspace ? 0xFF1B4F72 : 0xFF0E2E47);
        graphics.renderOutline(btnSubspaceX, btnY, btnW, btnH, hoverSubspace ? 0xFF5DADE2 : 0xFF2874A6);
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.btn_subspace"), btnSubspaceX + btnW / 2, btnY + 9, hoverSubspace ? 0xFFFFFFFF : 0xFF85C1E9);

        Component againText = lastPullCount == 10
                ? Component.translatable("gui.awakening_system.gacha.pull_10_again")
                : Component.translatable("gui.awakening_system.gacha.pull_1_again");
        graphics.fill(btnAgainX, btnY, btnAgainX + btnW, btnY + btnH, hoverAgain ? 0xFFA07825 : 0xFF664810);
        graphics.renderOutline(btnAgainX, btnY, btnW, btnH, hoverAgain ? 0xFFFFFFFF : 0xFFFFD700);
        graphics.drawCenteredString(this.font, againText, btnAgainX + btnW / 2, btnY + 9, 0xFFFFD700);

        if (hoveredItem != null) {
            graphics.renderTooltip(this.font, hoveredItem, mouseX, mouseY);
        }
    }

    private void renderSkipButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int skipW = 75;
        int skipH = 22;
        int skipX = getVirtW() - skipW - 14;
        int skipY = 14;

        boolean hover = mouseX >= skipX && mouseX <= skipX + skipW && mouseY >= skipY && mouseY <= skipY + skipH;
        graphics.fill(skipX, skipY, skipX + skipW, skipY + skipH, hover ? 0xCC444466 : 0x88222233);
        graphics.renderOutline(skipX, skipY, skipW, skipH, hover ? 0xFFFFFFFF : 0xFFAAAAAA);
        graphics.drawCenteredString(this.font, Component.translatable("gui.awakening_system.gacha.skip_btn"), skipX + skipW / 2, skipY + 7, hover ? 0xFFFFAA : 0xDDDDDD);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        syncDataFromPlayer();

        if (showNewsModal) {
            int mW = Math.min(520, getVirtW() - 20);
            int mH = Math.min(270, getVirtH() - 20);
            int mX = getVirtW() / 2 - mW / 2;
            int mY = getVirtH() / 2 - mH / 2;

            int closeX = mX + mW - 22;
            int closeY = mY + 10;
            boolean clickedClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
            boolean clickedOutside = mouseX < mX || mouseX > mX + mW || mouseY < mY || mouseY > mY + mH;

            if (clickedClose || clickedOutside) {
                showNewsModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                return true;
            }
            return true;
        }

        if (showHistoryModal) {
            int mW = 460;
            int mH = 260;
            int mX = getVirtW() / 2 - mW / 2;
            int mY = getVirtH() / 2 - mH / 2;

            int closeX = mX + mW - 22;
            int closeY = mY + 10;
            boolean clickedClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
            boolean clickedOutside = mouseX < mX || mouseX > mX + mW || mouseY < mY || mouseY > mY + mH;

            int btnPageY = mY + mH - 24;
            int btnPrevX = mX + 14;
            int btnPrevW = 85;
            int btnNextX = mX + mW - 14 - 85;
            int btnNextW = 85;
            int btnH = 16;
            int totalPages = Math.max(1, (int) Math.ceil((double) wishHistory.size() / 7.0));

            if (historyPage > 0 && mouseX >= btnPrevX && mouseX <= btnPrevX + btnPrevW && mouseY >= btnPageY && mouseY <= btnPageY + btnH) {
                historyPage--;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                return true;
            }

            if (historyPage < totalPages - 1 && mouseX >= btnNextX && mouseX <= btnNextX + btnNextW && mouseY >= btnPageY && mouseY <= btnPageY + btnH) {
                historyPage++;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                return true;
            }

            if (clickedClose || clickedOutside) {
                showHistoryModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
            }
            return true;
        }

        if (showTopUpModal) {
            int virtW = getVirtW();
            int virtH = getVirtH();
            int mW = Math.min(520, virtW - 16);
            int mH = Math.min(270, virtH - 16);
            int mX = virtW / 2 - mW / 2;
            int mY = virtH / 2 - mH / 2;

            int backBtnX = mX + 10;
            int backBtnY = mY + 8;
            int backBtnW = 60;
            int backBtnH = 14;
            boolean clickedBack = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH;

            int closeX = mX + mW - 22;
            int closeY = mY + 8;
            boolean clickedClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
            boolean clickedOutside = mouseX < mX || mouseX > mX + mW || mouseY < mY || mouseY > mY + mH;

            if (clickedBack || clickedClose || clickedOutside) {
                showTopUpModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                return true;
            }

            int tabGap = 6;
            int tabW = Math.max(100, (mW - 120) / 3);
            int tabH = 14;
            int tab0X = mX + 76;
            int tab1X = tab0X + tabW + tabGap;
            int tab2X = tab1X + tabW + tabGap;
            int tabY = mY + 8;
            if (mouseX >= tab0X && mouseX <= tab0X + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
                topUpTab = 0;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            if (mouseX >= tab1X && mouseX <= tab1X + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
                topUpTab = 1;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            if (mouseX >= tab2X && mouseX <= tab2X + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
                topUpTab = 2;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            int colGap = 10;
            int colW = (mW - 20 - colGap) / 2;
            int leftX = mX + 10;
            int rightX = leftX + colW + colGap;
            int panelY = mY + 38;

            if (topUpTab == 1) {
                int[] jadeAmounts = {160, 1600, 4800, 16000};
                int[] jadeCoins = {10000, 75000, 200000, 500000};
                int jR_X = leftX + 4;
                int jR_W = colW - 8;
                int jBtnW = Math.min(52, Math.max(40, jR_W / 5));
                int jBtnH = 16;
                int jBtnX = jR_X + jR_W - jBtnW - 4;

                for (int i = 0; i < 4; i++) {
                    int rowY = panelY + 22 + i * 36;
                    int btnY = rowY + 8;
                    if (mouseX >= jBtnX && mouseX <= jBtnX + jBtnW && mouseY >= btnY && mouseY <= btnY + jBtnH) {
                        if (coins >= jadeCoins[i]) {
                            PacketDistributor.sendToServer(new GachaTopUpPayload(8, i));
                            this.coins -= jadeCoins[i];
                            this.stellarJade += jadeAmounts[i];
                            if (Minecraft.getInstance().player != null) {
                                SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                                if (d != null) {
                                    d.setCoins(this.coins);
                                    d.setStellarJade(this.stellarJade);
                                }
                            }
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                        } else {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                        }
                        return true;
                    }
                }

                double[] packUsd = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};
                int[] packJadeAmounts = {60, 330, 1090, 2240, 3880, 8080, 300};
                int crR_X = rightX + 4;
                int crR_W = colW - 8;
                int crBtnW = Math.min(56, Math.max(44, (crR_W - 80) / 3));
                int crBtnH = 15;
                int crBtnUsdX = crR_X + crR_W - crBtnW - 4;

                for (int i = 0; i < 7; i++) {
                    int rowY = panelY + 18 + i * 23;
                    int btnY = rowY + 3;
                    if (mouseX >= crBtnUsdX && mouseX <= crBtnUsdX + crBtnW && mouseY >= btnY && mouseY <= btnY + crBtnH) {
                        if (usdBalance >= packUsd[i] - 0.001) {
                            PacketDistributor.sendToServer(new GachaTopUpPayload(7, i));
                            this.usdBalance = Math.max(0.0, this.usdBalance - packUsd[i]);
                            boolean isWelkinEnh = false;
                            if (Minecraft.getInstance().player != null) {
                                SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                                if (d != null) isWelkinEnh = d.isWelkinEnhanced();
                            }
                            if (i == 6) {
                                int bonusJade = isWelkinEnh ? 450 : 300;
                                this.stellarJade += bonusJade;
                                this.welkinDays += 30;
                            } else {
                                int baseJade = packJadeAmounts[i];
                                boolean isFirstBonus = ((firstJadeTopUpMask & (1 << i)) == 0);
                                int add = isFirstBonus ? (baseJade * 2) : baseJade;
                                this.stellarJade += add;
                                this.firstJadeTopUpMask |= (1 << i);
                            }
                            if (Minecraft.getInstance().player != null) {
                                SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                                if (d != null) {
                                    d.setUsdBalance(this.usdBalance);
                                    d.setStellarJade(this.stellarJade);
                                    if (i == 6) d.setWelkinDays(this.welkinDays);
                                    else d.setClaimedFirstJadeTopUp(i);
                                }
                            }
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F));
                        } else {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                        }
                        return true;
                    }
                }

                int convH = 22;
                int convY = mY + mH - convH - 8;
                int btnConvW = colW;
                int b1X = leftX;
                int b2X = rightX;

                if (mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + convH) {
                    if (genesisCrystals > 0) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(9, 0));
                        this.stellarJade += genesisCrystals;
                        this.genesisCrystals = 0;
                        if (Minecraft.getInstance().player != null) {
                            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                            if (d != null) {
                                d.setGenesisCrystals(0);
                                d.setStellarJade(this.stellarJade);
                            }
                        }
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.3F));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }

                if (mouseX >= b2X && mouseX <= b2X + btnConvW && mouseY >= convY && mouseY <= convY + convH) {
                    if (stellarJade > 0) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(2, 0));
                        this.primogems += stellarJade;
                        this.stellarJade = 0;
                        if (Minecraft.getInstance().player != null) {
                            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                            if (d != null) {
                                d.setPrimogems(this.primogems);
                                d.setStellarJade(0);
                            }
                        }
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.3F));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
                return true;
            }

            if (topUpTab == 2) {
                int[] caratAmounts = {150, 1500, 4500, 15000};
                int[] caratCoins = {10000, 75000, 200000, 500000};
                int cR_X = leftX + 4;
                int cR_W = colW - 8;
                int cBtnW = Math.min(52, Math.max(40, cR_W / 5));
                int cBtnH = 16;
                int cBtnX = cR_X + cR_W - cBtnW - 4;

                for (int i = 0; i < 4; i++) {
                    int rowY = panelY + 22 + i * 36;
                    int btnY = rowY + 8;
                    if (mouseX >= cBtnX && mouseX <= cBtnX + cBtnW && mouseY >= btnY && mouseY <= btnY + cBtnH) {
                        if (coins >= caratCoins[i]) {
                            PacketDistributor.sendToServer(new GachaTopUpPayload(4, i));
                            this.coins -= caratCoins[i];
                            this.carats += caratAmounts[i];
                            if (Minecraft.getInstance().player != null) {
                                SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                                if (d != null) {
                                    d.setCoins(this.coins);
                                    d.setCarats(this.carats);
                                }
                            }
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                        } else {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                        }
                        return true;
                    }
                }

                double[] usdCosts = {0.99, 4.99, 14.99, 29.99, 49.99};
                int[] usdCaratAmounts = {150, 1500, 5000, 12000, 25000};
                int crR_X = rightX + 4;
                int crR_W = colW - 8;
                int crBtnW = Math.min(56, Math.max(44, (crR_W - 80) / 3));
                int crBtnH = 15;
                int crBtnUsdX = crR_X + crR_W - crBtnW - 4;

                for (int i = 0; i < 5; i++) {
                    int rowY = panelY + 20 + i * 28;
                    int btnY = rowY + 4;
                    if (mouseX >= crBtnUsdX && mouseX <= crBtnUsdX + crBtnW && mouseY >= btnY && mouseY <= btnY + crBtnH) {
                        if (usdBalance >= usdCosts[i] - 0.001) {
                            PacketDistributor.sendToServer(new GachaTopUpPayload(5, i));
                            this.usdBalance = Math.max(0.0, this.usdBalance - usdCosts[i]);
                            this.carats += usdCaratAmounts[i];
                            if (Minecraft.getInstance().player != null) {
                                SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                                if (d != null) {
                                    d.setUsdBalance(this.usdBalance);
                                    d.setCarats(this.carats);
                                }
                            }
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F));
                        } else {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                        }
                        return true;
                    }
                }

                int convH = 22;
                int convY = mY + mH - convH - 8;
                int btnConvW = mW - 20;
                int b1X = leftX;
                if (mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + convH) {
                    if (genesisCrystals > 0) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(6, 0));
                        this.carats += genesisCrystals;
                        this.genesisCrystals = 0;
                        if (Minecraft.getInstance().player != null) {
                            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                            if (d != null) {
                                d.setGenesisCrystals(0);
                                d.setCarats(this.carats);
                            }
                        }
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.3F));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
                return true;
            }

            int[] gcVals = {5, 15, 30, 50, 100};
            int[] gcCosts = {25000, 75000, 150000, 250000, 500000};
            int gcR_X = leftX + 4;
            int gcR_W = colW - 8;
            int gcBtnW = Math.min(48, Math.max(38, gcR_W / 5));
            int gcBtnH = 16;
            int gcBtnX = gcR_X + gcR_W - gcBtnW - 4;

            for (int i = 0; i < 5; i++) {
                int rowY = panelY + 20 + i * 32;
                int btnY = rowY + 6;
                if (mouseX >= gcBtnX && mouseX <= gcBtnX + gcBtnW && mouseY >= btnY && mouseY <= btnY + gcBtnH) {
                    if (coins >= gcCosts[i]) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(3, i));
                        this.coins -= gcCosts[i];
                        this.usdBalance += gcVals[i];
                        if (Minecraft.getInstance().player != null) {
                            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                            if (d != null) {
                                d.setCoins(this.coins);
                                d.setUsdBalance(this.usdBalance);
                            }
                        }
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
            }

            double[] packUsd = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};
            int crR_X = rightX + 4;
            int crR_W = colW - 8;
            int crBtnW = Math.min(56, Math.max(44, (crR_W - 80) / 3));
            int crBtnH = 15;
            int crBtnUsdX = crR_X + crR_W - crBtnW - 4;

            for (int i = 0; i < 7; i++) {
                int rowY = panelY + 18 + i * 23;
                int btnY = rowY + 3;

                if (mouseX >= crBtnUsdX && mouseX <= crBtnUsdX + crBtnW && mouseY >= btnY && mouseY <= btnY + crBtnH) {
                    if (usdBalance >= packUsd[i] - 0.001) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(0, i));
                        this.usdBalance = Math.max(0.0, this.usdBalance - packUsd[i]);
                        if (Minecraft.getInstance().player != null) {
                            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                            if (d != null) {
                                d.setUsdBalance(this.usdBalance);
                            }
                        }
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
            }

            int convH = 22;
            int convY = mY + mH - convH - 8;
            int btnConvW = colW;
            int b1X = leftX;
            int b2X = rightX;

            if (mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + convH) {
                if (genesisCrystals > 0) {
                    PacketDistributor.sendToServer(new GachaTopUpPayload(1, 0));
                } else {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                }
                return true;
            }

            if (mouseX >= b2X && mouseX <= b2X + btnConvW && mouseY >= convY && mouseY <= convY + convH) {
                if (stellarJade > 0) {
                    PacketDistributor.sendToServer(new GachaTopUpPayload(2, 0));
                } else {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                }
                return true;
            }

            return true;
        }

        if (showShopModal) {
            int mW = 500;
            int mH = 265;
            int mX = getVirtW() / 2 - mW / 2;
            int mY = getVirtH() / 2 - mH / 2;

            int closeX = mX + mW - 22;
            int closeY = mY + 10;
            boolean clickedClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
            boolean clickedOutside = mouseX < mX || mouseX > mX + mW || mouseY < mY || mouseY > mY + mH;

            if (clickedClose || clickedOutside) {
                showShopModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                return true;
            }

            int colW = 230;
            int startY = mY + 36;

            int[] leftOfferIds = {0, 1, 6, 7};
            int[] leftCosts = {75, 75, 15, 10};
            for (int i = 0; i < 4; i++) {
                int y = startY + 14 + i * 48;
                int bX = mX + 14 + colW - 68;
                int bY = y + 10;
                if (mouseX >= bX && mouseX <= bX + 60 && mouseY >= bY && mouseY <= bY + 22) {
                    if (stardust >= leftCosts[i]) {
                        PacketDistributor.sendToServer(new PaimonBargainBuyPayload(leftOfferIds[i], 1));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
            }

            int rightColX = mX + 14 + colW + 12;
            int[] rightOfferIds = {2, 3, 4, 5};
            int[] rightCosts = {5, 5, 34, 34};
            for (int i = 0; i < 4; i++) {
                int y = startY + 14 + i * 48;
                int bX = rightColX + colW - 68;
                int bY = y + 10;
                if (mouseX >= bX && mouseX <= bX + 60 && mouseY >= bY && mouseY <= bY + 22) {
                    if (starglitter >= rightCosts[i]) {
                        PacketDistributor.sendToServer(new PaimonBargainBuyPayload(rightOfferIds[i], 1));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
            }

            return true;
        }

        if (showPathModal) {
            int mW = 460;
            int mH = 260;
            int mX = getVirtW() / 2 - mW / 2;
            int mY = getVirtH() / 2 - mH / 2;

            int closeX = mX + mW - 22;
            int closeY = mY + 10;
            boolean clickedClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
            boolean clickedOutside = mouseX < mX || mouseX > mX + mW || mouseY < mY || mouseY > mY + mH;

            if (clickedClose || clickedOutside) {
                showPathModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                return true;
            }

            int cardW = 205;
            int cardH = 135;
            int cardY = mY + 28 + 44;

            int btn1X = mX + 18 + (cardW - 120) / 2;
            int btn1Y = cardY + cardH - 26;
            if (mouseX >= btn1X && mouseX <= btn1X + 120 && mouseY >= btn1Y && mouseY <= btn1Y + 20) {
                int newTarget = (weaponBannerTarget == 1) ? 0 : 1;
                this.weaponBannerTarget = newTarget;
                this.weaponBannerFatePoints = 0;
                PacketDistributor.sendToServer(new SetGachaPathPayload(0, newTarget));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                return true;
            }

            int btn2X = mX + mW - 18 - cardW + (cardW - 120) / 2;
            int btn2Y = cardY + cardH - 26;
            if (mouseX >= btn2X && mouseX <= btn2X + 120 && mouseY >= btn2Y && mouseY <= btn2Y + 20) {
                int newTarget = (weaponBannerTarget == 2) ? 0 : 2;
                this.weaponBannerTarget = newTarget;
                this.weaponBannerFatePoints = 0;
                PacketDistributor.sendToServer(new SetGachaPathPayload(0, newTarget));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                return true;
            }

            return true;
        }

        if (screenState == STATE_METEOR) {
            int skipW = 75;
            int skipH = 22;
            int skipX = getVirtW() - skipW - 14;
            int skipY = 14;
            boolean clickedSkip = mouseX >= skipX && mouseX <= skipX + skipW && mouseY >= skipY && mouseY <= skipY + skipH;

            stopMeteorSound();
            stopCurrentVoiceSound();
            if (clickedSkip) {
                this.screenState = STATE_SUMMARY;
                playResultListSound();
            } else {
                this.screenState = STATE_REVEAL_SINGLE;
                this.currentRevealIndex = 0;
                this.revealStartTime = System.currentTimeMillis();
                if (!resultItems.isEmpty()) {
                    playRevealSound(resultItems.get(0), (0 < itemRarities.size()) ? itemRarities.get(0) : getItemStarRarity(resultItems.get(0)));
                }
            }
            return true;
        }

        if (screenState == STATE_REVEAL_SINGLE) {
            int skipW = 75;
            int skipH = 22;
            int skipX = getVirtW() - skipW - 14;
            int skipY = 14;
            boolean clickedSkip = mouseX >= skipX && mouseX <= skipX + skipW && mouseY >= skipY && mouseY <= skipY + skipH;

            stopCurrentVoiceSound();
            if (clickedSkip) {
                this.screenState = STATE_SUMMARY;
                playResultListSound();
                return true;
            }

            currentRevealIndex++;
            this.revealStartTime = System.currentTimeMillis();
            if (currentRevealIndex < resultItems.size()) {
                playRevealSound(resultItems.get(currentRevealIndex), (currentRevealIndex < itemRarities.size()) ? itemRarities.get(currentRevealIndex) : getItemStarRarity(resultItems.get(currentRevealIndex)));
            } else {
                this.screenState = STATE_SUMMARY;
                playResultListSound();
            }
            return true;
        }

        if (screenState == STATE_SUMMARY) {
            if (activeBanner == BANNER_UMA) {
                int virtW = getVirtW();
                int virtH = getVirtH();
                int total = resultItems.size();
                int btnY = getUmaResultBtnY(virtH, total);
                int backX = getUmaResultBackX(virtW);
                int againX = getUmaResultAgainX(virtW);

                if (isUmaResultBackHover(mouseX, mouseY, backX, btnY, UMA_RESULT_BACK_W, UMA_RESULT_BTN_H)) {
                    stopCurrentVoiceSound();
                    this.screenState = STATE_BANNER;
                    startAmbientSound();
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }

                if (isUmaResultAgainHover(mouseX, mouseY, againX, btnY, UMA_RESULT_AGAIN_W, UMA_RESULT_BTN_H)) {
                    stopCurrentVoiceSound();
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new GachaWishPayload(activeBanner, lastPullCount));
                    return true;
                }
                return true;
            }

            int btnW = 125;
            int btnH = 26;
            int btnY = getVirtH() - 42;
            int totalW = btnW * 3 + 16;
            int btnBackX = getVirtW() / 2 - totalW / 2;
            int btnSubspaceX = btnBackX + btnW + 8;
            int btnAgainX = btnSubspaceX + btnW + 8;

            if (mouseX >= btnBackX && mouseX <= btnBackX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                stopCurrentVoiceSound();
                this.screenState = STATE_BANNER;
                startAmbientSound();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (mouseX >= btnSubspaceX && mouseX <= btnSubspaceX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                stopCurrentVoiceSound();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.onClose();
                PacketDistributor.sendToServer(new OpenSubspaceC2SPayload());
                return true;
            }

            if (mouseX >= btnAgainX && mouseX <= btnAgainX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                stopCurrentVoiceSound();
                PacketDistributor.sendToServer(new GachaWishPayload(activeBanner, lastPullCount));
                return true;
            }
            return true;
        }

        if (mouseX >= 10 && mouseX <= 64 && mouseY >= 6 && mouseY <= 23) {
            onClose();
            return true;
        }

        if (isHistBtnHover(mouseX, mouseY)) {
            showHistoryModal = true;
            historyPage = 0;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            return true;
        }

        if (isShopBtnHover(mouseX, mouseY)) {
            showShopModal = true;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            return true;
        }

        if (activeBanner == 0 && isPathBtnHover(mouseX, mouseY)) {
            showPathModal = true;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            return true;
        }

        if (isSubspaceBtnHover(mouseX, mouseY)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            this.onClose();
            PacketDistributor.sendToServer(new OpenSubspaceC2SPayload());
            return true;
        }

        if (isPromotionalCharacterBanner()) {
            int cardW = getCardW();
            int stageW = Math.min(160, (int) (cardW * 0.35F));
            int stageX = getCardX() + cardW - stageW - 14;
            int leftX = getCardX() + 16;
            int leftW = stageX - leftX - 10;
            int toggleBtnW = Math.min(130, (leftW - 10) / 2);
            int toggleBtnH = 16;
            int toggleY = getCardY() + 66;
            int t1X = leftX;
            int t2X = leftX + toggleBtnW + 8;

            if (mouseX >= t1X && mouseX <= t1X + toggleBtnW && mouseY >= toggleY && mouseY <= toggleY + toggleBtnH) {
                if (activeBanner != 1) {
                    activeBanner = 1;
                    stopCurrentVoiceSound();
                    PromotionalBanner.BannerPhase phase1 = getPromoPhase(0);
                    if (phase1 != null && phase1.featuredCharacter() != null && phase1.featuredCharacter().getSummonSound() != null) {
                        playVoiceSound(phase1.featuredCharacter().getSummonSound(), 1.25F, 1.0F);
                    } else {
                        playVoiceSound(ModSounds.COMPANION_HU_TAO_SUMMON.get(), 1.25F, 1.0F);
                    }
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                return true;
            }
            if (mouseX >= t2X && mouseX <= t2X + toggleBtnW && mouseY >= toggleY && mouseY <= toggleY + toggleBtnH) {
                if (activeBanner != BANNER_PROMO_2) {
                    activeBanner = BANNER_PROMO_2;
                    stopCurrentVoiceSound();
                    PromotionalBanner.BannerPhase phase2 = getPromoPhase(1);
                    if (phase2 != null && phase2.featuredCharacter() != null && phase2.featuredCharacter().getSummonSound() != null) {
                        playVoiceSound(phase2.featuredCharacter().getSummonSound(), 1.25F, 1.0F);
                    } else {
                        playVoiceSound(ModSounds.COMPANION_FURINA_SUMMON.get(), 1.25F, 1.0F);
                    }
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                return true;
            }
        }

        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 9 : 8;
        int subTabH = 17;
        int subModeStartX = 10 + 54 + 4;
        int subModeY = 6;
        int rightReserved = (activeBanner == BANNER_UMA) ? 140 : ((activeBanner == BANNER_STELLAR_WARP) ? 145 : 185);
        int availW = getVirtW() - subModeStartX - rightReserved - 8;
        int subTabW = Math.max(42, Math.min(64, availW / numSubTabs));

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            if (mouseX >= sx && mouseX <= sx + subTabW && mouseY >= subModeY && mouseY <= subModeY + subTabH) {
                if ((hasNovice && i == 8) || (!hasNovice && i == 7)) {
                    showTopUpModal = true;
                    showNewsModal = false;
                    topUpTab = (activeBanner == BANNER_UMA) ? 2 : ((activeBanner == BANNER_STELLAR_WARP) ? 1 : 0);
                    showShopModal = false;
                    showHistoryModal = false;
                    showPathModal = false;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                    return true;
                }
                if (i == 6) {
                    showNewsModal = true;
                    showTopUpModal = false;
                    showShopModal = false;
                    showHistoryModal = false;
                    showPathModal = false;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                    return true;
                }

                showTopUpModal = false;
                showNewsModal = false;
                if (i == 0) {
                    if (activeBanner != 1) {
                        activeBanner = 1;
                        stopCurrentVoiceSound();
                        PromotionalBanner.BannerPhase phase1 = getPromoPhase(0);
                        if (phase1 != null && phase1.featuredCharacter() != null && phase1.featuredCharacter().getSummonSound() != null) {
                            playVoiceSound(phase1.featuredCharacter().getSummonSound(), 1.25F, 1.0F);
                        } else {
                            playVoiceSound(ModSounds.COMPANION_HU_TAO_SUMMON.get(), 1.25F, 1.0F);
                        }
                    }
                    return true;
                }
                if (i == 1) {
                    if (activeBanner != BANNER_PROMO_2) {
                        activeBanner = BANNER_PROMO_2;
                        stopCurrentVoiceSound();
                        PromotionalBanner.BannerPhase phase2 = getPromoPhase(1);
                        if (phase2 != null && phase2.featuredCharacter() != null && phase2.featuredCharacter().getSummonSound() != null) {
                            playVoiceSound(phase2.featuredCharacter().getSummonSound(), 1.25F, 1.0F);
                        } else {
                            playVoiceSound(ModSounds.COMPANION_FURINA_SUMMON.get(), 1.25F, 1.0F);
                        }
                    }
                    return true;
                }
                int targetBanner = switch (i) {
                    case 2 -> 0;
                    case 3 -> BANNER_STELLAR_WARP;
                    case 4 -> 2;
                    case 5 -> BANNER_UMA;
                    case 7 -> hasNovice ? 3 : 1;
                    default -> 1;
                };
                if (activeBanner != targetBanner) {
                    activeBanner = targetBanner;
                    stopCurrentVoiceSound();
                    if (targetBanner == 0) {
                        playVoiceSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.2F, 1.1F);
                    } else if (targetBanner == BANNER_STELLAR_WARP) {
                        playVoiceSound(ModSounds.HERTA_KURURIN.get(), 1.2F, 1.0F);
                    } else if (targetBanner == 2) {
                        playVoiceSound(ModSounds.MUSIC_DISC_DAY_OF_HOPE.get(), 1.0F, 1.0F);
                    } else if (targetBanner == 3) {
                        playVoiceSound(ModSounds.COMPANION_NOELLE_SUMMON.get(), 1.2F, 1.0F);
                    } else if (targetBanner == BANNER_UMA) {
                        playVoiceSound(SoundEvents.HORSE_GALLOP, 1.2F, 1.2F);
                    } else {
                        playFranchiseSwitchSound(activeFranchise);
                    }
                }
                return true;
            }
        }

        if (activeBanner == BANNER_UMA) {
            int pillCaratW = 90;
            int pillCaratX = getVirtW() - pillCaratW - 10;
            int pillY = 6;
            int pillH = 17;
            if (mouseX >= pillCaratX && mouseX <= pillCaratX + pillCaratW && mouseY >= pillY && mouseY <= pillY + pillH) {
                showTopUpModal = true;
                topUpTab = 2;
                showShopModal = false;
                showHistoryModal = false;
                showPathModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                return true;
            }
        }

        if (activeBanner == BANNER_STELLAR_WARP) {
            String sJade = String.format(java.util.Locale.US, "%,d", stellarJade);
            int pillJadeW = 18 + this.font.width(sJade) + 18;
            int pillJadeX = getVirtW() - pillJadeW - 10;
            int pillY = 6;
            int pillH = 17;
            if (mouseX >= pillJadeX && mouseX <= pillJadeX + pillJadeW && mouseY >= pillY && mouseY <= pillY + pillH) {
                showTopUpModal = true;
                topUpTab = 1;
                showShopModal = false;
                showHistoryModal = false;
                showPathModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                return true;
            }
        }

        long dayTime = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getDayTime() : 0L;
        if (activeBanner == 3) {
            if (isWish10BtnHover(mouseX, mouseY)) {
                if (VersionBannerManager.isUnderMaintenance(dayTime)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(Component.translatable("awakening_system.gacha.maintenance_msg", VersionBannerManager.getMaintenanceRemainingFormatted(dayTime)), false);
                    }
                    return true;
                }
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                PacketDistributor.sendToServer(new GachaWishPayload(3, 10));
                return true;
            }
        } else {
            if (isWish1BtnHover(mouseX, mouseY)) {
                if (VersionBannerManager.isUnderMaintenance(dayTime)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(Component.translatable("awakening_system.gacha.maintenance_msg", VersionBannerManager.getMaintenanceRemainingFormatted(dayTime)), false);
                    }
                    return true;
                }
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                PacketDistributor.sendToServer(new GachaWishPayload(activeBanner, 1));
                return true;
            }

            if (isWish10BtnHover(mouseX, mouseY)) {
                if (VersionBannerManager.isUnderMaintenance(dayTime)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(Component.translatable("awakening_system.gacha.maintenance_msg", VersionBannerManager.getMaintenanceRemainingFormatted(dayTime)), false);
                    }
                    return true;
                }
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                PacketDistributor.sendToServer(new GachaWishPayload(activeBanner, 10));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (showHistoryModal && !wishHistory.isEmpty()) {
            int totalPages = Math.max(1, (int) Math.ceil((double) wishHistory.size() / 7.0));
            if (verticalAmount > 0 && historyPage > 0) {
                historyPage--;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else if (verticalAmount < 0 && historyPage < totalPages - 1) {
                historyPage++;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showHistoryModal || showShopModal || showPathModal || showTopUpModal || showNewsModal) {
            if (keyCode == 256) {
                showHistoryModal = false;
                showShopModal = false;
                showTopUpModal = false;
                showPathModal = false;
                showNewsModal = false;
                return true;
            }
        }

        if (keyCode == 32 || keyCode == 257) {
            stopCurrentVoiceSound();
            if (screenState == STATE_REVEAL_SINGLE) {
                currentRevealIndex++;
                this.revealStartTime = System.currentTimeMillis();
                if (currentRevealIndex < resultItems.size()) {
                    playRevealSound(resultItems.get(currentRevealIndex), (currentRevealIndex < itemRarities.size()) ? itemRarities.get(currentRevealIndex) : getItemStarRarity(resultItems.get(currentRevealIndex)));
                } else {
                    this.screenState = STATE_SUMMARY;
                    playResultListSound();
                }
                return true;
            } else if (screenState == STATE_METEOR) {
                stopMeteorSound();
                this.screenState = STATE_SUMMARY;
                playResultListSound();
                return true;
            } else if (screenState == STATE_SUMMARY) {
                this.screenState = STATE_BANNER;
                startAmbientSound();
                return true;
            }
        }

        if (keyCode == 256) {
            stopCurrentVoiceSound();
            if (screenState == STATE_METEOR) {
                stopMeteorSound();
                this.screenState = STATE_SUMMARY;
                playResultListSound();
                return true;
            } else if (screenState == STATE_REVEAL_SINGLE) {
                this.screenState = STATE_SUMMARY;
                playResultListSound();
                return true;
            } else if (screenState == STATE_SUMMARY) {
                this.screenState = STATE_BANNER;
                startAmbientSound();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private int getItemStarRarity(ItemStack stack) {
        if (stack.isEmpty()) return 3;
        if (stack.getItem() instanceof CompanionContractItem) {
            return CompanionContractItem.getProfile(stack).stars();
        }
        if (stack.getItem() instanceof HsrLightConeItem lci) {
            return lci.getStars();
        }
        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.GenshinWeaponItem gwi) {
            return gwi.getWeaponType().stars;
        }
        if (stack.is(SystemItems.STAFF_OF_HOMA.get())
                || stack.is(SystemItems.LIGHT_CONE_MILKY_WAY.get())
                || stack.is(SystemItems.COMMAND_SPELL.get())
                || stack.is(SystemItems.ASRAHAN_HOLY_SWORD.get())
                || stack.is(SystemItems.DAWNS_BALLAD.get())
                || stack.is(SystemItems.BLACK_DRAGON_SCYTHE.get())
                || stack.is(SystemItems.TRANSCENDENT_THORN.get())
                || stack.is(SystemItems.HEAVEN_SHAKING_SWORD.get())
                || stack.is(SystemItems.UNBREAKABLE_FAITH.get())
                || stack.getItem() instanceof com.soybe.awakeningsystem.item.TeyvatVisionItem) {
            return 5;
        }
        if (stack.is(SystemItems.PLUM_BLOSSOM_SWORD.get())
                || stack.is(SystemItems.SHADOW_DAGGERS.get())
                || stack.is(SystemItems.RISING_DRAGON_PILL.get())
                || stack.is(SystemItems.MARROW_CLEANSING_PILL.get())) {
            return 4;
        }
        return 3;
    }

    private void playMeteorSound(int rarity) {
        stopMeteorSound();
        net.minecraft.sounds.SoundEvent sound = (rarity == 5) ? ModSounds.WISH_METEOR_5STAR.get()
                : (rarity == 4 ? ModSounds.WISH_METEOR_4STAR.get() : ModSounds.WISH_METEOR_3STAR.get());
        currentMeteorSoundInstance = new SimpleSoundInstance(
                sound.getLocation(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F,
                RandomSource.create(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
        );
        Minecraft.getInstance().getSoundManager().play(currentMeteorSoundInstance);
    }

    private void stopMeteorSound() {
        if (currentMeteorSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(currentMeteorSoundInstance);
            currentMeteorSoundInstance = null;
        }
    }

    private void playRevealSound(ItemStack item, int stars) {
        stopCurrentVoiceSound();
        if (item != null && CompanionContractItem.isUmaContract(item)) {
            AwakeningSystemClient.playUiSound(SoundEvents.HORSE_GALLOP, 1.2F, 1.2F);
            AwakeningSystemClient.playUiSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.3F);
        } else if (item != null && item.getItem() instanceof CompanionContractItem) {
            CompanionProfile p = CompanionContractItem.getProfile(item);
            if (p != null) {
                playVoiceSound(p.getSummonSound(), 1.25F, 1.0F);
            }
        }
        if (stars == 5) {
            AwakeningSystemClient.playUiSound(ModSounds.WISH_REVEAL_5STAR.get(), 1.4F, 1.0F);
        } else if (stars == 4) {
            AwakeningSystemClient.playUiSound(ModSounds.WISH_REVEAL_4STAR.get(), 1.4F, 1.0F);
        } else {
            AwakeningSystemClient.playUiSound(ModSounds.WISH_REVEAL_3STAR.get(), 1.2F, 1.0F);
        }
    }

    private void playResultListSound() {
        AwakeningSystemClient.playUiSound(ModSounds.WISH_RESULT_LIST.get(), 1.3F, 1.0F);
    }

    private String getWeaponTypeBadge(ItemStack stack, int stars) {
        if (stack.getItem() instanceof CompanionContractItem) {
            CompanionProfile p = CompanionContractItem.getProfile(stack);
            return Component.translatable("gui.awakening_system.gacha.badge.companion", p.stars(), p.getElementComponent().getString().toUpperCase()).getString();
        }
        if (stack.getItem() instanceof HsrLightConeItem lci) {
            return Component.translatable("gui.awakening_system.gacha.badge.light_cone", lci.getStars(), lci.getPathName().toUpperCase()).getString();
        }
        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.GenshinWeaponItem gwi) {
            return switch (gwi.getWeaponType()) {
                case ENGULFING_LIGHTNING, PRIMORDIAL_JADE_WINGED_SPEAR, CRIMSON_MOONS_SEMBLANCE, VORTEX_VANQUISHER, THE_CATCH, BLACK_TASSEL -> Component.translatable("gui.awakening_system.gacha.badge.polearm", stars).getString();
                case WOLFS_GRAVESTONE, REDHORN_STONETHRESHER, VERDICT, TAILLESS -> Component.translatable("gui.awakening_system.gacha.badge.claymore", stars).getString();
                case HERTA_HAMMER -> Component.translatable("gui.awakening_system.gacha.badge.hammer", stars).getString();
                case DOMAIN_OF_SANCTION -> Component.translatable("gui.awakening_system.gacha.badge.katana", stars).getString();
                default -> Component.translatable("gui.awakening_system.gacha.badge.sword", stars).getString();
            };
        }
        if (stack.is(SystemItems.STAFF_OF_HOMA.get())) return Component.translatable("gui.awakening_system.gacha.badge.celestial_spear").getString();
        if (stack.is(SystemItems.LIGHT_CONE_MILKY_WAY.get())) return Component.translatable("gui.awakening_system.gacha.badge.light_cone", 5, "ERUDITION").getString();
        if (stack.is(SystemItems.COMMAND_SPELL.get())) return Component.translatable("gui.awakening_system.gacha.badge.fate_relic").getString();
        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.TeyvatVisionItem) return Component.translatable("gui.awakening_system.gacha.badge.elemental_vision").getString();
        if (stars == 5) return Component.translatable("gui.awakening_system.gacha.badge.supreme_relic").getString();
        if (stars == 4) return Component.translatable("gui.awakening_system.gacha.badge.mystic_item").getString();
        return Component.translatable("gui.awakening_system.gacha.badge.adventurer_resource").getString();
    }

    private String getWeaponLorePreview(ItemStack stack) {
        if (stack.getItem() instanceof CompanionContractItem) {
            CompanionProfile p = CompanionContractItem.getProfile(stack);
            return p.getDisplayNameComponent().getString() + " - " + p.getTitleComponent().getString() + " (" + p.getElementComponent().getString() + ")";
        }
        if (stack.getItem() instanceof HsrLightConeItem lci) {
            return lci.getLore();
        }
        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.GenshinWeaponItem gwi) {
            return gwi.getWeaponType().lore;
        }
        if (stack.is(SystemItems.STAFF_OF_HOMA.get())) return Component.translatable("gui.awakening_system.gacha.lore.staff_of_homa").getString();
        if (stack.is(SystemItems.LIGHT_CONE_MILKY_WAY.get())) return Component.translatable("gui.awakening_system.gacha.lore.milky_way").getString();
        if (stack.is(SystemItems.COMMAND_SPELL.get())) return Component.translatable("gui.awakening_system.gacha.lore.command_spell").getString();
        return Component.translatable("gui.awakening_system.gacha.lore.default_item").getString();
    }

    private void renderFittedText(GuiGraphics graphics, String text, int x, int y, int maxW, int color, boolean shadow) {
        int textW = this.font.width(text);
        if (textW > maxW && maxW > 0) {
            float scale = (float) maxW / (float) textW;
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(this.font, text, 0, 0, color, shadow);
            graphics.pose().popPose();
        } else {
            graphics.drawString(this.font, text, x, y, color, shadow);
        }
    }

    private void renderCenteredFittedText(GuiGraphics graphics, String text, int centerX, int y, int maxW, int color) {
        int textW = this.font.width(text);
        if (textW > maxW && maxW > 0) {
            float scale = (float) maxW / (float) textW;
            graphics.pose().pushPose();
            graphics.pose().translate(centerX, y, 0);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawCenteredString(this.font, text, 0, 0, color);
            graphics.pose().popPose();
        } else {
            graphics.drawCenteredString(this.font, text, centerX, y, color);
        }
    }

    private void renderCompanionFace(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
        graphics.blit(texture, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.disableBlend();
    }

    private void renderUmaBust(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int torsoW = size;
        int torsoH = (int) (size * 0.65F);
        int torsoX = x;
        int torsoY = y + (int) (size * 0.70F);
        graphics.blit(texture, torsoX, torsoY, torsoW, torsoH, 20.0F, 20.0F, 8, 6, 64, 64);

        graphics.blit(texture, torsoX, torsoY, torsoW, torsoH, 20.0F, 36.0F, 8, 6, 64, 64);

        int shoulderW = Math.max(2, (int) (size * 0.30F));
        graphics.blit(texture, torsoX - shoulderW + 1, torsoY, shoulderW, torsoH, 44.0F, 20.0F, 4, 6, 64, 64);
        graphics.blit(texture, torsoX - shoulderW + 1, torsoY, shoulderW, torsoH, 44.0F, 36.0F, 4, 6, 64, 64);
        graphics.blit(texture, torsoX + torsoW - 1, torsoY, shoulderW, torsoH, 36.0F, 52.0F, 4, 6, 64, 64);
        graphics.blit(texture, torsoX + torsoW - 1, torsoY, shoulderW, torsoH, 52.0F, 52.0F, 4, 6, 64, 64);

        graphics.blit(texture, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);

        graphics.blit(texture, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);

        RenderSystem.disableBlend();
    }

    private int getElementBgColor(String element) {
        if (element == null) return 0xFF546E7A;
        return switch (element.toLowerCase()) {
            case "pyro", "fuego" -> 0xFFD32F2F;
            case "hydro", "agua" -> 0xFF1976D2;
            case "anemo", "viento" -> 0xFF26A69A;
            case "electro", "rayo" -> 0xFF7B1FA2;
            case "dendro", "planta" -> 0xFF388E3C;
            case "cryo", "hielo" -> 0xFF00ACC1;
            case "geo", "tierra" -> 0xFFFFA000;
            case "sombra" -> 0xFF303F9F;
            case "infinito" -> 0xFF0097A7;
            case "luz" -> 0xFFFFD54F;
            default -> 0xFF546E7A;
        };
    }
}
