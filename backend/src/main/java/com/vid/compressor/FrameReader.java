package com.vid.compressor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class FrameReader {

    // Load a batch of grayscale frames
    public static List<int[][]> loadGrayscaleFramesBatch(File folder, int startIndex, int batchSize) throws Exception {
        File[] files = folder.listFiles();
        if (files == null) return new ArrayList<>();

        java.util.Arrays.sort(files);

        List<int[][]> frames = new ArrayList<>();
        int endIndex = Math.min(startIndex + batchSize, files.length);

        for (int i = startIndex; i < endIndex; i++) {
            BufferedImage img = ImageIO.read(files[i]);
            int width = img.getWidth();
            int height = img.getHeight();
            int[][] gray = new int[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = img.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    gray[y][x] = (r + g + b) / 3;
                }
            }
            frames.add(gray);
        }

        return frames;
    }

    // Total number of frames in folder
    public static int totalFrames(File folder) {
        File[] files = folder.listFiles();
        return (files != null) ? files.length : 0;
    }
}
