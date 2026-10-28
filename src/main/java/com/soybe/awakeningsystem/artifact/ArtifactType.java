package com.soybe.awakeningsystem.artifact;

public enum ArtifactType {
    FLOWER(0, "Flower of Life", "flower", "Max Health", "+150 to +500 HP"),
    PLUME(1, "Plume of Death", "plume", "Base Attack", "+10 to +35 ATK"),
    SANDS(2, "Sands of Eon", "sands", "Aeon Clock", "+15% ATK / Mana"),
    GOBLET(3, "Goblet of Eonothem", "goblet", "Elemental Goblet", "+20% Elemental DMG"),
    CIRCLET(4, "Circlet of Logos", "circlet", "Crown of Destiny", "+12% CRIT Rate / +30% CRIT DMG");

    private final int slotIndex;
    private final String displayName;
    private final String idName;
    private final String statType;
    private final String statDescription;

    ArtifactType(int slotIndex, String displayName, String idName, String statType, String statDescription) {
        this.slotIndex = slotIndex;
        this.displayName = displayName;
        this.idName = idName;
        this.statType = statType;
        this.statDescription = statDescription;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIdName() {
        return idName;
    }

    public String getStatType() {
        return statType;
    }

    public String getStatDescription() {
        return statDescription;
    }

    public static ArtifactType fromSlot(int slot) {
        for (ArtifactType t : values()) {
            if (t.slotIndex == slot) return t;
        }
        return FLOWER;
    }

    public net.minecraft.network.chat.Component getDisplayNameComponent() {
        return net.minecraft.network.chat.Component.translatable("awakening_system.artifact_type." + idName + ".name");
    }

    public net.minecraft.network.chat.Component getStatBonusComponent() {
        return net.minecraft.network.chat.Component.translatable("awakening_system.screen.artifacts.bonus." + idName);
    }
}
