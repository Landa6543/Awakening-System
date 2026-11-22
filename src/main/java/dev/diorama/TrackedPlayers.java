package dev.diorama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

/**
 * Players the server reports wherever they are. Through Xaero's player tracker: Xaero's mods on
 * the server (for party members, or everyone when the server allows it) and plugins such as
 * XaeroTracker send their positions every quarter second or so as they move. And from Diorama on
 * the server, which sends the player's team, or whoever its owner picked ({@code FarPlayers}). The
 * radar shows the ones the client hasn't loaded, so friends show on the map from anywhere. Client
 * thread only.
 */
public final class TrackedPlayers {
	private record Tracked(double x, double y, double z, String dimension) {
	}

	/** From Xaero's tracker, one player a message. */
	private static final Map<UUID, Tracked> PLAYERS = new HashMap<>();
	/** From Diorama on the server, everyone in each message. Where both name a player, this one is newer or as new. */
	private static final Map<UUID, Tracked> FROM_DIORAMA = new HashMap<>();
	/**
	 * What the radar draws for each tracked player: a player the level doesn't hold, built from
	 * their tab list entry, so they look like any other player, skin and name included.
	 */
	private static final Map<UUID, RemotePlayer> STAND_INS = new HashMap<>();
	private static @Nullable ClientLevel standInLevel;

	private TrackedPlayers() {
	}

	/**
	 * One player's update: {@code r} true if they're no longer tracked, else their position
	 * {@code x}, {@code y}, {@code z} and dimension {@code d}. {@code i} is their UUID as four ints,
	 * or two longs from older servers.
	 */
	static void update(CompoundTag tag) {
		UUID id = uuid(tag);
		if (id == null) {
			return;
		}
		//? if >=1.21.5 {
		/*boolean removed = tag.getBooleanOr("r", false);
		*///?} else {
		// Before 1.21.5 a missing value read as false, 0 or "".
		boolean removed = tag.getBoolean("r");
		//?}
		if (removed) {
			PLAYERS.remove(id);
			if (!FROM_DIORAMA.containsKey(id)) {
				STAND_INS.remove(id);
			}
		} else {
			//? if >=1.21.5 {
			/*PLAYERS.put(id, new Tracked(tag.getDoubleOr("x", 0), tag.getDoubleOr("y", 0), tag.getDoubleOr("z", 0), tag.getStringOr("d", "")));
			*///?} else {
			PLAYERS.put(id, new Tracked(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getString("d")));
			//?}
		}
	}

	private static @Nullable UUID uuid(CompoundTag tag) {
		//? if >=1.21.5 {
		/*int[] ints = tag.getIntArray("i").orElse(new int[0]);
		long[] longs = tag.getLongArray("i").orElse(new long[0]);
		*///?} else {
		// Before 1.21.5 a missing or other-typed array read as empty.
		int[] ints = tag.getIntArray("i");
		long[] longs = tag.getLongArray("i");
		//?}
		if (ints.length == 4) {
			return UUIDUtil.uuidFromIntArray(ints);
		}
		if (longs.length == 2) {
			return new UUID(longs[0], longs[1]);
		}
		return null;
	}

	/**
	 * Everyone Diorama on the server lets this player see, replacing who it named before: a
	 * {@code ShareProtocol.FAR_PLAYERS} message. One cut short changes nothing.
	 */
	public static void fromDiorama(byte[] message) {
		Map<UUID, Tracked> named = new HashMap<>();
		try (java.io.DataInputStream in = new java.io.DataInputStream(new java.io.ByteArrayInputStream(message))) {
			in.readUnsignedByte();
			in.readUTF();
			int count = in.readUnsignedShort();
			for (int i = 0; i < count; i++) {
				UUID id = new UUID(in.readLong(), in.readLong());
				String dimension = in.readUTF();
				// The middle of the block, a little into it as a standing player's feet are.
				named.put(id, new Tracked(in.readInt() + 0.5, in.readInt(), in.readInt() + 0.5, dimension));
			}
		} catch (java.io.IOException e) {
			Diorama.LOGGER.warn("Couldn't read where the server says players are", e);
			return;
		}
		FROM_DIORAMA.clear();
		FROM_DIORAMA.putAll(named);
		STAND_INS.keySet().removeIf(id -> !FROM_DIORAMA.containsKey(id) && !PLAYERS.containsKey(id));
	}

	/**
	 * Whether the server reports this player wherever they are, loaded by the client or not: a
	 * teammate stays on the map at any height, near or far.
	 */
	public static boolean tracks(net.minecraft.world.entity.Entity entity) {
		return entity instanceof net.minecraft.world.entity.player.Player && (FROM_DIORAMA.containsKey(entity.getUUID()) || PLAYERS.containsKey(entity.getUUID()));
	}

	/** Who Diorama on the server last said this player sees, for tests. */
	public static java.util.Set<UUID> fromDiorama() {
		return java.util.Set.copyOf(FROM_DIORAMA.keySet());
	}

	/** Xaero's tracker is about to send everyone again. */
	static void clearXaero() {
		PLAYERS.clear();
		STAND_INS.keySet().removeIf(id -> !FROM_DIORAMA.containsKey(id));
	}

	/** The player joined or left a server. */
	public static void clear() {
		PLAYERS.clear();
		FROM_DIORAMA.clear();
		STAND_INS.clear();
		standInLevel = null;
	}

	/**
	 * The tracked players to draw in this level: in its dimension, not loaded by the client
	 * (those are drawn as they are), and on the tab list, which has their name and skin.
	 * Spectators are left off unless the player is one too, as the game hides them.
	 */
	public static List<RemotePlayer> unloaded(ClientLevel level) {
		Minecraft mc = Minecraft.getInstance();
		ClientPacketListener connection = mc.getConnection();
		if (PLAYERS.isEmpty() && FROM_DIORAMA.isEmpty() || connection == null || mc.player == null) {
			return List.of();
		}
		if (standInLevel != level) {
			STAND_INS.clear();
			standInLevel = level;
		}
		String dimension = level.dimension().location().toString();
		List<RemotePlayer> out = new ArrayList<>();
		Map<UUID, Tracked> players = PLAYERS;
		if (!FROM_DIORAMA.isEmpty()) {
			players = new HashMap<>(PLAYERS);
			players.putAll(FROM_DIORAMA);
		}
		for (Map.Entry<UUID, Tracked> e : players.entrySet()) {
			UUID id = e.getKey();
			Tracked tracked = e.getValue();
			if (id.equals(mc.player.getUUID()) || !tracked.dimension().equals(dimension) || level.getPlayerByUUID(id) != null) {
				continue;
			}
			PlayerInfo info = connection.getPlayerInfo(id);
			if (info == null || info.getGameMode() == GameType.SPECTATOR && !mc.player.isSpectator()) {
				continue;
			}
			RemotePlayer standIn = STAND_INS.computeIfAbsent(id, uuid -> new RemotePlayer(level, info.getProfile()));
			//? if >=1.21.5 {
			/*standIn.moveTo(tracked.x(), tracked.y(), tracked.z());
			*///?} else {
			// Sets the previous position too, as snapTo does, so the face doesn't slide from the old spot.
			standIn.moveTo(tracked.x(), tracked.y(), tracked.z());
			//?}
			out.add(standIn);
		}
		return out;
	}
}
