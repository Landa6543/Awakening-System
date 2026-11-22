package dev.diorama;

import net.neoforged.fml.ModList;

import java.util.List;

/**
 * Awakening System carries Diorama as its map. When the player also has another map mod, that one is the
 * map: Diorama turns itself off entirely (no minimap, no big map, no waypoints in the world, no map work in
 * the background), so there are never two maps on screen fighting over the same keys and corner.
 */
public final class OtherMaps {
	private OtherMaps() {}

	/** Mod ids of the other maps and minimaps. */
	private static final List<String> MAP_MODS = List.of(
			"journeymap",
			"xaerominimap", "xaerominimapfair", "xaeroworldmap",
			"ftbchunks",
			"voxelmap",
			"antiqueatlas",
			"map_atlases",
			"simple_minimap",
			"lightmans_mini_map",
			"mapfrontiers"
	);

	private static String found;
	private static boolean checked;

	/** The id of the other map mod installed, or null when there is none. */
	public static synchronized String found() {
		if (!checked) {
			checked = true;
			try {
				for (String id : MAP_MODS) {
					if (ModList.get().isLoaded(id)) {
						found = id;
						Diorama.LOGGER.info("Another map mod is installed ({}): Awakening System's Diorama map turns itself off", id);
						break;
					}
				}
			} catch (Throwable ignored) {
				// the mod list is not ready yet: ask again later
				checked = false;
			}
		}
		return found;
	}

	/** True when another map mod is installed, so Diorama stays off. */
	public static boolean present() {
		return found() != null;
	}
}
