package com.nothing;

import org.bytedeco.javacv.CanvasFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoDisplay extends CanvasFrame {
    BufferedImage image;
    public VideoDisplay(String title,double gamma) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}