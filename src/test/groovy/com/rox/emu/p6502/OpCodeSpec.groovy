package com.rox.emu.p6502

import com.rox.emu.Memory
import com.rox.emu.SimpleMemory
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.junit.rules.Timeout
import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.p6502.InstructionSet.*;

class OpCodeSpec extends Specification {
    @Unroll("LDA (Immediate) #Expected: Load #loadValue == #expectedAccumulator")
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

    @Unroll("LDA (Zero Page) #Expected: Expecting #loadValue @ [30]")
    def testLDAFromZeroPage() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_Z, 30]
        memory.setByteAt(30, loadValue)
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

    @Unroll("LDA (Zero Page[X]) #Expected: Load [0x30 + X(#index)] -> #expectedAccumulator")
    def testLDAFromZeroPageIndexedByX() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_Z_IX, 0x30]
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

    @Unroll("LDA (Absolute) #Expected: Expecting #loadValue @ [300]")
    def testAbsoluteLDA() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_ABS, 0x1, 0x2C]
        memory.setByteAt(300, loadValue)
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

    @Unroll("LDA (Absolute[X]). #Expected: 300[#index] = #expectedAccumulator")
    def testLDAIndexedByX() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_ABS_IX, 1, 0x2C]
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


    @Unroll("LDA (Absolute[Y]). #Expected: 300[#index] = #expectedAccumulator")
    def testLDAIndexedByY() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index,
                         OP_LDA_ABS_IY, 1, 0x2C]
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

    @Unroll("LDA (Indirect, X). #Expected: 0x30[#index] -> #indAddress = #expectedAccumulator")
    def testLDA_IND_IX() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,    //Value at indirect address
                         OP_STA_ABS, indAddressHi, indAddressLo,
                         OP_LDX_I, index,
                         OP_LDA_I, indAddressHi,  //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, indAddressLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_IND_IX, 0x30]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(8)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        indAddressHi | indAddressLo | index | firstValue | expectedAccumulator | Z     | N     | Expected
        0x02         | 0x20         | 0     | 0          | 0                   | true  | false | "With zero result"
        0x03         | 0x40         | 1     | 11         | 11                  | false | false | "With normal result"
        0x04         | 0x24         | 2     | 0xFF       | 0xFF                | false | true  | "With negative result"
    }

    @Unroll("LDX (Immediate): Load #firstValue")
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

    @Unroll("LDX (Absolute): Load [#addressHi | #addressLo] with #firstValue")
    def testLDX_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_ABS, addressHi, addressLo]
        memory.setMemory(0, program)

        and:
        memory.setByteAt(addressHi << 8 | addressLo, firstValue)

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
        addressHi | addressLo  | firstValue | expectedX  | Z      | N     | Expected
        1         | 0x20       | 99         | 99         | false  | false | "Simple load"
        2         | 0x20       | 0          | 0          | true   | false | "Load zero"
        3         | 0x20       | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDX (Absolute[Y]): Load [#addressHi | #addressLo] with #firstValue")
    def testLDX_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IY, addressHi, addressLo,
                         OP_LDX_ABS_IY, addressHi, addressLo]
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | addressHi | addressLo  | firstValue | expectedX  | Z      | N     | Expected
        0     | 1         | 0x20       | 99         | 99         | false  | false | "Simple load"
        1     | 2         | 0x20       | 0          | 0          | true   | false | "Load zero"
        2     | 3         | 0x20       | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDX (Zero Page): Load [#addressHi | #addressLo] with #firstValue")
    def testLX_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_STA_Z, address, OP_LDX_Z, address]
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        address | firstValue | expectedX  | Z      | N     | Expected
        1       | 99         | 99         | false  | false | "Simple load"
        2       | 0          | 0          | true   | false | "Load zero"
        3       | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDX (Zero Page[Y]): Load [#addressHi | #addressLo] with #firstValue")
    def testLX_Z_IY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index, OP_LDA_I, firstValue, OP_STA_Z, address, OP_LDX_Z_IY, address]
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        address | index | firstValue | expectedX  | Z      | N     | Expected
        1       | 0     | 99         | 99         | false  | false | "Simple load"
        2       | 0     | 0          | 0          | true   | false | "Load zero"
        3       | 0     | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDY (Immediate): Load #firstValue")
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

    @Unroll("LDY (Zero Page): Load [#addressHi | #addressLo] with #firstValue")
    def testLDY_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_STA_Z, address, OP_LDY_Z, address]
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_Y_INDEX) == expectedY
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        address | firstValue | expectedY  | Z      | N     | Expected
        1       | 99         | 99         | false  | false | "Simple load"
        2       | 0          | 0          | true   | false | "Load zero"
        3       | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDY (Zero Page[X]): Load Y with #firstValue from #address[#index#]")
    def testLDY_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_I, firstValue, OP_STA_Z_IX, address, OP_LDY_Z_IX, address]
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_Y_INDEX) == expectedY
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        address |index| firstValue | expectedY  | Z      | N     | Expected
        0x0F    | 3   | 99         | 99         | false  | false | "Simple load"
        0x0F    | 7   | 0          | 0          | true   | false | "Load zero"
        0x0F    | 4   | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDY (Absolute): Load Y with #firstValue at [#addressHi | #addressLo]")
    def testLDY_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_ABS, addressHi, addressLo]
        memory.setMemory(0, program)

        and:
        memory.setByteAt(addressHi << 8 | addressLo, firstValue)

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
        addressHi | addressLo | firstValue | expectedY  | Z      | N     | Expected
        1         | 0x23      | 99         | 99         | false  | false | "Simple load"
        2         | 0x22      | 0          | 0          | true   | false | "Load zero"
        3         | 0x21      | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("LDY (Absolute[X]): Load Y with #firstValue at [#addressHi | #addressLo][#index]")
    def testLDY_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDY_ABS_IX, addressHi, addressLo]
        memory.setMemory(0, program)

        and:
        memory.setByteAt((addressHi << 8 | addressLo)+index, firstValue)

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
        addressHi | addressLo | index | firstValue | expectedY  | Z      | N     | Expected
        1         | 0x23      | 0     | 99         | 99         | false  | false | "Simple load"
        2         | 0x22      | 1     | 0          | 0          | true   | false | "Load zero"
        3         | 0x21      | 10    | 0b11111111 | 0b11111111 | false  | true  | "Load negative value"
    }

    @Unroll("ADC (Immediate) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
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

    @Unroll("ADC (Zero Page[X]) #Expected: #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADC_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_LDX_I, index, OP_ADC_Z_IX, indexPoint]
        memory.setByteAt(memLoc, secondValue)
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        memLoc | firstValue  | secondValue | indexPoint  | index | expectedAccumulator | Z      | N     | C     | O     | Expected
        0x51   | 0x0         | 0x0         | 0x50        | 1     | 0x0                 | true   | false | false | false | "With zero result"
        0x53   | 0x50        | 0xD0        | 0x50        | 3     | 0x20                | false  | false | true  | false | "With positive, carried result"
        0x97   | 0x50        | 0x50        | 0x90        | 7     | 0xA0                | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("ADC (Zero Page) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADCFromZeroPage(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ADC_Z, 0x30]
        memory.setMemory(0, program);
        memory.setByteAt(0x30, secondValue)

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

    @Unroll("ADC (Absolute) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADCAbsolute(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ADC_ABS, 0x1, 0x2C]
        memory.setMemory(0, program);
        memory.setByteAt(300, secondValue)

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

    @Unroll("ADC (Absolute[X]) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_I, firstValue, OP_ADC_ABS_IX, 0x1, 0x2C]
        memory.setMemory(0, program);
        memory.setByteAt(300 + index, secondValue)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | index | expectedAccumulator | Z      | N     | C     | O     | Expected
        0x0        | 0x0         | 0     | 0x0                 | true   | false | false | false | "With zero result"
        0x50       | 0xD0        | 1     | 0x20                | false  | false | true  | false | "With positive, carried result"
        0x50       | 0x50        | 2     | 0xA0                | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("ADC (Absolute[Y]) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADC_ABS_IY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index, OP_LDA_I, firstValue, OP_ADC_ABS_IY, 0x1, 0x2C]
        memory.setMemory(0, program);
        memory.setByteAt(300 + index, secondValue)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | index | expectedAccumulator | Z      | N     | C     | O     | Expected
        0x0        | 0x0         | 0     | 0x0                 | true   | false | false | false | "With zero result"
        0x50       | 0xD0        | 1     | 0x20                | false  | false | true  | false | "With positive, carried result"
        0x50       | 0x50        | 2     | 0xA0                | false  | true  | false | true  | "With negative overflow"
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
        lowFirstByte | lowSecondByte | highFirstByte | highSecondByte | expectedAccumulator | storedValue | Z     | N     | C     | O     | Expected
        0            | 0             | 0             | 0              | 0                   | 0           | true  | false | false | false | "With zero result"
        0x50         | 0xD0          | 0             | 0              | 1                   | 0x20        | false | false | false | false | "With simple carry to high byte"
        0x50         | 0xD3          | 0             | 1              | 2                   | 0x23        | false | false | false | false | "With carry to high byte and changed high"
        0            | 0             | 0x50          | 0x50           | 0xA0                | 0           | false | true  | false | true  | "With negative overflow"
        0            | 0             | 0x50          | 0xD0           | 0x20                | 0           | false | false | true  | false | "With carried result"
    }

    @Unroll("AND (Immediate) #Expected:  #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
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

    @Unroll("AND (Zero Page) #Expected: #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    def testAND_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, secondValue,
                         OP_AND_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
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

    @Unroll("AND (Zero Page[X]) #Expected: #firstValue & #secondValue = #expectedAcc")
    def testAND_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, secondValue,
                         OP_AND_Z_IX, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | index | secondValue | expectedAcc | Z      | N     | Expected
        0b00000001 | 0     | 0b00000001  | 0b00000001  | false  | false | "Unchanged accumulator"
        0b00000001 | 1     | 0b00000010  | 0b00000000  | true   | false | "No matching bits"
        0b00000011 | 2     | 0b00000010  | 0b00000010  | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 3     | 0b00011010  | 0b00001010  | false  | false | "Multiple matched/unmatched bits"
    }

    @Unroll("AND (Absolute) #Expected:  #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    def testAND_A(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x20, 0x01,
                         OP_LDA_I, secondValue,
                         OP_AND_ABS, 0x20, 0x01];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
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

    @Unroll("AND (Absolute[X]) #Expected: #firstValue & #secondValue = #expectedAcc")
    def testAND_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, locationHi, locationLo,
                         OP_LDA_I, secondValue,
                         OP_AND_ABS_IX, locationHi, locationLo];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        locationHi | locationLo | firstValue | index | secondValue | expectedAcc | Z      | N     | Expected
        0x1        | 0x10       | 0b00000001 | 0     | 0b00000001  | 0b00000001  | false  | false | "Unchanged accumulator"
        0x2        | 0x20       | 0b00000001 | 1     | 0b00000010  | 0b00000000  | true   | false | "No matching bits"
        0x3        | 0x30       | 0b00000011 | 2     | 0b00000010  | 0b00000010  | false  | false | "1 matched bit, 1 unmatched"
        0x4        | 0x40       | 0b00101010 | 3     | 0b00011010  | 0b00001010  | false  | false | "Multiple matched/unmatched bits"
    }

    @Unroll("AND (Absolute[Y]) #Expected: #firstValue & #secondValue = #expectedAcc")
    def testAND_ABS_IY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IY, locationHi, locationLo,
                         OP_LDA_I, secondValue,
                         OP_AND_ABS_IY, locationHi, locationLo];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        locationHi | locationLo | firstValue | index | secondValue | expectedAcc | Z      | N     | Expected
        0x1        | 0x10       | 0b00000001 | 0     | 0b00000001  | 0b00000001  | false  | false | "Unchanged accumulator"
        0x2        | 0x20       | 0b00000001 | 1     | 0b00000010  | 0b00000000  | true   | false | "No matching bits"
        0x3        | 0x30       | 0b00000011 | 2     | 0b00000010  | 0b00000010  | false  | false | "1 matched bit, 1 unmatched"
        0x4        | 0x40       | 0b00101010 | 3     | 0b00011010  | 0b00001010  | false  | false | "Multiple matched/unmatched bits"
    }

    @Unroll("ORA (Immediate) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
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

    @Unroll("ORA (Zero Page) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    def testOR_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, secondValue,
                         OP_ORA_Z, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
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

    @Unroll("ORA (Zero Page[X]) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    def testOR_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, secondValue,
                         OP_ORA_Z_IX, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 1     | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 2     | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 3     | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 4     | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }

    @Unroll("ORA (Absolute) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    def testOR_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x20, 0x05,
                         OP_LDA_I, secondValue,
                         OP_ORA_ABS, 0x20, 0x05]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
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

    @Unroll("ORA (Absolute[X]) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    def testOR_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x20, 0x05,
                         OP_LDA_I, secondValue,
                         OP_ORA_ABS_IX, 0x20, 0x05]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 1     | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 2     | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 3     | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 4     | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }

    @Unroll("EOR (Immediate) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
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

    @Unroll("EOR (Zero Page) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    def testEOR_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, firstValue,
                         OP_EOR_Z, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
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

    @Unroll("EOR (Zero Page[X]) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    def testEOR_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, firstValue,
                         OP_EOR_Z_IX, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0     | 0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        1     | 0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        2     | 0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }

    @Unroll("EOR (Absolute) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    def testEOR_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x20, 0x04,
                         OP_LDA_I, firstValue,
                         OP_EOR_ABS, 0x20, 0x04]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
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

    @Unroll("EOR (Absolute[X]) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    def testEOR_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IX, 0x20, 0x04,
                         OP_LDA_I, firstValue,
                         OP_EOR_ABS_IX, 0x20, 0x04]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0     | 0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        1     | 0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        2     | 0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }

    @Unroll("SBC (Immediate) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
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

    @Unroll("SBC (Zero Page) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def testSBC_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_Z, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
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

    @Unroll("SBC (Zero Page[X]) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def testSBC_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_Z_IX, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(6)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | index | secondValue | expectedAccumulator | Z      | N     | O     | C     | Expected
        0x5        | 0     | 0x3         | 0x2                 | false  | false | false | false | "Basic subtraction"
        0x5        | 1     | 0x5         | 0x0                 | true   | false | false | false | "With zero result"
        0x5        | 2     | 0x6         | 0xFF                | false  | true  | false | false | "with negative result"
    }

    @Unroll("SBC (Absolute) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def testSBC_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x02, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_ABS, 0x02, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(5)
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

    @Unroll("SBC (Absolute[X]) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def testSBC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IX, 0x02, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_ABS_IX, 0x02, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(6)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        index | firstValue | secondValue | expectedAccumulator | Z      | N     | O     | C     | Expected
        0     | 0x5        | 0x3         | 0x2                 | false  | false | false | false | "Basic subtraction"
        1     | 0x5        | 0x5         | 0x0                 | true   | false | false | false | "With zero result"
        2     | 0x5        | 0x6         | 0xFF                | false  | true  | false | false | "with negative result"
    }

    @Unroll("SBC (Absolute[Y]) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def testSBC_ABS_IY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IY, 0x02, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_ABS_IY, 0x02, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(6)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        index | firstValue | secondValue | expectedAccumulator | Z      | N     | O     | C     | Expected
        0     | 0x5        | 0x3         | 0x2                 | false  | false | false | false | "Basic subtraction"
        1     | 0x5        | 0x5         | 0x0                 | true   | false | false | false | "With zero result"
        2     | 0x5        | 0x6         | 0xFF                | false  | true  | false | false | "with negative result"
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

    @Unroll("INC (Zero Page) #Expected: on #firstValue = #expectedMem")
    def testINC_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_STA_Z, 0x20, OP_INC_Z, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(0x20) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedMem | Z      | N     | Expected
        0          | 1           | false  | false | "Simple increment"
        0xFE       | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 0x0         | true   | false | "Increment to zero"
    }

    @Unroll("INC (Zero Page[X]) #Expected: on #firstValue = #expectedMem")
    def testINC_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_I, firstValue, OP_STA_Z_IX, 0x20, OP_INC_Z_IX, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(0x20 + index) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | index | expectedMem | Z      | N     | Expected
        0          | 0     | 1           | false  | false | "Simple increment"
        0xFE       | 0     | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 0     | 0x0         | true   | false | "Increment to zero"
    }

    @Unroll("INC (Absolute) #Expected: on #firstValue = #expectedMem")
    def testINC_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_STA_ABS, 0x01, 0x20, OP_INC_ABS, 0x01, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(0x0120) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedMem | Z      | N     | Expected
        0          | 1           | false  | false | "Simple increment"
        0xFE       | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 0x0         | true   | false | "Increment to zero"
    }

    @Unroll("INC (Absolute, X) #Expected: at 0x120[#index] on #firstValue = #expectedMem")
    def testINC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_I, firstValue, OP_STA_ABS_IX, 0x01, 0x20, OP_INC_ABS_IX, 0x01, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(0x0120 + index) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | index | expectedMem | Z      | N     | Expected
        0          | 1     | 1           | false  | false | "Simple increment"
        0xFE       | 2     | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 3     | 0x0         | true   | false | "Increment to zero"
    }

    @Unroll("DEC (Zero Page) #Expected: on #firstValue = #expectedMem")
    def testDEC_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_STA_Z, 0x20, OP_DEC_Z, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(0x20) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedMem | Z      | N     | Expected
        9          | 8           | false  | false | "Simple decrement"
        0xFF       | 0xFE        | false  | true  | "Decrement to negative value"
        0b00000001 | 0x0         | true   | false | "Decrement to zero"
    }

    @Unroll("DEC (Zero Page[X]) #Expected: on #firstValue at #loc[#index] = #expectedMem")
    def testDEC_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_I, firstValue, OP_STA_Z_IX, loc, OP_DEC_Z_IX, loc]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(loc + index) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | loc  | index | expectedMem | Z      | N     | Expected
        9          | 0x20 | 0     | 8           | false  | false | "Simple decrement"
        0xFF       | 0x20 | 1     | 0xFE        | false  | true  | "Decrement to negative value"
        0b00000001 | 0x20 | 2     | 0x0         | true   | false | "Decrement to zero"
    }

    @Unroll("DEC (Absolute) #Expected: on #firstValue = #expectedMem")
    def testDEC_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_DEC_ABS, 0x01, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(0x0120) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | expectedMem | Z      | N     | Expected
        9          | 8           | false  | false | "Simple decrement"
        0xFF       | 0xFE        | false  | true  | "Decrement to negative value"
        0b00000001 | 0x0         | true   | false | "Decrement to zero"
    }

    @Unroll("DEC (Absolute[X]) #Expected: on #firstValue = #expectedMem")
    def testDEC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x01, 0x20,
                         OP_DEC_ABS_IX, 0x01, 0x20]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(4)
        Registers registers = processor.getRegisters()

        then:
        memory.getByte(0x0120 + index) == expectedMem
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | firstValue | expectedMem | Z      | N     | Expected
        0     | 9          | 8           | false  | false | "Simple decrement"
        1     | 0xFF       | 0xFE        | false  | true  | "Decrement to negative value"
        2     | 0b00000001 | 0x0         | true   | false | "Decrement to zero"
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
        stackItem == memory.getByte(0x01FF)

        where:
        firstValue | expectedSP | stackItem  | expectedAccumulator | Z     | N     | Expected
        0x99       | 0x0FF      | 0x99       | 0x99                | false | false | "Basic stack push"
        0x0        | 0x0FF      | 0x0        | 0x0                 | false | true  | "Zero stack push"
        0b10001111 | 0x0FF      | 0b10001111 | 0b10001111          | true  | true  | "Negative stack push"
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
                         OP_ASL_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

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

    @Unroll("ASL (Absolute) #Expected: #firstValue becomes #expectedMem")
    def testASL_A(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_ASL_ABS, 0x01, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        memory.getByte(0x120) == expectedMem
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

    @Unroll("ASL (Zero Page at X) #Expected: #firstValue (@ 0x20[#index]) becomes #expectedMem")
    def testASL_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_LDX_I, index,
                         OP_STA_Z_IX, 0x20,
                         OP_ASL_Z_IX, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        memory.getByte(0x20 + index) == expectedMem
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | expectedMem | index | Z     | N     | C     | Expected
        0b00010101 | 0b00101010  | 0     | false | false | false | "Basic shift"
        0b00000000 | 0b00000000  | 1     | true  | false | false | "Zero shift"
        0b01000000 | 0b10000000  | 2     | false | true  | false | "Negative shift"
        0b10000001 | 0b00000010  | 3     | false | false | true  | "Carried shift"
        0b10000000 | 0b00000000  | 4     | true  | false | true  | "Carried, zero shift"
        0b11000000 | 0b10000000  | 5     | false | true  | true  | "Carried, negative shift"
    }

    @Unroll("ASL (Absolute[X]) #Expected: #firstValue (@ 0x20[#index]) becomes #expectedMem")
    def testASL_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_LDX_I, index,
                         OP_STA_ABS_IX, 0x01, 0x20,
                         OP_ASL_ABS_IX, 0x01, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        memory.getByte(0x120 + index) == expectedMem
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | expectedMem | index | Z     | N     | C     | Expected
        0b00010101 | 0b00101010  | 0     | false | false | false | "Basic shift"
        0b00000000 | 0b00000000  | 1     | true  | false | false | "Zero shift"
        0b01000000 | 0b10000000  | 2     | false | true  | false | "Negative shift"
        0b10000001 | 0b00000010  | 3     | false | false | true  | "Carried shift"
        0b10000000 | 0b00000000  | 4     | true  | false | true  | "Carried, zero shift"
        0b11000000 | 0b10000000  | 5     | false | true  | true  | "Carried, negative shift"
    }

    @Unroll("LSR (Accumulator) #Expected: #firstValue becomes #expectedAccumulator")
    def testLSR(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_LSR_A];
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
                         OP_LSR_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

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

    @Unroll("LSR (Zero Page[X]) #Expected: #firstValue becomes #expectedMem")
    def testLSR_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LSR_Z_IX, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        memory.getByte(0x20 + index) == expectedMem
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | index | expectedMem | Z     | N     | C     | Expected
        0b01000000 | 0     | 0b00100000  | false | false | false | "Basic shift"
        0b00000000 | 1     | 0b00000000  | true  | false | false | "Shift to zero"
        0b00000011 | 2     | 0b00000001  | false | false | true  | "Shift with carry"
        0b00000001 | 3     | 0b00000000  | true  | false | true  | "Shift to zero with carry"
    }

    @Unroll("LSR (Absolute) #Expected: #firstValue becomes #expectedMem")
    def testLSR_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x02, 0x20,
                         OP_LSR_ABS, 0x02, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        memory.getByte(0x220) == expectedMem
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

    @Unroll("LSR (Absolute[X]) #Expected: #firstValue becomes #expectedMem")
    def testLSR_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x02, 0x20,
                         OP_LSR_ABS_IX, 0x02, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        memory.getByte(0x220 + index) == expectedMem
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | index | expectedMem | Z     | N     | C     | Expected
        0b01000000 | 0     | 0b00100000  | false | false | false | "Basic shift"
        0b00000000 | 1     | 0b00000000  | true  | false | false | "Shift to zero"
        0b00000011 | 2     | 0b00000001  | false | false | true  | "Shift with carry"
        0b00000001 | 3     | 0b00000000  | true  | false | true  | "Shift to zero with carry"
    }

    @Unroll("JMP #expected: [#jmpLocationHi | #jmpLocationLow] -> #expectedPC")
    def testJMP(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, OP_JMP_ABS, jmpLocationHi, jmpLocationLow, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        jmpLocationHi | jmpLocationLow | instructions | expectedPC | expected
        0b00000000    | 0b00000001     | 4            | 1          | "Standard jump back"
        0b00000000    | 0b00000000     | 5            | 1          | "Standard jump back then step"
        0b00000000    | 0b00000111     | 4            | 7          | "Standard jump forward"
        0b00000000    | 0b00000111     | 5            | 8          | "Standard jump forward then step"
        0b00000001    | 0b00000000     | 4            | 256        | "High byte jump"
        0b00000001    | 0b00000001     | 4            | 257        | "Double byte jump"
    }

    @Unroll("BCC #expected: ending up at mem[#expectedPC] after #instructions steps")
    def testBCC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, preInstr, OP_BCC, jmpSteps, OP_NOP, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        preInstr | jmpSteps   | instructions | expectedPC | expected
        OP_SEC   | 4          | 5            | 0x6        | "No jump"
        OP_CLC   | 4          | 5            | 0xA        | "Basic forward jump"
        OP_CLC   | 1          | 6            | 0x8        | "Basic forward jump and step"
        OP_CLC   | 0b11111011 | 5            | 0x2        | "Basic backward jump"
        OP_CLC   | 0b11111011 | 6            | 0x3        | "Basic backward jump and step"
    }

    @Unroll("BCS #expected: ending up at mem[#expectedPC] after #instructions steps")
    def testBCS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, preInstr, OP_BCS, jmpSteps, OP_NOP, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        preInstr | jmpSteps   | instructions | expectedPC | expected
        OP_CLC   | 4          | 5            | 0x6        | "No jump"
        OP_SEC   | 4          | 5            | 0xA        | "Basic forward jump"
        OP_SEC   | 1          | 6            | 0x8        | "Basic forward jump and step"
        OP_SEC   | 0b11111011 | 5            | 0x2        | "Basic backward jump"
        OP_SEC   | 0b11111011 | 6            | 0x3        | "Basic backward jump and step"
    }

    @Unroll("ROL (Accumulator) #expected: #firstValue -> #expectedAccumulator")
    def testROL_A(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [preInstr, OP_LDA_I, firstValue, OP_ROL_A];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        preInstr | firstValue | expectedAccumulator | Z     | N     | C     | expected
        OP_CLC   | 0b00000001 | 0b00000010          | false | false | false | "Standard rotate left"
        OP_CLC   | 0b00000000 | 0b00000000          | true  | false | false | "Rotate to zero"
        OP_CLC   | 0b01000000 | 0b10000000          | false | true  | false | "Rotate to negative"
        OP_CLC   | 0b10000001 | 0b00000010          | false | false | true  | "Rotate to carry out"
        OP_SEC   | 0b00000001 | 0b00000011          | false | false | false | "Rotate with carry in, no carry out"
        OP_SEC   | 0b10000000 | 0b00000001          | false | false | true  | "Carry in then carry out"
        OP_SEC   | 0b01000000 | 0b10000001          | false | true  | false | "Carry in to negative"
    }

    @Unroll("ROL (Zero Page) #Expected: #firstValue -> #expectedMem")
    def testROL_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [firstInstr, OP_LDA_I, firstValue, OP_STA_Z, 0x20, OP_ROL_Z, 0x20];
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
        firstInstr |firstValue  | expectedMem | Z     | N     | C     | Expected
        OP_CLC     | 0b00000001 | 0b00000010  | false | false | false | "Basic rotate left"
        OP_CLC     | 0b01000000 | 0b10000000  | false | true  | false | "Rotate to negative"
        OP_CLC     | 0b00000000 | 0b00000000  | true  | false | false | "Rotate to zero without carry"
        OP_CLC     | 0b10000000 | 0b00000000  | true  | false | true  | "Rotate to zero with carry"
        OP_SEC     | 0b00000000 | 0b00000001  | false | false | false | "Rotate from zero to carry in"
    }

    @Unroll("ROL (Zero Page[X]) #Expected: #firstValue -> #expectedMem")
    def testROL_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         firstInstr,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_ROL_Z_IX, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        expectedMem == memory.getByte(0x20 + index)
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstInstr | index | firstValue | expectedMem | Z     | N     | C     | Expected
        OP_CLC     | 0     | 0b00000001 | 0b00000010  | false | false | false | "Basic rotate left"
        OP_CLC     | 1     | 0b01000000 | 0b10000000  | false | true  | false | "Rotate to negative"
        OP_CLC     | 2     | 0b00000000 | 0b00000000  | true  | false | false | "Rotate to zero without carry"
        OP_CLC     | 3     | 0b10000000 | 0b00000000  | true  | false | true  | "Rotate to zero with carry"
        OP_SEC     | 4     | 0b00000000 | 0b00000001  | false | false | false | "Rotate from zero to carry in"
    }

    @Unroll("ROL (Absolute) #Expected: #firstValue -> #expectedMem")
    def testROL_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [firstInstr,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x20, 0x07,
                         OP_ROL_ABS, 0x20, 0x07];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        expectedMem == memory.getByte( 0x2007 )
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstInstr |firstValue  | expectedMem | Z     | N     | C     | Expected
        OP_CLC     | 0b00000001 | 0b00000010  | false | false | false | "Basic rotate left"
        OP_CLC     | 0b01000000 | 0b10000000  | false | true  | false | "Rotate to negative"
        OP_CLC     | 0b00000000 | 0b00000000  | true  | false | false | "Rotate to zero without carry"
        OP_CLC     | 0b10000000 | 0b00000000  | true  | false | true  | "Rotate to zero with carry"
        OP_SEC     | 0b00000000 | 0b00000001  | false | false | false | "Rotate from zero to carry in"
    }

    @Unroll("ROL (Absolute[X]) #Expected: #firstValue -> #expectedMem")
    def testROL_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         firstInstr,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x20, 0x07,
                         OP_ROL_ABS_IX, 0x20, 0x07];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        expectedMem == memory.getByte( 0x2007 + index)
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstInstr | index | firstValue | expectedMem | Z     | N     | C     | Expected
        OP_CLC     | 0     | 0b00000001 | 0b00000010  | false | false | false | "Basic rotate left"
        OP_CLC     | 1     | 0b01000000 | 0b10000000  | false | true  | false | "Rotate to negative"
        OP_CLC     | 2     | 0b00000000 | 0b00000000  | true  | false | false | "Rotate to zero without carry"
        OP_CLC     | 3     | 0b10000000 | 0b00000000  | true  | false | true  | "Rotate to zero with carry"
        OP_SEC     | 4     | 0b00000000 | 0b00000001  | false | false | false | "Rotate from zero to carry in"
    }

    @Unroll("ROR (Accumulator) #expected: #firstValue -> #expectedAccumulator")
    def testROR_A(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [preInstr, OP_LDA_I, firstValue, OP_ROR_A];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        preInstr | firstValue | expectedAccumulator | Z     | N     | C     | expected
        OP_CLC   | 0b00000010 | 0b00000001          | false | false | false | "Standard rotate right"
        OP_CLC   | 0b00000001 | 0b00000000          | true  | false | true  | "Rotate to zero"
        OP_SEC   | 0b00000000 | 0b10000000          | false | true  | false | "Rotate to (carry in) negative"
        OP_CLC   | 0b00000011 | 0b00000001          | false | false | true  | "Rotate to carry out"
        OP_SEC   | 0b00000010 | 0b10000001          | false | true  | false | "Rotate with carry in, no carry out"
        OP_SEC   | 0b00000001 | 0b10000000          | false | true  | true  | "Carry in then carry out"
        OP_SEC   | 0b01111110 | 0b10111111          | false | true  | false | "Carry in to negative"
    }

    @Unroll("BNE #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    def testBNE(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, OP_LDA_I, accumulatorValue, OP_BNE, jumpSteps, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        1                | 4          | 5            | 0xB        | "Standard forward jump"
        1                | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        0                | 0b11111011 | 5            | 0x7        | "No jump"
    }

    @Unroll("BEQ #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    def testBEQ(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, OP_LDA_I, accumulatorValue, OP_BEQ, jumpSteps, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        0                | 4          | 5            | 0xB        | "Standard forward jump"
        0                | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        1                | 0b11111011 | 5            | 0x7        | "No jump"
    }

    @Unroll("BMI #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    def testBMI(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, OP_LDA_I, accumulatorValue, OP_BMI, jumpSteps, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        0b11111110       | 4          | 5            | 0xB        | "Standard forward jump"
        0b11111100       | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        0b00000001       | 0b11111011 | 5            | 0x7        | "No jump"
    }

    @Unroll("BPL #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    def testBPL(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, OP_LDA_I, accumulatorValue, OP_BPL, jumpSteps, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        0b00000001       | 4          | 5            | 0xB        | "Standard forward jump"
        0b01001001       | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        0b11111110       | 0b11111011 | 5            | 0x7        | "No jump"
    }

    @Unroll("BVC #expected: #accumulatorValue + #addedValue, checking overflow we end up at mem[#expectedPC] after #instructions steps")
    def testBVC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, OP_LDA_I, accumulatorValue, OP_ADC_I, addedValue, OP_BVC, jumpSteps, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        accumulatorValue | addedValue | jumpSteps  | instructions | expectedPC | expected
        0                | 0          | 4          | 6            | 0xD        | "Standard forward jump"
        0                | 0          | 0b11111011 | 6            | 0x5        | "Standard backward jump"
        0x50             | 0x50       | 0b11111011 | 6            | 0x9        | "No jump"
    }

    @Unroll("BVC #expected: #accumulatorValue + #addedValue, checking overflow we end up at mem[#expectedPC] after #instructions steps")
    def testBVS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_NOP, OP_NOP, OP_NOP, OP_LDA_I, accumulatorValue, OP_ADC_I, addedValue, OP_BVS, jumpSteps, OP_NOP, OP_NOP, OP_NOP];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(instructions)

        then:
        registers.getPC() == expectedPC

        where:
        accumulatorValue | addedValue | jumpSteps  | instructions | expectedPC | expected
        0x50             | 0x50       | 4          | 6            | 0xD        | "Standard forward jump"
        0x50             | 0x50       | 0b11111011 | 6            | 0x5        | "Standard backward jump"
        0                | 0          | 0b11111011 | 6            | 0x9        | "No jump"
    }

    @Unroll("TAX #expected: #loadedValue to X")
    def testTAX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, loadedValue, OP_TAX];
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
        registers.getRegister(Registers.REG_X_INDEX) == X
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadedValue | expectedAccumulator | X          | N     | Z     | expected
        0x10        | 0x10                | 0x10       | false | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true  | false | "Negative transferred"
    }

    @Unroll("TAY #expected: #loadedValue to Y")
    def testTAY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, loadedValue, OP_TAY];
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
        registers.getRegister(Registers.REG_Y_INDEX) == Y
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadedValue | expectedAccumulator | Y          | N      | Z     | expected
        0x10        | 0x10                | 0x10       | false  | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false  | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true   | false | "Negative transferred"
    }

    @Unroll("TYA #expected: #loadedValue to Accumulator")
    def testTYA(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, loadedValue, OP_TYA];
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
        registers.getRegister(Registers.REG_Y_INDEX) == Y
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadedValue | expectedAccumulator | Y          | N      | Z     | expected
        0x10        | 0x10                | 0x10       | false  | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false  | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true   | false | "Negative transferred"
    }

    @Unroll("TXA #expected: #loadedValue to Accumulator")
    def testTXA(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, loadedValue, OP_TXA];
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
        registers.getRegister(Registers.REG_X_INDEX) == X
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadedValue | expectedAccumulator | X          | N      | Z     | expected
        0x10        | 0x10                | 0x10       | false  | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false  | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true   | false | "Negative transferred"
    }

    @Unroll("TSX #expected: load #SPValue in SP into X")
    def testTSX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_TSX];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        registers.setRegister(Registers.REG_SP, SPValue)
        processor.step()

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_X_INDEX) == X
        registers.getRegister(Registers.REG_SP) == expectedSP
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        SPValue | expectedSP | X    | Z     | N     | expected
        0xFF    | 0xFF       | 0xFF | false | true  | "Empty stack"
        0x0F    | 0x0F       | 0x0F | false | false | "No flags set"
        0x0     | 0x0        | 0x0  | true  | false | "Zero stack"
    }

    @Unroll("BIT (Zero Page) #expected: #firstValue and #secondValue")
    def testBIT(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_STA_Z, memLoc, OP_LDA_I, secondValue, OP_BIT_Z, memLoc];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        O == registers.statusFlags[Registers.V]

        where:
        firstValue | secondValue | memLoc | O     | Z     | N     | expected
        0x01       | 0x01        | 0x20   | false | true  | false | "Equal values"
        0x01       | 0x12        | 0x20   | false | false | false | "Unequal values"
        0b10000000 | 0b00000000  | 0x20   | false | false | true  | "Negative flag on"
        0b01000000 | 0b00000000  | 0x20   | true  | false | false | "Overflow flag on"
        0b11000000 | 0b00000000  | 0x20   | true  | false | true  | "Negative & Overflow flag on"
    }

    @Unroll("BIT (Absolute) #expected: #firstValue and #secondValue")
    def testBIT_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_STA_ABS, memLocHi, memLocLo,
                         OP_LDA_I, secondValue,
                         OP_BIT_ABS, memLocHi, memLocLo];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        O == registers.statusFlags[Registers.V]

        where:
        firstValue | secondValue | memLocHi | memLocLo | O     | Z     | N     | expected
        0x01       | 0x01        | 1        | 0x20     | false | true  | false | "Equal values"
        0x01       | 0x12        | 2        | 0x20     | false | false | false | "Unequal values"
        0b10000000 | 0b00000000  | 3        | 0x20     | false | false | true  | "Negative flag on"
        0b01000000 | 0b00000000  | 4        | 0x20     | true  | false | false | "Overflow flag on"
        0b11000000 | 0b00000000  | 5        | 0x20     | true  | false | true  | "Negative & Overflow flag on"
    }

    @Unroll("STA (Zero Page[X]) #expected: Store #value at #location[#index]")
    def testOP_STA_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, value, OP_LDX_I, index, OP_STA_Z_IX, location];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        memory.getByte(location+index) == value

        where:
        location | index | value | expected
        0x20     | 0     | 0x0F  | "Store with 0 index"
        0x20     | 1     | 0x0E  | "Store at index 1"
        0x20     | 2     | 0x0D  | "Store at index 2"
        0x20     | 3     | 0x0C  | "Store at index 3"
    }

    @Unroll("STA (Absolute[X]) #expected: Store #value at [#locationHi|#locationLo@#index]")
    def testOP_STA_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, value,
                         OP_LDX_I, index,
                         OP_STA_ABS_IX, locationHi, locationLo];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        memory.getByte((locationHi << 8 | locationLo) + index) == value

        where:
        locationHi | locationLo | index | value | expected
        0x20       |  0         | 0     | 0x0F  | "Store with 0 index"
        0x20       |  30        | 1     | 0x0E  | "Store at index 1"
        0x20       |  9         | 2     | 0x0D  | "Store at index 2"
        0x20       |  1         | 3     | 0x0C  | "Store at index 3"
    }

    @Unroll("STA (Absolute[Y]) #expected: Store #value at [#locationHi|#locationLo@#index]")
    def testOP_STA_ABS_IY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, value,
                         OP_LDY_I, index,
                         OP_STA_ABS_IY, locationHi, locationLo];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        memory.getByte((locationHi << 8 | locationLo) + index) == value

        where:
        locationHi | locationLo | index | value | expected
        0x20       |  0         | 0     | 0x0F  | "Store with 0 index"
        0x20       |  30        | 1     | 0x0E  | "Store at index 1"
        0x20       |  9         | 2     | 0x0D  | "Store at index 2"
        0x20       |  1         | 3     | 0x0C  | "Store at index 3"
    }

    @Unroll("STY (Zero Page[X] #expected: Store #firstValue at #memLocation[#index]")
    def testOP_STY_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDY_I, firstValue, OP_STY_Z_IX, memLocation];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        memory.getByte(memLocation + index) == expectedValue

        where:
        firstValue | index | memLocation | expectedValue | expected
        0x0F       | 0     | 0x20        | 0x0F          | "Standard copy to memory"
        0x0F       | 1     | 0x20        | 0x0F          | "Copy to memory with index"

    }

    @Unroll("CMP (Immediate) #Expected: #firstValue == #secondValue")
    def testOP_CMP_I(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue,
                         OP_CMP_I, secondValue];
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
        firstValue | secondValue | expectedAccumulator | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10                | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 0x11                | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 0x10                | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 0xFF                | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Zero Page) #Expected: #firstValue == #secondValue")
    def testOP_CMP_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedAccumulator | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10                | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 0x11                | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 0x10                | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 0xFF                | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Zero Page[X]) #Expected: #firstValue == #secondValue")
    def testOP_CMP_Z_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_Z_IX, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | index | expectedAccumulator | Z     | N     | C     | Expected
        0x10       | 0x10        | 0     | 0x10                | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 1     | 0x11                | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 2     | 0x10                | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 3     | 0xFF                | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Absolute) #Expected: #firstValue == #secondValue")
    def testOP_CMP_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_ABS, 0x01, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedAccumulator | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10                | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 0x11                | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 0x10                | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 0xFF                | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Absolute[X]) #Expected: #firstValue == #secondValue")
    def testOP_CMP_ABS_IX(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IX, 0x01, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_ABS_IX, 0x01, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | index | expectedAccumulator | Z     | N     | C     | Expected
        0x10       | 0x10        | 0     | 0x10                | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 1     | 0x11                | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 2     | 0x10                | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 3     | 0xFF                | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Absolute[Y]) #Expected: #firstValue == #secondValue")
    def testOP_CMP_ABS_IY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IY, 0x01, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_ABS_IY, 0x01, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | index | expectedAccumulator | Z     | N     | C     | Expected
        0x10       | 0x10        | 0     | 0x10                | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 1     | 0x11                | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 2     | 0x10                | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 3     | 0xFF                | false | true  | true  | "Negative result"
    }

    @Unroll("CPY (Immediate) #Expected: #firstValue == #secondValue")
    def testOP_CPY_I(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, firstValue,
                         OP_CPY_I, secondValue];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_Y_INDEX) == expectedY
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedY | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10      | true  | false | true  | "Values are equal"
        0x11       | 0x10        | 0x11      | false | false | true  | "First value is greater"
        0x10       | 0x11        | 0x10      | false | true  | false | "Second value is greater"
    }

    @Unroll("CPY (Zero Page) #Expected: #firstValue == #secondValue")
    def testOP_CPY_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDY_I, firstValue,
                         OP_CPY_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_Y_INDEX) == expectedY
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedY | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10      | true  | false | true  | "Values are equal"
        0x11       | 0x10        | 0x11      | false | false | true  | "First value is greater"
        0x10       | 0x11        | 0x10      | false | true  | false | "Second value is greater"
    }

    @Unroll("CPY (Absolute) #Expected: #firstValue == #secondValue")
    def testOP_CPY_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_LDY_I, firstValue,
                         OP_CPY_ABS, 0x01, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_Y_INDEX) == expectedY
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedY | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10      | true  | false | true  | "Values are equal"
        0x11       | 0x10        | 0x11      | false | false | true  | "First value is greater"
        0x10       | 0x11        | 0x10      | false | true  | false | "Second value is greater"
    }

    @Unroll("CPX (Zero Page) #Expected: #firstValue == #secondValue")
    def testOP_CPX_Z(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDX_I, firstValue,
                         OP_CPX_Z, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedX | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10      | true  | false | true  | "Values are equal"
        0x11       | 0x10        | 0x11      | false | false | true  | "First value is greater"
        0x10       | 0x11        | 0x10      | false | true  | false | "Second value is greater"
    }

    @Unroll("CPX (Absolute) #Expected: #firstValue == #secondValue")
    def testOP_CPX_ABS(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_LDX_I, firstValue,
                         OP_CPX_ABS, 0x01, 0x20];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedX | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10      | true  | false | true  | "Values are equal"
        0x11       | 0x10        | 0x11      | false | false | true  | "First value is greater"
        0x10       | 0x11        | 0x10      | false | true  | false | "Second value is greater"
    }

    @Unroll("CPX (Immediate) #Expected: #firstValue == #secondValue")
    def testOP_CPX_I(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, firstValue,
                         OP_CPX_I, secondValue];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_X_INDEX) == expectedX
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedX | Z     | N     | C     | Expected
        0x10       | 0x10        | 0x10      | true  | false | true  | "Values are equal"
        0x11       | 0x10        | 0x11      | false | false | true  | "First value is greater"
        0x10       | 0x11        | 0x10      | false | true  | false | "Second value is greater"
    }

    @Unroll("STX (Zero Page[X] #expected: #firstValue -> #location[#index]")
    def OP_STX_Z_IY(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index,
                         OP_LDX_I, firstValue,
                         OP_STX_Z_IY, location];
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(3)

        then:
        registers.getPC() == program.length
        memory.getByte(location + index) == firstValue

        where:
        location | index | firstValue | expected
        0x20     | 0     | 0xA1       | "Basic store"
        0x20     | 1     | 0xA3       | "Store at index"
        0x60     | 2     | 0xA6       | "Store at higher location at index"
        0x20     | 50    | 0xAA       | "Store at higher index"
    }

//    @Ignore
//    def exampleTest(){
//        when:
//        Memory memory = new SimpleMemory(65534);
//        int[] program = [];
//        memory.setMemory(0, program);
//
//        and:
//        CPU processor = new CPU(memory)
//        processor.reset()
//        Registers registers = processor.getRegisters()
//
//        and:
//        processor.step(1)
//
//        then:
//        registers.getPC() == program.length
//
//        where:
//        A | B
//        A | B
//    }
}
