package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Jefe Legendario Semanal de Teyvat: Azhdaha (Señor de los Dragartos y Dragón Terrestre Sellado).
 * Titán colosal acorazado de roca milenaria que habita en las profundidades de la Caverna Subterránea.
 * Cuenta con Pisoteo Sísmico, Caída de Estalactitas/Rocas, Barrido de Cola y Modo de Furia Ígnea Subterránea.
 */
public class AzhdahaBossEntity extends Monster {

    public static final EntityDataAccessor<Boolean> DATA_INFUSED =
            SynchedEntityData.defineId(AzhdahaBossEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM =
            SynchedEntityData.defineId(AzhdahaBossEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§6§lAzhdaha §7- §eSeñor de los Dragartos y Dragón Terrestre Sellado"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int slamCooldown = 90;
    private int rockFallCooldown = 150;
    private int tailSwipeCooldown = 70;
    private int animTicks = 0;
    private boolean hasTriggeredInfusion = false;

    public AzhdahaBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.setCustomName(Component.literal("§6§lAzhdaha"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 36.0D)
                .add(Attributes.ARMOR, 26.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INFUSED, false);
        builder.define(DATA_ATTACK_ANIM, 0);
    }

    public boolean isInfused() {
        return this.entityData.get(DATA_INFUSED);
    }

    public void setInfused(boolean infused) {
        this.entityData.set(DATA_INFUSED, infused);
        if (infused) {
            bossEvent.setName(Component.literal("§c§lAzhdaha §7- §6Furia Primordial de la Tierra"));
            bossEvent.setColor(BossEvent.BossBarColor.RED);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(34.0D);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.26D);
        }
    }

    public int getAttackAnim() {
        return this.entityData.get(DATA_ATTACK_ANIM);
    }

    public void setAttackAnim(int anim) {
        this.entityData.set(DATA_ATTACK_ANIM, anim);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !hasTriggeredInfusion && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            triggerInfusion();
        }
        return hurt;
    }

    private void triggerInfusion() {
        this.hasTriggeredInfusion = true;
        setInfused(true);

        if (this.level() instanceof ServerLevel sl) {
            // Rugido y temblor sísmico masivo
            sl.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.5F, 0.6F);
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.8F);

