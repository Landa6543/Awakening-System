package com.soybe.awakeningsystem.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GiantSandwormHeadPart extends GiantSandwormBodyPart {

    public GiantSandwormHeadPart(EntityType<? extends GiantSandwormHeadPart> type, Level level) {
        super(type, level);
        this.setRenderScale(15);
    }

    @Override
    public void playerTouch(Player player) {
        if (!level().isClientSide && this.owner != null && this.owner.isAlive()) {
            boolean hurt = player.hurt(damageSources().mobAttack(this.owner), 24.0F);
            if (hurt) {
                // Knockback upward and apply desert sand trauma
                Vec3 throwDir = player.position().subtract(position()).normalize().scale(1.5).add(0, 0.8, 0);
                player.setDeltaMovement(throwDir);
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                this.playSound(SoundEvents.RAVAGER_ATTACK, 2.5F, 0.6F);
            }
        }
    }
}
