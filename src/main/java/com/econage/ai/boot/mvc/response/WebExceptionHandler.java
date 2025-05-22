package com.econage.ai.boot.mvc.response;

import com.econage.ai.support.utils.ExceptionMore;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Web全局的的异常拦截器（拦截所有的控制器）（带有@RequestMapping注解的方法上都会拦截）
 */
@Slf4j
@Order(-1)
@RestControllerAdvice
public class WebExceptionHandler {


    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> throwableException(Throwable e) {
        log.error(ExceptionMore.gracefulTruncateExceptionStack(e));
        var exceptionMsg = e.getMessage();
        if(StringUtils.isEmpty(exceptionMsg)){
            //像是NullPointException通常内容是空的，此时格式化异常调用栈
            exceptionMsg = ExceptionMore.gracefulTruncateExceptionStack(e);
        }
        return createResponseEntity( exceptionMsg,HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> createResponseEntity(
            @NonNull String msg,
            @NonNull HttpStatus httpStatus
    ){
        return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(msg),httpStatus);
    }


}
