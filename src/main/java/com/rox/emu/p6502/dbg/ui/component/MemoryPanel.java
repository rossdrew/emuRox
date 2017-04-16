package com.rox.emu.p6502.dbg.ui.component;

import com.rox.emu.Memory;
import com.rox.emu.p6502.Registers;

import javax.swing.*;
import java.awt.*;

/**
 * A UI panel intended to display a block of memory
 */
public class MemoryPanel extends JPanel {
    private Memory memory;
    private Registers registers;

    private int blockSize = 256;

    private final int fontSize = 12;

    private final int rowSize = fontSize;
    private final int columnSize = fontSize * 4;
    private final int verticalPadding = 10;

    private final int componentHeight = rowSize * (256 / 4);
    private final int componentWidth = columnSize * 5;

    private final Font emphasisFont = new Font("Monospaced", Font.BOLD, fontSize);
    private final Color emphasisColor = Color.BLACK;
    private final Font standardFont = new Font("Monospaced", Font.PLAIN, fontSize);
    private final Color standardColor = Color.GRAY;
    private final Color currentInstructionColor = new Color(9, 178, 0);
    private final Color addressColor = new Color(201, 0, 12);

    public void setMemory(Memory memory){
        this.memory = memory;
    }

    public void linkTo(Registers registers){
        this.registers = registers;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        final Font previousFont = g.getFont();
        final Color previousColor = g.getColor();

        setPreferredSize(new Dimension(componentWidth, componentHeight));

        drawMemory(g, 0, blockSize);

        setTextFormatting(g, previousFont, previousColor);
    }

    private void drawMemory(Graphics g, int from, int to) {
        setTextFormatting(g, standardFont, standardColor);

        final int[] memoryBlock = memory.getBlock(from, to);

        drawIndexedBlock(g, memoryBlock, 4);
    }

    private void drawIndexedBlock(Graphics g, int[] memoryBlock, int columns) {
        int pcLoLocation = -1;
        if (registers!=null)
            pcLoLocation = registers.getRegister(Registers.REG_PC_LOW);

        for (int i=0; i<memoryBlock.length; i++){
            final int column = i % columns;
            final int row = i / columns;
            final int rowLoc = verticalPadding + (row * rowSize);
            final int colLoc = ((column + 1) * columnSize);

            if (column == 0){
                drawIndex(g, i, rowLoc);
            }

            if (i == pcLoLocation){
                setTextFormatting(g, emphasisFont, currentInstructionColor);
                drawValue(g, colLoc, rowLoc, asHex(memoryBlock[i]));
                setTextFormatting(g, standardFont, standardColor);
            }else if (memoryBlock[i] != 0x0){
                drawEmphasisedValue(g, rowLoc, colLoc, asHex(memoryBlock[i]));
            }else {
                drawValue(g, colLoc, rowLoc, asHex(memoryBlock[i]));
            }
        }
    }

    private void drawIndex(Graphics g, int i, int rowLoc) {
        final String memAddressDisplay = "[" + asHex(i) + "]";

        setTextFormatting(g, emphasisFont, addressColor);
        drawValue(g, 0, rowLoc, memAddressDisplay);
        setTextFormatting(g, standardFont, standardColor);
    }

    private void drawEmphasisedValue(Graphics g, int rowLoc, int colLoc, String memValueDisplay) {
        setTextFormatting(g, emphasisFont, emphasisColor);
        drawValue(g, colLoc, rowLoc, memValueDisplay);
        setTextFormatting(g, standardFont, standardColor);
    }

    private void drawValue(Graphics g, int x, int y, String memValueDisplay) {
        g.drawChars(memValueDisplay.toCharArray(), 0, memValueDisplay.length(), x, y);
    }

    private void setTextFormatting(Graphics g, Font font, Color color){
        g.setColor(color);
        g.setFont(font);
    }

    public static String asHex(Integer val){
        String hex = Integer.toHexString(val).toUpperCase();
        hex = "0x" + (hex.length() % 2 == 1 ? "0" : "") + hex;
        return hex;
    }

}