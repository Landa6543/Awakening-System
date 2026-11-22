package dev.diorama;

import dev.diorama.bake.TileKey;
import dev.diorama.compat.ChunkyBorder;
import dev.diorama.drawing.Drawing;
import dev.diorama.drawing.DrawingOverlay;
import dev.diorama.render.MapViewport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

/**
 * The game's world border on a map view, the fullscreen map's or the minimap's: a line where its
 * wall meets the ground, in the colour the game draws the wall in (blue while it stands still,
 * green while it grows, red while it shrinks). The client knows the border of the dimension it's in
 * from the server, so there's nothing to set up, and a moving border moves on the map.
 *
 * <p>The map is tilted, so where a line on the ground shows depends on how high the ground is. The
 * walls the map looks along (its left and right sides) are straight lines down the picture whatever
 * the ground does. The two it looks across follow the ground: its height is read from the chunks
 * the game has loaded, and further out, where only the saved picture is left, the line carries on
 * at the nearest height it knew, or at sea level. Under a roof (the Nether) and in a cave slice
 * there's no one ground to follow, and the line is level, at the viewer's height.
 *
 * <p>An unset border is 60 million blocks wide: nothing is read or drawn until one of its sides is
 * in the view.
 *
 * <p>Where a server set a Chunky Border ({@link ChunkyBorders}) in the dimension, that's the border
 * players meet, and the add-on's client draws its wall in place of the game's: the map draws it
 * instead too, whatever its shape, in the wall's colour. Every side of it follows the ground.
 */
public final class WorldBorderOverlay {
	/** How far past the view a line is still made, in GUI pixels: its width, so an end never shows. */
	private static final float MARGIN = 8;
	/** The ground is read this often along a line, in GUI pixels; never more often than every block. */
	private static final float SAMPLE = 2;
	private static final int MAX_SAMPLES = 4096;
	/** How far down from the top of a column the ground is looked for under a tree. */
	private static final int TREE_REACH = 48;

	private final DrawingOverlay lines = new DrawingOverlay();
	/** The four sides, kept from frame to frame: north, south, west and east on the picture. */
	private final Drawing[] sides = {new Drawing(), new Drawing(), new Drawing(), new Drawing()};
	private final List<Drawing> shown = new ArrayList<>(4);
	/** The lines a Chunky Border's outline is cut into where it leaves the view, kept from frame to frame. */
	private final List<Drawing> pieces = new ArrayList<>();
	private double[] pieceXs = new double[256];
	private double[] pieceZs = new double[256];
	private float[] pieceHeights = new float[256];
	/** Which side of the outline each point is on: a point between two on its side at their height adds nothing. */
	private int[] pieceSides = new int[256];
	private float[] heights = new float[256];
	private double[] points = new double[512];
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	/** The chunk the last column read was in, so a run of columns looks it up once. */
	private @Nullable ChunkAccess chunk;
	private int chunkX;
	private int chunkZ;
	private boolean chunkRead;

	/** Draws the border of {@code level}, the dimension the player is in, where it's in {@code view}. */
	public void draw(GuiGraphics graphics, Font font, MapViewport.View view, ClientLevel level, Entity viewer) {
		List<Drawing> sides = this.sides(view, level, viewer);
		if (!sides.isEmpty()) {
			this.lines.draw(graphics, font, view, sides, null, null, false);
		}
	}

