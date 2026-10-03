package com.soybe.awakeningsystem.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public enum ElementType {
    PYRO("pyro", "Pyro", "§cPyro", 0xFFFF4422, ChatFormatting.RED, ParticleTypes.FLAME),
    HYDRO("hydro", "Hydro", "§bHydro", 0xFF2288FF, ChatFormatting.BLUE, ParticleTypes.SPLASH),
    ELECTRO("electro", "Electro", "§5Electro", 0xFFA044FF, ChatFormatting.LIGHT_PURPLE, ParticleTypes.ELECTRIC_SPARK),
    CRYO("cryo", "Cryo", "§3Cryo", 0xFF66DDFF, ChatFormatting.AQUA, ParticleTypes.SNOWFLAKE),
    ANEMO("anemo", "Anemo", "§aAnemo", 0xFF44FFAA, ChatFormatting.GREEN, ParticleTypes.CLOUD),
    GEO("geo", "Geo", "§6Geo", 0xFFFFBB33, ChatFormatting.GOLD, ParticleTypes.CRIT),
    DENDRO("dendro", "Dendro", "§2Dendro", 0xFF44CC33, ChatFormatting.DARK_GREEN, ParticleTypes.HAPPY_VILLAGER);

    private final String id;
    private final String name;
    private final String formattedName;
    private final int color;
    private final ChatFormatting format;
    private final ParticleOptions particle;

    ElementType(String id, String name, String formattedName, int color, ChatFormatting format, ParticleOptions particle) {
        this.id = id;
        this.name = name;
        this.formattedName = formattedName;
        this.color = color;
        this.format = format;
        this.particle = particle;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFormattedName() { return formattedName; }
    public int getColor() { return color; }
    public ChatFormatting getFormat() { return format; }
    public ParticleOptions getParticle() { return particle; }

    public static ElementType fromString(String str) {
        if (str == null) return null;
        for (ElementType e : values()) {
            if (e.id.equalsIgnoreCase(str) || e.name.equalsIgnoreCase(str)) {
                return e;
            }
        }
        return null;
    }
}
