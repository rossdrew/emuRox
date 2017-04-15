package com.rox.emu.p6502.dbg.ui.component;

import com.rox.emu.Memory;

import javax.swing.*;
import java.awt.*;

/**
 * A UI panel inteded to display a block of memory
 */
public class MemoryPanel extends JPanel {
    private Memory memory;

    private final int fontSize = 11;

    public void setMemory(Memory memory){
        this.memory = memory;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        final Font previousFont = g.getFont();
        final Color previousColor = g.getColor();

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

        drawIndexedBlock(g, memoryBlock, columns, rowSize, columnSize);
    }

    private void drawIndexedBlock(Graphics g, int[] memoryBlock, int columns, int rowSize, int columnSize) {
        for (int i=0; i<memoryBlock.length; i++){
            final int column = (i+1) % columns;
            final int row = i / columns;
            final int rowLoc = (row * rowSize);
            final int colLoc = (column + 1) * columnSize;

            if (column == 0){
                drawIndex(g, i, rowLoc);
            }

            if (memoryBlock[i] != 0x0){
                drawEmphasisedValue(g, rowLoc, colLoc, asHex(memoryBlock[i]));
            }else {
                drawValue(g, rowLoc, colLoc, asHex(memoryBlock[i]));
            }
        }
    }

    private void drawIndex(Graphics g, int i, int rowLoc) {
        final String memAddressDisplay = "[" + asHex(i) + "]";
        drawValue(g, rowLoc, 0, memAddressDisplay);
    }

    private void drawEmphasisedValue(Graphics g, int rowLoc, int colLoc, String memValueDisplay) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        drawValue(g, rowLoc, colLoc, memValueDisplay);
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
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