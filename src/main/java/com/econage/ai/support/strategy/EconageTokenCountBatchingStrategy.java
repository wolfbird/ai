package com.econage.ai.support.strategy;

import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.ai.document.ContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hanpeng
 * @date 2025/2/21 10:50
 */
public class EconageTokenCountBatchingStrategy implements BatchingStrategy {

    /**
     * Using openai upper limit of input token count as the default.
     */
    private static final int MAX_INPUT_TOKEN_COUNT = 8191;

    private final TokenCountEstimator tokenCountEstimator;

    private final int maxInputTokenCount;

    private final ContentFormatter contentFormater;

    private final MetadataMode metadataMode;

    public EconageTokenCountBatchingStrategy() {
        this(EncodingType.CL100K_BASE, MAX_INPUT_TOKEN_COUNT);
    }

    /**
     * @param encodingType {@link EncodingType}
     * @param maxInputTokenCount upper limit for input tokens
     */
    public EconageTokenCountBatchingStrategy(EncodingType encodingType, int maxInputTokenCount) {
        this(encodingType, maxInputTokenCount, Document.DEFAULT_CONTENT_FORMATTER, MetadataMode.NONE);
    }

    /**
     * @param encodingType {@link EncodingType}
     * @param maxInputTokenCount upper limit for input tokens
     * @param contentFormatter {@link ContentFormatter}
     * @param metadataMode {@link MetadataMode}
     */
    public EconageTokenCountBatchingStrategy(EncodingType encodingType, int maxInputTokenCount,
                                             ContentFormatter contentFormatter, MetadataMode metadataMode) {
        this.tokenCountEstimator = new JTokkitTokenCountEstimator(encodingType);
        this.maxInputTokenCount =(int) Math.round(maxInputTokenCount - (maxInputTokenCount * .1)) ;
        this.contentFormater = contentFormatter;
        this.metadataMode = metadataMode;
    }

    @Override
    public List<List<Document>> batch(List<Document> documents) {
        List<List<Document>> batches = new ArrayList<>();
        int currentSize = 0;
        List<Document> currentBatch = new ArrayList<>();

        for (Document document : documents) {
            int tokenCount = this.tokenCountEstimator
                    .estimate(document.getFormattedContent(this.contentFormater, this.metadataMode));
            if (tokenCount > this.maxInputTokenCount) {
                throw new IllegalArgumentException(
                        "Tokens in a single document exceeds the maximum number of allowed input tokens");
            }
            if (currentSize + tokenCount > maxInputTokenCount || currentBatch.size() > 20) {
                batches.add(currentBatch);
                currentBatch = new ArrayList<>();
                currentSize = 0;
            }
            currentBatch.add(document);
            currentSize += tokenCount;
        }
        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }
        return batches;
    }
}
