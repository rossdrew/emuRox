[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://opensource.org/licenses/MIT) 
[![Code Coverage by Codecov](https://codecov.io/gh/rossdrew/emuRox/branch/master/graph/badge.svg)](https://codecov.io/gh/rossdrew/emuRox)
[![Mutation Coverage by Pitest](http://rossdrew.pythonanywhere.com/shield&rnd=1)](http://rossdrew.pythonanywhere.com/report)
[![GitHub Actions CI](https://github.com/rossdrew/emurox/workflows/GitHub%20Actions/badge.svg)](https://github.com/rossdrew/emurox/workflows/GitHub%20Actions/badge.svg)

<a href="https://github.com/rossdrew/emuRox/blob/master/docs/LogoONE.png">
 <img align="right" src="https://github.com/rossdrew/emuRox/blob/master/docs/LogoONE.png" data-canonical-src="https://github.com/rossdrew/emuRox/blob/master/docs/LogoONE.png" alt="EmuRox - Image by ChatGPT" width="150" />
</a>

# EmuRox
NOTE: While not dead, I've found it hard to dedicate time to this project over the last few years.  I'm currently working on a bit of a restructure making this cycle accurate in order to work well with an APU.  

Ostensibly, a Nintendo Entertainment System ([NES](https://en.wikipedia.org/wiki/Nintendo_Entertainment_System)) emulator with a view to encompassing other systems. Creating a pluggable multi-emulator. In actuality, it is not about getting a working emulator, for a NES or anything else. There are plenty of them out there.  It's about taking a complex problem and designing the best abstraction possible.  Allowing us to discuss complex ideas in simple language.

Too often I've:

 - had to sacrifice good implementation for fast functionality
 - dealt with code in which concision hampered readability
 - consumed development time in lengthy, bloated builds.  Usually caused by complex and brittle system testing
 - written under-specified features which need to be re-written over and over
 - work with naive domain knowledge due to time pressure
 - written solutions to problems I had no time to completely understand.

 > _Any fool can write code that a computer can understand. Good programmers write code that humans can understand. (M. Fowler)_

 I wanted something I could redesign over and over as my knowledge of the domain grew and as I felt the frustrations of having to reacquaint myself with pieces of code I hadn't seen in a while.  Something I could [test in a thousand different ways](https://github.com/rossdrew/emuRox/blob/master/src/test/README.md), then tweak and experiment with build processes so that the development cycle is rapid and fluid.  That I could tweak the code over and over to make it simpler and simpler to understand. 

There are also desirable side effects.  I wanted to write my own emulator, play the games of my childhood and know that I wrote the platform that they were running on and becoming even more familiar with the first pieces of software I fell in love with.  I wanted a large codebase I had complete control over and could try out tools and technologies.  I got to the stage of the latter, quite a while ago and I'm crawling towards the former.

## Usage

There is a debug UI for the MOS 6502 which provides a basic register and memory overview for running code through step by step in [DebuggerWindow.java](https://github.com/rossdrew/emuRox/blob/master/src/main/java/com/rox/emu/processor/mos6502/dbg/ui/DebuggerWindow.java). Run with the Gradle command `runDebugUI` and you'll get an interface that looks something like

![EmuRox Test GUI](https://github.com/rossdrew/emuRox/blob/master/docs/EMuRox%20GUI%20Demo%20BIG.gif)

On the left you'll see tabs of the first 5 memory pages organised in blocks of 4 with their locations displayed in red on the left.  The Program Counter location will be colored green.

In the center we have a choice of two tabs
 - Registers: Which display all registers and flags, their names in blue and representations of them as decimal and hexadecimal in red.
 - Code: A simple code window which can be used to compile 6502 assembly and load it into page 0 at location 0.

On the bottom we have a reset button which will reset everything to it's initial state and a step button which will issue the next instruction.

At the top we have the currently executing instruction and on the right a history of instructions both consisting of their location, arguments and a short description.  

## 6502 and progress towards a working NES...

I've seperated (at least) the NES section of this down into 7 stages:

 - [x] I blasted through the first phase due to the massive amount of documentation on the MOS 6502
 - [x] While researching phase 3, I churned through the relatively basic work of parsing iNES files of phase 2 (without edge cases for now)
 - [ ] Phase 3 is kinda where I'm stuck at the moment, struggling to find time to consolidate all the documentation and existing data on the APU, which is less abundant than of the MOS 6502 and far more messy
 - [ ] Phase 4 is going to be a decent challenge I think, where I need to start concentrating on timings
 - [ ] Phase 5 and 6 are bringing it all together into a usable system

So currenly large scale features are undergoing research and early design.  This also gives me time to improve and tweak what is there.


 <img aligm="right" src="https://github.com/rossdrew/emuRox/blob/master/docs/EmuRox%20Roadmap.png" data-canonical-src="https://github.com/rossdrew/emuRox/blob/master/docs/EmuRox%20Roadmap.png" alt="EmuRox Roadmap" width="900" />


## Development & Testing

<a href="https://codecov.io/gh/rossdrew/emuRox">
 <img align="right" src="https://codecov.io/gh/rossdrew/emuRox/graphs/sunburst.svg" data-canonical-src="https://codecov.io/gh/rossdrew/emuRox/graphs/sunburst.svg" alt="The current codecov.io coverage chart" width="150" />
</a>

The plan was to develop in a TDD (Test Driven Development) centric way, that is via Red-Green testing; i.e. writing failing/red tests that describe functionality then writing that functionality to make the tests pass/green and iteratively writing a complete application.
 
#### Current technologies

 _Technologies that I have used, liked and continued to use as part of the project..._

 - [Java](https://www.java.com/)/[JUnit](http://junit.org/junit4/) for basic functionality tests, adding mocks 
   - [Mockito](http://site.mockito.org/) to add more expressive syntax to JUnit, including features such as [Theories](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html).
   - [junit-quickcheck](https://github.com/pholser/junit-quickcheck) for [property-based testing](http://blog.jessitron.com/2013/04/property-based-testing-what-is-it.html) in Java.
   - [JUnit Theory Suite](https://github.com/richard-melvin/junit-theory-suite) for unrolling [JUnit](http://junit.org/junit4/) [Theories](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html).  This will unroll all data points combinatorially to a [Theory](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html)
 - [Groovy](http://www.groovy-lang.org/)/[Spock](http://spockframework.org/) for [data-driven tests](https://en.wikipedia.org/wiki/Data-driven_testing), adding mocks and more expressive syntax with [Hamcrest](http://hamcrest.org/).  For covering various variations of class creation and method use in a clear, concise way that JUnit struggles with.
   - [Spock Genesis](https://github.com/Bijnagte/spock-genesis) for [property-based testing](http://blog.jessitron.com/2013/04/property-based-testing-what-is-it.html) in Groovy.
 - [Pitest](http://pitest.org/) reports. To allow us to use [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) to validate and improve our ([Spock](http://spockframework.org/) & [JUnit](http://junit.org/junit4/)) unit tests.  
   - A self made, super simple [Python program](https://github.com/rossdrew/PitestCI/blob/master/pitestReport.py) to [generate a Pitest GitHub shield](https://rossdrew.github.io//pitest-ci/).
 - [TravisCI](https://travis-ci.org/) for [continuous integration](https://en.wikipedia.org/wiki/Continuous_integration) & build status GitHub Shield, making sure my build always builds and my tests always pass.
 - [JaCoCo](http://www.eclemma.org/jacoco/) reports. To allow us to strive for high [code coverage](https://en.wikipedia.org/wiki/Code_coverage).  Mainly for the GitHub shield using [CodeCov](https://codecov.io). 
 - Static analysis done in my development environment on [IntelliJ IDEA](https://www.jetbrains.com/idea/) and online (including GitHub shield) by [Codeacy](https://www.codacy.com/).
 - [Trello](https://trello.com/b/ZWcFxEu3/emurox) for work breakdown and project management.

#### Investigated technologies 

 _Technologies that I have used yet disliked or haven't saw a place for them in my project for whatever reason..._

 - [JParams toStringTester](https://github.com/jparams/to-string-tester) for `toString` testing.  It didn't seem to work very well, I filed an [issue](https://github.com/jparams/to-string-tester/issues/1) but the project has since been archived.
 - [JUnit QuickTheories](https://github.com/ncredinburgh/QuickTheories) for property based testing.  It's much more verbose for no added benefit over [junit-quickcheck](https://www.google.co.uk/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0ahUKEwjq4-PF-aPSAhWHDsAKHV17BCIQFggaMAA&url=https%3A%2F%2Fgithub.com%2Fpholser%2Fjunit-quickcheck&usg=AFQjCNE37M0yEi68OG8Hr7y1MDoJwcLOaQ&sig2=AUpnbmKM5Sk9efhw1r-bKw&bvm=bv.147448319,d.d2s)
 - [Spockito](https://github.com/tools4j/spockito) to give the ability to unroll [JUnit](http://junit.org/junit4/) [Theories](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html) like in [Spock](http://spockframework.org/) data-driven tests.  I'm not a big fan of the syntax and attaches `@DataPoint`s to the methods.  It would be great for adding data-driven tests if you were limited to [JUnit](http://junit.org/junit4/), however
 - [Kotlin](https://kotlinlang.org/) for testing and various testing frameworks like [Spek](https://github.com/spekframework/spek) and [kotlintest](https://github.com/kotlintest/kotlintest).  It offers nothing I don't already have and property/data-driven/theory testing is pretty ugly and/or weak.
 - [Beads Project](https://github.com/orsjb/beads) currently investigating for audio synthesis until I have the knowledge to roll my own.  The library is so small, focused and nice to use I may just keep it.  Unfortunately it's not distributed so it needs to be manually included.

-----

### Problems

###### Javas unsigned byte problem. 
 Java bytes are signed, meaning it's a pain to deal with them.  Sometimes we want to deal with raw bytes such as memory read and writes, sometimes we want to have them signed and later in BCD mode.
###### JaCoCo coverage in String based switch 
 [JaCoCo](http://www.eclemma.org/jacoco/) doesn't report coverage of `String` based `switch` statements [well](http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements)
 
### Sources
 - StackOverflow as always has been a huge help, specifically [Godin](http://stackoverflow.com/users/244993/godin), also a developer of [JaCoCo](http://www.eclemma.org/jacoco/) for his help.  eg. [here](http://stackoverflow.com/questions/42642840/why-is-jacoco-not-covering-my-switch-statements) & [here](http://stackoverflow.com/questions/41652981/why-does-jacoco-ignore-myspock-tests-yet-sees-my-junit-tests) 
 - The [6502 Programming](https://www.facebook.com/groups/6502CPU/) Facebook group has been invaluable in resolving very specific questions 
 - I'm planning on reaching out to communities for code reviews (your comments are always welcome btw), another pair of eyes is always helpful, although so far these have proven to be unfruitful:-
    - [Reddit](https://www.reddit.com/r/reviewmycode/comments/5oorz1/java_6502_emulator/)
    - [Stack Exchange](http://codereview.stackexchange.com/questions/154600/op-code-decoding-in-an-emulator) 
 
### Outputs
 - A Dev.to blog post on [Functional Enums in Java](https://dev.to/rossdrew/functional-enums-in-java-34o1)
 - A Dev.to blog post on making a [custom GitHub shield](https://rossdrew.github.io//pitest-ci/), in this case to report [Pitest](http://pitest.org/) mutation testing results.
 
## Get involved 
Beerpay seems to have died so no contribution method.  I'll look into replacing it.
  
For now, [make a feature suggestion](https://beerpay.io/rossdrew/emuRox?focus=wish) if you are so inclined.

Lastly, feel free to volunteer solutions, fixes, improvements, pull requests, documentation, technology suggestions.  If you are a JavaFX/Swing guru, I'd love to see a better UI on this.  Just note that I'm trying to make this, as well as a working project, a highly tested, nice codebase to work on.

## Image

EmuRox chip image by [Søren Siebuhr](http://sorensiebuhr.dk/)
