[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://opensource.org/licenses/MIT) 
[![Build Status](https://travis-ci.org/rossdrew/emuRox.svg?branch=master)](https://travis-ci.org/rossdrew/emuRox)
[![Codecov](https://codecov.io/gh/rossdrew/emuRox/branch/master/graph/badge.svg)](https://codecov.io/gh/rossdrew/emuRox)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/519fed1cf9c64216a0c9992eed25a36f)](https://www.codacy.com/app/rossdrew/emuRox?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rossdrew/emuRox&amp;utm_campaign=Badge_Grade)
 
#EmuRox

At the moment, it's invisioned as an emulator for the 6502 processor in order to fit into a NES emulator which this is the first stage of development for.

##Progress

#### 6502

Most opcodes now implemented.  `BRK` and `RTI` left (ignoring ROR till later). In order to finish those, I'll need to implement proper interrupts so that they can be returned from.
Still working on __Indirect, Y__ addressing and a couple __Indirect, X__ addressed instructions.
Timing and clock ticks will be looked at later, as necessary.
 
###Development & Testing

 The plan was to develop this, a larger personal project, in a TDD (Test Driven Development) centric way.  This means writing failing tests that describe functionality then writing that functionality to make the tests pass and iteratively writing a complete application.
 This larger personal project that you are now looking at I am using to test out other technologies and resources.  Here is a list of those that I am using or have used along the way:-
 
 - [Trello](https://trello.com) for work breakdown, starting with all tasks to get a functioning 6502 with some added work for a [compiler](https://github.com/rossdrew/emuRox/commits/assembler) and [Debugger](https://github.com/rossdrew/emuRox/tree/master/src/main/java/com/rox/emu/P6502/dbg).
 - [Java](https://www.java.com/)/[JUnit](http://junit.org/junit4/) for basic functionality tests.  
 - [junit-quickcheck](https://www.google.co.uk/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0ahUKEwjq4-PF-aPSAhWHDsAKHV17BCIQFggaMAA&url=https%3A%2F%2Fgithub.com%2Fpholser%2Fjunit-quickcheck&usg=AFQjCNE37M0yEi68OG8Hr7y1MDoJwcLOaQ&sig2=AUpnbmKM5Sk9efhw1r-bKw&bvm=bv.147448319,d.d2s) for property-based testing.
 - [Groovy](http://www.groovy-lang.org/)/[Spock](http://spockframework.org/) for [data-driven tests](https://en.wikipedia.org/wiki/Data-driven_testing) .  For covering various variations of class creation and method use in a clear, concise way.  GitHub shield using [TravisCI](https://travis-ci.org/).
 - [Pitest](http://pitest.org/) reports. To allow us to use [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) to validate and improve our ([Spock](http://spockframework.org/) & [JUnit](http://junit.org/junit4/)) unit tests.
 - [JaCoCo](http://www.eclemma.org/jacoco/) reports. To allow us to strive for high [code coverage](https://en.wikipedia.org/wiki/Code_coverage).  Mainly for the GitHub shield using [CodeCov](https://codecov.io). 
 - Static analysis done in my development environment on [IntelliJ IDEA](https://www.jetbrains.com/idea/) and online (including GitHub shield) by [Codeacy](https://www.codacy.com/). 

 
####Branches

 - [Main](https://github.com/rossdrew/emuRox/commits/master) branch, pushing towards a fully working 6502
 - A [6502 compiler](https://github.com/rossdrew/emuRox/commits/assembler) branch so that more complicated pieces of code can be written, understood, edited and annotated easier.
 - A [Concourse](https://concourse.ci/) CI [branch](https://github.com/rossdrew/emuRox/commits/concourse-ci), to get that working and learn a little something along the way

-----


###Problems

######6502

######Javas unsigned byte problem. 
 - Java bytes are signed, meaning it's a pain to deal with them, instead we have to use ints to represent bytes.
 - `System.out` is confusing Pitest, need to invest some time in moving to a loggin framework
 
###Sources
 - [JaCoCo](http://www.eclemma.org/jacoco/), [Was missing Spock tests](http://stackoverflow.com/questions/41652981/why-does-jacoco-ignore-myspock-tests-yet-sees-my-junit-tests), thanks [Godin](http://stackoverflow.com/users/244993/godin) of Stack Overflow
 - [JaCoCo](http://www.eclemma.org/jacoco/), has a problem with code coverage of `String` based `switch` statements. In that it reports missing coverage where there is none. Thanks again thanks [Godin](http://stackoverflow.com/users/244993/godin) of Stack Overflow for your [explanation](http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements)
 - The [6502 Programming](https://www.facebook.com/groups/6502CPU/) Facebook group has been invaluable in resolving small questions 
 - I'm planning on reaching out to communities for code reviews, another pair of eyes is always helpful:-
    - [Reddit](https://www.reddit.com/r/reviewmycode/comments/5oorz1/java_6502_emulator/)
    - [Stack Exchange](http://codereview.stackexchange.com/questions/154600/op-code-decoding-in-an-emulator) 
 
