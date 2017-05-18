package com.rox.emu.processor.mos6502.util

import com.rox.emu.processor.mos6502.op.OpCode
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Array

class ProgramSpec extends Specification {
    def testCreation(){
        given:
        Program program

        when:
        program = new Program()

        then:
        program != null
    }

    @Unroll("Test valid program: #expected")
    testValidPrograms(){
        given:
        Program program = new Program().with(programInputBytes as Object[])

        when:
        byte[] programBytes = program.getProgramAsByteArray()

        then:
        programBytes == expectedProgramBytes

        where:
        programInputBytes               | expectedProgramBytes                           | expected
        [0x2A]                          | [0x2A]                                         | "Byte value added to program"
        [OpCode.OP_ADC_ABS]             | [OpCode.OP_ADC_ABS.byteValue]                  | "Op-code value added to program"
        [OpCode.OP_ADC_ABS, 0x10, 0x02] | [OpCode.OP_ADC_ABS.byteValue, 0x10, 0x02]      | "Op-code and arguments added to program"
    }
}
