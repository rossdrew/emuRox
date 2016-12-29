package com.rox.emu.P6502.dbg;

import javax.swing.*;
import java.awt.*;

/**
 * A UI for debugging 6502 CPU code
 *
 * @author Ross Drew
 */
public class UI extends JFrame{
    public UI(){
        super("6502 Debugger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500,500);
        setVisible(true);

        add(new RegistersPanel());
    }

    public static void main(String[] args){
        UI debugger = new UI();
    }

    private class RegistersPanel extends JPanel {
        private int bitSize = 20;
        private int byteSize = (bitSize*8);
        private int padding = 2;

        private void drawByte(Graphics g, int startX, int startY){
            g.setColor(Color.lightGray);
            for (int i=1; i<7; i++){
                drawBit(g, startX + (i*bitSize), startY);
            }
            g.setColor(Color.BLACK);
            g.drawRect(startX, startY, byteSize, bitSize); //F
        }

        private void drawBit(Graphics g, int startX, int startY){
            g.drawRect(startX, startY, bitSize, bitSize);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            g.setColor(Color.BLACK);

            int yLocation = 20;
            int xSecondByte = byteSize + bitSize + padding;
            drawByte(g, xSecondByte, yLocation);

            yLocation += padding + bitSize;
            drawByte(g, xSecondByte, yLocation);

            yLocation += padding + bitSize;
            drawByte(g, xSecondByte, yLocation);

            yLocation += padding + bitSize;
            drawByte(g, bitSize, yLocation);
            drawByte(g, xSecondByte, yLocation);

            yLocation += padding + bitSize;
            g.setColor(Color.lightGray);
            for (int i=1; i<7; i++){
                if (i==2)
                    g.fillRect(xSecondByte+(i*bitSize), yLocation, bitSize, bitSize);
                else
                    g.drawRect(xSecondByte+(i*bitSize), yLocation, bitSize, bitSize);
            }
            g.setColor(Color.BLACK);
            g.drawRect(xSecondByte, yLocation, byteSize, bitSize);
        }
    }
}
