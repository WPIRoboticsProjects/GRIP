#!/bin/bash

# Source of file: http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

if [ -z ${GH_TOKEN+x} ]; then
  echo "GH_TOKEN is unset, cannot publish Javadoc"
  exit 0;
fi

echo -e "Generating javadoc...\n"
./gradlew aggregateJavadocs

echo -e "Publishing javadoc...\n"

cp -R build/docs/javadoc $HOME/javadoc-latest

cd $HOME
git config --global user.email "azuredevops@microsoft.com"
git config --global user.name "azure-pipelines"
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/WPIRoboticsProjects/grip gh-pages #> /dev/null

cd gh-pages
git rm -rf ./javadoc
cp -Rf $HOME/javadoc-latest ./javadoc
git add -f .
git commit -m "Lastest javadoc on successful Azure build ${BUILD_BUILDNUMBER} auto-pushed to gh-pages"
git push -fq origin gh-pages #> /dev/null

echo -e "Published Javadoc to gh-pages.\n"