	/** The sides in the view, as lines in map space; empty when the border is out of sight. */
	public List<Drawing> sides(MapViewport.View view, ClientLevel level, Entity viewer) {
		this.shown.clear();
		ChunkyBorder.Shape chunky = ChunkyBorders.shape(level);
		if (chunky != null) {
			return this.outline(view, level, viewer, chunky, 0xFF000000 | ChunkyBorders.color());
		}
		WorldBorder border = level.getWorldBorder();
		Facing facing = Projection.facing();
		// Turned space, where the map's u is x: the border is a rectangle there too.
		//? if >=1.21.11 {
		/*// A moving border is where the game draws its wall this frame, between two ticks.
		float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		double minX = border.getMinX(partialTick);
		double minZ = border.getMinZ(partialTick);
		double maxX = border.getMaxX(partialTick);
		double maxZ = border.getMaxZ(partialTick);
		*///?} else {
		// Before 1.21.11 a moving border goes by the clock, so these are this frame's already.
		double minX = border.getMinX();
		double minZ = border.getMinZ();
		double maxX = border.getMaxX();
		double maxZ = border.getMaxZ();
		//?}
		double ax = facing.x(minX, minZ);
		double bx = facing.x(maxX, maxZ);
		double az = facing.z(minX, minZ);
		double bz = facing.z(maxX, maxZ);
		double west = Math.min(ax, bx);
		double east = Math.max(ax, bx);
		double north = Math.min(az, bz);
		double south = Math.max(az, bz);

		double margin = MARGIN / view.zoom();
		double left = view.minU() - margin;
		double right = view.minU() + view.width() / view.zoom() + margin;
		double top = view.minV() - margin;
		double bottom = view.minV() + view.height() / view.zoom() + margin;
		if (east < left || west > right) {
			return this.shown;
		}
		// Whatever the ground does, it's between the world's floor and its ceiling.
		int floor = level.getMinBuildHeight();
		int ceiling = (level.getMaxBuildHeight() - 1) + 1;
		if (Projection.v(floor, south) < top || Projection.v(ceiling, north) > bottom) {
			return this.shown;
		}
		boolean westShown = west >= left;
		boolean eastShown = east <= right;
		boolean northShown = Projection.v(floor, north) >= top;
		boolean southShown = Projection.v(ceiling, south) <= bottom;
		if (!westShown && !eastShown && !northShown && !southShown) {
			return this.shown;
		}

		boolean flatGround = view.layer() != TileKey.SURFACE || level.dimensionType().hasCeiling();
		double feet = viewer.getY();
		float flat = (float) (view.layer() != TileKey.SURFACE ? Math.min(feet, view.layer()) : feet);
		float fallback = flatGround ? flat : level.getSeaLevel();
		int color = 0xFF000000 | border.getStatus().getColor();
		this.chunkRead = false;

		// The first and last block inside, which the lines stand on.
		int northBlock = (int) Math.floor(north);
		int southBlock = (int) Math.ceil(south) - 1;
		int westBlock = (int) Math.floor(west);
		int eastBlock = (int) Math.ceil(east) - 1;
		double from = Math.max(west, left);
		double to = Math.min(east, right);
		// Where a line across ends at a corner, the line down from it starts at the same height.
		float[] corners = {Float.NaN, Float.NaN, Float.NaN, Float.NaN};
		for (int side = 0; side < 2; side++) {
			if (side == 0 ? !northShown : !southShown) {
				continue;
			}
			double z = side == 0 ? north : south;
			int blockZ = side == 0 ? northBlock : southBlock;
			int count = this.across(view, level, facing, from, to, westBlock, eastBlock, blockZ, flatGround ? flat : Float.NaN, fallback);
			if (westShown) {
				corners[side * 2] = this.heights[0];
			}
			if (eastShown) {
				corners[side * 2 + 1] = this.heights[count - 1];
			}
			// Level stretches need only their ends.
			int kept = 0;
			for (int i = 0; i < count; i++) {
				if (i > 0 && i < count - 1 && this.heights[i - 1] == this.heights[i] && this.heights[i + 1] == this.heights[i]) {
					continue;
				}
				this.points[kept * 2] = count == 1 ? from : from + (to - from) * i / (count - 1);
				this.points[kept * 2 + 1] = Projection.v(this.heights[i], z);
				kept++;
			}
			this.show(side, color, Arrays.copyOf(this.points, kept * 2), facing);
		}
		for (int side = 0; side < 2; side++) {
			if (side == 0 ? !westShown : !eastShown) {
				continue;
			}
			double u = side == 0 ? west : east;
			int blockX = side == 0 ? westBlock : eastBlock;
			float atNorth = corners[side];
			float atSouth = corners[2 + side];
			if (Float.isNaN(atNorth)) {
				atNorth = flatGround ? flat : this.groundOr(level, facing, blockX, northBlock, fallback);
			}
			if (Float.isNaN(atSouth)) {
				atSouth = flatGround ? flat : this.groundOr(level, facing, blockX, southBlock, fallback);
			}
			// Cut to the view: a side can be millions of blocks long. On a border a few blocks wide
			// with ground rising to the south, the south end is the higher one on the picture.
			double northV = Projection.v(atNorth, north);
			double southV = Projection.v(atSouth, south);
			double start = Math.max(top, Math.min(northV, southV));
			double end = Math.min(bottom, Math.max(northV, southV));
			if (start <= end) {
				this.show(2 + side, color, new double[] {u, start, u, end}, facing);
			}
		}
		this.chunk = null;
		return this.shown;
	}

