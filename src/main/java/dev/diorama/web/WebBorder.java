package dev.diorama.web;

import dev.diorama.Diorama;
import dev.diorama.Facing;
import dev.diorama.compat.ChunkyBorder;
import dev.diorama.data.ChunkSnapshot;
import dev.diorama.data.SavedChunk;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.jspecify.annotations.Nullable;

/**
 * A world border on the web page: a line where its wall meets the ground, as on the map in the game,
 * in the page's map space (the pictures' columns and rows), so the page only joins the points up
 * and the facing and the camera's angle are worked out here. The page reads them from
 * {@code border.json}: a list of lines, each with its dimension, its colour and its points, so any
 * border can come this way. Only Chunky Border's comes so far ({@link ChunkyBorder}).
 *
 * <p>The server's web map writes that file beside its pictures (see {@link Server}); the player's
 * own web map ({@link ClientWebMap}) answers it from memory.
 */
public final class WebBorder {
	static final String FILE = "border.json";
	/** The page's file when there's no border: no lines. */
	static final String NONE = "{\"lines\":[]}";
	/** At most this many points are read along a border's outline: one every block or two up to a border 20,000 blocks round. */
	private static final int MOST_SAMPLES = 20_000;
	/** How far a point may be off the straight line through its neighbours and still be left out, in blocks on the map. */
	private static final double TOLERANCE = 0.2;

	private WebBorder() {
	}

	/** The ground's height in a column of the world (where a player would stand), NaN where it isn't known. */
	public interface Ground {
		float at(int x, int z);
	}

	/** A border's line on the page: its dimension, its colour (RGB) and its points, map column and row in turn. */
	public record Line(String dimension, int color, double[] points) {
	}

	/**
	 * The outline of {@code shape} as a closed line in map space, each point on the ground just
	 * inside it. Where the ground isn't known, the nearest height known along the outline stands in,
	 * or {@code fallback}; a point the line doesn't bend at is left out.
	 */
	public static double[] line(ChunkyBorder.Shape shape, Facing facing, double sin, double cos, Ground ground, float fallback) {
		int corners = shape.corners();
		double[] xs = new double[corners];
		double[] zs = new double[corners];
		double area = 0;
		double round = 0;
		for (int i = 0; i < corners; i++) {
			xs[i] = facing.x(shape.xs()[i], shape.zs()[i]);
			zs[i] = facing.z(shape.xs()[i], shape.zs()[i]);
		}
		for (int i = 0; i < corners; i++) {
			int j = (i + 1) % corners;
			area += xs[i] * zs[j] - xs[j] * zs[i];
			round += Math.hypot(xs[j] - xs[i], zs[j] - zs[i]);
		}
		double step = Math.max(1, round / MOST_SAMPLES);
		int[] sides = new int[corners];
		int count = 0;
		for (int i = 0; i < corners; i++) {
			int j = (i + 1) % corners;
			double length = Math.hypot(xs[j] - xs[i], zs[j] - zs[i]);
			sides[i] = length == 0 ? 0 : (int) Math.max(1, Math.ceil(length / step));
			count += sides[i];
		}
		// Each side's points but its last, which is the next side's first; the first again at the end.
		double[] us = new double[count + 1];
		double[] rows = new double[count + 1];
		float[] heights = new float[count + 1];
		int n = 0;
		for (int i = 0; i < corners; i++) {
			int samples = sides[i];
			if (samples == 0) {
				continue;
			}
			int j = (i + 1) % corners;
			double dx = xs[j] - xs[i];
			double dz = zs[j] - zs[i];
			double length = Math.hypot(dx, dz);
			double inX = area > 0 ? -dz / length : dz / length;
			double inZ = area > 0 ? dx / length : -dx / length;
			for (int k = 0; k < samples; k++) {
				double t = (double) k / samples;
				double x = xs[i] + dx * t;
				double z = zs[i] + dz * t;
				int blockX = (int) Math.floor(x + inX * 0.5);
				int blockZ = (int) Math.floor(z + inZ * 0.5);
				us[n] = x;
				rows[n] = z;
				heights[n] = ground.at(facing.worldBlockX(blockX, blockZ), facing.worldBlockZ(blockX, blockZ));
				n++;
			}
		}
		if (n == 0) {
			return new double[0];
		}
		fill(heights, n, fallback);
		us[n] = us[0];
		rows[n] = rows[0];
		heights[n] = heights[0];
		n++;
		double[] points = new double[n * 2];
		for (int i = 0; i < n; i++) {
			points[i * 2] = us[i];
			points[i * 2 + 1] = rows[i] * sin - heights[i] * cos;
		}
		return simplify(points);
	}

