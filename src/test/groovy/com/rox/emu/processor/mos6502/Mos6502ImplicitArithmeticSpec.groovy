package com.rox.emu.processor.mos6502

import com.rox.emu.mem.Memory
import com.rox.emu.mem.SimpleMemory
import com.rox.emu.processor.mos6502.op.OpCode
import com.rox.emu.processor.mos6502.util.Program
import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.processor.mos6502.op.OpCode.*

class Mos6502ImplicitArithmeticSpec extends Specification {
    Memory memory
    Mos6502 processor
    Registers registers

    /**
     * Create a memory block of NOP so that we can increment through them
     * without any side effects for testing program flow.
     */
    private Memory createNOPMemory(Memory memory) {
        for (int i=0; i<0x10000; i++){
            memory.setByteAt(i, OpCode.NOP.byteValue)
        }
        return memory
    }

    private Program loadMemoryWithProgram(Object ... programElements){
        final Program program = new Program().with(programElements)
        memory.setBlock(0, program.getProgramAsByteArray())
        return program
    }

    def setup(){
        memory = new SimpleMemory()
        processor = new Mos6502(memory)
        processor.reset()
        registers = processor.getRegisters()
    }

    @Unroll("PC Flow: #description (#initialValue + #steps -> #expectedValue)")
    def testProgramCounterArithmetic(){
        given: 'A PC starting point'
        createNOPMemory(memory)
        registers.setPC(initialValue)

        when: 'we increment'
        processor.step(steps)

        then: 'we are were we are expected to be'
        registers.getPC() == expectedValue

        where:
        initialValue        | steps   || expectedValue | description
        0                   | 1       || 1             | "Simplest case"
        20                  | 10      || 30            | "Random case"
        0b11111111          | 1       || 0b100000000   | "Low byte overflow"
        0b1111111111111111  | 1       || 0             | "High byte overflow"
        0                   | 300     || 300           | "Zero to overflown low byte"
        0                   | 0x10000 || 0             | "Zero to overflown high byte"
    }

    @Unroll("Indexed Indirect: #description (#pLoc[#index] -> *(#pHi | #pLo) == #expectedAccumulator)")
    def testIndexedIndirectAddressingArithmetic() {
        given: 'a value in memory'
        memory.setByteAt(((pHi<<8)|pLo), value)

        and: 'a pointer in zero page, offset at an index pointing at that values memory location'
        memory.setBlock(pLoc + index, [pHi, pLo] as int[])

        and: 'a program that takes a value and loads it to the empty Accumulator in an indexed, indirect way'
        loadMemoryWithProgram(LDA_I, 0,
                              LDX_I, index,
                              LDA_IND_IX, pLoc)


        when: 'we run the program'
        processor.step(3)

        then: 'the correct value is in the accumulator'
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator

        where: 'Pointer location base (pLoc), the points (pHi|pLo) and the index are'
        pLoc | pHi  | pLo  | index | value || expectedAccumulator   | description
        0x30 | 0x00 | 0x50 | 0     | 12    || 12                    | "Zero index"
        0x40 | 0x00 | 0x60 | 1     | 9     || 9                     | "Simplest index"
        0xFE | 0x01 | 0x01 | 0     | 8     || 8                     | "Address with offset just inside zero page"

        0xFE | 0x02 | 0x40 | 1     | 7     || BRK.byteValue         | "Address with offset (FF:00 = 01:LDA_I/14) partially outwith zero page"
        //TODO 0xFE | 0x03 | 0x50 | 2     | 6     || BRK.byteValue         | "Address with offset (00:01 = LDA_I/14:0) just outwith zero page"
        //TODO 0xFF | 0x04 | 0x60 | 2     | 5     || BRK.byteValue         | "Address with offset (01:02 = 0:LDX_I/43) wholly outwith zero page"
    }

    //TODO Indirect indexed modes & overflows related to them
//    def testIndirectAddressingArithmetic() {
//        given: 'a value in memory'
//        and: 'a pointer in zero page'
//        and: 'a program that takes the value and loads it to the empty Accumulator in an indirect, indexed way'
//        when: 'we run the program'
//        then: 'the correct value is in the accumulator'
//        where:
//    }

    //TODO Branch to & overflows related to them
}
