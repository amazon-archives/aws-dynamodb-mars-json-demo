'use strict';
/*
global $, moment
*/

/**
 * @ngdoc function
 * @name MSLImageExplorerApp.controller:TimelineCtrl
 * @description
 * #  TimelineCtrl
 * Controller of the MSLImageExplorerApp that takes care of the main timeline view.
 */
angular.module('MSLImageExplorerApp').
    controller('TimelineCtrl', function ($scope, $log, $location, $routeParams, $timeout, $modal, MarsPhotosDBAccess, Blueimp) {
        var timelinePosition = $('.timeline').position();
        $scope.isUpdatingPhotos = false;
        $scope.showDatePicker = true;
        $scope.photos = [];

        /*
        * Initializes instrument in the scope according to $routeParams. It 
        * uses the default instrument if not specified.
        */
        if ($routeParams.instrument) {
            $scope.instrument = $routeParams.instrument;
        } else {
            $scope.instrument = MarsPhotosDBAccess.defaultInstrument;
        }
        $scope.mission = MarsPhotosDBAccess.defaultMission;
        $scope.missionInstrument = $scope.mission + '+' + $scope.instrument;
        $scope.instrumentList = MarsPhotosDBAccess.instrumentList;

        /*
        * Parses time specified in $routeParams and sets it in $scope.
        */
        if ($routeParams.time) {
            var time = parseInt($routeParams.time);
            if (typeof(time) === 'number' && time > 0) {
                $scope.time = time;
            } else {
                $log.error('Failed to parse time parameter:' + $routeParams.time);          
            }
        }

        /**
        * Fetches more photos through MarsPhotos service and concatinates results 
        * to $scope.photos. Called when the page is loaded and when the user 
        * scrolls to the bottom of window. 
        */
        $scope.updatePhotos = function(){
            $scope.isUpdatingPhotos = true;
            MarsPhotosDBAccess.queryWithDateIndex({
                hashKey: $scope.missionInstrument,
                rangeKey: $scope.time,
                lastEvaluatedKey: $scope.lastEvaluatedKey
            }, function(error, data) { 
                if (!error) {
                    $scope.lastEvaluatedKey = data.LastEvaluatedKey;         
                    $scope.$apply(function(){
                        for (var index in data.Items) {
                            var photo = data.Items[index];
                            /*jshint camelcase: false */
                            photo.time.creation_time = moment(photo.time.creation_timestamp_utc).format('MMMM Do YYYY, h:mm:ss a zz');
                            photo.time.received_in = moment(photo.time.received_timestamp_utc).from(photo.time.creation_timestamp_utc, true);
                            $scope.photos.push(photo);
                        }
                    });
                    $scope.isUpdatingPhotos = false;
                } else {
                    $log.error(error);
                }
            });
        };

        /**
        * Updates the location path and reloads the page. Called from the child
        * controller, SideMenuCtrl. 
        */
        $scope.reload = function(){
            var path = '/timeline/' + $scope.instrument;
            if ($scope.time) {
                path += '/' + $scope.time;
            }
            $log.debug('Updating path to ' + path);
            $timeout(function(){ // We use $timeout() to avoid calling $scope.$apply() during another $scope.$apply() call
                $scope.$apply(function(){
                    $location.path(path);          
                });
            });
        };

        /**
        * Votes on the specified photo via MarsPhotos service. It updates the voting
        * cound of the photo on the successful voting. It shows an error dialog if
        * the user has already voted on the photo.
        */
        $scope.vote = function(photo) {
            $log.debug('Voting on ' + photo.imageid);
            MarsPhotosDBAccess.voteOnPhoto(
                localStorage.getItem('userid'), 
                photo,
                function(error, data) {
                    if (!error) {
                        $scope.$apply(function(){
                            photo.votes = data.Attributes.votes;              
                        });
                    } else {
                        $modal.open({
                            templateUrl: 'views/partials/dialog.html',
                            controller: 'DialogCtrl',
                            resolve: {
                                title: function() {
                                    return 'Error';
                                },
                                message: function() {
                                    return error;
                                }
                            }
                        });
                    }
                });
        };

        /*
        * Handler called when the view content is loaded. The timelineAnimate function 
        * and scroll handler need to be called after the view content is ready.
        */ 
        $scope.$on('$viewContentLoaded', function() {
            $log.debug('Content loaded');
            $scope.updatePhotos();

            // Activates image gallery feature
            Blueimp.Gallery($('.timeline a.mars-photos'), $('#blueimp-gallery').data());                                                               

            $(window).scroll(scrollHandler);
        });

        /*
        * Handler called when the view is destroyed. The scroll handler needs to 
        * be deregistered when moving to another view.
        */
        $scope.$on('$destroy', function(){
            $(window).off('scroll', scrollHandler);
        });    

        /**
        * Handler called when a scrolling event is fired. It checks if the bottom of the window
        * is reaching and calls updatePhotos() if so.
        */
        var scrollHandler = function() {
            if (isBottomReaching() && !$scope.isUpdatingPhotos) {
                $log.debug('Fetching more photos');
                $scope.updatePhotos();
            }
            activateTimelineItemOnceWindowReached();
        };

        /**
        * Judges if the bottom of window is reaching. Used to decide if more photos 
        * should be fetched upon a scrolling event or not.
        */
        var isBottomReaching = function(){
            return $(window).scrollTop() >= ($(document).height() - $(window).height() - timelinePosition.top);
        };


        /**
        * Private function which triggers to display a time line item if the window
        * scrolls to the item. 
        */ 
        var activateTimelineItemOnceWindowReached = function() {
            var inactiveItems = $('.timeline-item:not(.active)');
            if(inactiveItems.length > 0){
                var item = inactiveItems.first();
                var itemHead = timelinePosition.top + item.position().top + item.outerHeight() / 3;
                var windowBottom = $(window).scrollTop() + $(window).height();
                if (windowBottom > itemHead) {
                    item.addClass('active');
                }
            }
        };
    });
