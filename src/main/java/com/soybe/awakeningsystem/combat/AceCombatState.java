package com.soybe.awakeningsystem.combat;

import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Manages player combat stance, combo counter, timing windows,
 * and client/server synchronization for the Awakening Combat Engine.
 */
public class AceCombatState {
    private static final AceCombatState CLIENT_INSTANCE = new AceCombatState();

    public static AceCombatState getClientInstance() {
        return CLIENT_INSTANCE;
    }

    private AceCombatMode currentMode = AceCombatMode.MINING;
    private int comboIndex = 0;
    private int comboResetTicks = 0;
    private long lastToggleTimeMs = 0;
    private boolean isUpswingActive = false;
    private int upswingTicksLeft = 0;

    public AceCombatMode getMode() {
        return currentMode;
    }

    public boolean isCombatMode() {
        return currentMode == AceCombatMode.COMBAT;
    }

    public boolean isMiningMode() {
        return currentMode == AceCombatMode.MINING;
    }

    public void setMode(AceCombatMode mode) {
        this.currentMode = mode;
    }

    /**
     * Toggles between MINING and COMBAT mode with sound feedback, HUD banner,
     * and network synchronization to the server.
     */
    public void toggleClientMode() {
        long now = System.currentTimeMillis();
        if (now - lastToggleTimeMs < 200) {
            return; // 200ms debounce
        }
        lastToggleTimeMs = now;

        currentMode = currentMode.next();
        resetCombo();

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            // Audio cue
            if (currentMode == AceCombatMode.COMBAT) {
                player.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.0F, 1.2F);
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.8F, 1.4F);
            } else {
                player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8F, 0.9F);
            }

            // HUD overlay banner message
            mc.gui.setOverlayMessage(currentMode.getFormattedBanner(), false);

            // Sync with server
            PacketDistributor.sendToServer(new AceToggleModePayload(currentMode.getId()));
        }
    }

    public int getComboIndex() {
        return comboIndex;
    }

    public void advanceCombo(int maxComboSteps) {
        comboIndex = (comboIndex + 1) % maxComboSteps;
        comboResetTicks = 20; // 1 second window to chain next strike
    }

    public void resetCombo() {
        comboIndex = 0;
        comboResetTicks = 0;
        isUpswingActive = false;
        upswingTicksLeft = 0;
    }

    public void onClientTick() {
        if (comboResetTicks > 0) {
            comboResetTicks--;
            if (comboResetTicks == 0) {
                comboIndex = 0;
            }
        }

        if (upswingTicksLeft > 0) {
            upswingTicksLeft--;
            if (upswingTicksLeft == 0) {
                isUpswingActive = false;
            }
        }
    }

    public boolean isUpswingActive() {
        return isUpswingActive;
    }

    public void startUpswing(int ticks) {
        this.isUpswingActive = true;
        this.upswingTicksLeft = Math.max(1, ticks);
    }

    // SERVER-SIDE UTILITY

    public static AceCombatMode getServerPlayerMode(ServerPlayer player) {
        String modeId = player.getPersistentData().getString("ace_combat_mode");
        return AceCombatMode.fromId(modeId);
    }

    public static void setServerPlayerMode(ServerPlayer player, AceCombatMode mode) {
        player.getPersistentData().putString("ace_combat_mode", mode.getId());
    }
}