	/**
	 * A border of any shape, as lines in map space: its outline where the view shows it, each point
	 * on the ground just inside it. Cut into pieces where it leaves the view and comes back.
	 */
	private List<Drawing> outline(MapViewport.View view, ClientLevel level, Entity viewer, ChunkyBorder.Shape shape, int color) {
		Facing facing = Projection.facing();
		int corners = shape.corners();
		double[] xs = new double[corners];
		double[] zs = new double[corners];
		double area = 0;
		for (int i = 0; i < corners; i++) {
			xs[i] = facing.x(shape.xs()[i], shape.zs()[i]);
			zs[i] = facing.z(shape.xs()[i], shape.zs()[i]);
		}
		for (int i = 0; i < corners; i++) {
			int j = (i + 1) % corners;
			area += xs[i] * zs[j] - xs[j] * zs[i];
		}
		double margin = MARGIN / view.zoom();
		double left = view.minU() - margin;
		double right = view.minU() + view.width() / view.zoom() + margin;
		double top = view.minV() - margin;
		double bottom = view.minV() + view.height() / view.zoom() + margin;
		// The rows of turned space the view can show ground from, whatever its height: between the
		// world's floor and its ceiling.
		double nearest = Projection.zAt(top, level.getMinBuildHeight());
		double farthest = Projection.zAt(bottom, (level.getMaxBuildHeight() - 1) + 1);

		boolean flatGround = view.layer() != TileKey.SURFACE || level.dimensionType().hasCeiling();
		double feet = viewer.getY();
		float flat = (float) (view.layer() != TileKey.SURFACE ? Math.min(feet, view.layer()) : feet);
		float fallback = flatGround ? flat : level.getSeaLevel();
		double step = Math.max(1, SAMPLE / view.zoom());
		this.chunkRead = false;

		int count = 0;
		int pieces = 0;
		// Whether the first piece starts at the first corner: then a last piece that ends there runs on into it.
		boolean firstFromStart = false;
		boolean open = false;
		double[] range = new double[2];
		// All the sides in view share this many points a frame, however many corners the server sent.
		int budget = MAX_SAMPLES * 4;
		for (int i = 0; i < corners && budget > 2; i++) {
			int j = (i + 1) % corners;
			double ax = xs[i];
			double az = zs[i];
			double dx = xs[j] - ax;
			double dz = zs[j] - az;
			double length = Math.hypot(dx, dz);
			// Where along this side it's in the view: in the view's columns, and in the rows of the world it can show.
			range[0] = 0;
			range[1] = 1;
			if (length == 0 || !clip(range, ax, dx, left, right) || !clip(range, az, dz, nearest, farthest)) {
				if (open) {
					pieces = this.endPiece(count, pieces, color, facing, fallback);
					count = 0;
					open = false;
				}
				continue;
			}
			if (open && range[0] > 0) {
				pieces = this.endPiece(count, pieces, color, facing, fallback);
				count = 0;
				open = false;
			}
			if (i == 0 && range[0] == 0) {
				firstFromStart = true;
			}
			// The ground is read a little way inside the side, in the blocks the wall stands on.
			double inX = area > 0 ? -dz / length : dz / length;
			double inZ = area > 0 ? dx / length : -dx / length;
			int samples = (int) Math.max(2, Math.min(Math.min(MAX_SAMPLES, budget), Math.ceil(length * (range[1] - range[0]) / step) + 1));
			budget -= samples;
			for (int k = open ? 1 : 0; k < samples; k++) {
				double t = range[0] + (range[1] - range[0]) * k / (samples - 1);
				double x = ax + dx * t;
				double z = az + dz * t;
				if (count == this.pieceXs.length) {
					this.grow();
				}
				this.pieceXs[count] = x;
				this.pieceZs[count] = z;
				this.pieceSides[count] = i;
				if (flatGround) {
					this.pieceHeights[count] = flat;
				} else {
					int blockX = (int) Math.floor(x + inX * 0.5);
					int blockZ = (int) Math.floor(z + inZ * 0.5);
					this.pieceHeights[count] = this.ground(level, facing.worldBlockX(blockX, blockZ), facing.worldBlockZ(blockX, blockZ));
				}
				count++;
			}
			open = true;
			if (range[1] < 1) {
				pieces = this.endPiece(count, pieces, color, facing, fallback);
				count = 0;
				open = false;
			}
		}
		if (open) {
			int before = pieces;
			pieces = this.endPiece(count, pieces, color, facing, fallback);
			if (firstFromStart && before > 0 && pieces > before) {
				// Round the first corner: the last piece and the first are one line.
				Drawing first = this.pieces.get(0);
				Drawing last = this.pieces.get(before);
				double[] joined = Arrays.copyOf(last.points, last.points.length + first.points.length - 2);
				System.arraycopy(first.points, 2, joined, last.points.length, first.points.length - 2);
				first.points = joined;
				this.shown.remove(last);
			}
		}
		this.chunk = null;
		return this.shown;
	}

