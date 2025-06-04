package com.github.starter.core.observability;
import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
@Configuration
public class OpenTelemetryConfig {
    @Value("${spring.application.name}")
    private String applicationName;
    @Bean
    public OpenTelemetry openTelemetry(Environment environment) {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
                        .put("service.name", applicationName)
                        .put("service.version", environment.getProperty("core.version", "unknown"))
                        .put("deployment.environment", environment.getProperty("core.environment", "unknown"))
                        .build()));
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(Sampler.alwaysOn())
                .build();
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::close));
        return openTelemetrySdk;
    }
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(applicationName);
    }
    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
}
