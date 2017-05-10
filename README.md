[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://opensource.org/licenses/MIT) 
[![Build Status](https://travis-ci.org/rossdrew/emuRox.svg?branch=master)](https://travis-ci.org/rossdrew/emuRox)
[![Codecov](https://codecov.io/gh/rossdrew/emuRox/branch/master/graph/badge.svg)](https://codecov.io/gh/rossdrew/emuRox)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/519fed1cf9c64216a0c9992eed25a36f)](https://www.codacy.com/app/rossdrew/emuRox?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rossdrew/emuRox&amp;utm_campaign=Badge_Grade)
 
# EmuRox

An emulator for the 6502 processor.  The plan is to evolve this into a working NES emulator so I'm developing the 6502 without BCD mode for now.  If/When I get that working, I'll expand the 6502 and move onto another 6502 system (Atari 2600, Commodore 64...) then perhaps add processors and more emulated hardware, in theory, creating a pluggable multi-emulator.

## Progress

With a first version of the 6502 processor (including a graphical debugger and compiler) written, currently I'm looking in to the NES PPU.  Trying to garner enough information to start emulating this piece of hardware while looking at code I've wrote over the last few months and looking at where it might be improved.  For example:-
 
  - getting rid of java performed arithmetic in favor of ALU performed arithmetic, it's causing problems...as expected
  - compressing the data driven tests;  extracting out common patterns, etc

#### 6502

All op-codes now implemented. Some changing of arithmetic (usually in `+`/`-` cases) to twos compliment arithmetic.  Some other bugs needing worked out.
Timing will be looked at later, as necessary.  BCD wont be needed for now for the NES (Ricoh) version and I'll need to look at the memory mapped registers.
 
## Development & Testing

 The plan was to develop this, a larger personal project, in a TDD (Test Driven Development) centric way.  This means writing failing tests that describe functionality then writing that functionality to make the tests pass and iteratively writing a complete application.
 This larger personal project that you are now looking at I am using to test out other technologies and resources.  Here is a list of those that I am using or have used along the way:-
 
 - [Trello](https://trello.com) for work breakdown, starting with all tasks to get a functioning 6502 with some added work for a [compiler](https://github.com/rossdrew/emuRox/commits/assembler) and [Debugger](https://github.com/rossdrew/emuRox/tree/master/src/main/java/com/rox/emu/P6502/dbg).
 - [Java](https://www.java.com/)/[JUnit](http://junit.org/junit4/) for basic functionality tests.  
 - [junit-quickcheck](https://www.google.co.uk/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0ahUKEwjq4-PF-aPSAhWHDsAKHV17BCIQFggaMAA&url=https%3A%2F%2Fgithub.com%2Fpholser%2Fjunit-quickcheck&usg=AFQjCNE37M0yEi68OG8Hr7y1MDoJwcLOaQ&sig2=AUpnbmKM5Sk9efhw1r-bKw&bvm=bv.147448319,d.d2s) for property-based testing.
 - [Groovy](http://www.groovy-lang.org/)/[Spock](http://spockframework.org/) for [data-driven tests](https://en.wikipedia.org/wiki/Data-driven_testing) .  For covering various variations of class creation and method use in a clear, concise way.  GitHub shield using [TravisCI](https://travis-ci.org/).
 - [Pitest](http://pitest.org/) reports. To allow us to use [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) to validate and improve our ([Spock](http://spockframework.org/) & [JUnit](http://junit.org/junit4/)) unit tests.
 - [JaCoCo](http://www.eclemma.org/jacoco/) reports. To allow us to strive for high [code coverage](https://en.wikipedia.org/wiki/Code_coverage).  Mainly for the GitHub shield using [CodeCov](https://codecov.io). 
 - Static analysis done in my development environment on [IntelliJ IDEA](https://www.jetbrains.com/idea/) and online (including GitHub shield) by [Codeacy](https://www.codacy.com/). 

 
#### Branches

 - [Main](https://github.com/rossdrew/emuRox/commits/master) branch, pushing towards a fully working 6502
 - A [Concourse](https://concourse.ci/) CI [branch](https://github.com/rossdrew/emuRox/commits/concourse-ci), to get that working and learn a little something along the way

-----

### Problems

###### 6502

###### Javas unsigned byte problem. 
 - Java bytes are signed, meaning it's a pain to deal with them, instead we have to use ints to represent bytes.
 - `System.out` is confusing Pitest, need to invest some time in moving to a loggin framework
 - [JaCoCo](http://www.eclemma.org/jacoco/) doesn't report coverage of `String` based `switch` statements [well](http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements)
 
### Sources
 - StackOverflow as always has been a huge help, specifically [Godin](http://stackoverflow.com/users/244993/godin), also a developer of [JaCoCo](http://www.eclemma.org/jacoco/) for his help.  eg. [here](http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements) & [here](http://stackoverflow.com/questions/41652981/why-does-jacoco-ignore-myspock-tests-yet-sees-my-junit-tests) 
 - The [6502 Programming](https://www.facebook.com/groups/6502CPU/) Facebook group has been invaluable in resolving very specific questions 
 - I'm planning on reaching out to communities for code reviews (your comments are always welcome btw), another pair of eyes is always helpful, although so far these have proven to be unfruitful:-
    - [Reddit](https://www.reddit.com/r/reviewmycode/comments/5oorz1/java_6502_emulator/)
    - [Stack Exchange](http://codereview.stackexchange.com/questions/154600/op-code-decoding-in-an-emulator) 
 
