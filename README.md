# GRIP GitHub Pages

## What is this madness?
This is a completely seperate orphan branch from the main repo.
This is what hosts the site (link above).
This branch should never be merged with master.

## JavaDocs
The javadoc folder is routinely overwritten by the travis-ci build.
Any changes you make to it will be deleted next time travis builds the master branch.


## Developing

### Setup the toolset

 1. Install [Node.js](http://nodejs.org/)
 2. Install [Bower](http://bower.io/)
 3. Inside the repository run `npm install`

### Running Locally

Simply run `grunt serve` and you'll have a local server with the site running.
Your changes will automatically trigger a page reload so your browser will always have the current code.

### Adding Dependencies

#### Developer Dependencies

Dev dependencies are through NPM. To add a dependency use `sudo npm install [package name] --save-dev`

#### Dependencies

Dependencies are automatically added to the index.html site with the grunt-wiredep plugin.
To add a dependency to the index.html use `bower install --save`. If wiredep doesn't add it
then the plugin author probably doesn't have their bower.json file setup correctly.
