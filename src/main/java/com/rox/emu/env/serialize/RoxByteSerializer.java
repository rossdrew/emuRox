package com.rox.emu.env.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rox.emu.com.rox.serialize.ClassSerialization;
import com.rox.emu.env.RoxByte;

import java.io.IOException;

public class RoxByteSerializer extends ClassSerialization<RoxByte> {
    public void customSerializations(RoxByte subject, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumberField("value", subject.getRawValue());
    }
}

