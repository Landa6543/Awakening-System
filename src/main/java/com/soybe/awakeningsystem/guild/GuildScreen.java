package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class GuildScreen extends Screen {
    private static final int GUI_WIDTH = 380;
    private static final int GUI_HEIGHT = 260;

    private final CompoundTag rawData;
    private final boolean inGuild;
    private Guild guild;
    private final CompoundTag allClaimsTag;

    private static int lastTab = 0;
    // Pestañas: 0 = Miembros, 1 = Bendiciones, 2 = Tesorería & Sede
    private int currentTab = lastTab;

    // Componentes para creación de gremio
    private EditBox createNameBox;
    private EditBox createTagBox;

    // Componente para invitar
    private EditBox inviteBox;

    // Scroll
    private int memberScrollOffset = 0;
    private int perkScrollOffset = 0;

    public GuildScreen(CompoundTag data) {
        super(Component.literal("Gremio de Cazadores"));
        this.rawData = data;
        this.inGuild = data.getBoolean("InGuild");
        if (inGuild && data.contains("Guild")) {
            this.guild = Guild.load(data.getCompound("Guild"));
        }
        if (data.contains("AllClaims")) {
            this.allClaimsTag = data.getCompound("AllClaims");
        } else if (AwakeningSystemClient.cachedClientClaims != null) {
            this.allClaimsTag = AwakeningSystemClient.cachedClientClaims;
        } else {
            this.allClaimsTag = new CompoundTag();
        }
        if (data.contains("InitialTab")) {
            this.currentTab = data.getInt("InitialTab");
            lastTab = this.currentTab;
        }
    }

    public static void open(CompoundTag data) {
        Minecraft.getInstance().setScreen(new GuildScreen(data));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        if (!inGuild) {
            this.createNameBox = new EditBox(font, left + 40, top + 100, 180, 18, Component.literal("Nombre"));
            this.createNameBox.setHint(Component.literal("§8Ej: Ahjin, Tigre Blanco..."));
            this.createNameBox.setMaxLength(16);
            addRenderableWidget(createNameBox);

            this.createTagBox = new EditBox(font, left + 230, top + 100, 100, 18, Component.literal("TAG"));
            this.createTagBox.setHint(Component.literal("§8Ej: AHJ"));
            this.createTagBox.setMaxLength(4);
            addRenderableWidget(createTagBox);
        } else {
            this.inviteBox = new EditBox(font, left + 20, top + GUI_HEIGHT - 38, 140, 16, Component.literal("Invitar"));
            this.inviteBox.setHint(Component.literal("§8Jugador a invitar..."));
            this.inviteBox.setMaxLength(16);
            this.inviteBox.visible = (currentTab == 0);
            this.inviteBox.active = (currentTab == 0);
            addRenderableWidget(inviteBox);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        // Marco y fondo holográfico de gremio
        g.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFFDFB76C);
        g.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF208111A);
        g.fill(left, top, left + GUI_WIDTH, top + 26, 0xFF141F2B);
        g.fill(left, top + 25, left + GUI_WIDTH, top + 26, 0xFFDFB76C);

        // Botón cerrar X
        boolean closeHover = mouseX >= left + GUI_WIDTH - 22 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20;
        g.fill(left + GUI_WIDTH - 22, top + 6, left + GUI_WIDTH - 6, top + 20, closeHover ? 0xDDFF4444 : 0x66222222);
        g.drawCenteredString(font, "✕", left + GUI_WIDTH - 14, top + 8, closeHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (!inGuild || guild == null) {
            renderCreateGuildView(g, left, top, mouseX, mouseY);
        } else {
            renderGuildView(g, left, top, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderCreateGuildView(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        g.drawString(font, "§6⚔ §fGREMIO DE CAZADORES §7- §eFUNDAR GREMIO", left + 14, top + 9, 0xFFFFFFFF);

        g.drawCenteredString(font, "§6✦ FORMA TU PROPIO GREMIO DE CAZADORES ✦", left + GUI_WIDTH / 2, top + 42, 0xFFFFD700);
        g.drawCenteredString(font, "§7Une a otros cazadores bajo tu estandarte, desbloquea bendiciones y comparte recursos.", left + GUI_WIDTH / 2, top + 56, 0xFFAAAAAA);

        g.drawString(font, "§bNombre del Gremio:", left + 40, top + 88, 0xFFFFFFFF);
        g.drawString(font, "§bTAG (3-4 letras):", left + 230, top + 88, 0xFFFFFFFF);

        // Requisitos
        int reqY = top + 135;
        g.fill(left + 40, reqY, left + GUI_WIDTH - 40, reqY + 50, 0x880E1C28);
        g.renderOutline(left + 40, reqY, GUI_WIDTH - 80, 50, 0xFF2A4255);

        g.drawString(font, "§eRequisitos para la Fundación:", left + 48, reqY + 8, 0xFFFFD700);
        g.drawString(font, "§7• Costo de Fundación: §b1,000 Monedas del Sistema", left + 48, reqY + 22, 0xFFFFFFFF);
        g.drawString(font, "§7• El fundador será nombrado §6Líder Supremo §7del Gremio.", left + 48, reqY + 34, 0xFFCCCCCC);

        // Botón Fundar Gremio
        int btnW = 160;
        int btnH = 22;
        int btnX = left + (GUI_WIDTH - btnW) / 2;
        int btnY = top + 200;

        boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHov ? 0xDD28864E : 0xAA1A5D3E);
        g.renderOutline(btnX, btnY, btnW, btnH, btnHov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, "⚔ FUNDAR GREMIO", btnX + btnW / 2, btnY + 7, btnHov ? 0xFFFFFFFF : 0xFF50FA7B);
    }

    private void renderGuildView(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        // Título del Gremio
        g.drawString(font, "§6⚔ [" + guild.getTag() + "] §f" + guild.getName() + " §7(Nv. §a" + guild.getLevel() + "§7)", left + 14, top + 9, 0xFFFFFFFF);

        // Barra de EXP del gremio
        long exp = guild.getExp();
        long expNeeded = guild.getExpForNextLevel();
        float ratio = Math.max(0.0F, Math.min(1.0F, (float) exp / expNeeded));
        int barW = 120;
        int barH = 8;
        int barX = left + GUI_WIDTH - 150;
        int barY = top + 10;
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF0D1721);
        g.fill(barX, barY, barX + (int) (barW * ratio), barY + barH, 0xFF00E5FF);
        g.renderOutline(barX, barY, barW, barH, 0xFF4EE2EC);

        // Pestañas
        int tabW = (GUI_WIDTH - 28) / 4;
        String[] tabs = {"✦ Miembros", "✦ Bendiciones", "✦ Tesorería", "⚔ Cacerías"};
        for (int i = 0; i < 4; i++) {
            int tX = left + 14 + i * tabW;
            int tY = top + 32;
            int tH = 18;
            boolean hov = mouseX >= tX && mouseX < tX + tabW && mouseY >= tY && mouseY < tY + tH;
            g.fill(tX, tY, tX + tabW, tY + tH, currentTab == i ? 0xCC2B3D4D : (hov ? 0x881E2B38 : 0x550F1822));
            g.renderOutline(tX, tY, tabW, tH, currentTab == i ? 0xFFDFB76C : 0xFF2A4255);
            g.drawCenteredString(font, tabs[i], tX + tabW / 2, tY + 5, currentTab == i ? 0xFFFFD700 : 0xFFAAAAAA);
        }

        if (this.inviteBox != null) {
            boolean showInvite = (currentTab == 0);
            this.inviteBox.visible = showInvite;
            this.inviteBox.active = showInvite;
        }

        int contentY = top + 56;
        switch (currentTab) {
            case 0 -> renderMembersTab(g, left, contentY, mouseX, mouseY);
            case 1 -> renderPerksTab(g, left, contentY, mouseX, mouseY);
            case 2 -> renderTreasuryTab(g, left, contentY, mouseX, mouseY);
            case 3 -> renderHuntingTab(g, left, contentY, mouseX, mouseY);
        }
    }

    private void renderMembersTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        GuildRole myRole = guild.getMemberRole(myUuid);
        boolean isLeader = guild.getLeaderUuid().equals(myUuid);

        // Encabezado de columnas
        g.drawString(font, "§8●", left + 22, top + 2, 0x888888);
        g.drawString(font, "§8RANGO", left + 38, top + 2, 0x888888);
        g.drawString(font, "§8NOMBRE", left + 110, top + 2, 0x888888);
        g.drawString(font, "§8DONADO", left + 220, top + 2, 0x888888);
        if (isLeader) {
            g.drawString(font, "§8ACCIONES", left + 285, top + 2, 0x888888);
        }

        g.fill(left + 16, top + 13, left + GUI_WIDTH - 16, top + 14, 0x334EE2EC);

        // Lista de miembros
        List<GuildMember> membersList = new ArrayList<>(guild.getMembers().values());
        int listH = 140;
        int rowH = 18;
        int maxVisible = listH / rowH;

        for (int i = 0; i < maxVisible; i++) {
            int idx = i + memberScrollOffset;
            if (idx >= membersList.size()) break;
            GuildMember m = membersList.get(idx);

            int rY = top + 18 + i * rowH;
            boolean rHov = mouseX >= left + 16 && mouseX < left + GUI_WIDTH - 16 && mouseY >= rY && mouseY < rY + rowH - 2;
            if (rHov) {
                g.fill(left + 16, rY, left + GUI_WIDTH - 16, rY + rowH - 2, 0x224EE2EC);
            }

            // Indicador de conexión
            boolean isOnline = mc.getConnection() != null && mc.getConnection().getPlayerInfo(m.getUuid()) != null;
            g.drawString(font, isOnline ? "§a●" : "§7○", left + 22, rY + 3, isOnline ? 0xFF50FA7B : 0xFF777777);

            // Rango
            g.drawString(font, m.getRole().getLabel(), left + 38, rY + 3, 0xFFFFFFFF);

            // Nombre
            String nameStr = m.getName();
            if (m.getUuid().equals(myUuid)) nameStr += " §e(Tú)";
            g.drawString(font, nameStr, left + 110, rY + 3, 0xFFFFFFFF);

            // Donado
            g.drawString(font, "§e" + m.getContributedMora() + " 🪙", left + 220, rY + 3, 0xFFFFD700);

            // Botones de acción para el Líder
            if (isLeader && !m.getUuid().equals(myUuid)) {
                // Ascender
                boolean hovUp = mouseX >= left + 285 && mouseX < left + 301 && mouseY >= rY && mouseY < rY + 14;
                g.fill(left + 285, rY, left + 301, rY + 14, hovUp ? 0xDD28864E : 0x881A5D3E);
                g.drawCenteredString(font, "▲", left + 293, rY + 2, 0xFF50FA7B);

                // Degradar
                boolean hovDown = mouseX >= left + 305 && mouseX < left + 321 && mouseY >= rY && mouseY < rY + 14;
                g.fill(left + 305, rY, left + 321, rY + 14, hovDown ? 0xDDAA6622 : 0x88663311);
                g.drawCenteredString(font, "▼", left + 313, rY + 2, 0xFFFFBB44);

                // Expulsar
                boolean hovKick = mouseX >= left + 325 && mouseX < left + 341 && mouseY >= rY && mouseY < rY + 14;
                g.fill(left + 325, rY, left + 341, rY + 14, hovKick ? 0xDD992222 : 0x88551111);
                g.drawCenteredString(font, "✕", left + 333, rY + 2, 0xFFFF6666);
            }
        }

        // Barra inferior de miembros: Invitar jugador & Salir del gremio
        int btmY = top + GUI_HEIGHT - 38 - 56;
        if (myRole != null && myRole.canInvite()) {
            int invBtnX = left + 165;
            int invBtnW = 75;
            int invBtnH = 16;
            boolean invHov = mouseX >= invBtnX && mouseX < invBtnX + invBtnW && mouseY >= btmY && mouseY < btmY + invBtnH;
            g.fill(invBtnX, btmY, invBtnX + invBtnW, btmY + invBtnH, invHov ? 0xDD225577 : 0x88113344);
            g.renderOutline(invBtnX, btmY, invBtnW, invBtnH, invHov ? 0xFF00E5FF : 0xFF2A4255);
            g.drawCenteredString(font, "+ Invitar", invBtnX + invBtnW / 2, btmY + 4, 0xFF70E8FF);
        }

        // Botón Abandonar / Disolver Gremio
        int leaveBtnX = left + GUI_WIDTH - 110;
        int leaveBtnW = 94;
        int leaveBtnH = 16;
        boolean leaveHov = mouseX >= leaveBtnX && mouseX < leaveBtnX + leaveBtnW && mouseY >= btmY && mouseY < btmY + leaveBtnH;
        g.fill(leaveBtnX, btmY, leaveBtnX + leaveBtnW, btmY + leaveBtnH, leaveHov ? 0xAA6E2020 : 0x663B1111);
        g.renderOutline(leaveBtnX, btmY, leaveBtnW, leaveBtnH, leaveHov ? 0xFFFF6666 : 0xFF882222);
        g.drawCenteredString(font, isLeader ? "☠ Disolver" : "✕ Salir", leaveBtnX + leaveBtnW / 2, btmY + 4, 0xFFFF8888);
    }

    private void renderPerksTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        boolean isLeader = guild.getLeaderUuid().equals(myUuid);

        GuildPerk[] perks = GuildPerk.values();
        int cardW = GUI_WIDTH - 32;
        int cardH = 24;

        for (int i = 0; i < perks.length; i++) {
            GuildPerk p = perks[i];
            int pY = top + i * (cardH + 4);
            if (pY + cardH > top + 190) break;

            boolean unlocked = guild.hasPerk(p);
            boolean canUnlock = isLeader && !unlocked && guild.getLevel() >= p.getRequiredGuildLevel() && guild.getMoraVault() >= p.getCostMora();

            int borderCol = unlocked ? 0xFF50FA7B : (canUnlock ? 0xFFFFD700 : 0xFF3D566E);
            int bgCol = unlocked ? 0x401A5D3E : 0x30101C28;

            g.fill(left + 16, pY, left + 16 + cardW, pY + cardH, bgCol);
            g.renderOutline(left + 16, pY, cardW, cardH, borderCol);

            // Icono y título
            g.drawString(font, p.getIcon() + " §f" + p.getTitle(), left + 22, pY + 4, unlocked ? 0xFF50FA7B : 0xFFFFFFFF);
            g.drawString(font, "§7" + p.getDescription(), left + 22, pY + 14, 0xFFAAAAAA);

            // Estado a la derecha
            if (unlocked) {
                g.drawString(font, "§a✔ ACTIVA", left + cardW - 55, pY + 8, 0xFF50FA7B);
            } else {
                if (guild.getLevel() < p.getRequiredGuildLevel()) {
                    g.drawString(font, "§cReq: Nv. " + p.getRequiredGuildLevel(), left + cardW - 65, pY + 8, 0xFFFF6666);
                } else if (isLeader) {
                    int btnX = left + cardW - 75;
                    int btnW = 85;
                    int btnH = 16;
                    boolean bHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= pY + 4 && mouseY < pY + 4 + btnH;
                    g.fill(btnX, pY + 4, btnX + btnW, pY + 4 + btnH, bHov ? 0xDD6E5515 : 0xAA453308);
                    g.renderOutline(btnX, pY + 4, btnW, btnH, 0xFFFFD700);
                    g.drawCenteredString(font, "🪙 " + p.getCostMora(), btnX + btnW / 2, pY + 8, 0xFFFFD700);
                } else {
                    g.drawString(font, "§e" + p.getCostMora() + " 🪙", left + cardW - 55, pY + 8, 0xFFFFD700);
                }
            }
        }
    }

    private void renderTreasuryTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        GuildRole myRole = guild.getMemberRole(myUuid);
        boolean isLeader = guild.getLeaderUuid().equals(myUuid);
        boolean canWithdraw = myRole != null && myRole.canWithdraw();
        boolean canClaim = myRole != null && myRole.canClaimTerritory();

        // 1. Panel de Tesorería (Dual Currency)
        g.fill(left + 16, top, left + GUI_WIDTH - 16, top + 88, 0x4010202E);
        g.renderOutline(left + 16, top, GUI_WIDTH - 32, 88, 0xFFDFB76C);

        g.drawString(font, "§6🪙 BÓVEDA DEL GREMIO", left + 24, top + 6, 0xFFFFD700);
        g.drawString(font, "§fMoras: §e" + String.format("%,d", guild.getMoraVault()) + " 🪙   §fMonedas: §b" + String.format("%,d", guild.getCoinVault()) + " ⭐", left + 24, top + 18, 0xFFFFFFFF);

        // Fila 1: Moras (Donar y Retirar)
        g.drawString(font, "§e🪙 Mora:", left + 24, top + 34, 0xFFFFD700);
        String[] depMoraLabels = {"+1k", "+10k", "+50k"};
        int btnW = 38;
        int btnH = 15;
        // Donar Mora
        for (int i = 0; i < 3; i++) {
            int bX = left + 76 + i * (btnW + 4);
            int bY = top + 31;
            boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
            g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD28864E : 0x881A5D3E);
            g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFF50FA7B : 0xFF28864E);
            g.drawCenteredString(font, depMoraLabels[i], bX + btnW / 2, bY + 3, 0xFF50FA7B);
        }
        // Retirar Mora
        if (canWithdraw) {
            String[] withMoraLabels = {"-1k", "-10k", "-50k"};
            for (int i = 0; i < 3; i++) {
                int bX = left + 210 + i * (btnW + 4);
                int bY = top + 31;
                boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
                g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD8B2B2B : 0x885A1A1A);
                g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFFFFB86C : 0xFF8B2B2B);
                g.drawCenteredString(font, withMoraLabels[i], bX + btnW / 2, bY + 3, 0xFFFFB86C);
            }
        } else {
            int reqW = 122;
            int reqH = 15;
            int rX = left + 210;
            int rY = top + 31;
            boolean rHov = mouseX >= rX && mouseX < rX + reqW && mouseY >= rY && mouseY < rY + reqH;
            g.fill(rX, rY, rX + reqW, rY + reqH, rHov ? 0xDD7A5A1A : 0x884A3510);
            g.renderOutline(rX, rY, reqW, reqH, rHov ? 0xFFFFD700 : 0xFF7A5A1A);
            g.drawCenteredString(font, "📩 Solicitar Retiro", rX + reqW / 2, rY + 3, rHov ? 0xFFFFFFFF : 0xFFFFD700);
        }

        // Fila 2: Monedas de Despertar (Donar y Retirar)
        g.drawString(font, "§b⭐ Coins:", left + 24, top + 56, 0xFF00E5FF);
        String[] depCoinLabels = {"+10", "+50", "+250"};
        // Donar Coins
        for (int i = 0; i < 3; i++) {
            int bX = left + 76 + i * (btnW + 4);
            int bY = top + 53;
            boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
            g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD1B5E7D : 0x88123B4E);
            g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFF00E5FF : 0xFF1B5E7D);
            g.drawCenteredString(font, depCoinLabels[i], bX + btnW / 2, bY + 3, 0xFF00E5FF);
        }
        // Retirar Coins
        if (canWithdraw) {
            String[] withCoinLabels = {"-10", "-50", "-250"};
            for (int i = 0; i < 3; i++) {
                int bX = left + 210 + i * (btnW + 4);
                int bY = top + 53;
                boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
                g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD7A2855 : 0x884D1A36);
                g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFFFF79C6 : 0xFF7A2855);
                g.drawCenteredString(font, withCoinLabels[i], bX + btnW / 2, bY + 3, 0xFFFF79C6);
            }
        } else {
            int reqW = 122;
            int reqH = 15;
            int rX = left + 210;
            int rY = top + 53;
            boolean rHov = mouseX >= rX && mouseX < rX + reqW && mouseY >= rY && mouseY < rY + reqH;
            g.fill(rX, rY, rX + reqW, rY + reqH, rHov ? 0xDD1B5E7D : 0x88123B4E);
            g.renderOutline(rX, rY, reqW, reqH, rHov ? 0xFF00E5FF : 0xFF1B5E7D);
            g.drawCenteredString(font, "📩 Solicitar Retiro", rX + reqW / 2, rY + 3, rHov ? 0xFFFFFFFF : 0xFF00E5FF);
        }

        // 2. Panel de Sede del Gremio & Territorio
        int hqY = top + 94;
        int hqH = 90;
        g.fill(left + 16, hqY, left + GUI_WIDTH - 16, hqY + hqH, 0x4010202E);
        g.renderOutline(left + 16, hqY, GUI_WIDTH - 32, hqH, 0xFF4EE2EC);

        g.drawString(font, "§b🌀 SEDE Y CONTROL DE TERRITORIO", left + 24, hqY + 6, 0xFF00E5FF);
        if (guild.hasHq()) {
            g.drawString(font, "§aEstado Sede: §f" + (int) guild.getHqX() + ", " + (int) guild.getHqY() + ", " + (int) guild.getHqZ() + " §7(" + guild.getHqDimension() + ")", left + 24, hqY + 20, 0xFFFFFFFF);
        } else {
            g.drawString(font, "§cEstado Sede: Sin Sede Establecida (¡Usa el botón para fijarla!)", left + 24, hqY + 20, 0xFFFF6666);
        }

        g.drawString(font, "§a⛳ Chunks: §e" + guild.getClaimedChunks().size() + " §7/ §f" + guild.getMaxClaims() + " §7(Escala con Nivel " + guild.getLevel() + ")", left + 24, hqY + 34, 0xFF50FA7B);

        // Fila de botones de acción
        int actionY = hqY + 54;
        int actBtnH = 18;
        int curX = left + 22;

        // Botón Viajar a la Sede
        int warpBtnW = 58;
        boolean warpHov = mouseX >= curX && mouseX < curX + warpBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
        g.fill(curX, actionY, curX + warpBtnW, actionY + actBtnH, warpHov ? 0xDD225577 : 0x88113344);
        g.renderOutline(curX, actionY, warpBtnW, actBtnH, warpHov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(font, "🌀 Viajar", curX + warpBtnW / 2, actionY + 5, 0xFF70E8FF);
        curX += warpBtnW + 5;

        // Botón Establecer Sede (Líder)
        if (isLeader) {
            int setHqW = 66;
            boolean setHov = mouseX >= curX && mouseX < curX + setHqW && mouseY >= actionY && mouseY < actionY + actBtnH;
            g.fill(curX, actionY, curX + setHqW, actionY + actBtnH, setHov ? 0xDD6E5515 : 0x88453308);
            g.renderOutline(curX, actionY, setHqW, actBtnH, setHov ? 0xFFFFD700 : 0xFF886622);
            g.drawCenteredString(font, "🚩 Fijar Sede", curX + setHqW / 2, actionY + 5, 0xFFFFD700);
            curX += setHqW + 5;
        }

        // Botón Mapa de Chunks (¡Visual e Interactivo!)
        int mapBtnW = 90;
        boolean mapHov = mouseX >= curX && mouseX < curX + mapBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
        g.fill(curX, actionY, curX + mapBtnW, actionY + actBtnH, mapHov ? 0xDD5A3A8E : 0x8838235A);
        g.renderOutline(curX, actionY, mapBtnW, actBtnH, mapHov ? 0xFFBD93F9 : 0xFF5A3A8E);
        g.drawCenteredString(font, "🗺️ Mapa Chunks", curX + mapBtnW / 2, actionY + 5, 0xFFFFD700);
        curX += mapBtnW + 5;

        // Botones rápidos de Reclamar / Liberar Territorio (Líder u Oficiales)
        if (canClaim) {
            int claimBtnW = 56;
            boolean claimHov = mouseX >= curX && mouseX < curX + claimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
            g.fill(curX, actionY, curX + claimBtnW, actionY + actBtnH, claimHov ? 0xDD28864E : 0x881A5D3E);
            g.renderOutline(curX, actionY, claimBtnW, actBtnH, claimHov ? 0xFF50FA7B : 0xFF28864E);
            g.drawCenteredString(font, "⛳ Reclamar", curX + claimBtnW / 2, actionY + 5, 0xFF50FA7B);
            curX += claimBtnW + 5;

            int unclaimBtnW = 44;
            boolean unclaimHov = mouseX >= curX && mouseX < curX + unclaimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
            g.fill(curX, actionY, curX + unclaimBtnW, actionY + actBtnH, unclaimHov ? 0xDD8B2B2B : 0x885A1A1A);
            g.renderOutline(curX, actionY, unclaimBtnW, actBtnH, unclaimHov ? 0xFFFF5555 : 0xFF8B2B2B);
            g.drawCenteredString(font, "❌ Quitar", curX + unclaimBtnW / 2, actionY + 5, 0xFFFF6666);
        }
    }

    private void renderHuntingTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        GuildRole myRole = guild.getMemberRole(myUuid);
        boolean canWithdraw = myRole != null && myRole.canWithdraw();

        // Encabezado
        g.drawString(font, "§6⚔ TABLÓN DE CACERÍAS DEL GREMIO §7(Misiones en Grupo)", left + 18, top + 2, 0xFFFFD700);
        g.drawString(font, "§8Las cacerías progresan en grupo. Al completarlas, el gremio recibe EXP y fondos.", left + 18, top + 13, 0xFFAAAAAA);

        int cardW = GUI_WIDTH - 32;
        int cardH = 28;
        int startY = top + 26;

        // 4 Misiones
        String[][] quests = {
                {"💀 Purga del Abismo y Sombras", "Hilichurls, criaturas del Vacío o Espectros", String.valueOf(guild.getHuntAbyssProgress()), String.valueOf(guild.getHuntAbyssGoal()), "+1,500 EXP | +25k 🪙 | +250 ⭐"},
                {"🐲 Cacería de Jefes Mayores", "Jefes de Mazmorra, Dragón, Wither o Hipóstasis", String.valueOf(guild.getHuntBossProgress()), String.valueOf(guild.getHuntBossGoal()), "+2,500 EXP | +50k 🪙 | +500 ⭐"},
                {"⚡ Exterminio de Monstruos de Élite", "Monstruos escalados de zona alta o minijefes", String.valueOf(guild.getHuntEliteProgress()), String.valueOf(guild.getHuntEliteGoal()), "+1,000 EXP | +15k 🪙 | +150 ⭐"},
                {"🗝️ Conquista de Mazmorras y Cofres", "Cofres Bloqueados abiertos con Llaves", String.valueOf(guild.getHuntChestsProgress()), String.valueOf(guild.getHuntChestsGoal()), "+2,000 EXP | +30k 🪙 | +300 ⭐"}
        };
        int[] barColors = {0xFF50FA7B, 0xFFFFD700, 0xFFFF79C6, 0xFF00E5FF};

        for (int i = 0; i < 4; i++) {
            int qY = startY + i * (cardH + 4);
            boolean cardHov = mouseX >= left + 16 && mouseX < left + 16 + cardW && mouseY >= qY && mouseY < qY + cardH;
            g.fill(left + 16, qY, left + 16 + cardW, qY + cardH, cardHov ? 0x60162A3C : 0x4010202E);
            g.renderOutline(left + 16, qY, cardW, cardH, cardHov ? 0xFF4EE2EC : 0xFF2A4255);

            // Título
            g.drawString(font, "§e" + quests[i][0], left + 22, qY + 4, 0xFFFFFFFF);

            // Recompensa
            g.drawString(font, "§a" + quests[i][4], left + 22, qY + 16, 0xFF50FA7B);

            // Barra de progreso
            int prog = Integer.parseInt(quests[i][2]);
            int goal = Integer.parseInt(quests[i][3]);
            float ratio = Math.min(1.0F, (float) prog / Math.max(1, goal));
            int bW = 80;
            int bH = 10;
            int bX = left + cardW - bW - 8;
            int bY = qY + 9;
            g.fill(bX, bY, bX + bW, bY + bH, 0xFF0D1721);
            g.fill(bX, bY, bX + (int) (bW * ratio), bY + bH, barColors[i]);
            g.renderOutline(bX, bY, bW, bH, 0xFF4EE2EC);
            g.drawCenteredString(font, prog + " / " + goal, bX + bW / 2, bY + 1, 0xFFFFFFFF);

            // Tooltip con el objetivo específico al pasar el ratón
            if (cardHov && mouseX < bX) {
                g.renderTooltip(font, Component.literal("§6Objetivo: §f" + quests[i][1]), mouseX, mouseY);
            }
        }

        // Panel inferior: Adquirir bloque de tablón de cacerías para colocar en la base
        int botY = startY + 4 * (cardH + 4) + 4;
        g.fill(left + 16, botY, left + 16 + cardW, botY + 24, 0x30101C28);
        g.renderOutline(left + 16, botY, cardW, 24, 0xFFDFB76C);

        g.drawString(font, "§eTablón Físico de Sede: §7(Acceso)", left + 22, botY + 8, 0xFFFFFFFF);

        if (canWithdraw) {
            int buyBtnW = 105;
            int buyBtnX = left + cardW - buyBtnW - 8;
            int buyBtnH = 16;
            int buyBtnY = botY + 4;
            boolean bHov = mouseX >= buyBtnX && mouseX < buyBtnX + buyBtnW && mouseY >= buyBtnY && mouseY < buyBtnY + buyBtnH;
            g.fill(buyBtnX, buyBtnY, buyBtnX + buyBtnW, buyBtnY + buyBtnH, bHov ? 0xDD6E5515 : 0x88453308);
            g.renderOutline(buyBtnX, buyBtnY, buyBtnW, buyBtnH, bHov ? 0xFFFFD700 : 0xFF886622);
            g.drawCenteredString(font, "📜 Comprar (5k 🪙)", buyBtnX + buyBtnW / 2, buyBtnY + 4, 0xFFFFD700);
        } else {
            g.drawString(font, "§8(Solo Oficiales)", left + cardW - 85, botY + 8, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (width - GUI_WIDTH) / 2;
            int top = (height - GUI_HEIGHT) / 2;

            // Botón cerrar
            if (mouseX >= left + GUI_WIDTH - 22 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20) {
                onClose();
                return true;
            }

            if (!inGuild || guild == null) {
                // Botón Fundar Gremio
                int btnW = 160;
                int btnH = 22;
                int btnX = left + (GUI_WIDTH - btnW) / 2;
                int btnY = top + 200;
                if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    if (createNameBox != null && createTagBox != null) {
                        String name = createNameBox.getValue().trim();
                        String tag = createTagBox.getValue().trim();
                        if (!name.isEmpty() && !tag.isEmpty()) {
                            PacketDistributor.sendToServer(new GuildActionPayload("CREATE", name + ":" + tag, 0));
                            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                            onClose();
                            return true;
                        }
                    }
                }
            } else {
                // Pestañas (4 pestañas)
                int tabW = (GUI_WIDTH - 28) / 4;
                for (int i = 0; i < 4; i++) {
                    int tX = left + 14 + i * tabW;
                    int tY = top + 32;
                    int tH = 18;
                    if (mouseX >= tX && mouseX < tX + tabW && mouseY >= tY && mouseY < tY + tH) {
                        currentTab = i;
                        lastTab = i;
                        if (this.inviteBox != null) {
                            this.inviteBox.visible = (currentTab == 0);
                            this.inviteBox.active = (currentTab == 0);
                        }
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                }

                int contentY = top + 56;
                Minecraft mc = Minecraft.getInstance();
                UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
                boolean isLeader = guild.getLeaderUuid().equals(myUuid);

                if (currentTab == 0) {
                    // Acciones de Miembros
                    List<GuildMember> membersList = new ArrayList<>(guild.getMembers().values());
                    int rowH = 18;
                    for (int i = 0; i < Math.min(7, membersList.size()); i++) {
                        int idx = i + memberScrollOffset;
                        if (idx >= membersList.size()) break;
                        GuildMember m = membersList.get(idx);
                        int rY = contentY + 18 + i * rowH;

                        if (isLeader && !m.getUuid().equals(myUuid)) {
                            // Ascender
                            if (mouseX >= left + 285 && mouseX < left + 301 && mouseY >= rY && mouseY < rY + 14) {
                                PacketDistributor.sendToServer(new GuildActionPayload("PROMOTE", m.getUuid().toString(), 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.5F);
                                return true;
                            }
                            // Degradar
                            if (mouseX >= left + 305 && mouseX < left + 321 && mouseY >= rY && mouseY < rY + 14) {
                                PacketDistributor.sendToServer(new GuildActionPayload("DEMOTE", m.getUuid().toString(), 0));
                                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.8F);
                                return true;
                            }
                            // Expulsar
                            if (mouseX >= left + 325 && mouseX < left + 341 && mouseY >= rY && mouseY < rY + 14) {
                                PacketDistributor.sendToServer(new GuildActionPayload("KICK", m.getUuid().toString(), 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_DESTROY, 0.7F, 1.2F);
                                return true;
                            }
                        }
                    }

                    // Botón Invitar
                    int btmY = contentY + GUI_HEIGHT - 38 - 56;
                    int invBtnX = left + 165;
                    int invBtnW = 75;
                    int invBtnH = 16;
                    if (mouseX >= invBtnX && mouseX < invBtnX + invBtnW && mouseY >= btmY && mouseY < btmY + invBtnH) {
                        if (inviteBox != null && !inviteBox.getValue().trim().isEmpty()) {
                            PacketDistributor.sendToServer(new GuildActionPayload("INVITE", inviteBox.getValue().trim(), 0));
                            inviteBox.setValue("");
                            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                            return true;
                        }
                    }

                    // Botón Salir / Disolver
                    int leaveBtnX = left + GUI_WIDTH - 110;
                    int leaveBtnW = 94;
                    int leaveBtnH = 16;
                    if (mouseX >= leaveBtnX && mouseX < leaveBtnX + leaveBtnW && mouseY >= btmY && mouseY < btmY + leaveBtnH) {
                        if (isLeader) {
                            PacketDistributor.sendToServer(new GuildActionPayload("DISBAND", "", 0));
                        } else {
                            PacketDistributor.sendToServer(new GuildActionPayload("LEAVE", "", 0));
                        }
                        AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.0F);
                        onClose();
                        return true;
                    }
                } else if (currentTab == 1) {
                    // Desbloquear Perks
                    GuildPerk[] perks = GuildPerk.values();
                    int cardW = GUI_WIDTH - 32;
                    int cardH = 24;

                    for (int i = 0; i < perks.length; i++) {
                        GuildPerk p = perks[i];
                        int pY = contentY + i * (cardH + 4);
                        if (isLeader && !guild.hasPerk(p) && guild.getLevel() >= p.getRequiredGuildLevel() && guild.getMoraVault() >= p.getCostMora()) {
                            int btnX = left + cardW - 75;
                            int btnW = 85;
                            int btnH = 16;
                            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= pY + 4 && mouseY < pY + 4 + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("PERK", p.getId(), 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                                return true;
                            }
                        }
                    }
                } else if (currentTab == 2) {
                    // Donaciones y Retiros
                    int btnW = 38;
                    int btnH = 15;
                    int[] depMora = {1000, 10000, 50000};
                    int[] depCoins = {10, 50, 250};

                    // Donaciones de Mora (3 botones a la izquierda)
                    for (int i = 0; i < 3; i++) {
                        int bX = left + 76 + i * (btnW + 4);
                        int bY = contentY + 31;
                        if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("DEPOSIT", "", depMora[i]));
                            AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.4F);
                            return true;
                        }
                    }

                    // Donaciones de Monedas (3 botones a la izquierda)
                    for (int i = 0; i < 3; i++) {
                        int bX = left + 76 + i * (btnW + 4);
                        int bY = contentY + 53;
                        if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("DEPOSIT_COINS", "", depCoins[i]));
                            AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.4F);
                            return true;
                        }
                    }

                    // Retiros (Líder u Oficiales - 3 botones a la derecha)
                    GuildRole myRole = guild.getMemberRole(myUuid);
                    if (myRole != null && myRole.canWithdraw()) {
                        // Retiro de Mora
                        for (int i = 0; i < 3; i++) {
                            int bX = left + 210 + i * (btnW + 4);
                            int bY = contentY + 31;
                            if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("WITHDRAW_MORA", "", depMora[i]));
                                AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.9F);
                                return true;
                            }
                        }
                        // Retiro de Monedas
                        for (int i = 0; i < 3; i++) {
                            int bX = left + 210 + i * (btnW + 4);
                            int bY = contentY + 53;
                            if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("WITHDRAW_COINS", "", depCoins[i]));
                                AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.9F);
                                return true;
                            }
                        }
                    } else {
                        int reqW = 122;
                        int reqH = 15;
                        int rX = left + 210;
                        // Solicitar Mora
                        if (mouseX >= rX && mouseX < rX + reqW && mouseY >= contentY + 31 && mouseY < contentY + 31 + reqH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("REQUEST_WITHDRAW", "MORA", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                            return true;
                        }
                        // Solicitar Monedas
                        if (mouseX >= rX && mouseX < rX + reqW && mouseY >= contentY + 53 && mouseY < contentY + 53 + reqH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("REQUEST_WITHDRAW", "COINS", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                            return true;
                        }
                    }

                    // Sede & Territorio
                    int hqY = contentY + 94;
                    int actionY = hqY + 54;
                    int actBtnH = 18;
                    int curX = left + 22;

                    // Viajar a Sede
                    int warpBtnW = 58;
                    if (mouseX >= curX && mouseX < curX + warpBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                        PacketDistributor.sendToServer(new GuildActionPayload("HOME", "", 0));
                        AwakeningSystemClient.playUiSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                        onClose();
                        return true;
                    }
                    curX += warpBtnW + 5;

                    // Fijar Sede (Líder)
                    if (isLeader) {
                        int setHqW = 66;
                        if (mouseX >= curX && mouseX < curX + setHqW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("SET_HOME", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                            return true;
                        }
                        curX += setHqW + 5;
                    }

                    // Mapa de Chunks (Abrir GuildChunkMapScreen)
                    int mapBtnW = 90;
                    if (mouseX >= curX && mouseX < curX + mapBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                        Minecraft.getInstance().setScreen(new GuildChunkMapScreen(this.guild, this.allClaimsTag, this));
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                        return true;
                    }
                    curX += mapBtnW + 5;

                    // Reclamar / Liberar Chunk Rápido (Líder u Oficiales)
                    if (myRole != null && myRole.canClaimTerritory()) {
                        int claimBtnW = 56;
                        if (mouseX >= curX && mouseX < curX + claimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("CLAIM_CHUNK", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                            return true;
                        }
                        curX += claimBtnW + 5;

                        int unclaimBtnW = 44;
                        if (mouseX >= curX && mouseX < curX + unclaimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("UNCLAIM_CHUNK", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                            return true;
                        }
                    }
                } else if (currentTab == 3) {
                    // Pestaña Cacerías: Comprar Tablón Físico
                    GuildRole myRole = guild.getMemberRole(myUuid);
                    if (myRole != null && myRole.canWithdraw()) {
                        int cardW = GUI_WIDTH - 32;
                        int botY = contentY + 26 + 4 * (28 + 4) + 4;
                        int buyBtnW = 105;
                        int buyBtnX = left + cardW - buyBtnW - 8;
                        int buyBtnH = 16;
                        int buyBtnY = botY + 4;
                        if (mouseX >= buyBtnX && mouseX < buyBtnX + buyBtnW && mouseY >= buyBtnY && mouseY < buyBtnY + buyBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("BUY_HUNTING_BOARD", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
