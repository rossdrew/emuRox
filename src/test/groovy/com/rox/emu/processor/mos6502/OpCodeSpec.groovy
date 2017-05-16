package com.rox.emu.processor.mos6502

import com.rox.emu.mem.Memory

import com.rox.emu.mem.SimpleMemory
import com.rox.emu.processor.mos6502.util.Program
import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.processor.mos6502.op.OpCode.*;

class OpCodeSpec extends Specification {

    def testNewOpCode(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, loadValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDA (Immediate) #Expected: Load #loadValue == #expectedAccumulator")
    testImmediateLDA() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, loadValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDAFromZeroPage() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_Z, 30)
        memory.setByteAt(30, loadValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDAFromZeroPageIndexedByX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_Z_IX, 0x30)
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(0x30, values)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testAbsoluteLDA() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_ABS, 0x1, 0x2C)
        memory.setByteAt(300, loadValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDAIndexedByX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_ABS_IX, 1, 0x2C)
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(300, values)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDAIndexedByY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_ABS_IY, 1, 0x2C)
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(300, values)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDA (Indirect, X). #Expected: 0x30[#index] -> [#indAddressHi|#indAddressLo] = #expectedAccumulator")
    testLDA_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,    //Value at indirect address
                         OP_STA_ABS, indAddressHi, indAddressLo,
                         OP_LDX_I, index,
                         OP_LDA_I, indAddressHi,  //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, indAddressLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDA (Indirect, Y). #expected: 0x60 -> [#pointerHi|#pointerLo][#index] = #expectedAccumulator")
    testLDA_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,           //Index to use
                         OP_LDA_I, value,           //High order byte at pointer
                         OP_STA_ABS_IY, pointerHi, pointerLo,
                         OP_LDA_I, pointerHi,       //Pointer location
                         OP_STA_Z, 0x60,
                         OP_LDA_I, pointerLo,       //Pointer location
                         OP_STA_Z, 0x61,
                         OP_LDA_I, 0x0,             //Reset accumulator
                         OP_LDA_IND_IY, 0x60)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == value

        where:
        pointerHi | pointerLo | index | value | expected
        0x02      | 0x10      | 0     | 0x01  | "Simple, small value"
        0x03      | 0x10      | 0     | 0x01  | "Alternate high byte of pointer"
        0x04      | 0x11      | 0     | 0x01  | "Alternate low byte of pointer"
        0x05      | 0x12      | 1     | 0x01  | "Using index"
        0x06      | 0x13      | 2     | 0x0F  | "A different value"
    }

    @Unroll("LDX (Immediate): Load #firstValue")
    testLDX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, firstValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDX (Absolute): Load #firstValue from [#addressHi | #addressLo]")
    testLDX_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_ABS, addressHi, addressLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDX (Absolute[Y]): Load #firstValue from [#addressHi | #addressLo]")
    testLDX_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IY, addressHi, addressLo,
                         OP_LDX_ABS_IY, addressHi, addressLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDX (Zero Page): Load #firstValue from [#address]")
    testLX_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, address,
                         OP_LDX_Z, address)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDX (Zero Page[Y]): Load #firstValue from [#address[#index]")
    testLX_Z_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z, address,
                         OP_LDX_Z_IY, address)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, firstValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("LDY (Zero Page): Load #firstValue from [#address]")
    testLDY_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, address,
                         OP_LDY_Z, address)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDY_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index, OP_LDA_I, firstValue, OP_STA_Z_IX, address, OP_LDY_Z_IX, address)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDY_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_ABS, addressHi, addressLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLDY_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index, OP_LDY_ABS_IX, addressHi, addressLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testADC(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue, OP_ADC_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testADC_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue, OP_LDX_I, index, OP_ADC_Z_IX, indexPoint)
        memory.setByteAt(memLoc, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testADC_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue, OP_ADC_Z, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())
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
    testADC_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue, OP_ADC_ABS, 0x1, 0x2C)
        memory.setMemory(0, program.getProgramAsByteArray())
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

    @Unroll("ADC (Absolute[X)) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    testADC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index, OP_LDA_I, firstValue, OP_ADC_ABS_IX, 0x1, 0x2C)
        memory.setMemory(0, program.getProgramAsByteArray())
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
    testADC_ABS_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, firstValue,
                         OP_ADC_ABS_IY, 0x1, 0x2C)
        memory.setMemory(0, program.getProgramAsByteArray())
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

    @Unroll("ADC (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) & #secondValue = #expectedAcc")
    testADC_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,    //Value at indirect address
                         OP_STA_ABS, locationHi, locationLo,
                         OP_LDX_I, index,
                         OP_LDA_I, locationHi,  //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, locationLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_I, secondValue,
                         OP_ADC_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        locationHi | locationLo | firstValue | secondValue | index | expectedAcc | Z      | N     | C     | O     | Expected
        0x1        | 0x10       | 0x0        | 0x0         | 0     | 0x0         | true   | false | false | false | "With zero result"
     //   0x1        | 0x10       | 0x50       | 0xD0        | 1     | 0x20        | false  | false | true  | false | "With positive, carried result"
        0x1        | 0x10       | 0x50       | 0x50        | 2     | 0xA0        | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("ADC (Indirect, Y) #Expected: #firstValue + #secondValue -> #expectedAcc")
    testADC_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,           //Index to use
                         OP_LDA_I, firstValue,      //High order byte at pointer
                         OP_STA_ABS_IY, pointerHi, pointerLo,
                         OP_LDA_I, pointerHi,       //Pointer location
                         OP_STA_Z, 0x60,
                         OP_LDA_I, pointerLo,       //Pointer location
                         OP_STA_Z, 0x61,
                         OP_LDA_I, secondValue,
                         OP_ADC_IND_IY, 0x60)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        pointerHi | pointerLo | firstValue | secondValue | index | expectedAcc | Z      | N     | C     | O     | Expected
        0x1       | 0x10      | 0x0        | 0x0         | 0     | 0x0         | true   | false | false | false | "With zero result"
    //    0x1       | 0x10      | 0x50       | 0xD0        | 1     | 0x20        | false  | false | true  | false | "With positive, carried result"
        0x1       | 0x10      | 0x50       | 0x50        | 2     | 0xA0        | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("ADC 16bit [#lowFirstByte|#highFirstByte] + [#lowSecondByte|#highSecondByte] = #Expected")
    testMultiByteADC(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_CLC,
                         OP_LDA_I, lowFirstByte,
                         OP_ADC_I, lowSecondByte,
                         OP_STA_Z, 40,
                         OP_LDA_I, highFirstByte,
                         OP_ADC_I, highSecondByte)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testAND(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue, OP_AND_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testAND_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, secondValue,
                         OP_AND_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testAND_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, secondValue,
                         OP_AND_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testAND_A(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x20, 0x01,
                         OP_LDA_I, secondValue,
                         OP_AND_ABS, 0x20, 0x01)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testAND_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, locationHi, locationLo,
                         OP_LDA_I, secondValue,
                         OP_AND_ABS_IX, locationHi, locationLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testAND_ABS_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IY, locationHi, locationLo,
                         OP_LDA_I, secondValue,
                         OP_AND_ABS_IY, locationHi, locationLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("AND (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) & #secondValue = #expectedAcc")
    testAND_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,    //Value at indirect address
                         OP_STA_ABS, locationHi, locationLo,
                         OP_LDX_I, index,
                         OP_LDA_I, locationHi,   //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, locationLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_I, secondValue,
                         OP_AND_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
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

    @Unroll("AND (Indirect, Y) #Expected: #firstValue & #secondValue -> #expectedAcc")
    testAND_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,           //Index to use
                         OP_LDA_I, firstValue,      //High order byte at pointer
                         OP_STA_ABS_IY, pointerHi, pointerLo,
                         OP_LDA_I, pointerHi,       //Pointer location
                         OP_STA_Z, 0x60,
                         OP_LDA_I, pointerLo,       //Pointer location
                         OP_STA_Z, 0x61,
                         OP_LDA_I, secondValue,
                         OP_AND_IND_IY, 0x60)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc

        where:

        pointerHi | pointerLo | firstValue | index | secondValue | expectedAcc | Z      | N     | Expected
        0x1       | 0x10      | 0b00000001 | 0     | 0b00000001  | 0b00000001  | false  | false | "Unchanged accumulator"
        0x2       | 0x20      | 0b00000001 | 1     | 0b00000010  | 0b00000000  | true   | false | "No matching bits"
        0x3       | 0x30      | 0b00000011 | 2     | 0b00000010  | 0b00000010  | false  | false | "1 matched bit, 1 unmatched"
        0x4       | 0x40      | 0b00101010 | 3     | 0b00011010  | 0b00001010  | false  | false | "Multiple matched/unmatched bits"
    }

    @Unroll("ORA (Immediate) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue, OP_ORA_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOR_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, secondValue,
                         OP_ORA_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("ORA (Zero Page[X)) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, secondValue,
                         OP_ORA_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOR_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x20, 0x05,
                         OP_LDA_I, secondValue,
                         OP_ORA_ABS, 0x20, 0x05)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOR_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x20, 0x05,
                         OP_LDA_I, secondValue,
                         OP_ORA_ABS_IX, 0x20, 0x05)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("ORA (Absolute[Y]) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR_ABS_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IY, 0x20, 0x05,
                         OP_LDA_I, secondValue,
                         OP_ORA_ABS_IY, 0x20, 0x05)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("ORA (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) | #secondValue = #expectedAcc")
    testOR_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,    //Value at indirect address
                         OP_STA_ABS, locationHi, locationLo,
                         OP_LDX_I, index,
                         OP_LDA_I, locationHi,  //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, locationLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_I, secondValue,
                         OP_ORA_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        locationHi | locationLo | firstValue | index | secondValue | expectedAcc | Z      | N     | Expected
        0x1        | 0x10       | 0b00000001 | 0     | 0b00000001  | 0b00000001  | false  | false | "Duplicate bits"
        0x2        | 0x20       | 0b00000000 | 1     | 0b00000001  | 0b00000001  | false  | false | "One bit in Accumulator"
        0x3        | 0x30       | 0b00000001 | 2     | 0b00000000  | 0b00000001  | false  | false | "One bit from passed value"
        0x4        | 0x40       | 0b00000001 | 3     | 0b00000010  | 0b00000011  | false  | false | "One bit fro Accumulator, one from new value"
        0x5        | 0x50       | 0b00000001 | 4     | 0b10000010  | 0b10000011  | false  | true  | "Negative result"
    }

    @Unroll("ORA (Indirect, Y) #Expected: #firstValue | #secondValue -> #expectedAcc")
    testORA_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,           //Index to use
                         OP_LDA_I, firstValue,      //High order byte at pointer
                         OP_STA_ABS_IY, pointerHi, pointerLo,
                         OP_LDA_I, pointerHi,       //Pointer location
                         OP_STA_Z, 0x60,
                         OP_LDA_I, pointerLo,       //Pointer location
                         OP_STA_Z, 0x61,
                         OP_LDA_I, secondValue,
                         OP_ORA_IND_IY, 0x60)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc

        where:
        pointerHi | pointerLo | firstValue | index | secondValue | expectedAcc | Z      | N     | Expected
        0x1       | 0x10      | 0b00000001 | 0     | 0b00000001  | 0b00000001  | false  | false | "Duplicate bits"
        0x2       | 0x20      | 0b00000000 | 1     | 0b00000001  | 0b00000001  | false  | false | "One bit in Accumulator"
        0x3       | 0x30      | 0b00000001 | 2     | 0b00000000  | 0b00000001  | false  | false | "One bit from passed value"
        0x4       | 0x40      | 0b00000001 | 3     | 0b00000010  | 0b00000011  | false  | false | "One bit fro Accumulator, one from new value"
        0x5       | 0x50      | 0b00000001 | 4     | 0b10000010  | 0b10000011  | false  | true  | "Negative result"
    }

    @Unroll("EOR (Immediate) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue, OP_EOR_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testEOR_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, firstValue,
                         OP_EOR_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testEOR_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, firstValue,
                         OP_EOR_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testEOR_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x20, 0x04,
                         OP_LDA_I, firstValue,
                         OP_EOR_ABS, 0x20, 0x04)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testEOR_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IX, 0x20, 0x04,
                         OP_LDA_I, firstValue,
                         OP_EOR_ABS_IX, 0x20, 0x04)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("EOR (Absolute[Y]) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR_ABS_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IY, 0x20, 0x04,
                         OP_LDA_I, firstValue,
                         OP_EOR_ABS_IY, 0x20, 0x04)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("EOR (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) EOR #secondValue = #expectedAcc")
    testEOR_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,      //Value at indirect address
                         OP_STA_ABS, locationHi, locationLo,
                         OP_LDX_I, index,
                         OP_LDA_I, locationHi,      //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, locationLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_I, secondValue,
                         OP_EOR_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        locationHi | locationLo | index | firstValue | secondValue | expectedAcc | Z      | N     | Expected
        0x1        | 0x10       | 0     | 0b00000001 | 0b00000000  | 0b00000001  | false  | false | "One"
        0x2        | 0x20       | 1     | 0b00000000 | 0b00000001  | 0b00000001  | false  | false | "The other"
        0x3        | 0x34       | 2     | 0b00000001 | 0b00000001  | 0b00000000  | true   | false | "Not both"
    }

    @Unroll("EOR (Indirect, Y) #Expected: #firstValue ^ #secondValue -> #expectedAcc")
    testEOR_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,           //Index to use
                         OP_LDA_I, firstValue,      //High order byte at pointer
                         OP_STA_ABS_IY, pointerHi, pointerLo,
                         OP_LDA_I, pointerHi,       //Pointer location
                         OP_STA_Z, 0x60,
                         OP_LDA_I, pointerLo,       //Pointer location
                         OP_STA_Z, 0x61,
                         OP_LDA_I, secondValue,
                         OP_EOR_IND_IY, 0x60)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc

        where:
        pointerHi | pointerLo | index | firstValue | secondValue | expectedAcc | Z      | N     | Expected
        0x1       | 0x10      | 0     | 0b00000001 | 0b00000000  | 0b00000001  | false  | false | "One"
        0x2       | 0x20      | 1     | 0b00000000 | 0b00000001  | 0b00000001  | false  | false | "The other"
        0x3       | 0x34      | 2     | 0b00000001 | 0b00000001  | 0b00000000  | true   | false | "Not both"
    }

    @Unroll("STA (Indirect, X) #Expected: #value stored at [#locationHi|#locationLo]")
    testSTA_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, locationHi,      //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, locationLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_I, value,           //Value to store
                         OP_STA_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(7)

        then: 'The value has been stored at the expected address'
        memory.getByte( (locationHi << 8) | locationLo ) == value

        where:
        locationHi | locationLo | value | index | Expected
        0x01       | 0x10       | 1     | 0     | "Standard store accumulator"
        0x01       | 0x10       | 1     | 1     | "Store using index 1"
        0x01       | 0x10       | 30    | 2     | "Store random value"
        0x01       | 0x11       | 45    | 3     | "Store at different low order location"
        0x04       | 0x11       | 12    | 4     | "Store at different high order location"
    }

    @Unroll("STA (Indirect, Y) #Expected: #value stored at [#locationHi|#locationLo]")
    testSTA_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, locationHi,      //Indirect address in memory
                         OP_STA_Z, 0x30,
                         OP_LDA_I, locationLo,
                         OP_STA_Z, 0x31,
                         OP_LDA_I, value,           //Value to store
                         OP_LDY_I, index,
                         OP_STA_IND_IY, 0x30)       // (Z[0x30] = two byte address) + Y -> pointer
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(7)
        Registers registers = processor.getRegisters()

        then: 'The value has been stored at the expected address'
        int address = (locationHi << 8) | locationLo
        int yIndex = registers.getRegister(Registers.REG_Y_INDEX)
        memory.getByte( address + yIndex )  == value

        where:
        locationHi | locationLo | value | index | Expected
        0x01       | 0x10       | 1     | 0     | "Standard store accumulator"
        0x01       | 0x10       | 1     | 1     | "Store using index 1"
        0x01       | 0x10       | 30    | 2     | "Store random value"
        0x01       | 0x11       | 45    | 3     | "Store at different low order location"
        0x04       | 0x11       | 12    | 4     | "Store at different high order location"
    }

    @Unroll("SBC (Immediate) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    testSBC(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_SEC, OP_LDA_I, firstValue,
                         OP_SBC_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testSBC_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testSBC_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testSBC_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x02, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_ABS, 0x02, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testSBC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IX, 0x02, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_ABS_IX, 0x02, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testSBC_ABS_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IY, 0x02, 0x20,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_ABS_IY, 0x02, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("SBC (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) - #secondValue = #expectedAcc")
    testSBC_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,    //Value at indirect address
                         OP_STA_ABS, locationHi, locationLo,
                         OP_LDX_I, index,
                         OP_LDA_I, locationHi,     //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, locationLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(10)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        //TODO O/C

        where:
        locationHi | locationLo | index | firstValue | secondValue | expectedAcc | Z      | N     | O     | C     | Expected
        0x1        | 0x10       | 0     | 0x5        | 0x3         | 0x2         | false  | false | false | false | "Basic subtraction"
        0x2        | 0x13       | 1     | 0x5        | 0x5         | 0x0         | true   | false | false | false | "With zero result"
        0x3        | 0x26       | 2     | 0x5        | 0x6         | 0xFF        | false  | true  | false | false | "with negative result"
    }

    @Unroll("SBC (Indirect, Y) #Expected: #firstValue (@[#locationHi|#locationLo]) - #secondValue = #expectedAcc")
    testSBC_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,           //Index to use
                         OP_LDA_I, secondValue,      //High order byte at pointer
                         OP_STA_ABS_IY, pointerHi, pointerLo,
                         OP_LDA_I, pointerHi,       //Pointer location
                         OP_STA_Z, 0x60,
                         OP_LDA_I, pointerLo,       //Pointer location
                         OP_STA_Z, 0x61,
                         OP_LDA_I, firstValue,
                         OP_SEC,
                         OP_SBC_IND_IY, 0x60)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(10)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAcc
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        //TODO O/C

        where:
        pointerHi | pointerLo | index | firstValue | secondValue | expectedAcc | Z      | N     | O     | C     | Expected
        0x1       | 0x10      | 0     | 0x5        | 0x3         | 0x2         | false  | false | false | false | "Basic subtraction"
        0x2       | 0x13      | 1     | 0x5        | 0x5         | 0x0         | true   | false | false | false | "With zero result"
        0x3       | 0x26      | 2     | 0x5        | 0x6         | 0xFF        | false  | true  | false | false | "with negative result"
    }

    @Unroll("INX #Expected: on #firstValue = #expectedX")
    testINX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, firstValue, OP_INX)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testINC_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_INC_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testINC_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_INC_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testINC_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_INC_ABS, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testINC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x01, 0x20,
                         OP_INC_ABS_IX, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testDEC_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_DEC_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("DEC (Zero Page[X]) #Expected: on #firstValue at #loc[#index) = #expectedMem")
    testDEC_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, loc,
                         OP_DEC_Z_IX, loc)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testDEC_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_DEC_ABS, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testDEC_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x01, 0x20,
                         OP_DEC_ABS_IX, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testDEX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, firstValue, OP_DEX)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testINY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, firstValue, OP_INY)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testDEY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, firstValue, OP_DEY)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testPLA(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_PHA,
                         OP_LDA_I, 0x11,
                         OP_PLA)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)
        assert(registers.getRegister(Registers.REG_SP) == 0xFE)
        processor.step(2)

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
    testASL(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_ASL_A)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testASL_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_ASL_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testASL_A(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_ASL_ABS, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testASL_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_LDX_I, index,
                         OP_STA_Z_IX, 0x20,
                         OP_ASL_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testASL_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_LDX_I, index,
                         OP_STA_ABS_IX, 0x01, 0x20,
                         OP_ASL_ABS_IX, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLSR(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_LSR_A)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLSR_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_LSR_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLSR_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LSR_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLSR_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x02, 0x20,
                         OP_LSR_ABS, 0x02, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testLSR_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x02, 0x20,
                         OP_LSR_ABS_IX, 0x02, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testJMP(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP,
                         OP_NOP,
                         OP_NOP,
                         OP_JMP_ABS, jmpLocationHi, jmpLocationLow,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("JMP (Indirect) #expected: [#jmpLocationHi | #jmpLocationLow] -> #expectedPC")
    testIndirectJMP(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, jmpLocationHi,
                         OP_STA_ABS, 0x01, 0x40,
                         OP_LDA_I, jmpLocationLow,
                         OP_STA_ABS, 0x01, 0x41,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP,
                         OP_JMP_IND, 0x01, 0x40,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
        0b00000000    | 0b00000001     | 8            | 1          | "Standard jump back"
        0b00000000    | 0b00000000     | 9            | 2          | "Standard jump back then step"
        0b00000000    | 0b00000111     | 8            | 7          | "Standard jump forward"
        0b00000000    | 0b00000111     | 9            | 10         | "Standard jump forward then step"
        0b00000001    | 0b00000000     | 8            | 256        | "High byte jump"
        0b00000001    | 0b00000001     | 8            | 257        | "Double byte jump"
    }

    @Unroll("BCC #expected: ending up at mem[#expectedPC] after #instructions steps")
    testBCC(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP, OP_NOP, OP_NOP, preInstr, OP_BCC, jmpSteps, OP_NOP, OP_NOP, OP_NOP, OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBCS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP, OP_NOP, OP_NOP, preInstr, OP_BCS, jmpSteps, OP_NOP, OP_NOP, OP_NOP, OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testROL_A(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(preInstr, OP_LDA_I,
                         firstValue, OP_ROL_A)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testROL_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(firstInstr,
                         OP_LDA_I, firstValue,
                         OP_STA_Z, 0x20,
                         OP_ROL_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testROL_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         firstInstr,
                         OP_LDA_I, firstValue,
                         OP_STA_Z_IX, 0x20,
                         OP_ROL_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testROL_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(firstInstr,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS, 0x20, 0x07,
                         OP_ROL_ABS, 0x20, 0x07)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testROL_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         firstInstr,
                         OP_LDA_I, firstValue,
                         OP_STA_ABS_IX, 0x20, 0x07,
                         OP_ROL_ABS_IX, 0x20, 0x07)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testROR_A(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(preInstr,
                         OP_LDA_I, firstValue,
                         OP_ROR_A)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBNE(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP,
                         OP_NOP,
                         OP_NOP,
                         OP_LDA_I, accumulatorValue,
                         OP_BNE, jumpSteps,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBEQ(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP, OP_NOP, OP_NOP,
                         OP_LDA_I, accumulatorValue,
                         OP_BEQ, jumpSteps,
                         OP_NOP, OP_NOP, OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBMI(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP,
                         OP_NOP,
                         OP_NOP,
                         OP_LDA_I, accumulatorValue,
                         OP_BMI, jumpSteps,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBPL(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP,
                         OP_NOP,
                         OP_NOP,
                         OP_LDA_I, accumulatorValue,
                         OP_BPL, jumpSteps,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBVC(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP,
                         OP_NOP,
                         OP_NOP,
                         OP_LDA_I, accumulatorValue,
                         OP_ADC_I, addedValue,
                         OP_BVC, jumpSteps,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBVS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_NOP,
                         OP_NOP,
                         OP_NOP,
                         OP_LDA_I, accumulatorValue,
                         OP_ADC_I, addedValue,
                         OP_BVS, jumpSteps,
                         OP_NOP,
                         OP_NOP,
                         OP_NOP)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testTAX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, loadedValue,
                         OP_TAX)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testTAY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, loadedValue,
                         OP_TAY)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testTYA(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, loadedValue, OP_TYA)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testTXA(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, loadedValue, OP_TXA)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testTSX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_TSX)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBIT(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_Z, memLoc,
                         OP_LDA_I, secondValue,
                         OP_BIT_Z, memLoc)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testBIT_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_STA_ABS, memLocHi, memLocLo,
                         OP_LDA_I, secondValue,
                         OP_BIT_ABS, memLocHi, memLocLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_STA_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, value,
                         OP_LDX_I, index,
                         OP_STA_Z_IX, location)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("STA (Absolute) #expected: Store #value at [#locationHi|#locationLo]")
    testOP_STA_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, value,
                         OP_STA_ABS, locationHi, locationLo)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)

        then:
        registers.getPC() == program.length
        memory.getByte((locationHi << 8 | locationLo)) == value

        where:
        locationHi | locationLo | value | expected
        0x20       |  0         | 0x0F  | "Store with 0 low byte"
        0x40       |  30        | 0x0E  | "Store at non zero low byte"
    }

    @Unroll("STA (Absolute[X]) #expected: Store #value at [#locationHi|#locationLo@#index]")
    testOP_STA_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, value,
                         OP_LDX_I, index,
                         OP_STA_ABS_IX, locationHi, locationLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_STA_ABS_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, value,
                         OP_LDY_I, index,
                         OP_STA_ABS_IY, locationHi, locationLo)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_STY_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index, OP_LDY_I, firstValue, OP_STY_Z_IX, memLocation)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_CMP_I(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, firstValue,
                         OP_CMP_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == firstValue
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | Z     | N     | C     | Expected
        0x10       | 0x10        | true  | false | true  | "Basic compare"
        0x11       | 0x10        | false | false | true  | "Carry flag set"
        0x10       | 0x11        | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Zero Page) #Expected: #firstValue == #secondValue")
    testOP_CMP_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == firstValue
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | Z     | N     | C     | Expected
        0x10       | 0x10        | true  | false | true  | "Basic compare"
        0x11       | 0x10        | false | false | true  | "Carry flag set"
        0x10       | 0x11        | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Zero Page[X]) #Expected: #firstValue == #secondValue")
    testOP_CMP_Z_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_Z_IX, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_Z_IX, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == firstValue
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | index | Z     | N     | C     | Expected
        0x10       | 0x10        | 0     | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 1     | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 2     | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 3     | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Absolute) #Expected: #firstValue == #secondValue")
    testOP_CMP_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_ABS, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(4)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == firstValue
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | Z     | N     | C     | Expected
        0x10       | 0x10        | true  | false | true  | "Basic compare"
        0x11       | 0x10        | false | false | true  | "Carry flag set"
        0x10       | 0x11        | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Absolute[X]) #Expected: #firstValue == #secondValue")
    testOP_CMP_ABS_IX(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IX, 0x01, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_ABS_IX, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == firstValue
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | index | Z     | N     | C     | Expected
        0x10       | 0x10        | 0     | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 1     | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 2     | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 3     | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Absolute[Y]) #Expected: #firstValue == #secondValue")
    testOP_CMP_ABS_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDA_I, secondValue,
                         OP_STA_ABS_IY, 0x01, 0x20,
                         OP_LDA_I, firstValue,
                         OP_CMP_ABS_IY, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == program.length
        registers.getRegister(Registers.REG_ACCUMULATOR) == firstValue
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | index | Z     | N     | C     | Expected
        0x10       | 0x10        | 0     | true  | false | true  | "Basic compare"
        0x11       | 0x10        | 1     | false | false | true  | "Carry flag set"
        0x10       | 0x11        | 2     | false | true  | false | "Smaller value - larger"
        0xFF       | 0x01        | 3     | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Indirect, X). #Expected: #firstValue == #secondValue")
    testCMP_IND_IX() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,    //Value at indirect address
                         OP_STA_ABS, pointerHi, pointerLo,
                         OP_LDX_I, index,
                         OP_LDA_I, pointerHi,  //Indirect address in memory
                         OP_STA_Z_IX, 0x30,
                         OP_LDA_I, pointerLo,
                         OP_STA_Z_IX, 0x31,
                         OP_LDA_I, firstValue,
                         OP_CMP_IND_IX, 0x30)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == firstValue
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        pointerHi | pointerLo | firstValue | secondValue | index | Z     | N     | C     | Expected
        0x02      | 0x20      | 0x10       | 0x10        | 0     | true  | false | true  | "Basic compare"
        0x02      | 0x22      | 0x11       | 0x10        | 1     | false | false | true  | "Carry flag set"
        0x03      | 0x35      | 0x10       | 0x11        | 2     | false | true  | false | "Smaller value - larger"
        0x04      | 0x41      | 0xFF       | 0x01        | 3     | false | true  | true  | "Negative result"
    }

    @Unroll("CMP (Indirect, Y) #Expected: #firstValue == #secondValue")
    testCMP_IND_IY() {
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,           //Index to use
                         OP_LDA_I, firstValue,      //High order byte at pointer
                         OP_STA_ABS_IY, pointerHi, pointerLo,
                         OP_LDA_I, pointerHi,       //Pointer location
                         OP_STA_Z, pointerHiMem,
                         OP_LDA_I, pointerLo,       //Pointer location
                         OP_STA_Z, pointerLoMem,
                         OP_LDA_I, secondValue,
                         OP_CMP_IND_IY, 0x60)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(9)
        Registers registers = processor.getRegisters()

        then:
        registers.getPC() == program.length
        Z == registers.statusFlags[Registers.Z]
      //  N == registers.statusFlags[Registers.N]
      //  C == registers.statusFlags[Registers.C]

        where:
        pointerHiMem | pointerLoMem | pointerHi | pointerLo | firstValue | secondValue | index | Z     | N     | C     | Expected
        0x60         | 0x61         | 0x02      | 0x20      | 0x10       | 0x10        | 0     | true  | false | true  | "Basic compare"
        0x80         | 0x81         | 0x02      | 0x22      | 0x11       | 0x10        | 1     | false | false | true  | "Carry flag set"
        0x55         | 0x56         | 0x03      | 0x35      | 0x10       | 0x11        | 2     | false | true  | false | "Smaller value - larger"
        0xF0         | 0xF1         | 0x04      | 0x41      | 0xFF       | 0x01        | 3     | false | true  | true  | "Negative result"
    }

    @Unroll("CPY (Immediate) #Expected: #firstValue == #secondValue")
    testOP_CPY_I(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, firstValue,
                         OP_CPY_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_CPY_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDY_I, firstValue,
                         OP_CPY_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_CPY_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_LDY_I, firstValue,
                         OP_CPY_ABS, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_CPX_Z(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_Z, 0x20,
                         OP_LDX_I, firstValue,
                         OP_CPX_Z, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_CPX_ABS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, secondValue,
                         OP_STA_ABS, 0x01, 0x20,
                         OP_LDX_I, firstValue,
                         OP_CPX_ABS, 0x01, 0x20)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    testOP_CPX_I(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDX_I, firstValue,
                         OP_CPX_I, secondValue)
        memory.setMemory(0, program.getProgramAsByteArray())

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
    OP_STX_Z_IY(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDY_I, index,
                         OP_LDX_I, firstValue,
                         OP_STX_Z_IY, location)
        memory.setMemory(0, program.getProgramAsByteArray())

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

    @Unroll("RTS #expected")
    testRTS(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, memHi,
                         OP_PHA,
                         OP_LDA_I, memLo,
                         OP_PHA,
                         OP_RTS)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(5)

        then:
        registers.getPC() == expectedPC
        registers.getRegister(Registers.REG_SP) == expectedSP

        where:
        memHi | memLo | expectedPC | expectedSP | expected
        0x1   | 0x0   | 0x100      | 0xFF       | "Simple return from subroutine"
    }

    @Unroll("BRK #expected")
    testBRK(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_BRK)
        memory.setByteAt(0xFFFE, newPCHi)
        memory.setByteAt(0xFFFF, newPCLo)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and: 'The status register is set to a value that will be pushed to stack'
        registers.setRegister(Registers.REG_STATUS, statusReg)

        and:
        processor.step(1)

        then:
        registers.getRegister(Registers.REG_PC_HIGH) == newPCHi
        registers.getRegister(Registers.REG_PC_LOW) == newPCLo
        registers.getRegister(Registers.REG_SP) == 0xFC

        memory.getByte(0x1FD) == (statusReg | Registers.STATUS_FLAG_BREAK)
        memory.getByte(0x1FE) == 0x03
        memory.getByte(0x1FF) == 0x00

        //XXX Refactor to test when PC overflows to high byte before loading to stack

        where:
        newPCHi | newPCLo | statusReg  | expected
        0x0     | 0x0     | 0b00000000 | "With empty status register and B not set"
        0x0     | 0x0     | 0b00100000 | "With empty status register and B already set"
        0x1     | 0x1     | 0b10000101 | "With loaded status register"
    }

    @Unroll("IRQ #expected #statusValue->#pushedStatus")
    testIRQ(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, 1,
                         OP_LDA_I, 2,
                         OP_LDA_I, 3,
                         OP_LDA_I, 4,
                         OP_LDA_I, 5)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        memory.setMemory(0xFFFA, 1)
        memory.setMemory(0xFFFB, 2)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(steps)
        registers.setRegister(Registers.REG_STATUS, statusValue)
        processor.irq()

        then: 'Three items have been added to stack'
        registers.getRegister(Registers.REG_SP) == 0xFC

        and: 'The PC on the stack is as expected'
        memory.getByte(0x1FE) == pushedPCLo
        memory.getByte(0x1FF) == pushedPCHi

        and: 'Status register is moved to stack with B set'
        memory.getByte(0x1FD) == pushedStatus

        and: 'The PC is set to 0xFFFE:0xFFFF'
        registers.getRegister(Registers.REG_PC_HIGH) == memory.getByte(0xFFFE)
        registers.getRegister(Registers.REG_PC_LOW)  == memory.getByte(0xFFFF)

        where:
        statusValue | steps | pushedStatus | pushedPCHi | pushedPCLo | expected
        0b00000000  | 1     | 0b00000100   | 0x00       | 0x02       | "Empty status register"
        0b11111111  | 1     | 0b11111111   | 0x00       | 0x02       | "Full status register"
        0b10101010  | 1     | 0b10101110   | 0x00       | 0x02       | "Random status register"
        0b00000000  | 2     | 0b00000100   | 0x00       | 0x04       | "Two steps"
        0b00000000  | 3     | 0b00000100   | 0x00       | 0x06       | "Three steps"
    }

    @Unroll("NMI #expected #statusValue->#pushedStatus")
    testNMI(){
        when:
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, 1,
                         OP_LDA_I, 2,
                         OP_LDA_I, 3,
                         OP_LDA_I, 4,
                         OP_LDA_I, 5)
        memory.setMemory(0, program.getProgramAsByteArray())

        and:
        memory.setMemory(0xFFFA, 1)
        memory.setMemory(0xFFFB, 2)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(steps)
        registers.setRegister(Registers.REG_STATUS, statusValue)
        processor.nmi()

        then: 'Three items have been added to stack'
        registers.getRegister(Registers.REG_SP) == 0xFC

        and: 'The PC on the stack is as expected'
        memory.getByte(0x1FE) == pushedPCLo
        memory.getByte(0x1FF) == pushedPCHi

        and: 'Status register is moved to stack with B set'
        memory.getByte(0x1FD) == pushedStatus

        and: 'The PC is set to 0xFFFE:0xFFFF'
        registers.getRegister(Registers.REG_PC_HIGH) == memory.getByte(0xFFFA)
        registers.getRegister(Registers.REG_PC_LOW)  == memory.getByte(0xFFFB)

        where:
        statusValue | steps | pushedStatus | pushedPCHi | pushedPCLo | expected
        0b00000000  | 1     | 0b00000100   | 0x00       | 0x02       | "Empty status register"
        0b11111111  | 1     | 0b11111111   | 0x00       | 0x02       | "Full status register"
        0b10101010  | 1     | 0b10101110   | 0x00       | 0x02       | "Random status register"
        0b00000000  | 2     | 0b00000100   | 0x00       | 0x04       | "Two steps"
        0b00000000  | 3     | 0b00000100   | 0x00       | 0x06       | "Three steps"
    }

    @Unroll("RTI #expected ")
    testRTI(){
        when: 'We have a programText which will be interrupted'
        Memory memory = new SimpleMemory()
        Program program = new Program().with(OP_LDA_I, 1,
                         OP_LDA_I, 2, //--> IRQ Here
                         OP_LDA_I, 4, //<-- Return here
                         OP_LDA_I, 5,
                         OP_LDA_I, 6)
        memory.setMemory(0, program.getProgramAsByteArray())

        and: 'An interrupt routine'
        Program irqRoutine = new Program().with(OP_LDA_I, 3, OP_RTI)
        memory.setMemory(0x100, irqRoutine.getProgramAsByteArray())
        memory.setByteAt(0xFFFE, 0x01)
        memory.setByteAt(0xFFFF, 0x00)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        Registers registers = processor.getRegisters()

        and:
        processor.step(2)
        registers.setRegister(Registers.REG_STATUS, statusValue)
        processor.irq()
        processor.step(2)

        then: 'Stack is empty again'
        registers.getRegister(Registers.REG_SP) == 0xFF

        and: 'The PC on the stack is as expected'
        registers.getRegister(Registers.REG_PC_HIGH) == restoredPCHi
        registers.getRegister(Registers.REG_PC_LOW) == restoredPCLo

        and: 'Status register is moved to stack with B set'
        registers.getRegister(Registers.REG_STATUS) == restoredStatus

        and: 'The PC is set to where it was before IRQ'
        registers.getRegister(Registers.REG_PC_HIGH) == 0x00
        registers.getRegister(Registers.REG_PC_LOW)  == 0x04

        where:
        statusValue | restoredStatus | restoredPCHi | restoredPCLo | expected
        0b00000000  | 0b00000100     | 0x00         | 0x04         | "Empty status register"
        0b11111111  | 0b11111111     | 0x00         | 0x04         | "Full status register"
        0b10101010  | 0b10101110     | 0x00         | 0x04         | "Random status register"
    }

//    @Ignore
//    def exampleTest(){
//        when:
//        Memory memory = new SimpleMemory(65534)
//        int[] programText = []
//        memory.setMemory(0, programText)
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
//        registers.getPC() == programText.length
//
//        where:
//        A | B
//        A | B
//    }
}