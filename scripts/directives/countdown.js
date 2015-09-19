(function () {
    "use strict";
    /*
     * http://codepen.io/garethdweaver/pen/eNpWBb/
     */
    var app = angular.module('CountdownDirective', []);
    app.directive('countdown', [
      'Util',
      '$interval',
      function (Util, $interval) {
        return {
          restrict: 'A',
          scope: { date: '@' },
          templateUrl:'scripts/templates/countdown.html',
          link: function (scope, element) {

          },
          controller: function($scope){
            var future;
            future = new Date($scope.date);
            $interval(function () {
              var diff;
              diff = Math.floor((future.getTime() - new Date().getTime()) / 1000);
              $scope.dateOut = Util.dhms(diff);
            }, 1000);
          }
        };
      }
    ]);
    app.factory('Util', [function () {
      return {
        dhms: function (t) {
          var days, hours, minutes, seconds;
          days = Math.floor(t / 86400);
          t -= days * 86400;
          hours = Math.floor(t / 3600) % 24;
          t -= hours * 3600;
          minutes = Math.floor(t / 60) % 60;
          t -= minutes * 60;
          seconds = t % 60;
          return {
            days: days,
            hours: hours,
            minutes: minutes,
            seconds: seconds
          };
        }
    };
  }]);
}.call(this));
