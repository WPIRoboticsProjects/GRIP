![logo](https://cloud.githubusercontent.com/assets/3964980/11156885/6fa1967a-8a1c-11e5-8c78-e552ffba31c0.png)

[![Join the chat at https://gitter.im/WPIRoboticsProjects/GRIP](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/WPIRoboticsProjects/GRIP?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://dev.azure.com/wpiroboticsprojects/GRIP/_apis/build/status/WPIRoboticsProjects.GRIP?branchName=master)](https://dev.azure.com/wpiroboticsprojects/GRIP/_build/latest?definitionId=1?branchName=master)
[![codecov.io](http://codecov.io/github/WPIRoboticsProjects/GRIP/coverage.svg?branch=master)](http://codecov.io/github/WPIRoboticsProjects/GRIP?branch=master)
[![Github Releases](https://img.shields.io/github/downloads/WPIRoboticsProjects/GRIP/total.svg)](https://github.com/WPIRoboticsProjects/GRIP/releases/latest)


# GRIP Computer Vision Engine

GRIP (the Graphically Represented Image Processing engine) is an application for rapidly prototyping and deploying computer
vision algorithms, primarily for robotics applications.
Developing a vision program can be difficult because it is hard to visualize the intermediate results. 
GRIP simplifies and accelerates the creation of vision systems for experienced users and reduces the barrier to entry for inexperienced users.
As a result, many teams with minimal computer vision knowledge successfully used GRIP since the 2016 FIRST Robotics Competition game.

# Features

 - Intuitive drag and drop UI.
 - Active development community.
 - Generates Java, C++, and Python code directly from the pipeline ([Example usage here](https://github.com/WPIRoboticsProjects/GRIP-code-generation))!
 - Extensible!
 - Deploys and runs headless.
 - Supports various network protocols
   - [Network Tables](https://github.com/wpilibsuite/allwpilib/tree/master/ntcore/)
   - [Robot Operating System (ROS)](http://www.ros.org/)
   - HTTP
 - CUDA acceleration
 - OS Support:
     - Windows
     - OSX
     - Linux (minimum Ubuntu 18.04 or libc version 2.27+)
     - Embedded Linux ARM (NI RoboRIO)


## Getting Started

1. Download the newest [release](https://github.com/WPIRoboticsProjects/GRIP/releases) for your operating system.
2. Run the included installer.
3. Open GRIP

Check out the release notes and [the wiki](https://github.com/WPIRoboticsProjects/GRIP/wiki) for more information.

Note for Linux users: GRIP requires GTK2 to be installed. Most Ubuntu-based distributions include it,
but some other distros such as Arch may require it to be manually installed. GRIP also requires libc version 2.27
or higher; for Ubuntu-based distributions, this requires Ubuntu 18.04 or newer.

## Building GRIP From Source

To build and run, use the included Gradle wrapper script on a Unix System:

    ./gradlew :ui:run

On Windows:

    gradlew.bat :ui:run

If you don't have an internet connection you can build using the `--offline` flag if you have built GRIP once before.

## CUDA Support
To enable CUDA acceleration, CUDA 10.0 needs to be installed on your computer. CUDA 10.1 may work on
Linux systems, but Windows _must_ use 10.0.

When running or building from source, add the Gradle flag `-Pcuda` to enable CUDA acceleration (eg `./gradlew :ui:run -Pcuda`)

Note that CUDA acceleration is not available for all operations.

Code generation does not support CUDA - it is only used for operations running in GRIP.

## Contributing

See the guide on [setting up build tools](https://github.com/WPIRoboticsProjects/GRIP/wiki/Setting-up-build-tools) in the wiki.
