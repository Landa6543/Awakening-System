package com.soybe.awakeningsystem.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Intercepts player attack and block-breaking actions on the client.
 * In COMBAT mode, routes left-clicks to the Awakening Combat Engine combo system
 * and prevents accidental block breaking during intense battles.
 * In MINING mode, passes input directly to vanilla Minecraft.
 */
public class AceAttackInteractor {
    private static final AceAttackInteractor INSTANCE = new AceAttackInteractor();

    public static AceAttackInteractor getInstance() {
        return INSTANCE;
    }

    private final AceCombatState state = AceCombatState.getClientInstance();
    private boolean externalCombatModPresent = false;
    private boolean checkedExternalMods = false;

    private AceAttackInteractor() {
    }

    private void checkExternalMods() {
        if (!checkedExternalMods) {
            checkedExternalMods = true;
            externalCombatModPresent = ModList.get().isLoaded("bettercombat") || ModList.get().isLoaded("epicfight");
        }
    }

    public boolean isExternalCombatModPresent() {
        checkExternalMods();
        return externalCombatModPresent;
    }

    /**
     * Called when the player clicks the attack button (Left Click).
     * @return true if the vanilla attack should be cancelled.
     */
    public boolean onDoAttack() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isAlive()) {
            return false;
        }

        // If player is in MINING mode, let vanilla handle it completely
        if (state.isMiningMode()) {
            return false;
        }

        // COMBAT MODE IS ACTIVE:
        if (player.isHandsBusy() || player.isUsingItem()) {
            return false;
        }

        // Anti-spam: enforce attack cooldown so attacks cannot be spammed
        if (player.getAttackStrengthScale(0.0F) < 0.85F) {
            return true;
        }

        // If an attack upswing is currently active, absorb the click to keep combo cadence smooth
        if (state.isUpswingActive()) {
            return true;
        }

        // Perform combat attack through ACE
        performCombatAttack(mc, player);
        return true; // Cancel vanilla single-pixel attack
    }

    /**
     * Called when the player holds the attack button (breaking blocks).
     * @return true if block breaking should be cancelled.
     */
    public boolean onHandleBlockBreaking() {
        // In COMBAT mode, completely prevent accidental block destruction
        if (state.isCombatMode()) {
            return true;
        }
        return false;
    }

    /**
     * Executes the combat strike, triggers swing animations, audio,
     * sweeping arc target detection, and advances the combo state.
     */
    private void performCombatAttack(Minecraft mc, LocalPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        boolean isUnarmed = weapon.isEmpty();

        // 3-hit combo for weapons and martial arts
        int comboStep = state.getComboIndex();
        state.advanceCombo(3);

        // Calculate upswing cooldown based on player attack speed (prevents autoclicker spam)
        double speed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        int upswingTicks = (int) Math.min(16, Math.max(6, Math.round(14.0 / Math.max(0.5, speed))));
        state.startUpswing(upswingTicks);

        // Client visual swing
        player.swing(InteractionHand.MAIN_HAND);
        player.resetAttackStrengthTicker();

        // High-fidelity PlayerAnimator skeletal animation (weapon-specific archetype)
        AcePlayerAnimationBridge.playAttackAnimation(player, weapon, comboStep);

        // Directional Slash Trail VFX
        AceSlashVfx.spawnClientSlashTrail(player, comboStep, weapon);

        // 1. Sweeping Arc Target Acquisition
        AceTargetFinder.TargetResult result = AceTargetFinder.findTargets(player, comboStep);

        if (result.hasTargets()) {
            java.util.List<Integer> targetIds = new java.util.ArrayList<>();
            for (net.minecraft.world.entity.LivingEntity target : result.allTargets()) {
                targetIds.add(target.getId());
            }

            // 2. Dispatch multi-target attack payload to server
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new AceAttackPacket(targetIds, comboStep));

            // 3. Audio feedback
            if (isUnarmed) {
                player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG, 0.9F, 1.1F + comboStep * 0.15F);
            } else {
                player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.95F + comboStep * 0.12F);
            }
        } else {
            // Whiff / air slash audio feedback
            player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_NODAMAGE, 0.7F, 1.2F);
        }
    }

    public void onClientTick() {
        state.onClientTick();
    }
}
