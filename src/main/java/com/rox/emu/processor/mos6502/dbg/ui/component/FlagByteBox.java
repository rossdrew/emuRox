package com.rox.emu.processor.mos6502.dbg.ui.component;

import java.awt.*;

/**
 * A {@link ByteBox} that represents a status register with added character
 * representation of bits and highlights for flagged or non-flagged
 *
 * @author Ross Drew
 */
public class FlagByteBox extends ByteBox {
    private final char[] flagIDs;

    public FlagByteBox(String byteName, int initialValue, char[] flagIDs) {
        super(byteName, initialValue);
        this.flagIDs = flagIDs;
    }

    @Override
    protected void paintBits(Graphics g, Point point, char[] bitValues) {
        for (int i=0; i<8; i++){
            g.setColor((bitValues[i] == '1') ? Color.black : Color.lightGray);
            paintBit(g, new Point(point.x + (i*bitSize), point.y), flagIDs[i]);
        }
    }

    @Override
    protected void paintBit(Graphics g, Point point, char val){
        final int padding = 5;

        g.setFont(new Font("Courier New", Font.PLAIN, bitFontSize));
        g.drawString("" + val, point.x + padding, point.y + (bitSize - padding));

        g.setColor(Color.lightGray);

        if (val == ' ')
            g.fillRect(point.x, point.y, bitSize, bitSize);
        else
            g.drawRect(point.x, point.y, bitSize, bitSize);
    }
}
