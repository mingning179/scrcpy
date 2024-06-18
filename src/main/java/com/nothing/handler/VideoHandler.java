package com.nothing.handler;

import com.nothing.DeviceGui;
import com.nothing.client.ScrcpyClient;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class VideoHandler implements Runnable {
    private final ScrcpyClient scrcpyClient;
    private final Socket socket;
    private DataInputStream dis;
    private String deviceMeta="unknown";
    private String codec;
    private int width;
    private int height;
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
            processFirstByteAndGetDeviceMeta();
            readCodecMetadata();

            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(dis);
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            grabber.setImageWidth(width);
            grabber.setImageHeight(height);
            grabber.start(false);   //开始获取摄像头数据

            deviceGui.setTitle(deviceMeta);
            deviceGui.setSize(width, height);
            deviceGui.setVisible(true);
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
                if (!deviceGui.isVisible()) {
                    break;
                }
                Frame frame = grabber.grabAtFrameRate();
                if (frame == null) {
                    continue;
                }
                deviceGui.showImage(frame);
            }
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 验证 dummyByte 以及解析 deviceMeta
     *
     * @return
     * @throws IOException
     */
    private String processFirstByteAndGetDeviceMeta() throws IOException {
        if(scrcpyClient.isSendDummyByte()){
            byte firstByte = dis.readByte();
            if (firstByte != 0) {
                throw new RuntimeException("first byte is not 0:" + firstByte);
            }
        }
        if(scrcpyClient.isSendDeviceMeta()){
            //read deviceMeta
            byte[] deviceMetaBytes = new byte[64];
            dis.readFully(deviceMetaBytes);
            // Find the first zero byte
            int zeroByteIndex = 0;
            for (; zeroByteIndex < 64; zeroByteIndex++) {
                if (deviceMetaBytes[zeroByteIndex] == 0) {
                    break;
                }
            }
            deviceMeta = new String(deviceMetaBytes, 0, zeroByteIndex, StandardCharsets.UTF_8);
        }
        return deviceMeta;
    }

    private void readCodecMetadata() throws IOException {
        if(scrcpyClient.isSendCodecMeta()) {
            byte[] datas = new byte[4];
            dis.readFully(datas);
            codec = new String(datas, 0, 4);
            width = dis.readInt();
            height = dis.readInt();
            System.out.printf("codec=%s,width=%d,height=%d \n", codec, width, height);
        }
    }
}
