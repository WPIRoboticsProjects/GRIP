#!/bin/bash

if [[ $TRAVIS_OS_NAME == 'osx' ]]; then

    # Install some custom requirements on OS X
    wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u66-b17/jdk-8u66-macosx-x64.dmg
    hdiutil mount jdk-8u66-macosx-x64.dmg
    sudo installer -pkg /Volumes/JDK\ 8\ Update\ 66/JDK\ 8\ Update\ 66.pkg -target LocalSystem
else
    export DISPLAY=:99.0
    sh -e /etc/init.d/xvfb start
    pip install --user codecov
fi