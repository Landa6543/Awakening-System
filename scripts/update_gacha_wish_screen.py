import re

path = "src/main/java/com/soybe/awakeningsystem/GachaWishScreen.java"
with open(path, "r", encoding="utf-8") as f:
    code = f.read()

# 1. Add fields
fields_anchor = "    private int mora = 0;"
new_fields = """    private int mora = 0;
    private int coins = 0;
    private double usdBalance = 0.0;
    private int genesisCrystals = 0;
    private int stellarJade = 0;
    private int welkinDays = 0;
    private boolean showTopUpModal = false;"""

code = code.replace(fields_anchor, new_fields, 1)

# 2. Add previews in initBannerPreviews
preview_anchor = "shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.NEZUKO));"
new_previews = """shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.NEZUKO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.XIAO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ARLECCHINO));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.FIREFLY));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.MEGUMI));
        shonenBannerPreviews.add(CompanionContractItem.createContract(CompanionProfile.ARCHER));"""

code = code.replace(preview_anchor, new_previews, 1)

# 3. Update main constructor and overloads
old_constructor = """    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, List<String> wishHistory) {
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
        if (wishHistory != null) {
            this.wishHistory = new ArrayList<>(wishHistory);
        }

        if (this.beginnerPulls < 20) {
            this.activeBanner = 3; // Mostrar Principiantes por defecto al abrir si está disponible
        } else {
            this.activeBanner = 1;
        }

        initBannerPreviews();
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, List<String> wishHistory) {
        this(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, 0, 0, 0, 0, wishHistory);
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int pity5Star, int pity4Star, boolean guaranteed) {
        this(primogems, intertwinedFates, acquaintFates, 0, 0, pity5Star, pity4Star, guaranteed, 0, 0, 0, 0, 0, new ArrayList<>());
    }"""

new_constructor = """    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, int coins, double usdBalance, int genesisCrystals, int stellarJade, int welkinDays, List<String> wishHistory) {
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
        if (wishHistory != null) {
            this.wishHistory = new ArrayList<>(wishHistory);
        }

        if (this.beginnerPulls < 20) {
            this.activeBanner = 3;
        } else {
            this.activeBanner = 1;
        }

        initBannerPreviews();
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, List<String> wishHistory) {
        this(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, mora, weaponBannerTarget, weaponBannerFatePoints, characterBannerTarget, 0, 0.0, 0, 0, 0, wishHistory);
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, List<String> wishHistory) {
        this(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, 0, 0, 0, 0, 0, 0.0, 0, 0, 0, wishHistory);
    }

    public GachaWishScreen(int primogems, int intertwinedFates, int acquaintFates, int pity5Star, int pity4Star, boolean guaranteed) {
        this(primogems, intertwinedFates, acquaintFates, 0, 0, pity5Star, pity4Star, guaranteed, 0, 0, 0, 0, 0, 0, 0.0, 0, 0, 0, new ArrayList<>());
    }"""

code = code.replace(old_constructor, new_constructor, 1)

# 4. Update updateAllData
old_update = """    public void updateAllData(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, List<String> wishHistory) {
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
        if (wishHistory != null) {
            this.wishHistory = new ArrayList<>(wishHistory);
        }
    }"""

new_update = """    public void updateAllData(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, int coins, double usdBalance, int genesisCrystals, int stellarJade, int welkinDays, List<String> wishHistory) {
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
        if (wishHistory != null) {
            this.wishHistory = new ArrayList<>(wishHistory);
        }
    }

    public void updateAllData(int primogems, int intertwinedFates, int acquaintFates, int stardust, int starglitter, int pity5Star, int pity4Star, boolean guaranteed, int beginnerPulls, int mora, int weaponBannerTarget, int weaponBannerFatePoints, int characterBannerTarget, List<String> wishHistory) {
        updateAllData(primogems, intertwinedFates, acquaintFates, stardust, starglitter, pity5Star, pity4Star, guaranteed, beginnerPulls, mora, weaponBannerTarget, weaponBannerFatePoints, characterBannerTarget, this.coins, this.usdBalance, this.genesisCrystals, this.stellarJade, this.welkinDays, wishHistory);
    }"""

code = code.replace(old_update, new_update, 1)

with open(path, "w", encoding="utf-8") as f:
    f.write(code)

print("Constructor and fields updated in GachaWishScreen.java!")
