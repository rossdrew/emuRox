package com.rox.emu.processor.mos6502.dbg.ui.component;

import java.awt.*;

/**
 * A {@link ByteBox} that represents a status register with added character
 * representation of bits and highlights for flagged or non-flagged
 */
public class FlagByteBox extends ByteBox {
    private final char[] flagIDs;

    public FlagByteBox(String byteName, int initialValue, char[] flagIDs) {
        super(byteName, initialValue);
        this.flagIDs = flagIDs;
    }

    @Override
    protected void paintBits(Graphics g, int startX, int startY, char[] bitValues) {
        for (int i=0; i<8; i++){
            g.setColor((bitValues[i] == '1') ? Color.black : Color.lightGray);
            paintBit(g, startX + (i*bitSize), startY, flagIDs[i]);
        }
    }

    @Override
    protected void paintBit(Graphics g, int startX, int startY, char val){
        final int padding = 5;

        g.setFont(new Font("Courier New", Font.PLAIN, bitFontSize));
        g.drawString("" + val, startX + padding, startY + (bitSize - padding));

        g.setColor(Color.lightGray);

        if (val == ' ')
            g.fillRect(startX, startY, bitSize, bitSize);
        else
            g.drawRect(startX, startY, bitSize, bitSize);
    }
}
