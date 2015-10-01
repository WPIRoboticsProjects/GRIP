'use strict';

/**
 * @ngdoc overview
 * @name gripApp
 * @description
 * # gripApp
 *
 * Main module of the application.
 */
angular
  .module('gripApp', [
    'ngAnimate',
    'ngAria',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngTouch',
    'CountdownDirective'
  ])
  .config(function ($routeProvider) {
    var routeProvider = $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .when('/posts', {
        templateUrl: 'views/posts.html',
        controller: 'PostsCtrl'
      })
    JEKYLL_POSTS.forEach(function(post){
      console.log(post);
      routeProvider.when("/posts" + post.simpleLink, {
        templateUrl: post.link
      });
    });
    routeProvider
      .otherwise({
        redirectTo: '/'
      });
  })
  .filter('cut', function () {
    return function (value, wordwise, max, tail) {
      if (!value) return '';

      max = parseInt(max, 10);
      if (!max) return value;
      if (value.length <= max) return value;

      value = value.substr(0, max);
      if (wordwise) {
        var lastspace = value.lastIndexOf(' ');
        if (lastspace != -1) {
            value = value.substr(0, lastspace);
        }
      }

      return value + (tail || ' …');
    };
  });
