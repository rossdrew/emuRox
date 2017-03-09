package com.rox.emu.p6502.op

import com.rox.emu.UnknownOpCodeException
import spock.lang.Specification
import spock.lang.Unroll

class OpCodeConverterSpec extends Specification{
    @Unroll("Valid (#expected) op-code name")
    testValidAddressingModeOpcodeNames() {
        given:
        String description = "UNKNOWN"

        when:
        description = OpCodeConverter.toDescription(opCodeName)

        then:
        description == expectedDescription

        where:
        opCodeName   | expectedDescription               | expected
        'OP_SEC'     | "SEC ($OpCodeConverter.ADDR_IMP)" | "Implied Addressed"
        'OP_ORA_I'   | "ORA ($OpCodeConverter.ADDR_I)"   | "Immediately Addressed"
        'OP_LSR_A'   | "LSR ($OpCodeConverter.ADDR_A)"   | "Accumulator Addressed"
        'OP_LSR_Z'   | "LSR ($OpCodeConverter.ADDR_Z)"   | "Zero Page Addressed"
        'OP_LSR_ABS' | "LSR ($OpCodeConverter.ADDR_ABS)" | "Absolutely Addressed"
    }

    @Unroll("Invalid op-code: #expected")
    testInvalidOpcodes(){
        given:
        String description = "UNKNOWN"

        when:
        description = OpCodeConverter.toDescription(opCodeName)

        then:
        thrown UnknownOpCodeException
        description == "UNKNOWN"

        where:
        opCodeName    | expected
        'OP_LSR_XXX'  | "Unknown addressing mode"
        'OP_LSR_Z_IZ' | "Unknown indexing mode"
    }

    /*
     * JaCoCo doesn't cover String switch statements properly, so we
     * need a case where two Strings have the same hascode but
     * different .equals()
     */
    @Unroll("JaCoCo: Invalid (#expected) op-code")
    testAroundJaCoCoIssue(){
        given:
        String description = "UNKNOWN"

        when:
        description = OpCodeConverter.toDescription(opCodeName)

        then:
        thrown UnknownOpCodeException
        description == "UNKNOWN"

        where:
        opCodeName     | expected
        '\0OP_SEC'     | "Implied Addressed"
        '\0OP_ORA_I'   | "Immediately Addressed"
        '\0OP_LSR_A'   | "Accumulator Addressed"
        '\0OP_LSR_Z'   | "Zero Page Addressed"
        '\0OP_LSR_ABS' | "Absolutely Addressed"
    }
}
