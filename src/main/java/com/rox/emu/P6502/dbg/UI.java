package com.rox.emu.P6502.dbg;

import com.rox.emu.Memory;
import com.rox.emu.P6502.CPU;
import com.rox.emu.P6502.Registers;
import com.rox.emu.SimpleMemory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.rox.emu.P6502.InstructionSet.*;
import static com.rox.emu.P6502.InstructionSet.OP_LDA_Z_IX;

/**
 * A UI for debugging 6502 CPU code
 *
 * @author Ross Drew
 */
public class UI extends JFrame{
    private CPU processor;
    private Memory memory;

    private RegistersPanel registersPanel = new RegistersPanel();

    public UI(){
        super("6502 Debugger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,500);
        setVisible(true);

        setLayout(new BorderLayout());

        add(registersPanel, BorderLayout.CENTER);

        JButton stepButton = new JButton("Step >>");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step();
            }
        });
        add(stepButton, BorderLayout.SOUTH);

        loadProgram(new int[] {OP_LDA_I, 99});
    }

    public void loadProgram(int[] program){
        memory = new SimpleMemory(65534);
        memory.setMemory(0, program);
        processor = new CPU(memory);
        registersPanel.setRegisters(processor.getRegisters());
        reset();
    }

    public void reset(){
        processor.reset();
        registersPanel.invalidate();
    }

    public void step(){
        processor.step();
        registersPanel.invalidate();
    }

    public static void main(String[] args){
        UI debugger = new UI();
    }

    private class RegistersPanel extends JPanel {
        private Registers registers;

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
                //g.drawChars(new char[] {'N'}, 0, 1, startX + (i*bitSize) + bitSize/2, startY + bitSize/2);
            }
            g.setColor(Color.BLACK);
            g.drawRect(startX, startY, byteSize, bitSize);
        }

        private void drawBit(Graphics g, int startX, int startY){
            g.drawRect(startX, startY, bitSize, bitSize);
        }

        public void setRegisters(Registers registers) {
            this.registers = registers;
        }
    }
}
