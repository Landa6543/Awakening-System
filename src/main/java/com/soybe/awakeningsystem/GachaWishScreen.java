package com.soybe.awakeningsystem;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import com.soybe.awakeningsystem.entity.CompanionProfile;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.item.CompanionContractItem;
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
    // Estados de la pantalla
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
    private int weaponBannerTarget = 0; // 0 = sin fijar, 1 = arma 1, 2 = arma 2
    private int weaponBannerFatePoints = 0; // 0 o 1
    private int characterBannerTarget = 0; // 0 = primario, 1 = secundario
    private List<String> wishHistory = new ArrayList<>();

    // 0 = Armas Divinas (5★), 1 = Genshin, 2 = Permanente Estándar, 3 = Principiantes, 4 = Honkai/ZZZ, 5 = Solo Leveling, 6 = JJK, 7 = Fate, 8 = Demon Slayer, 9 = Wuthering Waves
    private int activeBanner = 1;
    private PromotionalBanner.Franchise activeFranchise = PromotionalBanner.Franchise.GENSHIN;

    public boolean isPromotionalCharacterBanner() {
        return activeBanner == 1 || (activeBanner >= 4 && activeBanner <= 9);
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

    // Modales
    private boolean showHistoryModal = false;
    private int historyPage = 0;
    private boolean showShopModal = false;
    private boolean showPathModal = false;
    private int firstTopUpMask = 0;

    // Instancia de sonido ambiental, meteorito y voz de personaje
    private SoundInstance ambientSoundInstance = null;
    private SoundInstance currentMeteorSoundInstance = null;
    private SoundInstance currentVoiceSoundInstance = null;

    // Vista previa dinámica para los banners
    private final List<ItemStack> weaponBannerPreviews = new ArrayList<>();
    private final List<ItemStack> shonenBannerPreviews = new ArrayList<>();
    private final List<ItemStack> standardBannerPreviews = new ArrayList<>();
    private final List<ItemStack> noviceBannerPreviews = new ArrayList<>();
    // Cache de entidades 3D ficticias para renderizado de personajes
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

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, int coins, double usdBalance, int genesisCrystals, int stellarJade, int welkinDays, int totalWishes, List<String> wishHistory) {
        super(Component.literal("Sistema de Deseos (Gacha Teyvat & Shonen)"));
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

        // Si el cliente tiene datos del jugador, sincronizar balances reales de inmediato
        if (Minecraft.getInstance().player != null) {
            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (d != null) {
                if (this.coins == 0 && d.coins() > 0) this.coins = d.coins();
                if (this.usdBalance == 0.0 && d.usdBalance() > 0.0) this.usdBalance = d.usdBalance();
                if (this.genesisCrystals == 0 && d.genesisCrystals() > 0) this.genesisCrystals = d.genesisCrystals();
                if (this.stellarJade == 0 && d.stellarJade() > 0) this.stellarJade = d.stellarJade();
                if (this.welkinDays == 0 && d.welkinDays() > 0) this.welkinDays = d.welkinDays();
                if (this.mora == 0 && d.mora() > 0) this.mora = d.mora();
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
        // Armas para la vista previa del Banner 0
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

        // Personajes & Multiverso para Banners Promocionales
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

        // Permanente para Banner 2
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
            this.activeBanner = getBannerForFranchise(this.activeFranchise); // Si se agotó el banner de novatos, volver al banner de personajes activo
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
        // Vacío intencionalmente para evitar shaders de blur
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

        // Fondo cósmico sagrado de Genshin Impact (Nebulosas, constelaciones, pilares y destellos)
        renderCosmicSky(graphics, mX, mY);

        switch (screenState) {
            case STATE_METEOR -> renderMeteorAnimation(graphics, mX, mY);
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
                } else {
                    renderBannerSelection(graphics, mX, mY);
                }
            }
        }

        graphics.pose().popPose();
    }

    // ==========================================
    // FONDO CÓSMICO SAGRADO (OPTIMIZADO A 100+ FPS)
    // ==========================================
    private void renderCosmicSky(GuiGraphics graphics, int mouseX, int mouseY) {
        long time = System.currentTimeMillis();
        int w = getVirtW();
        int h = getVirtH();

        // 1. Fondo cielo nocturno crepuscular profundo (1 solo pase)
        int topBg = highestWonRarity == 5 && screenState == STATE_METEOR ? 0xFF1C1303 : 0xFF080B1C;
        int botBg = highestWonRarity == 5 && screenState == STATE_METEOR ? 0xFF352006 : 0xFF161432;
        graphics.fillGradient(0, 0, w, h, topBg, botBg);

        // 2. Gradiente inferior de bruma celestial sutil (1 solo pase)
        graphics.fillGradient(0, h - 90, w, h, 0x00000000, 0x2E100A24);

        // 3. Estrellas de Teyvat (1 solo quad ligero por estrella)
        for (int i = 0; i < 32; i++) {
            int sx = (i * 73 + 17) % Math.max(1, w);
            int sy = (i * 43 + 29) % Math.max(1, h);
            float twinkle = (float) Math.sin((time + i * 211) / 350.0);
            int alpha = twinkle > 0 ? (int) (70 + twinkle * 160) : 40;
            int sz = (i % 6 == 0) ? 2 : 1;
            graphics.fill(sx, sy, sx + sz, sy + sz, (alpha << 24) | 0x00FFFDD0);
        }

        // 4. Columnas arquitectónicas sutiles en los bordes
        if (w > 400) {
            int colW = 16;
            graphics.fill(0, 0, colW, h, 0x44080C1A);
            graphics.fill(colW - 1, 0, colW, h, 0x2264B5F6);

            graphics.fill(w - colW, 0, w, h, 0x44080C1A);
            graphics.fill(w - colW, 0, w - colW + 1, h, 0x2264B5F6);
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
        int w = (activeBanner == 3) ? 145 : 135;
        int x = getCardX() + getCardW() - w;
        int y = getBottomY() - 1;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 28;
    }

    private boolean isWish1BtnHover(double mouseX, double mouseY) {
        if (activeBanner == 3) return false;
        int wish10W = 135;
        int w = 120;
        int x = getCardX() + getCardW() - wish10W - 8 - w;
        int y = getBottomY() - 1;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 28;
    }

    private void playFranchiseSwitchSound(PromotionalBanner.Franchise franchise) {
        stopCurrentVoiceSound();
        if (franchise == null) return;
        switch (franchise) {
            case SOLO_LEVELING -> playVoiceSound(ModSounds.COMPANION_SUNG_JINWOO_SUMMON.get(), 1.25F, 1.0F);
            case JUJUTSU_KAISEN -> playVoiceSound(ModSounds.COMPANION_GOJO_SATORU_SUMMON.get(), 1.25F, 1.0F);
            case HONKAI_ZZZ -> playVoiceSound(ModSounds.HERTA_KURURIN.get(), 1.25F, 1.0F);
            case FATE -> playVoiceSound(SoundEvents.BELL_RESONATE, 1.0F, 1.2F);
            case DEMON_SLAYER -> playVoiceSound(SoundEvents.FIRECHARGE_USE, 1.1F, 1.0F);
            case WUTHERING_WAVES -> playVoiceSound(SoundEvents.PLAYER_LEVELUP, 1.1F, 1.3F);
            default -> playVoiceSound(ModSounds.COMPANION_RAIDEN_SHOGUN_SUMMON.get(), 1.25F, 1.0F);
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

        renderCompanionFace(graphics, profile.getTextureLocation(), x + 3, y + (h - 18) / 2, 18);

        int textX = x + 24;
        graphics.drawString(this.font, profile.displayName(), textX, y + 2, hover ? 0xFFFFFF : 0xFFE082, false);
        graphics.drawString(this.font, "§d★★★★ §7" + profile.element(), textX, y + 12, 0xFFDA70D6, false);
    }

    private void render4StarWeaponCard(GuiGraphics graphics, ItemStack weapon, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hover ? 0xDD1B2945 : 0xAA0D172B;
        int border = hover ? 0xFFFFD700 : 0xFF64B5F6;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);

        graphics.renderItem(weapon, x + 2, y + (h - 16) / 2);

        int textX = x + 22;
        renderFittedText(graphics, weapon.getHoverName().getString(), textX, y + 2, w - 24, hover ? 0xFFFFFF : 0xBBDEFB, false);
        graphics.drawString(this.font, "§b★★★★ Arma", textX, y + 12, 0xFF64B5F6, false);
    }

    // ==========================================
    // FASE 0: SELECCIÓN DE BANNER Y MONEDERO (GENSHIN IMPACT AUTHENTIC)
    // ==========================================
    private void renderBannerSelection(GuiGraphics graphics, int mouseX, int mouseY) {
        long dayTime = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getDayTime() : 0L;
        PromotionalBanner.BannerPhase promoPhase = PromotionalBanner.getActivePhase(dayTime);
        activeFranchise = promoPhase.franchise();
        String promoTimeLeft = PromotionalBanner.getFormattedTimeRemaining(dayTime);
        long currentMCDay = PromotionalBanner.getCurrentDay(dayTime);
        int currentPhaseNum = PromotionalBanner.getPhaseNumber(dayTime);
        int totalPhases = PromotionalBanner.getTotalPhases();

        // 1. Botón Salir [ESC]
        int exitX = 10;
        int exitY = 6;
        int exitW = 54;
        int exitH = 17;
        boolean hoverExit = mouseX >= exitX && mouseX <= exitX + exitW && mouseY >= exitY && mouseY <= exitY + exitH;
        graphics.fill(exitX, exitY, exitX + exitW, exitY + exitH, hoverExit ? 0xCC334155 : 0x881E293B);
        graphics.renderOutline(exitX, exitY, exitW, exitH, hoverExit ? 0xFFFFFFFF : 0xFF475569);
        graphics.drawCenteredString(this.font, "§7[ESC] §fSalir", exitX + exitW / 2, exitY + 5, 0xFFFFFF);

        // 2. Sub-modos en Fila 1: [Promocional] [Armas 5★] [Permanente] [Principiantes] [Tienda USD]
        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 5 : 4;
        int subTabW = hasNovice ? 80 : 96;
        int subTabH = 17;
        int subModeStartX = exitX + exitW + 6;

        String[] subTitles = hasNovice
                ? new String[]{"✦ PROMOCIONAL", "✦ ARMAS 5★", "✦ PERMANENTE", "✦ PRINCIPIANTES", "✦ RECARGAS USD"}
                : new String[]{"✦ PROMOCIONAL", "✦ ARMAS 5★", "✦ PERMANENTE", "✦ RECARGAS USD"};

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            boolean isSelected;
            if (i == 0) isSelected = isPromotionalCharacterBanner() && !showTopUpModal;
            else if (i == 1) isSelected = (activeBanner == 0) && !showTopUpModal;
            else if (i == 2) isSelected = (activeBanner == 2) && !showTopUpModal;
            else if (hasNovice && i == 3) isSelected = (activeBanner == 3) && !showTopUpModal;
            else isSelected = showTopUpModal;

            boolean isHover = mouseX >= sx && mouseX <= sx + subTabW && mouseY >= exitY && mouseY <= exitY + subTabH;

            if (isSelected) {
                graphics.fillGradient(sx, exitY, sx + subTabW, exitY + subTabH, 0xFF3D2706, 0xFF1F1403);
                graphics.renderOutline(sx, exitY, subTabW, subTabH, 0xFFFFD700);
                graphics.fill(sx + 6, exitY + subTabH - 1, sx + subTabW - 6, exitY + subTabH + 1, 0xFFFFD700);
                renderCenteredFittedText(graphics, subTitles[i], sx + subTabW / 2, exitY + 4, subTabW - 6, 0xFFFFD700);
            } else {
                graphics.fill(sx, exitY, sx + subTabW, exitY + subTabH, isHover ? 0xCC1A233A : 0x880C1220);
                graphics.renderOutline(sx, exitY, subTabW, subTabH, isHover ? 0xFF94A3B8 : 0x44475569);
                renderCenteredFittedText(graphics, subTitles[i], sx + subTabW / 2, exitY + 5, subTabW - 6, isHover ? 0xEEEEEE : 0x94A3B8);
            }
        }

        // 3. Monedero Superior Derecho (Fila 1)
        int pillH = 17;
        int pillY = 6;
        boolean usesIntertwined = (activeBanner == 0 || isPromotionalCharacterBanner());

        // Cápsula 1: Destinos
        int pill1W = 58;
        int pill1X = getVirtW() - pill1W - 10;
        graphics.fill(pill1X, pillY, pill1X + pill1W, pillY + pillH, 0xDD0B0F1A);
        graphics.renderOutline(pill1X, pillY, pill1W, pillH, 0xFF334155);
        ItemStack fateStack = usesIntertwined
                ? (SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : ItemStack.EMPTY)
                : (SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : ItemStack.EMPTY);
        int fateCount = usesIntertwined ? intertwinedFates : acquaintFates;
        int fateColor = usesIntertwined ? 0xFFFFD700 : 0xFF80D8FF;
        if (!fateStack.isEmpty()) {
            graphics.renderItem(fateStack, pill1X + 1, pillY + 1);
        }
        graphics.drawString(this.font, "" + fateCount, pill1X + 19, pillY + 4, fateColor, false);

        // Cápsula 2: Protogemas
        int pill2W = 72;
        int pill2X = pill1X - pill2W - 5;
        graphics.fill(pill2X, pillY, pill2X + pill2W, pillY + pillH, 0xDD0B0F1A);
        graphics.renderOutline(pill2X, pillY, pill2W, pillH, 0xFF334155);
        ItemStack primoStack = SystemItems.PRIMOGEM.isBound() ? new ItemStack(SystemItems.PRIMOGEM.get()) : ItemStack.EMPTY;
        if (!primoStack.isEmpty()) {
            graphics.renderItem(primoStack, pill2X + 1, pillY + 1);
        }
        graphics.drawString(this.font, "" + primogems, pill2X + 19, pillY + 4, 0xFFFFE082, false);

        int totalSubModeW = subModeStartX + numSubTabs * (subTabW + 4);

        // Cápsula 3: Polvo y Brillo Estelar
        ItemStack stardustStack = SystemItems.MASTERLESS_STARDUST.isBound() ? new ItemStack(SystemItems.MASTERLESS_STARDUST.get()) : ItemStack.EMPTY;
        ItemStack starglitterStack = SystemItems.MASTERLESS_STARGLITTER.isBound() ? new ItemStack(SystemItems.MASTERLESS_STARGLITTER.get()) : ItemStack.EMPTY;
        String sDust = String.valueOf(stardust);
        String sGlitter = String.valueOf(starglitter);
        int wDust = this.font.width(sDust);
        int wGlitter = this.font.width(sGlitter);
        int pill3W = 18 + wDust + 4 + 16 + wGlitter + 4;
        int pill3X = pill2X - pill3W - 5;
        int glitterX = pill3X + 18 + wDust + 4;

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

        // Cápsula 4: Mora
        int pill4W = 84;
        int pill4X = pill3X - pill4W - 5;
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

        // 4. Cartel Principal del Banner Activo
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
            bannerTag = "✦ ENCARNACIÓN DIVINA: ARMAS 5★ ✦";
            timerTag = "⏳ " + promoTimeLeft;
            mainTitle = "ENCARNACIÓN DIVINA";
            subtitle = "§eProbabilidad aumentada del 75% para las armas insignias del Multiverso";
            titleColor = 0xFFFFD700;
        } else if (isPromotionalCharacterBanner()) {
            cardBorder = promoPhase.themeColor();
            cardBgTop = 0xFF190B26;
            cardBgBot = 0xFF28103D;
            pedestalGlow = promoPhase.themeGlowColor();
            bannerTag = "✦ SAGA: " + activeFranchise.displayName().toUpperCase() + " · FASE " + currentPhaseNum + "/" + totalPhases + " ✦";
            timerTag = "⏳ Rotación en: " + promoTimeLeft;
            mainTitle = promoPhase.featuredCharacter().displayName().toUpperCase();
            subtitle = "§d" + promoPhase.bannerTitle() + " · " + promoPhase.bannerSubtitle();
            titleColor = promoPhase.themeColor();
        } else if (activeBanner == 3) {
            cardBorder = 0xFFFFE082;
            cardBgTop = 0xFF231A0B;
            cardBgBot = 0xFF352610;
            pedestalGlow = 0x66FFE082;
            bannerTag = "✦ DESEO DE PRINCIPIANTES -20% DTO. ✦";
            timerTag = (20 - beginnerPulls) + " / 20 restantes";
            mainTitle = "FLOR DEL CABALLERO";
            subtitle = "§a¡8 Destinos por deseo x10! Noelle garantizada en tu primera tirada";
            titleColor = 0xFFFFE082;
        } else {
            cardBorder = 0xFF64B5F6;
            cardBgTop = 0xFF0B1428;
            cardBgBot = 0xFF142446;
            pedestalGlow = 0x6664B5F6;
            bannerTag = "✦ INVOCACIÓN PERMANENTE DEL VIAJERO ✦";
            timerTag = "⏳ Permanente";
            mainTitle = "PASIÓN POR VIAJAR";
            subtitle = "§bArsenal legendario sin límite de tiempo: Visiones, Reliquias y Monarcas";
            titleColor = 0xFF80D8FF;
        }

        // Fondo y marcos ornamentales
        graphics.fillGradient(cardX, cardY, cardX + cardW, cardY + cardH, cardBgTop, cardBgBot);
        graphics.renderOutline(cardX, cardY, cardW, cardH, cardBorder);
        graphics.renderOutline(cardX + 2, cardY + 2, cardW - 4, cardH - 4, 0x33FFFFFF);

        // Floretes ornamentales en las 4 esquinas
        graphics.fill(cardX, cardY, cardX + 8, cardY + 2, cardBorder);
        graphics.fill(cardX, cardY, cardX + 2, cardY + 8, cardBorder);
        graphics.fill(cardX + cardW - 8, cardY, cardX + cardW, cardY + 2, cardBorder);
        graphics.fill(cardX + cardW - 2, cardY, cardX + cardW, cardY + 8, cardBorder);
        graphics.fill(cardX, cardY + cardH - 2, cardX + 8, cardY + cardH, cardBorder);
        graphics.fill(cardX, cardY + cardH - 8, cardX + 2, cardY + cardH, cardBorder);
        graphics.fill(cardX + cardW - 8, cardY + cardH - 2, cardX + cardW, cardY + cardH, cardBorder);
        graphics.fill(cardX + cardW - 2, cardY + cardH - 8, cardX + cardW, cardY + cardH, cardBorder);

        // Medidas de las dos columnas del Banner
        int leftX = cardX + 16;
        int stageW = Math.min(160, (int) (cardW * 0.35F));
        int stageX = cardX + cardW - stageW - 14;
        int stageY = cardY + 12;
        int stageH = cardH - 34;
        int leftW = stageX - leftX - 10;

        // --- FILA 1: Tag de categoría + Temporizador de Rotación ---
        int tagW = this.font.width(bannerTag) + 14;
        renderBadge(graphics, bannerTag, leftX, cardY + 10, tagW, 14, cardBorder, 0x66000000, cardBorder);

        int timerW = this.font.width(timerTag) + 12;
        int timerX = stageX - timerW - 6;
        renderBadge(graphics, timerTag, timerX, cardY + 10, timerW, 13, 0xFFFFAA, 0x66000000, 0xFFFFD700);

        // --- FILA 2: Título Gigante del Personaje o Evento ---
        graphics.pose().pushPose();
        graphics.pose().translate(leftX, cardY + 26, 0);
        graphics.pose().scale(1.25F, 1.25F, 1.0F);
        graphics.drawString(this.font, mainTitle, 0, 0, titleColor, true);
        graphics.pose().popPose();

        // --- FILA 3: Subtítulo y Cita Lore ---
        renderFittedText(graphics, subtitle, leftX, cardY + 42, leftW, 0xEEEEEE, false);

        // --- FILA 4: Destacados 5★ y Rate-Ups 4★ con Cartas Oficiales ---
        if (isPromotionalCharacterBanner()) {
            // BANNER PROMOCIONAL ROTATIVO (22 DÍAS MC)
            graphics.drawString(this.font, "§6★★★★★ Personaje Promocional: §e" + promoPhase.elementBadge(), leftX, cardY + 54, 0xFFFFD700, false);

            if (promoPhase.secondaryFeaturedCharacter() != null) {
                int halfW = (leftW - 6) / 2;
                renderBadge(graphics, "✦ " + promoPhase.featuredCharacter().displayName() + " 5★", leftX, cardY + 66, halfW, 15, 0xFFFFD700, 0x33B8860B, 0xFFFFD700);
                renderBadge(graphics, "✦ " + promoPhase.secondaryFeaturedCharacter().displayName() + " 5★", leftX + halfW + 6, cardY + 66, halfW, 15, 0xFFFFD700, 0x33B8860B, 0xFFFFD700);
            } else {
                renderBadge(graphics, "✦ " + promoPhase.featuredCharacter().displayName() + " 5★ ¡Probabilidad Aumentada 50%! (Garantizado al perder)", leftX, cardY + 66, leftW, 15, 0xFFFFD700, 0x33B8860B, 0xFFFFD700);
            }

            graphics.drawString(this.font, "§d★★★★ Tasa Aumentada (Probabilidad 50%):", leftX, cardY + 85, 0xFFDA70D6, false);

            int card4Count = promoPhase.rateUp4StarCharacters().size() + promoPhase.rateUp4StarWeapons().size();
            int c4W = Math.max(65, (leftW - (card4Count - 1) * 6) / Math.max(1, card4Count));
            int c4H = 24;

            int cX = leftX;
            for (CompanionProfile p : promoPhase.rateUp4StarCharacters()) {
                render4StarCharacterCard(graphics, p, cX, cardY + 97, c4W, c4H, mouseX, mouseY);
                cX += c4W + 6;
            }
            for (Supplier<ItemStack> wpSup : promoPhase.rateUp4StarWeapons()) {
                ItemStack wp = wpSup.get();
                render4StarWeaponCard(graphics, wp, cX, cardY + 97, c4W, c4H, mouseX, mouseY);
                cX += c4W + 6;
            }

        } else if (activeBanner == 0) {
            // BANNER DE ARMAS CELESTIALES
            ItemStack wp1 = promoPhase.primaryFeaturedWeapon().get();
            ItemStack wp2 = promoPhase.secondaryFeaturedWeapon() != null ? promoPhase.secondaryFeaturedWeapon().get() : ItemStack.EMPTY;

            graphics.drawString(this.font, "§6★★★★★ Armas Insignia Rate-Up 75%:", leftX, cardY + 54, 0xFFFFD700, false);
            int halfW = (leftW - 6) / 2;
            renderBadge(graphics, "✦ " + wp1.getHoverName().getString() + " 5★", leftX, cardY + 66, halfW, 15, 0xFFFFAA, 0x33B8860B, 0xFFFFD700);
            if (!wp2.isEmpty()) {
                renderBadge(graphics, "✦ " + wp2.getHoverName().getString() + " 5★", leftX + halfW + 6, cardY + 66, halfW, 15, 0xFFFFAA, 0x33B8860B, 0xFFFFD700);
            }

            graphics.drawString(this.font, "§d★★★★ Armas 4★ Destacadas:", leftX, cardY + 85, 0xFFDA70D6, false);
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
            // BANNER DE PRINCIPIANTES
            graphics.drawString(this.font, "§e★★★★ Personajes del Gacha de Bienvenida:", leftX, cardY + 54, 0xFFFFE082, false);
            int halfW = (leftW - 6) / 2;
            renderBadge(graphics, "✦ Noelle 4★ (Favonius) ¡Garantizada en 1er x10!", leftX, cardY + 66, halfW, 15, 0xFFFFD700, 0x33B8860B, 0xFFFFD700);
            renderBadge(graphics, "✦ Cha Hae-In 4★ (Cazadora de Rango S)", leftX + halfW + 6, cardY + 66, halfW, 15, 0xFFFFF59D, 0x33F57F17, 0xFFFFE082);

            graphics.drawString(this.font, "§d★★★★ Pool Promocional:", leftX, cardY + 85, 0xFFDA70D6, false);
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
            // BANNER PERMANENTE
            graphics.drawString(this.font, "§b★★★★★ Arsenal General de Celestia & Monarcas:", leftX, cardY + 54, 0xFF80D8FF, false);
            int chipW = (leftW - 12) / 3;
            renderBadge(graphics, "✦ Espada Asrahan", leftX, cardY + 66, chipW, 15, 0xFFFFAA, 0x331565C0, 0xFF64B5F6);
            renderBadge(graphics, "✦ Balada del Alba", leftX + chipW + 6, cardY + 66, chipW, 15, 0xFFFFAA, 0x331565C0, 0xFF64B5F6);
            renderBadge(graphics, "✦ Guadaña Dragón", leftX + (chipW + 6) * 2, cardY + 66, chipW, 15, 0xFFFFAA, 0x331565C0, 0xFF64B5F6);

            graphics.drawString(this.font, "§d★★★★ Arsenal General Disponible:", leftX, cardY + 85, 0xFFDA70D6, false);
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

        // --- COLUMNA DERECHA: Escenario 3D con Pedestal Mágico Interactivo ---
        int cx = stageX + stageW / 2;
        int cy = stageY + stageH - 16;
        int radX = stageW / 2 - 8;
        int radY = 11;

        // Pedestal mágico
        renderPedestal(graphics, cx, cy, radX, radY, pedestalGlow, cardBorder);

        // Partículas estelares que ascienden del pedestal
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

        if (isPromotionalCharacterBanner()) {
            // EXHIBICIÓN DEL PERSONAJE PROMOCIONAL ACTIVO DE LA FASE (22 DÍAS MC)
            CompanionProfile showcase = (promoPhase.secondaryFeaturedCharacter() != null && ((now / 4000L) % 2 == 1))
                    ? promoPhase.secondaryFeaturedCharacter()
                    : promoPhase.featuredCharacter();

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
            // EXHIBICIÓN DE NOELLE EN PRINCIPIANTES
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
            // ARMAS DESTACADAS DE LA FASE ACTIVA
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
            // ARMAS PERMANENTES
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

        // --- BARRA DE PITY ESTILO GENSHIN IMPACT ---
        int barY = cardY + cardH - 20;
        int barX = cardX + 12;
        int barW = cardW - 24;
        int barH = 12;

        graphics.fill(barX, barY, barX + barW, barY + barH, 0xDD070B16);
        graphics.renderOutline(barX, barY, barW, barH, 0xFF2A3D66);

        if (activeBanner == 3) {
            float noviceRatio = Math.min(1.0F, (float) beginnerPulls / 20.0F);
            int fillW = (int) (noviceRatio * (barW - 4));
            if (fillW > 0) graphics.fill(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2, 0xFF81C784);
            String novLabel = "§fDeseos Realizados: §e" + beginnerPulls + " / 20 §7(Restantes con Descuento: §a" + (20 - beginnerPulls) + "§7)";
            graphics.drawCenteredString(this.font, novLabel, barX + barW / 2, barY + 2, 0xFFFFFF);
        } else {
            float pity5Ratio = Math.min(1.0F, (float) pity5Star / 80.0F);
            int fillW = (int) (pity5Ratio * (barW - 4));
            int pityColor = (pity5Star >= 73) ? 0xFFFFD700 : 0xFF4A90E2;
            if (fillW > 0) graphics.fill(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2, pityColor);
            String guarText = guaranteed ? "§a✦ ¡100% GARANTIZADO! ✦" : "§e✦ 50/50 PROBABILIDAD ✦";
            String softPityNotice = pity5Star >= 73 ? " §6(¡Soft Pity!)" : "";
            String pityLabel = "§fPity 5★: §e" + pity5Star + "/80" + softPityNotice + " §7| Pity 4★: §d" + pity4Star + "/10 §7| " + guarText;
            graphics.drawCenteredString(this.font, pityLabel, barX + barW / 2, barY + 2, 0xFFFFFF);
        }

        // 5. Controles Inferiores: [Historial], [Gangas Paimon], [Diálogo], [Deseo x1], [Deseo x10]
        int bottomY = getBottomY();

        // Botón Historial
        boolean hoverHist = isHistBtnHover(mouseX, mouseY);
        int histX = cardX;
        int histW = 75;
        int histH = 26;
        graphics.fill(histX, bottomY, histX + histW, bottomY + histH, hoverHist ? 0xFF283B68 : 0xDD141F36);
        graphics.renderOutline(histX, bottomY, histW, histH, hoverHist ? 0xFF64B5F6 : 0xFF2A3D66);
        graphics.drawCenteredString(this.font, "✦ Historial", histX + histW / 2, bottomY + 8, hoverHist ? 0xFFFFFF : 0xBBDEFB);

        // Botón Gangas de Paimon
        boolean hoverShop = isShopBtnHover(mouseX, mouseY);
        int shopX = cardX + 80;
        int shopW = 95;
        int shopH = 26;
        graphics.fill(shopX, bottomY, shopX + shopW, bottomY + shopH, hoverShop ? 0xFF4A1F52 : 0xDD28102E);
        graphics.renderOutline(shopX, bottomY, shopW, shopH, hoverShop ? 0xFFDA70D6 : 0xFF5C246B);
        renderCenteredFittedText(graphics, "✦ Gangas Paimon", shopX + shopW / 2, bottomY + 8, shopW - 6, hoverShop ? 0xFFFFFF : 0xE1BEE7);

        // Botón Senda Divina (Sólo en Banner de Armas)
        if (activeBanner == 0) {
            boolean hoverPath = isPathBtnHover(mouseX, mouseY);
            int pathX = cardX + 180;
            int pathW = 88;
            int pathH = 26;
            graphics.fill(pathX, bottomY, pathX + pathW, bottomY + pathH, hoverPath ? 0xFF5D4037 : 0xDD3E2723);
            graphics.renderOutline(pathX, bottomY, pathW, pathH, hoverPath ? 0xFFFFD700 : 0xFF8D6E63);
            String pathLabel = weaponBannerTarget > 0 ? ("✦ Senda (" + weaponBannerFatePoints + "/1)") : "✦ Senda Divina";
            renderCenteredFittedText(graphics, pathLabel, pathX + pathW / 2, bottomY + 8, pathW - 4, hoverPath ? 0xFFFFFF : 0xFFFFD700);
        }

        // Botón Subespacio Dimensional
        boolean hoverSubspace = isSubspaceBtnHover(mouseX, mouseY);
        int subspaceX = (activeBanner == 0) ? (cardX + 273) : (cardX + 180);
        int subspaceW = 85;
        int subspaceH = 26;
        graphics.fill(subspaceX, bottomY, subspaceX + subspaceW, bottomY + subspaceH, hoverSubspace ? 0xFF1B4F72 : 0xDD0E2E47);
        graphics.renderOutline(subspaceX, bottomY, subspaceW, subspaceH, hoverSubspace ? 0xFF5DADE2 : 0xFF2874A6);
        renderCenteredFittedText(graphics, "✦ Subespacio", subspaceX + subspaceW / 2, bottomY + 8, subspaceW - 4, hoverSubspace ? 0xFFFFFFFF : 0xFF85C1E9);

        // Botones de Tirada a la Derecha
        ItemStack currentFate = (usesIntertwined && SystemItems.INTERTWINED_FATE.isBound())
                ? new ItemStack(SystemItems.INTERTWINED_FATE.get())
                : (SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : new ItemStack(Items.EMERALD));

        int wish10W = (activeBanner == 3) ? 145 : 135;
        int wish10X = cardX + cardW - wish10W;
        int wish10H = 28;
        boolean hover10 = isWish10BtnHover(mouseX, mouseY);

        if (activeBanner == 3) {
            // Deseo x10 Principiantes
            graphics.fillGradient(wish10X, bottomY - 1, wish10X + wish10W, bottomY - 1 + wish10H, hover10 ? 0xFFE5B036 : 0xFFB88220, hover10 ? 0xFFB88220 : 0xFF8A5C10);
            graphics.renderOutline(wish10X, bottomY - 1, wish10W, wish10H, hover10 ? 0xFFFFFFFF : 0xFFFFE082);
            if (hover10) graphics.renderOutline(wish10X - 1, bottomY - 2, wish10W + 2, wish10H + 2, 0x66FFE082);
            graphics.renderItem(currentFate, wish10X + 6, bottomY + 5);
            graphics.drawString(this.font, "Deseo x10", wish10X + 26, bottomY + 3, 0xFFFFFFFF, true);
            boolean hasFates10 = acquaintFates >= 8;
            String cost10Text = hasFates10 ? "×8 Destinos" : "1280 Primos";
            renderFittedText(graphics, cost10Text, wish10X + 26, bottomY + 15, wish10W - 28, hasFates10 ? 0xFF80D8FF : 0xFFFFE082, false);

            // Cinta de Descuento -20%
            graphics.fill(wish10X + wish10W - 38, bottomY - 4, wish10X + wish10W, bottomY + 7, 0xFFD32F2F);
            graphics.renderOutline(wish10X + wish10W - 38, bottomY - 4, 38, 11, 0xFFFFD700);
            graphics.drawCenteredString(this.font, "§f§l-20%", wish10X + wish10W - 19, bottomY - 3, 0xFFFFFF);

        } else {
            int wish1W = 120;
            int wish1X = wish10X - 8 - wish1W;
            boolean hover1 = isWish1BtnHover(mouseX, mouseY);

            // Deseo x1
            graphics.fillGradient(wish1X, bottomY - 1, wish1X + wish1W, bottomY - 1 + wish10H, hover1 ? 0xFF2A3D66 : 0xDD14203A, hover1 ? 0xFF1B2945 : 0xDD0D1526);
            graphics.renderOutline(wish1X, bottomY - 1, wish1W, wish10H, hover1 ? 0xFFFFFFFF : 0xFF354C75);
            graphics.renderItem(currentFate, wish1X + 6, bottomY + 5);
            graphics.drawString(this.font, "Deseo x1", wish1X + 26, bottomY + 3, 0xFFFFFFFF, true);
            boolean hasFates1 = (usesIntertwined ? intertwinedFates : acquaintFates) >= 1;
            String cost1Text = hasFates1 ? "×1 Destino" : "160 Primos";
            renderFittedText(graphics, cost1Text, wish1X + 26, bottomY + 15, wish1W - 28, hasFates1 ? 0xFF80D8FF : 0xFFFFE082, false);

            // Deseo x10
            graphics.fillGradient(wish10X, bottomY - 1, wish10X + wish10W, bottomY - 1 + wish10H, hover10 ? 0xFFE5B036 : 0xFFB88220, hover10 ? 0xFFB88220 : 0xFF8A5C10);
            graphics.renderOutline(wish10X, bottomY - 1, wish10W, wish10H, hover10 ? 0xFFFFFFFF : 0xFFFFE082);
            if (hover10) graphics.renderOutline(wish10X - 1, bottomY - 2, wish10W + 2, wish10H + 2, 0x66FFE082);
            graphics.renderItem(currentFate, wish10X + 6, bottomY + 5);
            graphics.drawString(this.font, "Deseo x10", wish10X + 26, bottomY + 3, 0xFFFFFFFF, true);
            boolean hasFates10 = (usesIntertwined ? intertwinedFates : acquaintFates) >= 10;
            String cost10Text = hasFates10 ? "×10 Destinos" : "1600 Primos";
            renderFittedText(graphics, cost10Text, wish10X + 26, bottomY + 15, wish10W - 28, hasFates10 ? 0xFF80D8FF : 0xFFFFE082, false);
        }

        // Bocadillo de Paimon en el centro inferior (Limpio, 2 líneas legibles a escala 1.0)
        int leftEdgeBtns = (activeBanner == 0) ? (cardX + 273 + 85) : (cardX + 180 + 85);
        int wishLeftEdge = (activeBanner == 3) ? wish10X : (wish10X - 8 - 120);
        int paimonX = leftEdgeBtns + 6;
        int paimonW = wishLeftEdge - paimonX - 6;
        if (paimonW > 50) {
            graphics.fill(paimonX, bottomY, paimonX + paimonW, bottomY + 26, 0xDD140D26);
            graphics.renderOutline(paimonX, bottomY, paimonW, 26, 0xFFFFD700);
            graphics.drawString(this.font, "§d§l✦ Paimon:", paimonX + 6, bottomY + 4, 0xFFDA70D6, false);
            String shortAdvice;
            if (activeBanner == 0) {
                shortAdvice = weaponBannerTarget > 0 ? ("§eSenda fijada (" + weaponBannerFatePoints + "/1)") : "§7¡Fija tu Senda Divina!";
            } else if (isPromotionalCharacterBanner()) {
                shortAdvice = guaranteed ? "§a¡Próximo 5★ Garantizado!" : "§e¡A por el 50/50!";
            } else if (activeBanner == 3) {
                shortAdvice = "§a¡-20% dto. y Noelle!";
            } else {
                shortAdvice = "§b¡Arsenal permanente!";
            }
            renderFittedText(graphics, shortAdvice, paimonX + 6, bottomY + 14, paimonW - 12, 0xFFFFFF, false);

            if (mouseX >= paimonX && mouseX <= paimonX + paimonW && mouseY >= bottomY && mouseY <= bottomY + 26) {
                String fullMsg;
                if (activeBanner == 0) {
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

        // Tooltips de Cápsulas Superiores (Destinos, Protogemas, Polvo/Brillo, Mora)
        if (mouseY >= pillY && mouseY <= pillY + pillH) {
            if (mouseX >= pill1X && mouseX <= pill1X + pill1W) {
                String fateName = usesIntertwined ? "Destino Entrelazado" : "Destino del Encuentro";
                graphics.renderTooltip(this.font, Component.literal("§e✦ " + fateName + ": §f" + fateCount), (int) mouseX, (int) mouseY);
            } else if (mouseX >= pill2X && mouseX <= pill2X + pill2W) {
                graphics.renderTooltip(this.font, Component.literal("§6✦ Protogemas: §f" + String.format(java.util.Locale.US, "%,d", primogems)), (int) mouseX, (int) mouseY);
            } else if (mouseX >= pill3X && mouseX <= pill3X + pill3W && pill3X > totalSubModeW + 8) {
                if (mouseX < glitterX) {
                    graphics.renderTooltip(this.font, Component.literal("§b✦ Polvo Estelar: §f" + String.format(java.util.Locale.US, "%,d", stardust)), (int) mouseX, (int) mouseY);
                } else {
                    graphics.renderTooltip(this.font, Component.literal("§6✦ Brillo Estelar: §f" + String.format(java.util.Locale.US, "%,d", starglitter)), (int) mouseX, (int) mouseY);
                }
            } else if (mouseX >= pill4X && mouseX <= pill4X + pill4W && pill4X > totalSubModeW + 8) {
                graphics.renderTooltip(this.font, Component.literal("§6🪙 Mora: §f" + String.format(java.util.Locale.US, "%,d", mora)), (int) mouseX, (int) mouseY);
            }
        }
    }

    // ==========================================
    // MODAL DE HISTORIAL DE DESEOS Y PITY
    // ==========================================
    private void renderHistoryModal(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, getVirtW(), getVirtH(), 0xEE05070E);

        int mW = 460;
        int mH = 260;
        int mX = getVirtW() / 2 - mW / 2;
        int mY = getVirtH() / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF10172A);
        graphics.renderOutline(mX, mY, mW, mH, 0xFF3B82F6);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x33FFFFFF);

        // Cabecera
        graphics.drawString(this.font, "§6§l✦ HISTORIAL DE DESEOS Y ESTADÍSTICAS DE PITY ✦", mX + 14, mY + 12, 0xFFFFD700, false);

        // Botón [X] Cerrar
        int closeX = mX + mW - 22;
        int closeY = mY + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        // Resumen de Pity
        int statY = mY + 30;
        int statH = 50;
        graphics.fill(mX + 12, statY, mX + mW - 12, statY + statH, 0xFF1A243D);
        graphics.renderOutline(mX + 12, statY, mW - 24, statH, 0xFF2A3A5E);

        String softPityNotice = pity5Star >= 73 ? " §a(¡Soft Pity Activo!)" : "";
        graphics.drawString(this.font, "§fPity 5★ Acumulado: §e" + pity5Star + " / 80" + softPityNotice, mX + 18, statY + 5, 0xFFFFFF, false);
        graphics.drawString(this.font, "§fPity 4★ Acumulado: §d" + pity4Star + " / 10", mX + 250, statY + 5, 0xFFFFFF, false);
        graphics.drawString(this.font, "§fGarantía Promocional: " + (guaranteed ? "§a¡100% GARANTIZADO!" : "§e50/50 Probabilidad"), mX + 18, statY + 19, 0xFFFFFF, false);
        graphics.drawString(this.font, "§fTiradas Principiante: §b" + beginnerPulls + " / 20", mX + 250, statY + 19, 0xFFFFFF, false);
        graphics.drawString(this.font, "§fDeseos Totales Realizados: §6" + totalWishes + " §e✦", mX + 18, statY + 33, 0xFFFFFF, false);
        graphics.drawString(this.font, "§fProtogemas Invertidas: §b" + String.format("%,d", (long) totalWishes * 160) + " §7💎", mX + 250, statY + 33, 0xFFFFFF, false);

        // Tabla de tiradas (Historial completo paginado)
        int tableY = statY + 58;
        int totalWishesCount = wishHistory.size();
        int visibleRows = 7;
        int totalPages = Math.max(1, (int) Math.ceil((double) totalWishesCount / (double) visibleRows));
        if (historyPage >= totalPages) historyPage = totalPages - 1;
        if (historyPage < 0) historyPage = 0;

        graphics.drawString(this.font, "§7Historial de Deseos (Pág. " + (historyPage + 1) + " / " + totalPages + " - " + totalWishesCount + " registrados):", mX + 14, tableY, 0xAAAAAA, false);

        int rowY = tableY + 14;
        if (wishHistory.isEmpty()) {
            graphics.drawCenteredString(this.font, "§7Aún no has realizado ningún deseo en esta partida.", mX + mW / 2, rowY + 30, 0x888888);
        } else {
            int startIndex = historyPage * visibleRows;
            int endIndex = Math.min(startIndex + visibleRows, wishHistory.size());
            for (int i = startIndex; i < endIndex; i++) {
                int displayIndex = i - startIndex;
                String raw = wishHistory.get(i);
                String[] parts = raw.split("::");
                String name = parts.length > 0 ? parts[0] : "Objeto Desconocido";
                int stars = 3;
                try {
                    stars = parts.length > 1 ? Integer.parseInt(parts[1]) : 3;
                } catch (Exception ignored) {}
                String banner = parts.length > 2 ? parts[2] : "Deseo";

                int rowColor = (displayIndex % 2 == 0) ? 0x221E293B : 0x111E293B;
                graphics.fill(mX + 12, rowY + displayIndex * 18, mX + mW - 12, rowY + (displayIndex + 1) * 18, rowColor);

                String starPrefix = stars == 5 ? "§6⭐⭐⭐⭐⭐" : (stars == 4 ? "§d⭐⭐⭐⭐" : "§b⭐⭐⭐");
                graphics.drawString(this.font, starPrefix, mX + 18, rowY + displayIndex * 18 + 5, 0xFFFFFF, false);
                renderFittedText(graphics, name, mX + 105, rowY + displayIndex * 18 + 5, 200, stars == 5 ? 0xFFFFD700 : (stars == 4 ? 0xFFDA70D6 : 0xFFBBDEFB), false);
                graphics.drawString(this.font, "§8[" + banner + "]", mX + 315, rowY + displayIndex * 18 + 5, 0x888888, false);
            }

            // Controles de paginación inferiores
            int btnPageY = mY + mH - 24;
            int btnPrevX = mX + 14;
            int btnPrevW = 85;
            int btnNextX = mX + mW - 14 - 85;
            int btnNextW = 85;
            int btnH = 16;

            boolean hoverPrev = historyPage > 0 && mouseX >= btnPrevX && mouseX <= btnPrevX + btnPrevW && mouseY >= btnPageY && mouseY <= btnPageY + btnH;
            boolean hoverNext = historyPage < totalPages - 1 && mouseX >= btnNextX && mouseX <= btnNextX + btnNextW && mouseY >= btnPageY && mouseY <= btnPageY + btnH;

            // Botón Anterior
            int prevBg = historyPage > 0 ? (hoverPrev ? 0xFF2563EB : 0xFF1D4ED8) : 0xFF1E293B;
            graphics.fill(btnPrevX, btnPageY, btnPrevX + btnPrevW, btnPageY + btnH, prevBg);
            graphics.renderOutline(btnPrevX, btnPageY, btnPrevW, btnH, historyPage > 0 ? (hoverPrev ? 0xFF60A5FA : 0xFF3B82F6) : 0xFF334155);
            graphics.drawCenteredString(this.font, historyPage > 0 ? "◀ Anterior" : "§7◀ Anterior", btnPrevX + btnPrevW / 2, btnPageY + 4, historyPage > 0 ? 0xFFFFFF : 0x777777);

            // Indicador de página
            graphics.drawCenteredString(this.font, "§ePágina " + (historyPage + 1) + " / " + totalPages, mX + mW / 2, btnPageY + 4, 0xFFE2E8F0);

            // Botón Siguiente
            int nextBg = historyPage < totalPages - 1 ? (hoverNext ? 0xFF2563EB : 0xFF1D4ED8) : 0xFF1E293B;
            graphics.fill(btnNextX, btnPageY, btnNextX + btnNextW, btnPageY + btnH, nextBg);
            graphics.renderOutline(btnNextX, btnPageY, btnNextW, btnH, historyPage < totalPages - 1 ? (hoverNext ? 0xFF60A5FA : 0xFF3B82F6) : 0xFF334155);
            graphics.drawCenteredString(this.font, historyPage < totalPages - 1 ? "Siguiente ▶" : "§7Siguiente ▶", btnNextX + btnNextW / 2, btnPageY + 4, historyPage < totalPages - 1 ? 0xFFFFFF : 0x777777);
        }
    }

    // ==========================================
    // MODAL DE GANGAS DE PAIMON (TIENDA GACHA)
    // ==========================================
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

        // Botón Regresar [ ⬅ Banners ] (Alineado a la izquierda superior)
        int backBtnX = mX + 10;
        int backBtnY = mY + 8;
        int backBtnW = Math.min(100, (mW - 40) / 4);
        int backBtnH = 14;
        boolean hoverBack = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH;
        graphics.fill(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + backBtnH, hoverBack ? 0xFF3D2706 : 0xAA101624);
        graphics.renderOutline(backBtnX, backBtnY, backBtnW, backBtnH, hoverBack ? 0xFFFFD700 : 0xFF334155);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.back").getString(), backBtnX + backBtnW / 2, backBtnY + 3, backBtnW - 4, hoverBack ? 0xFFFFE082 : 0xCBD5E1);

        // Botón Cerrar [X] (Alineado a la derecha superior)
        int closeX = mX + mW - 22;
        int closeY = mY + 8;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        // Título Centrado entre el botón volver y el botón cerrar
        int titleMaxW = Math.max(100, closeX - (backBtnX + backBtnW) - 16);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.title").getString(), mX + mW / 2, mY + 9, titleMaxW, 0xFFFFD700);

        // Barra de Balances Centrada y Auto-ajustable
        boolean isWelkinEnh = false;
        if (Minecraft.getInstance().player != null) {
            SystemData d = Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (d != null) isWelkinEnh = d.isWelkinEnhanced();
        }
        String balStr = "§6🪙 " + String.format(java.util.Locale.US, "%,d", coins) + " §7(5k=$1) | §a💳 $" + String.format(java.util.Locale.US, "%.2f", usdBalance) + " USD | §b💎 " + genesisCrystals + " | §d💠 " + stellarJade + (welkinDays > 0 ? (" §6| 🌙 " + welkinDays + "d" + (isWelkinEnh ? " §d(+50%)" : "")) : "");
        renderCenteredFittedText(graphics, balStr, mX + mW / 2, mY + 24, mW - 24, 0xFFE0E0E0);

        int colGap = 10;
        int colW = (mW - 20 - colGap) / 2;
        int leftX = mX + 10;
        int rightX = leftX + colW + colGap;
        int panelY = mY + 38;
        int convH = 22;
        int convY = mY + mH - convH - 8;
        int panelH = convY - panelY - 6;

        // PANEL IZQUIERDO: GIFT CARDS
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

        // PANEL DERECHO: PAQUETES DE CRISTALES & BENDICIÓN
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
                        Component.literal("§7- §b+" + String.format(java.util.Locale.US, "%,d", dbl) + " Cristales Génesis §6§l(2X)"),
                        Component.literal("§7- §d+" + String.format(java.util.Locale.US, "%,d", dbl) + " Jade Estelar §6§l(2X)")
                );
            }

            // Botón USD
            int btnY = rowY + 3;
            boolean canUsd = usdBalance >= packUsd[i] - 0.001;
            boolean hoverUsd = mouseX >= crBtnUsdX && mouseX <= crBtnUsdX + crBtnW && mouseY >= btnY && mouseY <= btnY + crBtnH;
            graphics.fill(crBtnUsdX, btnY, crBtnUsdX + crBtnW, btnY + crBtnH, canUsd ? (hoverUsd ? 0xFF0277BD : 0xFF01579B) : 0xFF263238);
            graphics.renderOutline(crBtnUsdX, btnY, crBtnW, crBtnH, canUsd ? 0xFF4FC3F7 : 0xFF37474F);
            renderCenteredFittedText(graphics, packPrices[i], crBtnUsdX + crBtnW / 2, btnY + 4, crBtnW - 4, canUsd ? 0xFFFFFF : 0x78909C);
        }

        // PANEL INFERIOR: CONVERSIÓN 1:1 DIRECTA
        int btnConvW = colW;

        // Botón 1: Cristales Génesis -> Protogemas
        int b1X = leftX;
        boolean canConv1 = genesisCrystals > 0;
        boolean hoverB1 = mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + convH;
        graphics.fill(b1X, convY, b1X + btnConvW, convY + convH, canConv1 ? (hoverB1 ? 0xFF0D47A1 : 0xFF0A2B66) : 0xFF1C2230);
        graphics.renderOutline(b1X, convY, btnConvW, convH, canConv1 ? (hoverB1 ? 0xFF64B5F6 : 0xFF1976D2) : 0xFF37474F);
        renderCenteredFittedText(graphics, Component.translatable("awakening_system.gacha.topup.convert_genesis", genesisCrystals).getString(), b1X + btnConvW / 2, convY + 7, btnConvW - 8, canConv1 ? 0xFF80D8FF : 0x78909C);

        // Botón 2: Jade Estelar -> Protogemas
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

        // Cabecera sin colisiones
        graphics.drawString(this.font, "§d§l✦ GANGAS DE PAIMON ✦", mX + 14, mY + 12, 0xFFDA70D6, false);

        // Botón [X] Cerrar
        int closeX = mX + mW - 22;
        int closeY = mY + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        // Balances en la cabecera con sus iconos reales
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

        // Ofertas (2 Columnas: Columna Izquierda Polvo Estelar, Columna Derecha Brillo Estelar)
        ItemStack[] leftItemStacks = {
                SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : ItemStack.EMPTY,
                SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : ItemStack.EMPTY,
                SystemItems.MORA.isBound() ? new ItemStack(SystemItems.MORA.get()) : ItemStack.EMPTY,
                new ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE)
        };
        String[] leftNames = {
                "1x Destino Entrelazado",
                "1x Destino del Encuentro",
                "15,000 Mora",
                "8x Botellas de EXP"
        };
        int[] leftCosts = {75, 75, 15, 10};

        ItemStack[] rightItemStacks = {
                SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : ItemStack.EMPTY,
                SystemItems.ACQUAINT_FATE.isBound() ? new ItemStack(SystemItems.ACQUAINT_FATE.get()) : ItemStack.EMPTY,
                CompanionContractItem.createContract(CompanionProfile.NOELLE),
                CompanionContractItem.createContract(CompanionProfile.CHA_HAEIN)
        };
        String[] rightNames = {
                "1x Destino Entrelazado",
                "1x Destino del Encuentro",
                "Pacto: Noelle (4★)",
                "Pacto: Cha Hae-In (4★)"
        };
        int[] rightCosts = {5, 5, 34, 34};

        int colW = 230;
        int startY = mY + 36;

        // Columna Izquierda: Polvo Estelar
        if (!stardustStack.isEmpty()) {
            graphics.renderItem(stardustStack, mX + 14, startY - 4);
        }
        graphics.drawString(this.font, "Canje de Polvo Estelar (3★):", mX + 34, startY, 0x80D8FF, false);
        for (int i = 0; i < 4; i++) {
            int y = startY + 14 + i * 48;
            int offerId = (i == 0) ? 0 : ((i == 1) ? 1 : ((i == 2) ? 6 : 7));
            boolean canAfford = stardust >= leftCosts[i];

            graphics.fill(mX + 14, y, mX + 14 + colW, y + 42, 0xFF0E1324);
            graphics.renderOutline(mX + 14, y, colW, 42, 0xFF2A3D66);

            // Icono del objeto a canjear
            if (!leftItemStacks[i].isEmpty()) {
                graphics.renderItem(leftItemStacks[i], mX + 18, y + 13);
            }

            renderFittedText(graphics, "§f" + leftNames[i], mX + 38, y + 6, colW - 95, 0xFFFFFF, false);
            graphics.drawString(this.font, "§bCoste: " + leftCosts[i], mX + 38, y + 24, 0x80D8FF, false);
            int cNumW = this.font.width("§bCoste: " + leftCosts[i]);
            if (!stardustStack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(mX + 38 + cNumW + 3, y + 22, 0);
                graphics.pose().scale(0.7F, 0.7F, 0.7F);
                graphics.renderItem(stardustStack, 0, 0);
                graphics.pose().popPose();
                graphics.drawString(this.font, "Polvo", mX + 38 + cNumW + 16, y + 24, 0x80D8FF, false);
            } else {
                graphics.drawString(this.font, "Polvo", mX + 38 + cNumW + 4, y + 24, 0x80D8FF, false);
            }

            int bX = mX + 14 + colW - 68;
            int bY = y + 10;
            boolean hoverBuy = mouseX >= bX && mouseX <= bX + 60 && mouseY >= bY && mouseY <= bY + 22;
            int btnColor = canAfford ? (hoverBuy ? 0xFF3558A8 : 0xFF1B2F63) : 0xFF222233;
            graphics.fill(bX, bY, bX + 60, bY + 22, btnColor);
            graphics.renderOutline(bX, bY, 60, 22, canAfford ? (hoverBuy ? 0xFFFFFFFF : 0xFF4A7BD4) : 0xFF444455);
            graphics.drawCenteredString(this.font, canAfford ? "Canjear" : "Falta", bX + 30, bY + 7, canAfford ? 0xFFFFFF : 0x777788);
        }

        // Columna Derecha: Brillo Estelar
        int rightColX = mX + 14 + colW + 12;
        if (!starglitterStack.isEmpty()) {
            graphics.renderItem(starglitterStack, rightColX, startY - 4);
        }
        graphics.drawString(this.font, "Canje de Brillo Estelar (4★/5★):", rightColX + 20, startY, 0xFFE082, false);
        for (int i = 0; i < 4; i++) {
            int y = startY + 14 + i * 48;
            int offerId = (i == 0) ? 2 : ((i == 1) ? 3 : ((i == 2) ? 4 : 5));
            boolean canAfford = starglitter >= rightCosts[i];

            graphics.fill(rightColX, y, rightColX + colW, y + 42, 0xFF1E0E28);
            graphics.renderOutline(rightColX, y, colW, 42, 0xFF5C246B);

            // Icono del objeto a canjear
            if (!rightItemStacks[i].isEmpty()) {
                graphics.renderItem(rightItemStacks[i], rightColX + 4, y + 13);
            }

            renderFittedText(graphics, "§f" + rightNames[i], rightColX + 24, y + 6, colW - 85, 0xFFFFFF, false);
            graphics.drawString(this.font, "§6Coste: " + rightCosts[i], rightColX + 24, y + 24, 0xFFFFD700, false);
            int cNumW2 = this.font.width("§6Coste: " + rightCosts[i]);
            if (!starglitterStack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(rightColX + 24 + cNumW2 + 3, y + 22, 0);
                graphics.pose().scale(0.7F, 0.7F, 0.7F);
                graphics.renderItem(starglitterStack, 0, 0);
                graphics.pose().popPose();
                graphics.drawString(this.font, "Brillo", rightColX + 24 + cNumW2 + 16, y + 24, 0xFFFFD700, false);
            } else {
                graphics.drawString(this.font, "Brillo", rightColX + 24 + cNumW2 + 4, y + 24, 0xFFFFD700, false);
            }

            int bX = rightColX + colW - 68;
            int bY = y + 10;
            boolean hoverBuy = mouseX >= bX && mouseX <= bX + 60 && mouseY >= bY && mouseY <= bY + 22;
            int btnColor = canAfford ? (hoverBuy ? 0xFFA07825 : 0xFF664810) : 0xFF2B2222;
            graphics.fill(bX, bY, bX + 60, bY + 22, btnColor);
            graphics.renderOutline(bX, bY, 60, 22, canAfford ? (hoverBuy ? 0xFFFFFFFF : 0xFFFFD700) : 0xFF554444);
            graphics.drawCenteredString(this.font, canAfford ? (i >= 2 ? "Reclutar" : "Canjear") : "Falta", bX + 30, bY + 7, canAfford ? 0xFFFFD700 : 0x887777);
        }

        // Tooltips de los objetos canjeables
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

    // ==========================================
    // MODAL DE SENDA DIVINA (EPITOMIZED PATH)
    // ==========================================
    private void renderPathModal(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, getVirtW(), getVirtH(), 0xEE05070E);

        int mW = 460;
        int mH = 260;
        int mX = getVirtW() / 2 - mW / 2;
        int mY = getVirtH() / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF181105);
        graphics.renderOutline(mX, mY, mW, mH, 0xFFFFD700);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x33FFE082);

        // Cabecera
        graphics.drawString(this.font, "§6§l✦ SENDA DIVINA (EPITOMIZED PATH) ✦", mX + 14, mY + 12, 0xFFFFD700, false);

        // Botón [X] Cerrar
        int closeX = mX + mW - 22;
        int closeY = mY + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        // Estado de Puntos de Destino
        int statY = mY + 28;
        graphics.fill(mX + 12, statY, mX + mW - 12, statY + 36, 0xFF2A1C0A);
        graphics.renderOutline(mX + 12, statY, mW - 24, 36, 0xFF5D4037);

        String fateStr = "§fPuntos de Destino acumulados: §e" + weaponBannerFatePoints + " / 1 §7(Máx. 1 punto)";
        graphics.drawString(this.font, fateStr, mX + 18, statY + 6, 0xFFFFFF, false);

        String statusNotice;
        if (weaponBannerTarget <= 0) {
            statusNotice = "§7No has fijado ningún rumbo todavía. ¡Elige un arma para comenzar!";
        } else if (weaponBannerFatePoints >= 1) {
            statusNotice = "§a✦ ¡GARANTÍA ACTIVA! Tu siguiente arma 5★ será la que elegiste al 100%.";
        } else {
            statusNotice = "§eSi tu siguiente arma 5★ no es la elegida, obtendrás 1 Punto de Destino.";
        }
        graphics.drawString(this.font, statusNotice, mX + 18, statY + 20, 0xFFFFFF, false);

        // Dos armas promocionales de la fase actual
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

        // Pie explicativo
        graphics.drawCenteredString(this.font, "§8Al obtener el arma fijada, los puntos de destino se reiniciarán a 0.", mX + mW / 2, mY + mH - 14, 0x888888);
    }

    private void renderPathWeaponCard(GuiGraphics graphics, ItemStack weapon, int targetIdx, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean isSelected = (weaponBannerTarget == targetIdx);
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        int bg = isSelected ? 0xFF352006 : (hover ? 0xFF2A1C0A : 0xFF1C1306);
        int border = isSelected ? 0xFFFFD700 : (hover ? 0xFFB8860B : 0xFF4A3515);

        graphics.fill(x, y, x + w, y + h, bg);
        graphics.renderOutline(x, y, w, h, border);

        // Icono del arma grande
        graphics.pose().pushPose();
        graphics.pose().translate(x + w / 2, y + 35, 100);
        graphics.pose().scale(2.5F, 2.5F, 2.5F);
        graphics.renderItem(weapon, -8, -8);
        graphics.pose().popPose();

        // Nombre
        renderCenteredFittedText(graphics, weapon.getHoverName().getString(), x + w / 2, y + 68, w - 12, isSelected ? 0xFFFFD700 : 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§6★★★★★ Arma", x + w / 2, y + 80, 0xFFFFD700);

        // Botón Fijar Rumbo
        int btnW = 120;
        int btnH = 20;
        int btnX = x + (w - btnW) / 2;
        int btnY = y + h - 26;
        boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

        if (isSelected) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF2E7D32);
            graphics.renderOutline(btnX, btnY, btnW, btnH, 0xFF81C784);
            graphics.drawCenteredString(this.font, "§a✔ Rumbo Fijado", btnX + btnW / 2, btnY + 6, 0xFFFFFF);
        } else {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverBtn ? 0xFFB8860B : 0xFF5D4037);
            graphics.renderOutline(btnX, btnY, btnW, btnH, hoverBtn ? 0xFFFFD700 : 0xFF8D6E63);
            graphics.drawCenteredString(this.font, "✦ Fijar Rumbo", btnX + btnW / 2, btnY + 6, hoverBtn ? 0xFFFFFF : 0xFFE082);
        }
    }

    // ==========================================
    // FASE 1: ANIMACIÓN CINEMÁTICA DEL METEORITO
    // ==========================================
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

        if (SystemItems.PRIMOGEM.isBound()) {
            graphics.pose().pushPose();
            graphics.pose().translate(cometX, cometY, 80);
            graphics.pose().scale(2.0F, 2.0F, 2.0F);
            graphics.renderItem(new ItemStack(SystemItems.PRIMOGEM.get()), -8, -8);
            graphics.pose().popPose();
        }

        String splash = highestWonRarity == 5 ? "§6§l✦✦✦ ¡DESEO DORADO LEGENDARIO 5★! ✦✦✦" : (highestWonRarity == 4 ? "§d§l✦ DESEO PÚRPURA CELESTIAL 4★ ✦" : "§b✦ DESEO ASTRAL 3★ ✦");
        graphics.drawCenteredString(this.font, splash, getVirtW() / 2, 28, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Descendiendo del firmamento estelar...", getVirtW() / 2, 42, 0xCCCCCC);

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

    // ==========================================
    // FASE 2: REVELACIÓN INDIVIDUAL
    // ==========================================
    private void renderSingleReveal(GuiGraphics graphics, int mouseX, int mouseY) {
        if (resultItems.isEmpty() || currentRevealIndex >= resultItems.size()) {
            this.screenState = STATE_SUMMARY;
            playResultListSound();
            return;
        }

        ItemStack item = resultItems.get(currentRevealIndex);
        int stars = (currentRevealIndex < itemRarities.size()) ? itemRarities.get(currentRevealIndex) : getItemStarRarity(item);

        renderSkipButton(graphics, mouseX, mouseY);

        graphics.drawString(this.font, "§7Objeto " + (currentRevealIndex + 1) + " de " + resultItems.size(), 20, 20, 0xAAAAAA, false);
        graphics.drawCenteredString(this.font, "§7(Haz clic en cualquier parte o pulsa Espacio para continuar)", getVirtW() / 2, getVirtH() - 28, 0x888888);

        long timeSinceReveal = System.currentTimeMillis() - this.revealStartTime;
        float pulse = (float) Math.sin(timeSinceReveal * 0.005D);

        int cx = getVirtW() / 2;
        int cy = getVirtH() / 2 - 24;

        // Aura cósmica suave y difusa (sin bordes cuadrados rígidos)
        int auraOuter = (int) (84 + pulse * 6);
        int auraInner = (int) (58 + pulse * 4);
        int auraColorOuter = stars == 5 ? 0x22FFD700 : (stars == 4 ? 0x24BA68C8 : 0x1A64B5F6);
        int auraColorInner = stars == 5 ? 0x44FFD700 : (stars == 4 ? 0x3EBA68C8 : 0x2C64B5F6);

        graphics.fill(cx - auraOuter, cy - auraOuter, cx + auraOuter, cy + auraOuter, auraColorOuter);
        graphics.fill(cx - auraInner, cy - auraInner, cx + auraInner, cy + auraInner, auraColorInner);

        if (item.getItem() instanceof CompanionContractItem) {
            CompanionProfile profile = CompanionContractItem.getProfile(item);
            CompanionEntity dummy = getOrCreateDummyCompanion(profile);
            if (dummy != null) {
                // Disco celestial de invocación (pedestal suave elíptico bajo los pies)
                int pedY = cy + 42;
                int pedW = 54;
                graphics.fill(cx - pedW, pedY, cx + pedW, pedY + 3, stars == 5 ? 0x88FFD700 : 0x88BA68C8);
                graphics.fill(cx - pedW + 10, pedY - 1, cx + pedW - 10, pedY + 4, stars == 5 ? 0xCCFFD700 : 0xCCBA68C8);
                graphics.fill(cx - pedW + 22, pedY, cx + pedW - 22, pedY + 3, 0xFFFFFFFF);

                // Renderizado de la entidad viva 3D siguiendo al cursor con escala 56
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

    // ==========================================
    // FASE 3: RESUMEN DE TIRADA EN MATRIZ
    // ==========================================
    private void renderSummaryGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, "§6§l✦ RESUMEN DE DESEOS OBTENIDOS ✦", getVirtW() / 2, 14, 0xFFFFFF);

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

            if (stack.getItem() instanceof CompanionContractItem) {
                CompanionProfile profile = CompanionContractItem.getProfile(stack);

                // Marco y retrato del compañero (cara auténtica de la skin)
                int faceSize = 36;
                int faceX = cx - faceSize / 2;
                int faceY = y + 7;

                // Fondo oscuro del retrato
                graphics.fill(faceX, faceY, faceX + faceSize, faceY + faceSize, 0x88000000);

                // Renderizar la cara del personaje (base + capa externa/cabello)
                renderCompanionFace(graphics, profile.getTextureLocation(), faceX, faceY, faceSize);

                // Borde elegante del retrato
                graphics.renderOutline(faceX - 1, faceY - 1, faceSize + 2, faceSize + 2, isHover ? 0xFFFFFFFF : slotBorder);

                // Insignia elemental en la esquina superior izquierda
                int elemColor = getElementBgColor(profile.element());
                graphics.fill(x + 3, y + 3, x + 15, y + 13, 0xDD000000 | (elemColor & 0x00FFFFFF));
                graphics.renderOutline(x + 3, y + 3, 12, 10, 0xAAFFFFFF);
                String elemInitial = profile.element().substring(0, 1).toUpperCase();
                graphics.drawCenteredString(this.font, elemInitial, x + 9, y + 4, 0xFFFFFF);
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(cx, y + 26, 50);
                graphics.pose().scale(1.9F, 1.9F, 1.9F);
                graphics.renderItem(stack, -8, -8);
                graphics.pose().popPose();
            }

            // Estrellas doradas/violetas nítidas
            String starsTxt = "★".repeat(stars);
            int sColor = stars == 5 ? 0xFFFFD700 : (stars == 4 ? 0xFFDA70D6 : 0xFF80D8FF);
            graphics.drawCenteredString(this.font, starsTxt, cx, y + 47, sColor);

            // Nombre limpio sin prefijos redundantes
            String labelName;
            if (stack.getItem() instanceof CompanionContractItem) {
                CompanionProfile profile = CompanionContractItem.getProfile(stack);
                labelName = profile.displayName();
            } else {
                labelName = stack.getHoverName().getString();
                labelName = labelName.replaceAll("\\[[★\\d\\s]+\\]", "").trim();
            }
            int nameColor = isHover ? 0xFFFFFFFF : (stars == 5 ? 0xFFFFE082 : (stars == 4 ? 0xFFF3E5F5 : 0xDDDDDD));
            renderCenteredFittedText(graphics, labelName, cx, y + 60, slotW - 6, nameColor);
        }

        // Paimon reacción
        int paimonSummaryY = getVirtH() - 76;
        int paimonW = Math.min(520, getVirtW() - 32);
        int paimonX = getVirtW() / 2 - paimonW / 2;
        String paimonReaction = highestWonRarity == 5
                ? "§d§l[Paimon] §f\"¡¡INCREÍBLE VIAJERO!! ¡¡UNA RECOMPENSA DE 5 ESTRELLAS!! ¡Paimon sabía que hoy triunfarías!\""
                : (highestWonRarity == 4
                ? "§d§l[Paimon] §f\"¡Vaya, un objeto o compañero de 4 estrellas! ¡Nada mal, seguro te servirá mucho!\""
                : "§d§l[Paimon] §f\"¡No te desanimes! ¡El pity está acumulado y la próxima tirada será la dorada!\"");
        graphics.fill(paimonX, paimonSummaryY, paimonX + paimonW, paimonSummaryY + 24, 0xFF140D26);
        graphics.renderOutline(paimonX, paimonSummaryY, paimonW, 24, highestWonRarity == 5 ? 0xFFFFD700 : 0xFFDA70D6);
        renderFittedText(graphics, paimonReaction, paimonX + 12, paimonSummaryY + 8, paimonW - 24, 0xFFFFFF, false);

        // Botones inferiores de acción
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

        // Botón Confirmar / Volver
        graphics.fill(btnBackX, btnY, btnBackX + btnW, btnY + btnH, hoverBack ? 0xFF3558A8 : 0xFF1B2F63);
        graphics.renderOutline(btnBackX, btnY, btnW, btnH, hoverBack ? 0xFFFFFFFF : 0xFF4A7BD4);
        graphics.drawCenteredString(this.font, "Confirmar / Volver", btnBackX + btnW / 2, btnY + 9, 0xFFFFFF);

        // Botón Subespacio Dimensional
        graphics.fill(btnSubspaceX, btnY, btnSubspaceX + btnW, btnY + btnH, hoverSubspace ? 0xFF1B4F72 : 0xFF0E2E47);
        graphics.renderOutline(btnSubspaceX, btnY, btnW, btnH, hoverSubspace ? 0xFF5DADE2 : 0xFF2874A6);
        graphics.drawCenteredString(this.font, "✦ Subespacio", btnSubspaceX + btnW / 2, btnY + 9, hoverSubspace ? 0xFFFFFFFF : 0xFF85C1E9);

        // Botón Tirar de nuevo
        String againText = lastPullCount == 10 ? "Tirar x10 de nuevo" : "Tirar x1 de nuevo";
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
        graphics.drawCenteredString(this.font, "Saltar ⏭", skipX + skipW / 2, skipY + 7, hover ? 0xFFFFAA : 0xDDDDDD);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        syncDataFromPlayer();

        // Si el modal de historial está abierto
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

        // Si el modal de recargas está abierto
        if (showTopUpModal) {
            int virtW = getVirtW();
            int virtH = getVirtH();
            int mW = Math.min(520, virtW - 16);
            int mH = Math.min(270, virtH - 16);
            int mX = virtW / 2 - mW / 2;
            int mY = virtH / 2 - mH / 2;

            int backBtnX = mX + 10;
            int backBtnY = mY + 8;
            int backBtnW = Math.min(100, (mW - 40) / 4);
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

            int colGap = 10;
            int colW = (mW - 20 - colGap) / 2;
            int leftX = mX + 10;
            int rightX = leftX + colW + colGap;
            int panelY = mY + 38;

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

            // Clics en Paquetes de Cristales (Panel Derecho - Solo USD)
            double[] packUsd = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};
            int crR_X = rightX + 4;
            int crR_W = colW - 8;
            int crBtnW = Math.min(56, Math.max(44, (crR_W - 80) / 3));
            int crBtnH = 15;
            int crBtnUsdX = crR_X + crR_W - crBtnW - 4;

            for (int i = 0; i < 7; i++) {
                int rowY = panelY + 18 + i * 23;
                int btnY = rowY + 3;

                // Comprar con USD
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

            // Clics en Conversión 1:1
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

        // Si el modal de gangas de Paimon está abierto
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

            // Revisar clics en columna izquierda (Polvo)
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

            // Revisar clics en columna derecha (Brillo)
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

        // Si el modal de senda divina está abierto
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

            // Botón Arma 1
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

            // Botón Arma 2
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

        // Fase 1: Clic durante animación de meteorito
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

        // Fase 2: Clic durante revelación 1 en 1
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

        // Fase 3: Clic en pantalla de resumen final
        if (screenState == STATE_SUMMARY) {
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

        // Fase 0: Menú principal de Banners
        // Clic en botón Salir [ESC]
        if (mouseX >= 10 && mouseX <= 64 && mouseY >= 6 && mouseY <= 23) {
            onClose();
            return true;
        }

        // Clic en botón Historial
        if (isHistBtnHover(mouseX, mouseY)) {
            showHistoryModal = true;
            historyPage = 0;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            return true;
        }

        // Clic en botón Gangas de Paimon
        if (isShopBtnHover(mouseX, mouseY)) {
            showShopModal = true;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            return true;
        }

        // Clic en botón Senda Divina (Banner de armas)
        if (activeBanner == 0 && isPathBtnHover(mouseX, mouseY)) {
            showPathModal = true;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            return true;
        }

        // Clic en botón Subespacio Dimensional
        if (isSubspaceBtnHover(mouseX, mouseY)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
            this.onClose();
            PacketDistributor.sendToServer(new OpenSubspaceC2SPayload());
            return true;
        }

        // 1. Clics en Sub-modos de Fila 1 (Promocional, Armas, Permanente, Principiantes, Tienda USD)
        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 5 : 4;
        int subTabW = hasNovice ? 80 : 96;
        int subTabH = 17;
        int subModeStartX = 10 + 54 + 6;
        int subModeY = 6;

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            if (mouseX >= sx && mouseX <= sx + subTabW && mouseY >= subModeY && mouseY <= subModeY + subTabH) {
                if ((hasNovice && i == 4) || (!hasNovice && i == 3)) {
                    showTopUpModal = true;
                    showShopModal = false;
                    showHistoryModal = false;
                    showPathModal = false;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                    return true;
                }
                showTopUpModal = false;
                if (i == 0) {
                    if (activeBanner != 1) {
                        activeBanner = 1;
                        playFranchiseSwitchSound(activeFranchise);
                    }
                    return true;
                }
                int targetBanner = switch (i) {
                    case 1 -> 0;
                    case 2 -> 2;
                    case 3 -> 3;
                    default -> 1;
                };
                if (activeBanner != targetBanner) {
                    activeBanner = targetBanner;
                    stopCurrentVoiceSound();
                    if (targetBanner == 0) {
                        playVoiceSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.2F, 1.1F);
                    } else if (targetBanner == 2) {
                        playVoiceSound(ModSounds.MUSIC_DISC_DAY_OF_HOPE.get(), 1.0F, 1.0F);
                    } else if (targetBanner == 3) {
                        playVoiceSound(ModSounds.COMPANION_NOELLE_SUMMON.get(), 1.2F, 1.0F);
                    } else {
                        playFranchiseSwitchSound(activeFranchise);
                    }
                }
                return true;
            }
        }

        // Clic en Botones de Tirada
        if (activeBanner == 3) {
            if (isWish10BtnHover(mouseX, mouseY)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                PacketDistributor.sendToServer(new GachaWishPayload(3, 10));
                return true;
            }
        } else {
            if (isWish1BtnHover(mouseX, mouseY)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                PacketDistributor.sendToServer(new GachaWishPayload(activeBanner, 1));
                return true;
            }

            if (isWish10BtnHover(mouseX, mouseY)) {
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
        if (showHistoryModal || showShopModal || showPathModal || showTopUpModal) {
            if (keyCode == 256) { // ESC cierra el modal activo primero
                showHistoryModal = false;
                showShopModal = false;
            showTopUpModal = false;
                showPathModal = false;
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
        if (item != null && item.getItem() instanceof CompanionContractItem) {
            CompanionProfile p = CompanionContractItem.getProfile(item);
            playVoiceSound(p.getSummonSound(), 1.25F, 1.0F);
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
            return "§6[COMPAÑERO/A " + p.stars() + "★ - " + p.element().toUpperCase() + "]";
        }
        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.GenshinWeaponItem gwi) {
            return switch (gwi.getWeaponType()) {
                case ENGULFING_LIGHTNING, PRIMORDIAL_JADE_WINGED_SPEAR, CRIMSON_MOONS_SEMBLANCE, VORTEX_VANQUISHER, THE_CATCH, BLACK_TASSEL -> "§e[LANZA Y ARMA DE ASTA " + stars + "★]";
                case WOLFS_GRAVESTONE, REDHORN_STONETHRESHER, VERDICT, TAILLESS -> "§c[MANDOBLE PESADO " + stars + "★]";
                case HERTA_HAMMER -> "§d[MARTILLO GÉLIDO HSR " + stars + "★]";
                case DOMAIN_OF_SANCTION -> "§d[KATANA DE RAYO HONKAI " + stars + "★]";
                default -> "§b[ESPADA LIGERA " + stars + "★]";
            };
        }
        if (stack.is(SystemItems.STAFF_OF_HOMA.get())) return "§6[LANZA CELESTIAL 5★]";
        if (stack.is(SystemItems.LIGHT_CONE_MILKY_WAY.get())) return "§d[CONO DE LUZ HSR 5★]";
        if (stack.is(SystemItems.COMMAND_SPELL.get())) return "§c[RELIQUIA FATE 5★]";
        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.TeyvatVisionItem) return "§e[VISIÓN ELEMENTAL 5★]";
        if (stars == 5) return "§6[RELIQUIA SUPREMA 5★]";
        if (stars == 4) return "§d[OBJETO MÍSTICO 4★]";
        return "§b[RECURSO DE AVENTURERO 3★]";
    }

    private String getWeaponLorePreview(ItemStack stack) {
        if (stack.getItem() instanceof CompanionContractItem) {
            CompanionProfile p = CompanionContractItem.getProfile(stack);
            return p.displayName() + " - " + p.title() + " (" + p.element() + ")";
        }
        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.GenshinWeaponItem gwi) {
            return gwi.getWeaponType().lore;
        }
        if (stack.is(SystemItems.STAFF_OF_HOMA.get())) return "La vara de fuego que purifica los pecados del mundo terrenal.";
        if (stack.is(SystemItems.LIGHT_CONE_MILKY_WAY.get())) return "La luz eterna del Expreso Astral surcando el mar cósmico.";
        if (stack.is(SystemItems.COMMAND_SPELL.get())) return "Pacto absoluto de obediencia que trasciende el espacio-tiempo.";
        return "Un regalo estelar forjado en los confines del firmamento.";
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
        // Cabeza base de la skin (X=8, Y=8, tamaño 8x8 dentro del mapa 64x64)
        graphics.blit(texture, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
        // Capa externa / accesorios / cabello (X=40, Y=8, tamaño 8x8 dentro del mapa 64x64)
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
