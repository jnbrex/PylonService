package com.pylon.pylonservice.util;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MetricsUtil {
    private static final String ENVIRONMENT_DIMENSION_NAME = "Environment";
    private static final String LATENCY_NAMESPACE = "PYLON/LATENCY";
    private static final String COUNT_NAMESPACE = "PYLON/COUNT";
    private static final String SUCCESS_NAMESPACE = "PYLON/SUCCESS";

    private final Dimension environmentDimension;

    @Autowired
    private AmazonCloudWatch amazonCloudWatch;

    MetricsUtil(@Value("${environment.name}") final String environmentName) {
        environmentDimension = new Dimension()
            .withName(ENVIRONMENT_DIMENSION_NAME)
            .withValue(environmentName);
    }

    public void addLatencyMetric(@NonNull final String metricName, final long nanoTime) {
        final MetricDatum metricDatum = new MetricDatum()
            .withMetricName(metricName)
            .withUnit(StandardUnit.Milliseconds)
            .withValue((double) TimeUnit.MILLISECONDS.convert(nanoTime, TimeUnit.NANOSECONDS))
            .withDimensions(environmentDimension);

        addMetric(metricDatum, LATENCY_NAMESPACE);
    }

    public void addCountMetric(@NonNull final String metricName) {
        final MetricDatum metricDatum = new MetricDatum()
            .withMetricName(metricName)
            .withUnit(StandardUnit.None)
            .withValue(1D)
            .withDimensions(environmentDimension);

        addMetric(metricDatum, COUNT_NAMESPACE);
    }

    public void addSuccessMetric(@NonNull final String metricName) {
        final MetricDatum metricDatum = new MetricDatum()
            .withMetricName(metricName)
            .withUnit(StandardUnit.None)
            .withValue(1D)
            .withDimensions(environmentDimension);

        addMetric(metricDatum, SUCCESS_NAMESPACE);
    }

    private void addMetric(@NonNull final MetricDatum metricDatum, @NonNull final String namespace) {
        amazonCloudWatch.putMetricData(
            new PutMetricDataRequest()
                .withNamespace(namespace)
                .withMetricData(metricDatum)
        );
    }
}