	/**
	 * Narrows {@code range} (a part of a side, from 0 to 1) to where {@code start + delta * t} is
	 * between {@code low} and {@code high}; false if that's nowhere.
	 */
	private static boolean clip(double[] range, double start, double delta, double low, double high) {
		if (delta == 0) {
			return start >= low && start <= high;
		}
		double a = (low - start) / delta;
		double b = (high - start) / delta;
		range[0] = Math.max(range[0], Math.min(a, b));
		range[1] = Math.min(range[1], Math.max(a, b));
		return range[0] <= range[1];
	}

	private void grow() {
		int size = this.pieceXs.length * 2;
		this.pieceXs = Arrays.copyOf(this.pieceXs, size);
		this.pieceZs = Arrays.copyOf(this.pieceZs, size);
		this.pieceHeights = Arrays.copyOf(this.pieceHeights, size);
		this.pieceSides = Arrays.copyOf(this.pieceSides, size);
	}

	/**
	 * Makes the {@code count} points gathered into a line, the next piece, and returns how many
	 * pieces there are now. Where the ground isn't known, the nearest height known along the piece
	 * stands in, or {@code fallback}.
	 */
	private int endPiece(int count, int pieces, int color, Facing facing, float fallback) {
		if (count < 2) {
			return pieces;
		}
		float[] heights = this.pieceHeights;
		int known = -1;
		for (int i = 0; i < count; i++) {
			if (Float.isNaN(heights[i])) {
				heights[i] = known >= 0 ? heights[known] : Float.NaN;
			} else {
				if (known < 0) {
					Arrays.fill(heights, 0, i, heights[i]);
				}
				known = i;
			}
		}
		if (known < 0) {
			Arrays.fill(heights, 0, count, fallback);
		}
		double[] points = new double[count * 2];
		int kept = 0;
		for (int i = 0; i < count; i++) {
			// A point between two on its side at their height is on the line between them.
			if (i > 0 && i < count - 1 && this.pieceSides[i - 1] == this.pieceSides[i] && this.pieceSides[i + 1] == this.pieceSides[i]
				&& heights[i - 1] == heights[i] && heights[i + 1] == heights[i]) {
				continue;
			}
			points[kept * 2] = this.pieceXs[i];
			points[kept * 2 + 1] = Projection.v(heights[i], this.pieceZs[i]);
			kept++;
		}
		if (this.pieces.size() == pieces) {
			this.pieces.add(new Drawing());
		}
		Drawing line = this.pieces.get(pieces);
		line.kind = Drawing.Kind.STROKE;
		line.color = color;
		line.points = Arrays.copyOf(points, kept * 2);
		line.turns = facing.turns;
		this.shown.add(line);
		return pieces + 1;
	}

