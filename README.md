![logo](https://cloud.githubusercontent.com/assets/3964980/11156885/6fa1967a-8a1c-11e5-8c78-e552ffba31c0.png)

[![Join the chat at https://gitter.im/WPIRoboticsProjects/GRIP](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/WPIRoboticsProjects/GRIP?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/WPIRoboticsProjects/GRIP.svg?branch=master)](https://travis-ci.org/WPIRoboticsProjects/GRIP)
[![Build status](https://ci.appveyor.com/api/projects/status/sbrd2nhpiktlhf58/branch/master?svg=true)](https://ci.appveyor.com/project/JLLeitschuh/grip/branch/master)
[![codecov.io](http://codecov.io/github/WPIRoboticsProjects/GRIP/coverage.svg?branch=master)](http://codecov.io/github/WPIRoboticsProjects/GRIP?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/56aaaac57e03c7003ba40ab6/badge.svg?style=plastic)](https://www.versioneye.com/user/projects/56aaaac57e03c7003ba40ab6)
[![Github Releases](https://img.shields.io/github/downloads/WPIRoboticsProjects/GRIP/total.svg)](https://github.com/WPIRoboticsProjects/GRIP/releases/latest)

# GRIP Computer Vision Engine

GRIP (the Graphically Represented Image Processing engine) is an application for rapidly prototyping and deploying computer
vision algorithms, primarily for robotics applications.
Developing a vision program can be difficult because it is hard to visualize the intermediate results. 
GRIP simplifies and accelerates the creation of vision systems for experienced users and reduces the barrier to entry for inexperienced users.
As a result, many teams with minimal computer vision knowledge successfully used GRIP in the 2016 FIRST Robotics Competition game.

# Features

 - Intuitive drag and drop UI.
 - Active development community.
 - Extensible!
 - Deploys and runs headless.
 - Supports various network protocols
   - [Network Tables](https://github.com/PeterJohnson/ntcore)
   - [Robot Operating System (ROS)](http://www.ros.org/)
 - OS Support:
     - Windows
     - OSX
     - Linux
     - Embedded Linux ARM (NI RoboRIO)


## Getting Started

1. Download the newest [release](https://github.com/WPIRoboticsProjects/GRIP/releases) for your operating system.
2. Run the included installer.
3. Open GRIP

Check out the release notes and [the wiki](https://github.com/WPIRoboticsProjects/GRIP/wiki) for more information.

## Building GRIP From Source

To build and run, use the included Gradle wrapper script on a Unix System:

    ./gradlew :ui:run

On Windows:

    gradlew.bat :ui:run

If you don't have an internet connection you can build using the `--offline` flag if you have built GRIP once before.

## Contributing

See the guide on [setting up build tools](https://github.com/WPIRoboticsProjects/GRIP/wiki/Setting-up-build-tools) in the wiki.

## Thanks

Thanks to [TravisCI](https://travis-ci.org/) and [AppVeyor](https://www.appveyor.com/) for providing their continuous integration 
servers to open source projects for free.
