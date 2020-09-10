package com.pylon.pylonservice.util;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TransactionCanceledException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class DynamoDbUtil {
    private DynamoDbUtil() {}

    public static void executeTransactionWrite(final DynamoDBMapper dynamoDBMapper,
                                               final TransactionWriteRequest transactionWriteRequest) {
        try {
            dynamoDBMapper.transactionWrite(transactionWriteRequest);
        } catch (DynamoDBMappingException ddbme) {
            log.error(String.format(
                "Client side error in Mapper, fix before retrying. Error: %s", ddbme.getMessage()));
            throw ddbme;
        } catch (ResourceNotFoundException rnfe) {
            log.error(String.format(
                "One of the tables was not found, verify table exists before retrying. Error: %s", rnfe.getMessage()));
            throw rnfe;
        } catch (InternalServerErrorException ise) {
            log.error(String.format(
                "Internal Server Error, generally safe to retry with back-off. Error: %s", ise.getMessage()));
            throw ise;
        } catch (TransactionCanceledException tce) {
            log.error(String.format(
                "Transaction Canceled, implies a client issue, fix before retrying. Error: %s", tce.getMessage()));
            throw tce;
        } catch (Exception ex) {
            log.error(String.format(
                "An exception occurred, investigate and configure retry strategy. Error: %s", ex.getMessage()));
            throw ex;
        }
    }
}
