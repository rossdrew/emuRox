package com.rox.emu.p6502.dbg.ui.component;

import com.rox.emu.p6502.Registers;

import javax.swing.*;
import java.awt.*;

/**
 * A UI representation of the MOS 6502 registers
 */
public class Registers6502 extends JPanel {
    private final Registers registers;

    private final ByteBox accumulator = new ByteBox("Accumulator", 0);

    private final ByteBox xIndex = new ByteBox("X Index", 0);
    private final ByteBox yIndex = new ByteBox("Y Index", 0);

    private final ByteBox stackPointerHi = new ByteBox("Stack Pointer (Hi)", 0);
    private final ByteBox stackPointerLo = new ByteBox("Stack Pointer (Lo)", 0);

    private final ByteBox programCounterHi = new ByteBox("Program Counter (Hi)", 0);
    private final ByteBox programCounterLo = new ByteBox("Program Counter (Lo)", 0);

    private final FlagByteBox statusRegister = new FlagByteBox("Status Register", 0x0, "NV BDIZC".toCharArray());

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        refreshValues();
    }

    public Registers6502(Registers registers) {
        this.registers = registers;

        setLayout(new GridLayout(6,2));

        add(Box.createHorizontalGlue());
        add(accumulator);

        add(Box.createHorizontalGlue());
        add(xIndex);

        add(Box.createHorizontalGlue());
        add(yIndex);

        add(stackPointerHi);
        add(stackPointerLo);

        add(programCounterHi);
        add(programCounterLo);

        add(Box.createHorizontalGlue());
        add(statusRegister);

        refreshValues();
    }

    private void refreshValues() {
        if (registers == null)
            return;

        accumulator.setValue(registers.getRegister(Registers.REG_ACCUMULATOR));
        xIndex.setValue(registers.getRegister(Registers.REG_X_INDEX));
        yIndex.setValue(registers.getRegister(Registers.REG_Y_INDEX));
        stackPointerHi.setValue(0x01);
        stackPointerLo.setValue(registers.getRegister(Registers.REG_SP));
        programCounterHi.setValue(registers.getRegister(Registers.REG_PC_HIGH));
        programCounterLo.setValue(registers.getRegister(Registers.REG_PC_LOW));

        statusRegister.setValue(registers.getRegister(Registers.REG_STATUS));
    }
}
