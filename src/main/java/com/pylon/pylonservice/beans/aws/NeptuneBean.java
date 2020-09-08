package com.pylon.pylonservice.beans.aws;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.SigV4WebSocketChannelizer;
import org.apache.tinkerpop.gremlin.driver.ser.Serializers;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static com.pylon.pylonservice.constants.EnvironmentConstants.LOCAL_ENVIRONMENT_NAME;
import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Component
public class NeptuneBean {
    @Bean(name = "writerCluster")
    public Cluster writerCluster(@Value("${neptune.writer.endpoint}") final String writerEndpoint,
                                 @Value("${environment.name}") final String environmentName) {
        return buildCluster(writerEndpoint, environmentName);
    }

    @Bean(name = "readerCluster")
    public Cluster readerCluster(@Value("${neptune.reader.endpoint}") final String readerEndpoint,
                                 @Value("${environment.name}") final String environmentName) {
        return buildCluster(readerEndpoint, environmentName);
    }

    private Cluster buildCluster(final String endpoint, final String environmentName) {
        Cluster.Builder clusterBuilder = Cluster.build()
            .addContactPoint(endpoint)
            .port(8182)
            .minConnectionPoolSize(2)
            .maxConnectionPoolSize(512)
            .serializer(Serializers.GRAPHBINARY_V1D0);

        // Make local development with an in-memory gremlin-server database work
        if (!environmentName.equals(LOCAL_ENVIRONMENT_NAME)) {
            clusterBuilder = clusterBuilder
                .channelizer(SigV4WebSocketChannelizer.class)
                .enableSsl(true)
                .keyCertChainFile("SFSRootCAG2.pem");
        }

        return clusterBuilder.create();
    }

    @Bean(name = "writer")
    public GraphTraversalSource writer(@Qualifier("writerCluster") final Cluster cluster) {
        return traversal().withRemote(DriverRemoteConnection.using(cluster));
    }

    @Bean(name = "reader")
    public GraphTraversalSource reader(@Qualifier("readerCluster") final Cluster cluster) {
        return traversal().withRemote(DriverRemoteConnection.using(cluster));
    }
}
