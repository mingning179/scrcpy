package com.nothing.handler;

import com.nothing.videos.FrameProcessor;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class VideoHandler implements Runnable {
    private DataInputStream dis;
    private Socket socket;
    private String codec;
    private int width;
    private int height;
    private FrameProcessor frameProcessor;

    public VideoHandler(Socket socket,FrameProcessor frameProcessor) throws IOException {
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.frameProcessor= frameProcessor;
    }

    @Override
    public void run() {
        try {
            readCodecMetadata();
            parseH264Data();
        } catch (Exception  e) {
            throw new RuntimeException(e);
        }
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
        byte[] datas = new byte[4];
        dis.readFully(datas);
        codec = new String(datas, 0, 4);

        width = dis.readInt();
        height = dis.readInt();
        System.out.printf("codec=%s,width=%d,height=%d", codec, width, height);
    }
}
