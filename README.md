# PylonService Local Development

## AWS Infrastructure

There is AWS infrastructure setup required that isn't covered here. PylonService has dependencies on S3, DynamoDB, SES, Neptune, and CloudWatch.

## Setup

1. Create an IAM user in an AWS Account with the following policies:
    * S3FullAccess
    * DynamoDbFullAccess
    * CloudWatchFullAccess
    * AmazonSESFullAccess

2. Set the following environment variables in your default shell (for example, in `~/.zshrc` or `~/.bash_config`):
```
export JWT_SECRET={Any long and random string. Create your own, don't use the example! Example: foawijfhi8y97guih8g76fdtcyf6d5s4dads4rxtcdyf67tuyg7t8fuh8y9guij089gui89g7yvbiuh78f6ctyvuf67d5xrs4dzerxtd54sa3ezrxs43asa2wzs34erszxtd56srxtcyuvf67vyubih8g97buhoij90h8inoj9h08g7biunoh89g7yv}
export AWS_ACCESS_KEY_ID={IAM user access key}
export AWS_SECRET_ACCESS_KEY={IAM user secret key}
export AWS_REGION=us-west-2
export AWS_DEFAULT_REGION=us-west-2
export SERVICE_REGION=us-west-2
export ENVIRONMENT_NAME=local
export NEPTUNE_WRITER_ENDPOINT=localhost
export NEPTUNE_READER_ENDPOINT=localhost
export EMAIL_FROM_ADDRESS=local-noreply@pylon.gg
```
3. Rename SFSRootCAG2.pem.example to SFSRootCAG2.pem and replace with a certificate to connect to AWS Neptune.
4. Download **Gremlin Server** from https://tinkerpop.apache.org.
5. Unzip the downloaded file and navigate to the root directory of the package.
6. Start a local gremlin server listening on port 8182: `/bin/gremlin-server.sh start`.

## Run Unit Tests Locally
`mvn install`

## Run Server Locally
### Command Line
`./mvnw spring-boot:run`
### IntelliJ
Use a run configuration like https://imgur.com/a/Mkqi8yF.

## PylonService Graph Model
http://www.plantuml.com/plantuml/uml/ZL9DRzGm4BtxLrYvjwLN3gWV22aIKAHqGHndu-bcL6TSxCck4EA_uqbOM9CqzBP-7_jUPkOg2KGPUczQ3odP8M6qFuorZKJYtVXcwD5UNDY1wTk0bXooq0DbONYmqb6ocwmoSzCj96oG4mdjiQIlzv23wnvsMBkIkW5cV8QYK0BlGmj2WJEw3S4Zy0X_ngW5dNRAnPlG4dZx0Hs1tKIVoZzv1orczsDcIkB-G28OPtZDIM0NeLzqjDqC_Xe66ROubzEZBEH-mMqY6UMPU0k2jeq8WkvQpC_VcteAIKutfU_QhU_fKzQOqd9Fuh64PgDNy3Qm88jsAl2RuBwlj4mTH7TQSmG-_lUY6fMPkQsygSLWCsLMh-pvkUNAdfsz-VkOkDUs6d_Kt64aIMDQjMhKlSnyUlULCIDNNJuNfVJTWOyfosgTvL7S3DjUBy_flkQ6ij1umS7xS9ZghBOfz2ylj7dQXpzrZ39BKxeyIL6Doj7LFCMisJJZNvT-pAhfM72wphv7bZkUAbuXkw7tlm00
https://imgur.com/a/BILmsex
