package com.rox.emu.p6502.dbg.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * A UI representation of the MOS 6502 registers
 */
public class Registers6502 extends JPanel {
    final ByteBox accumulator = new ByteBox("Accumulator", 0);

    final ByteBox xIndex = new ByteBox("X Index", 0);
    final ByteBox yIndex = new ByteBox("Y Index", 0);

    final ByteBox stackPointerHi = new ByteBox("Stack Pointer (Hi)", 0);
    final ByteBox stackPointerLo = new ByteBox("Stack Pointer (Lo)", 0);

    final ByteBox programCounterHi = new ByteBox("Program Counter (Hi)", 0);
    final ByteBox programCounterLo = new ByteBox("Program Counter (Lo)", 0);

    public Registers6502() {
        setLayout(new GridLayout(6,2));

        add(new JLabel(""));
        add(accumulator);

        add(new JLabel(""));
        add(xIndex);

        add(new JLabel(""));
        add(yIndex);

        add(stackPointerHi);
        add(stackPointerLo);

        add(programCounterHi);
        add(programCounterLo);

        add(new JLabel(""));
        add(new JLabel("FLAGS"));
    }

    public void setAccumulator(int accumulatorValue){
        accumulator.setValue(accumulatorValue);
    }

    public void setXIndex(int xIndexValue){
        xIndex.setValue(xIndexValue);
    }

    public void setYIndex(int yIndexValue){
        yIndex.setValue(yIndexValue);
    }

    public void setStackPointer(int hi, int lo){
        stackPointerHi.setValue(hi);
        stackPointerLo.setValue(lo);
    }

    public void setProgramCounter(int hi, int lo){
        programCounterHi.setValue(hi);
        programCounterLo.setValue(lo);
    }
}
