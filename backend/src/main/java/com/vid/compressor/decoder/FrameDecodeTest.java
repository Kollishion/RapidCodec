package com.vid.compressor.decoder;

import java.io.File;

public class FrameDecodeTest {
	public static void main(String[] args) throws Exception {

		File input = new File("compressed_proposed2.dat");

		int[][] frame = Decoder.decodeFrame(input);

		System.out.println("Frame decoded successfully!");
		System.out.println("Top-left pixel: " + frame[0][0]);
	}
}
