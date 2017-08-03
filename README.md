[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://opensource.org/licenses/MIT) 
[![Build Status](https://travis-ci.org/rossdrew/emuRox.svg?branch=master)](https://travis-ci.org/rossdrew/emuRox)
[![Codecov](https://codecov.io/gh/rossdrew/emuRox/branch/master/graph/badge.svg)](https://codecov.io/gh/rossdrew/emuRox)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/519fed1cf9c64216a0c9992eed25a36f)](https://www.codacy.com/app/rossdrew/emuRox?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rossdrew/emuRox&amp;utm_campaign=Badge_Grade)
[![Beerpay](https://beerpay.io/rossdrew/emuRox/badge.svg?style=beer-square)](https://beerpay.io/rossdrew/emuRox)
[![Beerpay](https://beerpay.io/rossdrew/emuRox/make-wish.svg?style=flat-square)](https://beerpay.io/rossdrew/emuRox?focus=wish)

# EmuRox ![Roxoft](https://github.com/rossdrew/rossdrew.github.io/blob/master/src/img/roxoft_small.gif)

An emulator for the [6502 processor](https://en.wikipedia.org/wiki/MOS_Technology_6502).  The plan is to evolve this into a working [NES](https://en.wikipedia.org/wiki/Nintendo_Entertainment_System) emulator so I'm developing the 6502 without BCD mode for now.  If/When I get that working, I'll expand the 6502 and move onto another 6502 system (Atari 2600, Commodore 64...) then perhaps add processors and more emulated hardware, in theory, creating a pluggable multi-emulator.

## 6502 and progress towards a working NES...

After nearly 7 months of close to daily work on this, development is on a little bit of a hold.  I need some time to do proper investigation of the PPU and then some more to do some proper exploratory coding and proper design which my work/personal life isn't allowing at this moment. Should be too long before I'm back bashing at it.

Any small chunks of time I get, I'm looking into improving on what's already written.  For example:-
 
  - getting rid of java performed arithmetic (usually in `+`/`-` cases) in favor of ALU performed arithmetic, it's causing problems...as expected
  - compressing the data driven tests;  extracting out common patterns, etc
  - abstracting away bytes into a custom class so that I don't need workarounds for Java types
  - abstracting away the concept of a Register into an enum to clean up the code
  - making the 6502 emulator, compiler and debugger available to 6502 community for testing.  This means getting labels to work, which means a refactor of Program, i.e.
                                                                                                                                                                   
```6502 Assembly
LDA #$1
STA $3C
TESTLABEL:
INC $3C
LDA #$0
BCC TESTLABEL
```
  
BCD and proper timing is a thing for another day as my first objective for now, is the NES emulator.

## Development & Testing

 The plan was to develop this, a larger personal project, in a TDD (Test Driven Development) centric way.  This means writing failing tests that describe functionality then writing that functionality to make the tests pass and iteratively writing a complete application.
 This larger personal project that you are now looking at I am using to test out other technologies and resources.  Here is a list of those that I am using or have used along the way:-
 
 - [Trello](https://trello.com) for work breakdown and project management.
 - [Java](https://www.java.com/)/[JUnit](http://junit.org/junit4/) for basic functionality tests.  
 - [junit-quickcheck](https://www.google.co.uk/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0ahUKEwjq4-PF-aPSAhWHDsAKHV17BCIQFggaMAA&url=https%3A%2F%2Fgithub.com%2Fpholser%2Fjunit-quickcheck&usg=AFQjCNE37M0yEi68OG8Hr7y1MDoJwcLOaQ&sig2=AUpnbmKM5Sk9efhw1r-bKw&bvm=bv.147448319,d.d2s) for property-based testing.
 - [Groovy](http://www.groovy-lang.org/)/[Spock](http://spockframework.org/) for [data-driven tests](https://en.wikipedia.org/wiki/Data-driven_testing) .  For covering various variations of class creation and method use in a clear, concise way.  GitHub shield using [TravisCI](https://travis-ci.org/).
 - [Pitest](http://pitest.org/) reports. To allow us to use [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) to validate and improve our ([Spock](http://spockframework.org/) & [JUnit](http://junit.org/junit4/)) unit tests.
 - [JaCoCo](http://www.eclemma.org/jacoco/) reports. To allow us to strive for high [code coverage](https://en.wikipedia.org/wiki/Code_coverage).  Mainly for the GitHub shield using [CodeCov](https://codecov.io). 
 - Static analysis done in my development environment on [IntelliJ IDEA](https://www.jetbrains.com/idea/) and online (including GitHub shield) by [Codeacy](https://www.codacy.com/). 

 
#### Branches

 - [Main](https://github.com/rossdrew/emuRox/commits/master) branch, now pushing towards a working NES PPU, then a working NES
 - A [Concourse](https://concourse.ci/) CI [branch](https://github.com/rossdrew/emuRox/commits/concourse-ci), to get that working and learn a little something along the way
 - A [RoxByte](https://github.com/rossdrew/emuRox/blob/migration-roxbyte/src/main/java/com/rox/emu/env/RoxByte.java) migration [branch](https://github.com/rossdrew/emuRox/tree/migration-roxbyte) for attempting to move towards an abstracted "_byte_" and away from trying to warp Java types to do what we need.

-----

### Problems

###### Javas unsigned byte problem. 
 - Java bytes are signed, meaning it's a pain to deal with them.  Sometimes we want to deal with raw bytes such as memory read and writes, sometimes we want to have them signed and later in BCD mode.
 - [JaCoCo](http://www.eclemma.org/jacoco/) doesn't report coverage of `String` based `switch` statements [well](http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements)
 
### Sources
 - StackOverflow as always has been a huge help, specifically [Godin](http://stackoverflow.com/users/244993/godin), also a developer of [JaCoCo](http://www.eclemma.org/jacoco/) for his help.  eg. [here](http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements) & [here](http://stackoverflow.com/questions/41652981/why-does-jacoco-ignore-myspock-tests-yet-sees-my-junit-tests) 
 - The [6502 Programming](https://www.facebook.com/groups/6502CPU/) Facebook group has been invaluable in resolving very specific questions 
 - I'm planning on reaching out to communities for code reviews (your comments are always welcome btw), another pair of eyes is always helpful, although so far these have proven to be unfruitful:-
    - [Reddit](https://www.reddit.com/r/reviewmycode/comments/5oorz1/java_6502_emulator/)
    - [Stack Exchange](http://codereview.stackexchange.com/questions/154600/op-code-decoding-in-an-emulator) 
 

## Get involved 
Found my project interesting or used it in some way?  Contribute by showing your appreciation with a [frosty beverage](https://beerpay.io/rossdrew/emuRox).  In my case this will go towards an [Old Fashioned](https://www.instagram.com/p/BUzyDHOAzkI/?taken-by=ross_drew) but as far as I'm aware there is no Spirit/Bourbon/WhiskeyPay.
  
Or even [make a feature suggestion](https://beerpay.io/rossdrew/emuRox?focus=wish) if you are so inclined.

Lastly, feel free to volunteer solutions, fixes, improvements, pull requests, documentation, technology suggestions.  If you are a JavaFX/Swing guru, I'd love to see a better UI on this.  Just note that I'm trying to make this, as well as a working project, a highly tested, nice codebase to work on.
