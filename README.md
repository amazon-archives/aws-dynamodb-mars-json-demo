## Note
This repository has been archived and may be deleted at any time. You have been warned.

## Features
Easily store and index the images of Mars published by NASA Jet Propulsion Laboratory. This project includes a Java application for storing the data in DynamoDB or DynamoDB Local and a frontend web application for interacting with and display the images. Provides an example of how to store JSON data in DynamoDB using the low-level Java SDK and query data from DynamoDB using the DynamoDB Document SDK for JavaScript.

## Getting Started
To run the demo locally with DynamoDB Local with a small subset of metadata, please run the following commands
```
    > cd viewer
    > npm install
    > bower install
    > grunt serve
```

## Minimum Requirements 
- Java 1.7+
- NodeJS
  - npm
  - bower
  - grunt
  - coffee-script
- Ruby
  - compass
- Maven

## Building from Source
### Image Ingester
You can build the Java application using Maven. Go to the directory `ingester` and run the following command
``` 
    > mvn clean install
```

### Image Viewer
You can build and run the frontend web application with the following commands.
```
    > npm install
    > bower install
    > grunt build
```

## Data Source / Data Ingestion Scheme

JSON image data is from [http://json.jpl.nasa.gov/data.json http://json.jpl.nasa.gov/data.json]. The image ingester is included and found under directory `photo_ingester`. 

## Release Notes
