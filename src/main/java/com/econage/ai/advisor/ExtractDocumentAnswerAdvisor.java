package com.econage.ai.advisor;

import com.econage.ai.dto.vectorstore.ChatUnionContext;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hanpeng
 * @date 2025/1/3 22:20
 */
public class ExtractDocumentAnswerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private final ChatUnionContext chatUnionContext;

    public ExtractDocumentAnswerAdvisor(
            ChatUnionContext chatUnionContext
    ) {
        this.chatUnionContext = chatUnionContext;
    }

    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        var context = new HashMap<>(advisedRequest.adviseContext());
        var requestPrim = adviseRequest(advisedRequest, context);
        advisedRequest = AdvisedRequest.from(requestPrim).adviseContext(Collections.unmodifiableMap(context)).build();
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        context = new HashMap<>(advisedResponse.adviseContext());
        return new AdvisedResponse(advisedResponse.response(), Collections.unmodifiableMap(context));
    }

    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<>(advisedRequest.adviseContext());
        advisedRequest = adviseRequest(advisedRequest, context);
        Flux<AdvisedResponse> advisedResponseStream = chain.nextAroundStream(advisedRequest);
        return advisedResponseStream.map(AdvisedResponse::response).map((chatResponse) -> new AdvisedResponse(chatResponse, context));
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    private AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        chatUnionContext.setDocuments((List<Document>) context.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS));
        return request;
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
