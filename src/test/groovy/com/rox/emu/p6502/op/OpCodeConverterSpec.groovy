package com.rox.emu.p6502.op

import com.rox.emu.UnknownOpCodeException
import spock.lang.Specification
import spock.lang.Unroll

class OpCodeConverterSpec extends Specification{
    @Unroll("Get opCode: #opCodeName -> #expectedOpCodeName")
    testGetOpcode(){
        when:
        String opCode = OpCodeConverter.getOpCode(opCodeName)

        then:
        opCode == expectedOpCodeName

        where:
        opCodeName      | expectedOpCodeName
        "OP_BRK"        | "BRK"
        "OP_ADC_Z_IX"   | "ADC"
        "OP_SEC"        | "SEC"
        "OP_AND_IND_IY" | "AND"
    }

    @Unroll("Get addressing mode: #opCodeName -> #expectedAddressingMode")
    testAddressingModeExtraction() {
        when:
        AddressingMode addressingMode = OpCodeConverter.getAddressingMode(opCodeName)

        then:
        addressingMode == expectedAddressingMode

        where:
        opCodeName        | expectedAddressingMode
        "OP_BRK"          | AddressingMode.IMPLIED            //VALID
        "OP_ADC_I"        | AddressingMode.IMMEDIATE
        "OP_ROL_A"        | AddressingMode.ACCUMULATOR
        "OP_ADC_Z"        | AddressingMode.ZERO_PAGE
        "OP_ADC_Z_IX"     | AddressingMode.ZERO_PAGE_X
        "OP_ADC_Z_IY"     | AddressingMode.ZERO_PAGE_Y
        "OP_ADC_ABS"      | AddressingMode.ABSOLUTE
        "OP_ADC_ABS_IX"   | AddressingMode.ABSOLUTE_X
        "OP_ADC_ABS_IY"   | AddressingMode.ABSOLUTE_Y
        "OP_ADC_IND"      | AddressingMode.INDIRECT
        "OP_ADC_IND_IX"   | AddressingMode.INDIRECT_X
        "OP_ADC_IND_IY"   | AddressingMode.INDIRECT_Y

//        "OP_ADC_\0I"      | AddressingMode.IMPLIED          //INVALID
//        "OP_ROL_\0A"      | AddressingMode.IMPLIED
//        "OP_ADC_\0Z"      | AddressingMode.IMPLIED
//        "OP_ADC_\0Z_IX"   | AddressingMode.IMPLIED
//        "OP_ADC_Z_\0IX"   | AddressingMode.ZERO_PAGE
//        "OP_ADC_Z_\0IY"   | AddressingMode.ZERO_PAGE
//        "OP_ADC_\0ABS"    | AddressingMode.IMPLIED
//        "OP_ADC_\0ABS_IX" | AddressingMode.IMPLIED
//        "OP_ADC_ABS_\0IX" | AddressingMode.ABSOLUTE
//        "OP_ADC_ABS_\0IY" | AddressingMode.ABSOLUTE
//        "OP_ADC_\0IND"    | AddressingMode.IMPLIED
//        "OP_ADC_\0IND_IX" | AddressingMode.IMPLIED
//        "OP_ADC_IND_\0IX" | AddressingMode.INDIRECT
//        "OP_ADC_IND_\0IY" | AddressingMode.INDIRECT
    }

    @Unroll("Get addressing mode (INVALID): #expected")
    testGetAddressingModeWithInvalidOpCode(){
        given:
        String description = "UNKNOWN"

        when:
        description = OpCodeConverter.getAddressingMode(opCodeName)

        then:
        thrown UnknownOpCodeException
        description == "UNKNOWN"

        where:
        opCodeName        | expected
        'OP_ADC_TRX'      | "TRX Not a valid addressing mode"
        'OP_ADC_1'        | "1 Not a valid addressing mode"
    }

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
                          "$OpCodeConverter.INDEX_X)"       | "Indirectly Addressed with X"
        'OP_AND_IND_IY' | "AND ($OpCodeConverter.ADDR_IND" +
                          "$OpCodeConverter.INDEX_Y)"       | "Indirectly Addressed with Y"
    }

    @Unroll("Invalid op-code name: #expected")
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
        ''            | "Empty"
        null          | "null"
        'PO_SEC'      | "Unknown prefix"
    //  'OP_XXX'      | "Unknown instruction"
        'OP_LSR_XXX'  | "Unknown addressing mode"
        'OP_LSR_Z_IZ' | "Unknown indexing mode"
    }

    /*
     * JaCoCo doesn't cover String switch statements properly, so we
     * need a case where two Strings have the same hashcode but
     * different .equals() in order to get 100% coverage
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
        '\0OP_SEC'        | "Implied op code"

        'OP_ORA_\0I'      | "Immediately addressed"
        'OP_LSR_\0A'      | "Accumulator addressed"
        'OP_LSR_\0Z'      | "Zero Page addressed"
        'OP_LSR_\0ABS'    | "Absolutely addressed"
        'OP_AND_\0IND_IX' | "Indirectly addressed with X"
        'OP_AND_\0IND_IY' | "Indirectly addressed with Y"

        'OP_AND_IND_\0IX' | "X Indexed"
        'OP_AND_IND_\0IY' | "Y Indexed"
    }
}
