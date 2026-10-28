package com.soybe.awakeningsystem.casino.pool;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import com.soybe.awakeningsystem.casino.pool.network.PoolTableStateS2CPayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class PoolTableBlockEntity extends BlockEntity {
    public static final float TABLE_HALF_WIDTH = 1.15F;
    public static final float TABLE_HALF_LENGTH = 1.65F;
    public static final float POCKET_RADIUS = 0.110F;
    public static final float SUBSTEP_DT = 0.2F;
    public static final int SUBSTEPS = 5;

    public static final int MODE_AI_NORMAL = 0;
    public static final int MODE_AI_HUSTLER = 1;
    public static final int MODE_PVP = 2;

    public static final int STATUS_WAITING = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_MOVING = 2;
    public static final int STATUS_P1_WON = 3;
    public static final int STATUS_P2_WON = 4;
    public static final int STATUS_SCRATCH_LOSS = 5;
    public static final int STATUS_WAITING_P2 = 6;

    public static final int SUIT_NONE = 0;
    public static final int SUIT_SOLIDS = 1;
    public static final int SUIT_STRIPES = 2;

    public static final float[][] POCKETS = {
            {-TABLE_HALF_WIDTH, -TABLE_HALF_LENGTH},
            { TABLE_HALF_WIDTH, -TABLE_HALF_LENGTH},
            {-TABLE_HALF_WIDTH,  0.0F},
            { TABLE_HALF_WIDTH,  0.0F},
            {-TABLE_HALF_WIDTH,  TABLE_HALF_LENGTH},
            { TABLE_HALF_WIDTH,  TABLE_HALF_LENGTH}
    };

    private final List<PoolBall> balls = new ArrayList<>(16);
    private int gameMode = MODE_AI_NORMAL;
    private int betAmount = 0;
    private int pot = 0;

    private UUID player1UUID = null;
    private String player1Name = "Jugador 1";
    private UUID player2UUID = null;
    private String player2Name = "IA Hustler";

    private int currentTurn = 1;
    private int p1Suit = SUIT_NONE;
    private int p2Suit = SUIT_NONE;
    private int gameStatus = STATUS_READY;
    private String statusMessage = Component.translatable("awakening_system.casino.pool.status.welcome").getString();

    private float cueAimAngle = 0.0F;
    private float cuePower = 0.0F;
    private boolean isAiming = false;

    private int aiShootTimer = 0;
    private int firstBallHitThisTurn = -1;
    private boolean cueBallPocketedThisTurn = false;
    private boolean eightBallPocketedThisTurn = false;
    private final List<PoolBall> pocketedThisTurn = new ArrayList<>();

    public PoolTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.POOL_TABLE.get(), pos, blockState);
        setupRack();
    }

    /**
     * Configura el rack inicial de 15 bolas en triángulo y la bola blanca en la línea de apertura.
     */
    public void setupRack() {
        balls.clear();
        balls.add(new PoolBall(0, 0.0F, -0.58F));

        List<Integer> poolNumbers = new ArrayList<>(List.of(
                1, 9, 2, 10, 8, 3, 4, 11, 5, 12, 13, 6, 14, 15, 7
        ));

        float spacing = PoolBall.RADIUS * 2.06F;
        float rowSpacing = spacing * 0.866F;
        float apexZ = 0.32F;

        int numIdx = 0;
        for (int row = 0; row < 5; row++) {
            float rowZ = apexZ + row * rowSpacing;
            float startX = -(row * spacing * 0.5F);
            for (int col = 0; col <= row; col++) {
                float ballX = startX + col * spacing;
                int id = poolNumbers.get(numIdx++);
                balls.add(new PoolBall(id, ballX, rowZ));
            }
        }

        currentTurn = 1;
        p1Suit = SUIT_NONE;
        p2Suit = SUIT_NONE;
        if (gameMode == MODE_PVP && player2UUID == null) {
            gameStatus = STATUS_WAITING_P2;
            statusMessage = Component.translatable("awakening_system.casino.pool.status.waiting_p2").getString();
        } else {
            gameStatus = STATUS_READY;
            statusMessage = Component.translatable("awakening_system.casino.pool.status.table_served", player1Name).getString();
        }
        cueAimAngle = 0.0F;
        cuePower = 0.0F;
        isAiming = false;
        aiShootTimer = 0;
    }

    public List<PoolBall> getBalls() {
        return balls;
    }

    public PoolBall getCueBall() {
        return balls.isEmpty() ? null : balls.get(0);
    }

    public int getGameMode() {
        return gameMode;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public int getPot() {
        return pot;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public int getP1Suit() {
        return p1Suit;
    }

    public int getP2Suit() {
        return p2Suit;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public float getCueAimAngle() {
        return cueAimAngle;
    }

    public float getCuePower() {
        return cuePower;
    }

    public boolean isAiming() {
        return isAiming;
    }

    public boolean isP1(ServerPlayer player) {
        return player != null && player.getUUID().equals(player1UUID);
    }

    public boolean isP2(ServerPlayer player) {
        return player != null && player.getUUID().equals(player2UUID);
    }

    public boolean isCurrentPlayer(ServerPlayer player) {
        if (player == null) return false;
        if (currentTurn == 1) return isP1(player);
        if (currentTurn == 2) return gameMode == MODE_PVP && isP2(player);
        return false;
    }

    public void setPlayer1(ServerPlayer player) {
        if (player != null) {
            this.player1UUID = player.getUUID();
            this.player1Name = player.getName().getString();
            setChanged();
        }
    }

    public void setPlayer2(ServerPlayer player) {
        if (player != null) {
            this.player2UUID = player.getUUID();
            this.player2Name = player.getName().getString();
            this.gameMode = MODE_PVP;
            if (this.gameStatus == STATUS_WAITING_P2) {
                this.gameStatus = STATUS_READY;
                this.statusMessage = Component.translatable("awakening_system.casino.pool.status.joined", this.player2Name, this.player1Name).getString();
            }
            setChanged();
            broadcastState();
        }
    }

    public void addBetToPot(int amount) {
        this.pot += amount;
        setChanged();
    }

    public void updateAim(float aimAngle, float power) {
        this.cueAimAngle = aimAngle;
        this.cuePower = power;
        this.isAiming = power > 0.01F;
        broadcastState();
    }

    /**
     * Aplica el tiro a la bola blanca con el ángulo y potencia especificados.
     */
    public boolean shoot(float aimAngle, float power) {
        if (gameStatus != STATUS_READY) return false;
        PoolBall cue = getCueBall();
        if (cue == null || cue.pocketed) return false;

        float clampedPower = Math.max(0.1F, Math.min(1.0F, power));
        float maxSpeed = 0.32F;
        float impulse = clampedPower * maxSpeed;

        cue.vx = (float) Math.sin(aimAngle) * impulse;
        cue.vz = (float) Math.cos(aimAngle) * impulse;

        this.cueAimAngle = aimAngle;
        this.cuePower = 0.0F;
        this.isAiming = false;
        this.gameStatus = STATUS_MOVING;

        this.firstBallHitThisTurn = -1;
        this.cueBallPocketedThisTurn = false;
        this.eightBallPocketedThisTurn = false;
        this.pocketedThisTurn.clear();

        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BONE_BLOCK_HIT, SoundSource.BLOCKS, 0.9F, 1.35F);
            level.playSound(null, worldPosition, SoundEvents.POINTED_DRIPSTONE_HIT, SoundSource.BLOCKS, 0.7F, 1.25F);
        }

        setChanged();
        broadcastState();
        return true;
    }

    public boolean isIllegal() {
        return getBlockState().getBlock() instanceof PoolTableBlock ptb && ptb.isIllegal();
    }

    public static int countBlackChips(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(SystemItems.CASINO_CHIP_BLACK.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean consumeBlackChips(Player player, int amount) {
        if (player.isCreative()) return true;
        if (countBlackChips(player) < amount) return false;
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(SystemItems.CASINO_CHIP_BLACK.get())) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    public void resetGame(int mode, int bet, ServerPlayer requester) {
        this.gameMode = mode;
        this.betAmount = bet;
        this.pot = 0;
        boolean illegal = isIllegal();

        if (requester != null) {
            this.player1UUID = requester.getUUID();
            this.player1Name = requester.getName().getString();

            if (bet > 0) {
                if (illegal) {
                    if (consumeBlackChips(requester, bet)) {
                        this.pot += bet;
                    } else {
                        requester.displayClientMessage(net.minecraft.network.chat.Component.translatable("awakening_system.casino.pool.insufficient_black_chips", bet), true);
                        this.betAmount = 0;
                    }
                } else {
                    if (com.soybe.awakeningsystem.casino.CasinoChipHelper.hasEnoughChips(requester, bet, false)
                            && com.soybe.awakeningsystem.casino.CasinoChipHelper.consumeChipValue(requester, bet, false)) {
                        this.pot += bet;
                    } else {
                        requester.displayClientMessage(net.minecraft.network.chat.Component.translatable("awakening_system.casino.pool.insufficient_chips", bet), true);
                        this.betAmount = 0;
                    }
                }
            }
        }

        if (mode == MODE_PVP) {
            this.player2UUID = null;
            this.player2Name = "Waiting for P2...";
        } else {
            this.player2UUID = null;
            this.player2Name = illegal
                    ? (mode == MODE_AI_HUSTLER ? "Underworld Hustler" : "Clandestine Shark")
                    : (mode == MODE_AI_HUSTLER ? "AI Hustler (Master)" : "AI Hustler");
            if (betAmount > 0) {
                this.pot += betAmount;
            }
        }

        setupRack();
        setChanged();
        broadcastState();
    }

    /**
     * Bucle de simulación física ejecutado en cada tick de servidor y cliente.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, PoolTableBlockEntity table) {
        if (table.gameStatus == STATUS_MOVING) {
            table.simulatePhysicsSubsteps();
            table.setChanged();

            if (table.areAllBallsStopped()) {
                table.onPhysicsHalted();
            } else {
                if (level.getGameTime() % 2 == 0) {
                    table.broadcastState();
                }
            }
        } else if (table.gameStatus == STATUS_READY && table.currentTurn == 2 && table.gameMode != MODE_PVP) {
            if (table.aiShootTimer <= 0) {
                table.aiShootTimer = 30;
            } else {
                table.aiShootTimer--;
                if (table.aiShootTimer == 0) {
                    table.executeAiTurn();
                }
            }
        }
    }

    private void simulatePhysicsSubsteps() {
        for (int step = 0; step < SUBSTEPS; step++) {
            for (PoolBall ball : balls) {
                if (ball.pocketed) continue;

                ball.x += ball.vx * SUBSTEP_DT;
                ball.z += ball.vz * SUBSTEP_DT;

                ball.vx *= 0.988F;
                ball.vz *= 0.988F;

                if (Math.abs(ball.vx) < 0.0002F) ball.vx = 0.0F;
                if (Math.abs(ball.vz) < 0.0002F) ball.vz = 0.0F;

                for (int p = 0; p < POCKETS.length; p++) {
                    float px = POCKETS[p][0];
                    float pz = POCKETS[p][1];
                    float dx = ball.x - px;
                    float dz = ball.z - pz;
                    float distSq = dx * dx + dz * dz;

                    if (distSq < POCKET_RADIUS * POCKET_RADIUS) {
                        ball.pocketed = true;
                        ball.pocketIndex = p;
                        ball.vx = 0.0F;
                        ball.vz = 0.0F;
                        pocketedThisTurn.add(ball);

                        if (ball.isCueBall()) cueBallPocketedThisTurn = true;
                        if (ball.isEightBall()) eightBallPocketedThisTurn = true;

                        if (level != null) {
                            level.playSound(null, worldPosition, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.6F, 0.8F);
                        }
                        break;
                    }
                }

                if (ball.pocketed) continue;

                float bounceFriction = 0.82F;
                if (ball.x - PoolBall.RADIUS < -TABLE_HALF_WIDTH) {
                    ball.x = -TABLE_HALF_WIDTH + PoolBall.RADIUS;
                    ball.vx = -ball.vx * bounceFriction;
                } else if (ball.x + PoolBall.RADIUS > TABLE_HALF_WIDTH) {
                    ball.x = TABLE_HALF_WIDTH - PoolBall.RADIUS;
                    ball.vx = -ball.vx * bounceFriction;
                }

                if (ball.z - PoolBall.RADIUS < -TABLE_HALF_LENGTH) {
                    ball.z = -TABLE_HALF_LENGTH + PoolBall.RADIUS;
                    ball.vz = -ball.vz * bounceFriction;
                } else if (ball.z + PoolBall.RADIUS > TABLE_HALF_LENGTH) {
                    ball.z = TABLE_HALF_LENGTH - PoolBall.RADIUS;
                    ball.vz = -ball.vz * bounceFriction;
                }
            }

            for (int i = 0; i < balls.size(); i++) {
                PoolBall a = balls.get(i);
                if (a.pocketed) continue;

                for (int j = i + 1; j < balls.size(); j++) {
                    PoolBall b = balls.get(j);
                    if (b.pocketed) continue;

                    float dx = b.x - a.x;
                    float dz = b.z - a.z;
                    float distSq = dx * dx + dz * dz;
                    float minDist = PoolBall.RADIUS * 2.0F;

                    if (distSq < minDist * minDist && distSq > 0.000001F) {
                        float dist = (float) Math.sqrt(distSq);
                        float nx = dx / dist;
                        float nz = dz / dist;

                        float overlap = 0.5F * (minDist - dist);
                        a.x -= nx * overlap;
                        a.z -= nz * overlap;
                        b.x += nx * overlap;
                        b.z += nz * overlap;

                        float kx = a.vx - b.vx;
                        float kz = a.vz - b.vz;
                        float p = 2.0F * (nx * kx + nz * kz) / 2.0F;

                        if (p > 0.0F) {
                            float restitution = 0.94F;
                            a.vx -= p * nx * restitution;
                            a.vz -= p * nz * restitution;
                            b.vx += p * nx * restitution;
                            b.vz += p * nz * restitution;

                            if (a.isCueBall() && firstBallHitThisTurn == -1) firstBallHitThisTurn = b.getId();
                            if (b.isCueBall() && firstBallHitThisTurn == -1) firstBallHitThisTurn = a.getId();
                        }
                    }
                }
            }
        }
    }

    private boolean areAllBallsStopped() {
        for (PoolBall b : balls) {
            if (b.isMoving()) return false;
        }
        return true;
    }

    /**
     * Evalúa las reglas oficiales del 8-Ball al detenerse el movimiento:
     * faltas, asignación de lisas/rayadas, entroneramiento de bola 8 y cambio de turno.
     */
    private void onPhysicsHalted() {
        String shooter = (currentTurn == 1) ? player1Name : player2Name;
        int activeSuit = (currentTurn == 1) ? p1Suit : p2Suit;

        if (eightBallPocketedThisTurn) {
            boolean hasClearedAllOwnBalls = areAllSuitBallsPocketed(activeSuit);
            if (hasClearedAllOwnBalls && !cueBallPocketedThisTurn) {
                gameStatus = (currentTurn == 1) ? STATUS_P1_WON : STATUS_P2_WON;
                statusMessage = Component.translatable("awakening_system.casino.pool.status.win_8ball", shooter).getString();
                awardPotToWinner(currentTurn);
            } else {
                gameStatus = STATUS_SCRATCH_LOSS;
                int winnerTurn = (currentTurn == 1) ? 2 : 1;
                String winnerName = (winnerTurn == 1) ? player1Name : player2Name;
                statusMessage = Component.translatable("awakening_system.casino.pool.status.foul_8ball", shooter, winnerName).getString();
                awardPotToWinner(winnerTurn);
            }
            broadcastState();
            return;
        }

        if (cueBallPocketedThisTurn) {
            PoolBall cue = getCueBall();
            if (cue != null) {
                cue.pocketed = false;
                cue.x = 0.0F;
                cue.z = -0.60F;
                cue.vx = 0.0F;
                cue.vz = 0.0F;
            }
            currentTurn = 3 - currentTurn;
            String nextShooter = (currentTurn == 1) ? player1Name : player2Name;
            statusMessage = Component.translatable("awakening_system.casino.pool.status.foul_cue", nextShooter).getString();
            gameStatus = STATUS_READY;
            broadcastState();
            return;
        }

        if (p1Suit == SUIT_NONE && !pocketedThisTurn.isEmpty()) {
            for (PoolBall b : pocketedThisTurn) {
                if (b.isSolid()) {
                    p1Suit = (currentTurn == 1) ? SUIT_SOLIDS : SUIT_STRIPES;
                    p2Suit = (currentTurn == 1) ? SUIT_STRIPES : SUIT_SOLIDS;
                    break;
                } else if (b.isStripe()) {
                    p1Suit = (currentTurn == 1) ? SUIT_STRIPES : SUIT_SOLIDS;
                    p2Suit = (currentTurn == 1) ? SUIT_SOLIDS : SUIT_STRIPES;
                    break;
                }
            }
        }

        boolean pocketedOwnBall = false;
        if (!pocketedThisTurn.isEmpty()) {
            for (PoolBall b : pocketedThisTurn) {
                if (activeSuit == SUIT_SOLIDS && b.isSolid()) pocketedOwnBall = true;
                if (activeSuit == SUIT_STRIPES && b.isStripe()) pocketedOwnBall = true;
                if (activeSuit == SUIT_NONE && (b.isSolid() || b.isStripe())) pocketedOwnBall = true;
            }
        }

        if (pocketedOwnBall) {
            statusMessage = Component.translatable("awakening_system.casino.pool.status.good_shot", shooter).getString();
        } else {
            currentTurn = 3 - currentTurn;
            String nextShooter = (currentTurn == 1) ? player1Name : player2Name;
            statusMessage = Component.translatable("awakening_system.casino.pool.status.turn", nextShooter).getString();
        }

        gameStatus = STATUS_READY;
        broadcastState();
    }

    private boolean areAllSuitBallsPocketed(int suit) {
        if (suit == SUIT_NONE) return false;
        for (PoolBall b : balls) {
            if (b.pocketed) continue;
            if (suit == SUIT_SOLIDS && b.isSolid()) return false;
            if (suit == SUIT_STRIPES && b.isStripe()) return false;
        }
        return true;
    }

    private void awardPotToWinner(int winnerTurn) {
        if (pot <= 0 || level == null || level.isClientSide()) return;
        UUID winnerUUID = (winnerTurn == 1) ? player1UUID : player2UUID;
        if (winnerUUID != null && level instanceof ServerLevel sl) {
            ServerPlayer winner = sl.getServer().getPlayerList().getPlayer(winnerUUID);
            if (winner != null) {
                SystemData data = SystemEvents.data(winner);
                data.incrementPoolMatchesWon();

                if (isIllegal()) {
                    ItemStack chips = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), pot);
                    if (!winner.getInventory().add(chips)) {
                        winner.drop(chips, false);
                    }
                    data.addCasinoWinnings(pot * 2000L);
                    winner.displayClientMessage(net.minecraft.network.chat.Component.translatable("awakening_system.casino.pool.win_illegal", pot), false);
                    winner.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

                    if (pot >= 5) {
                        com.soybe.awakeningsystem.casino.CasinoTrophyManager.tryClaimTrophy(
                                sl, winner, 4, "Clandestine Felt King (" + pot + " Black Chips)"
                        );
                    }
                } else {
                    com.soybe.awakeningsystem.casino.CasinoChipHelper.giveChips(winner, pot, false);
                    data.addCasinoWinnings(pot);
                    winner.displayClientMessage(net.minecraft.network.chat.Component.translatable("awakening_system.casino.pool.win_legal", String.format(java.util.Locale.US, "%,d", pot)), false);
                    winner.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

                    if (pot >= 10000) {
                        com.soybe.awakeningsystem.casino.CasinoTrophyManager.tryClaimTrophy(
                                sl, winner, 4, "8-Ball Pool Master (" + String.format(java.util.Locale.US, "%,d", pot) + " Chips)"
                        );
                    }
                }
                AwakeningSystemMod.sync(winner);
            }
        }
        pot = 0;
    }

    /**
     * Lógica de puntería de la IA:
     * Busca la bola objetivo legal más accesible y calcula el tiro hacia la tronera más favorable.
     */
    private void executeAiTurn() {
        PoolBall cue = getCueBall();
        if (cue == null || cue.pocketed) return;

        int aiSuit = p2Suit;
        PoolBall targetBall = null;
        float bestScore = -9999.0F;
        float bestAngle = 0.0F;
        float bestPower = 0.65F;

        boolean targetEight = areAllSuitBallsPocketed(aiSuit);

        for (PoolBall b : balls) {
            if (b.pocketed || b.isCueBall()) continue;
            if (!targetEight && b.isEightBall()) continue;
            if (!targetEight && aiSuit == SUIT_SOLIDS && !b.isSolid()) continue;
            if (!targetEight && aiSuit == SUIT_STRIPES && !b.isStripe()) continue;

            for (float[] pocket : POCKETS) {
                float toPocketX = pocket[0] - b.x;
                float toPocketZ = pocket[1] - b.z;
                float pocketDist = (float) Math.sqrt(toPocketX * toPocketX + toPocketZ * toPocketZ);
                if (pocketDist < 0.001F) continue;

                float ghostX = b.x - (toPocketX / pocketDist) * (PoolBall.RADIUS * 2.0F);
                float ghostZ = b.z - (toPocketZ / pocketDist) * (PoolBall.RADIUS * 2.0F);

                float cueToGhostX = ghostX - cue.x;
                float cueToGhostZ = ghostZ - cue.z;
                float cueDist = (float) Math.sqrt(cueToGhostX * cueToGhostX + cueToGhostZ * cueToGhostZ);

                float score = 10.0F - (pocketDist * 3.0F + cueDist * 2.0F);
                if (score > bestScore) {
                    bestScore = score;
                    targetBall = b;
                    bestAngle = (float) Math.atan2(cueToGhostX, cueToGhostZ);
                    bestPower = Math.max(0.35F, Math.min(0.95F, 0.4F + (pocketDist + cueDist) * 0.35F));
                }
            }
        }

        if (targetBall == null) {
            bestAngle = (float) (Math.random() * Math.PI * 2.0);
            bestPower = 0.5F;
        }

        if (gameMode == MODE_AI_NORMAL) {
            bestAngle += (float) ((Math.random() - 0.5) * 0.12);
        }

        shoot(bestAngle, bestPower);
    }

    public void broadcastState() {
        if (level != null && !level.isClientSide()) {
            PoolTableStateS2CPayload payload = new PoolTableStateS2CPayload(
                    worldPosition,
                    new ArrayList<>(balls),
                    gameMode,
                    betAmount,
                    player1Name,
                    player2Name,
                    currentTurn,
                    p1Suit,
                    p2Suit,
                    gameStatus,
                    statusMessage,
                    cueAimAngle,
                    cuePower,
                    isAiming,
                    isIllegal()
            );
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, level.getChunkAt(worldPosition).getPos(), payload);
        }
    }

    public void applyStatePayload(PoolTableStateS2CPayload payload) {
        this.balls.clear();
        this.balls.addAll(payload.balls());
        this.gameMode = payload.gameMode();
        this.betAmount = payload.betAmount();
        this.player1Name = payload.player1Name();
        this.player2Name = payload.player2Name();
        this.currentTurn = payload.currentTurn();
        this.p1Suit = payload.p1Suit();
        this.p2Suit = payload.p2Suit();
        this.gameStatus = payload.gameStatus();
        this.statusMessage = payload.statusMessage();
        this.cueAimAngle = payload.cueAimAngle();
        this.cuePower = payload.cuePower();
        this.isAiming = payload.isAiming();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag ballList = new ListTag();
        for (PoolBall b : balls) {
            ballList.add(b.toTag());
        }
        tag.put("balls", ballList);
        tag.putInt("gameMode", gameMode);
        tag.putInt("betAmount", betAmount);
        tag.putInt("pot", pot);
        if (player1UUID != null) tag.putUUID("p1UUID", player1UUID);
        tag.putString("p1Name", player1Name);
        if (player2UUID != null) tag.putUUID("p2UUID", player2UUID);
        tag.putString("p2Name", player2Name);
        tag.putInt("currentTurn", currentTurn);
        tag.putInt("p1Suit", p1Suit);
        tag.putInt("p2Suit", p2Suit);
        tag.putInt("gameStatus", gameStatus);
        tag.putString("statusMessage", statusMessage);
        tag.putFloat("cueAngle", cueAimAngle);
        tag.putFloat("cuePower", cuePower);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("balls")) {
            balls.clear();
            ListTag list = tag.getList("balls", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag bTag = list.getCompound(i);
                PoolBall ball = new PoolBall(bTag.getInt("id"), bTag.getFloat("x"), bTag.getFloat("z"));
                ball.readTag(bTag);
                balls.add(ball);
            }
        }
        this.gameMode = tag.getInt("gameMode");
        this.betAmount = tag.getInt("betAmount");
        this.pot = tag.getInt("pot");
        if (tag.hasUUID("p1UUID")) this.player1UUID = tag.getUUID("p1UUID");
        this.player1Name = tag.getString("p1Name");
        if (tag.hasUUID("p2UUID")) this.player2UUID = tag.getUUID("p2UUID");
        this.player2Name = tag.getString("p2Name");
        this.currentTurn = tag.getInt("currentTurn");
        this.p1Suit = tag.getInt("p1Suit");
        this.p2Suit = tag.getInt("p2Suit");
        this.gameStatus = tag.getInt("gameStatus");
        this.statusMessage = tag.getString("statusMessage");
        this.cueAimAngle = tag.getFloat("cueAngle");
        this.cuePower = tag.getFloat("cuePower");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
