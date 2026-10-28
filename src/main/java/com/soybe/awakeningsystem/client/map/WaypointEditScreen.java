package com.soybe.awakeningsystem.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.awt.Color;
import java.util.Random;

/**
 * Pantalla modal para crear y editar Waypoints estilo JourneyMap (New / Edit Waypoint).
 * Incluye campos Name, X, Z, Y, Color R/G/B con gran muestra rectangular en tiempo real,
 * código hexadecimal (#RRGGBB), 8 muestras de paleta rápida predefinidas, rueda cromática interactiva,
 * botones [Random Color], [Enabled: On/Off], [Reset], panel de selección de Dimensiones,
 * botones inferiores [Remove], [Save], [Cancel] y lectura de Current Location en el pie.
 */
public class WaypointEditScreen extends Screen {
    private final Screen parentScreen;
    private final Waypoint waypoint;
    private final boolean isNew;

    private EditBox nameInput;
    private EditBox xInput;
    private EditBox yInput;
    private EditBox zInput;

    private EditBox rInput;
    private EditBox gInput;
    private EditBox bInput;

    private int red = 0;
    private int green = 228;
    private int blue = 253;
    private float hue = 0.52F;
    private float saturation = 1.0F;
    private float brightness = 1.0F;

    private boolean enabled = true;
    private boolean showBeam = true;
    private boolean showLabel = true;
    private String selectedDimension;
    private boolean isUpdatingInputs = false;

    // Paleta de colores predefinidos ultra-vibrantes de alto impacto visual
    public static final int[] PRESET_COLORS = {
            0x00F5FF, // Cian Neón Eléctrico
            0x39FF14, // Verde Lima Neón
            0xFFFF00, // Amarillo Oro Solar
            0xFF6600, // Naranja Radiante
            0xFF0033, // Rojo Carmesí Puro
            0xFF007F, // Magenta Neón
            0xBF00FF, // Púrpura Eléctrico
            0xFFFFFF  // Blanco Radiante
    };

    // Rueda Cromática interactiva
    private static final int WHEEL_DIAMETER = 48;
    private static final int WHEEL_RADIUS = 24;
    private DynamicTexture wheelTexture;
    private ResourceLocation wheelTextureLoc;
    private boolean isDraggingWheel = false;

    private static final Random RNG = new Random();

    public WaypointEditScreen(Waypoint waypoint, boolean isNew, Screen parentScreen) {
        super(isNew ? Component.translatable("awakening_system.waypoint.new_title") : Component.translatable("awakening_system.waypoint.edit_title"));
        this.parentScreen = parentScreen;
        this.waypoint = waypoint;
        this.isNew = isNew;

        int c = waypoint.getColor();
        this.red = (c >> 16) & 0xFF;
        this.green = (c >> 8) & 0xFF;
        this.blue = c & 0xFF;
        syncHsvFromRgb();

        this.enabled = waypoint.isEnabled();
        this.showBeam = waypoint.isShowBeam();
        this.showLabel = waypoint.isShowLabel();
        this.selectedDimension = waypoint.getDimension() != null ? waypoint.getDimension() : "minecraft:overworld";
    }

    private void syncHsvFromRgb() {
        float[] hsv = Color.RGBtoHSB(red, green, blue, null);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.brightness = hsv[2];
    }

