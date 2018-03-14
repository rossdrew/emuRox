package com.rox.emu.rom;

/**
 <table>
  <tr>
    <th colspan=9>Bit</th>
    <th>Description</th>
  </tr>

 <tr><td colspan=10>[Flag 6]</td></tr>

  <tr>
     <th>7</th>
     <th>6</th>
     <th>5</th>
     <th>4</th>
     <th>3</th>
     <th>2</th>
     <th>1</th>
     <th>0</th>
     <th></th>
     <th> </th>
  </tr>

 <tr>
     <td style="border-right: 2px">|</td>
     <td style="border-right: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px"> Mirroring: 0: horizontal (vertical arrangement) (CIRAM A10 = PPU A11), 1: vertical (horizontal arrangement) (CIRAM A10 = PPU A10)</td>
 </tr>

 <tr>
     <td style="border-right: 2px">|</td>
     <td style="border-right: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px"> 1: Cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory</td>
 </tr>

 <tr>
     <td style="border-right: 2px">|</td>
     <td style="border-right: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px"> 1: 512-byte trainer at $7000-$71FF (stored before PRG data)</td>
 </tr>

 <tr>
     <td style="border-right: 2px">|</td>
     <td style="border-right: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">|</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px"> 1: Ignore mirroring control or above mirroring bit; instead provide four-screen VRAM</td>
 </tr>

 <tr>
     <td style="border-right: 2px">|_</td>
     <td style="border-right: 2px">|_</td>
     <td style="border-bottom: 2px">|_</td>
     <td style="border-bottom: 2px">|_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px">_</td>
     <td style="border-bottom: 2px"> Lower nybble of mapper number</td>
 </tr>

 <tr><td colspan=10>[Flag 7]</td></tr>

 <tr>
 <th>7</th>
 <th>6</th>
 <th>5</th>
 <th>4</th>
 <th>3</th>
 <th>2</th>
 <th>1</th>
 <th>0</th>
 <th></th>
 <th> </th>
 </tr>

 <tr>
 <td style="border-right: 2px">|</td>
 <td style="border-right: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px"> VS Unisystem</td>
 </tr>

 <tr>
 <td style="border-right: 2px">|</td>
 <td style="border-right: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px"> PlayChoice-10 (8KB of Hint Screen data stored after CHR data)</td>
 </tr>

 <tr>
 <td style="border-right: 2px">|</td>
 <td style="border-right: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|</td>
 <td style="border-bottom: 2px">|_</td>
 <td style="border-bottom: 2px">|_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px"> If equal to 2, flags 8-15 are in NES 2.0 format</td>
 </tr>

 <tr>
 <td style="border-right: 2px">|_</td>
 <td style="border-right: 2px">|_</td>
 <td style="border-bottom: 2px">|_</td>
 <td style="border-bottom: 2px">|_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px">_</td>
 <td style="border-bottom: 2px"> Upper nybble of mapper number</td>
 </tr>


 </table>
 */
class RomControlOptions {
    /**
     * <h3>Nametable mirroring</h3>
     * Defines the effect of accessing memory off the right or bottom edges of the current nametable.
     * When enabled on an axis, addresses wrap around.
     */
    enum Mirroring {
        VERTICAL,
        HORIZONTAL,
        FOUR_SCREEN
    }

    private final Mirroring mirroring;
    private final boolean ramPresent;
    private final boolean trainerPresent;
    private final int mapperNumber;

    private final boolean vsUnisystem;
    private final boolean playChoice10;
    private final int version;

    public RomControlOptions(final int flagByte6, final int flagByte7){
        int mapperNumberLo;
        int mapperNumberHi;

        mirroring = extractMirroring(flagByte6);
        ramPresent = (flagByte6 & 0b00000010) != 0;
        trainerPresent = (flagByte6 & 0b00000100) != 0;
        mapperNumberLo = (flagByte6 & 0b11110000) >> 4;

        vsUnisystem = (flagByte7 & 0b00000001) != 0;
        playChoice10 = (flagByte7 & 0b00000010) != 0;
        version = ((flagByte7 & 0b00001100) >> 2) == 2 ? 2 : 1;
        mapperNumberHi = (flagByte7 & 0b11110000);

        mapperNumber = mapperNumberLo | mapperNumberHi;
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

    public boolean isPlayChoice10() {
        return playChoice10;
    }

    public boolean isVsUnisystem() {
        return vsUnisystem;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "RomControlOptions{" +
                "mirroring=" + mirroring +
                ", ramPresent=" + ramPresent +
                ", trainerPresent=" + trainerPresent +
                ", mapperNumber=" + mapperNumber +
                ", vsUnisystem=" + vsUnisystem +
                ", playChoice10=" + playChoice10 +
                ", version=" + version +
                '}';
    }
}
