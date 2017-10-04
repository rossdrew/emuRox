# Contributing Guidelines

Contributions are of course welcome.  This is a large task to take on by myself while working full time.

# What are we looking for?

I want to make sure that:-

### The emulation is as black box accurate as possible
 
This is an emulator.  I want it to be composable so anything emulated should be as outwardly accurate as possible without compromising efficiency.  That is, running the same tests on a the system being emulated and the emulator should be as close as possible.

### Code in the codebase has high test and mutation coverage.

This is a TDD driven project.  That means for each step of functionality, I write a test to describe it then write the minimal amount of code required to pass that test and increment in that way.
So I expect any code written to at least be fully covered by tests.  These tests should also be backed up by mutation coverage to ensure their strength.

### Tt is as well designed as possible for testability and adaptability

I'm not the worlds greatest software designer but this is a learning process as much as anything else so improvements & comments on design are most welcome.

### It is well written and documented code

An important goal of a large software project should be maintainability.  Code should be written, not in the most clever way you can think, but in the clearest way you can.  The faster it can be understood by someone who's never seen it before; the quicker improvements will come.  This is more important than shaving off some lines of code or getting rid of a variable declaration.
Secondly, Javadoc.  I want this to be pluggable so Javadoc will be important as it grows.  

So there it is, if you have something to contribute to this project and you can write well tested, clear and accurate code or have a better design idea, I'm all ears.
