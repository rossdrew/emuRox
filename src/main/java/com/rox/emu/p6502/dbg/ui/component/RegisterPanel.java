package com.rox.emu.p6502.dbg.ui.component;

import com.rox.emu.p6502.Registers;

import javax.swing.*;
import java.awt.*;

/**
 * A UI component intended to display 6502 registers and their state
 */
public class RegisterPanel extends JPanel {
    private Registers registers;

    private final int bitSize = 40;
    private final int byteSize = (bitSize*8);
    private final int padding = 10;
    private final int bitFontSize = 40;
    private final int valueFontSize = 11;

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        setPreferredSize(new Dimension(1200,0));
        setMinimumSize(new Dimension(1200,0));

        if (registers != null)
            drawRegisters(g, 20, 20);
    }

    private void drawRegisters(Graphics g, int x, int y) {
        int yLocation = y;
        int xLocation = x;
        int rowSize = padding + bitSize;
        int secondByteColumn = byteSize + xLocation + padding;

        drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_ACCUMULATOR), Registers.getRegisterName(Registers.REG_ACCUMULATOR));

        yLocation += rowSize;
        drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_Y_INDEX), Registers.getRegisterName(Registers.REG_Y_INDEX));

        yLocation += rowSize;
        drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_X_INDEX), Registers.getRegisterName(Registers.REG_X_INDEX));

        //TODO this needs a combined value display
        yLocation += rowSize;
        drawByte(g, xLocation, yLocation, registers.getRegister(Registers.REG_PC_HIGH), Registers.getRegisterName(Registers.REG_PC_HIGH));
        drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_PC_LOW), Registers.getRegisterName(Registers.REG_PC_LOW));

        yLocation += rowSize;
        g.setColor(Color.lightGray);
        g.fillRect(secondByteColumn + (2 * bitSize), yLocation, bitSize, bitSize);
        drawFlags(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_STATUS), "NV BDIZC".toCharArray());
    }

    private void drawFlags(Graphics g, int startX, int startY, int byteValue, char[] values){
        char[] bitValues = to8BitString(byteValue).toCharArray();

        g.setColor(Color.lightGray);
        for (int i=0; i<8; i++){
            g.setColor((bitValues[i] == '1') ? Color.black : Color.lightGray);
            drawBit(g, startX + (i*bitSize), startY, values[i]);
        }
        g.setColor(Color.BLACK);
        g.drawRect(startX, startY, byteSize, bitSize);
    }

    private void drawByte(Graphics g, int startX, int startY, int byteValue, String name){
        char[] bitValues = to8BitString(byteValue).toCharArray();

        g.setColor(Color.lightGray);
        for (int i=0; i<8; i++){
            drawBit(g, startX + (i*bitSize), startY, bitValues[i]);
        }
        g.setColor(Color.BLACK);
        g.drawRect(startX, startY, byteSize, bitSize);

        g.setColor(Color.RED);

        g.setFont(new Font("Courier New", Font.PLAIN, valueFontSize));
        String values = "(" + fromSignedByte(byteValue) + ", 0x" + Integer.toHexString(byteValue) + ")";
        g.drawChars(values.toCharArray(), 0, values.length(), (startX+byteSize-bitSize), startY-1);

        g.setColor(Color.blue);
        g.drawChars(name.toCharArray(), 0, name.length(), startX, startY);
    }

    private void drawBit(Graphics g, int startX, int startY, char val){
        g.setFont(new Font("Courier New", Font.PLAIN, bitFontSize));
        g.drawRect(startX, startY, bitSize, bitSize);
        //XXX Don't like these numbers, they're not relative to anything
        g.drawChars(new char[] {val}, 0, 1, startX+5, startY+35);
    }

    public void setRegisters(Registers registers) {
        this.registers = registers;
    }

    private int fromSignedByte(int signedByte){
        int signedByteByte = signedByte & 0xFF;
        if ((signedByteByte & 128) == 128)
            return -( ( (~signedByteByte) +1) & 0xFF); //Twos compliment compensation
        else
            return signedByteByte & 0b01111111;
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
}