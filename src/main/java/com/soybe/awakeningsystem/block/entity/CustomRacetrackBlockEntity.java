package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.uma.UmaProfile;
import com.soybe.awakeningsystem.uma.UmaRacerEntity;
import com.soybe.awakeningsystem.uma.model.OwnedUma;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Entidad de bloque para la Terminal del Hipódromo Custom.
 * Administra el trazado de waypoints, el lobby de corredores (jugadores amigos y NPCs),
 * y la ejecución de la carrera con las estadísticas reales entrenadas de cada Uma.
 */
public class CustomRacetrackBlockEntity extends BlockEntity {

    public enum RaceState {
        IDLE,
        STARTING,
        RACING,
        FINISHED
    }

    public static class Participant {
        public UUID playerUuid;
        public String displayName;
        public String umaId;
        public int rarity;
        public int speed;
        public int stamina;
        public int power;
        public int guts;
        public int wisdom;
        public boolean isNpc;
        public int laneIndex;

        public Participant(UUID playerUuid, String displayName, String umaId, int rarity,
                           int speed, int stamina, int power, int guts, int wisdom, boolean isNpc, int laneIndex) {
            this.playerUuid = playerUuid;
            this.displayName = displayName;
            this.umaId = umaId;
            this.rarity = rarity;
            this.speed = speed;
            this.stamina = stamina;
            this.power = power;
            this.guts = guts;
            this.wisdom = wisdom;
            this.isNpc = isNpc;
            this.laneIndex = laneIndex;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            if (playerUuid != null) tag.putUUID("uuid", playerUuid);
            tag.putString("name", displayName);
            tag.putString("uma_id", umaId);
            tag.putInt("rarity", rarity);
            tag.putInt("speed", speed);
            tag.putInt("stamina", stamina);
            tag.putInt("power", power);
            tag.putInt("guts", guts);
            tag.putInt("wisdom", wisdom);
            tag.putBoolean("is_npc", isNpc);
            tag.putInt("lane", laneIndex);
            return tag;
        }

        public static Participant fromNBT(CompoundTag tag) {
            UUID uuid = tag.hasUUID("uuid") ? tag.getUUID("uuid") : null;
            return new Participant(
                    uuid,
                    tag.getString("name"),
                    tag.getString("uma_id"),
                    tag.getInt("rarity"),
                    tag.getInt("speed"),
                    tag.getInt("stamina"),
                    tag.getInt("power"),
                    tag.getInt("guts"),
                    tag.getInt("wisdom"),
                    tag.getBoolean("is_npc"),
                    tag.getInt("lane")
            );
        }
    }

    private final List<BlockPos> waypoints = new ArrayList<>();
    private final List<Participant> participants = new ArrayList<>();
    private final List<UmaRacerEntity> activeRunners = new ArrayList<>();
    private final List<UmaRacerEntity> finishOrder = new ArrayList<>();

    private RaceState state = RaceState.IDLE;
    private int stateTimer = 0;
    private String trackName = "Pista Personalizada";

