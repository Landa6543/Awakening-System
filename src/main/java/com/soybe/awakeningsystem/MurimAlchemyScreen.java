package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de Alquimia de Murim con diseño limpio por columnas, estética oriental y refinamiento de píldoras.
 */
public class MurimAlchemyScreen extends Screen {
    private final BlockPos cauldronPos;
    private boolean hasHeat;

    private static final int GUI_WIDTH = 420;
    private static final int GUI_HEIGHT = 252;

    public record AlchemyRecipe(
            Component title,
            Component tierBadge,
            Component descLine1,
            Component descLine2,
            ItemStack in1,
            ItemStack in2,
            ItemStack output,
            int recipeIndex
    ) {}

    private final List<AlchemyRecipe> recipes = new ArrayList<>();

    public MurimAlchemyScreen(OpenMurimAlchemyPayload payload) {
        super(Component.translatable("awakening_system.alchemy.title"));
        this.cauldronPos = payload.pos();
        this.hasHeat = payload.hasHeat();
    }

    public void updateState(boolean hasHeat) {
        this.hasHeat = hasHeat;
    }

    @Override
    protected void init() {
        super.init();
        recipes.clear();

        // 1. Píldora de Limpieza de Médula
        recipes.add(new AlchemyRecipe(
                Component.translatable("awakening_system.alchemy.recipe.0.title"),
                Component.translatable("awakening_system.alchemy.recipe.0.tier"),
                Component.translatable("awakening_system.alchemy.recipe.0.desc1"),
                Component.translatable("awakening_system.alchemy.recipe.0.desc2"),
                new ItemStack(SystemItems.WILD_GINSENG.get()),
                new ItemStack(SystemItems.MANDRAKE_ROOT.get()),
                new ItemStack(SystemItems.MARROW_CLEANSING_PILL.get()),
                0
        ));

        // 2. Píldora del Dragón Naciente
        recipes.add(new AlchemyRecipe(
                Component.translatable("awakening_system.alchemy.recipe.1.title"),
                Component.translatable("awakening_system.alchemy.recipe.1.tier"),
                Component.translatable("awakening_system.alchemy.recipe.1.desc1"),
                Component.translatable("awakening_system.alchemy.recipe.1.desc2"),
                new ItemStack(SystemItems.SNOW_LOTUS.get()),
                new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get()),
                new ItemStack(SystemItems.RISING_DRAGON_PILL.get()),
                1
        ));

