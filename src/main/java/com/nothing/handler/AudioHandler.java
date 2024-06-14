package com.nothing.handler;

import com.nothing.client.ScrcpyClient;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class AudioHandler implements Runnable{
    private final ScrcpyClient scrcpyClient;
    private final Socket socket;
    private DataInputStream dis;

    public AudioHandler(ScrcpyClient scrcpyClient, Socket socket) throws IOException {
        this.scrcpyClient=scrcpyClient;
        this.socket=socket;
        this.dis=new DataInputStream(socket.getInputStream());
    }
    @Override
    public void run() {
    }
}
