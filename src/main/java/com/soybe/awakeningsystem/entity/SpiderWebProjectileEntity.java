package com.soybe.awakeningsystem.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Proyectil viscoso de telaraña lanzado por la Araña Madre (Mother Spider).
 * Enreda, ralentiza gravemente e intoxica al objetivo, y al impactar puede generar telaraña.
 */
public class SpiderWebProjectileEntity extends ThrowableItemProjectile {

    private float impactDamage = 6.0F;
    private boolean isBossWeb = false;

    public SpiderWebProjectileEntity(EntityType<? extends SpiderWebProjectileEntity> type, Level level) {
        super(type, level);
    }

    public SpiderWebProjectileEntity(Level level, LivingEntity shooter, float damage, boolean isBossWeb) {
        super(ModEntities.SPIDER_WEB_PROJECTILE.get(), shooter, level);
        this.impactDamage = damage;
        this.isBossWeb = isBossWeb;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.COBWEB);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity target && !this.level().isClientSide()) {
            Entity owner = this.getOwner();
            target.hurt(this.damageSources().thrown(this, owner != null ? owner : this), this.impactDamage);

            // Ralentización severa y veneno
            int slowAmp = isBossWeb ? 3 : 2; // Lentitud IV o III
            int poisonAmp = isBossWeb ? 1 : 0; // Veneno II o I
            int duration = isBossWeb ? 120 : 80;

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slowAmp, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, poisonAmp, false, true));

            // Intentar colocar telaraña a los pies del objetivo si está en aire
            BlockPos targetPos = target.blockPosition();
            if (this.level().getBlockState(targetPos).isAir()) {
                this.level().setBlockAndUpdate(targetPos, Blocks.COBWEB.defaultBlockState());
            }

            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SLIME_BLOCK_PLACE, SoundSource.HOSTILE, 1.0F, 0.8F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            BlockPos placePos = result.getBlockPos().relative(result.getDirection());
            if (this.level().getBlockState(placePos).isAir()) {
                this.level().setBlockAndUpdate(placePos, Blocks.COBWEB.defaultBlockState());
            }
            this.level().playSound(null, placePos.getX(), placePos.getY(), placePos.getZ(),
                    SoundEvents.SLIME_BLOCK_PLACE, SoundSource.HOSTILE, 0.9F, 0.9F);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            // Partículas de telaraña al impactar
            serverLevel.sendParticles(ParticleTypes.ITEM_COBWEB,
                    this.getX(), this.getY(), this.getZ(),
                    isBossWeb ? 20 : 12, 0.3D, 0.3D, 0.3D, 0.05D);
            this.discard();
        }
    }
}
