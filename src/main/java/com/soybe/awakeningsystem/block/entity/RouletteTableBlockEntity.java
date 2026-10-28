package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.casino.roulette.RouletteWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class RouletteTableBlockEntity extends BlockEntity {

    private float wheelAngle = 0.0F;
    private float prevWheelAngle = 0.0F;
    private float wheelSpeed = 0.0F;

    private float ballAngle = 0.0F;
    private float prevBallAngle = 0.0F;
    private float ballSpeed = 0.0F;

    private boolean isSpinning = false;
    private int spinTicks = 0;
    private int restingTicks = 0;
    private int winningNumber = 0;

    private UUID spinningPlayer = null;
    private String spinningPlayerName = "";
    private long lastPayout = 0L;
    private long lastTotalBet = 0L;

    public RouletteTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROULETTE_TABLE.get(), pos, state);
    }

    public static final int TOTAL_SPIN_TICKS = 140;

    private float startWheelAngle = 0.0F;
    private float targetWheelDelta = 1440.0F;
    private float startBallAngle = 0.0F;
    private float targetBallDelta = -2880.0F;

    public void startSpin(int winningNumber) {
        startSpin(winningNumber, null, 0L, 0L);
    }

    public void startSpin(int winningNumber, ServerPlayer player, long payout, long totalBet) {
        this.winningNumber = winningNumber;
        this.spinTicks = TOTAL_SPIN_TICKS;
        this.restingTicks = 0;
        this.isSpinning = true;
        this.spinningPlayer = player != null ? player.getUUID() : null;
        this.spinningPlayerName = player != null ? player.getName().getString() : "";
        this.lastPayout = payout;
        this.lastTotalBet = totalBet;

        this.startWheelAngle = this.wheelAngle % 360.0F;
        this.startBallAngle = this.ballAngle % 360.0F;

        int winIdx = RouletteWheel.getIndexInWheel(winningNumber);
        float pocketAngleOnWheel = (360.0F / RouletteWheel.POCKET_COUNT) * winIdx;
        this.targetWheelDelta = 1440.0F;
        float finalWheelAngle = (this.startWheelAngle + this.targetWheelDelta) % 360.0F;
        float finalPocketWorld = (finalWheelAngle + (360.0F - pocketAngleOnWheel)) % 360.0F;

        float ballAngleOffset = (finalPocketWorld - (this.startBallAngle % 360.0F));
        while (ballAngleOffset > 0.0F) ballAngleOffset -= 360.0F;
        this.targetBallDelta = -2880.0F + ballAngleOffset;

        if (level != null && !level.isClientSide()) {
            level.playSound(null, worldPosition, ModSounds.ROULETTE_SPIN.get(), SoundSource.BLOCKS, 1.2F, 1.0F);
            level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8F, 1.6F);
            setChanged();
            if (level instanceof ServerLevel sl) {
                sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RouletteTableBlockEntity be) {
        be.prevWheelAngle = be.wheelAngle;
        be.prevBallAngle = be.ballAngle;

        if (be.isSpinning && be.spinTicks > 0) {
            be.spinTicks--;

            int elapsed = TOTAL_SPIN_TICKS - be.spinTicks;
            float p = (float) elapsed / (float) TOTAL_SPIN_TICKS;
            // Desaceleración cuártica ultrasuave: velocidad decae de forma paulatina y natural
            float ease = 1.0F - (float) Math.pow(1.0F - p, 4.0);

            be.wheelAngle = (be.startWheelAngle + be.targetWheelDelta * ease) % 360.0F;
            be.ballAngle = (be.startBallAngle + be.targetBallDelta * ease) % 360.0F;
            if (be.wheelAngle < 0.0F) be.wheelAngle += 360.0F;
            if (be.ballAngle < 0.0F) be.ballAngle += 360.0F;

            // Clacs audibles de bola sobre casillas con espaciado progresivo y suave
            int clickInterval = 3 + (int) (p * p * 12);
            if (elapsed % clickInterval == 0 && level != null && !level.isClientSide() && p < 0.95F) {
                float pitch = 1.3F - (p * 0.5F);
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.40F, pitch);
            }

            if (be.spinTicks == 0) {
                be.isSpinning = false;
                be.restingTicks = 120;

                if (level instanceof ServerLevel sl) {
                    be.deliverPayout(sl, pos);
                }
            }
        } else {
            // Rodaje suave por inercia: La rueda jamás se congela de golpe, gira calmadamente
            be.wheelAngle = (be.wheelAngle + 0.18F) % 360.0F;
            if (be.restingTicks > 0) {
                be.restingTicks--;
            }
            // La bola descansa fijada en la casilla ganadora acompañando la rotación de la rueda
            int winIdx = RouletteWheel.getIndexInWheel(be.winningNumber);
            float pocketAngleOnWheel = (360.0F / RouletteWheel.POCKET_COUNT) * winIdx;
            be.ballAngle = (be.wheelAngle + (360.0F - pocketAngleOnWheel)) % 360.0F;
        }
    }

    private void deliverPayout(ServerLevel sl, BlockPos pos) {
        RouletteWheel.PocketColor color = RouletteWheel.getColor(winningNumber);
        String colorStr = color == RouletteWheel.PocketColor.GREEN ? "§aVERDE" :
                (color == RouletteWheel.PocketColor.RED ? "§cROJO" : "§8NEGRO");

        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.85D;
        double cz = pos.getZ() + 0.5D;

        sl.playSound(null, pos, SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 1.0F, 1.2F);

        if (lastPayout > 0) {
            sl.playSound(null, pos, ModSounds.ROULETTE_WIN.get(), SoundSource.BLOCKS, 1.2F, 1.0F);
            boolean bigWin = lastPayout >= 10000L || lastPayout >= lastTotalBet * 10;
            if (bigWin) {
                sl.playSound(null, pos, ModSounds.CASINO_JACKPOT.get(), SoundSource.BLOCKS, 1.3F, 1.0F);
                sl.sendParticles(ParticleTypes.FIREWORK, cx, cy + 0.5D, cz, 40, 0.4D, 0.5D, 0.4D, 0.1D);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy + 0.2D, cz, 30, 0.3D, 0.3D, 0.3D, 0.15D);
            } else {
                sl.playSound(null, pos, ModSounds.CASINO_WIN.get(), SoundSource.BLOCKS, 1.1F, 1.0F);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy + 0.2D, cz, 20, 0.3D, 0.2D, 0.3D, 0.08D);
            }

            if (spinningPlayer != null) {
                Player p = sl.getPlayerByUUID(spinningPlayer);
                if (p instanceof ServerPlayer sp) {
                    Component msg = Component.translatable("awakening_system.casino.roulette.win_announcement",
                            winningNumber, Component.translatable("awakening_system.roulette.color." + color.name().toLowerCase(java.util.Locale.ROOT)), String.format(java.util.Locale.US, "%,d", lastPayout));
                    sp.sendSystemMessage(msg, false);
                    sp.sendSystemMessage(msg, true);
                }
            }
        } else {
            sl.playSound(null, pos, ModSounds.CASINO_LOSE.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
            sl.sendParticles(ParticleTypes.SMOKE, cx, cy + 0.2D, cz, 12, 0.2D, 0.1D, 0.2D, 0.02D);

            if (spinningPlayer != null) {
                Player p = sl.getPlayerByUUID(spinningPlayer);
                if (p instanceof ServerPlayer sp) {
                    Component msg = Component.translatable("awakening_system.casino.roulette.lose_announcement",
                            winningNumber, Component.translatable("awakening_system.roulette.color." + color.name().toLowerCase(java.util.Locale.ROOT)));
                    sp.sendSystemMessage(msg, false);
                    sp.sendSystemMessage(msg, true);
                }
            }
        }

        setChanged();
        sl.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
    }

    public float getInterpolatedWheelAngle(float partialTick) {
        return Mth.rotLerp(partialTick, prevWheelAngle, wheelAngle);
    }

    public float getInterpolatedBallAngle(float partialTick) {
        return Mth.rotLerp(partialTick, prevBallAngle, ballAngle);
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public int getSpinTicks() {
        return spinTicks;
    }

    public float getSpinProgress(float partialTick) {
        if (!isSpinning) return 0.0F;
        float remaining = Math.max(0.0F, spinTicks - partialTick);
        return remaining / (float) TOTAL_SPIN_TICKS;
    }

    public int getTotalSpinDuration() {
        return TOTAL_SPIN_TICKS;
    }

    public long getLastPayout() {
        return lastPayout;
    }

    public int getWinningNumber() {
        return winningNumber;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("wheelAngle", wheelAngle);
        tag.putFloat("wheelSpeed", wheelSpeed);
        tag.putFloat("ballAngle", ballAngle);
        tag.putFloat("ballSpeed", ballSpeed);
        tag.putBoolean("isSpinning", isSpinning);
        tag.putInt("spinTicks", spinTicks);
        tag.putInt("restingTicks", restingTicks);
        tag.putInt("winNumber", winningNumber);
        tag.putLong("payout", lastPayout);
        tag.putLong("bet", lastTotalBet);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.wheelAngle = tag.getFloat("wheelAngle");
        this.prevWheelAngle = this.wheelAngle;
        this.wheelSpeed = tag.getFloat("wheelSpeed");
        this.ballAngle = tag.getFloat("ballAngle");
        this.prevBallAngle = this.ballAngle;
        this.ballSpeed = tag.getFloat("ballSpeed");
        this.isSpinning = tag.getBoolean("isSpinning");
        this.spinTicks = tag.getInt("spinTicks");
        this.restingTicks = tag.getInt("restingTicks");
        this.winningNumber = tag.getInt("winNumber");
        this.lastPayout = tag.getLong("payout");
        this.lastTotalBet = tag.getLong("bet");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, lookupProvider);
        }
    }
}
