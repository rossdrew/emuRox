package com.rox.emu.P6502

import com.rox.emu.Memory
import com.rox.emu.SimpleMemory
import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.P6502.InstructionSet.*;

class OpCodeSpec extends Specification {

    @Unroll("LDA Immediate #Expected: Load #loadValue == #expectedAccumulator")
    def testImmediateLDA() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, loadValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadValue | expectedAccumulator | Z     | N     | Expected
        0x0       | 0x0                 | true  | false | "With zero result"
        0x1       | 0x1                 | false | false | "Generic test 1"
        0x7F      | 0x7F                | false | false | "Generic test 2"
        0x80      | 0x80                | false | true  | "With negative result"
        0x81      | 0x81                | false | true  | "With (boundary test) negative result "
        0xFF      | 0xFF                | false | true  | "With max negative result"
    }

    @Unroll("LDA ZeroPage #Expected: Expecting #loadValue @ [30]")
    def testLDAFromZeroPage() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_Z, 30]
        memory.setByte(30, loadValue)
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadValue | expectedAccumulator | Z     | N     | Expected
        0x0       | 0x0                 | true  | false | "With zero result"
        0x1       | 0x1                 | false | false | "Generic test 1"
        0x7F      | 0x7F                | false | false | "Generic test 2"
        0x80      | 0x80                | false | true  | "With negative result"
        0x81      | 0x81                | false | true  | "With (boundary test) negative result "
        0xFF      | 0xFF                | false | true  | "With max negative result"
    }

    @Unroll("LDA Absolute #Expected: Expecting #loadValue @ [300]")
    def testAbsoluteLDA() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_A, 0x1, 0x2C]
        memory.setByte(300, loadValue)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadValue | expectedAccumulator | Z     | N     | Expected
        0x0       | 0x0                 | true  | false | "With zero result"
        0x1       | 0x1                 | false | false | "Generic test 1"
        0x7F      | 0x7F                | false | false | "Generic test 2"
        0x80      | 0x80                | false | true  | "With negative result"
        0x81      | 0x81                | false | true  | "With (boundary test) negative result "
        0xFF      | 0xFF                | false | true  | "With max negative result"
    }

    @Unroll("LDA Indexed Zero Page, X #Expected: Load [0x30 + X(#index)] -> #expectedAccumulator")
    def testLDAFromZeroPageIndexedByX() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_Z_IX, 0x30]
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(0x30, values)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | expectedAccumulator | Z     | N     | Expected
          0   | 0                   | true  | false | "With zero result"
          1   | 11                  | false | false | "With normal result"
          2   | 0xFF                | false | true  | "With negative result"
    }

    @Unroll("LDA Indexed by X. #Expected: 300[#index] = #expectedAccumulator")
    def testLDAIndexedByX() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_IX, 1, 0x2C]
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(300, values)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | expectedAccumulator | Z     | N     | Expected
        0     | 0                   | true  | false | "With zero result"
        1     | 11                  | false | false | "With normal result"
        2     | 0xFF                | false | true  | "With negative result"
    }

    @Unroll("LDX Immediate: Load #firstValue")
    def testLDX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, firstValue]
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedX  | Z      | N     | Expected
        99         | 99         | false  | false | "Simple load"
        0          | 0          | true   | false | "Load zero"
        0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDY Immediate: Load #firstValue")
    def testLDY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, firstValue]
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_Y_INDEX) == expectedY
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedY  | Z      | N     | Expected
        99         | 99         | false  | false | "Simple load"
        0          | 0          | true   | false | "Load zero"
        0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDA Indexed by Y. #Expected: 300[#index] = #expectedAccumulator")
    def testLDAIndexedByY() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index, OP_LDA_IY, 1, 0x2C]
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(300, values)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | expectedAccumulator | Z     | N     | Expected
        0     | 0                   | true  | false | "With zero result"
        1     | 11                  | false | false | "With normal result"
        2     | 0xFF                | false | true  | "With negative result"
    }

    @Unroll("ADC Immediate #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ADC_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | C     | O     | Expected
        0x0        | 0x0         | 0x0                 | true   | false | false | false | "With zero result"
        0x50       | 0xD0        | 0x20                | false  | false | true  | false | "With positive, carried result"
        0x50       | 0x50        | 0xA0                | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("ADC ZeroPage #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADCFromZeroPage(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ADC_Z, 0x30]
        memory.setMemory(0, program);
        memory.setByte(0x30, secondValue)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | C     | O     | Expected
        0x0        | 0x0         | 0x0                 | true   | false | false | false | "With zero result"
        0x50       | 0xD0        | 0x20                | false  | false | true  | false | "With positive, carried result"
        0x50       | 0x50        | 0xA0                | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("ADC Absolute #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADCAbsolute(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ADC_A, 0x1, 0x2C]
        memory.setMemory(0, program);
        memory.setByte(300, secondValue)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | C     | O     | Expected
        0x0        | 0x0         | 0x0                 | true   | false | false | false | "With zero result"
        0x50       | 0xD0        | 0x20                | false  | false | true  | false | "With positive, carried result"
        0x50       | 0x50        | 0xA0                | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("ADC 16bit [#lowFirstByte|#highFirstByte] + [#lowSecondByte|#highSecondByte] = #Expected")
    def testMultiByteADC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_CLC,
                         OP_LDA_I, lowFirstByte,
                         OP_ADC_I, lowSecondByte,
                         OP_STA_Z, 40,
                         OP_LDA_I, highFirstByte,
                         OP_ADC_I, highSecondByte]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(6)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]
        storedValue == memory.getByte(40)

        where:
        lowFirstByte | lowSecondByte | highFirstByte | highSecondByte | expectedAccumulator | storedValue | Z    | N     | C     | O     | Expected
        0            | 0             | 0             | 0              | 0                   | 0           | true  | false | false | false | "With zero result"
        0x50         | 0xD0          | 0             | 0              | 1                   | 0x20        | false | false | true  | false | "With simple carry to high byte"
        0x50         | 0xD3          | 0             | 1              | 2                   | 0x23        | false | false | true  | false | "With carry to high byte and changed high"
        0            | 0             | 0x50          | 0x50           | 0xA0                | 0           | false | true  | false | true  | "With negative overflow"
    }

    @Unroll("AND Immediate #Expected:  #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    def testAND(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_AND_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0b00000001 | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0b00000011 | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }

    @Unroll("OR Immediate #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    def testOR(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ORA_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }

    @Unroll("EOR Immediate #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    def testEOR(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_EOR_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }

    @Unroll("SBC Immediate #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def testSBC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_SEC, OP_LDA_I, firstValue, OP_SBC_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | O     | C     | Expected
        0x5        | 0x3         | 0x2                 | false  | false | false | false | "Basic subtraction"
        0x5        | 0x5         | 0x0                 | true   | false | false | false | "With zero result"
        0x5        | 0x6         | 0xFF                | false  | true  | false | false | "with negative result"
    }

    @Unroll("INX #Expected: on #firstValue = #expectedX")
    def testINX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, firstValue, OP_INX]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedX | Z      | N     | Expected
        0          | 1         | false  | false | "Simple increment"
        0xFE       | 0xFF      | false  | true  | "Increment to negative value"
        0b11111111 | 0x0       | true   | false | "Increment to zero"
    }

    @Unroll("DEX #Expected: on #firstValue = #expectedX")
    def testDEX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, firstValue, OP_DEX]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedX | Z      | N     | Expected
        5          | 4         | false  | false | "Simple increment"
        0          | 0xFF      | false  | true  | "Decrement to negative value"
        1          | 0x0       | true   | false | "Increment to zero"
    }

    @Unroll("INY #Expected: on #firstValue = #expectedX")
    def testINY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, firstValue, OP_INY]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_Y_INDEX) == expectedX
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedX | Z      | N     | Expected
        0          | 1         | false  | false | "Simple increment"
        0xFE       | 0xFF      | false  | true  | "Increment to negative value"
        0b11111111 | 0x0       | true   | false | "Increment to zero"
    }

    @Unroll("DEY #Expected: on #firstValue = #expectedY")
    def testDEY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, firstValue, OP_DEY]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_Y_INDEX) == expectedY
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedY | Z      | N     | Expected
        5          | 4         | false  | false | "Simple increment"
        0          | 0xFF      | false  | true  | "Decrement to negative value"
        1          | 0x0       | true   | false | "Increment to zero"
    }

    @Unroll("PLA #Expected: #firstValue from stack at (#expectedSP - 1)")
    def testPLA(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_PHA, OP_LDA_I, 0x11, OP_PLA];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2);
        assert(registers.getRegister(Registers.REG_SP) == 0xFE)
        processor.step(2);

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_SP) == expectedSP
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        stackItem == memory.getByte(0xFF)

        where:
        firstValue | expectedSP | stackItem  | expectedAccumulator | Z     | N     | Expected
        0x99       | 0xFF       | 0x99       | 0x99                | false | false | "Basic stack push"
        0x0        | 0xFF       | 0x0        | 0x0                 | false | true  | "Zero stack push"
        0b10001111 | 0xFF       | 0b10001111 | 0b10001111          | true  | true  | "Negative stack push"
    }

    @Unroll("ASL (Accumulator) #Expected: #firstValue becomes #expectedAccumulator")
    def testASL(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ASL_A];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | expectedAccumulator | Z     | N     | C     | Expected
        0b00010101 | 0b00101010          | false | false | false | "Basic shift"
        0b00000000 | 0b00000000          | true  | false | false | "Zero shift"
        0b01000000 | 0b10000000          | false | true  | false | "Negative shift"
        0b10000001 | 0b00000010          | false | false | true  | "Carried shift"
        0b10000000 | 0b00000000          | true  | false | true  | "Carried, zero shift"
        0b11000000 | 0b10000000          | false | true  | true  | "Carried, negative shift"
    }

    @Unroll("ASL (ZeroPage) #Expected: #firstValue becomes #expectedMem")
    def testASL_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, 0,
                         OP_ASL_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        memory.getByte(0x20) == expectedMem
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | expectedMem | Z     | N     | C     | Expected
        0b00010101 | 0b00101010  | false | false | false | "Basic shift"
        0b00000000 | 0b00000000  | true  | false | false | "Zero shift"
        0b01000000 | 0b10000000  | false | true  | false | "Negative shift"
        0b10000001 | 0b00000010  | false | false | true  | "Carried shift"
        0b10000000 | 0b00000000  | true  | false | true  | "Carried, zero shift"
        0b11000000 | 0b10000000  | false | true  | true  | "Carried, negative shift"
    }

    @Unroll("LSR (Accumulator) #Expected: #firstValue becomes #expectedAccumulator")
    def testLSR(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_LSR_A];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | expectedAccumulator | Z     | N     | C     | Expected
        0b01000000 | 0b00100000          | false | false | false | "Basic shift"
        0b00000000 | 0b00000000          | true  | false | false | "Shift to zero"
        0b00000011 | 0b00000001          | false | false | true  | "Shift with carry"
        0b00000001 | 0b00000000          | true  | false | true  | "Shift to zero with carry"
    }

    @Unroll("LSR (Zero Page) #Expected: #firstValue becomes #expectedMem")
    def testLSR_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, 1,
                         OP_LSR_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        memory.getByte(0x20) == expectedMem
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | expectedMem | Z     | N     | C     | Expected
        0b01000000 | 0b00100000  | false | false | false | "Basic shift"
        0b00000000 | 0b00000000  | true  | false | false | "Shift to zero"
        0b00000011 | 0b00000001  | false | false | true  | "Shift with carry"
        0b00000001 | 0b00000000  | true  | false | true  | "Shift to zero with carry"
    }

    @Unroll("ROL ZeroPage #Expected: #firstValue -> #expectedMem")
    def testROL_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_CLC, OP_LDA_I, firstValue, OP_STA_Z, 0x20, OP_ROL_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        expectedMem == memory.getByte(0x20)
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | expectedMem | Z     | N     | C     | Expected
        0b00000001 | 0b00000010  | false | false | false | "Basic rotate left"
        0b01000000 | 0b10000000  | false | true  | false | "Rotate to negative"
        0b00000000 | 0b00000000  | true  | false | false | "Rotate to zero without carry"
      //  0b10000000 | 0b00000000  | true  | false | true  | "Rotate to zero with carry" TODO
        //TODO with/without carry in/out
    }

    //TODO BCC: jump forward/back, carry set/not set
    //TODO ROL: with/without carry in/out, with/without negative, with/without zero
    //TODO BNE: jump forward/back, zero set/not set
}
