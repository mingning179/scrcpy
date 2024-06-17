package com.nothing.videos;

import java.io.*;

public class FrameProcessor implements Runnable{
    private static final int BUFFER_SIZE = 1024 * 1024 * 10;  // Adjust this value
    private static final double FRAME_RATE = 60.0;  // Adjust this value

    OutputStream bos;
    InputStream bis;

    public FrameProcessor() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        bos=pos;
        PipedInputStream pis = new PipedInputStream();

        pis.connect(pos);
        bis=pis;

        Thread thread = new Thread(this);
        thread.start();
    }

    public void addFrame(byte[] frameData) throws IOException {
        bos.write(frameData);
        bos.write(0);
        bos.flush();
    }




    @Override
    public void run() {

    }
}