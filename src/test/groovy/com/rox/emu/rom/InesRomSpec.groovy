package com.rox.emu.rom

import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.rom.RomControlOptions.Mirroring.*


class InesRomSpec extends Specification {
    @Unroll
    def "ROM Control Options Byte: #description"(){
        given: 'a header containing a control options byte'
        final InesRom rom = InesRom.from(asPaddedHeader([0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0, controlOptionsByte] as int[]))

        when: 'that parsed header is retrieved'
        final InesRomHeader header = rom.getHeader()
        final RomControlOptions romCtrlOptions = header.getRomControlOptions()

        then: 'it is in the expected state'
        romCtrlOptions.ramPresent == isRamPresent
        romCtrlOptions.trainerPresent == isTrainerPresent
        romCtrlOptions.getMirroring() == mirroring

        where:
        controlOptionsByte || isRamPresent | isTrainerPresent | mirroring   | description
        0b11111111         || true         | true             | FOUR_SCREEN | "All bits switched on"
        0b11110111         || true         | true             | VERTICAL    | "Four screen set to off, VERTICAL on"
        0b11110110         || true         | true             | HORIZONTAL  | "Four screen set to off, VERTICAL off"
        0b11111101         || false        | true             | FOUR_SCREEN | "No ram present"
        0b11111011         || true         | false            | FOUR_SCREEN | "No trainer present"
        0b11111001         || false        | false            | FOUR_SCREEN | "No trainer or ram present"
        0b00000000         || false        | false            | HORIZONTAL  | "All bits switched oFF"

    }

    private int[] asPaddedHeader(int[] values){
        int[] header = new int[InesRomHeader.HEADER_SIZE];
        System.arraycopy(values, 0, header, 0, values.length);
        return header;
    }
}