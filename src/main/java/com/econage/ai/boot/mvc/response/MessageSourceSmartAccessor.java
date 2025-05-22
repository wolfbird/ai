package com.econage.ai.boot.mvc.response;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.LocaleResolver;

/*
* security的过滤器解析locale信息
* security过滤器环境下，LocaleResolver还未解析国际化信息并存入LocaleContextHolder，但是可以获取request请求对象
* */
public class MessageSourceSmartAccessor extends MessageSourceAccessor {

    @Getter
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public MessageSourceSmartAccessor(MessageSource messageSource, LocaleResolver localeResolver) {
        super(messageSource);
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }

    public String getMessage(String code, String defaultMessage, HttpServletRequest request) {
        return super.getMessage(code, null, defaultMessage, localeResolver.resolveLocale(request));
    }

    public String getMessage(String code, @Nullable Object[] args, String defaultMessage, HttpServletRequest request) {
        return super.getMessage(code, args, defaultMessage, localeResolver.resolveLocale(request));
    }

    public String getMessage(String code,HttpServletRequest request) throws NoSuchMessageException {
        return super.getMessage(code,localeResolver.resolveLocale(request));
    }

    public String getMessage(String code, @Nullable Object[] args,HttpServletRequest request) throws NoSuchMessageException {
        return super.getMessage(code, args,localeResolver.resolveLocale(request));
    }

    public String getMessage(MessageSourceResolvable resolvable,HttpServletRequest request) throws NoSuchMessageException {
        return super.getMessage(resolvable,localeResolver.resolveLocale(request));
    }

}
