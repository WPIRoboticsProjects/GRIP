'use strict';

/**
 * @ngdoc function
 * @name gripApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the gripApp
 */
angular.module('gripApp')
  .controller('AboutCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
