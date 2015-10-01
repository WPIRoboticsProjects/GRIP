# GRIP GitHub Pages

## What is this madness?
This is a completely separate orphan branch from the main repo.
This is what hosts the site (link above).
This branch should never be merged with master.

## JavaDocs
The javadoc folder is routinely overwritten by the travis-ci build.
Any changes you make to it will be deleted next time travis builds the master branch.

## Blog Posts
Blog Posts should sit inside of the `_posts` directory.
Post file names should follow the format `YYYY-MM-DD-[Name-dash-seperated].md`.
Posts should all have the same basic header:
```
---
layout: post
title: [Title]
---
```

<b>NOTE:</b> Do not use the internal jekyll linking system.
The blog post pages are all using angular for the underlying
navigation system. This will break using jekyll's link variables. 

## Developing

### Setup the toolset

 1. Install [Node.js](http://nodejs.org/)
 2. Install [Bower](http://bower.io/)
 3. Inside the repository run `npm install`
 4. Follow the installing jekyll guide on the [github website](https://help.github.com/articles/using-jekyll-with-pages/#installing-jekyll).

### Running Locally

Simply run `bundle exec jekyll serve` and you'll have a local server with the site running.
Your changes will automatically trigger a page reload so your browser will always have the current code.

### Adding Dependencies

Dev dependencies are through NPM. To add a dependency use `sudo npm install [package name] --save-dev`

#### Dependencies

Dependencies are automatically added to the index.html site with the grunt-wiredep plugin.
To add a dependency to the index.html use `bower install --save`. If wiredep doesn't add it
then the plugin author probably doesn't have their bower.json file setup correctly.
To update the index.html file with dependencies added to bower use `grunt wiredep`
