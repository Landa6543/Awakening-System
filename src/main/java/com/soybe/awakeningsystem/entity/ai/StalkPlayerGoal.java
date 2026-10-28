package com.soybe.awakeningsystem.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Meta de Acecho a Distancia para las entidades del Limbo:
 * La criatura detecta al jugador y mantiene un margen de distancia constante (10 - 16 bloques)
 * en la penumbra del vacío.
 * - Si el jugador se aproxima a menos de 9-10 bloques, la criatura retrocede para conservar su distancia.
 * - Si el jugador se aleja a más de 16 bloques, la criatura avanza sigilosamente para recuperar el margen.
 * - En la zona de acecho (10 a 15 bloques), se detiene inmóvil en la oscuridad, contemplándolo fijamente.
 * No ataca ni arremete contra el jugador.
 */
public class StalkPlayerGoal extends Goal {

    public interface StalkSoundEmitter {
        void emitStalkSound();
    }

    private final PathfinderMob mob;
    private Player targetPlayer;
    private final double speedModifier;
    private final float minStalkDistance;
    private final float maxFollowDistance;
    private int recalibratePathTicks = 0;
    private int soundCooldownTicks = 0;

    public StalkPlayerGoal(PathfinderMob mob, double speedModifier, float minStalkDistance, float maxFollowDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.minStalkDistance = minStalkDistance;
        this.maxFollowDistance = maxFollowDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private boolean isNearAnyDoor(Vec3 pos, double radius) {
        for (net.minecraft.core.BlockPos door : com.soybe.awakeningsystem.limbo.LimboWorldManager.CARDINAL_DOORS) {
            double dx = pos.x - (door.getX() + 0.5D);
            double dz = pos.z - (door.getZ() + 0.5D);
            if ((dx * dx + dz * dz) <= (radius * radius)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUse() {
        Player nearest = this.mob.level().getNearestPlayer(this.mob, this.maxFollowDistance);
        if (nearest == null || nearest.isCreative() || nearest.isSpectator()) {
            return false;
        }
        // Perímetro sagrado de 50 bloques de las Puertas del Limbo: las entidades no pueden acechar cerca
        if (isNearAnyDoor(this.mob.position(), 50.0D) || isNearAnyDoor(nearest.position(), 50.0D)) {
            return false;
        }
        this.targetPlayer = nearest;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isCreative() || this.targetPlayer.isSpectator()) {
            return false;
        }
        if (isNearAnyDoor(this.mob.position(), 50.0D) || isNearAnyDoor(this.targetPlayer.position(), 50.0D)) {
            return false;
        }
        double distSq = this.mob.distanceToSqr(this.targetPlayer);
        return distSq <= (this.maxFollowDistance * this.maxFollowDistance);
    }

    @Override
    public void start() {
        this.recalibratePathTicks = 0;
        this.soundCooldownTicks = 60 + this.mob.getRandom().nextInt(60);
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        // Perímetro de 50 bloques de las Puertas: el acechador debe retroceder y alejarse
        for (net.minecraft.core.BlockPos door : com.soybe.awakeningsystem.limbo.LimboWorldManager.CARDINAL_DOORS) {
            double dx = this.mob.getX() - (door.getX() + 0.5D);
            double dz = this.mob.getZ() - (door.getZ() + 0.5D);
            if ((dx * dx + dz * dz) <= 2500.0D) { // 50 bloques de radio
                Vec3 awayFromDoor = new Vec3(dx, 0, dz).normalize().scale(8.0D);
                Vec3 retreatTarget = this.mob.position().add(awayFromDoor);
                this.mob.getNavigation().moveTo(retreatTarget.x, retreatTarget.y, retreatTarget.z, this.speedModifier * 1.1D);
                return;
            }
        }

        // Mirar intensamente al jugador a la altura de los ojos
        this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

        double distSq = this.mob.distanceToSqr(this.targetPlayer);
        double desiredMinSq = this.minStalkDistance * this.minStalkDistance;
        double tooCloseDist = Math.max(5.0D, this.minStalkDistance - 2.5D);
        double tooCloseSq = tooCloseDist * tooCloseDist;

        if (distSq < tooCloseSq) {
            // El jugador se acercó demasiado; retroceder suavemente para mantener el margen de acecho
            Vec3 away = this.mob.position().subtract(this.targetPlayer.position()).normalize().scale(5.0D);
            Vec3 retreatTarget = this.mob.position().add(away);
            this.mob.getNavigation().moveTo(retreatTarget.x, retreatTarget.y, retreatTarget.z, this.speedModifier * 1.05D);
        } else if (distSq > desiredMinSq + 25.0D) {
            // El jugador se distanció a más de 16 bloques; avanzar sigilosamente en la penumbra
            if (--this.recalibratePathTicks <= 0) {
                this.recalibratePathTicks = 15;
                this.mob.getNavigation().moveTo(this.targetPlayer, this.speedModifier * 0.75D);
            }
        } else {
            // En el margen perfecto de acecho (10 a 15 bloques): detenerse en la oscuridad y observar fijamente
            this.mob.getNavigation().stop();
        }

        // Emisión periódica de susurros, chasquidos y distorsiones del vacío (cada 7 a 13 segundos para evitar solapamientos y cortes)
        if (--this.soundCooldownTicks <= 0) {
            this.soundCooldownTicks = 140 + this.mob.getRandom().nextInt(120);
            if (this.mob instanceof StalkSoundEmitter emitter) {
                emitter.emitStalkSound();
            } else {
                this.mob.playAmbientSound();
            }
        }
    }
}
