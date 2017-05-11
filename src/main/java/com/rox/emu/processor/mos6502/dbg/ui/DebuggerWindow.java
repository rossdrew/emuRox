package com.rox.emu.processor.mos6502.dbg.ui;

import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.CPU;
import com.rox.emu.processor.mos6502.dbg.ui.component.Registers6502;
import com.rox.emu.processor.mos6502.util.Program;
import com.rox.emu.processor.mos6502.Registers;
import com.rox.emu.mem.SimpleMemory;
import javax.swing.*;
import java.awt.*;

import com.rox.emu.processor.mos6502.op.AddressingMode;
import com.rox.emu.processor.mos6502.op.OpCode;
import com.rox.emu.processor.mos6502.dbg.ui.component.MemoryPanel;

/**
 * A DebuggerWindow for debugging 6502 CPU code
 *
 * @author Ross Drew
 */
public class DebuggerWindow extends JFrame {
    private CPU processor;
    private Memory memory;

    private Registers6502 newRegisterPanel;

    private final MemoryPanel zeroPageMemoryPanel = new MemoryPanel();
    private final MemoryPanel stackPageMemoryPanel = new MemoryPanel();

    private String instructionName = "...";
    private final JLabel instruction = new JLabel(instructionName);

    private final DefaultListModel<String> listModel;

    public DebuggerWindow() {
        super("6502 Debugger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        init();

        listModel = new DefaultListModel<>();
        instruction.setHorizontalAlignment(JLabel.CENTER);

        setLayout(new BorderLayout());
        add(instruction, BorderLayout.NORTH);
        add(getInstructionScroller(), BorderLayout.EAST);
        add(getControlPanel(), BorderLayout.SOUTH);
        add(getMemoryPanel(), BorderLayout.WEST);
        add(getRegisterPanel(), BorderLayout.CENTER);

        loadProgram(getProgram());
        setVisible(true);
        pack();
    }

    private JComponent getRegisterPanel(){
        return newRegisterPanel;
    }

    private JComponent getMemoryPanel(){
        JScrollPane p0 = new JScrollPane();
        p0.setViewportView(zeroPageMemoryPanel);
        p0.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane p1 = new JScrollPane();
        p1.setViewportView(stackPageMemoryPanel);
        p1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JTabbedPane memoryTabs = new JTabbedPane();
        memoryTabs.addTab("Zero Page", p0);
        memoryTabs.addTab("Stack Page", p1);

        return memoryTabs;
    }

    private JComponent getInstructionScroller(){
        final JList<String> instructionList = new JList<>(listModel);
        final JScrollPane scrollPane = new JScrollPane(instructionList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        return scrollPane;
    }

    private JPanel getControlPanel() {
        JButton stepButton = new JButton("Step >>");
        stepButton.addActionListener(e -> step());

        JButton resetButton = new JButton("Reset!");
        resetButton.addActionListener(e -> loadProgram(getProgram()));

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

        Program countToTenProgram = new Program().with( OpCode.OP_LDX_I, 10,
                                                        OpCode.OP_LDA_I, 0,
                                                        OpCode.OP_CLC,
                                                        OpCode.OP_ADC_I, 0x01,
                                                        OpCode.OP_DEX,
                                                        OpCode.OP_CPX_I, 0,
                                                        OpCode.OP_BNE, 0b11110111);

        Program multiplicationProgram = new Program().with( OpCode.OP_LDA_I, valMPD,
                                                            OpCode.OP_STA_Z, MPD,
                                                            OpCode.OP_LDA_I, valMPR,
                                                            OpCode.OP_STA_Z, MPR,
                                                            OpCode.OP_LDA_I, 0,         //<---- start
                                                            OpCode.OP_STA_Z, TMP,       //Clear
                                                            OpCode.OP_STA_Z, RESAD_0,   //...
                                                            OpCode.OP_STA_Z, RESAD_1,   //...
                                                            OpCode.OP_LDX_I, 8,         //X counts each bit
                                                //:MULT(18)
                                                            OpCode.OP_LSR_Z, MPR,       //LSR(MPR)
                                                            OpCode.OP_BCC, 13,          //Test carry and jump (forward 13) to NOADD

                                                            OpCode.OP_LDA_Z, RESAD_0,   //RESAD -> A
                                                            OpCode.OP_CLC,              //Prepare to add
                                                            OpCode.OP_ADC_Z, MPD,       //+MPD
                                                            OpCode.OP_STA_Z, RESAD_0,   //Save result
                                                            OpCode.OP_LDA_Z, RESAD_1,   //RESAD+1 -> A
                                                            OpCode.OP_ADC_Z, TMP,       //+TMP
                                                            OpCode.OP_STA_Z, RESAD_1,   //RESAD+1 <- A
                                                //:NOADD(35)
                                                            OpCode.OP_ASL_Z, MPD,       //ASL(MPD)
                                                            OpCode.OP_ROL_Z, TMP,       //Save bit from MPD
                                                            OpCode.OP_DEX,              //--X
                                                            OpCode.OP_BNE, 0b11100111   //Test equal and jump (back 24) to MULT
        );

        return multiplicationProgram.getProgramAsByteArray();
    }

    private void init(){
        memory = new SimpleMemory();
        processor = new CPU(memory);

        newRegisterPanel = new Registers6502(processor.getRegisters());

        zeroPageMemoryPanel.setMemory(memory, 0);
        zeroPageMemoryPanel.linkTo(processor.getRegisters());

        stackPageMemoryPanel.setMemory(memory, 256);
        stackPageMemoryPanel.linkTo(processor.getRegisters());
    }

    public void loadProgram(int[] program){
        reset();
        memory.setMemory(0, program);
    }

    public void reset(){
        processor.reset();
        memory.reset();
        listModel.clear();

        invalidate();
        revalidate();
        repaint();
    }

    public void step(){
        upDateWithNextInstruction();

        processor.step();
        invalidate();
        repaint();
    }

    private void upDateWithNextInstruction() {
        final Registers registers = processor.getRegisters();
        final int pointer = registers.getPC();
        final int instr = memory.getByte(pointer);

        String arguments = "";
        for (int i=0; i<getArgumentCount(instr); i++ ){
            arguments += " " + MemoryPanel.asHex(memory.getByte(pointer + (i+1)));
        }

        instructionName = OpCode.from(instr).toString();
        final String instructionLocation = MemoryPanel.asHex(pointer);
        final String instructionCode = MemoryPanel.asHex(instr);
        final String completeInstructionInfo = "[" + instructionLocation + "] (" + instructionCode + arguments + ") :" + instructionName;

        instruction.setText(completeInstructionInfo);
        listModel.add(0, completeInstructionInfo);
    }

    private int getArgumentCount(int instr) {
        final OpCode opCode = OpCode.from(instr);
        final AddressingMode addressingMode = opCode.getAddressingMode();
        return addressingMode.getInstructionBytes() - 1;
    }

    public static void main(String[] args){
        new DebuggerWindow();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 500);
    }
}
