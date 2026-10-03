package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.UUID;

public class RouletteAltarBlockEntity extends BlockEntity {
    public static final int SPIN_DURATION_TICKS = 100; // 5 segundos de giro físico

    private final NonNullList<ItemStack> slotItems = NonNullList.withSize(8, ItemStack.EMPTY);

    private boolean isSpinning = false;
    private int spinTicks = 0;
    private float wheelAngle = 0.0F;
    private float prevWheelAngle = 0.0F;
    private float startAngle = 0.0F;
    private float totalSpinAngle = 0.0F;
    private int targetSlot = 0; // 0..7
    private int coinsWon = 150;
    private UUID spinningPlayerId = null;
    private int winDisplayTicks = 0;

    public RouletteAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROULETTE_ALTAR.get(), pos, state);
    }

    private static ItemStack itemOr(DeferredHolder<Item, ? extends Item> holder, int count, Item fallback) {
        if (holder != null && holder.isBound()) {
            return new ItemStack(holder.get(), count);
        }
        return new ItemStack(fallback, count);
    }

    public void ensureSlotsPopulated() {
        if (!slotItems.get(0).isEmpty()) return;

        RandomSource rand = level != null ? level.random : RandomSource.create();

        // Slot 0: Gran Premio Mítico (Armas Legendarias y Míticas de las Series)
        ItemStack[] pool0 = new ItemStack[]{
                itemOr(SystemItems.PLUM_BLOSSOM_SWORD, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.SKY_DEMON_SWORD, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.ASRAHAN_HOLY_SWORD, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.BLACK_DRAGON_SCYTHE, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.HEAVEN_SHAKING_SWORD, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.DAWNS_BALLAD, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.RADIANT_LIGHT_SWORD, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.GIGANTIC_GREATSWORD, 1, Items.NETHERITE_AXE)
        };
        slotItems.set(0, pool0[rand.nextInt(pool0.length)].copy());

        // Slot 1: Reliquias Celestiales / Dagas de Rango Nacional
        ItemStack[] pool1 = new ItemStack[]{
                itemOr(SystemItems.CELESTIAL_DRAGON_PEARL, 1, Items.ENCHANTED_GOLDEN_APPLE),
                itemOr(SystemItems.DEATH_KING_CLOCK, 1, Items.CLOCK),
                itemOr(SystemItems.SHADOW_DAGGERS, 1, Items.NETHERITE_SWORD),
                itemOr(SystemItems.TRANSCENDENT_THORN, 1, Items.TRIDENT),
                itemOr(SystemItems.RADIANT_LIGHT_SWORD, 1, Items.NETHERITE_SWORD),
                new ItemStack(Items.NETHER_STAR, 1)
        };
        slotItems.set(1, pool1[rand.nextInt(pool1.length)].copy());

        // Slot 2: Escudos y Tótems de Inmortalidad
        ItemStack[] pool2 = new ItemStack[]{
                itemOr(SystemItems.UNBREAKABLE_FAITH, 1, Items.SHIELD),
                new ItemStack(Items.TOTEM_OF_UNDYING, 1),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2),
                itemOr(SystemItems.BOTTLED_MANA_PIXIE, 1, Items.EXPERIENCE_BOTTLE)
        };
        slotItems.set(2, pool2[rand.nextInt(pool2.length)].copy());

        // Slot 3: Escamas Ancestrales y Talismanes
        ItemStack[] pool3 = new ItemStack[]{
                itemOr(SystemItems.IMMORTAL_DRAGON_SCALE, 2, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                itemOr(SystemItems.ASTRAL_CRYSTAL_SCALE, 2, Items.AMETHYST_SHARD),
                itemOr(SystemItems.MURIM_ASCENSION_TALISMAN, 1, Items.PAPER),
                new ItemStack(Items.NETHERITE_BLOCK, 1)
        };
        slotItems.set(3, pool3[rand.nextInt(pool3.length)].copy());

        // Slot 4: Elixires y Hierbas Sagradas de Mil Años
        ItemStack[] pool4 = new ItemStack[]{
                itemOr(SystemItems.THOUSAND_YEAR_GINSENG, 2, Items.GOLDEN_APPLE),
                itemOr(SystemItems.SNOW_LOTUS, 2, Items.GOLDEN_APPLE),
                itemOr(SystemItems.WILD_GINSENG, 3, Items.GOLDEN_CARROT),
                itemOr(SystemItems.MANDRAKE_ROOT, 3, Items.BEETROOT),
                new ItemStack(Items.GOLDEN_APPLE, 4)
        };
        slotItems.set(4, pool4[rand.nextInt(pool4.length)].copy());

        // Slot 5: Minerales y Bloques de Tesoro
        ItemStack[] pool5 = new ItemStack[]{
                new ItemStack(Items.NETHERITE_INGOT, 2 + rand.nextInt(2)),
                new ItemStack(Items.DIAMOND_BLOCK, 2 + rand.nextInt(2)),
                new ItemStack(Items.GOLD_BLOCK, 4 + rand.nextInt(5)),
                new ItemStack(Items.EMERALD_BLOCK, 4 + rand.nextInt(5)),
                new ItemStack(Items.ANCIENT_DEBRIS, 3 + rand.nextInt(3))
        };
        slotItems.set(5, pool5[rand.nextInt(pool5.length)].copy());

        // Slot 6: Fichas de Ruleta y Recursos Especiales
        ItemStack[] pool6 = new ItemStack[]{
                itemOr(SystemItems.ROULETTE_TOKEN, 2 + rand.nextInt(2), Items.NETHER_STAR),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 16 + rand.nextInt(17)),
                new ItemStack(Items.ENDER_PEARL, 8 + rand.nextInt(9)),
                new ItemStack(Items.GOLDEN_APPLE, 3 + rand.nextInt(4))
        };
        slotItems.set(6, pool6[rand.nextInt(pool6.length)].copy());

        // Slot 7: Bolsa de Monedas del Sistema (Límite seguro de Minecraft: máximo 64)
        int coins = 32 + rand.nextInt(33); // 32 a 64 monedas físicas
        slotItems.set(7, itemOr(SystemItems.AWAKENING_COIN, coins, Items.DIAMOND));

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack getSlotItem(int slot) {
        int s = Math.floorMod(slot, 8);
        if (!slotItems.get(s).isEmpty()) {
            return slotItems.get(s);
        }
        return getDefaultFallbackItem(s);
    }

    public static ItemStack getDefaultFallbackItem(int slot) {
        return switch (Math.floorMod(slot, 8)) {
            case 0 -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD);
            case 1 -> SystemItems.CELESTIAL_DRAGON_PEARL.isBound() ? new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get()) : new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2);
            case 2 -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : new ItemStack(Items.TOTEM_OF_UNDYING);
            case 3 -> SystemItems.SKY_DEMON_SWORD.isBound() ? new ItemStack(SystemItems.SKY_DEMON_SWORD.get()) : new ItemStack(Items.NETHERITE_INGOT, 2);
            case 4 -> SystemItems.IMMORTAL_DRAGON_SCALE.isBound() ? new ItemStack(SystemItems.IMMORTAL_DRAGON_SCALE.get(), 2) : new ItemStack(Items.NETHERITE_BLOCK);
            case 5 -> SystemItems.THOUSAND_YEAR_GINSENG.isBound() ? new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get(), 2) : new ItemStack(Items.DIAMOND_BLOCK, 2);
            case 6 -> SystemItems.SNOW_LOTUS.isBound() ? new ItemStack(SystemItems.SNOW_LOTUS.get(), 2) : new ItemStack(Items.GOLDEN_APPLE, 4);
            default -> SystemItems.AWAKENING_COIN.isBound() ? new ItemStack(SystemItems.AWAKENING_COIN.get(), 64) : new ItemStack(Items.DIAMOND, 8);
        };
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public float getWheelAngle() {
        return wheelAngle;
    }

    public float getPrevWheelAngle() {
        return prevWheelAngle;
    }

    public int getTargetSlot() {
        return targetSlot;
    }

    public int getWinDisplayTicks() {
        return winDisplayTicks;
    }

    public ItemStack getWinningItem() {
        return getSlotItem(targetSlot);
    }

    public boolean startSpin(ServerPlayer player) {
        if (isSpinning || level == null || level.isClientSide) return false;

        ensureSlotsPopulated();
        this.spinningPlayerId = player.getUUID();
        this.isSpinning = true;
        this.spinTicks = 0;
        this.winDisplayTicks = 0;
        this.startAngle = this.wheelAngle % 360.0F;

        // Selección ponderada del slot ganador (0 a 7)
        int roll = player.getRandom().nextInt(100);
        if (roll < 6) this.targetSlot = 0;       // 6% Espada del Ciruelo
        else if (roll < 14) this.targetSlot = 1; // 8% Perla del Dragón Celestial
        else if (roll < 24) this.targetSlot = 2; // 10% Dagas de las Sombras
        else if (roll < 34) this.targetSlot = 3; // 10% Espada Demonio Celestial
        else if (roll < 50) this.targetSlot = 4; // 16% Escama de Dragón Inmortal x2
        else if (roll < 66) this.targetSlot = 5; // 16% Ginseng Milenario x2
        else if (roll < 82) this.targetSlot = 6; // 16% Loto de Nieve x2
        else this.targetSlot = 7;                // 18% Monedas del Sistema x64

        this.coinsWon = 75 + player.getRandom().nextInt(151);

        // Alineación geométrica exacta con la aguja indicadora fija en 0° (Norte)
        float targetAngleNormalized = (360.0F - (this.targetSlot * 45.0F)) % 360.0F;
        float turns = 5.0F * 360.0F; // 5 giros completos
        float currentNorm = this.startAngle % 360.0F;
        float diff = targetAngleNormalized - currentNorm;
        if (diff <= 0) diff += 360.0F;
        this.totalSpinAngle = turns + diff;

        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.5F);
        level.playSound(null, worldPosition, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8F, 1.8F);

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RouletteAltarBlockEntity be) {
        if (!level.isClientSide && be.slotItems.get(0).isEmpty()) {
            be.ensureSlotsPopulated();
        }
        be.prevWheelAngle = be.wheelAngle;

        if (be.isSpinning) {
            be.spinTicks++;

            // Curva cúbica de desaceleración física
            float p = Math.min(1.0F, (float) be.spinTicks / (float) SPIN_DURATION_TICKS);
            float easeOut = 1.0F - (float) Math.pow(1.0F - p, 3.0);
            be.wheelAngle = be.startAngle + be.totalSpinAngle * easeOut;

            // Sonido adictivo de trinquete de ruleta de casino mecánica: clic seco + campana de ranura
            float angleDelta = Math.abs(be.wheelAngle - be.prevWheelAngle);
            if (angleDelta > 0.05F) {
                int prevSector = (int) (be.prevWheelAngle / 45.0F);
                int currSector = (int) (be.wheelAngle / 45.0F);
                if (prevSector != currSector) {
                    float clickPitch = 1.5F + (p * 0.5F);
                    float bellPitch = 1.2F + ((Math.abs(currSector) % 8) * 0.1F);
                    level.playSound(null, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D,
                            SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.9F, clickPitch);
                    level.playSound(null, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D,
                            SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 0.5F, bellPitch);
                    level.playSound(null, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D,
                            SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.35F, 1.4F + (p * 0.4F));
                }
            }

            // Partículas alrededor de la pista circular de la gran ruleta vertical (radio ~0.85D)
            if (level.isClientSide && be.spinTicks % 2 == 0) {
                double rad = Math.toRadians(be.wheelAngle * 2.5D);
                double px = pos.getX() + 0.5D + Math.cos(rad) * 0.85D;
                double py = pos.getY() + 2.25D + Math.sin(rad) * 0.85D;
                double pz = pos.getZ() + 0.5D;
                level.addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0.01, 0);
            }

            // Finalización del giro (Tick 100)
            if (be.spinTicks >= SPIN_DURATION_TICKS) {
                be.isSpinning = false;
                be.winDisplayTicks = 45;

                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 2.25D, pos.getZ() + 0.5D, 80, 0.45D, 0.5D, 0.45D, 0.22D);
                    serverLevel.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5D, pos.getY() + 2.25D, pos.getZ() + 0.5D, 45, 0.35D, 0.4D, 0.35D, 0.15D);
                    serverLevel.sendParticles(ParticleTypes.FLASH, pos.getX() + 0.5D, pos.getY() + 2.25D, pos.getZ() + 0.5D, 1, 0, 0, 0, 0);

                    level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 1.0F, 2.0F);
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 0.85F, 2.0F);
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.65F, 2.0F);
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.2F);
                    level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 0.8F, 1.1F);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.3F);

                    if (be.spinningPlayerId != null) {
                        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(be.spinningPlayerId);
                        if (player != null && player.isAlive()) {
                            SystemData data = SystemEvents.data(player);
                            data.addCoins(be.coinsWon);
                            data.recordStatistic("missions_completed", 1);
                            SystemEvents.checkAchievements(player, data);
                            AwakeningSystemMod.sync(player);

                            ItemStack reward = be.getWinningItem().copy();
                            if (!player.getInventory().add(reward)) {
                                player.drop(reward, false);
                            }

                            AwakeningSystemMod.notify(player,
                                    "awakening_system.chest.roulette.title",
                                    "awakening_system.chest.roulette.awarded",
                                    0xFFFFD700);

                            String rewardDesc = reward.getHoverName().getString() + " x" + reward.getCount() +
                                    (be.coinsWon > 0 ? " §6(+§e" + be.coinsWon + " ⛃§6)" : "");
                            player.displayClientMessage(Component.translatable("awakening_system.roulette.reward_chat", rewardDesc), true);
                        }
                    }
                }

                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        } else {
            if (be.winDisplayTicks > 0) {
                // Cascada de campanas de casino y monedas mientras se muestra el premio ganador
                if (be.winDisplayTicks > 15 && be.winDisplayTicks % 2 == 0) {
                    int step = (45 - be.winDisplayTicks) / 2;
                    float bellPitch = Math.min(2.0F, 1.2F + (step * 0.12F));
                    float coinPitch = 1.1F + (step * 0.08F);
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 0.65F, bellPitch);
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.45F, bellPitch);
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.85F, coinPitch);
                    level.playSound(null, pos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.5F, 1.6F);
                }
                be.winDisplayTicks--;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.slotItems, registries);
        tag.putBoolean("IsSpinning", this.isSpinning);
        tag.putInt("SpinTicks", this.spinTicks);
        tag.putFloat("WheelAngle", this.wheelAngle);
        tag.putFloat("StartAngle", this.startAngle);
        tag.putFloat("TotalSpinAngle", this.totalSpinAngle);
        tag.putInt("TargetSlot", this.targetSlot);
        tag.putInt("CoinsWon", this.coinsWon);
        tag.putInt("WinDisplayTicks", this.winDisplayTicks);
        if (this.spinningPlayerId != null) {
            tag.putUUID("SpinningPlayer", this.spinningPlayerId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.slotItems, registries);
        for (int i = 0; i < this.slotItems.size(); i++) {
            ItemStack st = this.slotItems.get(i);
            if (!st.isEmpty() && st.getCount() > 64) {
                st.setCount(64);
            }
        }
        this.isSpinning = tag.getBoolean("IsSpinning");
        this.spinTicks = tag.getInt("SpinTicks");
        this.wheelAngle = tag.getFloat("WheelAngle");
        this.startAngle = tag.getFloat("StartAngle");
        this.totalSpinAngle = tag.getFloat("TotalSpinAngle");
        this.targetSlot = tag.getInt("TargetSlot");
        this.coinsWon = tag.getInt("CoinsWon");
        this.winDisplayTicks = tag.getInt("WinDisplayTicks");
        if (tag.hasUUID("SpinningPlayer")) {
            this.spinningPlayerId = tag.getUUID("SpinningPlayer");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
