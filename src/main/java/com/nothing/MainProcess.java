package com.nothing;

import com.nothing.client.ScrcpyClient;
import com.nothing.handler.AudioHandler;
import com.nothing.handler.ControlHandler;
import com.nothing.handler.VideoHandler;
import com.nothing.videos.FrameProcessor;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainProcess {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = ScrcpyClient.SERVER_PORT;

    public static void main(String[] args) throws InterruptedException, IOException {
        //启动服务
        if(ScrcpyClient.startServer())
        {
            System.out.println("服务启动成功");
        }else {
            throw new InterruptedException("服务启动失败");
        }

        FrameProcessor frameProcessor = new FrameProcessor();
        try {
            // Open three sockets to the same address
            Socket videoSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            Socket audioSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            Socket controlSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            // 验证 dummyByte 以及解析 deviceMeta
            String deviceMeta = processFirstByteAndGetDeviceMeta(videoSocket);
            System.out.printf("deviceMeta=%s\n", deviceMeta);

            // 视屏处理器
            VideoHandler videoHandler = new VideoHandler(videoSocket, frameProcessor);
            new Thread(videoHandler).start();

            // 音频处理器
            AudioHandler audioHandler = new AudioHandler(audioSocket);
            new Thread(audioHandler).start();

            // 控制处理器
            ControlHandler controlHandler = new ControlHandler(controlSocket);
            new Thread(controlHandler).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证 dummyByte 以及解析 deviceMeta
     *
     * @param socket
     * @return
     * @throws IOException
     */
    private static String processFirstByteAndGetDeviceMeta(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        byte firstByte = in.readByte();
        if (firstByte != 0) {
            throw new RuntimeException("first byte is not 0:" + firstByte);
        }
        //read deviceMeta
        byte[] deviceMeta = new byte[64];
        in.readFully(deviceMeta);
        // Find the first zero byte
        int zeroByteIndex = 0;
        for (; zeroByteIndex < 64; zeroByteIndex++) {
            if (deviceMeta[zeroByteIndex] == 0) {
                break;
            }
        }
        String deviceMetaString = new String(deviceMeta, 0, zeroByteIndex, StandardCharsets.UTF_8);
        return deviceMetaString;
    }
}