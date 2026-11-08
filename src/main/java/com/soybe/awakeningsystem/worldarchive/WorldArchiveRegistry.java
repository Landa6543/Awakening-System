package com.soybe.awakeningsystem.worldarchive;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Los mundos del Archivo: solo los mundos a los que se viaja desde las pestañas del costado de la
 * ventana del Sistema (la bóveda personal no es un mundo y tiene su propia pestaña). Un mundo nuevo
 * se agrega con {@link #register} y la pantalla lo muestra sola.
 */
public final class WorldArchiveRegistry {
    private static final Map<String, WorldArchiveEntry> ENTRIES = new LinkedHashMap<>();

    // Mismas condiciones que las pestañas del Sistema (SystemScreen.isSys20/30/40)
    private static boolean sys40(SystemData d) {
        return d.system40Updated() || d.isSystem40Updating() || d.teyvatUnlocked();
    }

    private static boolean sys30(SystemData d) {
        return sys40(d) || d.system30Updated() || d.isSystem30Updating() || d.murimUnlocked();
    }

    private static boolean sys20(SystemData d) {
        return sys30(d) || d.systemUpdated() || d.isSystemUpdating() || d.isekaiUnlocked();
    }

    private static SystemData data(Player p) {
        return p.getData(AwakeningSystemMod.PLAYER_SYSTEM);
    }

    private static WorldArchiveEntry world(String id, String icon, java.util.function.Predicate<Player> unlock, String lockKey) {
        String k = "awakening_system.world_archive." + id;
        return new WorldArchiveEntry(id,
                Component.translatable(k + ".name"),
                Component.translatable(k + ".subtitle"),
                Component.translatable(k + ".desc"),
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/" + icon + ".png"),
                unlock,
                Component.translatable(k + "." + lockKey),
                List.of(Component.translatable(k + ".struct_1"), Component.translatable(k + ".struct_2"), Component.translatable(k + ".struct_3")),
                List.of(Component.translatable(k + ".boss_1"), Component.translatable(k + ".boss_2"), Component.translatable(k + ".boss_3")),
                List.of(Component.translatable(k + ".mat_1"), Component.translatable(k + ".mat_2"), Component.translatable(k + ".mat_3")));
    }

    static {
        register(world("overworld", "tab_overworld", p -> true, "unlocked"));
        register(world("isekai", "tab_isekai", p -> sys20(data(p)), "lock_hint"));
        register(world("murim", "tab_murim", p -> sys30(data(p)), "lock_hint"));
        register(world("teyvat", "tab_teyvat", p -> sys40(data(p)), "lock_hint"));
    }

    public static synchronized void register(WorldArchiveEntry entry) {
        if (entry != null && entry.id() != null) {
            ENTRIES.put(entry.id(), entry);
        }
    }

    public static List<WorldArchiveEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(ENTRIES.values()));
    }

    public static WorldArchiveEntry get(String id) {
        return ENTRIES.get(id);
    }
}
