package com.rox.emu

import spock.lang.Specification
import spock.lang.Unroll

class OpCodeSpec extends Specification {

    @Unroll
    def "LDA (Load Accumulator) Test"() {
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
        //C == processor.statusFlags[P6502.STATUS_FLAG_CARRY]
        Z == processor.statusFlags[1]
        //I == processor.statusFlags[P6502.STATUS_FLAG_IRQ_DISABLE]
        //D == processor.statusFlags[P6502.STATUS_FLAG_DEC]
        //B == processor.statusFlags[P6502.STATUS_FLAG_BREAK]
        //U == processor.statusFlags[P6502.STATUS_FLAG_UNUSED]
        //O == processor.statusFlags[P6502.STATUS_FLAG_OVERFLOW]
        N == processor.statusFlags[7]

        where:
        loadValue || expectedAccumulator || PC || Z     || N
        0x0       || 0x0                 || 2  || true  || false
        0x1       || 0x1                 || 2  || false || false
        0x7F      || 0x7F                || 2  || false || false
        0x80      || 0x80                || 2  || false || true
        0x81      || 0x81                || 2  || false || true
        0xFF      || 0xFF                || 2  || false || true

        //loadValue || expectedAccumulator  || PC || C      || Z     || I     || D     || B     || U     || O     || N
    }

    @Unroll
    def "ADC (ADd with Carry) Test"(){
        when:
        int[] memory = new int[65534]
        int[] program = [P6502.OPCODE_LDA_I, firstValue, P6502.OPCODE_ADC_I, secondValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        P6502 processor = new P6502(memory)
        processor.reset()
        processor.step()
        processor.step()
        int[] registers = processor.getRegisters()

        then:
        registers[P6502.ACC_REG] == expectedAccumulator
        PC == processor.getPC()
        //C == processor.statusFlags[P6502.STATUS_FLAG_CARRY]
        Z == processor.statusFlags[1]
        //O == processor.statusFlags[P6502.STATUS_FLAG_OVERFLOW]
        N == processor.statusFlags[7]

        where:
        firstValue || secondValue || expectedAccumulator || PC  || Z     || N
        0x0        || 0x0         || 0x0                 || 4  || true   || false
        0x7F       || 0x1         || 0x80                || 4  || false  || true
    }
}
