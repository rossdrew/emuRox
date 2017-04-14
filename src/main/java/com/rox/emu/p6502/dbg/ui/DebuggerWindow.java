package com.rox.emu.p6502.dbg.ui;

import com.rox.emu.Memory;
import com.rox.emu.p6502.CPU;
import com.rox.emu.p6502.Registers;
import com.rox.emu.SimpleMemory;
import javax.swing.*;
import java.awt.*;

import static com.rox.emu.p6502.InstructionSet.*;
import static com.rox.emu.p6502.InstructionSet.OP_BNE;
import static com.rox.emu.p6502.InstructionSet.OP_CPX_I;

/**
 * A DebuggerWindow for debugging 6502 CPU code
 *
 * @author Ross Drew
 */
public class DebuggerWindow extends JFrame{
    private CPU processor;
    private Memory memory;

    private final RegistersPanel registersPanel = new RegistersPanel();
    private final MemoryPanel memoryPanel = new MemoryPanel();

    private String instructionName = "...";
    private final JLabel instruction = new JLabel(instructionName);

    private final DefaultListModel<String> listModel;

    public DebuggerWindow() {
        super("6502 Debugger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 500);

        listModel = new DefaultListModel<>();

        setLayout(new BorderLayout());

        instruction.setHorizontalAlignment(JLabel.CENTER);

        add(instruction, BorderLayout.NORTH);
        add(getInstructionScroller(), BorderLayout.EAST);
        add(getControlPanel(), BorderLayout.SOUTH);
        add(getMemoryPanel(), BorderLayout.WEST);
        add(registersPanel, BorderLayout.CENTER);

        loadProgram(getProgram());
        setVisible(true);
    }

    private JScrollPane getMemoryPanel(){
        JScrollPane p = new JScrollPane(memoryPanel);
        p.setViewportView(memoryPanel);
        return p;
    }

    private JScrollPane getInstructionScroller(){
        JList<String> instructionList = new JList<>(listModel);
        JScrollPane instructionScroller = new JScrollPane(instructionList);
        return instructionScroller;
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

        int[] countToTenProgram = new int[] {   OP_LDX_I, 10,
                                                OP_LDA_I, 0,
                                                OP_CLC,
                                                OP_ADC_I, 0x01,
                                                OP_DEX,
                                                OP_CPX_I, 0,
                                                OP_BNE, 0b11110111
                                            };

        int[] program = new int[]{  OP_LDA_I, valMPD,
                                    OP_STA_Z, MPD,
                                    OP_LDA_I, valMPR,
                                    OP_STA_Z, MPR,
                                    OP_LDA_I, 0,         //<---- start
                                    OP_STA_Z, TMP,       //Clear
                                    OP_STA_Z, RESAD_0,   //...
                                    OP_STA_Z, RESAD_1,   //...
                                    OP_LDX_I, 8,         //X counts each bit
                            //:MULT(18)
                                    OP_LSR_Z, MPR,       //LSR(MPR)
                                    OP_BCC, 13,          //Test carry and jump (forward 13) to NOADD

                                    OP_LDA_Z, RESAD_0,   //RESAD -> A
                                    OP_CLC,              //Prepare to add
                                    OP_ADC_Z, MPD,       //+MPD
                                    OP_STA_Z, RESAD_0,   //Save result
                                    OP_LDA_Z, RESAD_1,   //RESAD+1 -> A
                                    OP_ADC_Z, TMP,       //+TMP
                                    OP_STA_Z, RESAD_1,   //RESAD+1 <- A
                            //:NOADD(35)
                                    OP_ASL_Z, MPD,       //ASL(MPD)
                                    OP_ROL_Z, TMP,       //Save bit from MPD
                                    OP_DEX,              //--X
                                    OP_BNE, 0b11100111   //Test equal and jump (back 24) to MULT});
        };

        return program;
    }

    public void loadProgram(int[] program){
        memory = new SimpleMemory();
        memory.setMemory(0, program);
        processor = new CPU(memory);
        registersPanel.setRegisters(processor.getRegisters());
        memoryPanel.setMemory(memory);
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

        instructionName = getOpCodeName(instr);
        instruction.setText(instructionName);
        listModel.add(0, instructionName);

        processor.step();
        invalidate();
        repaint();
    }

    public static void main(String[] args){
        new DebuggerWindow();
    }

    private class MemoryPanel extends JPanel {
        private Memory memory;

        private int fontSize = 11;

        private void setMemory(Memory memory){
            this.memory = memory;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            Font previousFont = g.getFont();
            Color previousColor = g.getColor();

            setPreferredSize(new Dimension(200,3000));
            setMinimumSize(new Dimension(200,3000));

            drawMemory(g, "Zero Page", 0, 256);

            g.setFont(previousFont);
            g.setColor(previousColor);
        }

        private void drawMemory(Graphics g, String memoryBlockTitle, int from, int to) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
            g.drawChars(memoryBlockTitle.toCharArray(), 0,memoryBlockTitle.length(), 10, 10);
            int[] zeroPage = memory.getBlock(from, to);
            int blockSize = 2;
            for (int i=0; i < zeroPage.length; i+=blockSize){
                String location = asHex(i);
                String memoryAddress = "[" + location + "]";

                for (int j=0; j<blockSize; j++){
                    String value = asHex(zeroPage[i+j]);
                    memoryAddress += " " + value;
                }

                g.drawChars(memoryAddress.toCharArray(), 0, memoryAddress.length(), 10, 20 + (i*10));
            }
        }

        private String asHex(Integer val){
            String hex = Integer.toHexString(val).toUpperCase();
            hex = "0x" + (hex.length() % 2 == 1 ? "0" : "") + hex;
            return hex;
        }

    }

    private class RegistersPanel extends JPanel {
        private Registers registers;

        private final int bitSize = 40;
        private final int byteSize = (bitSize*8);
        private final int padding = 10;
        private final int bitFontSize = 40;
        private final int valueFontSize = 10;

        @Override
        public void paint(Graphics g) {
            super.paint(g);
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
}
