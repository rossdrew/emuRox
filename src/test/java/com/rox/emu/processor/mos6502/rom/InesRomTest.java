package com.rox.emu.processor.mos6502.rom;

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class InesRomTest {
    @Test
    public void testCreation(){
        assertNotNull(new InesRom());
    }
}
