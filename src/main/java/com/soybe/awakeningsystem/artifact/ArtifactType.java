package com.soybe.awakeningsystem.artifact;

public enum ArtifactType {
    FLOWER(0, "Flor de la Vida", "flower", "Vida Máxima", "+150 a +500 HP"),
    PLUME(1, "Pluma de la Muerte", "plume", "Ataque Base", "+10 a +35 ATQ"),
    SANDS(2, "Arenas del Eón", "sands", "Reloj de Eón", "+15% ATQ / Maná"),
    GOBLET(3, "Cáliz de Eonothem", "goblet", "Cáliz Elemental", "+20% Daño Elemental"),
    CIRCLET(4, "Tiara de Logos", "circlet", "Corona del Destino", "+12% Prob. Crítica / +30% Daño Crítico");

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
