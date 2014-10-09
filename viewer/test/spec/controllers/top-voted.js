'use strict';

describe('Controller: TopVotedCtrl', function () {

    // load the controller's module
    beforeEach(module('MSLImageExplorerApp'));

    var TopVotedCtrl, scope, log, controller, timeout, location, controllerArgs, path;

    var data = {
    	Items: [
    	{imageid: '123'},
    	{imageid: '456'}
    	]
    };

    var marsPhotos = {
    	queryWithVoteIndex: function(queryParams, callback){
    		data.LastEvaluatedKey = {
    			'Mission+InstrumentID': queryParams.hashKey,
    			'votes': 100
    		};
    		callback(null, data);
    	},
        getThumbnails: function(photos, callback){
            callback();
        },
        defaultInstrument: 'fcam',
        defaultMission: 'curiosity'
    };


    var Blueimp = {
    	Gallery: function() {}
    };

    beforeEach(function(){
    	inject(function ($controller, $rootScope, $log) {
    		$log.debug = function(msg) { console.log(msg); };
    		scope = $rootScope.$new();    		
    		log = $log;
    		location = {
    			path: function(_path){
    				path = _path;
    			}
    		};
    		controller = $controller;
    		timeout = function(callback) {
    			callback();
    		};

    		controllerArgs = {
    			$scope: scope,
    			$log: log,
    			$routeParams: {},
    			$timeout: timeout,
    			$location: location,
    			MarsPhotosDBAccess: marsPhotos,
    			Blueimp: Blueimp
    		};
    		TopVotedCtrl = controller('TopVotedCtrl', controllerArgs);
       	});
    });

    it('should attach a list of user voted photos to the scope', function(){
    	expect(scope.photos.length).toBe(2);
    });

    it('should set instrument to default if it is not specified in routeParams', function (){
    	expect(scope.instrument).toBe(marsPhotos.defaultInstrument);
    	expect(scope.missionInstrument).toBe(marsPhotos.defaultMission + '+' + marsPhotos.defaultInstrument);      
    });

    it('should set instrument to the one specified in routeParams', function (){
    	controllerArgs.$routeParams = {instrument: 'mastcam_right'};
    	TopVotedCtrl = controller('TopVotedCtrl', controllerArgs);
    	expect(scope.instrument).toBe('mastcam_right');
    	expect(scope.missionInstrument).toBe(marsPhotos.defaultMission + '+mastcam_right');    	
    });

    it('should set a function to reload', function(){
    	expect(typeof(scope.reload)).toBe('function');
    });

    it('should set path to /topVoted/ + instrument ID when it is reloaded', function(){
    	scope.instrument = 'mastcam_left';
    	scope.reload();
    	expect(path).toBe('/topVoted/mastcam_left');
    });

    it('should update photos and set lastEvaluatedKey', function(){
    	scope.missionInstrument = marsPhotos.defaultMission + '+mastcam_left';    
    	scope.updatePhotos();
    	expect(scope.lastEvaluatedKey['Mission+InstrumentID']).toBe(marsPhotos.defaultMission + '+mastcam_left');
    });
});
