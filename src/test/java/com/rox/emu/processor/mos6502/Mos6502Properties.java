package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.util.Program;
import org.junit.Before;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

import static com.rox.emu.processor.mos6502.op.OpCode.*;

@RunWith(JUnitQuickcheck.class)
public class Mos6502Properties {
    private Memory memory;
    private Mos6502 processor;

    @Before
    public void setUp() {
        memory = new SimpleMemory();
        memory.setByteAt(0x0, 0xFFFC);
        memory.setByteAt(0x0, 0xFFFD);

        processor = new Mos6502(memory);
        processor.reset();
    }

    @Property (trials = 100)
    public void testValidStartup(@InRange(min = "0", max = "255") int memHi,
                                 @InRange(min = "0", max = "255") int memLo) {
        memory = new SimpleMemory();
        memory.setByteAt(0xFFFC, memHi);
        memory.setByteAt(0xFFFD, memLo);

        processor = new Mos6502(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertEquals(memHi, registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));        // PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(memLo, registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));       // ...

        assertEquals(0x34, registers.getRegister(Registers.Register.STATUS_FLAGS));      //Status flags reset
        assertEquals(0xFF, registers.getRegister(Registers.Register.STACK_POINTER_HI));  //Stack Pointer at top of stack
        assertEquals(0, registers.getRegister(Registers.Register.ACCUMULATOR));          //All cleared
        assertEquals(0, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(0, registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Property (trials = 100)
    public void testValidImmediateADC(@InRange(min = "0", max = "255") int value){
        Program program = new Program().with(LDA_I, value);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(value, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
    }

    @Property (trials = 100)
    public void testInvalidImmediateADC(@When(satisfies = "#_ < 0 || #_ > 255") int value){
        Program program = new Program().with(LDA_I, value);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertNotEquals(value, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
    }
}
