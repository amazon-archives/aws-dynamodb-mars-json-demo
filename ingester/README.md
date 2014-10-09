## Features
Easily store and index the images of Mars published by NASA Jet Propulsion Laboratory. This project includes a Java application for storing the data in DynamoDB or DynamoDB Local. Provides an example of how to store JSON data in DynamoDB using the low-level Java SDK.

## Getting Started
- Option 1: Sign up for Amazon Web Services
- Option 2: Get DynamoDB Local

## Minimum Requirements
  - Java 1.7+
  - Maven

## Building the Image Ingester from Source
You can build the Java application using Maven. Use this command: mvn clean install
``` 
    > mvn clean install
```

## Running the Application
1. Set up DynamoDB or DynamoDB Local
  - Option 1: Using DynamoDB
		Get IAM credentials that allow for DynamoDB operations. (See [IAM Introduction](http://docs.aws.amazon.com/IAM/latest/UserGuide/IAM_Introduction.html))
  - Option 2: Using DynamoDB Local
		Run the DynamoDB Local server with the following command (See [DynamoDB Local](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html for more options)): 

		```
            java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar 
        ```
        
2. Configure Image Ingester
    Options for the image ingester are contained in the ImageIngester.properties file found in the root directory. The included file has some default settings that run the demo with DynamoDB Local.
3. Run the Image Ingester
	mvn exec:exec

## Release Notes
