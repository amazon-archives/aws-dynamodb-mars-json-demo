'use strict';

describe('View: Timeline', function() {
    beforeEach(module('MSLImageExplorerApp'));
    beforeEach(module('views/partials/timeline.html'));        
    beforeEach(module('views/partials/sidemenu.html'));            

    var Ctrl, scope, view;

    var data = {
        Items: [
        /*jshint camelcase: false */        
        {imageid: '123', instrument: 'fcam', time: {creation_timestamp_utc: 0, received_timestamp_utc: 0}},
        {imageid: '456', instrument: 'fcam', votes: 290, time: {creation_timestamp_utc: 0, received_timestamp_utc: 0}}
        ]
    };

    var marsPhotos = {
        queryWithDateIndex: function(queryParams, callback){
            callback(null, data);
        },
        getThumbnails: function(photos, callback) {
            callback();
        },
        instrumentList: {
            fcam: {id: 'fcam', name: 'Chemcam RMI'}
        }
    };

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($templateCache, $compile, $controller, $rootScope, $modal, $log) {
    	var html = $templateCache.get('views/partials/timeline.html');
    	scope = $rootScope.$new();

    	Ctrl = $controller('TimelineCtrl', {
    		$scope: scope,
    		$log: $log,
            MarsPhotosDBAccess: marsPhotos            
    	});
        
    	view = $compile(angular.element(html))(scope);
    	scope.$digest();
        scope.updatePhotos();
    }));

    it('should show 2 images under timeline content div', function(){
        expect(view.find('.timeline-item .img-responsive').length).toBe(2);
    });

    it('should give imageid as the id for the corresponding panel div', function(){
        expect(view.find('#123').length).toBe(1);
        expect(view.find('#456').length).toBe(1);
    });

    it('should show # of votes on the panel if defined', function(){
        expect(view.find('#456 h4').text()).toBe('# of votes: 290');
    });

    it('should show 0 if # of votes is not set in the photo object', function(){
        expect(view.find('#123 h4').text()).toBe('# of votes: 0');
    });

    it('should contain instrument name in the photo description', function(){
        expect(view.find('#123 .description').text()).toContain('Chemcam RMI');
    });
    
});


