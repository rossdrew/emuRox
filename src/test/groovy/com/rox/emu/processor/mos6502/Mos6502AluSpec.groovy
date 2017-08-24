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
        result.asInt == expectedValue

        where:
        operandA   | operandB || expectedResult | expectedValue | description
        0          | 0        || 0              | 0             | "No change"
        1          | 1        || 2              | 2             | "Simple addition"
        0          | 1        || 1              | 1             | "Left hand zero addition"
        1          | 0        || 1              | 1             | "Right hand zero addition"
        127        | 1        || 128            | -128          | "Signed Overflow"
        0b11111111 | 1        || 0              | 0             | "Signed negative to zero"
        0b11111111 | 10       || 9              | 9             | "Signed negative to positive"
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
        result.asInt == expectedValue

        where:
        operandA   | operandB   || expectedResult | expectedValue | description
        0          | 0          || 0              | 0             | "No change"
        1          | 1          || 0              | 0             | "Number minus itself"
        10         | 9          || 1              | 1             | "Positive result"
        0          | 1          || 0b11111111     | -1            | "Negative result"
        0b11111111 | 1          || 0b11111110     | -2            | "Positive subtraction from a negative"
        0b11111111 | 0b11111111 || 0              | 0             | "Negative subtraction from a negative"
        0b10000000 | 1          || 0b01111111     | 127           | "Signed underflow"
    }
}