    public CustomRacetrackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUSTOM_RACETRACK.get(), pos, state);
    }

    public List<BlockPos> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public void setWaypoints(List<BlockPos> points) {
        this.waypoints.clear();
        this.waypoints.addAll(points);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            if (level instanceof ServerLevel sl) {
                com.soybe.awakeningsystem.uma.RacetrackJsonHelper.exportTrack(this, sl);
            }
        }
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public RaceState getState() {
        return state;
    }

    public int getStateTimer() {
        return stateTimer;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String name) {
        this.trackName = name;
        setChanged();
    }

    /**
     * Inscribe a un jugador con su Uma activa en el siguiente carril libre (hasta 8).
     */
    public boolean joinRace(ServerPlayer player) {
        if (state != RaceState.IDLE) return false;
        if (participants.size() >= 8) return false;

        for (Participant p : participants) {
            if (player.getUUID().equals(p.playerUuid)) {
                return false;
            }
        }

        SystemData data = SystemEvents.data(player);
        OwnedUma ace = data.getActiveUma();
        if (ace == null) {
            ace = new OwnedUma("special_week", 3);
        }

        int lane = participants.size();
        Participant p = new Participant(
                player.getUUID(),
                player.getName().getString(),
                ace.getUmaId(),
                ace.getRarity(),
                ace.getSpeed(),
                ace.getStamina(),
                ace.getPower(),
                ace.getGuts(),
                ace.getWisdom(),
                false,
                lane
        );
        participants.add(p);
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    /**
     * Rellena los carriles restantes con corredoras NPC para llegar a 8 participantes.
     */
    public void fillWithNpcs() {
        if (state != RaceState.IDLE) return;
        List<UmaProfile> allProfiles = new ArrayList<>(List.of(UmaProfile.values()));
        Collections.shuffle(allProfiles);
        Random rnd = new Random();

        int profileIdx = 0;
        while (participants.size() < 8 && profileIdx < allProfiles.size()) {
            UmaProfile profile = allProfiles.get(profileIdx++);
            boolean alreadyIn = participants.stream().anyMatch(p -> p.umaId.equals(profile.getId()));
            if (alreadyIn && profileIdx < allProfiles.size()) continue;

            int lane = participants.size();
            int baseStat = 400 + rnd.nextInt(250);
            Participant npc = new Participant(
                    null,
                    "Rival " + profile.getDisplayName(),
                    profile.getId(),
                    2 + rnd.nextInt(2),
                    baseStat + rnd.nextInt(100),
                    baseStat + rnd.nextInt(100),
                    baseStat + rnd.nextInt(100),
                    baseStat + rnd.nextInt(100),
                    baseStat + rnd.nextInt(100),
                    true,
                    lane
            );
            participants.add(npc);
        }
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void clearParticipants() {
        if (state != RaceState.IDLE) return;
        participants.clear();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /**
     * Inicia el conteo regresivo y el derby.
     */
    public boolean startRace() {
        if (state != RaceState.IDLE) return false;
        if (waypoints.size() < 2) return false;
        if (participants.isEmpty()) return false;
        if (!(level instanceof ServerLevel sl)) return false;

        clearStrayRunners(sl);

        this.state = RaceState.STARTING;
        this.stateTimer = 80;
        this.finishOrder.clear();
        this.activeRunners.clear();

        BlockPos startPos = waypoints.get(0);
        BlockPos nextPos = waypoints.get(1);

        double dx = nextPos.getX() - startPos.getX();
        double dz = nextPos.getZ() - startPos.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) len = 1.0;
        double nx = -dz / len;
        double nz = dx / len;

        float yaw = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));

        for (Participant p : participants) {
            double offset = (p.laneIndex - 3.5);
            double spawnX = startPos.getX() + 0.5 + nx * offset;
            double spawnY = startPos.getY();
            double spawnZ = startPos.getZ() + 0.5 + nz * offset;

            if (ModEntities.UMA_RACER != null) {
                UmaRacerEntity runner = ModEntities.UMA_RACER.get().create(sl);
                if (runner != null) {
                    runner.setPos(spawnX, spawnY, spawnZ);
                    runner.setSpawnYaw(yaw);
                    runner.setUmaProfile(UmaProfile.byId(p.umaId));
                    runner.setLaneIndex(p.laneIndex);
                    runner.setCustomTrackCorePos(this.worldPosition);
                    runner.setCustomStats(p.speed, p.stamina, p.power, p.guts, p.wisdom);
                    runner.setRaceRunning(false);

                    String ownerTag = p.isNpc ? "§7[NPC] " : ("§6[" + p.displayName + "] ");
                    runner.setCustomName(Component.literal(ownerTag + "§e" + UmaProfile.byId(p.umaId).getDisplayName() + " §7(★" + p.rarity + ")"));
                    runner.setCustomNameVisible(true);

                    sl.addFreshEntity(runner);
                    this.activeRunners.add(runner);
                }
            }
        }

        if (this.activeRunners.isEmpty()) {
            this.state = RaceState.IDLE;
            broadcast(Component.translatable("awakening_system.racetrack.error_deploy"));
            return false;
        }

        broadcast(Component.translatable("awakening_system.racetrack.starting_gate"));
        playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.0F);
        triggerStartingGates(false);

        setChanged();
        sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public void triggerStartingGates(boolean open) {
        if (level == null || waypoints.isEmpty()) return;
        BlockPos startPos = waypoints.get(0);
        for (BlockPos p : BlockPos.betweenClosed(startPos.offset(-8, -1, -8), startPos.offset(8, 3, 8))) {
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof com.soybe.awakeningsystem.block.StartingGateBlock) {
                level.setBlock(p, st.setValue(com.soybe.awakeningsystem.block.StartingGateBlock.OPEN, open), 3);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CustomRacetrackBlockEntity be) {
        if (level.isClientSide()) return;
        if (be.stateTimer > 0) {
            be.stateTimer--;
        }

        switch (be.state) {
            case STARTING -> {
                if (be.stateTimer == 60) {
                    be.broadcast("§c🔴 3!");
                    be.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.0F);
                } else if (be.stateTimer == 40) {
                    be.broadcast("§e🟡 2!");
                    be.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.2F);
                } else if (be.stateTimer == 20) {
                    be.broadcast("§a🟢 1!");
                    be.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.4F);
                } else if (be.stateTimer <= 0) {
                    be.state = RaceState.RACING;
                    be.stateTimer = 1800;
                    be.broadcast(Component.translatable("awakening_system.racetrack.race_begins", be.trackName));
                    be.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.6F, 1.6F);
                    be.triggerStartingGates(true);

                    be.activeRunners.removeIf(net.minecraft.world.entity.Entity::isRemoved);
                    if (be.activeRunners.isEmpty() && level instanceof ServerLevel sl) {
                        AABB trackArea = new AABB(pos).inflate(160);
                        List<UmaRacerEntity> worldRunners = sl.getEntitiesOfClass(
                                UmaRacerEntity.class, trackArea,
                                r -> pos.equals(r.getCustomTrackCorePos()) && !r.isRemoved()
                        );
                        be.activeRunners.addAll(worldRunners);
                    }

                    for (UmaRacerEntity runner : be.activeRunners) {
                        runner.setRaceRunning(true);
                    }
                }
            }
            case RACING -> {
                be.activeRunners.removeIf(net.minecraft.world.entity.Entity::isRemoved);
                if (be.activeRunners.isEmpty() && level instanceof ServerLevel sl) {
                    AABB trackArea = new AABB(pos).inflate(160);
                    List<UmaRacerEntity> worldRunners = sl.getEntitiesOfClass(
                            UmaRacerEntity.class, trackArea,
                            r -> pos.equals(r.getCustomTrackCorePos()) && !r.isRemoved()
                    );
                    be.activeRunners.addAll(worldRunners);
                    for (UmaRacerEntity runner : be.activeRunners) {
                        if (!runner.isRaceRunning() && !runner.isFinished()) {
                            runner.setRaceRunning(true);
                        }
                    }
                }

                if (!be.activeRunners.isEmpty() && be.finishOrder.size() >= be.activeRunners.size()) {
                    be.finishRace();
                } else if (be.stateTimer <= 0) {
                    be.finishRace();
                }
            }
            case FINISHED -> {
                if (be.stateTimer <= 0) {
                    be.state = RaceState.IDLE;
                    be.clearStrayRunners((ServerLevel) level);
                    be.activeRunners.clear();
                    be.finishOrder.clear();
                }
            }
            default -> {}
        }
    }

    public synchronized void onRunnerFinished(UmaRacerEntity runner) {
        if (!this.finishOrder.contains(runner)) {
            int rank = this.finishOrder.size() + 1;
            this.finishOrder.add(runner);
            runner.setFinishRank(rank);

            Participant p = findParticipantByLane(runner.getLaneIndex());
            String nameStr = p != null ? (p.displayName + " (" + runner.getUmaProfile().getDisplayName() + ")") : runner.getUmaProfile().getDisplayName();

            if (rank == 1) {
                broadcast(Component.translatable("awakening_system.racetrack.winner_1st", nameStr.toUpperCase()));
                playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);

                if (level instanceof ServerLevel sl) {
                    BlockPos finishPos = waypoints.get(waypoints.size() - 1);
                    for (int i = 0; i < 20; i++) {
                        sl.sendParticles(ParticleTypes.FIREWORK,
                                finishPos.getX() + 0.5 + (sl.random.nextDouble() - 0.5) * 4.0,
                                finishPos.getY() + 1.0 + sl.random.nextDouble() * 3.0,
                                finishPos.getZ() + 0.5 + (sl.random.nextDouble() - 0.5) * 4.0,
                                4, 0.0, 0.1, 0.0, 0.2);
                    }
                }
                this.stateTimer = Math.min(this.stateTimer, 120);
            } else {
                broadcast(Component.translatable("awakening_system.racetrack.place", rank, nameStr));
            }

            if (this.finishOrder.size() >= this.activeRunners.size()) {
                finishRace();
            }
        }
    }

    private void finishRace() {
        if (this.state == RaceState.FINISHED) return;
        this.state = RaceState.FINISHED;
        this.stateTimer = 200;

        broadcast(Component.translatable("awakening_system.racetrack.finished"));

        if (level instanceof ServerLevel sl) {
            for (int rank = 1; rank <= finishOrder.size(); rank++) {
                UmaRacerEntity runner = finishOrder.get(rank - 1);
                Participant p = findParticipantByLane(runner.getLaneIndex());
                if (p != null && !p.isNpc && p.playerUuid != null) {
                    ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(p.playerUuid);
                    if (sp != null) {
                        SystemData data = SystemEvents.data(sp);
                        int caratsReward = switch (rank) {
                            case 1 -> 300;
                            case 2 -> 150;
                            case 3 -> 75;
                            default -> 25;
                        };
                        data.addCarats(caratsReward);
                        OwnedUma ou = data.getOwnedUma(p.umaId);
                        if (ou != null) {
                            ou.recordRace(rank == 1);
                        }
                        com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);

                        sp.sendSystemMessage(Component.translatable("awakening_system.racetrack.prize", rank, caratsReward));
                        sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                    }
                }
            }
        }
    }

    private Participant findParticipantByLane(int lane) {
        for (Participant p : participants) {
            if (p.laneIndex == lane) return p;
        }
        return null;
    }

    private void clearStrayRunners(ServerLevel sl) {
        AABB trackArea = new AABB(worldPosition).inflate(200);
        List<UmaRacerEntity> stray = sl.getEntitiesOfClass(UmaRacerEntity.class, trackArea,
                r -> worldPosition.equals(r.getCustomTrackCorePos()));
        for (UmaRacerEntity r : stray) {
            r.discard();
        }
    }

    public Vec3 getCustomWaypoint(int index, int laneIndex) {
        if (index < 0 || index >= waypoints.size()) return null;
        BlockPos current = waypoints.get(index);
        double dx, dz;

        if (index == waypoints.size() - 1 && waypoints.size() > 1) {
            BlockPos prev = waypoints.get(index - 1);
            dx = current.getX() - prev.getX();
            dz = current.getZ() - prev.getZ();
        } else {
            BlockPos next = waypoints.get((index + 1) % waypoints.size());
            dx = next.getX() - current.getX();
            dz = next.getZ() - current.getZ();
        }

        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) len = 1.0;
        double nx = -dz / len;
        double nz = dx / len;

        double laneOffset = (laneIndex - 3.5) * 1.15;
        return new Vec3(current.getX() + 0.5 + nx * laneOffset, current.getY(), current.getZ() + 0.5 + nz * laneOffset);
    }

    private void broadcast(Component comp) {
        if (level == null) return;
        for (ServerPlayer player : ((ServerLevel) level).players()) {
            if (player.blockPosition().closerThan(worldPosition, 180)) {
                player.sendSystemMessage(comp);
            }
        }
    }

    private void broadcast(String message) {
        if (level == null) return;
        Component comp = Component.literal(message);
        for (ServerPlayer player : ((ServerLevel) level).players()) {
            if (player.blockPosition().closerThan(worldPosition, 180)) {
                player.sendSystemMessage(comp);
            }
        }
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        if (level == null) return;
        for (ServerPlayer player : ((ServerLevel) level).players()) {
            if (player.blockPosition().closerThan(worldPosition, 180)) {
                player.playNotifySound(sound, SoundSource.RECORDS, volume, pitch);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("track_name", trackName);
        tag.putInt("race_state", state.ordinal());
        tag.putInt("state_timer", stateTimer);

        ListTag wpList = new ListTag();
        for (BlockPos p : waypoints) {
            CompoundTag pt = new CompoundTag();
            pt.putInt("x", p.getX());
            pt.putInt("y", p.getY());
            pt.putInt("z", p.getZ());
            wpList.add(pt);
        }
        tag.put("waypoints", wpList);

        ListTag partList = new ListTag();
        for (Participant p : participants) {
            partList.add(p.toNBT());
        }
        tag.put("participants", partList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        trackName = tag.contains("track_name") ? tag.getString("track_name") : "Pista Personalizada";
        state = RaceState.values()[Math.min(RaceState.values().length - 1, Math.max(0, tag.getInt("race_state")))];
        stateTimer = tag.getInt("state_timer");

        waypoints.clear();
        if (tag.contains("waypoints", net.minecraft.nbt.Tag.TAG_LIST)) {
            ListTag wpList = tag.getList("waypoints", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < wpList.size(); i++) {
                CompoundTag pt = wpList.getCompound(i);
                waypoints.add(new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z")));
            }
        }

        participants.clear();
        if (tag.contains("participants", net.minecraft.nbt.Tag.TAG_LIST)) {
            ListTag partList = tag.getList("participants", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < partList.size(); i++) {
                participants.add(Participant.fromNBT(partList.getCompound(i)));
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}
