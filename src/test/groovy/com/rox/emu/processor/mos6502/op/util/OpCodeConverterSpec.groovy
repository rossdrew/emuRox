package com.rox.emu.processor.mos6502.op.util

import com.rox.emu.UnknownOpCodeException
import com.rox.emu.processor.mos6502.op.Mos6502AddressingMode
import com.rox.emu.processor.mos6502.op.OpCode
import com.rox.emu.processor.mos6502.op.Mos6502Operation
import spock.lang.Specification
import spock.lang.Unroll

class OpCodeConverterSpec extends Specification{
    @Unroll("Get token: #opCodeName -> #expectedOpCodeName")
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
        Mos6502AddressingMode addressingMode = OpCodeConverter.getAddressingMode(opCodeName)

        then:
        addressingMode == expectedAddressingMode

        where:
        opCodeName        | expectedAddressingMode
        "BRK"             | Mos6502AddressingMode.IMPLIED            //VALID
        "ADC_I"           | Mos6502AddressingMode.IMMEDIATE
        "ROL_A"           | Mos6502AddressingMode.ACCUMULATOR
        "ADC_Z"           | Mos6502AddressingMode.ZERO_PAGE
        "ADC_Z_IX"        | Mos6502AddressingMode.ZERO_PAGE_X
        "ADC_Z_IY"        | Mos6502AddressingMode.ZERO_PAGE_Y
        "ADC_ABS"         | Mos6502AddressingMode.ABSOLUTE
        "ADC_ABS_IX"      | Mos6502AddressingMode.ABSOLUTE_X
        "ADC_ABS_IY"      | Mos6502AddressingMode.ABSOLUTE_Y
        "ADC_IND"         | Mos6502AddressingMode.INDIRECT
        "ADC_IND_IX"      | Mos6502AddressingMode.INDIRECT_X
        "ADC_IND_IY"      | Mos6502AddressingMode.INDIRECT_Y
        "BEQ"             | Mos6502AddressingMode.RELATIVE
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

    @Unroll("Get operation from #opCodeName")
    testGetOperation(){
        when:
        final Mos6502Operation operation = OpCodeConverter.getOperation(opCodeName)

        then:
        operation == expectedOperation

        where:
        opCodeName    || expectedOperation
        "BRK"         || Mos6502Operation.BRK
        "ASL_A"       || Mos6502Operation.ASL
        "AND_IND_IX"  || Mos6502Operation.AND
    }

    def testGetOperationForEveryOpcode(){
        when:
        boolean mismatches = OpCode.values().any {opCode -> opCode.operation != OpCodeConverter.getOperation(opCode.opCodeName)}

        then:
        !mismatches
    }
}
