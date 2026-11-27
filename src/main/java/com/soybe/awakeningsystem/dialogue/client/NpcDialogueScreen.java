package com.soybe.awakeningsystem.dialogue.client;

import com.soybe.awakeningsystem.dialogue.DialogueChoice;
import com.soybe.awakeningsystem.dialogue.network.CloseDialoguePayload;
import com.soybe.awakeningsystem.dialogue.network.DismissDialoguePayload;
import com.soybe.awakeningsystem.dialogue.network.OpenDialoguePayload;
import com.soybe.awakeningsystem.dialogue.network.SelectDialogueChoicePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pantalla de diálogo interactiva cinematográfica estilo RPG / Manhwa (Solo Leveling).
 * Inspirada en la arquitectura de IvyTheDragonMerchant con efectos de cámara suaves,
 * máquina de escribir por caracteres, blips de voz sonoros y opciones de ramificación.
 */
@OnlyIn(Dist.CLIENT)
public class NpcDialogueScreen extends Screen {

    private static final long SLIDE_IN_DURATION_MS = 250L;
    private static final long TEXT_START_DELAY_MS = 100L;
    private static final long TYPE_CHAR_INTERVAL_MS = 24L;
    private static final long VOICE_BLIP_INTERVAL_MS = 64L;
    private static final long CAMERA_TURN_DURATION_MS = 260L;

    private static final int CHOICE_MIN_H = 18;
    private static final int CHOICE_GAP = 3;

    private int getMaxVisibleChoices(int count, int viewportBottom) {
        int itemH = CHOICE_MIN_H + CHOICE_GAP;
        int availableH = Math.max(itemH, viewportBottom - 20);
        int maxPossible = Math.max(5, availableH / itemH);
        return Math.min(count, maxPossible);
    }

    private OpenDialoguePayload payload;
    private String resolvedSpeaker = "";
    private String resolvedText = "";
    private final List<String> resolvedChoices = new java.util.ArrayList<>();

    private final long boxStartTime;
    private long textStartTime;
    private boolean textSkipped;
    private int choiceScrollOffset;
    private int lastBlipCodePoints;
    private long lastVoiceBlipTime;

    // Control de giro de cámara suave hacia el interlocutor
    private boolean cameraTurnStarted;
    private boolean cameraTurnActive;
    private long cameraTurnStartTime;
    private float cameraStartYaw;
    private float cameraStartPitch;
    private float cameraTargetYaw;
    private float cameraTargetPitch;

    public static String resolveTranslatable(String input) {
        if (input == null || input.isEmpty()) return "";
        if (input.contains("|")) {
            String[] parts = input.split("\\|", -1);
            String key = parts[0];
            Object[] args = new Object[parts.length - 1];
            for (int i = 1; i < parts.length; i++) {
                // an argument that is itself a name key (an item, a block) is shown translated
                String a = parts[i];
                args[i - 1] = a.startsWith("item.") || a.startsWith("block.") || a.startsWith("awakening_system.") ? Component.translatable(a) : a;
            }
            try {
                return Component.translatable(key, args).getString();
            } catch (Throwable t) {
                return key;
            }
        }
        if (input.startsWith("awakening_system.") || input.startsWith("entity.") || input.startsWith("item.") || input.startsWith("block.")) {
            try {
                return Component.translatable(input).getString();
            } catch (Throwable t) {
                return input;
            }
        }
        return input;
    }

    public NpcDialogueScreen(OpenDialoguePayload payload) {
        super(Component.literal("NPC Dialogue"));
        this.payload = payload;
        long now = System.currentTimeMillis();
        this.boxStartTime = now;
        updateResolvedStrings();
        resetTextAnimation(now);
    }

    public void updatePayload(OpenDialoguePayload newPayload) {
        this.payload = newPayload;
        updateResolvedStrings();
        resetTextAnimation(System.currentTimeMillis());
    }

