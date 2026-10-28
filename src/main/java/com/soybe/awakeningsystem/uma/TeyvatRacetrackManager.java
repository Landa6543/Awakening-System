package com.soybe.awakeningsystem.uma;

import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor maestro del Hipódromo de Teyvat y organizador de las carreras de Uma Musume.
 * Controla el ciclo de vida de cada derby (Apuestas ➔ Formación ➔ Carrera ➔ Premiación),
 * la navegación de carriles paralelos y la resolución de ascensión de botín.
 */
public class TeyvatRacetrackManager {

    public enum RaceState {
        BETTING_OPEN,
        STARTING,
        RACING,
        FINISHED
    }

    private static final Map<ServerLevel, TeyvatRacetrackManager> INSTANCES = new ConcurrentHashMap<>();

    public static final BlockPos TRACK_CENTER = new BlockPos(180, 68, 260);

    private static final List<Vec3> BASE_WAYPOINTS = List.of(
            new Vec3(108.5, 68, 231.5),
            new Vec3(135.5, 68, 231.5),
            new Vec3(165.5, 68, 231.5),
            new Vec3(195.5, 68, 231.5),
            new Vec3(225.5, 68, 231.5),
            new Vec3(245.5, 68, 246.5),
            new Vec3(255.5, 68, 260.5),
            new Vec3(245.5, 68, 275.5),
            new Vec3(225.5, 68, 285.5),
            new Vec3(195.5, 68, 285.5),
            new Vec3(165.5, 68, 285.5),
            new Vec3(135.5, 68, 285.5),
            new Vec3(115.5, 68, 275.5),
            new Vec3(105.5, 68, 260.5),
            new Vec3(112.5, 68, 246.5),
            new Vec3(120.5, 68, 231.5),
            new Vec3(135.5, 68, 231.5),
            new Vec3(148.5, 68, 231.5)
    );

    private final ServerLevel level;
    private RaceState state = RaceState.BETTING_OPEN;
    private int stateTimer = 1200;
    private final List<UmaProfile> currentParticipants = new ArrayList<>();
    private final List<UmaRacerEntity> activeRunners = new ArrayList<>();
    private final List<UmaRacerEntity> finishOrder = new ArrayList<>();
    private final Map<UUID, PlayerBet> activeBets = new ConcurrentHashMap<>();
    private final Set<UmaProfile> lastRaceRunners = new HashSet<>();
    private static final Map<UUID, List<ItemStack>> PENDING_OFFLINE_REWARDS = new ConcurrentHashMap<>();

    public static List<ItemStack> claimPendingRewards(UUID playerUuid) {
        if (playerUuid == null) return Collections.emptyList();
        List<ItemStack> list = PENDING_OFFLINE_REWARDS.remove(playerUuid);
        return list != null ? list : Collections.emptyList();
    }

    public record PlayerBet(UUID playerUuid, UmaProfile chosenUma, ItemStack wageredStack) {}

    public TeyvatRacetrackManager(ServerLevel level) {
        this.level = level;
        setupNewRace();
    }

    public static TeyvatRacetrackManager get(Level level) {
        if (level == null) return null;
        if (!(level instanceof ServerLevel sl)) return null;
        ServerLevel target = sl;
        if (!sl.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            ServerLevel teyvat = sl.getServer().getLevel(TeyvatWorldManager.TEYVAT_WORLD);
            if (teyvat != null) {
                target = teyvat;
            }
        }
        return INSTANCES.computeIfAbsent(target, TeyvatRacetrackManager::new);
    }

    public static void clearInstances() {
        INSTANCES.clear();
    }

    public static Collection<TeyvatRacetrackManager> getAllInstances() {
        return INSTANCES.values();
    }

    public boolean hasTrack() {
        return true;
    }

    public int getTotalWaypoints() {
        return BASE_WAYPOINTS.size();
    }

    public Vec3 getWaypoint(int index, int laneIndex) {
        if (index < 0 || index >= BASE_WAYPOINTS.size()) return null;
        Vec3 current = BASE_WAYPOINTS.get(index);
        double dx, dz;

        if (index == BASE_WAYPOINTS.size() - 1) {
            Vec3 prev = BASE_WAYPOINTS.get(index - 1);
            double finishX = getFinishX();
            current = new Vec3(finishX, current.y, current.z);
            dx = current.x - prev.x;
            dz = current.z - prev.z;
        } else {
            Vec3 next = BASE_WAYPOINTS.get(index + 1);
            dx = next.x - current.x;
            dz = next.z - current.z;
        }

        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) return current;

        double nx = -dz / len;
        double nz = dx / len;

