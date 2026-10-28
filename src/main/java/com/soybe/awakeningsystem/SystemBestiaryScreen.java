package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.*;
import com.soybe.awakeningsystem.entity.animal.*;
import com.soybe.awakeningsystem.entity.hypostasis.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class SystemBestiaryScreen extends Screen {

    public record BestiaryEntry(
            String id,
            int categoryColor,
            LivingEntity entity,
            int scale,
            String hp,
            String dmg,
            String armor,
            String speed
    ) {
        public String name() {
            return net.minecraft.client.resources.language.I18n.get("awakening_system.bestiary." + id + ".name");
        }

        public String category() {
            return net.minecraft.client.resources.language.I18n.get("awakening_system.bestiary." + id + ".category");
        }

        public String title() {
            return net.minecraft.client.resources.language.I18n.get("awakening_system.bestiary." + id + ".title");
        }

        public List<String> abilities() {
            List<String> list = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                String k = "awakening_system.bestiary." + id + ".ability." + i;
                if (net.minecraft.client.resources.language.I18n.exists(k)) {
                    list.add(net.minecraft.client.resources.language.I18n.get(k));
                } else {
                    break;
                }
            }
            return list;
        }

        public String tactics() {
            return net.minecraft.client.resources.language.I18n.get("awakening_system.bestiary." + id + ".tactics");
        }

        public String drops() {
            return net.minecraft.client.resources.language.I18n.get("awakening_system.bestiary." + id + ".drops");
        }

        public String lore() {
            return net.minecraft.client.resources.language.I18n.get("awakening_system.bestiary." + id + ".lore");
        }
    }

    private final List<BestiaryEntry> entries = new ArrayList<>();
    private int selectedIndex = 0;
    private int scrollOffset = 0;
    private int dossierScrollOffset = 0;
    private float modelRotationYaw = 0.0F;

    public SystemBestiaryScreen() {
        super(Component.translatable("awakening_system.screen.bestiary.title"));
    }

    @Override
    protected void init() {
        super.init();
        initEntries();
        dossierScrollOffset = 0;
    }

    private void initEntries() {
        entries.clear();
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var ashborn = new ShadowMonarchBossEntity(ModEntities.SHADOW_MONARCH_BOSS.get(), level);
        ashborn.setVariant(1);
        entries.add(new BestiaryEntry(
                "ashborn", 0xFFE4A7FF, ashborn, 22,
                "500 - 8500 HP", "20 - 75 ATK", "16 - 30 DEF", "0.30 SPD"
        ));

        var antares = new ShadowMonarchBossEntity(ModEntities.SHADOW_MONARCH_BOSS.get(), level);
        antares.setVariant(0);
        entries.add(new BestiaryEntry(
                "antares", 0xFFFF5555, antares, 22,
                "500 - 8500 HP", "20 - 75 ATK", "16 - 30 DEF", "0.32 SPD"
        ));

        var igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "igris", 0xFFFF4444, igris, 24,
                "450 - 5500 HP", "18 - 60 ATK", "16 - 28 DEF", "0.34 SPD"
        ));

        var baran = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "baran", 0xFFFFD700, baran, 22,
                "550 - 7500 HP", "20 - 70 ATK", "18 - 30 DEF", "0.36 SPD"
        ));

        var beastman = new BeastmanWarriorEntity(ModEntities.BEASTMAN_WARRIOR.get(), level);
        entries.add(new BestiaryEntry(
                "beastman", 0xFFFFAA00, beastman, 26,
                "260 - 680 HP", "16 - 26 ATK", "12 - 20 DEF", "0.33 SPD"
        ));

        var ant = new AntSoldierEntity(ModEntities.ANT_SOLDIER.get(), level);
        entries.add(new BestiaryEntry(
                "ant_soldier", 0xFF70E8FF, ant, 24,
                "200 - 550 HP", "15 - 25 ATK", "14 - 22 DEF", "0.36 SPD"
        ));

        var beholder = new AbyssalBeholderEntity(ModEntities.ABYSSAL_BEHOLDER.get(), level);
        entries.add(new BestiaryEntry(
                "abyssal_beholder", 0xFF9955FF, beholder, 18,
                "350 - 900 HP", "18 - 30 ATK", "10 - 18 DEF", "0.38 SPD"
        ));

        var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
        entries.add(new BestiaryEntry(
                "blood_orc", 0xFFFF6666, orc, 24,
                "220 - 750 HP", "14 - 24 ATK", "10 - 18 DEF", "0.26 SPD"
        ));

        var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
        entries.add(new BestiaryEntry(
                "shadow_infantry", 0xFF8888FF, infantry, 26,
                "80 - 280 HP", "7 - 16 ATK", "6 - 14 DEF", "0.28 SPD"
        ));

        var necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
        entries.add(new BestiaryEntry(
                "abyssal_necromancer", 0xFF50FA7B, necro, 24,
                "220 - 500 HP", "14 - 22 ATK", "8 - 14 DEF", "0.27 SPD"
        ));

        var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
        entries.add(new BestiaryEntry(
                "ice_elf_assassin", 0xFF70FFFF, assassin, 26,
                "180 - 380 HP", "16 - 25 ATK", "10 - 16 DEF", "0.35 SPD"
        ));

        var dragonFire = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonFire.setVariant(AncientDragonEntity.VARIANT_FIRE);
        entries.add(new BestiaryEntry(
                "dragon_fire", 0xFFFF4500, dragonFire, 8,
                "800 - 3200 HP", "28 - 65 ATK", "20 - 32 DEF", "1.40 SPD"
        ));

        var dragonIce = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonIce.setVariant(AncientDragonEntity.VARIANT_ICE);
        entries.add(new BestiaryEntry(
                "dragon_ice", 0xFF00E5FF, dragonIce, 8,
                "850 - 3400 HP", "26 - 60 ATK", "24 - 36 DEF", "1.35 SPD"
        ));

        var dragonLightning = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonLightning.setVariant(AncientDragonEntity.VARIANT_LIGHTNING);
        entries.add(new BestiaryEntry(
                "dragon_lightning", 0xFFFFD700, dragonLightning, 8,
                "750 - 3000 HP", "30 - 70 ATK", "18 - 28 DEF", "1.60 SPD"
        ));

        var dragonShadow = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonShadow.setVariant(AncientDragonEntity.VARIANT_SHADOW);
        entries.add(new BestiaryEntry(
                "dragon_shadow", 0xFF9933FF, dragonShadow, 8,
                "820 - 3300 HP", "28 - 68 ATK", "22 - 30 DEF", "1.45 SPD"
        ));

        var dragonForest = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonForest.setVariant(AncientDragonEntity.VARIANT_FOREST);
        entries.add(new BestiaryEntry(
                "dragon_forest", 0xFF50FA7B, dragonForest, 8,
                "880 - 3500 HP", "25 - 58 ATK", "26 - 38 DEF", "1.30 SPD"
        ));

        var celestialDragon = new CelestialDragonEntity(ModEntities.CELESTIAL_DRAGON.get(), level);
        entries.add(new BestiaryEntry(
                "dragon_celestial", 0xFFFFE066, celestialDragon, 8,
                "1200 - 5000 HP", "35 - 85 ATK", "28 - 42 DEF", "1.50 SPD"
        ));

        var astralDragon = new AstralDragonEntity(ModEntities.ASTRAL_DRAGON.get(), level);
        entries.add(new BestiaryEntry(
                "dragon_astral", 0xFFBD93F9, astralDragon, 8,
                "1500 - 6000 HP", "40 - 95 ATK", "30 - 45 DEF", "1.55 SPD"
        ));

        var shadowWolf = new ShadowDirewolfEntity(ModEntities.SHADOW_DIREWOLF.get(), level);
        entries.add(new BestiaryEntry(
                "shadow_wolf", 0xFF6272A4, shadowWolf, 22,
                "160 - 420 HP", "14 - 24 ATK", "10 - 18 DEF", "0.42 SPD"
        ));

        var thunderTiger = new WhiteThunderTigerEntity(ModEntities.WHITE_THUNDER_TIGER.get(), level);
        entries.add(new BestiaryEntry(
                "white_tiger", 0xFFF8F8F2, thunderTiger, 20,
                "320 - 850 HP", "18 - 32 ATK", "14 - 22 DEF", "0.40 SPD"
        ));

        var spiritFox = new SpiritFoxEntity(ModEntities.SPIRIT_FOX.get(), level);
        entries.add(new BestiaryEntry(
                "spirit_fox", 0xFFFF79C6, spiritFox, 24,
                "200 - 550 HP", "16 - 28 ATK", "12 - 20 DEF", "0.38 SPD"
        ));

        var cloudCrane = new CloudCraneEntity(ModEntities.CLOUD_CRANE.get(), level);
        entries.add(new BestiaryEntry(
                "cloud_crane", 0xFF8BE9FD, cloudCrane, 22,
                "140 - 380 HP", "12 - 22 ATK", "8 - 16 DEF", "0.45 SPD"
        ));

        var manaPixie = new ManaPixieEntity(ModEntities.MANA_PIXIE.get(), level);
        entries.add(new BestiaryEntry(
                "mana_pixie", 0xFFFFB86C, manaPixie, 32,
                "60 - 180 HP", "8 - 15 ATK", "6 - 12 DEF", "0.40 SPD"
        ));

        var mandrake = new ScreamingMandrakeEntity(ModEntities.SCREAMING_MANDRAKE.get(), level);
        entries.add(new BestiaryEntry(
                "mandrake", 0xFF50FA7B, mandrake, 28,
                "120 - 320 HP", "10 - 20 ATK", "12 - 18 DEF", "0.22 SPD"
        ));

        var isekaiSlime = new IsekaiSlimeEntity(ModEntities.ISEKAI_SLIME.get(), level);
        entries.add(new BestiaryEntry(
                "isekai_slime", 0xFF8BE9FD, isekaiSlime, 26,
                "100 - 350 HP", "8 - 18 ATK", "16 - 25 DEF", "0.25 SPD"
        ));

        var goblinScout = new GoblinScoutEntity(ModEntities.GOBLIN_SCOUT.get(), level);
        entries.add(new BestiaryEntry(
                "goblin_scout", 0xFF50FA7B, goblinScout, 28,
                "90 - 240 HP", "10 - 18 ATK", "6 - 12 DEF", "0.36 SPD"
        ));

        var goblinWarrior = new GoblinWarriorEntity(ModEntities.GOBLIN_WARRIOR.get(), level);
        entries.add(new BestiaryEntry(
                "goblin_warrior", 0xFFFFB86C, goblinWarrior, 26,
                "140 - 380 HP", "14 - 24 ATK", "12 - 20 DEF", "0.30 SPD"
        ));

        var goblinShaman = new GoblinShamanEntity(ModEntities.GOBLIN_SHAMAN.get(), level);
        entries.add(new BestiaryEntry(
                "goblin_shaman", 0xFFBD93F9, goblinShaman, 26,
                "160 - 420 HP", "16 - 26 ATK", "8 - 14 DEF", "0.28 SPD"
        ));

        var jiangshi = new JiangshiEntity(ModEntities.JIANGSHI.get(), level);
        entries.add(new BestiaryEntry(
                "jiangshi", 0xFF8BE9FD, jiangshi, 24,
                "280 - 750 HP", "18 - 30 ATK", "18 - 28 DEF", "0.32 SPD"
        ));

        var demonicCultivator = new DemonicCultivatorEntity(ModEntities.DEMONIC_CULTIVATOR.get(), level);
        entries.add(new BestiaryEntry(
                "demonic_cultivator", 0xFFFF5555, demonicCultivator, 24,
                "350 - 950 HP", "22 - 38 ATK", "16 - 26 DEF", "0.36 SPD"
        ));

        var mountHua = new MountHuaDiscipleEntity(ModEntities.MOUNT_HUA_DISCIPLE.get(), level);
        entries.add(new BestiaryEntry(
                "mount_hua_disciple", 0xFFFF79C6, mountHua, 24,
                "300 - 800 HP", "20 - 34 ATK", "14 - 24 DEF", "0.38 SPD"
        ));

        var bloodDisciple = new BloodSectDiscipleEntity(ModEntities.BLOOD_SECT_DISCIPLE.get(), level);
        entries.add(new BestiaryEntry(
                "blood_disciple", 0xFFFF4444, bloodDisciple, 24,
                "260 - 720 HP", "18 - 30 ATK", "12 - 20 DEF", "0.35 SPD"
        ));

        var jadeBeast = new JadeArmoredBeastEntity(ModEntities.JADE_ARMORED_BEAST.get(), level);
        entries.add(new BestiaryEntry(
                "jade_beast", 0xFF50FA7B, jadeBeast, 20,
                "500 - 1500 HP", "24 - 45 ATK", "30 - 48 DEF", "0.22 SPD"
        ));

        var bloodPatriarch = new BloodPatriarchBossEntity(ModEntities.BLOOD_PATRIARCH_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "blood_patriarch", 0xFFFF0033, bloodPatriarch, 22,
                "1800 - 8000 HP", "35 - 80 ATK", "24 - 38 DEF", "0.38 SPD"
        ));

        var yggdrasilTreant = new YggdrasilTreantBossEntity(ModEntities.YGGDRASIL_TREANT_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "yggdrasil_treant", 0xFF50FA7B, yggdrasilTreant, 18,
                "2000 - 9000 HP", "30 - 75 ATK", "32 - 46 DEF", "0.20 SPD"
        ));

        var jadeGuardian = new JadeGuardianBossEntity(ModEntities.JADE_GUARDIAN_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "jade_guardian", 0xFF50FA7B, jadeGuardian, 18,
                "2200 - 9500 HP", "38 - 85 ATK", "35 - 50 DEF", "0.24 SPD"
        ));

        var elementalKing = new ElementalDemonKingBossEntity(ModEntities.ELEMENTAL_DEMON_KING.get(), level);
        entries.add(new BestiaryEntry(
                "elemental_king", 0xFFFF5555, elementalKing, 20,
                "2500 - 10000 HP", "40 - 90 ATK", "30 - 45 DEF", "0.34 SPD"
        ));

        var elementalLord = new ElementalLordBossEntity(ModEntities.ELEMENTAL_LORD.get(), level);
        entries.add(new BestiaryEntry(
                "elemental_lord", 0xFF8BE9FD, elementalLord, 22,
                "600 - 1800 HP", "25 - 50 ATK", "20 - 32 DEF", "0.32 SPD"
        ));

        var dokkaebi = new DokkaebiEntity(ModEntities.DOKKAEBI.get(), level);
        entries.add(new BestiaryEntry(
                "dokkaebi", 0xFFFFB86C, dokkaebi, 26,
                "500 - 1200 HP", "12 - 25 ATK", "15 - 25 DEF", "0.35 SPD"
        ));

        var manaCow = new ManaCowEntity(ModEntities.MANA_COW.get(), level);
        entries.add(new BestiaryEntry(
                "mana_cow", 0xFF8BE9FD, manaCow, 24,
                "40 - 100 HP", "0 ATK", "6 - 12 DEF", "0.20 SPD"
        ));

        var hilichurlShooter = new HilichurlShooterEntity(ModEntities.HILICHURL_SHOOTER.get(), level);
        entries.add(new BestiaryEntry(
                "hilichurl_shooter", 0xFF50FA7B, hilichurlShooter, 24,
                "80 - 180 HP", "8 - 16 ATK", "4 - 10 DEF", "0.26 SPD"
        ));

        var hilichurlBerserker = new HilichurlBerserkerEntity(ModEntities.HILICHURL_BERSERKER.get(), level);
        entries.add(new BestiaryEntry(
                "hilichurl_berserker", 0xFFFF5555, hilichurlBerserker, 24,
                "120 - 280 HP", "14 - 24 ATK", "8 - 14 DEF", "0.34 SPD"
        ));

        var ruinHunter = new RuinHunterEntity(ModEntities.RUIN_HUNTER.get(), level);
        entries.add(new BestiaryEntry(
                "ruin_hunter", 0xFFFFB86C, ruinHunter, 18,
                "650 - 2200 HP", "28 - 55 ATK", "24 - 36 DEF", "0.38 SPD"
        ));

        var lawachurl = new StonehideLawachurlEntity(ModEntities.STONEHIDE_LAWACHURL.get(), level);
        entries.add(new BestiaryEntry(
                "armored_lawachurl", 0xFFFFD700, lawachurl, 16,
                "1200 - 4500 HP", "35 - 75 ATK", "35 - 50 DEF", "0.30 SPD"
        ));

        var anemoHypostasis = new AnemoHypostasisBossEntity(ModEntities.ANEMO_HYPOSTASIS.get(), level);
        entries.add(new BestiaryEntry(
                "anemo_hypostasis", 0xFF50FA7B, anemoHypostasis, 16,
                "1500 - 6000 HP", "30 - 65 ATK", "25 - 40 DEF", "0.28 SPD"
        ));

        var electroHypostasis = new ElectroHypostasisBossEntity(ModEntities.ELECTRO_HYPOSTASIS.get(), level);
        entries.add(new BestiaryEntry(
                "electro_hypostasis", 0xFFBD93F9, electroHypostasis, 16,
                "1600 - 6400 HP", "35 - 70 ATK", "28 - 42 DEF", "0.30 SPD"
        ));

        var geoHypostasis = new GeoHypostasisBossEntity(ModEntities.GEO_HYPOSTASIS.get(), level);
        entries.add(new BestiaryEntry(
                "geo_hypostasis", 0xFFFFB86C, geoHypostasis, 16,
                "1800 - 7200 HP", "32 - 68 ATK", "35 - 55 DEF", "0.25 SPD"
        ));

        var cryoHypostasis = new CryoHypostasisBossEntity(ModEntities.CRYO_HYPOSTASIS.get(), level);
        entries.add(new BestiaryEntry(
                "cryo_hypostasis", 0xFF8BE9FD, cryoHypostasis, 16,
                "1700 - 6800 HP", "34 - 72 ATK", "30 - 45 DEF", "0.26 SPD"
        ));

        var pyroHypostasis = new PyroHypostasisBossEntity(ModEntities.PYRO_HYPOSTASIS.get(), level);
        entries.add(new BestiaryEntry(
                "pyro_hypostasis", 0xFFFF5555, pyroHypostasis, 16,
                "1600 - 6500 HP", "38 - 78 ATK", "24 - 38 DEF", "0.32 SPD"
        ));

        var hydroHypostasis = new HydroHypostasisBossEntity(ModEntities.HYDRO_HYPOSTASIS.get(), level);
        entries.add(new BestiaryEntry(
                "hydro_hypostasis", 0xFF4CD3FF, hydroHypostasis, 16,
                "1550 - 6200 HP", "30 - 66 ATK", "26 - 40 DEF", "0.27 SPD"
        ));

        var dendroHypostasis = new DendroHypostasisBossEntity(ModEntities.DENDRO_HYPOSTASIS.get(), level);
        entries.add(new BestiaryEntry(
                "dendro_hypostasis", 0xFF50FA7B, dendroHypostasis, 16,
                "1650 - 6600 HP", "32 - 70 ATK", "28 - 44 DEF", "0.28 SPD"
        ));

        var raiden = new RaidenBossEntity(ModEntities.RAIDEN_SHOGUN_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "raiden_shogun", 0xFFBD93F9, raiden, 22,
                "3000 - 12000 HP", "45 - 95 ATK", "30 - 48 DEF", "0.42 SPD"
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 500.0F;
        float scaleY = (float) this.height / 290.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.35F, scale));
    }

    public double transformMouseX(double mouseX) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseX;
        double cx = this.width / 2.0;
        return (mouseX - cx) / scale + cx;
    }

    public double transformMouseY(double mouseY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseY;
        double cy = this.height / 2.0;
        return (mouseY - cy) / scale + cy;
    }

    private void enableScissorScaled(GuiGraphics g, int minX, int minY, int maxX, int maxY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) {
            g.enableScissor(minX, minY, maxX, maxY);
        } else {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            int sMinX = (int) Math.floor((minX - cx) * scale + cx);
            int sMinY = (int) Math.floor((minY - cy) * scale + cy);
            int sMaxX = (int) Math.ceil((maxX - cx) * scale + cx);
            int sMaxY = (int) Math.ceil((maxY - cy) * scale + cy);
            g.enableScissor(sMinX, sMinY, sMaxX, sMaxY);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x9A050D14, 0xBA0A1620);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            g.pose().translate(cx, cy, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cx, -cy, 0.0F);
        }

        int panelW = 480;
        int panelH = 270;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        g.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF1D556C);
        g.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF39B9D5);
        g.fill(left, top, left + panelW, top + panelH, 0xFA0B161F);

        g.fill(left, top, left + panelW, top + 26, 0xFF122430);
        g.fill(left, top + 25, left + panelW, top + 26, 0xFF39B9D5);

        int cornerLen = 12;
        g.fill(left, top, left + cornerLen, top + 2, 0xFF70E8FF);
        g.fill(left, top, left + 2, top + cornerLen, 0xFF70E8FF);
        g.fill(left + panelW - cornerLen, top, left + panelW, top + 2, 0xFF70E8FF);
        g.fill(left + panelW - 2, top, left + panelW, top + cornerLen, 0xFF70E8FF);

        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.header"), left + 14, top + 9, 0xFF70E8FF, false);

        boolean exitHovered = mX >= left + panelW - 32 && mX <= left + panelW - 10 && mY >= top + 6 && mY <= top + 20;
        g.fill(left + panelW - 32, top + 6, left + panelW - 10, top + 20, exitHovered ? 0xFF992222 : 0xFF203545);
        g.fill(left + panelW - 31, top + 7, left + panelW - 11, top + 19, exitHovered ? 0xFFCC3333 : 0xFF14222D);
        g.drawCenteredString(font, "§f✖", left + panelW - 21, top + 8, exitHovered ? 0xFFFFFFFF : 0xFF8AAAB8);

        int listX = left + 8;
        int listY = top + 32;
        int listW = 135;
        int listH = panelH - 40;

        renderMonsterList(g, listX, listY, listW, listH, mX, mY);

        if (!entries.isEmpty() && selectedIndex >= 0 && selectedIndex < entries.size()) {
            BestiaryEntry cur = entries.get(selectedIndex);

            int centerBoxX = listX + listW + 8;
            int centerBoxY = top + 32;
            int centerBoxW = 125;
            int centerBoxH = panelH - 40;
            renderHoloPedestal(g, centerBoxX, centerBoxY, centerBoxW, centerBoxH, cur, mX, mY);

            int dossierX = centerBoxX + centerBoxW + 8;
            int dossierY = top + 32;
            int dossierW = left + panelW - dossierX - 8;
            int dossierH = panelH - 40;
            renderDossier(g, dossierX, dossierY, dossierW, dossierH, cur);
        }

        g.pose().popPose();
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderMonsterList(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.fill(x, y, x + w, y + h, 0xFF0D1822);
        g.fill(x + w, y, x + w + 1, y + h, 0xFF1F485B);

        int itemH = 26;
        int maxVisible = h / itemH;
        int maxScroll = Math.max(0, entries.size() - maxVisible);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int i = 0; i < maxVisible && (i + scrollOffset) < entries.size(); i++) {
            int idx = i + scrollOffset;
            BestiaryEntry entry = entries.get(idx);
            int ey = y + 4 + i * itemH;
            boolean isSel = (idx == selectedIndex);
            boolean hov = mouseX >= x + 2 && mouseX <= x + w - 2 && mouseY >= ey && mouseY <= ey + itemH - 2;

            int bgCol = isSel ? 0xFF1E465D : (hov ? 0xFF173445 : 0xFF122430);
            int borderCol = isSel ? 0xFF70E8FF : (hov ? 0xFF39B9D5 : 0xFF1D3E4F);

            g.fill(x + 2, ey, x + w - 2, ey + itemH - 2, borderCol);
            g.fill(x + 3, ey + 1, x + w - 3, ey + itemH - 3, bgCol);

            g.fill(x + 5, ey + 3, x + 7, ey + itemH - 5, entry.categoryColor);

            g.drawString(font, entry.name(), x + 11, ey + 3, isSel ? 0xFFFFFFFF : 0xFFDDECF2, false);
            g.drawString(font, "§8[" + entry.category() + "] §7" + entry.hp.split(" ")[0] + " HP", x + 11, ey + 13, 0xFF8AAAB8, false);
        }

        if (entries.size() > maxVisible) {
            int barX = x + w - 3;
            int trackH = h - 8;
            int thumbH = Math.max(16, trackH * maxVisible / entries.size());
            int thumbY = y + 4 + (trackH - thumbH) * scrollOffset / maxScroll;
            g.fill(barX, y + 4, barX + 2, y + 4 + trackH, 0xFF142735);
            g.fill(barX, thumbY, barX + 2, thumbY + thumbH, 0xFF39B9D5);
        }
    }

    private void renderHoloPedestal(GuiGraphics g, int x, int y, int w, int h, BestiaryEntry cur, int mouseX, int mouseY) {
        g.fill(x, y, x + w, y + h, 0xFF0B141C);
        g.fill(x, y, x + w, y + 1, 0xFF276A82);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF276A82);
        g.fill(x, y, x + 1, y + h, 0xFF276A82);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF276A82);

        int gridSpacing = 16;
        for (int gy = y + 10; gy < y + h - 20; gy += gridSpacing) {
            g.fill(x + 8, gy, x + w - 8, gy + 1, 0x1870E8FF);
        }

        int pedCenterX = x + w / 2;
        int pedCenterY = y + h - 26;
        g.fill(pedCenterX - 38, pedCenterY, pedCenterX + 38, pedCenterY + 4, 0x501D556C);
        g.fill(pedCenterX - 30, pedCenterY + 1, pedCenterX + 30, pedCenterY + 3, 0x8039B9D5);
        g.fill(pedCenterX - 18, pedCenterY + 1, pedCenterX + 18, pedCenterY + 3, 0xC070E8FF);

        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bestiary.projection"), pedCenterX, y + 6, 0xFF5D8496);

        if (cur.entity != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g, x + 4, y + 14, x + w - 4, y + h - 20,
                    cur.scale, 0.0625F, (float) mouseX, (float) mouseY, cur.entity
            );
        }

        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bestiary.scale", cur.scale), pedCenterX, y + h - 14, 0xFF8AAAB8);
    }

    private int calculateDossierContentHeight(BestiaryEntry cur, int maxW) {
        int h = 8;
        h += 11;
        h += font.split(Component.literal("§b" + cur.title()), maxW).size() * 10;
        h += 3;
        h += 36;
        h += 10;
        for (String abil : cur.abilities()) {
            h += font.split(Component.literal(abil), maxW).size() * 10;
        }
        h += 12;
        h += font.split(Component.literal("• " + cur.tactics()), maxW).size() * 10;
        h += 12;
        h += font.split(Component.literal("• " + cur.drops()), maxW).size() * 10;
        h += 12;
        h += font.split(Component.literal("§8\"" + cur.lore() + "\""), maxW).size() * 10;
        h += 16;
        return h;
    }

    private void renderDossier(GuiGraphics g, int x, int y, int w, int h, BestiaryEntry cur) {
        g.fill(x, y, x + w, y + h, 0xFF0D1922);
        g.fill(x, y, x + w, y + 1, 0xFF1F485B);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF1F485B);
        g.fill(x, y, x + 1, y + h, 0xFF1F485B);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF1F485B);

        int maxW = w - 20;
        int contentH = calculateDossierContentHeight(cur, maxW);
        int maxScroll = Math.max(0, contentH - (h - 16));
        dossierScrollOffset = Math.max(0, Math.min(dossierScrollOffset, maxScroll));

        enableScissorScaled(g, x + 2, y + 2, x + w - 2, y + h - 2);

        int cy = y + 8 - dossierScrollOffset;

        g.drawString(font, "§6§l" + cur.name().toUpperCase(), x + 8, cy, 0xFFFFD700, false);
        cy += 11;
        cy = drawWrapped(g, "§b" + cur.title(), x + 8, cy, maxW, 0xFF70E8FF);
        cy += 3;

        g.fill(x + 8, cy, x + w - 12, cy + 32, 0xFF11232E);
        g.fill(x + 8, cy, x + w - 12, cy + 1, 0xFF27556A);

        g.drawString(font, "§c❤ " + cur.hp(), x + 12, cy + 4, 0xFFFF7666, false);
        g.drawString(font, "§6⚔ " + cur.dmg(), x + 98, cy + 4, 0xFFFFB86C, false);
        g.drawString(font, "§9🛡 " + cur.armor(), x + 12, cy + 17, 0xFF70E8FF, false);
        g.drawString(font, "§a⚡ " + cur.speed(), x + 98, cy + 17, 0xFF50FA7B, false);
        cy += 36;

        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.section.abilities"), x + 8, cy, 0xFFFFD866, false);
        cy += 10;
        for (String abil : cur.abilities()) {
            cy = drawWrapped(g, abil, x + 8, cy, maxW, 0xFFDDECF2);
        }

        cy += 2;
        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.section.strategy"), x + 8, cy, 0xFF50FA7B, false);
        cy += 10;
        cy = drawWrapped(g, "• " + cur.tactics(), x + 8, cy, maxW, 0xFFDDECF2);

        cy += 2;
        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.section.drops"), x + 8, cy, 0xFFFFB86C, false);
        cy += 10;
        cy = drawWrapped(g, "• " + cur.drops(), x + 8, cy, maxW, 0xFFDDECF2);

        cy += 2;
        cy = drawWrapped(g, "§8\"" + cur.lore() + "\"", x + 8, cy, maxW, 0xFF8AAAB8);

        g.disableScissor();

        if (maxScroll > 0) {
            int barX = x + w - 4;
            int trackY = y + 4;
            int trackH = h - 8;
            int thumbH = Math.max(16, trackH * (h - 16) / contentH);
            int thumbY = trackY + (trackH - thumbH) * dossierScrollOffset / maxScroll;
            g.fill(barX, trackY, barX + 2, trackY + trackH, 0xFF142735);
            g.fill(barX, thumbY, barX + 2, thumbY + thumbH, 0xFF39B9D5);
        }
    }

    private int drawWrapped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        for (var line : font.split(Component.literal(text), maxWidth)) {
            g.drawString(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int panelW = 480;
        int panelH = 270;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        int listX = left + 8;
        int listY = top + 32;
        int listW = 135;
        int listH = panelH - 40;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (scrollY < 0) {
                int itemH = 26;
                int maxVisible = listH / itemH;
                int maxScroll = Math.max(0, entries.size() - maxVisible);
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            }
            return true;
        }

        int centerBoxX = listX + listW + 8;
        int centerBoxW = 125;
        int dossierX = centerBoxX + centerBoxW + 8;
        int dossierY = top + 32;
        int dossierW = left + panelW - dossierX - 8;
        int dossierH = panelH - 40;

        if (mouseX >= dossierX && mouseX <= dossierX + dossierW && mouseY >= dossierY && mouseY <= dossierY + dossierH) {
            if (!entries.isEmpty() && selectedIndex >= 0 && selectedIndex < entries.size()) {
                int maxW = dossierW - 20;
                int contentH = calculateDossierContentHeight(entries.get(selectedIndex), maxW);
                int maxScroll = Math.max(0, contentH - (dossierH - 16));
                if (maxScroll > 0) {
                    if (scrollY > 0) {
                        dossierScrollOffset = Math.max(0, dossierScrollOffset - 22);
                    } else if (scrollY < 0) {
                        dossierScrollOffset = Math.min(maxScroll, dossierScrollOffset + 22);
                    }
                    return true;
                }
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);

        int panelW = 480;
        int panelH = 270;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        if (button == 0) {
            if (mouseX >= left + panelW - 32 && mouseX <= left + panelW - 10 && mouseY >= top + 6 && mouseY <= top + 20) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
                }
                onClose();
                return true;
            }

            int listX = left + 8;
            int listY = top + 32;
            int listW = 135;
            int listH = panelH - 40;
            int itemH = 26;
            int maxVisible = listH / itemH;

            for (int i = 0; i < maxVisible && (i + scrollOffset) < entries.size(); i++) {
                int idx = i + scrollOffset;
                int ey = listY + 4 + i * itemH;
                if (mouseX >= listX + 2 && mouseX <= listX + listW - 2 && mouseY >= ey && mouseY <= ey + itemH - 2) {
                    if (selectedIndex != idx) {
                        selectedIndex = idx;
                        dossierScrollOffset = 0;
                    }
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.4F);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
