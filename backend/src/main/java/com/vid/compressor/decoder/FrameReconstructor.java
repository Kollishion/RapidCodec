package com.vid.compressor.decoder;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class FrameReconstructor {

    public static void writeFrame(int[][] frame, File out)
            throws Exception {

        int h = frame.length;
        int w = frame[0].length;

        BufferedImage img =
                new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = frame[y][x] & 0xFF;
                int rgb = (v << 16) | (v << 8) | v;
                img.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(img, "png", out);
    }
}
