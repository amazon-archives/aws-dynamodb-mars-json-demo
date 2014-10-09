'use strict';
/*
global $
*/

/**
 * @ngdoc function
 * @name MSLImageExplorerApp.controller:TopVotedCtrl
 * @description
 * # TopVotedCtrl
 * Controller of the MSLImageExplorerApp
 */
angular.module('MSLImageExplorerApp').
	controller('TopVotedCtrl', function ($scope, $routeParams, $timeout, $location, $log, MarsPhotosDBAccess, Blueimp) {

        $scope.title = 'Top Voted Mars Images';
        $scope.description = 'Photos taken by sorted by # of votes by viewers.';
        $scope.photos = [];
        
        /*
        * Initializes instrument in the scope according to $routeParams. It 
        * uses the default instrument if none specified.
        */
        if ($routeParams.instrument) {
        	$scope.instrument = $routeParams.instrument;
        } else {
        	$scope.instrument = MarsPhotosDBAccess.defaultInstrument;
        }
        $scope.mission = MarsPhotosDBAccess.defaultMission;
        $scope.missionInstrument = $scope.mission + '+' + $scope.instrument;
        $scope.instrumentList = MarsPhotosDBAccess.instrumentList;

        /**
         * Updates the location path and reloads the page. Called from the child
         * controller, SideMenuCtrl. 
         */
        $scope.reload = function(){
        	var path = '/topVoted/' + $scope.instrument;
                $log.debug('Updating path to ' + path);
        	$timeout(function(){
        		$scope.$apply(function(){
        			$location.path(path);          
        		});
        	});
        };

        /**
         * Fetches top voted photos via MarsPhotosDBAccess service and replaces
         * $scope.photos with the results. It also triggers Blueimp 
         * image gallery to prepare for the slideshow.
         */
        $scope.updatePhotos = function(){
            MarsPhotosDBAccess.queryWithVoteIndex({
                hashKey: $scope.missionInstrument,
                lastEvaluatedKey: $scope.lastEvaluatedKey
            }, function(error, data){
                if (!error) {
                    var photos = data.Items;
                    $scope.lastEvaluatedKey = data.LastEvaluatedKey;                            
                    $scope.$apply(function(){
                        $scope.photos = photos;
                    });
                } else {
                    $log.error(error);
                } 
            });
        };

        $scope.updatePhotos();
        Blueimp.Gallery($('#links a'), $('#blueimp-gallery').data());                                                               
});

