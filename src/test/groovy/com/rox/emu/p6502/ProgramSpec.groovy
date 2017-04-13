package com.rox.emu.p6502

import com.rox.emu.p6502.op.OpCode
import spock.lang.Specification

class ProgramSpec extends Specification {
    def testCreation(){
        given:
        Program program

        when:
        program = new Program()

        then:
        program != null
    }

    def testAddingByteValueToProgram(){
        given:
        Program program = new Program()

        when:
        program = program.with(0x2A)

        then:
        program.getProgramAsByteArray() == [0x2A]
    }

    def testAddingOpCodeToProgram(){
        given:
        Program program = new Program()

        when:
        program = program.with(OpCode.OP_ADC_ABS)

        then:
        program.getProgramAsByteArray() == [OpCode.OP_ADC_ABS.byteValue]
    }

    def testAddingWholeCommand(){
        given:
        Program program = new Program()

        when:
        program = program.with(OpCode.OP_ADC_ABS, 0x10, 0x02)

        then:
        program.getProgramAsByteArray() == [OpCode.OP_ADC_ABS.byteValue, 0x10, 0x02]
    }
}
