package com.nothing.handler;

import com.nothing.client.ScrcpyClient;
import com.nothing.videos.FrameProcessor;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class VideoHandler implements Runnable {
    private final ScrcpyClient scrcpyClient;
    private final Socket socket;
    private DataInputStream dis;
    private String deviceMeta="unknown";
    private String codec;
    private int width;
    private int height;
    private FrameProcessor frameProcessor;

    public VideoHandler(ScrcpyClient scrcpyClient,FrameProcessor frameProcessor) throws IOException {
        this.scrcpyClient = scrcpyClient;
        this.socket = scrcpyClient.getVideoSocket();
        this.dis = new DataInputStream(socket.getInputStream());
        this.frameProcessor= frameProcessor;
    }

    JFrame frame;
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

            CanvasFrame canvas = new CanvasFrame(deviceMeta, 1);//新建一个窗口
            canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            canvas.setAlwaysOnTop(true);
            canvas.setVisible(true);

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
                if (!canvas.isVisible()) {
                    break;
                }
                Frame frame = grabber.grabAtFrameRate();
                if (frame == null) {
                    continue;
                }
                canvas.showImage(frame);
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
    private void parseH264Data() throws IOException, InterruptedException {
        while (true) {
            // Read the frame header
            byte[] header = new byte[12];
            dis.readFully(header);
            // Extract the flags and PTS from the header
            boolean isConfigPacket = (header[0] & 0x80) != 0;
            boolean isKeyFrame = (header[0] & 0x40) != 0;
            long pts = ByteBuffer.wrap(header, 0, 8).getLong() & 0x3FFFFFFFFFFFFFFFL;

            // Extract the packet size from the header
            int packetSize = ByteBuffer.wrap(header, 8, 4).getInt();

            // Read the packet data
            byte[] packetData = new byte[packetSize];
            dis.readFully(packetData);

            frameProcessor.addFrame(packetData);
        }
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
