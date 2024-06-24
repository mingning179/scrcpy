package com.nothing;

import com.nothing.client.ScrcpyClient;
import com.nothing.handler.AudioHandler;
import com.nothing.handler.VideoHandler;
import com.nothing.handler.control.ControlHandler;
import org.bytedeco.javacv.CanvasFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DeviceGui extends CanvasFrame {
    VideoHandler videoHandler;
    AudioHandler audioHandler;
    ControlHandler controlHandler;
    private boolean train=false;

    public DeviceGui(String title) throws IOException {
        super(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void start(boolean train) throws IOException {
        this.train=train;
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
        super.canvas.addMouseListener(controlHandler);
        super.canvas.addMouseMotionListener(controlHandler);

        setTitle(scrcpyClient.getDeviceMeta());
        setSize(scrcpyClient.getVideoWidth(),scrcpyClient.getVideoHeight());
        setVisible(true);
        // 视屏处理器
        new Thread(videoHandler).start();
        // 音频处理器
        new Thread(audioHandler).start();
        // 控制处理器
        new Thread(controlHandler).start();
    }

    private Image image;
    @Override
    public void showImage(Image image) {
        super.showImage(image);
        this.image = image;
    }

    public void recodeOperation(byte[] opData) {
        if(!train){
            return;
        }
        //保存图片和操作
        if(!new File("traindata").exists()){
            new File("traindata").mkdirs();
        }
        String path = "traindata/"+System.currentTimeMillis();
        File imageFile = new File(path+".png");
        try {
            ImageIO.write((BufferedImage) image, "png", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File opDataFile = new File(path+".op");
        try (FileOutputStream fos = new FileOutputStream(opDataFile)){
             fos.write(opData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DeviceGui deviceGui = new DeviceGui("DeviceGui");
                deviceGui.setVisible(false);
                deviceGui.start(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
