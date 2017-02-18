package com.rox.emu.p6502;

import com.rox.emu.SimpleMemory;
import org.junit.Test;

import static com.rox.emu.p6502.InstructionSet.OP_LDA_I;
import static org.quicktheories.quicktheories.QuickTheory.qt;
import static org.quicktheories.quicktheories.generators.SourceDSL.integers;

public class CPUPropertyTest {
    @Test
    public void testValidValuesForLDA_I() {
        qt()
            .forAll(integers().between(0, 0xFF))
            .check((value) -> (runProgram(new int[] {OP_LDA_I, value}, 1)).getRegister(Registers.REG_ACCUMULATOR) == value);
    }

    @Test
    public void testInvalidValuesForLDA_I() {
        qt()
            .forAll(integers().from(0x100).upTo(0xFFFF))
            .check((value) -> (runProgram(new int[] {OP_LDA_I, value}, 1)).getRegister(Registers.REG_ACCUMULATOR) != value);

        qt()
            .forAll(integers().from(-0xFF).upTo(0x0))
            .check((value) -> (runProgram(new int[] {OP_LDA_I, value}, 1)).getRegister(Registers.REG_ACCUMULATOR) != value);
    }

    private Registers runProgram(int[] program, int steps){
        SimpleMemory memory = new SimpleMemory(65534);
        memory.setMemory(0, program);
        CPU processor = new CPU(memory);
        processor.reset();

        processor.step(steps);

        return processor.getRegisters();
    }

}
