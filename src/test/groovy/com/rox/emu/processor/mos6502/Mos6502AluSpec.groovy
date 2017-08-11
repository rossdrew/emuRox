package com.rox.emu.processor.mos6502

import com.rox.emu.env.RoxByte
import spock.lang.Specification
import spock.lang.Unroll

class Mos6502AluSpec extends Specification {
    @Unroll
    def "ADD (#description): #operandA + #operandB = #expectedResult"(){
        given:
        Mos6502Alu alu = new Mos6502Alu()

        and: 'Some numbers to add'
        final RoxByte a = RoxByte.literalFrom(operandA)
        final RoxByte b = RoxByte.literalFrom(operandB)

        when:
        final RoxByte result = alu.add(a,b)

        then:
        expectedResult == result.rawValue

        where:
        operandA | operandB || expectedResult | description
        1        | 1        || 2              | "Simple addition"
        0        | 1        || 1              | "Left hand zero addition"
        1        | 0        || 1              | "Right hand zero addition"
    }
}
