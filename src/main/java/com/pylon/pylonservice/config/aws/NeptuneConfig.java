package com.pylon.pylonservice.config.aws;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Component
public class NeptuneConfig {
    private static final int NEPTUNE_PORT = 8182;

    @Bean(name = "writerCluster")
    public Cluster writerCluster(@Value("${neptune.writer.endpoint}") final String writerEndpoint) {
        return buildCluster(writerEndpoint);
    }

    @Bean(name = "readerCluster")
    public Cluster readerCluster(@Value("${neptune.reader.endpoint}") final String readerEndpoint) {
        return buildCluster(readerEndpoint);
    }

    private Cluster buildCluster(final String endpoint) {
        return Cluster.build()
            .addContactPoint(endpoint)
            .port(NEPTUNE_PORT)
            .enableSsl(true)
            .keyCertChainFile("SFSRootCAG2.pem")
            .create();
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
