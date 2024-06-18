package com.nothing.handler;

import com.nothing.DeviceGui;
import com.nothing.client.ScrcpyClient;
import org.bytedeco.javacv.CanvasFrame;

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
    }
}