    private void updateResolvedStrings() {
        this.resolvedSpeaker = payload != null ? resolveTranslatable(payload.speaker()) : "";
        this.resolvedText = payload != null ? resolveTranslatable(payload.text()) : "";
        this.resolvedChoices.clear();
        if (payload != null && payload.choices() != null) {
            for (DialogueChoice c : payload.choices()) {
                this.resolvedChoices.add(resolveTranslatable(c.text()));
            }
        }
        // the light looks (parchment, SAO) cannot show colour codes made for a dark box: plain text there
        if (look() != Look.SYSTEM) {
            this.resolvedSpeaker = plain(resolvedSpeaker);
            this.resolvedText = plain(resolvedText);
            this.resolvedChoices.replaceAll(NpcDialogueScreen::plain);
        }
    }

    // ── looks: the System's dark glass, the town's parchment (RPG) and the SAO style ──

    enum Look { SYSTEM, RPG, SAO }

    private Look look() {
        // the town's people always talk in their own RPG look; everyone else follows the SAO style when it is on
        if (payload != null && ("town_npc".equalsIgnoreCase(payload.dialogueId()) || "town_smith".equalsIgnoreCase(payload.dialogueId()))) return Look.RPG;
        if (com.soybe.awakeningsystem.client.sao.UiStyle.fullDive()) return Look.SAO;
        return Look.SYSTEM;
    }

    private static String plain(String s) {
        String p = net.minecraft.ChatFormatting.stripFormatting(s);
        return p == null ? "" : p;
    }

    private int textColour() {
        return switch (look()) {
            case RPG -> 0xFF3A2814;
            case SAO -> 0xFF484848;
            default -> 0xFFF0F4F8;
        };
    }

