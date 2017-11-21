package com.rox.emu.processor.mos6502

import com.rox.emu.mem.Memory
import com.rox.emu.mem.SimpleMemory
import com.rox.emu.processor.mos6502.op.OpCode
import spock.lang.Specification
import spock.lang.Unroll

class Mos6502ImplicitArithmeticSpec extends Specification {

    /**
     * Create a memory block of NOP so that we can increment through them
     * without any side effects for testing program flow.
     */
    private Memory createNOPMemory() {
        final Memory memory = new SimpleMemory()
        for (int i=0; i<0x10000; i++){
            memory.setByteAt(i, OpCode.NOP.byteValue)
        }
        return memory
    }

    @Unroll("PC Flow: #description (#initialValue + #steps -> #expectedValue)")
    def testProgramCounterProgression(){
        given: 'A started 6502 processor'
        final Memory memory = createNOPMemory()
        final Mos6502 processor = new Mos6502(memory)
        processor.reset()
        final Registers registers = processor.getRegisters()

        when: 'we start at...'
        registers.setPC(initialValue)

        and: 'increment by...'
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

    //TODO Indexed addressing modes & overflows related to them
    //TODO Branch to & overflows related to them
}
