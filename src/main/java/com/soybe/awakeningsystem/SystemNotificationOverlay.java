package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class SystemNotificationOverlay {
    public static boolean HIDE_VANILLA_HEARTS = true;

    private static String announcementTitle = null;
    private static String announcementMessage = null;
    private static int announcementColor = 0xFFFFD700;
    private static int announcementTicks = 0;
    private static final int ANNOUNCEMENT_MAX_TICKS = 140;

    private SystemNotificationOverlay() {}

    public static void showAnnouncement(String title, String message, int color) {
        announcementTitle = title;
        announcementMessage = message;
        announcementColor = color;
        announcementTicks = ANNOUNCEMENT_MAX_TICKS;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F));
            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.3F));
        }
    }

    public static void tick() {
        if (announcementTicks > 0) {
            announcementTicks--;
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        boolean isChat = minecraft.screen instanceof ChatScreen;
        if (minecraft.screen == null || isChat) {
            SystemHudOverlay.render(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight());
            AwakeningSystemClient.renderNotification(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight());
            renderDartAimingReticle(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(), minecraft);
            renderAnkleWeightsSpeedBurst(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(), minecraft);
            com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.render(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight());
            renderLimboAwakening(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(), minecraft);
        }
        if (announcementTicks > 0) {
            renderGlobalAnnouncement(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(), minecraft);
        }
    }

    private static void renderDartAimingReticle(net.minecraft.client.gui.GuiGraphics graphics, int width, int height, Minecraft mc) {
        if (mc.player == null) return;
        boolean mainHand = mc.player.getMainHandItem().is(SystemItems.DART.get());
        boolean offHand = mc.player.getOffhandItem().is(SystemItems.DART.get());
        if (!mainHand && !offHand) return;

        boolean isUsing = mc.player.isUsingItem() && (mc.player.getUseItem().is(SystemItems.DART.get()));
        int useTicks = isUsing ? mc.player.getTicksUsingItem() : 0;

        int cx = width / 2;
        int cy = height / 2;

        long time = System.currentTimeMillis();
        boolean isCrouching = mc.player.isCrouching();

        double amp;
        if (!isUsing) {
            amp = isCrouching ? 4.0 : 8.0;
        } else if (useTicks < 10) {
            amp = (isCrouching ? 2.5 : 5.0);
        } else if (useTicks <= 30) {
            amp = isCrouching ? 1.0 : 2.5;
        } else {
            float fatigue = Math.min(6.0F, (useTicks - 30) * 0.3F);
            amp = (isCrouching ? 2.5 : 5.0) + fatigue;
        }

        int sx = (int) (Math.sin(time * 0.0025) * amp + Math.sin(time * 0.0052) * (amp * 0.4));
        int sy = (int) (Math.cos(time * 0.0030) * (amp * 0.7) + Math.cos(time * 0.0061) * (amp * 0.3));

        int rx = cx + sx;
        int ry = cy + sy;

        int col;
        Component hint;
        if (!isUsing) {
            col = isCrouching ? 0xFF00E5FF : 0xFFFFD700;
            hint = isCrouching
                    ? Component.translatable("awakening_system.dart.reticle.aim_prompt")
                    : Component.translatable("awakening_system.dart.reticle.stand_prompt");
        } else if (useTicks < 10) {
            col = 0xFFFFD700;
            hint = Component.translatable("awakening_system.dart.reticle.charging", (useTicks * 10));
        } else if (useTicks <= 30) {
            col = 0xFF00E676;
            hint = Component.translatable("awakening_system.dart.reticle.sweet_spot");
        } else {
            col = 0xFFFF3D00;
            hint = Component.translatable("awakening_system.dart.reticle.fatigue");
        }

        int bracketDist = isUsing ? Math.max(4, 12 - useTicks / 2) : 10;
        graphics.fill(rx - bracketDist - 5, ry - 1, rx - bracketDist, ry + 1, col);
        graphics.fill(rx + bracketDist, ry - 1, rx + bracketDist + 5, ry + 1, col);
        graphics.fill(rx - 1, ry - bracketDist - 5, rx + 1, ry - bracketDist, col);
        graphics.fill(rx - 1, ry + bracketDist, rx + 1, ry + bracketDist + 5, col);

        graphics.fill(rx - 1, ry - 1, rx + 2, ry + 2, col);

        if (isUsing) {
            int barW = 32;
            int barH = 3;
            int barX = rx - barW / 2;
            int barY = ry + 8;
            graphics.fill(barX, barY, barX + barW, barY + barH, 0xAA000000);
            int progress = Math.min(barW, (useTicks * barW) / 25);
            graphics.fill(barX, barY, barX + progress, barY + barH, col);
            graphics.renderOutline(barX, barY, barW, barH, 0x88FFFFFF);
        }

        int hw = mc.font.width(hint);
        graphics.drawString(mc.font, hint, rx - hw / 2, ry + (isUsing ? 15 : 12), 0xFFFFFF, true);
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPre(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) {
        boolean shouldHide = com.soybe.awakeningsystem.client.gui.GuiConfig.shouldHideVanillaHearts();
        if (shouldHide && (VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())
                || VanillaGuiLayers.ARMOR_LEVEL.equals(event.getName()))) {
            var mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
                event.setCanceled(true);
            }
        }
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.shouldHideVanillaFood() && VanillaGuiLayers.FOOD_LEVEL.equals(event.getName())) {
            var mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
                event.setCanceled(true);
            }
        }
    }

    private static void renderAnkleWeightsSpeedBurst(net.minecraft.client.gui.GuiGraphics graphics, int width, int height, Minecraft mc) {
        if (mc.player == null) return;
        var effect = mc.player.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
        if (effect == null || effect.getAmplifier() < 3) return;

        int duration = effect.getDuration();
        if (duration <= 0) return;

        int seconds = (duration + 19) / 20;
        int barW = 160;
        int barH = 14;
        int barX = (width - barW) / 2;
        int barY = height - 92;

        graphics.fill(barX, barY, barX + barW, barY + barH, 0xD0101622);
        graphics.renderOutline(barX, barY, barW, barH, 0xFF00E676);

        int fillW = Math.min(barW - 2, (int) ((barW - 2) * (duration / 300.0F)));
        graphics.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, 0xAA00C853);

        String text = "§a⚡ SPEED BURST IV: §f" + seconds + "s";
        graphics.drawCenteredString(mc.font, text, width / 2, barY + 3, 0xFFFFFFFF);
    }

    private static void renderGlobalAnnouncement(net.minecraft.client.gui.GuiGraphics graphics, int width, int height, Minecraft mc) {
        if (announcementTicks <= 0 || announcementTitle == null) return;

        float alpha = 1.0F;
        if (announcementTicks > ANNOUNCEMENT_MAX_TICKS - 15) {
            alpha = (float) (ANNOUNCEMENT_MAX_TICKS - announcementTicks) / 15.0F;
        } else if (announcementTicks < 25) {
            alpha = (float) announcementTicks / 25.0F;
        }
        alpha = net.minecraft.util.Mth.clamp(alpha, 0.0F, 1.0F);
        int aByte = (int) (alpha * 255.0F);
        if (aByte <= 4) return;

        String rawMsg = announcementMessage != null ? announcementMessage.replace("\\n", "\n") : null;
        String[] msgLines = rawMsg != null ? rawMsg.split("\n") : new String[0];

        int titleW = mc.font.width(announcementTitle);
        int maxMsgW = 0;
        for (String line : msgLines) {
            maxMsgW = Math.max(maxMsgW, mc.font.width(line));
        }

        int bannerW = Math.max(340, Math.max(titleW, maxMsgW) + 40);
        int bannerH = Math.max(30, 16 + msgLines.length * 12);
        int bannerX = (width - bannerW) / 2;
        int bannerY = 12;

        int bgCol = (Math.min(aByte, 0xEE) << 24) | 0x0E0B1A;
        int borderCol = (aByte << 24) | (announcementColor & 0x00FFFFFF);

        graphics.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, bgCol);
        graphics.fill(bannerX, bannerY, bannerX + bannerW, bannerY + 1, borderCol);
        graphics.fill(bannerX, bannerY + bannerH - 1, bannerX + bannerW, bannerY + bannerH, borderCol);
        graphics.fill(bannerX, bannerY, bannerX + 1, bannerY + bannerH, borderCol);
        graphics.fill(bannerX + bannerW - 1, bannerY, bannerX + bannerW, bannerY + bannerH, borderCol);

        int cornerCol = (aByte << 24) | 0xFFFFFF;
        graphics.fill(bannerX + 1, bannerY + 1, bannerX + 3, bannerY + 3, cornerCol);
        graphics.fill(bannerX + bannerW - 3, bannerY + 1, bannerX + bannerW - 1, bannerY + 3, cornerCol);
        graphics.fill(bannerX + 1, bannerY + bannerH - 3, bannerX + 3, bannerY + bannerH - 1, cornerCol);
        graphics.fill(bannerX + bannerW - 3, bannerY + bannerH - 3, bannerX + bannerW - 1, bannerY + bannerH - 1, cornerCol);

        graphics.drawCenteredString(mc.font, announcementTitle, width / 2, bannerY + 4, borderCol);
        int textY = bannerY + 16;
        for (String line : msgLines) {
            graphics.drawCenteredString(mc.font, line, width / 2, textY, (aByte << 24) | 0xEEEEEE);
            textY += 12;
        }
    }

    private static void renderLimboAwakening(net.minecraft.client.gui.GuiGraphics g, int screenW, int screenH, Minecraft mc) {
        if (AwakeningSystemClient.limboAwakeningTicks <= 0) return;
        float p = 1.0F - ((float) AwakeningSystemClient.limboAwakeningTicks / (float) AwakeningSystemClient.LIMBO_CINEMATIC_TOTAL);
        float aperture = 0.0F;
        if (p < 0.22F) {
            aperture = 0.0F;
        } else if (p < 0.44F) {
            float f = (p - 0.22F) / 0.22F;
            aperture = (float) Math.sin(f * Math.PI) * 0.32F;
        } else if (p < 0.54F) {
            aperture = 0.0F;
        } else if (p < 0.74F) {
            float f = (p - 0.54F) / 0.20F;
            aperture = 0.15F + (float) Math.sin(f * Math.PI) * 0.50F;
        } else {
            float f = (p - 0.74F) / 0.26F;
            float smooth = f * f * (3.0F - 2.0F * f);
            aperture = 0.15F + 0.85F * smooth;
        }
        aperture = Math.max(0.0F, Math.min(1.0F, aperture));

        int halfH = screenH / 2;
        int eyelidH = Math.round((1.0F - aperture) * halfH);
        if (eyelidH > 0) {
            g.fill(0, 0, screenW, eyelidH, 0xFF000000);
            g.fill(0, screenH - eyelidH, screenW, screenH, 0xFF000000);

            int rimH = Math.min(8, halfH - eyelidH);
            if (rimH > 0) {
                g.fillGradient(0, eyelidH, screenW, eyelidH + rimH, 0xAA000000, 0x00000000);
                g.fillGradient(0, screenH - eyelidH - rimH, screenW, screenH - eyelidH, 0x00000000, 0xAA000000);
            }
        }

        if (aperture < 0.95F) {
            int alpha = Math.round((1.0F - aperture) * 160.0F);
            g.fill(0, 0, screenW, screenH, (alpha << 24) | 0x050810);
        }
    }
}
