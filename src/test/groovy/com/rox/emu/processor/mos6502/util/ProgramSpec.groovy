package com.rox.emu.processor.mos6502.util

import com.rox.emu.processor.mos6502.op.OpCode
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class ProgramSpec extends Specification {
    def testCreation(){
        given:
        Program program

        when:
        program = new Program()

        then:
        program != null
    }

    @Unroll("Valid labels: #expected")
    testValidLabel(){
        given:
        final Program program = new Program().with(programInputBytes as Object[])

        when:
        byte[] programBytes = program.getProgramAsByteArray()

        then:
        programBytes.length == programSize
        program.getLabels().size() == labelCount
        program.getLocationOf('A:') == labelLoc

        where:
        programInputBytes                                    || programSize | labelCount | labelLoc | expected
        ['A:', OpCode.ADC_ABS, 0x10, 0x02, OpCode.CLC]       || 4           | 1          | 0        | "Label at the start"
        [OpCode.ADC_ABS, 0x10, 0x02, 'A:', OpCode.CLC]       || 4           | 1          | 3        | "Label in the middle"
        [OpCode.ADC_ABS, 0x10, 0x02, OpCode.CLC, 'A:']       || 4           | 1          | 4        | "Label at the end"
        ["A:", OpCode.ADC_ABS, 0x10, 0x02,
         "B:", OpCode.CLC,
         'C:']                                               || 4           | 3          | 0        | "Multiple labels"
    }

    @Ignore("Failing test for label replacement")
    testLabelReplacement(){
        given:
        final Program labelProgram = new Program().with("Start",
                                                         OpCode.BEQ, "MyLabel",
                                                         OpCode.LDA_I, 0x1,
                                                         "MyLabel",
                                                         OpCode.LDA_I, 0x2,
                                                         OpCode.BNE, "Start",
                                                         OpCode.LDA_I, 0x3)

        when:
        byte[] compiledProgram = labelProgram.getProgramAsByteArray()

        then:
        compiledProgram == [OpCode.BEQ.byteValue, 0b00000011,
                            OpCode.LDA_I.byteValue, 0x1,
                            OpCode.LDA_I.byteValue, 0x2,
                            OpCode.BNE.byteValue, 0x0,
                            OpCode.LDA_I.byteValue, 0x3] as byte[]

        /*
        E:  240, 3,  169, 1,  169, 2,   208, 0, 169, 3
        A: -16, -87, 1,  -87, 2,  -48, -87,  3

         */
    }

    def testInvalidLabel(){
        given:
        final Program program = new Program()

        when:
        program.getLocationOf('A:')

        then:
        thrown NullPointerException
    }

    @Unroll("Valid compilation: #expected")
    testValidPrograms(){
        given: 'A program with values pushed to it'
        final Program program = new Program().with(programInputBytes as Object[])

        when: 'It is converted into a byte stream'
        final byte[] programBytes = program.getProgramAsByteArray()

        then: 'The resulting byte stream is as expected'
        programBytes == expectedProgramBytes as byte[]

        where:
        programInputBytes               || expectedProgramBytes                           | expected
        [0x2A]                          || [0x2A]                                         | "Byte value added to program"
        [OpCode.ADC_ABS]                || [OpCode.ADC_ABS.byteValue]                     | "Op-code value added to program"
        [OpCode.ADC_ABS, 0x10, 0x02]    || [OpCode.ADC_ABS.byteValue, 0x10, 0x02]         | "Op-code and arguments added to program"
        ["START:"]                      || []                                             | "A program label doesn't change the output"
    }
}
