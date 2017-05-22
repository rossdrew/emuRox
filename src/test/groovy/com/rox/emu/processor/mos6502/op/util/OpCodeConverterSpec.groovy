package com.rox.emu.processor.mos6502.op.util

import com.rox.emu.UnknownOpCodeException
import com.rox.emu.processor.mos6502.op.AddressingMode
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
}
