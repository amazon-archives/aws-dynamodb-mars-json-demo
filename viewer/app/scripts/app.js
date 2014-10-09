'use strict';
angular.module('MSLImageExplorerApp', 
	[
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.bootstrap.modal',  
    'config'
	]).
    config(['$routeProvider', function($routeProvider) {
        $routeProvider.
        when('/', {
            templateUrl: 'views/partials/timeline.html',
            controller: 'TimelineCtrl'
        }).      
        when('/timeline', {
            templateUrl: 'views/partials/timeline.html',
            controller: 'TimelineCtrl'
        }).            
        when('/timeline/:instrument', {
            templateUrl: 'views/partials/timeline.html',
            controller: 'TimelineCtrl'
        }).            
        when('/timeline/:instrument/:time', {
            templateUrl: 'views/partials/timeline.html',
            controller: 'TimelineCtrl'
        }).
        when('/topVoted', {
            templateUrl: 'views/partials/image-gallery.html',
            controller: 'TopVotedCtrl'
        }).                     
        when('/topVoted/:instrument', {
            templateUrl: 'views/partials/image-gallery.html',
            controller: 'TopVotedCtrl'
        }).               
        when('/favorites', {
            templateUrl: 'views/partials/image-gallery.html',
            controller: 'FavoritesCtrl'
        }).                        
        otherwise({
            redirectTo: '/'
        });
    }]);


