package com.soybe.awakeningsystem.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Hilichurl Tirador especializado con ballesta y flechas elementales.
 */
public class HilichurlShooterEntity extends HilichurlEntity {

    public HilichurlShooterEntity(EntityType<? extends HilichurlEntity> type, Level level) {
        super(type, level);
        this.setRole(HilichurlRole.SHOOTER);
    }
}
