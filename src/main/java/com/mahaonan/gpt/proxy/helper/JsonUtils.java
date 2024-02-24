package com.mahaonan.gpt.proxy.helper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class JsonUtils {

    private static ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper notNullMapper =  new ObjectMapper();


    static {
        init(mapper, false);
        init(notNullMapper, true);
    }

    private static void init(ObjectMapper mapper, boolean includeNotNull) {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        if (includeNotNull) {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
    }

    public static String simpleJson(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return objectToJson(map);
    }

    public static <T> T parse(String str, Class<T> cls) {
        return parse(str, constructJavaType(cls));
    }


    public static <T> T parse(String jsonStr, JavaType valueType) {
        try {
            if(!StrUtil.isBlank(jsonStr)) {
                return mapper.readValue(jsonStr, valueType);
            }
        } catch (Exception e) {
            log.error("parse failed, jsonStr=" + jsonStr, e);
        }
        return null;
    }

    /**
     * 构建java简单类型
     * @param cls
     * @return
     */
    public static JavaType constructJavaType(Class<?> cls) {
       if (cls == null) {
           return null;
       }
       return mapper.getTypeFactory().constructType(cls);
    }

    /**
     * 构造泛型java类型
     * 使用parse解析的时候，会自动对应泛型
     *
     * @param genericClass 泛型对象Class
     * @param parameterClasses 泛型对象的泛型，例如List<String>, 第一个参数是List.class, 第二个参数为String.class
     * @return
     */
    public static JavaType constructGenericType(Class<?> genericClass, Class<?>... parameterClasses) {
        return mapper.getTypeFactory().constructParametricType(genericClass, parameterClasses);
    }

    /**
     * json字符串转list
     * @param jsonStr
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> parseToList(String jsonStr, Class<T> cls) {
        return parse(jsonStr, constructGenericType(List.class, cls));
    }

    public static <T> Set<T> parseToSet(String jsonStr, Class<T> cls) {
        return parse(jsonStr, constructGenericType(Set.class, cls));
    }

    public static <T> Set<T> parseToCollection(String jsonStr, Class<T> cls) {
        return parse(jsonStr, constructGenericType(Collection.class, cls));
    }

    public static Map<String, String> parseToMap(String str) {
        return parseToMap(str, String.class, String.class);
    }

    /**
     * json字符串转map
     * @param jsonStr
     * @param kCls
     * @param vCls
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> parseToMap(String jsonStr, Class<K> kCls, Class<V> vCls) {
        return parse(jsonStr, constructGenericType(Map.class, kCls, vCls));
    }


    /**
     * object对象转换给json string
     *
     * @param obj
     * @return
     */
    public static String objectToJson(Object obj) {
        return objectToJson(obj, false);
    }

    /**
     * object对象转换给json string
     *
     * @param obj
     * @param prettyPrinter
     * @return
     */
    public static String objectToJson(Object obj, boolean prettyPrinter) {
        return objectToJson(mapper, obj, prettyPrinter);
    }

    public static String toJsonNotNull(Object obj) {
        return objectToJson(notNullMapper, obj, false);
    }


    public static String toJsonNotNull(Object obj, boolean prettyPrinter) {
        return objectToJson(notNullMapper, obj, prettyPrinter);
    }

    /**
     * object对象转换给json string
     *
     * @param obj
     * @param prettyPrinter
     * @return
     */
    public static String objectToJson(ObjectMapper mapper, Object obj, boolean prettyPrinter) {
        String retStr = "";
        if (obj == null) {
            return retStr;
        }
        try {
            if(prettyPrinter) {
                retStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } else {
                retStr = mapper.writeValueAsString(obj);
            }
        } catch (Exception e) {
            log.error("Object to json string failed!", e);
        }
        return retStr;
    }

    public static <K, V> Map<K, List<V>> parseToMapList(String str, Class<K> keyClass, Class<V> valueClass) {
        return parse(str, mapper.getTypeFactory().constructMapType(Map.class, constructJavaType(keyClass), constructCollectionType(ArrayList.class, valueClass)));
    }

    public static JavaType constructCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return mapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    /**
     * 根据表达式获取指定值
     * persion
     * persion.name
     * persons[3]
     * person.friends[5].name
     * @param jsonStr
     * @param expression
     * @return
     */
    public static String strExpression(String jsonStr, String expression) {
        JSON json = JSONUtil.parse(jsonStr);
        Object path = json.getByPath(expression);
        if (path == null) {
            return "";
        }
        return path.toString();
    }


    public static <T> T strExpression(String jsonStr, String expression, Class<T> cls) {
        JSON json = JSONUtil.parse(jsonStr);
        return json.getByPath(expression, cls);
    }

    /**
     * 修改json字符串中指定表达式的值
     * @param expression 表达式 beanPath
     */
    public static String setValue(String jsonStr, String expression, Object value) {
        Map<String, Object> paramMap = JsonUtils.parseToMap(jsonStr, String.class, Object.class);
        BeanUtil.setProperty(paramMap, expression, value);
        return objectToJson(paramMap);
    }

    public static String setValue(String jsonStr, Map<String, Object> expressionValueMap) {
        Map<String, Object> paramMap = JsonUtils.parseToMap(jsonStr, String.class, Object.class);
        expressionValueMap.forEach((k, v) -> BeanUtil.setProperty(paramMap, k, v));
        return objectToJson(paramMap);
    }

    public static String setValue(String jsonStr, Object ... expressionValue) {
        if (expressionValue.length % 2 != 0) {
            throw new IllegalArgumentException("expressionValue length must be even");
        }
        Map<String, Object> paramMap = JsonUtils.parseToMap(jsonStr, String.class, Object.class);
        for (int i = 0; i < expressionValue.length; i += 2) {
            BeanUtil.setProperty(paramMap, expressionValue[i].toString(), expressionValue[i + 1]);
        }
        return objectToJson(paramMap);
    }


    /**
     *
     * @return
     */
    public static TypeFactory getTypeFactory() {
        return mapper.getTypeFactory();
    }

    /**
     *
     * @return
     */
    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

}
