package com.econage.ai.base.vectorstore.milvus;

import com.econage.ai.support.strategy.EconageTokenCountBatchingStrategy;
import io.micrometer.observation.ObservationRegistry;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.autoconfigure.vectorstore.milvus.MilvusServiceClientConnectionDetails;
import org.springframework.ai.autoconfigure.vectorstore.milvus.MilvusServiceClientProperties;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import com.econage.ai.base.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author hanpeng
 * @date 2025/2/18 11:11
 */
@AutoConfiguration
@EnableConfigurationProperties({ MilvusServiceClientProperties.class })
public class MilvusAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(MilvusServiceClientConnectionDetails.class)
    PropertiesMilvusServiceClientConnectionDetails milvusServiceClientConnectionDetails(
            MilvusServiceClientProperties properties) {
        return new PropertiesMilvusServiceClientConnectionDetails(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BatchingStrategy.class)
    BatchingStrategy milvusBatchingStrategy() {
        return new EconageTokenCountBatchingStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public MilvusVectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel,
                                         MilvusVectorStoreProperties properties, BatchingStrategy batchingStrategy,
                                         ObjectProvider<ObservationRegistry> observationRegistry,
                                         ObjectProvider<VectorStoreObservationConvention> customObservationConvention) {

        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName(properties.getCollectionName())
                .databaseName(properties.getDatabaseName())
                .indexType(IndexType.valueOf(properties.getIndexType().name()))
                .metricType(MetricType.valueOf(properties.getMetricType().name()))
                .indexParameters(properties.getIndexParameters())
                .embeddingDimension(properties.getEmbeddingDimension())
                .initializeSchema(properties.isInitializeSchema())
                .batchingStrategy(batchingStrategy)
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MilvusServiceClient milvusClient(MilvusVectorStoreProperties serverProperties,
                                            MilvusServiceClientProperties clientProperties, MilvusServiceClientConnectionDetails connectionDetails) {

        var builder = ConnectParam.newBuilder()
                .withHost(connectionDetails.getHost())
                .withPort(connectionDetails.getPort())
                .withDatabaseName(serverProperties.getDatabaseName())
                .withConnectTimeout(clientProperties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .withKeepAliveTime(clientProperties.getKeepAliveTimeMs(), TimeUnit.MILLISECONDS)
                .withKeepAliveTimeout(clientProperties.getKeepAliveTimeoutMs(), TimeUnit.MILLISECONDS)
                .withRpcDeadline(clientProperties.getRpcDeadlineMs(), TimeUnit.MILLISECONDS)
                .withSecure(clientProperties.isSecure())
                .withIdleTimeout(clientProperties.getIdleTimeoutMs(), TimeUnit.MILLISECONDS)
                .withAuthorization(clientProperties.getUsername(), clientProperties.getPassword());

        if (clientProperties.isSecure() && StringUtils.hasText(clientProperties.getUri())) {
            builder.withUri(clientProperties.getUri());
        }

        if (clientProperties.isSecure() && StringUtils.hasText(clientProperties.getToken())) {
            builder.withToken(clientProperties.getToken());
        }

        if (clientProperties.isSecure() && StringUtils.hasText(clientProperties.getClientKeyPath())) {
            builder.withClientKeyPath(clientProperties.getClientKeyPath());
        }

        if (clientProperties.isSecure() && StringUtils.hasText(clientProperties.getClientPemPath())) {
            builder.withClientPemPath(clientProperties.getClientPemPath());
        }

        if (clientProperties.isSecure() && StringUtils.hasText(clientProperties.getCaPemPath())) {
            builder.withCaPemPath(clientProperties.getCaPemPath());
        }

        if (clientProperties.isSecure() && StringUtils.hasText(clientProperties.getServerPemPath())) {
            builder.withServerPemPath(clientProperties.getServerPemPath());
        }

        if (clientProperties.isSecure() && StringUtils.hasText(clientProperties.getServerName())) {
            builder.withServerName(clientProperties.getServerName());
        }

        return new MilvusServiceClient(builder.build());
    }

    static class PropertiesMilvusServiceClientConnectionDetails implements MilvusServiceClientConnectionDetails {

        private final MilvusServiceClientProperties properties;

        PropertiesMilvusServiceClientConnectionDetails(MilvusServiceClientProperties properties) {
            this.properties = properties;
        }

        @Override
        public String getHost() {
            return this.properties.getHost();
        }

        @Override
        public int getPort() {
            return this.properties.getPort();
        }

    }
}
