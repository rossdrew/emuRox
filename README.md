[![Build Status](https://travis-ci.org/rossdrew/emuRox.svg?branch=master)](https://travis-ci.org/rossdrew/emuRox)
[![codecov](https://codecov.io/gh/rossdrew/emuRox/branch/master/graph/badge.svg)](https://codecov.io/gh/rossdrew/emuRox)
 
#EmuRox

At the moment, it's invisioned as an emulator for the 6502 processor in order to fit into a NES emulator which this is the first stage of development for.

Straight away Java doesn't feel like the right choice but it's a good learning exercise. I may switch language later.
 
###Testing

 The testing strategy uses 4 different technologies:- 
 
 - [Java](https://www.java.com/)/[JUnit](http://junit.org/junit4/) for basic functionality tests.  Can classes be created, initialised and called.
 - [Groovy](http://www.groovy-lang.org/)/[Spock](http://spockframework.org/) for [data-driven tests](https://en.wikipedia.org/wiki/Data-driven_testing) .  For covering various variations of class creation and method use in a clear, concise way.
 - [JaCoCo](http://www.eclemma.org/jacoco/) reports. To allow us to strive for high [code coverage](https://en.wikipedia.org/wiki/Code_coverage) .
 - [Pitest](http://pitest.org/) reports. To allow us to use [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) to validate and improve our ([Spock](http://spockframework.org/) & [JUnit](http://junit.org/junit4/)) unit tests.
 
 
###2506

 Resources:
   - [Online 6502 Emulator](https://skilldrick.github.io/easy6502/)
   - [Overflow Flag explained mathematically](http://www.righto.com/2012/12/the-6502-overflow-flag-explained.html)
   - [In-depth analysis of opcodes](http://www.llx.com/~nparker/a2/opcodes.html)

####OpCodes

![6502 OpCodes Page 1](https://github.com/rossdrew/emuRox/blob/master/img/6502_ISR_first_page.gif "6502 OpCodes Page 1")
![6502 OpCodes Page 2](https://github.com/rossdrew/emuRox/blob/master/img/6502_ISR_second_page.gif "6502 OpCodes Page 2")

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


###Implemented

 - Immediate LDC (N, Z)
 - Immediate ADC (N, Z, O, C)
 - Immediate AND/OR/EOR (N, Z) 
 
###Notes

 - I've kept the PC as two separate registers.  It makes it a little tougher to deal with but more authentic.

-----

###Problems

######Javas unsigned byte problem. 
 - Java bytes are signed, meaning it's a pain to deal with them, instead we have to use ints to represent bytes.
 - System.out is confusing Pitest, need to invest some time in moving to a loggin framework