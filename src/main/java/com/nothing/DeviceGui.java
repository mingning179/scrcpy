package com.nothing;

import com.nothing.client.ScrcpyClient;
import com.nothing.handler.AudioHandler;
import com.nothing.handler.VideoHandler;
import com.nothing.handler.control.ControlHandler;
import org.bytedeco.javacv.CanvasFrame;

import javax.swing.*;
import java.io.IOException;

public class DeviceGui extends CanvasFrame {
    VideoHandler videoHandler;
    AudioHandler audioHandler;
    ControlHandler controlHandler;

    public DeviceGui(String title) throws IOException {
        super(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void start() throws IOException {
        ScrcpyClient scrcpyClient = new ScrcpyClient();
        //启动服务
        if (scrcpyClient.startServer()) {
            System.out.println("服务启动成功");
        } else {
            throw new RuntimeException("服务启动失败");
        }
        videoHandler=new VideoHandler(scrcpyClient, this);
        audioHandler=new AudioHandler(scrcpyClient, this);
        controlHandler=new ControlHandler(scrcpyClient, this);

        // 视屏处理器
        new Thread(videoHandler).start();
        // 音频处理器
        new Thread(audioHandler).start();
        // 控制处理器
        new Thread(controlHandler).start();
    }


    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            try {
                DeviceGui deviceGui = new DeviceGui("DeviceGui");
                deviceGui.setVisible(false);
                deviceGui.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
