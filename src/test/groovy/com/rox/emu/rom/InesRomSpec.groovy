package com.rox.emu.rom

import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.rom.RomControlOptions.Mirroring.*


class InesRomSpec extends Specification {
    @Unroll
    def "ROM Control Options (Flag 6): #mapperNo #description"(){
        given: 'a header containing a control options byte'
        final InesRom rom = InesRom.from(asPaddedHeader([0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0, controlOptionsByte] as byte[]))

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

    @Unroll
    def "ROM Control Options (Flag 7): #description"(){
        given: 'a header containing a control options byte'
        final InesRom rom = InesRom.from(asPaddedHeader([0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0, 0b00000000, controlOptionsByte] as byte[]))

        when: 'that parsed header is retrieved'
        final InesRomHeader header = rom.getHeader()
        final RomControlOptions romCtrlOptions = header.getRomControlOptions()

        then: 'is in the expected state specified by flags7'
        romCtrlOptions.isPlayChoice10() == isPlayChoice10
        romCtrlOptions.isVsUnisystem() == isVsUnisystem
        romCtrlOptions.mapperNumber == mapperNo
        romCtrlOptions.version == version

        and: 'it is in the expected state specified by flags6'
        !romCtrlOptions.ramPresent
        !romCtrlOptions.trainerPresent
        romCtrlOptions.getMirroring() == HORIZONTAL

        and: 'the combination of flags are correct'
        romCtrlOptions.mapperNumber == mapperNo

        where:
        controlOptionsByte || isPlayChoice10 | isVsUnisystem | mapperNo   | version | description
        0b00000000         || false          | false         | 0b00000000 | 1       | "All bits switched OFF"
        0b00010000         || false          | false         | 0b00010000 | 1       | "Non-zero mapper number"
        0b00100001         || false          | true          | 0b00100000 | 1       | "Vs System"
        0b00110010         || true           | false         | 0b00110000 | 1       | "Play Choice 10"
        0b01000000         || false          | false         | 0b01000000 | 1       | "iNES Version 1"
        0b01011000         || false          | false         | 0b01010000 | 2       | "iNES Version 2"
    }

    @Unroll
    def "ROM Control Options (Mapper Number): #mapperNo #description"(){
        given: 'a header containing a control options byte'
        final InesRom rom = InesRom.from(asPaddedHeader([0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0, flag6Byte, flag7Byte] as byte[]))

        when: 'that parsed header is retrieved'
        final InesRomHeader header = rom.getHeader()
        final RomControlOptions romCtrlOptions = header.getRomControlOptions()

        then: 'the combination of flags are correct'
        romCtrlOptions.mapperNumber == mapperNo

        where:
        flag6Byte  | flag7Byte  || mapperNo   | description
        0b00000000 | 0b00000000 || 0b00000000 | "All bits switched OFF"
        0b00001111 | 0b00001111 || 0b00000000 | "All bits OFF for mapper number only"
        0b00010000 | 0b00000000 || 0b00000001 | "Low byte is used correctly"
        0b00000000 | 0b00010000 || 0b00010000 | "High byte is used correctly"
        0b00010000 | 0b00010000 || 0b00010001 | "High & Low byte are combined correctly"
        0b01010000 | 0b00110000 || 0b00110101 | "Non simple combination of high and low bytes"
    }

    private byte[] asPaddedHeader(byte[] values){
        byte[] header = new byte[InesRomHeader.HEADER_SIZE]
        System.arraycopy(values, 0, header, 0, values.length)
        return header
    }
}