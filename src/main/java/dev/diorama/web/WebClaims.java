package dev.diorama.web;

import dev.diorama.Diorama;
import dev.diorama.Facing;
import dev.diorama.Projection;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Stream;
//? if fabric {
/*import net.fabricmc.loader.api.FabricLoader;
*///?} else {
import net.neoforged.fml.ModList;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

/**
 * Land claims on the server's web map: FTB Chunks' and Open Parties and Claims', with their
 * owners' names and colours, as the map in the game shows them. Every couple of seconds the server
 * thread reads them from each mod's own server API, a little at a time, for the map's own web
 * server to answer {@code /claims.json} with. Which chunk each picture's pixel shows is written with
 * the picture ({@link WebChunks}), so the page paints a claim on the ground it covers, hills too.
 *
 * <p>Claims are only ever held in memory, as players are ({@link WebPlayers}), and never written
 * beside the pictures: a file would go on naming owners and places after a team hid its claims or a
 * player unclaimed, whenever a read or a write failed, after the owner turned claims off, or from a
 * folder another web server still serves. A mod's last read older than its cut-off counts as none.
 *
 * <p>The page shows only what every player in the game is shown: FTB Chunks hides a team's claims
 * from other teams unless the team's claim visibility is public, and Open Parties and Claims sends
 * other players' claims only while its {@code claimsSynchronization} is {@code ALL}. Dimensions the
 * web map leaves out ({@link WebMap#leftOut}) have none.
 *
 * <p>Diorama doesn't build against either mod: their server APIs are read by reflection. A name
 * that isn't where this class expects it turns that mod's claims off, logged once.
 */
final class WebClaims {
	static final String PATH = "/claims.json";
	/** The least time between two reads. */
	private static final long MIN_GAP_MS = 2000;
	/** A read may take this share of the server thread's time at most: the next waits this many times as long as it took. */
	private static final int BUDGET = 200;
	/** How long one server task may read before it hands the rest to the next tick. */
	private static final long TASK_NANOS = 1_000_000;
	/** A tick: the wait between two tasks of one read. */
	private static final long TICK_MS = 50;
	/** A mod's last read older than this, or three times its usual wait if longer, says nothing. */
	private static final long STALE_MS = 15_000;

	private final MinecraftServer server;
	private final Predicate<ServerLevel> leftOut;
	private final List<Source> sources;
	private final Thread thread;
	private volatile boolean stopping;
	/** How many claimed chunks the page has and what reading them costs, for the status command. */
	private volatile String summary = "land claims not read yet";
	/** Names this start's answers, so a page holding one from before a restart never takes a new one for it. */
	private final String epoch = Long.toString(System.currentTimeMillis(), 36);
	/** What was last answered and from which reads; see {@link #answer}. */
	private final List<Snapshot> answeredFrom = new ArrayList<>();
	private String answered = "{\"dimensions\":[]}";
	private long version;

	private WebClaims(MinecraftServer server, Predicate<ServerLevel> leftOut, List<Source> sources) {
		this.server = server;
		this.leftOut = leftOut;
		this.sources = sources;
		this.thread = new Thread(this::run, "Diorama web map claims");
		this.thread.setDaemon(true);
	}

	/** The claims to show, or null when no claims mod this reads is installed. */
	static @Nullable WebClaims of(MinecraftServer server, Predicate<ServerLevel> leftOut) {
		List<Source> sources = new ArrayList<>();
		if (loaded("ftbchunks")) {
			sources.add(new FtbChunks());
		}
		if (loaded("openpartiesandclaims")) {
			sources.add(new OpenPartiesAndClaims());
		}
		return sources.isEmpty() ? null : new WebClaims(server, leftOut, sources);
	}

	private static boolean loaded(String id) {
		//? if fabric {
		/*return FabricLoader.getInstance().isModLoaded(id);
		*///?} else {
		return ModList.get().isLoaded(id);
		//?}
	}

	/** Deletes the claims file an unreleased build wrote into a map's folder; claims are never a file now. */
	static void deleteOldFile(Path folder) {
		for (String name : new String[] {"claims.json", "claims.json.part"}) {
			try {
				Files.deleteIfExists(folder.resolve(name));
			} catch (IOException e) {
				Diorama.LOGGER.debug("Couldn't delete the web map's old {} in {}", name, folder, e);
			}
		}
	}

