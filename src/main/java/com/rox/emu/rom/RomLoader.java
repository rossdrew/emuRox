package com.rox.emu.rom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RomLoader {
    public static void main(String[] args) throws IOException {
        final File file = new File( "src" +  File.separator + "main" +  File.separator + "resources" + File.separator + "rom" + File.separator + "Zelda.NES");

        System.out.println("Loading '" + file.getAbsolutePath() + "'...");

        final FileInputStream fis = new FileInputStream(file);
        byte fileContent[] = new byte[(int)file.length()];
        fis.read(fileContent);

        final InesRom rom = InesRom.from(fileContent);
        System.out.println(rom);

    }
}
