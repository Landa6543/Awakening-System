package dev.diorama.web;

import dev.diorama.Diorama;
import dev.diorama.Facing;
import dev.diorama.Projection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * The players on the server's web map, as BlueMap, Dynmap and squaremap show theirs: once a
 * second, who is online and where, for the map's own web server to answer {@code /players.json}
 * with.
 *
 * <p>Where players are is only ever held in memory, as BlueMap holds its live data, and never
 * written beside the map's pictures: a file would go on saying where they were after a crash,
 * after the owner turned players off, or from a folder another web server still serves. So the
 * page has players only through Diorama's own web server (or a proxy to it), and it has none the
 * moment the server process is gone.
 *
 * <p>Left out, so the page says no more than a player near them in the game could see: anyone
 * sneaking (the game hides their name behind blocks then, and it's how a player takes themselves
 * off the page), invisible or spectating, and anyone in a dimension the map leaves out.
 */
final class WebPlayers {
	static final String PATH = "/players.json";
	static final String NONE = "{\"players\":[]}";
	private static final long EVERY_MS = 1000;
	/** An answer older than this says nothing: the server has stopped answering, and its players may be anywhere by now. */
	private static final long STALE_MS = 15_000;

	private final MinecraftServer server;
	private final Predicate<ServerLevel> leftOut;
	private final Thread thread;
	private volatile boolean stopping;
	private volatile String json = NONE;
	/** When the server last answered, on the clock that only runs forward; 0 before it has. */
	private volatile long answered;

	WebPlayers(MinecraftServer server, Predicate<ServerLevel> leftOut) {
		this.server = server;
		this.leftOut = leftOut;
		this.thread = new Thread(this::run, "Diorama web map players");
		this.thread.setDaemon(true);
	}

	void start() {
		this.thread.start();
	}

	void stop() {
		this.stopping = true;
		this.json = NONE;
		this.thread.interrupt();
	}

	/** What the page gets now: who it shows, or no one while the server isn't answering. */
	String json() {
		long answered = this.answered;
		return this.stopping || answered == 0 || System.nanoTime() - answered > STALE_MS * 1_000_000 ? NONE : this.json;
	}

	private void run() {
		boolean warned = false;
		// The look still waiting for the server thread, if it's behind: one at a time, never a queue of them.
		CompletableFuture<String> asked = null;
		while (!this.stopping) {
			try {
				Thread.sleep(EVERY_MS);
				if (asked == null) {
					// Entities are the server thread's; it answers between ticks.
					CompletableFuture<String> look = new CompletableFuture<>();
					asked = look;
					this.server.execute(() -> {
						try {
							look.complete(this.collect());
						} catch (RuntimeException e) {
							look.completeExceptionally(e);
						}
					});
				}
				String now = asked.get(EVERY_MS, TimeUnit.MILLISECONDS);
				asked = null;
				if (!this.stopping) {
					this.json = now;
					// Never 0, which means no answer yet.
					this.answered = Math.max(1, System.nanoTime());
				}
			} catch (InterruptedException e) {
				return;
			} catch (TimeoutException e) {
				// A server too far behind to answer: asked again for the same look next time.
			} catch (ExecutionException e) {
				asked = null;
				if (!warned) {
					warned = true;
					Diorama.LOGGER.warn("The web map couldn't read where players are", e.getCause());
				}
			}
		}
	}

	/** Who the page shows, as its JSON. Server thread. */
	private String collect() {
		List<ServerPlayer> shown = new ArrayList<>();
		for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
			if (player.isAlive() && !player.isSpectator() && !player.isInvisible() && !player.isShiftKeyDown()
				&& player.level() instanceof ServerLevel level && !this.leftOut.test(level)) {
				shown.add(player);
			}
		}
		shown.sort(Comparator.comparing(ServerPlayer::getScoreboardName));
		Facing facing = Projection.facing();
		StringBuilder json = new StringBuilder("{\"players\":[");
		for (int i = 0; i < shown.size(); i++) {
			ServerPlayer player = shown.get(i);
			// Which way they look, across the picture: yaw 0 is south, turning clockwise from above.
			double yaw = Math.toRadians(player.getYRot());
			double lookX = -Math.sin(yaw);
			double lookZ = Math.cos(yaw);
			double heading = Math.atan2(facing.z(lookX, lookZ) * Projection.sin(), facing.x(lookX, lookZ));
			json.append(i == 0 ? "" : ",").append(String.format(Locale.ROOT,
				"{\"name\":\"%s\",\"dimension\":\"%s\",\"x\":%d,\"y\":%d,\"z\":%d,\"u\":%.1f,\"v\":%.1f,\"heading\":%.2f}",
				escape(player.getScoreboardName()), escape(player.level().dimension().location().toString()),
				player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(),
				Projection.u(player.getX(), player.getZ()), Projection.v(player.getX(), player.getY(), player.getZ()), heading));
		}
		return json.append("]}").toString();
	}

	private static String escape(String text) {
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
}
