## Overview
The Mars Science Laboratory Image Explorer is a data exploration application that allows a user to view Mars Rover images. This is a sample application to demonstrate the following features of DynamoDB:
- JSON data support - image data is from http://json.jpl.nasa.gov and packaged in json format
- Indexes on JSON data - indexes are built on top of the json data for faster querying
- Large Items - thumbnail data can be pre-generated and stored in DynamoDB as binary
- DynamoDB Document SDK - hides low level data structure in Amazon DynamoDB and provides document level API with developers

It has timeline view and the user can look into specific point in time. The user can also switch from one equipment to another with the control panel on the side menu. Users can also vote on images and show images based on popularity rather than time. Each use case is implemented with Amazon DynamoDB.

## Getting started
1. Install node.js and ruby for your platform if not installed
   - node.js: http://nodejs.org/download/
   - ruby: https://www.ruby-lang.org/en/installation/
2. Install necessary tools, i.e. grunt, bower, coffee-script and compass
```
	sudo npm -g install grunt-cli bower coffee-script
  sudo gem install compass
```
3. Install dependencies
```
  npm install
  bower install
```
4. Run the following command to start the demo app. 
```
	grunt serve
```
This will download DynamoDB Local, ingest a small subset of images from NASA ,launches a local HTTP server and opens the demo app.

## Data Model
### <marsDemoImages table>
- HashKey: imageid
- RangeKey: ''none''
- GSI 1 (date-gsi)
  - HashKey: Mission+InstrumentID (concatenated)
  - RangeKey: TimeStamp
  - Projection (time, votes, mission, instrument)
- GSI 2 (vote-gsi)
  - HashKey: Mission+InstrumentID (concatenated)
  - RangeKey: votes

### <userVotes table>
- HashKey: userid
- RangeKey: imageid

### Queries used to retrieve data
- Fetching timeline photos: Query marsDemoImages with date-gsi + getItem on each imageid to get thumbnails
- Fetching top voted photos: Query marsDemoImages with vote-gsi + getItem on each imageid to get thumbnails
- Fetching user voted photos: Query userVotes table + getItem on each imageid to get thumbnails
- Voting on a photo: Conditional Update on userVotes table with (userid, imageid) and, if it successes, add 1 to votes attribute on marsDemoImages table

## Building distributable package
1. Edit the configuration in `lib/mynconf.coffee` as appropriate or overwride the parameters with environment variables with the same name. The default configuration is as follows.
```
nconf.defaults
  DYNAMODB_ENDPOINT_DEV: 'http://localhost:9000/dynamodb/'
  DYNAMODB_REGION_DEV: 'us-east-1'
  DYNAMODB_ENDPOINT_TEST: 'http://localhost:8080/dynamodb/'
  DYNAMODB_REGION_TEST: 'us-east-1'
  DYNAMODB_ENDPOINT_PROD: 'http://dynamodb.us-east-1.amazonaws.com/'
  DYNAMODB_REGION_PROD: 'us-east-1' 
  USE_COGNITO_DEV: false
  USE_COGNITO_TEST: false
  USE_COGNITO_PROD: true
  AWS_ACCOUNT_ID: 'DummyAWSAccountID'
  COGNITO_IDENTITY_POOL_ID: 'DummyCognitoIdenityPoolID'
  COGNITO_UNAUTH_ROLE_ARN 'DummyCognitoUnauthRoleARN'
  TABLE_PHOTOS: 'marsDemoImages'
  TABLE_USER_VOTES: 'userVotes' 
  TABLE_RESOURCES: 'marsDemoResources'
  READ_CAPACITY_PHOTOS: 1
  WRITE_CAPACITY_PHOTOS: 1
```

2. Run the following command
```
  grunt build
```

The above step will create two distribution directories. 
- dist -- contains the web app that can be deployed on an HTTP server, e.g. Amazon S3
- dist-launcher -- contains the web app and a grunt script to launch the web app locally

## Automated Test
The following command performs unit tests with DynamoDB Local.
```
  grunt test
```