    /** The box behind the words, in the current look. */
    private void drawBox(GuiGraphics g, int x, int y, int w, int h) {
        switch (look()) {
            case RPG -> {
                g.fill(x - 3, y - 3, x + w + 3, y + h + 3, 0xFF2A1A0E);              // dark wood frame
                g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xFF6A4424);
                g.fillGradient(x, y, x + w, y + h, 0xFFEEDDB0, 0xFFD8C08A);         // parchment
                g.renderOutline(x + 3, y + 3, w - 6, h - 6, 0x608A6A3A);
                for (int[] c : new int[][]{{x - 3, y - 3}, {x + w - 2, y - 3}, {x - 3, y + h - 2}, {x + w - 2, y + h - 2}})
                    g.fill(c[0], c[1], c[0] + 5, c[1] + 5, 0xFFD8A840);               // brass corners
            }
            case SAO -> {
                g.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x30000000);
                g.fillGradient(x, y, x + w, y + h, 0xF6FFFFFF, 0xF0E8E8EC);
                g.fill(x, y, x + w, y + 2, 0xFFEDA500);
                g.fill(x, y + h - 1, x + w, y + h, 0xFFC9C9CE);
            }
            default -> {
                g.fill(x, y, x + w, y + h, 0xEE07121E);
                g.fill(x + 1, y + 1, x + w - 1, y + 4, 0x5000E5FF);
                g.renderOutline(x, y, w, h, 0xA00099C0);
                g.renderOutline(x + 1, y + 1, w - 2, h - 2, 0x4000E5FF);
                int corn = 8;
                g.fill(x, y, x + corn, y + 2, 0xFF00E5FF);
                g.fill(x, y, x + 2, y + corn, 0xFF00E5FF);
                g.fill(x + w - corn, y, x + w, y + 2, 0xFF00E5FF);
                g.fill(x + w - 2, y, x + w, y + corn, 0xFF00E5FF);
            }
        }
    }

    /** The speaker's name plate above the box, in the current look. */
    private void drawSpeaker(GuiGraphics g, int boxX, int boxY) {
        if (resolvedSpeaker == null || resolvedSpeaker.isBlank()) return;
        int bw = font.width(resolvedSpeaker) + 16, bh = 14, bx = boxX + (look() == Look.RPG ? 56 : 10), by = boxY - bh + 2;
        switch (look()) {
            case RPG -> {
                g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF2A1A0E);
                g.fillGradient(bx, by, bx + bw, by + bh, 0xFF7A5030, 0xFF5A3A20);
                g.drawString(font, resolvedSpeaker, bx + 8, by + 3, 0xFFFFDE90, false);
            }
            case SAO -> {
                g.fillGradient(bx, by, bx + bw, by + bh, 0xFFF7B52A, 0xFFE29400);
                g.drawString(font, resolvedSpeaker, bx + 8, by + 3, 0xFFFFFFFF, false);
            }
            default -> {
                g.fill(bx, by, bx + bw, by + bh, 0xF005111B);
                g.renderOutline(bx, by, bw, bh, 0xC000D0FF);
                g.drawString(font, resolvedSpeaker, bx + 8, by + 3, 0xFFFFFFFF, false);
            }
        }
    }

    /** RPG look: a portrait of the speaker's face, cut from their skin, in a wooden frame at the box's left. */
    private void drawPortrait(GuiGraphics g, int boxX, int boxY) {
        if (look() != Look.RPG || minecraft == null || minecraft.level == null) return;
        Entity e = minecraft.level.getEntity(payload.entityId());
        int px = boxX + 8, py = boxY + 8, s = 40;
        g.fill(px - 2, py - 2, px + s + 2, py + s + 2, 0xFF2A1A0E);
        g.fill(px - 1, py - 1, px + s + 1, py + s + 1, 0xFF8A6034);
        g.fill(px, py, px + s, py + s, 0xFF4A3A2A);
        if (e != null) {
            var renderer = minecraft.getEntityRenderDispatcher().getRenderer(e);
            @SuppressWarnings("unchecked")
            net.minecraft.resources.ResourceLocation tex = ((net.minecraft.client.renderer.entity.EntityRenderer<Entity>) renderer).getTextureLocation(e);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            // face (8,8)-(16,16) and the hat layer over it, from a 64x64 skin
            g.blit(tex, px, py, s, s, 8, 8, 8, 8, 64, 64);
            g.blit(tex, px, py, s, s, 40, 8, 8, 8, 64, 64);
        }
    }

    private int textLeft(int boxX) {
        return look() == Look.RPG ? boxX + 58 : boxX + 16;
    }

    private void resetTextAnimation(long now) {
        this.textStartTime = now;
        this.textSkipped = false;
        this.choiceScrollOffset = 0;
        this.lastBlipCodePoints = 0;
        this.lastVoiceBlipTime = 0L;
    }

    @Override
    protected void init() {
        super.init();
        beginCameraTurnToSpeaker();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // No oscurecer ni desenfocar el juego para mantener una experiencia inmersiva continua
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateCameraTurn();

        int boxW = Math.min(540, width - 40);
        int boxH = calculateBoxHeight(boxW);
        int boxX = (width - boxW) / 2;
        int boxY = getBoxY(boxH);

        // 1. Renderizar opciones de elección centradas arriba del cuadro de diálogo
        renderChoices(graphics, boxX, boxW, boxY, mouseX, mouseY);

        // 2-3. The box and the speaker's plate, in the look of the moment (System, RPG parchment or SAO)
        drawBox(graphics, boxX, boxY, boxW, boxH);
        drawSpeaker(graphics, boxX, boxY);
        drawPortrait(graphics, boxX, boxY);

        // 4. Máquina de escribir del texto del diálogo
        String visibleText = getVisibleText();
        playVoiceBlipForNewText(visibleText);
        int tx = textLeft(boxX);
        graphics.drawWordWrap(font, Component.literal(visibleText), tx, boxY + 14, boxX + boxW - 16 - tx, textColour());

        // 5. Flecha pulsante de continuación o avance si el texto ya terminó
        if (isTextComplete()) {
            if (payload.choices().isEmpty()) {
                float pulse = (float) Math.sin((System.currentTimeMillis() % 1000) / 1000.0 * Math.PI * 2) * 0.3F + 0.7F;
                int alpha = (int) (pulse * 255);
                int color = (alpha << 24) | (look() == Look.RPG ? 0x6A4424 : look() == Look.SAO ? 0xC98700 : 0x00E5FF);
                String prompt = Component.translatable("awakening_system.dialogue.prompt_close").getString();
                graphics.drawString(font, prompt, boxX + boxW - font.width(prompt) - 14, boxY + boxH - 12, color, false);
            } else if (payload.choices().size() == 1 && (isContinueChoice(payload.choices().get(0)) || (resolvedChoices.size() == 1 && resolvedChoices.get(0).toLowerCase().contains("continu")))) {
                float pulse = (float) Math.sin((System.currentTimeMillis() % 800) / 800.0 * Math.PI * 2) * 0.3F + 0.7F;
                int alpha = (int) (pulse * 255);
                int color = (alpha << 24) | 0xFFD700;
                String prompt = Component.translatable("awakening_system.dialogue.prompt_advance").getString();
                graphics.drawString(font, prompt, boxX + boxW - font.width(prompt) - 14, boxY + boxH - 12, color, false);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private boolean isContinueChoice(DialogueChoice choice) {
        if (choice == null) return false;
        return "CONTINUE".equalsIgnoreCase(choice.action())
                || "continue".equalsIgnoreCase(choice.nextNodeId())
                || (choice.text() != null && choice.text().toLowerCase().contains("continu"));
    }

    private int calculateBoxHeight(int boxW) {
        if (font == null || resolvedText == null || resolvedText.isEmpty()) return 82;
        int maxTextWidth = boxW - 32 - (look() == Look.RPG ? 42 : 0);
        var lines = font.split(Component.literal(resolvedText), maxTextWidth);
        int textLineHeight = font.lineHeight + 3;
        int textBlockH = lines.size() * textLineHeight;
        int extraPromptH = (payload.choices().isEmpty() || (payload.choices().size() == 1 && isContinueChoice(payload.choices().get(0)))) ? 14 : 0;
        int minBoxH = 82;
        return Math.max(minBoxH, Math.min(height - 65, textBlockH + 32 + extraPromptH));
    }

    private int getChoiceWidth(int boxW) {
        if (resolvedChoices.isEmpty()) return 240;
        int maxTextW = 160;
        for (String cText : resolvedChoices) {
            String clean = cText.replaceFirst("^\\[[0-9<>A-Za-z_-]+\\]\\s*", "");
            maxTextW = Math.max(maxTextW, font.width(clean) + (resolvedChoices.size() > 1 ? 38 : 24));
        }
        int maxW = Math.max(280, boxW - 24);
        return Math.clamp(maxTextW, 220, maxW);
    }

    private int getChoiceX(int boxX, int boxW, int choiceW) {
        // Alinear a la izquierda sobre el cuadro de diálogo (zona despejada para no tapar al NPC)
        return boxX + 12;
    }

    private int getChoiceViewportBottom(int boxY) {
        boolean hasSpeaker = payload != null && payload.speaker() != null && !payload.speaker().isBlank();
        return hasSpeaker ? (boxY - 18) : (boxY - 8);
    }

    private void renderChoices(GuiGraphics graphics, int boxX, int boxW, int boxY, int mouseX, int mouseY) {
        List<DialogueChoice> choices = payload.choices();
        int count = choices.size();
        if (count == 0 || !isTextComplete()) return;

        int choiceW = getChoiceWidth(boxW);
        int choiceX = getChoiceX(boxX, boxW, choiceW);
        int viewportBottom = getChoiceViewportBottom(boxY);
        int maxVisible = getMaxVisibleChoices(count, viewportBottom);
        int itemH = CHOICE_MIN_H + CHOICE_GAP;
        int viewportH = maxVisible * itemH;
        int viewportTop = viewportBottom - viewportH;

        int totalH = count * itemH;
        int maxScroll = Math.max(0, totalH - viewportH);
        choiceScrollOffset = Math.max(0, Math.min(maxScroll, choiceScrollOffset));

        int curY = totalH <= viewportH ? viewportBottom - totalH : viewportTop - choiceScrollOffset;

        graphics.enableScissor(0, Math.max(0, viewportTop - 2), width, viewportBottom + 2);

        for (int i = 0; i < count; i++) {
            DialogueChoice choice = choices.get(i);
            int y = curY + i * itemH;
            if (y + CHOICE_MIN_H < viewportTop || y > viewportBottom) continue;

            boolean hovered = mouseX >= choiceX && mouseX <= choiceX + choiceW && mouseY >= y && mouseY <= y + CHOICE_MIN_H;

            if (look() == Look.SYSTEM) {
                int bgColor = hovered ? 0xDD0E283D : 0xBB061320;
                int borderColor = hovered ? 0xFF00E5FF : 0x700088A8;
                graphics.fill(choiceX, y, choiceX + choiceW, y + CHOICE_MIN_H, bgColor);
                graphics.renderOutline(choiceX, y, choiceW, CHOICE_MIN_H, borderColor);
            }

            String rawTxt = (i < resolvedChoices.size()) ? resolvedChoices.get(i) : choice.text();
            String choiceTxt = rawTxt.replaceAll("^(\u00a7[0-9a-fk-or])*\\[[0-9<>A-Za-z_-]+\\]\\s*", "")
                    .replaceAll("^(\u00a7[0-9a-fk-or])*\\[[0-9<>A-Za-z_-]+\\]\\s*", "");
            String numberBadge = (count > 1) ? ("§6[" + (i + 1) + "] ") : "";
            String text = (hovered ? "§b▶ §f§l" : "§7› ") + numberBadge + choiceTxt;
            if (look() == Look.SYSTEM) graphics.drawString(font, text, choiceX + 10, y + 6, hovered ? 0xFFFFFFFF : 0xFFCCCCCC, false);
            else if (look() == Look.RPG) {
                graphics.fill(choiceX - 1, y - 1, choiceX + choiceW + 1, y + CHOICE_MIN_H + 1, hovered ? 0xFFD8A840 : 0xFF2A1A0E);
                graphics.fillGradient(choiceX, y, choiceX + choiceW, y + CHOICE_MIN_H, hovered ? 0xFFF6E8C0 : 0xFFE6D2A0, hovered ? 0xFFE8D29A : 0xFFD2BA84);
                String n = (count > 1 ? (i + 1) + ". " : "") + choiceTxt;
                graphics.drawString(font, (hovered ? "» " : "") + n, choiceX + 10, y + 5, hovered ? 0xFF5A2A0A : 0xFF3A2814, false);
            } else {
                graphics.fill(choiceX + 1, y + 2, choiceX + choiceW + 1, y + CHOICE_MIN_H + 2, 0x26000000);
                graphics.fillGradient(choiceX, y, choiceX + choiceW, y + CHOICE_MIN_H, hovered ? 0xFFF7B52A : 0xFAFFFFFF, hovered ? 0xFFE29400 : 0xF0E6E6EA);
                if (!hovered) graphics.fill(choiceX, y, choiceX + 2, y + CHOICE_MIN_H, 0xFFEDA500);
                graphics.drawString(font, choiceTxt, choiceX + 10, y + 5, hovered ? 0xFFFFFFFF : 0xFF484848, false);
            }
        }

        graphics.disableScissor();

        // Indicadores de scroll si hay opciones fuera de vista
        if (maxScroll > 0) {
            if (choiceScrollOffset > 0) {
                graphics.drawCenteredString(font, "▲", choiceX + choiceW - 10, viewportTop - 8, 0xFF00E5FF);
            }
            if (choiceScrollOffset < maxScroll) {
                graphics.drawCenteredString(font, "▼", choiceX + choiceW - 10, viewportBottom + 2, 0xFF00E5FF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (!isTextComplete()) {
            skipText();
            return true;
        }

        int boxW = Math.min(540, width - 40);
        int boxH = calculateBoxHeight(boxW);
        int boxX = (width - boxW) / 2;
        int boxY = getBoxY(boxH);

        // Comprobar clics en las opciones
        List<DialogueChoice> choices = payload.choices();
        if (!choices.isEmpty()) {
            int choiceW = getChoiceWidth(boxW);
            int choiceX = getChoiceX(boxX, boxW, choiceW);
            int viewportBottom = getChoiceViewportBottom(boxY);
            int maxVisible = getMaxVisibleChoices(choices.size(), viewportBottom);
            int itemH = CHOICE_MIN_H + CHOICE_GAP;
            int viewportH = maxVisible * itemH;
            int viewportTop = viewportBottom - viewportH;

            int totalH = choices.size() * itemH;
            int maxScroll = Math.max(0, totalH - viewportH);
            choiceScrollOffset = Math.max(0, Math.min(maxScroll, choiceScrollOffset));
            int curY = totalH <= viewportH ? viewportBottom - totalH : viewportTop - choiceScrollOffset;

            for (int i = 0; i < choices.size(); i++) {
                int y = curY + i * itemH;
                if (y + CHOICE_MIN_H < viewportTop || y > viewportBottom) continue;
                if (mouseX >= choiceX && mouseX <= choiceX + choiceW && mouseY >= y && mouseY <= y + CHOICE_MIN_H) {
                    playButtonClickSound();
                    DialogueChoice clickedChoice = choices.get(i);
                    if ("CLOSE".equalsIgnoreCase(clickedChoice.action()) || "goodbye".equalsIgnoreCase(clickedChoice.nextNodeId())) {
                        onClose();
                        return true;
                    }
                    PacketDistributor.sendToServer(new SelectDialogueChoicePayload(
                            payload.entityId(), payload.dialogueId(), payload.nodeId(), i
                    ));
                    return true;
                }
            }
        } else {
            // Sin opciones -> clic para cerrar
            onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int count = payload.choices().size();
        int boxW = Math.min(540, width - 40);
        int boxX = (width - boxW) / 2;
        int boxH = calculateBoxHeight(boxW);
        int boxY = getBoxY(boxH);
        int viewportBottom = getChoiceViewportBottom(boxY);
        int maxVisible = getMaxVisibleChoices(count, viewportBottom);

        if (count > maxVisible && isTextComplete()) {
            int choiceW = getChoiceWidth(boxW);
            int choiceX = getChoiceX(boxX, boxW, choiceW);
            int itemH = CHOICE_MIN_H + CHOICE_GAP;
            int viewportH = maxVisible * itemH;
            int viewportTop = viewportBottom - viewportH;

            if (mouseX >= choiceX - 4 && mouseX <= choiceX + choiceW + 4 && mouseY >= viewportTop && mouseY <= viewportBottom) {
                int totalH = count * itemH;
                int maxScroll = Math.max(0, totalH - viewportH);
                choiceScrollOffset = Math.max(0, Math.min(maxScroll, choiceScrollOffset - (int) (scrollY * 18.0D)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isTextComplete()) {
            if (isAdvanceKey(keyCode)) {
                skipText();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        // Selección rápida con teclas numéricas 1-9
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            int idx = keyCode - GLFW.GLFW_KEY_1;
            if (idx >= 0 && idx < payload.choices().size()) {
                playButtonClickSound();
                DialogueChoice clickedChoice = payload.choices().get(idx);
                if ("CLOSE".equalsIgnoreCase(clickedChoice.action()) || "goodbye".equalsIgnoreCase(clickedChoice.nextNodeId())) {
                    onClose();
                    return true;
                }
                PacketDistributor.sendToServer(new SelectDialogueChoicePayload(
                        payload.entityId(), payload.dialogueId(), payload.nodeId(), idx
                ));
                return true;
            }
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            int idx = keyCode - GLFW.GLFW_KEY_KP_1;
            if (idx >= 0 && idx < payload.choices().size()) {
                playButtonClickSound();
                DialogueChoice clickedChoice = payload.choices().get(idx);
                if ("CLOSE".equalsIgnoreCase(clickedChoice.action()) || "goodbye".equalsIgnoreCase(clickedChoice.nextNodeId())) {
                    onClose();
                    return true;
                }
                PacketDistributor.sendToServer(new SelectDialogueChoicePayload(
                        payload.entityId(), payload.dialogueId(), payload.nodeId(), idx
                ));
                return true;
            }
        }

        if (isAdvanceKey(keyCode)) {
            if (payload.choices().isEmpty()) {
                onClose();
                return true;
            } else if (payload.choices().size() == 1) {
                playButtonClickSound();
                PacketDistributor.sendToServer(new SelectDialogueChoicePayload(
                        payload.entityId(), payload.dialogueId(), payload.nodeId(), 0
                ));
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null) {
            PacketDistributor.sendToServer(new DismissDialoguePayload(payload.entityId()));
        }
        super.onClose();
    }

    private static boolean isAdvanceKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_SPACE
                || keyCode == GLFW.GLFW_KEY_ENTER
                || keyCode == GLFW.GLFW_KEY_KP_ENTER;
    }

    private void skipText() {
        this.textSkipped = true;
        this.lastBlipCodePoints = resolvedText.length();
    }

    private boolean isTextComplete() {
        if (textSkipped) return true;
        long elapsed = Math.max(0L, System.currentTimeMillis() - textStartTime);
        long totalNeeded = TEXT_START_DELAY_MS + resolvedText.length() * TYPE_CHAR_INTERVAL_MS;
        return elapsed >= totalNeeded;
    }

    private String getVisibleText() {
        String full = resolvedText;
        if (textSkipped) return full;
        long elapsed = (System.currentTimeMillis() - textStartTime) - TEXT_START_DELAY_MS;
        if (elapsed <= 0) return "";
        int visibleCount = (int) (elapsed / TYPE_CHAR_INTERVAL_MS);
        if (visibleCount >= full.length()) return full;
        return full.substring(0, Math.max(0, visibleCount));
    }

    private void playVoiceBlipForNewText(String visibleText) {
        if (textSkipped || minecraft == null) return;
        long now = System.currentTimeMillis();
        if (now - lastVoiceBlipTime < VOICE_BLIP_INTERVAL_MS) return;

        int len = visibleText.length();
        if (len <= lastBlipCodePoints) return;

        char c = visibleText.charAt(len - 1);
        if (!Character.isWhitespace(c) && ".,!?;:()[]{}'\"".indexOf(c) < 0) {
            float pitch = (float) java.util.concurrent.ThreadLocalRandom.current().nextDouble(1.82D, 2.08D);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, 0.32F));
            lastVoiceBlipTime = now;
        }
        lastBlipCodePoints = len;
    }

    private void playButtonClickSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.6F));
        }
    }

    private int getBoxY(int boxH) {
        int targetY = height - boxH - 14;
        int startY = height + 10;
        long elapsed = Math.max(0L, System.currentTimeMillis() - boxStartTime);
        float t = Math.min(1.0F, (float) elapsed / SLIDE_IN_DURATION_MS);
        float eased = 1.0F - (float) Math.pow(1.0F - t, 3); // Ease out cubic
        return Math.round(startY + (targetY - startY) * eased);
    }

    // ==========================================
    // GIRO CINEMATOGRÁFICO DE CÁMARA
    // ==========================================

    private void beginCameraTurnToSpeaker() {
        if (cameraTurnStarted || minecraft == null || minecraft.player == null || minecraft.level == null) return;
        Entity speaker = minecraft.level.getEntity(payload.entityId());
        if (speaker == null) return;

        Vec3 eye = minecraft.player.getEyePosition();
        Vec3 target = speaker.getEyePosition().add(0.0D, -0.25D, 0.0D);
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        cameraStartYaw = minecraft.player.getYRot();
        cameraStartPitch = minecraft.player.getXRot();
        cameraTargetYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        cameraTargetPitch = (float) (-(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG));
        cameraTurnStartTime = System.currentTimeMillis();
        cameraTurnStarted = true;
        cameraTurnActive = true;
    }

    private void updateCameraTurn() {
        if (!cameraTurnStarted) {
            beginCameraTurnToSpeaker();
        }
        if (!cameraTurnActive || minecraft == null || minecraft.player == null) return;

        float t = Math.min(1.0F, (float) (System.currentTimeMillis() - cameraTurnStartTime) / CAMERA_TURN_DURATION_MS);
        float eased = t < 0.5F ? 4.0F * t * t * t : 1.0F - (float) Math.pow(-2.0F * t + 2.0F, 3) / 2.0F; // Ease in-out cubic

        float yaw = cameraStartYaw + Mth.wrapDegrees(cameraTargetYaw - cameraStartYaw) * eased;
        float pitch = Mth.lerp(eased, cameraStartPitch, cameraTargetPitch);

        minecraft.player.setYRot(yaw);
        minecraft.player.setYHeadRot(yaw);
        minecraft.player.setXRot(pitch);

        if (t >= 1.0F) {
            cameraTurnActive = false;
        }
    }
}
