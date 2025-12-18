package com.vid.compressor;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.vid.compressor.entropy.HuffmanBitWriter;
import com.vid.compressor.entropy.SymbolCollector;

public class QuadtreeUtils {

    /* ================= CONSTANTS ================= */

    public static final int CTU_SIZE = 256;
    public static final int MIN_CU   = 8;

    /* ================= SYMBOL MAPPING ================= */

    public static int sizeToSymbol(int size) {
        return switch (size) {
            case 8   -> 2;
            case 16  -> 3;
            case 32  -> 4;
            case 64  -> 5;
            case 128 -> 6;
            case 256 -> 7;
            default -> throw new IllegalArgumentException("Invalid CU size: " + size);
        };
    }

    public static int avgToSymbol(int avg) {
        return 8 + (avg >> 3);
    }

    /* ================= METRICS ================= */

    public static int avg(int sx, int sy, int size, int[][] f) {
        int sum = 0;
        for (int y = sy; y < sy + size; y++)
            for (int x = sx; x < sx + size; x++)
                sum += f[y][x];
        return sum / (size * size);
    }

    public static double variance(int sx, int sy, int size, int[][] f) {
        int sum = 0, sum2 = 0;
        for (int y = sy; y < sy + size; y++)
            for (int x = sx; x < sx + size; x++) {
                int p = f[y][x];
                sum += p;
                sum2 += p * p;
            }
        double mean = sum / (double)(size * size);
        return (sum2 / (double)(size * size)) - mean * mean;
    }

    public static double varianceThreshold(int size) {
        return 100.0 / size;
    }

    /* ================= CACHE KEY ================= */

    public static final class Key {
        public final int sx, sy, size, avg, vb;

        public Key(int sx, int sy, int size, int avg, int vb) {
            this.sx = sx;
            this.sy = sy;
            this.size = size;
            this.avg = avg;
            this.vb = vb;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sx, sy, size, avg, vb);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key k)) return false;
            return sx == k.sx && sy == k.sy &&
                   size == k.size &&
                   avg == k.avg &&
                   vb == k.vb;
        }
    }

    /* ================= BASELINE (RECURSIVE) ================= */

    public static void collectBaseline(
            int sx, int sy, int size,
            int[][] frame,
            SymbolCollector sc) {

        boolean split =
                size > MIN_CU &&
                variance(sx, sy, size, frame) > varianceThreshold(size);

        sc.add(split ? 1 : 0);

        if (split) {
            int h = size / 2;
            collectBaseline(sx, sy, h, frame, sc);
            collectBaseline(sx + h, sy, h, frame, sc);
            collectBaseline(sx, sy + h, h, frame, sc);
            collectBaseline(sx + h, sy + h, h, frame, sc);
        } else {
            sc.add(sizeToSymbol(size));
            sc.add(avgToSymbol(avg(sx, sy, size, frame)));
        }
    }

    public static void encodeBaseline(
            int sx, int sy, int size,
            int[][] frame,
            HuffmanBitWriter writer) throws IOException {

        boolean split =
                size > MIN_CU &&
                variance(sx, sy, size, frame) > varianceThreshold(size);

        writer.writeSymbol(split ? 1 : 0);

        if (split) {
            int h = size / 2;
            encodeBaseline(sx, sy, h, frame, writer);
            encodeBaseline(sx + h, sy, h, frame, writer);
            encodeBaseline(sx, sy + h, h, frame, writer);
            encodeBaseline(sx + h, sy + h, h, frame, writer);
        } else {
            writer.writeSymbol(sizeToSymbol(size));
            writer.writeSymbol(avgToSymbol(avg(sx, sy, size, frame)));
        }
    }

    /* ================= PROPOSED (CACHE + FALLBACK) ================= */

    public static void collectProposed(
            int sx, int sy, int size,
            int[][] frame,
            Map<Key, Boolean> cache,
            SymbolCollector sc) {

        int avg = avg(sx, sy, size, frame);
        int vb  = (int)(variance(sx, sy, size, frame) / 4);

        Key key = new Key(sx, sy, size, avg, vb);

        boolean split = cache.computeIfAbsent(
                key,
                k -> size > MIN_CU &&
                     variance(sx, sy, size, frame) > varianceThreshold(size)
        );

        sc.add(split ? 1 : 0);

        if (split) {
            int h = size / 2;
            collectProposed(sx, sy, h, frame, cache, sc);
            collectProposed(sx + h, sy, h, frame, cache, sc);
            collectProposed(sx, sy + h, h, frame, cache, sc);
            collectProposed(sx + h, sy + h, h, frame, cache, sc);
        } else {
            sc.add(sizeToSymbol(size));
            sc.add(avgToSymbol(avg));
        }
    }

    public static void encodeProposed(
            int sx, int sy, int size,
            int[][] frame,
            Map<Key, Boolean> cache,
            HuffmanBitWriter writer) throws IOException {

        int avg = avg(sx, sy, size, frame);
        int vb  = (int)(variance(sx, sy, size, frame) / 4);

        Key key = new Key(sx, sy, size, avg, vb);

        Boolean split = cache.get(key);

        /* ===== CACHE MISS → BASELINE FALLBACK ===== */
        if (split == null) {
            split = size > MIN_CU &&
                    variance(sx, sy, size, frame) > varianceThreshold(size);
            cache.put(key, split);
        }

        writer.writeSymbol(split ? 1 : 0);

        if (split) {
            int h = size / 2;
            encodeProposed(sx, sy, h, frame, cache, writer);
            encodeProposed(sx + h, sy, h, frame, cache, writer);
            encodeProposed(sx, sy + h, h, frame, cache, writer);
            encodeProposed(sx + h, sy + h, h, frame, cache, writer);
        } else {
            writer.writeSymbol(sizeToSymbol(size));
            writer.writeSymbol(avgToSymbol(avg));
        }
    }
}
