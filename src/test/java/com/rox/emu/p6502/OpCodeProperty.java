package com.rox.emu.p6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.Memory;
import com.rox.emu.SimpleMemory;
import org.junit.Before;
import org.junit.runner.RunWith;

import static com.rox.emu.p6502.InstructionSet.OP_LDA_I;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnitQuickcheck.class)
public class OpCodeProperty {
    private Memory memory;
    private CPU processor;

    @Before
    public void setUp() {
        memory = new SimpleMemory(65534);
        memory.setByteAt(0x0, 0xFFFC);
        memory.setByteAt(0x0, 0xFFFD);

        processor = new CPU(memory);
        processor.reset();
    }

    @Property public void testValidImmediateADC(@InRange(min = "0", max = "255") int value){
        int[] program = {OP_LDA_I, value};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(value, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
    }

    @Property public void testInvalidImmediateADC(@When(satisfies = "#_ < 0 || #_ > 255") int value){
        int[] program = {OP_LDA_I, value};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertNotEquals(value, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
    }
}
