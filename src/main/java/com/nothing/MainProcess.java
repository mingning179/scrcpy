package com.nothing;

import com.nothing.client.ScrcpyClient;
import com.nothing.handler.VideoHandler;
import com.nothing.videos.FrameProcessor;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainProcess {
    public static void main(String[] args) throws InterruptedException, IOException {
        ScrcpyClient scrcpyClient = new ScrcpyClient();
        //启动服务
        if(scrcpyClient.startServer())
        {
            System.out.println("服务启动成功");
        }else {
            throw new InterruptedException("服务启动失败");
        }

        FrameProcessor frameProcessor = new FrameProcessor();
        try {
            // Open three sockets to the same address
            Socket videoSocket = scrcpyClient.getVideoSocket();
            Socket audioSocket = scrcpyClient.getAudioSocket();
            Socket controlSocket = scrcpyClient.getControlSocket();

            // 验证 dummyByte 以及解析 deviceMeta
            String deviceMeta= processFirstByteAndGetDeviceMeta(videoSocket);
            System.out.printf("deviceMeta=%s\n", deviceMeta);

            // 视屏处理器
            VideoHandler videoHandler = new VideoHandler(scrcpyClient,videoSocket,frameProcessor);
            new Thread(videoHandler).start();

//            // 音频处理器
//            AudioHandler audioHandler = new AudioHandler(scrcpyClient,audioSocket);
//            new Thread(audioHandler).start();
//
//            // 控制处理器
//            ControlHandler controlHandler = new ControlHandler(scrcpyClient,controlSocket);
//            new Thread(controlHandler).start();

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