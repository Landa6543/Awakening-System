package com.soybe.awakeningsystem.tower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/** Debug pictures of the Great Tower (front view and floor plans), for checking the design without a client. */
final class GreatTowerSnapshot {
    private GreatTowerSnapshot() {}

    static void write(ServerLevel level, BlockPos origin, Direction front, Path dir) throws IOException {
        Direction right = front.getClockWise();
        int half = 40, height = Math.min(260, level.getMaxBuildHeight() - origin.getY() - 1);
        // front view: looking at the door, first solid block along the line of sight
        BufferedImage img = new BufferedImage(half * 2 + 1, height + 6, BufferedImage.TYPE_INT_RGB);
        for (int u = -half; u <= half; u++) {
            for (int y = -5; y <= height; y++) {
                int rgb = 0x9BC4FF;
                for (int depth = half; depth >= -half; depth--) {
                    BlockPos p = origin.relative(right, u).relative(front, depth).above(y);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir()) {
                        rgb = shade(s.getMapColor(level, p), depth);
                        break;
                    }
                }
                img.setRGB(u + half, height - y, rgb);
            }
        }
        ImageIO.write(scale(img, 3), "png", dir.resolve("front.png").toFile());
        // floor plans of the vestibule
        for (int y : new int[]{0, 1, 3, 6}) {
            BufferedImage plan = new BufferedImage(half * 2 + 1, half * 2 + 1, BufferedImage.TYPE_INT_RGB);
            for (int u = -half; u <= half; u++) {
                for (int v = -half; v <= half; v++) {
                    BlockPos p = origin.relative(right, u).relative(front, -v).above(y);
                    BlockState s = level.getBlockState(p);
                    plan.setRGB(u + half, v + half, s.isAir() ? 0x101010 : s.getMapColor(level, p).col);
                }
            }
            ImageIO.write(scale(plan, 6), "png", dir.resolve("plan_y" + y + ".png").toFile());
        }
    }

    /** Elevations from the south and from the east, and a top view, of a box around a point. */
    static void view(ServerLevel level, BlockPos c, int half, int below, int above, Path dir, String name) throws IOException {
        for (int x = -half; x <= half; x += 16) for (int z = -half; z <= half; z += 16) level.getChunk((c.getX() + x) >> 4, (c.getZ() + z) >> 4);
        int h = below + above + 1, w = half * 2 + 1;
        for (int side = 0; side < 2; side++) {
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            for (int u = -half; u <= half; u++) {
                for (int y = -below; y <= above; y++) {
                    int rgb = 0x9BC4FF;
                    for (int d = half; d >= -half; d--) {
                        BlockPos p = side == 0 ? c.offset(u, y, d) : c.offset(d, y, -u);
                        BlockState s = level.getBlockState(p);
                        if (!s.isAir()) { rgb = shade(s.getMapColor(level, p), d * 40 / Math.max(1, half)); break; }
                    }
                    img.setRGB(u + half, above - y, rgb);
                }
            }
            ImageIO.write(scale(img, Math.max(1, 600 / Math.max(w, h))), "png", dir.resolve(name + (side == 0 ? "_south.png" : "_east.png")).toFile());
        }
        BufferedImage top = new BufferedImage(w, w, BufferedImage.TYPE_INT_RGB);
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                int rgb = 0x101010;
                for (int y = above; y >= -below; y--) {
                    BlockPos p = c.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir()) { rgb = shade(s.getMapColor(level, p), (y + below) * 80 / Math.max(1, h) - 40); break; }
                }
                top.setRGB(x + half, z + half, rgb);
            }
        }
        ImageIO.write(scale(top, Math.max(1, 600 / w)), "png", dir.resolve(name + "_top.png").toFile());
    }

    /** Plain north-up plans around a point; villagers and NPCs show as red dots. */
    static void plan(ServerLevel level, BlockPos c, int half, int[] ys, Path dir, String name) throws IOException {
        for (int x = -half; x <= half; x++) for (int z = -half; z <= half; z++) level.getChunk((c.getX() + x) >> 4, (c.getZ() + z) >> 4);
        java.util.List<net.minecraft.world.entity.Entity> npcs = level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,
                new net.minecraft.world.phys.AABB(c).inflate(half, 6, half), e -> e instanceof com.soybe.awakeningsystem.entity.SeriesNpcEntity);
        for (int y : ys) {
            BufferedImage img = new BufferedImage(half * 2 + 1, half * 2 + 1, BufferedImage.TYPE_INT_RGB);
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    BlockPos p = c.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    img.setRGB(x + half, z + half, s.isAir() ? 0x101010 : s.getMapColor(level, p).col);
                }
            }
            for (net.minecraft.world.entity.Entity e : npcs) {
                int x = e.getBlockX() - c.getX() + half, z = e.getBlockZ() - c.getZ() + half;
                if (x >= 0 && z >= 0 && x <= half * 2 && z <= half * 2) img.setRGB(x, z, 0xFF0000);
            }
            ImageIO.write(scale(img, 10), "png", dir.resolve(name + "_y" + y + ".png").toFile());
        }
        StringBuilder sb = new StringBuilder();
        for (net.minecraft.world.entity.Entity e : npcs) sb.append(e.getType().toShortString()).append(' ').append(e.blockPosition().toShortString()).append('\n');
        java.nio.file.Files.writeString(dir.resolve(name + "_npcs.txt"), sb.toString());
    }

    private static int shade(MapColor c, int depth) {
        int col = c.col;
        double f = 0.65D + 0.35D * (depth + 40) / 80.0D;
        int r = (int) (((col >> 16) & 255) * f), g = (int) (((col >> 8) & 255) * f), b = (int) ((col & 255) * f);
        return (r << 16) | (g << 8) | b;
    }

    private static BufferedImage scale(BufferedImage src, int k) {
        BufferedImage out = new BufferedImage(src.getWidth() * k, src.getHeight() * k, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < out.getWidth(); x++)
            for (int y = 0; y < out.getHeight(); y++)
                out.setRGB(x, y, src.getRGB(x / k, y / k));
        return out;
    }
}
