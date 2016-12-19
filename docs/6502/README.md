#2506 Documentation

 Resources:
   - [Online 6502 Emulator](https://skilldrick.github.io/easy6502/)
   - [Overflow Flag explained mathematically](http://www.righto.com/2012/12/the-6502-overflow-flag-explained.html)
   - [In-depth analysis of opcodes](http://www.llx.com/~nparker/a2/opcodes.html)

----
####Status Flags
    
    NVss DIZC
    |||| ||||
    |||| |||`- Carry: 1 if last addition or shift resulted in a carry, or if last subtraction resulted in no borrow
    |||| ||`-- Zero: 1 if last operation resulted in a 0 value
    |||| |`--- Interrupt: Interrupt inhibit
    |||| |       (0: /IRQ and /NMI get through; 1: only /NMI gets through)
    |||| `---- Decimal: 1 to make ADC and SBC use binary-coded decimal arithmetic
    ||||         (ignored on second-source 6502 like that in the NES)
    ||``------ s: No effect, used by the stack copy, see note below
    |`-------- Overflow: 1 if last ADC or SBC resulted in signed overflow, or D6 from last BIT
    `--------- Negative: Set to bit 7 of the last operation

----
#### OpCodes

![6502 OpCodes Page 1](https://github.com/rossdrew/emuRox/blob/master/docs/6502/img/6502_ISR_first_page.gif "6502 OpCodes Page 1")
![6502 OpCodes Page 2](https://github.com/rossdrew/emuRox/blob/master/docs/6502/img/6502_ISR_second_page.gif "6502 OpCodes Page 2")

----

#### Block Diagram

![6502 Block Diagram](https://github.com/rossdrew/emuRox/blob/master/docs/6502/img/6502_block_diagram.jpg "6502 Block Diagram")