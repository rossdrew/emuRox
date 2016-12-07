package com.rox.emu

import spock.lang.Specification
import spock.lang.Unroll

class OpCodeSpec extends Specification {

    @Unroll
    def "ADC Test"(){
        when:
        int[] memory = new int[65534]
        int[] program = [P6502.OPCODE_LDA_I, loadValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        P6502 processor = new P6502(memory)
        processor.reset()
        processor.step()
        int[] registers = processor.getRegisters()

        then:
        registers[P6502.ACC_REG] == expectedAccumulator
        PC == processor.getPC()
        //C == processor.statusFlags[P6502.STATUS_FLAGS_CARRY]
        Z == processor.statusFlags[P6502.STATUS_FLAGS_ZERO]
        //I == processor.statusFlags[P6502.STATUS_FLAGS_IRQ_DISABLE]
        //D == processor.statusFlags[P6502.STATUS_FLAGS_DEC]
        //B == processor.statusFlags[P6502.STATUS_FLAGS_BREAK]
        //U == processor.statusFlags[P6502.STATUS_FLAGS_UNUSED]
        //O == processor.statusFlags[P6502.STATUS_FLAGS_OVERFLOW]
        N == processor.statusFlags[P6502.STATUS_FLAGS_NEGATIVE]

        statusFlags == processor.statusFlags

        where:
        loadValue || expectedAccumulator  || PC || Z     || N
        0x0       || 0x0                  || 2  || true  || false
        0x1       || 0x1                  || 2  || false || false

        //loadValue || expectedAccumulator  || PC || C      || Z     || I     || D     || B     || U     || O     || N
        //0x0       || 0x0                  || 2  || false  || true  || false || false || false || false || false || false
        //0x02      || 0x02                 || 2  || false  || false || false || false || false || false || false || false
    }
}
