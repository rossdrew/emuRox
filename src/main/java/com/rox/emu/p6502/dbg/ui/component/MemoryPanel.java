package com.rox.emu.p6502.dbg.ui.component;

import com.rox.emu.Memory;

import javax.swing.*;
import java.awt.*;

/**
 * A UI panel intended to display a block of memory
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

        drawIndexedBlock(g, memoryBlock, 4);
    }

    private void drawIndexedBlock(Graphics g, int[] memoryBlock, int columns) {
        final int rowSize = 10;         //XXX Should be based on font size
        final int columnSize = 40;      //XXX Should be based on font size
        final int verticalPadding = 10;

        for (int i=0; i<memoryBlock.length; i++){
            final int column = i % columns;
            final int row = i / columns;
            final int rowLoc = verticalPadding + (row * rowSize);
            final int colLoc = ((column + 1) * columnSize);

            if (column == 0){
                drawIndex(g, i, rowLoc);
            }

            if (memoryBlock[i] != 0x0){
                drawEmphasisedValue(g, rowLoc, colLoc, asHex(memoryBlock[i]));
            }else {
                drawValue(g, colLoc, rowLoc, asHex(memoryBlock[i]));
            }
        }
    }

    private void drawIndex(Graphics g, int i, int rowLoc) {
        final String memAddressDisplay = "[" + asHex(i) + "]";
        drawValue(g, 0, rowLoc, memAddressDisplay);
    }

    private void drawEmphasisedValue(Graphics g, int rowLoc, int colLoc, String memValueDisplay) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        drawValue(g, colLoc, rowLoc, memValueDisplay);
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
    }

    private void drawValue(Graphics g, int x, int y, String memValueDisplay) {
        g.drawChars(memValueDisplay.toCharArray(), 0, memValueDisplay.length(), x, y);
    }

    private String asHex(Integer val){
        String hex = Integer.toHexString(val).toUpperCase();
        hex = "0x" + (hex.length() % 2 == 1 ? "0" : "") + hex;
        return hex;
    }

}