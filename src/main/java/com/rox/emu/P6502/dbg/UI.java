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
        private int bitSize = 40;
        private int byteSize = (bitSize*8);
        private int padding = 4;

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            drawRegisters(g, 20, 20);
        }

        private void drawRegisters(Graphics g, int xLocation, int yLocation) {
            int rowSize = padding + bitSize;
            int xSecondByte = byteSize + xLocation + padding;
            drawByte(g, xSecondByte, yLocation);

            yLocation += rowSize;
            drawByte(g, xSecondByte, yLocation);

            yLocation += rowSize;
            drawByte(g, xSecondByte, yLocation);

            yLocation += rowSize;
            drawByte(g, xLocation, yLocation);
            drawByte(g, xSecondByte, yLocation);

            yLocation += rowSize;
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

        private void drawByte(Graphics g, int startX, int startY){
            g.setColor(Color.lightGray);
            for (int i=1; i<7; i++){
                drawBit(g, startX + (i*bitSize), startY);
                //   g.drawChars(new char[] {'N'}, 0, 1, startX + (i*bitSize) + bitSize/2, startY + bitSize/2);
            }
            g.setColor(Color.BLACK);
            g.drawRect(startX, startY, byteSize, bitSize);
        }

        private void drawBit(Graphics g, int startX, int startY){
            g.drawRect(startX, startY, bitSize, bitSize);
        }
    }
}
