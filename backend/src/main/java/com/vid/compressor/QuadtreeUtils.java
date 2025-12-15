package com.vid.compressor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class QuadtreeUtils {
    public static final int CTU_SIZE = 256;
    public static final int MIN_CU = 8;

    public static int[][] syntheticFrame(int w, int h) {
        int[][] f = new int[h][w];
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int qx = x < w / 2 ? 0 : 1;
                int qy = y < h / 2 ? 0 : 1;
                int base = 40 + qx * 20 + qy * 10;
                f[y][x] = clamp(base + rnd.nextInt(-5, 6));
            }
        }

        for (int i = 0; i < 10; i++) {
            int cx = rnd.nextInt(0, w);
            int cy = rnd.nextInt(0, h);
            int r = rnd.nextInt(5, 12);
            for (int yy = Math.max(0, cy - r); yy < Math.min(h, cy + r); yy++) {
                for (int xx = Math.max(0, cx - r); xx < Math.min(w, cx + r); xx++) {
                    f[yy][xx] = clamp(180 - rnd.nextInt(0, 40));
                }
            }
        }

        for (int y = 0; y < h / 2; y++) {
            for (int x = 0; x < w / 2; x++) {
                f[y + h / 2][x + w / 2] = f[y][x];
            }
        }

        return f;
    }

    public static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static class BaselineEncoder {
        public long rdoCount = 0;

        public void processCTU(int sx, int sy, int size, int[][] frame) {
            computeRDCost(sx, sy, size, frame);
            rdoCount++;

            if (size > MIN_CU) {
                double var = computeVariance(sx, sy, size, frame);
                if (var > varianceThreshold(size)) {
                    int half = size / 2;
                    processCTU(sx, sy, half, frame);
                    processCTU(sx + half, sy, half, frame);
                    processCTU(sx, sy + half, half, frame);
                    processCTU(sx + half, sy + half, half, frame);
                }
            }
        }

        double computeRDCost(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++) {
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p;
                    sum2 += p * p;
                }
            }
            double mean = sum / (double) (size * size);
            double var = (sum2 / (double) (size * size)) - mean * mean;
            double cost = mean * 0.6 + var * 0.4;
            for (int i = 0; i < 1000; i++) {
                cost += Math.sin(i + cost) * 0.000001;
            }
            return cost;
        }

        double computeVariance(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++) {
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p;
                    sum2 += p * p;
                }
            }
            double mean = sum / (double) (size * size);
            return (sum2 / (double) (size * size)) - mean * mean;
        }

        double varianceThreshold(int size) { return 100.0 / size; }
    }

    public static class ProposedEncoder {
        public long rdoCount = 0;
        public long cacheHits = 0;

        static class Key {
            final int sx, sy, size;
            final int avg, varBucket;
            Key(int sx, int sy, int size, int avg, int vb) {
                this.sx = sx; this.sy = sy; this.size = size;
                this.avg = avg; this.varBucket = vb;
            }
            @Override public int hashCode() {
                return Objects.hash(sx, sy, size, avg, varBucket);
            }
            @Override public boolean equals(Object o) {
                Key k = (Key) o;
                return k.sx == sx && k.sy == sy && k.size == size &&
                       k.avg == avg && k.varBucket == varBucket;
            }
        }

        static class Result {
            boolean split;
            double cost;
            Result(boolean s, double c) { split = s; cost = c; }
        }

        Map<Key, Result> cache = new HashMap<>();

        public void processCTU(int rootX, int rootY, int rootSize, int[][] frame) {
            Deque<int[]> stack = new ArrayDeque<>();
            stack.push(new int[]{rootX, rootY, rootSize});

            while (!stack.isEmpty()) {
                int[] top = stack.pop();
                int sx = top[0], sy = top[1], size = top[2];

                Key key = buildKey(sx, sy, size, frame);
                Result cached = cache.get(key);

                if (cached != null) {
                    cacheHits++;
                    if (cached.split && size > MIN_CU) {
                        int half = size / 2;
                        stack.push(new int[]{sx, sy, half});
                        stack.push(new int[]{sx + half, sy, half});
                        stack.push(new int[]{sx, sy + half, half});
                        stack.push(new int[]{sx + half, sy + half, half});
                    }
                    continue;
                }

                double cost = computeRDCost(sx, sy, size, frame);
                rdoCount++;

                boolean split = false;
                if (size > MIN_CU) {
                    double var = computeVariance(sx, sy, size, frame);
                    if (var > varianceThreshold(size)) split = true;
                }

                cache.put(key, new Result(split, cost));

                if (split) {
                    int half = size / 2;
                    stack.push(new int[]{sx, sy, half});
                    stack.push(new int[]{sx + half, sy, half});
                    stack.push(new int[]{sx, sy + half, half});
                    stack.push(new int[]{sx + half, sy + half, half});
                }
            }
        }

        Key buildKey(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++) {
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p;
                    sum2 += p * p;
                }
            }
            int avg = (int) (sum / (double) (size * size));
            double var = (sum2 / (double) (size * size)) - avg * avg;
            int bucket = (int) Math.min(255, Math.floor(var / 4.0));
            return new Key(sx, sy, size, avg, bucket);
        }

        double computeRDCost(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++) {
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p; sum2 += p * p;
                }
            }
            double mean = sum / (double) (size * size);
            double var = (sum2 / (double) (size * size)) - mean * mean;
            double cost = mean * 0.6 + var * 0.4;
            for (int i = 0; i < 1000; i++) {
                cost += Math.sin(i + cost) * 0.000001;
            }
            return cost;
        }

        double computeVariance(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++) {
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p; sum2 += p * p;
                }
            }
            double mean = sum / (double) (size * size);
            return (sum2 / (double) (size * size)) - mean * mean;
        }

        double varianceThreshold(int size) { return 100.0 / size; }
    }

    public static class ProposedEncoder2 {
        public long rdoCount = 0;
        public long cacheHits = 0;
        public long temporalHits = 0;

        Map<ProposedEncoder.Key, ProposedEncoder.Result> spatialCache = new HashMap<>();
        Map<ProposedEncoder.Key, ProposedEncoder.Result> temporalCache = new HashMap<>();

        static class Task extends RecursiveAction {
            ProposedEncoder2 enc;
            int sx, sy, size;
            int[][] frame;

            Task(ProposedEncoder2 e, int sx, int sy, int size, int[][] f) {
                this.enc = e; this.sx = sx; this.sy = sy; this.size = size; this.frame = f;
            }

            @Override
            protected void compute() {
                ProposedEncoder.Key key = enc.buildKey(sx, sy, size, frame);

                ProposedEncoder.Result tmp = enc.temporalCache.get(key);
                if (tmp != null) {
                    enc.temporalHits++;
                    if (tmp.split && size > MIN_CU) split();
                    return;
                }

                ProposedEncoder.Result cached = enc.spatialCache.get(key);
                if (cached != null) {
                    enc.cacheHits++;
                    if (cached.split && size > MIN_CU) split();
                    return;
                }

                double cost = enc.computeRDCost(sx, sy, size, frame);
                enc.rdoCount++;

                boolean split = false;
                if (size > MIN_CU) {
                    double var = enc.computeVariance(sx, sy, size, frame);
                    if (var > enc.varianceThreshold(size)) split = true;
                }

                ProposedEncoder.Result r = new ProposedEncoder.Result(split, cost);
                enc.spatialCache.put(key, r);

                if (split) split();
            }

            private void split() {
                int half = size / 2;
                invokeAll(
                    new Task(enc, sx, sy, half, frame),
                    new Task(enc, sx + half, sy, half, frame),
                    new Task(enc, sx, sy + half, half, frame),
                    new Task(enc, sx + half, sy + half, half, frame)
                );
            }
        }

        public void processFrames(List<int[][]> frames) {
            ForkJoinPool pool = ForkJoinPool.commonPool();
            for (int[][] frame : frames) {
                pool.invoke(new Task(this, 0, 0, CTU_SIZE, frame));
                temporalCache.putAll(spatialCache);
                spatialCache.clear();
            }
        }

        public ProposedEncoder.Key buildKey(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++)
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p; sum2 += p * p;
                }
            int avg = (int)(sum / (double)(size * size));
            double var = (sum2 / (double)(size * size)) - avg * avg;
            int bucket = (int)Math.min(255, Math.floor(var / 4.0));
            return new ProposedEncoder.Key(sx, sy, size, avg, bucket);
        }

        public double computeRDCost(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++)
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p; sum2 += p * p;
                }
            double mean = sum / (double)(size * size);
            double var = (sum2 / (double)(size * size)) - mean * mean;
            double cost = mean * 0.6 + var * 0.4;
            for (int i = 0; i < 1000; i++) cost += Math.sin(i + cost) * 0.000001;
            return cost;
        }

        public double computeVariance(int sx, int sy, int size, int[][] frame) {
            int sum = 0, sum2 = 0;
            for (int y = sy; y < sy + size; y++)
                for (int x = sx; x < sx + size; x++) {
                    int p = frame[y][x];
                    sum += p; sum2 += p * p;
                }
            double mean = sum / (double)(size * size);
            return (sum2 / (double)(size * size)) - mean * mean;
        }

        public double varianceThreshold(int size) { return 100.0 / size; }
    }
}
