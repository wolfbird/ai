package com.econage.ai.base.vectorstore.milvus;

import org.springframework.ai.autoconfigure.vectorstore.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author hanpeng
 * @date 2025/2/18 11:22
 */
@Component
@ConfigurationProperties(MilvusVectorStoreProperties.CONFIG_PREFIX)
public class MilvusVectorStoreProperties extends CommonVectorStoreProperties {

    public static final String CONFIG_PREFIX = "spring.ai.vectorstore.milvus";

    /**
     * The name of the Milvus database to connect to.
     */
    private String databaseName = MilvusVectorStore.DEFAULT_DATABASE_NAME;

    /**
     * Milvus collection name to store the vectors.
     */
    private String collectionName = MilvusVectorStore.DEFAULT_COLLECTION_NAME;

    /**
     * The dimension of the vectors to be stored in the Milvus collection.
     */
    private int embeddingDimension = MilvusVectorStore.OPENAI_EMBEDDING_DIMENSION_SIZE;

    /**
     * The type of the index to be created for the Milvus collection.
     */
    private MilvusVectorStoreProperties.MilvusIndexType indexType = MilvusVectorStoreProperties.MilvusIndexType.IVF_FLAT;

    /**
     * The metric type to be used for the Milvus collection.
     */
    private MilvusVectorStoreProperties.MilvusMetricType metricType = MilvusVectorStoreProperties.MilvusMetricType.COSINE;

    /**
     * The index parameters to be used for the Milvus collection.
     */
    private String indexParameters = "{\"nlist\":1024}";

    public enum MilvusMetricType {

        /**
         * Invalid metric type
         */
        INVALID,
        /**
         * Euclidean distance
         */
        L2,
        /**
         * Inner product
         */
        IP,
        /**
         * Cosine distance
         */
        COSINE,
        /**
         * Hamming distance
         */
        HAMMING,
        /**
         * Jaccard distance
         */
        JACCARD;

    }

    public enum MilvusIndexType {

        INVALID, FLAT, IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, DISKANN, AUTOINDEX, SCANN, GPU_IVF_FLAT, GPU_IVF_PQ, BIN_FLAT,
        BIN_IVF_FLAT, TRIE, STL_SORT;

    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        Assert.hasText(databaseName, "Database name should not be empty.");
        this.databaseName = databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        Assert.hasText(collectionName, "Collection name should not be empty.");
        this.collectionName = collectionName;
    }

    public int getEmbeddingDimension() {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(int embeddingDimension) {
        Assert.isTrue(embeddingDimension > 0, "Embedding dimension should be a positive value.");
        this.embeddingDimension = embeddingDimension;
    }

    public MilvusVectorStoreProperties.MilvusIndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(MilvusVectorStoreProperties.MilvusIndexType indexType) {
        Assert.notNull(indexType, "Index type can not be null");
        this.indexType = indexType;
    }

    public MilvusVectorStoreProperties.MilvusMetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MilvusVectorStoreProperties.MilvusMetricType metricType) {
        Assert.notNull(metricType, "MetricType can not be null");
        this.metricType = metricType;
    }

    public String getIndexParameters() {
        return indexParameters;
    }

    public void setIndexParameters(String indexParameters) {
        Assert.notNull(indexParameters, "indexParameters can not be null");
        this.indexParameters = indexParameters;
    }
}
