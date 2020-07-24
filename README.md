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
6. Run the service using either IntelliJ run configuration like https://imgur.com/a/Mkqi8yF or `./mvnw spring-boot:run`

## PylonService Graph Model
http://www.plantuml.com/plantuml/uml/ZLBDRjH03BxFKrYvjwLN3gWjg58ae4ZR1N7qPjnTgnDFcN6wGeXt9qvOs27Pq5lc-x6_sBxAYacxDhWcOCxGPKhmmm6iDB5iXxTWIt1n0Ptmjuw0FOdo0_V2-02wumphNkLAq_KpAgQwnItLlgHkK8HEK1nBu7riK3bAZsvY38Gom9yfMGBl75YoLMmO0tmWpmZh579Pv1ORwjsFFLCIztjMfJJ1QycAsuJjhAFPJU2liTDkGrCvF-fCto-qoQo3l2VUeH8qYL39Nwlx-NiLJp7hyIhiNVlbHNpgDQwqtBC6Ag79w0ry6qL9jCXg-4tqtnTIvXqcVnHp13x-zsHM9fkjwyQIrIiuFmUfuEpipT_7oBs6QlYePKU9DG_5gaMZZNtYixkjd1kuwl9KAwFlzdBeCg-sKHt4gsxJiYfvMySfWw47msqC8Ux7E8jfIlsB0wsVdkEVECwOkIbT7dKnZSd9rpAsMR9PZtzTzZEBfaF0yVZQbaJlP8nuHUAxDlm2
https://imgur.com/a/BILmsex
