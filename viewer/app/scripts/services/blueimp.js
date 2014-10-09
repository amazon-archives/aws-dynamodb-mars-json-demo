'use strict';
/*
global blueimp
*/
/**
 * @ngdoc function
 * @name MSLImageExplorerApp.service:Blueimp
 * @description
 * # Blueimp
 * Service of the MSLImageExplorerApp. Exposes blueimp Javascript object as a service.
 */
 angular.module('MSLImageExplorerApp').
	service('Blueimp', function (){
		return blueimp;
	});