	/** Fills the heights not known with the nearest known one along the closed outline, or all with {@code fallback}. */
	private static void fill(float[] heights, int n, float fallback) {
		int first = -1;
		for (int i = 0; i < n; i++) {
			if (!Float.isNaN(heights[i])) {
				first = i;
				break;
			}
		}
		if (first < 0) {
			Arrays.fill(heights, 0, n, fallback);
			return;
		}
		// Round the outline from the first known: each unknown takes the nearer of the known ones either side.
		int last = first;
		for (int k = 1; k <= n; k++) {
			int i = (first + k) % n;
			if (Float.isNaN(heights[i]) && k < n) {
				continue;
			}
			int gap = k - (last - first + n) % n - 1;
			for (int g = 1; g <= gap; g++) {
				int at = (last + g) % n;
				heights[at] = g <= (gap + 1) / 2 ? heights[last] : heights[i];
			}
			last = i;
		}
	}

	/** Leaves out the points within {@link #TOLERANCE} of the line through those kept either side (Douglas and Peucker's way). */
	private static double[] simplify(double[] points) {
		int n = points.length / 2;
		if (n <= 2) {
			return points;
		}
		boolean[] keep = new boolean[n];
		keep[0] = true;
		keep[n - 1] = true;
		int[] stack = new int[n * 2];
		int top = 0;
		stack[top++] = 0;
		stack[top++] = n - 1;
		while (top > 0) {
			int b = stack[--top];
			int a = stack[--top];
			double ax = points[a * 2];
			double ay = points[a * 2 + 1];
			double dx = points[b * 2] - ax;
			double dy = points[b * 2 + 1] - ay;
			double length = Math.hypot(dx, dy);
			double worst = -1;
			int far = -1;
			for (int i = a + 1; i < b; i++) {
				double px = points[i * 2] - ax;
				double py = points[i * 2 + 1] - ay;
				// From the chord, or from its start where the chord has no length (the closed line's ends).
				double off = length == 0 ? Math.hypot(px, py) : Math.abs(px * dy - py * dx) / length;
				if (off > worst) {
					worst = off;
					far = i;
				}
			}
			if (far >= 0 && worst > TOLERANCE) {
				keep[far] = true;
				stack[top++] = a;
				stack[top++] = far;
				stack[top++] = far;
				stack[top++] = b;
			}
		}
		int kept = 0;
		for (boolean k : keep) {
			kept += k ? 1 : 0;
		}
		double[] out = new double[kept * 2];
		int o = 0;
		for (int i = 0; i < n; i++) {
			if (keep[i]) {
				out[o++] = points[i * 2];
				out[o++] = points[i * 2 + 1];
			}
		}
		return out;
	}

	/** The page's file for {@code lines}. */
	public static String json(List<Line> lines) {
		StringBuilder json = new StringBuilder("{\"lines\":[");
		for (int l = 0; l < lines.size(); l++) {
			Line line = lines.get(l);
			json.append(l > 0 ? "," : "").append("{\"dimension\":\"").append(line.dimension().replace("\\", "\\\\").replace("\"", "\\\""))
				.append("\",\"color\":\"#").append(String.format(Locale.ROOT, "%06x", line.color() & 0xFFFFFF)).append("\",\"points\":[");
			double[] points = line.points();
			for (int i = 0; i < points.length; i++) {
				if (i > 0) {
					json.append(',');
				}
				// A tenth of a block: under a pixel at the page's closest zoom.
				long tenths = Math.round(points[i] * 10);
				if (tenths < 0) {
					json.append('-');
					tenths = -tenths;
				}
				json.append(tenths / 10);
				if (tenths % 10 != 0) {
					json.append('.').append(tenths % 10);
				}
			}
			json.append("]}");
		}
		return json.append("]}").toString();
	}

	/**
	 * The server's web map's borders: Chunky Border's, read from the add-on on the server thread every
	 * few seconds, on the ground the save has along them. Used on the web map's own thread.
	 */
	static final class Server {
		/** How often the add-on is asked whether a border changed. */
		static final long EVERY_MS = 5000;
		private static final short UNKNOWN = Short.MIN_VALUE;
		/** How far down from the top of a column the ground is looked for under a tree, as on the map in the game. */
		private static final int TREE_REACH = 48;

		private final MinecraftServer server;
		private final Path folder;
		private Map<String, ChunkyBorder.Shape> shapes = new HashMap<>();
		/** Per dimension, the ground of each chunk along a border read so far, by {@link ChunkSnapshot#key}. */
		private final Map<String, Map<Long, short[]>> grounds = new HashMap<>();
		private long asked;
		private String written = "";

		Server(MinecraftServer server, Path folder) {
			this.server = server;
			this.folder = folder;
		}

		/** Whether the add-on is installed: else there's never a border to show. */
		static boolean wanted() {
			return ChunkyBorder.serverInstalled();
		}

