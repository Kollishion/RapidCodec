package com.vid.compressor.decoder;

import java.io.IOException;

import com.vid.compressor.QuadtreeUtils;

public class BaselineDecoder {

	public static void decodeBlock(
			int sx, int sy, int size,
			int[][] frame,
			HuffmanDecoder decoder) throws IOException {

		int split = decoder.readSymbol();

		if (split == 1 && size > QuadtreeUtils.MIN_CU) {
			int h = size / 2;

			decodeBlock(sx, sy, h, frame, decoder);
			decodeBlock(sx + h, sy, h, frame, decoder);
			decodeBlock(sx, sy + h, h, frame, decoder);
			decodeBlock(sx + h, sy + h, h, frame, decoder);
			return;
		}

		int sizeSymbol = decoder.readSymbol();
		int avgSymbol = decoder.readSymbol();
		

		int blockSize = symbolToSize(sizeSymbol);
		int avg = symbolToAvg(avgSymbol);
		int actualSize = Math.min(blockSize, size);

		for (int y = sy; y < sy + actualSize; y++) {
			for (int x = sx; x < sx + actualSize; x++) {
				frame[y][x] = avg;
			}
		}
	}

	/* ================= SYMBOL DECODING ================= */

	private static int symbolToSize(int symbol) {
		return switch (symbol) {
			case 2 -> 8;
			case 3 -> 16;
			case 4 -> 32;
			case 5 -> 64;
			case 6 -> 128;
			case 7 -> 256;
			default -> throw new IllegalArgumentException("Invalid size symbol: " + symbol);
		};
	}

	private static int symbolToAvg(int symbol) {
		return (symbol - 8) << 3;
	}
}
