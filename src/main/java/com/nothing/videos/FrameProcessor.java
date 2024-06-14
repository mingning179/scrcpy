package com.nothing.videos;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.*;

public class FrameProcessor implements Runnable{
    private static final int BUFFER_SIZE = 1024 * 1024 * 10;  // Adjust this value
    private static final double FRAME_RATE = 60.0;  // Adjust this value

    BufferedOutputStream bos;
    BufferedInputStream bis;

    public FrameProcessor() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        bos = new BufferedOutputStream(pos, BUFFER_SIZE);
        PipedInputStream pis = new PipedInputStream(pos);
        bis = new BufferedInputStream(pis, BUFFER_SIZE);

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
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(bis);
            grabber.setFormat("h264");
            grabber.setFrameRate(FRAME_RATE);
            grabber.start(false);

            CanvasFrame videoDisplay = new CanvasFrame("hello", CanvasFrame.getDefaultGamma() / grabber.getGamma());
            videoDisplay.setSize(grabber.getImageWidth(), grabber.getImageHeight());

            Frame frame;
            while (true) {
                frame = grabber.grabImage();
                if (frame != null) {
                    videoDisplay.showImage(frame);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}