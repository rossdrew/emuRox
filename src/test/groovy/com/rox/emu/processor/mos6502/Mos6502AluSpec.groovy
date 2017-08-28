package com.rox.emu.processor.mos6502

import com.rox.emu.env.RoxByte
import spock.lang.Specification
import spock.lang.Unroll

class Mos6502AluSpec extends Specification {
    @Unroll
    def "ADD (#description): #operandA + #operandB = #expectedValue"(){
        given:
        Mos6502Alu alu = new Mos6502Alu()

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        when:
        final RoxByte result = alu.add(a,b)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        where:
        operandA   | operandB   || expectedResult | expectedValue | description
        0          | 0          || 0              | 0             | "No change"
        1          | 1          || 2              | 2             | "Simple addition"
        0          | 1          || 1              | 1             | "Left hand zero addition"
        1          | 0          || 1              | 1             | "Right hand zero addition"
        127        | 1          || 128            | -128          | "Signed Overflow"
        0b11111111 | 1          || 0              | 0             | "Signed negative to zero"
        0b11111111 | 10         || 9              | 9             | "Signed negative to positive"
        0b10000000 | 1          || 0b10000001     | -127          | "Positive addition to negative"
        1          | 0b11111111 || 0              | 0             | "Negative addition to positive"
    }

    @Unroll
    def "SBC (#description): #operandA - #operandB = #expectedValue"(){
        given:
        Mos6502Alu alu = new Mos6502Alu()

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        when:
        final RoxByte result = alu.sbc(a,b)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        where:
        operandA   | operandB   || expectedResult | expectedValue | description
        0          | 0          || 0              | 0             | "No change"
        1          | 1          || 0              | 0             | "Number minus itself"
        10         | 9          || 1              | 1             | "Positive result"
        0          | 1          || 0b11111111     | -1            | "Negative result"
        0b11111111 | 1          || 0b11111110     | -2            | "Positive subtraction from a negative"
        0b11111111 | 0b11111111 || 0              | 0             | "Negative subtraction from a negative"
        0b10000000 | 1          || 0b01111111     | 127           | "Signed underflow "
    }

    @Unroll
    def "OR (#description): #operandA | #operandB = #expectedValue"(){
        given:
        Mos6502Alu alu = new Mos6502Alu()

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        when:
        final RoxByte result = alu.or(a,b)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        where:
        operandA   | operandB   || expectedResult | expectedValue | description
        0          | 0          || 0              | 0             | "No change"
        1          | 0          || 1              | 1             | "Basic change"
        0b10101010 | 0b01010101 || 0b11111111     | -1            | "Full bit merge"
        0b11110000 | 0b00111100 || 0b11111100     | -4            | "Overlapping bit merge"
    }

    @Unroll
    def "AND (#description): #operandA & #operandB = #expectedValue"(){
        given:
        Mos6502Alu alu = new Mos6502Alu()

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        when:
        final RoxByte result = alu.and(a,b)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        where:
        operandA   | operandB   || expectedResult | expectedValue | description
        0          | 0          || 0              | 0             | "No change"
        0          | 1          || 0              | 0             | "Bit only in parameter A, so no change"
        1          | 0          || 0              | 0             | "Bit only in parameter B, so no change"
        1          | 1          || 1              | 1             | "Bit set in both parameters survives"
        0b11110000 | 0b11110000 || 0b11110000     | -16           | "Multiple matching bits"
        0b10101010 | 0b11110000 || 0b10100000     | -96           | "Matched and unmatched bits"
        0b11111111 | 0b00000001 || 1              | 1             | "Single matched bit"

    }
}
