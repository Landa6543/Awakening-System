package com.soybe.awakeningsystem.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Hilichurl Berserker especializado con antorcha llameante en carrera frenética.
 */
public class HilichurlBerserkerEntity extends HilichurlEntity {

    public HilichurlBerserkerEntity(EntityType<? extends HilichurlEntity> type, Level level) {
        super(type, level);
        this.setRole(HilichurlRole.BERSERKER);
    }
}
