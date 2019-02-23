package com.rox.emu.env.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rox.emu.env.RoxWord;

import java.io.IOException;

public class RoxWordDeserializer extends StdDeserializer<RoxWord> {
    public RoxWordDeserializer() {
        this(null);
    }

    public RoxWordDeserializer(Class<RoxWord> subject) {
        super(subject);
    }

    @Override
    public RoxWord deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = p.getCodec().readTree(p);
        //assert node.get("class").textValue().equals(RoxWord.class.getName());
        int literalValue = (Integer) node.get("value").numberValue();
        return RoxWord.fromLiteral(literalValue);
    }
}