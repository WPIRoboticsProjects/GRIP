'use strict';

/**
 * @ngdoc function
 * @name gripApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the gripApp
 */
angular.module('gripApp')
  .controller('PostsCtrl', function ($scope) {
    $scope.blogPosts = JEKYLL_POSTS;
  });
