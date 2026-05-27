package com.vid.compressor;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.vid.compressor.entropy.HuffmanBitWriter;
import com.vid.compressor.entropy.SymbolCollector;

public class QuadtreeUtils {


    public static final int CTU_SIZE = 256;
    public static final int MIN_CU   = 8;

	private static int clamp(int v) {
    		return Math.max(0, Math.min(255, v));
	}
	public static int subAvg(
        	int sx,
        	int sy,
        	int size,
        	int[][] f) {

    		int sum = 0;

    		for (int y = sy; y < sy + size; y++) {
        		for (int x = sx; x < sx + size; x++) {
            			sum += f[y][x];
        			}
    		}

    return sum / (size * size);
}
public static int sizeToSymbol(int size) {

    return switch (size) {

        case 4   -> 3;
        case 8  -> 4;
        case 16  -> 5;
        case 32  -> 6;
        case 64 -> 7;
        case 128 -> 8;
	case 256 -> 9;
        default ->
            throw new IllegalArgumentException(
                "Invalid CU size: " + size
            );
    };
}
    public static int avgToSymbol(int avg) {
        return 9 + avg;
    }


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
        return 1000.0 / size;
    }

public static final class Key {

    public final int sx;
    public final int sy;
    public final int size;

    public Key(int sx, int sy, int size) {

        this.sx = sx;
        this.sy = sy;
        this.size = size;
    }

    @Override
    public int hashCode() {

        return Objects.hash(sx, sy, size);
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Key k)) {
            return false;
        }

        return sx == k.sx &&
               sy == k.sy &&
               size == k.size; 
    }
}


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
		 int avg = avg(sx, sy, size, frame);

    sc.add(sizeToSymbol(size));
    sc.add(avgToSymbol(avg));

    int h = size / 2;

    int r1 = subAvg(sx, sy, h, frame) - avg;
    int r2 = subAvg(sx + h, sy, h, frame) - avg;
    int r3 = subAvg(sx, sy + h, h, frame) - avg;
    int r4 = subAvg(sx + h, sy + h, h, frame) - avg;

    sc.add(avgToSymbol(clamp(r1 + 128)));
    sc.add(avgToSymbol(clamp(r2 + 128)));
    sc.add(avgToSymbol(clamp(r3 + 128)));
    sc.add(avgToSymbol(clamp(r4 + 128)));
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
	    int avg = avg(sx, sy, size, frame);
            writer.writeSymbol(sizeToSymbol(size));
            writer.writeSymbol(avgToSymbol(avg));
	    int h = size/2;

	    int r1 = subAvg(sx, sy, h, frame) - avg;
	    int r2 = subAvg(sx + h, sy, h, frame) - avg;
	    int r3 = subAvg(sx, sy + h, h, frame) - avg;
	    int r4 = subAvg(sx + h, sy + h, h, frame) - avg;

	    writer.writeSymbol(avgToSymbol(clamp(r1 + 128)));
	    writer.writeSymbol(avgToSymbol(clamp(r2 + 128)));
	    writer.writeSymbol(avgToSymbol(clamp(r3 + 128)));
	    writer.writeSymbol(avgToSymbol(clamp(r4 + 128)));
        	}
    	}

