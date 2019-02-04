package com.rox.emu.processor.mos6502.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rox.emu.com.rox.serialize.ClassSerialization;
import com.rox.emu.processor.mos6502.Mos6502;

import java.io.IOException;

public class Mos6502Serializer extends ClassSerialization<Mos6502> {
    public void customSerializations(Mos6502 subject, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStringField("class", Mos6502.class.getName());
        gen.writeObjectField("registers", subject.getRegisters());
        gen.writeObjectField("memory", subject.getMemory());
    }
}
