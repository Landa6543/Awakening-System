package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class DokkaebiEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> QUEST_TITLE = SynchedEntityData.defineId(DokkaebiEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> QUEST_DESC = SynchedEntityData.defineId(DokkaebiEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> QUEST_PROGRESS = SynchedEntityData.defineId(DokkaebiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUEST_TARGET = SynchedEntityData.defineId(DokkaebiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUEST_REWARD_COINS = SynchedEntityData.defineId(DokkaebiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> QUEST_ACTIVE = SynchedEntityData.defineId(DokkaebiEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> QUEST_TYPE = SynchedEntityData.defineId(DokkaebiEntity.class, EntityDataSerializers.STRING);

    private UUID boundPlayerUuid = null;
    private int lifetimeTicks = 0;
    private static final int MAX_LIFETIME = 12000; // 10 minutos antes de despedirse

    public DokkaebiEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.setNoGravity(true);
        this.setCustomName(Component.literal("\u00A7e\u00A7lDokkaebi Bihyung"));
        this.setCustomNameVisible(true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(QUEST_TITLE, "Ninguno");
        builder.define(QUEST_DESC, "No hay misión activa.");
        builder.define(QUEST_PROGRESS, 0);
        builder.define(QUEST_TARGET, 1);
        builder.define(QUEST_REWARD_COINS, 250);
        builder.define(QUEST_ACTIVE, false);
        builder.define(QUEST_TYPE, "none");
    }

    public void bindPlayer(ServerPlayer player) {
        this.boundPlayerUuid = player.getUUID();
    }

    @Override
    public void tick() {
        super.tick();

        // Chispas eléctricas y aura de transmisión
        if (this.level().isClientSide() && this.random.nextInt(4) == 0) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D;
            double py = this.getY() + 0.3D + this.random.nextDouble() * 0.5D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D;
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.02D, 0);
        }

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
            lifetimeTicks++;

            // Seguir al jugador vinculado flotando suavemente a su alrededor
            if (boundPlayerUuid != null) {
                Player target = sl.getPlayerByUUID(boundPlayerUuid);
                if (target != null && target.isAlive() && target.level() == this.level()) {
                    double distSq = this.distanceToSqr(target);
                    if (distSq > 16.0D) {
                        this.getNavigation().moveTo(target.getX(), target.getY() + 1.5D, target.getZ(), 1.2D);
                    } else if (distSq > 256.0D) {
                        // Teletransporte seguro si está muy lejos
                        net.minecraft.world.phys.Vec3 safe = com.soybe.awakeningsystem.TeleportHelper.findSafeTeleportPos(this.level(), this, target.getX() + 1.5D, target.getY() + 0.5D, target.getZ() + 1.5D, target.position());
                        this.teleportTo(safe.x, safe.y, safe.z);
                    }
                }
            }

            // Despedida si expira el tiempo
            if (lifetimeTicks >= MAX_LIFETIME && !isQuestActive()) {
                dismissWithParticles();
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer sp)) {
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (boundPlayerUuid == null) {
            boundPlayerUuid = sp.getUUID();
        }

        // 1. Si no hay misión activa, ofrecer una
        if (!isQuestActive()) {
            assignRandomSubScenario(sp);
            return InteractionResult.SUCCESS;
        }

        // 2. Si la misión es de entregar ítems (hierro o diamante)
        String type = entityData.get(QUEST_TYPE);
        if ("collect_iron".equals(type) && !isQuestCompleted()) {
            ItemStack held = sp.getItemInHand(hand);
            if (held.is(Items.IRON_INGOT)) {
                int needed = entityData.get(QUEST_TARGET) - entityData.get(QUEST_PROGRESS);
                int take = Math.min(held.getCount(), needed);
                held.shrink(take);
                addProgress(take, sp);
                return InteractionResult.SUCCESS;
            }
        } else if ("mine_diamonds".equals(type) && !isQuestCompleted()) {
            ItemStack held = sp.getItemInHand(hand);
            if (held.is(Items.DIAMOND)) {
                int needed = entityData.get(QUEST_TARGET) - entityData.get(QUEST_PROGRESS);
                int take = Math.min(held.getCount(), needed);
                held.shrink(take);
                addProgress(take, sp);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Si la misión está completa, entregar recompensa
        if (isQuestCompleted()) {
            completeSubScenario(sp);
            return InteractionResult.SUCCESS;
        }

        // 4. Misión en progreso: mostrar estado
        int current = entityData.get(QUEST_PROGRESS);
        int target = entityData.get(QUEST_TARGET);
        String qTitleKey = entityData.get(QUEST_TITLE);
        Component qTitleComp = qTitleKey.startsWith("awakening_system.") ? Component.translatable(qTitleKey) : Component.literal(qTitleKey);
        sp.displayClientMessage(Component.translatable("awakening_system.dokkaebi.quest_in_progress", qTitleComp.getString(), current, target), true);
        playDokkaebiSound();
        return InteractionResult.SUCCESS;
    }

    public void assignRandomSubScenario(ServerPlayer player) {
        int pick = this.random.nextInt(5);
        String type;
        String titleKey;
        String descKey;
        int target;
        int rewardCoins;

        switch (pick) {
            case 0 -> {
                type = "kill_monsters";
                titleKey = "awakening_system.dokkaebi.quest.kill_monsters.title";
                descKey = "awakening_system.dokkaebi.quest.kill_monsters.desc";
                target = 6;
                rewardCoins = 350;
            }
            case 1 -> {
                type = "combo_strike";
                titleKey = "awakening_system.dokkaebi.quest.combo_strike.title";
                descKey = "awakening_system.dokkaebi.quest.combo_strike.desc";
                target = 6;
                rewardCoins = 300;
            }
            case 2 -> {
                type = "collect_iron";
                titleKey = "awakening_system.dokkaebi.quest.collect_iron.title";
                descKey = "awakening_system.dokkaebi.quest.collect_iron.desc";
                target = 8;
                rewardCoins = 400;
            }
            case 3 -> {
                type = "survive_damage";
                titleKey = "awakening_system.dokkaebi.quest.survive_damage.title";
                descKey = "awakening_system.dokkaebi.quest.survive_damage.desc";
                target = 10;
                rewardCoins = 350;
            }
            default -> {
                type = "mine_diamonds";
                titleKey = "awakening_system.dokkaebi.quest.mine_diamonds.title";
                descKey = "awakening_system.dokkaebi.quest.mine_diamonds.desc";
                target = 1;
                rewardCoins = 600;
            }
        }

        entityData.set(QUEST_TYPE, type);
        entityData.set(QUEST_TITLE, titleKey);
        entityData.set(QUEST_DESC, descKey);
        entityData.set(QUEST_PROGRESS, 0);
        entityData.set(QUEST_TARGET, target);
        entityData.set(QUEST_REWARD_COINS, rewardCoins);
        entityData.set(QUEST_ACTIVE, true);

        playDokkaebiSound();
        AwakeningSystemMod.notify(player,
                "awakening_system.dokkaebi.quest_started_title",
                "awakening_system.dokkaebi.quest_started_msg",
                0xFFFFD700,
                Component.translatable(titleKey).getString() + ":::" + Component.translatable(descKey).getString() + ":::" + rewardCoins);
    }

    public void addProgress(int amount, ServerPlayer player) {
        if (!isQuestActive()) return;
        int current = entityData.get(QUEST_PROGRESS) + amount;
        int target = entityData.get(QUEST_TARGET);
        entityData.set(QUEST_PROGRESS, Math.min(current, target));

        if (current >= target) {
            playDokkaebiSound();
            AwakeningSystemMod.notify(player,
                    "awakening_system.dokkaebi.quest_target_completed_title",
                    "awakening_system.dokkaebi.quest_target_completed_msg",
                    0xFF55FF55);
        } else {
            player.displayClientMessage(Component.translatable("awakening_system.dokkaebi.quest_progress", current, target), true);
        }
    }

    public void completeSubScenario(ServerPlayer player) {
        int coins = entityData.get(QUEST_REWARD_COINS);
        SystemData data = SystemEvents.data(player);
        data.addCoins(coins);
        SystemEvents.awardExperience(player, data, 150);
        AwakeningSystemMod.sync(player);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.2F, 1.0F);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 0.5D, this.getZ(), 40, 0.4D, 0.5D, 0.4D, 0.1D);
        }

        AwakeningSystemMod.notify(player,
                "awakening_system.dokkaebi.scenario_cleared_title",
                "awakening_system.dokkaebi.scenario_cleared_msg",
                0xFFFFD700,
                coins);

        entityData.set(QUEST_ACTIVE, false);

        // Se despide con chispas tras cumplir
        dismissWithParticles();
    }

    public void dismissWithParticles() {
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.2F);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.5D, this.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.15D);
            sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.5D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
        }
        this.discard();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        // Dokkaebi Bihyung flota libremente; no produce sonidos de pasos en madera ni piedra
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return null;
    }

    private void playDokkaebiSound() {
        this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.3F, 1.45F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.NEUTRAL, 1.1F, 1.6F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.5F);
    }

    public boolean isQuestActive() {
        return entityData.get(QUEST_ACTIVE);
    }

    public boolean isQuestCompleted() {
        return isQuestActive() && entityData.get(QUEST_PROGRESS) >= entityData.get(QUEST_TARGET);
    }

    public String getQuestType() {
        return entityData.get(QUEST_TYPE);
    }

    public String getQuestTitle() {
        return entityData.get(QUEST_TITLE);
    }

    public String getQuestDesc() {
        return entityData.get(QUEST_DESC);
    }

    public int getQuestProgress() {
        return entityData.get(QUEST_PROGRESS);
    }

    public int getQuestTarget() {
        return entityData.get(QUEST_TARGET);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // El Dokkaebi está protegido por la barrera del Star Stream
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 1.2F);
            sl.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 0.5D, this.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("QuestTitle", entityData.get(QUEST_TITLE));
        tag.putString("QuestDesc", entityData.get(QUEST_DESC));
        tag.putString("QuestType", entityData.get(QUEST_TYPE));
        tag.putInt("QuestProgress", entityData.get(QUEST_PROGRESS));
        tag.putInt("QuestTarget", entityData.get(QUEST_TARGET));
        tag.putInt("QuestRewardCoins", entityData.get(QUEST_REWARD_COINS));
        tag.putBoolean("QuestActive", entityData.get(QUEST_ACTIVE));
        tag.putInt("LifetimeTicks", lifetimeTicks);
        if (boundPlayerUuid != null) {
            tag.putUUID("BoundPlayer", boundPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(QUEST_TITLE, tag.getString("QuestTitle"));
        entityData.set(QUEST_DESC, tag.getString("QuestDesc"));
        entityData.set(QUEST_TYPE, tag.getString("QuestType"));
        entityData.set(QUEST_PROGRESS, tag.getInt("QuestProgress"));
        entityData.set(QUEST_TARGET, tag.getInt("QuestTarget"));
        entityData.set(QUEST_REWARD_COINS, tag.getInt("QuestRewardCoins"));
        entityData.set(QUEST_ACTIVE, tag.getBoolean("QuestActive"));
        lifetimeTicks = tag.getInt("LifetimeTicks");
        if (tag.hasUUID("BoundPlayer")) {
            boundPlayerUuid = tag.getUUID("BoundPlayer");
        }
    }
}
