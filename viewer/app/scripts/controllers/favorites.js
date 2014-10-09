'use strict';
/*
global $
*/

/**
 * @ngdoc function
 * @name MSLImageExplorerApp.controller:FavoritesCtrl
 * @description
 * # FavoritesCtrl
 * Controller of the MSLImageExplorerApp that takes care of favorite photos view.
 */
angular.module('MSLImageExplorerApp').
	controller('FavoritesCtrl', function ($scope, $log, MarsPhotosDBAccess, Blueimp) {

        $scope.title = 'Mars Images You Liked';
        $scope.description = 'Photos that you liked.';
        $scope.photos = [];

        /**
         * Fetches photos that the user has voted 
         * via MarsPhotos service and replaces $scope.photos with 
         * the results. It also triggers Blueimp 
         * image gallery to prepare for the slideshow.
        */
        $scope.updatePhotos = function(){
            MarsPhotosDBAccess.queryUserVotedPhotos({
                hashKey: localStorage.getItem('userid'),
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

