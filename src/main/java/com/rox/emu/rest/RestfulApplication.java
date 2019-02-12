package com.rox.emu.rest;

import com.rox.emu.com.rox.serialize.ObjectMapperContextResolver;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/emurox")
public class RestfulApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(ProcessorResource.class);
        s.add(ObjectMapperContextResolver.class);
        s.add(JacksonFeature.class);
        return s;
    }
}