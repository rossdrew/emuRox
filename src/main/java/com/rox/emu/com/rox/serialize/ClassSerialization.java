package com.rox.emu.com.rox.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * An extension of {@code com.fasterxml.jackson}s {@link StdSerializer} for custom
 * serializations containing common behaviour such as class name entry.
 *
 * @param <SubjectClass> The type of the class to be serialized
 */
public abstract class ClassSerialization<SubjectClass> extends StdSerializer<SubjectClass> {

    public ClassSerialization(){
        this(null);
    }

    protected ClassSerialization(Class<SubjectClass> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(SubjectClass classBeingSerialized, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        //gen.writeStringField("class", classBeingSerialized.getClass().getName());
        customSerializations(classBeingSerialized, gen, provider);
        gen.writeEndObject();
    }

    /**
     * @param subject of serialization
     * @param gen the {@code com.fasterxml.jackson} {@link JsonGenerator} being used to serialize
     * @param provider the {@code com.fasterxml.jackson} {@link SerializerProvider} being used to serialize
     * @throws IOException thrown if there are any exceptions while serializing
     */
    protected abstract void customSerializations(SubjectClass subject, JsonGenerator gen, SerializerProvider provider) throws IOException;
}
