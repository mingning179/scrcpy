package com.nothing.handler;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class AudioHandler implements Runnable{
    private final Socket socket;
    private DataInputStream dis=null;

    public AudioHandler(Socket socket) throws IOException {
        this.socket=socket;
        this.dis=new DataInputStream(socket.getInputStream());
    }
    @Override
    public void run() {
    }
}
