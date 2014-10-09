'use strict';

describe('Controller: TimelineCtrl', function () {

    // load the controller's module
    beforeEach(module('MSLImageExplorerApp'));

    var TimelineCtrl, scope, log, controller, timeout, location, controllerArgs, path;

    var data = {
        /*jshint camelcase: false */
    	Items: [
    	{imageid: '123', time: {creation_timestamp_utc: 0, received_timestamp_utc: 0}},
    	{imageid: '456', time: {creation_timestamp_utc: 0, received_timestamp_utc: 0}}
    	]
    };

    var marsPhotos = {
    	queryWithDateIndex: function(queryParams, callback){
    		setTimeout(function(){
    			data.LastEvaluatedKey = {
    				'Mission+InstrumentID': queryParams.hashKey
    			};    			
    			callback(null, data);
    		}, 5);
    	},
    	voteOnPhoto: function(userid, photo, callback){
    		if(photo.imageid === 'already_voted'){
    			callback('You have already voted');
    		} else {
    			callback(null, {Attributes: {votes: photo.votes + 1}});
    		}
    	},
        getThumbnails: function(photos, callback) {
            callback();
        },
        defaultInstrument: 'fcam',
        defaultMission: 'curiosity'
    };

    beforeEach(function(){
    	inject(function ($controller, $rootScope, $log) {
    		$log.debug = function(msg) { console.log(msg); };
    		$log.error = function(msg) { console.error(msg); };    		
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
    			MarsPhotosDBAccess: marsPhotos
    		};
    		TimelineCtrl = controller('TimelineCtrl', controllerArgs);
       	});
    });

    it('should not be updating photos just after loading page', function(){
    	expect(scope.isUpdatingPhotos).toBe(false);
    	expect(scope.photos.length).toBe(0);
    });

    it('should show date picker on the mission control', function(){
    	expect(scope.showDatePicker).toBe(true);
    });

    it('should attach a list of timeline photos to the scope once updatePhotos() is called', function(done){
    	scope.updatePhotos();
    	setTimeout(function(){
    		expect(scope.photos.length).toBe(2);
    		done();
    	}, 10);
    });


    it('should set isUpdatingPhotos flag while loading photos', function(done){
    	scope.updatePhotos();
    	expect(scope.isUpdatingPhotos).toBe(true);
    	setTimeout(function(){
    		expect(scope.isUpdatingPhotos).toBe(false);
    		done();
    	}, 10);
    });

    it('should set instrument to default if it is not specified in routeParams', function (){
    	expect(scope.instrument).toBe(marsPhotos.defaultInstrument);
    	expect(scope.missionInstrument).toBe(marsPhotos.defaultMission + '+' + marsPhotos.defaultInstrument);    	
    });

    it('should set instrument to the one specified in routeParams', function (){
    	controllerArgs.$routeParams = {instrument: 'mastcam_right'};
    	TimelineCtrl = controller('TimelineCtrl', controllerArgs);
    	expect(scope.instrument).toBe('mastcam_right');
    	expect(scope.missionInstrument).toBe(marsPhotos.defaultMission + '+mastcam_right');    	
    });

    it('should set time to if specified in routeParams', function (){
    	var time = new Date().getTime();
    	controllerArgs.$routeParams = {time: time};
    	TimelineCtrl = controller('TimelineCtrl', controllerArgs);
    	expect(scope.time).toBe(time);
    });

    it('should not set time if route param is not a valid integer', function (){
    	var time = 'not_integer';
    	controllerArgs.$routeParams = {time: time};
    	TimelineCtrl = controller('TimelineCtrl', controllerArgs);
    	expect(scope.time).toBe(undefined);
    });

    it('should set a function to reload', function(){
    	expect(typeof(scope.reload)).toBe('function');
    });

    it('should set path to /timeline/ + instrument ID when it is reloaded', function(){
    	scope.instrument = 'mastcam_left';
    	scope.reload();
    	expect(path).toBe('/timeline/mastcam_left');
    });

    it('should set path to /timeline/instrumentID/time if time is specified and reload is called', function(){
    	scope.instrument = 'mastcam_left';
    	var time = new Date().getTime();
    	scope.time = time;
    	scope.reload();
    	expect(path).toBe('/timeline/mastcam_left/' + time);
    });    

    it('should update photos and set lastEvaluatedKey', function(done){
    	scope.missionInstrument = marsPhotos.defaultMission + '+mastcam_left';
    	scope.updatePhotos();
    	setTimeout(function(){
    		expect(scope.lastEvaluatedKey['Mission+InstrumentID']).toBe(marsPhotos.defaultMission + '+mastcam_left');    		
    		done();
    	}, 10);
    });
    
    it('should increase # of votes for a photo which is not yet voted by the user', function(){
    	var photo = {
    		imageid: 'not_voted_yet',
    		votes: 1000
    	};
    	scope.vote(photo);
    	expect(photo.votes).toBe(1001);
    });

    it('should not increase # of votes for a photo which is already voted by the user', function(){
    	var photo = {
    		imageid: 'already_voted',
    		votes: 1000
    	};
    	scope.vote(photo);
    	expect(photo.votes).toBe(1000);
    });

});
