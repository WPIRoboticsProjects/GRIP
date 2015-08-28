
'use strict';

module.exports = function (grunt) {
    // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  // Configurable paths for the application
  var appConfig = {
    app: require('./bower.json').appPath || '.',
    dist: 'dist'
  };

  grunt.initConfig({
    watch: {
      bower: {
        files: ['bower.json'],
        tasks: ['wiredep']
      },
      js: {
        files: ['scripts/{,*/}*.js'],
        tasks: ['newer:jshint:all'],
        options: {
          livereload: '<%= connect.options.livereload %>'
        }
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },
      livereload: {
        options: {
          livereload: '<%= connect.options.livereload %>'
        },
        files: [
          '{,*/}*.html',
          '.tmp/styles/{,*/}*.css',
          'images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },
    connect: {
      options: {
        port: 9000,
        // Change this to '0.0.0.0' to access the server from outside.
        hostname: 'localhost',
        livereload: 35729
      },
      livereload: {
        options: {
          open: true,
          middleware: function (connect) {
            return [
              connect.static('.tmp'),
              connect().use(
                '/bower_components',
                connect.static('./bower_components')
              ),
              connect().use(
                '/styles',
                connect.static('./styles')
              ),
              connect.static(appConfig.app)
            ];
          }
        },
        dist: {
          options: {
            open: true,
            base: '.'
          }
        }
      },
    },
    wiredep: {
      app: {
        src: ['index.html'],
        ignorePath:  /\.\.\//
      }
    },
  });

  grunt.registerTask('build', [
    'wiredep'
  ]);

  grunt.registerTask('serve', [
    'wiredep',
    'connect:livereload',
    'watch'
  ]);

  grunt.registerTask('default', [
    'build'
  ]);
};
