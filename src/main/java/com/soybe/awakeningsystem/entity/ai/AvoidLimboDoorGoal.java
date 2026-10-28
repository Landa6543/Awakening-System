package com.soybe.awakeningsystem.entity.ai;

import com.soybe.awakeningsystem.limbo.LimboWorldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Meta de Evitación del Perímetro de la Puerta del Limbo:
 * Las criaturas del Limbo no pueden acercarse a menos de 50 bloques de las 4 Puertas Cardinales.
 * Si alguna entidad entra o se encuentra dentro del perímetro de 52 bloques, se detiene y
 * retrocede inmediatamente en dirección opuesta hasta una distancia segura (> 55 bloques).
 */
public class AvoidLimboDoorGoal extends Goal {

    private final PathfinderMob mob;
    private final double speedModifier;
    private BlockPos dangerousDoor = null;

    public AvoidLimboDoorGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!LimboWorldManager.isLimboDimension(this.mob.level())) {
            return false;
        }

        Vec3 pos = this.mob.position();
        for (BlockPos door : LimboWorldManager.CARDINAL_DOORS) {
            double dx = pos.x - (door.getX() + 0.5D);
            double dz = pos.z - (door.getZ() + 0.5D);
            // Radio de 52 bloques
            if ((dx * dx + dz * dz) <= (52.0D * 52.0D)) {
                this.dangerousDoor = door;
                return true;
            }
        }
        this.dangerousDoor = null;
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.dangerousDoor == null) return false;
        Vec3 pos = this.mob.position();
        double dx = pos.x - (this.dangerousDoor.getX() + 0.5D);
        double dz = pos.z - (this.dangerousDoor.getZ() + 0.5D);
        // Continuar alejándose hasta estar al menos a 56 bloques
        return (dx * dx + dz * dz) < (56.0D * 56.0D);
    }

    @Override
    public void start() {
        this.mob.setTarget(null);
        retreatFromDoor();
    }

    @Override
    public void tick() {
        this.mob.setTarget(null);
        if (this.mob.getNavigation().isDone() || this.mob.tickCount % 20 == 0) {
            retreatFromDoor();
        }
    }

    private void retreatFromDoor() {
        if (this.dangerousDoor == null) return;
        Vec3 pos = this.mob.position();
        double dx = pos.x - (this.dangerousDoor.getX() + 0.5D);
        double dz = pos.z - (this.dangerousDoor.getZ() + 0.5D);

        Vec3 away = new Vec3(dx, 0.0D, dz).normalize();
        if (away.lengthSqr() < 1.0E-4D) {
            away = new Vec3(1.0D, 0.0D, 0.0D);
        }

        Vec3 retreatTarget = pos.add(away.scale(12.0D));
        this.mob.getNavigation().moveTo(retreatTarget.x, retreatTarget.y, retreatTarget.z, this.speedModifier);
    }

    @Override
    public void stop() {
        this.dangerousDoor = null;
        this.mob.getNavigation().stop();
    }
}
