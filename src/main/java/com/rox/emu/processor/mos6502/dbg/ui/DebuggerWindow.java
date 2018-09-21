package com.rox.emu.processor.mos6502.dbg.ui;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.Registers;
import com.rox.emu.processor.mos6502.dbg.ui.component.MemoryPanel;
import com.rox.emu.processor.mos6502.dbg.ui.component.Registers6502;
import com.rox.emu.processor.mos6502.op.Mos6502AddressingMode;
import com.rox.emu.processor.mos6502.op.Mos6502OpCode;
import com.rox.emu.processor.mos6502.util.Mos6502Compiler;
import com.rox.emu.processor.mos6502.util.Program;
import com.rox.emu.rom.InesRom;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A DebuggerWindow for debugging 6502 CPU code
 *
 * @author Ross Drew
 */
final class DebuggerWindow extends JFrame {
    private Mos6502 processor;
    private Memory memory;

    private Registers6502 newRegisterPanel;

    private String instructionName = "...";
    private final JLabel instruction = new JLabel(instructionName);

    private final DefaultListModel<String> listModel;

    private DebuggerWindow() {
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
        add(getCenterPanel(), BorderLayout.CENTER);

        loadProgram(getProgram());
        setVisible(true);
        pack();
    }

    private JComponent getCenterPanel(){
        JTabbedPane centerPane = new JTabbedPane();

        centerPane.add("Registers", newRegisterPanel);
        centerPane.add("Code", getCodeInput());

        return centerPane;
    }

    private JComponent getCodeInput(){
        final JTextPane codeArea = getCodeArea();
        final JScrollPane codeScroller = new JScrollPane(codeArea);
        final JButton compilerButton = new JButton("Compile");
        compilerButton.addActionListener(e -> compile(codeArea.getText()));

        final JPanel codePanel = new JPanel();
        codePanel.setLayout(new BorderLayout());
        codePanel.add(codeScroller, BorderLayout.CENTER);
        codePanel.add(compilerButton, BorderLayout.SOUTH);

        return codePanel;
    }

    private JTextPane getCodeArea(){
        final StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

        final JTextPane codeArea = new JTextPane(doc);
        codeArea.setBackground(new Color(0x25401C));
        codeArea.setCaretColor(new Color(0xD1E8CE));

        final Style bodyStyle = sc.addStyle("body", null);
        bodyStyle.addAttribute(StyleConstants.Foreground, new Color(0x789C6C));
        bodyStyle.addAttribute(StyleConstants.FontSize, 13);
        bodyStyle.addAttribute(StyleConstants.FontFamily, "monospaced");
        bodyStyle.addAttribute(StyleConstants.Bold, true);

        doc.setLogicalStyle(0, bodyStyle);

        return codeArea;
    }

    private JComponent getMemoryPanel(){
//        JPanel memoryPanel = new JPanel();
//        memoryPanel.setLayout(new BorderLayout());
//
//        final Map<String, Component> memoryComponents = getMemoryComponents();
//
//        final Object[] objects = memoryComponents.keySet().toArray();
//        final String[] memoryStrings = new String[objects.length];
//        for (int i=0; i<objects.length; i++){
//            memoryStrings[i] = (String)objects[i];
//        }
//        JComboBox petList = new JComboBox(memoryStrings);
//
//        memoryPanel.add(petList, BorderLayout.NORTH);
//        memoryPanel.add(memoryComponents.get("1"));
//
//        return memoryPanel;

        JTabbedPane memoryTabs = new JTabbedPane();

        final Map<String, Component> memoryComponentBlocks = getMemoryComponents();
        for (String memoryKey : memoryComponentBlocks.keySet()) {
            memoryTabs.addTab(memoryKey, memoryComponentBlocks.get(memoryKey));
        }

        return memoryTabs;
    }

    private Map<String, Component> getMemoryComponents(){
        final Map<String, Component> memoryBlocks = new LinkedHashMap<>();

        final String[] blocks2 = new String[260];
        for (int i=0; i<260; i++){
            int start = i * 256;
            blocks2[i] = ""+ i;//(i + " (" + start + "-" + (start + 255) + ")");
        }

        for (int i=0; i<7; i++)
//        for (int i=0; i<blocks2.length; i++)
            memoryBlocks.put(blocks2[i], getMemoryComponent(i * 256));

        return memoryBlocks;
    }

    private Component getMemoryComponent(int fromMemoryAddress){
        final MemoryPanel memoryPanel = new MemoryPanel();
        final JScrollPane scrollPane = new JScrollPane();

        memoryPanel.setMemory(memory, fromMemoryAddress);
        memoryPanel.linkTo(processor.getRegisters());

        scrollPane.setViewportView(memoryPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
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
        resetButton.addActionListener(e -> loadProgram(getProgramFromFile()));

        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout());
        controls.add(resetButton);
        controls.add(stepButton);

        return controls;
    }

