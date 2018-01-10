package com.rox.emu.rom;

/**
 76543210
 ||||||||
 |||||||+- Mirroring: 0: horizontal (vertical arrangement) (CIRAM A10 = PPU A11)
 |||||||              1: vertical (horizontal arrangement) (CIRAM A10 = PPU A10)
 ||||||+-- 1: Cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
 |||||+--- 1: 512-byte trainer at $7000-$71FF (stored before PRG data)
 ||||+---- 1: Ignore mirroring control or above mirroring bit; instead provide four-screen VRAM
 ++++----- Lower nybble of mapper number
 */
class RomControlOptions {
    enum Mirroring {
        VERTICAL, HORIZONTAL, FOUR_SCREEN
    }

    private final Mirroring mirroring;
    private final boolean ramPresent;
    private final boolean trainerPresent;
    private final int mapperNumber;

    public RomControlOptions(final int ctrlOptionsByte){
        mirroring = extractMirroring(ctrlOptionsByte);
        ramPresent = (ctrlOptionsByte & 0b00000010) != 0;
        trainerPresent = (ctrlOptionsByte & 0b00000100) != 0;
        mapperNumber = (ctrlOptionsByte & 0b11110000) >> 4;
    }

    private Mirroring extractMirroring(final int ctrlOptionsByte){
        if ((0b00001000 & ctrlOptionsByte) != 0){
            return Mirroring.FOUR_SCREEN;
        }else{
            return (ctrlOptionsByte & 0b00000001) != 0 ? Mirroring.VERTICAL : Mirroring.HORIZONTAL;
        }
    }

    public boolean isRamPresent() {
        return ramPresent;
    }

    public boolean isTrainerPresent(){
        return trainerPresent;
    }

    public Mirroring getMirroring() {
        return mirroring;
    }

    public int getMapperNumber(){
        return mapperNumber;
    }
}
