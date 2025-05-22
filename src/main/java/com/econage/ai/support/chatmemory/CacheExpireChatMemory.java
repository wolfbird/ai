package com.econage.ai.support.chatmemory;

import com.econage.ai.support.cache.PlatMapCache;
import com.google.common.collect.Lists;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @author hanpeng
 * @date 2025/1/9 16:04
 */
public class CacheExpireChatMemory implements ChatMemory {

    private final PlatMapCache<List<Message>> chatMemoryCache;

    public CacheExpireChatMemory(PlatMapCache<List<Message>> chatMemoryCache) {
        this.chatMemoryCache = chatMemoryCache;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        var messagesCache = chatMemoryCache.get(conversationId);
        if (messagesCache == null) {
            messagesCache = Lists.newArrayList();
            chatMemoryCache.put(conversationId, messagesCache);
        }
        messagesCache.addAll(messages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> all = chatMemoryCache.get(conversationId);
        return all != null ? all.stream().skip(Math.max(0, all.size() - lastN)).toList() : List.of();
    }

    @Override
    public void clear(String conversationId) {
        this.chatMemoryCache.invalidData(conversationId);
    }
}
