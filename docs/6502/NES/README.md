#Nintento Entertainment System

This was the original intention of this project, to become a NES emulator.  As such I've added this directory as a source for storing information I may need later when I get beyong the CPU.

##6502

####Unoficial OpCodes

There are [missing opCodes](http://wiki.nesdev.com/w/index.php/CPU_unofficial_opcodes), some of them crash the CPU, some have [strange effects](http://wiki.nesdev.com/w/index.php/Programming_with_unofficial_opcodes) and in order to get every game working, here are some examples

 __Late/Unlicensed Games__
    - Puzznic (all regions) (US release November 1990) uses $89, which is a 2-byte NOP on 6502.
    - F-117A Stealth Fighter and Infiltrator also use $89.
    - Beauty and the Beast (E) (1994) uses a different 2-byte NOP ($80).[1]
    - Dynowarz uses 1-byte NOPs $DA and $FA on the first level when your dino throws his fist.
    - Super Cars (U) (February 1991) uses LAX ($B3)
    - Disney's Aladdin (E) (December 1994) uses SLO ($07). This is Virgin's port of the Game Boy game, itself a port of the Genesis game, not any of the pirate originals.
    
 __Hobbyist Games__
    - The MUSE music engine, used in Driar and STREEMERZ: Super Strength Emergency Squad Zeta, uses the unofficial opcodes $8F (SAX), $B3 (LAX), and $CB (AXS) [2]
    - Attribute Zone uses $0B (ANC), $2F (RLA), $4B (ALR), $A7 (LAX), $B3 (LAX), $CB (AXS), $D3 (DCP), $DB (DCP).
    - The port of Zork to the Famicom uses a few unofficial opcodes.
    
##PPU

 - [How NES Graphics work - The Basics](http://web.textfiles.com/games/nesgfx.txt)
 - [Emulating the Nintendo Entertainment System (NES) -  Rupert Shuttleworth](http://www.optimuscopri.me/nes/report.pdf)
 - [PPU Rendering](https://wiki.nesdev.com/w/index.php/PPU_rendering)
 - NES Graphics Series[One](http://www.dustmop.io/blog/2015/04/28/nes-graphics-part-1/), [Two](http://www.dustmop.io/blog/2015/06/08/nes-graphics-part-2/) and [Three](http://www.dustmop.io/blog/2015/12/18/nes-graphics-part-3/)

######Notes

 - Single scrollable playfield, 
 - 64 objects/sprites. 
    - 1 or 2 tiles to be displayed on a randomly-accessable xy coordinate
 - The screen resolution is 256*240
    - Made up of 32x30, 8x8 tiles
 - 2*4 color palettes each with 3 indexable colors
    - Playfield and objects have their own dedicated palettes