        // 3. Píldora de Ruptura de Reino
        recipes.add(new AlchemyRecipe(
                Component.translatable("awakening_system.alchemy.recipe.2.title"),
                Component.translatable("awakening_system.alchemy.recipe.2.tier"),
                Component.translatable("awakening_system.alchemy.recipe.2.desc1"),
                Component.translatable("awakening_system.alchemy.recipe.2.desc2"),
                new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get()),
                new ItemStack(SystemItems.SPIRIT_JADE.get()),
                new ItemStack(SystemItems.REALM_BREAKTHROUGH_PILL.get()),
                2
        ));

        // 4. Elixir de la Tortuga Negra
        recipes.add(new AlchemyRecipe(
                Component.translatable("awakening_system.alchemy.recipe.3.title"),
                Component.translatable("awakening_system.alchemy.recipe.3.tier"),
                Component.translatable("awakening_system.alchemy.recipe.3.desc1"),
                Component.translatable("awakening_system.alchemy.recipe.3.desc2"),
                new ItemStack(SystemItems.MANDRAKE_ROOT.get()),
                new ItemStack(SystemItems.ADAMANTITE_INGOT.get()),
                new ItemStack(SystemItems.BLACK_TORTOISE_PILL.get()),
                3
        ));

        // 5. Píldora del Escudo Celestial (Tribulación)
        recipes.add(new AlchemyRecipe(
                Component.translatable("awakening_system.alchemy.recipe.4.title"),
                Component.translatable("awakening_system.alchemy.recipe.4.tier"),
                Component.translatable("awakening_system.alchemy.recipe.4.desc1"),
                Component.translatable("awakening_system.alchemy.recipe.4.desc2"),
                new ItemStack(SystemItems.SNOW_LOTUS.get()),
                new ItemStack(SystemItems.SPIRIT_JADE.get()),
                new ItemStack(SystemItems.TRIBULATION_SHIELD_PILL.get()),
                4
        ));

        // 6. Píldora de Transformación del Alma
        recipes.add(new AlchemyRecipe(
                Component.translatable("awakening_system.alchemy.recipe.5.title"),
                Component.translatable("awakening_system.alchemy.recipe.5.tier"),
                Component.translatable("awakening_system.alchemy.recipe.5.desc1"),
                Component.translatable("awakening_system.alchemy.recipe.5.desc2"),
                new ItemStack(SystemItems.WILD_GINSENG.get()),
                new ItemStack(SystemItems.SPIRIT_JADE.get()),
                new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get()),
                5
        ));
    }

    private int currentPage = 0;
    private static final int RECIPES_PER_PAGE = 3;

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean hasIngredients(Player player, int recipeIndex) {
        if (player == null) return false;
        return switch (recipeIndex) {
            case 0 -> hasItem(player, SystemItems.WILD_GINSENG.get()) && hasItem(player, SystemItems.MANDRAKE_ROOT.get());
            case 1 -> hasItem(player, SystemItems.SNOW_LOTUS.get()) && hasItem(player, SystemItems.THOUSAND_YEAR_GINSENG.get());
            case 2 -> hasItem(player, SystemItems.THOUSAND_YEAR_GINSENG.get()) && (hasItem(player, SystemItems.SPIRIT_JADE.get()) || hasItem(player, SystemItems.SPIRIT_JADE_SHARD.get()));
            case 3 -> hasItem(player, SystemItems.MANDRAKE_ROOT.get()) && hasItem(player, SystemItems.ADAMANTITE_INGOT.get());
            case 4 -> hasItem(player, SystemItems.SNOW_LOTUS.get()) && (hasItem(player, SystemItems.SPIRIT_JADE.get()) || hasItem(player, SystemItems.SPIRIT_JADE_SHARD.get()));
            case 5 -> hasItem(player, SystemItems.WILD_GINSENG.get()) && (hasItem(player, SystemItems.SPIRIT_JADE.get()) || hasItem(player, SystemItems.SPIRIT_JADE_SHARD.get()));
            default -> false;
        };
    }

    private boolean hasItem(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) return true;
        }
        return false;
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 440.0F;
        float scaleY = (float) this.height / 270.0F;
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

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        guiGraphics.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            guiGraphics.pose().translate(cx, cy, 0.0F);
            guiGraphics.pose().scale(scale, scale, 1.0F);
            guiGraphics.pose().translate(-cx, -cy, 0.0F);
        }

        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        // Marco exterior e interior
        guiGraphics.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFF2A2012); // Borde oro antiguo
        guiGraphics.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFA091412); // Fondo Jade oscuro
        guiGraphics.fill(left + 3, top + 3, left + GUI_WIDTH - 3, top + GUI_HEIGHT - 3, 0xEE0B1A17);

        // Decoración de esquinas
        drawCornerOrnaments(guiGraphics, left, top, GUI_WIDTH, GUI_HEIGHT);

        // Encabezado
        guiGraphics.drawCenteredString(this.font, Component.translatable("awakening_system.alchemy.header"), this.width / 2, top + 8, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), this.width / 2, top + 19, 0xFF666666);
        guiGraphics.drawCenteredString(this.font, Component.translatable("awakening_system.alchemy.subtitle"), this.width / 2, top + 24, 0xFFA0D8EF);

        // Botón cerrar '✕'
        int closeX = left + GUI_WIDTH - 16;
        int closeY = top + 7;
        boolean hoverClose = mX >= closeX && mX <= closeX + 10 && mY >= closeY && mY <= closeY + 10;
        guiGraphics.drawString(this.font, hoverClose ? "§c✕" : "§7✕", closeX, closeY, 0xFFFFFFFF, false);

        // Banner de Estado del Fuego Espiritual
        int heatBannerY = top + 35;
        if (hasHeat) {
            guiGraphics.fill(left + 10, heatBannerY, left + GUI_WIDTH - 10, heatBannerY + 13, 0x551E5E2A);
            guiGraphics.fill(left + 10, heatBannerY, left + 13, heatBannerY + 13, 0xFF55FF55);
            guiGraphics.drawString(this.font, Component.translatable("awakening_system.alchemy.fire_active"), left + 17, heatBannerY + 3, 0xFFFFFFFF, false);
        } else {
            guiGraphics.fill(left + 10, heatBannerY, left + GUI_WIDTH - 10, heatBannerY + 13, 0x55661919);
            guiGraphics.fill(left + 10, heatBannerY, left + 13, heatBannerY + 13, 0xFFFF5555);
            guiGraphics.drawString(this.font, Component.translatable("awakening_system.alchemy.fire_inactive"), left + 17, heatBannerY + 3, 0xFFFFFFFF, false);
        }

        Player player = Minecraft.getInstance().player;
        ItemStack hoveredStack = ItemStack.EMPTY;
        Component hoveredTooltip = null;

        // Tarjetas de Recetas organizadas por columnas (Items | Info | Botón)
        int cardStartY = top + 50;
        int totalPages = Math.max(1, (int) Math.ceil((double) recipes.size() / RECIPES_PER_PAGE));
        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, recipes.size());

        for (int i = 0; i < endIndex - startIndex; i++) {
            AlchemyRecipe r = recipes.get(startIndex + i);
            int cardY = cardStartY + i * 50;
            int cardX = left + 10;
            int cardW = GUI_WIDTH - 20;
            int cardH = 46;

            boolean canCraft = hasIngredients(player, r.recipeIndex);

            // Fondo de la tarjeta
            int cardBg = canCraft && hasHeat ? 0x900E261F : 0x70081210;
            int cardBorder = canCraft && hasHeat ? 0xAA2ECC71 : 0x4033483E;
            guiGraphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, cardBg);
            drawBoxOutline(guiGraphics, cardX, cardY, cardW, cardH, cardBorder);

            // ==========================================
            // COLUMNA 1: DIAGRAMA DE TRANSMUTACIÓN (Ítems)
            // ==========================================
            int slotY = cardY + 14;

            // Slot Ingrediente 1
            int slot1X = cardX + 8;
            guiGraphics.fill(slot1X, slotY, slot1X + 18, slotY + 18, 0x90000000);
            drawBoxOutline(guiGraphics, slot1X, slotY, 18, 18, 0x6033483E);
            guiGraphics.renderItem(r.in1, slot1X + 1, slotY + 1);

            // Símbolo '+'
            guiGraphics.drawString(this.font, "§7+", slot1X + 21, slotY + 5, 0xFFFFFFFF, false);

            // Slot Ingrediente 2
            int slot2X = slot1X + 30;
            guiGraphics.fill(slot2X, slotY, slot2X + 18, slotY + 18, 0x90000000);
            drawBoxOutline(guiGraphics, slot2X, slotY, 18, 18, 0x6033483E);
            guiGraphics.renderItem(r.in2, slot2X + 1, slotY + 1);

            // Flecha '➔'
            guiGraphics.drawString(this.font, "§6➔", slot2X + 21, slotY + 5, 0xFFFFFFFF, false);

            // Slot Píldora Resultante
            int slotOutX = slot2X + 34;
            guiGraphics.fill(slotOutX, slotY, slotOutX + 18, slotY + 18, 0x902A2000);
            drawBoxOutline(guiGraphics, slotOutX, slotY, 18, 18, 0xAAFFD700);
            guiGraphics.renderItem(r.output, slotOutX + 1, slotY + 1);

            // Detección de hover para tooltips de ítems
            if (mX >= slot1X && mX <= slot1X + 18 && mY >= slotY && mY <= slotY + 18) {
                hoveredStack = r.in1;
            } else if (mX >= slot2X && mX <= slot2X + 18 && mY >= slotY && mY <= slotY + 18) {
                hoveredStack = r.in2;
            } else if (mX >= slotOutX && mX <= slotOutX + 18 && mY >= slotY && mY <= slotY + 18) {
                hoveredStack = r.output;
            }

            // ==========================================
            // COLUMNA 2: INFORMACIÓN Y EFECTOS
            // ==========================================
            int infoX = cardX + 96;
            int btnW = 68;
            int btnH = 22;
            int btnX = cardX + cardW - btnW - 8;
            int btnY = cardY + 12;
            int maxTextW = btnX - infoX - 8;

            String colorTag = switch (r.recipeIndex) {
                case 0 -> "§b✦ ";
                case 1 -> "§e✦ ";
                case 2 -> "§d✦ ";
                case 3 -> "§3✦ ";
                case 4 -> "§b✦ ";
                case 5 -> "§6✦ ";
                default -> "§f✦ ";
            };
            Component titleComp = Component.literal(colorTag).append(r.title).append(Component.literal(" ")).append(r.tierBadge);
            var tLines = this.font.split(titleComp, maxTextW);
            if (!tLines.isEmpty()) guiGraphics.drawString(this.font, tLines.get(0), infoX, cardY + 5, 0xFFFFFFFF, false);
            var d1Lines = this.font.split(r.descLine1, maxTextW);
            if (!d1Lines.isEmpty()) guiGraphics.drawString(this.font, d1Lines.get(0), infoX, cardY + 17, 0xFFAAAAAA, false);
            var d2Lines = this.font.split(r.descLine2, maxTextW);
            if (!d2Lines.isEmpty()) guiGraphics.drawString(this.font, d2Lines.get(0), infoX, cardY + 29, 0xFF88DDAA, false);

            // ==========================================
            // COLUMNA 3: BOTÓN DE REFINAR
            // ==========================================
            boolean hoverBtn = mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH;

            if (!hasHeat) {
                guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x70301515);
                drawBoxOutline(guiGraphics, btnX, btnY, btnW, btnH, 0x80602020);
                guiGraphics.drawCenteredString(this.font, Component.translatable("awakening_system.alchemy.no_fire"), btnX + btnW / 2, btnY + 7, 0xFF888888);
                if (hoverBtn) {
                    hoveredTooltip = Component.translatable("awakening_system.alchemy.no_fire_tooltip");
                }
            } else if (!canCraft) {
                guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x70202020);
                drawBoxOutline(guiGraphics, btnX, btnY, btnW, btnH, 0x80444444);
                guiGraphics.drawCenteredString(this.font, Component.translatable("awakening_system.alchemy.missing_items"), btnX + btnW / 2, btnY + 7, 0xFFAAAAAA);
                if (hoverBtn) {
                    hoveredTooltip = Component.translatable("awakening_system.alchemy.missing_items_tooltip");
                }
            } else {
                int btnBg = hoverBtn ? 0xDD1B5E20 : 0xAA0E3E15;
                int btnBorder = hoverBtn ? 0xFFFFD700 : 0xFF2ECC71;
                guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
                drawBoxOutline(guiGraphics, btnX, btnY, btnW, btnH, btnBorder);
                Component btnLabel = hoverBtn
                        ? Component.translatable("awakening_system.alchemy.refine_hover")
                        : Component.translatable("awakening_system.alchemy.refine");
                guiGraphics.drawCenteredString(this.font, btnLabel, btnX + btnW / 2, btnY + 7, 0xFFFFFFFF);
                if (hoverBtn) {
                    hoveredTooltip = Component.translatable("awakening_system.alchemy.refine_tooltip");
                }
            }
        }

        // Barra inferior de Navegación de Páginas
        int navY = top + 208;
        int btnNavW = 78;
        int btnNavH = 18;
        int prevBtnX = left + 14;
        int nextBtnX = left + GUI_WIDTH - 14 - btnNavW;

        boolean canPrev = currentPage > 0;
        boolean canNext = currentPage < totalPages - 1;

        boolean hoverPrev = mX >= prevBtnX && mX <= prevBtnX + btnNavW && mY >= navY && mY <= navY + btnNavH;
        boolean hoverNext = mX >= nextBtnX && mX <= nextBtnX + btnNavW && mY >= navY && mY <= navY + btnNavH;

        // Botón Anterior
        int prevBg = canPrev ? (hoverPrev ? 0xDD1B5E20 : 0xAA0E3E15) : 0x40202020;
        int prevBorder = canPrev ? (hoverPrev ? 0xFFFFD700 : 0xFF2ECC71) : 0x40444444;
        guiGraphics.fill(prevBtnX, navY, prevBtnX + btnNavW, navY + btnNavH, prevBg);
        drawBoxOutline(guiGraphics, prevBtnX, navY, btnNavW, btnNavH, prevBorder);
        guiGraphics.drawCenteredString(this.font, "§7◄ §fAnterior", prevBtnX + btnNavW / 2, navY + 5, canPrev ? 0xFFFFFFFF : 0xFF666666);

        // Indicador de Página
        String pageStr = "§ePágina §f" + (currentPage + 1) + " §7/ §f" + totalPages;
        guiGraphics.drawCenteredString(this.font, pageStr, left + GUI_WIDTH / 2, navY + 5, 0xFFFFFFFF);

        // Botón Siguiente
        int nextBg = canNext ? (hoverNext ? 0xDD1B5E20 : 0xAA0E3E15) : 0x40202020;
        int nextBorder = canNext ? (hoverNext ? 0xFFFFD700 : 0xFF2ECC71) : 0x40444444;
        guiGraphics.fill(nextBtnX, navY, nextBtnX + btnNavW, navY + btnNavH, nextBg);
        drawBoxOutline(guiGraphics, nextBtnX, navY, btnNavW, btnNavH, nextBorder);
        guiGraphics.drawCenteredString(this.font, "§fSiguiente §7►", nextBtnX + btnNavW / 2, navY + 5, canNext ? 0xFFFFFFFF : 0xFF666666);

        guiGraphics.pose().popPose();

        // Renderizado diferido de Tooltips
        if (!hoveredStack.isEmpty()) {
            guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        } else if (hoveredTooltip != null) {
            guiGraphics.renderTooltip(this.font, hoveredTooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int left = (this.width - GUI_WIDTH) / 2;
            int top = (this.height - GUI_HEIGHT) / 2;

            // Click en '✕'
            int closeX = left + GUI_WIDTH - 16;
            int closeY = top + 7;
            if (mouseX >= closeX && mouseX <= closeX + 10 && mouseY >= closeY && mouseY <= closeY + 10) {
                onClose();
                return true;
            }

            // Click en botones de Navegación de Páginas
            int navY = top + 208;
            int btnNavW = 78;
            int btnNavH = 18;
            int prevBtnX = left + 14;
            int nextBtnX = left + GUI_WIDTH - 14 - btnNavW;
            int totalPages = Math.max(1, (int) Math.ceil((double) recipes.size() / RECIPES_PER_PAGE));

            if (currentPage > 0 && mouseX >= prevBtnX && mouseX <= prevBtnX + btnNavW && mouseY >= navY && mouseY <= navY + btnNavH) {
                currentPage--;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            }
            if (currentPage < totalPages - 1 && mouseX >= nextBtnX && mouseX <= nextBtnX + btnNavW && mouseY >= navY && mouseY <= navY + btnNavH) {
                currentPage++;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            }

            // Click en botones Refinar
            Player player = Minecraft.getInstance().player;
            int cardStartY = top + 50;
            int startIndex = currentPage * RECIPES_PER_PAGE;
            int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, recipes.size());
            for (int i = 0; i < endIndex - startIndex; i++) {
                AlchemyRecipe r = recipes.get(startIndex + i);
                int cardY = cardStartY + i * 50;
                int cardX = left + 10;
                int cardW = GUI_WIDTH - 20;
                int btnW = 68;
                int btnH = 22;
                int btnX = cardX + cardW - btnW - 8;
                int btnY = cardY + 12;

                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    if (hasHeat && hasIngredients(player, r.recipeIndex)) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F));
                        PacketDistributor.sendToServer(new MurimAlchemyRefinePayload(cauldronPos, r.recipeIndex));
                        return true;
                    } else if (!hasHeat) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.FIRE_EXTINGUISH, 0.8F));
                        return true;
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawCornerOrnaments(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        int gold = 0xFFFFD700;
        int len = 8;
        // Superior izquierda
        guiGraphics.fill(x + 1, y + 1, x + len, y + 2, gold);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + len, gold);
        // Superior derecha
        guiGraphics.fill(x + w - len, y + 1, x + w - 1, y + 2, gold);
        guiGraphics.fill(x + w - 2, y + 1, x + w - 1, y + len, gold);
        // Inferior izquierda
        guiGraphics.fill(x + 1, y + h - 2, x + len, y + h - 1, gold);
        guiGraphics.fill(x + 1, y + h - len, x + 2, y + h - 1, gold);
        // Inferior derecha
        guiGraphics.fill(x + w - len, y + h - 2, x + w - 1, y + h - 1, gold);
        guiGraphics.fill(x + w - 2, y + h - len, x + w - 1, y + h - 1, gold);
    }

    private void drawBoxOutline(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y, x + 1, y + h, color);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}
