package com.rox.emu.rom

import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.rom.RomControlOptions.Mirroring.*


class InesRomSpec extends Specification {
    @Unroll
    def "ROM Control Options: #mapperNo #description"(){
        given: 'a header containing a control options byte'
        final InesRom rom = InesRom.from(asPaddedHeader([0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0, controlOptionsByte] as int[]))

        when: 'that parsed header is retrieved'
        final InesRomHeader header = rom.getHeader()
        final RomControlOptions romCtrlOptions = header.getRomControlOptions()

        then: 'it is in the expected state specified by flags6'
        romCtrlOptions.ramPresent == isRamPresent
        romCtrlOptions.trainerPresent == isTrainerPresent
        romCtrlOptions.getMirroring() == mirroring

        and: 'is in the expected state specified by flags7'
        !romCtrlOptions.isPlayChoice10()
        !romCtrlOptions.isVsUnisystem()

        and: 'the combination of flags are correct'
        romCtrlOptions.mapperNumber == mapperNo

        where:
        controlOptionsByte || isRamPresent | isTrainerPresent | mirroring   | mapperNo | description
        0b00000000         || false        | false            | HORIZONTAL  | 0b0000   | "All bits switched OFF"
        0b00011111         || true         | true             | FOUR_SCREEN | 0b0001   | "All bits switched on"
        0b00100111         || true         | true             | VERTICAL    | 0b0010   | "Four screen set to off, VERTICAL on"
        0b00110110         || true         | true             | HORIZONTAL  | 0b0011   | "Four screen set to off, VERTICAL off"
        0b01001101         || false        | true             | FOUR_SCREEN | 0b0100   | "No ram present"
        0b01011011         || true         | false            | FOUR_SCREEN | 0b0101   | "No trainer present"
        0b01101001         || false        | false            | FOUR_SCREEN | 0b0110   | "No trainer or ram present"
    }

    private int[] asPaddedHeader(int[] values){
        int[] header = new int[InesRomHeader.HEADER_SIZE]
        System.arraycopy(values, 0, header, 0, values.length)
        return header
    }
}