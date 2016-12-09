package com.rox.emu

import spock.lang.Specification
import spock.lang.Unroll

class OpCodeSpec extends Specification {

    @Unroll("LDA Immediate #Expected: Load #loadValue")
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
        loadValue || expectedAccumulator || PC || Z     || N     || Expected
        0x0       || 0x0                 || 2  || true  || false || "With zero result"
        0x1       || 0x1                 || 2  || false || false || ""
        0x7F      || 0x7F                || 2  || false || false || ""
        0x80      || 0x80                || 2  || false || true  || "With negative result"
        0x81      || 0x81                || 2  || false || true  || "With negative result"
        0xFF      || 0xFF                || 2  || false || true  || "With negative result"

        //loadValue || expectedAccumulator  || PC || C      || Z     || I     || D     || B     || U     || O     || N
    }

    @Unroll("ADC Immediate #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def "ADC (ADd with Carry to Accumulator) Test"(){
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
        C == processor.statusFlags[0]
        Z == processor.statusFlags[1]
        O == processor.statusFlags[6]
        N == processor.statusFlags[7]

        where:
        firstValue || secondValue || expectedAccumulator || PC  || Z      || N     || C     || O     || Expected
        0x0        || 0x0         || 0x0                 || 4   || true   || false || false || false || "With zero result"
        0x80       || 0x1         || 0x81                || 4   || false  || true  || false || false || "With valid negative result"
        0xFF       || 0xFF        || 0xFE                || 4   || false  || true  || true  || false || "With negative, carried result"
        0x50       || 0xD0        || 0x20                || 4   || false  || false || true  || false || "With positive, carried result"
        0x50       || 0x50        || 0xA0                || 4   || false  || true  || false || true  || "With negative overflow"
    }
}
