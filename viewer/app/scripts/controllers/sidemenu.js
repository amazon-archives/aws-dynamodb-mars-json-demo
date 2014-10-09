'use strict';
/*
global $
*/

/**
 * @ngdoc function
 * @name MSLImageExplorerApp.controller:SideMenuCtrl
 * @description
 * # SideMenuCtrl
 * Controller of the MSLImageExplorerApp that takes care of sidemenu a.k.a Mission Control. 
 */
angular.module('MSLImageExplorerApp').
	controller('SideMenuCtrl', function ($scope, $modal, $log, MarsPhotosDBAccess) {
        /**
         * List of instruments to select from.
         */
        $scope.instrumentList = MarsPhotosDBAccess.instrumentList;

        $scope.active = true;

        /**
         * Flag that indicates if the sidebar is initialized
         */
        $scope.isSidrInitialized = false;    

        /**
         * Flag that indicates if the instrument list should be shown or not.
         */
        $scope.showInstrumentList = false;

        /**
         * Opens the side menu. It initializes the component
         * on the first time it is called.
         */
        $scope.openSidr = function(){
            if (!$scope.isSidrInitialized) {
                initSidr();
                $scope.isSidrInitialized = true;
            }
        	$.sidr('open');
        };

        /**
         * Closes the side menu.
         */
        $scope.closeSidr = function(){
        	$.sidr('close');
            if ($scope.modalInstance) {
                $scope.modalInstance.dismiss('cancel');
            }
        };

        /**
         * Shows a modal window that shows the rover detail figure and
         * the list of instruments on the side menu.
         */
        $scope.showRoverDetails = function(){
        	$scope.modalInstance = $modal.open({
        		templateUrl: 'views/partials/rover-detail.html',
        		controller: 'DialogCtrl',
        		resolve: {
                    title: 'Instrument Selection',
                    message: ''
        		}
        	});
        	$scope.showInstrumentList = true;
        };

        /**
         * Sets the instrument to the one the user selected and reloads
         * the parent view, e.g. the timeline view and top-voted photos view.
         * It also closes the modal window that shows the rover details. 
         */
        $scope.setInstrument = function(instrument, $event) {
        	$log.debug('Instrument is set to ' + instrument);
        	$($event.currentTarget).addClass('active').siblings().removeClass('active');
        	$scope.$parent.instrument = instrument;  
        	$scope.showInstrumentList = false;        	      		
            $scope.modalInstance.dismiss('cancel');

        	reloadParentView();
        };

        /**
         * Called when the view is destroyed and closes the side menu.
         */
        $scope.$on('$destroy', function(){
            $scope.closeSidr();
        });

        /**
         * Calls reload function in the parent controller, e.g. timeline or 
         * top-voted photos view, if exists. It triggers reloading of the 
         * parent view with the specified time and instrument. 
         */
        var reloadParentView = function(){
        	if (typeof($scope.$parent.reload) === 'function') {
        		$log.debug('Reloading parent view');
        		$scope.$parent.reload();
        	} else {
                $log.warn('Parent view does not have reload()');
            }
        };

        /**
         * Initializes the side menu and the date picker in it.
         */
        var initSidr = function(){
        	$('#sidemenu').sidr({
        		side: 'right'
        	}); 
        	$('.datepicker').datepicker('update', new Date());    
        	if ($scope.time) {
        		try {
        			$('.datepicker').datepicker('update', new Date(parseInt($scope.time)));
        		} catch (e) {
        			$log.error('Invalid time specified: ' + $scope.time);
        		}
        	}

        	$('.datepicker').datepicker().on('changeDate', function(ev){
        		$(ev.target).datepicker('hide');
        		$scope.$parent.time = ev.date.valueOf();   
        		reloadParentView();
        	});
        	$('.datepicker').datepicker().on('show', function(){
        	   // Workaround to remove a triangle at left top
        	   $('.datepicker-dropdown').removeClass('datepicker-orient-top');
        	});    
        };
});

