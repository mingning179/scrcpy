package com.nothing.videos;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FrameProcessor implements Runnable{
    private final BlockingQueue<byte[]> frameQueue;
    private boolean showfps = true;
    public FrameProcessor() {
        this.frameQueue = new LinkedBlockingQueue<>();
        Thread thread = new Thread(this);
        if(showfps){
            thread.setDaemon(true);
            thread.start();
        }
    }

    public synchronized void addFrame(byte[] frameData) throws InterruptedException {
        frameQueue.put(frameData);
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