package com.nothing.handler;

import com.nothing.DeviceGui;
import com.nothing.client.ScrcpyClient;

import javax.sound.sampled.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class AudioHandler implements Runnable{
    private final ScrcpyClient scrcpyClient;
    private final Socket socket;
    private DataInputStream dis;
    DeviceGui deviceGui;
    public AudioHandler(ScrcpyClient scrcpyClient, DeviceGui deviceGui) throws IOException {
        this.scrcpyClient=scrcpyClient;
        this.socket=scrcpyClient.getAudioSocket();
        this.dis=new DataInputStream(socket.getInputStream());
        this.deviceGui=deviceGui;
    }
    @Override
    public void run() {
        SourceDataLine line = null;
        try {
            byte[] audioCodec = new byte[4];
            dis.readFully(audioCodec);
            System.out.println("audioCodec:"+new String(audioCodec));

            AudioFormat format=new AudioFormat(48000,16,2,true,false);

            AudioInputStream ais=new AudioInputStream(dis,format, AudioSystem.NOT_SPECIFIED);
            line=AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            byte[] buffer = new byte[1024]; // 创建一个缓冲区
            int bytesRead;

            // 从输入流中读取音频数据并播放
            while ((bytesRead = ais.read(buffer)) != -1) {
                line.write(buffer, 0, bytesRead);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            try {
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            line.drain();
            line.close();
        }
    }
}
