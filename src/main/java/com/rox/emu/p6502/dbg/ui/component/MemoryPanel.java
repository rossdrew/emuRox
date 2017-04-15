package com.rox.emu.p6502.dbg.ui.component;

import com.rox.emu.Memory;

import javax.swing.*;
import java.awt.*;

public class MemoryPanel extends JPanel {
    private Memory memory;

    private int fontSize = 11;

    public void setMemory(Memory memory){
        this.memory = memory;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Font previousFont = g.getFont();
        Color previousColor = g.getColor();

        setPreferredSize(new Dimension(210,3000));
        setMinimumSize(new Dimension(210,3000));

        drawMemory(g, 0, 256);

        g.setFont(previousFont);
        g.setColor(previousColor);
    }

    private void drawMemory(Graphics g, int from, int to) {
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, fontSize));

        final int[] memoryBlock = memory.getBlock(from, to);
        final int columns = 4;
        final int rowSize = 10;         //XXX Should be based on font size
        final int columnSize = 40;      //XXX Should be based on font size
        for (int i=0; i<memoryBlock.length; i++){
            final int column = (i+1) % columns;
            final int row = i / columns;
            final int rowLoc = (row * rowSize);
            final int colLoc = (column + 1) * columnSize;

            if (column == 0){
                final String memAddressDisplay = "[" + asHex(i) + "]";
                drawValue(g, rowLoc, 0, memAddressDisplay);
            }

            final String memValueDisplay = asHex(memoryBlock[i]);
            if (memoryBlock[i] != 0x0){
                g.setColor(Color.BLACK);
                g.setFont(new Font("Monospaced", Font.BOLD, fontSize));
                drawValue(g, rowLoc, colLoc, memValueDisplay);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
            }else {
                drawValue(g, rowLoc, colLoc, memValueDisplay);
            }


        }
    }

    private void drawValue(Graphics g, int y, int x, String memValueDisplay) {
        g.drawChars(memValueDisplay.toCharArray(), 0, memValueDisplay.length(), x, y);
    }

    private String asHex(Integer val){
        String hex = Integer.toHexString(val).toUpperCase();
        hex = "0x" + (hex.length() % 2 == 1 ? "0" : "") + hex;
        return hex;
    }

}