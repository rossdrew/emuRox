package com.rox.emu.rest;

import com.rox.emu.processor.mos6502.Mos6502;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("mos6502")
public class ProcessorResource {

    @GET
    @Path("/{param}")
    public Response testGet(@PathParam("param") String msg){
        return Response.status(200).entity("The test with '" + msg + "' was successful.").build();
    }

    @GET
    public Response testGet(){
        return Response.status(200).entity("You have hit the test base url").build();
    }

    @POST
    @Path("/report")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response report(final Mos6502 cpu){
        return Response.status(200).entity("MOS6502: Mem[" + cpu.getMemory().getSize() + "], Reg[" + cpu.getRegisters().getAndStepProgramCounter() + "]").build();
    }

    @POST
    @Path("/execute/{steps}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Mos6502 step(final Mos6502 cpu, @PathParam("steps") int steps){
        cpu.step(steps);
        return cpu;
    }
}
