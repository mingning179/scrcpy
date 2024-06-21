package com.nothing.handler.control;

import com.nothing.DeviceGui;
import com.nothing.client.ScrcpyClient;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ControlHandler extends MouseAdapter implements Runnable  {
    private final ScrcpyClient scrcpyClient;
    private final Socket socket;
    private InputStream is;
    private DataOutputStream dos;
    ControlMessageReader reader;
    DeviceGui deviceGui;
    private static final int POINTER_ID = 9999;
    private static final int PRESSURE = 0xffff;

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

    public void injectTouchEvent(int action, MouseEvent e) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(32); // Allocate a ByteBuffer with the necessary capacity

            buffer.put((byte) ControlMessage.TYPE_INJECT_TOUCH_EVENT);
            buffer.put((byte) action);
            buffer.putLong(POINTER_ID); // pointerId
            buffer.putInt(e.getX());
            buffer.putInt(e.getY());
            buffer.putShort((short) scrcpyClient.getVideoWidth());
            buffer.putShort((short) scrcpyClient.getVideoHeight());
            buffer.putShort((short) PRESSURE); // pressure
            buffer.putInt(MotionEvent.BUTTON_PRIMARY); // action button
            buffer.putInt(MotionEvent.BUTTON_PRIMARY); // buttons

            byte[] data=buffer.array();
            dos.write(data);
            dos.flush();

            deviceGui.recodeOperation(data);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    private void handleException(IOException ex) {
        // Log the exception and gracefully close your program
        ex.printStackTrace();
    }
    @Override
    public void mousePressed(MouseEvent e) {
        injectTouchEvent(MotionEvent.ACTION_DOWN, e);
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        injectTouchEvent(MotionEvent.ACTION_UP, e);
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        injectTouchEvent(MotionEvent.ACTION_MOVE, e);
    }
}
