util = require('./util')
nconf = require('./mynconf')

tables = [
    {
    	TableName: nconf.get('TABLE_PHOTOS')
    	AttributeDefinitions: [
            { AttributeName: 'imageid', AttributeType: 'S' }     
    		{ AttributeName: 'Mission+InstrumentID', AttributeType: 'S' }
            { AttributeName: 'TimeStamp', AttributeType: 'N' }
    		 { AttributeName: 'votes', AttributeType: 'N' }		
    	]
    	KeySchema: [
    		{ AttributeName: 'imageid', KeyType: 'HASH' }
    	]
    	GlobalSecondaryIndexes: [
    		{
                IndexName: 'date-gsi'
                KeySchema: [
                    {AttributeName: 'Mission+InstrumentID', KeyType: 'HASH' }
                    {AttributeName: 'TimeStamp', KeyType: 'RANGE' }
                ]
                Projection:
                    ProjectionType: 'INCLUDE'
                    NonKeyAttributes: [ 'url', 'time', 'instrument', 'votes', 'mission' ]
                ProvisionedThroughput: 
                	ReadCapacityUnits: nconf.get('READ_CAPACITY_PHOTOS')
                	WriteCapacityUnits: nconf.get('WRITE_CAPACITY_PHOTOS')
            }
            {
                IndexName: 'vote-gsi'
                KeySchema: [
                    {AttributeName: 'Mission+InstrumentID', KeyType: 'HASH' }
                    {AttributeName: 'votes', KeyType: 'RANGE' }
                ]
                Projection:
                    ProjectionType: 'INCLUDE'
                    NonKeyAttributes: [ 'url', 'time', 'instrument', 'TimeStamp', 'mission' ]
                ProvisionedThroughput: 
                	ReadCapacityUnits: nconf.get('READ_CAPACITY_PHOTOS')
                	WriteCapacityUnits: nconf.get('WRITE_CAPACITY_PHOTOS')
            }        
        ]
    	ProvisionedThroughput:
    		ReadCapacityUnits: nconf.get('READ_CAPACITY_PHOTOS')
    		WriteCapacityUnits: nconf.get('WRITE_CAPACITY_PHOTOS')
    }
    {
        TableName: nconf.get('TABLE_USER_VOTES')
        AttributeDefinitions: [
            { AttributeName: 'userid', AttributeType: 'S' }             
            { AttributeName: 'imageid', AttributeType: 'S' }     
        ]
        KeySchema: [
            { AttributeName: 'userid', KeyType: 'HASH' }
            { AttributeName: 'imageid', KeyType: 'RANGE' }        
        ]
        ProvisionedThroughput:
            ReadCapacityUnits: nconf.get('READ_CAPACITY_PHOTOS')
            WriteCapacityUnits: nconf.get('WRITE_CAPACITY_PHOTOS')
    }
    {
        TableName: nconf.get('TABLE_RESOURCES')
        AttributeDefinitions: [
            { AttributeName: 'resource', AttributeType: 'S' }     
        ]
        KeySchema: [
            { AttributeName: 'resource', KeyType: 'HASH' }
        ]
        ProvisionedThroughput:
            ReadCapacityUnits: nconf.get('READ_CAPACITY_PHOTOS')
            WriteCapacityUnits: nconf.get('WRITE_CAPACITY_PHOTOS')
    }
]

for table in tables
    util.createTable(table, nconf.get('delete_table_if_exists') 
        (error) ->
            console.error error
        (tableName) ->
            console.log "Table #{tableName} is ready"
    )
