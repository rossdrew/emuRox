# EmuRox Code Organisation

The code is organised quite simply; hopefully. The `main` directory, like in most Java projects these days, contains the source code.  The `test` will mirror that, with unit tests and such.
 
Inside the `main` directory we have the emulator base, which is `com.rox.emu`.  Inside these we have sections for each logically seperable element of the emulator library:-

## Env

Domain objects, such as the custom `RoxByte` for representing a byte throughout the system in a way that isn't as ugly as everything else you have to do in Java to get byte functionality.

## Mem

Memory used by the system, at this point only containing a `SimpleMemory` implementation of the `Memory` interface which acts as a memory block, with getting and setting of bytes.

## Processor

Individual processor emulations.  In our case, the MOS6502 (although more like the Ricoh2A03) and the Ricoh2C02 which are the CPU and PPU of an NES respectively.
Each processor will then have it's own logically seperable domain and component classes

##### Op

Op code representation inside this processor

##### Util

Any utils used in the domain of this processor, such as compilers, representations of programsm etc.

##### Dbg

Debug tools for this processor.   Such as a debug UI

## Rom

ROM (or Read Only Memory) files that different systems may use.  Such as the NES game ROMs.
