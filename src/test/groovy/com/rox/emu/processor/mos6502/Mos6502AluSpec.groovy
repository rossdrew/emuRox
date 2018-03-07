package com.rox.emu.processor.mos6502

import com.rox.emu.env.RoxByte
import spock.genesis.Gen
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class Mos6502AluSpec extends Specification {
    private Registers registers
    private Mos6502Alu alu

    private static final int RANDOM_ITERATIONS = 10

    def clearRegisters(){
        for (flag in Registers.Flag.values()) registers.setFlagTo(flag, false)
    }

    def setup(){
        registers = new Registers()
        clearRegisters()
        alu = new Mos6502Alu(registers)
    }

    @Unroll
    def "ADC (#description): #operandA + #operandB = #expectedValue"(){
        given: 'Some numbers to add'
        final RoxByte a = RoxByte.fromLiteral(operandA)
        final RoxByte b = RoxByte.fromLiteral(operandB)

        and: 'The status flags are setup beforehand'
        registers.setFlagTo(Registers.Flag.CARRY, carryIn)

        when: 'The operation is performed'
        final RoxByte result = alu.adc(a, b)

        then: 'The values and flags are as expected'
        expectedResult == result.rawValue
        expectedValue == result.asInt
        registers.getFlag(Registers.Flag.CARRY) == carryOut
        registers.getFlag(Registers.Flag.OVERFLOW) == overflow

        where:
        operandA   | operandB   | carryIn || expectedResult | expectedValue | carryOut | overflow | description
        0          | 0          | false   || 0              | 0             | false    | false    | "No change"
        1          | 1          | false   || 2              | 2             | false    | false    | "Simple addition"
        0          | 1          | false   || 1              | 1             | false    | false    | "Left hand zero addition"
        1          | 0          | false   || 1              | 1             | false    | false    | "Right hand zero addition"
        127        | 1          | false   || 128            | -128          | false    | true     | "Signed Overflow"
        0b11111111 | 1          | false   || 0              | 0             | true     | false    | "Signed negative to zero"
        0b11111111 | 10         | false   || 9              | 9             | true     | false    | "Signed negative to positive"
        0b10000000 | 1          | false   || 0b10000001     | -127          | false    | false    | "Positive addition to negative"
        1          | 0b11111111 | false   || 0              | 0             | true     | false    | "Negative addition to positive"
        0          | 0          | true    || 1              | 1             | false    | false    | "Carry in"
        0x50       | 0xD0       | false   || 0x20           | 32            | true     | false    | "Carry out"
        0x50       | 0xCF       | true    || 0x20           | 32            | true     | false    | "Carry in, carry out"
        126        | 1          | true    || 128            | -128          | false    | true     | "Carry in then overflow"
        0x80       | 0x90       | false   || 0x10           | 0x10          | true     | true     | "Carry out and overflow var.1"
        0xD0       | 0x90       | false   || 0x60           | 96            | true     | true     | "Carry out and overflow var.2"
    }

    @Unroll
    def "Expected value for: #randomValueA + #randomValueB"(){
        when: 'we add two random numbers'
        final RoxByte result = alu.adc(RoxByte.fromLiteral(randomValueA),
                                       RoxByte.fromLiteral(randomValueB))

        and: 'the expected (one byte) result is'
        final int expected = ((randomValueA + randomValueB) & 0xFF)

        then: 'the one byte result should be'
        result.getRawValue() == expected

        where: 'we generate 100 sets of random numbers in byte range'
        randomValueA << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
        randomValueB << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
    }

    @Unroll
    def "SBC (#description): #operandA - #operandB = #expectedValue"(){
        given: 'Some numbers to subtract'
        final RoxByte a = RoxByte.fromLiteral(operandA)
        final RoxByte b = RoxByte.fromLiteral(operandB)

        and: 'Carry is set before an SBC operation'
        registers.setFlag(Registers.Flag.CARRY)

        when:
        final RoxByte result = alu.sbc(a, b)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt
        registers.getFlag(Registers.Flag.CARRY) == carryOut

        and: 'The overflow flag, which functions as an underflow in this context is as expected'
        registers.getFlag(Registers.Flag.OVERFLOW) == underflow

        where:
        operandA   | operandB   || expectedResult | expectedValue | carryOut | underflow | description
        0          | 0          || 0              | 0             | true     | false     | "No change"
        1          | 1          || 0              | 0             | true     | false     | "Number minus itself"
        10         | 9          || 1              | 1             | true     | false     | "Positive result"
        0          | 1          || 0b11111111     | -1            | false    | false     | "Negative result"
        0b11111111 | 1          || 0b11111110     | -2            | true     | false     | "Positive subtraction from a negative"
        0b11111111 | 0b11111111 || 0              | 0             | true     | false     | "Negative subtraction from a negative"
        0b10000000 | 1          || 0b01111111     | 127           | true     | true      | "Signed underflow with carry"
        0x50       | 0xB0       || 0xA0           | -96           | false    | true      | "Signed underflow without carry"
    }

    @Unroll
    def "Expected value for: #randomValueA - #randomValueB"(){
        when: 'The status flags are setup beforehand'
        registers.setFlag(Registers.Flag.CARRY)

        and: 'we subtract a number from another'
        final RoxByte result = alu.sbc(RoxByte.fromLiteral(randomValueA),
                                       RoxByte.fromLiteral(randomValueB))

        and: 'the expected (one byte) result is'
        final int expected = ((randomValueA - randomValueB) & 0xFF)

        then: 'the one byte result should be'
        result.getRawValue() == expected

        where: 'we generate n sets of random numbers in byte range'
        randomValueA << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
        randomValueB << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
    }

    @Unroll
    def "OR (#description): #operandA | #operandB = #expectedValue"(){
        given: 'Some numbers to or'
        final RoxByte a = RoxByte.fromLiteral(operandA)
        final RoxByte b = RoxByte.fromLiteral(operandB)

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
    def "Expected value for: #randomValueA OR #randomValueB"(){
        when: 'we OR two random numbers'
        final RoxByte result = alu.or(RoxByte.fromLiteral(randomValueA),
                                      RoxByte.fromLiteral(randomValueB))

        and: 'the expected (one byte) result is'
        final int expected = ((randomValueA | randomValueB) & 0xFF)

        then: 'the one byte result should be'
        result.getRawValue() == expected

        where: 'we generate n sets of random numbers in byte range'
        randomValueA << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
        randomValueB << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
    }

    @Unroll
    def "AND (#description): #operandA & #operandB = #expectedValue"(){
        given: 'Some numbers to and'
        final RoxByte a = RoxByte.fromLiteral(operandA)
        final RoxByte b = RoxByte.fromLiteral(operandB)

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
    def "Expected value for: #randomValueA AND #randomValueB"(){
        when: 'we AND two random numbers'
        final RoxByte result = alu.and(RoxByte.fromLiteral(randomValueA),
                                       RoxByte.fromLiteral(randomValueB))

        and: 'the expected (one byte) result is'
        final int expected = ((randomValueA & randomValueB) & 0xFF)

        then: 'the one byte result should be'
        result.getRawValue() == expected

        where: 'we generate n sets of random numbers in byte range'
        randomValueA << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
        randomValueB << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
    }

    @Unroll
    def "XOR (#description): #operandA ^ #operandB = #expectedValue"(){
        given: 'Some numbers to xor'
        final RoxByte a = RoxByte.fromLiteral(operandA)
        final RoxByte b = RoxByte.fromLiteral(operandB)

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

    @Unroll
    def "Expected value for: #randomValueA XOR #randomValueB"(){
        when: 'we XOR two random numbers'
        final RoxByte result = alu.xor(RoxByte.fromLiteral(randomValueA),
                RoxByte.fromLiteral(randomValueB))

        and: 'the expected (one byte) result is'
        final int expected = ((randomValueA ^ randomValueB) & 0xFF)

        then: 'the one byte result should be'
        result.getRawValue() == expected

        where: 'we generate n sets of random numbers in byte range'
        randomValueA << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
        randomValueB << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
    }

    @Unroll
    def "ASL (#description): #operandA = #expectedValue"(){
        given: 'A number to shift left'
        final RoxByte a = RoxByte.fromLiteral(operandA)

        and: 'The status flags are setup beforehand'
        registers.setFlagTo(Registers.Flag.CARRY, carryIn)

        when:
        final RoxByte result = alu.asl(a)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        and: 'bits shifted out of the end, end up in the carry'
        registers.getFlag(Registers.Flag.CARRY) == carryOut

        where:
        operandA   | carryIn || expectedResult | expectedValue | carryOut | description
        0          | false   || 0              | 0             | false    | "Zero to zero"
        0          | true    || 0              | 0             | false    | "Zero always \"carried\" in"
        1          | false   || 2              | 2             | false    | "Simplest case"
        0b01000000 | false   || 0b10000000     | -128          | false    | "Shift positive to negative"
        0b10000000 | false   || 0              | 0             | true     | "Shift to zero"
        0b10000000 | false   || 0              | 0             | true     | "Shift to zero"
        0b10000001 | false   || 2              | 2             | true     | "Carry out"
    }

    @Unroll
    def "Expected value for: ASL #randomValueA"(){
        when: 'we ASL a random number'
        final RoxByte result = alu.asl(RoxByte.fromLiteral(randomValueA))

        and: 'the expected (one byte) result is'
        final int expected = ((randomValueA << 1) & 0xFF)

        then: 'the one byte result should be the same'
        result.getRawValue() == expected

        where: 'we generate n random numbers in byte range'
        randomValueA << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
    }

    @Unroll
    def "ROL (#description): #operandA = #expectedValue"(){
        given: 'A number to shift left'
        final RoxByte a = RoxByte.fromLiteral(operandA)

        and: 'The status flags are setup beforehand'
        registers.setFlagTo(Registers.Flag.CARRY, carryIn)

        when:
        final RoxByte result = alu.rol(a)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        and: 'bits shifted out of the end, end up in the carry'
        registers.getFlag(Registers.Flag.CARRY) == carryOut

        where:
        operandA   | carryIn || expectedResult | expectedValue | carryOut | description
        0          | false   || 0              | 0             | false    | "Zero to zero"
        1          | false   || 2              | 2             | false    | "Simplest case"
        0b01000000 | false   || 0b10000000     | -128          | false    | "Shift positive to negative"
        0b10000000 | false   || 0              | 0             | true     | "Shift to zero"
        0b10000000 | false   || 0              | 0             | true     | "Shift to zero"
        0b10000001 | false   || 2              | 2             | true     | "Carry out"
        0          | true    || 1              | 1             | false    | "Carry carried in"
        0b10000000 | true    || 1              | 1             | true     | "Carry in, carry out"
        0b01000001 | true    || 0x83           | -125          | false    | "Carry in, positive to negative"
    }

    @Ignore("This test is failing on some numbers, perhaps a badly written test?!")
    @Unroll
    def "Expected value for: ROL #randomValueA with carry set to #randomCarryFlag"(){
        when: 'we have a random carry state'
        int carryIn = randomCarryFlag ? 1 : 0

        and: 'we ROL a random number'
        final RoxByte result = alu.rol(RoxByte.fromLiteral(randomValueA))

        and: 'the expected (one byte) result, including the carry is'
        final int expected = (((randomValueA << 1) | carryIn) & 0xFF)

        then: 'the one byte result should be the same'
        result.getRawValue() == expected

        where: 'we generate n random numbers in byte range and n random true/false values'
        randomValueA << Gen.integer(0..255).iterator().take(RANDOM_ITERATIONS)
        randomCarryFlag << Gen.any(true, false).iterator().take(RANDOM_ITERATIONS)
    }

    @Unroll
    def "LSR (#description): #operandA = #expectedValue"(){
        given: 'A number to shift left'
        final RoxByte a = RoxByte.fromLiteral(operandA)

        and: 'The status flags are setup beforehand'
        registers.setFlagTo(Registers.Flag.CARRY, carryIn)

        when:
        final RoxByte result = alu.lsr(a)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        and: 'bits shifted out of the end, end up in the carry'
        registers.getFlag(Registers.Flag.CARRY) == carryOut

        where:
        operandA   | carryIn || expectedResult | expectedValue | carryOut | description
        0          | false   || 0              | 0             | false    | "Zero to zero"
        0b00000001 | false   || 0              | 0             | true     | "Shift to zero with carry out"
        0b00000000 | true    || 0              | 0             | false    | "Zero always \"carried\" in"
        0b10000000 | false   || 0b01000000     | 64            | false    | "Shift negative to positive"
        0b11111111 | false   || 0b01111111     | 127           | true     | "Shift negative to positive with carry out"
    }

    @Unroll
    def "ROR (#description): #operandA = #expectedValue"(){
        given: 'A number to shift left'
        final RoxByte a = RoxByte.fromLiteral(operandA)

        and: 'The status flags are setup beforehand'
        registers.setFlagTo(Registers.Flag.CARRY, carryIn)

        when:
        final RoxByte result = alu.ror(a)

        then:
        expectedResult == result.rawValue
        expectedValue == result.asInt

        and: 'bits shifted out of the end, end up in the carry'
        registers.getFlag(Registers.Flag.CARRY) == carryOut

        where:
        operandA   | carryIn || expectedResult | expectedValue | carryOut | description
        0          | false   || 0              | 0             | false    | "Zero to zero"
        0b00000001 | false   || 0              | 0             | true     | "Shift to zero with carry out"
        0b00000000 | true    || 0b10000000     | -128          | false    | "Carry in to negative"
        0b10000000 | false   || 0b01000000     | 64            | false    | "Shift negative to positive"
        0b11111111 | false   || 0b01111111     | 127           | true     | "Shift negative to positive with carry out"
        0b00000001 | true    || 0b10000000     | -128          | true     | "Carry in, carry out"
    }
}
