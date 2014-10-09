nconf = require 'nconf' 

nconf.argv()
	 .env()

nconf.defaults
	DYNAMODB_ENDPOINT_DEV: 'http://localhost:9000/dynamodb/'
	DYNAMODB_REGION_DEV: 'us-east-1'
	USE_COGNITO_DEV: false
	DYNAMODB_ENDPOINT_TEST: 'http://localhost:8080/dynamodb/'
	DYNAMODB_REGION_TEST: 'us-east-1'
	USE_COGNITO_TEST: false	
	DYNAMODB_ENDPOINT_PROD: 'http://dynamodb.us-east-1.amazonaws.com/'
	DYNAMODB_REGION_PROD: 'us-east-1'	
	USE_COGNITO_PROD: true
	AWS_ACCOUNT_ID: 'DummyAWSAccountID'
	COGNITO_IDENTITY_POOL_ID: 'DummyCognitoIdenityPoolID'
	COGNITO_UNAUTH_ROLE_ARN: 'DummyCognitoUnauthRoleARN'
	TABLE_PHOTOS: 'marsDemoImages'
	TABLE_USER_VOTES: 'userVotes'	
	TABLE_RESOURCES: 'marsDemoResources'		
	READ_CAPACITY_PHOTOS: 1
	WRITE_CAPACITY_PHOTOS: 1

module.exports = nconf
