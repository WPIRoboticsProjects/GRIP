![logo](https://cloud.githubusercontent.com/assets/3964980/7551665/c133ce00-f65f-11e4-8d65-f4f122880b1c.png)

[![Join the chat at https://gitter.im/WPIRoboticsProjects/GRIP](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/WPIRoboticsProjects/GRIP?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/WPIRoboticsProjects/GRIP.svg?branch=master)](https://travis-ci.org/WPIRoboticsProjects/GRIP)
[![codecov.io](http://codecov.io/github/WPIRoboticsProjects/GRIP/coverage.svg?branch=master)](http://codecov.io/github/WPIRoboticsProjects/GRIP?branch=master)

# GRIP Computer Vision Engine

GRIP (the Graphically Represented Image Processing engine) is a program for rapidly prototyping and deploying computer
vision algorithms, primarily for robotics applications.  **It is currently in a very early state of development**,
should be ready in time for the 2016 FIRST Robotics Competition.

## ALPHA TESTERS

1. Download the newest [release](https://github.com/WPIRoboticsProjects/GRIP/releases) for your operating system.
2. Run the included installer.
3. Open GRIP

## DEVELOPERS

To run, use the included Gradle wrapper script.

    ./gradlew run

## Docs
Javadocs for the project can be found [here](http://WPIRoboticsProjects.github.io/GRIP)

## Common Problems
### UnsatisfiedLinkError
The opencv library for linux requires version 4.8 or higher of gcc in order for `GOMP_4.0` to be present.
Check the version of gcc you have installed using `gcc -v` and update if necessary.
