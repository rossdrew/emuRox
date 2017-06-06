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
        "BRK"           | "BRK"
        "ADC_Z_IX"      | "ADC"
        "SEC"           | "SEC"
        "AND_IND_IY"    | "AND"
    }

    @Unroll("Get addressing mode: #opCodeName -> #expectedAddressingMode")
    testAddressingModeExtraction() {
        when:
        AddressingMode addressingMode = OpCodeConverter.getAddressingMode(opCodeName)

        then:
        addressingMode == expectedAddressingMode

        where:
        opCodeName        | expectedAddressingMode
        "BRK"             | AddressingMode.IMPLIED            //VALID
        "ADC_I"           | AddressingMode.IMMEDIATE
        "ROL_A"           | AddressingMode.ACCUMULATOR
        "ADC_Z"           | AddressingMode.ZERO_PAGE
        "ADC_Z_IX"        | AddressingMode.ZERO_PAGE_X
        "ADC_Z_IY"        | AddressingMode.ZERO_PAGE_Y
        "ADC_ABS"         | AddressingMode.ABSOLUTE
        "ADC_ABS_IX"      | AddressingMode.ABSOLUTE_X
        "ADC_ABS_IY"      | AddressingMode.ABSOLUTE_Y
        "ADC_IND"         | AddressingMode.INDIRECT
        "ADC_IND_IX"      | AddressingMode.INDIRECT_X
        "ADC_IND_IY"      | AddressingMode.INDIRECT_Y

//        "ADC_\0I"      | AddressingMode.IMPLIED          //INVALID
//        "ROL_\0A"      | AddressingMode.IMPLIED
//        "ADC_\0Z"      | AddressingMode.IMPLIED
//        "ADC_\0Z_IX"   | AddressingMode.IMPLIED
//        "ADC_Z_\0IX"   | AddressingMode.ZERO_PAGE
//        "ADC_Z_\0IY"   | AddressingMode.ZERO_PAGE
//        "ADC_\0ABS"    | AddressingMode.IMPLIED
//        "ADC_\0ABS_IX" | AddressingMode.IMPLIED
//        "ADC_ABS_\0IX" | AddressingMode.ABSOLUTE
//        "ADC_ABS_\0IY" | AddressingMode.ABSOLUTE
//        "ADC_\0IND"    | AddressingMode.IMPLIED
//        "ADC_\0IND_IX" | AddressingMode.IMPLIED
//        "ADC_IND_\0IX" | AddressingMode.INDIRECT
//        "ADC_IND_\0IY" | AddressingMode.INDIRECT
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
        'ADC_TRX'         | "TRX Not a valid addressing mode"
        'ADC_1'           | "1 Not a valid addressing mode"
    }
}
