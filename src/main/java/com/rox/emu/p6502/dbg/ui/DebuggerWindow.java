package com.rox.emu.p6502.dbg.ui;

import com.rox.emu.Memory;
import com.rox.emu.p6502.CPU;
import com.rox.emu.p6502.Registers;
import com.rox.emu.SimpleMemory;
import javax.swing.*;
import java.awt.*;

import com.rox.emu.p6502.dbg.ui.component.*;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;

import static com.rox.emu.p6502.InstructionSet.*;
import static com.rox.emu.p6502.InstructionSet.OP_BNE;
import static com.rox.emu.p6502.InstructionSet.OP_CPX_I;

/**
 * A DebuggerWindow for debugging 6502 CPU code
 *
 * @author Ross Drew
 */
public class DebuggerWindow extends JFrame {
    private CPU processor;
    private Memory memory;

    private final RegisterPanel registersPanel = new RegisterPanel();
    private final MemoryPanel memoryPanel = new MemoryPanel();

    private String instructionName = "...";
    private final JLabel instruction = new JLabel(instructionName);

    private final DefaultListModel<String> listModel;

    public DebuggerWindow() {
        super("6502 Debugger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 500);

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
        p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return p;
    }

    private JComponent getInstructionScroller(){
        JPanel instructionScrollerPanel = new JPanel();

        final JList<String> instructionList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(instructionList);
        return scrollPane;
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
        memoryPanel.linkTo(processor.getRegisters());
        reset();
    }

    public void reset(){
        processor.reset();
        listModel.clear();
        invalidate();
        revalidate();
        repaint();
    }

    public void step(){
        //Get the next instruction
        final Registers registers = processor.getRegisters();
        final int pointer = registers.getRegister(Registers.REG_PC_LOW) | (registers.getRegister(Registers.REG_PC_HIGH) << 8);
        final int instr = memory.getByte(pointer);
        final int args = getArgumentCount(instr);

        String arguments = "";
        if (args == 1)
            arguments += " " + MemoryPanel.asHex(memory.getByte(pointer + 1));
        if (args == 2)
            arguments += " " + MemoryPanel.asHex(memory.getByte(pointer + 2));

        instructionName = getOpCodeName(instr);
        final String instructionLocation = MemoryPanel.asHex(pointer);
        final String instructionCode = MemoryPanel.asHex(instr);
        final String completeInstructionInfo = "[" + instructionLocation + "] (" + instructionCode + arguments + ") :" + instructionName;

        instruction.setText(completeInstructionInfo);
        listModel.add(0, completeInstructionInfo);

        processor.step();
        invalidate();
        repaint();
    }

    private int getArgumentCount(int instr) {
        final OpCode opCode = OpCode.from(instr);
        final AddressingMode addressingMode = opCode.getAddressingMode();
        return addressingMode.getInstructionBytes() - 1;
    }

    public static void main(String[] args){
        new DebuggerWindow();
    }

}
