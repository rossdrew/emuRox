#EmuRox

At the moment, it's invisioned as an emulator for the 6502 processor in order to fit into a NES emulator which this is the first stage of development for.

Straight away Java doesn't feel like the right choice but it's a good learning excercise. I may switch language later.

-----

###Implemented

 - Immediate ADC instruction
 
###Notes

 I've kept the PC as two separate registers.  It makes it a little tougher to deal with but more authentic. 

-----

###Problems

######Javas unsigned byte problem.
Java bytes are signed, meaning it's a pain to deal with them, instead we have to use ints to represent bytes.