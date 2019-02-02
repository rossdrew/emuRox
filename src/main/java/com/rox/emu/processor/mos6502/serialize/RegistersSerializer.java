package com.rox.emu.processor.mos6502.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rox.emu.com.rox.serialize.ClassSerialization;
import com.rox.emu.processor.mos6502.Registers;

import java.io.IOException;

public class RegistersSerializer extends ClassSerialization<Registers> {
    public void customSerializations(Registers subject, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeObjectField("PC", subject.getPC());
        gen.writeObjectField("A", subject.getRegister(Registers.Register.ACCUMULATOR));
        gen.writeObjectField("X", subject.getRegister(Registers.Register.X_INDEX));
        gen.writeObjectField("Y", subject.getRegister(Registers.Register.Y_INDEX));
        gen.writeObjectField("SP Hi", subject.getRegister(Registers.Register.STACK_POINTER_HI));
        gen.writeObjectField("SP Lo", subject.getRegister(Registers.Register.STACK_POINTER_LOW));

    }
}
