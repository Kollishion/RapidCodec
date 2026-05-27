package com.vid.compressor.decoder;

import java.io.IOException;
import java.util.Map;

import com.vid.compressor.QuadtreeUtils.Key;

public class ProposedDecoder {

    public static final int NO_SPLIT = 0;
    public static final int SPLIT = 1;
    public static final int PREDICTED = 2;

    public static int symbolToSize(int symbol) {

        return switch (symbol) {

            case 3 -> 4;
            case 4 -> 8;
            case 5 -> 16;
            case 6 -> 32;
            case 7 -> 64;
            case 8 -> 128;
            case 9 -> 256;
            default ->
                throw new IllegalArgumentException(
                    "Invalid size symbol: " + symbol
                );
        };
    }

    public static int symbolToAvg(int symbol) {

        return symbol - 9;
    }

    public static void fillBlock(
            int sx,
            int sy,
            int size,
            int avg,
            int[][] frame) {

        for (int y = sy; y < sy + size; y++) {

            for (int x = sx; x < sx + size; x++) {

                frame[y][x] = avg;
            }
        }
    }

    public static void decodeBlock(
            int sx,
            int sy,
            int size,
            int[][] frame,
            HuffmanDecoder decoder,
            Map<Key, Boolean> cache) throws IOException {

        Key key = new Key(sx, sy, size);

        int symbol = decoder.readSymbol();

        boolean split;

        if (symbol == PREDICTED) {

            Boolean predicted = cache.get(key);

            if (predicted == null) {

                throw new IOException(
                    "Prediction requested but cache missing"
                );
            }

            split = predicted;

        } else if (symbol == SPLIT) {

            split = true;
            cache.put(key, true);

        } else if (symbol == NO_SPLIT) {

            split = false;
            cache.put(key, false);

        } else {

            throw new IOException(
                "Invalid split symbol: " + symbol
            );
        }
	        if (split) {

            int half = size / 2;

            decodeBlock(
                    sx,
                    sy,
                    half,
                    frame,
                    decoder,
                    cache
            );

            decodeBlock(
                    sx + half,
                    sy,
                    half,
                    frame,
                    decoder,
                    cache
            );

            decodeBlock(
                    sx,
                    sy + half,
                    half,
                    frame,
                    decoder,
                    cache
            );

            decodeBlock(
                    sx + half,
                    sy + half,
                    half,
                    frame,
                    decoder,
                    cache
            );

    }else{
	int sizeSymbol = decoder.readSymbol();

	int avgSymbol = decoder.readSymbol();

	int blockSize = symbolToSize(sizeSymbol);

	int avg = symbolToAvg(avgSymbol);

	decoder.readSymbol();
	decoder.readSymbol();
	decoder.readSymbol();
	decoder.readSymbol();

	int step = Math.max(1, blockSize / 4);

	for (int yy = sy; yy < sy + blockSize; yy += step) {

    		for (int xx = sx; xx < sx + blockSize; xx += step) {
 
        int tex = decoder.readSymbol() - 300;

        for (int y2 = yy; y2 < Math.min(yy + step, sy + blockSize);
             y2++) {

            	for (int x2 = xx; x2 < Math.min(xx + step, sx + blockSize);
                 x2++) {

                frame[y2][x2] = tex;
            		}
        				}
    				}
			}
    		}
	}
}
