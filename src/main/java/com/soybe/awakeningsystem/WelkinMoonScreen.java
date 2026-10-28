package com.soybe.awakeningsystem;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pantalla cinemática interactiva de la Bendición Lunar (Blessing of the Welkin Moon).
 * Admite la Doncella Lunar estándar y la versión Ascendida / Mejorada con Columbina
 * (Tercera Heraldo de los Fatui), otorgando +50% de Protogemas diarias (+135) y
 * efectos celestiales místicos violetas y lavanda.
 */
public class WelkinMoonScreen extends Screen {

    private static final ResourceLocation MAIDEN_TEX =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/welkin/welkin_maiden.png");
    private static final ResourceLocation COLUMBINA_TEX =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/welkin/welkin_columbina.png");

    private final int initialDaysRemaining;
    private final int rewardPrimos;
    private final boolean isEnhanced;
    private final int questStep;
    private final int questProgress;
    private boolean canClaimToday;
    private boolean claimed;
    private long openTime;
    private long claimTime = 0L;

    private record Star(float x, float y, float speed, float size, float phase) {}
    private final List<Star> stars = new ArrayList<>();

    private record Dust(float x, float speedY, float phase, float size) {}
    private final List<Dust> dustMotes = new ArrayList<>();

    private static class Spark {
        float x, y;
        float vx, vy;
        float size;
        int color;
        float life;
        float maxLife;

        Spark(float x, float y, float vx, float vy, float size, int color, float maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.color = color;
            this.life = 0;
            this.maxLife = maxLife;
        }
    }
    private final List<Spark> sparks = new ArrayList<>();
    private final Random rng = new Random();

    public WelkinMoonScreen(int daysRemaining, int rewardPrimos, boolean canClaimToday, boolean isEnhanced, int questStep, int questProgress) {
        super(Component.translatable(isEnhanced ? "awakening_system.welkin.columbina_title" : "awakening_system.welkin.title"));
        this.initialDaysRemaining = daysRemaining;
        this.rewardPrimos = rewardPrimos;
        this.canClaimToday = canClaimToday;
        this.claimed = !canClaimToday;
        this.isEnhanced = isEnhanced;
        this.questStep = questStep;
        this.questProgress = questProgress;
    }

    public WelkinMoonScreen(int daysRemaining, int rewardPrimos, boolean canClaimToday) {
        this(daysRemaining, rewardPrimos, canClaimToday, false, 0, 0);
    }

