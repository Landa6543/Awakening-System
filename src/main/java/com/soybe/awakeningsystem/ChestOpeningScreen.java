package com.soybe.awakeningsystem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class ChestOpeningScreen extends Screen {
    private final Screen parentScreen;
    private final String boxId;
    private final String nameKey;
    private final int tier;
    private final int cost;

    private int ticks = 0;
    private boolean opened = false;
    private float flashAlpha = 0.0F;
    private int lastClickCard = -1;

    private String rewardSummary = "";
    private int coinsWon = 0;
    private final List<ItemStack> rewardStacks = new ArrayList<>();
    private final List<ItemStack> reelItems = new ArrayList<>();
    private final List<Confetti> confettiList = new ArrayList<>();

    private static final class Confetti {
        float x, y;
        float vx, vy;
        float rot, vrot;
        float sizeW, sizeH;
        int color;
        float flutterPhase;
        float flutterSpeed;

        Confetti(float x, float y, float vx, float vy, float sizeW, float sizeH, int color, float vrot, float flutterSpeed) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.sizeW = sizeW;
            this.sizeH = sizeH;
            this.color = color;
            this.vrot = vrot;
            this.flutterSpeed = flutterSpeed;
            this.flutterPhase = (float) (Math.random() * Math.PI * 2);
        }

        void update() {
            vy += 0.14F;
            if (vy > 2.8F) vy = 2.8F;
            vx *= 0.98F;
            x += vx + (float) Math.sin(flutterPhase) * 0.9F;
            y += vy;
            rot += vrot;
            flutterPhase += flutterSpeed;
        }

        void render(GuiGraphics g) {
            float scaleW = Math.max(1.0F, Math.abs((float) Math.cos(rot)) * sizeW);
            int ix = (int) x;
            int iy = (int) y;
            int iw = (int) scaleW;
            int ih = (int) sizeH;
            g.fill(ix, iy, ix + iw, iy + ih, color);
            if (iw > 2 && ih > 2) {
                g.fill(ix + 1, iy + 1, ix + iw - 1, iy + ih - 1, (color & 0x00FFFFFF) | 0x88FFFFFF);
            }
        }
    }

    private void spawnConfetti(int count) {
        confettiList.clear();
        int cx = width / 2;
        int cy = height / 2 - 20;
        int[] palette = {
            0xFFFFD700, 0xFFFFEA75, 0xFF00E5FF, 0xFF70E8FF,
            0xFFFF4081, 0xFFE040FB, 0xFF50FA7B, 0xFF2ECC71,
            0xFFBD93F9, 0xFFFFFFFF, 0xFFFF9100
        };
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < count; i++) {
            float angle = (float) (rnd.nextFloat() * Math.PI * 2);
            float speed = 2.5F + rnd.nextFloat() * 5.5F;
            float vx = (float) Math.cos(angle) * speed;
            float vy = -3.0F - rnd.nextFloat() * 6.5F;
            float w = 4.0F + rnd.nextFloat() * 4.0F;
            float h = 2.5F + rnd.nextFloat() * 3.0F;
            int col = palette[rnd.nextInt(palette.length)];
            float vrot = 0.08F + rnd.nextFloat() * 0.16F;
            float flutter = 0.1F + rnd.nextFloat() * 0.15F;
            confettiList.add(new Confetti(cx + (rnd.nextFloat() - 0.5F) * 30, cy + (rnd.nextFloat() - 0.5F) * 20, vx, vy, w, h, col, vrot, flutter));
        }
    }

    public ChestOpeningScreen(Screen parentScreen, String boxId, String nameKey, int tier, int cost) {
        super(Component.translatable(nameKey));
        this.parentScreen = parentScreen;
        this.boxId = boxId;
        this.nameKey = nameKey;
        this.tier = tier;
        this.cost = cost;

        initReelCandidates();
    }

    private void initReelCandidates() {
        if ("system_roulette".equals(this.boxId)) {
            reelItems.clear();
            reelItems.add(new ItemStack(SystemItems.CELESTIAL_INGOT.get()));
            reelItems.add(new ItemStack(SystemItems.PRIMORDIAL_INGOT.get()));
            reelItems.add(new ItemStack(SystemItems.MITHRIL_INGOT.get()));
            reelItems.add(new ItemStack(SystemItems.SOUL_ALLOY_INGOT.get()));
            reelItems.add(new ItemStack(SystemItems.SHADOW_APPLE.get()));
            reelItems.add(new ItemStack(SystemItems.ABYSSAL_INGOT.get()));
            reelItems.add(new ItemStack(SystemItems.SPIRIT_QI_CARROT.get()));
            reelItems.add(new ItemStack(SystemItems.REFINED_WHITE_IRON_INGOT.get()));
            return;
        }

        Item[] pool;
        if (this.tier >= 6) {
            pool = new Item[]{
                    SystemItems.CELESTIAL_INGOT.get(), SystemItems.PRIMORDIAL_INGOT.get(),
                    SystemItems.ENCHANTED_CELESTIAL_APPLE.get(), SystemItems.PRIMORDIAL_APPLE.get(),
                    SystemItems.SHADOW_APPLE.get(), SystemItems.CELESTIAL_DRAGON_PEARL.get(),
                    SystemItems.PRIMORDIAL_REBIRTH_PILL.get(), SystemItems.PRIMORDIAL_JADE_CUTTER.get(),
                    SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()
            };
        } else if (this.tier == 5) {
            pool = new Item[]{
                    SystemItems.MITHRIL_INGOT.get(), SystemItems.ADAMANTITE_INGOT.get(), SystemItems.ASTRAL_INGOT.get(),
                    SystemItems.ENCHANTED_MITHRIL_APPLE.get(), SystemItems.ADAMANTITE_APPLE.get(),
                    SystemItems.SPIRIT_JADE_APPLE.get(), SystemItems.SHADOW_APPLE.get(),
                    SystemItems.REALM_BREAKTHROUGH_PILL.get(), SystemItems.ADAMANTITE_WAR_HAMMER.get()
            };
        } else if (this.tier == 4) {
            pool = new Item[]{
                    Items.NETHERITE_BLOCK, Items.NETHERITE_INGOT, SystemItems.SOUL_ALLOY_INGOT.get(),
                    SystemItems.ASTRALITE_INGOT.get(), SystemItems.SHADOW_APPLE.get(),
                    Items.ENCHANTED_GOLDEN_APPLE, Items.TOTEM_OF_UNDYING, Items.NETHER_STAR,
                    Items.DIAMOND_BLOCK
            };
        } else {
            pool = new Item[]{
                    Items.STICK, Items.POISONOUS_POTATO, Items.STRING, Items.COBWEB, Items.DIRT, Items.ROTTEN_FLESH, Items.WOODEN_HOE, Items.BONE,
                    Items.IRON_INGOT, Items.COAL, Items.GOLD_INGOT, Items.COOKED_BEEF, Items.ARROW, Items.SHIELD, Items.GOLDEN_APPLE,
                    Items.DIAMOND, Items.ENDER_PEARL, Items.NETHERITE_SCRAP, Items.EXPERIENCE_BOTTLE, Items.DIAMOND_BLOCK,
                    Items.NETHERITE_INGOT, Items.TOTEM_OF_UNDYING, Items.ENCHANTED_GOLDEN_APPLE, Items.HEAVY_CORE, Items.MACE
            };
        }
        for (int i = 0; i < 35; i++) {
            Item item = pool[i % pool.length];
            reelItems.add(new ItemStack(item, 1));
        }
    }

    @Override
    protected void init() {
        super.init();
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(SoundEvents.CHEST_LOCKED, 0.9F, 1.2F);
        }
    }

    public void receiveReward(ChestRewardPayload payload) {
        this.rewardSummary = payload.rewardSummary();
        this.coinsWon = payload.coinsWon();
        this.rewardStacks.clear();
        for (int i = 0; i < payload.itemIds().size(); i++) {
            String itemId = payload.itemIds().get(i);
            int count = payload.itemCounts().size() > i ? Math.max(1, payload.itemCounts().get(i)) : 1;
            ResourceLocation rl = ResourceLocation.parse(itemId);
            if (BuiltInRegistries.ITEM.containsKey(rl)) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(rl), count);
                if (!stack.isEmpty()) {
                    rewardStacks.add(stack);
                }
            }
        }

        if (!rewardStacks.isEmpty() && "system_roulette".equals(this.boxId)) {
            ItemStack win = rewardStacks.get(0).copy();
            int foundIdx = -1;
            for (int i = 0; i < reelItems.size(); i++) {
                if (reelItems.get(i).is(win.getItem())) {
                    foundIdx = i;
                    break;
                }
            }
            if (foundIdx > 0) {
                ItemStack temp = reelItems.get(0);
                reelItems.set(0, win);
                reelItems.set(foundIdx, temp);
            } else {
                reelItems.set(0, win);
            }
        } else if (!rewardStacks.isEmpty() && reelItems.size() > 20) {
            reelItems.set(20, rewardStacks.get(0).copy());
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticks++;

        if (flashAlpha > 0.0F) {
            flashAlpha = Math.max(0.0F, flashAlpha - 0.07F);
        }

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean isRoulette = "system_roulette".equals(boxId);
        if (!opened && ticks <= 56) {
            if (isRoulette) {
                if (ticks == 1) {
                    player.playSound(ModSounds.ROULETTE_SPIN.value(), 0.9F, 1.0F);
                }
                float progress = Math.min(1.0F, ticks / 56.0F);
                float ease = 1.0F - (float) Math.pow(1.0F - progress, 3.2);
                float spinAngle = (1.0F - ease) * (5.0F * 360.0F);
                int sectorIndex = (int) (spinAngle / 45.0F);
                if (sectorIndex != lastClickCard) {
                    lastClickCard = sectorIndex;
                    float clickPitch = 1.5F + (progress * 0.5F);
                    float bellPitch = 1.2F + ((sectorIndex % 8) * 0.1F);
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, clickPitch);
                    player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 0.5F, bellPitch);
                    player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 0.35F, 1.4F + (progress * 0.4F));
                }
            } else {
                if (ticks % 8 == 0) {
                    player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 0.8F + (ticks / 46.0F) * 0.6F);
                }
                if (ticks == 35) {
                    player.playSound(SoundEvents.ENDER_CHEST_OPEN, 0.8F, 1.3F);
                }
            }
        }

        if (!confettiList.isEmpty()) {
            for (Confetti c : confettiList) {
                c.update();
            }
        }

        if (isRoulette && opened && ticks > 56 && ticks <= 70 && ticks % 2 == 0) {
            int step = (ticks - 56) / 2;
            float bellPitch = Math.min(2.0F, 1.2F + (step * 0.12F));
            float coinPitch = 1.1F + (step * 0.08F);
            player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 0.65F, bellPitch);
            player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 0.45F, bellPitch);
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.85F, coinPitch);
            player.playSound(SoundEvents.CHAIN_STEP, 0.5F, 1.6F);
        }

        int targetOpenTick = isRoulette ? 56 : 46;
        if (ticks == targetOpenTick && !opened) {
            opened = true;
            flashAlpha = 0.85F;

            if (isRoulette) {
                if (coinsWon >= 10000) {
                    player.playSound(ModSounds.CASINO_JACKPOT.value(), 1.0F, 1.0F);
                    player.playSound(ModSounds.ROULETTE_WIN.value(), 1.0F, 1.0F);
                    player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                    spawnConfetti(120);
                } else {
                    player.playSound(ModSounds.ROULETTE_WIN.value(), 1.0F, 1.0F);
                    player.playSound(SoundEvents.PLAYER_LEVELUP, 0.9F, 1.2F);
                    player.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 0.8F, 1.1F);
                    spawnConfetti(70);
                }
            } else if (tier >= 4) {
                player.playSound(SoundEvents.TOTEM_USE, 0.7F, 1.0F);
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.9F, 1.0F);
                spawnConfetti(tier >= 6 ? 110 : (tier == 5 ? 80 : 55));
            } else if (tier == 3) {
                player.playSound(SoundEvents.PLAYER_LEVELUP, 0.9F, 0.95F);
                player.playSound(SoundEvents.ENDER_CHEST_OPEN, 0.8F, 1.2F);
                spawnConfetti(35);
            } else if (tier == 2) {
                player.playSound(SoundEvents.PLAYER_LEVELUP, 0.9F, 1.15F);
            } else {
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.9F, 1.3F);
            }
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xF008121D);

        int cx = width / 2;
        int cy = height / 2;
        float animTime = ticks + partialTick;

        int rarityColor = switch (tier) {
            case 6 -> 0xFFFFD700;
            case 5 -> 0xFF00FFB0;
            case 4 -> 0xFFFFAA00;
            case 3 -> 0xFFD055FF;
            case 2 -> 0xFF55AAFF;
            default -> "lucky_box".equals(boxId) ? 0xFFFFD866 : 0xFFE0E0E0;
        };

        renderAuraRays(g, cx, opened ? cy - 35 : cy - 10, animTime, rarityColor);

        boolean isRoulette = "system_roulette".equals(boxId);
        SystemData sData = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        boolean sys20 = sData != null && (sData.systemUpdated() || sData.isSystemUpdating());

        if (isRoulette) {
            Component mainTitle = Component.translatable(opened ? "awakening_system.chest.roulette.awarded" : "awakening_system.chest.roulette.title").withStyle(ChatFormatting.BOLD);
            g.drawCenteredString(font, mainTitle, cx, cy - 106, 0xFFFFD866);

            if (!opened) {
                Component subTitle = Component.translatable("awakening_system.chest.opening");
                float pulse = (float) ((Math.sin(animTime * 0.4F) + 1.0F) * 0.5F);
                int subAlpha = (int) (170 + pulse * 85);
                g.drawCenteredString(font, subTitle, cx, cy - 93, (subAlpha << 24) | (sys20 ? 0xFFD700 : 0x70E8FF));

                renderRouletteReel(g, cx, cy - 14, animTime, rarityColor, false);

                Component calib = Component.translatable("awakening_system.chest.roulette.calibrating");
                g.drawCenteredString(font, calib, cx, cy + 50, sys20 ? 0x90FFD700 : 0x9070E8FF);
            } else {
                renderRouletteReel(g, cx, cy - 14, 56.0F, rarityColor, true);

                if (coinsWon >= 10000) {
                    float jPulse = (float) ((Math.sin(animTime * 0.8F) + 1.0F) * 0.5F);
                    int jCol = ((int) (200 + jPulse * 55) << 24) | 0xFFD700;
                    Component jackpotComp = Component.translatable("awakening_system.chest.jackpot_banner").withStyle(ChatFormatting.BOLD);
                    g.drawCenteredString(font, jackpotComp, cx, cy + 34, jCol);
                }

                String prizeName = !rewardStacks.isEmpty() ? rewardStacks.get(0).getHoverName().getString() : (!rewardSummary.isEmpty() ? rewardSummary : Component.translatable("awakening_system.chest.default_prize").getString());
                Component prizeComp = Component.translatable("awakening_system.chest.prize_banner", prizeName);
                g.drawCenteredString(font, prizeComp, cx, cy + (coinsWon >= 10000 ? 46 : 48), 0xFFFFD700);

                if (coinsWon > 0) {
                    Component coinComp = Component.translatable("awakening_system.chest.coins_won", coinsWon);
                    g.drawCenteredString(font, coinComp, cx, cy + (coinsWon >= 10000 ? 58 : 60), coinsWon >= 10000 ? 0xFFFFEA75 : 0xFFFFD866);
                }

                int btnW = 140;
                int btnH = 22;
                int btnX = cx - btnW / 2;
                int btnY = cy + 74;
                boolean btnHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? 0xFF2ECC71 : 0xFF1D5A40);
                g.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFF50FA7B);
                g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, 0xFF50FA7B);
                g.drawCenteredString(font, Component.translatable("awakening_system.chest.continue"), cx, btnY + 7, btnHovered ? 0xFFFFFFFF : 0xFFDDECF2);
            }
        } else {
            if (!opened) {
                Component mainTitle = Component.translatable("awakening_system.chest.vault.unsealing").withStyle(ChatFormatting.BOLD);
                g.drawCenteredString(font, mainTitle, cx, cy - 86, rarityColor);

                Component boxName = Component.translatable(nameKey).withStyle(ChatFormatting.BOLD);
                g.drawCenteredString(font, boxName, cx, cy - 72, 0xFFFFFFFF);

                Component subTitle = Component.translatable("awakening_system.chest.vault.cracking");
                g.drawCenteredString(font, subTitle, cx, cy - 58, sys20 ? 0xFFFFD700 : 0xFF70E8FF);

                renderDimensionalVault(g, cx, cy - 2, animTime, rarityColor, false);

                Component calib = Component.translatable("awakening_system.chest.vault.overload");
                float pulse = (float) ((Math.sin(animTime * 0.6F) + 1.0F) * 0.5F);
                g.drawCenteredString(font, calib, cx, cy + 48, ((int)(160 + pulse * 95) << 24) | (rarityColor & 0xFFFFFF));
            } else {
                Component title = Component.translatable("awakening_system.chest.vault.opened").withStyle(ChatFormatting.BOLD);
                g.drawCenteredString(font, title, cx, cy - 90, 0xFFFFD866);

                Component chestTitle = Component.translatable(nameKey).withStyle(ChatFormatting.BOLD);
                g.drawCenteredString(font, chestTitle, cx, cy - 76, rarityColor);

                renderRewards(g, cx, cy, mouseX, mouseY);

                int btnW = 140;
                int btnH = 22;
                int btnX = cx - btnW / 2;
                int btnY = cy + 62;
                boolean btnHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? 0xFF2ECC71 : 0xFF1D5A40);
                g.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFF50FA7B);
                g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, 0xFF50FA7B);
                g.drawCenteredString(font, Component.translatable("awakening_system.chest.continue"), cx, btnY + 7, btnHovered ? 0xFFFFFFFF : 0xFFDDECF2);
            }
        }

        if (flashAlpha > 0.0F) {
            int a = (int) (flashAlpha * 255);
            g.fill(0, 0, width, height, (a << 24) | 0xFFFFFF);
        }

        if (!confettiList.isEmpty()) {
            for (Confetti c : confettiList) {
                if (c.y < height + 20) {
                    c.render(g);
                }
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderDimensionalVault(GuiGraphics g, int cx, int cy, float animTime, int rarityColor, boolean isOpened) {
        float shake = Math.min(1.0F, animTime / 40.0F) * 4.5F;
        int jx = isOpened ? 0 : (int) (Math.sin(animTime * 2.8F) * shake);
        int jy = isOpened ? 0 : (int) (Math.cos(animTime * 3.4F) * (shake * 0.7F));

        int vw = 74;
        int vh = 56;
        int vx = cx - vw / 2 + jx;
        int vy = cy - vh / 2 + jy;

        g.fill(cx - 24, cy - 80, cx + 24, cy + 30, (0x35 << 24) | (rarityColor & 0xFFFFFF));
        g.fill(cx - 10, cy - 90, cx + 10, cy + 30, (0x60 << 24) | 0xFFFFFF);

        g.fill(vx - 2, vy - 2, vx + vw + 2, vy + vh + 2, 0xFF08121D);
        g.fill(vx, vy, vx + vw, vy + vh, 0xFF142433);

        g.fill(vx, vy, vx + vw, vy + 4, rarityColor);
        g.fill(vx, vy + vh - 4, vx + vw, vy + vh, rarityColor);
        g.fill(vx, vy, vx + 4, vy + vh, rarityColor);
        g.fill(vx + vw - 4, vy, vx + vw, vy + vh, rarityColor);

        g.fill(vx + 4, vy + 18, vx + vw - 4, vy + 21, 0xFF08121D);
        g.fill(vx + 4, vy + 20, vx + vw - 4, vy + 22, rarityColor);

        if (!isOpened) {
            g.fill(vx + 6, vy + 6, vx + 16, vy + 12, 0xFF4A5568);
            g.fill(vx + vw - 16, vy + vh - 12, vx + vw - 6, vy + vh - 6, 0xFF4A5568);
            int sealSize = 18;
            int sx = cx - sealSize / 2 + jx;
            int sy = cy - sealSize / 2 + jy + 4;
            g.fill(sx, sy, sx + sealSize, sy + sealSize, rarityColor);
            g.fill(sx + 2, sy + 2, sx + sealSize - 2, sy + sealSize - 2, 0xFF102536);
            g.drawCenteredString(font, "❖", cx + jx, sy + 4, rarityColor);

            if (animTime > 18) {
                g.fill(vx + 14, vy + 26, vx + 28, vy + 28, 0xFFFFFFFF);
                g.fill(vx + vw - 28, vy + 26, vx + vw - 14, vy + 28, 0xFFFFFFFF);
            }
            if (animTime > 30) {
                g.fill(vx + 20, vy + 10, vx + 22, vy + 38, (0xC0 << 24) | (rarityColor & 0xFFFFFF));
                g.fill(vx + vw - 22, vy + 10, vx + vw - 20, vy + 38, (0xC0 << 24) | (rarityColor & 0xFFFFFF));
            }
        } else {
            g.fill(vx + 8, vy + 8, vx + vw - 8, vy + 22, (0xAA << 24) | (rarityColor & 0xFFFFFF));
        }
    }

    private void renderRouletteReel(GuiGraphics g, int cx, int cy, float animTime, int rarityColor, boolean isStopped) {
        float progress = Math.min(1.0F, animTime / 56.0F);
        float ease = 1.0F - (float) Math.pow(1.0F - progress, 3.2);
        float totalRotations = 5.0F * 360.0F;
        float spinAngle = (1.0F - ease) * totalRotations;

        int outerRadius = 56;
        int slotRadius = 40;
        int coreRadius = 14;

        for (int dy = -outerRadius; dy <= outerRadius; dy++) {
            int dx = (int) Math.sqrt(outerRadius * outerRadius - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, 0xEE081420);
        }

        int numTicks = 32;
        for (int i = 0; i < numTicks; i++) {
            double angleRad = Math.toRadians(i * (360.0 / numTicks) + spinAngle);
            int tx = (int) (cx + Math.cos(angleRad) * (outerRadius - 2));
            int ty = (int) (cy + Math.sin(angleRad) * (outerRadius - 2));
            boolean major = (i % 4 == 0);
            int tickColor = major ? 0xFFFFD866 : 0xFF286D87;
            g.fill(tx - (major ? 1 : 0), ty - (major ? 1 : 0), tx + (major ? 2 : 1), ty + (major ? 2 : 1), tickColor);
        }

        for (int k = 0; k < 8; k++) {
            double spokeRad = Math.toRadians(-90.0 + spinAngle + k * 45.0 + 22.5);
            double cosA = Math.cos(spokeRad);
            double sinA = Math.sin(spokeRad);
            for (int r = coreRadius + 2; r < outerRadius - 4; r += 4) {
                int px = (int) (cx + cosA * r);
                int py = (int) (cy + sinA * r);
                g.fill(px, py, px + 1, py + 1, 0x55307A94);
            }
        }

        for (int i = 0; i < 8; i++) {
            double slotAngleRad = Math.toRadians(-90.0 + spinAngle + i * 45.0);
            float px = (float) (cx + Math.cos(slotAngleRad) * slotRadius);
            float py = (float) (cy + Math.sin(slotAngleRad) * slotRadius);

            boolean isWinner = (i == 0);
            int boxX = (int) px - 10;
            int boxY = (int) py - 10;

            int borderColor = isWinner ? (isStopped || progress >= 0.95F ? 0xFFFFD700 : 0xFFE5C158) : 0xFF1E526B;
            int bgColor = isWinner ? (isStopped ? 0xDD42360E : 0x9942360E) : 0x880C1D2D;

            g.fill(boxX, boxY, boxX + 20, boxY + 20, bgColor);
            g.fill(boxX, boxY, boxX + 20, boxY + 1, borderColor);
            g.fill(boxX, boxY + 19, boxX + 20, boxY + 20, borderColor);
            g.fill(boxX, boxY, boxX + 1, boxY + 20, borderColor);
            g.fill(boxX + 19, boxY, boxX + 20, boxY + 20, borderColor);

            if (isWinner && (isStopped || progress >= 0.9F)) {
                g.fill(boxX - 1, boxY - 1, boxX + 21, boxY, 0xAAFFD700);
                g.fill(boxX - 1, boxY + 20, boxX + 21, boxY + 21, 0xAAFFD700);
            }

            ItemStack stack = (i < reelItems.size()) ? reelItems.get(i) : ItemStack.EMPTY;

            if (!stack.isEmpty()) {
                PoseStack pose = g.pose();
                pose.pushPose();
                pose.translate(px - 8, py - 8, 0);
                g.renderItem(stack, 0, 0);
                g.renderItemDecorations(font, stack, 0, 0);
                pose.popPose();
            }
        }

        for (int dy = -coreRadius; dy <= coreRadius; dy++) {
            int dx = (int) Math.sqrt(coreRadius * coreRadius - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, 0xFF0D1D2C);
        }
        for (int i = 0; i < 16; i++) {
            double a = Math.toRadians(i * 22.5);
            int kx = (int) (cx + Math.cos(a) * coreRadius);
            int ky = (int) (cy + Math.sin(a) * coreRadius);
            g.fill(kx, ky, kx + 1, ky + 1, 0xFFFFD866);
        }
        float pulse = (float) ((Math.sin(animTime * 0.4F) + 1.0F) * 0.5F);
        int emblemColor = ((int) (180 + pulse * 75) << 24) | 0xFFD866;
        g.drawCenteredString(font, "✦", cx, cy - 4, emblemColor);

        int pointerY = cy - outerRadius - 4;
        g.fill(cx - 8, pointerY - 4, cx + 8, pointerY, 0xFF142B3C);
        g.fill(cx - 7, pointerY - 3, cx + 7, pointerY - 1, 0xFFFFD866);
        g.drawCenteredString(font, "▼", cx, pointerY - 2, 0xFFFFD866);
    }

    private void renderAuraRays(GuiGraphics g, int cx, int cy, float animTime, int color) {
        int rays = 8;
        float baseAngle = animTime * 0.03F;
        int rayColor = (0x25 << 24) | (color & 0xFFFFFF);
        for (int i = 0; i < rays; i++) {
            float angle = baseAngle + i * (float) (Math.PI * 2 / rays);
            int rx = cx + (int) (Math.cos(angle) * 95);
            int ry = cy + (int) (Math.sin(angle) * 75);
            g.fill(rx - 2, ry - 2, rx + 2, ry + 2, rayColor);
        }
    }

    private void renderRewards(GuiGraphics g, int cx, int cy, int mouseX, int mouseY) {
        int totalCards = rewardStacks.size() + (coinsWon > 0 ? 1 : 0);

        if (totalCards == 0) {
            if (!rewardSummary.isEmpty()) {
                g.drawCenteredString(font, Component.literal(rewardSummary), cx, cy - 10, 0xFF50FA7B);
            } else {
                g.drawCenteredString(font, Component.translatable("awakening_system.chest.receiving"), cx, cy - 10, 0xFF70E8FF);
            }
            return;
        }

        int cardW = 54;
        int cardH = 58;
        int gap = 8;
        int totalW = totalCards * cardW + (totalCards - 1) * gap;
        int startX = cx - totalW / 2;
        int startY = cy - 36;

        int cardIndex = 0;
        ItemStack hoveredStack = null;

        for (ItemStack stack : rewardStacks) {
            if (stack == null || stack.isEmpty()) continue;
            int x = startX + cardIndex * (cardW + gap);
            int y = startY;
            boolean hovered = mouseX >= x && mouseX < x + cardW && mouseY >= y && mouseY < y + cardH;
            if (hovered) hoveredStack = stack;

            g.fill(x, y, x + cardW, y + cardH, hovered ? 0x601C465D : 0x50102536);
            g.fill(x, y, x + cardW, y + 1, 0xFF70E8FF);
            g.fill(x, y + cardH - 1, x + cardW, y + cardH, 0xFF70E8FF);

            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(x + 18, y + 12, 0);
            pose.scale(1.25F, 1.25F, 1.25F);
            g.renderItem(stack, 0, 0);
            pose.popPose();

            String countStr = "x" + stack.getCount();
            g.drawCenteredString(font, countStr, x + cardW / 2, y + 36, 0xFFFFFFFF);
            cardIndex++;
        }

        if (coinsWon > 0) {
            int x = startX + cardIndex * (cardW + gap);
            int y = startY;
            boolean hovered = mouseX >= x && mouseX < x + cardW && mouseY >= y && mouseY < y + cardH;

            g.fill(x, y, x + cardW, y + cardH, hovered ? 0x603C461D : 0x50252516);
            g.fill(x, y, x + cardW, y + 1, 0xFFFFD866);
            g.fill(x, y + cardH - 1, x + cardW, y + cardH, 0xFFFFD866);

            ItemStack coinStack = SystemItems.coin(coinsWon);
            if (hovered) hoveredStack = coinStack;
            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(x + 18, y + 12, 0);
            pose.scale(1.25F, 1.25F, 1.25F);
            g.renderItem(coinStack, 0, 0);
            pose.popPose();

            String coinStr = "+" + coinsWon;
            g.drawCenteredString(font, coinStr, x + cardW / 2, y + 36, 0xFFFFD866);
        }

        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            g.renderTooltip(font, hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && opened) {
            int cx = width / 2;
            int cy = height / 2;
            int btnW = 140;
            int btnH = 22;
            int btnX = cx - btnW / 2;
            int btnY = "system_roulette".equals(boxId) ? cy + 74 : cy + 62;
            if ((mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) || ticks > ("system_roulette".equals(boxId) ? 75 : 55)) {
                closeAndReturn();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (opened && (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER)) {
            closeAndReturn();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void closeAndReturn() {
        Minecraft.getInstance().setScreen(parentScreen);
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
        }
    }
}
