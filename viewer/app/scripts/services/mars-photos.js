'use strict';
/**
 * @ngdoc function
 * @name MSLImageExplorerApp.service:MarsPhotosDBAccess
 * @description
 * # MarsPhotosDBAccess
 * Service of the MSLImageExplorerApp. The service offers utility functions to 
 * access DynamoDB tables. 
 */
 angular.module('MSLImageExplorerApp').service('MarsPhotosDBAccess', function ($log, ENV, AWS){
 	var MarsPhotosDBAccess = {
 		/**
		* Queries photos table with the date index and give the result
		* to callback function. 
		* @param {object} queryParams - The query parameters used in the query. The parameter 
		* hashKey is required. The lastEvaluatedKey and rangeKey are optional.
		* @param {function} callback - The callback function used to return the result.
		*/
		queryWithDateIndex: function(queryParams, callback){
			assertHashKey(queryParams);
			assertFunction(callback);

			var params = { 
				TableName: ENV.photosTable,
				KeyConditions: [
				AWS.dynamoDB.Condition('Mission+InstrumentID', 'EQ', queryParams.hashKey)
				],
				IndexName: 'date-gsi',
				Limit: 5,
				ScanIndexForward: false
			};
			if(hasLastEvaluatedKey(queryParams)) {
				params.ExclusiveStartKey = queryParams.lastEvaluatedKey;
			}
			if(hasRangeKey(queryParams)){
				params.KeyConditions.push (AWS.dynamoDB.Condition('TimeStamp', 'LE', queryParams.rangeKey));
			}
			logRequest('query', params);
			AWS.dynamoDB.query(params, callback);
		},

		/**
		* Queries photos table with the vote index and give the result
		* to callback function. 
		* @param {object} queryParams - The query parameters used in the query. The parameter 
		* hashKey is required. The lastEvaluatedKey and rangeKey are optional.
		* @param {function} callback - The callback function used to return the result.
		*/
		queryWithVoteIndex: function(queryParams, callback) {
			assertHashKey(queryParams);
			assertFunction(callback);				

			var params = { 
				TableName: ENV.photosTable,
				KeyConditions: [
				AWS.dynamoDB.Condition('Mission+InstrumentID', 'EQ', queryParams.hashKey)
				],
				IndexName: 'vote-gsi',
				ScanIndexForward: false
			};
			if(hasLastEvaluatedKey(queryParams)) {
				params.ExclusiveStartKey = queryParams.lastEvaluatedKey;
			}
			if(hasRangeKey(queryParams)){
				params.KeyConditions.push (AWS.dynamoDB.Condition('votes', 'LE', queryParams.rangeKey));
			}
			logRequest('query', params);
			AWS.dynamoDB.query(params, callback);
		},

		/**
		* Queries user votes table and give the result to the callback function. 
		* @param {object} queryParams - The query parameters used in the query. The parameter 
		* hashKey is required. The lastEvaluatedKey is optional.
		* @param {function} callback - The callback function used to return the result.
		*/
		queryUserVotedPhotos: function(queryParams, callback) {
			assertHashKey(queryParams);				
			assertFunction(callback);

			var params = { 
				TableName: ENV.userVotesTable,
				KeyConditions: AWS.dynamoDB.Condition('userid', 'EQ', queryParams.hashKey),
				ScanIndexForward: false
			};    
			if(hasLastEvaluatedKey(queryParams)) {
				params.ExclusiveStartKey = queryParams.lastEvaluatedKey;
			}
			logRequest('query', params);
			AWS.dynamoDB.query(params, callback);
		},

		/**
		* Votes on the specified photo and notes that in the votes table. 
		* It uses conditional update
		* and succeeds only if the user votes on the photo for the first time.
		* @param {String} userid - The ID of the user who votes on the photo.
		* @param {Object} photo - The photo object to vote on.
		* @param {function} callback - The callback function used to return the result or error.
		*/
		voteOnPhoto: function(userid, photo, callback) {
			assertUserid(userid);
			assertPhoto(photo);
			assertFunction(callback);				

			var item = {};
			for (var key in photo){
				// Skip thumbnail data and project other metadata
				if(key === 'data'){
					continue;
				} 
				item[key] = photo[key];
			}
			item.userid = userid;
			var params = {
				TableName: ENV.userVotesTable,
				Item: item,
				Expected: AWS.dynamoDB.Condition('imageid', 'NULL')
			};
			logRequest('putItem', params);
			AWS.dynamoDB.putItem(params, function(error) {
				if (!error) {
					$log.debug('Liked image successfully');
					incrementVotesCount(photo.imageid, callback);
				} else {
					if(error.code === 'ConditionalCheckFailedException'){
						callback('You have already voted on this image');
					} else {
						$log.error(error);
					}
				}
			});
		},

		
		/**
		* List of instruments and its display names.
		*/
		instrumentList: {
			'fcam': {id: 'fcam', name: 'Front Hazcam'},
			'ccam': {id: 'ccam', name: 'Chemcam RMI'},			
			'mastcam_right': {id: 'mastcam_right', name: 'Right Mastcam'},
			'mastcam_left': {id: 'mastcam_left', name: 'Left Mastcam'},
			'mahli': {id: 'mahli', name: 'MAHLI'},
			'mardi': {id: 'mardi', name: 'MARDI'}			
		},

		/**
		* Default mission ID
		*/
		defaultMission: 'curiosity',

		/**
		* Default instrument ID
		*/
		defaultInstrument: 'fcam'
	};

	/**
	* Increments the voting count in the photo table. It gets the updated 
	* votes count in return. The function is meant to be used privately 
	* in MarsPhoto service. 
	* @param {String} imageid - The ID of the photo to increment vote count.
	* @param {function} callback - The callback function used to return the result or error.
	*/
	var incrementVotesCount = function(imageid, callback) {
		var params = {
			TableName: ENV.photosTable,
			Key: { imageid: imageid },
			UpdateExpression: 'add votes :v',
			ExpressionAttributeValues: {':v': 1},
			ReturnValues: 'UPDATED_NEW'
		};

		logRequest('updateItem', params);
		AWS.dynamoDB.updateItem(params, callback);
	};

	 var assertHashKey = function(queryParams){
	 	if(!queryParams || typeof queryParams.hashKey === 'undefined'){
	 		throw 'A required parameter, hash key is missing.';
	 	}
	 };

	 var assertPhoto = function(photo) {
	 	if(! photo || typeof photo.imageid === 'undefined'){
	 		throw 'Invalid object was given as a photo: ' + photo;
	 	}
	 };

	 var assertUserid = function(userid) {
	 	if(typeof userid === 'undefined'){
	 		throw 'User ID was invalid: ' + userid;
	 	}
	 };

	 var assertFunction = function(callback){
	 	if(typeof callback !== 'function'){
	 		throw 'No valid callback function was given: ' + callback;
	 	}
	 };

	 var hasLastEvaluatedKey = function(queryParams){
	 	return queryParams && typeof queryParams.lastEvaluatedKey !== 'undefined';
	 };

	 var hasRangeKey = function(queryParams){
	 	return queryParams && typeof queryParams.rangeKey  !== 'undefined';
	 };

	 var logRequest = function(method, params){
	 	$log.debug('Requesting DynamoDB to ' + method + ' with the following parameters');
	 	$log.debug(params);
	 };

	 return MarsPhotosDBAccess;
});
