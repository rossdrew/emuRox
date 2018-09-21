package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.util.Program;
import org.junit.Before;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

import static com.rox.emu.processor.mos6502.op.Mos6502OpCode.*;

@RunWith(JUnitQuickcheck.class)
public class Mos6502Properties {
    private Memory memory;
    private Mos6502 processor;

    @Before
    public void setUp() {
        memory = new SimpleMemory();
        memory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(0xFFFC));
        memory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(0xFFFD));

        processor = new Mos6502(memory);
        processor.reset();
    }

    @Property (trials = 100)
    public void testValidStartup(@InRange(min = "0", max = "255") int memHi,
                                 @InRange(min = "0", max = "255") int memLo) {
        memory = new SimpleMemory();
        memory.setByteAt(RoxWord.fromLiteral(0xFFFC), RoxByte.fromLiteral(memHi));
        memory.setByteAt(RoxWord.fromLiteral(0xFFFD), RoxByte.fromLiteral(memLo));

        processor = new Mos6502(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertEquals(RoxByte.fromLiteral(memHi), registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));        // PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(RoxByte.fromLiteral(memLo), registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));       // ...

        assertEquals(RoxByte.fromLiteral(0x34), registers.getRegister(Registers.Register.STATUS_FLAGS));      //Status flags reset
        assertEquals(RoxByte.fromLiteral(0xFF), registers.getRegister(Registers.Register.STACK_POINTER_HI));  //Stack Pointer at top of stack
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR));          //All cleared
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Property (trials = 100)
    public void testValidImmediateADC(@InRange(min = "0", max = "255") int value){
        Program program = new Program().with(LDA_I, value);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(value), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Property (trials = 100)
    public void testInvalidImmediateADC(@When(satisfies = "#_ < 0 || #_ > 255") int value){
        Program program = new Program().with(LDA_I, value);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertNotEquals(value, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }
}
