'use strict';

/**
 * @ngdoc function
 * @name gripApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the gripApp
 */
angular.module('gripApp')
  .controller('MainCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
