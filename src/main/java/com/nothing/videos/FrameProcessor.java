package com.nothing.videos;

import com.nothing.VideoDisplay;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FrameProcessor implements Runnable{
    private final BlockingQueue<byte[]> frameQueue;
    private VideoDisplay videoDisplay;

    BufferedOutputStream bos;
    BufferedInputStream bis;
    public FrameProcessor() throws IOException {
        this.frameQueue = new LinkedBlockingQueue<>();


        PipedOutputStream pos=new PipedOutputStream();
        bos=new BufferedOutputStream(pos,1024*1024*10);
        PipedInputStream pis=new PipedInputStream(pos);
        bis=new BufferedInputStream(pis,1024*1024*10);

        Thread thread = new Thread(this);
        thread.start();

    }

    public synchronized void addFrame(byte[] frameData) throws InterruptedException, IOException {
        frameQueue.put(frameData);
        bos.write(frameData);
        bos.write(0);
        bos.flush();
        notify();
    }
    public synchronized byte[] getFrame() throws InterruptedException {
        while (frameQueue.isEmpty()) {
            wait();
        }
        return frameQueue.take();
    }


    @Override
    public void run() {
        try {
            //用swing 显示图片
            FFmpegFrameGrabber grabber=new FFmpegFrameGrabber(bis);
            grabber.setFormat("h264");
            grabber.setFrameRate(120);
            grabber.start(false);

            CanvasFrame videoDisplay = new CanvasFrame("hello",CanvasFrame.getDefaultGamma()/grabber.getGamma());
            videoDisplay.setSize(grabber.getImageWidth(),grabber.getImageHeight());


            Frame frame=null;
            while (true){
                frame=grabber.grabImage();
                if(frame!=null){
                    videoDisplay.showImage(frame);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}