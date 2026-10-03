package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Discipulo de la Secta del Monte Hua (Mount Hua Orthodox Disciple / 华山派弟子)
 * Espadachin ortodoxo del Reino Murim devoto a las Tecnicas de la Espada de las Flores de Ciruelo.
 * Neutral ante cultivadores pacificos y hostil ante demonios, monstruos y sectarios de sangre.
 */
public class MountHuaDiscipleEntity extends PathfinderMob {

    private int dashCooldown = 60;
    private int blossomSlashCooldown = 80;

    public MountHuaDiscipleEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 65;
        this.setCustomName(Component.translatable("entity.awakening_system.mount_hua_disciple"));
        this.setCustomNameVisible(true);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()));
        this.addTag("mount_hua_disciple");
        this.addTag("orthodox_cultivator");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 130.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 9.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Defenderse si es atacado por cualquier entidad
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        // Hostilidad automatica ante cultistas demoniacos, zombis saltadores (Jiangshi) y sectarios de sangre
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, DemonicCultivatorEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, JiangshiEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, BloodSectDiscipleEntity.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, BloodPatriarchBossEntity.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                m -> !(m instanceof MountHuaDiscipleEntity)));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            if (dashCooldown > 0) dashCooldown--;
            if (blossomSlashCooldown > 0) blossomSlashCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);

                // Paso de Qinggong del Monte Hua (Abalanzarse con rafaga de petalos de ciruelo)
                if (dashCooldown <= 0 && distSq >= 16.0D && distSq <= 144.0D && this.hasLineOfSight(target)) {
                    Vec3 diff = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(diff.x * 1.15D, 0.36D, diff.z * 1.15D);
                    this.playSound(SoundEvents.WIND_CHARGE_BURST.value(), 1.1F, 1.3F);

                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CHERRY_LEAVES, this.getX(), this.getY() + 0.6D, this.getZ(),
                                25, 0.5D, 0.3D, 0.5D, 0.05D);
                    }
                    dashCooldown = 80 + this.random.nextInt(30);
                }

                // Tecnica de los 24 Movimientos de la Espada de Ciruelo en cuerpo a cuerpo
                if (blossomSlashCooldown <= 0 && distSq <= 12.0D) {
                    this.swing(InteractionHand.MAIN_HAND);
                    this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.15F);
                    target.hurt(this.damageSources().mobAttack(this), 18.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(),
                                3, 0.2D, 0.2D, 0.2D, 0.05D);
                        sl.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY() + 1.0D, target.getZ(),
                                30, 0.6D, 0.6D, 0.6D, 0.1D);
                    }
                    blossomSlashCooldown = 90 + this.random.nextInt(35);
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Intercambio amistoso: 4 Esmeraldas por 1 Talisman Taoista de Sellado
        if (held.is(Items.EMERALD) && held.getCount() >= 4) {
            if (!player.getAbilities().instabuild) {
                held.shrink(4);
            }
            ItemStack talisman = new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 1);
            if (!player.addItem(talisman)) {
                player.drop(talisman, false);
            }
            this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F, 1.3F);

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.02D);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.05D);
            }
            player.displayClientMessage(Component.translatable("awakening_system.npc.mount_hua.gift_talisman"), false);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Saludo taoista ortodoxo de cortesia
        if (hand == InteractionHand.MAIN_HAND && !this.level().isClientSide) {
            this.playSound(SoundEvents.VILLAGER_YES, 0.9F, 1.1F);
            player.displayClientMessage(Component.translatable("awakening_system.npc.mount_hua.greeting"), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        this.spawnAtLocation(SystemItems.coin(60 + this.random.nextInt(40)));

        if (this.random.nextFloat() < 0.35F) {
            this.spawnAtLocation(new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 1));
        }
        if (this.random.nextFloat() < 0.20F) {
            this.spawnAtLocation(SystemItems.coin(100 + this.random.nextInt(100)));
        }
        if (this.random.nextFloat() < 0.05F) {
            this.spawnAtLocation(new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get(), 1));
        }
    }
}
