'use strict';
/**
 * @ngdoc function
 * @name MSLImageExplorerApp.controller:DialogCtrl
 * @description
 * # DialogCtrl
 * Controller of the MSLImageExplorerApp that handles the error dialog view.
 */
angular.module('MSLImageExplorerApp').
	controller('DialogCtrl', function ($scope, $modalInstance, title, message) {
		/**
		 * Sets the title.
		 */
		$scope.title = title;

		/**
		 * Sets the message.
		 */
		$scope.message = message;
		
		/**
		 * Closes the dialog.
		 */
		$scope.close = function(){
			$modalInstance.close();
		};
    });

