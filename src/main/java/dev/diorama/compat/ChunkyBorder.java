package dev.diorama.compat;

import dev.diorama.Diorama;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Chunky Border, pop4959's add-on to the Chunky pregenerator: a world border of its own, in one of
 * Chunky's shapes (square, circle, star and so on), which servers set instead of the game's. It never
 * changes the game's world border. Its server tells clients the shape on a channel of its own,
 * {@code chunky:border}, the same on Fabric, NeoForge, Forge and Paper, and its client mod draws the
 * wall there in place of the game's.
 *
 * <p>This is the part that names no client class: the shape, reading the channel's message, and
 * reading a border from the add-on itself, on its server or its client. Diorama doesn't build
 * against it. The names read here have stayed the same from its first release with a client (1.1)
 * to 1.2.75; if one isn't there, the add-on counts as not installed.
 */
public final class ChunkyBorder {
	/** The colour the add-on's own client gives the wall unless set otherwise: the game's blue. */
	public static final int BLUE = 0x20A0FF;
	/** The version of the channel's message this reads, its first int. */
	private static final int VERSION = 0;
	/**
	 * How far a curve's outline may stray inside the true curve, in blocks: well under a pixel at the
	 * map's closest zoom, and a circle 10,000 blocks across still takes under 1,000 corners.
	 */
	private static final double CURVE_ERROR = 0.05;
	private static final int MAX_CURVE_CORNERS = 4096;

	private static boolean serverLooked;
	private static @Nullable Method provider;
	private static @Nullable Method borderOf;
	private static @Nullable Method shapeOf;

	private ChunkyBorder() {
	}

	/**
	 * A border's outline in the world: x and z of each corner in turn, the last joined back to the
	 * first. A curve comes as many corners. Never fewer than three.
	 */
	public record Shape(double[] xs, double[] zs) {
		public int corners() {
			return this.xs.length;
		}

		/** Whether two shapes have the same corners. */
		public boolean same(@Nullable Shape other) {
			return other != null && Arrays.equals(this.xs, other.xs) && Arrays.equals(this.zs, other.zs);
		}
	}

	/** A polygon's corners, or null if it has too few to enclose anything. */
	static @Nullable Shape polygon(double[] xs, double[] zs) {
		if (xs.length < 3 || xs.length != zs.length) {
			return null;
		}
		for (int i = 0; i < xs.length; i++) {
			// Finite, and no farther out than twice the world's edge.
			if (!(Math.abs(xs[i]) <= 6.0E7) || !(Math.abs(zs[i]) <= 6.0E7)) {
				return null;
			}
		}
		return new Shape(xs.clone(), zs.clone());
	}

	/** An ellipse as a polygon close enough to look round at the map's closest zoom. */
	static @Nullable Shape ellipse(double centerX, double centerZ, double radiusX, double radiusZ) {
		double radius = Math.max(radiusX, radiusZ);
		if (!(radiusX > 0) || !(radiusZ > 0) || !Double.isFinite(radius) || !Double.isFinite(centerX) || !Double.isFinite(centerZ)) {
			return null;
		}
		// Each side of a regular polygon falls short of its circle by r(1 - cos(pi / n)) at its middle.
		int corners = radius <= CURVE_ERROR ? 16 : (int) Math.ceil(Math.PI / Math.acos(1 - CURVE_ERROR / radius));
		corners = Math.max(16, Math.min(MAX_CURVE_CORNERS, corners));
		double[] xs = new double[corners];
		double[] zs = new double[corners];
		for (int i = 0; i < corners; i++) {
			double angle = 2 * Math.PI * i / corners;
			xs[i] = centerX + radiusX * Math.cos(angle);
			zs[i] = centerZ + radiusZ * Math.sin(angle);
		}
		return new Shape(xs, zs);
	}

	/** One message on the channel: the dimension it's for, by ID, and its border, null for none. */
	public record Message(String dimension, @Nullable Shape shape) {
	}

	/**
	 * Reads a message as the add-on writes it (Java's data stream: an int version, the dimension ID,
	 * a byte for the kind of shape, 0 none, 1 polygon, 2 ellipse, then its numbers). Null if it isn't
	 * one this can read.
	 */
	public static @Nullable Message read(byte[] bytes) {
		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
			if (in.readInt() != VERSION) {
				return null;
			}
			String dimension = in.readUTF();
			int kind = in.readByte();
			Shape shape = null;
			if (kind == 1) {
				int corners = in.readInt();
				// Each corner is 16 bytes: a count past what's left is a broken message. The add-on's own
				// shapes have ten corners at most; a server sending thousands isn't describing a border.
				if (corners < 0 || corners > in.available() / 16 || corners > MAX_CURVE_CORNERS) {
					return null;
				}
				double[] xs = new double[corners];
				double[] zs = new double[corners];
				for (int i = 0; i < corners; i++) {
					xs[i] = in.readDouble();
					zs[i] = in.readDouble();
				}
				shape = polygon(xs, zs);
			} else if (kind == 2) {
				shape = ellipse(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
			}
			return new Message(dimension, shape);
		} catch (IOException | RuntimeException e) {
			return null;
		}
	}

