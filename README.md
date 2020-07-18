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

## PylonService Graph Model
http://www.plantuml.com/plantuml/uml/ZLBBRXGn4BpxArfpJqAk752GkEo1XxJA1tZitjrMR7UmspkA4F-EFGOy0yEGqwfggwfBDNv8egDaxvHnEYLuJ1ZXcm8uIgHmXbnmq1xXxWvoeAyPWIm6eHCL8Pz0BjHSgflqLPVKn-WwDH9tuDXe8Gw56PaTwW1NZ3NIbfHwwiY6j8EFQ4b32KhTVD86Y-jf5IyJYU3w98Ka-Xor_qjfBYlgXRFa4TSoUf8_Ramu9f9PLiWFMX1CnF9Ztulw_dE_BvnakTzpmGUxlrxrgELfSTWXUNt6W-s4uI0bOpqlsvUTLiUBZdPPArNYq_--Q_DjvgeMERo1sri80zpSlEkW_VSM1WeNZ2Hf9ePTJtr2Iql5Wr8pFTp_wm1FuLVujh9kMuKfZvv4q5PYMzmxdDWvdbg5lGPT-xeI-VdAlokecTqOutwHNfyFsaDeyNkMrhqxAjunz5A6lSSWdqEhzO31Pkz-0000