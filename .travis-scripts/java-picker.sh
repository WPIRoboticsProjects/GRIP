#!/usr/bin/env bash

if [ "$TRAVIS_OS_NAME" == "" linux ]; then
    jdk_switcher use oraclejdk8
else
    brew tap caskroom/cask
    brew install brew-cask
    brew cask install java
    java -version
fi