package com.soybe.awakeningsystem.client.map;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Motor de persistencia ultraliviano y de alto rendimiento para el Mapa Táctico de Cazador.
 * Guarda en disco (.minecraft/awakening_map/<mundo>/<dimension>/) los mosaicos explorados en formato de regiones comprimidas
 * (16x16 chunks por región = 256x256 bloques), garantizando 0 lag, almacenamiento mínimo y que el mapa conserve permanentemente
 * el terreno descubierto entre reinicios de Minecraft sin requerir volver a visitar los chunks.
 */
public final class WorldMapChunkCache {
    private static final byte MAGIC_1 = 0x41; // 'A'
    private static final byte MAGIC_2 = 0x57; // 'W'
    private static final byte VERSION = 4;

    private static final Map<String, RegionData> REGION_CACHE = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService ASYNC_SAVER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Awakening-MapCache-Saver");
        t.setDaemon(true);
        return t;
    });

    static {
        // Guardado periódico en segundo plano cada 3 segundos si hay regiones modificadas
        ASYNC_SAVER.scheduleWithFixedDelay(() -> {
            try {
                flushDirtyRegions(false);
            } catch (Throwable t) {
                // Silencioso para evitar spam de consola
            }
        }, 3, 3, TimeUnit.SECONDS);

        // Guardado de emergencia al cerrar la JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                flushDirtyRegions(true);
            } catch (Throwable ignored) {}
        }));
    }

    private WorldMapChunkCache() {}

    public static class RegionData {
        public final String dim;
        public final int rx;
        public final int rz;
        public final int[][] chunks = new int[256][];
        public final BitSet present = new BitSet(256);
        public volatile boolean dirty = false;
        public volatile long lastAccessTime = System.currentTimeMillis();

        public RegionData(String dim, int rx, int rz) {
            this.dim = dim;
            this.rx = rx;
            this.rz = rz;
        }
    }

    /**
     * Obtiene los píxeles (16x16 = 256 ints) de un chunk previamente explorado y guardado en disco o memoria.
     */
    public static int[] getChunkPixels(String dim, int cx, int cz) {
        if (dim == null || dim.isBlank()) return null;
        int rx = cx >> 4;
        int rz = cz >> 4;
        int lx = cx & 15;
        int lz = cz & 15;
        int slot = (lz << 4) | lx;

        String key = dim + "#" + rx + "#" + rz;
        RegionData reg = REGION_CACHE.computeIfAbsent(key, k -> loadRegionFromDisk(dim, rx, rz));
        reg.lastAccessTime = System.currentTimeMillis();

        synchronized (reg) {
            if (reg.present.get(slot)) {
                return reg.chunks[slot];
            }
        }
        return null;
    }

    /**
     * Almacena los píxeles muestreados de un chunk en la región y la marca como pendiente de guardado en disco.
     */
    public static void putChunkPixels(String dim, int cx, int cz, int[] pixels) {
        if (dim == null || dim.isBlank() || pixels == null || pixels.length < 256) return;
        int rx = cx >> 4;
        int rz = cz >> 4;
        int lx = cx & 15;
        int lz = cz & 15;
        int slot = (lz << 4) | lx;

        String key = dim + "#" + rx + "#" + rz;
        RegionData reg = REGION_CACHE.computeIfAbsent(key, k -> loadRegionFromDisk(dim, rx, rz));
        reg.lastAccessTime = System.currentTimeMillis();

        synchronized (reg) {
            int[] stored = reg.chunks[slot];
            if (stored == null) {
                stored = new int[256];
                reg.chunks[slot] = stored;
            }
            System.arraycopy(pixels, 0, stored, 0, 256);
            reg.present.set(slot, true);
            reg.dirty = true;
        }

        // Limpieza de memoria LRU si hay más de 96 regiones en RAM
        if (REGION_CACHE.size() > 96) {
            cleanupMemoryCache();
        }
    }

    /**
     * Guarda inmediatamente todas las regiones modificadas en disco.
     */
    public static void flushAll() {
        CompletableFuture.runAsync(() -> flushDirtyRegions(true), ASYNC_SAVER);
    }

    /**
     * Limpia completamente la caché de regiones en memoria RAM para forzar recarga fresca.
     */
    public static void clearAllMemoryCache() {
        REGION_CACHE.clear();
    }

    private static synchronized void flushDirtyRegions(boolean forceAll) {
        for (RegionData reg : REGION_CACHE.values()) {
            if (!reg.dirty) continue;

            File file = getRegionFile(reg.dim, reg.rx, reg.rz);
            if (file == null) continue;

            byte[] buffer;
            synchronized (reg) {
                buffer = serializeRegion(reg);
                reg.dirty = false;
            }

            if (buffer != null) {
                try {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();
                    File tempFile = new File(parent, file.getName() + ".tmp");
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(buffer);
                    }
                    if (file.exists()) file.delete();
                    tempFile.renameTo(file);
                } catch (IOException e) {
                    // Marcar como dirty de nuevo para reintentar
                    reg.dirty = true;
                }
            }
        }
    }

    private static byte[] serializeRegion(RegionData reg) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(baos)))) {
                dos.writeByte(MAGIC_1);
                dos.writeByte(MAGIC_2);
                dos.writeByte(VERSION);

                byte[] bitmask = reg.present.toByteArray();
                dos.writeShort(bitmask.length);
                dos.write(bitmask);

                for (int i = 0; i < 256; i++) {
                    if (reg.present.get(i) && reg.chunks[i] != null) {
                        int[] px = reg.chunks[i];
                        for (int p = 0; p < 256; p++) {
                            dos.writeInt(px[p]);
                        }
                    }
                }
            }
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    private static RegionData loadRegionFromDisk(String dim, int rx, int rz) {
        RegionData reg = new RegionData(dim, rx, rz);
        File file = getRegionFile(dim, rx, rz);
        if (file == null || !file.exists() || file.length() < 5) {
            return reg;
        }

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new InflaterInputStream(new FileInputStream(file))))) {
            byte m1 = dis.readByte();
            byte m2 = dis.readByte();
            byte ver = dis.readByte();

            if (m1 == MAGIC_1 && m2 == MAGIC_2 && ver == VERSION) {
                int bitmaskLen = dis.readShort();
                byte[] bitmaskBytes = new byte[bitmaskLen];
                dis.readFully(bitmaskBytes);
                BitSet bitSet = BitSet.valueOf(bitmaskBytes);

                for (int i = 0; i < 256; i++) {
                    if (bitSet.get(i)) {
                        int[] px = new int[256];
                        for (int p = 0; p < 256; p++) {
                            px[p] = dis.readInt();
                        }
                        reg.chunks[i] = px;
                        reg.present.set(i, true);
                    }
                }
            }
        } catch (Throwable ignored) {
            // Archivo incompleto o versión antigua: se omite pacíficamente
        }

        return reg;
    }

    private static void cleanupMemoryCache() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<String, RegionData>> it = REGION_CACHE.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, RegionData> entry = it.next();
            RegionData r = entry.getValue();
            if (!r.dirty && (now - r.lastAccessTime > 60000L)) { // 1 minuto sin acceso
                it.remove();
            }
        }
    }

    private static File getRegionFile(String dim, int rx, int rz) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameDirectory == null) return null;

        File baseDir = new File(mc.gameDirectory, "awakening_map");
        String worldId = getWorldIdentifier();
        String safeDim = sanitize(dim);

        File dimDir = new File(new File(baseDir, worldId), safeDim);
        return new File(dimDir, "r." + rx + "." + rz + ".bin");
    }

    public static String getWorldIdentifier() {
        Minecraft mc = Minecraft.getInstance();
        try {
            if (mc.getSingleplayerServer() != null) {
                String name = mc.getSingleplayerServer().getWorldData().getLevelName();
                return "sp_" + sanitize(name);
            } else if (mc.getCurrentServer() != null) {
                return "mp_" + sanitize(mc.getCurrentServer().ip);
            }
        } catch (Throwable ignored) {}
        return "world_default";
    }

    private static String sanitize(String input) {
        if (input == null || input.isBlank()) return "unknown";
        return input.replace(':', '_').replace('/', '_').replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
