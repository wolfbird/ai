package com.econage.ai.support.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public final class CollectionMore {

    private CollectionMore() {
    }

    /*
    * 切割列表，返回的数据应当只用来做读取使用
    * */
    public static <T> List<List<T>> splitCollection(List<T> unionList,int partitionSize){
        if(CollectionUtils.isEmpty(unionList)||partitionSize==0){
            return Collections.emptyList();
        }
        final List<List<T>> splitList = new ArrayList<>();

        if(partitionSize>=unionList.size()){
            splitList.add(unionList);
        }else{
            int idx = 0;
            while(idx<unionList.size()){
                int endIdx = idx+partitionSize;
                if(endIdx>unionList.size()){
                    endIdx = unionList.size();
                }
                splitList.add(unionList.subList(idx,endIdx));
                idx = endIdx;
            }
        }
        return splitList;
    }

    /*
    * 切割列表，返回的数据应当只用来做读取使用
    * */
    public static <T> List<List<T>> splitCollection(Collection<T> unionList,int partitionSize){
        if(CollectionUtils.isEmpty(unionList)){
            return Collections.emptyList();
        }
        return splitCollection(List.copyOf(unionList),partitionSize);
    }


    /*
    * 获取集合里的第一个元素
    * */
    public static <T> T fetchFirstByIterator(Collection<T> collection){
        if(CollectionUtils.isEmpty(collection)){
            return null;
        }
        if(collection instanceof List){
            return ((List<T>)collection).get(0);
        }
        for(var entry : collection){
            return entry;
        }
        return null;
    }

    /*
    * 字符集合没有成员，或者所有成员是空白
    * */
    public static boolean isEmptyOrBlankStr(Collection<String> strCollection){
        if(CollectionUtils.isEmpty(strCollection)){
            return true;
        }
        for(var str : strCollection){
            if(StringUtils.isNotBlank(str)){
                return false;
            }
        }
        return true;
    }

    public static <E> ArrayList<E> newArrayList(Collection<E> elements) {
        if(CollectionUtils.isEmpty(elements)){
            return new ArrayList<>();
        }else{
            return new ArrayList<>(elements);
        }
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        if(ArrayUtils.isEmpty(elements)){
            return new ArrayList<>();
        }
        ArrayList<E> list = new ArrayList<>(computeArrayListCapacity(elements.length));
        Collections.addAll(list, elements);
        return list;
    }

    public static <E> ArrayList<E> newArrayListWithExpectedSize(int estimatedSize) {
        return new ArrayList<>(computeArrayListCapacity(estimatedSize));
    }

    static int computeArrayListCapacity(int arraySize) {
        if (arraySize < 0) {
            throw new IllegalArgumentException("arraySize cannot be negative but was: " + arraySize);
        }
        return saturatedCast(5L + arraySize + (arraySize / 10));
    }

    private static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    public static <E> List<E> filter(List<E> unfiltered, Predicate<? super E> predicate) {
        if(CollectionUtils.isEmpty(unfiltered)){
            return new ArrayList<>();
        }
        Assert.notNull(predicate,"predicate is empty");
        return unfiltered.stream().filter(predicate).collect(Collectors.toList());
    }

    public static <E> Set<E> filter(Set<E> unfiltered, Predicate<? super E> predicate) {
        if(CollectionUtils.isEmpty(unfiltered)){
            return new HashSet<>();
        }
        Assert.notNull(predicate,"predicate is empty");
        return unfiltered.stream().filter(predicate).collect(Collectors.toSet());
    }

}
