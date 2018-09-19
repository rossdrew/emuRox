package com.rox.emu.processor.mos6502.util

import com.rox.emu.env.RoxByte
import com.rox.emu.processor.mos6502.op.OpCode
import spock.lang.Specification
import spock.lang.Unroll

class ProgramSpec extends Specification {
    def testCreation() {
        given:
        Program program

        when:
        program = new Program()

        then:
        program != null
    }

    @Unroll("Test comparing equal #description program")
    def testValidComparisons() {
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

    def testIdenticalProgramCompare() {
        given:
        Program program = new Program().with([OpCode.LDA_I, 0x01] as Object[])

        when:
        boolean equal = program.equals(program)

        then:
        equal
        program.hashCode() == (new Program().with([OpCode.LDA_I, 0x01] as Object[])).hashCode()
    }

    def testUnequalObjects() {
        given:
        Program programA = new Program().with([OpCode.LDA_I, 0x01] as Object[])
        Program programB = new Program().with([OpCode.LDA_I, 0x02] as Object[])

        expect:
        !programA.equals(programB)
        programA.hashCode() != programB.hashCode()
    }

    @Unroll("Test comparing #description")
    def testInvalidComparisons() {
        given:
        Program program

        when:
        program = new Program().with([OpCode.LDA_I, 0x01] as Object[])

        then:
        !program.equals(programInputBytes)

        where:
        programInputBytes                    | description
        null                                 | "empty program"
        [0xA9, 0x02]                         | "different program"
        [OpCode.SEC]                         | "smaller program"
        [OpCode.SEC, OpCode.SED, OpCode.CLC] | "larger program"
        OpCode.SED                           | "single opcode"
        "Just a string"                      | "unsupported class"
    }

    def "Create program with invalid program data"() {
        given:
        final Class[] invalidProgramData = [Program.class] as Class[]

        when:
        new Program().with(invalidProgramData)

        then:
        thrown RuntimeException
    }

    @Unroll("Valid labels: #expected")
    testValidLabel() {
        given:
        final Program program = new Program().with(programInputBytes as Object[])

        when:
        RoxByte[] programBytes = program.getProgramAsByteArray()

        then:
        programBytes.length == programSize
        program.getLabels().size() == labelCount
        program.getLocationOf('A:') == labelLoc

        where:
        programInputBytes                              || programSize | labelCount | labelLoc | expected
        ['A:', OpCode.ADC_ABS, 0x10, 0x02, OpCode.CLC] || 4           | 1          | 0        | "Label at the start"
        [OpCode.ADC_ABS, 0x10, 0x02, 'A:', OpCode.CLC] || 4           | 1          | 3        | "Label in the middle"
        [OpCode.ADC_ABS, 0x10, 0x02, OpCode.CLC, 'A:'] || 4           | 1          | 4        | "Label at the end"
        ["A:", OpCode.ADC_ABS, 0x10, 0x02,
         "B:", OpCode.CLC,
         'C:']                                         || 4           | 3          | 0        | "Multiple labels"
    }

    def testLabelReplacement() {
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
        for (int i = 0; i < compiledProgram.length; i++) {
            compiledProgram[i].getRawValue() == expectedProgramCode[i]
        }
    }

    def testInvalidLabel() {
        given:
        final Program program = new Program()

        when:
        program.getLocationOf('A:')

        then:
        thrown NullPointerException
    }

    def testUseOfNonExistentLabel() {
        given:
        final Program program = new Program()
        program = program.with(OpCode.NOP, OpCode.BCC, program.referenceBuilder("ThisDoesntExist"))

        when:
        program.getProgramAsByteArray()

        then:
        thrown RuntimeException
    }

    @Unroll("#description program creation")
    def testProgramCreation() {
        given:
        Program program

        when:
        program = new Program().with(programInputBytes)

        then:
        program.getProgramAsByteArray() == RoxByte.fromIntArray(expectedBytes)

        where:
        programInputBytes                        | expectedBytes                                         | description
        [OpCode.SEC, OpCode.SED] as OpCode[]     | [OpCode.SEC.byteValue, OpCode.SED.byteValue] as int[] | "Only OpCode data"
        [OpCode.LDA_I.byteValue, 42] as Number[] | [OpCode.LDA_I.byteValue, 42] as int[]                 | "Only Number data"
        ["Test Label"] as String[]               | [] as int[]                                           | "Only String data"
    }

    @Unroll("Valid compilation: #expected")
    testValidPrograms() {
        given: 'A program'
        final Program program

        when: 'values are pushed into it'
        program = new Program().with(programInputBytes as Object[])

        then: 'The resulting byte stream is as expected'
        program.getProgramAsByteArray() == RoxByte.fromIntArray(expectedProgramBytes as int[])

        where:
        programInputBytes            || expectedProgramBytes                   | expected
        [0x2A]                       || [0x2A]                                 | "Byte value added to program"
        [OpCode.ADC_ABS]             || [OpCode.ADC_ABS.byteValue]             | "Op-code value added to program"
        [OpCode.ADC_ABS, 0x10, 0x02] || [OpCode.ADC_ABS.byteValue, 0x10, 0x02] | "Op-code and arguments added to program"
        ["START:"]                   || []                                     | "A program label doesn't change the output"
    }

    //TODO Test Program.equals() non-identical classtype, same class, no array
    //TODO Test Program.with() with a number an int
    //TODO Test Program.arrayMatches()
    //TODO Test Program.hashcode()
}
