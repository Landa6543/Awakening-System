package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CasinoTrophyBlockEntity extends BlockEntity {

    private int trophyType = 1;
    private String winnerName = "";
    private String achievementDesc = "";
    private String awardedDate = "";

    public CasinoTrophyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CASINO_TROPHY.get(), pos, blockState);
    }

    public int getTrophyType() {
        return trophyType;
    }

    public void setTrophyType(int type) {
        this.trophyType = Math.max(1, Math.min(5, type));
        this.setChanged();
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String name) {
        this.winnerName = name != null ? name : "";
        this.setChanged();
    }

    public String getAchievementDesc() {
        return achievementDesc;
    }

    public void setAchievementDesc(String desc) {
        this.achievementDesc = desc != null ? desc : "";
        this.setChanged();
    }

    public String getAwardedDate() {
        return awardedDate;
    }

    public void setAwardedDate(String date) {
        this.awardedDate = date != null ? date : "";
        this.setChanged();
    }

    public String getTrophyTitle() {
        return switch (this.trophyType) {
            case 1 -> "Teyvat Derby Cup (UMA Champion)";
            case 2 -> "Grand Ace of Spades Trophy (Blackjack High Roller)";
            case 3 -> "Grand Stellar Jackpot Trophy (Fortune Roulette)";
            case 4 -> "Felt Legend Trophy (8-Ball Pool)";
            case 5 -> "Sisal Master Trophy (180 PDC Darts)";
            default -> "Casino Honor Trophy";
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("trophy_type", this.trophyType);
        tag.putString("winner_name", this.winnerName);
        tag.putString("achievement_desc", this.achievementDesc);
        tag.putString("awarded_date", this.awardedDate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("trophy_type")) {
            this.trophyType = tag.getInt("trophy_type");
        }
        if (tag.contains("winner_name")) {
            this.winnerName = tag.getString("winner_name");
        }
        if (tag.contains("achievement_desc")) {
            this.achievementDesc = tag.getString("achievement_desc");
        }
        if (tag.contains("awarded_date")) {
            this.awardedDate = tag.getString("awarded_date");
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}
