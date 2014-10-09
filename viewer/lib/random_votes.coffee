util = require('./util')
nconf = require('./mynconf')


fetchPhotos = (lastEvaluatedKey, done) ->
	params =
		TableName: nconf.get('TABLE_PHOTOS')
		AttributesToGet: ['imageid']

	if lastEvaluatedKey 
		params.ExclusiveStartKey = lastEvaluatedKey

	util.dynamoDB.scan(
		params
		(error, data) ->
			unless error
				done(data)
			else 
				console.error error
	)

voteRandom = (data) ->
	for photo in data.Items
		votes = Math.floor((Math.random() * 1000) + 1)
		console.log "Voting #{votes} on #{photo.imageid}"
		params = 
			TableName: nconf.get('TABLE_PHOTOS')
			Key: 
				imageid: photo.imageid
			AttributeUpdates: 
				votes: 
					Action: 'ADD'
					Value: votes

		util.dynamoDB.updateItem(
			params
			(error, data) ->
				console.error error if error
		)
	if data.LastEvaluatedKey 
		fetchPhotos(data.LastEvaluatedKey, voteRandom)





fetchPhotos(null, voteRandom)