		/**
		 * Asks the add-on for its borders, at most every {@link #EVERY_MS}; true if one changed since
		 * the last time. Waits a second at most for the server thread.
		 */
		boolean changed(Iterable<String> dimensions) {
			long now = System.currentTimeMillis();
			if (now - this.asked < EVERY_MS) {
				return false;
			}
			this.asked = now;
			CompletableFuture<Map<String, ChunkyBorder.Shape>> look = new CompletableFuture<>();
			this.server.execute(() -> {
				try {
					look.complete(ChunkyBorder.serverBorders(dimensions));
				} catch (RuntimeException | LinkageError e) {
					look.completeExceptionally(e);
				}
			});
			Map<String, ChunkyBorder.Shape> borders;
			try {
				borders = look.get(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			} catch (Exception e) {
				// Too busy to answer, or the add-on failed: asked again next time.
				return false;
			}
			boolean changed = borders.size() != this.shapes.size();
			for (Map.Entry<String, ChunkyBorder.Shape> entry : borders.entrySet()) {
				changed |= !entry.getValue().same(this.shapes.get(entry.getKey()));
			}
			this.shapes = borders;
			return changed;
		}

		/** The chunk was saved since its ground was read. */
		void forget(String dimension, int chunkX, int chunkZ) {
			Map<Long, short[]> grounds = this.grounds.get(dimension);
			if (grounds != null) {
				grounds.remove(ChunkSnapshot.key(chunkX, chunkZ));
			}
		}

		/**
		 * Writes the page's file: the line of each border in one of {@code shown} (the dimensions the
		 * page shows), on the ground their saves have. Unchanged, it isn't written again, so the page's
		 * browser gets a "not modified".
		 */
		void write(Map<String, WebMap.Dimension> shown, Facing facing, double sin, double cos) {
			List<Line> lines = new ArrayList<>();
			for (Map.Entry<String, ChunkyBorder.Shape> entry : this.shapes.entrySet()) {
				WebMap.Dimension dimension = shown.get(entry.getKey());
				if (dimension == null) {
					continue;
				}
				ServerLevel level = dimension.level();
				Map<Long, short[]> known = this.grounds.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
				boolean[] read = {false};
				// Under a roof the page shows the land under it, at many heights: the line is level, at sea level.
				Ground ground = level.dimensionType().hasCeiling() ? (x, z) -> Float.NaN : (x, z) -> {
					short[] chunk = known.computeIfAbsent(ChunkSnapshot.key(x >> 4, z >> 4), key -> {
						read[0] = true;
						return groundOf(level, dimension.savedChunk(x >> 4, z >> 4));
					});
					short height = chunk[((z & 15) << 4) | (x & 15)];
					return height == UNKNOWN ? Float.NaN : height;
				};
				lines.add(new Line(entry.getKey(), ChunkyBorder.BLUE, line(entry.getValue(), facing, sin, cos, ground, level.getSeaLevel())));
				if (read[0]) {
					// The save keeps the whole chunks it read for the next pass's tiles, which these aren't for.
					dimension.forgetSavedChunks();
				}
			}
			// Kept for the borders there are now.
			this.grounds.keySet().retainAll(this.shapes.keySet());
			String json = json(lines);
			if (json.equals(this.written) && Files.isRegularFile(this.folder.resolve(FILE))) {
				return;
			}
			Path file = this.folder.resolve(FILE);
			Path part = this.folder.resolve(FILE + ".part");
			try {
				Files.writeString(part, json, StandardCharsets.UTF_8);
				Files.move(part, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
				this.written = json;
			} catch (IOException e) {
				Diorama.LOGGER.warn("Couldn't write the web map's {}", FILE, e);
			}
		}

		/**
		 * Each column's ground in a saved chunk: the highest block that isn't air or part of a tree,
		 * as the map in the game finds it. Unknown where the save has no finished chunk.
		 */
		private static short[] groundOf(ServerLevel level, @Nullable SavedChunk chunk) {
			short[] ground = new short[256];
			Arrays.fill(ground, UNKNOWN);
			if (chunk == null || !chunk.finished() || chunk.solid().length != 256 || chunk.states() == null) {
				return ground;
			}
			int minSection = level.getMinSection();
			int minY = level.getMinBuildHeight();
			@Nullable PalettedContainer<BlockState>[] states = chunk.states();
			for (int column = 0; column < 256; column++) {
				int x = column & 15;
				int z = column >> 4;
				int top = chunk.solid()[column];
				int y = top;
				for (; y >= Math.max(minY, top - TREE_REACH); y--) {
					int section = (y >> 4) - minSection;
					PalettedContainer<BlockState> blocks = section >= 0 && section < states.length ? states[section] : null;
					if (blocks == null) {
						continue;
					}
					BlockState state = blocks.get(x, y & 15, z);
					if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
						break;
					}
				}
				ground[column] = (short) ((y < Math.max(minY, top - TREE_REACH) ? top : y) + 1);
			}
			return ground;
		}

		/** No border to show: a file left from when there was one goes, so the page shows none. */
		static void deleteOld(Path folder) {
			try {
				Files.deleteIfExists(folder.resolve(FILE));
			} catch (IOException e) {
				Diorama.LOGGER.warn("Couldn't delete the web map's old {}", FILE, e);
			}
		}
	}
}
