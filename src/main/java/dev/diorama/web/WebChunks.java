package dev.diorama.web;

import dev.diorama.Projection;
import dev.diorama.bake.TileChunks;
import dev.diorama.bake.TileKey;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import org.jspecify.annotations.Nullable;

/**
 * Which chunk each part of a web map picture shows, beside the picture as {@code <x>_<y>.png}, so
 * the page can paint land claims on the ground they cover, as the map in the game does with its
 * {@link TileChunks}. The map looks down at an angle, so one row of a picture runs over many chunks
 * from north to south, one at each height; only the ground the tracer met says which.
 *
 * <p>A grey PNG of {@link #COLUMNS} by {@link #ROWS}: a column per block across at zoom 0 (all of a
 * column is one chunk across), a row per picture row. A value is 128 plus how many chunks south the
 * ground is of where the row crosses height 64 ({@link TileChunks#base}), 0 where the picture shows
 * nothing. Written only while the map shows claims.
 */
final class WebChunks {
	static final int COLUMNS = Projection.TILE_UNITS;
	static final int ROWS = Projection.TILE_PIXELS;
	static final int NONE = 0;
	private static final int ZERO = 128;

	private WebChunks() {
	}

	static Path file(Path dimension, int zoom, int u, int v) {
		return dimension.resolve(Integer.toString(zoom)).resolve(u + "_" + v + ".png");
	}

	/** A zoom 0 picture's, from the z of the ground each pixel shows ({@link dev.diorama.bake.TileBaker#reference(TileKey, dev.diorama.bake.ChunkSource, dev.diorama.bake.Looks, int[])}). */
	static byte[] fromGround(TileKey key, int[] ground) {
		byte[] out = new byte[COLUMNS * ROWS];
		int perColumn = Projection.TILE_PIXELS / COLUMNS;
		for (int py = 0; py < ROWS; py++) {
			int base = TileChunks.base(key, py);
			for (int column = 0; column < COLUMNS; column++) {
				// The middle of the block column: its pixels all show that one block across.
				int z = ground[py * Projection.TILE_PIXELS + column * perColumn + perColumn / 2];
				out[py * COLUMNS + column] = z == Integer.MIN_VALUE ? NONE : relative(Math.floorDiv(z, 16), base);
			}
		}
		return out;
	}

	private static byte relative(int chunkZ, int base) {
		int d = chunkZ - base;
		return d < -127 || d > 127 ? NONE : (byte) (d + ZERO);
	}

	/**
	 * A zoomed-out picture's, from the four under it, (0, 0), (1, 0), (0, 1), (1, 1); null where
	 * none has any. Each value takes one of the four it covers, as the picture's pixels average them.
	 */
	static byte @Nullable [] shrink(TileKey key, byte @Nullable [][] children) {
		byte[] out = null;
		for (int i = 0; i < 4; i++) {
			byte[] child = children[i];
			if (child == null) {
				continue;
			}
			if (out == null) {
				out = new byte[COLUMNS * ROWS];
			}
			int dx = i & 1;
			int dy = i >> 1;
			TileKey childKey = new TileKey(key.layer(), key.tileU() * 2 + dx, key.tileV() * 2 + dy, key.lod() - 1);
			for (int y = 0; y < ROWS / 2; y++) {
				int childRow = 2 * y + 1;
				int childBase = TileChunks.base(childKey, childRow);
				int row = dy * ROWS / 2 + y;
				int base = TileChunks.base(key, row);
				for (int x = 0; x < COLUMNS / 2; x++) {
					int value = child[childRow * COLUMNS + 2 * x + 1] & 0xFF;
					out[row * COLUMNS + dx * COLUMNS / 2 + x] = value == NONE ? NONE : relative(childBase + value - ZERO, base);
				}
			}
		}
		return out;
	}

	/** Writes them as a PNG, or deletes the file when nothing is known. */
	static void write(Path file, byte[] values) throws IOException {
		boolean any = false;
		for (byte value : values) {
			if (value != NONE) {
				any = true;
				break;
			}
		}
		if (!any) {
			Files.deleteIfExists(file);
			return;
		}
		BufferedImage image = new BufferedImage(COLUMNS, ROWS, BufferedImage.TYPE_BYTE_GRAY);
		System.arraycopy(values, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData(), 0, values.length);
		Files.createDirectories(file.getParent());
		Path part = file.resolveSibling(file.getFileName() + ".part");
		try {
			try (OutputStream out = Files.newOutputStream(part)) {
				if (!ImageIO.write(image, "png", out)) {
					throw new IOException("this Java has no PNG writer");
				}
			}
			Files.move(part, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException | RuntimeException e) {
			// Out of disk, say: no half-written file stays behind.
			try {
				Files.deleteIfExists(part);
			} catch (IOException ignored) {
			}
			throw e;
		}
	}

	/** What a file holds, or null if there's none to read. */
	static byte @Nullable [] read(Path file) throws IOException {
		if (!Files.exists(file)) {
			return null;
		}
		BufferedImage image = ImageIO.read(file.toFile());
		if (image == null || image.getWidth() != COLUMNS || image.getHeight() != ROWS || image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
			return null;
		}
		return ((DataBufferByte) image.getRaster().getDataBuffer()).getData().clone();
	}
}
