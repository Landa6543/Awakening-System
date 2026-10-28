package com.soybe.awakeningsystem.combat;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * High-fidelity skeletal player animation bridge using the open-source PlayerAnimationLib (by KosmX).
 * Plays genuine, fluid keyframe animations for each weapon archetype (swords, greatswords,
 * scythes, spears, daggers, and martial arts unarmed strikes).
 */
public class AcePlayerAnimationBridge {

    public static final ResourceLocation ANIMATION_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath("awakeningsystem", "combat_layer");

    private static boolean initialized = false;
    private static boolean available = false;

    public static boolean isAvailable() {
        return available;
    }

    /**
     * Initializes the PlayerAnimationLib modifier layer on client startup.
     */
    public static void initClient() {
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            // Check if player-anim library or mod is present
            available = ModList.get().isLoaded("playeranimator") ||
                    ModList.get().isLoaded("player_animation_lib") ||
                    isClassPresent("dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess");

            if (available) {
                dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                        ANIMATION_LAYER_ID,
                        1000,
                        player -> new ModifierLayer<>()
                );
            }
        } catch (Throwable t) {
            available = false;
        }
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Resolves the animation resource based on weapon archetype and combo step.
     */
    public static ResourceLocation getAnimationId(ItemStack weapon, int comboStep) {
        if (weapon.isEmpty()) {
            return switch (comboStep) {
                case 1 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_punch");
                case 2 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_uppercut_right");
                default -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_punch");
            };
        }

        String name = weapon.getHoverName().getString().toLowerCase();
        String path = BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath().toLowerCase();
        String combined = path + " " + name;

        // 1. Scythe (Guadaña) -> Wide lateral sweeps + 360 spin finisher
        if (combined.contains("scythe") || combined.contains("guadaña") || combined.contains("reaper")) {
            return switch (comboStep) {
                case 1 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_slash_horizontal_left");
                case 2 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_spin");
                default -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_slash_horizontal_right");
            };
        }

        // 2. Spear / Polearm (Lanza) -> Linear thrusts + heavy plunge
        if (combined.contains("spear") || combined.contains("lanza") || combined.contains("polearm") ||
                combined.contains("halberd") || combined.contains("pike")) {
            return switch (comboStep) {
                case 1 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_stab_left");
                case 2 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_slam");
                default -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_stab_right");
            };
        }

        // 3. Dagger (Daga) -> Swift switch-blade slashes + quick stab
        if (combined.contains("dagger") || combined.contains("daga") || combined.contains("knife")) {
            return switch (comboStep) {
                case 1 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_slash_switch_blade_left");
                case 2 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_stab");
                default -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_slash_switch_blade_right");
            };
        }

        // 4. Greatsword / Claymore (Mandoble / Espadón) -> Heavy two-handed slashes + ground slam
        if (combined.contains("claymore") || combined.contains("greatsword") || combined.contains("mandoble") ||
                combined.contains("heavy") || combined.contains("hammer") || combined.contains("battleaxe")) {
            return switch (comboStep) {
                case 1 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_slash_horizontal_left");
                case 2 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_slam_heavy");
                default -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "two_handed_slash_horizontal_right");
            };
        }

        // 5. Standard One-Handed Sword / Katana / Rapier / Sabre
        return switch (comboStep) {
            case 1 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_slash_horizontal_left");
            case 2 -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_slam");
            default -> ResourceLocation.fromNamespaceAndPath("awakeningsystem", "one_handed_slash_horizontal_right");
        };
    }

    /**
     * Plays the skeletal attack animation for the client player.
     */
    public static boolean playAttackAnimation(AbstractClientPlayer player, ItemStack weapon, int comboStep) {
        if (!available || player == null) {
            return false;
        }

        try {
            ResourceLocation animId = getAnimationId(weapon, comboStep);
            IPlayable playable = PlayerAnimationRegistry.getAnimation(animId);

            if (playable instanceof KeyframeAnimation anim) {
                var data = PlayerAnimationAccess.getPlayerAssociatedData(player);
                @SuppressWarnings("unchecked")
                ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) data.get(ANIMATION_LAYER_ID);

                if (layer != null) {
                    layer.setAnimation(new KeyframeAnimationPlayer(anim));
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Graceful fallback to standard swing
        }

        return false;
    }
}
