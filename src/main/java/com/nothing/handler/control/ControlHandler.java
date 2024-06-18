package com.nothing.handler.control;

import com.nothing.DeviceGui;
import com.nothing.client.ScrcpyClient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ControlHandler implements Runnable{
    private final ScrcpyClient scrcpyClient;
    private final Socket socket;
    private InputStream is;
    private DataOutputStream dos;
    ControlMessageReader reader;
    DeviceGui deviceGui;

    public ControlHandler(ScrcpyClient scrcpyClient, DeviceGui deviceGui) throws IOException {
        this.scrcpyClient=scrcpyClient;
        this.socket=scrcpyClient.getControlSocket();
        this.is = socket.getInputStream();
        this.dos=new DataOutputStream(socket.getOutputStream());
        reader = new ControlMessageReader();
        this.deviceGui=deviceGui;
    }
    @Override
    public void run() {
        while (true) {
            try {
                reader.readFrom(is);
                ControlMessage msg = reader.next();
                System.out.printf("Control message: %s%n", msg);

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
    public void sendEvent(ControlMessage msg) throws IOException {
//        ControlMessage.createInjectTouchEvent( 0, 0, 0).writeTo(dos);
//        dos.write(Binary.toUnsigned(0));
//        dos.flush();
    }
}
