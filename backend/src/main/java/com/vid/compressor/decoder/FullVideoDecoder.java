package com.vid.compressor.decoder;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.vid.compressor.QuadtreeUtils.Key;
import com.vid.compressor.QuadtreeUtils;
import com.vid.compressor.entropy.HuffmanTable;

public class FullVideoDecoder {

	public static List<int[][]> decode(File compressed, int frameCount)
			throws IOException {

		List<int[][]> frames = new ArrayList<>();

		try (BitstreamReader reader = new BitstreamReader(compressed)) {

			HuffmanTable table = HuffmanHeaderReader.readHeader(reader);
			HuffmanBitReader huff = new HuffmanBitReader(reader, table);
			Map<Key, Boolean> cache = new HashMap<>(); 	
			for (int i = 0; i < frameCount; i++) {
    				int[][] frame = new int[QuadtreeUtils.CTU_SIZE][QuadtreeUtils.CTU_SIZE];
				
    				ProposedDecoder.decodeBlock(
            			0, 0,
            			QuadtreeUtils.CTU_SIZE,
            			frame,
            			new HuffmanDecoder(huff), cache);

    				frames.add(frame);

    				BufferedImage image = new BufferedImage(
            			QuadtreeUtils.CTU_SIZE,
            			QuadtreeUtils.CTU_SIZE,
            			BufferedImage.TYPE_BYTE_GRAY);

    				for (int y = 0; y < QuadtreeUtils.CTU_SIZE; y++) {

        				for (int x = 0; x < QuadtreeUtils.CTU_SIZE; x++) {

            					int pixel = frame[y][x];

            					pixel = Math.max(0, Math.min(255, pixel));

            					int rgb = (pixel << 16) | (pixel << 8) | pixel;

            					image.setRGB(x, y, rgb);
        				}
    				}

    File outDir = new File("decoded_frames");

    if (!outDir.exists()) {
        outDir.mkdirs();
    }

    File outFile = new File(
            outDir,
            String.format("frame_%04d.png", i));

    ImageIO.write(image, "png", outFile);
    if(i % 100 == 0){
    System.out.println("Saved: " + outFile.getName());
    				}
			}
		}
		return frames;
	}
}
