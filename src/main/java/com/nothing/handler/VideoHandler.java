package com.nothing.handler;

import com.nothing.DeviceGui;
import com.nothing.client.ScrcpyClient;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class VideoHandler implements Runnable {
    private final ScrcpyClient scrcpyClient;
    private final Socket socket;
    private final DataInputStream dis;

    DeviceGui deviceGui;

    public VideoHandler(ScrcpyClient scrcpyClient,DeviceGui deviceGui) throws IOException {
        this.scrcpyClient = scrcpyClient;
        this.socket = scrcpyClient.getVideoSocket();
        this.dis = new DataInputStream(socket.getInputStream());
        this.deviceGui=deviceGui;
    }

    @Override
    public void run() {
        try {

            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(dis);
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            grabber.setImageWidth(scrcpyClient.getVideoWidth());
            grabber.setImageHeight(scrcpyClient.getVideoHeight());
            grabber.start(false);// 开始之后就可以获取帧了
            //等待窗口可见
            while (true) {
                if (deviceGui.isVisible()) {
                    break;
                }
                Thread.sleep(10);
            }
            //计算帧率
            long start = System.currentTimeMillis();
            int count = 0;

            while (true) {
                count++;
                long end = System.currentTimeMillis();
                if (end - start >= 1000) {
                    System.out.println("帧率：" + count);
                    count = 0;
                    start = end;
                }

                Frame frame = grabber.grabImage();
                if (frame != null) {
                    deviceGui.showImage(frame);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
