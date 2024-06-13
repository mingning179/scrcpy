package com.nothing.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ControlHandler implements Runnable{
    private final Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ControlHandler(Socket socket) throws IOException {
        this.socket=socket;
        this.dis=new DataInputStream(socket.getInputStream());
        this.dos=new DataOutputStream(socket.getOutputStream());
    }
    @Override
    public void run() {

    }

}
