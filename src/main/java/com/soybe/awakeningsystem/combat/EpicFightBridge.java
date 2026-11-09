package com.soybe.awakeningsystem.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

/**
 * The only place that talks to Epic Fight's code. Epic Fight is optional: every method checks that it is
 * loaded first, and the rest of the mod only calls through here, so nothing breaks without it.
 * <p>
 * Weapon skills use Epic Fight's own, already polished animations; what the skill does (movement,
 * damage, effects) is the mod's own code.
 */
public final class EpicFightBridge {
    private EpicFightBridge() {}

    /** The animations the mod's weapon skills use. */
    public enum Anim {
        /** Crouch-and-spring launch (Demolition Leap). */
        LEAP,
        /** Plunging slam from the air (Meteor Slam). */
        SLAM,
        /** Dual-dagger jumping strike. */
        DUAL_DAGGER_AIR
    }

    public static boolean loaded() {
        return ModList.get().isLoaded("epicfight");
    }

    /** Whether Epic Fight files this item as a dagger (covers Epic Fight's own daggers and datapack ones). */
    public static boolean isDagger(ItemStack stack) {
        if (!loaded() || stack.isEmpty()) return false;
        return Impl.isDagger(stack);
    }

    /**
     * Plays the animation on the player, for everyone around to see. With Epic Fight it is Epic Fight's own;
     * without it, the closest of the mod's own player animations (needs PlayerAnimator on the clients).
     */
    public static void play(ServerPlayer player, Anim anim) {
        if (loaded()) {
            Impl.play(player, anim);
            return;
        }
        String fallback = switch (anim) {
            case LEAP -> "dual_handed_slash_cross";
            case SLAM -> "dual_handed_stab";
            case DUAL_DAGGER_AIR -> "dual_handed_slash_uncross";
        };
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new com.soybe.awakeningsystem.PlaySkillAnimationPayload(player.getId(), fallback));
    }

    /** Takes stamina from Epic Fight's bar; true when there was enough (always true without Epic Fight). */
    public static boolean consumeStamina(ServerPlayer player, float amount) {
        return !loaded() || Impl.consumeStamina(player, amount);
    }

    /** Kept in its own class so Epic Fight's classes are only loaded when Epic Fight is present. */
    private static final class Impl {
        static boolean isDagger(ItemStack stack) {
            CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(stack);
            return cap != null && cap.getWeaponCategory() == CapabilityItem.WeaponCategories.DAGGER;
        }

        static AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor(Anim anim) {
            return switch (anim) {
                case LEAP -> Animations.BIPED_DEMOLITION_LEAP;
                case SLAM -> Animations.METEOR_SLAM;
                case DUAL_DAGGER_AIR -> Animations.DAGGER_DUAL_AIR_SLASH;
            };
        }

        static void play(ServerPlayer player, Anim anim) {
            ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
            if (patch != null) patch.playAnimationSynchronized(accessor(anim), 0.0F);
        }

        static boolean consumeStamina(ServerPlayer player, float amount) {
            ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
            if (patch == null) return true;
            float cost = patch.getModifiedStaminaConsume(amount);
            if (!patch.hasStamina(cost)) return false;
            patch.setStamina(patch.getStamina() - cost);
            return true;
        }
    }
}
