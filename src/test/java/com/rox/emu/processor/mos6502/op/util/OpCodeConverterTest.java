package com.rox.emu.processor.mos6502.op.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class OpCodeConverterTest {
    @Test
    public void testCreation(){
        //Class definitions need to be instantiated in tests in order to be counted in code coverage.
        // As this is a static utility class for now, we need to instantiate it to cover that edge case.
        assertNotNull(new OpCodeConverter());
    }
}