	void start() {
		this.thread.start();
	}

	void stop() {
		this.stopping = true;
		this.thread.interrupt();
	}

	/** How many claimed chunks the page shows, and what reading them costs the server thread. */
	String summary() {
		return this.summary;
	}

	/**
	 * What the page gets now: every claim of the mods whose last read isn't stale, with this
	 * answer's version in it; or null when the page says it has that version already.
	 */
	synchronized @Nullable String answer(@Nullable String since) {
		long now = System.nanoTime();
		List<Snapshot> fresh = new ArrayList<>();
		if (!this.stopping) {
			for (Source source : this.sources) {
				Snapshot read = source.last;
				if (read != null && now - read.at() <= read.staleAfter()) {
					fresh.add(read);
				}
			}
		}
		boolean same = fresh.size() == this.answeredFrom.size();
		for (int i = 0; same && i < fresh.size(); i++) {
			same = fresh.get(i) == this.answeredFrom.get(i);
		}
		if (!same) {
			String text = json(fresh);
			if (!text.equals(this.answered)) {
				this.answered = text;
				this.version++;
			}
			this.answeredFrom.clear();
			this.answeredFrom.addAll(fresh);
		}
		String tag = this.epoch + "-" + this.version;
		return tag.equals(since) ? null : "{\"version\":\"" + tag + "\"," + this.answered.substring(1);
	}