	private void show(int side, int color, double[] points, Facing facing) {
		Drawing line = this.sides[side];
		line.kind = Drawing.Kind.STROKE;
		line.color = color;
		line.points = points;
		line.turns = facing.turns;
		this.shown.add(line);
	}

	/**
	 * Fills {@link #heights} with the ground's height along a side the map looks across, from map
	 * column {@code from} to {@code to}, in the row of blocks {@code blockZ} (turned space), and
	 * returns how many. With {@code flat} not NaN it's that height all along. Where the chunks
	 * aren't loaded the nearest height known along the side stands in, or {@code fallback}.
	 */
	private int across(MapViewport.View view, ClientLevel level, Facing facing, double from, double to, int westBlock, int eastBlock, int blockZ, float flat, float fallback) {
		if (!Float.isNaN(flat)) {
			this.heights[0] = flat;
			this.heights[1] = flat;
			return 2;
		}
		double step = Math.max(1, SAMPLE / view.zoom());
		int count = (int) Math.max(2, Math.min(MAX_SAMPLES, Math.ceil((to - from) / step) + 1));
		if (this.heights.length < count) {
			this.heights = new float[count];
			this.points = new double[count * 2];
		}
		int known = -1;
		for (int i = 0; i < count; i++) {
			int blockX = (int) Math.max(westBlock, Math.min(eastBlock, Math.floor(from + (to - from) * i / (count - 1))));
			float height = this.ground(level, facing.worldBlockX(blockX, blockZ), facing.worldBlockZ(blockX, blockZ));
			if (Float.isNaN(height)) {
				// Until a known one comes, the last known; before the first, filled in below.
				height = known >= 0 ? this.heights[known] : Float.NaN;
			} else {
				if (known < 0) {
					Arrays.fill(this.heights, 0, i, height);
				}
				known = i;
			}
			this.heights[i] = height;
		}
		if (known < 0) {
			Arrays.fill(this.heights, 0, count, fallback);
		}
		return count;
	}

	private float groundOr(ClientLevel level, Facing facing, int blockX, int blockZ, float fallback) {
		float height = this.ground(level, facing.worldBlockX(blockX, blockZ), facing.worldBlockZ(blockX, blockZ));
		return Float.isNaN(height) ? fallback : height;
	}

	/** {@link #ground}, for the player's own web map's border. Client thread; {@link #forgetChunk} after. */
	public float groundAt(ClientLevel level, int x, int z) {
		return this.ground(level, x, z);
	}

	/** Lets go of the chunk {@link #groundAt} last read, and reads it again next time. */
	public void forgetChunk() {
		this.chunk = null;
		this.chunkRead = false;
	}

	/**
	 * The height of the top of the ground in a column of the world, where a player would stand: the
	 * highest block that isn't air or part of a tree, as the line belongs on the forest floor, not
	 * over the leaves. NaN where the chunk isn't loaded.
	 */
	private float ground(ClientLevel level, int x, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		if (!this.chunkRead || chunkX != this.chunkX || chunkZ != this.chunkZ) {
			this.chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
			this.chunkX = chunkX;
			this.chunkZ = chunkZ;
			this.chunkRead = true;
		}
		ChunkAccess chunk = this.chunk;
		if (chunk == null) {
			return Float.NaN;
		}
		int top = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15);
		int lowest = Math.max(level.getMinBuildHeight(), top - TREE_REACH);
		for (int y = top; y >= lowest; y--) {
			BlockState state = chunk.getBlockState(this.pos.set(x, y, z));
			if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
				return y + 1;
			}
		}
		return top + 1;
	}
}
