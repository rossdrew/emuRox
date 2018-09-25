package com.rox.emu.processor.mos6502;

import com.github.radm.theories.TheorySuite;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import org.junit.Before;
import org.junit.contrib.theories.*;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

@RunWith(TheorySuite.class)
public class Mos6502Theories {
    private Memory memory;
    private Mos6502 processor;

    @DataPoints("validBytes")
    public static final int[] VALID_BYTE_DATA = new int[] {0, 5, 127, 128, 200, 255};

    @Before
    public void setUp() {
        memory = new SimpleMemory();
        memory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(0xFFFC)); //XXX FFFC is not a byte, it's a word?!!
        memory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(0xFFFD));

        processor = new Mos6502(memory);
        processor.reset();
    }

    @Theory
    public void testValidStartup(@FromDataPoints("validBytes") int memHi,
                                 @FromDataPoints("validBytes") int memLo) {
        assumeThat(memHi, is(both(greaterThanOrEqualTo(0)).and(lessThanOrEqualTo(255))));

        memory = new SimpleMemory();
        memory.setByteAt(RoxWord.fromLiteral(0xFFFC), RoxByte.fromLiteral(memHi));
        memory.setByteAt(RoxWord.fromLiteral(0xFFFD), RoxByte.fromLiteral(memLo));

        processor = new Mos6502(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertThat(RoxByte.fromLiteral(memHi), equalTo(registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI)));               // PC Set to location pointed to by mem[FFFC:FFFD]
        assertThat(RoxByte.fromLiteral(memLo), equalTo(registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW)));              // ...

        assertThat(registers.getRegister(Registers.Register.STATUS_FLAGS), equalTo(RoxByte.fromLiteral(0x34)));              //Status flags reset

        assertThat(registers.getRegister(Registers.Register.STATUS_FLAGS), equalTo(RoxByte.fromLiteral(0x34)));              //Stack Pointer at top of stack
        assertThat(registers.getRegister(Registers.Register.STACK_POINTER_LOW), equalTo(RoxByte.fromLiteral(0xFF)));          //All cleared
        assertThat(registers.getRegister(Registers.Register.ACCUMULATOR), equalTo(RoxByte.ZERO));
        assertThat(registers.getRegister(Registers.Register.X_INDEX), equalTo(RoxByte.ZERO));
        assertThat(registers.getRegister(Registers.Register.Y_INDEX), equalTo(RoxByte.ZERO));
    }
}
