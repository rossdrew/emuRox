package com.rox.emu.p6502.dbg.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 *  A UI component that displays a byte as a series of bits with an
 * identification name and Dec/Hex values displayed.
 */
public class ByteBox extends JPanel {
    private final int bitSize = 40;
    private final int byteSize = (bitSize*8);
    private final int bitFontSize = 40;
    private final int valueFontSize = 11;

    private int byteValue = 0b00000000;
    private String byteName = "Unknown";

    public ByteBox(String byteName, int initialValue){
        this.byteValue = initialValue;
        this.byteName = byteName;
    }

    public void setValue(int newValue){
        this.byteValue = newValue;

        refresh();
    }

    private void refresh() {
        invalidate();
        revalidate();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        turnOnClearText(g);

        drawByte(g, 0, bitFontSize, byteValue, byteName);
    }

    private void turnOnClearText(Graphics g) {
        if (g instanceof Graphics2D){
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
    }

    private void drawByte(Graphics g, int startX, int startY, int byteValue, String name){
        char[] bitValues = to8BitString(byteValue).toCharArray();

        paintBits(g, startX, startY, bitValues);
        paintByteBorder(g, startX, startY);
        paintByteName(g, startX, startY, name);
        paintByteValues(g, startX, startY, byteValue);

    }

    private void paintBits(Graphics g, int startX, int startY, char[] bitValues) {
        g.setColor(Color.lightGray);
        for (int i=0; i<8; i++){
            paintBit(g, startX + (i*bitSize), startY, bitValues[i]);
        }
    }

    private void paintBit(Graphics g, int startX, int startY, char val){
        final int padding = 5;

        g.setFont(new Font("Courier New", Font.PLAIN, bitFontSize));
        g.drawRect(startX, startY, bitSize, bitSize);
        g.drawString(""+val, startX+padding, startY+(bitSize-padding));
    }

    private void paintByteBorder(Graphics g, int startX, int startY) {
        g.setColor(Color.BLACK);
        g.drawRect(startX, startY, byteSize, bitSize);
    }

    private void paintByteName(Graphics g, int startX, int startY, String name) {
        g.setColor(Color.blue);
        g.setFont(new Font("Courier New", Font.PLAIN, valueFontSize));
        g.drawString(name, startX, startY-1);
    }

    private void paintByteValues(Graphics g, int startX, int startY, int byteValue) {
        g.setColor(Color.RED);
        g.setFont(new Font("Courier New", Font.PLAIN, valueFontSize));
        String values = "(" + fromSignedByte(byteValue) + ", 0x" + Integer.toHexString(byteValue) + ")";
        g.drawString(values, (startX + byteSize - bitSize - (values.length() * (valueFontSize/2))), startY-1);
    }

    private String to8BitString(int fakeByte){
        String formattedByteString = Integer.toBinaryString(fakeByte);
        if (formattedByteString.length() < 8){
            for (int i=formattedByteString.length(); i<8; i++){
                formattedByteString = "0" + formattedByteString;
            }
        }
        return formattedByteString;
    }

    private int fromSignedByte(int signedByte){
        int signedByteByte = signedByte & 0xFF;
        if ((signedByteByte & 128) == 128)
            return -( ( (~signedByteByte) +1) & 0xFF); //Twos compliment compensation
        else
            return signedByteByte & 0b01111111;
    }
}
