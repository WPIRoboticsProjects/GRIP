'use strict';

angular.module('gripApp')
  .controller('header', function($scope) {
    $scope.isPost = function() {
      return window.location.hash.includes('posts'); 
    };
  });
