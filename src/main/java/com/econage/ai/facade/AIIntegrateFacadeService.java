package com.econage.ai.facade;

import com.econage.ai.AIConst;
import com.econage.ai.advisor.ExtractDocumentAnswerAdvisor;
import com.econage.ai.advisor.ExtractQuestionAnswerAdvisor;
import com.econage.ai.base.dashscope.chat.DashScopeChatModel;
import com.econage.ai.base.dashscope.chat.MessageFormat;
import com.econage.ai.base.vectorstore.milvus.MilvusVectorStore;
import com.econage.ai.dto.chat.*;
import com.econage.ai.dto.vectorstore.ChatUnionContext;
import com.econage.ai.dto.vectorstore.DocSegmentSettingUnion;
import com.econage.ai.dto.vectorstore.VectorStorePushRequest;
import com.econage.ai.support.cache.CacheManager;
import com.econage.ai.support.cache.PlatMapCache;
import com.econage.ai.support.cache.PlatPlainCache;
import com.econage.ai.support.cache.config.MapCacheEntrySetting;
import com.econage.ai.support.cache.config.PlainCacheEntrySetting;
import com.econage.ai.support.exception.ChatException;
import com.econage.ai.support.factory.ChatModelFactory;
import com.econage.ai.support.utils.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import com.econage.ai.base.vectorstore.SearchRequest;
import com.econage.ai.base.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.econage.ai.base.vectorstore.filter.Filter.ExpressionType.AND;

/**
 * @author hanpeng
 * @date 2024/9/26 10:43
 */
@Slf4j
@Component
public class AIIntegrateFacadeService {


    private MilvusVectorStore vectorStore;
    private PlatMapCache<List<Document>> chatDocumentsCache;
    private PlatPlainCache<ChatModelInfo, ChatModel> chatModelCache;
    private ObjectMapperSupport objectMapperSupport;
    private ChatModelFactory chatModelFactory;


    @Autowired(required = false)
    void autoVectorStore(
            MilvusVectorStore vectorStore
    ) {
        this.vectorStore = vectorStore;
    }

    @Autowired
    void auto(
            ChatModelFactory chatModelFactory,
            CacheManager cacheManager,
            ObjectMapperSupport objectMapperSupport
    ) {
        chatDocumentsCache = cacheManager.createPlatMapCache(
                MapCacheEntrySetting.builder()
                        .name(AIConst.CHAT_DOCUMENT_CACHE_NAME)
                        .maxSize(500)
                        .ttl(Duration.ofHours(1))
                        .build());

        chatModelCache = cacheManager.createPlainCache(
                PlainCacheEntrySetting.<ChatModelInfo, ChatModel>builder()
                        .keyClazz(ChatModelInfo.class)
                        .valueClazz(ChatModel.class).
                        name(AIConst.CHAT_DOCUMENT_CACHE_NAME)
                        .maximumSize(500)
                        .expireTimeAfterWrite(Duration.ofHours(1))
                        .build(),
                chatModelInfo -> chatModelFactory.createChatModel(chatModelInfo.getProviderType(), chatModelInfo.getModel(), chatModelInfo.getBaseUrl(), chatModelInfo.getApiKey()));

        this.chatModelFactory = chatModelFactory ;
        this.objectMapperSupport = objectMapperSupport;
    }