    private void syncRgbFromHsv() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
        updateRgbInputs();
    }

    private void updateRgbInputs() {
        isUpdatingInputs = true;
        try {
            if (rInput != null) rInput.setValue(String.valueOf(red));
            if (gInput != null) gInput.setValue(String.valueOf(green));
            if (bInput != null) bInput.setValue(String.valueOf(blue));
        } finally {
            isUpdatingInputs = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Neutralizado: El fondo se dibuja exclusivamente al inicio de render() antes del marco
        // para garantizar que ningún callback de Screen pinte capas oscuras sobre los colores
    }

    @Override
    public void renderBlurredBackground(float partialTick) {}

    @Override
    public void renderTransparentBackground(GuiGraphics g) {}

    @Override
    public void removed() {
        super.removed();
        if (this.wheelTexture != null) {
            this.wheelTexture.close();
            this.wheelTexture = null;
        }
        this.wheelTextureLoc = null;
    }

    private void generateWheelTexture() {
        if (this.wheelTextureLoc != null) return;

        NativeImage img = new NativeImage(WHEEL_DIAMETER, WHEEL_DIAMETER, false);
        double center = WHEEL_RADIUS;

        for (int py = 0; py < WHEEL_DIAMETER; py++) {
            double dy = py - center + 0.5D;
            for (int px = 0; px < WHEEL_DIAMETER; px++) {
                double dx = px - center + 0.5D;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist > WHEEL_RADIUS) {
                    img.setPixelRGBA(px, py, 0x00000000);
                } else {
                    float sat = (float) Math.min(1.0D, dist / WHEEL_RADIUS);
                    double angle = Math.atan2(dy, dx);
                    float h = (float) ((angle / (2.0D * Math.PI)) + 0.5D);
                    if (h < 0.0F) h += 1.0F;
                    if (h >= 1.0F) h -= 1.0F;

                    int rgb = Color.HSBtoRGB(h, sat, 1.0F);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    int a = 0xFF;
                    if (dist > WHEEL_RADIUS - 1.2D) {
                        float edge = (float) (WHEEL_RADIUS - dist) / 1.2F;
                        a = Mth.clamp((int) (255 * edge), 0, 255);
                    }

                    img.setPixelRGBA(px, py, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
        }

        this.wheelTexture = new DynamicTexture(img);
        this.wheelTexture.setFilter(false, false);
        this.wheelTextureLoc = Minecraft.getInstance().getTextureManager().register("waypoint_color_wheel", this.wheelTexture);
    }

    @Override
    protected void init() {
        super.init();
        generateWheelTexture();

        int dialogW = 416;
        int dialogH = 228;
        int left = (width - dialogW) / 2;
        int top = (height - dialogH) / 2 - 4;

        // 1. Name input
        this.nameInput = new EditBox(font, left + 14, top + 34, 204, 16, Component.translatable("awakening_system.waypoint.field.name"));
        this.nameInput.setValue(waypoint.getName());
        this.nameInput.setMaxLength(32);
        this.addWidget(nameInput);

        // 2. Coords X, Z, Y
        this.xInput = new EditBox(font, left + 26, top + 64, 54, 15, Component.literal("X"));
        this.xInput.setValue(String.valueOf(waypoint.getX()));
        this.addWidget(xInput);

        this.zInput = new EditBox(font, left + 96, top + 64, 54, 15, Component.literal("Z"));
        this.zInput.setValue(String.valueOf(waypoint.getZ()));
        this.addWidget(zInput);

        this.yInput = new EditBox(font, left + 166, top + 64, 42, 15, Component.literal("Y"));
        this.yInput.setValue(String.valueOf(waypoint.getY()));
        this.addWidget(yInput);

        // 3. Color R, G, B inputs
        int rgbW = 30;
        this.rInput = new EditBox(font, left + 26, top + 93, rgbW, 15, Component.literal("R"));
        this.rInput.setValue(String.valueOf(red));
        this.rInput.setResponder(t -> onRgbInputChanged());
        this.addWidget(rInput);

        this.gInput = new EditBox(font, left + 71, top + 93, rgbW, 15, Component.literal("G"));
        this.gInput.setValue(String.valueOf(green));
        this.gInput.setResponder(t -> onRgbInputChanged());
        this.addWidget(gInput);

        this.bInput = new EditBox(font, left + 116, top + 93, rgbW, 15, Component.literal("B"));
        this.bInput.setValue(String.valueOf(blue));
        this.bInput.setResponder(t -> onRgbInputChanged());
        this.addWidget(bInput);
    }

    private void onRgbInputChanged() {
        if (isUpdatingInputs) return;
        try {
            int nr = Integer.parseInt(rInput.getValue().trim());
            int ng = Integer.parseInt(gInput.getValue().trim());
            int nb = Integer.parseInt(bInput.getValue().trim());
            this.red = Mth.clamp(nr, 0, 255);
            this.green = Mth.clamp(ng, 0, 255);
            this.blue = Mth.clamp(nb, 0, 255);
            syncHsvFromRgb();
        } catch (Exception ignored) {}
    }

    private void randomizeColor() {
        // Generación de color 100% aleatoria a lo largo de todo el espectro cromático (0 a 360°)
        // con saturación y brillo óptimos para garantizar máxima visibilidad y vivacidad
        float h = RNG.nextFloat();
        float s = 0.80F + RNG.nextFloat() * 0.20F; // 80% - 100%
        float b = 0.90F + RNG.nextFloat() * 0.10F; // 90% - 100%
        int rgb = java.awt.Color.HSBtoRGB(h, s, b) & 0xFFFFFF;
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
        syncHsvFromRgb();
        updateRgbInputs();
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
    }

    private void resetToCurrent() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            xInput.setValue(String.valueOf(mc.player.getBlockX()));
            yInput.setValue(String.valueOf(mc.player.getBlockY()));
            zInput.setValue(String.valueOf(mc.player.getBlockZ()));
            selectedDimension = mc.player.level().dimension().location().toString();
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
        }
    }

    private void saveAndClose() {
        waypoint.setName(nameInput.getValue());
        try { waypoint.setX(Integer.parseInt(xInput.getValue().trim())); } catch (Exception ignored) {}
        try { waypoint.setY(Integer.parseInt(yInput.getValue().trim())); } catch (Exception ignored) {}
        try { waypoint.setZ(Integer.parseInt(zInput.getValue().trim())); } catch (Exception ignored) {}

        int rgb = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
        waypoint.setColor(rgb);
        waypoint.setEnabled(enabled);
        waypoint.setShowBeam(showBeam);
        waypoint.setShowLabel(showLabel);
        waypoint.setDimension(selectedDimension);

        if (isNew) {
            WaypointManager.addWaypoint(waypoint);
        } else {
            WaypointManager.updateWaypoint(waypoint);
        }

        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
        onClose();
    }

    private void deleteAndClose() {
        WaypointManager.removeWaypoint(waypoint.getId());
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 0.8F);
        onClose();
    }

    @Override
    public void onClose() {
        if (parentScreen != null) {
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

        // Fondo oscuro translúcido dibujado de forma atómica antes del diálogo (sin llamadas repetidas)
        g.fillGradient(0, 0, this.width, this.height, 0x85050C14, 0xA0050C14);
        Minecraft mc = Minecraft.getInstance();

        int dialogW = 416;
        int dialogH = 228;
        int left = (width - dialogW) / 2;
        int top = (height - dialogH) / 2 - 4;

        // Marco y Fondo
        g.fill(left - 1, top - 1, left + dialogW + 1, top + dialogH + 1, 0xFF4EE2EC);
        g.fill(left, top, left + dialogW, top + dialogH, 0xF508111A);
        g.fill(left, top, left + dialogW, top + 22, 0xFF10202E);

        // Título
        Component titleComp = isNew ? Component.translatable("awakening_system.waypoint.new_title") : Component.translatable("awakening_system.waypoint.edit_title");
        g.drawCenteredString(font, titleComp, left + dialogW / 2, top + 7, 0xFF4EE2EC);

        // ==========================================
        // SECCIÓN IZQUIERDA: Name, Location, Color
        // ==========================================
        // 1. Name label
        g.drawString(font, Component.translatable("awakening_system.waypoint.field.name"), left + 14, top + 24, 0xFF70E8FF);

        // 2. Location labels
        g.drawString(font, Component.translatable("awakening_system.waypoint.field.location"), left + 14, top + 53, 0xFF70E8FF);
        g.drawString(font, "X", left + 15, top + 67, 0xFFAAAAAA);
        g.drawString(font, "Z", left + 85, top + 67, 0xFFAAAAAA);
        g.drawString(font, "Y", left + 155, top + 67, 0xFFAAAAAA);

        // 3. Color labels & preview diamond
        g.drawString(font, Component.translatable("awakening_system.waypoint.field.color"), left + 14, top + 83, 0xFF70E8FF);
        g.drawString(font, "R", left + 15, top + 96, 0xFFAAAAAA);
        g.drawString(font, "G", left + 60, top + 96, 0xFFAAAAAA);
        g.drawString(font, "B", left + 105, top + 96, 0xFFAAAAAA);

        g.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Rectángulo de Color Táctico (estilo JourneyMap)
        int curRgb = (0xFF << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
        int previewX = left + 150;
        int previewY = top + 91;
        int previewW = 32;
        int previewH = 18;

        g.fill(previewX - 1, previewY - 1, previewX + previewW + 1, previewY + previewH + 1, 0xFF000000);
        g.fill(previewX, previewY, previewX + previewW, previewY + previewH, curRgb);
        g.renderOutline(previewX - 1, previewY - 1, previewW + 2, previewH + 2, 0xFF4EE2EC);
        g.drawCenteredString(font, "◆", previewX + previewW / 2, previewY + 5, 0xFFFFFFFF);

        // Rueda Cromática interactiva (48x48)
        int wheelLeft = left + 188;
        int wheelTop = top + 83;
        int wheelCenterX = wheelLeft + WHEEL_RADIUS;
        int wheelCenterY = wheelTop + WHEEL_RADIUS;

        g.fill(wheelLeft - 1, wheelTop - 1, wheelLeft + WHEEL_DIAMETER + 1, wheelTop + WHEEL_DIAMETER + 1, 0xFF000000);
        g.renderOutline(wheelLeft - 1, wheelTop - 1, WHEEL_DIAMETER + 2, WHEEL_DIAMETER + 2, 0x884EE2EC);

        if (this.wheelTextureLoc != null) {
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            g.blit(this.wheelTextureLoc, wheelLeft, wheelTop, WHEEL_DIAMETER, WHEEL_DIAMETER, 0.0F, 0.0F, WHEEL_DIAMETER, WHEEL_DIAMETER, WHEEL_DIAMETER, WHEEL_DIAMETER);
        }

        // Cursor táctico de alto contraste en la rueda (libre movimiento 2D 360 grados)
        double curAngle = (hue - 0.5D) * 2.0D * Math.PI;
        double curDist = saturation * (WHEEL_RADIUS - 1);
        int curX = wheelCenterX + (int) Math.round(Math.cos(curAngle) * curDist);
        int curY = wheelCenterY + (int) Math.round(Math.sin(curAngle) * curDist);
        g.renderOutline(curX - 3, curY - 3, 7, 7, 0xFF000000);
        g.renderOutline(curX - 2, curY - 2, 5, 5, 0xFFFFFFFF);
        g.fill(curX - 1, curY - 1, curX + 2, curY + 2, curRgb);

        // 8 Muestras rápidas de color estilo JourneyMap (a la izquierda de la rueda)
        int swatchY = top + 114;
        int swatchW = 19;
        int swatchH = 13;
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int sx = left + 14 + i * 21;
            int c = PRESET_COLORS[i] | 0xFF000000;
            boolean isCur = (curRgb & 0xFFFFFF) == PRESET_COLORS[i];
            boolean hov = mouseX >= sx && mouseX < sx + swatchW && mouseY >= swatchY && mouseY < swatchY + swatchH;

            // Base negra sólida para garantizar vivacidad absoluta y cero pérdida de saturación
            g.fill(sx - 1, swatchY - 1, sx + swatchW + 1, swatchY + swatchH + 1, 0xFF000000);
            g.fill(sx, swatchY, sx + swatchW, swatchY + swatchH, c);
            g.renderOutline(sx - 1, swatchY - 1, swatchW + 2, swatchH + 2, (hov || isCur) ? 0xFFFFFFFF : 0xFF4A657D);

            if (isCur) {
                int dotCol = (PRESET_COLORS[i] == 0xFFFFFF) ? 0xFF000000 : 0xFFFFFFFF;
                g.fill(sx + swatchW / 2 - 1, swatchY + swatchH / 2 - 1, sx + swatchW / 2 + 2, swatchY + swatchH / 2 + 2, dotCol);
            }
        }

        // Botones debajo de Color: [Random Color], [Reset Pos], [Enabled: On/Off] (top + 135, debajo de la rueda con cero solapamiento)
        int btnH = 16;
        int rBtnY = top + 135;

        int randBtnX = left + 14;
        int randBtnW = 74;
        renderButton(g, randBtnX, rBtnY, randBtnW, btnH, Component.translatable("awakening_system.waypoint.btn.random_color"), mouseX, mouseY, 0xFF70E8FF);

        int resetBtnX = left + 92;
        int resetBtnW = 50;
        renderButton(g, resetBtnX, rBtnY, resetBtnW, btnH, Component.translatable("awakening_system.waypoint.btn.reset"), mouseX, mouseY, 0xFFFFD700);

        int enBtnX = left + 146;
        int enBtnW = 78;
        Component enComp = Component.literal(Component.translatable("awakening_system.waypoint.field.enabled").getString() + ": ")
                .append(enabled ? Component.translatable("awakening_system.gui_config.toggle.on") : Component.translatable("awakening_system.gui_config.toggle.off"));
        renderButton(g, enBtnX, rBtnY, enBtnW, btnH, enComp, mouseX, mouseY, enabled ? 0xFF50FA7B : 0xFFFFAAAA);

        // ==========================================
        // SECCIÓN DERECHA: Dimensiones (JourneyMap Dimensions List)
        // ==========================================
        int rightX = left + 252;
        g.drawString(font, Component.translatable("awakening_system.waypoint.field.dimensions"), rightX, top + 24, 0xFF70E8FF);

        String[] dimKeys = {"ALL", "OVERWORLD", "NETHER", "END"};
        Component[] dimLabels = {
                Component.translatable("awakening_system.waypoint.dim.all"),
                Component.translatable("awakening_system.waypoint.dim.overworld"),
                Component.translatable("awakening_system.waypoint.dim.nether"),
                Component.translatable("awakening_system.waypoint.dim.end")
        };

        int dimBtnW = 144;
        int dimBtnH = 16;
        for (int i = 0; i < dimKeys.length; i++) {
            int dy = top + 36 + i * 20;
            String key = dimKeys[i];
            boolean isSel = switch (key) {
                case "ALL" -> selectedDimension.equalsIgnoreCase("all");
                case "OVERWORLD" -> selectedDimension.contains("overworld");
                case "NETHER" -> selectedDimension.contains("nether");
                case "END" -> selectedDimension.contains("end");
                default -> false;
            };

            Component statusText = Component.literal(dimLabels[i].getString() + ": ")
                    .append(isSel ? Component.translatable("awakening_system.gui_config.toggle.on") : Component.translatable("awakening_system.gui_config.toggle.off"));

            boolean dimHov = mouseX >= rightX && mouseX < rightX + dimBtnW && mouseY >= dy && mouseY < dy + dimBtnH;
            g.fill(rightX, dy, rightX + dimBtnW, dy + dimBtnH, isSel ? (dimHov ? 0xDD1B4B32 : 0xAA123222) : (dimHov ? 0xAA2B4055 : 0x77162533));
            g.renderOutline(rightX, dy, dimBtnW, dimBtnH, isSel ? 0xFF50FA7B : 0xFF2A4255);
            g.drawCenteredString(font, statusText, rightX + dimBtnW / 2, dy + 4, isSel ? 0xFF50FA7B : 0xFFAAAAAA);
        }

        // Toggles de Visibilidad: Beam y Label (JourneyMap style)
        int toggleY = top + 120;
        int tBtnW = 70;
        int tBtnH = 15;

        // Botón Haz de Luz (Beam)
        Component beamComp = Component.literal("Beam: ")
                .append(showBeam ? Component.translatable("awakening_system.gui_config.toggle.on") : Component.translatable("awakening_system.gui_config.toggle.off"));
        renderButton(g, rightX, toggleY, tBtnW, tBtnH, beamComp, mouseX, mouseY, showBeam ? 0xFF00E5FF : 0xFFFFAAAA);

        // Botón Etiqueta 3D (Label)
        Component labelComp = Component.literal("Label: ")
                .append(showLabel ? Component.translatable("awakening_system.gui_config.toggle.on") : Component.translatable("awakening_system.gui_config.toggle.off"));
        renderButton(g, rightX + 74, toggleY, tBtnW, tBtnH, labelComp, mouseX, mouseY, showLabel ? 0xFF50FA7B : 0xFFFFAAAA);

        // ==========================================
        // PIE DE PÁGINA: Current Location (X, Z, Y) y Botones de Acción
        // ==========================================
        int footerY = top + dialogH - 42;
        g.fill(left, footerY, left + dialogW, footerY + 1, 0xFF2A3D4D);

        // Coordenadas actuales del jugador
        if (mc.player != null) {
            String curLoc = String.format("Current Location:  x: %d,  z: %d,  y: %d", mc.player.getBlockX(), mc.player.getBlockZ(), mc.player.getBlockY());
            g.drawString(font, curLoc, left + 14, footerY + 5, 0xFFAAAAAA);
        }

        // Botones inferiores: [Remove] [Save] [Cancel]
        int bY = top + dialogH - 22;
        int actBtnH = 16;

        if (!isNew) {
            int bW = 86;
            int remX = left + 14;
            renderButton(g, remX, bY, bW, actBtnH, Component.translatable("awakening_system.waypoint.btn.delete"), mouseX, mouseY, 0xFFFF6666);

            int saveX = left + dialogW / 2 - bW / 2;
            renderButton(g, saveX, bY, bW, actBtnH, Component.translatable("awakening_system.waypoint.btn.save"), mouseX, mouseY, 0xFF50FA7B);

            int cancelX = left + dialogW - 14 - bW;
            renderButton(g, cancelX, bY, bW, actBtnH, Component.translatable("awakening_system.waypoint.btn.cancel"), mouseX, mouseY, 0xFFCCCCCC);
        } else {
            int bW = 120;
            int saveX = left + 50;
            renderButton(g, saveX, bY, bW, actBtnH, Component.translatable("awakening_system.waypoint.btn.save"), mouseX, mouseY, 0xFF50FA7B);

            int cancelX = left + dialogW - 50 - bW;
            renderButton(g, cancelX, bY, bW, actBtnH, Component.translatable("awakening_system.waypoint.btn.cancel"), mouseX, mouseY, 0xFFCCCCCC);
        }

        // Renderizar inputs
        nameInput.render(g, mouseX, mouseY, partialTick);
        xInput.render(g, mouseX, mouseY, partialTick);
        zInput.render(g, mouseX, mouseY, partialTick);
        yInput.render(g, mouseX, mouseY, partialTick);
        rInput.render(g, mouseX, mouseY, partialTick);
        gInput.render(g, mouseX, mouseY, partialTick);
        bInput.render(g, mouseX, mouseY, partialTick);

        g.flush();
    }

    private void renderButton(GuiGraphics g, int x, int y, int w, int h, Component text, int mouseX, int mouseY, int textCol) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xDD2A4D66 : 0xAA102230);
        g.renderOutline(x, y, w, h, hov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(font, text, x + w / 2, y + 4, hov ? 0xFFFFFFFF : textCol);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int dialogW = 416;
            int dialogH = 228;
            int left = (width - dialogW) / 2;
            int top = (height - dialogH) / 2 - 4;

            // 1. Clic en Rueda Cromática (radio 24, centrada en left + 212, top + 107)
            int wheelLeft = left + 188;
            int wheelTop = top + 83;
            int wheelCenterX = wheelLeft + WHEEL_RADIUS;
            int wheelCenterY = wheelTop + WHEEL_RADIUS;
            double dx = mouseX - wheelCenterX;
            double dy = mouseY - wheelCenterY;
            if (dx * dx + dy * dy <= (WHEEL_RADIUS + 3) * (WHEEL_RADIUS + 3)) {
                isDraggingWheel = true;
                updateHsvFromWheel(dx, dy);
                return true;
            }

            // 2. Clic en muestras de color predefinidas
            int swatchY = top + 114;
            int swatchW = 19;
            int swatchH = 13;
            for (int i = 0; i < PRESET_COLORS.length; i++) {
                int sx = left + 14 + i * 21;
                if (mouseX >= sx && mouseX < sx + swatchW && mouseY >= swatchY && mouseY < swatchY + swatchH) {
                    int rgb = PRESET_COLORS[i];
                    this.red = (rgb >> 16) & 0xFF;
                    this.green = (rgb >> 8) & 0xFF;
                    this.blue = rgb & 0xFF;
                    syncHsvFromRgb();
                    updateRgbInputs();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
            }

            // 3. Clic en fila de botones debajo del color (top + 135)
            int btnH = 16;
            int rBtnY = top + 135;

            // [Random Color]
            int randBtnX = left + 14;
            int randBtnW = 74;
            if (mouseX >= randBtnX && mouseX < randBtnX + randBtnW && mouseY >= rBtnY && mouseY < rBtnY + btnH) {
                randomizeColor();
                return true;
            }

            // [Reset Pos]
            int resetBtnX = left + 92;
            int resetBtnW = 50;
            if (mouseX >= resetBtnX && mouseX < resetBtnX + resetBtnW && mouseY >= rBtnY && mouseY < rBtnY + btnH) {
                resetToCurrent();
                return true;
            }

            // [Enabled: On/Off]
            int enBtnX = left + 146;
            int enBtnW = 78;
            if (mouseX >= enBtnX && mouseX < enBtnX + enBtnW && mouseY >= rBtnY && mouseY < rBtnY + btnH) {
                enabled = !enabled;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, enabled ? 1.2F : 0.8F);
                return true;
            }

            // 6. Clic en Dimensiones
            int rightX = left + 252;
            int dimBtnW = 144;
            int dimBtnH = 16;
            String[] dimKeys = {"ALL", "OVERWORLD", "NETHER", "END"};
            String[] dimVals = {"all", "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};
            for (int i = 0; i < dimKeys.length; i++) {
                int dyPos = top + 36 + i * 20;
                if (mouseX >= rightX && mouseX < rightX + dimBtnW && mouseY >= dyPos && mouseY < dyPos + dimBtnH) {
                    selectedDimension = dimVals[i];
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                    return true;
                }
            }

            // 6.1 Clic en Toggles de Visibilidad (Beam y Label)
            int toggleY = top + 120;
            int tBtnW = 70;
            int tBtnH = 15;
            if (mouseX >= rightX && mouseX < rightX + tBtnW && mouseY >= toggleY && mouseY < toggleY + tBtnH) {
                showBeam = !showBeam;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, showBeam ? 1.2F : 0.8F);
                return true;
            }
            if (mouseX >= rightX + 74 && mouseX < rightX + 74 + tBtnW && mouseY >= toggleY && mouseY < toggleY + tBtnH) {
                showLabel = !showLabel;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, showLabel ? 1.2F : 0.8F);
                return true;
            }

            // 7. Clic en Botones Inferiores: [Remove], [Save], [Cancel]
            int bY = top + dialogH - 22;
            int actBtnH = 16;

            if (!isNew) {
                int bW = 86;
                int remX = left + 14;
                if (mouseX >= remX && mouseX < remX + bW && mouseY >= bY && mouseY < bY + actBtnH) {
                    deleteAndClose();
                    return true;
                }
                int saveX = left + dialogW / 2 - bW / 2;
                if (mouseX >= saveX && mouseX < saveX + bW && mouseY >= bY && mouseY < bY + actBtnH) {
                    saveAndClose();
                    return true;
                }
                int cancelX = left + dialogW - 14 - bW;
                if (mouseX >= cancelX && mouseX < cancelX + bW && mouseY >= bY && mouseY < bY + actBtnH) {
                    onClose();
                    return true;
                }
            } else {
                int bW = 120;
                int saveX = left + 50;
                if (mouseX >= saveX && mouseX < saveX + bW && mouseY >= bY && mouseY < bY + actBtnH) {
                    saveAndClose();
                    return true;
                }
                int cancelX = left + dialogW - 50 - bW;
                if (mouseX >= cancelX && mouseX < cancelX + bW && mouseY >= bY && mouseY < bY + actBtnH) {
                    onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateHsvFromWheel(double dx, double dy) {
        float sat = (float) Math.min(1.0D, Math.sqrt(dx * dx + dy * dy) / WHEEL_RADIUS);
        double angle = Math.atan2(dy, dx);
        float h = (float) ((angle / (2.0D * Math.PI)) + 0.5D);
        if (h < 0.0F) h += 1.0F;
        if (h >= 1.0F) h -= 1.0F;
        this.hue = h;
        this.saturation = sat;
        this.brightness = 1.0F;
        syncRgbFromHsv();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && isDraggingWheel) {
            int dialogW = 416;
            int dialogH = 228;
            int left = (width - dialogW) / 2;
            int top = (height - dialogH) / 2 - 4;
            int wheelCenterX = left + 188 + WHEEL_RADIUS;
            int wheelCenterY = top + 83 + WHEEL_RADIUS;
            double dx = mouseX - wheelCenterX;
            double dy = mouseY - wheelCenterY;
            updateHsvFromWheel(dx, dy);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingWheel = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
