package com.econage.ai.support.utils;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class RecursiveTokenTextSplitter extends TextSplitter {

    private static final int DEFAULT_CHUNK_SIZE = 800;

    private static final int MIN_CHUNK_SIZE_CHARS = 350;

    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 5;

    private static final int MAX_NUM_CHUNKS = 10000;

    private static final boolean KEEP_SEPARATOR = true;

    private static final String CUSTOM_SEPARATOR = "\n\n";

    private static final String NO_SEPARATOR_FLAG = "NO_NEEDED_SEPARATOR";

    private final EncodingRegistry registry = Encodings.newLazyEncodingRegistry();

    private final Encoding encoding = this.registry.getEncoding(EncodingType.CL100K_BASE);

    // The target size of each text chunk in tokens
    private final int chunkSize;

    // The minimum size of each text chunk in characters
    private final int minChunkSizeChars;

    // Discard chunks shorter than this
    private final int minChunkLengthToEmbed;

    // The maximum number of chunks to generate from a text
    private final int maxNumChunks;

    private final boolean keepSeparator;

    private final String customSeparator;

    public RecursiveTokenTextSplitter() {
        this(DEFAULT_CHUNK_SIZE, MIN_CHUNK_SIZE_CHARS, MIN_CHUNK_LENGTH_TO_EMBED, MAX_NUM_CHUNKS, KEEP_SEPARATOR,CUSTOM_SEPARATOR);
    }


    public RecursiveTokenTextSplitter(int chunkSize, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks,
                             boolean keepSeparator, String customSeparator) {
        this.chunkSize = chunkSize;
        this.minChunkSizeChars = minChunkSizeChars;
        this.minChunkLengthToEmbed = minChunkLengthToEmbed;
        this.maxNumChunks = maxNumChunks;
        this.keepSeparator = keepSeparator;
        this.customSeparator = customSeparator;
    }

    public static RecursiveTokenTextSplitter.Builder builder() {
        return new RecursiveTokenTextSplitter.Builder();
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
            List<Integer> chunk = tokens.subList(0, Math.min(chunkSize, tokens.size()));
            String chunkText = decodeTokens(chunk);

            // Skip the chunk if it is empty or whitespace
            if (chunkText.trim().isEmpty()) {
                tokens = tokens.subList(chunk.size(), tokens.size());
                continue;
            }

            // Find the last period or punctuation mark in the chunk
            int lastPunctuation = Math.max(chunkText.lastIndexOf('.'), Math.max(chunkText.lastIndexOf('?'),
                    Math.max(chunkText.lastIndexOf('!'), chunkText.lastIndexOf('\n'))));

            if (lastPunctuation != -1 && lastPunctuation > this.minChunkSizeChars) {
                // Truncate the chunk text at the punctuation mark
                chunkText = chunkText.substring(0, lastPunctuation + 1);
            }

            String chunkTextToAppend = (this.keepSeparator) ? chunkText.trim()
                    : chunkText.replace(System.lineSeparator(), " ").trim();
            if (chunkTextToAppend.length() > this.minChunkLengthToEmbed) {
                chunks.add(chunkTextToAppend);
            }

            // Remove the tokens corresponding to the chunk text from the remaining tokens
            tokens = tokens.subList(getEncodedTokens(chunkText).size(), tokens.size());

            num_chunks++;
        }

        // Handle the remaining tokens
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

        private String customSeparator = CUSTOM_SEPARATOR ;

        private Builder() {
        }

        public RecursiveTokenTextSplitter.Builder withChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public RecursiveTokenTextSplitter.Builder withMinChunkSizeChars(int minChunkSizeChars) {
            this.minChunkSizeChars = minChunkSizeChars;
            return this;
        }

        public RecursiveTokenTextSplitter.Builder withMinChunkLengthToEmbed(int minChunkLengthToEmbed) {
            this.minChunkLengthToEmbed = minChunkLengthToEmbed;
            return this;
        }

        public RecursiveTokenTextSplitter.Builder withMaxNumChunks(int maxNumChunks) {
            this.maxNumChunks = maxNumChunks;
            return this;
        }

        public RecursiveTokenTextSplitter.Builder withKeepSeparator(boolean keepSeparator) {
            this.keepSeparator = keepSeparator;
            return this;
        }
        public RecursiveTokenTextSplitter.Builder withCustomSeparator(String customSeparator) {
            this.customSeparator = customSeparator;
            return this;
        }

        public RecursiveTokenTextSplitter build() {
            return new RecursiveTokenTextSplitter(this.chunkSize, this.minChunkSizeChars, this.minChunkLengthToEmbed,
                    this.maxNumChunks, this.keepSeparator,this.customSeparator);
        }

    }

}