    private RoxByte[] getProgramFromFile() {
        final File file = new File( "src" +  File.separator + "main" +  File.separator + "resources" + File.separator + "rom" + File.separator + "SMB1.NES");

        System.out.println("Loading '" + file.getAbsolutePath() + "'...");

        final FileInputStream fis;
        byte fileContent[] = {};
        try {
            fis = new FileInputStream(file);
            fileContent= new byte[(int)file.length()];
            fis.read(fileContent);
        } catch (IOException e ) {
            e.printStackTrace();
        }

        final InesRom rom = InesRom.from(fileContent);

        Memory prgRom = rom.getProgramRom();
        return prgRom.getBlock(RoxWord.ZERO, RoxWord.fromLiteral(prgRom.getSize()-1));
    }

    private RoxByte[] getProgram(){
        int data_offset = 0x32;
        int MPD = data_offset + 0x10;
        int MPR = data_offset + 0x11;
        int TMP = data_offset + 0x20;
        int RESAD_0 = data_offset + 0x30;
        int RESAD_1 = data_offset + 0x31;

        int valMPD = 7;
        int valMPR = 4;

//        Program countToTenProgram = new Program().with( OpCode.LDX_I, 10,
//                                                        OpCode.LDA_I, 0,
//                                                        OpCode.CLC,
//                                                        OpCode.ADC_I, 0x01,
//                                                        OpCode.DEX,
//                                                        OpCode.CPX_I, 0,
//                                                        OpCode.BNE, 0b11110111);

        Program multiplicationProgram = new Program().with( Mos6502OpCode.LDA_I, valMPD,
                                                            Mos6502OpCode.STA_Z, MPD,
                                                            Mos6502OpCode.LDA_I, valMPR,
                                                            Mos6502OpCode.STA_Z, MPR,
                                                            Mos6502OpCode.LDA_I, 0,         //<---- start
                                                            Mos6502OpCode.STA_Z, TMP,       //Clear
                                                            Mos6502OpCode.STA_Z, RESAD_0,   //...
                                                            Mos6502OpCode.STA_Z, RESAD_1,   //...
                                                            Mos6502OpCode.LDX_I, 8,         //X counts each bit
                                                //:MULT(18)
                                                            Mos6502OpCode.LSR_Z, MPR,       //LSR(MPR)
                                                            Mos6502OpCode.BCC, 13,          //Test carry and jump (forward 13) to NOADD

                                                            Mos6502OpCode.LDA_Z, RESAD_0,   //RESAD -> A
                                                            Mos6502OpCode.CLC,              //Prepare to add
                                                            Mos6502OpCode.ADC_Z, MPD,       //+MPD
                                                            Mos6502OpCode.STA_Z, RESAD_0,   //Save result
                                                            Mos6502OpCode.LDA_Z, RESAD_1,   //RESAD+1 -> A
                                                            Mos6502OpCode.ADC_Z, TMP,       //+TMP
                                                            Mos6502OpCode.STA_Z, RESAD_1,   //RESAD+1 <- A
                                                //:NOADD(35)
                                                            Mos6502OpCode.ASL_Z, MPD,       //ASL(MPD)
                                                            Mos6502OpCode.ROL_Z, TMP,       //Save bit from MPD
                                                            Mos6502OpCode.DEX,              //--X
                                                            Mos6502OpCode.BNE, 0b11100111   //Test equal and jump (back 24) to MULT
        );

        return multiplicationProgram.getProgramAsByteArray();
    }

    private void init(){
        memory = new SimpleMemory();
        processor = new Mos6502(memory);

        newRegisterPanel = new Registers6502(processor.getRegisters());
    }

    public void loadProgram(RoxByte[] program){
        reset();
        memory.setBlock(RoxWord.ZERO, program);
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

    public void compile(String programText){
        final Mos6502Compiler compiler = new Mos6502Compiler(programText);
        try {
            final Program program = compiler.compileProgram();
            final RoxByte[] programAsByteArray = program.getProgramAsByteArray();
            loadProgram(programAsByteArray);
        }catch (UnknownOpCodeException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void upDateWithNextInstruction() {
        final Registers registers = processor.getRegisters();
        final RoxWord pointer = registers.getPC();
        final RoxByte instr = memory.getByte(pointer);

        StringBuilder arguments = new StringBuilder();
        for (int i=0; i<getArgumentCount(instr.getRawValue()); i++ ){
            RoxWord n = RoxWord.fromLiteral(pointer.getRawValue() + (i+1));
            arguments.append(" " + MemoryPanel.asHex(memory.getByte(n).getRawValue()));
        }

        instructionName = Mos6502OpCode.from(instr.getRawValue()).toString();
        final String instructionLocation = MemoryPanel.asHex(pointer.getRawValue());
        final String instructionCode = MemoryPanel.asHex(instr.getRawValue());
        final String completeInstructionInfo = "[" + instructionLocation + "] (" + instructionCode + arguments.toString() + ") :" + instructionName;

        instruction.setText(completeInstructionInfo);
        listModel.add(0, completeInstructionInfo);
    }

    private int getArgumentCount(int instr) {
        final Mos6502OpCode opCode = Mos6502OpCode.from(instr);
        final Mos6502AddressingMode addressingMode = opCode.getAddressingMode();
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
