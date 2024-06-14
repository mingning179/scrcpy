package com.nothing.videos;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FrameProcessor implements Runnable{
    private final BlockingQueue<byte[]> frameQueue;
    private boolean showfps = true;
    PipedInputStream pis;
    PipedOutputStream pos;

    public FrameProcessor() throws IOException {
        this.frameQueue = new LinkedBlockingQueue<>();
        Thread thread = new Thread(this);
        if(showfps){
            thread.setDaemon(true);
            thread.start();
        }
        //构造一个管道
        pis= new PipedInputStream();
        pos=new PipedOutputStream();
        pis.connect(pos);
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // Convert the packet data to an image
                    FFmpegFrameGrabber frameGrabber=new FFmpegFrameGrabber(pis);
                    frameGrabber.setFormat("h264");
                    frameGrabber.setNumBuffers(5);
                    frameGrabber.start();
                    Java2DFrameConverter converter = new Java2DFrameConverter();
                    BufferedImage image = null;
                    while (true){
                        image = converter.convert(frameGrabber.grabImage());
                        if(image!=null){
                            //处理图片
                            ImageIO.write(image, "jpg", new File("./"+System.currentTimeMillis()+"test.jpg"));
                        }
                    }
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public synchronized void addFrame(byte[] frameData) throws InterruptedException {
        frameQueue.put(frameData);
        try {
            pos.write(frameData);
            pos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server_fps();
        notify();
    }
    public synchronized byte[] getFrame() throws InterruptedException {
        while (frameQueue.isEmpty()) {
            wait();
        }
        client_fps();
        return frameQueue.take();
    }


    @Override
    public void run() {
        while (true) {
            //计算fps
            try {
                Thread.sleep(1000);
                System.out.println("server fps:" + server_frameCount);
                System.out.println("client fps:" + client_frameCount);
                server_frameCount = 0;
                client_frameCount = 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    long server_lastTime = -1;
    long server_frameCount = 0;
    private void server_fps() {
        // 计算 server fps
        long currentTime = System.currentTimeMillis();
        if (server_lastTime == -1) {
            server_lastTime = currentTime;
        }
        server_frameCount++;
    }

    long client_lastTime = -1;
    long client_frameCount = 0;
    private void client_fps() {
        // 计算client fps
        long currentTime = System.currentTimeMillis();
        if (client_lastTime == -1) {
            client_lastTime = currentTime;
        }
        client_frameCount++;
    }
}