package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class AncientDragonEggBlockEntity extends BlockEntity {
    public static final int MAX_INCUBATION_TICKS = 1200;

    private int variant = 0;
    private int incubationTicks = 0;
    private boolean comfortable = false;
    private UUID placerUUID = null;

    public AncientDragonEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANCIENT_DRAGON_EGG.get(), pos, state);
    }

    public int getVariant() {
        return this.variant;
    }

    public void setVariant(int variant) {
        this.variant = variant;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void setPlacer(Player player) {
        if (player != null) {
            this.placerUUID = player.getUUID();
            this.setChanged();
        }
    }

    public boolean isComfortable() {
        return this.comfortable;
    }

    public int getIncubationTicks() {
        return this.incubationTicks;
    }

    public void boostIncubation(int bonusTicks) {
        this.incubationTicks = Math.min(MAX_INCUBATION_TICKS, this.incubationTicks + bonusTicks);
        this.setChanged();
        if (this.level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.8D,
                    this.worldPosition.getZ() + 0.5D,
                    15, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.playSound(null, this.worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AncientDragonEggBlockEntity egg) {
        if (level.isClientSide) {
            if (egg.comfortable && level.random.nextInt(15) == 0) {
                double px = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.6D;
                double py = pos.getY() + 0.5D + level.random.nextDouble() * 0.6D;
                double pz = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.6D;
                switch (egg.variant) {
                    case AncientDragonEntity.VARIANT_FIRE -> level.addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.02, 0);
                    case AncientDragonEntity.VARIANT_ICE -> level.addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0, 0.01, 0);
                    case AncientDragonEntity.VARIANT_FOREST -> level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.02, 0);
                    case AncientDragonEntity.VARIANT_SHADOW -> level.addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.03, 0);
                    case AncientDragonEntity.VARIANT_LIGHTNING -> level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.04, 0);
                    case AncientDragonEntity.VARIANT_CELESTIAL -> level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.02, 0);
                }
            }
            return;
        }

        boolean wasComfortable = egg.comfortable;
        egg.comfortable = checkComfort(level, pos, egg.variant);
        if (wasComfortable != egg.comfortable) {
            egg.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (egg.comfortable) {
            egg.incubationTicks++;
            if (egg.incubationTicks % 100 == 0) {
                egg.setChanged();
            }

            if (egg.incubationTicks > (MAX_INCUBATION_TICKS * 0.75) && level.random.nextInt(60) == 0) {
                level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.8F, 1.2F);
            }

            if (egg.incubationTicks >= MAX_INCUBATION_TICKS) {
                hatchDragon(level, pos, egg);
            }
        }
    }

    private static boolean checkComfort(Level level, BlockPos pos, int variant) {
        switch (variant) {
            case AncientDragonEntity.VARIANT_FIRE -> {
                for (BlockPos check : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 2, 3))) {
                    BlockState bs = level.getBlockState(check);
                    if (bs.is(Blocks.FIRE) || bs.is(Blocks.CAMPFIRE) || bs.is(Blocks.LAVA) || bs.is(Blocks.MAGMA_BLOCK)) {
                        return true;
                    }
                }
                return level.dimension() == Level.NETHER;
            }
            case AncientDragonEntity.VARIANT_ICE -> {
                for (BlockPos check : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 2, 3))) {
                    BlockState bs = level.getBlockState(check);
                    if (bs.is(Blocks.ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.BLUE_ICE) || bs.is(Blocks.POWDER_SNOW)) {
                        return true;
                    }
                }
                return level.getBiome(pos).value().coldEnoughToSnow(pos);
            }
            case AncientDragonEntity.VARIANT_FOREST -> {
                int floraCount = 0;
                for (BlockPos check : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 2, 2))) {
                    BlockState bs = level.getBlockState(check);
                    if (bs.is(net.minecraft.tags.BlockTags.LEAVES) || bs.is(net.minecraft.tags.BlockTags.FLOWERS) || bs.is(Blocks.MOSS_BLOCK)) {
                        floraCount++;
                    }
                }
                return floraCount >= 2;
            }
            case AncientDragonEntity.VARIANT_SHADOW -> {
                if (level.getRawBrightness(pos, 0) <= 4) return true;
                for (BlockPos check : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 2, 2))) {
                    BlockState bs = level.getBlockState(check);
                    if (bs.is(Blocks.CRYING_OBSIDIAN) || bs.is(Blocks.SOUL_FIRE) || bs.is(Blocks.SOUL_CAMPFIRE) || bs.is(Blocks.SCULK)) {
                        return true;
                    }
                }
                return false;
            }
            case AncientDragonEntity.VARIANT_LIGHTNING -> {
                if (level.isThundering()) return true;
                for (BlockPos check : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 2, 3))) {
                    BlockState bs = level.getBlockState(check);
                    if (bs.is(Blocks.LIGHTNING_ROD) || bs.is(Blocks.COPPER_BLOCK) || bs.is(Blocks.CUT_COPPER) || bs.is(Blocks.WAXED_COPPER_BLOCK) || bs.is(Blocks.REDSTONE_BLOCK)) {
                        return true;
                    }
                }
                return false;
            }
            case AncientDragonEntity.VARIANT_CELESTIAL -> {
                if (level.canSeeSky(pos.above()) && level.isDay()) return true;
                for (BlockPos check : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 2, 3))) {
                    BlockState bs = level.getBlockState(check);
                    if (bs.is(Blocks.GOLD_BLOCK) || bs.is(Blocks.SEA_LANTERN) || bs.is(Blocks.BEACON) || bs.is(Blocks.AMETHYST_CLUSTER)) {
                        return true;
                    }
                }
                return false;
            }
            default -> {
                return true;
            }
        }
    }

    private static void hatchDragon(Level level, BlockPos pos, AncientDragonEggBlockEntity egg) {
        if (!(level instanceof ServerLevel sl)) return;

        sl.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.5F, 0.9F);
        sl.playSound(null, pos, ModSounds.ANCIENT_DRAGON_ROAR.get(), SoundSource.NEUTRAL, 1.6F, 1.65F);
        sl.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 1, 0, 0, 0, 0);

        AncientDragonEntity baby = ModEntities.ANCIENT_DRAGON.get().create(level);
        if (baby != null) {
            baby.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            baby.setVariant(egg.variant);
            baby.setDragonAge(0);

            if (egg.placerUUID != null) {
                Player owner = level.getPlayerByUUID(egg.placerUUID);
                if (owner != null) {
                    baby.tame(owner);
                    baby.setOwnerUUID(egg.placerUUID);
                    owner.displayClientMessage(Component.translatable("message.awakening_system.dragon_egg_hatched"), false);
                }
            }

            level.addFreshEntity(baby);
        }

        level.removeBlock(pos, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Variant", this.variant);
        tag.putInt("IncubationTicks", this.incubationTicks);
        tag.putBoolean("Comfortable", this.comfortable);
        if (this.placerUUID != null) {
            tag.putUUID("PlacerUUID", this.placerUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.variant = tag.getInt("Variant");
        this.incubationTicks = tag.getInt("IncubationTicks");
        this.comfortable = tag.getBoolean("Comfortable");
        if (tag.hasUUID("PlacerUUID")) {
            this.placerUUID = tag.getUUID("PlacerUUID");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Variant", this.variant);
        tag.putInt("IncubationTicks", this.incubationTicks);
        tag.putBoolean("Comfortable", this.comfortable);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
