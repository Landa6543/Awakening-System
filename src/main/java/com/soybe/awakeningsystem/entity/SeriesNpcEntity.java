package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.GuildRankManager;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.OpenGuildReceptionPayload;
import com.soybe.awakeningsystem.GuildReceptionActionPayload;
import net.minecraft.ChatFormatting;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class SeriesNpcEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> PROFILE_ID =
            SynchedEntityData.defineId(SeriesNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(SeriesNpcEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> FOLLOWING =
            SynchedEntityData.defineId(SeriesNpcEntity.class, EntityDataSerializers.BOOLEAN);

    public static final Map<UUID, SeriesNpcEntity> LOADED_SERIES_NPCS = new ConcurrentHashMap<>();
    public static final Map<String, SeriesNpcEntity> LOADED_SERIES_NPCS_BY_PROFILE = new ConcurrentHashMap<>();

    private BlockPos homePos = null;

    public SeriesNpcEntity(EntityType<? extends SeriesNpcEntity> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public SeriesNpcEntity(EntityType<? extends SeriesNpcEntity> entityType, Level level, NpcProfile profile) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.setNpcProfile(profile);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide()) {
            LOADED_SERIES_NPCS.put(this.getUUID(), this);
            if (this.getNpcProfile() != null) {
                LOADED_SERIES_NPCS_BY_PROFILE.put(this.getNpcProfile().id(), this);
            }
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (!this.level().isClientSide()) {
            LOADED_SERIES_NPCS.remove(this.getUUID());
            if (this.getNpcProfile() != null) {
                LOADED_SERIES_NPCS_BY_PROFILE.remove(this.getNpcProfile().id());
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new DialogueTalkGoal());
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CompanionMeleeAttackGoal(this, 1.35D, true));
        this.goalSelector.addGoal(3, new FollowCompanionOwnerGoal(this));
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal(this, 0.6D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.35D) {
            @Override
            public boolean canUse() {
                if (SeriesNpcEntity.this.isInDialogue()) return false;
                if (SeriesNpcEntity.this.isFollowing()) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.ELENA_RECEPTIONIST) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.BLACK_MARKET_DEALER) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.CASINO_RECEPTIONIST) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.HUNTER_EVALUATOR) return false;
                return super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                if (SeriesNpcEntity.this.isInDialogue()) return false;
                if (SeriesNpcEntity.this.isFollowing()) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.ELENA_RECEPTIONIST) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.BLACK_MARKET_DEALER) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.CASINO_RECEPTIONIST) return false;
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.HUNTER_EVALUATOR) return false;
                return super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Companion Combat Targeting Goals
        this.targetSelector.addGoal(1, new TargetGoal(this, false) {
            @Override
            public boolean canUse() {
                if (!SeriesNpcEntity.this.isFollowing() || SeriesNpcEntity.this.isInDialogue()) return false;
                Player owner = SeriesNpcEntity.this.getOwner();
                if (owner == null) return false;
                LivingEntity lastAttacker = owner.getLastHurtByMob();
                if (lastAttacker != null && lastAttacker.isAlive() && !(lastAttacker instanceof Player)) {
                    SeriesNpcEntity.this.setTarget(lastAttacker);
                    return true;
                }
                return false;
            }
        });
        this.targetSelector.addGoal(2, new TargetGoal(this, false) {
            @Override
            public boolean canUse() {
                if (!SeriesNpcEntity.this.isFollowing() || SeriesNpcEntity.this.isInDialogue()) return false;
                Player owner = SeriesNpcEntity.this.getOwner();
                if (owner == null) return false;
                LivingEntity ownerTarget = owner.getLastHurtMob();
                if (ownerTarget != null && ownerTarget.isAlive() && !(ownerTarget instanceof Player)) {
                    SeriesNpcEntity.this.setTarget(ownerTarget);
                    return true;
                }
                return false;
            }
        });
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true) {
            @Override
            public boolean canUse() {
                if (!SeriesNpcEntity.this.isFollowing() || SeriesNpcEntity.this.isInDialogue()) return false;
                return super.canUse();
            }
        });
    }

    public boolean isInDialogue() {
        return !this.level().isClientSide() && com.soybe.awakeningsystem.dialogue.NpcDialogueManager.isNpcSpeaking(this.getId());
    }

    public Player getDialogueSpeaker() {
        if (!isInDialogue() || !(this.level() instanceof ServerLevel sl)) return null;
        UUID speakerUuid = com.soybe.awakeningsystem.dialogue.NpcDialogueManager.getSpeakerForNpc(this.getId());
        return speakerUuid != null ? sl.getPlayerByUUID(speakerUuid) : null;
    }

    private class DialogueTalkGoal extends net.minecraft.world.entity.ai.goal.Goal {
        public DialogueTalkGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return SeriesNpcEntity.this.isInDialogue();
        }

        @Override
        public boolean canContinueToUse() {
            return SeriesNpcEntity.this.isInDialogue();
        }

        @Override
        public void start() {
            SeriesNpcEntity.this.getNavigation().stop();
            SeriesNpcEntity.this.setDeltaMovement(0, SeriesNpcEntity.this.getDeltaMovement().y, 0);
        }

        @Override
        public void tick() {
            SeriesNpcEntity.this.getNavigation().stop();
            Player speaker = SeriesNpcEntity.this.getDialogueSpeaker();
            if (speaker != null) {
                SeriesNpcEntity.this.getLookControl().setLookAt(speaker, 30.0F, 30.0F);
            }
        }
    }

    private class FollowCompanionOwnerGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final SeriesNpcEntity npc;
        private Player owner;
        private int timeToRecalcPath;

        public FollowCompanionOwnerGoal(SeriesNpcEntity npc) {
            this.npc = npc;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!npc.isFollowing() || npc.isInDialogue()) return false;
            Player p = npc.getOwner();
            if (p == null || p.isSpectator()) return false;
            if (npc.distanceToSqr(p) < 9.0D) return false;
            this.owner = p;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!npc.isFollowing() || npc.isInDialogue()) return false;
            if (this.owner == null || !this.owner.isAlive() || this.owner.isSpectator()) return false;
            return npc.distanceToSqr(this.owner) > 6.25D;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.npc.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) return;
            this.npc.getLookControl().setLookAt(this.owner, 10.0F, (float) this.npc.getMaxHeadXRot());
            double distSq = this.npc.distanceToSqr(this.owner);

            if (distSq > 576.0D) {
                teleportNearOwner();
                return;
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                this.npc.getNavigation().moveTo(this.owner, 1.25D);
            }
        }

        private void teleportNearOwner() {
            BlockPos pPos = this.owner.blockPosition();
            for (int i = 0; i < 10; ++i) {
                int dx = npc.getRandom().nextInt(7) - 3;
                int dy = npc.getRandom().nextInt(3) - 1;
                int dz = npc.getRandom().nextInt(7) - 3;
                BlockPos targetPos = pPos.offset(dx, dy, dz);
                if (npc.level().getBlockState(targetPos).isAir() && npc.level().getBlockState(targetPos.above()).isAir()) {
                    npc.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                    npc.getNavigation().stop();
                    if (npc.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.PORTAL, npc.getX(), npc.getY() + 1.0D, npc.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.1D);
                    }
                    break;
                }
            }
        }
    }

    private class CompanionMeleeAttackGoal extends MeleeAttackGoal {
        public CompanionMeleeAttackGoal(SeriesNpcEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            if (SeriesNpcEntity.this.isInDialogue() || !SeriesNpcEntity.this.isFollowing()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (SeriesNpcEntity.this.isInDialogue() || !SeriesNpcEntity.this.isFollowing()) return false;
            return super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(target);
                if (this.mob.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.2D, target.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.15D);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROFILE_ID, NpcProfile.SUNG_JINWOO.id());
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(FOLLOWING, false);
    }

    public UUID getOwnerUuid() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUuid(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    public void setFollowing(boolean following) {
        this.entityData.set(FOLLOWING, following);
    }

    public Player getOwner() {
        UUID uuid = getOwnerUuid();
        if (uuid == null) return null;
        if (this.level() instanceof ServerLevel sl) {
            return sl.getPlayerByUUID(uuid);
        }
        return this.level().getPlayerByUUID(uuid);
    }

    public NpcProfile getNpcProfile() {
        return NpcProfile.getById(this.entityData.get(PROFILE_ID));
    }

    public NpcProfile getProfile() {
        return getNpcProfile();
    }

    public void setNpcProfile(NpcProfile profile) {
        if (profile == null) profile = NpcProfile.SUNG_JINWOO;
        this.entityData.set(PROFILE_ID, profile.id());
        this.updateCustomName();
        this.updateEquipmentForProfile();
    }

    public void setProfile(NpcProfile profile) {
        setNpcProfile(profile);
    }

    public void updateEquipmentForProfile() {
        NpcProfile p = getNpcProfile();
        ItemStack weapon = p.getSignatureWeapon();
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, weapon);
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
    }

    public void updateCustomName() {
        NpcProfile p = getNpcProfile();
        ChatFormatting color = ChatFormatting.WHITE;
        String tc = p.titleColor();
        if (tc != null && tc.length() >= 2 && tc.charAt(0) == '§') {
            ChatFormatting found = ChatFormatting.getByCode(tc.charAt(1));
            if (found != null && found.isColor()) {
                color = found;
            }
        }
        boolean bold = tc != null && tc.contains("§l");

        MutableComponent nameComp = Component.translatableWithFallback("awakening_system.npc." + p.id() + ".name", p.displayName())
                .withStyle(color);
        if (bold) {
            nameComp.withStyle(ChatFormatting.BOLD);
        }

        String fallbackTitle = (p.title() != null && !p.title().isEmpty()) ? p.title() : p.displayName();
        MutableComponent titleComp = Component.translatableWithFallback("awakening_system.npc." + p.id() + ".title", fallbackTitle)
                .withStyle(ChatFormatting.GRAY);

        this.setCustomName(nameComp
                .append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
                .append(titleComp)
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY)));
        this.setCustomNameVisible(true);
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
        if (pos != null) {
            this.restrictTo(pos, 6);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("ProfileId", this.entityData.get(PROFILE_ID));
        if (getOwnerUuid() != null) {
            compound.putUUID("OwnerUUID", getOwnerUuid());
        }
        compound.putBoolean("Following", isFollowing());
        if (this.homePos != null) {
            compound.putInt("HomeX", this.homePos.getX());
            compound.putInt("HomeY", this.homePos.getY());
            compound.putInt("HomeZ", this.homePos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ProfileId")) {
            this.entityData.set(PROFILE_ID, compound.getString("ProfileId"));
        }
        if (compound.hasUUID("OwnerUUID")) {
            setOwnerUuid(compound.getUUID("OwnerUUID"));
        }
        if (compound.contains("Following")) {
            setFollowing(compound.getBoolean("Following"));
        }
        if (compound.contains("HomeX")) {
            this.homePos = new BlockPos(compound.getInt("HomeX"), compound.getInt("HomeY"), compound.getInt("HomeZ"));
            this.restrictTo(this.homePos, 6);
        }
        this.updateCustomName();
        this.updateEquipmentForProfile();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NpcProfile profile = getNpcProfile();
            String quote = profile.getRandomDialogue();

            int diagIdx = (int) (Math.random() * 4);
            AwakeningSystemMod.notify(serverPlayer,
                    "awakening_system.npc." + profile.id() + ".dialogue_title",
                    "awakening_system.npc." + profile.id() + ".dialogue." + diagIdx,
                    0xFFFFD700);

            net.minecraft.sounds.SoundEvent voice = profile.getVoiceSound();
            if (voice != null) {
                serverPlayer.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
                serverPlayer.playNotifySound(voice, SoundSource.VOICE, 1.2F, 1.0F);
            }

            if (profile == NpcProfile.ELENA_RECEPTIONIST) {
                CompoundTag pTag = serverPlayer.getPersistentData();
                int completed = pTag.getInt("bounties_total_completed");
                String currentRank = GuildRankManager.getGuildRank(completed);
                String lastPromoted = pTag.getString("last_promoted_guild_rank");
                if (lastPromoted.isEmpty()) lastPromoted = "F";

                int neededNext = GuildRankManager.getNextRankNeeded(completed);
                String nextRank = GuildRankManager.getNextRankName(completed);

                boolean canPromote = !currentRank.equals(lastPromoted);
                long lastBlessingTime = pTag.getLong("elena_last_blessing_time");
                long currentTime = serverPlayer.level().getGameTime();
                boolean canClaimBlessing = (currentTime - lastBlessingTime >= 24000L || lastBlessingTime == 0);

                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer,
                        new OpenGuildReceptionPayload(
                                currentRank, completed, neededNext, nextRank, canPromote, canClaimBlessing
                        ));
                serverPlayer.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            if (profile == NpcProfile.BLACK_MARKET_DEALER) {
                com.soybe.awakeningsystem.blackmarket.BlackMarketServerHandler.openMarket(serverPlayer);
                return InteractionResult.SUCCESS;
            }

            if (profile == NpcProfile.CASINO_RECEPTIONIST) {
                com.soybe.awakeningsystem.SystemData data = SystemEvents.data(serverPlayer);
                boolean isVip = data.isCasinoVip();
                var passItem = SystemItems.CASINO_VIP_PASS.get();
                boolean hasPass = serverPlayer.getInventory().contains(new ItemStack(passItem));

                if (isVip) {
                    if (!hasPass) {
                        ItemStack pass = new ItemStack(passItem);
                        com.soybe.awakeningsystem.item.CasinoVipPassItem.bindToPlayer(pass, serverPlayer);
                        serverPlayer.getInventory().add(pass);
                        serverPlayer.displayClientMessage(Component.translatable("awakening_system.npc.valerie.welcome_card"), false);
                        serverPlayer.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                    } else {
                        serverPlayer.displayClientMessage(Component.translatable("awakening_system.npc.valerie.active"), false);
                        serverPlayer.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                } else {
                    final int VIP_PRICE_MORA = 50000;
                    if (data.mora() >= VIP_PRICE_MORA) {
                        data.spendMora(VIP_PRICE_MORA);
                        data.setCasinoVipUntilTick(-1L);
                        AwakeningSystemMod.sync(serverPlayer);
                        if (!hasPass) {
                            ItemStack pass = new ItemStack(passItem);
                            com.soybe.awakeningsystem.item.CasinoVipPassItem.bindToPlayer(pass, serverPlayer);
                            serverPlayer.getInventory().add(pass);
                        }
                        serverPlayer.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                        if (serverPlayer.level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, serverPlayer.getX(), serverPlayer.getY() + 1.2D, serverPlayer.getZ(), 45, 0.5D, 0.8D, 0.5D, 0.15D);
                        }
                        serverPlayer.displayClientMessage(Component.translatable("awakening_system.npc.valerie.purchased"), false);
                    } else {
                        serverPlayer.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        serverPlayer.displayClientMessage(Component.translatable("awakening_system.npc.valerie.need_mora", data.mora()), false);
                    }
                }
                return InteractionResult.SUCCESS;
            }

            if (profile == NpcProfile.HUNTER_EVALUATOR) {
                com.soybe.awakeningsystem.dialogue.NpcDialogueManager.startDialogue(serverPlayer, this, "hunter_evaluator");
                return InteractionResult.SUCCESS;
            } else {
                com.soybe.awakeningsystem.dialogue.NpcDialogueManager.startSeriesNpcDialogue(serverPlayer, this);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            if (player.isCreative()) {
                return super.hurt(source, amount);
            }
            // Friendly fire protection: owner or companions cannot hurt each other
            if (player.getUUID().equals(getOwnerUuid())) {
                return false;
            }
            return false;
        }

        // In combat while following:
        if (isFollowing()) {
            if (this.getHealth() - amount <= 0.0F) {
                // Faint and revive with 50% HP so companion is never lost permanently
                this.setHealth(this.getMaxHealth() * 0.5F);
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);
                }
                Player owner = getOwner();
                if (owner != null) {
                    owner.displayClientMessage(Component.translatable("awakening_system.guild.npc_fatal_blow_saved", getNpcProfile().displayName()), false);
                }
                return false;
            }
            return super.hurt(source, amount);
        }

        return false;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean success = target.hurt(this.damageSources().mobAttack(this), dmg);
        if (success) {
            this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        }
        return success;
    }

    @Override
    public boolean isPushable() {
        return getNpcProfile() != NpcProfile.ELENA_RECEPTIONIST
                && getNpcProfile() != NpcProfile.BLACK_MARKET_DEALER
                && getNpcProfile() != NpcProfile.CASINO_RECEPTIONIST
                && super.isPushable();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (isFollowing() && this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(2.0F);
            }
            if ((getNpcProfile() == NpcProfile.ELENA_RECEPTIONIST || getNpcProfile() == NpcProfile.BLACK_MARKET_DEALER || getNpcProfile() == NpcProfile.CASINO_RECEPTIONIST) && this.homePos != null) {
                double targetX = this.homePos.getX() + 0.5D;
                double targetY = this.homePos.getY();
                double targetZ = this.homePos.getZ() + 0.5D;
                if (this.distanceToSqr(targetX, targetY, targetZ) > 0.25D) {
                    this.teleportTo(targetX, targetY, targetZ);
                }
                this.setDeltaMovement(0, Math.min(0, this.getDeltaMovement().y), 0);
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.getEntity() instanceof Player player && player.isCreative()) {
            return false;
        }
        if (isFollowing() && !(source.getEntity() instanceof Player)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
    public static void handleReceptionAction(ServerPlayer player, GuildReceptionActionPayload payload) {
        CompoundTag pTag = player.getPersistentData();
        int completed = pTag.getInt("bounties_total_completed");
        String currentRank = GuildRankManager.getGuildRank(completed);
        String lastPromoted = pTag.getString("last_promoted_guild_rank");
        if (lastPromoted.isEmpty()) lastPromoted = "F";

        int neededNext = GuildRankManager.getNextRankNeeded(completed);
        String nextRank = GuildRankManager.getNextRankName(completed);

        if (payload.actionId() == 1) {
            if (!currentRank.equals(lastPromoted)) {
                pTag.putString("last_promoted_guild_rank", currentRank);
                pTag.putString("guild_rank", currentRank);

                int bonusMedals = 3;
                int bonusCoins = 1500;
                SystemEvents.data(player).addCoins(bonusCoins);
                if (SystemItems.ADVENTURER_MEDAL.isBound()) {
                    ItemStack mStack = new ItemStack(SystemItems.ADVENTURER_MEDAL.get(), bonusMedals);
                    if (!player.getInventory().add(mStack)) {
                        player.drop(mStack, false);
                    }
                }

                player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.5, 0.5, 0.5, 0.15);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

                AwakeningSystemMod.notify(player,
                        "awakening_system.elena.promoted_banner",
                        "awakening_system.elena.promoted_desc",
                        0xFFFFD700,
                        currentRank);

                long lastBlessingTime = pTag.getLong("elena_last_blessing_time");
                long currentTime = player.level().getGameTime();
                boolean canClaimBlessing = (currentTime - lastBlessingTime >= 24000L || lastBlessingTime == 0);

                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                        new OpenGuildReceptionPayload(
                                currentRank, completed, neededNext, nextRank, false, canClaimBlessing
                        ));
            }
        } else if (payload.actionId() == 2) {
            long lastBlessingTime = pTag.getLong("elena_last_blessing_time");
            long currentTime = player.level().getGameTime();
            if (currentTime - lastBlessingTime >= 24000L || lastBlessingTime == 0) {
                pTag.putLong("elena_last_blessing_time", currentTime);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 12000, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 12000, 0, false, true, true));
                player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
                player.serverLevel().sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2D, player.getZ(), 16, 0.4, 0.4, 0.4, 0.1);
                player.displayClientMessage(Component.translatable("awakening_system.elena.blessing_quote"), false);

                boolean canPromote = !currentRank.equals(lastPromoted);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                        new OpenGuildReceptionPayload(
                                currentRank, completed, neededNext, nextRank, canPromote, false
                        ));
            }
        }
    }

}
