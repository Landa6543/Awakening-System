package com.soybe.awakeningsystem.item.pouch;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import com.soybe.awakeningsystem.AwakeningSystemMod;

public enum PouchTier {
    WANDERER("wanderer_pouch", 18, Rarity.UNCOMMON, 176, 156,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/pouch/wanderer_pouch_gui.png"),
            "item.awakening_system.wanderer_pouch"),
    ADVENTURER("adventurer_pouch", 27, Rarity.RARE, 176, 166,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/pouch/adventurer_pouch_gui.png"),
            "item.awakening_system.adventurer_pouch"),
    OUTLANDER("outlander_pouch", 66, Rarity.EPIC, 216, 217,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/pouch/ruler_pouch_gui.png"),
            "item.awakening_system.outlander_pouch"),
    EMERGENCY("emergency_pouch", 143, Rarity.EPIC, 324, 238,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/pouch/outlander_pouch_gui.png"),
            "item.awakening_system.emergency_pouch");

    private final String id;
    private final int slotCount;
    private final Rarity rarity;
    private final int guiWidth;
    private final int guiHeight;
    private final ResourceLocation texture;
    private final String translationKey;

    PouchTier(String id, int slotCount, Rarity rarity, int guiWidth, int guiHeight, ResourceLocation texture, String translationKey) {
        this.id = id;
        this.slotCount = slotCount;
        this.rarity = rarity;
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
        this.texture = texture;
        this.translationKey = translationKey;
    }

    public String getId() { return id; }
    public int getSlotCount() { return slotCount; }
    public Rarity getRarity() { return rarity; }
    public int getGuiWidth() { return guiWidth; }
    public int getGuiHeight() { return guiHeight; }
    public ResourceLocation getTexture() { return texture; }
    public String getTranslationKey() { return translationKey; }
}
