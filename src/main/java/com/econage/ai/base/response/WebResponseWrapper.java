package com.econage.ai.base.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/*
* 控制器应答包装类，附带状态信息，统一处理控制器外抛的异常
* */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebResponseWrapper<T> {

    public static final WebResponseWrapper EMPTY_SUCCESS = new WebResponseWrapper();

    /*
    * 标识是否成功
    * 通常code不是200就是有问题
    * 默认true
    * */
    @Builder.Default
    private boolean success = true;

    /*
    * 状态码，默认200
    * */
    @Builder.Default
    private int code = 200;

    /*
    * 消息
    * */
    @Builder.Default
    private String msg = StringUtils.EMPTY;

    /*
    * 应答体
    * */
    private T data;

    public static <O> WebResponseWrapper<O> wrapBody(O data){
        if(data==null){
            return EMPTY_SUCCESS;
        }
        else if(data instanceof WebResponseWrapper){
            return (WebResponseWrapper)data;
        }
        else {
            WebResponseWrapper<O> wrapper = new WebResponseWrapper<>();
            wrapper.setData(data);
            return wrapper;
        }
    }

}
