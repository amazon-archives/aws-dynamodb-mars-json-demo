'use strict';

describe('Controller: FavoritesCtrl', function () {

    // load the controller's module
    beforeEach(module('MSLImageExplorerApp'));


    var FavoritesCtrl, scope;

    var data = {
    	Items: [
    	{imageid: '123'},
    	{imageid: '456'}
    	]
    };

    var marsPhotos = {
    	queryUserVotedPhotos: function(queryParams, callback){
    		callback(null, data);
    	},
        getThumbnails: function(photos, callback) {
            callback();
        }
    };

    var Blueimp = {
    	Gallery: function() {}
    };

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope, $log) {
    	scope = $rootScope.$new();
    	FavoritesCtrl = $controller('FavoritesCtrl', {
    		$scope: scope,
    		$log: $log,
    		MarsPhotosDBAccess: marsPhotos,
    		Blueimp: Blueimp
    	});
    }));

    it('should attach a list of user voted photos to the scope', function () {
    	expect(scope.photos.length).toBe(2);
    });
});