    @Override
    protected void init() {
        super.init();
        this.openTime = System.currentTimeMillis();

        stars.clear();
        Random starRng = new Random(42L);
        for (int i = 0; i < 90; i++) {
            stars.add(new Star(
                    starRng.nextFloat(),
                    starRng.nextFloat(),
                    0.4F + starRng.nextFloat() * 1.6F,
                    1.0F + starRng.nextFloat() * 1.8F,
                    starRng.nextFloat() * 6.28F
            ));
        }

        dustMotes.clear();
        for (int i = 0; i < 35; i++) {
            dustMotes.add(new Dust(
                    starRng.nextFloat(),
                    0.03F + starRng.nextFloat() * 0.06F,
                    starRng.nextFloat() * 6.28F,
                    1.2F + starRng.nextFloat() * 1.5F
            ));
        }

        sparks.clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long elapsed = System.currentTimeMillis() - openTime;
        float introAlpha = Math.min(1.0F, elapsed / 600.0F);

        int cx = this.width / 2;
        int cy = this.height / 2;

        if (isEnhanced) {
            graphics.fillGradient(0, 0, this.width, this.height, 0xF8080518, 0xF81C103C);
        } else {
            graphics.fillGradient(0, 0, this.width, this.height, 0xF8030510, 0xF8121634);
        }

        for (Star star : stars) {
            float blink = (float) Math.sin((elapsed / 1000.0F) * star.speed + star.phase);
            int alpha = (int) (Math.max(0.08F, (blink + 1.0F) * 0.45F) * 255 * introAlpha);
            if (alpha > 12) {
                int sx = (int) (star.x * this.width);
                int sy = (int) (star.y * this.height);
                int starColor = isEnhanced ? ((alpha << 24) | 0xE1BEE7) : ((alpha << 24) | 0xFFE082);
                graphics.fill(sx, sy, sx + (int) star.size, sy + (int) star.size, starColor);
            }
        }

        for (Dust dust : dustMotes) {
            float yPos = ((1.0F - (elapsed * 0.00005F * dust.speedY * 20.0F + dust.phase) % 1.0F)) * this.height;
            float xOffset = (float) Math.sin((elapsed * 0.001F) + dust.phase) * 12.0F;
            int dx = (int) (dust.x * this.width + xOffset);
            int dy = (int) yPos;
            int alpha = (int) (65 * introAlpha);
            int dustCol = isEnhanced ? ((alpha << 24) | 0xCE93D8) : ((alpha << 24) | 0x80D8FF);
            graphics.fill(dx, dy, dx + (int) dust.size, dy + (int) dust.size, dustCol);
        }

        int maidenSize = Math.min(220, Math.min(this.width - 40, this.height - 130));
        float floatOffset = (float) Math.sin(elapsed / 900.0F) * 3.5F;
        int maidenCenterY = cy - 8;

        drawAstrolabe(graphics, cx, maidenCenterY + (int) floatOffset, maidenSize, elapsed);

        drawCelestialHalo(graphics, cx, maidenCenterY + (int) floatOffset, maidenSize, elapsed);

        int maidenX = cx - (maidenSize / 2);
        int maidenY = maidenCenterY - (maidenSize / 2) + (int) floatOffset;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(isEnhanced ? COLUMBINA_TEX : MAIDEN_TEX, maidenX, maidenY, maidenSize, maidenSize, 0.0F, 0.0F, 512, 512, 512, 512);
        RenderSystem.disableBlend();

        int titleY = Math.max(12, maidenY - 42);
        if (isEnhanced) {
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.welkin.columbina_header").getString(), cx, titleY, 0xFFE1BEE7);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.welkin.columbina_subtitle").getString(), cx, titleY + 12, 0xFF80D8FF);
        } else {
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.welkin.header").getString(), cx, titleY, 0xFFFFE082);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.welkin.subtitle").getString(), cx, titleY + 12, 0xFF80D8FF);
        }

        int divHalfW = Math.min(180, this.width / 3);
        int divCol = isEnhanced ? 0x66CE93D8 : 0x66FFE082;
        graphics.fill(cx - divHalfW, titleY + 24, cx - 10, titleY + 25, divCol);
        graphics.drawCenteredString(this.font, isEnhanced ? "§d✦" : "§6✦", cx, titleY + 20, isEnhanced ? 0xFFCE93D8 : 0xFFFFD700);
        graphics.fill(cx + 10, titleY + 24, cx + divHalfW, titleY + 25, divCol);

        int currentDays = claimed ? Math.max(0, initialDaysRemaining - (canClaimToday ? 1 : 0)) : initialDaysRemaining;
        int badgeW = isEnhanced ? 240 : 210;
        int badgeH = 20;
        int badgeX = cx - (badgeW / 2);
        int badgeY = maidenY + maidenSize + 6;

        graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, isEnhanced ? 0xDD140E26 : 0xCC090E20);
        graphics.renderOutline(badgeX, badgeY, badgeW, badgeH, isEnhanced ? 0xCCCE93D8 : 0xAAFFD700);
        drawFiligreeCorners(graphics, badgeX, badgeY, badgeW, badgeH, isEnhanced ? 0xFFCE93D8 : 0xFFFFD700);

        String daysText = isEnhanced
                ? Component.translatable("awakening_system.welkin.columbina_days_remaining", currentDays).getString()
                : Component.translatable("awakening_system.welkin.days_remaining", currentDays).getString();
        graphics.drawCenteredString(this.font, daysText, cx, badgeY + 6, isEnhanced ? 0xFFF3E5F5 : 0xFFFFFFFF);

        int questY = badgeY + badgeH + 6;
        int questW = 280;
        int questH = 17;
        int questX = cx - (questW / 2);

        if (!isEnhanced) {
            boolean questHover = mouseX >= questX && mouseX <= questX + questW && mouseY >= questY && mouseY <= questY + questH;
            String questLabel;
            int qBg;
            int qBorder;
            int qTextCol;

            if (questStep <= 0) {
                questLabel = "✦ Start Quest: Tri-Lunar Awakening (+50%) ✦";
                qBg = questHover ? 0xDD4A148C : 0x99230D42;
                qBorder = questHover ? 0xFFFFD700 : 0xFFBA68C8;
                qTextCol = questHover ? 0xFFFFF176 : 0xFFE1BEE7;
            } else if (questStep == 1) {
                questLabel = "✦ Quest: Journey to the Three Moons Shrine ✦";
                qBg = 0xAA1E1236;
                qBorder = 0xFFAB47BC;
                qTextCol = 0xFFE1BEE7;
            } else if (questStep == 2) {
                questLabel = "✦ Quest: Trials of Aria, Sonnet, and Canon ✦";
                qBg = 0xAA2B0B3A;
                qBorder = 0xFFFF7043;
                qTextCol = 0xFFFFCC80;
            } else {
                questLabel = "✦ Final Rite: Consecration at the Tri-Lunar Altar ✦";
                qBg = 0xAA0B243B;
                qBorder = 0xFF4DD0E1;
                qTextCol = 0xFFB2EBF2;
            }

            graphics.fill(questX, questY, questX + questW, questY + questH, qBg);
            graphics.renderOutline(questX, questY, questW, questH, qBorder);
            graphics.drawCenteredString(this.font, questLabel, cx, questY + 5, qTextCol);
        } else {
            graphics.fill(questX, questY, questX + questW, questY + questH, 0x882A084E);
            graphics.renderOutline(questX, questY, questW, questH, 0xAAAB47BC);
            graphics.drawCenteredString(this.font, "✦ TRI-LUNAR GODDESS AWAKENED (+50% PRIMOGEMS) ✦", cx, questY + 5, 0xFFE1BEE7);
        }

        int btnY = questY + questH + 8;
        int btnW = Math.min(340, this.width - 32);
        int btnX = cx - (btnW / 2);
        int btnH = 26;

        boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

        if (!claimed) {
            float pulse = (float) (Math.sin(elapsed / 220.0F) * 0.5F + 0.5F);
            int btnBorder = isEnhanced ? 0xFFE1BEE7 : 0xFFFFD700;
            int btnBg = hoverBtn
                    ? (isEnhanced ? 0xFF7B1FA2 : 0xFFC99020)
                    : (isEnhanced ? (0xEE311B92 + (int)(pulse * 25.0F) * 0x010001) : (0xEE6E4510 + (int)(pulse * 30.0F) * 0x010100));

            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
            graphics.renderOutline(btnX, btnY, btnW, btnH, btnBorder);
            drawFiligreeCorners(graphics, btnX, btnY, btnW, btnH, 0xFFFFFFFF);

            ItemStack primoStack = SystemItems.PRIMOGEM.isBound() ? new ItemStack(SystemItems.PRIMOGEM.get()) : ItemStack.EMPTY;
            if (!primoStack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(btnX + 16, btnY + 5, 0);
                graphics.pose().scale(1.0F, 1.0F, 1.0F);
                graphics.renderItem(primoStack, 0, 0);
                graphics.pose().popPose();
            }

            String claimText = isEnhanced
                    ? Component.translatable("awakening_system.welkin.columbina_claim_button", rewardPrimos).getString()
                    : Component.translatable("awakening_system.welkin.claim_button", rewardPrimos).getString();
            graphics.drawCenteredString(this.font, claimText, cx + 8, btnY + 9, 0xFFFFFFFF);
        } else {
            int sealW = Math.min(300, this.width - 32);
            int sealX = cx - (sealW / 2);
            graphics.fill(sealX, btnY, sealX + sealW, btnY + btnH, isEnhanced ? 0xCC1B0E2E : 0xCC0E2E1E);
            graphics.renderOutline(sealX, btnY, sealW, btnH, isEnhanced ? 0xFFBA68C8 : 0xFF2ECC71);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.welkin.already_claimed").getString(), cx, btnY + 9, 0xFFFFFFFF);

            float promptAlpha = 0.55F + 0.45F * (float) Math.sin(elapsed / 380.0F);
            int promptCol = ((int) (promptAlpha * 255) << 24) | (isEnhanced ? 0xE1BEE7 : 0xBBDEFB);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.welkin.continue_prompt").getString(), cx, btnY + btnH + 8, promptCol);
        }

        renderClaimCelebration(graphics, cx, maidenCenterY, elapsed);
    }

    private void drawAstrolabe(GuiGraphics graphics, int cx, int cy, int size, long elapsed) {
        float rot1 = elapsed * 0.0004F;
        float rot2 = -elapsed * 0.0003F;

        float rOut = size * 0.56F;
        drawOrbitRing(graphics, cx, cy, rOut, isEnhanced ? 0x33CE93D8 : 0x33FFE082, rot1, 6);

        float rIn = size * 0.49F;
        drawOrbitRing(graphics, cx, cy, rIn, isEnhanced ? 0x3380D8FF : 0x2280D8FF, rot2, 4);
    }

    private void drawOrbitRing(GuiGraphics graphics, int cx, int cy, float radius, int ringColor, float rot, int nodes) {
        int segs = 48;
        for (int i = 0; i < segs; i++) {
            double a1 = (i * 2.0 * Math.PI) / segs;
            double a2 = ((i + 1) * 2.0 * Math.PI) / segs;
            int x1 = (int) (cx + Math.cos(a1) * radius);
            int y1 = (int) (cy + Math.sin(a1) * radius);
            int x2 = (int) (cx + Math.cos(a2) * radius);
            int y2 = (int) (cy + Math.sin(a2) * radius);
            graphics.fill(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2) + 1, Math.max(y1, y2) + 1, ringColor);
        }

        for (int n = 0; n < nodes; n++) {
            double angle = rot + (n * 2.0 * Math.PI) / nodes;
            int nx = (int) (cx + Math.cos(angle) * radius);
            int ny = (int) (cy + Math.sin(angle) * radius);
            graphics.fill(nx - 2, ny - 2, nx + 2, ny + 2, isEnhanced ? 0xFFE1BEE7 : 0xFFFFE082);
            graphics.fill(nx - 1, ny - 1, nx + 1, ny + 1, 0xFFFFFFFF);
        }
    }

    private void drawCelestialHalo(GuiGraphics graphics, int cx, int cy, int size, long elapsed) {
        float pulse = (float) Math.sin(elapsed / 600.0F);
        int maxR = (int) (size * 0.44F + pulse * 5.0F);
        for (int r = maxR; r > maxR - 16; r -= 4) {
            int alpha = (int) (((maxR - r) / 16.0F) * (claimed ? 24 : 38));
            int col = isEnhanced
                    ? ((alpha << 24) | (claimed ? 0x80D8FF : 0xCE93D8))
                    : ((alpha << 24) | (claimed ? 0x80D8FF : 0xFFE082));
            graphics.fill(cx - r, cy - r, cx + r, cy + r, col);
        }
    }

    private void renderClaimCelebration(GuiGraphics graphics, int cx, int cy, long elapsed) {
        if (claimTime <= 0L) return;

        long dt = System.currentTimeMillis() - claimTime;

        if (dt < 480L) {
            float flashProg = 1.0F - (dt / 480.0F);
            int flashAlpha = (int) (flashProg * 140);
            int flashCol = isEnhanced ? ((flashAlpha << 24) | 0xF3E5F5) : ((flashAlpha << 24) | 0xFFFFF0);
            graphics.fill(0, 0, this.width, this.height, flashCol);
        }

        for (int i = sparks.size() - 1; i >= 0; i--) {
            Spark s = sparks.get(i);
            s.life += 1.0F;
            if (s.life >= s.maxLife) {
                sparks.remove(i);
                continue;
            }
            s.x += s.vx;
            s.y += s.vy;
            s.vy += 0.12F;
            s.vx *= 0.96F;

            float lifeRatio = 1.0F - (s.life / s.maxLife);
            int alpha = (int) (lifeRatio * 255);
            int col = (alpha << 24) | (s.color & 0x00FFFFFF);
            int sz = Math.max(1, (int) (s.size * lifeRatio));
            graphics.fill((int) s.x, (int) s.y, (int) s.x + sz, (int) s.y + sz, col);
        }

        if (dt < 3000L) {
            float bounceScale = 1.0F;
            if (dt < 350L) {
                float t = dt / 350.0F;
                bounceScale = 1.0F + 0.55F * (1.0F - t);
            }

            ItemStack primoStack = SystemItems.PRIMOGEM.isBound() ? new ItemStack(SystemItems.PRIMOGEM.get()) : ItemStack.EMPTY;
            if (!primoStack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(cx, cy - 12, 100);
                float finalScale = 3.2F * bounceScale;
                graphics.pose().scale(finalScale, finalScale, 1.0F);
                graphics.renderItem(primoStack, -8, -8);
                graphics.pose().popPose();
            }

            if (isEnhanced) {
                graphics.drawCenteredString(this.font, "§d§l✦ §6§l+" + rewardPrimos + " PRIMOGEMS §d§l✦", cx, cy + 30, 0xFFFFD700);
                graphics.drawCenteredString(this.font, "§b✦ COLUMBINA'S GRACE (+50% BONUS) ✦", cx, cy + 42, 0xFF80D8FF);
            } else {
                graphics.drawCenteredString(this.font, "§6§l✦ §e§l+" + rewardPrimos + " PRIMOGEMS §6§l✦", cx, cy + 32, 0xFFFFD700);
            }
        }
    }

    private void drawFiligreeCorners(GuiGraphics graphics, int x, int y, int w, int h, int gold) {
        graphics.fill(x, y, x + 4, y + 1, gold);
        graphics.fill(x, y, x + 1, y + 4, gold);
        graphics.fill(x + w - 4, y, x + w, y + 1, gold);
        graphics.fill(x + w - 1, y, x + w, y + 4, gold);
        graphics.fill(x, y + h - 1, x + 4, y + h, gold);
        graphics.fill(x, y + h - 4, x + 1, y + h, gold);
        graphics.fill(x + w - 4, y + h - 1, x + w, y + h, gold);
        graphics.fill(x + w - 1, y + h - 4, x + w, y + h, gold);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (!isEnhanced && questStep <= 0) {
                int maidenSize = Math.min(220, Math.min(this.width - 40, this.height - 130));
                int maidenCenterY = (this.height / 2) - 24;
                int maidenY = maidenCenterY - (maidenSize / 2);
                int badgeH = 20;
                int badgeY = maidenY + maidenSize + 6;
                int qY = badgeY + badgeH + 6;
                int qW = 280;
                int qH = 17;
                int qX = (this.width / 2) - (qW / 2);

                if (mouseX >= qX && mouseX <= qX + qW && mouseY >= qY && mouseY <= qY + qH) {
                    PacketDistributor.sendToServer(new StartWelkinQuestPayload());
                    try {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    } catch (Throwable t) {
                        AwakeningSystemMod.LOGGER.debug("Error playing UI button click sound: {}", t.getMessage());
                    }
                    return true;
                }
            }
            handleClaimOrClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 32 || keyCode == 257) {
            handleClaimOrClose();
            return true;
        }
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void handleClaimOrClose() {
        if (!claimed && canClaimToday) {
            this.claimed = true;
            this.canClaimToday = false;
            this.claimTime = System.currentTimeMillis();

            PacketDistributor.sendToServer(new ClaimWelkinDailyPayload());

            try {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.1F));
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.debug("Error playing claim reward sound: {}", t.getMessage());
            }

            int cx = this.width / 2;
            int cy = (this.height / 2) - 24;
            for (int i = 0; i < 54; i++) {
                float angle = rng.nextFloat() * 6.283185F;
                float spd = 2.5F + rng.nextFloat() * 5.5F;
                int color;
                if (isEnhanced) {
                    int r = rng.nextInt(4);
                    color = (r == 0) ? 0xFFCE93D8 : (r == 1 ? 0xFFFFE082 : (r == 2 ? 0xFF80D8FF : 0xFFFFFFFF));
                } else {
                    color = rng.nextInt(3) == 0 ? 0xFFFFE082 : (rng.nextBoolean() ? 0xFF80D8FF : 0xFFFFFFFF);
                }
                sparks.add(new Spark(
                        cx, cy,
                        (float) Math.cos(angle) * spd,
                        (float) Math.sin(angle) * spd - 1.2F,
                        2.5F + rng.nextFloat() * 2.5F,
                        color,
                        30.0F + rng.nextFloat() * 25.0F
                ));
            }
        } else {
            this.onClose();
        }
    }
}
