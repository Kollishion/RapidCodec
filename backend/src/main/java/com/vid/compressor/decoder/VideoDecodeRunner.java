package com.vid.compressor.decoder;

import java.io.File;

public class VideoDecodeRunner {

    public static void main(String[] args) throws Exception {

        File compressed = new File("compressed_proposed2.dat");

        int frameCount = 2234;

        FullVideoDecoder.decode(
                compressed,
                frameCount);

        System.out.println("\n===== VIDEO DECODE COMPLETE =====");
    }
}