	private void run() {
		long wait = 0;
		while (!this.stopping) {
			try {
				Thread.sleep(wait);
				long started = System.nanoTime();
				Cost cost = new Cost();
				int chunks = 0;
				for (Source source : this.sources) {
					// Each mod's claims are on the page as soon as they're read, and as old as when their
					// read began: a claim hidden or given up while a slow read went on is in it, and
					// the read's age says so. A read that took longer than claims are trusted for is
					// stale as it lands, and shows nothing.
					long began = System.nanoTime();
					Map<String, Map<Long, Owner>> read = this.read(source, cost);
					if (this.stopping) {
						return;
					}
					// Until the next read is in, with room for it to take a few times as long as this one.
					long staleAfter = Math.max(STALE_MS * 1_000_000, 3 * (wait * 1_000_000 + System.nanoTime() - began));
					// A mod that couldn't be read has nothing on the page until it's read again.
					source.last = read == null ? null : new Snapshot(read, began, staleAfter);
					if (read != null) {
						for (Map<Long, Owner> dimension : read.values()) {
							chunks += dimension.size();
						}
					}
				}
				wait = Math.max(MIN_GAP_MS, cost.nanos * BUDGET / 1_000_000);
				long took = System.nanoTime() - started;
				this.summary = String.format(Locale.ROOT, "%d claimed chunks on the page; the last read took %.1f ms of the server thread in %d tasks (the longest %.2f ms) over %.1f s, and the next is in %.0f s",
					chunks, cost.nanos / 1e6, cost.tasks, cost.longest / 1e6, took / 1e9, wait / 1000.0);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	/** The server thread's time a read took, over all its tasks. */
	private static final class Cost {
		long nanos;
		long longest;
		int tasks;
	}

	/** What one task of a read handed back: where the read is, and whether it's done. */
	private record Step(@Nullable Job job, boolean done, long nanos) {
	}

	/**
	 * One mod's claims, read a little at a time on the server thread, one task a tick, each stopping
	 * after {@link #TASK_NANOS}. Null when the mod can't be read now. Waits as long as the server
	 * thread takes to get to each task.
	 */
	private @Nullable Map<String, Map<Long, Owner>> read(Source source, Cost cost) throws InterruptedException {
		Job job = null;
		while (!this.stopping) {
			CompletableFuture<Step> asked = new CompletableFuture<>();
			Job going = job;
			// The claims mods keep their claims on the server thread; it reads them between ticks.
			this.server.execute(() -> {
				try {
					asked.complete(this.step(source, going));
				} catch (Error e) {
					asked.completeExceptionally(e);
					throw e;
				}
			});
			Step step = null;
			while (step == null) {
				try {
					step = asked.get(500, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {
					// A server too far behind to get to it yet.
					if (this.stopping) {
						return null;
					}
				} catch (ExecutionException e) {
					return null;
				}
			}
			cost.nanos += step.nanos();
			cost.longest = Math.max(cost.longest, step.nanos());
			cost.tasks++;
			if (step.done()) {
				return step.job() == null ? null : step.job().claims;
			}
			job = step.job();
			Thread.sleep(TICK_MS);
		}
		return null;
	}

	/** One task of a read. Server thread. */
	private Step step(Source source, @Nullable Job going) {
		java.lang.management.ThreadMXBean threads = java.lang.management.ManagementFactory.getThreadMXBean();
		boolean cpu = threads.isCurrentThreadCpuTimeSupported();
		long start = cpu ? threads.getCurrentThreadCpuTime() : System.nanoTime();
		long deadline = System.nanoTime() + TASK_NANOS;
		Job job = going;
		boolean done;
		try {
			if (job == null) {
				Map<String, Boolean> shown = new HashMap<>();
				for (ServerLevel level : this.server.getAllLevels()) {
					shown.put(level.dimension().location().toString(), !this.leftOut.test(level));
				}
				job = source.begin(this.server, id -> shown.getOrDefault(id, false));
			}
			done = job == null || job.step(deadline);
		} catch (InvocationTargetException e) {
			source.threw(e.getCause());
			job = null;
			done = true;
		} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
			source.fail(e);
			job = null;
			done = true;
		}
		return new Step(job, done, (cpu ? threads.getCurrentThreadCpuTime() : System.nanoTime()) - start);
	}

	private static long key(int x, int z) {
		return ((long) x << 32) | (z & 0xFFFFFFFFL);
	}

	/**
	 * One claim's owner as the page shows it. {@code id} tells owners apart however they're named
	 * and coloured: the mod's own id for the team or player (and sub-claim). {@code titled}:
	 * {@code name} is the claim's whole title, as Open Parties and Claims words it.
	 */
	private record Owner(String id, String name, int rgb, boolean forced, boolean titled) {
	}

	/** One mod's claims at one moment, by dimension: chunk (world x, z) to owner; and when it goes stale. */
	private record Snapshot(Map<String, Map<Long, Owner>> dimensions, long at, long staleAfter) {
	}

	/** The page's JSON, without its version: every chunk claimed in the reads, by dimension. */
	private static String json(List<Snapshot> reads) {
		Map<String, TreeMap<Long, Owner>> byDimension = new TreeMap<>();
		Facing facing = Projection.facing();
		for (Snapshot read : reads) {
			for (Map.Entry<String, Map<Long, Owner>> dimension : read.dimensions().entrySet()) {
				// Chunks in the pictures' turned space, in order, so the same claims always read the same.
				TreeMap<Long, Owner> chunks = byDimension.computeIfAbsent(dimension.getKey(), d -> new TreeMap<>());
				for (Map.Entry<Long, Owner> chunk : dimension.getValue().entrySet()) {
					int x = (int) (chunk.getKey() >> 32);
					int z = (int) (long) chunk.getKey();
					// A chunk both mods claim, which shouldn't happen: the first one's.
					chunks.putIfAbsent(key(facing.blockX(x, z), facing.blockZ(x, z)), chunk.getValue());
				}
			}
		}
		StringBuilder json = new StringBuilder("{\"dimensions\":[");
		boolean firstDimension = true;
		for (Map.Entry<String, TreeMap<Long, Owner>> dimension : byDimension.entrySet()) {
			if (dimension.getValue().isEmpty()) {
				continue;
			}
			Map<Owner, Integer> owners = new HashMap<>();
			List<Owner> order = new ArrayList<>();
			StringBuilder list = new StringBuilder();
			for (Map.Entry<Long, Owner> chunk : dimension.getValue().entrySet()) {
				Integer index = owners.get(chunk.getValue());
				if (index == null) {
					index = order.size();
					owners.put(chunk.getValue(), index);
					order.add(chunk.getValue());
				}
				list.append(list.isEmpty() ? "" : ",").append((int) (chunk.getKey() >> 32)).append(',').append((int) (long) chunk.getKey()).append(',').append(index);
			}
			json.append(firstDimension ? "" : ",").append("{\"id\":\"").append(escape(dimension.getKey())).append("\",\"owners\":[");
			firstDimension = false;
			for (int i = 0; i < order.size(); i++) {
				Owner owner = order.get(i);
				json.append(i == 0 ? "" : ",").append("{\"id\":\"").append(escape(owner.id())).append("\",\"name\":\"").append(escape(owner.name()))
					.append(String.format(Locale.ROOT, "\",\"color\":\"#%06x\"", owner.rgb() & 0xFFFFFF))
					.append(owner.forced() ? ",\"forced\":true" : "").append(owner.titled() ? ",\"titled\":true" : "").append('}');
			}
			json.append("],\"chunks\":[").append(list).append("]}");
		}
		return json.append("]}").toString();
	}

	static String escape(String text) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '"' || c == '\\') {
				out.append('\\').append(c);
			} else if (c < 0x20 || c > 0x7E) {
				out.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	/**
	 * A read of one mod's claims under way, kept between its tasks; only the server thread touches
	 * it. {@link #step} reads until the deadline and says whether it's done; {@link #claims} has
	 * what it found, in the dimensions the page shows.
	 */
	private abstract static class Job {
		final Map<String, Map<Long, Owner>> claims = new HashMap<>();

		abstract boolean step(long deadline) throws ReflectiveOperationException;

		final void claim(String dimension, int chunkX, int chunkZ, Owner owner) {
			this.claims.computeIfAbsent(dimension, d -> new HashMap<>()).putIfAbsent(key(chunkX, chunkZ), owner);
		}
	}

	/** One claims mod's claims, read through its server API. */
	private abstract static class Source {
		private boolean looked;
		private boolean failed;
		/** Whether the mod throwing has been logged: once, then only at debug level. */
		private boolean warned;
		/** Its last read, or null while it has none to show. Set by the reading thread, read by the web server's. */
		volatile @Nullable Snapshot last;

		/** A read of every claim others may see, in the dimensions {@code shown} says yes to; null when the mod can't say now. Server thread. */
		final @Nullable Job begin(MinecraftServer server, Predicate<String> shown) throws ReflectiveOperationException {
			if (!this.looked) {
				this.looked = true;
				try {
					this.lookUp(server);
					Diorama.LOGGER.info("{} is installed: the web map shows its land claims", this.name());
				} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
					this.fail(e);
				}
			}
			return this.failed ? null : this.read(server, shown);
		}

		/** The mod itself threw: nothing of it on the page until it's read again. */
		final void threw(Throwable e) {
			if (!this.warned) {
				this.warned = true;
				Diorama.LOGGER.warn("{} couldn't give its land claims to the web map; trying again", this.name(), e);
			} else {
				Diorama.LOGGER.debug("{} couldn't give its land claims", this.name(), e);
			}
		}

		/** A name not where it's expected: its claims are off until the server starts again. */
		final void fail(Throwable e) {
			if (!this.failed) {
				this.failed = true;
				Diorama.LOGGER.warn("Couldn't read {}'s land claims; the web map won't show them", this.name(), e);
			}
		}

		abstract String name();

		abstract void lookUp(MinecraftServer server) throws ReflectiveOperationException;

		abstract @Nullable Job read(MinecraftServer server, Predicate<String> shown) throws ReflectiveOperationException;
	}

	/**
	 * FTB Chunks: every claimed chunk from its manager, and each team's name and colour from FTB
	 * Teams, as the map in the game shows them. A team whose claim visibility isn't public has its
	 * claims sent only to its allies, so the page leaves them out. The same names from 1.20.1 to 26.1.
	 */
	private static final class FtbChunks extends Source {
		private Object api;
		private Method managerLoaded;
		private Method manager;
		private Method allChunks;
		private Method chunkPos;
		private Method chunkTeamData;
		private Method chunkForceLoaded;
		private Method posDimension;
		private Method posX;
		private Method posZ;
		private Method hidden;
		private Method team;
		private Method teamId;
		private Method getProperty;
		private Object colorProperty;
		private Object nameProperty;
		private Method rgb;

		@Override
		String name() {
			return "FTB Chunks";
		}

		@Override
		void lookUp(MinecraftServer server) throws ReflectiveOperationException {
			String api = "dev.ftb.mods.ftbchunks.api.";
			this.api = Class.forName(api + "FTBChunksAPI").getMethod("api").invoke(null);
			Class<?> apiType = Class.forName(api + "FTBChunksAPI$API");
			this.managerLoaded = apiType.getMethod("isManagerLoaded");
			this.manager = apiType.getMethod("getManager");
			this.allChunks = Class.forName(api + "ClaimedChunkManager").getMethod("getAllClaimedChunks");
			Class<?> chunk = Class.forName(api + "ClaimedChunk");
			this.chunkPos = chunk.getMethod("getPos");
			this.chunkTeamData = chunk.getMethod("getTeamData");
			this.chunkForceLoaded = chunk.getMethod("isForceLoaded");
			Class<?> pos = Class.forName("dev.ftb.mods.ftblibrary.math.ChunkDimPos");
			this.posDimension = pos.getMethod("dimension");
			this.posX = pos.getMethod("x");
			this.posZ = pos.getMethod("z");
			Class<?> teamData = Class.forName(api + "ChunkTeamData");
			this.hidden = teamData.getMethod("shouldHideClaims");
			this.team = teamData.getMethod("getTeam");
			Class<?> team = Class.forName("dev.ftb.mods.ftbteams.api.Team");
			this.teamId = team.getMethod("getId");
			this.getProperty = team.getMethod("getProperty", Class.forName("dev.ftb.mods.ftbteams.api.property.TeamProperty"));
			Class<?> properties = Class.forName("dev.ftb.mods.ftbteams.api.property.TeamProperties");
			this.colorProperty = properties.getField("COLOR").get(null);
			this.nameProperty = properties.getField("DISPLAY_NAME").get(null);
			this.rgb = Class.forName("dev.ftb.mods.ftblibrary.icon.Color4I").getMethod("rgb");
		}

		@Override
		@Nullable Job read(MinecraftServer server, Predicate<String> shown) throws ReflectiveOperationException {
			if (!(boolean) this.managerLoaded.invoke(this.api)) {
				return null;
			}
			// A copy: the manager's own list may change between this read's tasks.
			List<Object> chunks = new ArrayList<>((Collection<?>) this.allChunks.invoke(this.manager.invoke(this.api)));
			// Looked up once a read, not for each chunk: a team's owner, plain and force loaded (none
			// when only its allies see its claims), and a dimension's name (null when the page leaves it out).
			Map<Object, Owner[]> teams = new IdentityHashMap<>();
			Map<Object, Optional<String>> dimensions = new IdentityHashMap<>();
			return new Job() {
				private int next;

				@Override
				boolean step(long deadline) throws ReflectiveOperationException {
					while (this.next < chunks.size()) {
						Object chunk = chunks.get(this.next++);
						Object pos = FtbChunks.this.chunkPos.invoke(chunk);
						Object key = FtbChunks.this.posDimension.invoke(pos);
						Optional<String> dimension = dimensions.get(key);
						if (dimension == null) {
							String id = ((net.minecraft.resources.ResourceKey<?>) key).location().toString();
							dimension = shown.test(id) ? Optional.of(id) : Optional.empty();
							dimensions.put(key, dimension);
						}
						if (dimension.isPresent()) {
							Object data = FtbChunks.this.chunkTeamData.invoke(chunk);
							Owner[] owner = teams.get(data);
							if (owner == null) {
								owner = FtbChunks.this.owner(data).map(o -> new Owner[] {o, new Owner(o.id(), o.name(), o.rgb(), true, false)}).orElse(new Owner[0]);
								teams.put(data, owner);
							}
							if (owner.length > 0) {
								this.claim(dimension.get(), (int) FtbChunks.this.posX.invoke(pos), (int) FtbChunks.this.posZ.invoke(pos), owner[(boolean) FtbChunks.this.chunkForceLoaded.invoke(chunk) ? 1 : 0]);
							}
						}
						if ((this.next & 15) == 0 && System.nanoTime() > deadline) {
							return this.next >= chunks.size();
						}
					}
					return true;
				}
			};
		}

		/** A team's claims as the page shows them; empty when only its allies see them. */
		private Optional<Owner> owner(Object data) throws ReflectiveOperationException {
			if ((boolean) this.hidden.invoke(data)) {
				return Optional.empty();
			}
			Object team = this.team.invoke(data);
			Object colour = this.getProperty.invoke(team, this.colorProperty);
			Object name = this.getProperty.invoke(team, this.nameProperty);
			return Optional.of(new Owner("ftb:" + this.teamId.invoke(team), name instanceof String s ? s : "", colour == null ? 0xFFFFFF : (int) this.rgb.invoke(colour), false, false));
		}
	}

	/**
	 * Open Parties and Claims: every claim from its server claims manager, owner by owner, with the
	 * title and colour it gives a claim ("Base - Alex's Claim"), in the server's own language as it
	 * words them for players without the mod. Older versions (the newest on 1.21.3, 1.21.6 and
	 * 1.21.9) have no call for a title or colour, so they're put together here as those word them.
	 *
	 * <p>It keeps each owner's claims per dimension as lists of chunks, one list per kind of claim
	 * (sub-claim, forceloadable or not), so only claimed chunks are read, and a dimension the page
	 * leaves out isn't read at all.
	 */
	private static final class OpenPartiesAndClaims extends Source {
		private static final String PAC = "xaero.pac.";
		private static final UUID SERVER_CLAIM = new UUID(0, 0);
		private static final UUID EXPIRED_CLAIM = new UUID(0, 1);

		private Method serverApi;
		private Method claimsManager;
		private Method localizer;
		private Method localize;
		/** The server's own wording of one of its texts, for older versions, whose localizer needs a player. */
		private @Nullable Method defaultTranslation;
		private Method playerInfos;
		private Method infoPlayer;
		private Method infoDimensions;
		private Method dimensionLists;
		private Method listState;
		private Method listChunks;
		private Method listCount;
		private Method claimPlayer;
		private Method claimSub;
		private Method claimForceloadable;
		private @Nullable Method fullName;
		private @Nullable Method colour;
		private Method playerInfo;
		private Method username;
		private Method claimsName;
		private Method subClaimsName;
		private Method claimsColour;
		private Method subClaimsColour;
		private Object config;
		private java.lang.reflect.Field syncField;

		@Override
		String name() {
			return "Open Parties and Claims";
		}

		@Override
		void lookUp(MinecraftServer server) throws ReflectiveOperationException {
			Class<?> api = Class.forName(PAC + "common.server.api.OpenPACServerAPI");
			this.serverApi = api.getMethod("get", MinecraftServer.class);
			this.claimsManager = api.getMethod("getServerClaimsManager");
			this.localizer = api.getMethod("getAdaptiveTextLocalizer");
			Class<?> localizerType = Class.forName(PAC + "common.server.player.localization.api.IAdaptiveLocalizerAPI");
			this.localize = localizerType.getMethod("getFor", net.minecraft.server.level.ServerPlayer.class, Component.class);
			try {
				this.defaultTranslation = Class.forName(PAC + "common.server.player.localization.AdaptiveLocalizer").getMethod("getDefaultTranslation", String.class);
			} catch (NoSuchMethodException e) {
				this.defaultTranslation = null;
			}
			Class<?> manager = Class.forName(PAC + "common.server.claims.api.IServerClaimsManagerAPI");
			Class<?> claim = Class.forName(PAC + "common.claims.player.api.IPlayerChunkClaimAPI");
			this.playerInfos = manager.getMethod("getPlayerInfoStream");
			Class<?> info = Class.forName(PAC + "common.server.claims.player.api.IServerPlayerClaimInfoAPI");
			this.infoPlayer = info.getMethod("getPlayerId");
			this.infoDimensions = info.getMethod("getStream");
			this.dimensionLists = Class.forName(PAC + "common.claims.player.api.IPlayerDimensionClaimsAPI").getMethod("getStream");
			Class<?> list = Class.forName(PAC + "common.claims.player.api.IPlayerClaimPosListAPI");
			this.listState = list.getMethod("getClaimState");
			this.listChunks = list.getMethod("getStream");
			this.listCount = list.getMethod("getCount");
			this.claimPlayer = claim.getMethod("getPlayerId");
			this.claimSub = claim.getMethod("getSubConfigIndex");
			this.claimForceloadable = claim.getMethod("isForceloadable");
			try {
				this.fullName = manager.getMethod("getFullName", claim, net.minecraft.resources.ResourceLocation.class, boolean.class);
				this.colour = manager.getMethod("getColor", claim, net.minecraft.resources.ResourceLocation.class);
			} catch (NoSuchMethodException e) {
				// Older versions.
				this.fullName = null;
				this.colour = null;
			}
			this.playerInfo = manager.getMethod("getPlayerInfo", UUID.class);
			this.username = info.getMethod("getPlayerUsername");
			this.claimsName = info.getMethod("getClaimsName");
			this.subClaimsName = info.getMethod("getClaimsName", int.class);
			this.claimsColour = info.getMethod("getClaimsColor");
			this.subClaimsColour = info.getMethod("getClaimsColor", int.class);
			// Whether other players' claims reach the game at all: not its API, but the only place it's kept.
			Class<?> config = Class.forName(PAC + "common.server.config.ServerConfig");
			this.config = config.getField("CONFIG").get(null);
			this.syncField = config.getField("claimsSynchronization");
		}

		@Override
		@Nullable Job read(MinecraftServer server, Predicate<String> shown) throws ReflectiveOperationException {
			// ALL sends every claim to every player; OWNED_ONLY each player their own and the server's;
			// NOT_SYNCED none but their own. The page is no one's, so it shows what reaches everyone.
			Object sync = this.syncField.get(this.config);
			String mode = String.valueOf(sync.getClass().getMethod("get").invoke(sync));
			boolean all = mode.equals("ALL");
			Object api = this.serverApi.invoke(null, server);
			Object manager = this.claimsManager.invoke(api);
			Object localizer = this.localizer.invoke(api);
			// The owners, copied now: its own list may change between this read's tasks.
			List<Object> owners = new ArrayList<>();
			if (!mode.equals("NOT_SYNCED")) {
				try (Stream<?> infos = (Stream<?>) this.playerInfos.invoke(manager)) {
					for (Iterator<?> i = infos.iterator(); i.hasNext(); ) {
						Object info = i.next();
						if (all || SERVER_CLAIM.equals(this.infoPlayer.invoke(info))) {
							owners.add(info);
						}
					}
				}
			}
			// A kind of claim's owner, as the page shows it, per dimension: its title can name the dimension.
			Map<String, Map<Object, Owner>> kinds = new HashMap<>();
			return new Job() {
				private int next;
				/** The current owner's lists of chunks still to read: dimension, then the list. */
				private final ArrayDeque<Object[]> lists = new ArrayDeque<>();
				/** The chunks of the list being added, copied out of it whole; null between lists. */
				private long @Nullable [] chunks;
				private int added;
				private String dimension = "";
				private @Nullable Owner owner;

				@Override
				boolean step(long deadline) throws ReflectiveOperationException {
					while (true) {
						long[] chunks = this.chunks;
						if (chunks != null) {
							// A list can hold thousands: added a slice at a time.
							while (this.added < chunks.length) {
								long chunk = chunks[this.added++];
								this.claim(this.dimension, (int) (chunk >> 32), (int) chunk, this.owner);
								if ((this.added & 255) == 0 && System.nanoTime() > deadline) {
									break;
								}
							}
							if (this.added >= chunks.length) {
								this.chunks = null;
							}
						} else if (this.lists.isEmpty()) {
							if (this.next >= owners.size()) {
								return true;
							}
							this.expand(owners.get(this.next++));
						} else {
							Object[] list = this.lists.poll();
							this.dimension = (String) list[0];
							Object state = OpenPartiesAndClaims.this.listState.invoke(list[2]);
							Map<Object, Owner> ofDimension = kinds.computeIfAbsent(this.dimension, d -> new IdentityHashMap<>());
							Owner owner = ofDimension.get(state);
							if (owner == null) {
								owner = OpenPartiesAndClaims.this.owner(manager, localizer, state, list[1]);
								ofDimension.put(state, owner);
							}
							this.owner = owner;
							// Copied whole now: the list itself may change before the next task.
							int count = (int) OpenPartiesAndClaims.this.listCount.invoke(list[2]);
							long[] copy = new long[count];
							int n = 0;
							try (Stream<?> positions = (Stream<?>) OpenPartiesAndClaims.this.listChunks.invoke(list[2])) {
								for (Iterator<?> c = positions.iterator(); c.hasNext() && n < count; ) {
									ChunkPos pos = (ChunkPos) c.next();
									copy[n++] = key(pos.getMinBlockX() >> 4, pos.getMinBlockZ() >> 4);
								}
							}
							this.chunks = n == count ? copy : java.util.Arrays.copyOf(copy, n);
							this.added = 0;
						}
						if (System.nanoTime() > deadline) {
							return this.chunks == null && this.lists.isEmpty() && this.next >= owners.size();
						}
					}
				}

				/** An owner's lists of chunks, in the dimensions the page shows. */
				private void expand(Object info) throws ReflectiveOperationException {
					try (Stream<?> dimensions = (Stream<?>) OpenPartiesAndClaims.this.infoDimensions.invoke(info)) {
						for (Iterator<?> d = dimensions.iterator(); d.hasNext(); ) {
							Map.Entry<?, ?> entry = (Map.Entry<?, ?>) d.next();
							String dimension = entry.getKey().toString();
							if (!shown.test(dimension)) {
								continue;
							}
							try (Stream<?> lists = (Stream<?>) OpenPartiesAndClaims.this.dimensionLists.invoke(entry.getValue())) {
								for (Iterator<?> l = lists.iterator(); l.hasNext(); ) {
									this.lists.add(new Object[] {dimension, entry.getKey(), l.next()});
								}
							}
						}
					}
				}
			};
		}

		private Owner owner(Object manager, Object localizer, Object state, Object dimension) throws ReflectiveOperationException {
			boolean forceloadable = (boolean) this.claimForceloadable.invoke(state);
			UUID player = (UUID) this.claimPlayer.invoke(state);
			int sub = (int) this.claimSub.invoke(state);
			String id = "opac:" + player + "/" + sub;
			if (this.fullName != null) {
				Component title = (Component) this.localize.invoke(localizer, null, this.fullName.invoke(manager, state, dimension, true));
				return new Owner(id, title.getString(), (int) this.colour.invoke(manager, state, dimension) & 0xFFFFFF, forceloadable, true);
			}
			Object info = this.playerInfo.invoke(manager, player);
			String custom = (String) this.subClaimsName.invoke(info, sub);
			if (sub != -1 && (custom == null || custom.isEmpty())) {
				custom = (String) this.claimsName.invoke(info);
			}
			Component forceloaded = forceloadable ? this.text(localizer, "gui.xaero_pac_marked_for_forceload") : Component.literal("");
			Component owner = player.equals(SERVER_CLAIM) ? this.text(localizer, "gui.xaero_pac_title_entered_server_claim", forceloaded)
				: player.equals(EXPIRED_CLAIM) ? this.text(localizer, "gui.xaero_pac_title_entered_expired_claim", forceloaded)
				: this.text(localizer, "gui.xaero_pac_title_entered_claim", (String) this.username.invoke(info), forceloaded);
			String title = custom == null || custom.isEmpty() ? owner.getString() : custom + " - " + owner.getString();
			Integer own = (Integer) this.subClaimsColour.invoke(info, sub);
			return new Owner(id, title, (own != null ? own : (int) this.claimsColour.invoke(info)) & 0xFFFFFF, forceloadable, true);
		}

		/** One of its texts in the server's own language, as it words them for players without the mod. */
		private Component text(Object localizer, String key, Object... args) throws ReflectiveOperationException {
			// A key the game doesn't know is its own wording: the default translation is the wording.
			String wording = this.defaultTranslation != null && this.defaultTranslation.getDeclaringClass().isInstance(localizer)
				? (String) this.defaultTranslation.invoke(localizer, key) : key;
			return Component.translatable(wording, args);
		}
	}
}
