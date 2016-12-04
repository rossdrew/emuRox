package com.rox.emu;

/**
 * @author rossdrew
 */
public class P6502 {
    private final String[] registerNames = new String[] {"Accumulator", "", "", "Program Counter Hi", "Program Counter Low", "", "Stack Pointer", "Status Flags"};

    public static final int ACC_REG = 0x0;
    public static final int PC_HI_REG = 0x3;
    public static final int PC_LO_REG = 0x4;
    public static final int SP_REG = 0x6;
    public static final int STATUS_FLAGS_REG = 0x0;
    public static final int STATUS_FLAG_CARRY = 0x0;
    public static final int STATUS_FLAGS_ZERO = 0x1;
    public static final int STATUS_FLAGS_IRQ_DISABLE = 0x2;
    public static final int STATUS_FLAGS_DEC = 0x3;
    public static final int STATUS_FLAGS_BREAK = 0x4;
    public static final int STATUS_FLAGS_UNUSED = 0x5;
    public static final int STATUS_FLAGS_OVERFLOW = 0x6;
    public static final int STATUS_FLAGS_NEGATIVE = 0x7;

    public static final int OPCODE_ADC_I = 0x69;  //ADC Immediate
    public static final int OPCODE_LDA_I = 0xA9;  //LDA Immediate

    private int[] registers = new int[8];
    private int[] memory;

    public P6502(int[] memory) {
        this.memory = memory;
    }

    /**
     * IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it
     */
    public void reset(){
        System.out.println("Resetting...");
        setRegister(STATUS_FLAGS_REG, 0x34);    //= 00110100  XXX EnumSet might be better here
        setRegister(PC_HI_REG, memory[0xFFFC]);
        setRegister(PC_LO_REG, memory[0xFFFD]);
        setRegister(SP_REG, 0xFF);
        System.out.println("...READY!");
    }

    public int[] getRegisters() {
        return registers;
    }

    public int getRegister(int registerID){
        return registers[registerID];
    }


    /**
     * Get the value of the 16 bit Program Counter (PC) and increment
     *
     * @param incrementFirst true = increment before returning, false = return then increment
     * @return the PC value before or after increment as per <i>incrementFirst</i>
     */
    private int getAndStepPC(boolean incrementFirst){
        final int originalPC = getPC();
        final int incrementedPC = originalPC + 1;
        setRegister(PC_HI_REG, incrementedPC & 0xFF00);
        setRegister(PC_LO_REG, incrementedPC & 0x00FF);
        System.out.println("Program Counter now " + incrementedPC);

        return incrementFirst ? incrementedPC : originalPC;
    }

    public int getPC(){
        return (getRegister(PC_HI_REG) << 8) | getRegister(PC_LO_REG);
    }

    private void setRegister(int registerID, int val){
        System.out.println("Setting (R)" + registerNames[registerID] + " to " + val);
        registers[registerID] = val;
    }

    private int getByteOfMemoryAt(int location){
        final int memoryByte = memory[location];
        System.out.println("Got " + memoryByte + " from mem[" + location + "]");
        return memoryByte;
    }

    public void step() {
        System.out.println("*** Step ***");
        int memoryLocation = getAndStepPC(false);
        int opCode = getByteOfMemoryAt(memoryLocation);

        //Execute the opcode
        switch (opCode){
            case OPCODE_ADC_I:
                System.out.println("Instruction: Immediate ADC...");
                memoryLocation = getAndStepPC(false);
                setRegister(ACC_REG, getByteOfMemoryAt(memoryLocation) + getRegister(ACC_REG));
                break;

            case OPCODE_LDA_I:
                System.out.println("Instruction: Immediate LDA...");
                memoryLocation = getAndStepPC(false);
                setRegister(ACC_REG, getByteOfMemoryAt(memoryLocation));
                break;

            default:
                System.out.println("Unknown OPCODE: " + opCode);
        }
    }
}