        double laneOffset = (laneIndex - 4.0);
        return new Vec3(current.x + nx * laneOffset, current.y, current.z + nz * laneOffset);
    }

    public void setupNewRace() {
        this.state = RaceState.BETTING_OPEN;
        this.stateTimer = 1200;
        this.currentParticipants.clear();
        this.finishOrder.clear();
        this.activeBets.clear();

        int gx = getGateX();
        BlockPos gatePos = new BlockPos(gx, 68, 231);
        if (this.level.hasChunkAt(gatePos)) {
            this.level.getChunk(gatePos);
        }

        net.minecraft.world.phys.AABB trackArea = new net.minecraft.world.phys.AABB(TRACK_CENTER).inflate(300);
        List<UmaRacerEntity> strayRunners = this.level.getEntitiesOfClass(UmaRacerEntity.class, trackArea,
                r -> !r.isCompanion() && r.getCustomTrackCorePos() == null);
        for (UmaRacerEntity runner : strayRunners) {
            runner.discard();
        }
        activeRunners.clear();

        List<UmaProfile> allProfiles = new ArrayList<>(List.of(UmaProfile.values()));
        List<UmaProfile> eligibleProfiles = new ArrayList<>(allProfiles);
        eligibleProfiles.removeAll(lastRaceRunners);

        Collections.shuffle(eligibleProfiles);
        if (eligibleProfiles.size() >= 9) {
            for (int i = 0; i < 9; i++) {
                currentParticipants.add(eligibleProfiles.get(i));
            }
        } else {
            currentParticipants.addAll(eligibleProfiles);
            List<UmaProfile> remaining = new ArrayList<>(allProfiles);
            remaining.removeAll(currentParticipants);
            Collections.shuffle(remaining);
            while (currentParticipants.size() < 9 && !remaining.isEmpty()) {
                currentParticipants.add(remaining.remove(0));
            }
        }

        lastRaceRunners.clear();
        lastRaceRunners.addAll(currentParticipants);

        double stallX = gx - 0.5D;
        for (int lane = 0; lane < currentParticipants.size(); lane++) {
            UmaProfile profile = currentParticipants.get(lane);
            double stallY = 68.0D;
            double stallZ = (231 - 4 + lane) + 0.5D;
            if (ModEntities.UMA_RACER != null) {
                UmaRacerEntity runner = ModEntities.UMA_RACER.get().create(this.level);
                if (runner != null) {
                    runner.setPos(stallX, stallY, stallZ);
                    runner.setSpawnYaw(-90.0F);
                    runner.setUmaProfile(profile);
                    runner.setLaneIndex(lane);
                    runner.setRaceRunning(false);
                    runner.setCompanion(false);
                    this.level.addFreshEntity(runner);
                    this.activeRunners.add(runner);
                }
            }
        }
        openStartingGates(false);
        pruneDuplicateRunners();
    }

    public void placeBet(ServerPlayer player, UmaProfile chosenUma, ItemStack wagerItem) {
        if (state != RaceState.BETTING_OPEN) {
            player.sendSystemMessage(Component.translatable("awakening_system.uma.race.bet_closed_racing"));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        if (activeBets.containsKey(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("awakening_system.uma.race.bet_already_registered"));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        if (!currentParticipants.contains(chosenUma)) {
            player.sendSystemMessage(Component.translatable("awakening_system.uma.race.not_participating"));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        if (wagerItem.isEmpty()) {
            player.sendSystemMessage(Component.translatable("awakening_system.uma.race.bet_empty_item"));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        ItemStack apostado = wagerItem.split(wagerItem.getCount());
        activeBets.put(player.getUUID(), new PlayerBet(player.getUUID(), chosenUma, apostado));

        player.sendSystemMessage(Component.translatable("awakening_system.uma.race.bet_success",
                apostado.getCount() + "x " + apostado.getHoverName().getString(),
                chosenUma.getDisplayName(),
                chosenUma.getBaseOdds()));
        player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    public void startRaceNow() {
        if (this.state == RaceState.BETTING_OPEN) {
            this.stateTimer = 60;
            this.state = RaceState.STARTING;
        }
    }

    public void pruneDuplicateRunners() {
        if (this.level == null) return;
        net.minecraft.world.phys.AABB gateArea = new net.minecraft.world.phys.AABB(
                getGateX() - 8, 65.0, 215.0,
                getGateX() + 8, 75.0, 245.0
        );
        List<UmaRacerEntity> gateRunners = this.level.getEntitiesOfClass(UmaRacerEntity.class, gateArea,
                r -> !r.isCompanion() && r.getCustomTrackCorePos() == null && !r.isRemoved());

        if (this.state == RaceState.BETTING_OPEN) {
            Map<Integer, UmaRacerEntity> laneMap = new HashMap<>();
            for (UmaRacerEntity runner : gateRunners) {
                int lane = runner.getLaneIndex();
                if (lane < 0 || lane >= 9) {
                    runner.discard();
                    continue;
                }
                if (laneMap.containsKey(lane)) {
                    UmaRacerEntity existing = laneMap.get(lane);
                    if (this.activeRunners.contains(runner) && !this.activeRunners.contains(existing)) {
                        existing.discard();
                        laneMap.put(lane, runner);
                    } else {
                        runner.discard();
                    }
                } else {
                    laneMap.put(lane, runner);
                }
            }
            if (this.activeRunners.isEmpty() && !laneMap.isEmpty()) {
                this.activeRunners.addAll(laneMap.values());
            }
        }
    }

    public void tick() {
        if (this.state == RaceState.BETTING_OPEN) {
            if (this.level.getGameTime() % 20 == 0) {
                pruneDuplicateRunners();
            }
            if (this.activeBets.isEmpty()) {
                this.stateTimer = 1200;
                return;
            }
        }

        if (this.stateTimer > 0) {
            this.stateTimer--;
        }

        switch (this.state) {
            case BETTING_OPEN -> {
                if (this.stateTimer <= 0) {
                    this.state = RaceState.STARTING;
                    this.stateTimer = 80;
                    broadcastToRacetrack(Component.translatable("awakening_system.uma.race.betting_closed"));
                    playRacetrackSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.0F);
                }
            }
            case STARTING -> {
                if (this.stateTimer == 60) {
                    broadcastToRacetrack(Component.translatable("awakening_system.uma.race.countdown.3"));
                    playRacetrackSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.0F);
                } else if (this.stateTimer == 40) {
                    broadcastToRacetrack(Component.translatable("awakening_system.uma.race.countdown.2"));
                    playRacetrackSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.2F);
                } else if (this.stateTimer == 20) {
                    broadcastToRacetrack(Component.translatable("awakening_system.uma.race.countdown.1"));
                    playRacetrackSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.4F);
                } else if (this.stateTimer <= 0) {
                    this.state = RaceState.RACING;
                    this.stateTimer = 1800;
                    broadcastToRacetrack(Component.translatable("awakening_system.uma.race.start"));
                    playRacetrackSound(com.soybe.awakeningsystem.ModSounds.UMA_START_CHEER.get(), 1.4F, 1.0F);
                    playRacetrackSound(SoundEvents.GENERIC_EXPLODE.value(), 0.6F, 1.6F);
                    openStartingGates(true);

                    activeRunners.removeIf(Entity::isRemoved);
                    if (activeRunners.isEmpty()) {
                        net.minecraft.world.phys.AABB trackArea = new net.minecraft.world.phys.AABB(TRACK_CENTER).inflate(200);
                        List<UmaRacerEntity> worldRunners = this.level.getEntitiesOfClass(
                                UmaRacerEntity.class, trackArea,
                                r -> r.getCustomTrackCorePos() == null && !r.isRemoved()
                        );
                        activeRunners.addAll(worldRunners);
                    }

                    for (UmaRacerEntity runner : activeRunners) {
                        runner.setRaceRunning(true);
                    }
                }
            }
            case RACING -> {
                activeRunners.removeIf(Entity::isRemoved);
                if (activeRunners.isEmpty()) {
                    net.minecraft.world.phys.AABB trackArea = new net.minecraft.world.phys.AABB(TRACK_CENTER).inflate(200);
                    List<UmaRacerEntity> worldRunners = this.level.getEntitiesOfClass(
                            UmaRacerEntity.class, trackArea,
                            r -> r.getCustomTrackCorePos() == null && !r.isRemoved()
                    );
                    activeRunners.addAll(worldRunners);
                    for (UmaRacerEntity runner : activeRunners) {
                        if (!runner.isRaceRunning() && !runner.isFinished()) {
                            runner.setRaceRunning(true);
                        }
                    }
                }

                if (this.stateTimer % 35 == 0 && !activeRunners.isEmpty()) {
                    UmaRacerEntity leader = getRaceLeader();
                    if (leader != null && !leader.isFinished()) {
                        float p = leader.getRaceProgress();
                        String leadName = leader.getUmaProfile().getDisplayName();
                        Component comment;
                        if (p < 0.20F) {
                            comment = Component.translatable("awakening_system.uma.race.comment.start", leadName);
                        } else if (p < 0.45F) {
                            comment = Component.translatable("awakening_system.uma.race.comment.turn1", leadName);
                        } else if (p < 0.65F) {
                            comment = Component.translatable("awakening_system.uma.race.comment.backstretch", leadName);
                        } else if (p < 0.80F) {
                            comment = Component.translatable("awakening_system.uma.race.comment.last_turn", leadName);
                        } else if (p < 0.92F) {
                            comment = Component.translatable("awakening_system.uma.race.comment.last_spurt", leadName);
                        } else {
                            comment = Component.translatable("awakening_system.uma.race.comment.final_stretch");
                        }
                        broadcastActionbarToRacetrack(comment);
                    }
                }
                if (this.finishOrder.size() >= activeRunners.size() || this.stateTimer <= 0) {
                    finishRace();
                }
            }
            case FINISHED -> {
                if (this.stateTimer <= 0) {
                    setupNewRace();
                }
            }
        }
    }

    public int getGateX() {
        if (this.level != null && this.level.getBlockState(new BlockPos(104, 68, 231)).getBlock() instanceof com.soybe.awakeningsystem.block.StartingGateBlock) {
            return 104;
        }
        return 104;
    }

    public double getFinishX() {
        if (this.level != null) {
            for (int x = 140; x <= 160; x++) {
                BlockState state = this.level.getBlockState(new BlockPos(x, 67, 231));
                if (state.is(Blocks.BLACK_CONCRETE) ||
                    state.is(Blocks.SMOOTH_QUARTZ) ||
                    state.is(Blocks.WHITE_CONCRETE)) {
                    return x + 0.5D;
                }
            }
        }
        return 148.5D;
    }

    public void openStartingGates(boolean open) {
        if (this.level == null) return;
        int gx = getGateX(), gy = 68, gz = 231;
        for (int lane = 0; lane < 9; lane++) {
            BlockPos pos = new BlockPos(gx, gy, gz - 4 + lane);
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() instanceof com.soybe.awakeningsystem.block.StartingGateBlock) {
                this.level.setBlock(pos, state.setValue(com.soybe.awakeningsystem.block.StartingGateBlock.OPEN, open), 3);
            }
            BlockPos upperPos = pos.above();
            BlockState upperState = this.level.getBlockState(upperPos);
            if (upperState.getBlock() instanceof com.soybe.awakeningsystem.block.StartingGateBlock) {
                this.level.setBlock(upperPos, upperState.setValue(com.soybe.awakeningsystem.block.StartingGateBlock.OPEN, open), 3);
            }
        }
    }

    public synchronized void onRunnerFinished(UmaRacerEntity runner) {
        if (!this.finishOrder.contains(runner)) {
            int rank = this.finishOrder.size() + 1;
            this.finishOrder.add(runner);
            runner.setFinishRank(rank);

            if (rank == 1) {
                broadcastToRacetrack(Component.translatable("awakening_system.uma.race.finish.first",
                        runner.getUmaProfile().getDisplayName().toUpperCase()));

                double fx = getFinishX();
                for (int i = 0; i < 25; i++) {
                    this.level.sendParticles(ParticleTypes.FIREWORK,
                            fx + (level.random.nextDouble() - 0.5) * 4.0,
                            70.0 + level.random.nextDouble() * 4.0,
                            231.0 + (level.random.nextDouble() - 0.5) * 8.0,
                            5, 0.0, 0.15, 0.0, 0.2);
                }

                this.stateTimer = Math.min(this.stateTimer, 700);
            } else {
                broadcastToRacetrack(Component.translatable("awakening_system.uma.race.finish.rank",
                        rank, runner.getUmaProfile().getDisplayName()));
            }

            if (this.finishOrder.size() >= activeRunners.size()) {
                finishRace();
            }
        }
    }

    private void finishRace() {
        if (this.state == RaceState.FINISHED) return;
        this.state = RaceState.FINISHED;
        this.stateTimer = 220;

        List<UmaRacerEntity> remaining = new ArrayList<>(this.activeRunners);
        remaining.removeAll(this.finishOrder);
        remaining.sort((a, b) -> Float.compare(b.getRaceProgress(), a.getRaceProgress()));
        for (UmaRacerEntity r : remaining) {
            int rank = this.finishOrder.size() + 1;
            this.finishOrder.add(r);
            r.setFinishRank(rank);
        }

        if (finishOrder.isEmpty()) return;
        UmaRacerEntity winner = finishOrder.get(0);
        UmaProfile winningUma = winner.getUmaProfile();

        for (UmaRacerEntity runner : this.activeRunners) {
            runner.setRaceRunning(false);
            runner.setDeltaMovement(0, 0, 0);
            runner.getNavigation().stop();
        }

        double fx = getFinishX();
        for (int i = 0; i < 30; i++) {
            this.level.sendParticles(ParticleTypes.FIREWORK,
                    fx - 2.5 + (level.random.nextDouble() - 0.5) * 5.0,
                    71.0 + level.random.nextDouble() * 3.0,
                    231.5 + (level.random.nextDouble() - 0.5) * 6.0,
                    4, 0.0, 0.18, 0.0, 0.15);
            this.level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    fx - 2.5 + (level.random.nextDouble() - 0.5) * 3.0,
                    69.5 + level.random.nextDouble() * 2.0,
                    231.5 + (level.random.nextDouble() - 0.5) * 4.0,
                    5, 0.0, 0.1, 0.0, 0.1);
        }

        broadcastToRacetrack(Component.literal("§6§l✦====================================✦"));
        broadcastToRacetrack(Component.translatable("awakening_system.uma.race.podium.title"));
        broadcastToRacetrack(Component.translatable("awakening_system.uma.race.podium.first", winningUma.getDisplayName()));
        if (finishOrder.size() > 1) broadcastToRacetrack(Component.translatable("awakening_system.uma.race.podium.second", finishOrder.get(1).getUmaProfile().getDisplayName()));
        if (finishOrder.size() > 2) broadcastToRacetrack(Component.translatable("awakening_system.uma.race.podium.third", finishOrder.get(2).getUmaProfile().getDisplayName()));
        broadcastToRacetrack(Component.literal("§6§l✦====================================✦"));

        for (PlayerBet bet : activeBets.values()) {
            ServerPlayer player = this.level.getServer().getPlayerList().getPlayer(bet.playerUuid());

            if (bet.chosenUma() == winningUma) {
                UmaLootUpgradeTable.UpgradeResult result = UmaLootUpgradeTable.upgradeLoot(bet.wageredStack(), winningUma);

                if (player != null) {
                    for (ItemStack reward : result.upgradedItems()) {
                        if (!player.getInventory().add(reward)) {
                            player.drop(reward, false);
                        }
                    }
                    player.sendSystemMessage(result.celebrationMessage());
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

                    com.soybe.awakeningsystem.casino.CasinoTrophyManager.tryClaimTrophy(
                            this.level, player, 1, "Victory in the Teyvat Derby with " + winningUma.getDisplayName()
                    );
                } else {
                    List<ItemStack> offlineItems = new ArrayList<>(result.upgradedItems());
                    PENDING_OFFLINE_REWARDS.computeIfAbsent(bet.playerUuid(), k -> new ArrayList<>()).addAll(offlineItems);
                }
            } else {
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("awakening_system.uma.race.bet_lost",
                            bet.chosenUma().getDisplayName()));
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.6F);
                }
            }
        }
        activeBets.clear();
    }

    private UmaRacerEntity getRaceLeader() {
        UmaRacerEntity best = null;
        float maxProg = -1.0F;
        for (UmaRacerEntity r : activeRunners) {
            if (r.getRaceProgress() > maxProg) {
                maxProg = r.getRaceProgress();
                best = r;
            }
        }
        return best;
    }

    public void broadcastToRacetrack(Component comp) {
        for (ServerPlayer player : this.level.players()) {
            if (player.blockPosition().closerThan(TRACK_CENTER, 250)) {
                if (hasActiveBet(player.getUUID())) {
                    player.sendSystemMessage(comp);
                } else {
                    player.displayClientMessage(comp, true);
                }
            }
        }
    }

    public void broadcastToRacetrack(String message) {
        broadcastToRacetrack(Component.literal(message));
    }

    public void broadcastActionbarToRacetrack(Component comp) {
        for (ServerPlayer player : this.level.players()) {
            if (player.blockPosition().closerThan(TRACK_CENTER, 250)) {
                player.displayClientMessage(comp, true);
            }
        }
    }

    private void playRacetrackSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        for (ServerPlayer player : this.level.players()) {
            if (player.blockPosition().closerThan(TRACK_CENTER, 250)) {
                player.playNotifySound(sound, SoundSource.RECORDS, volume, pitch);
            }
        }
    }

    public RaceState getState() { return state; }
    public int getStateTimer() { return stateTimer; }
    public List<UmaProfile> getCurrentParticipants() { return currentParticipants; }
    public boolean hasActiveBet(UUID playerUuid) { return activeBets.containsKey(playerUuid); }
    public PlayerBet getActiveBet(UUID playerUuid) { return activeBets.get(playerUuid); }
}
