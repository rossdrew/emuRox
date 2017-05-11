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

import static com.rox.emu.processor.mos6502.op.util.OpCode.*;

@RunWith(JUnitQuickcheck.class)
public class CPUProperty {
    private Memory memory;
    private CPU processor;

    @Before
    public void setUp() {
        memory = new SimpleMemory();
        memory.setByteAt(0x0, 0xFFFC);
        memory.setByteAt(0x0, 0xFFFD);

        processor = new CPU(memory);
        processor.reset();
    }

    @Property (trials = 100)
    public void testValidStartup(@InRange(min = "0", max = "255") int memHi,
                                 @InRange(min = "0", max = "255") int memLo) {
        memory = new SimpleMemory();
        memory.setByteAt(0xFFFC, memHi);
        memory.setByteAt(0xFFFD, memLo);

        processor = new CPU(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertEquals(memHi, registers.getRegister(Registers.REG_PC_HIGH));        // PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(memLo, registers.getRegister(Registers.REG_PC_LOW));         // ...

        assertEquals(0x34, registers.getRegister(Registers.REG_STATUS));   //Status flags reset
        assertEquals(0xFF, registers.getRegister(Registers.REG_SP));       //Stack Pointer at top of stack
        assertEquals(0, registers.getRegister(Registers.REG_ACCUMULATOR)); //All cleared
        assertEquals(0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Property (trials = 100)
    public void testValidImmediateADC(@InRange(min = "0", max = "255") int value){
        Program program = new Program().with(OP_LDA_I, value);
        memory.setMemory(0, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(value, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
    }

    @Property (trials = 100)
    public void testInvalidImmediateADC(@When(satisfies = "#_ < 0 || #_ > 255") int value){
        Program program = new Program().with(OP_LDA_I, value);
        memory.setMemory(0, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertNotEquals(value, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
    }
}
