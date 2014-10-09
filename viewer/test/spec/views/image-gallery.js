'use strict';

describe('View: ImageGallery', function() {
    beforeEach(module('MSLImageExplorerApp'));
    beforeEach(module('views/partials/image-gallery.html'));    
    beforeEach(module('views/partials/sidemenu.html'));        

    var FavoritesCtrl, scope, view;

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
    beforeEach(inject(function ($templateCache, $compile, $controller, $rootScope, $log) {
    	var html = $templateCache.get('views/partials/image-gallery.html');
    	scope = $rootScope.$new();

    	FavoritesCtrl = $controller('FavoritesCtrl', {
    		$scope: scope,
    		$log: $log,
    		MarsPhotosDBAccess: marsPhotos,
    		Blueimp: Blueimp
    	});
    	view = $compile(angular.element(html))(scope);
    	scope.$digest();
    }));

    it('should show mars images the user liked', function() {
        expect(view.find('h1').text()).toEqual('Mars Images You Liked');
    });

    it('should show list of favorite images', function() {
        expect(view.find('#links img').length).toBe(2);
    });
});
