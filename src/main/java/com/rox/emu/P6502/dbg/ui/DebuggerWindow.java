package com.rox.emu.P6502.dbg.ui;

import com.rox.emu.Memory;
import com.rox.emu.P6502.CPU;
import com.rox.emu.P6502.InstructionSet;
import com.rox.emu.P6502.Registers;
import com.rox.emu.SimpleMemory;

import javax.swing.*;
import java.awt.*;

import static com.rox.emu.P6502.InstructionSet.*;

/**
 * A DebuggerWindow for debugging 6502 CPU code
 *
 * @author Ross Drew
 */
public class DebuggerWindow extends JFrame{
    private CPU processor;
    private Memory memory;

    private RegistersPanel registersPanel = new RegistersPanel();

    private String instructionName = "...";
    private JLabel instruction = new JLabel(instructionName);

    private DefaultListModel listModel;

    public DebuggerWindow() {
        super("6502 Debugger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 500);

        listModel= new DefaultListModel();

        setLayout(new BorderLayout());

        instruction.setHorizontalAlignment(JLabel.CENTER);

        add(instruction, BorderLayout.NORTH);
        add(registersPanel, BorderLayout.CENTER);
        add(getControlPanel(), BorderLayout.SOUTH);

        JList instructionList = new JList<>(listModel);
        JScrollPane instructionScroller = new JScrollPane(instructionList);
        add(instructionScroller, BorderLayout.EAST);

        loadProgram(getProgram());
        setVisible(true);
    }

    private JPanel getControlPanel() {
        JButton stepButton = new JButton("Step >>");
        stepButton.addActionListener(e -> step());

        JButton resetButton = new JButton("Reset!");
        resetButton.addActionListener(e -> reset());

        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout());
        controls.add(resetButton);
        controls.add(stepButton);
        return controls;
    }

    private int[] getProgram(){
        int data_offset = 0x32;
        int MPD = data_offset + 0x10;
        int MPR = data_offset + 0x11;
        int TMP = data_offset + 0x20;
        int RESAD_0 = data_offset + 0x30;
        int RESAD_1 = data_offset + 0x31;

        int valMPD = 7;
        int valMPR = 4;

        int[] program = new int[]{  OP_LDA_I, valMPD,
                                    OP_STA_Z, MPD,
                                    OP_LDA_I, valMPR,
                                    OP_STA_Z, MPR,
                                    OP_LDA_I, 0,         //<---- start
                                    OP_STA_Z, TMP,       //Clear
                                    OP_STA_Z, RESAD_0,   //...
                                    OP_STA_Z, RESAD_1,   //...
                                    OP_LDX_I, 8,         //X counts each bit
                                    OP_LSR_Z, MPR,       //:MULT(18) LSR(MPR)
                                    OP_BCC, 13,          //Test carry and jump (forward 13) to NOADD
                                    OP_LDA_Z, RESAD_0,   //RESAD -> A
                                    OP_CLC,              //Prepare to add
                                    OP_ADC_Z, MPD,       //+MPD
                                    OP_STA_Z, RESAD_0,   //Save result
                                    OP_LDA_Z, RESAD_1,   //RESAD+1 -> A
                                    OP_ADC_Z, TMP,       //+TMP
                                    OP_STA_Z, RESAD_1,   //RESAD+1 <- A
                                    OP_ASL_Z, MPD,       //:NOADD(35) ASL(MPD)
                                    OP_ROL_Z, TMP,       //Save bit from MPD
                                    OP_DEX,              //--X
                                    OP_BNE, 0b11100111   //Test equal and jump (back 24) to MULT});
        };

        return program;
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
        listModel.clear();
        invalidate();
        repaint();
    }

    public void step(){
        //Get the next instruction
        Registers registers = processor.getRegisters();
        int pointer = registers.getRegister(Registers.REG_PC_LOW) | (registers.getRegister(Registers.REG_PC_HIGH) << 8);
        int instr = memory.getByte(pointer);
        //TODO get arguments

        instructionName = InstructionSet.getName(instr);
        instruction.setText(instructionName);
        listModel.add(0, instructionName);

        processor.step();
        invalidate();
        repaint();
    }

    public static void main(String[] args){
        DebuggerWindow debugger = new DebuggerWindow();
    }

    private class RegistersPanel extends JPanel {
        private Registers registers;

        private int bitSize = 40;
        private int byteSize = (bitSize*8);
        private int padding = 10;
        private int bitFontSize = 40;
        private int valueFontSize = 10;

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (registers != null)
                drawRegisters(g, 20, 20);
        }

        private void drawRegisters(Graphics g, int xLocation, int yLocation) {
            int rowSize = padding + bitSize;
            int secondByteColumn = byteSize + xLocation + padding;

            drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_ACCUMULATOR), "Accumulator");

            yLocation += rowSize;
            drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_Y_INDEX), "Y Index");

            yLocation += rowSize;
            drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_X_INDEX), "X Index");

            //TODO this needs a combined value display
            yLocation += rowSize;
            drawByte(g, xLocation, yLocation, registers.getRegister(Registers.REG_PC_HIGH), "PC High");
            drawByte(g, secondByteColumn, yLocation, registers.getRegister(Registers.REG_PC_LOW), "PC Low");

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
            signedByte &= 0xFF;
            if ((signedByte & 128) == 128)
                return -( ( (~signedByte) +1) & 0xFF); //Twos compliment compensation
            else
                return signedByte & 0b01111111;
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
}