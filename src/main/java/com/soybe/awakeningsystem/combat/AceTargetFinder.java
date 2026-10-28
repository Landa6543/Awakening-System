package com.soybe.awakeningsystem.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sweeping arc target acquisition engine for the Awakening Combat Engine (ACE).
 * Computes forward 3D oriented volumes and radial conical filters to detect
 * multiple adversaries in an arc, supporting swing-through-foliage and anti-wall-clipping.
 */
public class AceTargetFinder {

    public record TargetResult(Entity primaryTarget, List<LivingEntity> allTargets, double attackRange, double arcAngle) {
        public boolean hasTargets() {
            return !allTargets.isEmpty();
        }
    }

    /**
     * Finds valid attack targets in front of the player based on the current combo step.
     */
    public static TargetResult findTargets(Player player, int comboStep) {
        ItemStack weapon = player.getMainHandItem();
        double attackRange = getWeaponRange(player, weapon);
        double arcAngle = getComboArcAngle(weapon, comboStep);

        Vec3 origin = getTracingOrigin(player);
        Vec3 lookDir = player.getViewVector(1.0F).normalize();

        // 1. Initial broadphase search box
        AABB searchBox = player.getBoundingBox().inflate(attackRange + 1.5);
        List<Entity> candidateEntities = player.level().getEntities(player, searchBox, e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator());

        // 2. Build 3D Oriented Bounding Box for the forward swing volume
        double boxWidth = attackRange * 1.8;
        double boxHeight = Math.max(2.2, player.getBbHeight() * 1.2);
        double boxDepth = attackRange;

        AceOBB obb = new AceOBB(origin, boxWidth, boxHeight, boxDepth, player.getXRot(), player.getYRot());
        obb.offsetAlongAxisZ(boxDepth / 2.0);

        // 3. Narrowphase filtering (OBB + Radial Angle + Line of Sight)
        double maxHalfAngle = arcAngle / 2.0;
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity candidate : candidateEntities) {
            if (!(candidate instanceof LivingEntity living)) {
                continue;
            }
            if (isFriendlyOrImmune(player, living)) {
                continue;
            }

            AABB entityBox = living.getBoundingBox();
            Vec3 entityCenter = living.position().add(0, living.getBbHeight() * 0.5, 0);

            // A. Check OBB intersection
            if (!obb.intersects(entityBox) && !obb.contains(entityCenter)) {
                continue;
            }

            // B. Radial angle filter
            Vec3 distVec = AceCollisionHelper.distanceVector(origin, entityBox);
            Vec3 centerVec = entityCenter.subtract(origin);
            double distLength = distVec.length();

            if (distLength > attackRange) {
                continue;
            }

            double angleToCenter = AceCollisionHelper.angleBetween(centerVec, lookDir);
            double angleToBox = AceCollisionHelper.angleBetween(distVec, lookDir);

            if (angleToCenter > maxHalfAngle && angleToBox > maxHalfAngle) {
                continue;
            }

            // C. Line of Sight (Raycast obstacle check - swings through grass/foliage, blocks on walls)
            if (isBlockedBySolidWall(player, origin, entityCenter)) {
                continue;
            }

            targets.add(living);
        }

        // 4. Sort targets: cursor target first, then remaining targets by proximity
        Entity cursorTarget = getCursorTarget();
        Entity primary = null;

        if (cursorTarget instanceof LivingEntity cursorLiving && targets.contains(cursorLiving)) {
            primary = cursorLiving;
            targets.remove(cursorLiving);
            targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            targets.add(0, cursorLiving);
        } else {
            targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            if (!targets.isEmpty()) {
                primary = targets.get(0);
            }
        }

        // Limit to max 5 targets per swing for crisp focus and zero stutter
        if (targets.size() > 5) {
            targets = new ArrayList<>(targets.subList(0, 5));
        }

        return new TargetResult(primary, targets, attackRange, arcAngle);
    }

    /**
     * Calculates the tracing origin point slightly below eye level (shoulder/chest level).
     */
    public static Vec3 getTracingOrigin(Player player) {
        double shoulderOffset = player.getBbHeight() * 0.15;
        return player.getEyePosition().subtract(0, shoulderOffset, 0);
    }

    /**
     * Determines reach distance according to weapon type and attributes.
     */
    public static double getWeaponRange(Player player, ItemStack weapon) {
        if (weapon.isEmpty()) {
            return 2.5; // Unarmed martial arts reach
        }

        String itemId = weapon.getItem().toString().toLowerCase();

        // Awakening System custom spears / polearms
        if (itemId.contains("spear") || itemId.contains("lance") || itemId.contains("polearm") || itemId.contains("trident")) {
            return 4.5;
        }
        // Greatswords, claymores, scythes
        if (itemId.contains("claymore") || itemId.contains("greatsword") || itemId.contains("scythe") || itemId.contains("halberd")) {
            return 4.0;
        }
        // Daggers, knives
        if (itemId.contains("dagger") || itemId.contains("knife") || itemId.contains("tanto")) {
            return 2.8;
        }
        // Swords, katanas, axes
        if (weapon.getItem() instanceof SwordItem || itemId.contains("blade") || itemId.contains("katana") || itemId.contains("sword")) {
            return 3.6;
        }
        if (weapon.getItem() instanceof AxeItem) {
            return 3.2;
        }

        return 3.0;
    }

    /**
     * Determines the horizontal sweeping arc based on combo progression.
     */
    public static double getComboArcAngle(ItemStack weapon, int comboStep) {
        return switch (comboStep) {
            case 0 -> 110.0; // Strike 1: Quick diagonal cut
            case 1 -> 135.0; // Strike 2: Wide horizontal cleave
            case 2 -> 165.0; // Finisher: Sweeping massive arc
            default -> 120.0;
        };
    }

    /**
     * Verifies that the attack ray does not penetrate solid walls/doors.
     */
    private static boolean isBlockedBySolidWall(Player player, Vec3 start, Vec3 end) {
        BlockHitResult hit = player.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        return hit.getType() == HitResult.Type.BLOCK;
    }

    /**
     * Prevents friendly fire against the player's own mount or tamed pets.
     */
    private static boolean isFriendlyOrImmune(Player player, LivingEntity target) {
        if (target == player) return true;
        if (target.isPassengerOfSameVehicle(player)) return true;
        if (target.isAlliedTo(player)) return true;

        if (target instanceof TamableAnimal tamable) {
            if (tamable.isOwnedBy(player)) return true;
        }

        return false;
    }

    private static Entity getCursorTarget() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult ehr) {
            return ehr.getEntity();
        }
        return null;
    }
}
