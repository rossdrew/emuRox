package com.rox.emu.env.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rox.emu.env.RoxByte;

import java.io.IOException;

public class RoxByteDeserializer extends StdDeserializer<RoxByte> {
    public RoxByteDeserializer() {
        this(null);
    }

    public RoxByteDeserializer(Class<RoxByte> subject) {
        super(subject);
    }

    @Override
    public RoxByte deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = p.getCodec().readTree(p);
        assert node.get("class").textValue().equals(RoxByte.class.getName());
        int literalValue = (Integer) node.get("value").numberValue();
        return RoxByte.fromLiteral(literalValue);
    }
}