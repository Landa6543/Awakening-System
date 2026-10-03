package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.GuildRankManager;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.OpenGuildReceptionPayload;
import com.soybe.awakeningsystem.GuildReceptionActionPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

public class SeriesNpcEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> PROFILE_ID =
            SynchedEntityData.defineId(SeriesNpcEntity.class, EntityDataSerializers.STRING);

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

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal(this, 0.6D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.35D) {
            @Override
            public boolean canUse() {
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.ELENA_RECEPTIONIST) return false;
                return super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                if (SeriesNpcEntity.this.getNpcProfile() == NpcProfile.ELENA_RECEPTIONIST) return false;
                return super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROFILE_ID, NpcProfile.SUNG_JINWOO.id());
    }

    public NpcProfile getNpcProfile() {
        return NpcProfile.getById(this.entityData.get(PROFILE_ID));
    }

    public void setNpcProfile(NpcProfile profile) {
        if (profile == null) profile = NpcProfile.SUNG_JINWOO;
        this.entityData.set(PROFILE_ID, profile.id());
        this.updateCustomName();
        this.updateEquipmentForProfile();
    }

    public void updateEquipmentForProfile() {
        NpcProfile p = getNpcProfile();
        ItemStack weapon = p.getSignatureWeapon();
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, weapon);
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
    }

    public void updateCustomName() {
        NpcProfile p = getNpcProfile();
        this.setCustomName(Component.literal(p.titleColor() + p.displayName() + " §7[").append(Component.translatable("awakening_system.npc." + p.id() + ".title")).append(Component.literal("]")));
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

            // Mensaje en pantalla estilo Sistema
            int diagIdx = (int) (Math.random() * 4);
            AwakeningSystemMod.notify(serverPlayer,
                    "awakening_system.npc." + profile.id() + ".dialogue_title",
                    "awakening_system.npc." + profile.id() + ".dialogue." + diagIdx,
                    0xFFFFD700);

            serverPlayer.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
            serverPlayer.playNotifySound(com.soybe.awakeningsystem.ModSounds.NPC_INTERACT.get(), SoundSource.VOICE, 1.0F, 1.0F);

            // Gestión de Rango y Pantalla Interactiva de Elena (Recepcionista del Gremio)
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

            // Sistema de Bendición / Buff Diario
            long currentDay = serverPlayer.level().getDayTime() / 24000L;
            String tagKey = "blessing_day_" + profile.id();
            long lastBlessingDay = serverPlayer.getPersistentData().getLong(tagKey);

            if (lastBlessingDay != currentDay) {
                serverPlayer.getPersistentData().putLong(tagKey, currentDay);
                profile.applyBlessing(serverPlayer);
                serverPlayer.playNotifySound(com.soybe.awakeningsystem.ModSounds.NPC_BLESSING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                serverPlayer.displayClientMessage(
                        Component.literal(profile.titleColor() + "[" + profile.displayName() + "]§r ")
                                .append(Component.translatable("awakening_system.elena.already_blessed")),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Los NPCs protagonistas son legendarios e inmortales frente a monstruos, fuego, asfixia y caídas
        if (source.getEntity() instanceof Player player) {
            return player.isCreative() && super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean isPushable() {
        return getNpcProfile() != NpcProfile.ELENA_RECEPTIONIST && super.isPushable();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && getNpcProfile() == NpcProfile.ELENA_RECEPTIONIST && this.homePos != null) {
            double targetX = this.homePos.getX() + 0.5D;
            double targetY = this.homePos.getY();
            double targetZ = this.homePos.getZ() + 0.5D;
            if (this.distanceToSqr(targetX, targetY, targetZ) > 0.25D) {
                this.teleportTo(targetX, targetY, targetZ);
            }
            this.setDeltaMovement(0, Math.min(0, this.getDeltaMovement().y), 0);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.getEntity() instanceof Player player && player.isCreative()) {
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
            // ASCENSO DE RANGO
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
            // BENDICIÓN DE ELENA
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
