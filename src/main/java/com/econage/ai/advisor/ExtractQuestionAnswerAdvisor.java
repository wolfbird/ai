package com.econage.ai.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import com.econage.ai.base.vectorstore.SearchRequest;
import com.econage.ai.base.vectorstore.VectorStore;
import com.econage.ai.base.vectorstore.filter.Filter;
import com.econage.ai.base.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author hanpeng
 * @date 2025/3/4 11:08
 */
public class ExtractQuestionAnswerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    public static final String RETRIEVED_DOCUMENTS = "qa_retrieved_documents";

    public static final String FILTER_EXPRESSION = "qa_filter_expression";

    private static final String DEFAULT_USER_TEXT_ADVISE = """

			Context information is below, surrounded by ---------------------

			---------------------
			{question_answer_context}
			---------------------

			Given the context and provided history information and not prior knowledge,
			reply to the user comment. If the answer is not in the context, inform
			the user that you can't answer the question.
			""";

    private static final int DEFAULT_ORDER = 0;

    private final VectorStore vectorStore;

    private final String userTextAdvise;

    private final SearchRequest searchRequest;

    private final boolean protectFromBlocking;

    private final int order;

    /**
     * The QuestionAnswerAdvisor retrieves context information from a Vector Store and
     * combines it with the user's text.
     * @param vectorStore The vector store to use
     */
    public ExtractQuestionAnswerAdvisor(VectorStore vectorStore) {
        this(vectorStore, SearchRequest.builder().build(), DEFAULT_USER_TEXT_ADVISE);
    }

    /**
     * The QuestionAnswerAdvisor retrieves context information from a Vector Store and
     * combines it with the user's text.
     * @param vectorStore The vector store to use
     * @param searchRequest The search request defined using the portable filter
     * expression syntax
     */
    public ExtractQuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this(vectorStore, searchRequest, DEFAULT_USER_TEXT_ADVISE);
    }

    /**
     * The QuestionAnswerAdvisor retrieves context information from a Vector Store and
     * combines it with the user's text.
     * @param vectorStore The vector store to use
     * @param searchRequest The search request defined using the portable filter
     * expression syntax
     * @param userTextAdvise The user text to append to the existing user prompt. The text
     * should contain a placeholder named "question_answer_context".
     */
    public ExtractQuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, String userTextAdvise) {
        this(vectorStore, searchRequest, userTextAdvise, true);
    }

    /**
     * The QuestionAnswerAdvisor retrieves context information from a Vector Store and
     * combines it with the user's text.
     * @param vectorStore The vector store to use
     * @param searchRequest The search request defined using the portable filter
     * expression syntax
     * @param userTextAdvise The user text to append to the existing user prompt. The text
     * should contain a placeholder named "question_answer_context".
     * @param protectFromBlocking If true the advisor will protect the execution from
     * blocking threads. If false the advisor will not protect the execution from blocking
     * threads. This is useful when the advisor is used in a non-blocking environment. It
     * is true by default.
     */
    public ExtractQuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, String userTextAdvise,
                                        boolean protectFromBlocking) {
        this(vectorStore, searchRequest, userTextAdvise, protectFromBlocking, DEFAULT_ORDER);
    }

    /**
     * The QuestionAnswerAdvisor retrieves context information from a Vector Store and
     * combines it with the user's text.
     * @param vectorStore The vector store to use
     * @param searchRequest The search request defined using the portable filter
     * expression syntax
     * @param userTextAdvise The user text to append to the existing user prompt. The text
     * should contain a placeholder named "question_answer_context".
     * @param protectFromBlocking If true the advisor will protect the execution from
     * blocking threads. If false the advisor will not protect the execution from blocking
     * threads. This is useful when the advisor is used in a non-blocking environment. It
     * is true by default.
     * @param order The order of the advisor.
     */
    public ExtractQuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, String userTextAdvise,
                                        boolean protectFromBlocking, int order) {

        Assert.notNull(vectorStore, "The vectorStore must not be null!");
        Assert.notNull(searchRequest, "The searchRequest must not be null!");
        Assert.hasText(userTextAdvise, "The userTextAdvise must not be empty!");

        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.userTextAdvise = userTextAdvise;
        this.protectFromBlocking = protectFromBlocking;
        this.order = order;
    }

    public static ExtractQuestionAnswerAdvisor.Builder builder(VectorStore vectorStore) {
        return new ExtractQuestionAnswerAdvisor.Builder(vectorStore);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

        AdvisedRequest advisedRequest2 = before(advisedRequest);

        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest2);

        return after(advisedResponse);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

        // This can be executed by both blocking and non-blocking Threads
        // E.g. a command line or Tomcat blocking Thread implementation
        // or by a WebFlux dispatch in a non-blocking manner.
        Flux<AdvisedResponse> advisedResponses = (this.protectFromBlocking) ?
                // @formatter:off
                Mono.just(advisedRequest)
                        .publishOn(Schedulers.boundedElastic())
                        .map(this::before)
                        .flatMapMany(request -> chain.nextAroundStream(request))
                : chain.nextAroundStream(before(advisedRequest));
        // @formatter:on

        return advisedResponses.map(ar -> {
            if (onFinishReason().test(ar)) {
                ar = after(ar);
            }
            return ar;
        });
    }

    private AdvisedRequest before(AdvisedRequest request) {

        var context = new HashMap<>(request.adviseContext());

        // 1. Advise the system text.
        String advisedUserText = this.userTextAdvise + System.lineSeparator() + request.systemText();

        // 2. Search for similar documents in the vector store.
//        String query = new PromptTemplate(request.userText(), request.userParams()).render();

        //todo 需控制单次userText长度
        var searchRequestToUse = SearchRequest.from(this.searchRequest)
                .query(request.userText().length() > 8192 ? request.userText().substring(0, 8192) : request.userText())
                .filterExpression(doGetFilterExpression(context))
                .build();

        List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);

        // 3. Create the context from the documents.
        context.put(RETRIEVED_DOCUMENTS, documents);

        String documentContext = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        // 4. Advise the user parameters.
        Map<String, Object> advisedUserParams = new HashMap<>(request.userParams());
        advisedUserParams.put("question_answer_context", documentContext);

        AdvisedRequest advisedRequest = AdvisedRequest.from(request)
                .systemText(advisedUserText)
                .systemParams(advisedUserParams)
                .adviseContext(context)
                .build();

        return advisedRequest;
    }

    private AdvisedResponse after(AdvisedResponse advisedResponse) {
        ChatResponse.Builder chatResponseBuilder = ChatResponse.builder().from(advisedResponse.response());
        chatResponseBuilder.metadata(RETRIEVED_DOCUMENTS, advisedResponse.adviseContext().get(RETRIEVED_DOCUMENTS));
        return new AdvisedResponse(chatResponseBuilder.build(), advisedResponse.adviseContext());
    }

    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {

        if (!context.containsKey(FILTER_EXPRESSION)
                || !StringUtils.hasText(context.get(FILTER_EXPRESSION).toString())) {
            return this.searchRequest.getFilterExpression();
        }
        return new FilterExpressionTextParser().parse(context.get(FILTER_EXPRESSION).toString());

    }

    private Predicate<AdvisedResponse> onFinishReason() {
        return advisedResponse -> advisedResponse.response()
                .getResults()
                .stream()
                .filter(result -> result != null && result.getMetadata() != null
                        && StringUtils.hasText(result.getMetadata().getFinishReason()))
                .findFirst()
                .isPresent();
    }

    public static final class Builder {

        private final VectorStore vectorStore;

        private SearchRequest searchRequest = SearchRequest.builder().build();

        private String userTextAdvise = DEFAULT_USER_TEXT_ADVISE;

        private boolean protectFromBlocking = true;

        private int order = DEFAULT_ORDER;

        private Builder(VectorStore vectorStore) {
            Assert.notNull(vectorStore, "The vectorStore must not be null!");
            this.vectorStore = vectorStore;
        }

        public ExtractQuestionAnswerAdvisor.Builder searchRequest(SearchRequest searchRequest) {
            Assert.notNull(searchRequest, "The searchRequest must not be null!");
            this.searchRequest = searchRequest;
            return this;
        }

        public ExtractQuestionAnswerAdvisor.Builder userTextAdvise(String userTextAdvise) {
            Assert.hasText(userTextAdvise, "The userTextAdvise must not be empty!");
            this.userTextAdvise = userTextAdvise;
            return this;
        }

        public ExtractQuestionAnswerAdvisor.Builder protectFromBlocking(boolean protectFromBlocking) {
            this.protectFromBlocking = protectFromBlocking;
            return this;
        }

        public ExtractQuestionAnswerAdvisor.Builder order(int order) {
            this.order = order;
            return this;
        }

        public ExtractQuestionAnswerAdvisor build() {
            return new ExtractQuestionAnswerAdvisor(this.vectorStore, this.searchRequest, this.userTextAdvise,
                    this.protectFromBlocking, this.order);
        }

    }

}