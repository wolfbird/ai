package com.econage.ai.support.utils;

import java.util.ArrayList;
import java.util.List;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.Assert;

public class TokenTextSplitter extends TextSplitter {

    private static final int DEFAULT_CHUNK_SIZE = 800;
    private static final int MIN_CHUNK_SIZE_CHARS = 350;
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 5;
    private static final int MAX_NUM_CHUNKS = 10000;
    private static final boolean KEEP_SEPARATOR = true;
    private static final int DEFAULT_OVERLAP_SIZE = 50;

    private final EncodingRegistry registry = Encodings.newLazyEncodingRegistry();
    private final Encoding encoding = this.registry.getEncoding(EncodingType.CL100K_BASE);

    private final int chunkSize;
    private final int minChunkSizeChars;
    private final int minChunkLengthToEmbed;
    private final int maxNumChunks;
    private final boolean keepSeparator;
    private final String customSeparator;
    private final int overlapSize;

    public TokenTextSplitter() {
        this(DEFAULT_CHUNK_SIZE, MIN_CHUNK_SIZE_CHARS, MIN_CHUNK_LENGTH_TO_EMBED, MAX_NUM_CHUNKS, KEEP_SEPARATOR, "\n", DEFAULT_OVERLAP_SIZE);
    }

    public TokenTextSplitter(int chunkSize, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks,
                             boolean keepSeparator, String customSeparator, int overlapSize) {
        this.chunkSize = chunkSize;
        this.minChunkSizeChars = minChunkSizeChars;
        this.minChunkLengthToEmbed = minChunkLengthToEmbed;
        this.maxNumChunks = maxNumChunks;
        this.keepSeparator = keepSeparator;
        this.customSeparator = customSeparator;
        this.overlapSize = overlapSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected List<String> splitText(String text) {
        return doSplit(text, this.chunkSize);
    }

    protected List<String> doSplit(String text, int chunkSize) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> tokens = getEncodedTokens(text);
        List<String> chunks = new ArrayList<>();
        int num_chunks = 0;

        while (!tokens.isEmpty() && num_chunks < this.maxNumChunks) {
            int endIndex = Math.min(chunkSize, tokens.size());
            List<Integer> chunk = tokens.subList(0, endIndex);
            String chunkText = decodeTokens(chunk);

            if (chunkText.trim().isEmpty()) {
                tokens = tokens.subList(chunk.size(), tokens.size());
                continue;
            }

            int lastSeparator = chunkText.lastIndexOf(this.customSeparator);
            if (lastSeparator != -1 && lastSeparator > this.minChunkSizeChars) {
                chunkText = chunkText.substring(0, lastSeparator + this.customSeparator.length());
            }

            String chunkTextToAppend = this.keepSeparator ? chunkText.trim() : chunkText.replace(System.lineSeparator(), " ").trim();
            if (chunkTextToAppend.length() > this.minChunkLengthToEmbed) {
                chunks.add(chunkTextToAppend);
            }

            int nextStart = Math.max(0, endIndex - this.overlapSize);
            tokens = tokens.subList(nextStart, tokens.size());

            num_chunks++;
        }

        if (!tokens.isEmpty()) {
            String remaining_text = decodeTokens(tokens).replace(System.lineSeparator(), " ").trim();
            if (remaining_text.length() > this.minChunkLengthToEmbed) {
                chunks.add(remaining_text);
            }
        }

        return chunks;
    }

    private List<Integer> getEncodedTokens(String text) {
        Assert.notNull(text, "Text must not be null");
        return this.encoding.encode(text).boxed();
    }

    private String decodeTokens(List<Integer> tokens) {
        Assert.notNull(tokens, "Tokens must not be null");
        var tokensIntArray = new IntArrayList(tokens.size());
        tokens.forEach(tokensIntArray::add);
        return this.encoding.decode(tokensIntArray);
    }

    public static final class Builder {
        private int chunkSize = DEFAULT_CHUNK_SIZE;
        private int minChunkSizeChars = MIN_CHUNK_SIZE_CHARS;
        private int minChunkLengthToEmbed = MIN_CHUNK_LENGTH_TO_EMBED;
        private int maxNumChunks = MAX_NUM_CHUNKS;
        private boolean keepSeparator = KEEP_SEPARATOR;
        private String customSeparator = "\n\n";
        private int overlapSize = DEFAULT_OVERLAP_SIZE;

        public Builder withChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public Builder withMinChunkSizeChars(int minChunkSizeChars) {
            this.minChunkSizeChars = minChunkSizeChars;
            return this;
        }

        public Builder withMinChunkLengthToEmbed(int minChunkLengthToEmbed) {
            this.minChunkLengthToEmbed = minChunkLengthToEmbed;
            return this;
        }

        public Builder withMaxNumChunks(int maxNumChunks) {
            this.maxNumChunks = maxNumChunks;
            return this;
        }

        public Builder withKeepSeparator(boolean keepSeparator) {
            this.keepSeparator = keepSeparator;
            return this;
        }

        public Builder withCustomSeparator(String customSeparator) {
            this.customSeparator = customSeparator;
            return this;
        }

        public Builder withOverlapSize(int overlapSize) {
            this.overlapSize = overlapSize;
            return this;
        }

        public TokenTextSplitter build() {
            return new TokenTextSplitter(this.chunkSize, this.minChunkSizeChars, this.minChunkLengthToEmbed,
                    this.maxNumChunks, this.keepSeparator, this.customSeparator, this.overlapSize);
        }
    }
}
