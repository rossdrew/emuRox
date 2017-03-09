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
        opCodeName      | expectedDescription               | expected
        'OP_SEC'        | "SEC ($OpCodeConverter.ADDR_IMP)" | "Implied Addressed"
        'OP_ORA_I'      | "ORA ($OpCodeConverter.ADDR_I)"   | "Immediately Addressed"
        'OP_LSR_A'      | "LSR ($OpCodeConverter.ADDR_A)"   | "Accumulator Addressed"
        'OP_LSR_Z'      | "LSR ($OpCodeConverter.ADDR_Z)"   | "Zero Page Addressed"
        'OP_LSR_ABS'    | "LSR ($OpCodeConverter.ADDR_ABS)" | "Absolutely Addressed"
        'OP_AND_IND_IX' | "AND ($OpCodeConverter.ADDR_IND" +
                          "$OpCodeConverter.INDEX_X)"       | "Indirectly Addressed"
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
     * need a case where two Strings have the same hashcode but
     * different .equals()
     *
     * This is because String switch statements compile to a hashcode
     * check then an equals() check so the negative case on the
     * equals() isn't checked by standard unit tests:-
     *
     * switch (s.hashCode()) {  // switch on String hashcode()
     *   case 65:               // Case selected by hascode() value
     *     if (s.equals("I"))   // Confirmed with equals()
     *
     * ref:
     * http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements/42680333?noredirect=1#comment72503143_42680333
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
        opCodeName        | expected
        '\0OP_SEC'        | "Implied addressed"
        '\0OP_ORA_I'      | "Immediately addressed"
        '\0OP_LSR_A'      | "Accumulator addressed"
        '\0OP_LSR_Z'      | "Zero Page addressed"
        '\0OP_LSR_ABS'    | "Absolutely addressed"
        '\0OP_AND_IND_IX' | "Indirectly addressed"

    }
}
