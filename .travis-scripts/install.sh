#!/bin/bash

if [[ $TRAVIS_OS_NAME != 'osx' ]]; then
    # Install some custom requirements on Linux if necessary
    export DISPLAY=:99.0
    sh -e /etc/init.d/xvfb start
    pip install --user codecov
fi