            sl.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.5D, this.getZ(), 80, 2.5D, 1.0D, 2.5D, 0.2D);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);

            for (Player p : sl.players()) {
                if (p.distanceToSqr(this) < 3600.0D) {
                    p.displayClientMessage(Component.literal("§6§lAzhdaha: §c\"¡La arrogancia de los mortales sacudirá los cimientos de la tierra!\""), false);
                }
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // Manejo de animaciones
        if (animTicks > 0) {
            animTicks--;
            if (animTicks <= 0) {
                setAttackAnim(0);
            }
        }

        if (this.level().isClientSide) {
            // Partículas de tierra o magma alrededor del cuerpo
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()),
                        this.getRandomX(1.8D), this.getY() + 0.2D, this.getRandomZ(1.8D), 0, 0.1D, 0);
            }
            if (isInfused() && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.FLAME, this.getRandomX(1.6D), this.getY() + 1.2D, this.getRandomZ(1.6D), 0, 0.08D, 0);
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        double distSq = this.distanceToSqr(target);

        slamCooldown--;
        rockFallCooldown--;
        tailSwipeCooldown--;

        // 1. Pisoteo Sísmico (Seismic Slam)
        if (slamCooldown <= 0 && distSq < 144.0D) {
            slamCooldown = isInfused() ? 70 : 100;
            performSeismicSlam();
            return;
        }

        // 2. Caída de Rocas del Techo (Rock Avalanche)
        if (rockFallCooldown <= 0 && distSq > 36.0D) {
            rockFallCooldown = isInfused() ? 120 : 160;
            performRockFall(target);
            return;
        }

        // 3. Barrido de Cola (Tail Swipe si el jugador intenta flanquearlo)
        if (tailSwipeCooldown <= 0 && distSq < 64.0D) {
            Vec3 toTarget = target.position().subtract(this.position()).normalize();
            double dot = toTarget.dot(this.getLookAngle());
            if (dot < -0.2D) { // Está detrás
                tailSwipeCooldown = 60;
                performTailSwipe(target);
            }
        }
    }

    private void performSeismicSlam() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        setAttackAnim(1); // Slam anim
        animTicks = 25;

        this.playSound(SoundEvents.ANVIL_LAND, 2.0F, 0.5F);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.8F, 0.7F);

        // Ondas de choque concéntricas en radio de 9 bloques
        for (int r = 3; r <= 9; r += 3) {
            for (int angle = 0; angle < 360; angle += 20) {
                double rad = Math.toRadians(angle);
                double px = this.getX() + Math.cos(rad) * r;
                double pz = this.getZ() + Math.sin(rad) * r;
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        px, this.getY() + 0.1D, pz, 6, 0.2D, 0.5D, 0.2D, 0.15D);
                if (isInfused()) {
                    sl.sendParticles(ParticleTypes.LAVA, px, this.getY() + 0.1D, pz, 2, 0.1D, 0.3D, 0.1D, 0.05D);
                }
            }
        }

        // Golpear a todas las entidades en rango y lanzarlas al aire
        List<LivingEntity> hit = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(9.0D), e -> e != this);
        for (LivingEntity e : hit) {
            e.hurt(this.damageSources().mobAttack(this), isInfused() ? 28.0F : 20.0F);
            e.setDeltaMovement(e.getDeltaMovement().x, 0.85D, e.getDeltaMovement().z);
            e.hurtMarked = true;
            if (isInfused()) {
                e.setRemainingFireTicks(80);
            }
        }
    }

    private void performRockFall(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        this.playSound(SoundEvents.WARDEN_ROAR, 2.0F, 0.7F);
        if (target instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.literal("§6§l¡Cuidado! §eEl techo de la caverna se está derrumbando..."), true);
        }

        // Marcas de caída de roca sobre la posición del objetivo
        Vec3 tPos = target.position();
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, tPos.x, tPos.y + 0.1D, tPos.z, 25, 1.5D, 0.1D, 1.5D, 0.05D);

        // Desprendimiento retardado
        sl.getServer().tell(new net.minecraft.server.TickTask(sl.getServer().getTickCount() + 20, () -> {
            if (!this.isAlive()) return;
            sl.playSound(null, tPos.x, tPos.y, tPos.z, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 1.8F, 0.5F);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                    tPos.x, tPos.y + 1.0D, tPos.z, 50, 1.8D, 1.0D, 1.8D, 0.25D);
            sl.sendParticles(ParticleTypes.EXPLOSION, tPos.x, tPos.y + 0.5D, tPos.z, 3, 0.5D, 0.5D, 0.5D, 0.0D);

            List<LivingEntity> hit = sl.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(tPos.x - 3, tPos.y - 1, tPos.z - 3, tPos.x + 3, tPos.y + 3, tPos.z + 3), e -> e != this);
            for (LivingEntity e : hit) {
                e.hurt(this.damageSources().mobAttack(this), 24.0F);
                e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0));
            }
        }));
    }

    private void performTailSwipe(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        setAttackAnim(2); // Tail anim
        animTicks = 20;

        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.8F);

        Vec3 look = this.getLookAngle();
        Vec3 behind = this.position().subtract(look.scale(3.0D));

        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, behind.x, behind.y + 0.8D, behind.z, 4, 1.5D, 0.2D, 1.5D, 0.05D);

        List<LivingEntity> hit = sl.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(behind.x - 3.5D, behind.y - 1, behind.z - 3.5D, behind.x + 3.5D, behind.y + 3, behind.z + 3.5D), e -> e != this);
        for (LivingEntity e : hit) {
            e.hurt(this.damageSources().mobAttack(this), 22.0F);
            Vec3 knockback = e.position().subtract(this.position()).normalize().scale(2.2D).add(0, 0.4D, 0);
            e.setDeltaMovement(knockback);
            e.hurtMarked = true;
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 2.0F, 0.6F);

            for (Player p : sl.players()) {
                if (p.distanceToSqr(this) < 3600.0D) {
                    p.displayClientMessage(Component.literal("§6§lAzhdaha: §f\"Morax... El rencor de la tierra nunca será olvidado...\""), false);
                }
            }

            // Drops masivos de Jefe Semanal
            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            // 1. Protogemas (150) - Exclusivo Teyvat
            if (isTeyvat) {
                spawnItem(sl, new ItemStack(SystemItems.PRIMOGEM.get(), 150));
            }

            // 2. Mora (80,000) - Exclusivo Teyvat
            if (isTeyvat) {
                if (SystemItems.MORA_BUNDLE.isBound()) {
                    spawnItem(sl, new ItemStack(SystemItems.MORA_BUNDLE.get(), 3));
                } else {
                    spawnItem(sl, new ItemStack(SystemItems.MORA.get(), 64));
                }
            } else {
                if (SystemItems.AWAKENING_COIN.isBound()) {
                    spawnItem(sl, new ItemStack(SystemItems.AWAKENING_COIN.get(), 32));
                }
            }

            // 3. Destinos Entrelazados (2 tiradas de gacha)
            if (SystemItems.INTERTWINED_FATE.isBound()) {
                spawnItem(sl, new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2));
            }

            // 4. Artefacto Dorado 5★ garantizado
            ItemStack artifact = createRandomGenshinArtifact();
            if (!artifact.isEmpty()) {
                spawnItem(sl, artifact);
            }

            // 5. Minerales ancestrales
            spawnItem(sl, new ItemStack(Items.DIAMOND, 2));
            spawnItem(sl, new ItemStack(Items.GOLD_INGOT, 10));
            spawnItem(sl, new ItemStack(Items.NETHERITE_SCRAP, 1));
        }
    }

    private void spawnItem(ServerLevel level, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity item = new ItemEntity(level, this.getX(), this.getY() + 1.0D, this.getZ(), stack);
            item.setDeltaMovement(this.random.nextGaussian() * 0.05D, 0.3D, this.random.nextGaussian() * 0.05D);
            level.addFreshEntity(item);
        }
    }

    private ItemStack createRandomGenshinArtifact() {
        try {
            var items = net.minecraft.core.registries.BuiltInRegistries.ITEM;
            List<GenshinArtifactItem> arts = new ArrayList<>();
            for (var it : items) {
                if (it instanceof GenshinArtifactItem art) {
                    arts.add(art);
                }
            }
            if (!arts.isEmpty()) {
                GenshinArtifactItem chosen = arts.get(this.random.nextInt(arts.size()));
                return new ItemStack(chosen);
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }
}
