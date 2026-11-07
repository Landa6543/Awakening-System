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
                args[i - 1] = parts[i];
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

        // 2. Fondo del cuadro de diálogo (Cristal obsidiana oscuro con gradiente)
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xEE07121E);
        graphics.fill(boxX + 1, boxY + 1, boxX + boxW - 1, boxY + 4, 0x5000E5FF); // Reflejo superior sutil

        // Bordes de alta tecnología holográfica
        graphics.renderOutline(boxX, boxY, boxW, boxH, 0xA00099C0);
        graphics.renderOutline(boxX + 1, boxY + 1, boxW - 2, boxH - 2, 0x4000E5FF);

        // Esquinas resaltadas
        int corn = 8;
        graphics.fill(boxX, boxY, boxX + corn, boxY + 2, 0xFF00E5FF);
        graphics.fill(boxX, boxY, boxX + 2, boxY + corn, 0xFF00E5FF);
        graphics.fill(boxX + boxW - corn, boxY, boxX + boxW, boxY + 2, 0xFF00E5FF);
        graphics.fill(boxX + boxW - 2, boxY, boxX + boxW, boxY + corn, 0xFF00E5FF);

        // 3. Placa del Hablante (Speaker Badge)
        if (resolvedSpeaker != null && !resolvedSpeaker.isBlank()) {
            int badgeW = font.width(resolvedSpeaker) + 16;
            int badgeH = 14;
            int badgeX = boxX + 10;
            int badgeY = boxY - badgeH + 2;

            graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 0xF005111B);
            graphics.renderOutline(badgeX, badgeY, badgeW, badgeH, 0xC000D0FF);
            graphics.drawString(font, resolvedSpeaker, badgeX + 8, badgeY + 3, 0xFFFFFFFF, false);
        }

        // 4. Máquina de escribir del texto del diálogo
        String visibleText = getVisibleText();
        playVoiceBlipForNewText(visibleText);
        graphics.drawWordWrap(font, Component.literal(visibleText), boxX + 16, boxY + 14, boxW - 32, 0xFFF0F4F8);

        // 5. Flecha pulsante de continuación o avance si el texto ya terminó
        if (isTextComplete()) {
            if (payload.choices().isEmpty()) {
                float pulse = (float) Math.sin((System.currentTimeMillis() % 1000) / 1000.0 * Math.PI * 2) * 0.3F + 0.7F;
                int alpha = (int) (pulse * 255);
                int color = (alpha << 24) | 0x00E5FF;
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
        int maxTextWidth = boxW - 32;
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

            int bgColor = hovered ? 0xDD0E283D : 0xBB061320;
            int borderColor = hovered ? 0xFF00E5FF : 0x700088A8;

            graphics.fill(choiceX, y, choiceX + choiceW, y + CHOICE_MIN_H, bgColor);
            graphics.renderOutline(choiceX, y, choiceW, CHOICE_MIN_H, borderColor);

            String rawTxt = (i < resolvedChoices.size()) ? resolvedChoices.get(i) : choice.text();
            String choiceTxt = rawTxt.replaceAll("^(\u00a7[0-9a-fk-or])*\\[[0-9<>A-Za-z_-]+\\]\\s*", "")
                    .replaceAll("^(\u00a7[0-9a-fk-or])*\\[[0-9<>A-Za-z_-]+\\]\\s*", "");
            String numberBadge = (count > 1) ? ("§6[" + (i + 1) + "] ") : "";
            String text = (hovered ? "§b▶ §f§l" : "§7› ") + numberBadge + choiceTxt;
            graphics.drawString(font, text, choiceX + 10, y + 6, hovered ? 0xFFFFFFFF : 0xFFCCCCCC, false);
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
            int curY = totalH <= viewportH ? viewportBottom - totalH : viewportTop - choiceScrollOffset;

            for (int i = 0; i < choices.size(); i++) {
                int y = curY + i * itemH;
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
