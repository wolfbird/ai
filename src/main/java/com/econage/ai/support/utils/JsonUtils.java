package com.econage.ai.support.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author hanpeng
 * @date 2025/3/8 16:10
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectMapper ROOT_MAPPER = new ObjectMapper();
    
    static {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone(System.getProperty("user.timezone"));
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        MAPPER.disable(SerializationFeature.WRAP_ROOT_VALUE);

        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        MAPPER.setDateFormat(df);
        MAPPER.setTimeZone(tz);
        MAPPER.registerModule(new JavaTimeModule());

        ROOT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ROOT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ROOT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        ROOT_MAPPER.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        ROOT_MAPPER.setDateFormat(df);
        ROOT_MAPPER.setTimeZone(tz);
    }

    public static String objectToJsonStr(Object data) throws JsonProcessingException {
        if (objIsEmpty(data)) {
            return null;
        }
        return MAPPER.writeValueAsString(data);
    }

    public static String objectToRootJsonStr(Object data) throws JsonProcessingException {
        if (objIsEmpty(data)) {
            return null;
        }
        return ROOT_MAPPER.writeValueAsString(data);
    }

    public static String objectToRootJson(Object data) {
        try {
            if (objIsEmpty(data)) {
                return null;
            }
            return ROOT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("json operation error {}", data, e);
        }
        return null;
    }

    /**
     * 将对象转换成json字符串。
     * <p>Title: pojoToJson</p>
     * <p>Description: </p>
     */
    public static String objectToJson(Object data) {
        try {
            if (objIsEmpty(data)) {
                return null;
            }
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("json operation error {}", data, e);
        }
        return null;
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            return MAPPER.readValue(jsonData, beanType);
        } catch (Exception e) {
            log.error("json operation error {}", jsonData, e);
        }
        return null;
    }

    public static <T> T objectToPojo(Object obj, Class<T> beanType) {
        try {
            return MAPPER.convertValue(obj, beanType);
        } catch (Exception e) {
            log.error("json operation error {}", obj, e);
        }
        return null;
    }

    /**
     * 将json数据转换成pojo对象list
     * <p>Title: jsonToList</p>
     * <p>Description: </p>
     *
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            return MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            log.error("json operation error {}", jsonData, e);
        }

        return new ArrayList<>();
    }

    public static Map<String, String> objectToMap(String json) {
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("json operation error {}", json, e);
        }
        return new HashMap<>();
    }

    public static Map<String, Object> objectToMapObject(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("json operation error {}", json, e);
        }
        return new HashMap<>();
    }

    /**
     * 把一个 object对象的属性转换成map
     */
    public static Map<String, String> objectToMap(Object data) {
        String json = objectToJson(data);
        return objectToMap(json);
    }

    private static boolean objIsEmpty(Object object) {
        if (object == null) {
            return true;
        } else if (object instanceof CharSequence) {
            return ((CharSequence) object).isEmpty();
        } else if (object.getClass().isArray()) {
            return Array.getLength(object) == 0;
        } else if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        } else {
            return object instanceof Map && ((Map<?, ?>) object).isEmpty();
        }
    }

    public static String serialize(String s) {
        return serialize(s, false);
    }

    public static String serialize(Object object, boolean withPretty) {
        try {
            Writer write = new StringWriter();
            if (withPretty) {
                MAPPER.writerWithDefaultPrettyPrinter().writeValue(write, object);
            } else {
                MAPPER.writeValue(write, object);
            }
            return write.toString();
        } catch (IOException e) {
            log.error("json operation error {}", object, e);
        }
        return null;
    }

    /**
     * 创建json节点
     *
     * @param json json
     * @return {@link JsonNode}
     */
    public static ObjectNode createJsonNode(String json) {
        if(StringUtils.isNotEmpty(json)){
            try {
                return (ObjectNode) MAPPER.readTree(json);
            } catch (IOException e) {
                log.error("json operation error {}", json, e);
            }
        }
        return MAPPER.createObjectNode();
    }

    public static ObjectNode createJsonNode() {
        return MAPPER.createObjectNode();
    }

}
