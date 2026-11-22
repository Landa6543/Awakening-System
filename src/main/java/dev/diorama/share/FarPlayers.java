package dev.diorama.share;

import dev.diorama.Diorama;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Where a player's teammates are, sent to their Diorama a few times a second, so the map shows
 * friends who are too far away for the game to send. Xaero's Minimap on a server does the same for
 * its own players, and Diorama's client draws those too (see {@code TrackedPlayers}); this is for
 * servers with Diorama and no Xaero's.
 *
 * <p>Who sees whom is the server owner's choice, {@code far-players} in
 * {@code config/diorama-server.properties}: teammates only (the default, as Xaero's has it),
 * everyone, or no one. Teammates are the players in the same FTB Teams party or the same Open
 * Parties and Claims party. Diorama doesn't build against either mod: they're read by reflection,
 * and a server with neither sends nothing until its owner picks {@code everyone}.
 *
 * <p>Nothing is sent while the server's {@code radar-allowed} is false, and no one is told about a
 * player in a dimension the server keeps off the map. Where players are is never stored: each
 * message says who a player sees right now, and replaces the one before it.
 */
public final class FarPlayers {
	/** Who a player with Diorama sees from any distance. */
	public enum Who {
		OFF, TEAM, EVERYONE;

		String key() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	private static final String KEY = "far-players";
	private static final String ABOUT = """

		# Players with Diorama see these players on their map and minimap from any distance, not only
		# the ones near enough for the game to show:
		#   team     - the players in their FTB Teams party or Open Parties and Claims party (default)
		#   everyone - every player on the server
		#   off      - no one
		# Nothing is shown while radar-allowed is false, or of a player in a dimension the map is off in.
		""";
	/** How often positions go out, as Xaero's sends its tracked players. */
	private static final long EVERY_MS = 250;
	/** Every so many sends, each player is told again though nothing changed, in case their Diorama lost it. */
	private static final int AGAIN = 40;
	private static volatile @Nullable FarPlayers running;

	private final MinecraftServer server;
	private final Who who;
	private final List<Teams> teams;
	/** The players whose Diorama listens, and what each was last sent. Server thread. */
	private final Map<UUID, byte[]> listeners = new HashMap<>();
	private final AtomicBoolean waiting = new AtomicBoolean();
	/** Counts {@link #send}s, for {@link #AGAIN}. */
	private int sends;
	private final Thread clock;
	private volatile boolean stopped;

	private FarPlayers(MinecraftServer server, Who who) {
		this.server = server;
		this.who = who;
		this.teams = who == Who.TEAM ? Teams.installed() : List.of();
		this.clock = new Thread(this::run, "diorama-far-players");
		this.clock.setDaemon(true);
	}

	/** A dedicated server started: reads the setting, adding it to the file if it isn't there. */
	static void started(MinecraftServer server, Path configFolder, ServerLocks locks) {
		Who who = load(configFolder);
		if (who == Who.OFF || !locks.radarAllowed()) {
			return;
		}
		FarPlayers far = new FarPlayers(server, who);
		if (who == Who.TEAM && far.teams.isEmpty()) {
			Diorama.LOGGER.info("Diorama on the server: far-players is team, and neither FTB Teams nor Open Parties and Claims is installed, so players see no one from afar");
			return;
		}
		running = far;
		far.clock.start();
		Diorama.LOGGER.info("Diorama on the server: players with Diorama see {} from any distance", who == Who.EVERYONE ? "everyone" : "their team (" + String.join(", ", far.teams.stream().map(Teams::name).toList()) + ")");
	}

	static void stopped(MinecraftServer server) {
		FarPlayers far = running;
		if (far != null && far.server == server) {
			running = null;
			far.stopped = true;
			far.clock.interrupt();
		}
	}

	/** A player's Diorama listens, as they join. Server thread. */
	static void listening(ServerPlayer player) {
		FarPlayers far = running;
		if (far != null && far.server == player.level().getServer()) {
			// Nothing sent yet, so the first message goes out whatever it says.
			far.listeners.put(player.getUUID(), new byte[0]);
		}
	}

	static Who load(Path configFolder) {
		Path file = configFolder.resolve(ShareSettings.FILE);
		Properties properties = new Properties();
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				properties.load(reader);
			} catch (IOException | IllegalArgumentException e) {
				// The owner may have set it to off in there: without knowing, no one is shown.
				Diorama.LOGGER.warn("Couldn't read {} in {}; players see no one from afar until it can be read", KEY, file, e);
				return Who.OFF;
			}
		}
		if (!properties.containsKey(KEY)) {
			try {
				Files.createDirectories(configFolder);
				Files.writeString(file, (Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8) : "") + ABOUT + KEY + "=" + Who.TEAM.key() + "\n", StandardCharsets.UTF_8);
			} catch (IOException e) {
				Diorama.LOGGER.warn("Couldn't add {} to {}", KEY, file, e);
			}
		}
		String text = properties.getProperty(KEY, Who.TEAM.key()).trim().toLowerCase(Locale.ROOT);
		for (Who who : Who.values()) {
			if (who.key().equals(text)) {
				return who;
			}
		}
		Diorama.LOGGER.warn("{} in {} is {}, which is none of team, everyone and off; players see no one from afar", KEY, file, text);
		return Who.OFF;
	}

	private void run() {
		while (!this.stopped) {
			try {
				Thread.sleep(EVERY_MS);
			} catch (InterruptedException e) {
				return;
			}
			// One at a time: a server that's behind isn't handed a queue of these.
			if (this.waiting.compareAndSet(false, true)) {
				this.server.execute(() -> {
					this.waiting.set(false);
					if (!this.stopped) {
						this.send();
					}
				});
			}
		}
	}

	/** Tells each listening player who they see and where. Server thread. */
	private void send() {
		if (this.listeners.isEmpty()) {
			return;
		}
		List<ServerPlayer> online = this.server.getPlayerList().getPlayers();
		this.listeners.keySet().removeIf(id -> this.server.getPlayerList().getPlayer(id) == null);
		if (this.listeners.isEmpty()) {
			return;
		}
		boolean again = ++this.sends % AGAIN == 0;
		// Each player's team in each mod, looked up once however many players listen.
		Map<UUID, Object[]> shares = new HashMap<>();
		Map<UUID, Object[]> receives = new HashMap<>();
		if (this.who == Who.TEAM) {
			for (ServerPlayer player : online) {
				Object[] shared = new Object[this.teams.size()];
				Object[] received = new Object[this.teams.size()];
				for (int i = 0; i < shared.length; i++) {
					Teams mod = this.teams.get(i);
					Object team = mod.team(this.server, player);
					shared[i] = team != null && mod.shares(this.server, player) ? team : null;
					received[i] = team != null && mod.receives(this.server, player) ? team : null;
				}
				shares.put(player.getUUID(), shared);
				receives.put(player.getUUID(), received);
			}
		}
		for (Map.Entry<UUID, byte[]> listener : this.listeners.entrySet()) {
			ServerPlayer player = this.server.getPlayerList().getPlayer(listener.getKey());
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (DataOutputStream out = new DataOutputStream(bytes)) {
				out.writeByte(ShareProtocol.FAR_PLAYERS);
				// Where the shared map's messages name a dimension. No dimension has this name, so a
				// Diorama from before this message drops it there.
				out.writeUTF("");
				List<ServerPlayer> seen = new ArrayList<>();
				for (ServerPlayer other : online) {
					if (other != player && !ShareServer.mapOff((net.minecraft.server.level.ServerLevel) other.level())
						&& (this.who == Who.EVERYONE || together(receives.get(player.getUUID()), shares.get(other.getUUID())))) {
						seen.add(other);
					}
				}
				out.writeShort(Math.min(seen.size(), 0xFFFF));
				for (int i = 0; i < Math.min(seen.size(), 0xFFFF); i++) {
					ServerPlayer other = seen.get(i);
					out.writeLong(other.getUUID().getMostSignificantBits());
					out.writeLong(other.getUUID().getLeastSignificantBits());
					out.writeUTF(other.level().dimension().location().toString());
					// Whole blocks: fine enough for a map, and a player standing still sends nothing new.
					out.writeInt(other.blockPosition().getX());
					out.writeInt(other.blockPosition().getY());
					out.writeInt(other.blockPosition().getZ());
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			byte[] message = bytes.toByteArray();
			if (again || !Arrays.equals(message, listener.getValue())) {
				listener.setValue(message);
				ShareProtocol.send(player, message);
			}
		}
	}

	/** Whether a player who receives from these teams sees one who shares with those: the same team in some mod. */
	private static boolean together(Object[] receives, Object[] shares) {
		for (int i = 0; i < receives.length; i++) {
			if (receives[i] != null && receives[i].equals(shares[i])) {
				return true;
			}
		}
		return false;
	}

	/** A mod that puts players in teams, read by reflection. A call that fails says so once and counts as no team. */
	private abstract static class Teams {
		private boolean failed;

		static List<Teams> installed() {
			List<Teams> found = new ArrayList<>();
			for (Teams mod : List.of(new FtbTeams(), new OpenParties())) {
				try {
					mod.lookUp();
					found.add(mod);
				} catch (ClassNotFoundException e) {
					// Not installed.
				} catch (ReflectiveOperationException | LinkageError e) {
					Diorama.LOGGER.warn("Couldn't read teams from {}; its players see no one from afar through it", mod.name(), e);
				}
			}
			return found;
		}

		abstract String name();

		abstract void lookUp() throws ReflectiveOperationException;

		/** The team the player is in, something equal for its every member; null for none. */
		abstract @Nullable Object teamOf(MinecraftServer server, ServerPlayer player) throws ReflectiveOperationException;

		/** Whether the player lets their team see where they are, and wants to see where it is; the mod's own settings. */
		boolean sharesOf(MinecraftServer server, ServerPlayer player) throws ReflectiveOperationException {
			return true;
		}

		boolean receivesOf(MinecraftServer server, ServerPlayer player) throws ReflectiveOperationException {
			return true;
		}

		final @Nullable Object team(MinecraftServer server, ServerPlayer player) {
			try {
				return this.failed ? null : this.teamOf(server, player);
			} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
				this.fail(e);
				return null;
			}
		}

		final boolean shares(MinecraftServer server, ServerPlayer player) {
			try {
				return !this.failed && this.sharesOf(server, player);
			} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
				this.fail(e);
				return false;
			}
		}

		final boolean receives(MinecraftServer server, ServerPlayer player) {
			try {
				return !this.failed && this.receivesOf(server, player);
			} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
				this.fail(e);
				return false;
			}
		}

		private void fail(Throwable e) {
			this.failed = true;
			Diorama.LOGGER.warn("Reading teams from {} failed; its players see no one from afar through it until the server restarts", this.name(), e);
		}
	}

	/**
	 * FTB Teams: a party. Every player also has a team of their own, which is no party and counts
	 * as none. The same names from 1.20.1 on.
	 */
	private static final class FtbTeams extends Teams {
		private Method api;
		private Method manager;
		private Method teamForPlayer;
		private Method isParty;
		private Method id;

		@Override
		String name() {
			return "FTB Teams";
		}

		@Override
		void lookUp() throws ReflectiveOperationException {
			String api = "dev.ftb.mods.ftbteams.api.";
			this.api = Class.forName(api + "FTBTeamsAPI").getMethod("api");
			this.manager = Class.forName(api + "FTBTeamsAPI$API").getMethod("getManager");
			this.teamForPlayer = Class.forName(api + "TeamManager").getMethod("getTeamForPlayerID", UUID.class);
			Class<?> team = Class.forName(api + "Team");
			this.isParty = team.getMethod("isPartyTeam");
			this.id = team.getMethod("getId");
		}

		@Override
		@Nullable Object teamOf(MinecraftServer server, ServerPlayer player) throws ReflectiveOperationException {
			Object team = ((Optional<?>) this.teamForPlayer.invoke(this.manager.invoke(this.api.invoke(null)), player.getUUID())).orElse(null);
			return team != null && (boolean) this.isParty.invoke(team) ? this.id.invoke(team) : null;
		}
	}

	/**
	 * Open Parties and Claims: a party, and each player's own settings for it, "Share Location With
	 * Party" and "Receive Locations From Party", which Xaero's Minimap follows too.
	 */
	private static final class OpenParties extends Teams {
		private Method serverApi;
		private Method partyManager;
		private Method partyByMember;
		private Method partyId;
		private Method playerConfigs;
		private Method loadedConfig;
		private Method effective;
		private Object shareOption;
		private Object receiveOption;

		@Override
		String name() {
			return "Open Parties and Claims";
		}

		@Override
		void lookUp() throws ReflectiveOperationException {
			String pac = "xaero.pac.common.server.";
			Class<?> api = Class.forName(pac + "api.OpenPACServerAPI");
			this.serverApi = api.getMethod("get", MinecraftServer.class);
			this.partyManager = api.getMethod("getPartyManager");
			this.partyByMember = Class.forName(pac + "parties.party.api.IPartyManagerAPI").getMethod("getPartyByMember", UUID.class);
			this.partyId = Class.forName(pac + "parties.party.api.IServerPartyAPI").getMethod("getId");
			this.playerConfigs = api.getMethod("getPlayerConfigs");
			this.loadedConfig = Class.forName(pac + "player.config.api.IPlayerConfigManagerAPI").getMethod("getLoadedConfig", UUID.class);
			this.effective = Class.forName(pac + "player.config.api.IPlayerConfigAPI").getMethod("getEffective", Class.forName(pac + "player.config.api.IPlayerConfigOptionSpecAPI"));
			Class<?> options = Class.forName(pac + "player.config.api.PlayerConfigOptions");
			this.shareOption = options.getField("SHARE_LOCATION_WITH_PARTY").get(null);
			this.receiveOption = options.getField("RECEIVE_LOCATIONS_FROM_PARTY").get(null);
		}

		@Override
		@Nullable Object teamOf(MinecraftServer server, ServerPlayer player) throws ReflectiveOperationException {
			Object party = this.partyByMember.invoke(this.partyManager.invoke(this.serverApi.invoke(null, server)), player.getUUID());
			return party == null ? null : this.partyId.invoke(party);
		}

		@Override
		boolean sharesOf(MinecraftServer server, ServerPlayer player) throws ReflectiveOperationException {
			return this.option(server, player, this.shareOption);
		}

		@Override
		boolean receivesOf(MinecraftServer server, ServerPlayer player) throws ReflectiveOperationException {
			return this.option(server, player, this.receiveOption);
		}

		private boolean option(MinecraftServer server, ServerPlayer player, Object option) throws ReflectiveOperationException {
			Object config = this.loadedConfig.invoke(this.playerConfigs.invoke(this.serverApi.invoke(null, server)), player.getUUID());
			return config != null && Boolean.TRUE.equals(this.effective.invoke(config, option));
		}
	}
}
