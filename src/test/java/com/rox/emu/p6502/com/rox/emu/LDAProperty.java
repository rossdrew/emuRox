package com.rox.emu.p6502.com.rox.emu;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.Memory;
import com.rox.emu.SimpleMemory;
import com.rox.emu.p6502.CPU;
import com.rox.emu.p6502.Registers;
import org.junit.Before;
import org.junit.runner.RunWith;

import static com.rox.emu.p6502.InstructionSet.OP_LDA_I;
import static junit.framework.TestCase.assertEquals;

@RunWith(JUnitQuickcheck.class)
public class LDAProperty {
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

    //No Generators are being loaded for int in the GeneratorRepository
    @Property
    public void loadAccumulator(int byteValue){
        int[] program = {OP_LDA_I, byteValue};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(byteValue, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
    }
}
