package com.soybe.awakeningsystem.abyss.client;

import com.soybe.awakeningsystem.abyss.AbyssChamberResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * "Challenge completed", as the game shows it: a band across the middle with the chamber's name, its three
 * stars coming in one after the other, the time left and the floor's stars, then the buttons.
 */
public final class AbyssChamberResultScreen extends Screen {
    private final AbyssChamberResult.ResultPayload r;
    private final long opened = System.currentTimeMillis();

    private AbyssChamberResultScreen(AbyssChamberResult.ResultPayload r) {
        super(Component.translatable("gui.awakening_system.abyss_result.title"));
        this.r = r;
    }

    public static void open(AbyssChamberResult.ResultPayload r) {
        Minecraft.getInstance().setScreen(new AbyssChamberResultScreen(r));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // the world stays in view, darkened
        g.fill(0, 0, this.width, this.height, 0x88000000);
    }

    private String nextLabel() {
        if (!r.lastChamber()) return Component.translatable("gui.awakening_system.abyss_result.next_chamber", r.chamber() + 1).getString();
        if (r.canAdvanceFloor()) return Component.translatable("gui.awakening_system.abyss_result.next_floor", r.floor() + 1).getString();
        return Component.translatable("gui.awakening_system.abyss_result.finish").getString();
    }

    private String[] labels() {
        return new String[]{nextLabel(), Component.translatable("gui.awakening_system.abyss_result.leave").getString(),
                Component.translatable("gui.awakening_system.abyss_result.stay").getString()};
    }

    private int bw(String s) {
        return this.font.width(s) + 34;
    }

    /** How much the whole window is blown up: the text was too small to read at low interface scales. */
    private float ui() {
        return Math.max(1.0F, Math.min(2.2F, this.width / 380.0F));
    }

    private int vw() {
        return (int) (this.width / ui());
    }

    private int vh() {
        return (int) (this.height / ui());
    }

    private int[] xs() {
        String[] l = labels();
        int total = 0;
        for (String s : l) total += bw(s) + 12;
        int x = (vw() - (total - 12)) / 2;
        int[] out = new int[l.length];
        for (int i = 0; i < l.length; i++) {
            out[i] = x;
            x += bw(l[i]) + 12;
        }
        return out;
    }

    private int buttonsY() {
        return vh() / 2 + 60;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        float ui = ui();
        g.pose().pushPose();
        g.pose().scale(ui, ui, 1);
        mouseX = (int) (mouseX / ui);
        mouseY = (int) (mouseY / ui);
        int w = vw(), h = vh(), cy = h / 2 - 10;
        float t = (System.currentTimeMillis() - opened) / 1000F;
        // the band, fading in from the middle outwards
        int half = (int) (w / 2 * Math.min(1F, t * 2.5F));
        g.fillGradient(w / 2 - half, cy - 56, w / 2 + half, cy + 52, 0xE0101A2C, 0xE0182A40);
        g.fill(w / 2 - half, cy - 57, w / 2 + half, cy - 56, 0xFFE8D29A);
        g.fill(w / 2 - half, cy + 52, w / 2 + half, cy + 53, 0xFFE8D29A);

        String title = Component.translatable("gui.awakening_system.abyss_result.title").getString();
        g.pose().pushPose();
        g.pose().translate(w / 2F, cy - 46, 0);
        g.pose().scale(1.6F, 1.6F, 1);
        g.drawCenteredString(this.font, title, 0, 0, 0xFFFFE6A8);
        g.pose().popPose();
        String sub = Component.translatable("gui.awakening_system.abyss_result.chamber", r.floor(), r.chamber()).getString();
        g.drawCenteredString(this.font, sub, w / 2, cy - 26, 0xFFFFFFFF);

        // three big stars, each lighting up in turn
        for (int i = 0; i < 3; i++) {
            float appear = Math.max(0, Math.min(1, (t - 0.35F - i * 0.3F) * 4));
            boolean won = i < r.stars();
            float s = 2.6F * (won ? (1 + (1 - appear) * 0.8F) : 1);
            int col = won ? (((int) (255 * appear)) << 24 | 0xFFE27A) : 0xFF4A5568;
            if (won && appear <= 0) continue;
            g.pose().pushPose();
            g.pose().translate(w / 2F + (i - 1) * 34, cy + 2, 0);
            g.pose().scale(s, s, 1);
            g.drawCenteredString(this.font, "★", 0, -4, col);
            g.pose().popPose();
        }
        int secs = r.secondsLeft();
        String time = Component.translatable("gui.awakening_system.abyss_result.time_left", String.format("%d:%02d", secs / 60, secs % 60)).getString();
        g.drawCenteredString(this.font, time, w / 2, cy + 26, 0xFFFFFFFF);
        // the floor so far: a row of nine little stars, three per chamber
        StringBuilder row = new StringBuilder();
        for (int c = 0; c < 3; c++) {
            for (int s = 0; s < 3; s++) row.append(s < r.floorStars()[c] ? "★" : "☆");
            if (c < 2) row.append("  ");
        }
        g.drawCenteredString(this.font, row.toString(), w / 2, cy + 38, 0xFFE8D29A);

        // the buttons
        String[] l = labels();
        int[] x = xs();
        int by = buttonsY();
        for (int i = 0; i < l.length; i++) {
            boolean hover = mouseX >= x[i] && mouseX < x[i] + bw(l[i]) && mouseY >= by && mouseY < by + 22;
            int fill = i == 0 ? (hover ? 0xFFF2D58A : 0xFFE0BE6A) : (hover ? 0xFF4A5A70 : 0xFF2E3A4C);
            g.fill(x[i], by, x[i] + bw(l[i]), by + 22, fill);
            g.renderOutline(x[i], by, bw(l[i]), 22, i == 0 ? 0xFFFFF2C8 : 0xFFB8C4D4);
            // dark text on the gold button, white with a shadow on the others
            if (i == 0) g.drawString(this.font, l[i], x[i] + (bw(l[i]) - this.font.width(l[i])) / 2, by + 7, 0xFF1E1608, false);
            else g.drawCenteredString(this.font, l[i], x[i] + bw(l[i]) / 2, by + 7, 0xFFFFFFFF);
        }
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX /= ui();
        mouseY /= ui();
        String[] l = labels();
        int[] x = xs();
        int by = buttonsY();
        for (int i = 0; i < l.length; i++) {
            if (mouseX >= x[i] && mouseX < x[i] + bw(l[i]) && mouseY >= by && mouseY < by + 22) {
                choose(i == 0 ? AbyssChamberResult.NEXT : (i == 1 ? AbyssChamberResult.LEAVE : AbyssChamberResult.STAY));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void choose(int choice) {
        if (this.minecraft != null && this.minecraft.player != null)
            this.minecraft.player.playSound(choice == AbyssChamberResult.NEXT ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.2F);
        if (choice != AbyssChamberResult.STAY) PacketDistributor.sendToServer(new AbyssChamberResult.ChoicePayload(choice));
        this.onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }
}