    /**
     * 更新数据至向量数据库
     * 向量数据库不支持传统更新惭怍，故先删后新增
     *
     * @param request 同步请求
     */
    public void updateVectorStore(VectorStorePushRequest request) {

        if( CollectionUtils.isEmpty( request.getUnions())) {
            return;
        }

        List<Document> documentList = Lists.newArrayList();
        for(var union : request.getUnions()) {
            if(StringUtils.isEmpty(union.getDocText())) {
                continue;
            }
            Map<String, Object> finalMetaDataMap = new HashMap<>(
                    union.getMetaDataMap().entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().toString().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            finalMetaDataMap.put(AIConst.MODULAR_INNER_ID_FIELD_NAME, union.getModularInnerId());

            var document = new Document(union.getDocText(), finalMetaDataMap);
            var splitter = RecursiveTokenTextSplitter
                    .builder();

            Optional.ofNullable(union.getSegmentSettingUnion()).map(DocSegmentSettingUnion::getCommonSegmentSetting).ifPresent(setting -> {
                if(StringUtils.isNotEmpty(setting.getSegmentIdentifier())) {
                    splitter.withCustomSeparator(setting.getSegmentIdentifier());
                }
                if(setting.getSegmentMaxLength() != null) {
                    splitter.withChunkSize(setting.getSegmentMaxLength());
                }
            });


            documentList.addAll(splitter.build().apply(List.of(document)));
        }



        var modularInnerIdList = request.getUnions().stream().map(VectorStorePushRequest.VectorStorePushUnion::getModularInnerId).toList();
        vectorStore.delete(modularInnerIdList);


        if (CollectionUtils.isNotEmpty(documentList)) {
            for(var chunkDocuments : CollectionMore.splitCollection(documentList, 10)) {
                vectorStore.add(chunkDocuments);
            }
            log.info("updateVectorStore success , documents : {}", String.join(",", modularInnerIdList));
        }
    }

    /**
     * 更新数据至向量数据库
     * 向量数据库不支持传统更新惭怍，故先删后新增
     *
     * @param resource       文件流
     * @param modularInnerId 逻辑主键值
     * @param metaDataMap    元信息
     */
    public void updateVectorStore(Resource resource, String modularInnerId, Map<String, Object> metaDataMap) {
        Map<String, Object> finalMetaDataMap = new HashMap<>(metaDataMap);

        finalMetaDataMap.put(AIConst.MODULAR_INNER_ID_FIELD_NAME, modularInnerId);
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        var documentList = tikaDocumentReader.read();
        for (var document : documentList) {
            document.getMetadata().putAll(finalMetaDataMap);
        }
        vectorStore.delete(List.of(modularInnerId));
        var splitter = TokenTextSplitter.builder().build();
        var splitterDocumentList = splitter.apply(documentList);
        if (CollectionUtils.isNotEmpty(splitterDocumentList)) {
            vectorStore.add(splitterDocumentList);
            log.info("updateVectorStore success : metaDataMap = {}", finalMetaDataMap);
        }
    }

    /**
     * 通过逻辑主键删除向量数据
     *
     * @param modularInnerIdList 逻辑主键集合
     */
    public void deleteVectorStore(List<String> modularInnerIdList) {
        Assert.notNull(modularInnerIdList, "Document id list must not be null");
        vectorStore.delete(modularInnerIdList);
    }

    /**
     * 对话
     *
     * @param chatRequest
     * @return
     */
    public ChatCompletionResponse chat(ChatCompletionRequest chatRequest)  {
//        log.info("chat Request Info: {}", objectMapperSupport.writeValueAsString(chatRequest));

        var chatClient = parseChatClient(chatRequest);

        ChatUnionContext chatUnionContext = new ChatUnionContext();

        var response = Optional.ofNullable(chatClient
                .advisors(advisorSpec -> {
                    if (BooleanUtils.isTrue(chatRequest.isWithVectorStore()) && StringUtils.isNotEmpty(chatRequest.getQueryMessage())) {
                        var searchRequest = new SearchRequest
                                .Builder()
                                .topK(chatRequest.getTopK() == null ? 4 : chatRequest.getTopK())
                                .similarityThreshold( chatRequest.getSimilarityThreshold() == null ? 0.5d : chatRequest.getSimilarityThreshold());
                        if( CollectionUtils.isNotEmpty(chatRequest.getFilterExpression())) {
                            Filter.Expression expression = null;
                            for( var filterExpression : chatRequest.getFilterExpression()) {

                                if( expression == null) {
                                    expression = new Filter.Expression(filterExpression.getExpressionType(), new Filter.Key(filterExpression.getKey()), new Filter.Value(filterExpression.getValue()));
                                } else{
                                    expression = new Filter.Expression(
                                            AND,
                                            new Filter.Expression(filterExpression.getExpressionType(), new Filter.Key(filterExpression.getKey()), new Filter.Value(filterExpression.getValue())),
                                            expression
                                    );
                                }
                            }
                            searchRequest.filterExpression(expression);
                        }

                        advisorSpec.advisors(
                                new ExtractQuestionAnswerAdvisor(vectorStore, searchRequest.build(), DEFAULT_USER_TEXT_ADVISE),
                                new ExtractDocumentAnswerAdvisor(chatUnionContext)
                        );
                    }

                })
                .call()
                .chatResponse())
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .orElseThrow( () -> new IllegalArgumentException("chat response is null"));

        var responseId = (String) response.getMetadata().get("id");

        chatUnionContext.setContent(response.getText());
        chatDocumentsCache.put(responseId, chatUnionContext.getDocuments());
        log.info("chat Response Info: {}", objectMapperSupport.writeValueAsString(chatUnionContext));
        return parseChatCompletionResponse(response);
    }

    private static final String DEFAULT_USER_TEXT_ADVISE = """
			Use the following context as your learned knowledge, inside
			---------------------
			{question_answer_context}
			---------------------
			When answer to user:
            - If you don't know, just say that you don't know.
            - If you don't know when you are not sure, ask for clarification.
            - Avoid mentioning that you obtained the information from the context.
			""";

    private ChatClient.ChatClientRequestSpec parseChatClient(ChatCompletionRequest chatRequest) {


        var chatClient = ChatClient.create(chatModelCache.get(chatRequest.getChatModelInfo())).prompt();

        if (StringUtils.isNotEmpty(chatRequest.getSystemText())) {
            chatClient.system(StringMore.escapeBrackets(chatRequest.getSystemText()));
        }

        if (CollectionUtils.isNotEmpty(chatRequest.getMessagesMemory())) {
            List<Message> memoryList = chatRequest.getMessagesMemory()
                    .stream()
                    .filter(message -> StringUtils.isNotEmpty(message.getContent()))
                    .map(this::createMemoryMessage).toList();
            if(CollectionUtils.isNotEmpty(memoryList)){
                chatClient.messages(memoryList);
            }

        }

        if (CollectionUtils.isNotEmpty(chatRequest.getFileTextList())) {

            StringBuilder userText = new StringBuilder("以下是用户上传的文件，请根据文件内容回答用户问题：\n");
            chatRequest.getFileTextList().forEach(fileText -> userText.append(fileText.getFileTextContent()).append("\n"));

            var queryMessage = StringUtils.defaultIfEmpty(chatRequest.getQueryMessage(), "请对以上文件内容进行总结");
            chatRequest.setQueryMessage(userText.append(queryMessage).toString());
        }

        if(CollectionUtils.isNotEmpty(chatRequest.getMediaList())) {
            List<Media> mediaList = chatRequest.getMediaList().stream().map(this::createMedia).toList();
            var message = new UserMessage(chatRequest.getQueryMessage(),mediaList);
            message.getMetadata().put(DashScopeChatModel.MESSAGE_FORMAT, MessageFormat.IMAGE);
            chatClient.messages(message);


        } else if (StringUtils.isNotEmpty(chatRequest.getQueryMessage())) {
            chatClient.user(chatRequest.getQueryMessage());
        } else {
            chatClient.user("请依据已有信息回答");
        }

        var options = chatModelFactory.createChatOptions(chatRequest.getChatModelInfo().getProviderType(), chatRequest);

        if( options != null) {
            chatClient.options(options);
        }

        return chatClient;
    }


    /**
     * 流式对话
     *
     * @param chatRequest
     * @return
     */
    public Object chatStream(ChatCompletionRequest chatRequest) {
//        log.info("chat Request Info: {}", objectMapperSupport.writeValueAsString(chatRequest));

        var chatClient = parseChatClient(chatRequest);
        StringBuilder responseContent = new StringBuilder();

        ChatUnionContext chatUnionContext = new ChatUnionContext();
        return chatClient.advisors(advisorSpec -> {

            if (BooleanUtils.isTrue(chatRequest.isWithVectorStore()) && StringUtils.isNotEmpty(chatRequest.getQueryMessage())) {
                var searchRequest = new SearchRequest
                        .Builder()
                        .topK(chatRequest.getTopK() == null ? 4 : chatRequest.getTopK())
                        .similarityThreshold( chatRequest.getSimilarityThreshold() == null ? 0.5d : chatRequest.getSimilarityThreshold());
                if( CollectionUtils.isNotEmpty(chatRequest.getFilterExpression())) {
                    Filter.Expression expression = null;
                    for( var filterExpression : chatRequest.getFilterExpression()) {

                        if( expression == null) {
                            expression = new Filter.Expression(filterExpression.getExpressionType(), new Filter.Key(filterExpression.getKey()), new Filter.Value(filterExpression.getValue()));
                        } else{
                            expression = new Filter.Expression(
                                    AND,
                                    new Filter.Expression(filterExpression.getExpressionType(), new Filter.Key(filterExpression.getKey()), new Filter.Value(filterExpression.getValue())),
                                    expression
                            );
                        }
                    }
                    searchRequest.filterExpression(expression);
                }
                advisorSpec.advisors(
                        new ExtractQuestionAnswerAdvisor(vectorStore, searchRequest.build(),DEFAULT_USER_TEXT_ADVISE),
                        new ExtractDocumentAnswerAdvisor(chatUnionContext)
                );
            }

        }).stream().chatResponse().mapNotNull(response -> {
            var output = response.getResult().getOutput();

            if (StringUtils.isEmpty(chatUnionContext.getResponseMessageId())) {
                var responseId = (String) output.getMetadata().get("id");
                chatUnionContext.setResponseMessageId(responseId);
            }
            responseContent.append(StringUtils.defaultString(output.getText()));
            return parseChatCompletionResponse(output);
        }).doOnComplete(() -> {
            if (CollectionUtils.isNotEmpty(chatUnionContext.getDocuments())) {
                chatDocumentsCache.put(chatUnionContext.getResponseMessageId(), chatUnionContext.getDocuments());
            }
            chatUnionContext.setContent(responseContent.toString());
            log.info("chat Response Info: {}", objectMapperSupport.writeValueAsString(chatUnionContext));

        }).doOnError(error -> {
            log.error("Error occurred during chat response processing {}", chatRequest.getChatModelInfo(), error);
            throw new ChatException("Error processing chat request", error);
        });
    }

    private Message createMemoryMessage(ChatMemoryDTO chatMemoryDTO) {
        if (chatMemoryDTO.getMessageType() == MessageType.USER) {
            return new UserMessage(chatMemoryDTO.getContent());
        } else {
            return new AssistantMessage(chatMemoryDTO.getContent());
        }
    }

    private ChatCompletionResponse parseChatCompletionResponse(AssistantMessage assistantMessage) {
        var chatCompletionResponse = new ChatCompletionResponse();
        chatCompletionResponse.setMetadata(assistantMessage.getMetadata());
        chatCompletionResponse.setContent(assistantMessage.getText());
        return chatCompletionResponse;
    }

    private Media createMedia( ChatMediaDTO chatMediaDTO) {
        return new Media( chatMediaDTO.getMimeType() , base64ToResource(chatMediaDTO.getBase64Content()));
    }

    public static Resource base64ToResource(String base64Data) {
        // 去掉 Base64 编码中的前缀部分（例如：data:image/png;base64,）
        String base64 = base64Data.split(",")[1];

        // 解码 Base64 字符串
        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        // 使用 ByteArrayResource 将字节数组包装成 Resource
        return new ByteArrayResource(decodedBytes);
    }

    /**
     * 通过对话id获取引用文档
     *
     * @param messageId 对话id
     * @return
     */
    public List<Document> getMessageDocuments(String messageId) {
        return chatDocumentsCache.get(messageId);
    }




}
