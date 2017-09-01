package com.rox.emu.processor.mos6502

import com.rox.emu.env.RoxByte
import spock.lang.Specification
import spock.lang.Unroll

class Mos6502AluSpec extends Specification {
    @Unroll
    def "ADD (#description): #operandA + #operandB = #expectedValue"(){
        given:
        final Registers registers = new Registers()
        final Mos6502Alu alu = new Mos6502Alu(registers)

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        and: 'The carry status is setup beforehand'
        registers.setFlagTo(Registers.C, carryIn)

        when: 'The numbers are added'
        final RoxByte result = alu.add(a,b)

        then: 'The values and flags are as expected'
        expectedResult == result.rawValue
        expectedValue == result.asInt
        registers.getFlag(Registers.C) == carryOut

        where:
        operandA   | operandB   | carryIn || expectedResult | expectedValue | carryOut | description
        0          | 0          | false   || 0              | 0             | false    | "No change"
        1          | 1          | false   || 2              | 2             | false    | "Simple addition"
        0          | 1          | false   || 1              | 1             | false    | "Left hand zero addition"
        1          | 0          | false   || 1              | 1             | false    | "Right hand zero addition"
        127        | 1          | false   || 128            | -128          | false    | "Signed Overflow"
        0b11111111 | 1          | false   || 0              | 0             | true     | "Signed negative to zero"
        0b11111111 | 10         | false   || 9              | 9             | true     | "Signed negative to positive"
        0b10000000 | 1          | false   || 0b10000001     | -127          | false    | "Positive addition to negative"
        1          | 0b11111111 | false   || 0              | 0             | true     | "Negative addition to positive"
        0          | 0          | true    || 1              | 1             | false    | "Carry in"
        0x50       | 0xD0       | false   || 0x20           | 32            | true     | "Carry out"
    }

    @Unroll
    def "SUB (#description): #operandA - #operandB = #expectedValue"(){
        given:
        final Registers registers = new Registers()
        final Mos6502Alu alu = new Mos6502Alu(registers)

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        when:
        final RoxByte result = alu.sub(a,b)

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
        final Registers registers = new Registers()
        final Mos6502Alu alu = new Mos6502Alu(registers)

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
        final Registers registers = new Registers()
        final Mos6502Alu alu = new Mos6502Alu(registers)

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

    @Unroll
    def "XOR (#description): #operandA ^ #operandB = #expectedValue"(){
        given:
        final Registers registers = new Registers()
        final Mos6502Alu alu = new Mos6502Alu(registers)

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        when:
        final RoxByte result = alu.xor(a,b)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        where:
        operandA   | operandB   || expectedResult | expectedValue | description
        0          | 0          || 0              | 0             | "No matching bits"
        1          | 0          || 1              | 1             | "Bit present in A"
        0          | 1          || 1              | 1             | "Bit present in B"
        1          | 1          || 0              | 0             | "Bit present in A+B"
        0b11111111 | 0b10101010 || 0b01010101     | 85            | "Alternating bits matching"
        0b11111111 | 0b11111111 || 0b00000000     | 0             | "ALL bits matching"
        0b01010101 | 0b10101010 || 0b11111111     | -1            | "Consummate bits"
    }
}
