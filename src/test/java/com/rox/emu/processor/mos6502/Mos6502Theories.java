package com.rox.emu.processor.mos6502;

import com.github.radm.theories.TheorySuite;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import org.junit.Before;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(TheorySuite.class)
public class Mos6502Theories {
    private Memory memory;
    private Mos6502 processor;

    @DataPoint
    public static final int LOWEST_VALID_BYTE = 0;
    @DataPoint
    public static final int HIGHEST_VALID_BYTE = 255;
    @DataPoint
    public static final int MIDRANGE_VALID_BYTE = 127;

    @Before
    public void setUp() {
        memory = new SimpleMemory();
        memory.setByteAt(0x0, 0xFFFC);
        memory.setByteAt(0x0, 0xFFFD);

        processor = new Mos6502(memory);
        processor.reset();
    }

    @Theory
    public void testValidStartup(int memHi, int memLo) {
        memory = new SimpleMemory();
        memory.setByteAt(0xFFFC, memHi);
        memory.setByteAt(0xFFFD, memLo);

        processor = new Mos6502(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertThat(memHi, equalTo(registers.getRegister(Registers.REG_PC_HIGH)));           // PC Set to location pointed to by mem[FFFC:FFFD]
        assertThat(memLo, equalTo(registers.getRegister(Registers.REG_PC_LOW)));            // ...

        assertThat(registers.getRegister(Registers.REG_STATUS), equalTo(0x34));     //Status flags reset

        assertThat(registers.getRegister(Registers.REG_STATUS), equalTo(0x34));     //Stack Pointer at top of stack
        assertThat(registers.getRegister(Registers.REG_SP), equalTo(0xFF));         //All cleared
        assertThat(registers.getRegister(Registers.REG_ACCUMULATOR), equalTo(0));
        assertThat(registers.getRegister(Registers.REG_X_INDEX), equalTo(0));
        assertThat(registers.getRegister(Registers.REG_Y_INDEX), equalTo(0));
    }
}
