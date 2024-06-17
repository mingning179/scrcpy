package com.nothing;

import com.nothing.client.ScrcpyClient;
import com.nothing.handler.AudioHandler;
import com.nothing.handler.ControlHandler;
import com.nothing.handler.VideoHandler;
import com.nothing.videos.FrameProcessor;

import java.io.IOException;

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
            // 视屏处理器
            VideoHandler videoHandler = new VideoHandler(scrcpyClient,frameProcessor);
            new Thread(videoHandler).start();

            // 音频处理器
            AudioHandler audioHandler = new AudioHandler(scrcpyClient);
            new Thread(audioHandler).start();

            // 控制处理器
            ControlHandler controlHandler = new ControlHandler(scrcpyClient);
            new Thread(controlHandler).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}