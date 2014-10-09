'use strict';

describe('View: Sidemenu', function() {
    beforeEach(module('MSLImageExplorerApp'));
    beforeEach(module('views/partials/sidemenu.html'));        

    var Ctrl, scope, view;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($templateCache, $compile, $controller, $rootScope, $modal, $log) {
    	var html = $templateCache.get('views/partials/sidemenu.html');
    	scope = $rootScope.$new();

    	Ctrl = $controller('SideMenuCtrl', {
    		$scope: scope,
    		$log: $log,
            $modal: $modal
    	});
    	view = $compile(angular.element(html))(scope);
    	scope.$digest();
    }));


    it('should have 3 side menu links', function(){
        expect(view.find('.sidemenu-link').length).toBe(3);
    });

    it('should have 4 instruments in the instrument list', function() {
        expect(view.find('#instrumentList li').length).toBe(4);
    });

});


