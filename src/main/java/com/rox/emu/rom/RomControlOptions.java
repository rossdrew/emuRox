package com.rox.emu.rom;

class RomControlOptions {
    enum Mirroring {
        VERTICAL, HORIZONTAL, FOUR_SCREEN
    }

    private Mirroring mirroring; // Bit 0: 0=horizontal, 1=vertical.  Bit 3: 1=four-screen
    private boolean ramPresent;
    private boolean trainerPresent;
    private int mapperNumber; //4 lower bits?

    public RomControlOptions(final int ctrlOptionsByte){

    }
}
