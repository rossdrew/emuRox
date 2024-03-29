package com.rox.emu.processor.mos6502.dbg.ui.component;

import com.rox.emu.processor.mos6502.Registers;

import javax.swing.*;
import java.awt.*;

/**
 * A UI representation of the MOS 6502 registers<br/>
 *
 * <table>
 *     <tr>
 *         <th colspan="2" align="center">6502 Registers</th>
 *     </tr>
 *     <tr>
 *         <td></td>
 *         <td>Accumulator</td>
 *     </tr>
 *
 *     <tr>
 *         <td></td>
 *         <td>X Index</td>
 *     </tr>
 *
 *     <tr>
 *         <td></td>
 *         <td>Y Index</td>
 *     </tr>
 *
 *     <tr>
 *         <td>Stack Pointer (Hi)</td>
 *         <td>Stack Pointer (Lo)</td>
 *     </tr>
 *
 *     <tr>
 *         <td>Program Counter (Hi)</td>
 *         <td>Program Counter (Lo)</td>
 *     </tr>
 *
 *     <tr>
 *         <td></td>
 *         <td>Status Register {N,V,-,B,D,I,Z,C}</td>
 *     </tr>
 * </table>
 *
 * @author Ross Drew
 */
public class Registers6502 extends JPanel {
    private final Registers registers;

    private final ByteBox accumulator = new ByteBox("Accumulator", 0);

    private final ByteBox xIndex = new ByteBox("X Index", 0);
    private final ByteBox yIndex = new ByteBox("Y Index", 0);

    private final ByteBox stackPointerHi = new ByteBox("Stack Pointer (Hi)", 0);
    private final ByteBox stackPointerLo = new ByteBox("Stack Pointer (Lo)", 0);

    private final ByteBox programCounterHi = new ByteBox("Program Counter (Hi)", 0, new Color(9, 178, 0));
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

        accumulator.setValue(registers.getRegister(Registers.Register.ACCUMULATOR).getRawValue());
        xIndex.setValue(registers.getRegister(Registers.Register.X_INDEX).getRawValue());
        yIndex.setValue(registers.getRegister(Registers.Register.Y_INDEX).getRawValue());
        stackPointerHi.setValue(0x01);
        stackPointerLo.setValue(registers.getRegister(Registers.Register.STACK_POINTER_LOW).getRawValue());
        programCounterHi.setValue(registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI).getRawValue());
        programCounterLo.setValue(registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW).getRawValue());

        statusRegister.setValue(registers.getRegister(Registers.Register.STATUS_FLAGS).getRawValue());
    }
}
