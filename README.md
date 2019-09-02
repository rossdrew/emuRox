[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://opensource.org/licenses/MIT) 
[![Build Status by TravisCI](https://travis-ci.org/rossdrew/emuRox.svg?branch=master)](https://travis-ci.org/rossdrew/emuRox)
[![Code Coverage by Codecov](https://codecov.io/gh/rossdrew/emuRox/branch/master/graph/badge.svg)](https://codecov.io/gh/rossdrew/emuRox)
[![Mutation Coverage by Pitest](http://rossdrew.pythonanywhere.com/shield&rnd=1)](http://rossdrew.pythonanywhere.com/report)
[![Code Quality by Codacy](https://api.codacy.com/project/badge/Grade/519fed1cf9c64216a0c9992eed25a36f)](https://www.codacy.com/app/rossdrew/emuRox?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rossdrew/emuRox&amp;utm_campaign=Badge_Grade)
[![Quality Gate by sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=com.rox%3AEmuRox&metric=alert_status)](https://sonarcloud.io/api/project_badges/measure?project=com.rox%3AEmuRox&metric=alert_status)
[![GitHub Actions CI](https://github.com/rossdrew/emurox/workflows/GitHub%20Actions/badge.svg)](https://github.com/rossdrew/emurox/workflows/GitHub%20Actions/badge.svg)

<a href="http://sorensiebuhr.dk/">
 <img align="right" src="https://github.com/rossdrew/rossdrew.github.io/blob/master/src/img/EmuRox.png" data-canonical-src="https://github.com/rossdrew/rossdrew.github.io/blob/master/src/img/EmuRox.png" alt="EmuRox - Image by Søren Siebuhr" width="150" />
</a>

# EmuRox
Ostensibly, an Nintendo Entertainment System ([NES](https://en.wikipedia.org/wiki/Nintendo_Entertainment_System)) emulator with a view to encompassing other systems. Creating a pluggable multi-emulator. In actuality, it is not about getting a working emulator, for a NES or anything else. There are plenty of them out there.  It's about taking a complex problem and designing the best abstraction possible.  Allowing us to discuss complex ideas in simple language.

Too often I've:

 - had to sacrifice good implementation for fast functionality
 - dealt with code in which concision hampered readability
 - consumed development time in lengthy, bloated builds.  Usually caused by complex and brittle system testing
 - written under-specified features which need to be re-written over and over
 - work with naive domain knowledge due to time pressure
 - written solutions to problems I had no time to completely understand.

 > _Any fool can write code that a computer can understand. Good programmers write code that humans can understand. (M. Fowler)_

 I wanted something I could redesign over and over as my knowledge of the domain grew and as I felt the frustrations of having to reaquaint myself with pirces of code I hadn't seen in a while.  Something I could test in a thousand different ways, then tweak and experiment with build processes so that the development cycle is rapid and fluid.  That I could tweak the code over and over to make it simpler and simpler to understand. 

There are also desirable side effects.  I wanted to write my own emulator, play the games of my childhood and know that I wrote the platform that they were running on and becoming even more familiar with the first pieces of software I fell in love with.  I wanted a large codebase I had complete control over and could try out tools and technologies.  I got to the stage of the latter, quite a while ago and I'm crawling towards the former.

## Usage

Most of my testing is done via the plethora of tests I've written.
There is a debug UI for the MOS 6502 which provides a basic register and memory overview for running code through step by step in `src/main/java/com/roxemu/processor/mos6502/dbg/ui/DebuggerWindow.java` and run with the Gradle command `runDebugUI`

## 6502 and progress towards a working NES...

I've seperate (at least) the NES section of this down into 7 stages:

 - [x] I blasted through the first phase due to the massive amount of documentation on the MOS 6502
 - [x] While researching phase 3, I churned through the relitively basic work of parsing iNES files of phase 2 (without edge cases for now)
 - [ ] Phase 3 is kinda where I'm stuck at the moment, struggling to find time to consolodate all the documentation and existing on the APU, which is less abundant than of the MOD 6502 and far more messy
 - [ ] Phase 4 is going to be a decent challenge I think, where I need to start concentrating on timings
 - [ ] Phase 5 and 6 are bringing it all together into a useable system

So currenly large scale features are undergoing research and early design.  This also gives me time to improve and tweak what is there.


 <img aligm="right" src="https://github.com/rossdrew/emuRox/blob/master/docs/EmuRox%20Roadmap.png" data-canonical-src="https://github.com/rossdrew/emuRox/blob/master/docs/EmuRox%20Roadmap.png" alt="EmuRox Roadmap" width="900" />


## Development & Testing

<a href="https://codecov.io/gh/rossdrew/emuRox">
 <img align="right" src="https://codecov.io/gh/rossdrew/emuRox/graphs/sunburst.svg" data-canonical-src="https://codecov.io/gh/rossdrew/emuRox/graphs/sunburst.svg" alt="The current codecov.io coverage chart" width="150" />
</a>

The plan was to develop in a TDD (Test Driven Development) centric way, that is via Red-Green testing; i.e. writing failing/red tests that describe functionality then writing that functionality to make the tests pass/green and iteratively writing a complete application.
 
#### Current technologies

 _Technologies that I have used, liked and continued to use as part of the project..._

 - [Trello](https://trello.com/b/ZWcFxEu3/emurox) for work breakdown and project management. 
 - [Java](https://www.java.com/)/[JUnit](http://junit.org/junit4/) for basic functionality tests, adding mocks and more expressive syntax with [Mockito](http://site.mockito.org/).  This includes features such as [Theories](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html).  
 - [Groovy](http://www.groovy-lang.org/)/[Spock](http://spockframework.org/) for [data-driven tests](https://en.wikipedia.org/wiki/Data-driven_testing), adding mocks and more expressive syntax with [Hamcrest](http://hamcrest.org/).  For covering various variations of class creation and method use in a clear, concise way that JUnit struggles with.  
 - [junit-quickcheck](https://github.com/pholser/junit-quickcheck) and [Spock Genesis](https://github.com/Bijnagte/spock-genesis) for [property-based testing](http://blog.jessitron.com/2013/04/property-based-testing-what-is-it.html) in Java and Groovy respectively.
 - [JUnit Theory Suite](https://github.com/richard-melvin/junit-theory-suite) for unrolling [JUnit](http://junit.org/junit4/) [Theories](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html).  This will unroll all data points combinatorially to a [Theory](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html)
 - [TravisCI](https://travis-ci.org/) for [continuous integration](https://en.wikipedia.org/wiki/Continuous_integration) & build status GitHub Shield, making sure my build always builds and my tests always pass.
 - [Pitest](http://pitest.org/) reports. To allow us to use [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) to validate and improve our ([Spock](http://spockframework.org/) & [JUnit](http://junit.org/junit4/)) unit tests.  The Pitest GitHub shield is a [self made Python program](https://rossdrew.github.io//pitest-ci/).
 - [JaCoCo](http://www.eclemma.org/jacoco/) reports. To allow us to strive for high [code coverage](https://en.wikipedia.org/wiki/Code_coverage).  Mainly for the GitHub shield using [CodeCov](https://codecov.io). 
 - Static analysis done in my development environment on [IntelliJ IDEA](https://www.jetbrains.com/idea/) and online (including GitHub shield) by [Codeacy](https://www.codacy.com/). 

#### Investigated technologies 

 _Technologies that I have used yet disliked or haven't saw a place for them in my project for whatever reason..._

 - [JParams toStringTester](https://github.com/jparams/to-string-tester) for `toString` testing but it didn't seem to work very well, I filed an [issue](https://github.com/jparams/to-string-tester/issues/1) but the project has since been archived.
 - [JUnit QuickTheories](https://github.com/ncredinburgh/QuickTheories) for property based testing but it's much more verbose for no added benefit over [junit-quickcheck](https://www.google.co.uk/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0ahUKEwjq4-PF-aPSAhWHDsAKHV17BCIQFggaMAA&url=https%3A%2F%2Fgithub.com%2Fpholser%2Fjunit-quickcheck&usg=AFQjCNE37M0yEi68OG8Hr7y1MDoJwcLOaQ&sig2=AUpnbmKM5Sk9efhw1r-bKw&bvm=bv.147448319,d.d2s)
 - [Spockito](https://github.com/tools4j/spockito) to give the ability to unroll [JUnit](http://junit.org/junit4/) [Theories](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html) like in [Spock](http://spockframework.org/) data-driven tests but I'm not a big fan of the syntax and attaches `@DataPoint`s to the methods.  It would be great for adding data-driven tests if you were limited to [JUnit](http://junit.org/junit4/), however
 - [Kotlin](https://kotlinlang.org/) for testing and various testing frameworks like [Spek](https://github.com/spekframework/spek) and [kotlintest](https://github.com/kotlintest/kotlintest) but it offers nothing I don't already have and property/data-driven/theory testing is pretty ugly and/or weak.

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
 
## Get involved 
Found my project interesting or used it in some way?  Contribute by showing your appreciation with a [frosty beverage](https://beerpay.io/rossdrew/emuRox).  In my case this will go towards an [Old Fashioned](https://www.instagram.com/p/BUzyDHOAzkI/?taken-by=ross_drew) but as far as I'm aware there is no Spirit/Bourbon/WhiskeyPay.
  
Or even [make a feature suggestion](https://beerpay.io/rossdrew/emuRox?focus=wish) if you are so inclined.

Lastly, feel free to volunteer solutions, fixes, improvements, pull requests, documentation, technology suggestions.  If you are a JavaFX/Swing guru, I'd love to see a better UI on this.  Just note that I'm trying to make this, as well as a working project, a highly tested, nice codebase to work on.

## Image

EmuRox chip image by [Søren Siebuhr](http://sorensiebuhr.dk/)
