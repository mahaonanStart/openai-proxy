package com.mahaonan.gpt.proxy.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: M˚Haonan
 * @Date: 2020-06-17 16:07
 * @Description: map工具类
 */
public class MapUtils {

    /**
     * k,v 都为String的map按照指定的分隔符拼接存放在list中
     * @param map
     * @param delimiter
     * @return
     */
    public static List<String> mapToList(Map<String, String> map, String delimiter) {
        return mapToList(map, delimiter, k -> k);
    }


    /**
     * k,v 都为String的map按照指定的分隔符拼接存放在list中
     * v可以指定映射规则，转化为另外的字符串
     * @param map
     * @param delimiter
     * @param vFun
     * @return
     */
    public static List<String> mapToList(Map<String, String> map, String delimiter, Function<String, String> vFun) {
        List<String> res = new ArrayList<>();
        map.forEach((k, v) -> {
            res.add(k + delimiter + vFun.apply(v));
        });
        return res;
    }

    /**
     * 将map的value按照某种规则映射
     * @param map
     * @param vFun 映射规则
     * @return
     */
    public static <T, R> Map<T, R> mapValueConvert(Map<T, R> map, Function<R, R> vFun) {
        map.forEach((k, v) -> map.put(k, vFun.apply(v)));
        return map;
    }

    /**
     * 判断map为空
     * @param map
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> boolean isEmpty(Map<T, R> map) {
        return map == null || map.size() == 0;
    }

    /**
     * 判断map不为空
     * @param map
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> boolean isNotEmpty(Map<T, R> map) {
        return map != null && map.size() > 0;
    }
}
