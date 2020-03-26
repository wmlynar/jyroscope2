package com.github.jy2.sm.monitor.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private BufferedImage image = null;
    private Dimension size = new Dimension();

    public Dimension getPreferredSize() {
        return size;
    }

    protected void paintComponent(Graphics g) {
        if (image != null) {
            // Center image in this component.
            int x = (getWidth() - size.width) / 2;
            int y = (getHeight() - size.height) / 2;
            g.drawImage(image, x, y, this);
        }
    }

    public void setImage(BufferedImage image) {
        if (image == null) {
            size.setSize(0, 0);
        } else {
            size.setSize(image.getWidth(), image.getHeight());
        }
        this.image = image;
    }

}
