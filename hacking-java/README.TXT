1. INTRODUCTION 

This directory contains code samples as well as a corresponding Eclipse 
project and Ant build.xml file.  The contents are ordered as follows: 

bin   -- Binaries and scripts
build -- Build artifacts
eclipse-settings -- Settings for Eclipse formatting and comments
lib   -- Required libraries
licenses -- Licenses for libraries
src   -- Java source
test  -- Java unit tests

2. PREREQS

Code in this directory was developed on MacOSX 10.9 using Eclipse 4.3 (Kepler).
To compile and test you will need the following: 

  * Java 1.6 or greater
  * Ant 1.9 (earlier versions may also work but are not guaranteed)

Eclipse settings are stored in the eclipse-settings directory. 

3. COMPILE AND TEST

Cd into this directory and run ant -p to see targets.  The following 
targets are most useful. 

  ant all - Build and run unit tests
  ant clean - Remove files

Build artifacts and JUnit test output go into the builds directory. 