	/**
	 * A shape object of the add-on's own client (its {@code PolygonBorderShape} or
	 * {@code EllipseBorderShape}) as a {@link Shape}; null for anything else.
	 */
	public static @Nullable Shape ofClientShape(@Nullable Object shape) {
		if (shape == null) {
			return null;
		}
		try {
			Class<?> type = shape.getClass();
			if (type.getSimpleName().equals("PolygonBorderShape")) {
				return polygon((double[]) type.getMethod("getPointsX").invoke(shape), (double[]) type.getMethod("getPointsZ").invoke(shape));
			}
			if (type.getSimpleName().equals("EllipseBorderShape")) {
				return ellipse(number(type, shape, "getCenterX"), number(type, shape, "getCenterZ"), number(type, shape, "getRadiusX"), number(type, shape, "getRadiusZ"));
			}
		} catch (ReflectiveOperationException | RuntimeException e) {
			Diorama.LOGGER.debug("Couldn't read Chunky Border's shape {}", shape, e);
		}
		return null;
	}

	private static double number(Class<?> type, Object shape, String getter) throws ReflectiveOperationException {
		return ((Number) type.getMethod(getter).invoke(shape)).doubleValue();
	}

	/**
	 * A shape of Chunky's (an {@code AbstractPolygon}, with its corners, or an
	 * {@code AbstractEllipse}, with its centre and radii) as a {@link Shape}, as the add-on turns it
	 * into its message; null for anything else.
	 */
	private static @Nullable Shape ofChunkyShape(@Nullable Object shape) {
		if (shape == null) {
			return null;
		}
		try {
			for (Class<?> type = shape.getClass(); type != null; type = type.getSuperclass()) {
				if (type.getName().equals("org.popcraft.chunky.shape.AbstractPolygon")) {
					List<?> points = (List<?>) type.getMethod("points").invoke(shape);
					double[] xs = new double[points.size()];
					double[] zs = new double[points.size()];
					for (int i = 0; i < xs.length; i++) {
						xs[i] = vector(points.get(i), "getX");
						zs[i] = vector(points.get(i), "getZ");
					}
					return polygon(xs, zs);
				}
				if (type.getName().equals("org.popcraft.chunky.shape.AbstractEllipse")) {
					Object center = type.getMethod("center").invoke(shape);
					Object radii = type.getMethod("radii").invoke(shape);
					return ellipse(vector(center, "getX"), vector(center, "getZ"), vector(radii, "getX"), vector(radii, "getZ"));
				}
			}
		} catch (ReflectiveOperationException | RuntimeException e) {
			Diorama.LOGGER.debug("Couldn't read Chunky's shape {}", shape, e);
		}
		return null;
	}

	private static double vector(Object vector, String getter) throws ReflectiveOperationException {
		return ((Number) vector.getClass().getMethod(getter).invoke(vector)).doubleValue();
	}

	/**
	 * The border of each of {@code dimensions} (IDs) that has one, as the add-on on this server has it:
	 * empty if it isn't installed or hasn't started yet. Server thread: the add-on makes a border's
	 * shape the first time it's asked for.
	 */
	public static Map<String, Shape> serverBorders(Iterable<String> dimensions) {
		Map<String, Shape> borders = new HashMap<>();
		if (!serverInstalled()) {
			return borders;
		}
		Object chunkyBorder;
		try {
			chunkyBorder = provider.invoke(null);
		} catch (ReflectiveOperationException | RuntimeException e) {
			// Not started yet: it throws until the server has.
			return borders;
		}
		for (String dimension : dimensions) {
			try {
				Optional<?> data = (Optional<?>) borderOf.invoke(chunkyBorder, dimension);
				if (data.isPresent()) {
					Shape shape = ofChunkyShape(shapeOf.invoke(data.get()));
					if (shape != null) {
						borders.put(dimension, shape);
					}
				}
			} catch (ReflectiveOperationException | RuntimeException e) {
				Diorama.LOGGER.debug("Couldn't read Chunky Border's border of {}", dimension, e);
			}
		}
		return borders;
	}

	/** Whether the add-on is installed on this server, with everything {@link #serverBorders} reads. */
	public static synchronized boolean serverInstalled() {
		if (!serverLooked) {
			serverLooked = true;
			try {
				ClassLoader loader = ChunkyBorder.class.getClassLoader();
				Class<?> providerClass = Class.forName("org.popcraft.chunkyborder.ChunkyBorderProvider", false, loader);
				Class<?> mainClass = Class.forName("org.popcraft.chunkyborder.ChunkyBorder", false, loader);
				Class<?> dataClass = Class.forName("org.popcraft.chunkyborder.BorderData", false, loader);
				Method get = providerClass.getMethod("get");
				Method border = mainClass.getMethod("getBorder", String.class);
				Method shape = dataClass.getMethod("getBorder");
				provider = get;
				borderOf = border;
				shapeOf = shape;
				Diorama.LOGGER.info("Chunky Border is installed: the web map shows its border");
			} catch (ClassNotFoundException e) {
				// Not installed.
			} catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
				Diorama.LOGGER.warn("Chunky Border is installed, but not as Diorama knows it, so the web map can't show its border", e);
			}
		}
		return provider != null;
	}
}
