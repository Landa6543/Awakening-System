package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SlotMachineBlock;
import com.soybe.awakeningsystem.casino.slot.SlotSymbol;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SlotMachineBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);
    private static final Random RNG = new Random();
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private int spinTicks = 0;
    private CasinoChipItem.ChipTier currentTier = CasinoChipItem.ChipTier.BRONZE;

    private SlotSymbol symbol0 = SlotSymbol.MORA;
    private SlotSymbol symbol1 = SlotSymbol.MORA;
    private SlotSymbol symbol2 = SlotSymbol.MORA;

    private float leverAngle = 0.0F;
    private float prevLeverAngle = 0.0F;

    private final float[] reelAngles = new float[] { 0.0F, 0.0F, 0.0F };
    private final float[] prevReelAngles = new float[] { 0.0F, 0.0F, 0.0F };
    private final float[] startReelAngles = new float[] { 0.0F, 0.0F, 0.0F };
    private final float[] finalReelAngles = new float[] { 0.0F, 0.0F, 0.0F };

    private boolean winningSpin = false;
    private boolean isJackpot = false;
    private long winMora = 0;
    private long lastDisplayWinMora = 0;
    private final List<ItemStack> pendingRewards = new ArrayList<>();

    private UUID spinningPlayer = null;
    private String spinningPlayerName = "";

    private UUID ownerUUID = null;
    private String ownerName = "";
    private int reserveChips = 0;
    private int collectedVaultChips = 0;
    private int totalCustomerSpins = 0;
    private int totalWonChips = 0;

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName != null && !ownerName.isEmpty() ? ownerName : "Desconocido"; }
    public void setOwner(Player player) {
        if (player != null) {
            this.ownerUUID = player.getUUID();
            this.ownerName = player.getName().getString();
            setChanged();
        }
    }
    public boolean isOwner(Player player) {
        if (player == null) return false;
        if (ownerUUID == null) return true;
        return ownerUUID.equals(player.getUUID());
    }
    public int getReserveChips() { return reserveChips; }
    public int getCollectedVaultChips() { return collectedVaultChips; }
    public int getTotalCustomerSpins() { return totalCustomerSpins; }
    public int getTotalWonChips() { return totalWonChips; }

    public static int countBlackChips(Player player) {
        if (player == null) return 0;
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(SystemItems.CASINO_CHIP_BLACK.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean consumeBlackChips(Player player, int amount) {
        if (player == null) return false;
        if (player.isCreative()) return true;
        if (countBlackChips(player) < amount) return false;
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(SystemItems.CASINO_CHIP_BLACK.get())) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    public static int countLegalChips(Player player) {
        if (player == null) return 0;
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(SystemItems.CASINO_CHIP_GOLD.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean consumeLegalChips(Player player, int amount) {
        if (player == null) return false;
        if (player.isCreative()) return true;
        if (countLegalChips(player) < amount) return false;
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(SystemItems.CASINO_CHIP_GOLD.get())) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    public boolean depositReserve(Player player, int amount) {
        if (player == null) return false;
        int countInInv = countBlackChips(player);
        int toDeposit = (amount <= 0 || amount > countInInv) ? countInInv : amount;
        if (toDeposit <= 0) return false;
        if (consumeBlackChips(player, toDeposit)) {
            this.reserveChips += toDeposit;
            setChanged();
            if (level instanceof ServerLevel sl) {
                sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    public int withdrawVault(Player player) {
        if (player == null || (collectedVaultChips <= 0 && reserveChips <= 0)) return 0;
        int amount = collectedVaultChips + reserveChips;
        ItemStack stack = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), amount);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        collectedVaultChips = 0;
        reserveChips = 0;
        setChanged();
        if (level instanceof ServerLevel sl) {
            sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return amount;
    }

    public int withdrawReserve(Player player, int amount) {
        if (player == null || reserveChips <= 0) return 0;
        int toWithdraw = (amount <= 0 || amount > reserveChips) ? reserveChips : amount;
        if (toWithdraw <= 0) return 0;
        ItemStack stack = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), toWithdraw);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        reserveChips -= toWithdraw;
        setChanged();
        if (level instanceof ServerLevel sl) {
            sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return toWithdraw;
    }

    public void onCustomerLostBet(int betChips) {
        this.collectedVaultChips += betChips;
        this.totalCustomerSpins++;
        setChanged();
    }

    public void onCustomerWonBet(int wonChips) {
        this.reserveChips = Math.max(0, this.reserveChips - wonChips);
        this.totalCustomerSpins++;
        this.totalWonChips += wonChips;
        setChanged();
    }

    public SlotMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLOT_MACHINE.get(), pos, state);
        initInitialAngles();
    }

    public static float getTargetAngleForSymbol(SlotSymbol sym) {
        return switch (sym) {
            case SEVEN -> 0.0F;
            case LIGHT_CONE -> 45.0F;
            case STARDUST -> 90.0F;
            case STARGLITTER -> 135.0F;
            case INTERTWINED_FATE -> 180.0F;
            case SLIME -> 225.0F;
            case PRIMOGEM -> 270.0F;
            case MORA -> 315.0F;
            case PAIMON -> 45.0F;
        };
    }

    private void initInitialAngles() {
        for (int i = 0; i < 3; i++) {
            float angle = getTargetAngleForSymbol(getSymbol(i));
            finalReelAngles[i] = angle;
            startReelAngles[i] = angle;
            reelAngles[i] = angle;
            prevReelAngles[i] = angle;
        }
    }

    public boolean isSpinning() {
        return spinTicks > 0;
    }

    public boolean isInUse() {
        return isSpinning();
    }

    public boolean isUsedBy(Player player) {
        return spinningPlayer != null && spinningPlayer.equals(player.getUUID());
    }

    public boolean tryLock(Player player) {
        return !isSpinning();
    }

    public void unlock(Player player) {
    }

    public void triggerSpin() {
        this.spinTicks = 80;
    }

    public float getLeverAngle() {
        return leverAngle;
    }

    public float getPrevLeverAngle() {
        return prevLeverAngle;
    }

    public float getReelAngle(int index) {
        return (index >= 0 && index < 3) ? reelAngles[index] : 0.0F;
    }

    public float getPrevReelAngle(int index) {
        return (index >= 0 && index < 3) ? prevReelAngles[index] : 0.0F;
    }

    public float getInterpolatedLeverAngle(float partialTick) {
        return net.minecraft.util.Mth.lerp(partialTick, prevLeverAngle, leverAngle);
    }

    public float getInterpolatedReelAngle(int index, float partialTick) {
        if (index < 0 || index >= 3) return 0.0F;
        return net.minecraft.util.Mth.lerp(partialTick, prevReelAngles[index], reelAngles[index]);
    }

    public SlotSymbol getSymbol(int index) {
        return switch (index) {
            case 0 -> symbol0;
            case 1 -> symbol1;
            case 2 -> symbol2;
            default -> SlotSymbol.MORA;
        };
    }

    public boolean isWinningSpin() {
        return winningSpin;
    }

    public boolean isJackpot() {
        return isJackpot;
    }

    public CasinoChipItem.ChipTier getCurrentTier() {
        return currentTier;
    }

    /**
     * Inicia una tirada física in-world consumiendo la ficha del jugador.
     */
    public boolean startSpin(Player player, CasinoChipItem.ChipTier tier) {
        if (isSpinning()) return false;

        this.spinTicks = 80;
        this.currentTier = tier;
        this.spinningPlayer = player.getUUID();
        this.spinningPlayerName = player.getName().getString();
        this.pendingRewards.clear();
        this.winningSpin = false;
        this.isJackpot = false;
        this.winMora = 0;

        calculateSpinResult(tier);

        for (int i = 0; i < 3; i++) {
            startReelAngles[i] = reelAngles[i];
            int revs = 4 + i * 2;
            float targetMod = getTargetAngleForSymbol(getSymbol(i));
            float currentMod = (startReelAngles[i] % 360.0F + 360.0F) % 360.0F;
            float diff = targetMod - currentMod;
            if (diff <= 0.0F) diff += 360.0F;
            finalReelAngles[i] = startReelAngles[i] + (revs * 360.0F) + diff;
        }

        if (level != null && !level.isClientSide()) {
            level.playSound(null, worldPosition, ModSounds.CASINO_INSERT_COIN.get(), SoundSource.BLOCKS, 1.2F, 1.0F);
            level.playSound(null, worldPosition, ModSounds.CASINO_REELS_SPINNING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return true;
    }

    /**
     * Inicia una tirada desde la interfaz de usuario con apuesta en Mora.
     * Sincroniza 1:1 la física del bloque in-world, palanca y tambores con los símbolos obtenidos.
     */
    public boolean startMoraSpin(ServerPlayer player, SlotSymbol s0, SlotSymbol s1, SlotSymbol s2, boolean isWin, boolean jackpot, long moraWon) {
        this.spinTicks = 80;
        this.currentTier = (getBlockState().getBlock() instanceof SlotMachineBlock smb && smb.isIllegal())
                ? CasinoChipItem.ChipTier.BLACK
                : CasinoChipItem.ChipTier.BRONZE;
        this.spinningPlayer = player.getUUID();
        this.spinningPlayerName = player.getName().getString();
        this.pendingRewards.clear();
        this.winningSpin = isWin;
        this.isJackpot = jackpot;
        this.winMora = 0;
        this.lastDisplayWinMora = moraWon;
        this.symbol0 = s0;
        this.symbol1 = s1;
        this.symbol2 = s2;

        if (jackpot) {
            this.pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_PLATINUM.get(), 3));
        }

        for (int i = 0; i < 3; i++) {
            startReelAngles[i] = reelAngles[i];
            int revs = 4 + i * 2;
            float targetMod = getTargetAngleForSymbol(getSymbol(i));
            float currentMod = (startReelAngles[i] % 360.0F + 360.0F) % 360.0F;
            float diff = targetMod - currentMod;
            if (diff <= 0.0F) diff += 360.0F;
            finalReelAngles[i] = startReelAngles[i] + (revs * 360.0F) + diff;
        }

        if (level != null && !level.isClientSide()) {
            level.playSound(null, worldPosition, ModSounds.CASINO_INSERT_COIN.get(), SoundSource.BLOCKS, 1.2F, 1.0F);
            level.playSound(null, worldPosition, ModSounds.CASINO_REELS_SPINNING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return true;
    }

    private static final SlotSymbol[] PHYSICAL_REEL_SYMBOLS = {
            SlotSymbol.SEVEN,
            SlotSymbol.LIGHT_CONE,
            SlotSymbol.STARDUST,
            SlotSymbol.STARGLITTER,
            SlotSymbol.INTERTWINED_FATE,
            SlotSymbol.SLIME,
            SlotSymbol.PRIMOGEM,
            SlotSymbol.MORA
    };

    private void calculateSpinResult(CasinoChipItem.ChipTier tier) {
        SlotSymbol[] all = PHYSICAL_REEL_SYMBOLS;
        int roll = RNG.nextInt(100);

        if (roll < 20) {
            SlotSymbol chosen = selectWeightedTripleSymbol(tier);
            symbol0 = chosen;
            symbol1 = chosen;
            symbol2 = chosen;
            evaluateTripleMatch(tier, chosen);
        } else if (roll < 55) {
            SlotSymbol matchSym = all[RNG.nextInt(all.length)];
            SlotSymbol otherSym;
            do {
                otherSym = all[RNG.nextInt(all.length)];
            } while (otherSym == matchSym);

            int oddReel = RNG.nextInt(3);
            symbol0 = (oddReel == 0) ? otherSym : matchSym;
            symbol1 = (oddReel == 1) ? otherSym : matchSym;
            symbol2 = (oddReel == 2) ? otherSym : matchSym;
            evaluateDoubleMatch(tier, matchSym);
        } else {
            symbol0 = all[RNG.nextInt(all.length)];
            do {
                symbol1 = all[RNG.nextInt(all.length)];
            } while (symbol1 == symbol0);
            do {
                symbol2 = all[RNG.nextInt(all.length)];
            } while (symbol2 == symbol0 || symbol2 == symbol1);

            winningSpin = false;
        }
    }

    private SlotSymbol selectWeightedTripleSymbol(CasinoChipItem.ChipTier tier) {
        int r = RNG.nextInt(100);
        return switch (tier) {
            case BLACK -> {
                if (r < 15) yield SlotSymbol.SEVEN;
                if (r < 35) yield SlotSymbol.LIGHT_CONE;
                if (r < 55) yield SlotSymbol.INTERTWINED_FATE;
                if (r < 75) yield SlotSymbol.PRIMOGEM;
                yield SlotSymbol.STARGLITTER;
            }
            case PLATINUM -> {
                if (r < 12) yield SlotSymbol.SEVEN;
                if (r < 30) yield SlotSymbol.LIGHT_CONE;
                if (r < 50) yield SlotSymbol.INTERTWINED_FATE;
                if (r < 72) yield SlotSymbol.PRIMOGEM;
                yield SlotSymbol.STARGLITTER;
            }
            case GOLD -> {
                if (r < 8) yield SlotSymbol.SEVEN;
                if (r < 22) yield SlotSymbol.LIGHT_CONE;
                if (r < 45) yield SlotSymbol.INTERTWINED_FATE;
                if (r < 70) yield SlotSymbol.PRIMOGEM;
                yield SlotSymbol.STARGLITTER;
            }
            case SILVER -> {
                if (r < 4) yield SlotSymbol.SEVEN;
                if (r < 16) yield SlotSymbol.LIGHT_CONE;
                if (r < 35) yield SlotSymbol.INTERTWINED_FATE;
                if (r < 55) yield SlotSymbol.PRIMOGEM;
                if (r < 75) yield SlotSymbol.STARGLITTER;
                yield SlotSymbol.STARDUST;
            }
            case BRONZE -> {
                if (r < 2) yield SlotSymbol.SEVEN;
                if (r < 10) yield SlotSymbol.LIGHT_CONE;
                if (r < 22) yield SlotSymbol.INTERTWINED_FATE;
                if (r < 42) yield SlotSymbol.PRIMOGEM;
                if (r < 62) yield SlotSymbol.STARGLITTER;
                if (r < 82) yield SlotSymbol.STARDUST;
                yield SlotSymbol.MORA;
            }
        };
    }

    private void evaluateTripleMatch(CasinoChipItem.ChipTier tier, SlotSymbol sym) {
        winningSpin = true;
        int mult = Math.max(5, sym.getTripleMultiplier());
        long baseMora = (long) tier.getMoraValue() * mult;
        winMora = baseMora;

        if (sym == SlotSymbol.SEVEN) {
            isJackpot = true;
            winMora = (long) tier.getMoraValue() * 500L;
            switch (tier) {
                case BLACK -> {
                    pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 5));
                    pendingRewards.add(new ItemStack(SystemItems.PRIMOGEM.get(), 3200));
                    pendingRewards.add(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 20));
                    pendingRewards.add(new ItemStack(SystemItems.END_VOID_KEY.get(), 2));
                    pendingRewards.add(new ItemStack(Items.NETHERITE_BLOCK, 3));
                }
                case PLATINUM -> {
                    pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_PLATINUM.get(), 10));
                    pendingRewards.add(new ItemStack(SystemItems.PRIMOGEM.get(), 1600));
                    pendingRewards.add(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 10));
                    pendingRewards.add(new ItemStack(SystemItems.END_VOID_KEY.get(), 1));
                }
                case GOLD -> {
                    pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_PLATINUM.get(), 3));
                    pendingRewards.add(new ItemStack(SystemItems.PRIMOGEM.get(), 800));
                    pendingRewards.add(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 3));
                    pendingRewards.add(new ItemStack(Items.NETHERITE_INGOT, 3));
                }
                case SILVER -> {
                    pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_GOLD.get(), 3));
                    pendingRewards.add(new ItemStack(SystemItems.PRIMOGEM.get(), 3200));
                    pendingRewards.add(new ItemStack(Items.NETHERITE_INGOT, 1));
                    pendingRewards.add(new ItemStack(Items.DIAMOND, 4));
                }
                case BRONZE -> {
                    pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_SILVER.get(), 3));
                    pendingRewards.add(new ItemStack(SystemItems.PRIMOGEM.get(), 160));
                    pendingRewards.add(new ItemStack(Items.GOLD_INGOT, 5));
                }
            }
            return;
        }

        switch (sym) {
            case PAIMON -> {
                pendingRewards.add(new ItemStack(SystemItems.INTERTWINED_FATE.get(), tier.ordinal() + 1));
                pendingRewards.add(new ItemStack(SystemItems.MORA.get(), 5 * (tier.ordinal() + 1)));
            }
            case LIGHT_CONE -> {
                pendingRewards.add(new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get(), 1));
            }
            case INTERTWINED_FATE -> {
                pendingRewards.add(new ItemStack(SystemItems.INTERTWINED_FATE.get(), Math.max(1, tier.ordinal() * 2)));
            }
            case PRIMOGEM -> {
                pendingRewards.add(new ItemStack(SystemItems.PRIMOGEM.get(), 80 * (tier.ordinal() + 1)));
            }
            case STARGLITTER -> {
                pendingRewards.add(new ItemStack(SystemItems.MASTERLESS_STARGLITTER.get(), 5 * (tier.ordinal() + 1)));
                pendingRewards.add(new ItemStack(Items.DIAMOND, tier.ordinal() + 1));
            }
            case STARDUST -> {
                pendingRewards.add(new ItemStack(SystemItems.MASTERLESS_STARDUST.get(), 15 * (tier.ordinal() + 1)));
            }
            case MORA -> {
                pendingRewards.add(new ItemStack(SystemItems.MORA_POUCH.get(), tier.ordinal() + 1));
            }
            case SLIME -> {
                pendingRewards.add(new ItemStack(Items.SLIME_BALL, 8 * (tier.ordinal() + 1)));
            }
            default -> {}
        }
    }

    private void evaluateDoubleMatch(CasinoChipItem.ChipTier tier, SlotSymbol sym) {
        winningSpin = true;
        winMora = tier.getMoraValue();

        switch (tier) {
            case BRONZE -> pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_BRONZE.get(), 1));
            case SILVER -> pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_SILVER.get(), 1));
            case GOLD -> pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_GOLD.get(), 1));
            case PLATINUM -> pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_PLATINUM.get(), 1));
            case BLACK -> pendingRewards.add(new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 1));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SlotMachineBlockEntity be) {
        be.prevLeverAngle = be.leverAngle;
        for (int i = 0; i < 3; i++) {
            be.prevReelAngles[i] = be.reelAngles[i];
        }

        if (be.spinTicks > 0) {
            be.spinTicks--;
            int elapsed = 80 - be.spinTicks;

            if (elapsed <= 12) {
                float progress = elapsed / 12.0F;
                be.leverAngle = progress * 55.0F;
            } else if (elapsed <= 28) {
                float returnProg = (elapsed - 12) / 16.0F;
                float bounce = (float) Math.sin(returnProg * Math.PI) * 8.0F;
                be.leverAngle = (1.0F - returnProg) * 55.0F + bounce;
            } else {
                be.leverAngle = 0.0F;
            }

            int[] stopElapsed = new int[] { 35, 52, 68 };
            for (int i = 0; i < 3; i++) {
                if (elapsed < stopElapsed[i]) {
                    float p = (float) elapsed / (float) stopElapsed[i];
                    float ease = 1.0F - (1.0F - p) * (1.0F - p) * (1.0F - p);
                    be.reelAngles[i] = be.startReelAngles[i] + (be.finalReelAngles[i] - be.startReelAngles[i]) * ease;
                } else {
                    be.reelAngles[i] = be.finalReelAngles[i];
                    if (elapsed == stopElapsed[i]) {
                        float pitch = switch (i) {
                            case 0 -> 1.35F;
                            case 1 -> 1.10F;
                            default -> 0.85F;
                        };
                        be.playClickSound(pos, pitch);
                    }
                }
            }

            if (be.spinTicks == 0 && level instanceof ServerLevel sl) {
                be.deliverPayout(sl, pos, state);
            }
        } else {
            be.leverAngle = 0.0F;
            for (int i = 0; i < 3; i++) {
                be.reelAngles[i] = be.finalReelAngles[i];
            }
        }
    }

    private void playClickSound(BlockPos pos, float pitch) {
        if (level != null) {
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 1.0F, pitch);
        }
    }

    private void deliverPayout(ServerLevel sl, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(SlotMachineBlock.FACING);
        double spawnX = pos.getX() + 0.5D + facing.getStepX() * 0.44D;
        double spawnY = pos.getY() + 0.28D;
        double spawnZ = pos.getZ() + 0.5D + facing.getStepZ() * 0.44D;

        if (winningSpin) {
            long displayMora = winMora > 0 ? winMora : lastDisplayWinMora;
            if (isJackpot) {
                sl.playSound(null, pos, ModSounds.CASINO_JACKPOT.get(), SoundSource.BLOCKS, 1.5F, 1.0F);
                sl.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5D, pos.getY() + 1.8D, pos.getZ() + 0.5D, 70, 0.6D, 0.8D, 0.6D, 0.12D);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 60, 0.4D, 0.4D, 0.4D, 0.2D);

                if (currentTier == CasinoChipItem.ChipTier.BLACK) {
                    sl.getServer().getPlayerList().broadcastSystemMessage(
                            Component.translatable("awakening_system.slot.jackpot_broadcast_black",
                                    spinningPlayerName, NF.format(displayMora)), false);
                } else {
                    sl.getServer().getPlayerList().broadcastSystemMessage(
                            Component.translatable("awakening_system.slot.jackpot_broadcast_official",
                                    spinningPlayerName, currentTier.name(), NF.format(displayMora)), false);
                }
            } else {
                sl.playSound(null, pos, ModSounds.CASINO_WIN.get(), SoundSource.BLOCKS, 1.2F, 1.0F);
                sl.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D, 35, 0.3D, 0.2D, 0.3D, 0.12D);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 20, 0.2D, 0.2D, 0.2D, 0.08D);
            }

            if (spinningPlayer != null) {
                Player p = sl.getPlayerByUUID(spinningPlayer);
                if (p instanceof ServerPlayer sp) {
                    SystemData data = SystemEvents.data(sp);
                    data.addCasinoWinnings(displayMora);
                    if (winMora > 0) {
                        int chipVal = currentTier.getMoraValue();
                        int chipsToGive = Math.max(1, (int) (winMora / chipVal));
                        ItemStack chipReward = new ItemStack(switch (currentTier) {
                            case BRONZE -> SystemItems.CASINO_CHIP_BRONZE.get();
                            case SILVER -> SystemItems.CASINO_CHIP_SILVER.get();
                            case GOLD -> SystemItems.CASINO_CHIP_GOLD.get();
                            case PLATINUM -> SystemItems.CASINO_CHIP_PLATINUM.get();
                            case BLACK -> SystemItems.CASINO_CHIP_BLACK.get();
                        }, chipsToGive);
                        if (!sp.getInventory().add(chipReward)) {
                            pendingRewards.add(chipReward);
                        }

                    }
                    if (isJackpot) {
                        ItemStack trophy = com.soybe.awakeningsystem.item.CasinoTrophyItem.createTrophy(
                                3, spinningPlayerName,
                                currentTier == CasinoChipItem.ChipTier.BLACK ? "Underworld Clandestine Jackpot (777)" : "Grand Casino 777 Jackpot"
                        );
                        if (!sp.getInventory().add(trophy)) sp.drop(trophy, false);
                        sp.sendSystemMessage(Component.translatable("awakening_system.slot.trophy_received"));
                    }
                    int chipsDisplay = Math.max(1, (int) (displayMora / currentTier.getMoraValue()));
                    if (currentTier == CasinoChipItem.ChipTier.BLACK) {
                        sp.sendSystemMessage(Component.translatable("awakening_system.slot.win_clandestine", chipsDisplay), true);
                    } else {
                        sp.sendSystemMessage(Component.translatable("awakening_system.slot.win_official", chipsDisplay, currentTier.name()), true);
                    }
                }
            }

            for (ItemStack reward : pendingRewards) {
                if (!reward.isEmpty()) {
                    ItemEntity drop = new ItemEntity(sl, spawnX, spawnY, spawnZ, reward.copy());
                    drop.setDeltaMovement(
                            facing.getStepX() * 0.16D + (sl.random.nextDouble() - 0.5D) * 0.04D,
                            0.12D,
                            facing.getStepZ() * 0.16D + (sl.random.nextDouble() - 0.5D) * 0.04D
                    );
                    sl.addFreshEntity(drop);
                }
            }
        } else {
            sl.playSound(null, pos, ModSounds.CASINO_LOSE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            sl.sendParticles(ParticleTypes.SMOKE, spawnX, spawnY, spawnZ, 12, 0.1D, 0.05D, 0.1D, 0.02D);

            if (getBlockState().getBlock() instanceof SlotMachineBlock smb && smb.isIllegal()) {
                onCustomerLostBet(1);
            }

            if (spinningPlayer != null) {
                Player p = sl.getPlayerByUUID(spinningPlayer);
                if (p instanceof ServerPlayer sp) {
                    if (getBlockState().getBlock() instanceof SlotMachineBlock smb && smb.isIllegal() && isOwner(sp)) {
                        sp.sendSystemMessage(Component.translatable("awakening_system.slot.owner_lost_hint"), true);
                    } else {
                        sp.sendSystemMessage(Component.translatable("awakening_system.slot.lose_hint"), true);
                    }
                }
            }
        }

        setChanged();
        sl.sendBlockUpdated(pos, state, state, 3);
    }

    public void onBlockBroken(ServerLevel sl, BlockPos pos) {
        if (reserveChips > 0) {
            ItemStack stack = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), reserveChips);
            ItemEntity drop = new ItemEntity(sl, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
            sl.addFreshEntity(drop);
            reserveChips = 0;
        }
        if (collectedVaultChips > 0) {
            ItemStack stack = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), collectedVaultChips);
            ItemEntity drop = new ItemEntity(sl, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
            sl.addFreshEntity(drop);
            collectedVaultChips = 0;
        }

        if (isSpinning() || !pendingRewards.isEmpty()) {
            if (winningSpin) {
                for (ItemStack reward : pendingRewards) {
                    if (!reward.isEmpty()) {
                        ItemEntity drop = new ItemEntity(sl, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, reward.copy());
                        sl.addFreshEntity(drop);
                    }
                }
                long displayMora = winMora > 0 ? winMora : lastDisplayWinMora;
                if (spinningPlayer != null && displayMora > 0) {
                    Player p = sl.getPlayerByUUID(spinningPlayer);
                    if (p instanceof ServerPlayer sp) {
                        SystemData data = SystemEvents.data(sp);
                        data.addCasinoWinnings(displayMora);
                        if (winMora > 0) {
                            int chipVal = currentTier.getMoraValue();
                            int chipsToGive = Math.max(1, (int) (winMora / chipVal));
                            ItemStack chipReward = new ItemStack(switch (currentTier) {
                                case BRONZE -> SystemItems.CASINO_CHIP_BRONZE.get();
                                case SILVER -> SystemItems.CASINO_CHIP_SILVER.get();
                                case GOLD -> SystemItems.CASINO_CHIP_GOLD.get();
                                case PLATINUM -> SystemItems.CASINO_CHIP_PLATINUM.get();
                                case BLACK -> SystemItems.CASINO_CHIP_BLACK.get();
                            }, chipsToGive);
                            if (!sp.getInventory().add(chipReward)) {
                                ItemEntity drop = new ItemEntity(sl, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, chipReward);
                                sl.addFreshEntity(drop);
                            }
                        }
                    }
                }
            } else if (spinningPlayer != null) {
                ItemStack refund = new ItemStack(switch (currentTier) {
                    case BRONZE -> SystemItems.CASINO_CHIP_BRONZE.get();
                    case SILVER -> SystemItems.CASINO_CHIP_SILVER.get();
                    case GOLD -> SystemItems.CASINO_CHIP_GOLD.get();
                    case PLATINUM -> SystemItems.CASINO_CHIP_PLATINUM.get();
                    case BLACK -> SystemItems.CASINO_CHIP_BLACK.get();
                });
                ItemEntity drop = new ItemEntity(sl, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, refund);
                sl.addFreshEntity(drop);
            }
            spinTicks = 0;
            pendingRewards.clear();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("spinTicks", spinTicks);
        tag.putInt("tier", currentTier.ordinal());
        tag.putInt("sym0", symbol0.ordinal());
        tag.putInt("sym1", symbol1.ordinal());
        tag.putInt("sym2", symbol2.ordinal());
        tag.putBoolean("winning", winningSpin);
        tag.putBoolean("jackpot", isJackpot);
        tag.putLong("winMora", winMora);
        tag.putLong("lastDisplayWinMora", lastDisplayWinMora);
        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
        }
        tag.putString("ownerName", ownerName != null ? ownerName : "");
        tag.putInt("reserveChips", reserveChips);
        tag.putInt("collectedVaultChips", collectedVaultChips);
        tag.putInt("totalCustomerSpins", totalCustomerSpins);
        tag.putInt("totalWonChips", totalWonChips);

        for (int i = 0; i < 3; i++) {
            tag.putFloat("startAngle" + i, startReelAngles[i]);
            tag.putFloat("finalAngle" + i, finalReelAngles[i]);
            tag.putFloat("reelAngle" + i, reelAngles[i]);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.spinTicks = tag.getInt("spinTicks");
        if (tag.contains("tier")) {
            int t = tag.getInt("tier");
            if (t >= 0 && t < CasinoChipItem.ChipTier.values().length) {
                this.currentTier = CasinoChipItem.ChipTier.values()[t];
            }
        }
        if (tag.contains("sym0")) {
            this.symbol0 = SlotSymbol.values()[tag.getInt("sym0") % SlotSymbol.values().length];
            this.symbol1 = SlotSymbol.values()[tag.getInt("sym1") % SlotSymbol.values().length];
            this.symbol2 = SlotSymbol.values()[tag.getInt("sym2") % SlotSymbol.values().length];
        }
        this.winningSpin = tag.getBoolean("winning");
        this.isJackpot = tag.getBoolean("jackpot");
        this.winMora = tag.getLong("winMora");
        if (tag.contains("lastDisplayWinMora")) {
            this.lastDisplayWinMora = tag.getLong("lastDisplayWinMora");
        }

        if (tag.hasUUID("ownerUUID")) {
            this.ownerUUID = tag.getUUID("ownerUUID");
        }
        if (tag.contains("ownerName")) {
            this.ownerName = tag.getString("ownerName");
        }
        this.reserveChips = tag.getInt("reserveChips");
        this.collectedVaultChips = tag.getInt("collectedVaultChips");
        this.totalCustomerSpins = tag.getInt("totalCustomerSpins");
        this.totalWonChips = tag.getInt("totalWonChips");

        for (int i = 0; i < 3; i++) {
            if (tag.contains("startAngle" + i)) {
                this.startReelAngles[i] = tag.getFloat("startAngle" + i);
                this.finalReelAngles[i] = tag.getFloat("finalAngle" + i);
                this.reelAngles[i] = tag.getFloat("reelAngle" + i);
                this.prevReelAngles[i] = this.reelAngles[i];
            } else {
                float angle = getTargetAngleForSymbol(getSymbol(i));
                this.finalReelAngles[i] = angle;
                this.startReelAngles[i] = angle;
                this.reelAngles[i] = angle;
                this.prevReelAngles[i] = angle;
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("spinTicks", spinTicks);
        tag.putInt("tier", currentTier.ordinal());
        tag.putInt("sym0", symbol0.ordinal());
        tag.putInt("sym1", symbol1.ordinal());
        tag.putInt("sym2", symbol2.ordinal());
        tag.putBoolean("winning", winningSpin);
        tag.putBoolean("jackpot", isJackpot);
        tag.putLong("winMora", winMora);
        tag.putLong("lastDisplayWinMora", lastDisplayWinMora);
        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
        }
        tag.putString("ownerName", ownerName != null ? ownerName : "");
        tag.putInt("reserveChips", reserveChips);
        tag.putInt("collectedVaultChips", collectedVaultChips);
        tag.putInt("totalCustomerSpins", totalCustomerSpins);
        for (int i = 0; i < 3; i++) {
            tag.putFloat("startAngle" + i, startReelAngles[i]);
            tag.putFloat("finalAngle" + i, finalReelAngles[i]);
            tag.putFloat("reelAngle" + i, reelAngles[i]);
        }
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

