package com.rox.emu.processor.mos6502.util

import com.rox.emu.env.RoxByte
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

    @Unroll("Test comparing #description program")
    def testComparisons(){
        given:
        Program program

        when:
        program = new Program().with(programInputBytes as Object[])

        then:
        program.equals(programInputBytes)

        where:
        programInputBytes            | description
        []                           | "empty"
        [OpCode.LDA_I, 0x01]         | "OpCode constructed"
        [0xA9, 0x02]                 | "integer constructed"
        [0xA9 as byte, 0x03 as byte] | "byte constructed"
    }

    @Unroll("Valid labels: #expected")
    testValidLabel(){
        given:
        final Program program = new Program().with(programInputBytes as Object[])

        when:
        RoxByte[] programBytes = program.getProgramAsByteArray()

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

    def testLabelReplacement(){
        given: 'a program'
        Program myProgram = new Program()
        myProgram = myProgram.with("Start",
                                   OpCode.BEQ, myProgram.referenceBuilder("MyLabel"),
                                   OpCode.LDA_I, 0x1,
                                   "MyLabel",
                                   OpCode.LDA_I, 0x2,
                                   OpCode.BNE, myProgram.referenceBuilder("Start"),
                                   OpCode.LDA_I, 0x3)

        and: 'its expected compiled code'
        int[] expectedProgramCode = [OpCode.BEQ.byteValue, 0b00000010,
                                     OpCode.LDA_I.byteValue, 0x1,
                                     OpCode.LDA_I.byteValue, 0x2,
                                     OpCode.BNE.byteValue, 0b11111000,
                                     OpCode.LDA_I.byteValue, 0x3]

        when: 'we get the compiled code'
        RoxByte[] compiledProgram = myProgram.getProgramAsByteArray()

        then: 'it is exactly as expected'
        for (int i=0; i<compiledProgram.length; i++){
            compiledProgram[i].getRawValue() == expectedProgramCode[i]
        }
    }

    def testInvalidLabel(){
        given:
        final Program program = new Program()

        when:
        program.getLocationOf('A:')

        then:
        thrown NullPointerException
    }

    def testUseOfNonExistentLabel(){
        given:
        final Program program = new Program()
        program = program.with(OpCode.NOP, OpCode.BCC, program.referenceBuilder("ThisDoesntExist"))

        when:
        program.getProgramAsByteArray()

        then:
        thrown RuntimeException
    }

    @Unroll("Valid compilation: #expected")
    testValidPrograms(){
        given: 'A program'
        final Program program

        when: 'values are pushed into it'
        program = new Program().with(programInputBytes as Object[])

        then: 'The resulting byte stream is as expected'
        program.equals(expectedProgramBytes as Object[])

        where:
        programInputBytes               || expectedProgramBytes                           | expected
        [0x2A]                          || [0x2A]                                         | "Byte value added to program"
        [OpCode.ADC_ABS]                || [OpCode.ADC_ABS.byteValue]                     | "Op-code value added to program"
        [OpCode.ADC_ABS, 0x10, 0x02]    || [OpCode.ADC_ABS.byteValue, 0x10, 0x02]         | "Op-code and arguments added to program"
        ["START:"]                      || []                                             | "A program label doesn't change the output"
    }
}
