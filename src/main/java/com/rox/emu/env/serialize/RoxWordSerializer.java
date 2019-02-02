package com.rox.emu.env.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rox.emu.com.rox.serialize.ClassSerialization;
import com.rox.emu.env.RoxWord;

import java.io.IOException;

public class RoxWordSerializer extends ClassSerialization<RoxWord> {
    public void customSerializations(RoxWord subject, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumberField("value", subject.getRawValue());
    }
}

