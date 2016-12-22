[![license](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://opensource.org/licenses/MIT) 
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

-----

###Problems

######Javas unsigned byte problem. 
 - Java bytes are signed, meaning it's a pain to deal with them, instead we have to use ints to represent bytes.
 - System.out is confusing Pitest, need to invest some time in moving to a loggin framework
 - JaCoCo seems to have a problem picking up both the Spock and JUnit tests, resulting in bad coverage.  Pitest however picks up both.