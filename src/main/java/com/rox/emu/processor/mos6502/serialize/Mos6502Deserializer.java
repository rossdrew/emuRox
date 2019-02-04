package com.rox.emu.processor.mos6502.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rox.emu.processor.mos6502.Mos6502;

import java.io.IOException;

public class Mos6502Deserializer extends StdDeserializer<Mos6502> {

    public Mos6502Deserializer(){
        this(null);
    }

    protected Mos6502Deserializer(Class<Mos6502> vc) {
        super(vc);
    }

    @Override
    public Mos6502 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        //TODO class
        //TODO registers
        //TODO memory

        return null;
    }
}
