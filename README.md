## PylonService Local Development

### Setup

1. Create an IAM user in the AWS Account with accountId: 252737728016 and name: pylon with the following policies attached
    * S3FullAccess
    * DynamoDbFullAccess
    * CloudWatchFullAccess

2. Set the following environment variables:
```
export JWT_SECRET={Any long and random string. Create your own, don't use the one for Beta or Prod or someone else's! Example: foawijfhi8y97guih8g76fdtcyf6d5s4dads4rxtcdyf67tuyg7t8fuh8y9guij089gui89g7yvbiuh78f6ctyvuf67d5xrs4dzerxtd54sa3ezrxs43asa2wzs34erszxtd56srxtcyuvf67vyubih8g97buhoij90h8inoj9h08g7biunoh89g7yv}
export AWS_ACCESS_KEY_ID={IAM user access key}
export AWS_SECRET_ACCESS_KEY={IAM user secret key}
export AWS_REGION=us-east-1
export SERVICE_REGION=us-east-1
export ENVIRONMENT_NAME=local
export NEPTUNE_WRITER_ENDPOINT=localhost
export NEPTUNE_READER_ENDPOINT=localhost
```

3. Download **Gremlin Server** from https://tinkerpop.apache.org
4. Unzip the download and navigate to the root of the package
5. Start a local gremlin server listening on port 8182: `/bin/gremlin-server.sh start`
6. Run the service using either IntelliJ or `./mvnw spring-boot:run`