#!/bin/bash

# Source of file: http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/
if [ "$TRAVIS_REPO_SLUG" == "WPIRoboticsProjects/GRIP" ] && [ "$TRAVIS_JDK_VERSION" == "oraclejdk8" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ] && [[ "$TRAVIS_OS_NAME" != "osx" ]]; then

  echo -e "Generating javadoc...\n"
  ./gradlew aggregateJavadocs

  echo -e "Publishing javadoc...\n"

  cp -R build/docs/javadoc $HOME/javadoc-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/WPIRoboticsProjects/grip gh-pages #> /dev/null

  cd gh-pages
  git rm -rf ./javadoc
  cp -Rf $HOME/javadoc-latest ./javadoc
  git add -f .
  git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages #> /dev/null

  echo -e "Published Javadoc to gh-pages.\n"

fi