public static void collectProposed(
        int sx, int sy, int size,
        int[][] frame,
        Map<Key, Boolean> cache,
        SymbolCollector sc) {

    int avg = avg(sx, sy, size, frame);

    Key key = new Key(sx, sy, size);

    boolean actualSplit =
            size > MIN_CU &&
            variance(sx, sy, size, frame) > varianceThreshold(size);

    Boolean predicted = cache.get(key);

    if (predicted != null && predicted == actualSplit) {

        sc.add(2);

    } else {

        sc.add(actualSplit ? 1 : 0);
        cache.put(key, actualSplit);
    }

    if (actualSplit) {

        int h = size / 2;

        collectProposed(sx, sy, h, frame, cache, sc);
        collectProposed(sx + h, sy, h, frame, cache, sc);
        collectProposed(sx, sy + h, h, frame, cache, sc);
        collectProposed(sx + h, sy + h, h, frame, cache, sc);

    } else {
    sc.add(sizeToSymbol(size));
    sc.add(avgToSymbol(avg));

    int h = size / 2;

    int r1 = subAvg(sx, sy, h, frame) - avg;
    int r2 = subAvg(sx + h, sy, h, frame) - avg;
    int r3 = subAvg(sx, sy + h, h, frame) - avg;
    int r4 = subAvg(sx + h, sy + h, h, frame) - avg;

    sc.add(avgToSymbol(clamp(r1 + 128)));
    sc.add(avgToSymbol(clamp(r2 + 128)));
    sc.add(avgToSymbol(clamp(r3 + 128)));
    sc.add(avgToSymbol(clamp(r4 + 128)));
    int step = Math.max(1, size / 4);
    for (int yy = sy; yy < sy + size; yy += step) {

    	for (int xx = sx; xx < sx + size; xx += step) {

        int localSum = 0;
        int count = 0;

        for (int y2 = yy;
             y2 < Math.min(yy + step, sy + size);
             y2++) {

            for (int x2 = xx;
                 x2 < Math.min(xx + step, sx + size);
                 x2++) {

                localSum += frame[y2][x2];
                count++;
            }
        }

        int tex = localSum / count;

        sc.add(300 + tex);
    		}
	}
    }
}
public static void encodeProposed(
        int sx, int sy, int size,
        int[][] frame,
        Map<Key, Boolean> cache,
        HuffmanBitWriter writer) throws IOException {

    int avg = avg(sx, sy, size, frame);

    Key key = new Key(sx, sy, size);

    boolean actualSplit =
            size > MIN_CU &&
            variance(sx, sy, size, frame) > varianceThreshold(size);

    Boolean predicted = cache.get(key);

    if (predicted != null && predicted == actualSplit) {

        writer.writeSymbol(2);

    } else {

        writer.writeSymbol(actualSplit ? 1 : 0);
        cache.put(key, actualSplit);
    }

    if (actualSplit) {

        int h = size / 2;

        encodeProposed(sx, sy, h, frame, cache, writer);
        encodeProposed(sx + h, sy, h, frame, cache, writer);
        encodeProposed(sx, sy + h, h, frame, cache, writer);
        encodeProposed(sx + h, sy + h, h, frame, cache, writer);

    } else {
	int avg1 = avg(sx, sy, size, frame);
        writer.writeSymbol(sizeToSymbol(size));
        writer.writeSymbol(avgToSymbol(avg1));
	int h = size / 2;

        int r1 = subAvg(sx, sy, h, frame) - avg1;
        int r2 = subAvg(sx + h, sy, h, frame) - avg1;
        int r3 = subAvg(sx, sy + h, h, frame) - avg1;
        int r4 = subAvg(sx + h, sy + h, h, frame) - avg1;

        writer.writeSymbol(avgToSymbol(clamp(r1 + 128)));
        writer.writeSymbol(avgToSymbol(clamp(r2 + 128)));
        writer.writeSymbol(avgToSymbol(clamp(r3 + 128)));
        writer.writeSymbol(avgToSymbol(clamp(r4 + 128)));
	int step = Math.max(1, size / 4);

for (int yy = sy; yy < sy + size; yy += step) {

    for (int xx = sx; xx < sx + size; xx += step) {

        int localSum = 0;
        int count = 0;

        for (int y2 = yy;
             y2 < Math.min(yy + step, sy + size);
             y2++) {

            for (int x2 = xx;
                 x2 < Math.min(xx + step, sx + size);
                 x2++) {

                localSum += frame[y2][x2];
                count++;
            }
        }

        int tex = localSum / count;

        writer.writeSymbol(300 + tex);
    				}
			}
    		}
	}
}
